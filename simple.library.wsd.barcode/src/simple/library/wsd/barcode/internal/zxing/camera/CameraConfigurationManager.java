package simple.library.wsd.barcode.internal.zxing.camera;

import android.content.Context;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.Build;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import java.lang.reflect.Method;
import java.util.regex.Pattern;

/**
 * 沿用原组件的相机参数和预览尺寸选择规则。
 */
final class CameraConfigurationManager {
	private static final String TAG = CameraConfigurationManager.class.getSimpleName();
	private static final int TEN_DESIRED_ZOOM = 27;
	private static final Pattern COMMA_PATTERN = Pattern.compile(",");
	private final Context context;
	private Point screenResolution;
	private Point cameraResolution;
	private int previewFormat;
	private String previewFormatString;

	CameraConfigurationManager(Context context) {
		this.context = context;
	}

	void initFromCameraParameters(Camera camera) {
		Camera.Parameters parameters = camera.getParameters();
		this.previewFormat = parameters.getPreviewFormat();
		this.previewFormatString = parameters.get("preview-format");
		Log.d(TAG, "Default preview format: " + this.previewFormat + '/' + this.previewFormatString);
		WindowManager manager = (WindowManager)this.context.getSystemService("window");
		Display display = manager.getDefaultDisplay();

		this.screenResolution = new Point(display.getWidth(), display.getHeight());
		Log.d(TAG, "Screen resolution: " + this.screenResolution);
		Point screenResolutionForCamera = new Point();
		screenResolutionForCamera.x = this.screenResolution.x;
		screenResolutionForCamera.y = this.screenResolution.y;
		if (this.screenResolution.x < this.screenResolution.y) {
			screenResolutionForCamera.x = this.screenResolution.y;
			screenResolutionForCamera.y = this.screenResolution.x;
		}

		this.cameraResolution = CameraConfigurationManager.getCameraResolution(parameters, screenResolutionForCamera);
		Log.d(TAG, "Camera resolution: " + this.cameraResolution);
	}

	void setDesiredCameraParameters(Camera camera) {
		Camera.Parameters parameters = camera.getParameters();
		Log.d(TAG, "Setting preview size: " + this.cameraResolution);
		parameters.setPreviewSize(this.cameraResolution.x, this.cameraResolution.y);
		this.setFlash(parameters);
		this.setZoom(parameters);
		camera.setParameters(parameters);
		this.setDisplayOrientation(camera, 90);
	}

	private void setDisplayOrientation(Camera camera, int angle) {
		try {
			Method downPolymorphic = camera.getClass().getMethod("setDisplayOrientation", Integer.TYPE);
			if (downPolymorphic != null) {
				downPolymorphic.invoke(camera, angle);
			}
		} catch (Exception exception) {
			// 沿用原实现的容错分支，后续按原流程继续。
		}
	}

	Point getCameraResolution() {
		return this.cameraResolution;
	}

	Point getScreenResolution() {
		return this.screenResolution;
	}

	int getPreviewFormat() {
		return this.previewFormat;
	}

	String getPreviewFormatString() {
		return this.previewFormatString;
	}

	private static Point getCameraResolution(Camera.Parameters parameters, Point screenResolution) {
		String previewSizeValueString = parameters.get("preview-size-values");
		if (previewSizeValueString == null) {
			previewSizeValueString = parameters.get("preview-size-value");
		}

		Point cameraResolution = null;
		if (previewSizeValueString != null) {
			Log.d(TAG, "preview-size-values parameter: " + previewSizeValueString);
			cameraResolution = CameraConfigurationManager.findBestPreviewSizeValue(previewSizeValueString, screenResolution);
		}

		if (cameraResolution == null) {
			cameraResolution = new Point(screenResolution.x >> 3 << 3, screenResolution.y >> 3 << 3);
		}

		return cameraResolution;
	}

	private static Point findBestPreviewSizeValue(CharSequence previewSizeValueString, Point screenResolution) {
		int bestX = 0;
		int bestY = 0;
		int diff = Integer.MAX_VALUE;

		for (String previewSize : COMMA_PATTERN.split(previewSizeValueString)) {
			int newY;
			int newX;
			int dimPosition = (previewSize = previewSize.trim()).indexOf(120);

			if (dimPosition < 0) {
				Log.w(TAG, "Bad preview-size: " + previewSize);
				continue;
			}

			try {
				newX = Integer.parseInt(previewSize.substring(0, dimPosition));
				newY = Integer.parseInt(previewSize.substring(dimPosition + 1));
			} catch (NumberFormatException nfe) {
				Log.w(TAG, "Bad preview-size: " + previewSize);
				continue;
			}

			int newDiff = Math.abs(newX - screenResolution.x) + Math.abs(newY - screenResolution.y);
			if (newDiff == 0) {
				bestX = newX;
				bestY = newY;
				break;
			}

			if (newDiff >= diff) continue;

			bestX = newX;
			bestY = newY;
			diff = newDiff;
		}

		if (bestX > 0 && bestY > 0) {
			return new Point(bestX, bestY);
		}

		return null;
	}

	private static int findBestMotZoomValue(CharSequence stringValues, int tenDesiredZoom) {
		int tenBestValue = 0;

		for (String stringValue : COMMA_PATTERN.split(stringValues)) {
			double value;
			stringValue = stringValue.trim();

			try {
				value = Double.parseDouble(stringValue);
			} catch (NumberFormatException nfe) {
				return tenDesiredZoom;
			}

			int tenValue = (int)(10.0 * value);
			if (!(Math.abs((double)tenDesiredZoom - value) < (double)Math.abs(tenDesiredZoom - tenBestValue))) continue;

			tenBestValue = tenValue;
		}

		return tenBestValue;
	}

	private void setFlash(Camera.Parameters parameters) {
		if (Build.MODEL.contains("Behold II") && CameraManager.SDK_INT == 3) {
			parameters.set("flash-value", 1);
		} else {
			parameters.set("flash-value", 2);
		}

		parameters.set("flash-mode", "off");
	}

	private void setZoom(Camera.Parameters parameters) {
		String motZoomStepString;
		String motZoomValuesString;
		String zoomSupportedString = parameters.get("zoom-supported");

		if (zoomSupportedString != null && !Boolean.parseBoolean(zoomSupportedString)) {
			return;
		}

		int tenDesiredZoom = 27;
		String maxZoomString = parameters.get("max-zoom");

		if (maxZoomString != null) {
			try {
				int tenMaxZoom = (int)(10.0 * Double.parseDouble(maxZoomString));
				if (tenDesiredZoom > tenMaxZoom) {
					tenDesiredZoom = tenMaxZoom;
				}
			} catch (NumberFormatException nfe) {
				Log.w(TAG, "Bad max-zoom: " + maxZoomString);
			}
		}

		String takingPictureZoomMaxString = parameters.get("taking-picture-zoom-max");
		Log.i("MSG", "taking-picture-zoom-max:" + takingPictureZoomMaxString);
		if (takingPictureZoomMaxString != null) {
			try {
				int tenMaxZoom = Integer.parseInt(takingPictureZoomMaxString);
				if (tenDesiredZoom > tenMaxZoom) {
					tenDesiredZoom = tenMaxZoom;
				}
			} catch (NumberFormatException nfe) {
				Log.w(TAG, "Bad taking-picture-zoom-max: " + takingPictureZoomMaxString);
			}
		}

		if ((motZoomValuesString = parameters.get("mot-zoom-values")) != null) {
			tenDesiredZoom = CameraConfigurationManager.findBestMotZoomValue(motZoomValuesString, tenDesiredZoom);
		}

		if ((motZoomStepString = parameters.get("mot-zoom-step")) != null) {
			try {
				double motZoomStep = Double.parseDouble(motZoomStepString.trim());
				int tenZoomStep = (int)(10.0 * motZoomStep);

				if (tenZoomStep > 1) {
					tenDesiredZoom -= tenDesiredZoom % tenZoomStep;
				}
			} catch (NumberFormatException numberFormatException) {
				// 沿用原实现的容错分支，后续按原流程继续。
			}
		}

		if (maxZoomString != null || motZoomValuesString != null) {
			parameters.set("zoom", String.valueOf((double)tenDesiredZoom / 10.0));
		}

		if (takingPictureZoomMaxString != null) {
			parameters.set("taking-picture-zoom", tenDesiredZoom);
		}
	}
}
