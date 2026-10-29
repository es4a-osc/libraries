package simple.library.wsd.xui;

import com.xuexiang.xui.utils.ColorUtils;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.errors.非法参数错误;

/**
 * XUI 颜色计算；颜色均采用 ARGB 整数。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class XUI颜色工具 {

	/**
	 * 静态工具不创建实例。
	 */
	private XUI颜色工具() {
	}

	/**
	 * 设置绝对透明度或与原透明度相乘。
	 *
	 * @param color ARGB 颜色。
	 * @param alpha 0～1 的不透明度。
	 * @param override 是否替换原透明度。
	 * @return 新的 ARGB 颜色。
	 */
	@SimpleFunction
	public static int 置透明度(int color, float alpha, boolean override) {
		if (Float.isNaN(alpha) || alpha < 0 || alpha > 1) {
			throw new 非法参数错误();
		}
		return ColorUtils.setColorAlpha(color, alpha, override);
	}

	/**
	 * 计算两个颜色之间的中间颜色。
	 *
	 * @param start 起始颜色。
	 * @param end 结束颜色。
	 * @param fraction 0～1 的渐变比例。
	 * @return 中间颜色。
	 */
	@SimpleFunction
	public static int 计算渐变(int start, int end, float fraction) {
		if (Float.isNaN(fraction) || fraction < 0 || fraction > 1) {
			throw new 非法参数错误();
		}
		return ColorUtils.computeColor(start, end, fraction);
	}

	/**
	 * 把颜色转换为包含透明度的十六进制文本。
	 *
	 * @param color ARGB 颜色。
	 * @return 颜色文本。
	 */
	@SimpleFunction
	public static String 颜色到文本(int color) {
		return ColorUtils.colorToString(color);
	}

	/**
	 * 按系数加深颜色。
	 *
	 * @param color 原颜色。
	 * @param factor 0～1 的乘数。
	 * @return 新的颜色。
	 */
	@SimpleFunction
	public static int 加深(int color, float factor) {
		if (Float.isNaN(factor) || factor < 0 || factor > 1) {
			throw new 非法参数错误();
		}
		return ColorUtils.darker(color, factor);
	}

	/**
	 * 按系数向白色靠近。
	 *
	 * @param color 原颜色。
	 * @param factor 0～1 的比例。
	 * @return 新的颜色。
	 */
	@SimpleFunction
	public static int 变浅(int color, float factor) {
		if (Float.isNaN(factor) || factor < 0 || factor > 1) {
			throw new 非法参数错误();
		}
		return ColorUtils.lighter(color, factor);
	}

	/**
	 * 判断颜色是否为深色。
	 *
	 * @param color ARGB 颜色。
	 * @return 是否深色。
	 */
	@SimpleFunction
	public static boolean 是否深色(int color) {
		return ColorUtils.isColorDark(color);
	}

	/**
	 * 生成不透明的随机颜色。
	 *
	 * @return 随机 ARGB 颜色。
	 */
	@SimpleFunction
	public static int 取随机颜色() {
		return ColorUtils.getRandomColor();
	}
}
