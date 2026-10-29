package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleEvent;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.components.文本组件;

/**
 * 具有标准 Simple 文本属性的 XUI 组件接口。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public interface XUI文本组件 extends XUI视图组件, 文本组件 {

	/**
	 * 文本内容改变时触发。
	 *
	 * @param text 最新文本。
	 */
	@SimpleEvent
	void 文本改变(String text);
}
