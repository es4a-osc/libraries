package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * XUI基础阴影按钮组件。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI基础阴影按钮 extends XUI文本组件 {

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
	 * @param radius 圆角。
	 */
	@SimpleFunction
	void 置圆角(int radius);
}
