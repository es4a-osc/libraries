package simple.library.wsd.mqttclient;

import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.SimpleProperty;

/**
 * 异步操作或连接丢失的错误信息快照，不包含账号和密码。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class MQTT错误 {

	/** 错误代码；非 Paho 异常使用 -1。 */
	private final int code;

	/** 错误说明。 */
	private final String description;

	/** 最内层异常类型。 */
	private final String causeType;

	/**
	 * 复制可公开的错误信息。
	 *
	 * @param error 原始错误
	 */
	MQTT错误(Throwable error) {
		code = error instanceof org.eclipse.paho.client.mqttv3.MqttException
			? ((org.eclipse.paho.client.mqttv3.MqttException) error).getReasonCode() : -1;

		Throwable cause = error;
		while (cause != null && cause.getCause() != null && cause.getCause() != cause) {
			cause = cause.getCause();
		}
		causeType = cause == null ? "" : cause.getClass().getName();
		description = error == null ? "未知错误" : String.valueOf(error.getMessage())
			+ (cause != null && cause != error && cause.getMessage() != null ? "；" + cause.getMessage() : "");
	}

	/* 属性 */

	/**
	 * Paho 错误代码，非 Paho 异常为 -1。
	 *
	 * @return Paho 错误代码，非 Paho 异常为 -1。
	 */
	@SimpleProperty
	public int 代码() {
		return code;
	}

	/**
	 * 错误说明。
	 *
	 * @return 错误说明。
	 */
	@SimpleProperty
	public String 说明() {
		return description;
	}

	/**
	 * 底层异常类型名称。
	 *
	 * @return 底层异常类型名称。
	 */
	@SimpleProperty
	public String 原因类型() {
		return causeType;
	}
}
