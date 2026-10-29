package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleEvent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.components.组件;

/**
 * 显示普通或迷你加载对话框；切换所属窗口时关闭。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI加载对话框 extends 组件 {

	/**
	 * 显示加载提示。
	 *
	 * @param message 提示文本。
	 * @param mini 是否使用迷你样式。
	 * @param cancelable 是否允许返回键或外部点击取消。
	 */
	@SimpleFunction
	void 显示(String message, boolean mini, boolean cancelable);

	/**
	 * 更新正在显示的加载提示。
	 *
	 * @param message 提示文本。
	 */
	@SimpleFunction
	void 置提示文本(String message);

	/**
	 * 用户取消加载提示时触发；主动关闭不触发。
	 */
	@SimpleEvent
	void 被取消();

	/**
	 * 关闭当前显示的界面。
	 */
	@SimpleFunction
	void 关闭();

	/**
	 * 取得当前界面是否正在显示。
	 *
	 * @return 是否显示。
	 */
	@SimpleFunction
	boolean 是否显示();
}
