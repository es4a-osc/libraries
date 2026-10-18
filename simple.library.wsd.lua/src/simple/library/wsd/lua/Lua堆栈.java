package simple.library.wsd.lua;

import org.keplerproject.luajava.LuaException;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.variants.Variant;

/**
 * Lua 实例或同步回调的堆栈操作视图。
 *
 * <p>正索引从 1 开始，负索引从栈顶 -1 开始；不接受伪索引。
 * 回调视图隐藏 LuaJava 自身参数，且仅在当前回调线程和期间有效。
 * 单次方法串行执行；多步底层操作须由调用者保证不交错。</p>
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public class Lua堆栈 {

	/**
	 * 当前视图所属的解释器上下文。
	 */
	final LuaContext context;

	/**
	 * 隐藏的栈底值数量，用于隔离 LuaJava 自身参数。
	 */
	private final int base;

	/**
	 * 回调所属线程；普通实例视图不限定线程。
	 */
	private final Thread callbackThread;

	/**
	 * 回调结束后置为假，拒绝继续访问已失效的调用帧。
	 */
	private boolean valid = true;

	/**
	 * 建立堆栈视图，不取得解释器的关闭权。
	 *
	 * @param context 视图所属的解释器上下文。
	 * @param base 隐藏的栈底参数数量，普通视图为 0，回调视图为 1。
	 * @param callbackThread 回调所属线程；普通视图为空引用。
	 */
	Lua堆栈(LuaContext context, int base, Thread callbackThread) {
		this.context = context;
		this.base = base;
		this.callbackThread = callbackThread;
	}

	/* 栈顶与数量 */

	/**
	 * 取得当前视图可访问的栈值数量。
	 *
	 * @return 栈值数量。
	 */
	@SimpleFunction
	public int 取数量() {
		synchronized (context) {
			requireValid();
			return context.state.getTop() - base;
		}
	}

	/**
	 * 调整栈顶，缩小时弹出值，扩展时填充 nil。
	 *
	 * @param count 非负数量；扩展部分填充 nil。
	 */
	@SimpleFunction
	public void 置数量(int count) {
		synchronized (context) {
			requireValid();
			if (count < 0 || count > Integer.MAX_VALUE - base) {
				throw LuaContext.error("Lua 堆栈数量无效");
			}

			context.reserve(Math.max(0, count - (context.state.getTop() - base)));
			context.state.setTop(base + count);
		}
	}

	/* 值压栈 */

	/**
	 * 将空值压入栈顶，栈值数量增加 1。
	 */
	@SimpleFunction
	public void 压入空值() {
		synchronized (context) {
			requireValid();
			context.reserve(1);
			context.state.pushNil();
		}
	}

	/**
	 * 将整数压入栈顶，栈值数量增加 1。
	 *
	 * @param value 32 位整数，作为 Lua number 传入。
	 */
	@SimpleFunction
	public void 压入整数(int value) {
		synchronized (context) {
			requireValid();
			context.reserve(1);
			context.state.pushNumber(value);
		}
	}

	/**
	 * 将小数压入栈顶，栈值数量增加 1。
	 *
	 * @param value 双精度数值，遵循 Lua 5.1 的 number 规则。
	 */
	@SimpleFunction
	public void 压入小数(double value) {
		synchronized (context) {
			requireValid();
			context.reserve(1);
			context.state.pushNumber(value);
		}
	}

	/**
	 * 将文本压入栈顶，栈值数量增加 1。
	 *
	 * @param value 文本内容，不得为空引用。
	 */
	@SimpleFunction
	public void 压入文本(String value) {
		synchronized (context) {
			requireValid();
			context.reserve(1);
			LuaContext.requireText(value);
			context.state.pushString(value);
		}
	}

	/**
	 * 将逻辑压入栈顶，栈值数量增加 1。
	 *
	 * @param value 待压入的逻辑值。
	 */
	@SimpleFunction
	public void 压入逻辑(boolean value) {
		synchronized (context) {
			requireValid();
			context.reserve(1);
			context.state.pushBoolean(value);
		}
	}

	/**
	 * 将字节集压入栈顶，栈值数量增加 1。
	 *
	 * @param value 原始字节数组；空引用压入 nil。
	 */
	@SimpleFunction
	public void 压入字节集(byte[] value) {
		synchronized (context) {
			requireValid();
			context.reserve(1);
			context.state.pushString(value);
		}
	}

	/**
	 * 将对象压入栈顶，栈值数量增加 1。
	 *
	 * @param value 通过 Java 对象桥接传入；空引用压入 nil。
	 */
	@SimpleFunction
	public void 压入对象(Object value) {
		synchronized (context) {
			requireValid();
			context.reserve(8);
			if (value == null) {
				context.state.pushNil();
			} else {
				context.state.pushJavaObject(value);
			}
		}
	}

	/**
	 * 将值压入栈顶，栈值数量增加 1。
	 *
	 * @param value 同一实例的值引用，空引用压入 nil。
	 */
	@SimpleFunction
	public void 压入值(Lua值 value) {
		synchronized (context) {
			requireValid();
			context.reserve(1);
			if (value == null) {
				context.state.pushNil();
			} else {
				value.push(context);
			}
		}
	}

	/**
	 * 将变体压入栈顶，栈值数量增加 1。
	 *
	 * @param value 基本值、字节集、Java 对象或同一实例的 Lua值。
	 */
	@SimpleFunction
	public void 压入变体(Variant value) {
		synchronized (context) {
			requireValid();
			context.reserve(1);
			context.pushVariant(value);
		}
	}

	/* 值读取 */

	/**
	 * 取得栈值的 Lua 类型，不改变栈。
	 *
	 * @param position 正索引从 1 开始，负索引从 -1 开始。
	 * @return 类型代码，参见 Lua值 的类型常量。
	 */
	@SimpleFunction
	public int 取类型(int position) {
		synchronized (context) {
			return context.state.type(index(position));
		}
	}

	/**
	 * 取得独立值引用，不改变栈；引用使用后应释放。
	 *
	 * @param position 正索引从 1 开始，负索引从 -1 开始。
	 * @return 同一实例的稳定引用。
	 */
	@SimpleFunction
	public Lua值 取值(int position) {
		synchronized (context) {
			return context.capture(index(position));
		}
	}

	/**
	 * 按 Lua 5.1 规则读取整数，不弹出栈值。
	 *
	 * @param position 正索引从 1 开始，负索引从 -1 开始。
	 * @return 整数值。
	 */
	@SimpleFunction
	public int 取整数(int position) {
		synchronized (context) {
			int actual = index(position);
			if (!context.state.isNumber(actual)) {
				throw LuaContext.error("Lua 值不能转换为整数");
			}

			return context.state.toInteger(actual);
		}
	}

	/**
	 * 按 Lua 5.1 规则读取小数，不弹出栈值。
	 *
	 * @param position 正索引从 1 开始，负索引从 -1 开始。
	 * @return 小数值。
	 */
	@SimpleFunction
	public double 取小数(int position) {
		synchronized (context) {
			int actual = index(position);
			if (!context.state.isNumber(actual)) {
				throw LuaContext.error("Lua 值不能转换为小数");
			}

			return context.state.toNumber(actual);
		}
	}

	/**
	 * 按 Lua 5.1 规则读取文本，不弹出栈值。
	 *
	 * @param position 正索引从 1 开始，负索引从 -1 开始。
	 * @return 文本值。
	 */
	@SimpleFunction
	public String 取文本(int position) {
		synchronized (context) {
			int actual = index(position);
			if (!context.state.isString(actual)) {
				throw LuaContext.error("Lua 值不能转换为文本");
			}

			return context.state.toString(actual);
		}
	}

	/**
	 * 按 Lua 5.1 规则读取逻辑，不弹出栈值。
	 *
	 * @param position 正索引从 1 开始，负索引从 -1 开始。
	 * @return 逻辑值；逻辑转换仅 nil 和 false 为假。
	 */
	@SimpleFunction
	public boolean 取逻辑(int position) {
		synchronized (context) {
			int actual = index(position);
			return context.state.toBoolean(actual);
		}
	}

	/**
	 * 按 Lua 5.1 规则读取对象，不弹出栈值。
	 *
	 * @param position 正索引从 1 开始，负索引从 -1 开始。
	 * @return 对象值。
	 */
	@SimpleFunction
	public Object 取对象(int position) {
		synchronized (context) {
			int actual = index(position);
			if (!context.state.isObject(actual)) {
				throw LuaContext.error("Lua 值不能转换为对象");
			}

			try {
				return context.state.getObjectFromUserdata(actual);
			} catch (LuaException failure) {
				throw LuaContext.error("Lua 对象读取失败", failure);
			}
		}
	}

	/**
	 * 按 Lua 5.1 规则读取字节集，不弹出栈值。
	 *
	 * @param position 正索引从 1 开始，负索引从 -1 开始。
	 * @return 字节集值。
	 */
	@SimpleFunction
	public byte[] 取字节集(int position) {
		synchronized (context) {
			int actual = index(position);
			return context.readBytes(actual);
		}
	}

	/* 栈位置调整 */

	/**
	 * 复制指定位置的值到栈顶，栈值数量增加 1。
	 *
	 * @param position 正索引从 1 开始，负索引从 -1 开始。
	 */
	@SimpleFunction
	public void 复制(int position) {
		synchronized (context) {
			int actual = index(position);
			context.reserve(1);
			context.state.pushValue(actual);
		}
	}

	/**
	 * 移除指定位置的值，其后的值前移，栈值数量减少 1。
	 *
	 * @param position 正索引从 1 开始，负索引从 -1 开始。
	 */
	@SimpleFunction
	public void 移除(int position) {
		synchronized (context) {
			int actual = index(position);
			context.state.remove(actual);
		}
	}

	/**
	 * 把栈顶值移动到指定位置，数量不变。
	 *
	 * @param position 正索引从 1 开始，负索引从 -1 开始。
	 */
	@SimpleFunction
	public void 插入(int position) {
		synchronized (context) {
			int actual = index(position);
			context.state.insert(actual);
		}
	}

	/**
	 * 用栈顶值替换指定位置并弹出栈顶。
	 *
	 * @param position 正索引从 1 开始，负索引从 -1 开始。
	 */
	@SimpleFunction
	public void 替换(int position) {
		synchronized (context) {
			int actual = index(position);
			context.state.replace(actual);
		}
	}

	/**
	 * 弹出一个栈值，沿用完整方法的边界检查。
	 */
	@SimpleFunction
	public void 弹出() {
		弹出(1);
	}

	/**
	 * 弹出栈顶指定数量的值。
	 *
	 * @param count 要弹出的非负栈值数量。
	 */
	@SimpleFunction
	public void 弹出(int count) {
		synchronized (context) {
			requireValid();
			if (count < 0 || count > context.state.getTop() - base) {
				throw LuaContext.error("Lua 弹栈数量无效");
			}

			context.state.pop(count);
		}
	}

	/* 受保护函数调用 */

	/**
	 * 调用栈上的函数并保留全部返回值，结果数量参数为 -1。
	 *
	 * <p>这是低层堆栈调用，会用返回值替换函数及参数；不恢复调用前的栈。</p>
	 *
	 * @param argumentCount 栈顶函数后面的参数个数。
	 */
	@SimpleFunction
	public void 调用(int argumentCount) {
		调用(argumentCount, -1);
	}

	/**
	 * 受保护调用栈中的函数；成功消费函数和参数，失败清理本次函数及参数。
	 *
	 * @param argumentCount 函数后已压入的参数数量，必须非负。
	 * @param resultCount 非负数；-1 表示保留全部结果。
	 */
	@SimpleFunction
	public void 调用(int argumentCount, int resultCount) {
		synchronized (context) {
			requireValid();

			int top = context.state.getTop();
			if (argumentCount < 0 || resultCount < -1 || (long) argumentCount + 1 > top - base) {
				throw LuaContext.error("Lua 调用数量无效");
			}

			int start = top - argumentCount - 1;
			if (!context.state.isFunction(start + 1)) {
				throw LuaContext.error("Lua 栈中目标不是函数");
			}

			context.reserve(resultCount < 0 ? 1 : resultCount);
			try {
				context.protectedCall(argumentCount, resultCount);
			} catch (RuntimeException failure) {
				context.state.setTop(start);
				throw failure;
			}
		}
	}

	/* 内部状态与索引校验 */

	/**
	 * 在回调结束时立即使当前视图失效。
	 */
	void invalidate() {
		valid = false;
	}

	/**
	 * 检查实例、回调线程及当前调用帧，拒绝访问失效或不属于当前帧的栈。
	 */
	void requireValid() {
		context.requireOpen();
		if (!valid || (callbackThread != null
			&& (Thread.currentThread() != callbackThread || context.callbackStack != this))) {
			throw LuaContext.error("Lua 回调堆栈仅在原回调线程和期间有效");
		}
		if (callbackThread == null && context.callbackStack != null) {
			throw LuaContext.error("回调期间请使用传入的回调堆栈");
		}
	}

	/**
	 * 把视图索引转换为有效的原生正索引，禁止访问隐藏参数和伪索引。
	 *
	 * @param index 正索引从 1 开始，负索引从栈顶 -1 开始。
	 * @return 对应当前原生调用帧的正索引。
	 */
	int index(int index) {
		requireValid();

		int count = context.state.getTop() - base;
		if (index == 0 || index > count || (long) index < -(long) count) {
			throw LuaContext.error("Lua 堆栈索引超出范围");
		}

		return index > 0 ? base + index : context.state.getTop() + index + 1;
	}
}
