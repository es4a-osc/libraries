package simple.library.wsd.ftpclient;

import org.apache.commons.net.ftp.FTPListParseEngine;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.SimpleProperty;

/**
 * FTP 已取得列表的本地分页游标；翻页不会再次访问服务器。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class FTP分页列表 {
	private final FTPListParseEngine engine;

	/**
	 * 包装服务器列表解析结果。
	 *
	 * @param engine 独立列表解析器。
	 */
	FTP分页列表(FTPListParseEngine engine) {
		this.engine = engine;
	}

	/**
	 * 取得完整列表快照，不移动游标。
	 *
	 * @return 全部条目。
	 */
	@SimpleFunction
	public synchronized FTP远程文件[] 取全部() {
		try {
			return FtpSession.files(engine.getFiles());
		} catch (java.io.IOException error) {
			throw new FTP错误(error.getMessage());
		}
	}

	/**
	 * 向后读取指定数量的条目。
	 *
	 * @param count 条目数量，1 至 10000。
	 * @return 后续条目，没有时为空数组。
	 */
	@SimpleFunction
	public synchronized FTP远程文件[] 取下一页(int count) {
		check(count);
		return FtpSession.files(engine.getNext(count));
	}

	/**
	 * 向前读取指定数量的条目。
	 *
	 * @param count 条目数量，1 至 10000。
	 * @return 之前的条目，没有时为空数组。
	 */
	@SimpleFunction
	public synchronized FTP远程文件[] 取上一页(int count) {
		check(count);
		return FtpSession.files(engine.getPrevious(count));
	}

	/**
	 * 判断游标之后是否还有条目。
	 *
	 * @return 是否有下一页。
	 */
	@SimpleProperty
	public synchronized boolean 有下一页() {
		return engine.hasNext();
	}

	/**
	 * 判断游标之前是否有条目。
	 *
	 * @return 是否有上一页。
	 */
	@SimpleProperty
	public synchronized boolean 有上一页() {
		return engine.hasPrevious();
	}

	/**
	 * 将游标移至列表开始。
	 */
	@SimpleFunction
	public synchronized void 移到首页() {
		engine.resetIterator();
	}

	/**
	 * 校验每次读取数量。
	 *
	 * @param count 预期条目数量。
	 */
	private static void check(int count) {
		if (count < 1 || count > 10000) {
			throw new FTP错误("分页数量须为 1 至 10000。");
		}
	}
}
