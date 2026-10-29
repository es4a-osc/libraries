package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * XUI圆角按钮组件。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI圆角按钮 extends XUI文本组件 {

	/**
	 * 设置圆角按钮填充颜色，保留圆角与边框。
	 *
	 * @param color ARGB 颜色。
	 */
	@SimpleFunction
	void 置填充颜色(int color);

	/**
	 * 设置按钮圆角半径。
	 *
	 * @param radius 圆角半径，单位为物理像素。
	 */
	@SimpleFunction
	void 置圆角(float radius);

	/**
	 * 设置圆角按钮描边。
	 *
	 * @param width 描边宽度，单位为物理像素。
	 * @param color ARGB 颜色。
	 */
	@SimpleFunction
	void 置描边(int width, int color);
}
