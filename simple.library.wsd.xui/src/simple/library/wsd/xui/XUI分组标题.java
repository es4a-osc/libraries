package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * XUI分组标题组件。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI分组标题 extends XUI视图组件 {

	/**
	 * 设置文本对齐。
	 *
	 * @param gravity 对齐。
	 */
	@SimpleFunction
	void 置文本对齐(int gravity);

	/**
	 * 设置组件显示或输入的文本。
	 *
	 * @param text 文本内容。
	 */
	@SimpleFunction
	void 置文本(String text);
}
