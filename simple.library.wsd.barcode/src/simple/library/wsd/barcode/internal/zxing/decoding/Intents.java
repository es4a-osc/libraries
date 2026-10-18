package simple.library.wsd.barcode.internal.zxing.decoding;

/**
 * 保留原扫描实现使用的 Intent 参数名称。
 */
public final class Intents {

	private Intents() {
	}

	/**
	 * 分享意图使用的参数名称。
	 */
	public static final class Share {
		public static final String ACTION = "com.google.zxing.client.android.SHARE";

		private Share() {
		}
	}

	/**
	 * 无线网络连接意图使用的参数名称。
	 */
	public static final class WifiConnect {
		public static final String ACTION = "com.google.zxing.client.android.WIFI_CONNECT";
		public static final String SSID = "SSID";
		public static final String TYPE = "TYPE";
		public static final String PASSWORD = "PASSWORD";

		private WifiConnect() {
		}
	}

	/**
	 * 图书内容搜索意图使用的参数名称。
	 */
	public static final class SearchBookContents {
		public static final String ACTION = "com.google.zxing.client.android.SEARCH_BOOK_CONTENTS";
		public static final String ISBN = "ISBN";
		public static final String QUERY = "QUERY";

		private SearchBookContents() {
		}
	}

	/**
	 * 条码编码意图使用的参数名称。
	 */
	public static final class Encode {
		public static final String ACTION = "com.google.zxing.client.android.ENCODE";
		public static final String DATA = "ENCODE_DATA";
		public static final String TYPE = "ENCODE_TYPE";
		public static final String FORMAT = "ENCODE_FORMAT";

		private Encode() {
		}
	}

	/**
	 * 条码扫描意图使用的参数名称。
	 */
	public static final class Scan {
		public static final String ACTION = "com.google.zxing.client.android.SCAN";
		public static final String MODE = "SCAN_MODE";
		public static final String SCAN_FORMATS = "SCAN_FORMATS";
		public static final String CHARACTER_SET = "CHARACTER_SET";
		public static final String PRODUCT_MODE = "PRODUCT_MODE";
		public static final String ONE_D_MODE = "ONE_D_MODE";
		public static final String QR_CODE_MODE = "QR_CODE_MODE";
		public static final String DATA_MATRIX_MODE = "DATA_MATRIX_MODE";
		public static final String RESULT = "SCAN_RESULT";
		public static final String RESULT_FORMAT = "SCAN_RESULT_FORMAT";
		public static final String SAVE_HISTORY = "SAVE_HISTORY";

		private Scan() {
		}
	}
}
