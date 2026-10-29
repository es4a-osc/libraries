package simple.library.wsd.xupdate;

import java.io.File;
import java.io.IOException;

import android.os.Handler;
import android.os.Looper;

import com.xuexiang.xupdate.proxy.IUpdateHttpService;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * 自定义下载器的进度与终止回调；完成后仍由更新流程执行最终文件校验。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class 下载回调 {

	private final Handler main = new Handler(Looper.getMainLooper());
	/** 核心接收精确字节数，不将 long 进度转为浮点比例再反推。 */
	interface Progress {
		void received(long received, long total);
	}

	private final IUpdateHttpService.DownloadCallback callback;
	private final Progress progress;
	private boolean finished;

	/**
	 * 固定本次结果与字节进度接收者。
	 *
	 * @param callback 官方下载结果适配。
	 * @param progress 精确字节进度接收者。
	 */
	下载回调(IUpdateHttpService.DownloadCallback callback, Progress progress) {
		this.callback = callback;
		this.progress = progress;
	}

	/**
	 * 汇报下载进度，尚未校验时不会作为最终完成。
	 *
	 * @param received 已下载字节数。
	 * @param total 总字节数，未知时为零。
	 */
	@SimpleFunction
	public void 报告进度(final long received, final long total) {
		if (received < 0 || total < 0 || (total > 0 && received > total)) {
			throw new IllegalArgumentException("下载字节数无效。");
		}
		main.post(new Runnable() {
			@Override
			public void run() {
				if (!finished) {
					progress.received(received, total);
				}
			}
		});
	}

	/**
	 * 提交已经写入完成的文件，随后执行当前配置的校验。
	 *
	 * @param path 本地文件绝对路径。
	 */
	@SimpleFunction
	public void 提交文件(final String path) {
		main.post(new Runnable() {
			@Override
			public void run() {
				if (!finished) {
					finished = true;
					callback.onSuccess(new File(path));
				}
			}
		});
	}

	/**
	 * 结束下载并报告失败。
	 *
	 * @param message 错误说明。
	 */
	@SimpleFunction
	public void 报告失败(final String message) {
		main.post(new Runnable() {
			@Override
			public void run() {
				if (!finished) {
					finished = true;
					callback.onError(new IOException(message));
				}
			}
		});
	}
}
