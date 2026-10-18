package simple.library.wsd.barcode.internal.zxing.decoding;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import simple.library.wsd.barcode.internal.zxing.CaptureActivity;
import simple.library.wsd.barcode.internal.zxing.camera.CameraManager;
import simple.library.wsd.barcode.internal.zxing.camera.PlanarYUVLuminanceSource;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import java.util.Hashtable;

/**
 * 旋转预览帧并使用 ZXing 解码，返回原组件消息格式。
 */
final class DecodeHandler extends Handler {
	private static final String TAG = DecodeHandler.class.getSimpleName();
	private final CaptureActivity activity;
	private final MultiFormatReader multiFormatReader = new MultiFormatReader();

	DecodeHandler(CaptureActivity activity, Hashtable<DecodeHintType, Object> hints) {
		this.multiFormatReader.setHints(hints);
		this.activity = activity;
	}

	@Override
	public void handleMessage(Message message) {
		switch (message.what) {
			case 2131099649: {
				this.decode((byte[])message.obj, message.arg1, message.arg2);
				break;
			}
			case 2131099655: {
				Looper.myLooper().quit();
			}
		}
	}

	/**
	 * 无论识别成功与否都重置读取器，下一帧使用相同提示重新识别。
	 *
	 * @param data 本次传入的数据
	 * @param width 宽度
	 * @param height 高度
	 */
	private void decode(byte[] data, int width, int height) {
		long start = System.currentTimeMillis();
		Result rawResult = null;
		byte[] rotatedData = new byte[data.length];

		// 按竖屏识别方向旋转预览亮度数据，随后交换宽高供解码器使用。
		for (int y = 0; y < height; ++y) {
			for (int x = 0; x < width; ++x) {
				rotatedData[x * height + height - y - 1] = data[x + y * width];
			}
		}

		int tmp = width;
		width = height;
		height = tmp;
		PlanarYUVLuminanceSource source = CameraManager.get().buildLuminanceSource(rotatedData, width, height);
		BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

		try {
			rawResult = this.multiFormatReader.decodeWithState(bitmap);
		} catch (ReaderException readerException) {
		} finally {
			this.multiFormatReader.reset();
		}

		// 识别结果交回扫描界面；未识别到内容时由失败消息驱动后续取帧。
		if (rawResult != null) {
			long end = System.currentTimeMillis();
			Log.d(TAG, "Found barcode (" + (end - start) + " ms):\n" + rawResult.toString());
			Message message = Message.obtain(this.activity.getHandler(), 2131099651, rawResult);
			Bundle bundle = new Bundle();

			bundle.putParcelable("barcode_bitmap", source.renderCroppedGreyscaleBitmap());
			message.setData(bundle);
			message.sendToTarget();
		} else {
			Message message = Message.obtain(this.activity.getHandler(), 2131099650);
			message.sendToTarget();
		}
	}
}
