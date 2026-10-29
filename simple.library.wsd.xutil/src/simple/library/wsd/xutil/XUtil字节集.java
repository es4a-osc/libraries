package simple.library.wsd.xutil;

import com.xuexiang.xutil.data.ArrayUtils;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * 字节数组拼接、查找与切片。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class XUtil字节集 {

	/**
	 * 函数集仅提供静态成员，不创建实例。
	 */
	private XUtil字节集() {
	}

	/**
	 * 返回两个字节集连接后的新字节集。
	 *
	 * @param data 数据。
	 * @param other 另一数据。
	 * @return 返回的字节集。
	 */
	@SimpleFunction
	public static byte[] 拼接(byte[] data, byte[] other) {
		return ArrayUtils.append(data, other);
	}

	/**
	 * 返回子字节集首次出现的零基索引；不存在返回 -1。
	 *
	 * @param data 数据。
	 * @param other 另一数据。
	 * @return 返回的整数值。
	 */
	@SimpleFunction
	public static int 查找(byte[] data, byte[] other) {
		return ArrayUtils.indexOf(data, other);
	}

	/**
	 * 返回子字节集最后出现的零基索引；不存在返回 -1。
	 *
	 * @param data 数据。
	 * @param other 另一数据。
	 * @return 返回的整数值。
	 */
	@SimpleFunction
	public static int 反向查找(byte[] data, byte[] other) {
		return ArrayUtils.lastIndexOf(data, other);
	}

	/**
	 * 按左闭右开的零基范围复制；越界拒绝，不自动补零。
	 *
	 * @param data 数据。
	 * @param start 起始索引。
	 * @param end 结束索引。
	 * @return 返回的字节集。
	 */
	@SimpleFunction
	public static byte[] 切片(byte[] data, int start, int end) {
		if (start < 0 || end < start || end > data.length) {
			throw new IllegalArgumentException("字节集范围越界");
		}
		return ArrayUtils.copyOfRange(data, start, end);
	}
}
