package simple.library.wsd.barcode.internal.zxing.decoding;

import android.app.Activity;
import android.content.DialogInterface;

/**
 * 在扫描空闲超时后关闭扫描界面。
 */
public final class FinishListener implements DialogInterface.OnClickListener,
DialogInterface.OnCancelListener,
Runnable {
	private final Activity activityToFinish;

	public FinishListener(Activity activityToFinish) {
		this.activityToFinish = activityToFinish;
	}

	@Override
	public void onCancel(DialogInterface dialogInterface) {
		this.run();
	}

	@Override
	public void onClick(DialogInterface dialogInterface, int i) {
		this.run();
	}

	@Override
	public void run() {
		this.activityToFinish.finish();
	}
}
