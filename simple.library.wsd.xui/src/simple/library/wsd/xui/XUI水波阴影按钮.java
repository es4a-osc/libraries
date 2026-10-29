package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * XUI水波阴影按钮组件。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI水波阴影按钮 extends XUI文本组件 {

	/**
	 * 取得圆角。
	 *
	 * @return 圆角。
	 */
	@SimpleFunction
	int 取圆角();

	/**
	 * 设置圆角。
	 *
	 * @param roundRadius 圆角。
	 */
	@SimpleFunction
	void 置圆角(int roundRadius);

	/**
	 * 取得水波颜色。
	 *
	 * @return 水波颜色。
	 */
	@SimpleFunction
	int 取水波颜色();

	/**
	 * 设置水波颜色。
	 *
	 * @param rippleColor 水波颜色。
	 */
	@SimpleFunction
	void 置水波颜色(int rippleColor);

	/**
	 * 取得水波时长。
	 *
	 * @return 水波时长。
	 */
	@SimpleFunction
	int 取水波时长();

	/**
	 * 设置水波时长。
	 *
	 * @param rippleDuration 水波时长。
	 */
	@SimpleFunction
	void 置水波时长(int rippleDuration);

	/**
	 * 取得水波圆角。
	 *
	 * @return 水波圆角。
	 */
	@SimpleFunction
	int 取水波圆角();

	/**
	 * 设置水波圆角。
	 *
	 * @param rippleRadius 水波圆角。
	 */
	@SimpleFunction
	void 置水波圆角(int rippleRadius);

	/**
	 * 取得水波透明度。
	 *
	 * @return 水波透明度。
	 */
	@SimpleFunction
	int 取水波透明度();

	/**
	 * 设置水波透明度。
	 *
	 * @param rippleAlpha 水波透明度。
	 */
	@SimpleFunction
	void 置水波透明度(int rippleAlpha);

	/**
	 * 设置未按下颜色。
	 *
	 * @param color 颜色。
	 */
	@SimpleFunction
	void 置未按下颜色(int color);

	/**
	 * 取得形状类型。
	 *
	 * @return 形状类型。
	 */
	@SimpleFunction
	int 取形状类型();

	/**
	 * 设置形状类型。
	 *
	 * @param shapeType 形状类型。
	 */
	@SimpleFunction
	void 置形状类型(int shapeType);
}
