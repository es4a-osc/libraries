package simple.library.wsd.xutil;

import android.graphics.Color;

import com.xuexiang.xutil.display.ColorUtils;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * 颜色明暗、透明比例与文本转换。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class XUtil颜色 {

	/**
	 * 函数集仅提供静态成员，不创建实例。
	 */
	private XUtil颜色() {
	}

	/**
	 * 取得颜色十六进制文本。
	 *
	 * @param color ARGB 整数颜色。
	 * @return 返回的文本。
	 */
	@SimpleFunction
	public static String 到文本(int color) {
		return ColorUtils.colorToString(color);
	}

	/**
	 * 解析 #RRGGBB、#AARRGGBB 或系统颜色名称。
	 *
	 * @param text 文本。
	 * @return 返回的整数值。
	 */
	@SimpleFunction
	public static int 从文本(String text) {
		return Color.parseColor(text);
	}

	/**
	 * 按亮度阈值判断是否为深色。
	 *
	 * @param color ARGB 整数颜色。
	 * @return 判断或操作的结果。
	 */
	@SimpleFunction
	public static boolean 是否深色(int color) {
		return ColorUtils.isColorDark(color);
	}

	/**
	 * 返回新的透明比例颜色，不修改传入颜色。
	 *
	 * @param color ARGB 整数颜色。
	 * @param alpha 范围 0 到 1；0 为透明，1 为不透明。
	 * @return 返回的整数值。
	 */
	@SimpleFunction
	public static int 置透明比例(int color, float alpha) {
		XUtilSupport.fraction(alpha);
		return ColorUtils.setColorAlpha(color, alpha);
	}

	/**
	 * 按 0 到 1 的比例调整颜色明暗。
	 *
	 * @param color ARGB 整数颜色。
	 * @param factor 比例。
	 * @return 返回的整数值。
	 */
	@SimpleFunction
	public static int 加深(int color, float factor) {
		XUtilSupport.fraction(factor);
		return ColorUtils.darker(color, factor);
	}

	/**
	 * 按 0 到 1 的比例调整颜色明暗。
	 *
	 * @param color ARGB 整数颜色。
	 * @param factor 比例。
	 * @return 返回的整数值。
	 */
	@SimpleFunction
	public static int 变浅(int color, float factor) {
		XUtilSupport.fraction(factor);
		return ColorUtils.lighter(color, factor);
	}
}
