package simple.library.wsd.ftpclient;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleDataElement;
import simple.runtime.annotations.SimpleProperty;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleEvent;
import simple.runtime.annotations.UsesPermissions;
import simple.runtime.components.组件;

/**
 * FTP/SFTP 客户端；同步方法应在后台线程调用，同一连接的操作串行执行。
 * SFTP 需要 Android 8.0+，支持密码认证；默认首次确认服务器后保存信任，亦可手动指定公钥指纹。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
@SimpleComponent
@UsesPermissions(
	permissionNames =
		// 允许连接FTP服务器并传输数据。
		"android.permission.INTERNET," +
		// 允许向外部存储写入下载文件。
		"android.permission.WRITE_EXTERNAL_STORAGE"
)
public interface FTP客户端 extends 组件 {

	/**
	 * 普通 FTP，值为 0。
	 */
	@SimpleDataElement
	int 协议_FTP = 0;

	/**
	 * SFTP，值为 1；需要 Android 8.0 或以上版本。
	 */
	@SimpleDataElement
	int 协议_SFTP = 1;

	/**
	 * @return 下次登录使用的协议，默认协议_FTP。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_INTEGER,
		initializer = "0"
	)
	int 协议();

	/**
	 * @param value 下次登录使用的协议；不改变现有连接。
	 */
	@SimpleProperty
	void 协议(int value);

	/**
	 * @return 下次 SFTP 登录使用的 SHA256 公钥指纹；默认空文本，由应用确认并记住服务器。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_STRING,
		initializer = "\"\""
	)
	String 服务器公钥指纹();

	/**
	 * @param value 高级选项：指定可信 SHA256:Base64 指纹时严格匹配；留空时首次或公钥变化弹框确认并保存，下次登录生效。
	 */
	@SimpleProperty
	void 服务器公钥指纹(String value);

	/**
	 * @return 最近操作的错误说明；SFTP 空列表无错误时为空文本，FTP 沿用有限错误信息。
	 */
	@SimpleProperty
	String 错误信息();

	/**
	 * 按当前协议使用默认端口登录：FTP 为 21，SFTP 为 22；保留原有认证及主机公钥校验。
	 *
	 * <p>阻塞调用线程，应在后台调用。</p>
	 *
	 * @param host 主机名或 IP，不含协议前缀。
	 * @param username 登录用户名。
	 * @param password 登录密码。
	 * @return 是否登录成功。
	 */
	@SimpleFunction
	boolean 登录服务器(String host, String username, String password);

	/**
	 * 按协议属性连接并登录服务器，先关闭旧连接；FTP 使用被动模式、二进制和 GBK，SFTP 使用密码认证及主机公钥校验。连接超时 5 秒；首次或公钥变化等待确认最多 60 秒，取消则拒绝连接。同步方法会阻塞调用线程，请在后台线程调用。
	 *
	 * @param host 主机名或 IP，不含协议前缀。
	 * @param port 服务器端口，FTP 通常为 21，SFTP 通常为 22，范围 1 至 65535。
	 * @param username 登录用户名。
	 * @param password 登录密码。
	 * @return 是否登录成功。
	 */
	@SimpleFunction
	boolean 登录服务器(String host, int port, String username, String password);

	/**
	 * 返回服务器当前工作目录。 同步方法会阻塞调用线程，请在后台线程调用。
	 *
	 * @return 当前目录；失败或未连接返回空文本。
	 */
	@SimpleFunction
	String 取工作目录();

	/**
	 * 列出服务器当前工作目录，沿用完整方法的列表格式及错误信息。
	 *
	 * <p>阻塞调用线程，应在后台调用。</p>
	 *
	 * @return 列表文本；空目录或失败返回空文本。
	 */
	@SimpleFunction
	String 取文件列表();

	/**
	 * 列出指定目录；每行格式为 名称/字节数/是否目录，忽略点目录。 同步方法会阻塞调用线程，请在后台线程调用。
	 *
	 * @param remoteDirectory 服务器目录；空文本表示当前目录，路径分隔符使用 /。
	 * @return 列表文本；空目录与失败均返回空文本。
	 */
	@SimpleFunction
	String 取文件列表(String remoteDirectory);

	/**
	 * 在指定远程目录下创建目录。 同步方法会阻塞调用线程，请在后台线程调用。
	 *
	 * @param remoteDirectory 服务器目录；空文本表示当前目录，路径分隔符使用 /。
	 * @param name 单个文件或目录名称，不含路径分隔符。
	 * @return 是否成功。
	 */
	@SimpleFunction
	boolean 创建目录(String remoteDirectory, String name);

	/**
	 * 在指定远程目录下重命名文件或目录。 同步方法会阻塞调用线程，请在后台线程调用。
	 *
	 * @param remoteDirectory 服务器目录；空文本表示当前目录，路径分隔符使用 /。
	 * @param oldName 原文件或目录名称，不含路径分隔符。
	 * @param newName 新文件或目录名称，不含路径分隔符。
	 * @return 是否成功。
	 */
	@SimpleFunction
	boolean 重命名(String remoteDirectory, String oldName, String newName);

	/**
	 * 删除指定远程目录下的空目录，不递归删除。 同步方法会阻塞调用线程，请在后台线程调用。
	 *
	 * @param remoteDirectory 服务器目录；空文本表示当前目录，路径分隔符使用 /。
	 * @param name 单个文件或目录名称，不含路径分隔符。
	 * @return 是否成功。
	 */
	@SimpleFunction
	boolean 删除目录(String remoteDirectory, String name);

	/**
	 * 删除指定远程目录下的文件。 同步方法会阻塞调用线程，请在后台线程调用。
	 *
	 * @param remoteDirectory 服务器目录；空文本表示当前目录，路径分隔符使用 /。
	 * @param name 单个文件或目录名称，不含路径分隔符。
	 * @return 是否成功。
	 */
	@SimpleFunction
	boolean 删除文件(String remoteDirectory, String name);

	/**
	 * 上传文件或递归上传目录，跳过隐藏子项；断线时尝试用上次登录信息重连。 同步方法会阻塞调用线程，请在后台线程调用。
	 *
	 * @param localDirectory 绝对目录或相对于应用内部文件目录的路径。
	 * @param localName 本地文件或目录名称，不含路径分隔符。
	 * @param remoteDirectory 服务器目录；空文本表示当前目录，路径分隔符使用 /。
	 * @return 是否全部成功；失败可能已上传部分内容。
	 */
	@SimpleFunction
	boolean 上传文件(String localDirectory, String localName, String remoteDirectory);

	/**
	 * 下载文件或递归下载目录；断线时尝试用上次登录信息重连，不跟随远程符号链接。 同步方法会阻塞调用线程，请在后台线程调用。
	 *
	 * @param remoteDirectory 服务器目录；空文本表示当前目录，路径分隔符使用 /。
	 * @param remoteName 远程文件或目录名称，不含路径分隔符。
	 * @param localDirectory 绝对目录或相对于应用内部文件目录的路径。
	 * @return 是否全部成功；失败可能留下部分文件，已有文件会被覆盖。
	 */
	@SimpleFunction
	boolean 下载文件(String remoteDirectory, String remoteName, String localDirectory);

	/**
	 * 断开连接并清除重连凭据；等待当前会话操作结束，不是取消传输。 同步方法会阻塞调用线程，请在后台线程调用。
	 */
	@SimpleFunction
	void 断开连接();

	/**
	 * 检查本地连接状态，不发送保活命令。
	 *
	 * @return 套接字是否连接；不能据此保证服务端在线。
	 */
	@SimpleFunction
	boolean 取连接状态();

	/**
	 * 启动后台上传；同一组件仅接受一个异步传输，结果由上传完毕事件在主线程通知。
	 *
	 * @param localDirectory 绝对目录或相对于应用内部文件目录的路径。
	 * @param localName 本地文件或目录名称，不含路径分隔符。
	 * @param remoteDirectory 服务器目录；空文本表示当前目录，路径分隔符使用 /。
	 * @return 是否接受任务；真不代表传输成功，假时不触发完成事件。
	 */
	@SimpleFunction
	boolean 开始上传(String localDirectory, String localName, String remoteDirectory);

	/**
	 * 启动后台下载；同一组件仅接受一个异步传输，结果由下载完毕事件在主线程通知。
	 *
	 * @param remoteDirectory 服务器目录；空文本表示当前目录，路径分隔符使用 /。
	 * @param remoteName 远程文件或目录名称，不含路径分隔符。
	 * @param localDirectory 绝对目录或相对于应用内部文件目录的路径。
	 * @return 是否接受任务；真不代表传输成功，假时不触发完成事件。
	 */
	@SimpleFunction
	boolean 开始下载(String remoteDirectory, String remoteName, String localDirectory);

	/**
	 * 后台上传结束时在主线程触发；销毁后不再通知。
	 *
	 * @param succeeded 传输是否全部成功。
	 */
	@SimpleEvent
	void 上传完毕(boolean succeeded);

	/**
	 * 后台下载结束时在主线程触发；销毁后不再通知。
	 *
	 * @param succeeded 传输是否全部成功。
	 */
	@SimpleEvent
	void 下载完毕(boolean succeeded);
}
