package simple.library.wsd.quickjs;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * 同步宿主函数适配器；覆写处理调用。this 和参数仅在当前回调有效，需要长期持有时调用保留。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public class QJS回调 {
	/**
	 * 创建可继承的宿主适配对象。
	 */
	public QJS回调() {
	}

	/**
	 * 处理 JS 对宿主函数的同步调用，空返回值表示 undefined。
	 *
	 * @param receiver  JS this 值，借用引用。
	 * @param arguments JS 参数，借用引用。
	 * @return 同一实例的返回值，空表示 undefined；封装复制其值，不转移或释放拥有引用。
	 */
	@SimpleFunction
	public QJS值 处理调用(QJS值 receiver, QJS参数 arguments) {
		throw new QJS错误("请覆写 QJS回调.处理调用");
	}
}
