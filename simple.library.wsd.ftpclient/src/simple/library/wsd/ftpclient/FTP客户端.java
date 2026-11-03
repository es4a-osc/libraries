package simple.library.wsd.ftpclient;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleEvent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.SimpleProperty;
import simple.runtime.annotations.UsesPermissions;
import simple.runtime.components.组件;

/**
 * 独立 FTP 客户端；同步网络方法须在后台线程调用，后台任务结果回到界面线程。
 *
 * 本地路径可为绝对路径或相对应用私有 files 目录的路径。FTP 偏移续传要求二进制模式，已有前缀须与源文件一致。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
@SimpleComponent
@UsesPermissions(permissionNames = "android.permission.INTERNET")
public interface FTP客户端 extends 组件 {

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
	 * FTP 是否已经通过登录，连接状态不等于登录状态。
	 *
	 * @return FTP 是否已经通过登录，连接状态不等于登录状态。
	 */
	@SimpleProperty
	boolean 已登录();

	/**
	 * 是否通知控制命令与答复；PASS 和 ACCT 参数会隐藏。
	 *
	 * @return 是否通知控制命令与答复；PASS 和 ACCT 参数会隐藏。
	 */
	@SimpleProperty
	boolean 启用命令事件();

	/**
	 * 是否通知控制命令与答复；PASS 和 ACCT 参数会隐藏。
	 *
	 * @param value 新值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_BOOLEAN,
		initializer = "False"
	)
	void 启用命令事件(boolean value);

	/**
	 * 连接超时毫秒数，1 至 300000；在连接前设置。
	 *
	 * @return 连接超时毫秒数，1 至 300000；在连接前设置。
	 */
	@SimpleProperty
	int 连接超时();

	/**
	 * 连接超时毫秒数，1 至 300000；在连接前设置。
	 *
	 * @param value 新值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_INTEGER,
		initializer = "10000"
	)
	void 连接超时(int value);

	/**
	 * 控制连接读取超时毫秒数，1 至 300000；已连接时须后台线程设置。
	 *
	 * @return 控制连接读取超时毫秒数，1 至 300000；已连接时须后台线程设置。
	 */
	@SimpleProperty
	int 控制超时();

	/**
	 * 控制连接读取超时毫秒数，1 至 300000；已连接时须后台线程设置。
	 *
	 * @param value 新值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_INTEGER,
		initializer = "10000"
	)
	void 控制超时(int value);

	/**
	 * 数据连接读取超时毫秒数，1 至 300000；空闲时设置。
	 *
	 * @return 数据连接读取超时毫秒数，1 至 300000；空闲时设置。
	 */
	@SimpleProperty
	int 数据超时();

	/**
	 * 数据连接读取超时毫秒数，1 至 300000；空闲时设置。
	 *
	 * @param value 新值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_INTEGER,
		initializer = "10000"
	)
	void 数据超时(int value);

	/**
	 * 控制连接编码，在连接前设置，默认 UTF-8。
	 *
	 * @return 控制连接编码，在连接前设置，默认 UTF-8。
	 */
	@SimpleProperty
	String 编码();

	/**
	 * 控制连接编码，在连接前设置，默认 UTF-8。
	 *
	 * @param value 新值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_STRING,
		initializer = "\"UTF-8\""
	)
	void 编码(String value);

	/**
	 * 是否在连接时自动检测 UTF-8 支持，在连接前设置。
	 *
	 * @return 是否在连接时自动检测 UTF-8 支持，在连接前设置。
	 */
	@SimpleProperty
	boolean 自动检测编码();

	/**
	 * 是否在连接时自动检测 UTF-8 支持，在连接前设置。
	 *
	 * @param value 新值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_BOOLEAN,
		initializer = "True"
	)
	void 自动检测编码(boolean value);

	/**
	 * 是否请求在 LIST 列表中包含隐藏文件；以服务器支持为准。
	 *
	 * @return 是否请求在 LIST 列表中包含隐藏文件；以服务器支持为准。
	 */
	@SimpleProperty
	boolean 包含隐藏文件();

	/**
	 * 是否请求在 LIST 列表中包含隐藏文件；以服务器支持为准。
	 *
	 * @param value 新值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_BOOLEAN,
		initializer = "False"
	)
	void 包含隐藏文件(boolean value);

	/**
	 * 数据套接字接收缓冲区字节数，零表示系统默认。
	 *
	 * @return 数据套接字接收缓冲区字节数，零表示系统默认。
	 */
	@SimpleProperty
	int 数据接收缓冲区尺寸();

	/**
	 * 数据套接字接收缓冲区字节数，零表示系统默认。
	 *
	 * @param value 新值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_INTEGER,
		initializer = "0"
	)
	void 数据接收缓冲区尺寸(int value);

	/**
	 * 数据套接字发送缓冲区字节数，零表示系统默认。
	 *
	 * @return 数据套接字发送缓冲区字节数，零表示系统默认。
	 */
	@SimpleProperty
	int 数据发送缓冲区尺寸();

	/**
	 * 数据套接字发送缓冲区字节数，零表示系统默认。
	 *
	 * @param value 新值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_INTEGER,
		initializer = "0"
	)
	void 数据发送缓冲区尺寸(int value);

	/**
	 * 取值见 FTP常量；登录后修改须在后台线程调用，服务器拒绝时抛出 FTP错误。
	 *
	 * @return 取值见 FTP常量；登录后修改须在后台线程调用，服务器拒绝时抛出 FTP错误。
	 */
	@SimpleProperty
	int 文件类型();

	/**
	 * 取值见 FTP常量；登录后修改须在后台线程调用，服务器拒绝时抛出 FTP错误。
	 *
	 * @param value 新值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_INTEGER,
		initializer = "2"
	)
	void 文件类型(int value);

	/**
	 * 取值见 FTP常量；登录后修改须在后台线程调用，服务器拒绝时抛出 FTP错误。
	 *
	 * @return 取值见 FTP常量；登录后修改须在后台线程调用，服务器拒绝时抛出 FTP错误。
	 */
	@SimpleProperty
	int 文件结构();

	/**
	 * 取值见 FTP常量；登录后修改须在后台线程调用，服务器拒绝时抛出 FTP错误。
	 *
	 * @param value 新值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_INTEGER,
		initializer = "7"
	)
	void 文件结构(int value);

	/**
	 * 取值见 FTP常量；登录后修改须在后台线程调用，服务器拒绝时抛出 FTP错误。
	 *
	 * @return 取值见 FTP常量；登录后修改须在后台线程调用，服务器拒绝时抛出 FTP错误。
	 */
	@SimpleProperty
	int 传输模式();

	/**
	 * 取值见 FTP常量；登录后修改须在后台线程调用，服务器拒绝时抛出 FTP错误。
	 *
	 * @param value 新值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_INTEGER,
		initializer = "10"
	)
	void 传输模式(int value);

	/**
	 * 同步建立 FTP 控制连接，不登录；须在后台线程调用。
	 *
	 * @param host 主机名或 IP，不含协议前缀。
	 * @param port 端口，1 至 65535。
	 */
	@SimpleFunction
	void 连接(String host, int port);

	/**
	 * 后台建立控制连接，返回受理编号；结果见连接完成事件，成功后仍需登录。
	 *
	 * @param host 主机名或 IP。
	 * @param port 端口，1 至 65535。
	 * @return 实际结果；失败抛出 FTP错误。
	 */
	@SimpleFunction
	long 开始连接(String host, int port);

	/**
	 * 同步认证并设置传输参数；认证失败抛出 FTP错误，须在后台线程调用。
	 *
	 * @param user 用户名，匿名可填 anonymous。
	 * @param password 密码。
	 */
	@SimpleFunction
	void 登录(String user, String password);

	/**
	 * 同步退出认证，保留控制连接；须在后台线程调用。
	 */
	@SimpleFunction
	void 退出登录();

	/**
	 * 同步发送 NOOP 并核对服务器结果，须在后台线程调用。
	 */
	@SimpleFunction
	void 保持连接();

	/**
	 * 空闲时选择本地主动模式，实际数据连接由服务器建立。
	 */
	@SimpleFunction
	void 主动模式();

	/**
	 * 空闲时选择本地被动模式，实际数据连接由客户端建立。
	 */
	@SimpleFunction
	void 被动模式();

	/**
	 * 空闲时设置主动数据连接监听端口范围。
	 *
	 * @param minimum 最小端口。
	 * @param maximum 最大端口，不小于最小端口。
	 */
	@SimpleFunction
	void 置主动端口范围(int minimum, int maximum);

	/**
	 * 指定主动模式对外通告的地址；须在后台线程调用。
	 *
	 * @param host 服务器能够回连的数据地址。
	 */
	@SimpleFunction
	void 置主动外部地址(String host);

	/**
	 * 取得最近一次被动数据连接地址，尚未建立时为空文本。
	 *
	 * @return 实际结果；失败抛出 FTP错误。
	 */
	@SimpleFunction
	String 取被动主机地址();

	/**
	 * 取得最近一次被动数据连接端口，尚未建立时为 -1。
	 *
	 * @return 实际结果；失败抛出 FTP错误。
	 */
	@SimpleFunction
	int 取被动主机端口();

	/**
	 * 取得最近的 FTP 控制答复码。
	 *
	 * @return 实际结果；失败抛出 FTP错误。
	 */
	@SimpleFunction
	int 取答复码();

	/**
	 * 取得最近的 FTP 控制答复文本。
	 *
	 * @return 实际结果；失败抛出 FTP错误。
	 */
	@SimpleFunction
	String 取答复文本();

	/**
	 * 取得最近的多行 FTP 答复快照。
	 *
	 * @return 实际结果；失败抛出 FTP错误。
	 */
	@SimpleFunction
	String[] 取答复文本数组();

	/**
	 * 判断答复码是否为 2xx 肯定完成。
	 *
	 * @param code 服务器答复码。
	 * @return 实际结果；失败抛出 FTP错误。
	 */
	@SimpleFunction
	boolean 检查答复码(int code);

	/**
	 * 同步通过 FEAT 查询功能支持，须在后台线程调用。
	 *
	 * @param feature 服务器功能名称。
	 * @return 实际结果；失败抛出 FTP错误。
	 */
	@SimpleFunction
	boolean 检查支持功能(String feature);

	/**
	 * 同步查询服务器系统类型，须在后台线程调用。
	 *
	 * @return 实际结果；失败抛出 FTP错误。
	 */
	@SimpleFunction
	String 取系统类型();

	/**
	 * 同步发送 STAT 查询状态，须在后台线程调用。
	 *
	 * @param path 可为空文本，非空时查询该路径。
	 * @return 实际结果；失败抛出 FTP错误。
	 */
	@SimpleFunction
	String 取状态(String path);

	/**
	 * 同步读取 HELP 文本，须在后台线程调用。
	 *
	 * @return 实际结果；失败抛出 FTP错误。
	 */
	@SimpleFunction
	String 取帮助();

	/**
	 * 同步发送服务器专用 SITE 命令；须在后台线程调用。
	 *
	 * @param command SITE 参数，不含换行。
	 */
	@SimpleFunction
	void 发送特定命令(String command);

	/**
	 * 同步发送控制命令并返回答复；1xx 数据命令须使用本库流或传输接口。须在后台线程调用。
	 *
	 * @param command FTP 命令名称。
	 * @param arguments 命令参数，可为空文本。
	 * @return 实际结果；失败抛出 FTP错误。
	 */
	@SimpleFunction
	String[] 发送命令(String command, String arguments);

	/**
	 * 同步请求 MFMT 修改文件时间；须在后台线程调用。
	 *
	 * @param file 远程文件路径。
	 * @param time FTP 时间格式 yyyyMMddHHmmss，按服务器约定时区。
	 */
	@SimpleFunction
	void 置文件修改时间(String file, String time);

	/**
	 * 同步取得 MDTM 时间文本；须在后台线程调用。
	 *
	 * @param file 远程文件路径。
	 * @return 实际结果；失败抛出 FTP错误。
	 */
	@SimpleFunction
	String 取文件修改时间(String file);

	/**
	 * 同步切换到上级工作目录；须在后台线程调用。
	 */
	@SimpleFunction
	void 切换到父目录();

	/**
	 * 同步发送 REIN，服务器支持时恢复未登录状态；须在后台线程调用。
	 */
	@SimpleFunction
	void 重置登录状态();

	/**
	 * 同步列出目录类型条目；须在后台线程调用。
	 *
	 * @param directory 目录路径，可为空文本。
	 * @return 实际结果；失败抛出 FTP错误。
	 */
	@SimpleFunction
	FTP远程文件[] 取目录列表(String directory);

	/**
	 * 同步通过 MLSD 读取机器格式列表，服务器须支持 MLSD；须在后台线程调用。
	 *
	 * @param directory 目录路径，可为空文本。
	 * @return 实际结果；失败抛出 FTP错误。
	 */
	@SimpleFunction
	FTP远程文件[] 取机器目录列表(String directory);

	/**
	 * 同步通过 NLST 读取文件名，服务器失败抛出 FTP错误；须在后台线程调用。
	 *
	 * @param directory 目录路径，可为空文本。
	 * @return 实际结果；失败抛出 FTP错误。
	 */
	@SimpleFunction
	String[] 取文件名列表(String directory);

	/**
	 * 同步取得一次 LIST 结果并建立本地分页游标；须在后台线程调用。
	 *
	 * @param directory 目录路径，可为空文本。
	 * @return 实际结果；失败抛出 FTP错误。
	 */
	@SimpleFunction
	FTP分页列表 取分页文件(String directory);

	/**
	 * 同步打开独占连接的受控输入流；关闭时自动检查最终 FTP 答复。须在后台线程调用，打开后不可执行其它命令。
	 *
	 * @param file 远程文件路径。
	 * @param offset 起始字节偏移，零从头开始。
	 * @return 实际结果；失败抛出 FTP错误。
	 */
	@SimpleFunction
	FTP输入流 取文件输入流(String file, long offset);

	/**
	 * 同步打开独占连接的受控输出流；关闭时自动检查最终 FTP 答复。须在后台线程调用，偏移零覆盖文件。
	 *
	 * @param file 远程文件路径。
	 * @param offset 起始字节偏移，零从头开始。
	 * @return 实际结果；失败抛出 FTP错误。
	 */
	@SimpleFunction
	FTP输出流 取文件输出流(String file, long offset);

	/**
	 * 启用命令事件后在界面线程通知控制通道记录。
	 *
	 * @param command 命令名称，答复时可为空文本。
	 * @param message 命令或答复文本；认证密码及账号附加凭据会隐藏。
	 * @param code 服务器答复码，发送命令时为零。
	 * @param received 是否为服务器答复。
	 */
	@SimpleEvent
	void 传输命令(String command, String message, int code, boolean received);
}
