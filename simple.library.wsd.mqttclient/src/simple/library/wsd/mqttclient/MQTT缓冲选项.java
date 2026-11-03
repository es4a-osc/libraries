package simple.library.wsd.mqttclient;

import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;

import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.SimpleProperty;

/**
 * 离线发布缓冲配置。连接至少成功过一次后使用；持久缓冲需要客户端启用持久存储。补发可能重复，业务应去重。改变配置前须清空队列。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class MQTT缓冲选项 {

	/** 离线缓冲配置。 */
	private final DisconnectedBufferOptions data = new DisconnectedBufferOptions();

	/**
	 * 创建默认关闭、容量为 100 条、不持久化且不淘汰旧消息的缓冲配置。
	 */
	public MQTT缓冲选项() {
	}

	/**
	 * 创建提交给客户端的独立配置。
	 *
	 * @return 配置副本
	 */
	DisconnectedBufferOptions snapshot() {
		DisconnectedBufferOptions copy = new DisconnectedBufferOptions();
		copy.setBufferEnabled(data.isBufferEnabled());
		copy.setBufferSize(data.getBufferSize());
		copy.setPersistBuffer(data.isPersistBuffer());
		copy.setDeleteOldestMessages(data.isDeleteOldestMessages());
		return copy;
	}

	/* 属性 */

	/**
	 * 是否缓冲离线期间的发布。
	 *
	 * @return 是否缓冲离线期间的发布。
	 */
	@SimpleProperty
	public boolean 启用() {
		return data.isBufferEnabled();
	}

	/**
	 * 设置是否缓冲离线期间的发布。
	 *
	 * @param value 要设置的值
	 */
	@SimpleProperty
	public void 启用(boolean value) {
		data.setBufferEnabled(value);
	}

	/**
	 * 最多缓冲的消息条数，必须大于 0。
	 *
	 * @return 最多缓冲的消息条数，必须大于 0。
	 */
	@SimpleProperty
	public int 容量() {
		return data.getBufferSize();
	}

	/**
	 * 设置最多缓冲的消息条数，必须大于 0。
	 *
	 * @param value 要设置的值
	 */
	@SimpleProperty
	public void 容量(int value) {
		data.setBufferSize(value);
	}

	/**
	 * 是否将缓冲消息保存在应用私有目录。
	 *
	 * @return 是否将缓冲消息保存在应用私有目录。
	 */
	@SimpleProperty
	public boolean 持久化() {
		return data.isPersistBuffer();
	}

	/**
	 * 设置是否将缓冲消息保存在应用私有目录。
	 *
	 * @param value 要设置的值
	 */
	@SimpleProperty
	public void 持久化(boolean value) {
		data.setPersistBuffer(value);
	}

	/**
	 * 队列已满时是否移除最旧消息；关闭时本次发布失败。
	 *
	 * @return 队列已满时是否移除最旧消息；关闭时本次发布失败。
	 */
	@SimpleProperty
	public boolean 淘汰最旧消息() {
		return data.isDeleteOldestMessages();
	}

	/**
	 * 设置队列已满时是否移除最旧消息；关闭时本次发布失败。
	 *
	 * @param value 要设置的值
	 */
	@SimpleProperty
	public void 淘汰最旧消息(boolean value) {
		data.setDeleteOldestMessages(value);
	}
}
