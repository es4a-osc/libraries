package simple.library.wsd.quickjs;

/**
 * 固定版本引擎的 JNI 边界；指针不向 Simple 导出。
 *
 * @author 树先生 xhwsd@qq.com
 */
final class Native {
	// 与 bridge.c 的 Operation 一一对应，禁止直接在调用处使用操作编号。
	static final int EVAL_SCRIPT = 0;
	static final int EVAL_MODULE = 1;
	static final int GET_GLOBAL = 2;
	static final int NEW_UNDEFINED = 3;
	static final int NEW_NULL = 4;
	static final int NEW_BOOLEAN = 5;
	static final int NEW_NUMBER = 6;
	static final int NEW_STRING = 7;
	static final int NEW_BIGINT = 8;
	static final int NEW_OBJECT = 9;
	static final int NEW_ARRAY = 10;
	static final int NEW_BUFFER = 11;
	static final int PARSE_JSON = 12;
	static final int GET_PROPERTY = 13;
	static final int SET_PROPERTY = 14;
	static final int HAS_PROPERTY = 15;
	static final int DELETE_PROPERTY = 16;
	static final int GET_INDEX = 17;
	static final int SET_INDEX = 18;
	static final int CALL = 19;
	static final int CONSTRUCT = 20;
	static final int DUPLICATE = 21;
	static final int CALL_INTRINSIC = 22;
	static final int THROW = 23;
	static final int PROMISE_STATE = 24;
	static final int PROMISE_RESULT = 25;
	static final int MODULE_NAMESPACE = 26;
	static final int ENABLE_EXTENDED_NUMBERS = 27;
	static final int READ_VALUE = 28;
	static final int RUN_BYTECODE = 29;
	static final int EVAL_OPTIONS = 30;
	static final int EVAL_MODULE_OPTIONS = 31;
	static final int LOAD_MODULE = 32;
	static final int DETACH_BUFFER = 33;

	static {
		System.loadLibrary("quickjs-android-wrapper");
		System.loadLibrary("es4a-quickjs");
	}

	/**
	 * 禁止构造工具对象。
	 */
	private Native() {
	}

	/**
	 * 创建独立原生运行时。
	 *
	 * @param owner 对应 Java 对象。
	 * @return 原生拥有引用；失败时抛出异常。
	 */
	static synchronized native long runtime(QuickJS运行时 owner);

	/**
	 * 在运行时中创建上下文。
	 *
	 * @param runtime 原生运行时引用。
	 * @param owner 对应 Java 对象。
	 * @return 原生拥有引用；失败时抛出异常。
	 */
	static native long context(long runtime, QuickJS实例 owner);

	/**
	 * 设置外层调用的中断边界。
	 *
	 * @param runtime 原生运行时引用。
	 * @param milliseconds 调用时限，毫秒；0 不限制。
	 */
	static native void begin(long runtime, long milliseconds);

	/**
	 * 结束外层调用并清除中断状态。
	 *
	 * @param runtime 原生运行时引用。
	 */
	static native void stop(long runtime);

	/**
	 * 从其它线程发出中断请求。
	 *
	 * @param runtime 原生运行时引用。
	 */
	static native void cancel(long runtime);

	/**
	 * 设置资源限制或执行垃圾回收。
	 *
	 * @param runtime 原生运行时引用。
	 * @param operation 内部操作编号。
	 * @param value 值引用或资源参数。
	 */
	static native void configure(long runtime, int operation, long value);

	/**
	 * 执行原生值操作并返回拥有引用。
	 *
	 * @param context 原生上下文引用。
	 * @param operation 内部操作编号。
	 * @param left 主操作数引用。
	 * @param right 次操作数引用。
	 * @param text 可选文本输入。
	 * @param name 来源或名称。
	 * @param index 整数输入。
	 * @param number 小数输入。
	 * @param arguments 参数引用数组。
	 * @param binary 二进制输入。
	 * @return 原生拥有引用；失败时抛出异常。
	 */
	static native long value(long context, int operation, long left, long right, String text, String name, long index,
			double number, long[] arguments, byte[] binary);

	/**
	 * 读取 JS 值的实际类型。
	 *
	 * @param context 原生上下文引用。
	 * @param value 值引用或资源参数。
	 * @return 类型编号或已执行的作业数。
	 */
	static native int kind(long context, long value);

	/**
	 * 读取 JS 字符串内容。
	 *
	 * @param context 原生上下文引用。
	 * @param value 值引用或资源参数。
	 * @return 完整文本。
	 */
	static native String string(long context, long value);

	/**
	 * 读取已经转换的 Number。
	 *
	 * @param context 原生上下文引用。
	 * @param value 值引用或资源参数。
	 * @return 数值。
	 */
	static native double number(long context, long value);

	/**
	 * 按 JS 真值规则转换。
	 *
	 * @param context 原生上下文引用。
	 * @param value 值引用或资源参数。
	 * @return 逻辑结果。
	 */
	static native boolean bool(long context, long value);

	/**
	 * 复制缓冲区的可见字节。
	 *
	 * @param context 原生上下文引用。
	 * @param value 值引用或资源参数。
	 * @return 独立字节数组。
	 */
	static native byte[] bytes(long context, long value);

	/**
	 * 编译代码或序列化数据。
	 *
	 * @param context 原生上下文引用。
	 * @param value 值引用或资源参数。
	 * @param source 可选待编译代码。
	 * @param filename 代码来源名称。
	 * @param mode 编译或序列化模式。
	 * @param flags 执行选项标志。
	 * @return 独立字节数组。
	 */
	static native byte[] write(long context, long value, String source, String filename, int mode, int flags);

	/**
	 * 创建绑定到 Java 回调的函数。
	 *
	 * @param context 原生上下文引用。
	 * @param id 宿主回调编号。
	 * @param name 来源或名称。
	 * @param length 函数形参数量。
	 * @return 原生拥有引用；失败时抛出异常。
	 */
	static native long host(long context, int id, String name, int length);

	/**
	 * 创建 Promise 及其解决和拒绝函数。
	 *
	 * @param context 原生上下文引用。
	 * @return 独立原生结果数组。
	 */
	static native long[] promise(long context);

	/**
	 * 执行有数量上限的待执行任务。
	 *
	 * @param runtime 原生运行时引用。
	 * @param maximum 最多执行的作业数。
	 * @return 类型编号或已执行的作业数。
	 */
	static native int jobs(long runtime, int maximum);

	/**
	 * 检查是否存在待执行任务。
	 *
	 * @param runtime 原生运行时引用。
	 * @return 逻辑结果。
	 */
	static native boolean pending(long runtime);

	/**
	 * 读取原生内存统计。
	 *
	 * @param runtime 原生运行时引用。
	 * @return 独立原生结果数组。
	 */
	static native long[] memory(long runtime);

	/**
	 * 读取内部对象身份，仅用于数据图转换，不向 Simple 导出。
	 *
	 * @param context 所属上下文。
	 * @param value 值引用。
	 * @return 对象身份；基本值为 0。
	 */
	static native long identity(long context, long value);

	/**
	 * 释放一个原生拥有引用。
	 *
	 * @param value 值引用或资源参数。
	 */
	static native void release(long value);

	/**
	 * 读取引用所属 Java 实例。
	 *
	 * @param value 值引用或资源参数。
	 * @return 值所属实例；实例关闭时为空。
	 */
	static native QuickJS实例 owner(long value);

	/**
	 * 关闭上下文并保留作业需要的状态。
	 *
	 * @param context 原生上下文引用。
	 */
	static native void closeContext(long context);

	/**
	 * 销毁运行时及剩余上下文。
	 *
	 * @param runtime 原生运行时引用。
	 */
	static native void closeRuntime(long runtime);
}
