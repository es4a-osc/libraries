package simple.library.wsd.barcode.internal.zbar.scan;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.hardware.Camera;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * 沿用原组件的相机参数和预览尺寸选择规则。
 */
public final class CameraConfigurationManager {
	private static final String TAG = "CameraConfiguration";
	private static final int MIN_PREVIEW_PIXELS = 153600;
	private static final double MAX_ASPECT_DISTORTION = 0.15;
	private final Context context;
	private Point screenResolution;
	private Point cameraResolution;

	public CameraConfigurationManager(Context context) {
		this.context = context;
	}

	/**
	 * 读取相机支持的预览参数并计算匹配尺寸。
	 *
	 * @param camera 本次使用的相机
	 */
	public void initFromCameraParameters(Camera camera) {
		Camera.Parameters parameters = camera.getParameters();
		WindowManager manager = (WindowManager)this.context.getSystemService("window");
		Display display = manager.getDefaultDisplay();
		Point theScreenResolution = new Point();

		this.screenResolution = theScreenResolution = this.getDisplaySize(display);
		Log.i(TAG, "Screen resolution: " + this.screenResolution);
		Point screenResolutionForCamera = new Point();
		screenResolutionForCamera.x = this.screenResolution.x;
		screenResolutionForCamera.y = this.screenResolution.y;
		if (this.screenResolution.x < this.screenResolution.y) {
			screenResolutionForCamera.x = this.screenResolution.y;
			screenResolutionForCamera.y = this.screenResolution.x;
		}

		this.cameraResolution = this.findBestPreviewSizeValue(parameters, screenResolutionForCamera);
		Log.i(TAG, "Camera resolution x: " + this.cameraResolution.x);
		Log.i(TAG, "Camera resolution y: " + this.cameraResolution.y);
	}

	/**
	 * 读取显示设备的尺寸。
	 *
	 * @param display 显示设备
	 * @return 显示设备尺寸
	 */
	@SuppressLint("NewApi")
	private Point getDisplaySize(Display display) {
		Point point = new Point();

		try {
			display.getSize(point);
		} catch (NoSuchMethodError ignore) {
			point.x = display.getWidth();
			point.y = display.getHeight();
		}

		return point;
	}

	/**
	 * 将选定的预览参数应用到相机。
	 *
	 * @param camera 本次使用的相机
	 * @param safeMode 是否仅应用安全的相机参数
	 */
	public void setDesiredCameraParameters(Camera camera, boolean safeMode) {
		Camera.Parameters parameters = camera.getParameters();
		if (parameters == null) {
			Log.w(TAG, "Device error: no camera parameters are available. Proceeding without configuration.");
			return;
		}

		Log.i(TAG, "Initial camera parameters: " + parameters.flatten());
		if (safeMode) {
			Log.w(TAG, "In camera config safe mode -- most settings will not be honored");
		}

		parameters.setPreviewSize(this.cameraResolution.x, this.cameraResolution.y);
		camera.setParameters(parameters);
		Camera.Parameters afterParameters = camera.getParameters();
		Camera.Size afterSize = afterParameters.getPreviewSize();

		if (afterSize != null && (this.cameraResolution.x != afterSize.width || this.cameraResolution.y != afterSize.height)) {
			Log.w(TAG, "Camera said it supported preview size " + this.cameraResolution.x + 'x' + this.cameraResolution.y + ", but after setting it, preview size is " + afterSize.width + 'x' + afterSize.height);
			this.cameraResolution.x = afterSize.width;
			this.cameraResolution.y = afterSize.height;
		}

		camera.setDisplayOrientation(90);
	}

	/**
	 * 返回相机使用的预览分辨率。
	 *
	 * @return 相机预览分辨率
	 */
	public Point getCameraResolution() {
		return this.cameraResolution;
	}

	/**
	 * 返回预览界面使用的屏幕分辨率。
	 *
	 * @return 屏幕分辨率
	 */
	public Point getScreenResolution() {
		return this.screenResolution;
	}

	/**
	 * 从相机支持的尺寸中选择最匹配屏幕的预览尺寸。
	 *
	 * @param parameters 相机参数
	 * @param screenResolution 屏幕分辨率
	 * @return 与屏幕最匹配的预览分辨率
	 */
	private Point findBestPreviewSizeValue(Camera.Parameters parameters, Point screenResolution) {
		List<Camera.Size> rawSupportedSizes = parameters.getSupportedPreviewSizes();
		if (rawSupportedSizes == null) {
			Log.w(TAG, "Device returned no supported preview sizes; using default");
			Camera.Size defaultSize = parameters.getPreviewSize();
			return new Point(defaultSize.width, defaultSize.height);
		}

		ArrayList<Camera.Size> supportedPreviewSizes = new ArrayList<Camera.Size>(rawSupportedSizes);

		// 按像素数从大到小排列，筛选后首项即为最大的可用预览尺寸。
		Collections.sort(supportedPreviewSizes, new Comparator<Camera.Size>() {

			@Override
			public int compare(Camera.Size a, Camera.Size b) {
				int bPixels = b.height * b.width;
				int aPixels = a.height * a.width;

				if (bPixels < aPixels) {
					return -1;
				}

				if (bPixels > aPixels) {
					return 1;
				}

				return 0;
			}
		});
		if (Log.isLoggable(TAG, 4)) {
			StringBuilder previewSizesString = new StringBuilder();

			for (Camera.Size supportedPreviewSize : supportedPreviewSizes) {
				previewSizesString.append(supportedPreviewSize.width).append('x').append(supportedPreviewSize.height).append(' ');
			}

			Log.i(TAG, "Supported preview sizes: " + previewSizesString);
		}

		// 将候选尺寸换算到同一横竖屏方向，再按像素下限和比例偏差筛选。
		double screenAspectRatio = (double)screenResolution.x / (double)screenResolution.y;
		Iterator it = supportedPreviewSizes.iterator();

		while (it.hasNext()) {
			int maybeFlippedHeight;
			Camera.Size supportedPreviewSize = (Camera.Size)it.next();
			int realWidth = supportedPreviewSize.width;
			int realHeight = supportedPreviewSize.height;

			if (realWidth * realHeight < 153600) {
				it.remove();
				continue;
			}

			boolean isCandidatePortrait = realWidth < realHeight;
			int maybeFlippedWidth = isCandidatePortrait ? realHeight : realWidth;
			double aspectRatio = (double)maybeFlippedWidth / (double)(maybeFlippedHeight = isCandidatePortrait ? realWidth : realHeight);
			double distortion = Math.abs(aspectRatio - screenAspectRatio);

			if (distortion > 0.15) {
				it.remove();
				continue;
			}

			if (maybeFlippedWidth != screenResolution.x || maybeFlippedHeight != screenResolution.y) continue;

			Point exactPoint = new Point(realWidth, realHeight);
			Log.i(TAG, "Found preview size exactly matching screen size: " + exactPoint);
			return exactPoint;
		}

		if (!supportedPreviewSizes.isEmpty()) {
			Camera.Size largestPreview = (Camera.Size)supportedPreviewSizes.get(0);
			Point largestSize = new Point(largestPreview.width, largestPreview.height);

			Log.i(TAG, "Using largest suitable preview size: " + largestSize);
			return largestSize;
		}

		// 没有合适候选时保留设备默认尺寸，不构造驱动未声明的尺寸。
		Camera.Size defaultPreview = parameters.getPreviewSize();
		Point defaultSize = new Point(defaultPreview.width, defaultPreview.height);

		Log.i(TAG, "No suitable preview sizes, using default: " + defaultSize);
		return defaultSize;
	}
}
