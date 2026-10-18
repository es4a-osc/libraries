package simple.library.wsd.barcode.internal.zxing.camera;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Build;
import android.os.Handler;
import android.view.SurfaceHolder;
import java.io.IOException;

/**
 * 管理扫描相机、预览参数及闪光灯。
 */
public final class CameraManager {
	private static int style;
	private static CameraManager cameraManager;
	static final int SDK_INT;
	private final CameraConfigurationManager configManager;
	private Camera camera;
	private Rect framingRect;
	private Rect framingRectInPreview;
	private boolean initialized;
	private boolean previewing;
	private final boolean useOneShotPreviewCallback;
	private final PreviewCallback previewCallback;
	private final AutoFocusCallback autoFocusCallback;

	/**
	 * 初始化相机管理器共享实例。
	 *
	 * @param context Android 上下文
	 * @param value 扫描框采用的样式编号。
	 */
	public static void init(Context context, int value) {
		style = value;
		cameraManager = new CameraManager(context);
	}

	/**
	 * 取得相机管理器共享实例。
	 *
	 * @return 相机管理器共享实例
	 */
	public static CameraManager get() {
		return cameraManager;
	}

	private CameraManager(Context context) {
		this.configManager = new CameraConfigurationManager(context);
		this.useOneShotPreviewCallback = Integer.parseInt(Build.VERSION.SDK) > 3;
		this.previewCallback = new PreviewCallback(this.configManager, this.useOneShotPreviewCallback);
		this.autoFocusCallback = new AutoFocusCallback();
	}

	/**
	 * 打开相机并准备预览参数。
	 *
	 * @param holder 预览表面的持有者
	 * @throws IOException 访问文件、流或网络资源失败时
	 */
	public void openDriver(SurfaceHolder holder) throws IOException {
		if (this.camera == null) {
			this.camera = Camera.open();
			if (this.camera == null) {
				throw new IOException();
			}

			this.camera.setPreviewDisplay(holder);
			if (!this.initialized) {
				this.initialized = true;
				this.configManager.initFromCameraParameters(this.camera);
			}

			this.configManager.setDesiredCameraParameters(this.camera);
		}
	}

	/**
	 * 打开相机补光灯。
	 */
	public void openFlash() {
		if (this.camera != null) {
			Camera.Parameters parameter = this.camera.getParameters();
			parameter.setFlashMode("torch");
			this.camera.setParameters(parameter);
		}
	}

	/**
	 * 关闭并释放当前相机。
	 */
	public void closeDriver() {
		if (this.camera != null) {
			this.closeFlash();
			this.camera.release();
			this.camera = null;
		}
	}

	/**
	 * 关闭相机补光灯。
	 */
	public void closeFlash() {
		if (this.camera != null) {
			Camera.Parameters parameter = this.camera.getParameters();
			parameter.setFlashMode("off");
			this.camera.setParameters(parameter);
		}
	}

	/**
	 * 启动相机预览。
	 */
	public void startPreview() {
		if (this.camera != null && !this.previewing) {
			this.camera.startPreview();
			this.previewing = true;
		}
	}

	/**
	 * 停止相机预览。
	 */
	public void stopPreview() {
		if (this.camera != null && this.previewing) {
			if (!this.useOneShotPreviewCallback) {
				this.camera.setPreviewCallback(null);
			}

			this.camera.stopPreview();
			this.previewCallback.setHandler(null, 0);
			this.autoFocusCallback.setHandler(null, 0);
			this.previewing = false;
		}
	}

	/**
	 * 请求下一帧预览图像并投递给指定处理器。
	 *
	 * @param handler 接收异步消息的处理器
	 * @param message 消息文本
	 */
	public void requestPreviewFrame(Handler handler, int message) {
		if (this.camera != null && this.previewing) {
			this.previewCallback.setHandler(handler, message);
			if (this.useOneShotPreviewCallback) {
				this.camera.setOneShotPreviewCallback(this.previewCallback);
			} else {
				this.camera.setPreviewCallback(this.previewCallback);
			}
		}
	}

	/**
	 * 请求相机自动对焦并投递完成消息。
	 *
	 * @param handler 接收异步消息的处理器
	 * @param message 消息文本
	 */
	public void requestAutoFocus(Handler handler, int message) {
		if (this.camera != null && this.previewing) {
			this.autoFocusCallback.setHandler(handler, message);
			this.camera.autoFocus(this.autoFocusCallback);
		}
	}

	/**
	 * 返回屏幕坐标系内的扫描取景框。
	 *
	 * @return 屏幕坐标系下的取景区域
	 */
	public Rect getFramingRect() {
		Point screenResolution = this.configManager.getScreenResolution();
		if (this.framingRect == null) {
			int width;
			if (this.camera == null) {
				return null;
			}

			int height = width = screenResolution.x * 3 / 4;
			int leftOffset = (screenResolution.x - width) / 2;
			int topOffset = (screenResolution.y - height) / 2;

			this.framingRect = style == 2 ? new Rect(0, topOffset, leftOffset * 2 + width, topOffset + height / 2) : new Rect(leftOffset, topOffset, leftOffset + width, topOffset + height);
		}

		return this.framingRect;
	}

	/**
	 * 返回预览图像坐标系内的扫描取景框。
	 *
	 * @return 预览坐标系下的取景区域
	 */
	public Rect getFramingRectInPreview() {
		if (this.framingRectInPreview == null) {
			Rect rect = new Rect(this.getFramingRect());
			Point cameraResolution = this.configManager.getCameraResolution();
			Point screenResolution = this.configManager.getScreenResolution();

			rect.left = rect.left * cameraResolution.y / screenResolution.x;
			rect.right = rect.right * cameraResolution.y / screenResolution.x;
			rect.top = rect.top * cameraResolution.x / screenResolution.y;
			rect.bottom = rect.bottom * cameraResolution.x / screenResolution.y;
			this.framingRectInPreview = rect;
		}

		return this.framingRectInPreview;
	}

	/**
	 * 按取景区域从预览数据构建解码亮度源。
	 *
	 * @param data 本次传入的数据
	 * @param width 宽度
	 * @param height 高度
	 * @return 仅包含取景区域的亮度源
	 */
	public PlanarYUVLuminanceSource buildLuminanceSource(byte[] data, int width, int height) {
		Rect rect = this.getFramingRectInPreview();
		int previewFormat = this.configManager.getPreviewFormat();
		String previewFormatString = this.configManager.getPreviewFormatString();

		switch (previewFormat) {
			case 16:
			case 17: {
				return new PlanarYUVLuminanceSource(data, width, height, rect.left, rect.top, rect.width(), rect.height());
			}
		}

		if ("yuv420p".equals(previewFormatString)) {
			return new PlanarYUVLuminanceSource(data, width, height, rect.left, rect.top, rect.width(), rect.height());
		}

		throw new IllegalArgumentException("Unsupported picture format: " + previewFormat + '/' + previewFormatString);
	}

	static {
		int sdkInt;

		try {
			sdkInt = Integer.parseInt(Build.VERSION.SDK);
		} catch (NumberFormatException nfe) {
			sdkInt = 10000;
		}

		SDK_INT = sdkInt;
	}
}
