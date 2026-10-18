package simple.library.wsd.ftpclient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

/**
 * 一个组件拥有一个 FTP 会话；目录切换和操作在同一锁内完成并恢复工作目录。
 * 同步方法阻塞调用线程。连接状态仅表示本地套接字状态，不保证服务端仍在线。
 */
final class FtpSession implements TransferSession {

	private final FTPClient client;
	private volatile boolean disposed;
	private String host;
	private int port;
	private String username;
	private String password;

	FtpSession() {
		this(new FTPClient());
	}

	FtpSession(FTPClient client) {
		this.client = client;
		client.setConnectTimeout(5000);
		client.setDefaultTimeout(5000);
		client.setDataTimeout(5000);
		client.setControlEncoding("GBK");
		FTPClientConfig config = new FTPClientConfig();
		config.setServerLanguageCode("zh");
		client.configure(config);
	}

	public synchronized boolean login(String host, int port, String username, String password) {
		closeConnection();
		this.host = null;
		this.username = null;
		this.password = null;
		if (disposed || !command(host) || host.length() == 0 || port < 1 || port > 65535
				|| !command(username) || !command(password)) {
			return false;
		}

		this.host = host;
		this.port = port;
		this.username = username;
		this.password = password;
		return connect();
	}

	private boolean connect() {
		if (disposed || host == null) {
			return false;
		}

		try {
			client.connect(host, port);
			if (!FTPReply.isPositiveCompletion(client.getReplyCode()) || !client.login(username, password)) {
				closeConnection();
				return false;
			}

			client.setSoTimeout(5000);
			client.enterLocalPassiveMode();
			if (!client.setFileType(FTP.BINARY_FILE_TYPE)) {
				closeConnection();
				return false;
			}

			return true;
		} catch (IOException ex) {
			closeConnection();
			return false;
		}
	}

	public synchronized boolean connected() {
		return !disposed && client.isConnected();
	}

	public synchronized String workingDirectory() {
		if (!connected()) {
			return "";
		}

		try {
			String path = client.printWorkingDirectory();
			return path == null ? "" : path;
		} catch (IOException ex) {
			return "";
		}
	}

	private interface DirectoryAction {
		boolean run() throws IOException;
	}

	/**
	 * 工作目录未知时拒绝操作；恢复失败也不得报告成功。
	 */
	private boolean inDirectory(String path, DirectoryAction action) throws IOException {
		if (!command(path)) {
			return false;
		}

		String previous = client.printWorkingDirectory();
		String target = path.length() == 0 ? "." : path.replace('\\', '/');

		if (previous == null || !client.changeWorkingDirectory(target)) {
			return false;
		}

		boolean result;

		try {
			result = action.run();
		} finally {
			if (!client.changeWorkingDirectory(previous)) {
				throw new IOException("Cannot restore FTP working directory");
			}
		}

		return result;
	}

	public synchronized String list(String directory) {
		final StringBuilder result = new StringBuilder();
		if (!connected()) {
			return "";
		}

		try {
			boolean success = inDirectory(directory, new DirectoryAction() {
				@Override
				public boolean run() throws IOException {
					FTPFile[] files = client.listFiles();
					if (files == null || !FTPReply.isPositiveCompletion(client.getReplyCode())) {
						return false;
					}

					for (FTPFile file : files) {
						if (file == null || ".".equals(file.getName()) || "..".equals(file.getName())) {
							continue;
						}

						if (result.length() > 0) {
							result.append('\n');
						}

						result.append(file.getName()).append('/').append(file.getSize()).append('/')
							.append(file.isDirectory());
					}

					return true;
				}
			});
			return success ? result.toString() : "";
		} catch (IOException ex) {
			return "";
		}
	}

	/**
	 * mode: 0 创建目录，1 重命名，2 删除空目录，3 删除文件。
	 */
	public synchronized boolean mutate(String directory, final String name, final String newName, final int mode) {
		if (!connected() || !basename(name) || (mode == 1 && !basename(newName))) {
			return false;
		}

		try {
			return inDirectory(directory, new DirectoryAction() {
				@Override
				public boolean run() throws IOException {
					switch (mode) {
					case 0: return client.makeDirectory(name);
					case 1: return client.rename(name, newName);
					case 2: return client.removeDirectory(name);
					case 3: return client.deleteFile(name);
					default: return false;
					}
				}
			});
		} catch (IOException ex) {
			return false;
		}
	}

	public synchronized boolean upload(File localDirectory, final String name, String remoteDirectory) {
		if (disposed || localDirectory == null || !basename(name) || !command(remoteDirectory)
				|| (!connected() && !connect())) {
			return false;
		}

		final File source = new File(localDirectory, name);

		try {
			return inDirectory(remoteDirectory, new DirectoryAction() {
				@Override
				public boolean run() throws IOException {
					return uploadEntry(source, name, new HashSet<String>());
				}
			});
		} catch (IOException ex) {
			return false;
		}
	}

	private boolean uploadEntry(final File source, final String name, final Set<String> ancestors) throws IOException {
		if (disposed || !basename(name)) {
			return false;
		}

		if (source.isFile()) {
			FileInputStream input = new FileInputStream(source);

			try {
				return client.storeFile(name, input);
			} finally {
				input.close();
			}
		}

		if (!source.isDirectory() || !ancestors.add(source.getCanonicalPath())) {
			return false;
		}

		try {
			// 已存在的目录允许继续，但仍必须由 CWD 成功确认。
			client.makeDirectory(name);
			return inDirectory(name, new DirectoryAction() {
				@Override
				public boolean run() throws IOException {
					File[] children = source.listFiles();
					if (children == null) {
						return false;
					}

					for (File child : children) {
						if (!child.isHidden() && !uploadEntry(child, child.getName(), ancestors)) {
							return false;
						}
					}

					return true;
				}
			});
		} finally {
			ancestors.remove(source.getCanonicalPath());
		}
	}

	public synchronized boolean download(String remoteDirectory, final String name, final File localDirectory) {
		if (disposed || localDirectory == null || !basename(name) || !command(remoteDirectory)
				|| (!connected() && !connect())) {
			return false;
		}

		try {
			return inDirectory(remoteDirectory, new DirectoryAction() {
				@Override
				public boolean run() throws IOException {
					FTPFile[] files = client.listFiles();
					if (files == null || !FTPReply.isPositiveCompletion(client.getReplyCode())) {
						return false;
					}

					for (FTPFile file : files) {
						if (file != null && name.equals(file.getName())) {
							return downloadEntry(file, localDirectory, 0);
						}
					}

					return false;
				}
			});
		} catch (IOException ex) {
			return false;
		}
	}

	private boolean downloadEntry(final FTPFile file, File parent, final int depth) throws IOException {
		if (disposed || depth > 128 || !basename(file.getName()) || file.isSymbolicLink()) {
			return false;
		}

		if (!parent.isDirectory() && !parent.mkdirs()) {
			return false;
		}

		final File target = new File(parent, file.getName());

		// 拒绝现存本地链接把下载内容引出调用者指定的目录。
		if (!target.getCanonicalFile().getParentFile().equals(parent.getCanonicalFile())) {
			return false;
		}

		if (file.isFile()) {
			FileOutputStream output = new FileOutputStream(target);

			try {
				return client.retrieveFile(file.getName(), output);
			} finally {
				output.close();
			}
		}

		if (!file.isDirectory() || (!target.isDirectory() && !target.mkdirs())) {
			return false;
		}

		return inDirectory(file.getName(), new DirectoryAction() {
			@Override
			public boolean run() throws IOException {
				FTPFile[] children = client.listFiles();
				if (children == null || !FTPReply.isPositiveCompletion(client.getReplyCode())) {
					return false;
				}

				for (FTPFile child : children) {
					if (child == null) {
						return false;
					}

					if (".".equals(child.getName()) || "..".equals(child.getName())) {
						continue;
					}

					if (!downloadEntry(child, target, depth + 1)) {
						return false;
					}
				}

				return true;
			}
		});
	}

	public synchronized void disconnect() {
		closeConnection();

		// 显式断开后不再自动重连；销毁时也释放保存的凭据。
		host = null;
		username = null;
		password = null;
	}

	void dispose() {
		disposed = true;
		new Thread(new Runnable() {
			@Override
			public void run() {
				disconnect();
			}
		}, "Simple-FTP-close").start();
	}

	@Override
	public String error() {
		return "";
	}

	private void closeConnection() {
		if (client.isConnected()) {
			try {
				client.disconnect();
			} catch (IOException ignored) {
				// 关闭失败不能把随后操作误报为成功。
			}
		}
	}

	private static boolean command(String value) {
		return value != null && value.indexOf('\r') < 0 && value.indexOf('\n') < 0;
	}

	private static boolean basename(String value) {
		return command(value) && value.length() > 0 && !".".equals(value) && !"..".equals(value)
			&& value.indexOf('/') < 0 && value.indexOf('\\') < 0;
	}
}
