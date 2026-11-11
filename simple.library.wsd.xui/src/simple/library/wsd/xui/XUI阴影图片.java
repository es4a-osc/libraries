package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * XUI阴影图片组件。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI阴影图片 extends XUI图片组件 {

	/**
	 * 取得按下颜色。
	 *
	 * @return 按下颜色。
	 */
	@SimpleFunction
	int 取按下颜色();

	/**
	 * 设置按下颜色。
	 *
	 * @param pressedColor 按下颜色。
	 */
	@SimpleFunction
	void 置按下颜色(int pressedColor);

}
