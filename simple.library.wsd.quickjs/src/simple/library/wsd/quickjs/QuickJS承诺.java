package simple.library.wsd.quickjs;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * 真实 Promise；处理器及 async/await 由运行时任务队列推进。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class QuickJS承诺 extends QuickJS对象 {

	/**
	 * 持有所属实例和拥有引用。
	 *
	 * @param context 所属实例。
	 * @param handle 原生引用。
	 */
	QuickJS承诺(QuickJS实例 context, long handle) {
		super(context, handle);
	}

	/**
	 * 接续操作的结果须为实际 Promise，错误结果在报告类型错误前释放。
	 *
	 * @param value 接续操作产生的拥有引用。
	 * @return 实际承诺包装。
	 */
	private static QuickJS承诺 requirePromise(QuickJS值 value) {
		try {
			return value.到承诺();
		} catch (QuickJS错误 error) {
			value.释放();
			throw error;
		}
	}

	/**
	 * 读取 Promise 状态：0 等待、1 兑现、2 拒绝。
	 *
	 * @return 0 等待、1 兑现、2 拒绝，对应 QuickJS常量中的承诺状态。
	 */
	@SimpleFunction
	public int 取状态() {
		QuickJS值 value = context.op(Native.PROMISE_STATE, this, null, null, null, 0, 0, null, null);
		try {
			return value.到整数();
		} finally {
			value.释放();
		}
	}

	/**
	 * 取得兑现值或拒绝原因；等待状态返回 undefined。
	 *
	 * @return 兑现值或拒绝原因的独立引用；等待时为 undefined。
	 */
	@SimpleFunction
	public QuickJS值 取结果() {
		return context.op(Native.PROMISE_RESULT, this, null, null, null, 0, 0, null, null);
	}

	/**
	 * 安装 then 处理器，返回新 Promise；需执行运行时任务队列。
	 *
	 * @param fulfilled 成功处理器，空表示忽略。
	 * @param rejected 失败处理器，空表示忽略。
	 * @return 后续承诺的独立引用。
	 */
	@SimpleFunction
	public QuickJS承诺 接续(QuickJS函数 fulfilled, QuickJS函数 rejected) {
		return requirePromise(
				context.apply("invoke", this, "then", fulfilled == null ? QuickJS实例.UNDEFINED : fulfilled,
						rejected == null ? QuickJS实例.UNDEFINED : rejected));
	}

	/**
	 * 安装 catch 处理器，只处理拒绝并返回后续承诺；需执行运行时任务队列。
	 *
	 * @param handler 拒绝处理器；空表示继续传递原拒绝。
	 * @return 后续承诺的独立引用。
	 */
	@SimpleFunction
	public QuickJS承诺 捕获拒绝(QuickJS函数 handler) {
		return 接续(null, handler);
	}

	/**
	 * 安装 finally 处理器并返回新 Promise。
	 *
	 * @param handler finally 处理器；空表示忽略。
	 * @return 后续承诺的独立引用。
	 */
	@SimpleFunction
	public QuickJS承诺 最终处理(QuickJS函数 handler) {
		return requirePromise(
				context.apply("invoke", this, "finally", handler == null ? QuickJS实例.UNDEFINED : handler));
	}
}
