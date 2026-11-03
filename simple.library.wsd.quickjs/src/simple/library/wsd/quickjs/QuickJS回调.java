package simple.library.wsd.quickjs;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * 同步宿主函数适配器；覆写处理调用。this 和参数仅在当前回调有效，需要长期持有时调用保留。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public class QuickJS回调 {
	/**
	 * 创建可继承的宿主适配对象。
	 */
	public QuickJS回调() {
	}

	/**
	 * 处理 JS 对宿主函数的同步调用，空返回值表示 undefined。
	 *
	 * @param receiver JS this 值，借用引用。
	 * @param arguments JS 参数，借用引用。
	 * @return 处理 JS 对宿主函数的同步调用，空返回值表示 undefined。
	 */
	@SimpleFunction
	public QuickJS值 处理调用(QuickJS值 receiver, QuickJS参数 arguments) {
		throw new QuickJS错误("请覆写 QuickJS回调.处理调用");
	}
}
