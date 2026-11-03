package simple.library.wsd.ftpclient;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;

/**
 * 基于 Commons Net 3.6 的 FTP 会话，显式核对控制答复及数据流最终结果。
 *
 * @author 树先生 xhwsd@qq.com
 */
final class FtpSession implements RemoteSession {
	final org.apache.commons.net.ftp.FTPClient client = new org.apache.commons.net.ftp.FTPClient();
	boolean loggedIn;
	int fileType = 2;
	int structure = 7;
	int transferMode = 10;
	int dataTimeout = 10000;

	/**
	 * 设置明确的 FTP 初始参数。
	 */
	FtpSession() {
		client.setConnectTimeout(10000);
		client.setDefaultTimeout(10000);
		client.setDataTimeout(dataTimeout);
		client.setControlEncoding("UTF-8");
		client.setAutodetectUTF8(true);
	}

	/**
	 * 规范化可为空的服务器文本。
	 *
	 * @param value 服务器文本。
	 * @return 非空文本。
	 */
	static String text(String value) {
		return value == null ? "" : value;
	}

	/**
	 * 检查服务器命令是否成功。
	 *
	 * @param success 本次命令的实际结果。
	 * @throws IOException 服务器未返回肯定完成答复。
	 */
	void require(boolean success) throws IOException {
		if (!success) {
			throw new IOException(
					"FTP " + client.getReplyCode() + "：" + text(client.getReplyString()).trim());
		}
	}

	/**
	 * 建立控制连接，不执行登录。
	 *
	 * @param host 主机名。
	 * @param port 服务器端口。
	 * @throws Exception 控制连接失败或服务器拒绝连接。
	 */
	void connect(String host, int port) throws Exception {
		disconnect();
		try {
			client.connect(host, port);
			require(
					org.apache.commons.net.ftp.FTPReply.isPositiveCompletion(
							client.getReplyCode()));
			client.enterLocalPassiveMode();
		} catch (Exception error) {
			disconnect();
			throw error;
		}
	}

	/**
	 * 登录并应用预先配置的传输参数。
	 *
	 * @param user 用户名。
	 * @param password 密码。
	 * @throws Exception 认证失败或服务器拒绝传输参数。
	 */
	void login(String user, String password) throws Exception {
		require(client.login(user, password));
		loggedIn = true;
		require(client.setFileType(fileType));
		require(client.setFileStructure(structure));
		require(client.setFileTransferMode(transferMode));
	}

	/**
	 * 拒绝未经认证的文件操作。
	 * @throws IOException 连接未建立或尚未登录。
	 */
	private void ready() throws IOException {
		if (!connected() || !loggedIn) {
			throw new IOException("请先连接并登录 FTP 服务器。");
		}
	}

	@Override
	public boolean connected() {
		return client.isConnected();
	}

	@Override
	public void disconnect() throws Exception {
		loggedIn = false;
		if (client.isConnected()) {
			client.disconnect();
		}
	}

	@Override
	public String pwd() throws Exception {
		ready();
		String result = client.printWorkingDirectory();
		require(result != null);
		return result;
	}

	@Override
	public void cwd(String directory) throws Exception {
		ready();
		require(client.changeWorkingDirectory(directory));
	}

	@Override
	public FTP远程文件[] list(String directory) throws Exception {
		ready();
		org.apache.commons.net.ftp.FTPFile[] result =
				client.listFiles(directory.isEmpty() ? null : directory);
		require(org.apache.commons.net.ftp.FTPReply.isPositiveCompletion(client.getReplyCode()));
		return files(result);
	}

	@Override
	public FTP远程文件 stat(String file) throws Exception {
		ready();
		org.apache.commons.net.ftp.FTPFile entry = client.mlistFile(file);
		if (entry == null) {
			org.apache.commons.net.ftp.FTPFile[] entries = client.listFiles(file);
			require(
					org.apache.commons.net.ftp.FTPReply.isPositiveCompletion(client.getReplyCode())
							&& entries != null
							&& entries.length == 1);
			entry = entries[0];
		}
		return file(entry);
	}

	@Override
	public void mutate(String operation, String source, String target) throws Exception {
		ready();
		boolean result;
		switch (operation) {
			case "mkdir":
				result = client.makeDirectory(source);
				break;
			case "rmdir":
				result = client.removeDirectory(source);
				break;
			case "delete":
				result = client.deleteFile(source);
				break;
			case "rename":
				result = client.rename(source, target);
				break;
			default:
				throw new IOException("未知远程操作。");
		}
		require(result);
	}

	@Override
	public InputStream read(String file, long offset) throws Exception {
		ready();
		client.setRestartOffset(offset);
		InputStream result = client.retrieveFileStream(file);
		require(result != null);
		return result;
	}

	@Override
	public OutputStream write(String file, long offset) throws Exception {
		ready();
		client.setRestartOffset(offset);
		OutputStream result = client.storeFileStream(file);
		require(result != null);
		return result;
	}

	@Override
	public void finish() throws Exception {
		try {
			require(client.completePendingCommand());
		} finally {
			client.setRestartOffset(0);
		}
	}

	/**
	 * 转换原生文件列表。
	 *
	 * @param entries Commons Net 文件数组。
	 * @return 独立文件快照数组。
	 */
	static FTP远程文件[] files(org.apache.commons.net.ftp.FTPFile[] entries) {
		if (entries == null) {
			return new FTP远程文件[0];
		}
		FTP远程文件[] result = new FTP远程文件[entries.length];
		for (int index = 0; index < entries.length; index++) {
			result[index] = file(entries[index]);
		}
		return result;
	}

	/**
	 * 复制原生文件信息。
	 *
	 * @param entry Commons Net 文件条目。
	 * @return 独立文件快照。
	 */
	static FTP远程文件 file(org.apache.commons.net.ftp.FTPFile entry) {
		Calendar time = entry.getTimestamp();
		return new FTP远程文件(
				text(entry.getName()),
				entry.getSize(),
				time == null ? 0 : time.getTimeInMillis(),
				entry.getType(),
				text(entry.getUser()),
				text(entry.getGroup()),
				text(entry.getLink()),
				text(entry.getRawListing()));
	}
}
