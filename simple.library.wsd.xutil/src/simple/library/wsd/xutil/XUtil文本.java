package simple.library.wsd.xutil;

import com.xuexiang.xutil.common.StringUtils;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * 文本判空、整理、转换及版本名称比较。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class XUtil文本 {

	/**
	 * 函数集仅提供静态成员，不创建实例。
	 */
	private XUtil文本() {
	}

	/**
	 * 判断文本是否为空或长度为零。
	 *
	 * @param text 文本。
	 * @return 判断或操作的结果。
	 */
	@SimpleFunction
	public static boolean 是否空(String text) {
		return StringUtils.isEmpty(text);
	}

	/**
	 * 判断文本是否为空或仅包含空白字符。
	 *
	 * @param text 文本。
	 * @return 判断或操作的结果。
	 */
	@SimpleFunction
	public static boolean 是否空白(String text) {
		return StringUtils.isSpace(text);
	}

	/**
	 * 去除文本首尾空白，空引用返回空文本。
	 *
	 * @param text 文本。
	 * @return 返回的文本。
	 */
	@SimpleFunction
	public static String 去首尾空白(String text) {
		return StringUtils.getStringTrim(text);
	}

	/**
	 * 去除空白字符，包括换行及制表符。
	 *
	 * @param text 文本。
	 * @return 返回的文本。
	 */
	@SimpleFunction
	public static String 去全部空白(String text) {
		return StringUtils.replaceBlank(text);
	}

	/**
	 * 按上游规则反转文本。
	 *
	 * @param text 文本。
	 * @return 返回的文本。
	 */
	@SimpleFunction
	public static String 反转(String text) {
		return StringUtils.reverse(text);
	}

	/**
	 * 将首个字母转换为大写。
	 *
	 * @param text 文本。
	 * @return 返回的文本。
	 */
	@SimpleFunction
	public static String 首字母大写(String text) {
		return StringUtils.upperFirstLetter(text);
	}

	/**
	 * 将首个字母转换为小写。
	 *
	 * @param text 文本。
	 * @return 返回的文本。
	 */
	@SimpleFunction
	public static String 首字母小写(String text) {
		return StringUtils.lowerFirstLetter(text);
	}

	/**
	 * 忽略大小写比较两段文本。
	 *
	 * @param first 第一文本。
	 * @param second 第二文本。
	 * @return 判断或操作的结果。
	 */
	@SimpleFunction
	public static boolean 忽略大小写比较(String first, String second) {
		return StringUtils.equalsIgnoreCase(first, second);
	}

	/**
	 * 比较点分隔的数字版本；大于、等于、小于分别返回正数、零、负数。
	 *
	 * @param first 第一文本。
	 * @param second 第二文本。
	 * @return 返回的整数值。
	 */
	@SimpleFunction
	public static int 比较版本(String first, String second) {
		return StringUtils.compareVersionName(first, second);
	}

	/**
	 * 转换数值，失败时返回指定默认值。
	 *
	 * @param text 文本。
	 * @param fallback 默认值。
	 * @return 返回的整数值。
	 */
	@SimpleFunction
	public static int 到整数(String text, int fallback) {
		return StringUtils.toInt(text, fallback);
	}

	/**
	 * 转换数值，失败时返回指定默认值。
	 *
	 * @param text 文本。
	 * @param fallback 默认值。
	 * @return 返回的长整数值。
	 */
	@SimpleFunction
	public static long 到长整数(String text, long fallback) {
		return StringUtils.toLong(text, fallback);
	}

	/**
	 * 转换数值，失败时返回指定默认值。
	 *
	 * @param text 文本。
	 * @param fallback 默认值。
	 * @return 返回的双精度数值。
	 */
	@SimpleFunction
	public static double 到小数(String text, double fallback) {
		return StringUtils.toDouble(text, fallback);
	}
}
