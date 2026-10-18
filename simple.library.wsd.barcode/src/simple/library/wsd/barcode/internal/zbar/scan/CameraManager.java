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

	public synchronized boolean isOpen() {
		return this.camera != null;
	}

	public Camera getCamera() {
		return this.camera;
	}

	public synchronized void closeDriver() {
		if (this.camera != null) {
			this.camera.release();
			this.camera = null;
		}
	}

	public Point getCameraResolution() {
		return this.configManager.getCameraResolution();
	}
}
