package simple.library.wsd.xhttp2;

import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;

import android.system.ErrnoException;
import android.system.Os;

/**
 * 工作线程和取消命令共享的任务边界，下载提交与取消在同一锁内决定先后顺序。
 *
 * @author 树先生 xhwsd@qq.com
 */
final class TransferControl {

	interface Progress {
		void report(boolean upload, long transferred, long total);
	}

	final Progress progress;
	private boolean cancelled;
	private boolean committed;

	TransferControl(Progress progress) {
		this.progress = progress;
	}

	synchronized boolean cancel() {
		if (cancelled || committed) {
			return false;
		}
		cancelled = true;
		return true;
	}

	synchronized void check() throws InterruptedIOException {
		if (cancelled || Thread.currentThread().isInterrupted()) {
			throw new InterruptedIOException("请求已取消。");
		}
	}

	synchronized boolean cancelled() {
		return cancelled;
	}

	synchronized void commit(File temporary, File target) throws IOException {
		check();
		try {
			// API 24 可用且同目录，替换为原子 rename，不先删除已有目标文件。
			Os.rename(temporary.getAbsolutePath(), target.getAbsolutePath());
			committed = true;
		} catch (ErrnoException failure) {
			throw new IOException("下载文件提交失败。", failure);
		}
	}
}
