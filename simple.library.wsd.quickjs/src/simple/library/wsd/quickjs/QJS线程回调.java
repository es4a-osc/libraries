package simple.library.wsd.quickjs;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * 工作线程消息和生命周期回调，在接收实例的调度线程执行，消息值为借用引用。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public class QJS线程回调 {
	/**
	 * 创建可继承的回调对象。
	 */
	public QJS线程回调() {
	}

	/**
	 * 接收已复制到接收实例的数据，长期保存时须保留引用。
	 *
	 * @param value 本次回调有效的消息值。
	 */
	@SimpleFunction
	public void 收到消息(QJS值 value) {
	}

	/**
	 * 接收工作线程的实际脚本错误或消息传输错误。
	 *
	 * @param message 错误说明及可用堆栈。
	 */
	@SimpleFunction
	public void 发生错误(String message) {
		android.util.Log.e("QuickJS", message);
	}

	/**
	 * 工作线程资源已释放，后续不会再接收消息；接收实例已销毁或停止调度时不再通知。
	 */
	@SimpleFunction
	public void 已经结束() {
	}

	/**
	 * 释放库内部监听持有的引用，不作为用户回调导出。
	 */
	void releaseInternal() {
	}
}
