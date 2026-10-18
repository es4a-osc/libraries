package simple.library.wsd.barcode.internal.zxing.decoding;

import android.content.Intent;
import android.net.Uri;
import com.google.zxing.BarcodeFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;

/**
 * 解析扫描格式并保留原组件的条码格式集合。
 */
final class DecodeFormatManager {
	private static final Pattern COMMA_PATTERN = Pattern.compile(",");
	static final Vector<BarcodeFormat> PRODUCT_FORMATS = new Vector<BarcodeFormat>(5);
	static final Vector<BarcodeFormat> ONE_D_FORMATS;
	static final Vector<BarcodeFormat> QR_CODE_FORMATS;
	static final Vector<BarcodeFormat> DATA_MATRIX_FORMATS;

	private DecodeFormatManager() {
	}

	/**
	 * 解析扫描请求中的解码格式配置。
	 *
	 * @param intent 系统意图
	 * @return 需要识别的条码格式集合
	 */
	static Vector<BarcodeFormat> parseDecodeFormats(Intent intent) {
		List<String> scanFormats = null;
		String scanFormatsString = intent.getStringExtra("SCAN_FORMATS");

		if (scanFormatsString != null) {
			scanFormats = Arrays.asList(COMMA_PATTERN.split(scanFormatsString));
		}

		return DecodeFormatManager.parseDecodeFormats(scanFormats, intent.getStringExtra("SCAN_MODE"));
	}

	/**
	 * 解析扫描请求中的解码格式配置。
	 *
	 * @param inputUri 包含扫描参数的 URI
	 * @return 需要识别的条码格式集合
	 */
	static Vector<BarcodeFormat> parseDecodeFormats(Uri inputUri) {
		List<String> formats = inputUri.getQueryParameters("SCAN_FORMATS");
		if (formats != null && formats.size() == 1 && formats.get(0) != null) {
			formats = Arrays.asList(COMMA_PATTERN.split(formats.get(0)));
		}

		return DecodeFormatManager.parseDecodeFormats(formats, inputUri.getQueryParameter("SCAN_MODE"));
	}

	/**
	 * 解析扫描请求中的解码格式配置。
	 *
	 * @param scanFormats 扫描格式名称列表
	 * @param decodeMode 扫描模式
	 * @return 需要识别的条码格式集合
	 */
	private static Vector<BarcodeFormat> parseDecodeFormats(Iterable<String> scanFormats, String decodeMode) {
		if (scanFormats != null) {
			Vector<BarcodeFormat> formats = new Vector<BarcodeFormat>();

			try {
				for (String format : scanFormats) {
					formats.add(BarcodeFormat.valueOf(format));
				}

				return formats;
			} catch (IllegalArgumentException illegalArgumentException) {
				// 格式名称无效时回退到扫描模式指定的默认格式。
			}
		}

		if (decodeMode != null) {
			if ("PRODUCT_MODE".equals(decodeMode)) {
				return PRODUCT_FORMATS;
			}

			if ("QR_CODE_MODE".equals(decodeMode)) {
				return QR_CODE_FORMATS;
			}

			if ("DATA_MATRIX_MODE".equals(decodeMode)) {
				return DATA_MATRIX_FORMATS;
			}

			if ("ONE_D_MODE".equals(decodeMode)) {
				return ONE_D_FORMATS;
			}
		}

		return null;
	}

	static {
		PRODUCT_FORMATS.add(BarcodeFormat.UPC_A);
		PRODUCT_FORMATS.add(BarcodeFormat.UPC_E);
		PRODUCT_FORMATS.add(BarcodeFormat.EAN_13);
		PRODUCT_FORMATS.add(BarcodeFormat.EAN_8);
		PRODUCT_FORMATS.add(BarcodeFormat.RSS_14);
		ONE_D_FORMATS = new Vector<BarcodeFormat>(PRODUCT_FORMATS.size() + 4);
		ONE_D_FORMATS.addAll(PRODUCT_FORMATS);
		ONE_D_FORMATS.add(BarcodeFormat.CODE_39);
		ONE_D_FORMATS.add(BarcodeFormat.CODE_93);
		ONE_D_FORMATS.add(BarcodeFormat.CODE_128);
		ONE_D_FORMATS.add(BarcodeFormat.ITF);
		QR_CODE_FORMATS = new Vector<BarcodeFormat>(1);
		QR_CODE_FORMATS.add(BarcodeFormat.QR_CODE);
		DATA_MATRIX_FORMATS = new Vector<BarcodeFormat>(1);
		DATA_MATRIX_FORMATS.add(BarcodeFormat.DATA_MATRIX);
	}
}
