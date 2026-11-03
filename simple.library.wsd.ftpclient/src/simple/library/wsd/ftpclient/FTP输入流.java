package simple.library.wsd.ftpclient;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.SimpleProperty;

/**
 * 独占 FTP 连接的输入流，关闭时自动核对最终 FTP 答复；所有读写及关闭须在后台线程串行调用。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class FTP输入流 {
	private final StreamLease lease;
	private boolean ended;

	/**
	 * 绑定实际数据流租约。
	 *
	 * @param lease 所属会话的独占流。
	 */
	FTP输入流(StreamLease lease) {
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
	 * 最近读取是否已经到达文件末尾。
	 *
	 * @return 是否到达 EOF。
	 */
	@SimpleProperty
	public boolean 已结束() {
		return ended;
	}

	/**
	 * 读取最多指定字节；空数组且已结束为真表示 EOF，仍须关闭流。
	 *
	 * @param maximum 最大读取字节数。
	 * @return 实际收到的字节数组。
	 */
	@SimpleFunction
	public byte[] 读入(int maximum) {
		lease.check();
		if (maximum < 1 || maximum > 8388608) {
			throw new FTP错误("读取数量须为 1 至 8388608。");
		}
		try {
			byte[] buffer = new byte[maximum];
			int count = ((InputStream) lease.stream).read(buffer);
			ended = count < 0;
			return count <= 0 ? new byte[0] : Arrays.copyOf(buffer, count);
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
