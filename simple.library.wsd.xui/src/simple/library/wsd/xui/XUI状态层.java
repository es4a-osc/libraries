package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.components.组件;

/**
 * 在当前窗口中央显示加载、完成或错误状态，支持指定自动关闭时长。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI状态层 extends 组件 {

	/**
	 * 显示状态，零时长持续显示至主动关闭。
	 *
	 * @param state 0：加载；1：完成；2：错误。
	 * @param duration 自动关闭毫秒数；零为不自动关闭。
	 */
	@SimpleFunction
	void 显示(int state, long duration);

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
