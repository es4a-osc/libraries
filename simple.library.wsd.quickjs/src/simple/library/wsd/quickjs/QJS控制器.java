package simple.library.wsd.quickjs;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * 宿主创建的 Promise 及兑现/拒绝控制函数；重复结算遵循 JS Promise 规则。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class QJS控制器 {

	private final QJS承诺 promise;
	private final QJS函数 resolve;
	private final QJS函数 reject;

	/**
	 * 保存 Promise 能力的三个拥有引用。
	 *
	 * @param promise Promise 引用。
	 * @param resolve 兑现函数。
	 * @param reject  拒绝函数。
	 */
	QJS控制器(QJS承诺 promise, QJS函数 resolve, QJS函数 reject) {
		this.promise = promise;
		this.resolve = resolve;
		this.reject = reject;
	}

	/**
	 * 取得 Promise 的独立拥有引用。
	 *
	 * @return 控制器所管理承诺的独立引用。
	 */
	@SimpleFunction
	public QJS承诺 取承诺() {
		return (QJS承诺) promise.保留();
	}

	/**
	 * 兑现 Promise；后续处理器进入运行时队列。
	 *
	 * @param value 同一实例的兑现值。
	 */
	@SimpleFunction
	public void 兑现(QJS值 value) {
		resolve.context.op(Native.CALL, resolve, null, null, null, 0, 0, new QJS值[] { value }, null).释放();
	}

	/**
	 * 拒绝 Promise。
	 *
	 * @param reason 同一实例的拒绝原因。
	 */
	@SimpleFunction
	public void 拒绝(QJS值 reason) {
		reject.context.op(Native.CALL, reject, null, null, null, 0, 0, new QJS值[] { reason }, null).释放();
	}

	/**
	 * 释放控制器的三个拥有引用，已被其它引用持有的 Promise 继续有效。
	 */
	@SimpleFunction
	public void 释放() {
		promise.context.runtime.checkAccess();
		synchronized (promise.context.runtime.lock) {
			promise.释放();
			resolve.释放();
			reject.释放();
		}
	}
}
