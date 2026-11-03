package simple.library.wsd.ftpclient;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.HostKey;
import com.jcraft.jsch.HostKeyRepository;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.UserInfo;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Vector;

/**
 * 独立的 SFTP 文件会话；密码认证严格校验固定指纹或已确认的服务器公钥。
 *
 * @author 树先生 xhwsd@qq.com
 */
final class SftpSession implements RemoteSession {
	volatile Session connection;
	volatile ChannelSftp channel;

	/**
	 * 建立未连接的 SFTP 会话。
	 */
	SftpSession() {}

	/**
	 * 复用已核对的固定指纹及首次确认验证器。
	 *
	 * @author 树先生 xhwsd@qq.com
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
				boolean matches =
						expected == null
								? trust.verify(
										this.host, port, new HostKey(host, key).getType(), key)
								: MessageDigest.isEqual(
										expected, MessageDigest.getInstance("SHA-256").digest(key));
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
		public void add(HostKey key, UserInfo userInfo) {}

		@Override
		public void remove(String host, String type) {}

		@Override
		public void remove(String host, String type, byte[] key) {}

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

	/**
	 * 建立并认证 SFTP 会话。
	 *
	 * @param host 主机名。
	 * @param port 端口。
	 * @param user 用户名。
	 * @param password 密码。
	 * @param fingerprint 固定指纹，空文本使用确认策略。
	 * @param trust 应用内信任策略。
	 * @param timeout 连接与读取超时毫秒。
	 * @throws Exception 连接、认证或服务器公钥校验失败。
	 */
	void connect(
			String host,
			int port,
			String user,
			String password,
			String fingerprint,
			SftpHostTrust trust,
			int timeout)
			throws Exception {
		disconnect();
		PinnedHostKey keys = new PinnedHostKey(fingerprint, trust, host, port);
		try {
			JSch jsch = new JSch();
			jsch.setHostKeyRepository(keys);
			Session created = jsch.getSession(user, host, port);
			connection = created;
			created.setConfig("StrictHostKeyChecking", "yes");
			created.setConfig("PreferredAuthentications", "password");
			created.setPassword(password.getBytes(StandardCharsets.UTF_8));
			created.setTimeout(timeout);
			created.connect(timeout);
			ChannelSftp opened = (ChannelSftp) created.openChannel("sftp");
			channel = opened;
			opened.connect(timeout);
		} catch (Exception error) {
			disconnect();
			throw new IOException(
					keys.rejected ? keys.rejectionReason : "SFTP 连接失败：" + error.getMessage(),
					error);
		}
	}

	/**
	 * 将路径作为字面值传给 JSch，避免通配符展开。
	 *
	 * @param value 远程路径。
	 * @return 转义后的路径。
	 */
	static String quote(String value) {
		StringBuilder result = new StringBuilder();
		for (int index = 0; index < value.length(); index++) {
			char c = value.charAt(index);
			if (c == '\\' || c == '*' || c == '?' || c == '[' || c == ']') {
				result.append('\\');
			}
			result.append(c);
		}
		return result.toString();
	}

	/**
	 * 取得已连接的 SFTP 通道。
	 *
	 * @return 有效通道。
	 * @throws IOException SFTP 通道尚未连接。
	 */
	private ChannelSftp ready() throws IOException {
		ChannelSftp result = channel;
		if (result == null || !result.isConnected()) {
			throw new IOException("请先连接 SFTP 服务器。");
		}
		return result;
	}

	@Override
	public boolean connected() {
		ChannelSftp value = channel;
		return value != null && value.isConnected();
	}

	@Override
	public void disconnect() {
		ChannelSftp value = channel;
		channel = null;
		if (value != null) {
			value.disconnect();
		}
		Session session = connection;
		connection = null;
		if (session != null) {
			session.disconnect();
		}
	}

	@Override
	public String pwd() throws Exception {
		return ready().pwd();
	}

	@Override
	public void cwd(String directory) throws Exception {
		ready().cd(quote(directory));
	}

	@Override
	public FTP远程文件[] list(String directory) throws Exception {
		ChannelSftp value = ready();
		String directoryPath = directory.isEmpty() ? value.pwd() : directory;
		Vector<ChannelSftp.LsEntry> entries = value.ls(quote(directoryPath));
		ArrayList<FTP远程文件> result = new ArrayList<>();
		for (ChannelSftp.LsEntry entry : entries) {
			if (entry.getFilename().equals(".") || entry.getFilename().equals("..")) {
				continue;
			}
			String link =
					entry.getAttrs().isLink()
							? value.readlink(quote(directoryPath + "/" + entry.getFilename()))
							: "";
			result.add(file(entry.getFilename(), entry.getAttrs(), link, entry.getLongname()));
		}
		return result.toArray(new FTP远程文件[0]);
	}

	@Override
	public FTP远程文件 stat(String file) throws Exception {
		ChannelSftp value = ready();
		SftpATTRS attrs = value.lstat(quote(file));
		return file(
				file.substring(file.lastIndexOf('/') + 1),
				attrs,
				attrs.isLink() ? value.readlink(quote(file)) : "",
				"");
	}

	@Override
	public void mutate(String operation, String source, String target) throws Exception {
		ChannelSftp value = ready();
		switch (operation) {
			case "mkdir":
				value.mkdir(quote(source));
				break;
			case "rmdir":
				value.rmdir(quote(source));
				break;
			case "delete":
				value.rm(quote(source));
				break;
			case "rename":
				value.rename(quote(source), quote(target));
				break;
			default:
				throw new IOException("未知远程操作。");
		}
	}

	@Override
	public InputStream read(String file, long offset) throws Exception {
		return ready().get(quote(file), null, offset);
	}

	@Override
	public OutputStream write(String file, long offset) throws Exception {
		ChannelSftp value = ready();
		if (offset == 0) {
			return value.put(quote(file), ChannelSftp.OVERWRITE);
		}
		if (value.lstat(quote(file)).getSize() != offset) {
			throw new IOException("SFTP 上传续传偏移须等于远程已有文件尺寸。");
		}
		return value.put(quote(file), null, ChannelSftp.RESUME, 0);
	}

	@Override
	public void finish() throws Exception {
		if (!connected()) {
			throw new IOException("SFTP 通道已断开。");
		}
	}

	/**
	 * 转换 SFTP 文件属性。
	 *
	 * @param name 条目名。
	 * @param attrs 服务器属性。
	 * @param link 链接目标。
	 * @param raw 原始记录。
	 * @return 独立条目快照。
	 */
	static FTP远程文件 file(String name, SftpATTRS attrs, String link, String raw) {
		int kind = attrs.isLink() ? 2 : attrs.isDir() ? 1 : attrs.isReg() ? 0 : 3;
		return new FTP远程文件(
				name,
				attrs.getSize(),
				Integer.toUnsignedLong(attrs.getMTime()) * 1000L,
				kind,
				Integer.toString(attrs.getUId()),
				Integer.toString(attrs.getGId()),
				link,
				raw);
	}
}
