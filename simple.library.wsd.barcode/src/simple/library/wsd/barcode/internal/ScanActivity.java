package simple.library.wsd.barcode.internal;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.Rect;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.OutputConfiguration;
import android.hardware.camera2.params.SessionConfiguration;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Size;
import android.view.Gravity;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.ReaderException;
import com.google.zxing.common.HybridBinarizer;
import simple.library.wsd.barcode.internal.zxing.camera.PlanarYUVLuminanceSource;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

/**
 * 两种条码扫描界面共享 Camera2 会话、亮度帧读取和前后台资源管理。
 *
 * <p>项目支持范围从 API 24 起，所有设备均可使用 Camera2；会话在 API 28 分流。
 * 解码在相机线程串行执行，界面和对外广播只由主线程更新。</p>
 *
 * @author 树先生 xhwsd@qq.com
 */
public class ScanActivity extends Activity implements SurfaceHolder.Callback {
	private final Handler ui = new Handler(Looper.getMainLooper());
	private HandlerThread thread;
	private Handler worker;
	private Preview preview;
	private TextView status;
	private Button flash;
	private Session current;
	private boolean active;
	private boolean destroyed;
	private boolean registered;
	private volatile boolean torch;
	private boolean continuous;
	private boolean beep;
	private int style;
	private int opening;
	private MediaPlayer player;
	private String previousText = "";
	private long previousResult;

	private final BroadcastReceiver stopReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if ((getPackageName() + ".barcode.STOP").equals(intent.getAction())) {
				finish();
			}
		}
	};

	@Override
	protected void onCreate(Bundle state) {
		super.onCreate(state);
		setTitle("二维码/条码扫描");
		style = getIntent().getIntExtra("style", 1);
		continuous = getIntent().getBooleanExtra("continuous", false);
		torch = getIntent().getBooleanExtra("flashlight", false);
		beep = getIntent().getBooleanExtra("beep", false);
		thread = new HandlerThread("Simple-BarcodeCamera");
		thread.start();
		worker = new Handler(thread.getLooper());

		FrameLayout root = new FrameLayout(this);
		root.setBackgroundColor(Color.BLACK);
		preview = new Preview();
		root.addView(preview, new FrameLayout.LayoutParams(-1, -1, Gravity.CENTER));
		root.addView(new Finder(), new FrameLayout.LayoutParams(-1, -1));
		LinearLayout controls = new LinearLayout(this);
		controls.setOrientation(LinearLayout.VERTICAL);
		controls.setGravity(Gravity.CENTER_HORIZONTAL);
		controls.setPadding(8, 8, 8, 8);
		LinearLayout actions = new LinearLayout(this);
		flash = new Button(this);
		flash.setText(torch ? "关闪光灯" : "开闪光灯");
		flash.setOnClickListener(view -> {
			torch = !torch;
			flash.setText(torch ? "关闪光灯" : "开闪光灯");
			Session owner = current;
			if (owner != null) {
				worker.post(owner::repeat);
			}
		});
		Button cancel = new Button(this);
		cancel.setText("取消扫描");
		cancel.setOnClickListener(view -> finish());
		actions.addView(flash);
		actions.addView(cancel);
		controls.addView(actions);
		status = new TextView(this);
		status.setTextColor(Color.WHITE);
		status.setText("请将二维码或条形码置于扫描框内");
		controls.addView(status);
		root.addView(controls, new FrameLayout.LayoutParams(-1, -2, Gravity.TOP));
		setContentView(root);

		if (beep) {
			int id = getResources().getIdentifier("beep", "raw", getPackageName());
			if (id != 0) {
				player = MediaPlayer.create(this, id);
			}
		}
		registerReceiver(stopReceiver, new IntentFilter(getPackageName() + ".barcode.STOP"));
		registered = true;
	}

	@Override
	protected void onResume() {
		super.onResume();
		active = true;
		openIfReady();
	}

	@Override
	protected void onPause() {
		active = false;
		closeSession();
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		destroyed = true;
		closeSession();
		if (registered) {
			unregisterReceiver(stopReceiver);
			registered = false;
		}
		if (player != null) {
			player.release();
			player = null;
		}
		worker.post(this::retireThread);
		super.onDestroy();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		openIfReady();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		closeSession();
	}

	/**
	 * 仅为前台、可用表面创建一个会话。
	 */
	private void openIfReady() {
		if (active && !destroyed && current == null && preview.getHolder().getSurface().isValid()) {
			current = new Session();
			worker.post(current::open);
		}
	}

	/**
	 * 先同步撤销所有权，迟到的打开和取帧回调不能重新启动相机。
	 */
	private void closeSession() {
		Session owner = current;
		current = null;
		if (owner != null) {
			owner.retired = true;
			worker.post(owner::close);
		}
	}

	/**
	 * 打开回调全部归还后才结束线程，避免销毁时丢失关闭迟到设备的机会。
	 */
	private void retireThread() {
		if (destroyed && opening == 0) {
			thread.quitSafely();
		}
	}

	/**
	 * 只由当前会话报告实际失败，返回调用窗口并保留错误信息。
	 *
	 * @param owner 失败会话
	 * @param message 错误说明
	 */
	private void fail(Session owner, String message) {
		ui.post(() -> {
			if (current == owner && !destroyed) {
				sendBroadcast(new Intent(getPackageName() + ".barcode.ERROR")
					.setPackage(getPackageName()).putExtra("message", message));
				finish();
			}
		});
	}

	/**
	 * 主线程发送识别结果；连续模式抑制同一内容的瞬时重复。
	 *
	 * @param owner 提供结果的会话
	 * @param text 实际识别文本
	 */
	private void result(Session owner, String text) {
		ui.post(() -> {
			if (current != owner || destroyed || text == null || text.isEmpty()) {
				return;
			}
			long now = SystemClock.elapsedRealtime();
			if (text.equals(previousText) && now - previousResult < 1000) {
				return;
			}
			previousText = text;
			previousResult = now;
			status.setText("识别结果：" + text);
			if (beep && player != null) {
				player.start();
			}
			sendBroadcast(new Intent(getPackageName() + ".barcode.RESULT")
				.setPackage(getPackageName()).putExtra("code", text));
			if (!continuous) {
				finish();
			}
		});
	}

	/**
	 * 扫描框在预览画面正中，两种样式分别采用方框和窄条框。
	 *
	 * @param width 画面宽度
	 * @param height 画面高度
	 * @return 当前坐标系中的扫描范围
	 */
	private Rect crop(int width, int height) {
		int w = Math.max(1, Math.min(width * 3 / 4, height * 3 / 4));
		int h = style == 2 ? Math.max(1, w / 2) : w;
		return new Rect((width - w) / 2, (height - h) / 2, (width + w) / 2, (height + h) / 2);
	}

	/**
	 * 保持预览比例，不把设备画面拉伸到窗口比例。
	 */
	private final class Preview extends SurfaceView {
		private int imageWidth = 3;
		private int imageHeight = 4;

		/**
		 * 创建带表面通知的预览视图。
		 */
		Preview() {
			super(ScanActivity.this);
			getHolder().addCallback(ScanActivity.this);
			setKeepScreenOn(true);
		}

		@Override
		protected void onMeasure(int widthSpec, int heightSpec) {
			int width = MeasureSpec.getSize(widthSpec);
			int height = MeasureSpec.getSize(heightSpec);
			float scale = Math.min(width / (float) imageWidth, height / (float) imageHeight);
			setMeasuredDimension(Math.max(1, Math.round(imageWidth * scale)), Math.max(1, Math.round(imageHeight * scale)));
		}
	}

	/**
	 * 显示与亮度裁剪区域一致的取景框。
	 */
	private final class Finder extends View {
		private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

		/**
		 * 创建不拦截触摸的透明覆盖层。
		 */
		Finder() {
			super(ScanActivity.this);
		}

		@Override
		protected void onDraw(Canvas canvas) {
			Rect rect = crop(preview.getWidth(), preview.getHeight());
			rect.offset(preview.getLeft(), preview.getTop());
			paint.setColor(style == 2 ? Color.GREEN : Color.CYAN);
			paint.setStyle(Paint.Style.STROKE);
			paint.setStrokeWidth(3 * getResources().getDisplayMetrics().density);
			canvas.drawRect(rect, paint);
		}
	}

	/**
	 * 会话原生对象只归相机线程使用，retired 由主线程立即撤销。
	 */
	private final class Session {
		private volatile boolean retired;
		private CameraDevice camera;
		private CameraCaptureSession session;
		private CaptureRequest.Builder request;
		private ImageReader reader;
		private net.sourceforge.zbar.ImageScanner nativeScanner;
		private final MultiFormatReader decoder = new MultiFormatReader();
		private int rotation;
		private boolean hasFlash;
		private int autofocus;
		private long lastFrame;
		private final Runnable focusTask = this::focusOnce;

		/**
		 * 检查能力并异步打开后置相机；没有后置相机时允许使用其它相机。
		 */
		void open() {
			try {
				if (retired) {
					return;
				}
				android.hardware.camera2.CameraManager manager =
					(android.hardware.camera2.CameraManager) getSystemService(Context.CAMERA_SERVICE);
				String selected = null;
				for (String id : manager.getCameraIdList()) {
					if (selected == null) {
						selected = id;
					}
					Integer facing = manager.getCameraCharacteristics(id).get(CameraCharacteristics.LENS_FACING);
					if (facing != null && facing == CameraCharacteristics.LENS_FACING_BACK) {
						selected = id;
						break;
					}
				}
				if (selected == null) {
					throw new IOException("设备没有可用摄像头");
				}
				CameraCharacteristics info = manager.getCameraCharacteristics(selected);
				StreamConfigurationMap map = info.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
				if (map == null) {
					throw new IOException("设备没有报告相机输出能力");
				}
				Size chosen = chooseSize(map);
				Integer sensor = info.get(CameraCharacteristics.SENSOR_ORIENTATION);
				int display = preview.getDisplay() == null ? 0 : preview.getDisplay().getRotation() * 90;
				Integer facing = info.get(CameraCharacteristics.LENS_FACING);
				int orientation = sensor == null ? 0 : sensor;
				rotation = facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT
					? (orientation + display) % 360 : (orientation - display + 360) % 360;
				hasFlash = Boolean.TRUE.equals(info.get(CameraCharacteristics.FLASH_INFO_AVAILABLE));
				int[] modes = info.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES);
				autofocus = CaptureRequest.CONTROL_AF_MODE_OFF;
				if (modes != null) {
					for (int mode : modes) {
						if (mode == CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE) {
							autofocus = mode;
							break;
						}
						if (mode == CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_VIDEO
							|| (autofocus == CaptureRequest.CONTROL_AF_MODE_OFF
								&& mode == CaptureRequest.CONTROL_AF_MODE_AUTO)) {
							autofocus = mode;
						}
					}
				}
				reader = ImageReader.newInstance(chosen.getWidth(), chosen.getHeight(), ImageFormat.YUV_420_888, 2);
				reader.setOnImageAvailableListener(this::frame, worker);
				if (style != 2) {
					System.loadLibrary("iconv");
					System.loadLibrary("ZBarDecoder");
					nativeScanner = new net.sourceforge.zbar.ImageScanner();
					nativeScanner.setConfig(0, 256, 3);
					nativeScanner.setConfig(0, 257, 3);
				}
				Hashtable<DecodeHintType, Object> hints = new Hashtable<>();
				hints.put(DecodeHintType.CHARACTER_SET, "UTF-8");
				decoder.setHints(hints);
				final String cameraId = selected;
				ui.post(() -> {
					if (current != this || retired) {
						return;
					}
					preview.imageWidth = rotation % 180 == 0 ? chosen.getWidth() : chosen.getHeight();
					preview.imageHeight = rotation % 180 == 0 ? chosen.getHeight() : chosen.getWidth();
					preview.getHolder().setFixedSize(chosen.getWidth(), chosen.getHeight());
					preview.requestLayout();
					flash.setEnabled(hasFlash);
					if (!hasFlash) {
						flash.setText("无闪光灯");
					}
					worker.post(() -> openDevice(manager, cameraId));
				});
			} catch (CameraAccessException | IOException | RuntimeException | LinkageError error) {
				fail(this, error.toString());
				close();
			}
		}

		/**
		 * 优先选择约 640×480 的共同输出，限制每帧解码开销。
		 *
		 * @param map 相机输出表
		 * @return 同时支持预览与 YUV 的尺寸
		 * @throws IOException 无共同尺寸
		 */
		private Size chooseSize(StreamConfigurationMap map) throws IOException {
			List<Size> previews = Arrays.asList(map.getOutputSizes(SurfaceHolder.class));
			Size chosen = null;
			for (Size size : map.getOutputSizes(ImageFormat.YUV_420_888)) {
				if (previews.contains(size) && size.getWidth() <= 1920 && size.getHeight() <= 1920
					&& (chosen == null || Math.abs(size.getWidth() * size.getHeight() - 307200)
						< Math.abs(chosen.getWidth() * chosen.getHeight() - 307200))) {
					chosen = size;
				}
			}
			if (chosen == null) {
				throw new IOException("设备不支持同时输出预览和亮度图像");
			}
			return chosen;
		}

		/**
		 * 按可用版本打开设备，打开完成后再次检查本轮所有权。
		 *
		 * @param manager 相机服务
		 * @param id 选择的设备编号
		 */
		private void openDevice(android.hardware.camera2.CameraManager manager, String id) {
			if (retired) {
				close();
				return;
			}
			CameraDevice.StateCallback callback = new CameraDevice.StateCallback() {
				private boolean pending = true;

				/**
				 * 打开计数只归还一次，已打开后的断连不再减少计数。
				 */
				private void completed() {
					if (pending) {
						pending = false;
						opening--;
					}
				}

				@Override
				public void onOpened(CameraDevice device) {
					completed();
					if (retired) {
						device.close();
					} else {
						camera = device;
						configure();
					}
					retireThread();
				}

				@Override
				public void onDisconnected(CameraDevice device) {
					completed();
					device.close();
					if (!retired) {
						fail(Session.this, "摄像头已断开");
					}
					retireThread();
				}

				@Override
				public void onError(CameraDevice device, int error) {
					completed();
					device.close();
					if (!retired) {
						fail(Session.this, "摄像头错误：" + error);
					}
					retireThread();
				}
			};
			try {
				opening++;
				if (Build.VERSION.SDK_INT >= 28) {
					manager.openCamera(id, command -> worker.post(command), callback);
				} else {
					manager.openCamera(id, callback, worker);
				}
			} catch (CameraAccessException | RuntimeException error) {
				opening--;
				fail(this, error.toString());
				retireThread();
			}
		}

		/**
		 * 建立预览与图像读取的双输出会话。
		 */
		private void configure() {
			try {
				List<Surface> outputs = Arrays.asList(preview.getHolder().getSurface(), reader.getSurface());
				request = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
				for (Surface surface : outputs) {
					request.addTarget(surface);
				}
				request.set(CaptureRequest.CONTROL_AF_MODE, autofocus);
				CameraCaptureSession.StateCallback callback = new CameraCaptureSession.StateCallback() {
					@Override
					public void onConfigured(CameraCaptureSession configured) {
						if (retired) {
							configured.close();
							return;
						}
						session = configured;
						repeat();
						worker.post(focusTask);
					}

					@Override
					public void onConfigureFailed(CameraCaptureSession failed) {
						failed.close();
						if (!retired) {
							fail(Session.this, "相机预览会话配置失败");
						}
					}
				};
				if (Build.VERSION.SDK_INT >= 28) {
					createModernSession(outputs, callback);
				} else {
					createLegacySession(outputs, callback);
				}
			} catch (CameraAccessException | RuntimeException error) {
				fail(this, error.toString());
			}
		}

		/**
		 * API 28 起使用会话配置对象。
		 *
		 * @param surfaces 预览与图像输出
		 * @param callback 会话结果
		 * @throws CameraAccessException 相机不可用
		 */
		private void createModernSession(List<Surface> surfaces, CameraCaptureSession.StateCallback callback)
				throws CameraAccessException {
			List<OutputConfiguration> outputs = new ArrayList<>();
			for (Surface surface : surfaces) {
				outputs.add(new OutputConfiguration(surface));
			}
			camera.createCaptureSession(new SessionConfiguration(SessionConfiguration.SESSION_REGULAR,
				outputs, command -> worker.post(command), callback));
		}

		/**
		 * API 24 至 27 没有 SessionConfiguration，保留实际可用的会话入口。
		 *
		 * @param surfaces 预览与图像输出
		 * @param callback 会话结果
		 * @throws CameraAccessException 相机不可用
		 */
		@SuppressWarnings("deprecation")
		private void createLegacySession(List<Surface> surfaces, CameraCaptureSession.StateCallback callback)
				throws CameraAccessException {
			camera.createCaptureSession(surfaces, callback, worker);
		}

		/**
		 * 更新预览及补光，不依赖固定延时猜测相机是否打开。
		 */
		void repeat() {
			if (retired || session == null) {
				return;
			}
			try {
				request.set(CaptureRequest.FLASH_MODE,
					hasFlash && torch ? CaptureRequest.FLASH_MODE_TORCH : CaptureRequest.FLASH_MODE_OFF);
				session.setRepeatingRequest(request.build(), null, worker);
			} catch (CameraAccessException | RuntimeException error) {
				fail(this, error.toString());
			}
		}

		/**
		 * 仅在不支持连续对焦时触发单次对焦，保持移动扫码时仍能重新合焦。
		 */
		private void focusOnce() {
			if (retired || session == null || autofocus != CaptureRequest.CONTROL_AF_MODE_AUTO) {
				return;
			}
			try {
				// 先解除上次锁定，再发出单次触发；不把 START 放进重复预览请求。
				request.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_CANCEL);
				session.capture(request.build(), null, worker);
				request.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_START);
				session.capture(request.build(), null, worker);
				request.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_IDLE);
				worker.postDelayed(focusTask, 1500);
			} catch (CameraAccessException | RuntimeException error) {
				fail(this, error.toString());
			}
		}

		/**
		 * 读取 Y 平面的真实行距，关闭图像后再解码，避免阻塞缓冲队列。
		 *
		 * @param source 当前会话图像读取器
		 */
		private void frame(ImageReader source) {
			try {
				byte[] y;
				int width;
				int height;
				try (Image image = source.acquireLatestImage()) {
					if (image == null || retired || SystemClock.elapsedRealtime() - lastFrame < 120) {
						return;
					}
					lastFrame = SystemClock.elapsedRealtime();
					width = image.getWidth();
					height = image.getHeight();
					y = new byte[width * height];
					Image.Plane plane = image.getPlanes()[0];
					ByteBuffer bytes = plane.getBuffer();
					int offset = bytes.position();
					for (int row = 0; row < height; row++) {
						for (int column = 0; column < width; column++) {
							y[row * width + column] = bytes.get(offset + row * plane.getRowStride()
								+ column * plane.getPixelStride());
						}
					}
				}
				int rotatedWidth = rotation % 180 == 0 ? width : height;
				int rotatedHeight = rotation % 180 == 0 ? height : width;
				byte[] rotated = new byte[y.length];
				for (int row = 0; row < height; row++) {
					for (int column = 0; column < width; column++) {
						int x = column;
						int v = row;
						if (rotation == 90) {
							x = height - row - 1;
							v = column;
						} else if (rotation == 180) {
							x = width - column - 1;
							v = height - row - 1;
						} else if (rotation == 270) {
							x = row;
							v = width - column - 1;
						}
						rotated[v * rotatedWidth + x] = y[row * width + column];
					}
				}
				Rect crop = crop(rotatedWidth, rotatedHeight);
				if (nativeScanner != null) {
					net.sourceforge.zbar.Image image = new net.sourceforge.zbar.Image(rotatedWidth, rotatedHeight, "Y800");
					try {
						image.setData(rotated);
						image.setCrop(crop.left, crop.top, crop.width(), crop.height());
						if (nativeScanner.scanImage(image) != 0) {
							for (net.sourceforge.zbar.Symbol symbol : nativeScanner.getResults()) {
								result(this, symbol.getData());
								break;
							}
						}
					} finally {
						image.destroy();
					}
				} else {
					PlanarYUVLuminanceSource luminance = new PlanarYUVLuminanceSource(rotated, rotatedWidth,
						rotatedHeight, crop.left, crop.top, crop.width(), crop.height());
					try {
						result(this, decoder.decodeWithState(new BinaryBitmap(new HybridBinarizer(luminance))).getText());
					} catch (ReaderException noCode) {
						// 未识别到内容时等待下一帧，不把正常空帧当作组件错误。
					} finally {
						decoder.reset();
					}
				}
			} catch (RuntimeException error) {
				if (!retired) {
					fail(this, error.toString());
				}
			}
		}

		/**
		 * 释放当前会话取得的全部原生资源，可重复调用。
		 */
		void close() {
			worker.removeCallbacks(focusTask);
			if (session != null) {
				session.close();
				session = null;
			}
			if (camera != null) {
				camera.close();
				camera = null;
			}
			if (reader != null) {
				reader.close();
				reader = null;
			}
			if (nativeScanner != null) {
				nativeScanner.destroy();
				nativeScanner = null;
			}
			decoder.reset();
		}
	}
}
