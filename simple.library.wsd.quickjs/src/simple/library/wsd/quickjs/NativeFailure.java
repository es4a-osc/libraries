package simple.library.wsd.quickjs;

/**
 * JNI 失败传输，保留错误堆栈、中断原因及尚未转换的 JS 异常值。
 *
 * @author 树先生 xhwsd@qq.com
 */
final class NativeFailure extends RuntimeException {
	private static final long serialVersionUID = 1L;
	final String trace;
	final int reason;
	final long value;

	/**
	 * 创建未持有 JS 值的原生失败。
	 *
	 * @param message 错误说明。
	 */
	NativeFailure(String message) {
		this(message, "", 0, 0);
	}

	/**
	 * 保存 JNI 返回的诊断数据。
	 *
	 * @param message 错误说明。
	 * @param trace   JavaScript 堆栈。
	 * @param reason  0 普通错误，1 主动取消，2 超时。
	 * @param value   原生异常值的拥有引用。
	 */
	NativeFailure(String message, String trace, int reason, long value) {
		super(message);
		this.trace = trace;
		this.reason = reason;
		this.value = value;
	}
}
