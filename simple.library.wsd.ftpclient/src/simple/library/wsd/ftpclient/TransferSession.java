package simple.library.wsd.ftpclient;

import java.io.File;

/**
 * FTP 与 SFTP 共用的会话操作；组件层统一负责串行调用及生命周期。
 */
interface TransferSession {
	boolean login(String host, int port, String username, String password);
	boolean connected();
	String workingDirectory();
	String list(String directory);
	boolean mutate(String directory, String name, String newName, int mode);
	boolean upload(File localDirectory, String name, String remoteDirectory);
	boolean download(String remoteDirectory, String name, File localDirectory);
	void disconnect();
	String error();
}
