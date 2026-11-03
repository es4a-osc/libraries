package simple.library.wsd.ftpclient;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * FTP 与 SFTP 的共同文件能力，协议专属控制操作不放在此接口。
 *
 * @author 树先生 xhwsd@qq.com
 */
interface RemoteSession {

	/**
	 * 查询底层连接状态。
	 *
	 * @return 是否已连接。
	 */
	boolean connected();

	/**
	 * 断开网络连接，允许取消线程调用。
	 * @throws Exception 网络连接、文件操作或协议确认失败。
	 */
	void disconnect() throws Exception;

	/**
	 * 查询工作目录。
	 *
	 * @return 远程工作目录。
	 * @throws Exception 网络连接、文件操作或协议确认失败。
	 */
	String pwd() throws Exception;

	/**
	 * 切换工作目录。
	 *
	 * @param directory 目标目录。
	 * @throws Exception 网络连接、文件操作或协议确认失败。
	 */
	void cwd(String directory) throws Exception;

	/**
	 * 取得目录列表快照。
	 *
	 * @param directory 远程目录。
	 * @return 条目数组。
	 * @throws Exception 网络连接、文件操作或协议确认失败。
	 */
	FTP远程文件[] list(String directory) throws Exception;

	/**
	 * 查询文件信息。
	 *
	 * @param file 远程路径。
	 * @return 条目快照。
	 * @throws Exception 网络连接、文件操作或协议确认失败。
	 */
	FTP远程文件 stat(String file) throws Exception;

	/**
	 * 执行单项远程变更。
	 *
	 * @param operation mkdir、rmdir、delete 或 rename。
	 * @param source 原路径。
	 * @param target 重命名目标。
	 * @throws Exception 网络连接、文件操作或协议确认失败。
	 */
	void mutate(String operation, String source, String target) throws Exception;

	/**
	 * 打开数据输入流。
	 *
	 * @param file 远程路径。
	 * @param offset 起始字节偏移。
	 * @return 网络数据流。
	 * @throws Exception 网络连接、文件操作或协议确认失败。
	 */
	InputStream read(String file, long offset) throws Exception;

	/**
	 * 打开数据输出流。
	 *
	 * @param file 远程路径。
	 * @param offset 起始字节偏移。
	 * @return 网络数据流。
	 * @throws Exception 网络连接、文件操作或协议确认失败。
	 */
	OutputStream write(String file, long offset) throws Exception;

	/**
	 * 数据流关闭后核对最终协议结果。
	 * @throws Exception 网络连接、文件操作或协议确认失败。
	 */
	void finish() throws Exception;
}
