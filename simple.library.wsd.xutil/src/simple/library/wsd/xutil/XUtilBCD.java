package simple.library.wsd.xutil;

import com.xuexiang.xutil.data.BCDUtils;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * 十进制数字文本与压缩 BCD 字节集转换。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class XUtilBCD {

	/**
	 * 函数集仅提供静态成员，不创建实例。
	 */
	private XUtilBCD() {
	}

	/**
	 * 编码非空偶数位数字文本为 BCD；非法字符或奇数长度拒绝。
	 *
	 * @param text 文本。
	 * @return 返回的字节集。
	 */
	@SimpleFunction
	public static byte[] 编码(String text) {
		if (text == null || text.isEmpty() || (text.length() & 1) != 0 || !text.matches("[0-9]+")) {
			throw new IllegalArgumentException("BCD 文本必须为偶数位数字");
		}
		return BCDUtils.string2Bcd(text);
	}

	/**
	 * 解码压缩 BCD 为数字文本；任一半字节大于 9 时拒绝。
	 *
	 * @param data 数据。
	 * @return 返回的文本。
	 */
	@SimpleFunction
	public static String 解码(byte[] data) {
		for (byte value : data) {
			if ((value & 15) > 9 || ((value & 255) >>> 4) > 9) {
				throw new IllegalArgumentException("BCD 包含非十进制半字节");
			}
		}
		return BCDUtils.bcd2String(data);
	}
}
