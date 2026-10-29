package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * XUI密码显示框组件。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI密码显示框 extends XUI文本组件 {

	/**
	 * 清除密码。
	 */
	@SimpleFunction
	void 清空密码();
}
