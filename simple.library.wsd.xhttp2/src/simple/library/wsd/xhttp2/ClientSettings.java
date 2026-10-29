package simple.library.wsd.xhttp2;

import java.util.ArrayList;
import java.util.List;

/**
 * 发送时复制的客户端默认配置；任务只读自己的快照，不访问后续变化的界面配置。
 *
 * @author 树先生 xhwsd@qq.com
 */
final class ClientSettings {

	String baseUrl = "";
	int timeout = 15000;
	int connectTimeout = -1;
	int readTimeout = -1;
	int writeTimeout = -1;
	int callTimeout;
	int retries;
	long retryDelay = 1000;
	long retryIncrease;
	int cacheMode;
	long cacheSeconds = 3600;
	long maxBytes = 8L * 1024 * 1024;
	boolean redirects = true;
	String proxyHost = "";
	int proxyPort;
	String proxyUser = "";
	String proxyPassword = "";
	byte[][] authorities = new byte[0][];
	boolean includeSystem = true;
	byte[] clientCertificate = new byte[0];
	char[] certificatePassword = new char[0];
	final List<String[]> headers = new ArrayList<String[]>();
	final List<String[]> query = new ArrayList<String[]>();

	ClientSettings copy() {
		ClientSettings result = new ClientSettings();
		result.baseUrl = baseUrl;
		result.timeout = timeout;
		result.connectTimeout = connectTimeout;
		result.readTimeout = readTimeout;
		result.writeTimeout = writeTimeout;
		result.callTimeout = callTimeout;
		result.retries = retries;
		result.retryDelay = retryDelay;
		result.retryIncrease = retryIncrease;
		result.cacheMode = cacheMode;
		result.cacheSeconds = cacheSeconds;
		result.maxBytes = maxBytes;
		result.redirects = redirects;
		result.proxyHost = proxyHost;
		result.proxyPort = proxyPort;
		result.proxyUser = proxyUser;
		result.proxyPassword = proxyPassword;
		result.authorities = new byte[authorities.length][];
		for (int index = 0; index < authorities.length; index++) {
			result.authorities[index] = authorities[index].clone();
		}
		result.includeSystem = includeSystem;
		result.clientCertificate = clientCertificate.clone();
		result.certificatePassword = certificatePassword.clone();
		HttpSupport.copy(headers, result.headers);
		HttpSupport.copy(query, result.query);
		return result;
	}
}
