package simple.library.wsd.xhttp2;

import java.io.ByteArrayInputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;

/**
 * 仅构建当前客户端的严格 TLS 配置，主机名校验沿用新建 OkHttpClient 的默认实现。
 * 自定义 CA 可追加到系统信任或完全替换；客户端证书仅用于服务端请求的双向认证。
 *
 * @author 树先生 xhwsd@qq.com
 */
final class StrictTls {

	private StrictTls() {
	}

	static void apply(OkHttpClient.Builder builder, byte[][] authorities, boolean includeSystem,
			byte[] clientStore, char[] password) throws Exception {
		if (authorities.length == 0 && clientStore.length == 0) {
			if (!includeSystem) {
				throw new GeneralSecurityException("替换系统信任时至少需要一份 CA 证书。");
			}
			return;
		}
		final List<X509TrustManager> accepted = new ArrayList<X509TrustManager>();
		if (includeSystem) {
			accepted.add(manager(null));
		}
		if (authorities.length > 0) {
			KeyStore custom = KeyStore.getInstance(KeyStore.getDefaultType());
			custom.load(null);
			int count = 0;
			for (byte[] encoded : authorities) {
				for (Certificate certificate : CertificateFactory.getInstance("X.509")
						.generateCertificates(new ByteArrayInputStream(encoded))) {
					custom.setCertificateEntry("ca-" + count++, certificate);
				}
			}
			if (count == 0) {
				throw new CertificateException("没有有效的 CA 证书。");
			}
			accepted.add(manager(custom));
		}
		final X509TrustManager trust = new X509TrustManager() {
			@Override
			public X509Certificate[] getAcceptedIssuers() {
				List<X509Certificate> all = new ArrayList<X509Certificate>();
				for (X509TrustManager item : accepted) {
					Collections.addAll(all, item.getAcceptedIssuers());
				}
				return all.toArray(new X509Certificate[all.size()]);
			}

			@Override
			public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				verify(chain, authType, true);
			}

			@Override
			public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				verify(chain, authType, false);
			}

			private void verify(X509Certificate[] chain, String authType, boolean client) throws CertificateException {
				CertificateException last = new CertificateException("没有可接受的信任链。");
				for (X509TrustManager item : accepted) {
					try {
						if (client) {
							item.checkClientTrusted(chain, authType);
						} else {
							item.checkServerTrusted(chain, authType);
						}
						return;
					} catch (CertificateException failure) {
						last = failure;
					}
				}
				throw last;
			}
		};
		KeyManager[] keys = null;
		if (clientStore.length > 0) {
			KeyStore store = KeyStore.getInstance("PKCS12");
			store.load(new ByteArrayInputStream(clientStore), password);
			KeyManagerFactory factory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			factory.init(store, password);
			keys = factory.getKeyManagers();
		}
		SSLContext context = SSLContext.getInstance("TLS");
		context.init(keys, new TrustManager[] {trust}, new SecureRandom());
		builder.sslSocketFactory(context.getSocketFactory(), trust);
	}

	private static X509TrustManager manager(KeyStore store) throws GeneralSecurityException {
		TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		factory.init(store);
		for (TrustManager item : factory.getTrustManagers()) {
			if (item instanceof X509TrustManager) {
				return (X509TrustManager) item;
			}
		}
		throw new GeneralSecurityException("系统没有 X509 信任管理器。");
	}
}
