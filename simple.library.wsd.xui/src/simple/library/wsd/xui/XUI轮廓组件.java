package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleDataElement;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.errors.非法参数错误;

/**
 * 按钮及布局容器共用的圆角、阴影、边框与分隔线能力。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public interface XUI轮廓组件 extends XUI视图组件 {

	/**
	 * 设置顶部分隔线透明度。
	 *
	 * @param dividerAlpha 分隔线透明度。
	 */
	@SimpleFunction
	default void 置顶部分隔线透明度(int dividerAlpha) {
		ComponentSupport.layout(this).setTopDividerAlpha(dividerAlpha);
	}

	/**
	 * 设置底部分隔线透明度。
	 *
	 * @param dividerAlpha 分隔线透明度。
	 */
	@SimpleFunction
	default void 置底部分隔线透明度(int dividerAlpha) {
		ComponentSupport.layout(this).setBottomDividerAlpha(dividerAlpha);
	}

	/**
	 * 设置左侧分隔线透明度。
	 *
	 * @param dividerAlpha 分隔线透明度。
	 */
	@SimpleFunction
	default void 置左侧分隔线透明度(int dividerAlpha) {
		ComponentSupport.layout(this).setLeftDividerAlpha(dividerAlpha);
	}

	/**
	 * 设置右侧分隔线透明度。
	 *
	 * @param dividerAlpha 分隔线透明度。
	 */
	@SimpleFunction
	default void 置右侧分隔线透明度(int dividerAlpha) {
		ComponentSupport.layout(this).setRightDividerAlpha(dividerAlpha);
	}

	/**
	 * 设置隐藏圆角边。
	 *
	 * @param hideRadiusSide 隐藏圆角边。
	 */
	@SimpleFunction
	default void 置隐藏圆角边(int hideRadiusSide) {
		ComponentSupport.layout(this).setHideRadiusSide(hideRadiusSide);
	}

	/**
	 * 取得隐藏圆角边。
	 *
	 * @return 隐藏圆角边。
	 */
	@SimpleFunction
	default int 取隐藏圆角边() {
		return ComponentSupport.layout(this).getHideRadiusSide();
	}

	/**
	 * 设置圆角与阴影。
	 *
	 * @param radius          圆角。
	 * @param shadowElevation 阴影高度。
	 * @param shadowAlpha     阴影透明度。
	 */
	@SimpleFunction
	default void 置圆角与阴影(int radius, int shadowElevation, float shadowAlpha) {
		ComponentSupport.layout(this).setRadiusAndShadow(radius, shadowElevation, shadowAlpha);
	}

	/**
	 * 设置圆角与阴影。
	 *
	 * @param radius          圆角。
	 * @param hideRadiusSide  隐藏圆角边。
	 * @param shadowElevation 阴影高度。
	 * @param shadowAlpha     阴影透明度。
	 */
	@SimpleFunction
	default void 置圆角与阴影(int radius, int hideRadiusSide, int shadowElevation, float shadowAlpha) {
		ComponentSupport.layout(this).setRadiusAndShadow(radius, hideRadiusSide, shadowElevation, shadowAlpha);
	}

	/**
	 * 设置圆角。
	 *
	 * @param radius 圆角。
	 */
	@SimpleFunction
	default void 置圆角(int radius) {
		ComponentSupport.layout(this).setRadius(radius);
	}

	/**
	 * 设置圆角。
	 *
	 * @param radius         圆角。
	 * @param hideRadiusSide 隐藏圆角边。
	 */
	@SimpleFunction
	default void 置圆角(int radius, int hideRadiusSide) {
		ComponentSupport.layout(this).setRadius(radius, hideRadiusSide);
	}

	/**
	 * 取得圆角。
	 *
	 * @return 圆角。
	 */
	@SimpleFunction
	default int 取圆角() {
		return ComponentSupport.layout(this).getRadius();
	}

	/**
	 * 设置轮廓内缩。
	 *
	 * @param left   左侧。
	 * @param top    顶部。
	 * @param right  右侧。
	 * @param bottom 底部。
	 */
	@SimpleFunction
	default void 置轮廓内缩(int left, int top, int right, int bottom) {
		ComponentSupport.layout(this).setOutlineInset(left, top, right, bottom);
	}

	/**
	 * 设置边框颜色。
	 *
	 * @param borderColor 边框颜色。
	 */
	@SimpleFunction
	default void 置边框颜色(int borderColor) {
		ComponentSupport.layout(this).setBorderColor(borderColor);
	}

	/**
	 * 设置边框宽度。
	 *
	 * @param borderWidth 边框宽度。
	 */
	@SimpleFunction
	default void 置边框宽度(int borderWidth) {
		ComponentSupport.layout(this).setBorderWidth(borderWidth);
	}

	/**
	 * 设置宽度限制。
	 *
	 * @param widthLimit 宽度限制。
	 * @return 设置宽度限制。
	 */
	@SimpleFunction
	default boolean 置宽度限制(int widthLimit) {
		return ComponentSupport.layout(this).setWidthLimit(widthLimit);
	}

	/**
	 * 设置高度限制。
	 *
	 * @param heightLimit 高度限制。
	 * @return 设置高度限制。
	 */
	@SimpleFunction
	default boolean 置高度限制(int heightLimit) {
		return ComponentSupport.layout(this).setHeightLimit(heightLimit);
	}

	/**
	 * 设置轮廓排除内边距。
	 *
	 * @param outlineExcludePadding 轮廓排除内边距。
	 */
	@SimpleFunction
	default void 置轮廓排除内边距(boolean outlineExcludePadding) {
		ComponentSupport.layout(this).setOutlineExcludePadding(outlineExcludePadding);
	}

	/**
	 * 设置阴影高度。
	 *
	 * @param elevation 高度。
	 */
	@SimpleFunction
	default void 置阴影高度(int elevation) {
		ComponentSupport.layout(this).setShadowElevation(elevation);
	}

	/**
	 * 取得阴影高度。
	 *
	 * @return 阴影高度。
	 */
	@SimpleFunction
	default int 取阴影高度() {
		return ComponentSupport.layout(this).getShadowElevation();
	}

	/**
	 * 设置阴影透明度。
	 *
	 * @param shadowAlpha 阴影透明度。
	 */
	@SimpleFunction
	default void 置阴影透明度(float shadowAlpha) {
		ComponentSupport.layout(this).setShadowAlpha(shadowAlpha);
	}

	/**
	 * 取得阴影透明度。
	 *
	 * @return 阴影透明度。
	 */
	@SimpleFunction
	default float 取阴影透明度() {
		return ComponentSupport.layout(this).getShadowAlpha();
	}

	/**
	 * 设置阴影颜色。
	 *
	 * @param shadowColor 阴影颜色。
	 */
	@SimpleFunction
	default void 置阴影颜色(int shadowColor) {
		ComponentSupport.layout(this).setShadowColor(shadowColor);
	}

	/**
	 * 取得阴影颜色。
	 *
	 * @return 阴影颜色。
	 */
	@SimpleFunction
	default int 取阴影颜色() {
		return ComponentSupport.layout(this).getShadowColor();
	}

	/**
	 * 设置外部正常颜色。
	 *
	 * @param color 颜色。
	 */
	@SimpleFunction
	default void 置外部正常颜色(int color) {
		ComponentSupport.layout(this).setOuterNormalColor(color);
	}

	/**
	 * 设置顶部分隔线的缩进、粗细和颜色。
	 *
	 * @param start 非负像素。
	 * @param end   非负像素。
	 * @param size  非负像素，0 隐藏。
	 * @param color ARGB 颜色。
	 */
	@SimpleFunction
	default void 置顶部分隔线(int start, int end, int size, int color) {
		if (start < 0 || end < 0 || size < 0) {
			throw new 非法参数错误();
		}
		ComponentSupport.layout(this).updateTopDivider(start, end, size, color);
	}

	/**
	 * 设置底部分隔线的缩进、粗细和颜色。
	 *
	 * @param start 非负像素。
	 * @param end   非负像素。
	 * @param size  非负像素，0 隐藏。
	 * @param color ARGB 颜色。
	 */
	@SimpleFunction
	default void 置底部分隔线(int start, int end, int size, int color) {
		if (start < 0 || end < 0 || size < 0) {
			throw new 非法参数错误();
		}
		ComponentSupport.layout(this).updateBottomDivider(start, end, size, color);
	}

	/**
	 * 设置左侧分隔线的缩进、粗细和颜色。
	 *
	 * @param start 非负像素。
	 * @param end   非负像素。
	 * @param size  非负像素，0 隐藏。
	 * @param color ARGB 颜色。
	 */
	@SimpleFunction
	default void 置左侧分隔线(int start, int end, int size, int color) {
		if (start < 0 || end < 0 || size < 0) {
			throw new 非法参数错误();
		}
		ComponentSupport.layout(this).updateLeftDivider(start, end, size, color);
	}

	/**
	 * 设置右侧分隔线的缩进、粗细和颜色。
	 *
	 * @param start 非负像素。
	 * @param end   非负像素。
	 * @param size  非负像素，0 隐藏。
	 * @param color ARGB 颜色。
	 */
	@SimpleFunction
	default void 置右侧分隔线(int start, int end, int size, int color) {
		if (start < 0 || end < 0 || size < 0) {
			throw new 非法参数错误();
		}
		ComponentSupport.layout(this).updateRightDivider(start, end, size, color);
	}

	/**
	 * 保留全部圆角。
	 */
	@SimpleDataElement
	int 隐藏圆角_无 = 0;

	/**
	 * 取消顶部一侧圆角；无表示保留全部圆角。
	 */
	@SimpleDataElement
	int 隐藏圆角_顶部 = 1;

	/**
	 * 取消右侧一侧圆角；无表示保留全部圆角。
	 */
	@SimpleDataElement
	int 隐藏圆角_右侧 = 2;

	/**
	 * 取消底部一侧圆角；无表示保留全部圆角。
	 */
	@SimpleDataElement
	int 隐藏圆角_底部 = 3;

	/**
	 * 取消左侧一侧圆角；无表示保留全部圆角。
	 */
	@SimpleDataElement
	int 隐藏圆角_左侧 = 4;

}
