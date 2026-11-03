package simple.library.wsd.quickjs;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * 可继承的宿主构造回调，接收新实例、new.target 和借用参数。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public class QuickJS构造回调 {
	/**
	 * 创建构造回调适配对象。
	 */
	public QuickJS构造回调() {
	}

	/**
	 * 初始化新实例；返回对象可替换新实例，空或基本值保留默认新实例。
	 *
	 * @param receiver 新实例，借用引用。
	 * @param newTarget 实际构造目标，借用引用。
	 * @param arguments 构造参数，借用引用。
	 * @return 可选替代对象；返回值会复制。
	 */
	@SimpleFunction
	public QuickJS值 处理构造(QuickJS对象 receiver, QuickJS函数 newTarget, QuickJS参数 arguments) {
		throw new QuickJS错误("请覆写 QuickJS构造回调.处理构造");
	}
}
