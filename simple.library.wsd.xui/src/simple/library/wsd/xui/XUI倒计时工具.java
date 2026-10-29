package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleEvent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.components.可视组件;
import simple.runtime.components.组件;

/**
 * 为任意文本按钮提供倒计时，结束、取消或切窗时恢复原文本和启用状态。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI倒计时工具 extends 组件 {

	/**
	 * 开始倒计时并暂时禁用目标。
	 *
	 * @param component 原生视图为 TextView 的组件。
	 * @param seconds 总秒数，1～86400。
	 * @param interval 回调间隔秒数，须为正数且不大于总秒数。
	 */
	@SimpleFunction
	void 开始(可视组件 component, int seconds, int interval);

	/**
	 * 取消当前倒计时，恢复原状态。
	 */
	@SimpleFunction
	void 关闭();

	/**
	 * 倒计时更新。
	 *
	 * @param seconds 剩余秒数。
	 */
	@SimpleEvent
	void 剩余时间改变(int seconds);

	/**
	 * 自然结束时触发，主动取消不触发。
	 */
	@SimpleEvent
	void 倒计时结束();
}
