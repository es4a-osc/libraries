package simple.library.wsd.mqttclient;

import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.SimpleProperty;

/**
 * 一次发布操作的令牌及发送消息快照；发送完成事件不代表业务接收方已经处理。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class MQTT发送令牌 extends MQTT令牌 {

	/** 发布时复制的消息。 */
	private final MQTT消息 message;

	/**
	 * 建立发布关联信息。
	 *
	 * @param sequence 操作编号
	 * @param tag 关联标记
	 * @param topic 发布主题
	 * @param message 发布消息副本
	 */
	MQTT发送令牌(long sequence, String tag, String topic, MQTT消息 message) {
		super(sequence, "发布", tag, new String[] {topic});
		this.message = new MQTT消息(message.snapshot());
	}

	/* 属性 */

	/**
	 * 发布时的消息独立副本。
	 *
	 * @return 发布时的消息独立副本。
	 */
	@SimpleProperty
	public MQTT消息 消息() {
		return new MQTT消息(message.snapshot());
	}
}
