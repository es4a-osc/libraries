package simple.library.wsd.lua;

import java.util.ArrayList;
import java.util.List;

import org.keplerproject.luajava.JavaFunction;
import org.keplerproject.luajava.LuaException;
import org.keplerproject.luajava.LuaState;
import org.keplerproject.luajava.LuaStateFactory;

import simple.runtime.errors.运行错误;
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
 * 串行保护单个解释器，统一处理值转换、稳定引用和受保护调用。
 *
 * @author 树先生 xhwsd@qq.com
 */
final class LuaContext {

	/**
	 * 当前实例唯一的原生状态，访问时须持有本上下文锁。
	 */
	final LuaState state;

	/**
	 * 标识解释器是否已关闭，关闭后不再访问原生指针。
	 */
	boolean closed;

	/**
	 * 正在执行的受保护调用层数，用于禁止回调期间关闭实例。
	 */
	int activeCalls;

	/**
	 * 当前有效回调视图；嵌套回调须保存并恢复上一视图。
	 */
	Lua堆栈 callbackStack;

	/**
	 * 把宿主可调用对象包装成 Lua 函数的注册表引用。
	 */
	private final int callbackFactory;

	/**
	 * 读取二进制字符串的辅助函数引用，不依赖后续全局库修改。
	 */
	private final int bytesReader;

	/**
	 * 创建独立解释器，打开标准库并持有辅助函数引用。
	 *
	 * @throws RuntimeException 原生库不可加载或解释器初始化失败时抛出。
	 */
	LuaContext() {
		LuaState created = null;
		try {
			// 每个上下文单独持有解释器，不共享窗口或全局实例。
			System.loadLibrary("luajava");
			created = LuaStateFactory.newLuaState();
			created.openLibs();

			// 提前捕获字节读取函数，避免脚本修改标准库后影响二进制读取。
			String source = "local byte, len = string.byte, string.len; "
				+ "return function(s) local t = {}; for i = 1, len(s) do "
				+ "t[i] = byte(s, i) end; return t end, "
				+ "function(f) return function(...) return f(...) end end";
			if (created.LloadString(source) != 0 || created.pcall(0, 2, 0) != 0) {
				throw error("无法初始化 Lua 二进制读取函数");
			}

			// Lua 返回两个函数，按栈顶顺序逆序保存为稳定引用。
			callbackFactory = created.Lref(LuaState.LUA_REGISTRYINDEX);
			bytesReader = created.Lref(LuaState.LUA_REGISTRYINDEX);
			state = created;
		} catch (RuntimeException failure) {
			if (created != null) {
				created.close();
			}
			throw error("Lua 初始化失败", failure);
		} catch (LinkageError failure) {
			if (created != null) {
				created.close();
			}
			throw error("无法加载 LuaJava；附带原生库仅支持 32 位 ARM", failure);
		}
	}

	/* 状态与参数校验 */

	/**
	 * 检查解释器是否仍可使用；调用者须持有当前上下文锁。
	 */
	void requireOpen() {
		if (closed) {
			throw error("Lua 实例已经关闭");
		}
	}

	/**
	 * 拒绝传给 JNI 字符串接口的空引用；空文本本身仍然有效。
	 *
	 * @param text 待检查的文本引用。
	 */
	static void requireText(String text) {
		if (text == null) {
			throw error("Lua 文本参数不能是空引用");
		}
	}

	/**
	 * 确保后续原生操作有足够的额外栈空间，不改变当前栈顶。
	 *
	 * @param count 需要追加的非负栈槽数量。
	 */
	void reserve(int count) {
		requireOpen();
		if (count < 0 || state.checkStack(count) == 0) {
			throw error("Lua 堆栈空间不足");
		}
	}

	/* 值转换 */

	/**
	 * 将普通 Java 值压入栈；基本类型自动转换，Lua 值按引用传递。
	 *
	 * @param value 基本值、字节数组、对象或同一实例的 Lua 值；空引用压入 nil。
	 */
	void pushObject(Object value) {
		reserve(1);
		if (value == null) {
			state.pushNil();
		} else if (value instanceof Lua值) {
			((Lua值) value).push(this);
		} else if (value instanceof Boolean) {
			state.pushBoolean((Boolean) value);
		} else if (value instanceof Number) {
			state.pushNumber(((Number) value).doubleValue());
		} else if (value instanceof String) {
			state.pushString((String) value);
		} else if (value instanceof byte[]) {
			state.pushString((byte[]) value);
		} else {
			reserve(8);
			state.pushJavaObject(value);
		}
	}

	/**
	 * 按变体的实际类型压栈，保留显式 Java 对象的桥接语义。
	 *
	 * @param value Simple 变体；未初始化变体或空引用压入 nil，数组仅接受字节数组。
	 */
	void pushVariant(Variant value) {
		if (value == null || value instanceof UninitializedVariant) {
			pushObject(null);
		} else if (value instanceof BooleanVariant) {
			pushObject(value.getBoolean());
		} else if (value instanceof ByteVariant || value instanceof ShortVariant
			|| value instanceof IntegerVariant || value instanceof LongVariant
			|| value instanceof SingleVariant || value instanceof DoubleVariant) {
			pushObject(value.getDouble());
		} else if (value instanceof StringVariant) {
			pushObject(value.getString());
		} else if (value instanceof ObjectVariant) {
			Object object = value.getObject();
			if (object == null || object instanceof Lua值) {
				pushObject(object);
			} else {
				reserve(8);
				state.pushJavaObject(object);
			}
		} else if (value instanceof ArrayVariant) {
			Object array = value.getArray();
			if (array != null && !(array instanceof byte[])) {
				throw error("Lua 自动参数转换仅支持字节数组");
			}

			pushObject(array);
		} else {
			throw error("此变体类型不能自动转换为 Lua 值");
		}
	}

	/* 全局字段与回调包装 */

	/**
	 * 读取原始全局字段并压栈，不触发全局表的用户元方法。
	 *
	 * @param name 单个全局名称，不解析点号路径。
	 */
	void pushGlobal(String name) {
		requireText(name);
		reserve(1);
		state.pushString(name);
		state.rawGet(LuaState.LUA_GLOBALSINDEX);
	}

	/**
	 * 把栈顶值保存为原始全局字段，并消费该值。
	 *
	 * @param name 单个全局名称，不解析点号路径。
	 */
	void setGlobal(String name) {
		requireText(name);
		reserve(1);
		state.pushString(name);
		state.insert(-2);
		state.rawSet(LuaState.LUA_GLOBALSINDEX);
	}

	/**
	 * 把可调用 Java 对象包装为真正的 Lua 函数，并留在栈顶。
	 *
	 * @param function 已关联当前原生状态的宿主回调。
	 * @throws LuaException Java 回调对象无法压入原生栈时抛出。
	 */
	void pushCallback(JavaFunction function) throws LuaException {
		reserve(8);

		state.rawGetI(LuaState.LUA_REGISTRYINDEX, callbackFactory);
		state.pushJavaFunction(function);
		protectedCall(1, 1);
	}

	/* 稳定引用管理 */

	/**
	 * 复制栈值到注册表，按实际类型建立不依赖原栈位置的引用。
	 *
	 * @param index 原生栈索引；调用前须确认索引有效。
	 * @return 调用者负责释放的稳定值引用。
	 */
	Lua值 capture(int index) {
		reserve(1);
		int kind = state.type(index);
		state.pushValue(index);
		int reference = state.Lref(LuaState.LUA_REGISTRYINDEX);
		if (kind == LuaState.LUA_TTABLE) {
			return new Lua表(this, reference);
		}
		if (kind == LuaState.LUA_TFUNCTION) {
			return new Lua函数(this, reference);
		}

		return new Lua值(this, reference);
	}

	/**
	 * 按顺序持有指定栈顶之后的全部结果，失败时释放已建立的引用。
	 *
	 * @param base 本次操作开始前的原生栈顶数量。
	 * @return 保留中间和末尾 nil 的结果引用数组；不消费原栈结果。
	 */
	Lua值[] captureResults(int base) {
		List<Lua值> results = new ArrayList<Lua值>();
		try {
			for (int index = base + 1; index <= state.getTop(); index++) {
				results.add(capture(index));
			}

			return results.toArray(new Lua值[results.size()]);
		} catch (RuntimeException failure) {
			release(results);
			throw failure;
		}
	}

	/**
	 * 释放批量操作尚未交付调用者的临时引用。
	 *
	 * @param values 需要回收的非空引用列表。
	 */
	static void release(List<Lua值> values) {
		for (Lua值 value : values) {
			value.释放();
		}
	}

	/* 受保护调用与二进制读取 */

	/**
	 * 受保护调用栈中的函数，并在调用期间禁止关闭解释器。
	 *
	 * @param arguments 函数之后已压入的参数数量。
	 * @param results 需要保留的结果数量，-1 表示全部结果。
	 */
	void protectedCall(int arguments, int results) {
		activeCalls++;
		try {
			int status = state.pcall(arguments, results, 0);
			if (status != 0) {
				String message = state.isString(-1) ? state.toString(-1) : "Lua 抛出了非文本错误值";
				throw error("Lua 执行失败：" + message);
			}
		} finally {
			activeCalls--;
		}
	}

	/**
	 * 按原始字节读取 Lua 字符串，保留零字节与非 UTF-8 数据。
	 *
	 * <p>借助解释器初始化时捕获的辅助函数读取，结束时恢复原栈顶。</p>
	 *
	 * @param index 有效的原生栈索引，目标必须是 Lua 字符串。
	 * @return 独立的字节数组。
	 */
	byte[] readBytes(int index) {
		if (state.type(index) != LuaState.LUA_TSTRING) {
			throw error("Lua 值不是字节字符串");
		}

		int top = state.getTop();
		reserve(3);
		int absolute = index > 0 ? index : top + index + 1;

		try {
			state.rawGetI(LuaState.LUA_REGISTRYINDEX, bytesReader);
			state.pushValue(absolute);
			protectedCall(1, 1);

			// 辅助表中的整数逐字节复制，不通过 JNI 字符串编码。
			int length = state.objLen(-1);
			byte[] data = new byte[length];

			for (int i = 0; i < length; i++) {
				state.rawGetI(-1, i + 1);
				data[i] = (byte) state.toInteger(-1);
				state.pop(1);
			}

			return data;
		} finally {
			state.setTop(top);
		}
	}

	/* 错误转换 */

	/**
	 * 创建 Simple 错误分支可以捕获的运行错误。
	 *
	 * @param message 供调用者定位失败原因的说明。
	 * @return 未抛出的运行错误。
	 */
	static RuntimeException error(String message) {
		return new LuaFailure(message);
	}

	/**
	 * 创建运行错误并保留底层失败原因。
	 *
	 * @param message 供调用者定位失败原因的说明。
	 * @param cause 原始失败原因。
	 * @return 已附加失败原因、尚未抛出的运行错误。
	 */
	static RuntimeException error(String message, Throwable cause) {
		RuntimeException failure = error(message);
		failure.initCause(cause);
		return failure;
	}

	/**
	 * 把库内失败交给 Simple 错误处理，不作为类库对象导出。
	 */
	@SuppressWarnings("serial")
	private static final class LuaFailure extends 运行错误 {

		/**
		 * 创建库内部使用的 Simple 运行错误。
		 *
		 * @param message 失败原因说明。
		 */
		LuaFailure(String message) {
			super(message);
		}
	}
}
