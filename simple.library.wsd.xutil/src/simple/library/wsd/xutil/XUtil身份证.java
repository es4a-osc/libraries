package simple.library.wsd.xutil;

import com.xuexiang.xutil.common.IDCardUtils;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * 中国居民身份证本地格式与校验位检查、生日及脱敏，不代替联网实名核验。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class XUtil身份证 {

	/**
	 * 函数集仅提供静态成员，不创建实例。
	 */
	private XUtil身份证() {
	}

	/**
	 * 按 XUtil 本地规则校验身份证号码；提取信息前应先校验。
	 *
	 * @param text 文本。
	 * @return 判断或操作的结果。
	 */
	@SimpleFunction
	public static boolean 校验(String text) {
		return IDCardUtils.validateCard(text);
	}

	/**
	 * 按 XUtil 本地规则取生日身份证号码；提取信息前应先校验。
	 *
	 * @param text 文本。
	 * @return 返回的文本。
	 */
	@SimpleFunction
	public static String 取生日(String text) {
		return IDCardUtils.getBirthByIdCard(text);
	}

	/**
	 * 按 XUtil 本地规则取性别身份证号码；提取信息前应先校验。
	 *
	 * @param text 文本。
	 * @return 返回的文本。
	 */
	@SimpleFunction
	public static String 取性别(String text) {
		return IDCardUtils.getGenderByIdCard(text);
	}

	/**
	 * 按 XUtil 本地规则取省份身份证号码；提取信息前应先校验。
	 *
	 * @param text 文本。
	 * @return 返回的文本。
	 */
	@SimpleFunction
	public static String 取省份(String text) {
		return IDCardUtils.getProvinceByIdCard(text);
	}

	/**
	 * 按 XUtil 本地规则脱敏身份证号码；提取信息前应先校验。
	 *
	 * @param text 文本。
	 * @return 返回的文本。
	 */
	@SimpleFunction
	public static String 脱敏(String text) {
		return IDCardUtils.hideIdCard(text);
	}
}
