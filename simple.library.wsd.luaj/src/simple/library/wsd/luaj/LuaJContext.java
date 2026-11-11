package simple.library.wsd.luaj;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PushbackInputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import org.luaj.vm2.LoadState;
import org.luaj.vm2.LuaBoolean;
import org.luaj.vm2.LuaClosure;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaNil;
import org.luaj.vm2.LuaNumber;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaThread;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.UpValue;
import org.luaj.vm2.compiler.LuaC;
import org.luaj.vm2.lib.BaseLib;
import org.luaj.vm2.lib.DebugLib;
import org.luaj.vm2.lib.MathLib;
import org.luaj.vm2.lib.PackageLib;
import org.luaj.vm2.lib.ResourceFinder;
import org.luaj.vm2.lib.StringLib;
import org.luaj.vm2.lib.VarArgFunction;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.JsePlatform;
import simple.runtime.android.MainActivity;
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
 * 隔离全局表并协调 LuaJ 2.0.1 的进程级运行线程及编译器状态。
 *
 * @author 树先生 xhwsd@qq.com
 */
final class LuaJContext {

	// 上游 running_thread、编译器与基础元表是静态状态，顶层调用必须全库串行。
	private static final Object VM_LOCK = new Object();
	static final ThreadLocal<LuaJContext> OWNER = new ThreadLocal<LuaJContext>();
	final LuaTable globals;
	private final VmState state;
	volatile String lastError = "";

	/**
	 * 创建独立全局表，不把当前执行环境留给另一实例。
	 */
	LuaJContext() {
		if (OWNER.get() != null) {
			throw new LuaJ错误("不能在 LuaJ 执行或宿主回调中创建另一个实例");
		}
		synchronized (VM_LOCK) {
			LuaThread running = LuaThread.getRunning();
			LuaValue previous = running.getfenv();
			VmState previousState = new VmState();
			try {
				// StringLib 只在静态元表为空时初始化，不能沿用另一实例的 __index。
				VmState.clear();
				globals = JsePlatform.standardGlobals();
				// 标准 load/loadstring/loadfile 同样经过二进制捕获槽初始化。
				LoadState.compiler = LuaJContext::compileInput;
				installCoroutines();
				state = new VmState();
			} finally {
				previousState.install();
				running.setfenv(previous);
			}
		}
	}

	/**
	 * 为 Lua 创建的协程登记同一执行所有权，使其直接 Java 调用也能安全重入所属实例。
	 */
	private void installCoroutines() {
		LuaValue library = globals.get("coroutine");
		library.set("create", new VarArgFunction() {
			@Override
			public Varargs invoke(Varargs arguments) {
				return thread(arguments.checkfunction(1));
			}
		});
		library.set("resume", new VarArgFunction() {
			@Override
			public Varargs invoke(Varargs arguments) {
				LuaThread coroutine = arguments.checkthread(1);
				String status = coroutine.getStatus();
				// 标准 resume 对运行中的协程返回失败，不能进入上游的等待路径。
				if (!"suspended".equals(status) && !"dead".equals(status)) {
					return LuaValue.varargsOf(LuaValue.FALSE,
							LuaJContext.text("cannot resume " + status + " coroutine"));
				}
				LuaValue[] values = new LuaValue[arguments.narg() - 1];
				for (int i = 0; i < values.length; i++) {
					values[i] = arguments.arg(i + 2);
				}
				return coroutine.resume(LuaValue.varargsOf(values));
			}
		});
		library.set("wrap", new VarArgFunction() {
			@Override
			public Varargs invoke(Varargs arguments) {
				final LuaThread coroutine = thread(arguments.checkfunction(1));
				return new VarArgFunction() {
					@Override
					public Varargs invoke(Varargs arguments) {
						String status = coroutine.getStatus();
						if (!"suspended".equals(status) && !"dead".equals(status)) {
							throw new LuaError("cannot resume " + status + " coroutine");
						}
						Varargs result = coroutine.resume(arguments);
						if (!result.arg1().toboolean()) {
							throw new LuaError(result.arg(2).tojstring());
						}
						LuaValue[] values = new LuaValue[result.narg() - 1];
						for (int i = 0; i < values.length; i++) {
							values[i] = result.arg(i + 2);
						}
						return LuaValue.varargsOf(values);
					}
				};
			}
		});
	}

	/**
	 * 包装协程入口，所有权随其 Java 线程保留至结束；让出期间只有恢复方可以推进执行。
	 *
	 * @param function 当前实例的协程函数。
	 * @return 尚未运行的协程。
	 */
	LuaThread thread(final LuaValue function) {
		return new LuaThread(new VarArgFunction() {
			@Override
			public Varargs invoke(Varargs arguments) {
				LuaJContext previous = OWNER.get();
				OWNER.set(LuaJContext.this);
				try {
					return function.invoke(arguments).eval();
				} finally {
					if (previous == null) {
						OWNER.remove();
					} else {
						OWNER.set(previous);
					}
				}
			}
		}, globals);
	}

	/**
	 * 在受控 LuaJ 执行环境内完成一项操作。
	 *
	 * @param <T> 结果类型。
	 */
	interface Action<T> {
		/**
		 * 执行已取得环境所有权的操作。
		 *
		 * @return 操作结果。
		 * @throws Exception 加载、转换或宿主调用失败。
		 */
		T run() throws Exception;
	}

	/**
	 * 串行顶层调用；协程中的已登记宿主回调沿用父调用所有权，避免跨线程互锁。
	 *
	 * @param <T>    结果类型。
	 * @param action 要执行的操作。
	 * @return 操作结果。
	 */
	<T> T perform(Action<T> action) {
		LuaJContext callback = OWNER.get();
		if (callback != null) {
			if (callback != this) {
				throw new LuaJ错误("LuaJ 执行或宿主回调不能重入另一个实例");
			}
			// 同一执行链已经选择本实例；再次安装快照会撤销回调中的元表修改。
			return invoke(action);
		}
		synchronized (VM_LOCK) {
			return evaluate(action);
		}
	}

	/**
	 * 临时选择本实例的运行环境，结束时恢复调用方环境并统一错误边界。
	 *
	 * @param <T>    结果类型。
	 * @param action 已取得执行所有权的操作。
	 * @return 操作结果。
	 */
	private <T> T evaluate(Action<T> action) {
		LuaThread running = LuaThread.getRunning();
		LuaValue previous = running.getfenv();
		VmState previousState = new VmState();
		LuaJContext previousOwner = OWNER.get();
		try {
			// Java 互操作同样可能回到本库，所有执行入口都必须登记所有权。
			OWNER.set(this);
			state.install();
			running.setfenv(globals);
			return invoke(action);
		} finally {
			state.capture();
			previousState.install();
			running.setfenv(previous);
			if (previousOwner == null) {
				OWNER.remove();
			} else {
				OWNER.set(previousOwner);
			}
		}
	}

	/**
	 * 执行当前所有权内的操作并保存统一错误，不重新切换全局状态。
	 *
	 * @param <T>    结果类型。
	 * @param action 已取得执行所有权的操作。
	 * @return 操作结果。
	 */
	private <T> T invoke(Action<T> action) {
		try {
			return action.run();
		} catch (LuaJ错误 error) {
			lastError = error.getMessage();
			throw error;
		} catch (Exception error) {
			lastError = "LuaJ 操作失败：" + error.getMessage();
			throw new LuaJ错误("LuaJ 操作失败：" + error.getMessage(), error);
		}
	}

	/**
	 * 持有上游静态库入口及基础类型元表，调用期间按实例选择。
	 *
	 * @author 树先生 xhwsd@qq.com
	 */
	private static final class VmState {
		private BaseLib base;
		private PackageLib packages;
		private ResourceFinder finder;
		private LuaTable stringLibrary;
		private MathLib mathLibrary;
		private LoadState.LuaCompiler compiler;
		private boolean debugEnabled;
		private Object debugState;
		private LuaValue strings;
		private LuaValue numbers;
		private LuaValue booleans;
		private LuaValue nil;
		private LuaValue functions;
		private LuaValue threads;

		/**
		 * 保存当前上游状态。
		 */
		VmState() {
			capture();
		}

		/**
		 * 为新实例清空基础入口和元表，随后由标准库重新初始化。
		 */
		static void clear() {
			BaseLib.instance = null;
			PackageLib.instance = null;
			BaseLib.FINDER = null;
			StringLib.instance = null;
			MathLib.MATHLIB = null;
			LoadState.compiler = null;
			DebugLib.DEBUG_ENABLED = false;
			LuaThread.getRunning().debugState = null;
			LuaString.s_metatable = null;
			LuaNumber.s_metatable = null;
			LuaBoolean.s_metatable = null;
			LuaNil.s_metatable = null;
			LuaFunction.s_metatable = null;
			LuaThread.s_metatable = null;
		}

		/**
		 * 记住本次调用留下的库入口及元表，保留合法的实例内修改。
		 */
		void capture() {
			base = BaseLib.instance;
			packages = PackageLib.instance;
			finder = BaseLib.FINDER;
			stringLibrary = StringLib.instance;
			mathLibrary = MathLib.MATHLIB;
			compiler = LoadState.compiler;
			debugEnabled = DebugLib.DEBUG_ENABLED;
			debugState = LuaThread.getRunning().debugState;
			strings = LuaString.s_metatable;
			numbers = LuaNumber.s_metatable;
			booleans = LuaBoolean.s_metatable;
			nil = LuaNil.s_metatable;
			functions = LuaFunction.s_metatable;
			threads = LuaThread.s_metatable;
		}

		/**
		 * 选择已保存状态；调用者须持有顶层所有权或处于其同步宿主回调中。
		 */
		void install() {
			BaseLib.instance = base;
			PackageLib.instance = packages;
			BaseLib.FINDER = finder;
			StringLib.instance = stringLibrary;
			MathLib.MATHLIB = mathLibrary;
			LoadState.compiler = compiler;
			DebugLib.DEBUG_ENABLED = debugEnabled;
			LuaThread.getRunning().debugState = debugState;
			LuaString.s_metatable = strings;
			LuaNumber.s_metatable = numbers;
			LuaBoolean.s_metatable = booleans;
			LuaNil.s_metatable = nil;
			LuaFunction.s_metatable = functions;
			LuaThread.s_metatable = threads;
		}
	}

	/**
	 * 将 Simple 参数转换为 Lua 值，复制字节数组并拒绝跨实例引用。
	 *
	 * @param value Simple 值；空值转为 nil。
	 * @return 当前实例可使用的 Lua 值。
	 */
	LuaValue convert(Variant value) {
		if (value == null || value instanceof UninitializedVariant) {
			return LuaValue.NIL;
		}
		if (value instanceof BooleanVariant) {
			return LuaValue.valueOf(value.getBoolean());
		}
		if (value instanceof ByteVariant || value instanceof ShortVariant || value instanceof IntegerVariant) {
			return LuaValue.valueOf(value.getInteger());
		}
		if (value instanceof LongVariant) {
			return LuaValue.valueOf((double) value.getLong());
		}
		if (value instanceof SingleVariant || value instanceof DoubleVariant) {
			return LuaValue.valueOf(value.getDouble());
		}
		if (value instanceof StringVariant) {
			return text(value.getString());
		}
		Object object;
		if (value instanceof ObjectVariant) {
			object = value.getObject();
		} else if (value instanceof ArrayVariant) {
			object = value.getArray();
			if (object != null && !(object instanceof byte[])) {
				throw new LuaJ错误("自动转换仅接受字节数组；多个参数请用创建参数");
			}
		} else {
			throw new LuaJ错误("不支持的 Simple 值类型");
		}
		if (object == null) {
			return LuaValue.NIL;
		}
		if (object instanceof LuaJ值) {
			return unwrap((LuaJ值) object);
		}
		if (object instanceof byte[]) {
			return LuaString.valueOf(((byte[]) object).clone());
		}
		return CoerceJavaToLua.coerce(object);
	}

	/**
	 * 取出同一实例拥有的底层值。
	 *
	 * @param value 包装值；空引用表示 nil。
	 * @return 底层值。
	 */
	LuaValue unwrap(LuaJ值 value) {
		if (value == null) {
			return LuaValue.NIL;
		}
		if (value.context != this) {
			throw new LuaJ错误("不能混用不同 LuaJ 实例的值");
		}
		return value.value;
	}

	/**
	 * 按类型返回普通值、表、函数或协程包装，不用空引用代替 nil。
	 *
	 * @param value 底层值；空引用表示 nil。
	 * @return 当前实例拥有的包装。
	 */
	LuaJ值 wrap(LuaValue value) {
		if (value == null) {
			value = LuaValue.NIL;
		}
		if (value.istable()) {
			return new LuaJ表(this, value.checktable());
		}
		if (value.isfunction()) {
			return new LuaJ函数(this, value);
		}
		if (value.isthread()) {
			return new LuaJ协程(this, value.checkthread());
		}
		return new LuaJ值(this, value);
	}

	/**
	 * 建立保留 nil 和顺序的参数列表。
	 *
	 * @param values Simple 参数；空数组引用表示无参数。
	 * @return Lua 参数列表。
	 */
	Varargs arguments(Variant[] values) {
		if (values == null) {
			return LuaValue.NONE;
		}
		LuaValue[] converted = new LuaValue[values.length];
		for (int i = 0; i < values.length; i++) {
			converted[i] = convert(values[i]);
		}
		return LuaValue.varargsOf(converted);
	}

	/**
	 * 在当前全局表中编译 UTF-8 代码，加载与执行分开。
	 *
	 * @param code 完整代码。
	 * @param name 诊断使用的来源名称。
	 * @return 尚未执行的函数。
	 * @throws Exception 编译失败。
	 */
	LuaJ函数 load(String code, String name) throws Exception {
		byte[] bytes = requireText(code).getBytes(StandardCharsets.UTF_8);
		try (ByteArrayInputStream input = new ByteArrayInputStream(bytes)) {
			return loadInput(input, name);
		}
	}

	/**
	 * 读取并编译指定编码的文件；相对路径位于应用私有 files 目录。
	 *
	 * @param path     可读取文件的路径。
	 * @param encoding 字符集名称，例如 UTF-8。
	 * @return 尚未执行的函数。
	 * @throws Exception 文件或编译失败。
	 */
	LuaJ函数 loadFile(String path, String encoding) throws Exception {
		File file = new File(requireText(path));
		if (!file.isAbsolute()) {
			file = new File(MainActivity.getContext().getFilesDir(), path);
		}
		StringBuilder text = new StringBuilder();
		try (Reader reader = new InputStreamReader(new FileInputStream(file),
				Charset.forName(requireText(encoding)).newDecoder().onMalformedInput(CodingErrorAction.REPORT)
						.onUnmappableCharacter(CodingErrorAction.REPORT))) {
			char[] buffer = new char[4096];
			int count;
			while ((count = reader.read(buffer)) != -1) {
				text.append(buffer, 0, count);
			}
		}
		return load(text.toString(), "@" + file.getAbsolutePath());
	}

	/**
	 * 复制 Lua 字符串原始字节，保留零字节和非 UTF-8 数据。
	 *
	 * @param value Lua 字符串。
	 * @return 不共享的字节数组。
	 */
	static byte[] bytes(LuaValue value) {
		LuaString text = value.checkstring();
		return Arrays.copyOfRange(text.m_bytes, text.m_offset, text.m_offset + text.m_length);
	}

	/**
	 * 按标准 UTF-8 建立 Lua 字符串，保留补充平面字符。
	 *
	 * @param text 非空文本引用。
	 * @return Lua 字符串。
	 */
	static LuaString text(String text) {
		return LuaString.valueOf(requireText(text).getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * 解码 Lua 原始字符串，避免上游旧式 UTF-8 转换损坏表情字符。
	 *
	 * @param value 要显示的值。
	 * @return 文本或非字符串值的 Lua 表示。
	 */
	static String display(LuaValue value) {
		if (value.type() == LuaValue.TSTRING) {
			return new String(bytes(value), StandardCharsets.UTF_8);
		}
		return value.tojstring();
	}

	/**
	 * 拒绝空文本引用，允许合法空字符串。
	 *
	 * @param text 调用参数。
	 * @return 原文本。
	 */
	static String requireText(String text) {
		if (text == null) {
			throw new LuaJ错误("文本不能是空引用");
		}
		return text;
	}

	/**
	 * 建立同实例的宿主函数，供全局注册和直接传参共用。
	 *
	 * @param callback 非空回调对象。
	 * @return 当前实例拥有的函数。
	 */
	LuaJ函数 function(LuaJ回调 callback) {
		if (callback == null) {
			throw new LuaJ错误("宿主回调不能为空。");
		}
		return (LuaJ函数) wrap(new VarArgFunction() {
			@Override
			public Varargs invoke(Varargs arguments) {
				LuaJContext previous = OWNER.get();
				OWNER.set(LuaJContext.this);
				try {
					LuaJ参数 result = callback.处理调用(new LuaJ参数(LuaJContext.this, arguments));
					if (result == null) {
						return LuaValue.NONE;
					}
					if (result.context != LuaJContext.this) {
						throw new LuaJ错误("回调结果属于另一个 LuaJ 实例");
					}
					return result.values;
				} catch (RuntimeException error) {
					throw new LuaError(new LuaJ错误("宿主回调失败：" + error.getMessage(), error));
				} finally {
					if (previous == null) {
						OWNER.remove();
					} else {
						OWNER.set(previous);
					}
				}
			}
		});
	}

	/**
	 * 加载原始字节快照，支持 UTF-8 源码和本版本 Lua 字节码。
	 *
	 * @param data 原始内容。
	 * @param name 来源名称。
	 * @return 尚未执行的代码块。
	 * @throws Exception 输入或编译失败。
	 */
	LuaJ函数 loadBytes(byte[] data, String name) throws Exception {
		if (data == null) {
			throw new LuaJ错误("脚本字节集不得为空。");
		}
		try (ByteArrayInputStream input = new ByteArrayInputStream(data.clone())) {
			return loadInput(input, name);
		}
	}

	/**
	 * 加载资产中的 UTF-8 源码或本版本字节码，结束前关闭输入流。
	 *
	 * @param name 资产相对路径。
	 * @return 尚未执行的代码块。
	 * @throws Exception 资产读取或编译失败。
	 */
	LuaJ函数 loadAsset(String name) throws Exception {
		try (InputStream input = MainActivity.getContext().getAssets().open(requireText(name))) {
			return loadInput(input, "@assets/" + name);
		}
	}

	/**
	 * 统一移除单个 UTF-8 BOM，并由上游识别源码或二进制块；输入流由调用者管理。
	 *
	 * @param source 原始输入流。
	 * @param name   来源名称。
	 * @return 同实例代码块。
	 * @throws Exception 输入或编译失败。
	 */
	private LuaJ函数 loadInput(InputStream source, String name) throws Exception {
		return (LuaJ函数) wrap(compileInput(source, name, globals));
	}

	/**
	 * 为宿主和 Lua 标准加载函数共用编译入口，保留传入的环境表。
	 *
	 * @param source      调用者管理的输入流。
	 * @param name        来源名称。
	 * @param environment 代码块的执行环境。
	 * @return 已初始化捕获槽的代码块。
	 * @throws IOException 读取或二进制编译失败。
	 */
	private static LuaFunction compileInput(InputStream source, String name, LuaValue environment) throws IOException {
		requireText(name);
		PushbackInputStream input = new PushbackInputStream(source, 3);
		byte[] prefix = new byte[3];
		int count = 0;
		while (count < prefix.length) {
			int next = input.read();
			if (next < 0) {
				break;
			}
			prefix[count++] = (byte) next;
		}
		if (count != 3 || (prefix[0] & 255) != 239 || (prefix[1] & 255) != 187 || (prefix[2] & 255) != 191) {
			input.unread(prefix, 0, count);
		}
		LuaClosure chunk = LuaC.instance.load(input, name, environment).checkclosure();
		// 二进制块不保存闭包捕获值；上游只分配空槽，须初始化为可读写的 nil。
		for (int i = 0; i < chunk.upValues.length; i++) {
			chunk.upValues[i] = new UpValue(new LuaValue[] { LuaValue.NIL }, 0);
		}
		return chunk;
	}
}
