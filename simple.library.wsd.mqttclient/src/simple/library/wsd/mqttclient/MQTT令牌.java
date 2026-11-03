package simple.library.wsd.mqttclient;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.SimpleProperty;

/**
 * 一次异步操作的结果。提交不表示成功；通过操作成功或操作失败事件读取完成结果，不在主线程等待。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public class MQTT令牌 {

	/** 本组件内唯一的操作编号。 */
	final long sequence;

	/** 操作名称。 */
	final String operation;

	/** 调用方关联标记。 */
	final String tag;

	/** 目标主题副本。 */
	String[] topics;

	/** 是否已完成。 */
	boolean done;

	/** 是否成功。 */
	boolean success;

	/** 服务端授权的 QoS。 */
	int[] granted = new int[0];

	/** 消息编号。 */
	int messageId;

	/** 失败时的错误快照。 */
	MQTT错误 error;

	/**
	 * 为一项操作建立关联令牌。
	 *
	 * @param sequence 操作编号
	 * @param operation 操作名称
	 * @param tag 关联标记
	 * @param topics 目标主题
	 */
	MQTT令牌(long sequence, String operation, String tag, String[] topics) {
		this.sequence = sequence;
		this.operation = operation;
		this.tag = tag == null ? "" : tag;
		this.topics = topics.clone();
	}

	/* 属性 */

	/**
	 * 本组件内唯一的操作编号。
	 *
	 * @return 本组件内唯一的操作编号。
	 */
	@SimpleProperty
	public long 操作编号() {
		return sequence;
	}

	/**
	 * 操作名称，例如连接、订阅、取消订阅、发布、断开或关闭。
	 *
	 * @return 操作名称，例如连接、订阅、取消订阅、发布、断开或关闭。
	 */
	@SimpleProperty
	public String 操作() {
		return operation;
	}

	/**
	 * 调用方提交的关联标记。
	 *
	 * @return 调用方提交的关联标记。
	 */
	@SimpleProperty
	public String 标记() {
		return tag;
	}

	/**
	 * 异步操作是否已经结束。
	 *
	 * @return 异步操作是否已经结束。
	 */
	@SimpleProperty
	public boolean 已完成() {
		return done;
	}

	/**
	 * 操作已完成且成功时为真。
	 *
	 * @return 操作已完成且成功时为真。
	 */
	@SimpleProperty
	public boolean 成功() {
		return done && success;
	}

	/**
	 * 协议消息编号，不适用或尚未分配时为 0。
	 *
	 * @return 协议消息编号，不适用或尚未分配时为 0。
	 */
	@SimpleProperty
	public int 消息编号() {
		return messageId;
	}

	/**
	 * 失败时的错误快照，尚未失败时为空。
	 *
	 * @return 失败时的错误快照，尚未失败时为空。
	 */
	@SimpleProperty
	public MQTT错误 错误() {
		return error;
	}

	/* 方法 */

	/**
	 * 返回本次操作的主题副本。
	 *
	 * @return 返回本次操作的主题副本。
	 */
	@SimpleFunction
	public String[] 取主题() {
		return topics.clone();
	}

	/**
	 * 返回订阅确认的实际 QoS，非订阅操作返回空数组；128 表示该项被拒绝。
	 *
	 * @return 返回订阅确认的实际 QoS，非订阅操作返回空数组；128 表示该项被拒绝。
	 */
	@SimpleFunction
	public int[] 取授权QoS() {
		return granted.clone();
	}
}
