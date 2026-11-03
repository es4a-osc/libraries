package simple.library.wsd.ftpclient;

import simple.runtime.annotations.SimpleDataElement;
import simple.runtime.annotations.SimpleObject;

/**
 * FTP 传输参数及远程条目类型常量。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class FTP常量 {
	/**
	 * 常量对象不可创建。
	 */
	private FTP常量() {}

	/**
	 * 传输模式_字节流。
	 */
	@SimpleDataElement public static final int 传输模式_字节流 = 10;

	/**
	 * 传输模式_块。
	 */
	@SimpleDataElement public static final int 传输模式_块 = 11;

	/**
	 * 传输模式_压缩。
	 */
	@SimpleDataElement public static final int 传输模式_压缩 = 12;

	/**
	 * 文件结构_文件。
	 */
	@SimpleDataElement public static final int 文件结构_文件 = 7;

	/**
	 * 文件结构_记录。
	 */
	@SimpleDataElement public static final int 文件结构_记录 = 8;

	/**
	 * 文件结构_页。
	 */
	@SimpleDataElement public static final int 文件结构_页 = 9;

	/**
	 * 文件类型_ASCII。
	 */
	@SimpleDataElement public static final int 文件类型_ASCII = 0;

	/**
	 * 文件类型_EBCDIC。
	 */
	@SimpleDataElement public static final int 文件类型_EBCDIC = 1;

	/**
	 * 文件类型_二进制。
	 */
	@SimpleDataElement public static final int 文件类型_二进制 = 2;

	/**
	 * 文件类型_本地。
	 */
	@SimpleDataElement public static final int 文件类型_本地 = 3;

	/**
	 * 条目_文件。
	 */
	@SimpleDataElement public static final int 条目_文件 = 0;

	/**
	 * 条目_目录。
	 */
	@SimpleDataElement public static final int 条目_目录 = 1;

	/**
	 * 条目_链接。
	 */
	@SimpleDataElement public static final int 条目_链接 = 2;

	/**
	 * 条目_未知。
	 */
	@SimpleDataElement public static final int 条目_未知 = 3;
}
