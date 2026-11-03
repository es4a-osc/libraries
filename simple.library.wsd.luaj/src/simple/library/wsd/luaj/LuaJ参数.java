package simple.library.wsd.luaj;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.variants.Variant;

/**
 * 函数参数或多返回值的不可变列表快照；索引从 1 开始，中间及末尾 nil 均计数。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class LuaJ参数 {

	final LuaJContext context;
	final Varargs values;

	/**
	 * 计算尾调用并复制所有值的位置，避免持有可变的参数容器。
	 *
	 * @param context 所属实例。
	 * @param values 原参数或返回值。
	 */
	LuaJ参数(LuaJContext context, Varargs values) {
		this.context = context;
		Varargs evaluated = values.eval();
		LuaValue[] copy = new LuaValue[evaluated.narg()];
		for (int i = 0; i < copy.length; i++) {
			copy[i] = evaluated.arg(i + 1);
		}
		this.values = LuaValue.varargsOf(copy);
	}

	/**
	 * 取得参数或返回值数量，计入所有 nil。
	 *
	 * @return 实际数量。
	 */
	@SimpleFunction
	public int 取数量() {
		return context.perform(
				new LuaJContext.Action<Integer>() {
					@Override
					public Integer run() throws Exception {
						return values.narg();
					}
				});
	}

	/**
	 * 按位置读取；超出已有数量时返回 nil，位置必须大于零。
	 *
	 * @param index 从 1 开始的位置。
	 * @return 对应值或 nil。
	 */
	@SimpleFunction
	public LuaJ值 取值(int index) {
		return context.perform(
				new LuaJContext.Action<LuaJ值>() {
					@Override
					public LuaJ值 run() throws Exception {
						if (index < 1) {
							throw new LuaJ错误("参数位置必须从 1 开始");
						}
						return context.wrap(values.arg(index));
					}
				});
	}

	/**
	 * 按顺序取得全部值包装，不丢弃 nil。
	 *
	 * @return 全部值包装。
	 */
	@SimpleFunction
	public LuaJ值[] 取全部() {
		return context.perform(
				new LuaJContext.Action<LuaJ值[]>() {
					@Override
					public LuaJ值[] run() throws Exception {
						LuaJ值[] result = new LuaJ值[values.narg()];
						for (int i = 0; i < result.length; i++) {
							result[i] = context.wrap(values.arg(i + 1));
						}
						return result;
					}
				});
	}

	/**
	 * 取得从指定位置开始的子列表；末项之后的位置返回空列表。
	 *
	 * @param index 1 到数量加 1。
	 * @return 保留 nil 的子列表。
	 */
	@SimpleFunction
	public LuaJ参数 从位置取(int index) {
		return context.perform(
				new LuaJContext.Action<LuaJ参数>() {
					@Override
					public LuaJ参数 run() throws Exception {
						if (index < 1 || index > values.narg() + 1) {
							throw new LuaJ错误("参数子列表位置无效");
						}
						LuaValue[] result = new LuaValue[values.narg() - index + 1];
						for (int i = 0; i < result.length; i++) {
							result[i] = values.arg(index + i);
						}
						return new LuaJ参数(context, LuaValue.varargsOf(result));
					}
				});
	}

	/**
	 * 以当前实例转换一组 Simple 值，可用于构造宿主回调结果。
	 *
	 * @param arguments 按顺序传入的参数；空引用或空数组表示没有参数，空元素保留为 nil。
	 * @return 新参数列表。
	 */
	@SimpleFunction
	public LuaJ参数 生成(Variant[] arguments) {
		return context.perform(
				new LuaJContext.Action<LuaJ参数>() {
					@Override
					public LuaJ参数 run() throws Exception {
						return new LuaJ参数(context, context.arguments(arguments));
					}
				});
	}
}
