package simple.library.wsd.xutil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.xuexiang.xutil.data.ArrayUtils;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * 字节数组拼接、查找、切片、十六进制及大小端整数转换。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class XU字节集 {

	/**
	 * 函数集仅提供静态成员，不创建实例。
	 */
	private XU字节集() {
	}

	/**
	 * 返回两个字节集连接后的新字节集。
	 *
	 * @param data  数据。
	 * @param other 另一数据。
	 * @return 返回的字节集。
	 */
	@SimpleFunction
	public static byte[] 拼接(byte[] data, byte[] other) {
		return ArrayUtils.append(data, other);
	}

	/**
	 * 返回子字节集首次出现的零基索引；不存在返回 -1，空模式返回0。
	 *
	 * @param data  数据。
	 * @param other 另一数据。
	 * @return 返回的整数值。
	 */
	@SimpleFunction
	public static int 查找(byte[] data, byte[] other) {
		return other.length == 0 ? 0 : ArrayUtils.indexOf(data, other);
	}

	/**
	 * 返回子字节集最后出现的零基索引；不存在返回 -1，空模式返回数据长度。
	 *
	 * @param data  数据。
	 * @param other 另一数据。
	 * @return 返回的整数值。
	 */
	@SimpleFunction
	public static int 反向查找(byte[] data, byte[] other) {
		// 上游 KMP 会提前返回且跳过重叠匹配；从尾部逐项比较以保证最后一次出现的含义。
		outer: for (int i = data.length - other.length; i >= 0; i--) {
			for (int j = 0; j < other.length; j++) {
				if (data[i + j] != other[j]) {
					continue outer;
				}
			}
			return i;
		}
		return -1;
	}

	/**
	 * 按左闭右开的零基范围复制；越界拒绝，不自动补零。
	 *
	 * @param data  数据。
	 * @param start 起始索引。
	 * @param end   结束索引。
	 * @return 返回的字节集。
	 */
	@SimpleFunction
	public static byte[] 切片(byte[] data, int start, int end) {
		if (start < 0 || end < start || end > data.length) {
			throw new IllegalArgumentException("字节集范围越界");
		}
		return ArrayUtils.copyOfRange(data, start, end);
	}

	/**
	 * 转换为大写十六进制，空字节集返回空文本。
	 *
	 * @param data 数据。
	 * @return 转换为大写十六进制，空字节集返回空文本。
	 */
	@SimpleFunction
	public static String 到十六进制(byte[] data) {
		char[] hex = "0123456789ABCDEF".toCharArray();
		char[] out = new char[data.length * 2];
		for (int i = 0; i < data.length; i++) {
			out[i * 2] = hex[(data[i] & 255) >>> 4];
			out[i * 2 + 1] = hex[data[i] & 15];
		}
		return new String(out);
	}

	/**
	 * 解析偶数位十六进制，空文本返回空字节集；非法字符拒绝。
	 *
	 * @param text 文本。
	 * @return 解析偶数位十六进制，空文本返回空字节集；非法字符拒绝。
	 */
	@SimpleFunction
	public static byte[] 从十六进制(String text) {
		if ((text.length() & 1) != 0) {
			throw new IllegalArgumentException("十六进制长度必须为偶数");
		}
		byte[] out = new byte[text.length() / 2];
		for (int i = 0; i < out.length; i++) {
			int high = Character.digit(text.charAt(i * 2), 16);
			int low = Character.digit(text.charAt(i * 2 + 1), 16);
			if (high < 0 || low < 0) {
				throw new IllegalArgumentException("包含非十六进制字符");
			}
			out[i] = (byte) ((high << 4) | low);
		}
		return out;
	}

	/**
	 * 将短整数编码为固定 2 字节的二进制。
	 *
	 * @param value        数值。
	 * @param littleEndian 真为低字节在前；假为大端序。
	 * @return 将短整数编码为固定 2 字节的二进制。
	 */
	@SimpleFunction
	public static byte[] 短整数到字节集(int value, boolean littleEndian) {
		if (value < Short.MIN_VALUE || value > Short.MAX_VALUE) {
			throw new IllegalArgumentException("短整数越界");
		}
		return ByteBuffer.allocate(2).order(littleEndian ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN)
				.putShort((short) value).array();
	}

	/**
	 * 从完整范围读取有符号短整数，不足 2 字节时拒绝。
	 *
	 * @param data         数据。
	 * @param offset       零基字节偏移。
	 * @param littleEndian 小端序。
	 * @return 从完整范围读取有符号短整数，不足 2 字节时拒绝。
	 */
	@SimpleFunction
	public static int 读短整数(byte[] data, int offset, boolean littleEndian) {
		if (offset < 0 || offset > data.length - 2) {
			throw new IllegalArgumentException("字节集范围越界");
		}
		return ByteBuffer.wrap(data, offset, 2).order(littleEndian ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN)
				.getShort();
	}

	/**
	 * 将整数编码为固定 4 字节的二进制。
	 *
	 * @param value        数值。
	 * @param littleEndian 真为低字节在前；假为大端序。
	 * @return 将整数编码为固定 4 字节的二进制。
	 */
	@SimpleFunction
	public static byte[] 整数到字节集(int value, boolean littleEndian) {
		return ByteBuffer.allocate(4).order(littleEndian ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN).putInt(value)
				.array();
	}

	/**
	 * 从完整范围读取有符号整数，不足 4 字节时拒绝。
	 *
	 * @param data         数据。
	 * @param offset       零基字节偏移。
	 * @param littleEndian 小端序。
	 * @return 从完整范围读取有符号整数，不足 4 字节时拒绝。
	 */
	@SimpleFunction
	public static int 读整数(byte[] data, int offset, boolean littleEndian) {
		if (offset < 0 || offset > data.length - 4) {
			throw new IllegalArgumentException("字节集范围越界");
		}
		return ByteBuffer.wrap(data, offset, 4).order(littleEndian ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN)
				.getInt();
	}

	/**
	 * 将长整数编码为固定 8 字节的二进制。
	 *
	 * @param value        数值。
	 * @param littleEndian 真为低字节在前；假为大端序。
	 * @return 将长整数编码为固定 8 字节的二进制。
	 */
	@SimpleFunction
	public static byte[] 长整数到字节集(long value, boolean littleEndian) {
		return ByteBuffer.allocate(8).order(littleEndian ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN)
				.putLong(value).array();
	}

	/**
	 * 从完整范围读取有符号长整数，不足 8 字节时拒绝。
	 *
	 * @param data         数据。
	 * @param offset       零基字节偏移。
	 * @param littleEndian 小端序。
	 * @return 从完整范围读取有符号长整数，不足 8 字节时拒绝。
	 */
	@SimpleFunction
	public static long 读长整数(byte[] data, int offset, boolean littleEndian) {
		if (offset < 0 || offset > data.length - 8) {
			throw new IllegalArgumentException("字节集范围越界");
		}
		return ByteBuffer.wrap(data, offset, 8).order(littleEndian ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN)
				.getLong();
	}
}
