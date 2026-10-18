package simple.library.wsd.ftpclient;

import java.io.File;

/**
 * FTP 与 SFTP 共用的会话操作；组件层统一负责串行调用及生命周期。
 */
interface TransferSession {

	/**
	 * 保存连接参数并尝试登录服务器。
	 *
	 * @param host 服务器地址
	 * @param port 端口号
	 * @param username 登录用户名
	 * @param password 认证密码
	 * @return 操作成功时为 {@code true}，失败时为 {@code false}
	 */
	boolean login(String host, int port, String username, String password);

	/**
	 * 判断底层会话是否已连接。
	 *
	 * @return 底层会话已连接时为 {@code true}，否则为 {@code false}
	 */
	boolean connected();

	/**
	 * 读取服务器当前工作目录。
	 *
	 * @return 当前远程工作目录；失败时为空文本
	 */
	String workingDirectory();

	/**
	 * 列出指定远程目录的内容。
	 *
	 * @param directory 远程目录
	 * @return 远程目录内容；格式与组件的目录列表约定一致
	 */
	String list(String directory);

	/**
	 * 按指定模式修改远程文件或目录。
	 *
	 * @param directory 远程目录
	 * @param name 要操作的名称
	 * @param newName 修改后的名称
	 * @param mode 操作模式
	 * @return 操作成功时为 {@code true}，失败时为 {@code false}
	 */
	boolean mutate(String directory, String name, String newName, int mode);

	/**
	 * 将指定本地条目上传至远程目录。
	 *
	 * @param localDirectory 本地传输目录
	 * @param name 要操作的名称
	 * @param remoteDirectory 远程传输目录
	 * @return 操作成功时为 {@code true}，失败时为 {@code false}
	 */
	boolean upload(File localDirectory, String name, String remoteDirectory);

	/**
	 * 下载指定资源到本地。
	 *
	 * @param remoteDirectory 远程传输目录
	 * @param name 要操作的名称
	 * @param localDirectory 本地传输目录
	 * @return 操作成功时为 {@code true}，失败时为 {@code false}
	 */
	boolean download(String remoteDirectory, String name, File localDirectory);

	/**
	 * 关闭当前连接并清理连接状态。
	 */
	void disconnect();

	/**
	 * 读取最近一次操作的错误信息。
	 *
	 * @return 最近一次操作的错误信息
	 */
	String error();
}
