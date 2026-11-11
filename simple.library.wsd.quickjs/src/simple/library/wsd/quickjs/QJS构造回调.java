package simple.library.wsd.quickjs;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * 可继承的宿主构造回调，接收新实例、new.target 和借用参数。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public class QJS构造回调 {
	/**
	 * 创建构造回调适配对象。
	 */
	public QJS构造回调() {
	}

	/**
	 * 初始化新实例；返回对象可替换新实例，空或基本值保留默认新实例。
	 *
	 * @param receiver  新实例，借用引用。
	 * @param newTarget 实际构造目标，借用引用。
	 * @param arguments 构造参数，借用引用。
	 * @return 同一实例的可选替代对象；封装复制其值，不转移或释放拥有引用。
	 */
	@SimpleFunction
	public QJS值 处理构造(QJS对象 receiver, QJS函数 newTarget, QJS参数 arguments) {
		throw new QJS错误("请覆写 QJS构造回调.处理构造");
	}
}
