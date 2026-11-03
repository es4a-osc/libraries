package simple.library.wsd.mqttclient;

import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.SimpleProperty;

/**
 * TLS 属性配置。默认使用系统信任证书并校验服务器主机名；自定义密钥库文件位于应用可访问路径，类型须由设备支持。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class MQTTTLS配置 {

	/** 提交时复制的 TLS 属性。 */
	private final java.util.Properties values = new java.util.Properties();

	/**
	 * 创建使用系统默认信任配置的 TLS 参数。
	 */
	public MQTTTLS配置() {
	}

	/**
	 * 复制底层属性，防止连接后被外部修改。
	 *
	 * @return 独立属性表
	 */
	java.util.Properties snapshot() {
		return (java.util.Properties) values.clone();
	}

	/* 属性 */

	/**
	 * TLS 协议，默认为 TLS。
	 *
	 * @return TLS 协议，默认为 TLS。
	 */
	@SimpleProperty
	public String 协议() {
		return values.getProperty("com.ibm.ssl.protocol", "TLS");
	}

	/**
	 * 设置TLS 协议，默认为 TLS。
	 *
	 * @param value 要设置的值
	 */
	@SimpleProperty
	public void 协议(String value) {
		if (value == null || value.isEmpty()) {
			values.remove("com.ibm.ssl.protocol");
		} else {
			values.setProperty("com.ibm.ssl.protocol", value);
		}
	}

	/**
	 * 客户端密钥库路径。
	 *
	 * @return 客户端密钥库路径。
	 */
	@SimpleProperty
	public String 密钥库路径() {
		return values.getProperty("com.ibm.ssl.keyStore", "");
	}

	/**
	 * 设置客户端密钥库路径。
	 *
	 * @param value 要设置的值
	 */
	@SimpleProperty
	public void 密钥库路径(String value) {
		if (value == null || value.isEmpty()) {
			values.remove("com.ibm.ssl.keyStore");
		} else {
			values.setProperty("com.ibm.ssl.keyStore", value);
		}
	}

	/**
	 * 客户端密钥库密码。
	 *
	 * @return 客户端密钥库密码。
	 */
	@SimpleProperty
	public String 密钥库密码() {
		return values.getProperty("com.ibm.ssl.keyStorePassword", "");
	}

	/**
	 * 设置客户端密钥库密码。
	 *
	 * @param value 要设置的值
	 */
	@SimpleProperty
	public void 密钥库密码(String value) {
		if (value == null || value.isEmpty()) {
			values.remove("com.ibm.ssl.keyStorePassword");
		} else {
			values.setProperty("com.ibm.ssl.keyStorePassword", value);
		}
	}

	/**
	 * 客户端密钥库类型，例如 PKCS12。
	 *
	 * @return 客户端密钥库类型，例如 PKCS12。
	 */
	@SimpleProperty
	public String 密钥库类型() {
		return values.getProperty("com.ibm.ssl.keyStoreType", "");
	}

	/**
	 * 设置客户端密钥库类型，例如 PKCS12。
	 *
	 * @param value 要设置的值
	 */
	@SimpleProperty
	public void 密钥库类型(String value) {
		if (value == null || value.isEmpty()) {
			values.remove("com.ibm.ssl.keyStoreType");
		} else {
			values.setProperty("com.ibm.ssl.keyStoreType", value);
		}
	}

	/**
	 * 受信任证书库路径。
	 *
	 * @return 受信任证书库路径。
	 */
	@SimpleProperty
	public String 信任库路径() {
		return values.getProperty("com.ibm.ssl.trustStore", "");
	}

	/**
	 * 设置受信任证书库路径。
	 *
	 * @param value 要设置的值
	 */
	@SimpleProperty
	public void 信任库路径(String value) {
		if (value == null || value.isEmpty()) {
			values.remove("com.ibm.ssl.trustStore");
		} else {
			values.setProperty("com.ibm.ssl.trustStore", value);
		}
	}

	/**
	 * 受信任证书库密码。
	 *
	 * @return 受信任证书库密码。
	 */
	@SimpleProperty
	public String 信任库密码() {
		return values.getProperty("com.ibm.ssl.trustStorePassword", "");
	}

	/**
	 * 设置受信任证书库密码。
	 *
	 * @param value 要设置的值
	 */
	@SimpleProperty
	public void 信任库密码(String value) {
		if (value == null || value.isEmpty()) {
			values.remove("com.ibm.ssl.trustStorePassword");
		} else {
			values.setProperty("com.ibm.ssl.trustStorePassword", value);
		}
	}

	/**
	 * 信任库类型，需由当前设备支持。
	 *
	 * @return 信任库类型，需由当前设备支持。
	 */
	@SimpleProperty
	public String 信任库类型() {
		return values.getProperty("com.ibm.ssl.trustStoreType", "");
	}

	/**
	 * 设置信任库类型，需由当前设备支持。
	 *
	 * @param value 要设置的值
	 */
	@SimpleProperty
	public void 信任库类型(String value) {
		if (value == null || value.isEmpty()) {
			values.remove("com.ibm.ssl.trustStoreType");
		} else {
			values.setProperty("com.ibm.ssl.trustStoreType", value);
		}
	}

	/**
	 * 以英文逗号分隔的密码套件名称，空文本使用系统默认。
	 *
	 * @return 以英文逗号分隔的密码套件名称，空文本使用系统默认。
	 */
	@SimpleProperty
	public String 密码套件() {
		return values.getProperty("com.ibm.ssl.enabledCipherSuites", "");
	}

	/**
	 * 设置以英文逗号分隔的密码套件名称，空文本使用系统默认。
	 *
	 * @param value 要设置的值
	 */
	@SimpleProperty
	public void 密码套件(String value) {
		if (value == null || value.isEmpty()) {
			values.remove("com.ibm.ssl.enabledCipherSuites");
		} else {
			values.setProperty("com.ibm.ssl.enabledCipherSuites", value);
		}
	}
}
