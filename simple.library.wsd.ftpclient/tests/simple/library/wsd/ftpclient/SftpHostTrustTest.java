package simple.library.wsd.ftpclient;

import java.util.HashMap;
import java.util.Map;

/** 校验确认、持久信任及失败分支；网络握手由独立集成测试覆盖。 */
public final class SftpHostTrustTest {

	private static int checks;
	private static void check(boolean value, String message) {
		checks++;
		if (!value) throw new AssertionError(message);
	}

	static final class MemoryStore implements SftpHostTrust.Store {
		final Map<String, String> values = new HashMap<String, String>();
		boolean writable = true;
		@Override public String read(String key) { return values.get(key); }
		@Override public boolean write(String key, String pin) {
			if (!writable) return false;
			values.put(key, pin);
			return true;
		}
	}

	static final class Choice implements SftpHostTrust.Confirmation {
		boolean accept;
		int prompts;
		String previous;
		@Override public boolean confirm(String host, int port, String algorithm, String fingerprint, String old) {
			prompts++;
			previous = old;
			return accept;
		}
	}

	public static void main(String[] args) {
		MemoryStore store = new MemoryStore();
		Choice choice = new Choice();
		SftpHostTrust trust = new SftpHostTrust(store, choice);
		byte[] first = {1, 2, 3}, second = {4, 5, 6};
		check(!trust.verify("example.test", 22, "test", first), "first connection cancelled");
		check(choice.prompts == 1 && choice.previous.equals(""), "unknown key prompt");
		check(store.values.isEmpty(), "cancel does not save");
		check(trust.error().contains("取消"), "cancel reason");
		choice.accept = true;
		check(trust.verify("example.test", 22, "test", first), "first key accepted");
		check(store.values.size() == 1, "accepted key saved");
		check(trust.error().equals(""), "success clears error");
		choice.accept = false;
		check(trust.verify("EXAMPLE.TEST", 22, "test", first), "same host normalization");
		check(choice.prompts == 2, "remembered key does not prompt");
		check(!trust.verify("example.test", 22, "test", second), "changed key cancelled");
		check(choice.previous.equals(SftpHostTrust.fingerprint(first)), "changed prompt carries original pin");
		check(trust.error().contains("已变化"), "changed key reason");
		check(trust.verify("example.test", 22, "test", first), "cancel preserves old trust");
		check(!trust.verify("example.test", 2222, "test", first), "port isolation");
		check(choice.previous.equals(""), "new port is unknown");
		check(!trust.verify("other.test", 22, "test", first), "host isolation");
		choice.accept = true;
		check(trust.verify("example.test", 22, "test", second), "explicitly replace changed key");
		int prompted = choice.prompts;
		check(new SftpHostTrust(store, choice).verify("example.test", 22, "test", second), "new policy instance reads saved trust");
		check(choice.prompts == prompted, "recreated policy does not prompt");
		store.writable = false;
		check(!trust.verify("example.test", 22, "test", first), "save failure rejects connection");
		check(trust.error().contains("无法保存"), "save failure reason");
		check(trust.verify("example.test", 22, "test", second), "save failure preserves previous key");
		SftpHostTrust conflict = new SftpHostTrust(store, new SftpHostTrust.Confirmation() {
			@Override public boolean confirm(String host, int port, String algorithm, String fingerprint, String old) {
				for (String key : store.values.keySet()) store.values.put(key, "concurrent-change");
				return true;
			}
		});
		check(!conflict.verify("example.test", 22, "test", first), "concurrent change is not overwritten");
		check(conflict.error().contains("记录已变化"), "concurrent change reason");
		SftpHostTrust broken = new SftpHostTrust(new SftpHostTrust.Store() {
			@Override public String read(String key) { throw new IllegalStateException(); }
			@Override public boolean write(String key, String value) { throw new AssertionError(); }
		}, choice);
		check(!broken.verify("example.test", 22, "test", first), "read failure rejects");
		System.out.println("SFTP host trust: " + checks + " checks passed");
	}
}
