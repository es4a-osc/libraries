package simple.library.wsd.quickjs;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.errors.运行错误;

/**
 * QuickJS 执行、类型转换、资源状态及宿主回调错误。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class QJS错误 extends 运行错误 {
	private static final long serialVersionUID = 1L;
	private final String trace;
	private final int reason;
	private final QJS运行时 runtime;
	private QJS值 value;

	/**
	 * 创建宿主侧明确错误。
	 *
	 * @param message 错误说明。
	 */
	public QJS错误(String message) {
		this(message, "", 0, null);
	}

	/**
	 * 保存引擎的完整错误信息。
	 *
	 * @param message 错误说明。
	 * @param trace   JavaScript 堆栈。
	 * @param reason  原因编号。
	 * @param value   可选异常值。
	 */
	QJS错误(String message, String trace, int reason, QJS值 value) {
		super(message);
		this.trace = trace;
		this.reason = reason;
		this.value = value;
		this.runtime = value == null ? null : value.context.runtime;
	}

	/**
	 * 将原生诊断转换为 Simple 可捕获的错误。
	 *
	 * @param failure 原生失败。
	 * @return 有效 Simple 错误。
	 */
	static QJS错误 from(NativeFailure failure) {
		QJS值 value = null;
		QJS实例 owner = null;
		if (failure.value != 0) {
			owner = Native.owner(failure.value);
			if (owner != null && !owner.closed) {
				value = owner.wrap(failure.value);
			} else {
				Native.release(failure.value);
			}
		}
		QJS错误 error = new QJS错误(failure.getMessage(), failure.trace, failure.reason, value);
		error.initCause(failure);
		if (owner != null && !owner.closed) {
			owner.rememberFailure(error);
		}
		return error;
	}

	/**
	 * 在同步宿主回调中恢复同一运行时的原始 JS 异常值，返回引用交给 JNI 消费。
	 *
	 * @param target 当前执行实例。
	 * @return 原生拥有引用；无可恢复值时为 0。
	 */
	long nativeThrown(QJS实例 target) {
		QJS值 current = value;
		if (current == null || current.context.runtime != target.runtime || current.是否已释放()) {
			return 0;
		}
		return Native.value(target.handle, Native.DUPLICATE, current.handle, 0, null, null, 0, 0, null, null);
	}

	/**
	 * 读取错误说明。
	 *
	 * @return 错误说明。
	 */
	@SimpleFunction
	public String 取说明() {
		return getMessage();
	}

	/**
	 * 读取 JavaScript 来源及调用堆栈。
	 *
	 * @return 堆栈文本；宿主参数错误时可为空。
	 */
	@SimpleFunction
	public String 取堆栈() {
		return trace;
	}

	/**
	 * 读取执行中断原因。
	 *
	 * @return 0 普通错误，1 主动取消，2 超时。
	 */
	@SimpleFunction
	public int 取原因() {
		return reason;
	}

	/**
	 * 取得 JS 抛出值的独立引用。
	 *
	 * @return 异常值；没有 JS 异常值、已释放或实例已销毁时为空。
	 */
	@SimpleFunction
	public QJS值 取异常值() {
		if (runtime == null) {
			return null;
		}
		runtime.checkAccess();
		synchronized (runtime.lock) {
			return value == null || value.是否已释放() ? null : value.保留();
		}
	}

	/**
	 * 释放错误对象保留的 JS 异常引用。
	 */
	@SimpleFunction
	public void 释放异常值() {
		if (runtime == null) {
			return;
		}
		runtime.checkAccess();
		synchronized (runtime.lock) {
			if (value != null) {
				value.释放();
				value = null;
			}
		}
	}
}
