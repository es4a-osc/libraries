package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * XUI超级按钮组件。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI超级按钮 extends XUI文本组件 {

	/**
	 * 设置形状类型。
	 *
	 * @param type 类型。
	 */
	@SimpleFunction
	void 置形状类型(int type);

	/**
	 * 设置文字对齐方式。
	 *
	 * @param gravity 对齐方式。
	 */
	@SimpleFunction
	void 置文本对齐(int gravity);

	/**
	 * 设置按下的颜色。
	 *
	 * @param color 颜色。
	 */
	@SimpleFunction
	void 置形状按下颜色(int color);

	/**
	 * 设置正常的颜色。
	 *
	 * @param color 颜色。
	 */
	@SimpleFunction
	void 置形状正常颜色(int color);

	/**
	 * 设置不可点击的颜色。
	 *
	 * @param color 颜色。
	 */
	@SimpleFunction
	void 置形状禁用颜色(int color);

	/**
	 * 设置填充的颜色。
	 *
	 * @param color 颜色。
	 */
	@SimpleFunction
	void 置形状填充颜色(int color);

	/**
	 * 设置边框宽度。
	 *
	 * @param strokeWidth 边框宽度值。
	 */
	@SimpleFunction
	void 置形状描边宽度(int strokeWidth);

	/**
	 * 设置边框颜色。
	 *
	 * @param strokeColor 边框颜色。
	 */
	@SimpleFunction
	void 置形状描边颜色(int strokeColor);

	/**
	 * 设置边框虚线宽度。
	 *
	 * @param strokeDashWidth 边框虚线宽度。
	 */
	@SimpleFunction
	void 置形状描边虚线宽度(float strokeDashWidth);

	/**
	 * 设置边框虚线间隙。
	 *
	 * @param strokeDashGap 边框虚线间隙值。
	 */
	@SimpleFunction
	void 置形状描边虚线间隔(float strokeDashGap);

	/**
	 * 设置圆角半径。
	 *
	 * @param radius 半径。
	 */
	@SimpleFunction
	void 置形状圆角(float radius);

	/**
	 * 设置左上圆角半径。
	 *
	 * @param radius 半径。
	 */
	@SimpleFunction
	void 置形状左上圆角(float radius);

	/**
	 * 设置右上圆角半径。
	 *
	 * @param radius 半径。
	 */
	@SimpleFunction
	void 置形状右上圆角(float radius);

	/**
	 * 设置左下圆角半径。
	 *
	 * @param radius 半径。
	 */
	@SimpleFunction
	void 置形状左下圆角(float radius);

	/**
	 * 设置右下圆角半径。
	 *
	 * @param radius 半径。
	 */
	@SimpleFunction
	void 置形状右下圆角(float radius);

	/**
	 * 设置shape的宽度。
	 *
	 * @param sizeWidth 宽。
	 */
	@SimpleFunction
	void 置形状尺寸宽度(int sizeWidth);

	/**
	 * 设置shape的高度。
	 *
	 * @param sizeHeight 高。
	 */
	@SimpleFunction
	void 置形状尺寸高度(int sizeHeight);

	/**
	 * 设置背景渐变方式。
	 *
	 * @param gradientOrientation 渐变类型。
	 */
	@SimpleFunction
	void 置形状渐变方向(int gradientOrientation);

	/**
	 * 设置渐变中心横坐标。
	 *
	 * @param gradientCenterX 中心x。
	 */
	@SimpleFunction
	void 置形状渐变中心横坐标(int gradientCenterX);

	/**
	 * 设置渐变中心纵坐标。
	 *
	 * @param gradientCenterY 中心y。
	 */
	@SimpleFunction
	void 置形状渐变中心纵坐标(int gradientCenterY);

	/**
	 * 设置渐变半径。
	 *
	 * @param gradientGradientRadius 渐变半径。
	 */
	@SimpleFunction
	void 置形状渐变半径(int gradientGradientRadius);

	/**
	 * 设置渐变开始的颜色。
	 *
	 * @param gradientStartColor 开始颜色。
	 */
	@SimpleFunction
	void 置形状渐变开始颜色(int gradientStartColor);

	/**
	 * 设置渐变中间的颜色。
	 *
	 * @param gradientCenterColor 中间颜色。
	 */
	@SimpleFunction
	void 置形状渐变中间颜色(int gradientCenterColor);

	/**
	 * 设置渐变结束的颜色。
	 *
	 * @param gradientEndColor 结束颜色。
	 */
	@SimpleFunction
	void 置形状渐变结束颜色(int gradientEndColor);

	/**
	 * 设置渐变类型。
	 *
	 * @param gradientType 类型。
	 */
	@SimpleFunction
	void 置形状渐变类型(int gradientType);

	/**
	 * 设置渐变是否使用绘制级别。
	 *
	 * @param gradientUseLevel 渐变使用级别。
	 */
	@SimpleFunction
	void 置形状渐变使用级别(boolean gradientUseLevel);

	/**
	 * 设置是否根据交互状态选择颜色。
	 *
	 * @param useSelector 使用状态选择器。
	 */
	@SimpleFunction
	void 置形状使用状态选择器(boolean useSelector);

	/**
	 * 应用此前设置的形状参数；修改形状参数后须调用本方法刷新。
	 */
	@SimpleFunction
	void 应用形状();

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
}
