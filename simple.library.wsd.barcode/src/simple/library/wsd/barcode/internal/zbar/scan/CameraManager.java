package simple.library.wsd.barcode.internal.zbar.scan;

import android.content.Context;
import android.graphics.Point;
import android.hardware.Camera;
import android.util.Log;
import java.io.IOException;

/**
 * 管理扫描相机、预览参数及闪光灯。
 */
public final class CameraManager {
	private static final String TAG = CameraManager.class.getSimpleName();
	private final CameraConfigurationManager configManager;
	private Camera camera;
	private boolean initialized;

	public CameraManager(Context context) {
		this.configManager = new CameraConfigurationManager(context);
	}

	/**
	 * 打开相机并准备预览参数。
	 *
	 * @throws IOException 访问文件、流或网络资源失败时
	 */
	public synchronized void openDriver() throws IOException {
		block7: {
			Camera.Parameters parameters;
			Camera theCamera = this.camera;

			if (theCamera == null) {
				theCamera = Camera.open();
				if (theCamera == null) {
					throw new IOException();
				}

				this.camera = theCamera;
			}

			if (!this.initialized) {
				this.initialized = true;
				this.configManager.initFromCameraParameters(theCamera);
			}

			String parametersFlattened = (parameters = theCamera.getParameters()) == null ? null : parameters.flatten();

			try {
				this.configManager.setDesiredCameraParameters(theCamera, false);
			} catch (RuntimeException re) {
				Log.w(TAG, "Camera rejected parameters. Setting only minimal safe-mode parameters");
				Log.i(TAG, "Resetting to saved camera params: " + parametersFlattened);
				if (parametersFlattened == null) break block7;

				parameters = theCamera.getParameters();
				parameters.unflatten(parametersFlattened);

				try {
					theCamera.setParameters(parameters);
					this.configManager.setDesiredCameraParameters(theCamera, true);
				} catch (RuntimeException re2) {
					Log.w(TAG, "Camera rejected even safe-mode parameters! No configuration");
				}
			}
		}
	}

	/**
	 * 判断相机或连接是否已经打开。
	 *
	 * @return 相机或连接已经打开时为 {@code true}，否则为 {@code false}
	 */
	public synchronized boolean isOpen() {
		return this.camera != null;
	}

	/**
	 * 返回当前打开的相机对象。
	 *
	 * @return 当前打开的相机
	 */
	public Camera getCamera() {
		return this.camera;
	}

	/**
	 * 关闭并释放当前相机。
	 */
	public synchronized void closeDriver() {
		if (this.camera != null) {
			this.camera.release();
			this.camera = null;
		}
	}

	/**
	 * 返回相机使用的预览分辨率。
	 *
	 * @return 相机预览分辨率
	 */
	public Point getCameraResolution() {
		return this.configManager.getCameraResolution();
	}
}
