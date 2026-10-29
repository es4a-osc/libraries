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
public final class 检查回调 {

	/** 在界面线程消费检查结果，不向调用方公开第三方类型。 */
	interface Result {
		void response(String value);
		void information(更新信息 value);
		void failure(String message);
	}

	private final Handler main = new Handler(Looper.getMainLooper());
	private final Result result;
	private boolean submitted;

	/** @param result 本次检查的接收者。 */
	检查回调(Result result) {
		this.result = result;
	}

	/**
	 * 提交原始响应，交由当前解析器处理。
	 *
	 * @param response 原始响应文本。
	 */
	@SimpleFunction
	public void 提交响应(final String response) {
		dispatch(new Runnable() {
			@Override
			public void run() {
				result.response(response);
			}
		});
	}

	/**
	 * 直接提交已解析的信息，跳过文本解析器。
	 *
	 * @param info 更新信息；提交时固定其值。
	 */
	@SimpleFunction
	public void 提交信息(更新信息 info) {
		final 更新信息 snapshot = info == null ? null : info.copy();
		dispatch(new Runnable() {
			@Override
			public void run() {
				result.information(snapshot);
			}
		});
	}

	/**
	 * 报告本次检查失败。
	 *
	 * @param message 错误说明。
	 */
	@SimpleFunction
	public void 报告失败(final String message) {
		dispatch(new Runnable() {
			@Override
			public void run() {
				result.failure(message);
			}
		});
	}

	/** 串行消费单次结果，支持后台线程提交。 */
	private void dispatch(final Runnable action) {
		main.post(new Runnable() {
			@Override
			public void run() {
				if (!submitted) {
					submitted = true;
					action.run();
				}
			}
		});
	}
}
