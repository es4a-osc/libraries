package simple.library.wsd.lua;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.variants.Variant;

/**
 * Lua 函数的稳定引用，支持直接传参及多个返回值。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public class Lua函数 extends Lua值 {

	/**
	 * 接管已确认类型为函数的注册表引用。
	 *
	 * @param context 引用所属的解释器上下文。
	 * @param reference 已建立的函数引用编号。
	 */
	Lua函数(LuaContext context, int reference) {
		super(context, reference);
	}

	/* 函数调用 */

	/**
	 * 不传参数调用函数，成功或失败后均恢复原栈。
	 *
	 * @return 结果引用数组，保留所有 nil，逐项释放。
	 */
	@SimpleFunction
	public Lua值[] 调用() {
		return 调用(null);
	}

	/**
	 * 受保护调用函数，成功或失败后均恢复原栈；不使用全局变量中转结果。
	 *
	 * @param arguments 按顺序转换的参数，空引用或空数组表示无参数；Lua值只能来自同一实例。
	 * @return 按原顺序返回的引用数组，包含中间及末尾 nil，逐项释放。
	 */
	@SimpleFunction
	public Lua值[] 调用(Variant[] arguments) {
		synchronized (context) {
			requireValid();

			int top = context.state.getTop();
			int count = arguments == null ? 0 : arguments.length;
			if (count == Integer.MAX_VALUE) {
				throw LuaContext.error("Lua 参数过多");
			}
			context.reserve(count + 1);
			try {
				push(context);
				if (arguments != null) {
					for (Variant argument : arguments) {
						context.pushVariant(argument);
					}
				}
				context.protectedCall(count, -1);
				return context.captureResults(top);
			} finally {
				context.state.setTop(top);
			}
		}
	}
}
