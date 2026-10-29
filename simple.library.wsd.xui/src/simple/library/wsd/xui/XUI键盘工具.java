package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleEvent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.components.可视组件;
import simple.runtime.components.组件;

/**
 * 控制软键盘并监听所属窗口内的键盘显示状态。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI键盘工具 extends 组件 {

	/**
	 * 为编辑组件请求焦点并显示软键盘。
	 *
	 * @param component 原生视图为 EditText 的组件。
	 */
	@SimpleFunction
	void 显示(可视组件 component);

	/**
	 * 隐藏当前窗口的软键盘。
	 */
	@SimpleFunction
	void 隐藏();

	/**
	 * 查询所属窗口是否受软键盘遮挡。
	 *
	 * @return 键盘是否显示。
	 */
	@SimpleFunction
	boolean 是否显示();

	/**
	 * 监听键盘显示状态，重复调用不会重复注册。
	 */
	@SimpleFunction
	void 开始监听();

	/**
	 * 停止本组件注册的键盘监听。
	 */
	@SimpleFunction
	void 关闭();

	/**
	 * 软键盘显示或隐藏时触发。
	 *
	 * @param visible 是否显示。
	 */
	@SimpleEvent
	void 显示状态改变(boolean visible);
}
