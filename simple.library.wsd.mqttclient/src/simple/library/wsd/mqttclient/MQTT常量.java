package simple.library.wsd.mqttclient;

import simple.runtime.annotations.SimpleDataElement;
import simple.runtime.annotations.SimpleObject;

/**
 * MQTT 服务质量、协议版本和客户端状态常量。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class MQTT常量 {

	/**
	 * 常量类不创建实例。
	 */
	private MQTT常量() {
	}

	/**
	 * 最多传递一次，不提供送达确认。
	 */
	@SimpleDataElement
	public static final int QoS_最多一次 = 0;

	/**
	 * 至少传递一次，可能重复。
	 */
	@SimpleDataElement
	public static final int QoS_至少一次 = 1;

	/**
	 * 在协议参与方之间按仅一次语义传递，不代表业务只处理一次。
	 */
	@SimpleDataElement
	public static final int QoS_仅一次 = 2;

	/**
	 * 优先 MQTT 3.1.1，失败时按底层规则尝试 3.1。
	 */
	@SimpleDataElement
	public static final int 协议_自动 = 0;

	/**
	 * MQTT 3.1。
	 */
	@SimpleDataElement
	public static final int 协议_3_1 = 3;

	/**
	 * MQTT 3.1.1。
	 */
	@SimpleDataElement
	public static final int 协议_3_1_1 = 4;

	/**
	 * 尚未创建客户端。
	 */
	@SimpleDataElement
	public static final int 状态_未创建 = 0;

	/**
	 * 客户端存在但未连接。
	 */
	@SimpleDataElement
	public static final int 状态_已断开 = 1;

	/**
	 * 正在连接。
	 */
	@SimpleDataElement
	public static final int 状态_连接中 = 2;

	/**
	 * 已连接。
	 */
	@SimpleDataElement
	public static final int 状态_已连接 = 3;

	/**
	 * 正在断开。
	 */
	@SimpleDataElement
	public static final int 状态_断开中 = 4;

	/**
	 * 正在关闭并释放客户端。
	 */
	@SimpleDataElement
	public static final int 状态_关闭中 = 5;

	/**
	 * 组件已销毁。
	 */
	@SimpleDataElement
	public static final int 状态_已销毁 = 6;
}
