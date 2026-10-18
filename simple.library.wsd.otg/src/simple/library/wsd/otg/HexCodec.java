package simple.library.wsd.otg;

/**
 * 十六进制转换的纯规则，保留旧库的小写、尾空格和奇数位补零约定。
 */
final class HexCodec {

	private HexCodec() {
	}

	/* 十六进制转换 */

	static String encode(byte[] data, int length) {
		if (data == null) return "";
		if (length < 0 || length > data.length) throw new IllegalArgumentException("转换长度超出字节集范围");

		StringBuilder result = new StringBuilder();

		// 保留旧协议的小写十六进制和每字节尾空格格式。
		final char[] digits = "0123456789abcdef".toCharArray();

		for (int index = 0; index < length; index++) {
			int value = data[index] & 255;
			result.append(digits[value >>> 4]).append(digits[value & 15]).append(' ');
		}

		return result.toString();
	}

	static byte[] decode(String source) {
		if (source == null) return new byte[0];

		String value = source.replaceAll("\\s", "");
		byte[] result = new byte[(value.length() + 1) / 2];

		for (int index = 0; index < value.length(); index++) {
			char character = value.charAt(index);
			int digit = character >= '0' && character <= '9' ? character - '0'
			: character >= 'a' && character <= 'f' ? character - 'a' + 10
			: character >= 'A' && character <= 'F' ? character - 'A' + 10 : -1;
			if (digit < 0) throw new IllegalArgumentException("包含无效的十六进制字符");

			// 先写高四位再写低四位，奇数个字符时末字节低四位保留为零。
			result[index / 2] |= (byte) (index % 2 == 0 ? digit << 4 : digit);
		}

		return result;
	}
}
