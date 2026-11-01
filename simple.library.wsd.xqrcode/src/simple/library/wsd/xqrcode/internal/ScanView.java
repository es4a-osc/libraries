package simple.library.wsd.xqrcode.internal;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.*;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.*;
import android.view.*;
import android.widget.FrameLayout;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import simple.library.wsd.xqrcode.*;
import simple.library.wsd.xqrcode.internal.zxing.*;

/**
 * 同时服务独立扫码页和 Simple 扫码框的单会话预览。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class ScanView extends FrameLayout implements SurfaceHolder.Callback {
	// 驱动打开或释放可能阻塞；串行放在后台，避免拖住主线程和并发打开设备。
	private static final ExecutorService DEVICES = Executors.newSingleThreadExecutor(new ThreadFactory() {
		@Override
		public Thread newThread(Runnable task) {
			Thread thread = new Thread(task, "ES4A-XQRCode-camera");
			thread.setDaemon(true);
			return thread;
		}
	});
	private static final AtomicInteger PERMISSIONS = new AtomicInteger(31000);
	private final Activity activity;
	private final SurfaceView preview;
	private final Overlay overlay;
	private final Handler handler = new Handler(Looper.getMainLooper());
	private final ExecutorService executor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(1));
	private final LegacyCamera camera = new LegacyCamera();
	private final int permissionCode = PERMISSIONS.getAndIncrement();
	private Listener listener;
	private 扫码配置 options = new 扫码配置();
	private boolean desired;
	private boolean resumed = true;
	private boolean paused;
	private boolean surface;
	private boolean active;
	private boolean opening;
	private Future<?> openingTask;
	private Runnable openingTimeout;
	private boolean closed;
	private boolean light;
	private boolean permissionPending;
	private long generation;
	private String state = "未开始";
	private MediaPlayer player;
	private final Runnable nextFrame = new Runnable() {
		@Override
		public void run() {
			requestFrame();
		}
	};
	private final Runnable nextFocus = new Runnable() {
		@Override
		public void run() {
			if (active) {
				camera.focus();
				handler.postDelayed(this, options.对焦间隔());
			}
		}
	};

	/**
	 * 创建可嵌入普通 Activity 的预览与覆盖层。
	 *
	 * @param context 所属活动。
	 */
	public ScanView(Activity context) {
		super(context);
		activity = context;
		setBackgroundColor(Color.BLACK);
		setClipChildren(true);
		preview = new SurfaceView(context);
		addView(preview, new LayoutParams(-1, -1, Gravity.CENTER));
		overlay = new Overlay(context);
		addView(overlay, new LayoutParams(-1, -1));
		preview.getHolder().addCallback(this);
	}

	/**
	 * 安装会话事件接收者。
	 *
	 * @param value 本实例接收者。
	 */
	public void listener(Listener value) {
		listener = value;
	}

	/**
	 * 提交独立扫描配置，已有活动会话时拒绝覆盖。
	 *
	 * @param config 调用方配置。
	 * @return 接受本次请求时为真，不代表已取得权限或解码成功。
	 */
	public boolean start(扫码配置 config) {
		if (closed || desired || config == null) {
			return false;
		}
		options = config.copy();
		generation++;
		paused = false;
		desired = true;
		overlay.points = new float[0];
		try {
			overlay.loadImage();
			attempt();
		} catch (Exception error) {
			fail(error);
		}
		return true;
	}

	/**
	 * 暂停或继续当前请求，暂停期间释放相机。
	 *
	 * @param value 是否暂停。
	 * @return 存在可控制的会话时为真。
	 */
	public boolean pause(boolean value) {
		if (!desired || closed) {
			return false;
		}
		paused = value;
		if (value) {
			release();
			state("已暂停");
		} else {
			attempt();
		}
		return true;
	}

	/**
	 * 接入活动前后台状态；后台不保留设备。
	 *
	 * @param value 活动是否处于前台。
	 */
	public void resumed(boolean value) {
		resumed = value;
		if (!value) {
			release();
			if (desired) {
				state("已暂停");
			}
		} else {
			attempt();
		}
	}

	/**
	 * 停止扫描并取消本次会话。
	 *
	 * @param notify 是否通知用户主动取消。
	 * @return 曾存在待处理会话时为真。
	 */
	public boolean stop(boolean notify) {
		boolean existed = desired;
		desired = false;
		release();
		state("未开始");
		if (existed && notify && listener != null) {
			listener.cancelled();
		}
		return existed;
	}

	/**
	 * 永久释放线程、图像、声音和相机，不再派发事件。
	 */
	public void close() {
		if (!closed) {
			listener = null;
			closed = true;
			stop(false);
			executor.shutdownNow();
			overlay.dispose();
			if (player != null) {
				player.release();
				player = null;
			}
		}
	}

	/**
	 * 处理本预览发起的相机授权结果。
	 *
	 * @param requestCode 本次请求编号。
	 */
	public void permission(int requestCode) {
		if (requestCode == permissionCode) {
			permissionPending = false;
			if (desired && activity.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
				fail(new SecurityException("没有相机权限，请授权后重新开始"));
			} else {
				attempt();
			}
		}
	}

	/**
	 * 读取当前可见的会话状态。
	 *
	 * @return 中文状态名。
	 */
	public String state() {
		return state;
	}

	/**
	 * 读取相机是否正在取帧。
	 *
	 * @return 真实预览已启动时为真。
	 */
	public boolean active() {
		return active;
	}

	/**
	 * 读取当前会话的配置副本。
	 *
	 * @return 可独立修改的快照。
	 */
	public 扫码配置 configuration() {
		return options.copy();
	}

	/**
	 * 修改已打开设备的补光状态。
	 *
	 * @param value 是否开启。
	 * @return 设备支持且设置成功时为真；失败会报告原因。
	 */
	public boolean torch(boolean value) {
		if (!active || !camera.torch(value)) {
			if (listener != null) {
				listener.problem("当前相机未开启或不支持所请求的补光模式");
			}
			return false;
		}
		light = value;
		return true;
	}

	/**
	 * 读取本次真实设置的补光状态。
	 *
	 * @return 已成功开启补光时为真。
	 */
	public boolean light() {
		return light;
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		surface = true;
		attempt();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		if (active) {
			release();
		}
		attempt();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		surface = false;
		release();
		if (desired) {
			state("等待界面");
		}
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		attempt();
	}

	@Override
	protected void onDetachedFromWindow() {
		stop(true);
		super.onDetachedFromWindow();
	}

	@Override
	protected void onWindowVisibilityChanged(int visibility) {
		super.onWindowVisibilityChanged(visibility);
		if (camera != null) {
			if (visibility != VISIBLE) {
				release();
			} else {
				attempt();
			}
		}
	}

	/**
	 * 在权限、表面及前台条件均满足时打开真实设备。
	 */
	private void attempt() {
		if (!desired || closed || active || opening) {
			return;
		}
		if (paused || !resumed) {
			state("已暂停");
			return;
		}
		if (!isAttachedToWindow() || !surface || getWidth() == 0 || getHeight() == 0 || getWindowVisibility() != VISIBLE) {
			state("等待界面");
			return;
		}
		if (activity.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
			state("等待权限");
			if (!desired || closed || paused) {
				return;
			}
			if (!permissionPending) {
				permissionPending = true;
				activity.requestPermissions(new String[] {Manifest.permission.CAMERA}, permissionCode);
			}
			return;
		}
		opening = true;
		final long token = generation;
		final int cameraId = options.相机编号();
		state("正在打开相机");
		if (!opening || !desired || generation != token || closed) {
			return;
		}
		openingTimeout = new Runnable() {
			@Override
			public void run() {
				if (opening && generation == token && !closed) {
					fail(new IllegalStateException("打开相机超时，相机服务可能被占用或无响应"));
				}
			}
		};
		handler.postDelayed(openingTimeout, 10000);
		openingTask = DEVICES.submit(new Runnable() {
			@Override
			public void run() {
				Exception failure = null;
				try {
					camera.open(activity, preview.getHolder(), cameraId, new LegacyCamera.ErrorListener() {
						@Override
						public void failed(final String reason) {
							handler.post(new Runnable() {
								@Override
								public void run() {
									if (generation == token && desired && !closed) {
										fail(new IllegalStateException(reason));
									}
								}
							});
						}
					});
				} catch (Exception error) {
					failure = error;
				}
				final Exception error = failure;
				handler.post(new Runnable() {
					@Override
					public void run() {
						if (generation != token || closed || !desired) {
							return;
						}
						opening = false;
						handler.removeCallbacks(openingTimeout);
						openingTimeout = null;
						openingTask = null;
						if (error != null) {
							fail(error);
							return;
						}
						int width = camera.rotation % 180 == 0 ? camera.width : camera.height;
						int height = camera.rotation % 180 == 0 ? camera.height : camera.width;
						float scale = Math.max((float) getWidth() / width, (float) getHeight() / height);
						preview.setLayoutParams(new LayoutParams((int) Math.ceil(width * scale), (int) Math.ceil(height * scale), Gravity.CENTER));
						active = true;
						setKeepScreenOn(options.保持亮屏());
						state("正在扫描");
						if (generation != token || !active) {
							return;
						}
						if (options.初始补光()) {
							torch(true);
						}
						if (listener != null && generation == token && active) {
							listener.ready();
						}
						handler.post(nextFocus);
						handler.post(nextFrame);
						overlay.invalidate();
					}
				});
			}
		});
	}

	/**
	 * 使所有在途帧失效并释放占用。
	 */
	private void release() {
		generation++;
		active = false;
		light = false;
		handler.removeCallbacks(nextFrame);
		handler.removeCallbacks(nextFocus);
		opening = false;
		if (openingTimeout != null) {
			handler.removeCallbacks(openingTimeout);
			openingTimeout = null;
		}
		if (openingTask != null) {
			openingTask.cancel(false);
			openingTask = null;
		}
		closeCamera(camera);
		setKeepScreenOn(false);
		if (overlay != null) {
			overlay.invalidate();
		}
	}

	/**
	 * 在后台释放设备，排队任务不持有视图及活动。
	 *
	 * @param target 待释放的兼容会话。
	 */
	private static void closeCamera(final LegacyCamera target) {
		DEVICES.execute(new Runnable() {
			@Override
			public void run() {
				target.close();
			}
		});
	}

	/**
	 * 发出终止失败，重新开始才能恢复。
	 *
	 * @param error 设备或输入错误。
	 */
	private void fail(Exception error) {
		desired = false;
		release();
		long token = generation;
		state("失败");
		if (!closed && listener != null && generation == token) {
			listener.problem(error.getMessage() == null ? error.getClass().getSimpleName() : error.getMessage());
		}
	}

	/**
	 * 仅在实际变化时报告状态。
	 *
	 * @param value 新状态。
	 */
	private void state(String value) {
		if (!state.equals(value)) {
			String previous = state;
			state = value;
			if (!closed && listener != null) {
				listener.changed(previous, value);
			}
		}
	}

	/**
	 * 请求一帧并按当时的区域快照异步解码，始终只保留一个在途帧。
	 */
	private void requestFrame() {
		if (!active || closed) {
			return;
		}
		final long token = generation;
		try {
			camera.frame(new LegacyCamera.FrameListener() {
				@Override
				public void received(byte[] bytes) {
					if (!active || generation != token || bytes == null) {
						return;
					}
					final byte[] frame = bytes.clone();
					final 扫码配置 decodeOptions = options.copy();
					final int width = camera.width;
					final int height = camera.height;
					final int angle = camera.rotation;
					final boolean mirror = camera.mirror;
					final int orientedWidth = angle % 180 == 0 ? width : height;
					final int orientedHeight = angle % 180 == 0 ? height : width;
					RectF frameRect = options.仅识别框内() ? overlay.frame() : new RectF(0, 0, getWidth(), getHeight());
					final int[] crop = FrameGeometry.crop(orientedWidth, orientedHeight, getWidth(), getHeight(), frameRect.left, frameRect.top, frameRect.right, frameRect.bottom);
					if (mirror) {
						// 前摄仅显示镜像；解码保持原码方向，避免镜像的数据矩阵无法识别。
						crop[0] = orientedWidth - crop[0] - crop[2];
					}
					try {
						executor.execute(new Runnable() {
							@Override
							public void run() {
								识别结果 found = null;
								Exception failure = null;
								try {
									byte[] oriented = FrameGeometry.orient(frame, width, height, angle, false);
									PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(oriented, orientedWidth, orientedHeight, crop[0], crop[1], crop[2], crop[3], false);
									Result result = Images.decode(source, decodeOptions);
									byte[] png = new byte[0];
									if (decodeOptions.返回识别图像()) {
										byte[] luminance = source.getMatrix();
										int[] pixels = new int[luminance.length];
										for (int i = 0; i < pixels.length; i++) {
											int gray = luminance[i] & 255;
											pixels[i] = 0xff000000 | gray * 0x010101;
										}
										Bitmap image = Bitmap.createBitmap(pixels, crop[2], crop[3], Bitmap.Config.ARGB_8888);
										try {
											png = Images.png(image);
										} finally {
											image.recycle();
										}
									}
									found = 识别结果.from(result, png);
								} catch (NotFoundException ignored) {
									// 普通无结果帧继续取下一帧。
								} catch (Exception error) {
									failure = error;
								} catch (OutOfMemoryError error) {
									failure = new IllegalStateException("扫描图像内存不足", error);
								}
								final 识别结果 result = found;
								final Exception error = failure;
								handler.post(new Runnable() {
									@Override
									public void run() {
										if (!active || generation != token || closed) {
											return;
										}
										if (error != null) {
											fail(error);
										} else if (result == null) {
											handler.postDelayed(nextFrame, 50);
										} else {
											overlay.result(result, crop, orientedWidth, orientedHeight, mirror);
											feedback();
											if (!active || generation != token || closed) {
												return;
											}
											boolean continuous = options.连续扫描();
											if (!continuous) {
												desired = false;
												release();
											}
											long resultGeneration = generation;
											if (!continuous) {
												state("已完成");
											}
											if (listener != null && !closed && generation == resultGeneration) {
												listener.result(result);
											}
											if (continuous && active && generation == token) {
												handler.postDelayed(nextFrame, options.连续间隔());
											}
										}
									}
								});
							}
						});
					} catch (RejectedExecutionException error) {
						if (!closed) {
							fail(error);
						}
					}
				}
			});
		} catch (RuntimeException error) {
			fail(error);
		}
	}

	/**
	 * 按会话设置播放提示音并振动；静音模式不播放声音。
	 */
	private void feedback() {
		try {
			AudioManager audio = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
			if (options.提示音() && audio != null && audio.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
				if (player == null) {
					int resource = activity.getResources().getIdentifier("xqrcode_beep", "raw", activity.getPackageName());
					if (resource != 0) {
						player = MediaPlayer.create(activity, resource);
					}
				}
				if (player != null) {
					player.start();
				}
			}
			if (options.振动() && options.振动时长() > 0) {
				Vibrator vibrator = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
				if (vibrator != null && vibrator.hasVibrator()) {
					if (Build.VERSION.SDK_INT >= 26) {
						vibrator.vibrate(VibrationEffect.createOneShot(options.振动时长(), VibrationEffect.DEFAULT_AMPLITUDE));
					} else {
						legacyVibrate(vibrator, options.振动时长());
					}
				}
			}
		} catch (RuntimeException error) {
			if (listener != null && !closed) {
				listener.problem("提示音或振动不可用：" + error.getMessage());
			}
		}
	}

	/**
	 * 在 API 24、25 上执行实际振动。
	 *
	 * @param vibrator 系统振动器。
	 * @param duration 振动毫秒数。
	 */
	@SuppressWarnings("deprecation")
	private static void legacyVibrate(Vibrator vibrator, int duration) {
		// VibrationEffect 从 API 26 才可用，旧系统必须保留此调用。
		vibrator.vibrate(duration);
	}

	/**
	 * 扫码会话事件。
	 */
	public interface Listener {
		/**
		 * 通知相机已打开。
		 */
		void ready();

		/**
		 * 通知识别结果。
		 *
		 * @param result 独立结果。
		 */
		void result(识别结果 result);

		/**
		 * 通知设备或权限问题，终止问题使状态变为失败，补光问题不终止扫描。
		 *
		 * @param reason 原因。
		 */
		void problem(String reason);

		/**
		 * 通知本次会话已取消。
		 */
		void cancelled();

		/**
		 * 通知状态改变。
		 *
		 * @param oldState 原状态。
		 * @param newState 新状态。
		 */
		void changed(String oldState, String newState);
	}

	/**
	 * 按预览自身尺寸绘制扫描框，避免依赖全屏静态矩形。
	 */
	private final class Overlay extends View {
		private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		private float position;
		private float[] points = new float[0];
		private Bitmap image;

		/**
		 * 创建覆盖层。
		 *
		 * @param context 所属活动。
		 */
		Overlay(Context context) {
			super(context);
		}

		/**
		 * 释放旧扫描图片后读取本次图片。
		 *
		 * @throws Exception 图像不可读。
		 */
		void loadImage() throws Exception {
			dispose();
			if (!options.扫描图像().isEmpty()) {
				image = Images.bitmap(Images.read(activity, options.扫描图像()));
			}
			position = 0;
			invalidate();
		}

		/**
		 * 释放覆盖层位图。
		 */
		void dispose() {
			if (image != null) {
				image.recycle();
				image = null;
			}
		}

		/**
		 * 计算本视图范围内的扫描区域。
		 *
		 * @return 完全位于预览内部的矩形。
		 */
		RectF frame() {
			float density = getResources().getDisplayMetrics().density;
			float width = options.扫描框宽度() == 0 ? getWidth() * 0.72f : options.扫描框宽度() * density;
			float height = options.扫描框高度() == 0 ? Math.min(width, getHeight() * 0.65f) : options.扫描框高度() * density;
			width = Math.max(1, Math.min(width, getWidth()));
			height = Math.max(1, Math.min(height, getHeight()));
			float left = (getWidth() - width) / 2;
			float top = options.顶部偏移() < 0 ? (getHeight() - height) / 2 : options.顶部偏移() * density;
			top = Math.max(0, Math.min(top, getHeight() - height));
			return new RectF(left, top, left + width, top + height);
		}

		/**
		 * 将识别图像坐标转换为覆盖层坐标。
		 *
		 * @param result 识别结果。
		 * @param crop 裁剪区域。
		 * @param width 全预览图像宽。
		 * @param height 全预览图像高。
		 * @param mirror 预览是否水平镜像。
		 */
		void result(识别结果 result, int[] crop, int width, int height, boolean mirror) {
			points = result.识别点();
			float scale = Math.max((float) getWidth() / width, (float) getHeight() / height);
			for (int i = 0; i < points.length; i += 2) {
				float x = points[i] + crop[0];
				points[i] = (mirror ? width - 1 - x : x) * scale + (getWidth() - width * scale) / 2;
				points[i + 1] = (points[i + 1] + crop[1]) * scale + (getHeight() - height * scale) / 2;
			}
			invalidate();
		}

		@Override
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);
			RectF frame = frame();
			float density = getResources().getDisplayMetrics().density;
			paint.setStyle(Paint.Style.FILL);
			paint.setColor("已完成".equals(state) ? options.结果遮罩颜色() : options.遮罩颜色());
			canvas.drawRect(0, 0, getWidth(), frame.top, paint);
			canvas.drawRect(0, frame.bottom, getWidth(), getHeight(), paint);
			canvas.drawRect(0, frame.top, frame.left, frame.bottom, paint);
			canvas.drawRect(frame.right, frame.top, getWidth(), frame.bottom, paint);
			paint.setColor(options.边角颜色());
			paint.setStrokeWidth(options.边角线宽() * density);
			float length = Math.min(options.边角长度() * density, Math.min(frame.width(), frame.height()) / 2);
			for (int x = 0; x < 2; x++) {
				for (int y = 0; y < 2; y++) {
					float px = x == 0 ? frame.left : frame.right;
					float py = y == 0 ? frame.top : frame.bottom;
					canvas.drawLine(px, py, px + (x == 0 ? length : -length), py, paint);
					canvas.drawLine(px, py, px, py + (y == 0 ? length : -length), paint);
				}
			}
			if (active) {
				position = (position + options.扫描速度() * density) % Math.max(1, frame.height());
				float top = frame.top + position;
				if (image == null) {
					canvas.drawLine(frame.left, top, frame.right, top, paint);
				} else {
					if (options.扫描图着色() != -1) {
						paint.setColorFilter(new PorterDuffColorFilter(options.扫描图着色(), PorterDuff.Mode.SRC_IN));
					}
					float height = frame.width() * image.getHeight() / image.getWidth();
					canvas.save();
					canvas.clipRect(frame);
					canvas.drawBitmap(image, null, new RectF(frame.left, top, frame.right, top + height), paint);
					canvas.restore();
					paint.setColorFilter(null);
				}
				postInvalidateDelayed(options.动画间隔());
			}
			if (options.显示结果点()) {
				paint.setColor(options.识别点颜色());
				for (int i = 0; i < points.length; i += 2) {
					canvas.drawCircle(points[i], points[i + 1], 4 * density, paint);
				}
			}
		}
	}
}
