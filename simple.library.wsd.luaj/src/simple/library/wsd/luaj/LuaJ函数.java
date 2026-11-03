package simple.library.wsd.luaj;

import org.luaj.vm2.LuaValue;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.variants.Variant;

/**
 * 同一实例拥有的 Lua 函数或已编译代码块，调用保留全部返回值。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class LuaJ函数 extends LuaJ值 {

	/**
	 * 包装当前实例的函数。
	 *
	 * @param context 所属实例。
	 * @param value 有效函数。
	 */
	LuaJ函数(LuaJContext context, LuaValue value) {
		super(context, value);
	}

	/**
	 * 不传参数调用函数，保留全部结果。
	 *
	 * @return 全部返回值。
	 */
	@SimpleFunction
	public LuaJ参数 调用() {
		return context.perform(
				new LuaJContext.Action<LuaJ参数>() {
					@Override
					public LuaJ参数 run() throws Exception {
						return new LuaJ参数(context, value.invoke());
					}
				});
	}

	/**
	 * 传入 Simple 参数并调用，保留全部结果及 nil 位置。
	 *
	 * @param arguments 按顺序传入的参数；空引用或空数组表示没有参数，空元素保留为 nil。
	 * @return 全部返回值。
	 */
	@SimpleFunction
	public LuaJ参数 带参调用(Variant[] arguments) {
		return context.perform(
				new LuaJContext.Action<LuaJ参数>() {
					@Override
					public LuaJ参数 run() throws Exception {
						return new LuaJ参数(context, value.invoke(context.arguments(arguments)));
					}
				});
	}

	/**
	 * 使用同一实例的参数列表调用，保留全部结果。
	 *
	 * @param arguments 同一实例的参数列表；空引用表示无参数。
	 * @return 全部返回值。
	 */
	@SimpleFunction
	public LuaJ参数 使用参数调用(LuaJ参数 arguments) {
		return context.perform(
				new LuaJContext.Action<LuaJ参数>() {
					@Override
					public LuaJ参数 run() throws Exception {
						if (arguments != null && arguments.context != context) {
							throw new LuaJ错误("参数列表属于另一个 LuaJ 实例");
						}
						return new LuaJ参数(
								context,
								value.invoke(arguments == null ? LuaValue.NONE : arguments.values));
					}
				});
	}
}
