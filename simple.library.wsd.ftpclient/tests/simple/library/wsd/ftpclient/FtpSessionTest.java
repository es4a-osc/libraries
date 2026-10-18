package simple.library.wsd.ftpclient;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

/** 保留 FTP 行为的回归：目录、递归兄弟路径、失败传播和资源关闭。 */
public final class FtpSessionTest {
	private static int checks;
	private static void check(boolean value, String message) {
		checks++;
		if (!value) throw new AssertionError(message);
	}

	private static final class Fake extends FTPClient {
		boolean connected, failTransfer;
		String cwd = "/", failure;
		InputStream input;
		OutputStream output;
		int deletes, disconnects;
		Map<String, Boolean> files = new LinkedHashMap<String, Boolean>();
		Fake() { files.put("/", true); files.put("/data", true); }
		String path(String name) { return name.equals(".") ? cwd : name.startsWith("/") ? name : (cwd.equals("/") ? "/" : cwd + "/") + name; }
		@Override public void connect(String host, int port) { connected = true; cwd = "/"; }
		@Override public boolean isConnected() { return connected; }
		@Override public int getReplyCode() { return 226; }
		@Override public boolean login(String user, String password) { return true; }
		@Override public void setSoTimeout(int value) { }
		@Override public void enterLocalPassiveMode() { }
		@Override public boolean setFileType(int value) { return true; }
		@Override public void disconnect() { connected = false; disconnects++; }
		@Override public String printWorkingDirectory() { return cwd; }
		@Override public boolean changeWorkingDirectory(String name) {
			String next = path(name);
			if (!Boolean.TRUE.equals(files.get(next))) return false;
			cwd = next; return true;
		}
		@Override public boolean makeDirectory(String name) { files.put(path(name), true); return true; }
		@Override public boolean deleteFile(String name) { deletes++; return files.remove(path(name)) != null; }
		@Override public boolean removeDirectory(String name) { return deleteFile(name); }
		@Override public boolean rename(String old, String next) { Boolean type = files.remove(path(old)); if (type == null) return false; files.put(path(next), type); return true; }
		@Override public FTPFile[] listFiles() {
			List<FTPFile> entries = new ArrayList<FTPFile>();
			String prefix = cwd.equals("/") ? "/" : cwd + "/";
			for (Map.Entry<String, Boolean> entry : files.entrySet()) {
				if (!entry.getKey().startsWith(prefix)) continue;
				String name = entry.getKey().substring(prefix.length());
				if (name.length() == 0 || name.contains("/")) continue;
				FTPFile file = new FTPFile(); file.setName(name); file.setSize(1);
				file.setType(entry.getValue() ? FTPFile.DIRECTORY_TYPE : FTPFile.FILE_TYPE); entries.add(file);
			}
			return entries.toArray(new FTPFile[entries.size()]);
		}
		@Override public boolean storeFile(String name, InputStream value) throws IOException {
			input = value;
			if (failTransfer) throw new IOException("fixture");
			if (path(name).equals(failure)) return false;
			files.put(path(name), false); return true;
		}
		@Override public boolean retrieveFile(String name, OutputStream value) throws IOException {
			output = value;
			if (failTransfer) throw new IOException("fixture");
			if (path(name).equals(failure)) return false;
			value.write(1); return true;
		}
	}

	public static void main(String[] args) throws Exception {
		File root = new File(args[0]); root.mkdirs();
		Fake client = new Fake(); FtpSession ftp = new FtpSession(client);
		check(ftp.login("host", 21, "user", "password"), "FTP login");
		check(ftp.connected(), "FTP state");
		check(!ftp.mutate("/missing", "victim", null, 3) && client.deletes == 0, "no deletion from wrong directory");
		check(!ftp.mutate("/data", "../victim", null, 3), "name traversal rejected");
		check(ftp.mutate("/data", "dir", null, 0), "mkdir");
		check(ftp.mutate("/data", "dir", "renamed", 1), "rename");
		check(ftp.mutate("/data", "renamed", null, 2), "rmdir");
		check(ftp.workingDirectory().equals("/"), "cwd restored");
		new File(root, "tree/a").mkdirs(); new File(root, "tree/b").mkdirs();
		Files.write(new File(root, "tree/a/one").toPath(), new byte[] {1});
		Files.write(new File(root, "tree/b/two").toPath(), new byte[] {2});
		check(ftp.upload(root, "tree", "/data"), "recursive upload");
		check(client.files.containsKey("/data/tree/a/one") && client.files.containsKey("/data/tree/b/two"), "sibling upload paths");
		check(ftp.workingDirectory().equals("/"), "upload preserves cwd");
		check(ftp.list("/data/tree/a").equals("one/1/false"), "legacy list format");
		File downloads = new File(root, "downloads");
		check(ftp.download("/data", "tree", downloads), "recursive download");
		check(new File(downloads, "tree/a/one").length() == 1 && new File(downloads, "tree/b/two").length() == 1, "sibling download paths");
		client.failure = "/data/tree/a/one";
		check(!ftp.upload(root, "tree", "/data"), "upload child failure");
		check(!ftp.download("/data", "tree", downloads), "download child failure");
		check(ftp.workingDirectory().equals("/"), "failure preserves cwd");
		client.failTransfer = true;
		check(!ftp.upload(new File(root, "tree/a"), "one", "/data"), "upload exception false");
		boolean closed = false;
		try { client.input.read(); } catch (IOException expected) { closed = true; }
		check(closed, "input closed after failure");
		check(!ftp.download("/data/tree/a", "one", downloads), "download exception false");
		closed = false;
		try { client.output.write(2); } catch (IOException expected) { closed = true; }
		check(closed, "output closed after failure");
		check(ftp.login("host", 21, "user", "password") && client.disconnects == 1, "relogin closes old socket");
		ftp.disconnect(); ftp.disconnect();
		check(!ftp.upload(new File(root, "tree/a"), "one", "/data"), "disconnect clears credentials");
		System.out.println("FTP regression: " + checks + " checks passed");
	}
}
