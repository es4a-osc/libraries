package simple.library.wsd.lua;

import org.keplerproject.luajava.LuaException;
import org.keplerproject.luajava.LuaState;

import simple.runtime.annotations.SimpleDataElement;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * 属于单个 Lua 实例的稳定值引用，使用完毕须释放。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public class Lua值 {

	/**
	 * Lua 5.1 的空值类型代码。
	 */
	@SimpleDataElement
	public static final int 类型_空值 = 0;

	/**
	 * Lua 5.1 的逻辑类型代码。
	 */
	@SimpleDataElement
	public static final int 类型_逻辑 = 1;

	/**
	 * Lua 5.1 的轻量对象类型代码。
	 */
	@SimpleDataElement
	public static final int 类型_轻量对象 = 2;

	/**
	 * Lua 5.1 的数值类型代码。
	 */
	@SimpleDataElement
	public static final int 类型_数值 = 3;

	/**
	 * Lua 5.1 的文本类型代码。
	 */
	@SimpleDataElement
	public static final int 类型_文本 = 4;

	/**
	 * Lua 5.1 的表类型代码。
	 */
	@SimpleDataElement
	public static final int 类型_表 = 5;

	/**
	 * Lua 5.1 的函数类型代码。
	 */
	@SimpleDataElement
	public static final int 类型_函数 = 6;

	/**
	 * Lua 5.1 的对象类型代码。
	 */
	@SimpleDataElement
	public static final int 类型_对象 = 7;

	/**
	 * Lua 5.1 的线程类型代码。
	 */
	@SimpleDataElement
	public static final int 类型_线程 = 8;

	/**
	 * 引用所属的解释器上下文。
	 */
	final LuaContext context;

	/**
	 * 注册表引用编号；-1 为 nil，释放后置为 -2。
	 */
	private int reference;

	/**
	 * 标识当前引用是否已释放，避免重复回收。
	 */
	private boolean released;

	/**
	 * 接管已建立的注册表引用，不重新复制原生值。
	 *
	 * @param context 引用所属的解释器上下文。
	 * @param reference 注册表引用编号，-1 表示 nil。
	 */
	Lua值(LuaContext context, int reference) {
		this.context = context;
		this.reference = reference;
	}

	/* 引用生命期 */

	/**
	 * 检查引用及其所属实例是否仍然有效，不访问原生状态。
	 *
	 * @return 未释放且实例未关闭时为真。
	 */
	@SimpleFunction
	public boolean 是否有效() {
		synchronized (context) {
			return !released && !context.closed;
		}
	}

	/**
	 * 显式释放注册表引用，重复释放或实例关闭后释放均无副作用。
	 */
	@SimpleFunction
	public void 释放() {
		synchronized (context) {
			if (!released) {
				if (!context.closed && reference >= 0) {
					context.state.LunRef(LuaState.LUA_REGISTRYINDEX, reference);
				}
				released = true;
				reference = -2;
			}
		}
	}

	/* 值类型 */

	/**
	 * 取得 Lua 类型代码，不改变调用者的栈。
	 *
	 * @return 类型代码，参见类型常量。
	 */
	@SimpleFunction
	public int 取类型() {
		synchronized (context) {
			requireValid();

			int top = context.state.getTop();

			try {
				push(context);
				return context.state.type(-1);
			} finally {
				context.state.setTop(top);
			}
		}
	}

	/**
	 * 判断值是否为 Lua nil。
	 *
	 * @return nil 时为真。
	 */
	@SimpleFunction
	public boolean 是否空值() {
		return 取类型() == 类型_空值;
	}

	/* 值读取与类型转换 */

	/**
	 * 把数值或可转换的数值文本读取为 32 位整数，恢复原栈顶。
	 *
	 * @return 整数结果；小数部分沿用 Lua 5.1 的整数转换规则。
	 * @throws RuntimeException 实例已关闭、引用已释放或值的类型不兼容时抛出。
	 */
	@SimpleFunction
	public int 到整数() {
		synchronized (context) {
			requireValid();

			int top = context.state.getTop();

			try {
				push(context);
				if (!context.state.isNumber(-1)) {
					throw LuaContext.error("Lua 值不能转换为整数");
				}

				return context.state.toInteger(-1);
			} finally {
				context.state.setTop(top);
			}
		}
	}

	/**
	 * 把数值或可转换的数值文本读取为双精度小数，恢复原栈顶。
	 *
	 * @return 双精度结果。
	 * @throws RuntimeException 实例已关闭、引用已释放或值的类型不兼容时抛出。
	 */
	@SimpleFunction
	public double 到小数() {
		synchronized (context) {
			requireValid();

			int top = context.state.getTop();

			try {
				push(context);
				if (!context.state.isNumber(-1)) {
					throw LuaContext.error("Lua 值不能转换为小数");
				}

				return context.state.toNumber(-1);
			} finally {
				context.state.setTop(top);
			}
		}
	}

	/**
	 * 读取 Lua 文本或数值的文本形式，恢复原栈顶。
	 *
	 * @return 文本结果；二进制字符串应使用到字节集读取。
	 * @throws RuntimeException 实例已关闭、引用已释放或值的类型不兼容时抛出。
	 */
	@SimpleFunction
	public String 到文本() {
		synchronized (context) {
			requireValid();

			int top = context.state.getTop();

			try {
				push(context);
				if (!context.state.isString(-1)) {
					throw LuaContext.error("Lua 值不能转换为文本");
				}

				return context.state.toString(-1);
			} finally {
				context.state.setTop(top);
			}
		}
	}

	/**
	 * 读取逻辑，结束后恢复原栈顶。
	 *
	 * @return 逻辑结果；仅 nil 和 false 转为逻辑假。
	 */
	@SimpleFunction
	public boolean 到逻辑() {
		synchronized (context) {
			requireValid();

			int top = context.state.getTop();

			try {
				push(context);
				return context.state.toBoolean(-1);
			} finally {
				context.state.setTop(top);
			}
		}
	}

	/**
	 * 读取 LuaJava 桥接的原 Java 对象，恢复原栈顶。
	 *
	 * @return 被桥接的原对象，不把 Lua 表自动转换为 Java 集合。
	 * @throws RuntimeException 实例已关闭、引用已释放或值的类型不兼容时抛出。
	 */
	@SimpleFunction
	public Object 到对象() {
		synchronized (context) {
			requireValid();

			int top = context.state.getTop();

			try {
				push(context);
				try {
					if (!context.state.isObject(-1)) {
						throw LuaContext.error("Lua 值不能转换为对象");
					}

					return context.state.getObjectFromUserdata(-1);
				} catch (LuaException failure) {
					throw LuaContext.error("Lua 对象读取失败", failure);
				}
			} finally {
				context.state.setTop(top);
			}
		}
	}

	/**
	 * 复制 Lua 字符串的原始字节，恢复原栈顶。
	 *
	 * @return 保留零字节和非 UTF-8 数据的独立数组。
	 * @throws RuntimeException 实例已关闭、引用已释放或值的类型不兼容时抛出。
	 */
	@SimpleFunction
	public byte[] 到字节集() {
		synchronized (context) {
			requireValid();

			int top = context.state.getTop();

			try {
				push(context);
				return context.readBytes(-1);
			} finally {
				context.state.setTop(top);
			}
		}
	}

	/* 表和函数引用转换 */

	/**
	 * 复制为表引用，原引用仍有效；新旧引用分别释放。
	 *
	 * @return 独立的表引用。
	 */
	@SimpleFunction
	public Lua表 到表() {
		synchronized (context) {
			requireValid();

			int top = context.state.getTop();

			try {
				push(context);
				if (context.state.type(-1) != 5) {
					throw LuaContext.error("Lua 值不是表");
				}

				return (Lua表) context.capture(-1);
			} finally {
				context.state.setTop(top);
			}
		}
	}

	/**
	 * 复制为函数引用，原引用仍有效；新旧引用分别释放。
	 *
	 * @return 独立的函数引用。
	 */
	@SimpleFunction
	public Lua函数 到函数() {
		synchronized (context) {
			requireValid();

			int top = context.state.getTop();

			try {
				push(context);
				if (context.state.type(-1) != 6) {
					throw LuaContext.error("Lua 值不是函数");
				}

				return (Lua函数) context.capture(-1);
			} finally {
				context.state.setTop(top);
			}
		}
	}

	/* 引用生命期与所属实例校验 */

	/**
	 * 检查实例与引用生命期，拒绝将失效引用传入 JNI。
	 */
	void requireValid() {
		context.requireOpen();
		if (released) {
			throw LuaContext.error("Lua 值引用已经释放");
		}
	}

	/**
	 * 把值压回所属解释器，不允许跨实例使用引用。
	 *
	 * @param target 准备接收此值的目标上下文，必须与所属上下文相同。
	 */
	void push(LuaContext target) {
		requireValid();
		if (target != context) {
			throw LuaContext.error("Lua 值不能跨实例使用");
		}
		context.reserve(1);
		if (reference == -1) {
			context.state.pushNil();
		} else {
			context.state.rawGetI(LuaState.LUA_REGISTRYINDEX, reference);
		}
	}
}
