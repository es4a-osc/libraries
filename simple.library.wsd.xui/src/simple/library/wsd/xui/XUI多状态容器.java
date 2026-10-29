package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleDataElement;
import simple.runtime.annotations.SimpleEvent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.components.可视组件;
import simple.runtime.components.组件容器;

/**
 * XUI多状态容器组件。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI多状态容器 extends XUI视图组件, 组件容器 {

	/**
	 * 显示空视图。
	 */
	@SimpleFunction
	void 显示空状态();

	/**
	 * 显示错误视图。
	 */
	@SimpleFunction
	void 显示错误();

	/**
	 * 显示加载中视图。
	 */
	@SimpleFunction
	void 显示加载();

	/**
	 * 显示无网络视图。
	 */
	@SimpleFunction
	void 显示无网络();

	/**
	 * 显示内容视图。
	 */
	@SimpleFunction
	void 显示内容();

	/**
	 * 用户点击错误或无网络状态的重试按钮时触发。
	 */
	@SimpleEvent
	void 请求重试();

	/**
	 * 取得当前显示状态。
	 *
	 * @return 当前显示状态。
	 */
	@SimpleFunction
	int 取当前状态();

	/**
	 * 设置加载、空、错误或无网络状态的自定义 Simple 内容；内容在解除或销毁时恢复。
	 *
	 * @param state 非内容状态常量。
	 * @param component 同一窗口中可借用的组件。
	 */
	@SimpleFunction
	void 置状态内容(int state, 可视组件 component);

	/**
	 * 解除该状态的自定义内容并恢复默认状态样式。
	 *
	 * @param state 非内容状态常量。
	 */
	@SimpleFunction
	void 清除状态内容(int state);

	/**
	 * 显示内容状态。
	 */
	@SimpleDataElement
	int 状态_内容 = 0;

	/**
	 * 显示加载状态。
	 */
	@SimpleDataElement
	int 状态_加载 = 1;

	/**
	 * 显示空状态。
	 */
	@SimpleDataElement
	int 状态_空 = 2;

	/**
	 * 显示错误状态。
	 */
	@SimpleDataElement
	int 状态_错误 = 3;

	/**
	 * 显示无网络状态。
	 */
	@SimpleDataElement
	int 状态_无网络 = 4;

}
