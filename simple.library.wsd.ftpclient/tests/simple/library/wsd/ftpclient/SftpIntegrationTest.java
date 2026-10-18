package simple.library.wsd.ftpclient;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import com.jcraft.jsch.Session;

/** 对临时服务器执行真实 SSH 握手和 SFTP 文件操作，参数由 Python 测试入口提供。 */
public final class SftpIntegrationTest {
	private static int checks;
	private static void check(boolean result, String message) {
		checks++;
		if (!result) throw new AssertionError(message);
	}

	public static void main(String[] args) throws Exception {
		String host = args[0], user = args[2], password = args[3], pin = args[4];
		int port = Integer.parseInt(args[1]);
		File temp = new File(args[5]);
		SftpSession blank = new SftpSession("");
		check(!blank.login(host, port, user, password), "missing pin rejected");
		check(blank.error().contains("指纹"), "missing pin reason");
		SftpSession malformed = new SftpSession("SHA256:bad");
		check(!malformed.login(host, port, user, password), "malformed pin rejected");
		SftpSession mismatch = new SftpSession("SHA256:AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
		check(!mismatch.login(host, port, user, password), "wrong host key rejected");
		check(mismatch.error().contains("不匹配") && !mismatch.connected(), "wrong pin closes connection");
		SftpHostTrustTest.MemoryStore remembered = new SftpHostTrustTest.MemoryStore();
		SftpHostTrustTest.Choice choice = new SftpHostTrustTest.Choice();
		SftpSession automatic = new SftpSession("", new SftpHostTrust(remembered, choice));
		check(!automatic.login(host, port, user, password), "first trust cancelled before login");
		check(remembered.values.isEmpty() && automatic.error().contains("取消"), "cancel leaves no trust");
		choice.accept = true;
		check(automatic.login(host, port, user, password), "confirmed host login");
		check(remembered.values.containsValue(pin), "actual negotiated public key saved");
		automatic.disconnect();
		int prompts = choice.prompts;
		choice.accept = false;
		automatic = new SftpSession("", new SftpHostTrust(remembered, choice));
		check(automatic.login(host, port, user, password) && choice.prompts == prompts, "saved key reused across sessions");
		automatic.disconnect();
		for (String key : remembered.values.keySet()) remembered.values.put(key, "SHA256:old-key");
		check(!automatic.login(host, port, user, password) && automatic.error().contains("已变化"), "changed remembered key refused");
		choice.accept = true;
		check(automatic.login(host, port, user, password), "changed key explicitly replaced");
		automatic.disconnect();
		SftpSession strict = new SftpSession("SHA256:AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", new SftpHostTrust(remembered, choice));
		prompts = choice.prompts;
		check(!strict.login(host, port, user, password) && prompts == choice.prompts, "explicit wrong pin cannot fall back to remembered trust");
		strict.disconnect();
		SftpSession session = new SftpSession(pin);
		try {
			check(!session.login(host, port, user, "incorrect"), "wrong password rejected");
			check(!session.connected(), "failed auth closes connection");
			check(session.login(host, port, user, password), "correct login: " + session.error());
			check(session.connected(), "connected");
			String home = session.workingDirectory();
			check("/".equals(home), "working directory");
			check(session.list("").equals("") && session.error().equals(""), "empty directory without error");
			check(session.mutate("/", "folder", null, 0), "mkdir");
			check(session.mutate("/", "folder", "renamed", 1), "rename directory");
			check(session.mutate("/", "renamed", null, 2), "rmdir");
			File local = new File(temp, "local");
			new File(local, "tree/a").mkdirs(); new File(local, "tree/b").mkdirs();
			byte[] content = "Simple 中文 SFTP\n".getBytes("UTF-8");
			Files.write(new File(local, "tree/a/one.bin").toPath(), content);
			Files.write(new File(local, "tree/b/two.bin").toPath(), new byte[] {0, 1, 2, -1});
			Files.write(new File(local, "one.bin").toPath(), content);
			check(session.upload(local, "one.bin", "/"), "file upload: " + session.error());
			check(session.list("/").contains("one.bin/" + content.length + "/false"), "legacy listing format");
			check(!session.mutate("/missing", "one.bin", null, 3), "wrong directory rejected");
			check(!session.mutate("/", "*", null, 3), "wildcard treated literally");
			check(session.list("/").contains("one.bin/"), "wildcard did not delete unrelated file");
			check(!session.mutate("/", "../one.bin", null, 3), "name traversal rejected");
			check(session.upload(local, "tree", "/"), "recursive upload: " + session.error());
			File downloaded = new File(temp, "downloads");
			check(session.download("/", "tree", downloaded), "recursive download: " + session.error());
			check(Arrays.equals(Files.readAllBytes(new File(downloaded, "tree/a/one.bin").toPath()), content), "download byte equality");
			check(Arrays.equals(Files.readAllBytes(new File(downloaded, "tree/b/two.bin").toPath()), new byte[] {0, 1, 2, -1}), "sibling byte equality");
			check(home.equals(session.workingDirectory()), "operations preserve working directory");
			check(!session.mutate("/", "tree", null, 2), "nonempty directory rejected");
			check(session.mutate("/", "one.bin", "renamed.bin", 1), "rename file");
			check(session.download("/", "renamed.bin", downloaded), "single file download");
			check(session.mutate("/", "renamed.bin", null, 3), "remove file");
			check(!session.download("/", "renamed.bin", downloaded), "missing file false");
			check(!session.download("/", "link", downloaded), "symlink download rejected");
			check(session.mutate("/", "dir[one]", null, 0), "literal bracket directory");
			check(session.upload(local, "one.bin", "/dir[one]"), "upload under bracket directory");
			check(session.list("/dir[one]").contains("one.bin/"), "list bracket directory");
			check(session.download("/dir[one]", "one.bin", downloaded), "download bracket directory");
			File failures = new File(local, "failures"); failures.mkdirs();
			Files.write(new File(failures, "blocked.bin").toPath(), content);
			Files.write(new File(failures, "ok.bin").toPath(), content);
			check(!session.upload(local, "failures", "/"), "child upload failure not masked");
			check(session.error().length() > 0, "child error retained");
			java.lang.reflect.Field connection = SftpSession.class.getDeclaredField("connection");
			connection.setAccessible(true); ((Session) connection.get(session)).disconnect();
			check(session.upload(local, "one.bin", "/"), "transfer reconnects with pinned key");
			check(session.login(host, port, user, password), "relogin closes and reopens");
			session.disconnect(); session.disconnect();
			check(!session.upload(local, "one.bin", "/"), "explicit disconnect clears reconnect credentials");
			check(!session.connected(), "disconnected");
		} finally {
			session.disconnect(); mismatch.disconnect(); malformed.disconnect(); blank.disconnect();
		}
		System.out.println("SFTP integration: " + checks + " checks passed");
	}
}
