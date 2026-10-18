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

	interface Store {
		String read(String key);
		boolean write(String key, String fingerprint);
	}

	interface Confirmation {
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

	static String fingerprint(byte[] publicKey) {
		return "SHA256:" + digest(publicKey);
	}

	private static String digest(byte[] value) {
		try {
			return Base64.getEncoder().withoutPadding().encodeToString(MessageDigest.getInstance("SHA-256").digest(value));
		} catch (NoSuchAlgorithmException impossible) {
			throw new IllegalStateException(impossible);
		}
	}

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

	String error() { return lastError; }
}
