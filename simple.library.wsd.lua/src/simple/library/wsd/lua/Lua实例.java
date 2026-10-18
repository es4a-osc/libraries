package simple.library.wsd.lua;

import org.keplerproject.luajava.JavaFunction;
import org.keplerproject.luajava.LuaException;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.variants.Variant;

/**
 * 拥有独立 Lua 5.1.5 解释器的普通对象，继承完整堆栈操作。
 *
 * <p>创建对象即打开标准库；结束使用后显式关闭。高层方法恢复临时栈，
 * 返回的 Lua 值由调用者释放。脚本与应用共享权限，不是安全沙箱。</p>
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class Lua实例 extends Lua堆栈 {

	/**
	 * 创建独立 Lua 5.1.5 解释器并打开标准库。
	 *
	 * @throws RuntimeException 原生依赖不可加载或解释器初始化失败时抛出。
	 */
	public Lua实例() {
		super(new LuaContext(), 0, null);
	}

	/* 实例生命期 */

	/**
	 * 查询解释器是否已经关闭。
	 *
	 * @return 关闭后为真。
	 */
	@SimpleFunction
	public boolean 是否关闭() {
		synchronized (context) {
			return context.closed;
		}
	}

	/**
	 * 释放解释器，所有值引用同时失效；不允许在脚本或回调执行期间关闭。
	 */
	@SimpleFunction
	public void 关闭() {
		synchronized (context) {
			if (context.closed) {
				return;
			}
			if (context.activeCalls != 0) {
				throw LuaContext.error("不能在 Lua 执行或回调期间关闭实例");
			}
			context.state.close();
			context.closed = true;
		}
	}

	/* 脚本执行 */

	/**
	 * 执行代码并按顺序取得全部返回值，包含 nil；失败抛出可捕获错误。
	 *
	 * @param code Lua 5.1 代码；加载后立即执行。
	 * @return 返回值引用数组；无返回值时为空数组，逐项释放。
	 */
	@SimpleFunction
	public Lua值[] 执行代码(String code) {
		return execute(code, false);
	}

	/**
	 * 加载并执行脚本文件，结束后恢复原栈。
	 *
	 * @param path 可读取的本地 Lua 脚本路径；文件访问仍受 Android 权限限制。
	 * @return 全部返回值引用，逐项释放。
	 */
	@SimpleFunction
	public Lua值[] 执行文件(String path) {
		return execute(path, true);
	}

	/* 全局值读写 */

	/**
	 * 读取全局值，不残留临时栈值；不存在时返回 nil 引用。
	 *
	 * @param name 单个全局名称，不解析点号路径。
	 * @return 独立值引用，使用后释放。
	 */
	@SimpleFunction
	public Lua值 取全局(String name) {
		synchronized (context) {
			context.requireOpen();

			int top = context.state.getTop();

			try {
				context.reserve(3);
				LuaContext.requireText(name);

				context.pushGlobal(name);
				return context.capture(-1);
			} finally {
				context.state.setTop(top);
			}
		}
	}

	/**
	 * 设置全局值并恢复原栈。
	 *
	 * @param name 单个全局名称。
	 * @param value 基本值、字节集、Java 对象或同一实例的 Lua值；空值删除全局。
	 */
	@SimpleFunction
	public void 置全局(String name, Variant value) {
		synchronized (context) {
			context.requireOpen();

			int top = context.state.getTop();

			try {
				context.reserve(3);
				LuaContext.requireText(name);

				context.pushVariant(value);
				context.setGlobal(name);
			} finally {
				context.state.setTop(top);
			}
		}
	}

	/* 表和函数 */

	/**
	 * 创建空表并返回稳定引用，不残留栈值。
	 *
	 * @return 新表引用，使用后释放。
	 */
	@SimpleFunction
	public Lua表 创建表() {
		synchronized (context) {
			context.requireOpen();

			int top = context.state.getTop();

			try {
				context.reserve(3);
				context.state.newTable();
				return (Lua表) context.capture(-1);
			} finally {
				context.state.setTop(top);
			}
		}
	}

	/**
	 * 取得全局函数引用；不存在或不是函数时报告错误。
	 *
	 * @param name 全局函数名称。
	 * @return 函数引用，使用后释放。
	 */
	@SimpleFunction
	public Lua函数 取函数(String name) {
		synchronized (context) {
			context.requireOpen();

			int top = context.state.getTop();

			try {
				context.reserve(3);
				LuaContext.requireText(name);

				context.pushGlobal(name);
				if (!context.state.isFunction(-1)) {
					throw LuaContext.error("Lua 全局值不是函数：" + name);
				}

				return (Lua函数) context.capture(-1);
			} finally {
				context.state.setTop(top);
			}
		}
	}

	/**
	 * 不传参数调用全局函数，返回全部结果并恢复原栈。
	 *
	 * @param name 全局函数名称。
	 * @return 结果引用数组，保留所有 nil，逐项释放。
	 */
	@SimpleFunction
	public Lua值[] 调用函数(String name) {
		return 调用函数(name, null);
	}

	/**
	 * 直接调用全局函数，返回全部结果并恢复原栈。
	 *
	 * @param name 全局函数名称。
	 * @param arguments 按顺序传入的参数；空引用或空数组表示无参数。
	 * @return 结果引用数组，保留所有 nil，逐项释放。
	 */
	@SimpleFunction
	public Lua值[] 调用函数(String name, Variant[] arguments) {
		synchronized (context) {
			Lua函数 function = 取函数(name);
			try {
				return function.调用(arguments);
			} finally {
				function.释放();
			}
		}
	}

	/* 宿主回调 */

	/**
	 * 注册同步宿主回调，隐藏 JavaFunction 自身参数；回调不得跨线程保存堆栈。
	 *
	 * @param name Lua 全局函数名称；同名全局值被替换。
	 * @param callback 继承 Lua回调 并覆写 处理调用 方法的对象。
	 */
	@SimpleFunction
	public void 注册函数(String name, Lua回调 callback) {
		synchronized (context) {
			context.requireOpen();

			int top = context.state.getTop();

			try {
				context.reserve(3);
				LuaContext.requireText(name);
				if (callback == null) {
					throw LuaContext.error("Lua 回调不能为空");
				}

				// 回调视图只暴露业务参数；嵌套调用保留上一帧的有效身份。
				JavaFunction function = new JavaFunction(context.state) {
					@Override
					public int execute() throws LuaException {
						synchronized (context) {
							Lua堆栈 stack = new Lua堆栈(context, 1, Thread.currentThread());
							Lua堆栈 previous = context.callbackStack;
							context.callbackStack = stack;
							try {
								int count = callback.处理调用(stack);
								if (count < 0 || count > stack.取数量()) {
									throw LuaContext.error("Lua 回调返回值数量无效");
								}

								return count;
							} catch (RuntimeException failure) {
								throw new LuaException("宿主回调失败：" + failure.getMessage());
							} finally {
								stack.invalidate();
								context.callbackStack = previous;
							}
						}
					}
				};

				// 注册为真正的 Lua 函数，统一支持取函数和高层调用。
				try {
					context.pushCallback(function);
					context.setGlobal(name);
				} catch (LuaException failure) {
					throw LuaContext.error("Lua 回调注册失败", failure);
				}
			} finally {
				context.state.setTop(top);
			}
		}
	}

	/**
	 * 将指定全局名称置为 nil；已经取得的函数引用仍按其生命期有效。
	 *
	 * @param name 要移除的全局函数名称。
	 */
	@SimpleFunction
	public void 注销函数(String name) {
		synchronized (context) {
			context.requireOpen();

			int top = context.state.getTop();

			try {
				context.reserve(3);
				LuaContext.requireText(name);
				context.state.pushNil();
				context.setGlobal(name);
			} finally {
				context.state.setTop(top);
			}
		}
	}

	/* 代码加载与受保护执行 */

	/**
	 * 加载并执行代码或文件，成功和失败均恢复调用前的栈顶。
	 *
	 * @param source Lua 代码或可读取的本地文件路径，不得为空引用。
	 * @param file 为真时按文件路径加载，为假时按代码文本加载。
	 * @return 按原顺序持有的全部结果引用，由调用者释放。
	 */
	private Lua值[] execute(String source, boolean file) {
		synchronized (context) {
			context.requireOpen();
			LuaContext.requireText(source);
			int top = context.state.getTop();
			context.reserve(1);
			try {
				int status = file ? context.state.LloadFile(source) : context.state.LloadString(source);
				if (status != 0) {
					throw LuaContext.error("Lua 加载失败：" + context.state.toString(-1));
				}
				context.protectedCall(0, -1);
				return context.captureResults(top);
			} finally {
				context.state.setTop(top);
			}
		}
	}
}
