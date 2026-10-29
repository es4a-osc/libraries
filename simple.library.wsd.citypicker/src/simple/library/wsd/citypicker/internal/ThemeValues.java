package simple.library.wsd.citypicker.internal;

import android.content.Context;
import android.util.TypedValue;

/**
 * 从当前 Dialog 主题读取 CityPicker 属性，兼容资源引用与直接颜色、尺寸值。
 *
 * @author 树先生 xhwsd@qq.com
 */
final class ThemeValues {

	/**
	 * 禁止创建没有主题上下文的工具实例。
	 */
	private ThemeValues() {
	}

	/**
	 * 解析主题颜色；未定义属性时采用明确的后备颜色。
	 *
	 * @param context 当前主题上下文。
	 * @param attribute 颜色属性编号。
	 * @param fallback 后备 ARGB 颜色。
	 * @return 解析后的 ARGB 颜色。
	 */
	static int color(Context context, int attribute, int fallback) {
		TypedValue value = new TypedValue();
		if (!context.getTheme().resolveAttribute(attribute, value, true)) {
			return fallback;
		}
		return value.resourceId == 0 ? value.data : context.getColor(value.resourceId);
	}

	/**
	 * 解析主题尺寸，不额外乘以屏幕密度。
	 *
	 * @param context 当前主题上下文。
	 * @param attribute 尺寸属性编号。
	 * @param fallback 未定义时的像素值。
	 * @return 像素尺寸。
	 */
	static int dimension(Context context, int attribute, int fallback) {
		TypedValue value = new TypedValue();
		if (!context.getTheme().resolveAttribute(attribute, value, true)) {
			return fallback;
		}
		if (value.resourceId != 0) {
			return context.getResources().getDimensionPixelSize(value.resourceId);
		}
		return Math.round(value.getDimension(context.getResources().getDisplayMetrics()));
	}
}
