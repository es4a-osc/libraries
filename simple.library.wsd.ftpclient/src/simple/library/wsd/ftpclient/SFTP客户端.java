package simple.library.wsd.ftpclient;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleEvent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.SimpleProperty;
import simple.runtime.annotations.UsesPermissions;
import simple.runtime.components.组件;

/**
 * 独立 SFTP 文件客户端；Android 8.0+，同步网络方法须在后台线程调用，公钥验证失败拒绝认证。
 *
 * 本地路径可为绝对路径或相对应用私有 files 目录的路径。已有续传前缀须与源文件一致。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
@SimpleComponent
@UsesPermissions(permissionNames = "android.permission.INTERNET")
public interface SFTP客户端 extends 组件 {

	/**
	 * 底层连接是否存在；不代替登录状态及网络健康检查。
	 *
	 * @return 底层连接是否存在；不代替登录状态及网络健康检查。
	 */
	@SimpleProperty
	boolean 已连接();

	/**
	 * 连接是否正在执行命令、任务或持有数据流。
	 *
	 * @return 连接是否正在执行命令、任务或持有数据流。
	 */
	@SimpleProperty
	boolean 忙();

	/**
	 * 最近一次实际操作的错误说明；任务失败也通过完成事件返回。
	 *
	 * @return 最近一次实际操作的错误说明；任务失败也通过完成事件返回。
	 */
	@SimpleProperty
	String 错误信息();

	/**
	 * 正在运行的任务编号，无后台任务为零。
	 *
	 * @return 正在运行的任务编号，无后台任务为零。
	 */
	@SimpleProperty
	long 当前任务编号();

	/**
	 * 每次复制缓冲区的字节数，4096 至 1048576；空闲时设置。
	 *
	 * @return 每次复制缓冲区的字节数，4096 至 1048576；空闲时设置。
	 */
	@SimpleProperty
	int 传输缓冲区();

	/**
	 * 每次复制缓冲区的字节数，4096 至 1048576；空闲时设置。
	 *
	 * @param value 新值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_INTEGER,
		initializer = "65536"
	)
	void 传输缓冲区(int value);

	/**
	 * 同步读取远程工作目录；须在后台线程调用。
	 *
	 * @return 实际结果；失败抛出 FTP错误。
	 */
	@SimpleFunction
	String 取工作目录();

	/**
	 * 同步切换远程工作目录；须在后台线程调用。
	 *
	 * @param directory 远程目录。
	 */
	@SimpleFunction
	void 置工作目录(String directory);

	/**
	 * 同步读取结构化文件列表；空目录返回空数组，失败抛出 FTP错误；须在后台线程调用。
	 *
	 * @param directory 远程目录，空文本取当前目录。
	 * @return 实际结果；失败抛出 FTP错误。
	 */
	@SimpleFunction
	FTP远程文件[] 取文件列表(String directory);

	/**
	 * 同步读取一个文件或目录的元数据；须在后台线程调用。
	 *
	 * @param file 远程文件路径。
	 * @return 实际结果；失败抛出 FTP错误。
	 */
	@SimpleFunction
	FTP远程文件 取文件信息(String file);

	/**
	 * 同步执行远程变更，失败抛出 FTP错误；须在后台线程调用。
	 *
	 * @param path 远程路径；删除目录须为空目录。
	 */
	@SimpleFunction
	void 创建目录(String path);

	/**
	 * 同步执行远程变更，失败抛出 FTP错误；须在后台线程调用。
	 *
	 * @param path 远程路径；删除目录须为空目录。
	 */
	@SimpleFunction
	void 删除目录(String path);

	/**
	 * 同步执行远程变更，失败抛出 FTP错误；须在后台线程调用。
	 *
	 * @param path 远程路径；删除目录须为空目录。
	 */
	@SimpleFunction
	void 删除文件(String path);

	/**
	 * 同步重命名或移动远程条目；须在后台线程调用。
	 *
	 * @param source 原远程路径。
	 * @param target 目标远程路径。
	 */
	@SimpleFunction
	void 重命名(String source, String target);

	/**
	 * 同步下载小文件为字节数组，超出上限会失败并断开连接；须在后台线程调用。
	 *
	 * @param file 远程文件路径。
	 * @param limit 最大接收字节数，1 至 8388608。
	 * @return 实际结果；失败抛出 FTP错误。
	 */
	@SimpleFunction
	byte[] 下载字节(String file, int limit);

	/**
	 * 同步覆盖上传字节数组；失败可能留下远程部分文件，须在后台线程调用。
	 *
	 * @param file 远程文件路径。
	 * @param bytes 待上传内容，最多 8388608 字节。
	 */
	@SimpleFunction
	void 上传字节(String file, byte[] bytes);

	/**
	 * 受理后台上传并返回任务编号，通过传输完成事件报告结果；续传偏移须等于远程已有尺寸。
	 *
	 * @param local 本地普通文件路径。
	 * @param remotePath 远程完整目标路径。
	 * @param offset 起始字节偏移，零从头开始。
	 * @return 实际结果；失败抛出 FTP错误。
	 */
	@SimpleFunction
	long 上传文件(String local, String remotePath, long offset);

	/**
	 * 受理后台下载，通过传输完成事件报告结果。服务器确认后才替换目标；取消及失败保留本实例断点，续传前重新连接。
	 *
	 * @param remotePath 远程普通文件路径。
	 * @param local 本地完整目标路径。
	 * @param offset 起始字节偏移，零从头开始。
	 * @return 实际结果；失败抛出 FTP错误。
	 */
	@SimpleFunction
	long 下载文件(String remotePath, String local, long offset);

	/**
	 * 后台递归上传普通文件及目录，拒绝链接；失败可能留下部分条目。进度总量为 -1。
	 *
	 * @param local 本地源目录。
	 * @param remotePath 将创建的远程目标目录，须不存在。
	 * @return 实际结果；失败抛出 FTP错误。
	 */
	@SimpleFunction
	long 上传目录(String local, String remotePath);

	/**
	 * 后台递归下载普通文件及目录，拒绝链接；失败可能留下部分条目。进度总量为 -1。
	 *
	 * @param remotePath 远程源目录。
	 * @param local 将创建的本地目标目录，须不存在。
	 * @return 实际结果；失败抛出 FTP错误。
	 */
	@SimpleFunction
	long 下载目录(String remotePath, String local);

	/**
	 * 取得本实例下载断点长度；没有断点时返回现有目标长度，没有文件返回零。
	 *
	 * @param local 与下载任务相同的本地目标路径。
	 * @return 实际结果；失败抛出 FTP错误。
	 */
	@SimpleFunction
	long 取下载偏移(String local);

	/**
	 * 空闲时删除本实例拥有的断点文件；不删除目标文件。
	 *
	 * @param local 本地目标路径。
	 */
	@SimpleFunction
	void 清理下载断点(String local);

	/**
	 * 非阻塞请求取消当前后台传输，连接随之断开；是否受理通过返回值表示，终态仍由传输完成事件报告。
	 *
	 * @return 实际结果；失败抛出 FTP错误。
	 */
	@SimpleFunction
	boolean 取消传输();

	/**
	 * 空闲时同步断开连接；忙时先取消任务或关闭数据流。须在后台线程调用。
	 */
	@SimpleFunction
	void 断开连接();

	/**
	 * 后台连接任务在界面线程报告一次结果；FTP 连接成功后仍需登录。
	 *
	 * @param id 任务编号。
	 * @param success 是否实际连接成功。
	 * @param message 失败原因，成功为空文本。
	 */
	@SimpleEvent
	void 连接完成(long id, boolean success, String message);

	/**
	 * 界面线程收到实际数据进度；最终成功须以传输完成为准。
	 *
	 * @param id 任务编号。
	 * @param direction 0 下载、1 上传。
	 * @param source 任务来源路径。
	 * @param target 任务目标路径。
	 * @param completed 已完成字节数，续传包含已有前缀；目录任务为累计字节数。
	 * @param total 总字节数，未知或目录任务为 -1。
	 */
	@SimpleEvent
	void 传输进度(long id, int direction, String source, String target, long completed, long total);

	/**
	 * 每个受理的文件任务在界面线程报告一次终态；销毁后不再发送事件。
	 *
	 * @param id 任务编号。
	 * @param direction 0 下载、1 上传。
	 * @param source 任务来源路径。
	 * @param target 任务目标路径。
	 * @param success 服务器及本地操作是否均已完成。
	 * @param cancelled 是否因取消结束。
	 * @param message 失败或取消原因。
	 */
	@SimpleEvent
	void 传输完成(
			long id,
			int direction,
			String source,
			String target,
			boolean success,
			boolean cancelled,
			String message);

	/**
	 * 连接和网络读取超时毫秒，1 至 300000，在连接前设置。
	 *
	 * @return 连接和网络读取超时毫秒，1 至 300000，在连接前设置。
	 */
	@SimpleProperty
	int 连接超时();

	/**
	 * 连接和网络读取超时毫秒，1 至 300000，在连接前设置。
	 *
	 * @param value 新值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_INTEGER,
		initializer = "10000"
	)
	void 连接超时(int value);

	/**
	 * SHA256:Base64 固定指纹；空文本时首次及公钥变化必须经用户确认并保存信任。连接前设置。
	 *
	 * @return SHA256:Base64 固定指纹；空文本时首次及公钥变化必须经用户确认并保存信任。连接前设置。
	 */
	@SimpleProperty
	String 服务器公钥指纹();

	/**
	 * SHA256:Base64 固定指纹；空文本时首次及公钥变化必须经用户确认并保存信任。连接前设置。
	 *
	 * @param value 新值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_STRING,
		initializer = "\"\""
	)
	void 服务器公钥指纹(String value);

	/**
	 * 同步连接并认证 SFTP，须在后台线程调用；Android 8.0+，服务器公钥须通过验证。
	 *
	 * @param host 主机名或 IP。
	 * @param port 端口，通常为 22。
	 * @param user 用户名。
	 * @param password 密码。
	 */
	@SimpleFunction
	void 连接(String host, int port, String user, String password);

	/**
	 * 后台连接并认证 SFTP，返回受理编号；真实结果通过连接完成事件通知。Android 8.0+。
	 *
	 * @param host 主机名或 IP。
	 * @param port 端口，通常为 22。
	 * @param user 用户名。
	 * @param password 密码。
	 * @return 实际结果；失败抛出 FTP错误。
	 */
	@SimpleFunction
	long 开始连接(String host, int port, String user, String password);
}
