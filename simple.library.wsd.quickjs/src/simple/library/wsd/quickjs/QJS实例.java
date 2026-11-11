package simple.library.wsd.quickjs;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import simple.runtime.android.MainActivity;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.variants.ArrayVariant;
import simple.runtime.variants.BooleanVariant;
import simple.runtime.variants.ByteVariant;
import simple.runtime.variants.DoubleVariant;
import simple.runtime.variants.IntegerVariant;
import simple.runtime.variants.LongVariant;
import simple.runtime.variants.ObjectVariant;
import simple.runtime.variants.ShortVariant;
import simple.runtime.variants.SingleVariant;
import simple.runtime.variants.StringVariant;
import simple.runtime.variants.UninitializedVariant;
import simple.runtime.variants.Variant;

/**
 * 独立全局环境；默认自带运行时，也可在同一运行时中创建多个实例。 所有值保留 JS 类型，显式释放引用或销毁实例后不可继续使用。 JS 值形参的空引用表示 undefined；JS null
 * 须通过创建空值传递。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class QJS实例 {
	static final Object UNDEFINED = new Object();
	final QJS运行时 runtime;
	private final boolean ownsRuntime;
	final Set<QJS值> values = new LinkedHashSet<QJS值>();
	private final Map<Integer, QJS回调> callbacks = new HashMap<Integer, QJS回调>();
	private final Map<String, QJS模块> modules = new LinkedHashMap<String, QJS模块>();
	private final Map<String, Map<String, QJS值>> loadedExports = new HashMap<String, Map<String, QJS值>>();
	private final java.util.concurrent.ConcurrentMap<Integer, QJS线程> workers = new java.util.concurrent.ConcurrentHashMap<Integer, QJS线程>();
	String workerBase = "asset:/";
	private int nextWorker = 1;
	private int nextCallback = 1;
	private QJS加载器 loader;
	private QJS监听 rejection;
	private QJS日志 logger = new QJS日志();
	private String lastError = "";
	private QJS错误 lastFailure;
	volatile boolean closed;
	long handle;
	private static final byte[] BYTECODE_HEADER = "ES4A-QJS:3.2.3:f209a855:2\n".getBytes(StandardCharsets.US_ASCII);

	/**
	 * 创建独立运行时及实例；默认原生 JS 堆 64 MiB、顶层调用时限 10 秒。
	 */
	public QJS实例() {
		this(new QJS运行时(), true);
	}

	/**
	 * 在指定运行时中建立独立全局环境。
	 *
	 * @param runtime     所属运行时。
	 * @param ownsRuntime 是否负责销毁运行时。
	 */
	QJS实例(QJS运行时 runtime, boolean ownsRuntime) {
		this.runtime = runtime;
		this.ownsRuntime = ownsRuntime;
		runtime.checkAccess();
		synchronized (runtime.lock) {
			try {
				handle = runtime.attach(this);
				installConsole();
			} catch (RuntimeException | Error error) {
				if (handle != 0) {
					closeInternal();
				}
				if (ownsRuntime) {
					runtime.销毁();
				}
				throw error;
			}
		}
	}

	/**
	 * 包装原生拥有引用；仅在运行时锁内调用。
	 *
	 * @param pointer 原生值引用。
	 * @return 与实际 JS 类型对应的包装。
	 */
	QJS值 wrap(long pointer) {
		if (pointer == 0) {
			throw new QJS错误("QuickJS 未返回有效值");
		}
		int kind;
		try {
			kind = Native.kind(handle, pointer);
		} catch (RuntimeException | Error error) {
			Native.release(pointer);
			throw error;
		}
		QJS值 value;
		if (kind == 8) {
			value = new QJS数组(this, pointer);
		} else if (kind == 9) {
			value = new QJS函数(this, pointer);
		} else if (kind == 10) {
			value = new QJS承诺(this, pointer);
		} else if (kind == 7 || kind == 11 || kind == 12) {
			value = new QJS对象(this, pointer);
		} else {
			value = new QJS值(this, pointer);
		}
		values.add(value);
		return value;
	}

	/**
	 * 核对引用身份及存活状态。
	 *
	 * @param value 待核对值。
	 * @return 原生引用。
	 */
	long pointer(QJS值 value) {
		if (value == null) {
			return 0;
		}
		if (value.context != this) {
			throw new QJS错误("不能隐式混用其它实例的 JS 值");
		}
		if (value.handle == 0 || closed) {
			throw new QJS错误("JS 值或实例已释放");
		}
		return value.handle;
	}

	/**
	 * 执行一次具有统一生命周期和错误边界的原生值操作。
	 *
	 * @param operation 操作编号。
	 * @param left      主操作数。
	 * @param right     次操作数。
	 * @param text      文本数据。
	 * @param name      来源或操作名。
	 * @param index     整数数据。
	 * @param number    小数数据。
	 * @param arguments JS 参数。
	 * @param binary    二进制数据。
	 * @return 拥有独立引用的结果。
	 */
	QJS值 op(final int operation, final QJS值 left, final QJS值 right, final String text, final String name,
			final long index, final double number, final QJS值[] arguments, final byte[] binary) {
		return runtime.perform(this, () -> {
			long[] args = arguments == null ? null : new long[arguments.length];
			if (args != null) {
				if (args.length > 65535) {
					throw new QJS错误("函数参数数量不能超过 65535");
				}
				for (int i = 0; i < args.length; i++) {
					args[i] = pointer(arguments[i]);
				}
			}
			try {
				return wrap(Native.value(handle, operation, pointer(left), pointer(right), text, name, index, number,
						args, binary));
			} catch (NativeFailure error) {
				lastError = error.getMessage();
				throw error;
			}
		});
	}

	/**
	 * 调用捕获的标准内建操作；临时的简单参数在调用后释放。
	 *
	 * @param name      内建操作名。
	 * @param arguments Java 简单参数或已有 JS 值。
	 * @return 独立 JS 结果。
	 */
	QJS值 apply(String name, Object... arguments) {
		runtime.checkAccess();
		synchronized (runtime.lock) {
			QJS值[] converted = new QJS值[arguments.length];
			boolean[] temporary = new boolean[arguments.length];
			try {
				for (int i = 0; i < arguments.length; i++) {
					temporary[i] = !(arguments[i] instanceof QJS值);
					converted[i] = temporary[i] ? fromJava(arguments[i] == null ? UNDEFINED : arguments[i])
							: (QJS值) arguments[i];
				}
				return op(Native.CALL_INTRINSIC, null, null, name, null, 0, 0, converted, null);
			} finally {
				for (int i = 0; i < converted.length; i++) {
					if (temporary[i] && converted[i] != null) {
						converted[i].释放();
					}
				}
			}
		}
	}

	/**
	 * 转换 Java 简单值，不能自动导出任意 Java 对象。
	 *
	 * @param object 简单值、数组或已有 JS 值。
	 * @return 独立引用。
	 */
	QJS值 fromJava(Object object) {
		return ValueConversion.toJS(this, object);
	}

	/**
	 * 转换 Simple 变体中的实际值。
	 *
	 * @param value Simple 变体。
	 * @return Java 简单值。
	 */
	Object unbox(Variant value) {
		if (value == null || value instanceof UninitializedVariant) {
			return null;
		}
		if (value instanceof BooleanVariant) {
			return value.getBoolean();
		}
		if (value instanceof StringVariant) {
			return value.getString();
		}
		if (value instanceof LongVariant) {
			return value.getLong();
		}
		if (value instanceof ByteVariant || value instanceof ShortVariant || value instanceof IntegerVariant) {
			return value.getInteger();
		}
		if (value instanceof SingleVariant || value instanceof DoubleVariant) {
			return value.getDouble();
		}
		if (value instanceof ArrayVariant) {
			return value.getArray();
		}
		if (value instanceof ObjectVariant) {
			return value.getObject();
		}
		throw new QJS错误("不支持的 Simple 变体类型");
	}

	/**
	 * 执行函数参数转换并在调用后回收临时参数。
	 *
	 * @param function  JS 函数。
	 * @param receiver  this 值；空引用表示 undefined。
	 * @param arguments Simple 参数数组。
	 * @param construct 是否作为构造器调用。
	 * @return 调用结果。
	 */
	QJS值 call(QJS值 function, QJS值 receiver, Variant[] arguments, boolean construct) {
		runtime.checkAccess();
		synchronized (runtime.lock) {
			QJS值[] args = new QJS值[arguments == null ? 0 : arguments.length];
			try {
				for (int i = 0; i < args.length; i++) {
					args[i] = 生成值(arguments[i]);
				}
				return op(construct ? Native.CONSTRUCT : Native.CALL, function, receiver, null, null, 0, 0, args, null);
			} finally {
				for (QJS值 value : args) {
					if (value != null) {
						value.释放();
					}
				}
			}
		}
	}

	/**
	 * 从 JNI 同步进入 Simple 宿主回调，返回值先复制再释放借用参数。
	 *
	 * @param id       回调编号。
	 * @param receiver this 值。
	 * @param pointers 参数引用。
	 * @return 交给引擎消费的返回引用。
	 */
	long nativeCall(int id, long receiver, long[] pointers) {
		QJS值 self = wrap(receiver);
		QJS值[] args = new QJS值[pointers.length];
		for (int i = 0; i < args.length; i++) {
			args[i] = wrap(pointers[i]);
		}
		QJS回调 callback = callbacks.get(id);
		QJS值 value = null;
		try {
			if (callback == null) {
				throw new QJS错误("宿主回调已注销");
			}
			value = callback.处理调用(self, new QJS参数(args));
			return Native.value(handle, Native.DUPLICATE, pointer(value), 0, null, null, 0, 0, null, null);
		} finally {
			if (callback instanceof ManagedCallback && value != null) {
				value.释放();
			}
			self.释放();
			for (QJS值 arg : args) {
				arg.释放();
			}
		}
	}

	/**
	 * 移除已被 JS 回收的宿主函数登记；原生终结器在运行时锁内调用，不进入 JS。
	 *
	 * @param id 已失效的回调编号。
	 */
	void nativeDisposeCallback(int id) {
		callbacks.remove(id);
	}

	/**
	 * 接收 Promise 拒绝状态通知；参数仅在当前回调有效。
	 *
	 * @param promise Promise 引用。
	 * @param reason  拒绝原因引用。
	 * @param handled 是否已经出现处理器。
	 */
	void nativeRejected(long promise, long reason, boolean handled) {
		QJS值 p = wrap(promise), r = wrap(reason);
		try {
			if (rejection != null) {
				rejection.状态改变((QJS承诺) p, r, handled);
			}
		} finally {
			p.释放();
			r.释放();
		}
	}

	/**
	 * 将相对模块标识解析到导入者目录，普通模块名保持不变。
	 *
	 * @param base 导入者标识。
	 * @param name 模块标识。
	 * @return 规范化标识。
	 */
	String nativeNormalize(String base, String name) {
		if (loader != null) {
			return require(loader.规范名称(base, name));
		}
		if (name.startsWith("./") || name.startsWith("../")) {
			return URI.create(base).resolve(name).normalize().toString();
		}
		return name;
	}

	/**
	 * 提供模块源码或已验证的字节码。
	 *
	 * @param name 规范化模块名。
	 * @return 首字节为编码标识的模块内容。
	 */
	byte[] nativeLoad(String name) {
		QJS模块 module = modules.get(name);
		if (module == null && loader != null) {
			module = loader.加载模块(name);
		}
		if (module == null) {
			throw new QJS错误("未找到模块：" + name);
		}
		byte[] content = module.nativeContent();
		if (content[0] == 2) {
			Map<String, QJS值> snapshot = module.snapshot(this);
			Map<String, QJS值> old = loadedExports.put(name, snapshot);
			if (old != null) {
				for (QJS值 value : old.values()) {
					value.释放();
				}
			}
		}
		return content;
	}

	/**
	 * 提供宿主模块的固定导出名。
	 *
	 * @param name 模块标识。
	 * @return 导出名数组。
	 */
	String[] nativeModuleNames(String name) {
		return loadedExports.get(name).keySet().toArray(new String[0]);
	}

	/**
	 * 复制宿主模块的导出值，交给原生模块初始化消费。
	 *
	 * @param name   模块标识。
	 * @param member 导出名。
	 * @return 原生拥有引用。
	 */
	long nativeModuleValue(String name, String member) {
		return Native.value(handle, Native.DUPLICATE, pointer(loadedExports.get(name).get(member)), 0, null, null, 0, 0,
				null, null);
	}

	/**
	 * 调用加载器配置模块元信息，回调后释放借用引用。
	 *
	 * @param pointer 元信息引用。
	 * @param name    模块标识。
	 */
	void nativeMeta(long pointer, String name) {
		QJS值 value = wrap(pointer);
		try {
			if (loader != null) {
				loader.初始化元信息(name, value.到对象());
			}
		} finally {
			value.释放();
		}
	}

	/**
	 * 安装 console 的同步输出接口，不提供浏览器或 Node.js 环境。
	 */
	private void installConsole() {
		final QJS实例 self = this;
		QJS对象 console = 创建对象();
		String[] names = { "log", "info", "warn", "error", "debug" };
		for (int i = 0; i < names.length; i++) {
			final int level = i;
			QJS函数 function = 创建函数(names[i], 0, new QJS回调() {
				@Override
				public QJS值 处理调用(QJS值 receiver, QJS参数 arguments) {
					StringBuilder text = new StringBuilder();
					for (int j = 0; j < arguments.取数量(); j++) {
						if (j != 0) {
							text.append(' ');
						}
						text.append(arguments.取值(j).到文本());
					}
					if (self.logger != null) {
						self.logger.输出(level, text.toString());
					}
					return null;
				}
			});
			try {
				console.置成员(names[i], function);
			} finally {
				function.释放();
			}
		}
		try {
			置全局值("console", console);
		} finally {
			console.释放();
		}
	}

	/**
	 * 非空文本参数校验。
	 *
	 * @param text 待校验文本。
	 * @return 原文本。
	 */
	static String require(String text) {
		if (text == null) {
			throw new QJS错误("文本参数不能为 null");
		}
		return text;
	}

	/**
	 * 严格读取 UTF-8 输入，移除可选 BOM。
	 *
	 * @param input 输入流，由调用方关闭。
	 * @return JS 源码。
	 * @throws IOException 读取或编码失败。
	 */
	private static String read(InputStream input) throws IOException {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		byte[] buffer = new byte[8192];
		int count;
		while ((count = input.read(buffer)) != -1) {
			bytes.write(buffer, 0, count);
		}
		String text = StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
				.onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(bytes.toByteArray()))
				.toString();
		return text.startsWith("\ufeff") ? text.substring(1) : text;
	}

	/**
	 * 为字节码或值序列化添加引擎标识和内容摘要；仅接受可信来源。
	 *
	 * @param data 引擎二进制内容。
	 * @param kind 1 为代码，2 为数据。
	 * @return 格式化二进制。
	 */
	static byte[] pack(byte[] data, int kind) {
		try {
			byte[] hash = MessageDigest.getInstance("SHA-256").digest(data);
			byte[] output = Arrays.copyOf(BYTECODE_HEADER, BYTECODE_HEADER.length + 1 + 32 + data.length);
			output[BYTECODE_HEADER.length] = (byte) kind;
			System.arraycopy(hash, 0, output, BYTECODE_HEADER.length + 1, 32);
			System.arraycopy(data, 0, output, BYTECODE_HEADER.length + 33, data.length);
			return output;
		} catch (java.security.NoSuchAlgorithmException error) {
			throw new QJS错误("无法取得 SHA-256");
		}
	}

	/**
	 * 核对来源格式、固定引擎版本及摘要；该检查不能将恶意字节码变为可信。
	 *
	 * @param data 格式化二进制。
	 * @param kind 预期种类。
	 * @return 引擎二进制。
	 */
	static byte[] unpack(byte[] data, int kind) {
		if (data == null || data.length < BYTECODE_HEADER.length + 33) {
			throw new QJS错误("QuickJS 二进制格式不完整");
		}
		for (int i = 0; i < BYTECODE_HEADER.length; i++) {
			if (data[i] != BYTECODE_HEADER[i]) {
				throw new QJS错误("字节码引擎版本或格式不匹配");
			}
		}
		if (data[BYTECODE_HEADER.length] != kind) {
			throw new QJS错误("QuickJS 二进制种类不匹配");
		}
		byte[] payload = Arrays.copyOfRange(data, BYTECODE_HEADER.length + 33, data.length);
		if (!Arrays.equals(pack(payload, kind), data)) {
			throw new QJS错误("QuickJS 二进制摘要不匹配");
		}
		return payload;
	}

	/**
	 * 关闭实例，回收全部拥有引用；共享运行时中的上下文底层资源随运行时统一释放。
	 */
	void closeInternal() {
		if (closed) {
			return;
		}
		stopWorkers();
		runtime.tasks.closeContext(this);
		for (QJS值 value : new ArrayList<QJS值>(values)) {
			value.closeInternal();
		}
		loadedExports.clear();
		callbacks.clear();
		modules.clear();
		loader = null;
		logger = null;
		rejection = null;
		closed = true;
		Native.closeContext(handle);
		handle = 0;
		runtime.detach(this);
	}

	/**
	 * 在当前全局环境执行代码并保留真实结果类型。
	 *
	 * @param code 完整 JS 代码。
	 * @return 最后一个表达式的值；无结果时为 undefined，调用方负责释放。
	 */
	@SimpleFunction
	public QJS值 执行代码(String code) {
		return 执行代码(code, "script");
	}

	/**
	 * 执行代码并指定来源名称。
	 *
	 * @param code     完整 JS 代码。
	 * @param filename 错误定位使用的来源名称。
	 * @return 最后一个表达式的值；无结果时为 undefined，调用方负责释放。
	 */
	@SimpleFunction
	public QJS值 执行代码(String code, String filename) {
		return op(Native.EVAL_SCRIPT, null, null, require(code), require(filename), 0, 0, null, null);
	}

	/**
	 * 编译并执行模块；Promise 后续作业由所属运行时执行。
	 *
	 * @param code     完整 ES 模块代码。
	 * @param filename 唯一模块标识。
	 * @return 模块求值结果；异步初始化须继续执行任务队列。
	 */
	@SimpleFunction
	public QJS值 执行模块(String code, String filename) {
		return op(Native.EVAL_MODULE, null, null, require(code), require(filename), 0, 0, null, null);
	}

	/**
	 * 取得当前全局对象的独立引用。
	 *
	 * @return 当前实例全局对象的独立引用。
	 */
	@SimpleFunction
	public QJS对象 取全局对象() {
		return (QJS对象) op(Native.GET_GLOBAL, null, null, null, null, 0, 0, null, null);
	}

	/**
	 * 创建 JS undefined 值。
	 *
	 * @return undefined 的独立引用。
	 */
	@SimpleFunction
	public QJS值 创建未定义() {
		return op(Native.NEW_UNDEFINED, null, null, null, null, 0, 0, null, null);
	}

	/**
	 * 创建 JS null 值。
	 *
	 * @return null 的独立引用。
	 */
	@SimpleFunction
	public QJS值 创建空值() {
		return op(Native.NEW_NULL, null, null, null, null, 0, 0, null, null);
	}

	/**
	 * 创建普通 JS 对象。
	 *
	 * @return 新对象的独立引用。
	 */
	@SimpleFunction
	public QJS对象 创建对象() {
		return (QJS对象) op(Native.NEW_OBJECT, null, null, null, null, 0, 0, null, null);
	}

	/**
	 * 创建空 JS 数组。
	 *
	 * @return 新空数组的独立引用。
	 */
	@SimpleFunction
	public QJS数组 创建数组() {
		return (QJS数组) op(Native.NEW_ARRAY, null, null, null, null, 0, 0, null, null);
	}

	/**
	 * 创建 JS boolean。
	 *
	 * @param value 逻辑值。
	 * @return boolean 值的独立引用。
	 */
	@SimpleFunction
	public QJS值 创建逻辑(boolean value) {
		return op(Native.NEW_BOOLEAN, null, null, null, null, value ? 1 : 0, 0, null, null);
	}

	/**
	 * 创建 JS Number。
	 *
	 * @param value 数值，允许 NaN 和无穷大。
	 * @return Number 值的独立引用。
	 */
	@SimpleFunction
	public QJS值 创建小数(double value) {
		return op(Native.NEW_NUMBER, null, null, null, null, 0, value, null, null);
	}

	/**
	 * 创建 JS 字符串。
	 *
	 * @param value 文本，可含零字符及表情。
	 * @return 保留完整 UTF-16 内容的字符串值。
	 */
	@SimpleFunction
	public QJS值 创建文本(String value) {
		return op(Native.NEW_STRING, null, null, require(value), null, 0, 0, null, null);
	}

	/**
	 * 创建任意精度 BigInt，非法文本抛出错误。
	 *
	 * @param value 十进制整数文本。
	 * @return 不丢失整数精度的 BigInt 值。
	 */
	@SimpleFunction
	public QJS值 创建大整数(String value) {
		try {
			return op(Native.NEW_BIGINT, null, null, new BigInteger(require(value)).toString(), null, 0, 0, null, null);
		} catch (NumberFormatException error) {
			throw new QJS错误("无效的大整数文本");
		}
	}

	/**
	 * 创建 JS ArrayBuffer。
	 *
	 * @param bytes 复制输入字节，不共享 Java 数组。
	 * @return 持有输入字节副本的 ArrayBuffer。
	 */
	@SimpleFunction
	public QJS对象 创建缓冲区(byte[] bytes) {
		if (bytes == null) {
			throw new QJS错误("字节集不能为 null");
		}
		return (QJS对象) op(Native.NEW_BUFFER, null, null, null, null, 0, 0, null, bytes);
	}

	/**
	 * 转换 Simple 值；超出安全整数范围的长整数转为 BigInt。
	 *
	 * @param value Simple 基本值、数组、字节集或同一实例 JS 值；空转为 null。
	 * @return 保留实际 JS 类型的新引用。
	 */
	@SimpleFunction
	public QJS值 生成值(Variant value) {
		return fromJava(unbox(value));
	}

	/**
	 * 严格解析 JSON，支持对象、数组和基本类型。
	 *
	 * @param json 完整 JSON 文本。
	 * @return 解析后的 JS 值。
	 */
	@SimpleFunction
	public QJS值 解析JSON(String json) {
		return op(Native.PARSE_JSON, null, null, require(json), null, 0, 0, null, null);
	}

	/**
	 * 按 JSON.stringify 语义返回字符串值或 undefined，不伪造 JSON。
	 *
	 * @param value    待序列化值。
	 * @param replacer 替换函数或项目数组；空引用表示省略。
	 * @param space    缩进值；空引用表示省略。
	 * @return JSON 字符串值；无法表示时为 undefined。
	 */
	@SimpleFunction
	public QJS值 生成JSON(QJS值 value, QJS值 replacer, QJS值 space) {
		return apply("json", value, replacer, space);
	}

	/**
	 * 读取全局属性，不存在时返回 undefined。
	 *
	 * @param name 全局属性名。
	 * @return 全局成员值；成员不存在时为 undefined。
	 */
	@SimpleFunction
	public QJS值 取全局值(String name) {
		QJS对象 global = 取全局对象();
		try {
			return global.取成员(name);
		} finally {
			global.释放();
		}
	}

	/**
	 * 设置全局属性。
	 *
	 * @param name  全局属性名。
	 * @param value 同一实例的值。
	 */
	@SimpleFunction
	public void 置全局值(String name, QJS值 value) {
		QJS对象 global = 取全局对象();
		try {
			global.置成员(name, value);
		} finally {
			global.释放();
		}
	}

	/**
	 * 创建可供 JS 调用的宿主函数；参数为借用值，返回值会复制。
	 *
	 * @param name     函数显示名称。
	 * @param length   函数声明的形参数量，0 至 65535。
	 * @param callback 同步宿主回调对象。
	 * @return 可调用的宿主函数引用。
	 */
	@SimpleFunction
	public QJS函数 创建函数(String name, int length, QJS回调 callback) {
		if (callback == null || length < 0 || length > 65535) {
			throw new QJS错误("回调不能为空且形参数量须为 0 至 65535");
		}
		final String functionName = require(name);
		final int arity = length;
		final QJS回调 target = callback;
		return runtime.perform(this, () -> {
			if (nextCallback == Integer.MAX_VALUE) {
				throw new QJS错误("宿主函数登记数量已达上限");
			}
			int id = nextCallback++;
			callbacks.put(id, target);
			try {
				return (QJS函数) wrap(Native.host(handle, id, functionName, arity));
			} catch (RuntimeException | Error error) {
				callbacks.remove(id);
				throw error;
			}
		});
	}

	/**
	 * 创建并注册全局宿主函数。
	 *
	 * @param name     全局函数名。
	 * @param callback 同步宿主回调。
	 */
	@SimpleFunction
	public void 注册函数(String name, QJS回调 callback) {
		QJS函数 function = 创建函数(name, 0, callback);
		try {
			置全局值(name, function);
		} finally {
			function.释放();
		}
	}

	/**
	 * 设置 console.log/info/warn/error/debug 的同步接收器。
	 *
	 * @param listener 日志接收器；空表示不接收。
	 */
	@SimpleFunction
	public void 置日志接收器(QJS日志 listener) {
		runtime.checkAccess();
		synchronized (runtime.lock) {
			if (closed) {
				throw new QJS错误("实例已销毁");
			}
			logger = listener;
		}
	}

	/**
	 * 设置 Promise 未处理拒绝及后续处理状态监听。
	 *
	 * @param listener 监听器；空表示不接收。
	 */
	@SimpleFunction
	public void 置监听(QJS监听 listener) {
		runtime.checkAccess();
		synchronized (runtime.lock) {
			if (closed) {
				throw new QJS错误("实例已销毁");
			}
			rejection = listener;
		}
	}

	/**
	 * 注册源码供 import 加载；不替换已经求值的模块缓存。
	 *
	 * @param name   模块标识。
	 * @param source ES 模块源码。
	 */
	@SimpleFunction
	public void 注册模块(String name, String source) {
		QJS模块 module = new QJS模块();
		module.置源码(require(source));
		runtime.checkAccess();
		synchronized (runtime.lock) {
			if (closed) {
				throw new QJS错误("实例已销毁");
			}
			modules.put(require(name), module);
		}
	}

	/**
	 * 注册模块字节码供 import 加载。
	 *
	 * @param name 模块标识。
	 * @param code 本库同版本编译的可信模块字节码。
	 */
	@SimpleFunction
	public void 注册模块字节码(String name, byte[] code) {
		QJS模块 module = new QJS模块();
		module.置字节码(code);
		runtime.checkAccess();
		synchronized (runtime.lock) {
			if (closed) {
				throw new QJS错误("实例已销毁");
			}
			modules.put(require(name), module);
		}
	}

	/**
	 * 设置动态模块规范化及加载回调。
	 *
	 * @param loader 动态加载器；空恢复仅使用注册模块。
	 */
	@SimpleFunction
	public void 置加载器(QJS加载器 loader) {
		runtime.checkAccess();
		synchronized (runtime.lock) {
			if (closed) {
				throw new QJS错误("实例已销毁");
			}
			this.loader = loader;
		}
	}

	/**
	 * 取得已执行入口模块的命名空间；仅编译不可读取，顶层 await 未完成的导出可能暂不可读。
	 *
	 * @param name 通过执行模块或执行字节码初始化的入口标识。
	 * @return 入口模块导出的命名空间对象。
	 */
	@SimpleFunction
	public QJS对象 取模块命名空间(String name) {
		return (QJS对象) op(Native.MODULE_NAMESPACE, null, null, require(name), null, 0, 0, null, null);
	}

	/**
	 * 编译但不执行，生成仅供本库同版本引擎使用的字节码。
	 *
	 * @param code     完整源码。
	 * @param filename 来源或模块名称。
	 * @return 带引擎标识和摘要的脚本字节码。
	 */
	@SimpleFunction
	public byte[] 编译代码(String code, String filename) {
		final String source = require(code);
		final String file = require(filename);
		return runtime.perform(this, () -> {
			return pack(Native.write(handle, 0, source, file, 1, 0), 1);
		});
	}

	/**
	 * 编译但不执行，生成仅供本库同版本引擎使用的字节码。
	 *
	 * @param code     完整源码。
	 * @param filename 来源或模块名称。
	 * @return 带引擎标识和摘要的模块字节码。
	 */
	@SimpleFunction
	public byte[] 编译模块(String code, String filename) {
		final String source = require(code);
		final String file = require(filename);
		return runtime.perform(this, () -> {
			return pack(Native.write(handle, 0, source, file, 2, 0), 3);
		});
	}

	/**
	 * 校验格式和摘要后执行字节码；不接受不可信二进制。
	 *
	 * @param code 同版本本库生成、来源可信的字节码。
	 * @return 字节码的实际执行结果。
	 */
	@SimpleFunction
	public QJS值 执行字节码(byte[] code) {
		return op(Native.RUN_BYTECODE, null, null, null, null, 0, 0, null, unpack(code,
				code != null && code.length > BYTECODE_HEADER.length && code[BYTECODE_HEADER.length] == 3 ? 3 : 1));
	}

	/**
	 * 按引擎二进制格式复制数据，保留支持的循环引用。
	 *
	 * @param value 可序列化的 JS 数据；函数等不受支持的值会报错。
	 * @return 带引擎标识和摘要的序列化数据。
	 */
	@SimpleFunction
	public byte[] 序列化值(QJS值 value) {
		final QJS值 target = value;
		return runtime.perform(this, () -> {
			return pack(Native.write(handle, pointer(target), null, null, 0, 0), 2);
		});
	}

	/**
	 * 恢复序列化数据；不接受不可信二进制。
	 *
	 * @param data 本库同版本生成的可信数据。
	 * @return 恢复后的 JS 数据引用。
	 */
	@SimpleFunction
	public QJS值 反序列化值(byte[] data) {
		return op(Native.READ_VALUE, null, null, null, null, 0, 0, null, unpack(data, 2));
	}

	/**
	 * 严格按 UTF-8 读取并执行文件。
	 *
	 * @param path 绝对文件路径或相对应用私有 files 目录的路径。
	 * @return 文件脚本的实际执行结果。
	 */
	@SimpleFunction
	public QJS值 执行文件(String path) {
		File file = new File(require(path));
		if (!file.isAbsolute()) {
			file = new File(MainActivity.getContext().getFilesDir(), path);
		}
		try (InputStream input = new FileInputStream(file)) {
			return 执行代码(read(input), file.getAbsolutePath());
		} catch (IOException error) {
			throw new QJS错误("读取脚本失败：" + error.getMessage());
		}
	}

	/**
	 * 严格按 UTF-8 读取并执行资产中的脚本。
	 *
	 * @param name 应用资产名称。
	 * @return 资产脚本的实际执行结果。
	 */
	@SimpleFunction
	public QJS值 执行资产(String name) {
		try (InputStream input = MainActivity.getContext().getAssets().open(require(name))) {
			return 执行代码(read(input), "asset:/" + name);
		} catch (IOException error) {
			throw new QJS错误("读取资产脚本失败：" + error.getMessage());
		}
	}

	/**
	 * 创建由宿主兑现或拒绝的 Promise 及控制函数。
	 *
	 * @return 包含承诺及兑现、拒绝函数的控制器。
	 */
	@SimpleFunction
	public QJS控制器 创建控制器() {
		return runtime.perform(this, () -> {
			long[] refs = Native.promise(handle);
			return new QJS控制器((QJS承诺) wrap(refs[0]), (QJS函数) wrap(refs[1]), (QJS函数) wrap(refs[2]));
		});
	}

	/**
	 * 创建独立 Symbol。
	 *
	 * @param description 符号说明。
	 * @return 新建的独立 Symbol。
	 */
	@SimpleFunction
	public QJS值 创建符号(String description) {
		return apply("symbol", require(description));
	}

	/**
	 * 读取或创建运行时全局注册 Symbol。
	 *
	 * @param key 注册键。
	 * @return 当前运行时内全局注册的 Symbol。
	 */
	@SimpleFunction
	public QJS值 取全局符号(String key) {
		return apply("globalSymbol", require(key));
	}

	/**
	 * 统一校验类型数组构造参数，避免负长度、非法类型或偏移被隐式转换。
	 *
	 * @param type      类型编号。
	 * @param offset    字节偏移。
	 * @param length    元素数量。
	 * @param remainder 是否允许 -1 表示剩余全部。
	 */
	private static void checkTypedArray(int type, int offset, int length, boolean remainder) {
		if (type < 0 || type > 10 || offset < 0 || length < (remainder ? -1 : 0)) {
			throw new QJS错误("类型数组参数无效：类型须为 0 至 10，偏移及长度须在允许范围内");
		}
	}

	/**
	 * 创建类型化数组；类型顺序见 QJS常量。
	 *
	 * @param type   类型常量 0 至 10。
	 * @param length 元素数量，非负。
	 * @return 新建的类型化数组。
	 */
	@SimpleFunction
	public QJS对象 创建类型数组(int type, int length) {
		checkTypedArray(type, 0, length, false);
		return (QJS对象) apply("typed", type, length);
	}

	/**
	 * 以现有缓冲区建立类型数组视图，视图共享底层数据。
	 *
	 * @param type   类型常量 0 至 10。
	 * @param buffer ArrayBuffer。
	 * @param offset 字节偏移，满足元素对齐。
	 * @param length 元素数量；负一表示剩余全部。
	 * @return 共享输入缓冲区的类型化数组视图。
	 */
	@SimpleFunction
	public QJS对象 从缓冲区创建类型数组(int type, QJS值 buffer, int offset, int length) {
		checkTypedArray(type, offset, length, true);
		return (QJS对象) apply("typedView", type, buffer, offset, length == -1 ? UNDEFINED : Integer.valueOf(length));
	}

	/**
	 * 创建 DataView；数据通过成员函数读写。
	 *
	 * @param buffer ArrayBuffer。
	 * @param offset 字节偏移。
	 * @param length 字节数；负一表示剩余全部。
	 * @return 共享输入缓冲区的 DataView。
	 */
	@SimpleFunction
	public QJS对象 创建数据视图(QJS值 buffer, int offset, int length) {
		if (offset < 0 || length < -1) {
			throw new QJS错误("字节偏移不能为负数，长度须为 -1 或非负整数");
		}
		return (QJS对象) apply("dataView", buffer, offset, length == -1 ? UNDEFINED : Integer.valueOf(length));
	}

	/**
	 * 创建 JS Date。
	 *
	 * @param milliseconds Unix 毫秒时间。
	 * @return 对应毫秒时间的 Date 对象。
	 */
	@SimpleFunction
	public QJS对象 创建日期(double milliseconds) {
		return (QJS对象) apply("date", milliseconds);
	}

	/**
	 * 创建 JS RegExp。
	 *
	 * @param pattern 正则表达式。
	 * @param flags   JS 标志，例如 gi。
	 * @return 编译后的 RegExp 对象。
	 */
	@SimpleFunction
	public QJS对象 创建正则(String pattern, String flags) {
		return (QJS对象) apply("regexp", require(pattern), require(flags));
	}

	/**
	 * 创建 JS Map。
	 *
	 * @return 新建的 Map 对象。
	 */
	@SimpleFunction
	public QJS对象 创建Map() {
		return (QJS对象) apply("map");
	}

	/**
	 * 创建 JS Set。
	 *
	 * @return 新建的 Set 对象。
	 */
	@SimpleFunction
	public QJS对象 创建Set() {
		return (QJS对象) apply("set");
	}

	/**
	 * 创建 JS WeakMap。
	 *
	 * @return 新建的 WeakMap 对象。
	 */
	@SimpleFunction
	public QJS对象 创建WeakMap() {
		return (QJS对象) apply("weakMap");
	}

	/**
	 * 创建 JS WeakSet。
	 *
	 * @return 新建的 WeakSet 对象。
	 */
	@SimpleFunction
	public QJS对象 创建WeakSet() {
		return (QJS对象) apply("weakSet");
	}

	/**
	 * 创建 JS Proxy。
	 *
	 * @param target  目标对象。
	 * @param handler 代理处理器。
	 * @return 按处理器规则工作的 Proxy 对象。
	 */
	@SimpleFunction
	public QJS对象 创建代理(QJS对象 target, QJS对象 handler) {
		if (target == null || handler == null) {
			throw new QJS错误("代理目标和处理器不能为空");
		}
		return (QJS对象) apply("proxy", target, handler);
	}

	/**
	 * 启用当前引擎的 BigFloat、BigDecimal 及运算符扩展；不是标准 ECMAScript 功能。
	 */
	@SimpleFunction
	public void 启用扩展数值() {
		op(Native.ENABLE_EXTENDED_NUMBERS, null, null, null, null, 0, 0, null, null).释放();
	}

	/**
	 * 抛出指定 JS 值，可在宿主回调中保留 JS 的原始异常类型和属性。
	 *
	 * @param value 同一实例的异常值；空引用表示 undefined。
	 * @throws QJS错误 携带原始异常值的执行错误。
	 */
	@SimpleFunction
	public void 抛出异常(QJS值 value) {
		op(Native.THROW, value, null, null, null, 0, 0, null, null);
	}

	/**
	 * 取得所属运行时，可用于执行任务及配置资源限制。
	 *
	 * @return 当前实例所属的运行时。
	 */
	@SimpleFunction
	public QJS运行时 取运行时() {
		return runtime;
	}

	/**
	 * 读取最近一次原生执行错误说明；成功调用不会自动清除。
	 *
	 * @return 最近一次原生错误说明；尚无错误或已清除时为空文本。
	 */
	@SimpleFunction
	public String 取最近错误() {
		runtime.checkAccess();
		synchronized (runtime.lock) {
			return lastError;
		}
	}

	/**
	 * 读取最近一次原生错误的详细信息，便于在 Simple 错误分支中检查堆栈及中断原因。
	 *
	 * @return 最近异常；尚无异常或已清除时为空。
	 */
	@SimpleFunction
	public QJS错误 取最近异常() {
		runtime.checkAccess();
		synchronized (runtime.lock) {
			return lastFailure;
		}
	}

	/**
	 * 保存已经转换的原生异常；调用方已持有运行时锁。
	 *
	 * @param error 最近原生错误。
	 */
	void rememberFailure(QJS错误 error) {
		lastError = error.getMessage();
		lastFailure = error;
	}

	/**
	 * 清除保存的错误说明。
	 */
	@SimpleFunction
	public void 清除最近错误() {
		runtime.checkAccess();
		synchronized (runtime.lock) {
			lastError = "";
			lastFailure = null;
		}
	}

	/**
	 * 判断实例是否已销毁。
	 *
	 * @return 实例已销毁时为真，否则为假。
	 */
	@SimpleFunction
	public boolean 是否已销毁() {
		return closed;
	}

	/**
	 * 释放本实例全部值及回调；自带运行时一并销毁，共享运行时另行销毁。
	 */
	@SimpleFunction
	public void 销毁() {
		runtime.checkAccess();
		synchronized (runtime.lock) {
			if (closed) {
				return;
			}
			if (QJS运行时.ACTIVE.get() == runtime) {
				throw new QJS错误("执行或回调中不能销毁实例");
			}
			closeInternal();
			if (ownsRuntime) {
				runtime.销毁();
			}
		}
	}

	/**
	 * 按 Simple 注解绑定宿主方法、属性及字段；Simple 自定义方法使用成员列表显式选择。
	 *
	 * @param target 非空宿主对象。
	 * @return 新 JS 对象引用。
	 */
	@SimpleFunction
	public QJS对象 创建宿主对象(final Object target) {
		return 创建宿主对象(target, null);
	}

	/**
	 * 绑定明确指定的公开成员；空列表表示使用 Simple 注解。参数中的特殊 JS 值仅在调用期间有效。
	 *
	 * @param target  非空宿主对象。
	 * @param members 成员名列表；空引用使用 Simple 注解。
	 * @return 新 JS 对象引用。
	 */
	@SimpleFunction
	public QJS对象 创建宿主对象(final Object target, String[] members) {
		final String[] names = members == null || members.length == 0 ? null : members.clone();
		return runtime.perform(this, () -> {
			return HostBinding.create(QJS实例.this, target, names);
		});
	}

	/**
	 * 建立宿主绑定并设置全局成员，自动释放中间引用。
	 *
	 * @param name   全局成员名。
	 * @param target 宿主对象。
	 */
	@SimpleFunction
	public void 绑定对象(String name, Object target) {
		QJS对象 value = 创建宿主对象(target);
		try {
			置全局值(name, value);
		} finally {
			value.释放();
		}
	}

	/**
	 * 按指定公开成员建立全局宿主对象；Simple 自定义方法使用成员列表显式选择。
	 *
	 * @param name    全局名称。
	 * @param target  宿主对象。
	 * @param members 公开成员名；空使用 Simple 注解。
	 */
	@SimpleFunction
	public void 绑定对象(String name, Object target, String[] members) {
		QJS对象 value = 创建宿主对象(target, members);
		try {
			置全局值(name, value);
		} finally {
			value.释放();
		}
	}

	/**
	 * 创建可被 new、继承及 Reflect.construct 调用的宿主构造函数。
	 *
	 * @param name     函数名。
	 * @param length   形参数量，0 至 65534。
	 * @param callback 构造回调。
	 * @return 构造函数的独立引用。
	 */
	@SimpleFunction
	public QJS函数 创建构造函数(String name, int length, final QJS构造回调 callback) {
		if (callback == null || length < 0 || length > 65534) {
			throw new QJS错误("构造回调不能为空，形参数量须为 0 至 65534");
		}
		QJS函数 host = 创建函数(name, length + 1, new QJS回调() {
			@Override
			public QJS值 处理调用(QJS值 receiver, QJS参数 arguments) {
				QJS值[] args = arguments.取全部();
				return callback.处理构造(receiver.到对象(), args[0].到函数(),
						new QJS参数(Arrays.copyOfRange(args, 1, args.length)));
			}
		});
		try {
			return apply("constructor", host, require(name), length).到函数();
		} finally {
			host.释放();
		}
	}

	/**
	 * 执行普通脚本，支持指定 this、严格模式及顶层 await。
	 *
	 * @param code     源码。
	 * @param filename 来源名称。
	 * @param receiver this；空引用使用全局对象。
	 * @param options  执行选项；空使用默认值。
	 * @return 实际执行结果；启用顶层 await 时返回承诺。
	 */
	@SimpleFunction
	public QJS值 执行代码(String code, String filename, QJS值 receiver, QJS执行选项 options) {
		QJS执行选项 settings = options == null ? new QJS执行选项() : options.snapshot();
		return op(Native.EVAL_OPTIONS, receiver, null, require(code), require(filename), settings.flags(false), 0, null,
				null);
	}

	/**
	 * 执行模块并配置入口元信息，加载器仍可进一步调整 import.meta。
	 *
	 * @param code     模块源码。
	 * @param filename 模块标识。
	 * @param options  执行选项；模块本身允许顶层 await。
	 * @return 模块求值结果。
	 */
	@SimpleFunction
	public QJS值 执行模块(String code, String filename, QJS执行选项 options) {
		QJS执行选项 settings = options == null ? new QJS执行选项() : options.snapshot();
		QJS对象 meta = settings.metadata(this);
		try {
			return op(Native.EVAL_MODULE_OPTIONS, meta, null, require(code), require(filename), settings.flags(true), 0,
					null, null);
		} finally {
			meta.释放();
		}
	}

	/**
	 * 按选项编译普通脚本，字节码执行仍使用其编译时的严格及异步设置。
	 *
	 * @param code     源码。
	 * @param filename 来源名称。
	 * @param options  编译选项；空使用默认值。
	 * @return 同版本可执行的可信字节码。
	 */
	@SimpleFunction
	public byte[] 编译代码(String code, String filename, QJS执行选项 options) {
		return compile(code, filename, options, false);
	}

	/**
	 * 按选项编译 ES 模块；元信息在实际加载时由加载器设置。
	 *
	 * @param code     模块源码。
	 * @param filename 模块标识。
	 * @param options  编译选项；空使用默认值，模块地址和入口选项不写入字节码。
	 * @return 同版本模块字节码。
	 */
	@SimpleFunction
	public byte[] 编译模块(String code, String filename, QJS执行选项 options) {
		return compile(code, filename, options, true);
	}

	/**
	 * 固定源码和选项后在所属运行时中编译。
	 *
	 * @param code     源码。
	 * @param filename 来源。
	 * @param options  选项。
	 * @param module   是否为模块。
	 * @return 带类型及校验信息的字节码。
	 */
	private byte[] compile(String code, String filename, QJS执行选项 options, final boolean module) {
		final String source = require(code), file = require(filename);
		final QJS执行选项 settings = options == null ? new QJS执行选项() : options.snapshot();

		return runtime.perform(this, () -> {
			return pack(Native.write(handle, 0, source, file, module ? 2 : 1, settings.flags(module)), module ? 3 : 1);
		});
	}

	/**
	 * 注册宿主导出对象的快照；default 成员作为默认导出，已加载缓存保持不变。
	 *
	 * @param name    模块标识。
	 * @param exports 导出对象。
	 */
	@SimpleFunction
	public void 注册模块对象(String name, QJS对象 exports) {
		QJS模块 module = new QJS模块();
		module.置导出(exports);
		runtime.checkAccess();
		synchronized (runtime.lock) {
			if (closed) {
				module.释放();
				throw new QJS错误("实例已销毁");
			}
			QJS模块 old = modules.put(require(name), module);
			if (old != null) {
				old.释放();
			}
		}
	}

	/**
	 * 动态导入模块；承诺兑现值为模块命名空间，需执行任务队列或启用自动调度。
	 *
	 * @param name 模块标识。
	 * @param base 导入者标识。
	 * @return 导入结果承诺。
	 */
	@SimpleFunction
	public QJS承诺 导入模块(String name, String base) {
		return op(Native.LOAD_MODULE, null, null, require(name), require(base), 0, 0, null, null).到承诺();
	}

	/**
	 * 分离 ArrayBuffer，使现有视图失效；SharedArrayBuffer 不可分离。
	 *
	 * @param buffer 当前实例的 ArrayBuffer。
	 */
	@SimpleFunction
	public void 分离缓冲区(QJS对象 buffer) {
		if (buffer == null) {
			throw new QJS错误("缓冲区不能为空");
		}
		op(Native.DETACH_BUFFER, buffer, null, null, null, 0, 0, null, null).释放();
	}

	/**
	 * 安装 setTimeout、clearTimeout、setInterval、clearInterval 和 queueMicrotask，并启用自动调度。
	 * 回调在后台调度线程执行，实例销毁或停止调度时取消全部定时器；不接受字符串代码作为回调。
	 */
	@SimpleFunction
	public void 启用计时器() {
		runtime.启动自动调度(100);
		QJS函数 start = 创建函数("schedule", 4, new ManagedCallback() {
			@Override
			public QJS值 处理调用(QJS值 receiver, QJS参数 arguments) {
				QJS函数 function = arguments.取值(0).到函数();
				double delay = arguments.取值(1).到小数();
				if (Double.isNaN(delay) || delay < 0) {
					delay = 0;
				}
				if (delay > Integer.MAX_VALUE) {
					throw new QJS错误("计时器延迟超出范围");
				}
				QJS值[] args = arguments.取值(2).到数组().取全部项目();
				try {
					int id = runtime.tasks.timer(QJS实例.this, function, (long) delay, arguments.取值(3).到逻辑(), args);
					return 创建小数(id);
				} finally {
					for (QJS值 value : args) {
						value.释放();
					}
				}
			}
		});
		QJS函数 cancel = 创建函数("cancel", 1, new ManagedCallback() {
			@Override
			public QJS值 处理调用(QJS值 receiver, QJS参数 arguments) {
				if (arguments.取数量() != 0 && !arguments.取值(0).是否未定义()) {
					double id = arguments.取值(0).到小数();
					if (id >= 1 && id < Integer.MAX_VALUE) {
						runtime.tasks.cancel((int) id, QJS实例.this);
					}
				}
				return null;
			}
		});
		try {
			apply("timers", start, cancel).释放();
		} finally {
			start.释放();
			cancel.释放();
		}
	}

	/**
	 * 创建执行源码的独立运行时工作线程，自动启动接收实例的消息调度。 消息按引擎序列化规则复制，不支持转移列表及 SharedArrayBuffer。
	 *
	 * @param code     工作线程源码。
	 * @param callback 消息、错误及结束回调，可为空。
	 * @return 活动工作线程。
	 */
	@SimpleFunction
	public QJS线程 创建工作线程(String code, QJS线程回调 callback) {
		return createWorker(require(code), "asset:/__worker_source__.js", false, callback);
	}

	/**
	 * 创建按 ES 模块执行的工作线程；相对模块从资产或 file 地址加载。
	 *
	 * @param code     模块源码。
	 * @param name     模块来源标识。
	 * @param callback 消息回调，可为空。
	 * @return 活动工作线程。
	 */
	@SimpleFunction
	public QJS线程 创建模块线程(String code, String name, QJS线程回调 callback) {
		return createWorker(require(code), require(name), true, callback);
	}

	/**
	 * 在运行时锁内登记并启动线程，不等待其它运行时执行。
	 *
	 * @param code     源码。
	 * @param name     来源。
	 * @param module   是否为模块。
	 * @param callback 回调。
	 * @return 工作线程。
	 */
	private QJS线程 createWorker(String code, String name, boolean module, QJS线程回调 callback) {
		runtime.checkAccess();
		synchronized (runtime.lock) {
			if (closed || nextWorker == Integer.MAX_VALUE) {
				throw new QJS错误("实例已销毁或工作线程编号已用尽");
			}
			runtime.启动自动调度(100);
			QJS线程 worker = new QJS线程(nextWorker++, this, callback);
			workers.put(worker.id, worker);
			worker.start(code, name, module);
			return worker;
		}
	}

	/**
	 * 从任意线程移除已经清理的工作线程，不进入 JS。
	 *
	 * @param worker 已结束线程。
	 */
	void forgetWorker(QJS线程 worker) {
		workers.remove(worker.id, worker);
	}

	/**
	 * 停止当前实例的全部工作线程，提交中断及清理后立即返回。
	 */
	void stopWorkers() {
		for (QJS线程 worker : workers.values()) {
			worker.终止();
		}
		workers.clear();
	}

	/**
	 * 读取工作线程源码；相对地址及 asset 地址读取资产，file 地址读取可访问的文件。
	 *
	 * @param address 地址。
	 * @return 严格 UTF-8 源码。
	 */
	static String readWorkerSource(String address) {
		String name = require(address);
		try {
			if (name.startsWith("file:")) {
				try (InputStream input = new FileInputStream(new File(URI.create(name)))) {
					return read(input);
				}
			}
			if (name.startsWith("asset:")) {
				name = name.substring(6);
			}
			while (name.startsWith("/")) {
				name = name.substring(1);
			}
			try (InputStream input = MainActivity.getContext().getAssets().open(name)) {
				return read(input);
			}
		} catch (IOException | IllegalArgumentException error) {
			throw new QJS错误("读取工作线程源码失败：" + error.getMessage());
		}
	}

	/**
	 * 触发工作线程全局 onmessage，消息封装为含 data 的对象。
	 *
	 * @param data 当前实例的消息数据。
	 */
	void dispatchMessage(QJS值 data) {
		QJS对象 global = 取全局对象();
		try {
			dispatchEvent(global, "onmessage", "data", data);
		} finally {
			global.释放();
		}
	}

	/**
	 * 在当前实例内执行事件回调，统一释放事件和处理器临时引用。
	 *
	 * @param target 事件接收对象。
	 * @param member 处理器名。
	 * @param key    事件数据名。
	 * @param data   事件数据。
	 */
	private void dispatchEvent(final QJS对象 target, final String member, final String key, final QJS值 data) {
		runtime.perform(this, () -> {
			QJS值 function = target.取成员(member);
			try {
				if (function.是否函数()) {
					QJS对象 event = 创建对象();
					try {
						event.置成员(key, data);
						op(Native.CALL, function, target, null, null, 0, 0, new QJS值[] { event }, null).释放();
					} finally {
						event.释放();
					}
				}
			} finally {
				function.释放();
			}
			return null;
		});
	}

	/**
	 * 安装 Worker 构造器，源码从应用资产或 file 地址读取，支持 classic 和 module。 消息按 QuickJS 可序列化数据复制，不支持转移列表和
	 * SharedArrayBuffer；回调在后台调度线程执行。
	 */
	@SimpleFunction
	public void 启用工作线程() {
		runtime.启动自动调度(100);
		QJS函数 start = 创建函数("workerStart", 3, new ManagedCallback() {
			@Override
			public QJS值 处理调用(QJS值 self, QJS参数 arguments) {
				final QJS对象 object = arguments.取值(0).保留().到对象();
				try {
					String address = arguments.取值(1).到文本();
					boolean module = arguments.取值(2).到逻辑();
					String name = URI.create(workerBase).resolve(address).normalize().toString();
					QJS线程 worker = createWorker(readWorkerSource(name), name, module, new QJS线程回调() {
						@Override
						public void 收到消息(QJS值 value) {
							dispatchEvent(object, "onmessage", "data", value);
						}

						@Override
						public void 发生错误(String message) {
							QJS值 text = 创建文本(message);
							try {
								dispatchEvent(object, "onerror", "message", text);
							} finally {
								text.释放();
							}
						}

						@Override
						void releaseInternal() {
							object.释放();
						}
					});
					return 创建小数(worker.id);
				} catch (RuntimeException | Error error) {
					object.释放();
					throw error;
				}
			}
		});
		QJS函数 send = 创建函数("workerSend", 2, new ManagedCallback() {
			@Override
			public QJS值 处理调用(QJS值 self, QJS参数 arguments) {
				QJS线程 worker = workers.get(arguments.取值(0).到整数());
				if (worker == null) {
					throw new QJS错误("工作线程已经结束");
				}
				worker.发送(arguments.取值(1));
				return null;
			}
		});
		QJS函数 end = 创建函数("workerEnd", 1, new ManagedCallback() {
			@Override
			public QJS值 处理调用(QJS值 self, QJS参数 arguments) {
				if (arguments.取数量() != 0 && !arguments.取值(0).是否未定义()) {
					QJS线程 worker = workers.get(arguments.取值(0).到整数());
					if (worker != null) {
						worker.终止();
					}
				}
				return null;
			}
		});
		try {
			apply("workers", start, send, end).释放();
		} finally {
			start.释放();
			send.释放();
			end.释放();
		}
	}
}
