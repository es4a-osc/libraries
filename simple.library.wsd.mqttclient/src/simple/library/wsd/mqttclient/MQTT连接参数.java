package simple.library.wsd.mqttclient;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.SimpleProperty;

/**
 * 连接参数。时间均按属性注明的单位传入；连接时复制参数，之后修改不影响已经提交的连接。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class MQTT连接参数 {

	/** 可编辑的连接参数。 */
	private final MqttConnectOptions data = new MqttConnectOptions();

	/** 是否使用应用私有文件持久化协议和离线消息，创建客户端时固定。 */
	private boolean persistent = true;

	/**
	 * 创建使用干净会话、不自动重连、15 秒连接超时的参数。
	 */
	public MQTT连接参数() {
		data.setConnectionTimeout(15);
	}

	/**
	 * 复制连接参数；重连调度由组件统一控制，保证可以取消。
	 *
	 * @return 独立连接参数
	 */
	MqttConnectOptions snapshot() {
		MqttConnectOptions copy = new MqttConnectOptions();
		copy.setUserName(data.getUserName());
		if (data.getPassword() != null) {
			copy.setPassword(data.getPassword().clone());
		}
		copy.setKeepAliveInterval(data.getKeepAliveInterval());
		copy.setConnectionTimeout(data.getConnectionTimeout());
		copy.setMaxInflight(data.getMaxInflight());
		copy.setCleanSession(data.isCleanSession());
		copy.setMqttVersion(data.getMqttVersion());
		copy.setHttpsHostnameVerificationEnabled(true);
		if (data.getSSLProperties() != null) {
			copy.setSSLProperties((java.util.Properties) data.getSSLProperties().clone());
		}
		if (data.getWillMessage() != null) {
			org.eclipse.paho.client.mqttv3.MqttMessage will = data.getWillMessage();
			copy.setWill(data.getWillDestination(), will.getPayload().clone(), will.getQos(), will.isRetained());
		}
		return copy;
	}

	/* 属性 */

	/**
	 * 登录账号，空文本表示未设置。
	 *
	 * @return 登录账号，空文本表示未设置。
	 */
	@SimpleProperty
	public String 账号() {
		return data.getUserName() == null ? "" : data.getUserName();
	}

	/**
	 * 设置登录账号，空文本表示未设置。
	 *
	 * @param value 要设置的值
	 */
	@SimpleProperty
	public void 账号(String value) {
		data.setUserName(value == null || value.isEmpty() ? null : value);
	}

	/* 方法 */

	/**
	 * 设置登录密码；不提供读取接口。
	 *
	 * @param password 服务器密码，空文本表示清空
	 */
	@SimpleFunction
	public void 设置密码(String password) {
		data.setPassword(password == null ? new char[0] : password.toCharArray());
	}

	/* 属性 */

	/**
	 * 心跳间隔，单位秒，范围 0 至 65535；0 表示不发送心跳。
	 *
	 * @return 心跳间隔，单位秒，范围 0 至 65535；0 表示不发送心跳。
	 */
	@SimpleProperty
	public int 心跳间隔() {
		return data.getKeepAliveInterval();
	}

	/**
	 * 设置心跳间隔，单位秒，范围 0 至 65535；0 表示不发送心跳。
	 *
	 * @param value 要设置的值
	 */
	@SimpleProperty
	public void 心跳间隔(int value) {
		data.setKeepAliveInterval(value);
	}

	/**
	 * 连接超时，单位秒，范围 1 至 300。
	 *
	 * @return 连接超时，单位秒，范围 1 至 300。
	 */
	@SimpleProperty
	public int 连接超时() {
		return data.getConnectionTimeout();
	}

	/**
	 * 设置连接超时，单位秒，范围 1 至 300。
	 *
	 * @param value 要设置的值
	 */
	@SimpleProperty
	public void 连接超时(int value) {
		if (value < 1 || value > 300) {
			throw new IllegalArgumentException("连接超时必须为 1 至 300 秒");
		}
		data.setConnectionTimeout(value);
	}

	/**
	 * 同时进行协议交付的消息数，必须大于 0。
	 *
	 * @return 同时进行协议交付的消息数，必须大于 0。
	 */
	@SimpleProperty
	public int 最大在途消息数() {
		return data.getMaxInflight();
	}

	/**
	 * 设置同时进行协议交付的消息数，必须大于 0。
	 *
	 * @param value 要设置的值
	 */
	@SimpleProperty
	public void 最大在途消息数(int value) {
		if (value <= 0) {
			throw new IllegalArgumentException("最大在途消息数须大于 0");
		}
		data.setMaxInflight(value);
	}

	/**
	 * 是否清理服务器保存的订阅和会话；持久会话须使用稳定且唯一的客户端 ID。
	 *
	 * @return 是否清理服务器保存的订阅和会话；持久会话须使用稳定且唯一的客户端 ID。
	 */
	@SimpleProperty
	public boolean 清理会话() {
		return data.isCleanSession();
	}

	/**
	 * 设置是否清理服务器保存的订阅和会话；持久会话须使用稳定且唯一的客户端 ID。
	 *
	 * @param value 要设置的值
	 */
	@SimpleProperty
	public void 清理会话(boolean value) {
		data.setCleanSession(value);
	}

	/**
	 * 连接曾经成功后意外断开时是否自动重连；主动断开或关闭会停止重连。
	 *
	 * @return 连接曾经成功后意外断开时是否自动重连；主动断开或关闭会停止重连。
	 */
	@SimpleProperty
	public boolean 自动重连() {
		return data.isAutomaticReconnect();
	}

	/**
	 * 设置连接曾经成功后意外断开时是否自动重连；主动断开或关闭会停止重连。
	 *
	 * @param value 要设置的值
	 */
	@SimpleProperty
	public void 自动重连(boolean value) {
		data.setAutomaticReconnect(value);
	}

	/**
	 * 协议版本，使用 MQTT常量中的协议值。
	 *
	 * @return 协议版本，使用 MQTT常量中的协议值。
	 */
	@SimpleProperty
	public int 协议版本() {
		return data.getMqttVersion();
	}

	/**
	 * 设置协议版本，使用 MQTT常量中的协议值。
	 *
	 * @param value 要设置的值
	 */
	@SimpleProperty
	public void 协议版本(int value) {
		data.setMqttVersion(value);
	}

	/**
	 * 是否在应用私有目录持久保存协议和离线数据；同一客户端关闭前不能改变。
	 *
	 * @return 是否在应用私有目录持久保存协议和离线数据；同一客户端关闭前不能改变。
	 */
	@SimpleProperty
	public boolean 持久存储() {
		return persistent;
	}

	/**
	 * 设置是否在应用私有目录持久保存协议和离线数据；同一客户端关闭前不能改变。
	 *
	 * @param value 要设置的值
	 */
	@SimpleProperty
	public void 持久存储(boolean value) {
		persistent = value;
	}

	/* 方法 */

	/**
	 * 设置异常掉线时由服务器发布的遗嘱消息；正常发送断开报文不会触发遗嘱。
	 *
	 * @param topic 不含通配符的发布主题
	 * @param message 遗嘱内容、QoS 和保留标志
	 */
	@SimpleFunction
	public void 设置遗嘱(String topic, MQTT消息 message) {
		data.setWill(topic, java.util.Objects.requireNonNull(message, "消息").内容(), message.QoS(), message.保留());
	}

	/**
	 * 使用独立副本设置 TLS 属性，服务器地址须使用 ssl://。
	 *
	 * @param settings TLS 配置
	 */
	@SimpleFunction
	public void 设置TLS(MQTTTLS配置 settings) {
		data.setSSLProperties(java.util.Objects.requireNonNull(settings, "TLS配置").snapshot());
	}
}
