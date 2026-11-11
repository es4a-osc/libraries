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

	/**
	 * 本次传输的字节进度回调。
	 *
	 * @author 树先生 xhwsd@qq.com
	 */
	interface Progress {
		/**
		 * 记录当前上传或下载的字节进度，总量未知时为 -1。
		 *
		 * @param upload      真表示上传，假表示下载。
		 * @param transferred 当前已经传输的字节数。
		 * @param total       总字节数，未知时为 -1。
		 */
		void report(boolean upload, long transferred, long total);
	}

	final Progress progress;
	private boolean cancelled;
	private boolean committed;

	/**
	 * 保存本次传输的进度回调。
	 *
	 * @param progress 分发进度的回调。
	 */
	TransferControl(Progress progress) {
		this.progress = progress;
	}

	/**
	 * 在下载提交前设置取消状态，已经取消或提交时返回假。
	 *
	 * @return 在下载提交前设置取消状态，已经取消或提交时返回假。
	 */
	synchronized boolean cancel() {
		if (cancelled || committed) {
			return false;
		}
		cancelled = true;
		return true;
	}

	/**
	 * 在取消或线程中断后停止传输。
	 *
	 * @throws InterruptedIOException 当前读取、校验或传输无法完成。
	 */
	synchronized void check() throws InterruptedIOException {
		if (cancelled || Thread.currentThread().isInterrupted()) {
			throw new InterruptedIOException("请求已取消。");
		}
	}

	/**
	 * 取得本次传输是否已经取消。
	 *
	 * @return 本次传输是否已经取消。
	 */
	synchronized boolean cancelled() {
		return cancelled;
	}

	/**
	 * 在同一锁内确认未取消，再以原子重命名提交文件。
	 *
	 * @param temporary 本次临时文件或匿名会话标记。
	 * @param target    接收字段或文件内容的目标。
	 * @throws IOException 当前读取、校验或传输无法完成。
	 */
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
