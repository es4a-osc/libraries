package simple.library.wsd.xutil;

import com.xuexiang.xutil.common.RandomUtils;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * 一般用途随机数和随机文本，不用于密码或安全令牌。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class XUtil随机 {

	/**
	 * 函数集仅提供静态成员，不创建实例。
	 */
	private XUtil随机() {
	}

	/**
	 * 取左闭右开的随机整数；上下限相等时返回该值。
	 *
	 * @param minimum 下限。
	 * @param maximum 上限。
	 * @return 返回的整数值。
	 */
	@SimpleFunction
	public static int 取整数(int minimum, int maximum) {
		if (maximum < minimum || (long) maximum - minimum > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("随机范围无效或过大");
		}
		return RandomUtils.getRandom(minimum, maximum);
	}

	/**
	 * 生成指定长度的随机文本；长度必须非负。
	 *
	 * @param length 长度。
	 * @return 返回的文本。
	 */
	@SimpleFunction
	public static String 取数字文本(int length) {
		XUtilSupport.nonNegative(length);
		return RandomUtils.getRandomNumbers(length);
	}

	/**
	 * 生成指定长度的随机文本；长度必须非负。
	 *
	 * @param length 长度。
	 * @return 返回的文本。
	 */
	@SimpleFunction
	public static String 取字母文本(int length) {
		XUtilSupport.nonNegative(length);
		return RandomUtils.getRandomLetters(length);
	}

	/**
	 * 生成指定长度的随机文本；长度必须非负。
	 *
	 * @param length 长度。
	 * @return 返回的文本。
	 */
	@SimpleFunction
	public static String 取大写字母(int length) {
		XUtilSupport.nonNegative(length);
		return RandomUtils.getRandomCapitalLetters(length);
	}

	/**
	 * 生成指定长度的随机文本；长度必须非负。
	 *
	 * @param length 长度。
	 * @return 返回的文本。
	 */
	@SimpleFunction
	public static String 取小写字母(int length) {
		XUtilSupport.nonNegative(length);
		return RandomUtils.getRandomLowerCaseLetters(length);
	}

	/**
	 * 生成指定长度的随机文本；长度必须非负。
	 *
	 * @param length 长度。
	 * @return 返回的文本。
	 */
	@SimpleFunction
	public static String 取数字字母(int length) {
		XUtilSupport.nonNegative(length);
		return RandomUtils.getRandomNumbersAndLetters(length);
	}
}
