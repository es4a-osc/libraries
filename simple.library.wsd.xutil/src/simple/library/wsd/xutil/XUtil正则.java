package simple.library.wsd.xutil;

import com.xuexiang.xutil.common.RegexUtils;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * 正则匹配、提取、分割与替换。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class XUtil正则 {

	/**
	 * 函数集仅提供静态成员，不创建实例。
	 */
	private XUtil正则() {
	}

	/**
	 * 判断整段文本是否匹配表达式。
	 *
	 * @param pattern Java 正则表达式，格式错误时抛出异常。
	 * @param text 文本。
	 * @return 判断或操作的结果。
	 */
	@SimpleFunction
	public static boolean 匹配(String pattern, String text) {
		return RegexUtils.isMatch(pattern, text);
	}

	/**
	 * 按出现顺序提取全部匹配文本。
	 *
	 * @param pattern Java 正则表达式，格式错误时抛出异常。
	 * @param text 文本。
	 * @return 返回的文本数组。
	 */
	@SimpleFunction
	public static String[] 提取(String pattern, String text) {
		return RegexUtils.getMatches(pattern, text).toArray(new String[0]);
	}

	/**
	 * 按表达式分割文本，末尾空项按 Java 分割规则处理。
	 *
	 * @param text 文本。
	 * @param pattern Java 正则表达式，格式错误时抛出异常。
	 * @return 返回的文本数组。
	 */
	@SimpleFunction
	public static String[] 分割(String text, String pattern) {
		return RegexUtils.getSplits(text, pattern);
	}

	/**
	 * 替换匹配内容；替换文本支持 $1 等捕获组引用。
	 *
	 * @param text 文本。
	 * @param pattern Java 正则表达式，格式错误时抛出异常。
	 * @param replacement 替换文本。
	 * @return 返回的文本。
	 */
	@SimpleFunction
	public static String 替换首次(String text, String pattern, String replacement) {
		return RegexUtils.getReplaceFirst(text, pattern, replacement);
	}

	/**
	 * 替换匹配内容；替换文本支持 $1 等捕获组引用。
	 *
	 * @param text 文本。
	 * @param pattern Java 正则表达式，格式错误时抛出异常。
	 * @param replacement 替换文本。
	 * @return 返回的文本。
	 */
	@SimpleFunction
	public static String 替换全部(String text, String pattern, String replacement) {
		return RegexUtils.getReplaceAll(text, pattern, replacement);
	}

	/**
	 * 按 XUtil 内置规则检查文本格式；不验证数据真实性。
	 *
	 * @param text 文本。
	 * @return 判断或操作的结果。
	 */
	@SimpleFunction
	public static boolean 是否邮箱(String text) {
		return RegexUtils.isEmail(text);
	}

	/**
	 * 按 XUtil 内置规则检查文本格式；不验证数据真实性。
	 *
	 * @param text 文本。
	 * @return 判断或操作的结果。
	 */
	@SimpleFunction
	public static boolean 是否网址(String text) {
		return RegexUtils.isURL(text);
	}

	/**
	 * 按 XUtil 内置规则检查文本格式；不验证数据真实性。
	 *
	 * @param text 文本。
	 * @return 判断或操作的结果。
	 */
	@SimpleFunction
	public static boolean 是否IP地址(String text) {
		return RegexUtils.isIP(text);
	}

	/**
	 * 按 XUtil 内置规则检查文本格式；不验证数据真实性。
	 *
	 * @param text 文本。
	 * @return 判断或操作的结果。
	 */
	@SimpleFunction
	public static boolean 是否手机号码(String text) {
		return RegexUtils.isMobileSimple(text);
	}

	/**
	 * 按 XUtil 内置规则检查文本格式；不验证数据真实性。
	 *
	 * @param text 文本。
	 * @return 判断或操作的结果。
	 */
	@SimpleFunction
	public static boolean 是否中文(String text) {
		return RegexUtils.isZh(text);
	}
}
