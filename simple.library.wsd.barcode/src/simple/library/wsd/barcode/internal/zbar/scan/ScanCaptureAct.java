package simple.library.wsd.barcode.internal.zbar.scan;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Rect;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Display;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.io.IOException;
import java.lang.reflect.Field;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;

/**
 * ZBar 扫描界面，组织相机预览、扫描区域和结果通知。
 */
public class ScanCaptureAct extends Activity {
	private Camera mCamera;
	private CameraPreview mPreview;
	private Handler autoFocusHandler;
	private CameraManager mCameraManager;
	private TextView scanResult;
	private FrameLayout scanPreview;
	private Button scanFlash;
	private RelativeLayout scanContainer;
	private RelativeLayout scanCropView;
	private ImageView scanLine;
	private Rect mCropRect = null;
	private boolean barcodeScanned = false;
	private boolean previewing = true;
	private ImageScanner mImageScanner = null;
	private boolean flashState = false;
	private boolean continuous = false;
	private boolean beep = false;
	private BroadcastReceiver mReceiver;
	private boolean receiverRegistered;
	private MediaPlayer player;
	private Runnable doAutoFocus = new Runnable() {

		@Override
		public void run() {
			if (ScanCaptureAct.this.previewing) {
				ScanCaptureAct.this.mCamera.autoFocus(ScanCaptureAct.this.autoFocusCB);
			}
		}
	};
	Camera.PreviewCallback previewCb = new Camera.PreviewCallback() {

		@Override
		public void onPreviewFrame(byte[] data, Camera camera) {
			// 原始相机帧为横向数据，旋转后再按扫描框区域送入 ZBar。
			Camera.Size size = camera.getParameters().getPreviewSize();
			byte[] rotatedData = new byte[data.length];

			for (int y = 0; y < size.height; ++y) {
				for (int x = 0; x < size.width; ++x) {
					rotatedData[x * size.height + size.height - y - 1] = data[x + y * size.width];
				}
			}

			int tmp = size.width;
			size.width = size.height;
			size.height = tmp;
			ScanCaptureAct.this.initCrop();
			Image barcode = new Image(size.width, size.height, "Y800");
			barcode.setData(rotatedData);
			barcode.setCrop(((ScanCaptureAct)ScanCaptureAct.this).mCropRect.left, ((ScanCaptureAct)ScanCaptureAct.this).mCropRect.top, ScanCaptureAct.this.mCropRect.width(), ScanCaptureAct.this.mCropRect.height());
			int result = ScanCaptureAct.this.mImageScanner.scanImage(barcode);
			String resultStr = null;

			if (result != 0) {
				SymbolSet syms = ScanCaptureAct.this.mImageScanner.getResults();

				for (Symbol sym : syms) {
					resultStr = sym.getData();
				}
			}

			// 只通知有效结果，连续模式下保持当前预览。
			if (TextUtils.isEmpty(resultStr)) {
				return;
			}

			Intent intent3 = new Intent();
			intent3.setAction(getPackageName() + ".barcode.RESULT");
			intent3.setPackage(getPackageName());
			intent3.putExtra("code", resultStr);
			ScanCaptureAct.this.sendBroadcast(intent3);
			if (ScanCaptureAct.this.beep) {
				ScanCaptureAct.this.player.start();
			}

			if (!ScanCaptureAct.this.continuous) {
				ScanCaptureAct.this.unregisterStopReceiver();
				ScanCaptureAct.this.previewing = false;
				ScanCaptureAct.this.mCamera.setPreviewCallback(null);
				ScanCaptureAct.this.mCamera.stopPreview();
				ScanCaptureAct.this.releaseCamera();
				ScanCaptureAct.this.finish();
			}
		}
	};
	Camera.AutoFocusCallback autoFocusCB = new Camera.AutoFocusCallback() {

		@Override
		public void onAutoFocus(boolean success, Camera camera) {
			ScanCaptureAct.this.autoFocusHandler.postDelayed(ScanCaptureAct.this.doAutoFocus, 1000L);
		}
	};

	private int getResourceId(String resourceName, String resourceType) {
		Resources res = this.getResources();
		return res.getIdentifier(resourceName, resourceType, this.getPackageName());
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(this.getResourceId("widget_zbar_scan_capture", "layout"));
		this.setRequestedOrientation(1);
		this.setTitle("二维码/条码 扫描");
		this.findViewById();
		this.addEvents();
		this.initViews();

		// 每次打开界面时固定本次扫描设置。
		Intent intent = this.getIntent();
		Bundle bundle = intent.getExtras();

		this.beep = bundle.getBoolean("beep");
		this.continuous = bundle.getBoolean("continuous");
		this.flashState = bundle.getBoolean("flashlight");
		if (this.beep) {
			this.player = MediaPlayer.create((Context)this, this.getResourceId("beep", "raw"));
		}

		this.mReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context arg0, Intent arg1) {
				String actionStr = arg1.getAction();
				if (actionStr.equals(getPackageName() + ".barcode.STOP")) {
					ScanCaptureAct.this.unregisterStopReceiver();
					ScanCaptureAct.this.previewing = false;
					ScanCaptureAct.this.mCamera.setPreviewCallback(null);
					ScanCaptureAct.this.mCamera.stopPreview();
					ScanCaptureAct.this.releaseCamera();
					ScanCaptureAct.this.finish();
				}
			}
		};
		IntentFilter filter = new IntentFilter();
		filter.addAction(getPackageName() + ".barcode.STOP");
		this.registerReceiver(this.mReceiver, filter);
		receiverRegistered = true;
		if (this.flashState) {
			new Handler().postDelayed(new Runnable() {

				@Override
				public void run() {
					Camera.Parameters mParameters = ScanCaptureAct.this.mCamera.getParameters();
					mParameters.setFlashMode("torch");
					ScanCaptureAct.this.mCamera.setParameters(mParameters);
					ScanCaptureAct.this.scanFlash.setBackgroundResource(ScanCaptureAct.this.getResourceId("qr_code_flash_close", "drawable"));
				}
			}, 1000L);
		}
	}

	private void findViewById() {
		this.scanPreview = (FrameLayout)this.findViewById(this.getResourceId("capture_preview", "id"));
		this.scanResult = (TextView)this.findViewById(this.getResourceId("capture_scan_result", "id"));
		this.scanFlash = (Button)this.findViewById(this.getResourceId("capture_btn_flash", "id"));
		this.scanContainer = (RelativeLayout)this.findViewById(this.getResourceId("capture_container", "id"));
		this.scanCropView = (RelativeLayout)this.findViewById(this.getResourceId("capture_crop_view", "id"));
		this.scanLine = (ImageView)this.findViewById(this.getResourceId("capture_scan_line", "id"));
	}

	private void addEvents() {
		this.scanFlash.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!ScanCaptureAct.this.flashState) {
					ScanCaptureAct.this.flashState = true;
					Camera.Parameters mParameters = ScanCaptureAct.this.mCamera.getParameters();
					mParameters.setFlashMode("torch");
					ScanCaptureAct.this.mCamera.setParameters(mParameters);
					ScanCaptureAct.this.scanFlash.setBackgroundResource(ScanCaptureAct.this.getResourceId("qr_code_flash_close", "drawable"));
				} else {
					ScanCaptureAct.this.flashState = false;
					Camera.Parameters mParameters = ScanCaptureAct.this.mCamera.getParameters();
					mParameters.setFlashMode("off");
					ScanCaptureAct.this.mCamera.setParameters(mParameters);
					ScanCaptureAct.this.scanFlash.setBackgroundResource(ScanCaptureAct.this.getResourceId("qr_code_flash_open", "drawable"));
				}
			}
		});
	}

	private void initViews() {
		this.mImageScanner = new ImageScanner();
		this.mImageScanner.setConfig(0, 256, 3);
		this.mImageScanner.setConfig(0, 257, 3);
		this.autoFocusHandler = new Handler();
		this.mCameraManager = new CameraManager(this);

		try {
			this.mCameraManager.openDriver();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Display display = this.getWindowManager().getDefaultDisplay();
		int width = display.getWidth();
		int height = display.getHeight();
		RelativeLayout.LayoutParams linearParams = (RelativeLayout.LayoutParams)this.scanCropView.getLayoutParams();

		linearParams.height = (int)((double)width * 0.8);
		linearParams.width = (int)((double)width * 0.8);
		this.scanCropView.setLayoutParams(linearParams);
		this.mCamera = this.mCameraManager.getCamera();
		this.mPreview = new CameraPreview((Context)this, this.mCamera, this.previewCb, this.autoFocusCB);
		this.scanPreview.addView(this.mPreview);
		TranslateAnimation animation = new TranslateAnimation(2, 0.0f, 2, 0.0f, 2, 0.0f, 2, 0.85f);
		animation.setDuration(5000L);
		animation.setRepeatCount(-1);
		animation.setRepeatMode(1);
		this.scanLine.startAnimation(animation);
	}

	@Override
	public void onPause() {
		super.onPause();
		this.releaseCamera();
	}

	@Override
	protected void onDestroy() {
		unregisterStopReceiver();
		releaseCamera();
		if (autoFocusHandler != null) {
			autoFocusHandler.removeCallbacksAndMessages(null);
		}

		if (player != null) {
			player.release();
			player = null;
		}

		if (mImageScanner != null) {
			mImageScanner.destroy();
			mImageScanner = null;
		}

		super.onDestroy();
	}

	private void releaseCamera() {
		if (this.mCamera != null) {
			this.previewing = false;
			this.mCamera.setPreviewCallback(null);
			this.mCamera.release();
			this.mCamera = null;
		}
	}

	/**
	 * 把界面扫描框换算为旋转后的相机帧裁剪区域。
	 */
	private void initCrop() {
		int cameraWidth = this.mCameraManager.getCameraResolution().y;
		int cameraHeight = this.mCameraManager.getCameraResolution().x;
		int[] location = new int[2];

		this.scanCropView.getLocationInWindow(location);
		int cropLeft = location[0];
		int cropTop = location[1] - this.getStatusBarHeight();
		int cropWidth = this.scanCropView.getWidth();
		int cropHeight = this.scanCropView.getHeight();
		int containerWidth = this.scanContainer.getWidth();
		int containerHeight = this.scanContainer.getHeight();
		int x = cropLeft * cameraWidth / containerWidth;
		int y = cropTop * cameraHeight / containerHeight;
		int width = cropWidth * cameraWidth / containerWidth;
		int height = cropHeight * cameraHeight / containerHeight;

		this.mCropRect = new Rect(x, y, width + x, height + y);
	}

	private int getStatusBarHeight() {
		try {
			Class<?> c = Class.forName("com.android.internal.R$dimen");
			Object obj = c.newInstance();
			Field field = c.getField("status_bar_height");
			int x = Integer.parseInt(field.get(obj).toString());

			return this.getResources().getDimensionPixelSize(x);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	static {
		System.loadLibrary("iconv");
	}

	/**
	 * 结果、停止与销毁可能先后到达，只撤销实际注册的接收器。
	 */
	private void unregisterStopReceiver() {
		if (receiverRegistered) {
			unregisterReceiver(mReceiver);
			receiverRegistered = false;
		}
	}
}
