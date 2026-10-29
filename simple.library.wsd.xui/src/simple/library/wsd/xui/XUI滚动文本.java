package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * XUI滚动文本组件。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI滚动文本 extends XUI文本组件 {

	/**
	 * 是否获得焦点。
	 *
	 * @return 是否获得焦点。
	 */
	@SimpleFunction
	boolean 是否获得焦点();
}
