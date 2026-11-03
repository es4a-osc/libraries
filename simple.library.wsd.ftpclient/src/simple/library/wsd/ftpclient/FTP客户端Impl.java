package simple.library.wsd.ftpclient;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import simple.runtime.components.组件容器;

/**
 * 参考火山 commons_ftp 的独立 FTP 客户端；同步网络方法须在后台线程调用，后台任务结果回到界面线程。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class FTP客户端Impl extends ClientCore implements FTP客户端 {
	private final FtpSession ftp;
	private boolean commands;
	private int controlTimeout = 10000;

	/**
	 * 建立当前协议组件。
	 *
	 * @param container 所属窗口容器。
	 */
	public FTP客户端Impl(组件容器 container) {
		super(container, new FtpSession());
		ftp = (FtpSession) remote;
		ftp.client.addProtocolCommandListener(
				new org.apache.commons.net.ProtocolCommandListener() {
					@Override
					public void protocolCommandSent(
							org.apache.commons.net.ProtocolCommandEvent record) {
						if (!commands) {
							return;
						}
						String command = FtpSession.text(record.getCommand());
						String message =
								command.equalsIgnoreCase("PASS") || command.equalsIgnoreCase("ACCT")
										? command + " ******"
										: FtpSession.text(record.getMessage());
						event("传输命令", command, message, 0, false);
					}

					@Override
					public void protocolReplyReceived(
							org.apache.commons.net.ProtocolCommandEvent record) {
						if (commands) {
							event(
									"传输命令",
									"",
									FtpSession.text(record.getMessage()),
									record.getReplyCode(),
									true);
						}
					}
				});
	}

	@Override
	public boolean 已连接() {
		return !disposed.get() && remote.connected();
	}

	@Override
	public boolean 忙() {
		return occupied.get();
	}

	@Override
	public String 错误信息() {
		return lastError;
	}

	@Override
	public long 当前任务编号() {
		Task task = active;
		return task == null ? 0 : task.id;
	}

	@Override
	public int 传输缓冲区() {
		return bufferSize;
	}

	@Override
	public void 传输缓冲区(int value) {
		if (value < 4096 || value > 1048576) {
			throw new FTP错误("缓冲区须为 4096 至 1048576 字节。");
		}
		setting(
				new ClientCore.Action<Object>() {
					@Override
					public Object run() throws Exception {
						bufferSize = value;
						return null;
					}
				});
	}

	@Override
	public String 取工作目录() {
		return operation(
				new ClientCore.Action<String>() {
					@Override
					public String run() throws Exception {
						return remote.pwd();
					}
				});
	}

	@Override
	public void 置工作目录(String directory) {
		operation(
				new ClientCore.Action<Object>() {
					@Override
					public Object run() throws Exception {
						remote.cwd(text(directory));
						return null;
					}
				});
	}

	@Override
	public FTP远程文件[] 取文件列表(String directory) {
		return operation(
				new ClientCore.Action<FTP远程文件[]>() {
					@Override
					public FTP远程文件[] run() throws Exception {
						return remote.list(text(directory));
					}
				});
	}

	@Override
	public FTP远程文件 取文件信息(String file) {
		return operation(
				new ClientCore.Action<FTP远程文件>() {
					@Override
					public FTP远程文件 run() throws Exception {
						return remote.stat(text(file));
					}
				});
	}

	@Override
	public void 创建目录(String path) {
		operation(
				new ClientCore.Action<Object>() {
					@Override
					public Object run() throws Exception {
						remote.mutate("mkdir", text(path), "");
						return null;
					}
				});
	}

	@Override
	public void 删除目录(String path) {
		operation(
				new ClientCore.Action<Object>() {
					@Override
					public Object run() throws Exception {
						remote.mutate("rmdir", text(path), "");
						return null;
					}
				});
	}

	@Override
	public void 删除文件(String path) {
		operation(
				new ClientCore.Action<Object>() {
					@Override
					public Object run() throws Exception {
						remote.mutate("delete", text(path), "");
						return null;
					}
				});
	}

	@Override
	public void 重命名(String source, String target) {
		operation(
				new ClientCore.Action<Object>() {
					@Override
					public Object run() throws Exception {
						remote.mutate("rename", text(source), text(target));
						return null;
					}
				});
	}

	@Override
	public byte[] 下载字节(String file, int limit) {
		return operation(
				new ClientCore.Action<byte[]>() {
					@Override
					public byte[] run() throws Exception {
						if (limit < 1 || limit > 8388608) {
							throw new FTP错误("接收上限须为 1 至 8388608。");
						}
						try (InputStream input = remote.read(text(file), 0);
								ByteArrayOutputStream output = new ByteArrayOutputStream()) {
							byte[] buffer = new byte[Math.min(65536, limit)];
							int count;
							while ((count = input.read(buffer)) >= 0) {
								if ((long) output.size() + count > limit) {
									throw new IOException("文件超出接收上限。");
								}
								output.write(buffer, 0, count);
							}
							input.close();
							remote.finish();
							return output.toByteArray();
						} catch (Exception error) {
							closeNetwork();
							throw error;
						}
					}
				});
	}

	@Override
	public void 上传字节(String file, byte[] bytes) {
		operation(
				new ClientCore.Action<Object>() {
					@Override
					public Object run() throws Exception {
						if (bytes == null || bytes.length > 8388608) {
							throw new FTP错误("字节数组不能为空对象或超过 8 MiB。");
						}
						try (OutputStream output = remote.write(text(file), 0)) {
							output.write(bytes);
						} catch (Exception error) {
							closeNetwork();
							throw error;
						}
						remote.finish();
						return null;
					}
				});
	}

	@Override
	public long 上传文件(String local, String remotePath, long offset) {
		text(local);
		text(remotePath);
		if (offset < 0) {
			throw new FTP错误("偏移不能为负。");
		}
		return begin(
				1,
				local,
				remotePath,
				new ClientCore.Job() {
					@Override
					public void run(ClientCore.Task task) throws Exception {
						upload(task, localFile(local), remotePath, offset);
					}
				});
	}

	@Override
	public long 下载文件(String remotePath, String local, long offset) {
		text(local);
		text(remotePath);
		if (offset < 0) {
			throw new FTP错误("偏移不能为负。");
		}
		return begin(
				0,
				remotePath,
				local,
				new ClientCore.Job() {
					@Override
					public void run(ClientCore.Task task) throws Exception {
						download(task, remotePath, localFile(local), offset);
					}
				});
	}

	@Override
	public long 上传目录(String local, String remotePath) {
		text(local);
		text(remotePath);
		return begin(
				1,
				local,
				remotePath,
				new ClientCore.Job() {
					@Override
					public void run(ClientCore.Task task) throws Exception {
						uploadTree(task, localFile(local), remotePath, 0);
					}
				});
	}

	@Override
	public long 下载目录(String remotePath, String local) {
		text(local);
		text(remotePath);
		return begin(
				0,
				remotePath,
				local,
				new ClientCore.Job() {
					@Override
					public void run(ClientCore.Task task) throws Exception {
						downloadTree(task, remotePath, localFile(local), 0);
					}
				});
	}

	@Override
	public long 取下载偏移(String local) {
		return downloadOffset(text(local));
	}

	@Override
	public void 清理下载断点(String local) {
		setting(
				new ClientCore.Action<Object>() {
					@Override
					public Object run() throws Exception {
						File part = partials.remove(localFile(local).toString());
						if (part != null) {
							LocalFiles.deleteIfExists(part);
						}
						return null;
					}
				});
	}

	@Override
	public boolean 取消传输() {
		return cancel();
	}

	@Override
	public void 断开连接() {
		operation(
				new ClientCore.Action<Object>() {
					@Override
					public Object run() throws Exception {
						remote.disconnect();
						return null;
					}
				});
	}

	@Override
	public void 连接完成(long id, boolean success, String message) {
		event("连接完成", id, success, message);
	}

	@Override
	public void 传输进度(
			long id, int direction, String source, String target, long completed, long total) {
		event("传输进度", id, direction, source, target, completed, total);
	}

	@Override
	public void 传输完成(
			long id,
			int direction,
			String source,
			String target,
			boolean success,
			boolean cancelled,
			String message) {
		event("传输完成", id, direction, source, target, success, cancelled, message);
	}

	@Override
	public boolean 已登录() {
		return ftp.loggedIn && ftp.connected();
	}

	@Override
	public boolean 启用命令事件() {
		return commands;
	}

	@Override
	public void 启用命令事件(boolean value) {
		setting(
				new ClientCore.Action<Object>() {
					@Override
					public Object run() throws Exception {
						commands = value;
						return null;
					}
				});
	}

	@Override
	public int 连接超时() {
		return ftp.client.getConnectTimeout();
	}

	@Override
	public void 连接超时(int value) {
		setting(
				new ClientCore.Action<Object>() {
					@Override
					public Object run() throws Exception {
						timeout(value);
						if (remote.connected()) {
							throw new FTP错误("连接超时须在连接前设置。");
						}
						ftp.client.setConnectTimeout(value);
						return null;
					}
				});
	}

	@Override
	public int 控制超时() {
		return controlTimeout;
	}

	@Override
	public void 控制超时(int value) {
		if (remote.connected()) {
			background();
		}
		setting(
				new ClientCore.Action<Object>() {
					@Override
					public Object run() throws Exception {
						timeout(value);
						controlTimeout = value;
						ftp.client.setDefaultTimeout(value);
						if (remote.connected()) {
							ftp.client.setSoTimeout(value);
						}
						return null;
					}
				});
	}

	@Override
	public int 数据超时() {
		return ftp.dataTimeout;
	}

	@Override
	public void 数据超时(int value) {
		setting(
				new ClientCore.Action<Object>() {
					@Override
					public Object run() throws Exception {
						timeout(value);
						ftp.dataTimeout = value;
						ftp.client.setDataTimeout(value);
						return null;
					}
				});
	}

	@Override
	public String 编码() {
		return ftp.client.getControlEncoding();
	}

	@Override
	public void 编码(String value) {
		setting(
				new ClientCore.Action<Object>() {
					@Override
					public Object run() throws Exception {
						if (remote.connected()) {
							throw new FTP错误("编码须在连接前设置。");
						}
						java.nio.charset.Charset.forName(value);
						ftp.client.setControlEncoding(value);
						return null;
					}
				});
	}

	@Override
	public boolean 自动检测编码() {
		return ftp.client.getAutodetectUTF8();
	}

	@Override
	public void 自动检测编码(boolean value) {
		setting(
				new ClientCore.Action<Object>() {
					@Override
					public Object run() throws Exception {
						if (remote.connected()) {
							throw new FTP错误("自动检测须在连接前设置。");
						}
						ftp.client.setAutodetectUTF8(value);
						return null;
					}
				});
	}

	@Override
	public boolean 包含隐藏文件() {
		return ftp.client.getListHiddenFiles();
	}

	@Override
	public void 包含隐藏文件(boolean value) {
		setting(
				new ClientCore.Action<Object>() {
					@Override
					public Object run() throws Exception {
						ftp.client.setListHiddenFiles(value);
						return null;
					}
				});
	}

	@Override
	public int 数据接收缓冲区尺寸() {
		return ftp.client.getReceiveDataSocketBufferSize();
	}

	@Override
	public void 数据接收缓冲区尺寸(int value) {
		setting(
				new ClientCore.Action<Object>() {
					@Override
					public Object run() throws Exception {
						if (value < 0) {
							throw new FTP错误("缓冲区不能为负。");
						}
						ftp.client.setReceieveDataSocketBufferSize(value);
						return null;
					}
				});
	}

	@Override
	public int 数据发送缓冲区尺寸() {
		return ftp.client.getSendDataSocketBufferSize();
	}

	@Override
	public void 数据发送缓冲区尺寸(int value) {
		setting(
				new ClientCore.Action<Object>() {
					@Override
					public Object run() throws Exception {
						if (value < 0) {
							throw new FTP错误("缓冲区不能为负。");
						}
						ftp.client.setSendDataSocketBufferSize(value);
						return null;
					}
				});
	}

	@Override
	public int 文件类型() {
		return ftp.fileType;
	}

	@Override
	public void 文件类型(int value) {
		if (remote.connected()) {
			background();
		}
		setting(
				new ClientCore.Action<Object>() {
					@Override
					public Object run() throws Exception {
						if (!(value >= 0 && value <= 3)) {
							throw new FTP错误("参数不在有效范围。");
						}
						if (ftp.loggedIn) {
							ftp.require(ftp.client.setFileType(value));
						}
						ftp.fileType = value;
						return null;
					}
				});
	}

	@Override
	public int 文件结构() {
		return ftp.structure;
	}

	@Override
	public void 文件结构(int value) {
		if (remote.connected()) {
			background();
		}
		setting(
				new ClientCore.Action<Object>() {
					@Override
					public Object run() throws Exception {
						if (!(value >= 7 && value <= 9)) {
							throw new FTP错误("参数不在有效范围。");
						}
						if (ftp.loggedIn) {
							ftp.require(ftp.client.setFileStructure(value));
						}
						ftp.structure = value;
						return null;
					}
				});
	}

	@Override
	public int 传输模式() {
		return ftp.transferMode;
	}

	@Override
	public void 传输模式(int value) {
		if (remote.connected()) {
			background();
		}
		setting(
				new ClientCore.Action<Object>() {
					@Override
					public Object run() throws Exception {
						if (!(value >= 10 && value <= 12)) {
							throw new FTP错误("参数不在有效范围。");
						}
						if (ftp.loggedIn) {
							ftp.require(ftp.client.setFileTransferMode(value));
						}
						ftp.transferMode = value;
						return null;
					}
				});
	}

	@Override
	public void 连接(String host, int port) {
		operation(
				new ClientCore.Action<Object>() {
					@Override
					public Object run() throws Exception {
						address(host, port);
						ftp.connect(host, port);
						return null;
					}
				});
	}

	@Override
	public long 开始连接(String host, int port) {
		address(host, port);
		return begin(
				-1,
				host,
				"",
				new ClientCore.Job() {
					@Override
					public void run(ClientCore.Task task) throws Exception {
						ftp.connect(host, port);
					}
				});
	}

	@Override
	public void 登录(String user, String password) {
		operation(
				new ClientCore.Action<Object>() {
					@Override
					public Object run() throws Exception {
						ftp.login(text(user), text(password));
						return null;
					}
				});
	}

	@Override
	public void 退出登录() {
		operation(
				new ClientCore.Action<Object>() {
					@Override
					public Object run() throws Exception {
						ftp.require(ftp.client.logout());
						ftp.loggedIn = false;
						return null;
					}
				});
	}

	@Override
	public void 保持连接() {
		operation(
				new ClientCore.Action<Object>() {
					@Override
					public Object run() throws Exception {
						ftp.require(ftp.client.sendNoOp());
						return null;
					}
				});
	}

	@Override
	public void 主动模式() {
		setting(
				new ClientCore.Action<Object>() {
					@Override
					public Object run() throws Exception {
						ftp.client.enterLocalActiveMode();
						return null;
					}
				});
	}

	@Override
	public void 被动模式() {
		setting(
				new ClientCore.Action<Object>() {
					@Override
					public Object run() throws Exception {
						ftp.client.enterLocalPassiveMode();
						return null;
					}
				});
	}

	@Override
	public void 置主动端口范围(int minimum, int maximum) {
		setting(
				new ClientCore.Action<Object>() {
					@Override
					public Object run() throws Exception {
						if (minimum < 1 || maximum < minimum || maximum > 65535) {
							throw new FTP错误("主动端口范围无效。");
						}
						ftp.client.setActivePortRange(minimum, maximum);
						return null;
					}
				});
	}

	@Override
	public void 置主动外部地址(String host) {
		operation(
				new ClientCore.Action<Object>() {
					@Override
					public Object run() throws Exception {
						ftp.client.setActiveExternalIPAddress(text(host));
						return null;
					}
				});
	}

	@Override
	public String 取被动主机地址() {
		return FtpSession.text(ftp.client.getPassiveHost());
	}

	@Override
	public int 取被动主机端口() {
		return ftp.client.getPassivePort();
	}

	@Override
	public int 取答复码() {
		return ftp.client.getReplyCode();
	}

	@Override
	public String 取答复文本() {
		return FtpSession.text(ftp.client.getReplyString());
	}

	@Override
	public String[] 取答复文本数组() {
		return ftp.client.getReplyStrings().clone();
	}

	@Override
	public boolean 检查答复码(int code) {
		return org.apache.commons.net.ftp.FTPReply.isPositiveCompletion(code);
	}

	@Override
	public boolean 检查支持功能(String feature) {
		return operation(
				new ClientCore.Action<Boolean>() {
					@Override
					public Boolean run() throws Exception {
						return ftp.client.hasFeature(text(feature));
					}
				});
	}

	@Override
	public String 取系统类型() {
		return operation(
				new ClientCore.Action<String>() {
					@Override
					public String run() throws Exception {
						return ftp.client.getSystemType();
					}
				});
	}

	@Override
	public String 取状态(String path) {
		return operation(
				new ClientCore.Action<String>() {
					@Override
					public String run() throws Exception {
						String result =
								path.isEmpty()
										? ftp.client.getStatus()
										: ftp.client.getStatus(text(path));
						ftp.require(result != null);
						return result;
					}
				});
	}

	@Override
	public String 取帮助() {
		return operation(
				new ClientCore.Action<String>() {
					@Override
					public String run() throws Exception {
						String result = ftp.client.listHelp();
						ftp.require(result != null);
						return result;
					}
				});
	}

	@Override
	public void 发送特定命令(String command) {
		operation(
				new ClientCore.Action<Object>() {
					@Override
					public Object run() throws Exception {
						ftp.require(ftp.client.sendSiteCommand(text(command)));
						return null;
					}
				});
	}

	@Override
	public String[] 发送命令(String command, String arguments) {
		return operation(
				new ClientCore.Action<String[]>() {
					@Override
					public String[] run() throws Exception {
						int code = ftp.client.sendCommand(text(command), text(arguments));
						if (code >= 100 && code < 200) {
							closeNetwork();
							throw new IOException("请使用数据流或文件传输接口执行数据命令。");
						}
						return ftp.client.getReplyStrings().clone();
					}
				});
	}

	@Override
	public void 置文件修改时间(String file, String time) {
		operation(
				new ClientCore.Action<Object>() {
					@Override
					public Object run() throws Exception {
						ftp.require(ftp.client.setModificationTime(text(file), text(time)));
						return null;
					}
				});
	}

	@Override
	public String 取文件修改时间(String file) {
		return operation(
				new ClientCore.Action<String>() {
					@Override
					public String run() throws Exception {
						String result = ftp.client.getModificationTime(text(file));
						ftp.require(result != null);
						return result;
					}
				});
	}

	@Override
	public void 切换到父目录() {
		operation(
				new ClientCore.Action<Object>() {
					@Override
					public Object run() throws Exception {
						ftp.require(ftp.client.changeToParentDirectory());
						return null;
					}
				});
	}

	@Override
	public void 重置登录状态() {
		operation(
				new ClientCore.Action<Object>() {
					@Override
					public Object run() throws Exception {
						ftp.require(ftp.client.reinitialize());
						ftp.loggedIn = false;
						return null;
					}
				});
	}

	@Override
	public FTP远程文件[] 取目录列表(String directory) {
		return operation(
				new ClientCore.Action<FTP远程文件[]>() {
					@Override
					public FTP远程文件[] run() throws Exception {
						org.apache.commons.net.ftp.FTPFile[] result =
								ftp.client.listDirectories(
										directory.isEmpty() ? null : text(directory));
						ftp.require(
								org.apache.commons.net.ftp.FTPReply.isPositiveCompletion(
										ftp.client.getReplyCode()));
						return FtpSession.files(result);
					}
				});
	}

	@Override
	public FTP远程文件[] 取机器目录列表(String directory) {
		return operation(
				new ClientCore.Action<FTP远程文件[]>() {
					@Override
					public FTP远程文件[] run() throws Exception {
						org.apache.commons.net.ftp.FTPFile[] result =
								ftp.client.mlistDir(directory.isEmpty() ? null : text(directory));
						ftp.require(
								org.apache.commons.net.ftp.FTPReply.isPositiveCompletion(
										ftp.client.getReplyCode()));
						return FtpSession.files(result);
					}
				});
	}

	@Override
	public String[] 取文件名列表(String directory) {
		return operation(
				new ClientCore.Action<String[]>() {
					@Override
					public String[] run() throws Exception {
						String[] result =
								ftp.client.listNames(directory.isEmpty() ? null : text(directory));
						ftp.require(
								org.apache.commons.net.ftp.FTPReply.isPositiveCompletion(
										ftp.client.getReplyCode()));
						return result == null ? new String[0] : result;
					}
				});
	}

	@Override
	public FTP分页列表 取分页文件(String directory) {
		return operation(
				new ClientCore.Action<FTP分页列表>() {
					@Override
					public FTP分页列表 run() throws Exception {
						org.apache.commons.net.ftp.FTPListParseEngine result =
								directory.isEmpty()
										? ftp.client.initiateListParsing()
										: ftp.client.initiateListParsing(text(directory));
						ftp.require(
								org.apache.commons.net.ftp.FTPReply.isPositiveCompletion(
										ftp.client.getReplyCode()));
						return new FTP分页列表(result);
					}
				});
	}

	@Override
	public FTP输入流 取文件输入流(String file, long offset) {
		return openInput(text(file), offset);
	}

	@Override
	public FTP输出流 取文件输出流(String file, long offset) {
		return openOutput(text(file), offset);
	}

	@Override
	public void 传输命令(String command, String message, int code, boolean received) {
		event("传输命令", command, message, code, received);
	}
}
