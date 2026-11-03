package simple.library.wsd.ftpclient;

import java.io.IOException;
import java.io.OutputStream;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.SimpleProperty;

/**
 * 独占 FTP 连接的输出流，关闭时自动核对最终 FTP 答复；所有读写及关闭须在后台线程串行调用。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class FTP输出流 {
	private final StreamLease lease;

	/**
	 * 绑定实际数据流租约。
	 *
	 * @param lease 所属会话的独占流。
	 */
	FTP输出流(StreamLease lease) {
		this.lease = lease;
	}

	/**
	 * 是否已经关闭数据流。
	 *
	 * @return 是否已关闭。
	 */
	@SimpleProperty
	public boolean 已关闭() {
		return lease.closed.get() || lease.owner.disposed.get();
	}

	/**
	 * 是否已关闭数据流并收到服务器成功确认。
	 *
	 * @return 是否真正完成。
	 */
	@SimpleProperty
	public boolean 已完成() {
		return lease.completed;
	}

	/**
	 * 写出指定字节；服务器最终结果在关闭时核对。
	 *
	 * @param bytes 待写出内容。
	 */
	@SimpleFunction
	public void 写出(byte[] bytes) {
		lease.check();
		if (bytes == null || bytes.length > 8388608) {
			throw new FTP错误("写出字节不能为空对象或超过 8 MiB。");
		}
		try {
			((OutputStream) lease.stream).write(bytes);
		} catch (IOException error) {
			throw lease.abort(error);
		}
	}

	/**
	 * 将流缓冲中的数据写出；不代表服务器已确认完成。
	 */
	@SimpleFunction
	public void 刷新() {
		lease.check();
		try {
			((OutputStream) lease.stream).flush();
		} catch (IOException error) {
			throw lease.abort(error);
		}
	}

	/**
	 * 关闭并完成 FTP 控制命令；失败抛出 FTP错误并断开连接。重复关闭不会再次发送命令。
	 */
	@SimpleFunction
	public void 关闭() {
		lease.close();
	}
}
