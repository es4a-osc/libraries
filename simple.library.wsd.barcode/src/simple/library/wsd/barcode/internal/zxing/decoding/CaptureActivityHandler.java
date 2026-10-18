package simple.library.wsd.barcode.internal.zxing.decoding;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import simple.library.wsd.barcode.internal.zxing.CaptureActivity;
import simple.library.wsd.barcode.internal.zxing.camera.CameraManager;
import simple.library.wsd.barcode.internal.zxing.view.ViewfinderResultPointCallback;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import java.util.Vector;

/**
 * 协调相机预览、自动对焦和解码消息。
 */
public final class CaptureActivityHandler extends Handler {
	private static final String TAG = CaptureActivityHandler.class.getSimpleName();
	private final CaptureActivity activity;
	private final DecodeThread decodeThread;
	private State state;

	public CaptureActivityHandler(CaptureActivity activity, Vector<BarcodeFormat> decodeFormats, String characterSet) {
		this.activity = activity;
		this.decodeThread = new DecodeThread(activity, decodeFormats, characterSet, new ViewfinderResultPointCallback(activity.getViewfinderView()));
		this.decodeThread.start();
		this.state = State.SUCCESS;
		CameraManager.get().startPreview();
		this.restartPreviewAndDecode();
	}

	@Override
	public void handleMessage(Message message) {
		switch (message.what) {
			case 2131099648: {
				if (this.state != State.PREVIEW) break;

				CameraManager.get().requestAutoFocus(this, 2131099648);
				break;
			}
			case 2131099656: {
				Log.d(TAG, "Got restart preview message");
				this.restartPreviewAndDecode();
				break;
			}
			case 2131099651: {
				Log.d(TAG, "Got decode succeeded message");
				this.state = State.SUCCESS;
				Bundle bundle = message.getData();
				Bitmap barcode = bundle == null ? null : (Bitmap)bundle.getParcelable("barcode_bitmap");

				this.activity.handleDecode((Result)message.obj, barcode);
				break;
			}
			case 2131099650: {
				this.state = State.PREVIEW;
				CameraManager.get().requestPreviewFrame(this.decodeThread.getHandler(), 2131099649);
				break;
			}
			case 2131099657: {
				Log.d(TAG, "Got return scan result message");
				this.activity.setResult(-1, (Intent)message.obj);
				this.activity.finish();
				break;
			}
			case 2131099654: {
				Log.d(TAG, "Got product query message");
				String url = (String)message.obj;
				Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(url));

				intent.addFlags(524288);
				this.activity.startActivity(intent);
			}
		}
	}

	/**
	 * 停止预览并等待解码线程退出，移除尚未派发的结果消息。
	 */
	public void quitSynchronously() {
		this.state = State.DONE;
		CameraManager.get().stopPreview();
		Message quit = Message.obtain(this.decodeThread.getHandler(), 2131099655);
		quit.sendToTarget();

		try {
			this.decodeThread.join();
		} catch (InterruptedException interruptedException) {
			// 沿用原实现的容错分支，后续按原流程继续。
		}

		this.removeMessages(2131099651);
		this.removeMessages(2131099650);
	}

	private void restartPreviewAndDecode() {
		if (this.state == State.SUCCESS) {
			this.state = State.PREVIEW;
			CameraManager.get().requestPreviewFrame(this.decodeThread.getHandler(), 2131099649);
			CameraManager.get().requestAutoFocus(this, 2131099648);
			this.activity.drawViewfinder();
		}
	}

	private static enum State {
		PREVIEW,
		SUCCESS,
		DONE;

	}
}
