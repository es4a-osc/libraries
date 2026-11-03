package simple.library.wsd.ftpclient;

import android.os.Build;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import simple.runtime.components.组件容器;

/**
 * 独立 SFTP 文件客户端；Android 8.0+，同步网络方法须在后台线程调用，公钥验证失败拒绝认证。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class SFTP客户端Impl extends ClientCore implements SFTP客户端 {
	private final SftpSession session;
	private final SftpHostTrust trust;
	private final SftpHostConfirmation confirmation;
	private String fingerprint = "";
	private int timeout = 10000;

	/**
	 * 建立当前协议组件。
	 *
	 * @param container 所属窗口容器。
	 */
	public SFTP客户端Impl(组件容器 container) {
		super(container, createSession());
		session = (SftpSession) remote;
		confirmation = new SftpHostConfirmation(activity);
		android.content.SharedPreferences preferences =
				activity.getSharedPreferences(
						"simple.library.wsd.ftpclient.hosts", android.content.Context.MODE_PRIVATE);
		trust =
				new SftpHostTrust(
						new SftpHostTrust.Store() {
							@Override
							public String read(String key) {
								return preferences.getString(key, "");
							}

							@Override
							public boolean write(String key, String value) {
								return preferences.edit().putString(key, value).commit();
							}
						},
						confirmation);
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
	public int 连接超时() {
		return timeout;
	}

	@Override
	public void 连接超时(int value) {
		setting(
				new ClientCore.Action<Object>() {
					@Override
					public Object run() throws Exception {
						ClientCore.timeout(value);
						if (remote.connected()) {
							throw new FTP错误("请断开后设置超时。");
						}
						timeout = value;
						return null;
					}
				});
	}

	@Override
	public String 服务器公钥指纹() {
		return fingerprint;
	}

	@Override
	public void 服务器公钥指纹(String value) {
		setting(
				new ClientCore.Action<Object>() {
					@Override
					public Object run() throws Exception {
						if (remote.connected()) {
							throw new FTP错误("指纹须在连接前设置。");
						}
						fingerprint = text(value).trim();
						return null;
					}
				});
	}

	@Override
	public void 连接(String host, int port, String user, String password) {
		operation(
				new ClientCore.Action<Object>() {
					@Override
					public Object run() throws Exception {
						address(host, port);
						credentials(user, password);
						session.connect(host, port, user, password, fingerprint, trust, timeout);
						return null;
					}
				});
	}

	@Override
	public long 开始连接(String host, int port, String user, String password) {
		address(host, port);
		credentials(user, password);
		String pinned = fingerprint;
		int milliseconds = timeout;
		return begin(
				-1,
				host,
				"",
				new ClientCore.Job() {
					@Override
					public void run(ClientCore.Task task) throws Exception {
						credentials(user, password);
						session.connect(host, port, user, password, pinned, trust, milliseconds);
					}
				});
	}

	/**
	 * 仅在支持的系统创建 SFTP 后端。
	 *
	 * @return 未连接的 SFTP 后端。
	 */
	private static RemoteSession createSession() {
		if (Build.VERSION.SDK_INT < 26) {
			throw new FTP错误("SFTP 需要 Android 8.0 或以上版本。");
		}
		return new SftpSession();
	}

	@Override
	void dispose() {
		if (confirmation != null) {
			confirmation.close();
		}
		super.dispose();
	}

	/**
	 * 校验结构化 SSH 认证参数，不套用 FTP 文本命令的换行限制。
	 *
	 * @param user 用户名。
	 * @param password 密码。
	 */
	private static void credentials(String user, String password) {
		if (user == null || user.isEmpty() || password == null) {
			throw new FTP错误("SFTP 用户名或密码无效。");
		}
	}
}
