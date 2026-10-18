package simple.library.wsd.lua;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * 供 Simple 对象继承并覆写的同步宿主函数。
 *
 * <p>回调参数从逻辑索引 1 开始，压入返回值后返回数量。
 * 不跨线程派发；回调结束后堆栈视图立即失效。</p>
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public class Lua回调 {

	/**
	 * 创建回调基类；派生对象应覆写处理调用方法。
	 */
	public Lua回调() {
	}

	/* 宿主调用入口 */

	/**
	 * 覆写此方法处理 Lua 调用；先读取参数，再压入结果并返回结果数量。
	 *
	 * @param stack 仅在本次回调线程和期间有效；不包含 LuaJava 自身参数。
	 * @return 栈顶作为结果返回的非负数量，可返回多个值。
	 */
	@SimpleFunction
	public int 处理调用(Lua堆栈 stack) {
		throw LuaContext.error("请覆写 Lua回调.处理调用 方法");
	}
}
