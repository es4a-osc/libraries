package simple.library.wsd.xutil;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.xuexiang.xutil.common.BigDecimalUtils;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * 十进制文本运算及金额格式化，避免小数通过二进制浮点输入丢失精度。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class XUtil大数 {

	/**
	 * 函数集仅提供静态成员，不创建实例。
	 */
	private XUtil大数() {
	}

	/**
	 * 计算两段十进制数值文本，结果用普通十进制表示。
	 *
	 * @param first 第一文本。
	 * @param second 第二文本。
	 * @return 返回的文本。
	 */
	@SimpleFunction
	public static String 加(String first, String second) {
		return new BigDecimal(first).add(new BigDecimal(second)).toPlainString();
	}

	/**
	 * 计算两段十进制数值文本，结果用普通十进制表示。
	 *
	 * @param first 第一文本。
	 * @param second 第二文本。
	 * @return 返回的文本。
	 */
	@SimpleFunction
	public static String 减(String first, String second) {
		return new BigDecimal(first).subtract(new BigDecimal(second)).toPlainString();
	}

	/**
	 * 计算两段十进制数值文本，结果用普通十进制表示。
	 *
	 * @param first 第一文本。
	 * @param second 第二文本。
	 * @return 返回的文本。
	 */
	@SimpleFunction
	public static String 乘(String first, String second) {
		return new BigDecimal(first).multiply(new BigDecimal(second)).toPlainString();
	}

	/**
	 * 十进制除法，按四舍五入保留指定小数位；除零抛出异常。
	 *
	 * @param first 第一文本。
	 * @param second 第二文本。
	 * @param scale 小数位数。
	 * @return 返回的文本。
	 */
	@SimpleFunction
	public static String 除(String first, String second, int scale) {
		XUtilSupport.nonNegative(scale);
		return new BigDecimal(first).divide(new BigDecimal(second), scale, RoundingMode.HALF_UP).toPlainString();
	}

	/**
	 * 按四舍五入保留指定小数位。
	 *
	 * @param text 文本。
	 * @param scale 小数位数。
	 * @return 返回的文本。
	 */
	@SimpleFunction
	public static String 舍入(String text, int scale) {
		XUtilSupport.nonNegative(scale);
		return new BigDecimal(text).setScale(scale, RoundingMode.HALF_UP).toPlainString();
	}

	/**
	 * 比较十进制数，返回负数、零或正数。
	 *
	 * @param first 第一文本。
	 * @param second 第二文本。
	 * @return 返回的整数值。
	 */
	@SimpleFunction
	public static int 比较(String first, String second) {
		return new BigDecimal(first).compareTo(new BigDecimal(second));
	}

	/**
	 * 按上游金额规则添加分组分隔符。
	 *
	 * @param text 文本。
	 * @param separator 分隔符。
	 * @return 返回的文本。
	 */
	@SimpleFunction
	public static String 金额格式化(String text, String separator) {
		return BigDecimalUtils.formatMoney(new BigDecimal(text), separator);
	}
}
