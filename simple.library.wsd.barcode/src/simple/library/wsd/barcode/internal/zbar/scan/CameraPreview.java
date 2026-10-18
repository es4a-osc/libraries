package simple.library.wsd.barcode.internal.zbar.scan;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.io.IOException;

/**
 * 通过 Surface 生命周期驱动 ZBar 相机预览。
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
	private SurfaceHolder mHolder;
	private Camera mCamera;
	private Camera.PreviewCallback previewCallback;
	private Camera.AutoFocusCallback autoFocusCallback;

	public CameraPreview(Context context, Camera camera, Camera.PreviewCallback previewCb, Camera.AutoFocusCallback autoFocusCb) {
		super(context);
		this.mCamera = camera;
		this.previewCallback = previewCb;
		this.autoFocusCallback = autoFocusCb;
		this.mHolder = this.getHolder();
		this.mHolder.addCallback(this);
		this.mHolder.setType(3);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		try {
			this.mCamera.setPreviewDisplay(holder);
		} catch (IOException e) {
			Log.d("DBG", "Error setting camera preview: " + e.getMessage());
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		if (this.mHolder.getSurface() == null) {
			return;
		}

		try {
			this.mCamera.stopPreview();
		} catch (Exception exception) {
			// 沿用原实现的容错分支，后续按原流程继续。
		}

		try {
			this.mCamera.setDisplayOrientation(90);
			this.mCamera.setPreviewDisplay(this.mHolder);
			this.mCamera.setPreviewCallback(this.previewCallback);
			this.mCamera.startPreview();
			this.mCamera.autoFocus(this.autoFocusCallback);
		} catch (Exception e) {
			Log.d("DBG", "Error starting camera preview: " + e.getMessage());
		}
	}
}
