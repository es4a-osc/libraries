package simple.library.wsd.luaj;

import org.luaj.vm2.LuaThread;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.variants.Variant;

/**
 * Lua 协程对象；恢复返回的首项为成功逻辑，后续为让出值、结束值或错误说明。挂起协程须继续恢复至结束。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class LuaJ协程 extends LuaJ值 {

	/**
	 * 包装当前实例的协程。
	 *
	 * @param context 所属实例。
	 * @param value 有效协程。
	 */
	LuaJ协程(LuaJContext context, LuaThread value) {
		super(context, value);
	}

	/**
	 * 取得上游协程状态，例如 suspended、running 或 dead。
	 *
	 * @return 状态文本。
	 */
	@SimpleFunction
	public String 取状态() {
		return context.perform(
				new LuaJContext.Action<String>() {
					@Override
					public String run() throws Exception {
						return value.checkthread().getStatus();
					}
				});
	}

	/**
	 * 开始或继续协程；首个结果为成功逻辑，后续为让出值或结束值，失败时为错误说明。
	 *
	 * @param arguments 按顺序传入的参数；空引用或空数组表示没有参数，空元素保留为 nil。
	 * @return 包含成功标记的全部结果，保留 nil。
	 */
	@SimpleFunction
	public LuaJ参数 恢复(Variant[] arguments) {
		return context.perform(
				new LuaJContext.Action<LuaJ参数>() {
					@Override
					public LuaJ参数 run() throws Exception {
						LuaThread thread = value.checkthread();
						String status = thread.getStatus();
						if (!"suspended".equals(status) && !"dead".equals(status)) {
							throw new LuaJ错误("不能恢复正在执行的协程");
						}
						return new LuaJ参数(context, thread.resume(context.arguments(arguments)));
					}
				});
	}
}
