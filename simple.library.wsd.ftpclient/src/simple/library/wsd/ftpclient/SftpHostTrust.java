package simple.library.wsd.ftpclient;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Locale;

/**
 * 首次及变更公钥必须经用户确认；已保存的信任按主机与端口隔离。
 */
final class SftpHostTrust {

	/**
	 * 服务器公钥指纹的持久化访问接口。
	 */
	interface Store {

		/**
		 * 读取指定主机已保存的公钥指纹。
		 *
		 * @param key 主机公钥或记录键
		 * @return 读取到的数据
		 */
		String read(String key);

		/**
		 * 保存指定主机已经确认的公钥指纹。
		 *
		 * @param key 主机公钥或记录键
		 * @param fingerprint 服务器公钥指纹
		 * @return 操作成功时为 {@code true}，失败时为 {@code false}
		 */
		boolean write(String key, String fingerprint);
	}

	/**
	 * 向用户请求确认服务器公钥的接口。
	 */
	interface Confirmation {

		/**
		 * 请求用户确认服务器身份。
		 *
		 * @param host 服务器地址
		 * @param port 端口号
		 * @param algorithm 公钥算法名称
		 * @param fingerprint 服务器公钥指纹
		 * @param previous 之前保存的服务器指纹
		 * @return 操作成功时为 {@code true}，失败时为 {@code false}
		 */
		boolean confirm(String host, int port, String algorithm, String fingerprint, String previous);
	}

	private static final Object SAVE_LOCK = new Object();
	private final Store store;
	private final Confirmation confirmation;
	private String lastError = "";

	SftpHostTrust(Store store, Confirmation confirmation) {
		this.store = store;
		this.confirmation = confirmation;
	}

	/**
	 * 计算服务器公钥的 SHA-256 指纹。
	 *
	 * @param publicKey 服务器公钥字节
	 * @return 带 SHA256 前缀的服务器公钥指纹
	 */
	static String fingerprint(byte[] publicKey) {
		return "SHA256:" + digest(publicKey);
	}

	/**
	 * 计算 SHA-256 摘要并编码为不带填充的 Base64 文本。
	 *
	 * @param value 计算摘要的原始字节数据。
	 * @return SHA-256 摘要的无填充 Base64 文本
	 */
	private static String digest(byte[] value) {
		try {
			return Base64.getEncoder().withoutPadding().encodeToString(MessageDigest.getInstance("SHA-256").digest(value));
		} catch (NoSuchAlgorithmException impossible) {
			throw new IllegalStateException(impossible);
		}
	}

	/**
	 * 校验服务器公钥与已保存或已确认的信任记录。
	 *
	 * @param host 服务器地址
	 * @param port 端口号
	 * @param algorithm 公钥算法名称
	 * @param publicKey 服务器公钥字节
	 * @return 校验通过时为 {@code true}，否则为 {@code false}
	 */
	boolean verify(String host, int port, String algorithm, byte[] publicKey) {
		lastError = "";
		String key = digest((host.trim().toLowerCase(Locale.ROOT) + ":" + port).getBytes(StandardCharsets.UTF_8));
		String actual = fingerprint(publicKey);

		try {
			String previous = store.read(key);
			if (previous == null) previous = "";
			if (actual.equals(previous)) return true;
			if (!confirmation.confirm(host, port, algorithm, actual, previous)) {
				lastError = previous.length() == 0 ? "尚未信任服务器公钥，已取消连接。" : "服务器公钥已变化，未确认更新信任，已拒绝连接。";
				return false;
			}

			// 用户确认期间信任记录可能被其他会话更新，保存前在锁内重新核对。
			synchronized (SAVE_LOCK) {
				String current = store.read(key);
				if (current == null) current = "";
				if (actual.equals(current)) return true;
				if (!previous.equals(current)) {
					lastError = "服务器信任记录已变化，请重新登录。";
					return false;
				}

				if (store.write(key, actual)) return true;
			}

			lastError = "无法保存服务器信任记录，已拒绝连接。";
		} catch (RuntimeException ex) {
			lastError = "服务器公钥确认或信任记录访问失败，已拒绝连接。";
		}

		return false;
	}

	/**
	 * 读取最近一次操作的错误信息。
	 *
	 * @return 最近一次操作的错误信息
	 */
	String error() {
		return lastError;
	}
}
