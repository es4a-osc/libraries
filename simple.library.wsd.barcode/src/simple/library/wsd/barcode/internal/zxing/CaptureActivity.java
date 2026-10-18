package simple.library.wsd.barcode.internal.zxing;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import simple.library.wsd.barcode.internal.zxing.camera.CameraManager;
import simple.library.wsd.barcode.internal.zxing.decoding.CaptureActivityHandler;
import simple.library.wsd.barcode.internal.zxing.decoding.InactivityTimer;
import simple.library.wsd.barcode.internal.zxing.view.ViewfinderView;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Vector;

/**
 * ZXing 扫描界面，组织预览、解码线程和结果通知。
 */
public class CaptureActivity extends Activity implements SurfaceHolder.Callback {
	private CaptureActivityHandler handler;
	private ViewfinderView viewfinderView;
	private SurfaceView surfaceView;
	private boolean hasSurface;
	private Vector<BarcodeFormat> decodeFormats;
	private String characterSet = "ISO-8859-1";
	private InactivityTimer inactivityTimer;
	private Intent intent;
	private int style = 1;
	private boolean continuous = false;
	private boolean flashlight = false;
	private boolean beep = false;
	private BroadcastReceiver mReceiver;
	private boolean receiverRegistered;
	private MediaPlayer player;
	private String flashButtonTitle = "开闪光灯";

	/**
	 * 按名称和资源类型查找本应用资源编号。
	 *
	 * @param resourceName 资源名称
	 * @param resourceType 资源类型
	 * @return 资源编号，未找到时为 0
	 */
	private int getResourceId(String resourceName, String resourceType) {
		Resources res = this.getResources();
		return res.getIdentifier(resourceName, resourceType, this.getPackageName());
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		FrameLayout layout = new FrameLayout(this);
		FrameLayout.LayoutParams param = new FrameLayout.LayoutParams(-1, -1);

		param.gravity = 48;
		this.surfaceView = new SurfaceView(this);
		layout.addView((View)this.surfaceView, param);
		this.viewfinderView = new ViewfinderView(this);
		this.viewfinderView.setBackgroundColor(0);
		layout.addView((View)this.viewfinderView, param);
		LinearLayout layout2 = new LinearLayout(this);
		layout2.setOrientation(1);
		LinearLayout.LayoutParams param2 = new LinearLayout.LayoutParams(-1, -2);
		layout2.setHorizontalGravity(1);
		TextView text1 = new TextView(this);
		text1.setTextSize(18.0f);
		text1.setGravity(17);
		text1.setTextColor(0);
		text1.setText("  \n  ");
		layout2.addView((View)text1, param2);
		LinearLayout layout4 = new LinearLayout(this);
		layout4.setOrientation(0);
		final Button button1 = new Button(this);
		Button button2 = new Button(this);

		button1.setTextSize(18.0f);
		button2.setTextSize(18.0f);
		button1.setGravity(17);
		button2.setGravity(17);
		button1.setText("开闪光灯");
		button2.setText("取消扫描");
		LinearLayout.LayoutParams param3 = new LinearLayout.LayoutParams(-2, -2);
		layout4.addView((View)button1, param3);
		layout4.addView((View)button2, param3);
		layout2.addView((View)layout4, param2);
		TextView text2 = new TextView(this);
		text2.setTextSize(18.0f);
		text2.setGravity(17);
		text2.setTextColor(-16711936);
		text2.setText("请将图像置于方框内");
		layout2.addView((View)text2, param2);
		FrameLayout.LayoutParams param4 = new FrameLayout.LayoutParams(-2, -2);
		param4.gravity = 1;
		layout.addView((View)layout2, param4);
		this.setContentView(layout, param);

		// 相机预览独立于调用窗口，返回结果后恢复原窗口。
		Intent intent = this.getIntent();
		Bundle bundle = intent.getExtras();

		this.style = bundle.getInt("style");
		this.beep = bundle.getBoolean("beep");
		this.continuous = bundle.getBoolean("continuous");
		this.flashlight = bundle.getBoolean("flashlight");
		if (this.beep) {
			this.player = MediaPlayer.create((Context)this, this.getResourceId("beep", "raw"));
		}

		CameraManager.init(this.getApplication(), this.style);
		this.hasSurface = false;
		this.inactivityTimer = new InactivityTimer(this);
		this.mReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context arg0, Intent arg1) {
				String actionStr = arg1.getAction();
				if (actionStr.equals(getPackageName() + ".barcode.STOP")) {
					CaptureActivity.this.stopscan();
				}
			}
		};
		IntentFilter filter = new IntentFilter();
		filter.addAction(getPackageName() + ".barcode.STOP");
		this.registerReceiver(this.mReceiver, filter);
		receiverRegistered = true;
		if (this.flashlight) {
			new Handler().postDelayed(new Runnable() {

				@Override
				public void run() {
					CameraManager.get().openFlash();
					button1.setText("关闪光灯");
					CaptureActivity.this.flashButtonTitle = "关闪光灯";
				}
			}, 1000L);
		}

		button1.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (button1.getText().toString() == "开闪光灯") {
					CameraManager.get().openFlash();
					button1.setText("关闪光灯");
					CaptureActivity.this.flashButtonTitle = "关闪光灯";
				} else {
					CameraManager.get().closeFlash();
					button1.setText("开闪光灯");
					CaptureActivity.this.flashButtonTitle = "开闪光灯";
				}
			}
		});
		button2.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				CaptureActivity.this.stopscan();
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		SurfaceHolder surfaceHolder = this.surfaceView.getHolder();
		if (this.hasSurface) {
			this.initCamera(surfaceHolder);
		} else {
			surfaceHolder.addCallback(this);
			surfaceHolder.setType(3);
		}

		this.decodeFormats = null;
		this.characterSet = "ISO-8859-1";
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (this.handler != null) {
			this.handler.quitSynchronously();
			this.handler = null;
		}

		CameraManager.get().closeDriver();
	}

	@Override
	protected void onDestroy() {
		this.inactivityTimer.shutdown();
		unregisterStopReceiver();
		if (player != null) {
			player.release();
			player = null;
		}

		super.onDestroy();
	}

	/**
	 * 为扫描页面打开相机和解码流程。
	 *
	 * @param surfaceHolder 相机预览表面
	 */
	private void initCamera(SurfaceHolder surfaceHolder) {
		try {
			CameraManager.get().openDriver(surfaceHolder);
		} catch (IOException ioe) {
			return;
		} catch (RuntimeException e) {
			return;
		}

		if (this.handler == null) {
			this.handler = new CaptureActivityHandler(this, this.decodeFormats, this.characterSet);
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (!this.hasSurface) {
			this.hasSurface = true;
			this.initCamera(holder);
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		this.hasSurface = false;
	}

	/**
	 * 返回扫描取景视图。
	 *
	 * @return 扫描取景视图
	 */
	public ViewfinderView getViewfinderView() {
		return this.viewfinderView;
	}

	/**
	 * 取得接收扫描或解码消息的处理器。
	 *
	 * @return 当前线程的消息处理器
	 */
	public Handler getHandler() {
		return this.handler;
	}

	/**
	 * 刷新扫描取景视图。
	 */
	public void drawViewfinder() {
		this.viewfinderView.drawViewfinder();
	}

	/**
	 * 通知当前应用并按连续扫描设置决定是否重新启动预览。
	 *
	 * @param obj 待处理的对象
	 * @param barcode 识别结果图片
	 */
	public void handleDecode(Result obj, Bitmap barcode) {
		this.inactivityTimer.onActivity();
		Intent intent3 = new Intent();
		intent3.setAction(getPackageName() + ".barcode.RESULT");
		intent3.setPackage(getPackageName());
		intent3.putExtra("code", this.recode(obj.getText()));
		this.sendBroadcast(intent3);
		if (this.beep) {
			this.player.start();
		}

		if (!this.continuous) {
			this.unregisterStopReceiver();
			this.finish();
		} else {
			if (this.handler != null) {
				this.handler.quitSynchronously();
				this.handler = null;
			}

			CameraManager.get().closeDriver();
			SurfaceHolder surfaceHolder = this.surfaceView.getHolder();
			if (this.hasSurface) {
				this.initCamera(surfaceHolder);
			} else {
				surfaceHolder.addCallback(this);
				surfaceHolder.setType(3);
			}

			this.decodeFormats = null;
			this.characterSet = "ISO-8859-1";
			if (this.flashButtonTitle == "关闪光灯") {
				CameraManager.get().openFlash();
			}
		}
	}

	/**
	 * 保留原组件对 UTF-8 和 GB2312 扫描结果的判定方式。
	 *
	 * @param resultStr 识别或操作结果文本
	 * @return 按检测结果转换字符编码后的文本
	 */
	private String recode(String resultStr) {
		String utfText = "";
		String gbText = "";
		boolean isChinese = false;

		try {
			utfText = new String(resultStr.getBytes("ISO-8859-1"), "UTF-8");
			isChinese = this.isChineseCharacter(utfText);
			boolean b = this.isSpecialCharacter(resultStr);
			if (b) {
				isChinese = true;
			}

			if (!isChinese) {
				gbText = new String(resultStr.getBytes("ISO-8859-1"), "GB2312");
				return gbText;
			}

			return utfText;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return "";
		}
	}

	/**
	 * 判断文本是否包含所检查的中文字符。
	 *
	 * @param chineseStr 待检查的文本
	 * @return 文本包含所检查的中文字符时为 {@code true}，否则为 {@code false}
	 */
	public boolean isChineseCharacter(String chineseStr) {
		char[] charArray = chineseStr.toCharArray();

		for (int i = 0; i < charArray.length; ++i) {
			if (charArray[i] >= '\u0000' && charArray[i] < '\uFFFD' || charArray[i] > '\uFFFD' && charArray[i] < '\uFFFF') continue;

			return false;
		}

		return true;
	}

	/**
	 * 判断文本是否包含所检查的特殊字符。
	 *
	 * @param str 待处理文本
	 * @return 文本包含所检查的特殊字符时为 {@code true}，否则为 {@code false}
	 */
	public boolean isSpecialCharacter(String str) {
		return str.contains("???");
	}

	/**
	 * 结束本次扫码并关闭扫描页面。
	 */
	public void stopscan() {
		this.unregisterStopReceiver();
		this.finish();
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
