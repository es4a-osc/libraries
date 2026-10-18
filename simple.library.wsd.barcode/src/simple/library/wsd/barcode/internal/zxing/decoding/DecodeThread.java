package simple.library.wsd.barcode.internal.zxing.decoding;

import android.os.Handler;
import android.os.Looper;
import simple.library.wsd.barcode.internal.zxing.CaptureActivity;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.ResultPointCallback;
import java.util.Hashtable;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;

/**
 * 创建持有解码处理器的消息循环线程。
 */
final class DecodeThread extends Thread {
	public static final String BARCODE_BITMAP = "barcode_bitmap";
	private final CaptureActivity activity;
	private final Hashtable<DecodeHintType, Object> hints;
	private Handler handler;
	private final CountDownLatch handlerInitLatch;

	DecodeThread(CaptureActivity activity, Vector<BarcodeFormat> decodeFormats, String characterSet, ResultPointCallback resultPointCallback) {
		this.activity = activity;
		this.handlerInitLatch = new CountDownLatch(1);
		this.hints = new Hashtable<DecodeHintType, Object>(3);
		if (decodeFormats == null || decodeFormats.isEmpty()) {
			decodeFormats = new Vector<BarcodeFormat>();
			decodeFormats.addAll(DecodeFormatManager.ONE_D_FORMATS);
			decodeFormats.addAll(DecodeFormatManager.QR_CODE_FORMATS);
			decodeFormats.addAll(DecodeFormatManager.DATA_MATRIX_FORMATS);
		}

		this.hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);
		if (characterSet != null) {
			this.hints.put(DecodeHintType.CHARACTER_SET, characterSet);
		}

		this.hints.put(DecodeHintType.NEED_RESULT_POINT_CALLBACK, resultPointCallback);
	}

	/**
	 * 取得接收扫描或解码消息的处理器。
	 *
	 * @return 当前线程的消息处理器
	 */
	Handler getHandler() {
		try {
			this.handlerInitLatch.await();
		} catch (InterruptedException interruptedException) {
			// 与原实现一致：等待被打断时返回当前处理器状态。
		}

		return this.handler;
	}

	@Override
	public void run() {
		Looper.prepare();
		this.handler = new DecodeHandler(this.activity, this.hints);
		this.handlerInitLatch.countDown();
		Looper.loop();
	}
}
