package simple.library.wsd.ftpclient;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.HostKey;
import com.jcraft.jsch.HostKeyRepository;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.UserInfo;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

/**
 * SFTP 使用绝对远程路径，操作不改变工作目录；不共享 JSch 全局配置。
 */
final class SftpSession implements TransferSession {
	private final String fingerprint;
	private final SftpHostTrust trust;
	private Session connection;
	private ChannelSftp channel;
	private String host;
	private int port;
	private String username;
	private String password;
	private String lastError = "";

	SftpSession(String fingerprint) {
		this(fingerprint, null);
	}

	SftpSession(String fingerprint, SftpHostTrust trust) {
		this.fingerprint = fingerprint;
		this.trust = trust;
	}

	/**
	 * 指定指纹时严格匹配；留空时由应用信任策略确认，不隐式接受未知公钥。
	 */
	static final class PinnedHostKey implements HostKeyRepository {
		private final byte[] expected;
		private final SftpHostTrust trust;
		private final String host;
		private final int port;
		boolean rejected;
		String rejectionReason = "";

		/**
		 * 建立固定指纹或显式信任策略的服务器公钥验证器。
		 *
		 * @param fingerprint 服务器公钥指纹
		 */
		PinnedHostKey(String fingerprint) {
			this(fingerprint, null, "", 0);
		}

		/**
		 * 建立固定指纹或显式信任策略的服务器公钥验证器。
		 *
		 * @param fingerprint 服务器公钥指纹
		 * @param trust 服务器公钥信任策略
		 * @param host 服务器地址
		 * @param port 端口号
		 */
		PinnedHostKey(String fingerprint, SftpHostTrust trust, String host, int port) {
			this.trust = trust;
			this.host = host;
			this.port = port;
			if ((fingerprint == null || fingerprint.length() == 0) && trust != null) {
				expected = null;
				return;
			}

			if (fingerprint == null || !fingerprint.startsWith("SHA256:")) {
				throw new IllegalArgumentException("请配置服务器 SHA256 公钥指纹。");
			}

			try {
				expected = Base64.getDecoder().decode(fingerprint.substring(7));
				if (expected.length != 32) {
					throw new IllegalArgumentException();
				}
			} catch (IllegalArgumentException ex) {
				throw new IllegalArgumentException("服务器公钥指纹格式无效，需要 SHA256:Base64 格式。");
			}
		}

		@Override
		public int check(String host, byte[] key) {
			try {
				boolean matches = expected == null ? trust.verify(this.host, port, new HostKey(host, key).getType(), key)
					: MessageDigest.isEqual(expected, MessageDigest.getInstance("SHA-256").digest(key));
				rejected = !matches;
				rejectionReason = expected == null ? trust.error() : "服务器公钥指纹不匹配，已拒绝连接。";
				return matches ? OK : CHANGED;
			} catch (JSchException invalidKey) {
				rejected = true;
				rejectionReason = "服务器公钥无效，已拒绝连接。";
				return CHANGED;
			} catch (NoSuchAlgorithmException impossible) {
				throw new IllegalStateException(impossible);
			}
		}

		@Override
		public void add(HostKey key, UserInfo userInfo) {
		}

		@Override
		public void remove(String host, String type) {
		}

		@Override
		public void remove(String host, String type, byte[] key) {
		}

		@Override
		public String getKnownHostsRepositoryID() {
			return "SHA256 pinned host key";
		}

		@Override
		public HostKey[] getHostKey() {
			return new HostKey[0];
		}

		@Override
		public HostKey[] getHostKey(String host, String type) {
			return new HostKey[0];
		}
	}

	@Override
	public synchronized boolean login(String host, int port, String username, String password) {
		disconnect();
		lastError = "";
		if (!text(host) || host.length() == 0 || port < 1 || port > 65535
				|| !text(username) || username.length() == 0 || password == null) {
			lastError = "服务器地址、端口或用户名无效。";
			return false;
		}

		this.host = host;
		this.port = port;
		this.username = username;
		this.password = password;
		return connect();
	}

	/**
	 * 建立底层连接。
	 *
	 * @return 操作成功时为 {@code true}，失败时为 {@code false}
	 */
	private boolean connect() {
		if (host == null) {
			lastError = "请先登录服务器。";
			return false;
		}

		closeConnection();
		PinnedHostKey keys = null;

		try {
			keys = new PinnedHostKey(fingerprint, trust, host, port);
			JSch jsch = new JSch();
			jsch.setHostKeyRepository(keys);
			connection = jsch.getSession(username, host, port);
			connection.setConfig("StrictHostKeyChecking", "yes");
			connection.setConfig("PreferredAuthentications", "password");
			connection.setPassword(password.getBytes(StandardCharsets.UTF_8));
			connection.setTimeout(5000);
			connection.connect(5000);
			channel = (ChannelSftp) connection.openChannel("sftp");
			channel.connect(5000);
			return true;
		} catch (JSchException ex) {
			lastError = keys != null && keys.rejected ? keys.rejectionReason : "SFTP 连接失败：" + ex.getMessage();
		} catch (IllegalArgumentException ex) {
			lastError = ex.getMessage();
		} finally {
			if (!connected()) {
				closeConnection();
			}
		}

		return false;
	}

	@Override
	public synchronized boolean connected() {
		return connection != null && connection.isConnected() && channel != null && channel.isConnected();
	}

	/**
	 * 要求底层会话已经连接，否则拒绝操作。
	 *
	 * @throws IOException 访问文件、流或网络资源失败时
	 */
	private void requireConnection() throws IOException {
		if (!connected()) {
			throw new IOException("请先登录服务器。");
		}
	}

	@Override
	public synchronized String workingDirectory() {
		lastError = "";

		try {
			requireConnection();
			return channel.pwd();
		} catch (IOException ex) {
			lastError = ex.getMessage();
		} catch (SftpException ex) {
			lastError = ex.getMessage();
		}

		return "";
	}

	/**
	 * 先验证目录；不调用 cd，避免递归和失败分支遗留远程 CWD。
	 *
	 * @param path 文件或目录路径
	 * @return 校验通过的远程目录
	 * @throws IOException 访问文件、流或网络资源失败时
	 * @throws SftpException 服务器拒绝 SFTP 操作或远程路径无效时
	 */
	private String directory(String path) throws IOException, SftpException {
		requireConnection();
		if (!text(path)) {
			throw new IOException("远程目录无效。");
		}

		String absolute = path.replace('\\', '/');
		if (!absolute.startsWith("/")) {
			absolute = channel.pwd() + "/" + absolute;
		}

		ArrayDeque<String> parts = new ArrayDeque<String>();

		for (String part : absolute.split("/")) {
			if (part.length() == 0 || ".".equals(part)) continue;
			if ("..".equals(part)) {
				if (!parts.isEmpty()) parts.removeLast();
			} else {
				parts.addLast(part);
			}
		}

		StringBuilder normalized = new StringBuilder();

		for (String part : parts) normalized.append('/').append(part);
		absolute = normalized.length() == 0 ? "/" : normalized.toString();
		if (!channel.stat(quote(absolute)).isDir()) {
			throw new IOException("远程目标不是目录。");
		}

		return absolute;
	}

	/**
	 * 组合远程父目录和单个文件名。
	 *
	 * @param parent 父目录或父容器
	 * @param name 要操作的名称
	 * @return 组合后的远程路径
	 */
	private static String join(String parent, String name) {
		return ("/".equals(parent) ? "" : parent) + "/" + name;
	}

	/**
	 * JSch 的路径 API 解析通配符；调用者名称按字面值处理。
	 *
	 * @param path 文件或目录路径
	 * @return 已引用或转义的文本
	 */
	private static String quote(String path) {
		return path.replace("\\", "\\\\").replace("*", "\\*").replace("?", "\\?");
	}

	@Override
	public synchronized String list(String path) {
		lastError = "";

		try {
			Vector<ChannelSftp.LsEntry> entries = channelEntries(directory(path));
			StringBuilder result = new StringBuilder();

			for (ChannelSftp.LsEntry entry : entries) {
				if (dot(entry.getFilename())) continue;
				if (result.length() > 0) result.append('\n');
				result.append(entry.getFilename()).append('/').append(entry.getAttrs().getSize())
					.append('/').append(entry.getAttrs().isDir());
			}

			return result.toString();
		} catch (Exception ex) {
			lastError = ex.getMessage();
			return "";
		}
	}

	/**
	 * 读取远程目录的 SFTP 条目。
	 *
	 * @param path 文件或目录路径
	 * @return 远程目录条目
	 * @throws SftpException 服务器拒绝 SFTP 操作或远程路径无效时
	 */
	private Vector<ChannelSftp.LsEntry> channelEntries(String path) throws SftpException {
		return channel.ls(quote(path));
	}

	@Override
	public synchronized boolean mutate(String path, String name, String newName, int mode) {
		lastError = "";

		try {
			checkName(name);
			if (mode == 1) checkName(newName);
			String parent = directory(path);
			String target = quote(join(parent, name));

			switch (mode) {
			case 0: channel.mkdir(target); break;
			case 1: channel.rename(target, quote(join(parent, newName))); break;
			case 2: channel.rmdir(target); break;
			case 3: channel.rm(target); break;
			default: throw new IOException("未知文件操作。");
			}

			return true;
		} catch (Exception ex) {
			lastError = ex.getMessage();
			return false;
		}
	}

	@Override
	public synchronized boolean upload(File localDirectory, String name, String path) {
		lastError = "";

		try {
			checkName(name);
			if (localDirectory == null) throw new IOException("本地目录无效。");
			if (!connected() && !connect()) return false;

			uploadEntry(new File(localDirectory, name), join(directory(path), name), new HashSet<String>());
			return true;
		} catch (Exception ex) {
			lastError = ex.getMessage();
			return false;
		}
	}

	/**
	 * 递归上传文件或目录，并防止重复进入同一目录。
	 *
	 * @param local 本地文件
	 * @param remote 远程文件路径
	 * @param ancestors 当前递归链已访问的目录集合
	 * @throws IOException 访问文件、流或网络资源失败时
	 * @throws SftpException 服务器拒绝 SFTP 操作或远程路径无效时
	 */
	private void uploadEntry(File local, String remote, Set<String> ancestors) throws IOException, SftpException {
		checkName(local.getName());
		if (local.isFile()) {
			FileInputStream input = new FileInputStream(local);

			try {
				channel.put(input, quote(remote), ChannelSftp.OVERWRITE);
			} finally {
				input.close();
			}

			return;
		}

		// 只跟踪当前递归链的规范路径，阻止目录链接形成循环。
		if (!local.isDirectory() || !ancestors.add(local.getCanonicalPath())) {
			throw new IOException("本地目录无效或存在循环链接。");
		}

		try {
			SftpATTRS attrs;

			try {
				attrs = channel.stat(quote(remote));
			} catch (SftpException missing) {
				if (missing.id != ChannelSftp.SSH_FX_NO_SUCH_FILE) throw missing;

				channel.mkdir(quote(remote));
				attrs = channel.stat(quote(remote));
			}

			if (!attrs.isDir()) throw new IOException("远程同名目标不是目录。");

			File[] children = local.listFiles();
			if (children == null) throw new IOException("无法读取本地目录。");

			for (File child : children) {
				if (!child.isHidden()) uploadEntry(child, join(remote, child.getName()), ancestors);
			}
		} finally {
			ancestors.remove(local.getCanonicalPath());
		}
	}

	@Override
	public synchronized boolean download(String path, String name, File localDirectory) {
		lastError = "";

		try {
			checkName(name);
			if (localDirectory == null) throw new IOException("本地目录无效。");
			if (!connected() && !connect()) return false;

			downloadEntry(join(directory(path), name), name, localDirectory, 0);
			return true;
		} catch (Exception ex) {
			lastError = ex.getMessage();
			return false;
		}
	}

	/**
	 * 递归下载远程条目，并限制不安全路径和递归深度。
	 *
	 * @param remote 远程文件路径
	 * @param name 要操作的名称
	 * @param parent 父目录或父容器
	 * @param depth 当前递归深度
	 * @throws IOException 访问文件、流或网络资源失败时
	 * @throws SftpException 服务器拒绝 SFTP 操作或远程路径无效时
	 */
	private void downloadEntry(String remote, String name, File parent, int depth) throws IOException, SftpException {
		checkName(name);
		if (depth > 128) throw new IOException("远程目录超过递归深度限制。");

		// 读取链接本身的属性并拒绝跟随，避免递归下载越出预期目录。
		SftpATTRS attrs = channel.lstat(quote(remote));
		if (attrs.isLink()) throw new IOException("不跟随远程符号链接。");
		if (!parent.isDirectory() && !parent.mkdirs()) throw new IOException("无法创建本地目录。");

		File target = new File(parent, name);

		// 目标规范路径必须仍直属当前下载目录，不能借已有链接绕过边界。
		if (!target.getCanonicalFile().getParentFile().equals(parent.getCanonicalFile())) {
			throw new IOException("本地目标链接超出下载目录。");
		}

		if (attrs.isReg()) {
			FileOutputStream output = new FileOutputStream(target);

			try {
				channel.get(quote(remote), output);
			} finally {
				output.close();
			}
		} else if (attrs.isDir()) {
			if (!target.isDirectory() && !target.mkdirs()) throw new IOException("无法创建本地目录。");

			for (ChannelSftp.LsEntry entry : channelEntries(remote)) {
				if (!dot(entry.getFilename())) {
					downloadEntry(join(remote, entry.getFilename()), entry.getFilename(), target, depth + 1);
				}
			}
		} else {
			throw new IOException("不支持该远程文件类型。");
		}
	}

	@Override
	public synchronized void disconnect() {
		closeConnection();
		host = null;
		username = null;
		password = null;
	}

	/**
	 * 关闭底层通道及会话。
	 */
	private void closeConnection() {
		if (channel != null) channel.disconnect();
		if (connection != null) connection.disconnect();
		channel = null;
		connection = null;
	}

	@Override
	public synchronized String error() {
		return lastError == null ? "SFTP 操作失败。" : lastError;
	}

	/**
	 * 判断文本是否非空引用且不包含协议换行符。
	 *
	 * @param value 待检查的协议参数文本。
	 * @return 文本非空引用且不包含协议换行符时为 {@code true}，否则为 {@code false}
	 */
	private static boolean text(String value) {
		return value != null && value.indexOf('\r') < 0 && value.indexOf('\n') < 0;
	}

	/**
	 * 判断名称是否为当前目录或父目录标记。
	 *
	 * @param value 待检查的目录条目名称。
	 * @return 名称为当前目录或父目录标记时为 {@code true}，否则为 {@code false}
	 */
	private static boolean dot(String value) {
		return ".".equals(value) || "..".equals(value);
	}

	/**
	 * 拒绝不安全或不合法的单个文件名。
	 *
	 * @param value 待检查的单级文件名。
	 * @throws IOException 访问文件、流或网络资源失败时
	 */
	private static void checkName(String value) throws IOException {
		if (!text(value) || value.length() == 0 || dot(value) || value.indexOf('/') >= 0 || value.indexOf('\\') >= 0) {
			throw new IOException("文件或目录名称无效。");
		}
	}
}
