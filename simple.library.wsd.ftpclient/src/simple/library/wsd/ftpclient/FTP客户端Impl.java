package simple.library.wsd.ftpclient;

import android.os.Handler;
import android.os.Looper;
import android.os.Build;
import android.content.Context;
import android.content.SharedPreferences;
import java.io.File;
import simple.runtime.android.MainActivity;
import simple.runtime.components.组件容器;
import simple.runtime.components.impl.组件Impl;
import simple.runtime.events.EventDispatcher;

/**
 * 主线程通知传输结果；同步方法和传输共用一个串行会话。
 */
public final class FTP客户端Impl extends 组件Impl implements FTP客户端 {

	private final Object operations = new Object();
	private TransferSession session = new FtpSession();
	private volatile int protocol = 协议_FTP;
	private volatile String fingerprint = "";
	private volatile String lastError = "";
	private final Handler handler = new Handler(Looper.getMainLooper());
	private final MainActivity activity;
	private final SftpHostTrust hostTrust;
	private final SftpHostConfirmation hostConfirmation;
	private volatile boolean destroyed;
	private boolean transferring;
	private final MainActivity.OnDestroyListener destroyListener = new MainActivity.OnDestroyListener() {

		@Override
		public void onDestroy() {
			dispose(false);
			EventDispatcher.unregisterEvent(FTP客户端Impl.this);
		}
	};

	public FTP客户端Impl(组件容器 container) {
		super(container);
		activity = MainActivity.getContext();
		if (Build.VERSION.SDK_INT >= 26) {
			hostConfirmation = new SftpHostConfirmation(activity);
			final SharedPreferences trustedHosts = activity.getSharedPreferences("simple.library.wsd.ftpclient.hosts", Context.MODE_PRIVATE);
			hostTrust = new SftpHostTrust(new SftpHostTrust.Store() {

				@Override
				public String read(String key) {
					return trustedHosts.getString(key, "");
				}

				@Override
				public boolean write(String key, String value) {
					return trustedHosts.edit().putString(key, value).commit();
				}
			}, hostConfirmation);
		} else {
			hostConfirmation = null;
			hostTrust = null;
		}

		activity.addOnDestroyListener(destroyListener);
	}

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
	@Override
	public boolean 登录服务器(String host, String username, String password) {
		return 登录服务器(host, 协议() == 协议_SFTP ? 22 : 21, username, password);
	}

	@Override
	public boolean 登录服务器(String host, int port, String username, String password) {
		synchronized (operations) {
			if (destroyed) return false;

			session.disconnect();
			int selected = protocol;
			if (selected == 协议_SFTP && Build.VERSION.SDK_INT < 26) {
				lastError = "SFTP 需要 Android 8.0 或以上版本。";
				return false;
			}

			session = selected == 协议_SFTP ? new SftpSession(fingerprint, hostTrust) : new FtpSession();
			boolean result = session.login(host, port, username, password);
			if (destroyed) {
				session.disconnect();
				return false;
			}

			return result(result);
		}
	}

	@Override
	public int 协议() {
		return protocol;
	}

	@Override
	public void 协议(int value) {
		if (value != 协议_FTP && value != 协议_SFTP) throw new IllegalArgumentException("未知传输协议。");

		protocol = value;
	}

	@Override
	public String 服务器公钥指纹() {
		return fingerprint;
	}

	@Override
	public void 服务器公钥指纹(String value) {
		fingerprint = value == null ? "" : value.trim();
	}

	@Override
	public String 错误信息() {
		return lastError;
	}

	/**
	 * 按操作结果更新组件错误状态。
	 *
	 * @param success 本次操作是否成功
	 * @return 操作成功时为 {@code true}，失败时为 {@code false}
	 */
	private boolean result(boolean success) {
		lastError = success ? "" : session.error();
		if (!success && lastError.length() == 0) lastError = "操作失败，请检查连接、路径和访问权限。";
		return success;
	}

	@Override
	public String 取工作目录() {
		synchronized (operations) {
			if (destroyed) return "";

			String path = session.workingDirectory();
			lastError = session.error();
			return path;
		}
	}

	/**
	 * 列出服务器当前工作目录，沿用完整方法的列表格式及错误信息。
	 *
	 * <p>阻塞调用线程，应在后台调用。</p>
	 *
	 * @return 列表文本；空目录或失败返回空文本。
	 */
	@Override
	public String 取文件列表() {
		return 取文件列表("");
	}

	@Override
	public String 取文件列表(String remoteDirectory) {
		synchronized (operations) {
			if (destroyed) return "";

			String listing = session.list(remoteDirectory);
			lastError = session.error();
			return listing;
		}
	}

	@Override
	public boolean 创建目录(String remoteDirectory, String name) {
		return mutate(remoteDirectory, name, null, 0);
	}

	@Override
	public boolean 重命名(String remoteDirectory, String oldName, String newName) {
		return mutate(remoteDirectory, oldName, newName, 1);
	}

	@Override
	public boolean 删除目录(String remoteDirectory, String name) {
		return mutate(remoteDirectory, name, null, 2);
	}

	@Override
	public boolean 删除文件(String remoteDirectory, String name) {
		return mutate(remoteDirectory, name, null, 3);
	}

	/**
	 * 按指定模式修改远程文件或目录。
	 *
	 * @param directory 远程目录
	 * @param name 要操作的名称
	 * @param newName 修改后的名称
	 * @param mode 操作模式
	 * @return 操作成功时为 {@code true}，失败时为 {@code false}
	 */
	private boolean mutate(String directory, String name, String newName, int mode) {
		synchronized (operations) {
			return !destroyed && result(session.mutate(directory, name, newName, mode));
		}
	}

	/**
	 * 校验并取得本地传输目录。
	 *
	 * @param path 文件或目录路径
	 * @return 校验通过的本地目录
	 */
	private File localDirectory(String path) {
		if (path == null) {
			return null;
		}

		File directory = new File(path);
		return directory.isAbsolute() ? directory : new File(activity.getFilesDir(), path);
	}

	@Override
	public boolean 上传文件(String localDirectory, String localName, String remoteDirectory) {
		synchronized (operations) {
			return !destroyed && result(session.upload(localDirectory(localDirectory), localName, remoteDirectory));
		}
	}

	@Override
	public boolean 下载文件(String remoteDirectory, String remoteName, String localDirectory) {
		synchronized (operations) {
			return !destroyed && result(session.download(remoteDirectory, remoteName, localDirectory(localDirectory)));
		}
	}

	@Override
	public void 断开连接() {
		synchronized (operations) {
			session.disconnect();
			lastError = "";
		}
	}

	@Override
	public boolean 取连接状态() {
		synchronized (operations) {
			return !destroyed && session.connected();
		}
	}

	@Override
	public boolean 开始上传(String localDirectory, String localName, String remoteDirectory) {
		return transfer(true, localDirectory, localName, remoteDirectory);
	}

	@Override
	public boolean 开始下载(String remoteDirectory, String remoteName, String localDirectory) {
		return transfer(false, localDirectory, remoteName, remoteDirectory);
	}

	/**
	 * 安排上传或下载任务并分发传输结果。
	 *
	 * @param upload 是否执行上传
	 * @param localDirectory 本地传输目录
	 * @param name 要操作的名称
	 * @param remoteDirectory 远程传输目录
	 * @return 操作成功时为 {@code true}，失败时为 {@code false}
	 */
	private synchronized boolean transfer(final boolean upload, final String localDirectory,
			final String name, final String remoteDirectory) {
		if (destroyed || transferring) {
			return false;
		}

		transferring = true;
		Thread worker = new Thread(new Runnable() {

			@Override
			public void run() {
				boolean result = false;

				try {
					result = upload ? 上传文件(localDirectory, name, remoteDirectory)
						: 下载文件(remoteDirectory, name, localDirectory);
				} catch (RuntimeException ignored) {
					// 本地访问异常与网络失败均通过完成事件返回，不泄漏凭据或路径日志。
					lastError = "传输失败，请检查本地访问权限及连接状态。";
				}

				final boolean succeeded = result;
				handler.post(new Runnable() {

					@Override
					public void run() {
						synchronized (FTP客户端Impl.this) {
							transferring = false;
							if (destroyed) {
								return;
							}
						}

						if (upload) {
							上传完毕(succeeded);
						} else {
							下载完毕(succeeded);
						}
					}
				});
			}
		}, "Simple-FTP-transfer");

		try {
			worker.start();
			return true;
		} catch (RuntimeException ex) {
			transferring = false;
			return false;
		}
	}

	@Override
	public void 上传完毕(boolean succeeded) {
		EventDispatcher.dispatchEvent(this, "上传完毕", succeeded);
	}

	@Override
	public void 下载完毕(boolean succeeded) {
		EventDispatcher.dispatchEvent(this, "下载完毕", succeeded);
	}

	@Override
	public void 销毁() {
		dispose(true);
		super.销毁();
	}

	/**
	 * 释放连接、线程及按要求移除生命周期监听器。
	 *
	 * @param removeListener 是否移除生命周期监听器
	 */
	private synchronized void dispose(boolean removeListener) {
		if (destroyed) {
			return;
		}

		destroyed = true;
		if (hostConfirmation != null) hostConfirmation.close();
		if (removeListener) {
			activity.removeOnDestroyListener(destroyListener);
		}

		handler.removeCallbacksAndMessages(null);
		new Thread(new Runnable() {

			@Override
			public void run() {
				synchronized (operations) {
					session.disconnect();
				}
			}
		}, "Simple-FTP-close").start();
	}
}
