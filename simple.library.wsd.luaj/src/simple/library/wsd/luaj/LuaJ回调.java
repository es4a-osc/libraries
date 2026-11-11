package simple.library.wsd.luaj;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * 由 Simple 对象继承并覆写的同步宿主函数；接收完整参数并返回完整结果列表。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public class LuaJ回调 {

	/**
	 * 创建供派生对象使用的回调基类。
	 */
	public LuaJ回调() {
	}

	/**
	 * 处理 Lua 对宿主函数的调用；在实际执行线程调用，不能创建或重入其它实例。
	 *
	 * @param arguments 本次调用的完整参数；可用生成方法构造同一实例的返回值。
	 * @return 完整返回值；空引用表示无返回值。
	 */
	@SimpleFunction
	public LuaJ参数 处理调用(LuaJ参数 arguments) {
		throw new LuaJ错误("请覆写 LuaJ回调.处理调用");
	}
}
