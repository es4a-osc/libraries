package simple.library.wsd.xqrcode.internal;

import android.app.Activity;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.view.Surface;
import android.view.SurfaceHolder;
import java.util.List;

/**
 * 为 XQRCode 的 NV21 解码链保存旧相机兼容实现，一次只允许一个真实预览。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class LegacyCamera {
	private static LegacyCamera owner;
	// NV21 单帧接口依赖 Camera；这里保留上游相机协议，所有调用在本兼容层内。
	@SuppressWarnings("deprecation")
	private volatile Camera camera;
	public int width;
	public int height;
	public int rotation;
	public boolean mirror;

	/**
	 * 创建尚未打开设备的会话。
	 */
	public LegacyCamera() {
	}

	/**
	 * 打开相机并建立 Surface 预览。
	 *
	 * @param activity 当前活动。
	 * @param holder 已就绪的预览表面。
	 * @param requestedCamera 设备编号；-1 优先后摄。
	 * @param listener 驱动断开或预览错误的接收者。
	 * @throws Exception 无可用设备、被其它会话占用或预览启动失败。
	 */
	@SuppressWarnings("deprecation")
	public void open(Activity activity, SurfaceHolder holder, int requestedCamera, final ErrorListener listener) throws Exception {
		// API 24 起仍需该兼容链提供上游使用的同步 NV21 帧，弃用调用集中于此。
		synchronized (LegacyCamera.class) {
			if (owner != null && owner != this) {
				throw new IllegalStateException("另一个扫码会话正在占用相机");
			}
			owner = this;
		}
		try {
			int count = Camera.getNumberOfCameras();
			if (count == 0) {
				throw new IllegalStateException("设备没有可用相机");
			}
			int id = requestedCamera;
			Camera.CameraInfo info = new Camera.CameraInfo();
			if (id < 0) {
				id = 0;
				for (int i = 0; i < count; i++) {
					Camera.getCameraInfo(i, info);
					if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
						id = i;
						break;
					}
				}
			}
			if (id >= count) {
				throw new IllegalArgumentException("相机编号不存在：" + id);
			}
			Camera.getCameraInfo(id, info);
			mirror = info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT;
			int device = activity.getWindowManager().getDefaultDisplay().getRotation();
			int degrees = device == Surface.ROTATION_90 ? 90 : device == Surface.ROTATION_180 ? 180 : device == Surface.ROTATION_270 ? 270 : 0;
			rotation = mirror ? (info.orientation + degrees) % 360 : (info.orientation - degrees + 360) % 360;
			camera = Camera.open(id);
			camera.setErrorCallback(new Camera.ErrorCallback() {
				@Override
				public void onError(int code, Camera source) {
					listener.failed("相机驱动报告错误：" + code);
				}
			});
			Camera.Parameters parameters = camera.getParameters();
			Camera.Size best = null;
			for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
				if (size.width * size.height <= 1280 * 960 && (best == null || size.width * size.height > best.width * best.height)) {
					best = size;
				}
			}
			if (best != null) {
				parameters.setPreviewSize(best.width, best.height);
			}
			if (!parameters.getSupportedPreviewFormats().contains(ImageFormat.NV21)) {
				throw new IllegalStateException("相机不支持 NV21 预览");
			}
			parameters.setPreviewFormat(ImageFormat.NV21);
			List<String> modes = parameters.getSupportedFocusModes();
			if (modes != null && modes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
				parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
			}
			camera.setParameters(parameters);
			Camera.Size actual = camera.getParameters().getPreviewSize();
			width = actual.width;
			height = actual.height;
			camera.setDisplayOrientation(mirror ? (360 - rotation) % 360 : rotation);
			camera.setPreviewDisplay(holder);
			camera.startPreview();
		} catch (Exception error) {
			close();
			throw error;
		}
	}

	/**
	 * 请求一帧，由界面层复制后交给解码线程。
	 *
	 * @param listener 帧接收者。
	 */
	@SuppressWarnings("deprecation")
	public void frame(final FrameListener listener) {
		if (camera != null) {
			camera.setOneShotPreviewCallback(new Camera.PreviewCallback() {
				@Override
				public void onPreviewFrame(byte[] bytes, Camera source) {
					listener.received(bytes);
				}
			});
		}
	}

	/**
	 * 调整真实闪光灯模式。
	 *
	 * @param enabled 是否开启常亮补光。
	 * @return 硬件支持且设置成功时为真。
	 */
	@SuppressWarnings("deprecation")
	public boolean torch(boolean enabled) {
		if (camera == null) {
			return false;
		}
		try {
			Camera.Parameters parameters = camera.getParameters();
			String mode = enabled ? Camera.Parameters.FLASH_MODE_TORCH : Camera.Parameters.FLASH_MODE_OFF;
			List<String> modes = parameters.getSupportedFlashModes();
			if (modes == null || !modes.contains(mode)) {
				return false;
			}
			parameters.setFlashMode(mode);
			camera.setParameters(parameters);
			return mode.equals(camera.getParameters().getFlashMode());
		} catch (RuntimeException error) {
			return false;
		}
	}

	/**
	 * 触发支持自动对焦的设备对焦，不支持时保留原焦距。
	 */
	public void focus() {
		requestFocus(null);
	}

	/**
	 * 请求一次真实对焦。
	 *
	 * @param listener 对焦完成监听；空表示仅发起请求。
	 * @return 驱动已接受请求时为真，不代表已经对焦成功。
	 */
	@SuppressWarnings("deprecation")
	public boolean requestFocus(final FocusListener listener) {
		if (camera == null) {
			return false;
		}
		try {
			if (!Camera.Parameters.FOCUS_MODE_AUTO.equals(camera.getParameters().getFocusMode())) {
				return false;
			}
			camera.autoFocus(
					new Camera.AutoFocusCallback() {
						@Override
						public void onAutoFocus(boolean success, Camera source) {
							if (listener != null) {
								listener.completed(success);
							}
						}
					});
			return true;
		} catch (RuntimeException error) {
			return false;
		}
	}

	/**
	 * 真实设备对焦完成通知。
	 */
	public interface FocusListener {
		/**
		 * 接收驱动报告的对焦结果。
		 *
		 * @param success 实际对焦是否成功。
		 */
		void completed(boolean success);
	}

	/**
	 * 释放预览及设备占用，重复调用安全。
	 */
	@SuppressWarnings("deprecation")
	public void close() {
		if (camera != null) {
			try {
				camera.setOneShotPreviewCallback(null);
				camera.stopPreview();
			} catch (RuntimeException ignored) {
				// 设备已被系统断开时仍须释放句柄。
			} finally {
				try {
					camera.release();
				} catch (RuntimeException ignored) {
					// 服务进程已断开时仍清理 Java 会话和全局占用。
				} finally {
					camera = null;
				}
			}
		}
		synchronized (LegacyCamera.class) {
			if (owner == this) {
				owner = null;
			}
		}
	}

	/**
	 * 驱动错误接收边界。
	 */
	public interface ErrorListener {
		/**
		 * 接收设备断开或驱动错误。
		 *
		 * @param reason 错误原因。
		 */
		void failed(String reason);
	}

	/**
	 * 单帧的同步接收边界。
	 */
	public interface FrameListener {
		/**
		 * 接收设备 NV21 数据。
		 *
		 * @param bytes 本帧原始数据。
		 */
		void received(byte[] bytes);
	}
}
