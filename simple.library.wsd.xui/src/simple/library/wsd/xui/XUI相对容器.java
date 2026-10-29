package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleDataElement;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.components.组件容器;

/**
 * XUI相对容器组件。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI相对容器 extends XUI视图组件, 组件容器 {

	/**
	 * 设置顶部分隔线透明度。
	 *
	 * @param dividerAlpha 分隔线透明度。
	 */
	@SimpleFunction
	void 置顶部分隔线透明度(int dividerAlpha);

	/**
	 * 设置底部分隔线透明度。
	 *
	 * @param dividerAlpha 分隔线透明度。
	 */
	@SimpleFunction
	void 置底部分隔线透明度(int dividerAlpha);

	/**
	 * 设置左侧分隔线透明度。
	 *
	 * @param dividerAlpha 分隔线透明度。
	 */
	@SimpleFunction
	void 置左侧分隔线透明度(int dividerAlpha);

	/**
	 * 设置右侧分隔线透明度。
	 *
	 * @param dividerAlpha 分隔线透明度。
	 */
	@SimpleFunction
	void 置右侧分隔线透明度(int dividerAlpha);

	/**
	 * 设置圆角与阴影。
	 *
	 * @param radius 圆角。
	 * @param shadowElevation 阴影高度。
	 * @param shadowAlpha 阴影透明度。
	 */
	@SimpleFunction
	void 置圆角与阴影(int radius, int shadowElevation, float shadowAlpha);

	/**
	 * 设置圆角与阴影。
	 *
	 * @param radius 圆角。
	 * @param hideRadiusSide 隐藏圆角边。
	 * @param shadowElevation 阴影高度。
	 * @param shadowAlpha 阴影透明度。
	 */
	@SimpleFunction
	void 置圆角与阴影(int radius, int hideRadiusSide, int shadowElevation, float shadowAlpha);

	/**
	 * 设置圆角。
	 *
	 * @param radius 圆角。
	 */
	@SimpleFunction
	void 置圆角(int radius);

	/**
	 * 设置圆角。
	 *
	 * @param radius 圆角。
	 * @param hideRadiusSide 隐藏圆角边。
	 */
	@SimpleFunction
	void 置圆角(int radius, int hideRadiusSide);

	/**
	 * 取得圆角。
	 *
	 * @return 圆角。
	 */
	@SimpleFunction
	int 取圆角();

	/**
	 * 设置轮廓内缩。
	 *
	 * @param left 左侧。
	 * @param top 顶部。
	 * @param right 右侧。
	 * @param bottom 底部。
	 */
	@SimpleFunction
	void 置轮廓内缩(int left, int top, int right, int bottom);

	/**
	 * 设置隐藏圆角边。
	 *
	 * @param hideRadiusSide 隐藏圆角边。
	 */
	@SimpleFunction
	void 置隐藏圆角边(int hideRadiusSide);

	/**
	 * 取得隐藏圆角边。
	 *
	 * @return 隐藏圆角边。
	 */
	@SimpleFunction
	int 取隐藏圆角边();

	/**
	 * 设置边框颜色。
	 *
	 * @param borderColor 边框颜色。
	 */
	@SimpleFunction
	void 置边框颜色(int borderColor);

	/**
	 * 设置边框宽度。
	 *
	 * @param borderWidth 边框宽度。
	 */
	@SimpleFunction
	void 置边框宽度(int borderWidth);

	/**
	 * 设置宽度限制。
	 *
	 * @param widthLimit 宽度限制。
	 * @return 设置宽度限制。
	 */
	@SimpleFunction
	boolean 置宽度限制(int widthLimit);

	/**
	 * 设置高度限制。
	 *
	 * @param heightLimit 高度限制。
	 * @return 设置高度限制。
	 */
	@SimpleFunction
	boolean 置高度限制(int heightLimit);

	/**
	 * 设置轮廓排除内边距。
	 *
	 * @param outlineExcludePadding 轮廓排除内边距。
	 */
	@SimpleFunction
	void 置轮廓排除内边距(boolean outlineExcludePadding);

	/**
	 * 设置阴影高度。
	 *
	 * @param elevation 高度。
	 */
	@SimpleFunction
	void 置阴影高度(int elevation);

	/**
	 * 取得阴影高度。
	 *
	 * @return 阴影高度。
	 */
	@SimpleFunction
	int 取阴影高度();

	/**
	 * 设置阴影透明度。
	 *
	 * @param shadowAlpha 阴影透明度。
	 */
	@SimpleFunction
	void 置阴影透明度(float shadowAlpha);

	/**
	 * 设置阴影颜色。
	 *
	 * @param shadowColor 阴影颜色。
	 */
	@SimpleFunction
	void 置阴影颜色(int shadowColor);

	/**
	 * 取得阴影颜色。
	 *
	 * @return 阴影颜色。
	 */
	@SimpleFunction
	int 取阴影颜色();

	/**
	 * 设置外部正常颜色。
	 *
	 * @param color 颜色。
	 */
	@SimpleFunction
	void 置外部正常颜色(int color);

	/**
	 * 取得阴影透明度。
	 *
	 * @return 阴影透明度。
	 */
	@SimpleFunction
	float 取阴影透明度();

	/**
	 * 设置按压时是否改变透明度。
	 *
	 * @param changeAlphaWhenPress 按压时是否改变透明度。
	 */
	@SimpleFunction
	void 置按压透明反馈(boolean changeAlphaWhenPress);

	/**
	 * 设置禁用时是否改变透明度。
	 *
	 * @param changeAlphaWhenDisable 禁用时是否改变透明度。
	 */
	@SimpleFunction
	void 置禁用透明反馈(boolean changeAlphaWhenDisable);

	/**
	 * 设置顶部分隔线的缩进、粗细和颜色。
	 *
	 * @param start 非负像素。
	 * @param end 非负像素。
	 * @param size 非负像素，0 隐藏。
	 * @param color ARGB 颜色。
	 */
	@SimpleFunction
	void 置顶部分隔线(int start, int end, int size, int color);

	/**
	 * 设置底部分隔线的缩进、粗细和颜色。
	 *
	 * @param start 非负像素。
	 * @param end 非负像素。
	 * @param size 非负像素，0 隐藏。
	 * @param color ARGB 颜色。
	 */
	@SimpleFunction
	void 置底部分隔线(int start, int end, int size, int color);

	/**
	 * 设置左侧分隔线的缩进、粗细和颜色。
	 *
	 * @param start 非负像素。
	 * @param end 非负像素。
	 * @param size 非负像素，0 隐藏。
	 * @param color ARGB 颜色。
	 */
	@SimpleFunction
	void 置左侧分隔线(int start, int end, int size, int color);

	/**
	 * 设置右侧分隔线的缩进、粗细和颜色。
	 *
	 * @param start 非负像素。
	 * @param end 非负像素。
	 * @param size 非负像素，0 隐藏。
	 * @param color ARGB 颜色。
	 */
	@SimpleFunction
	void 置右侧分隔线(int start, int end, int size, int color);

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
