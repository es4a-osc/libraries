package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleEvent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * XUI折叠文本组件。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI折叠文本 extends XUI视图组件 {

	/**
	 * 设置组件显示或输入的文本。
	 *
	 * @param text 文本内容。
	 */
	@SimpleFunction
	void 置文本(String text);

	/**
	 * 取得组件显示或输入的文本。
	 *
	 * @return 组件显示或输入的文本。
	 */
	@SimpleFunction
	String 取文本();

	/**
	 * 文本展开或收起后触发。
	 *
	 * @param expanded 是否展开全部文本。
	 */
	@SimpleEvent
	void 展开改变(boolean expanded);
}
