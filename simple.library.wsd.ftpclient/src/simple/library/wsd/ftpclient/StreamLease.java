package simple.library.wsd.ftpclient;

import java.io.Closeable;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 独占会话的数据流租约，关闭流后再核对控制连接，失败时丢弃失步连接。
 *
 * @author 树先生 xhwsd@qq.com
 */
final class StreamLease {
	final ClientCore owner;
	final Closeable stream;
	final AtomicBoolean closed = new AtomicBoolean();
	volatile boolean completed;

	/**
	 * 保存本次流占用。
	 *
	 * @param owner 所属客户端。
	 * @param stream 实际网络流。
	 */
	StreamLease(ClientCore owner, Closeable stream) {
		this.owner = owner;
		this.stream = stream;
	}

	/**
	 * 校验同步读写的线程和流状态。
	 */
	void check() {
		ClientCore.background();
		if (closed.get() || owner.disposed.get() || owner.lease != this) {
			throw new FTP错误("数据流已关闭或客户端已销毁。");
		}
	}

	/**
	 * 实际数据读写失败后丢弃连接并释放占用。
	 *
	 * @param error 实际读写错误。
	 * @return 可捕获的失败原因。
	 */
	FTP错误 abort(Exception error) {
		closed.set(true);
		owner.closeNetwork();
		owner.lease = null;
		owner.dataStream = null;
		owner.occupied.set(false);
		return owner.failure(error);
	}

	/**
	 * 关闭数据流并核对最终 FTP 答复，之后释放连接占用。
	 */
	void close() {
		ClientCore.background();
		if (owner.disposed.get()) {
			closed.set(true);
			return;
		}
		if (!closed.compareAndSet(false, true)) {
			return;
		}
		try {
			stream.close();
			owner.remote.finish();
			completed = true;
		} catch (Exception error) {
			owner.closeNetwork();
			throw owner.failure(error);
		} finally {
			owner.lease = null;
			owner.dataStream = null;
			owner.occupied.set(false);
		}
	}
}
