package simple.library.wsd.quickjs;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * Promise 拒绝与后续处理通知；传入值仅本次回调有效，可调用保留延长持有。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public class QuickJS监听 {
	/**
	 * 创建可继承的宿主适配对象。
	 */
	public QuickJS监听() {
	}

	/**
	 * 接收拒绝处理状态变化，默认记录未处理拒绝。
	 *
	 * @param promise 发生状态变化的 Promise。
	 * @param reason 拒绝原因。
	 * @param handled 是否已经出现拒绝处理器。
	 */
	@SimpleFunction
	public void 状态改变(QuickJS承诺 promise, QuickJS值 reason, boolean handled) {
		if (!handled) {
			android.util.Log.w("QuickJS", "未处理的 Promise 拒绝：" + reason.到文本());
		}
	}
}
