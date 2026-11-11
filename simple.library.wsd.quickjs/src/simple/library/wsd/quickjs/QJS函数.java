package simple.library.wsd.quickjs;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.variants.Variant;

/**
 * JS 函数，支持接收者、参数、返回值和构造调用。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class QJS函数 extends QJS对象 {

	/**
	 * 持有所属实例和拥有引用。
	 *
	 * @param context 所属实例。
	 * @param handle  原生引用。
	 */
	QJS函数(QJS实例 context, long handle) {
		super(context, handle);
	}

	/**
	 * 以 undefined 作为 this 调用函数。
	 *
	 * @param arguments Simple 参数数组，空表示无参数。
	 * @return 函数的实际返回值。
	 */
	@SimpleFunction
	public QJS值 调用(Variant[] arguments) {
		return context.call(this, null, arguments, false);
	}

	/**
	 * 指定 this 调用函数。
	 *
	 * @param receiver  this 值。
	 * @param arguments Simple 参数数组。
	 * @return 使用指定 this 后的实际返回值。
	 */
	@SimpleFunction
	public QJS值 带接收者调用(QJS值 receiver, Variant[] arguments) {
		return context.call(this, receiver, arguments, false);
	}

	/**
	 * 通过 new 语义调用构造器，不可构造时抛出错误。
	 *
	 * @param arguments 构造参数。
	 * @return 构造调用产生的 JS 值。
	 */
	@SimpleFunction
	public QJS值 构造(Variant[] arguments) {
		return context.call(this, null, arguments, true);
	}
}
