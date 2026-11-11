package simple.library.wsd.xupdate;

import android.os.Handler;
import android.os.Looper;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * 一次检查的异步结果；首个提交生效，取消或新任务开始后旧结果不再进入流程。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class XU检查回调 {

	/**
	 * 在界面线程消费检查结果，不向调用方公开第三方类型。
	 */
	interface Result {
		/**
		 * 把原始响应交给当前解析策略。
		 *
		 * @param value 响应文本。
		 */
		void response(String value);

		/**
		 * 直接提交业务解析出的更新信息。
		 *
		 * @param value 独立更新信息快照。
		 */
		void information(XU更新信息 value);

		/**
		 * 报告本次检查失败。
		 *
		 * @param message 可显示的实际原因。
		 */
		void failure(String message);
	}

	private final Handler main = new Handler(Looper.getMainLooper());
	private final Result result;
	private boolean submitted;

	/**
	 * 绑定本次检查的接收者；结果提交后不再接受第二次提交。
	 *
	 * @param result 本次检查的接收者。
	 */
	XU检查回调(Result result) {
		this.result = result;
	}

	/**
	 * 提交原始响应，交由当前解析器处理。
	 *
	 * @param response 原始响应文本。
	 */
	@SimpleFunction
	public void 提交响应(final String response) {
		dispatch(() -> {
			result.response(response);
		});
	}

	/**
	 * 直接提交已解析的信息，跳过文本解析器。
	 *
	 * @param info XU更新信息；提交时固定其值。
	 */
	@SimpleFunction
	public void 提交信息(XU更新信息 info) {
		final XU更新信息 snapshot = info == null ? null : info.copy();
		dispatch(() -> {
			result.information(snapshot);
		});
	}

	/**
	 * 报告本次检查失败。
	 *
	 * @param message 错误说明。
	 */
	@SimpleFunction
	public void 报告失败(final String message) {
		dispatch(() -> {
			result.failure(message);
		});
	}

	/**
	 * 串行消费单次结果，支持后台线程提交。
	 *
	 * @param action 首个提交成功后需要执行的主线程操作。
	 */
	private void dispatch(final Runnable action) {
		main.post(() -> {
			if (!submitted) {
				submitted = true;
				action.run();
			}
		});
	}
}
