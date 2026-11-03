package simple.library.wsd.mqttclient;

import org.eclipse.paho.client.mqttv3.MqttMessage;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.SimpleProperty;

/**
 * MQTT 消息的独立副本。内容是原始字节，可用于空消息或二进制；保留标志与重复标志分别表示不同语义。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class MQTT消息 {

	/** 独立消息数据。 */
	private final MqttMessage data;

	/**
	 * 创建内容为空、QoS 为 1、不保留的消息。
	 */
	public MQTT消息() {
		data = new MqttMessage();
	}

	/**
	 * 复制收到或发送的消息，不保留底层可变数组。
	 *
	 * @param source 原始消息
	 */
	MQTT消息(MqttMessage source) {
		data = new MessageSnapshot(source);
	}

	/**
	 * 为一次发送创建独立底层消息。
	 *
	 * @return 独立消息
	 */
	MqttMessage snapshot() {
		return new MQTT消息(data).data;
	}

	/** 完整复制只读的重复标志。 */
	private static final class MessageSnapshot extends MqttMessage {

		/**
		 * 复制底层消息。
		 *
		 * @param source 原始消息
		 */
		MessageSnapshot(MqttMessage source) {
			super(source.getPayload().clone());
			setQos(source.getQos());
			setRetained(source.isRetained());
			setDuplicate(source.isDuplicate());
			setId(source.getId());
		}
	}

	/* 属性 */

	/**
	 * 消息的原始字节，读取和设置均复制数组。
	 *
	 * @return 消息的原始字节，读取和设置均复制数组。
	 */
	@SimpleProperty
	public byte[] 内容() {
		return data.getPayload().clone();
	}

	/**
	 * 设置消息的原始字节，读取和设置均复制数组。
	 *
	 * @param value 要设置的值
	 */
	@SimpleProperty
	public void 内容(byte[] value) {
		data.setPayload(java.util.Objects.requireNonNull(value, "内容").clone());
	}

	/**
	 * 服务质量，取值 0、1 或 2。
	 *
	 * @return 服务质量，取值 0、1 或 2。
	 */
	@SimpleProperty
	public int QoS() {
		return data.getQos();
	}

	/**
	 * 设置服务质量，取值 0、1 或 2。
	 *
	 * @param value 要设置的值
	 */
	@SimpleProperty
	public void QoS(int value) {
		MqttMessage.validateQos(value);
		data.setQos(value);
	}

	/**
	 * 是否请求服务器保留最后一条消息；收到时表示本消息的保留标志。
	 *
	 * @return 是否请求服务器保留最后一条消息；收到时表示本消息的保留标志。
	 */
	@SimpleProperty
	public boolean 保留() {
		return data.isRetained();
	}

	/**
	 * 设置是否请求服务器保留最后一条消息；收到时表示本消息的保留标志。
	 *
	 * @param value 要设置的值
	 */
	@SimpleProperty
	public void 保留(boolean value) {
		data.setRetained(value);
	}

	/**
	 * 本消息是否被标记为重复传递。
	 *
	 * @return 本消息是否被标记为重复传递。
	 */
	@SimpleProperty
	public boolean 重复() {
		return data.isDuplicate();
	}

	/**
	 * 协议消息编号，未分配时为 0。
	 *
	 * @return 协议消息编号，未分配时为 0。
	 */
	@SimpleProperty
	public int 消息编号() {
		return data.getId();
	}

	/* 方法 */

	/**
	 * 使用 UTF-8 设置文本内容。
	 *
	 * @param text 消息文本，可为空文本
	 */
	@SimpleFunction
	public void 置文本(String text) {
		data.setPayload(java.util.Objects.requireNonNull(text, "文本").getBytes(java.nio.charset.StandardCharsets.UTF_8));
	}

	/**
	 * 按 UTF-8 读取内容；二进制消息应直接读取内容属性。
	 *
	 * @return 按 UTF-8 读取内容；二进制消息应直接读取内容属性。
	 */
	@SimpleFunction
	public String 取文本() {
		return new String(data.getPayload(), java.nio.charset.StandardCharsets.UTF_8);
	}

	/**
	 * 将消息内容清空为零字节。
	 */
	@SimpleFunction
	public void 清空内容() {
		data.clearPayload();
	}
}
