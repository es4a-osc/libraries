package simple.library.wsd.mqttclient;

import simple.runtime.components.组件;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleEvent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.SimpleProperty;
import simple.runtime.annotations.UsesPermissions;

/**
 * 基于 Eclipse Paho 的 MQTT 3.1/3.1.1 非可视客户端。一个组件维护一个客户端，方法和事件均在主线程使用；连接不保证在应用退出或系统休眠后维持。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
@SimpleComponent
@UsesPermissions(permissionNames = "android.permission.INTERNET")
public interface MQTT客户端 extends 组件 {

	/* 属性 */

	/**
	 * 是否实际连接到服务器。
	 *
	 * @return 是否实际连接到服务器。
	 */
	@SimpleProperty
	boolean 已连接();

	/**
	 * 是否持有客户端；关闭完成后为假，可创建另一客户端。
	 *
	 * @return 是否持有客户端；关闭完成后为假，可创建另一客户端。
	 */
	@SimpleProperty
	boolean 已创建();

	/**
	 * 当前客户端状态，见 MQTT常量。
	 *
	 * @return 当前客户端状态，见 MQTT常量。
	 */
	@SimpleProperty
	int 状态();

	/**
	 * 创建客户端使用的服务器地址，未创建时为空。
	 *
	 * @return 创建客户端使用的服务器地址，未创建时为空。
	 */
	@SimpleProperty
	String 服务器地址();

	/**
	 * 实际使用的客户端 ID，未创建时为空。
	 *
	 * @return 实际使用的客户端 ID，未创建时为空。
	 */
	@SimpleProperty
	String 客户端ID();

	/**
	 * 当前离线缓冲消息条数，未配置缓冲时为 0。
	 *
	 * @return 当前离线缓冲消息条数，未配置缓冲时为 0。
	 */
	@SimpleProperty
	int 缓冲数量();

	/* 方法 */

	/**
	 * 异步连接。首次调用创建客户端；重复连接须使用相同地址、ID 和持久存储选项，切换客户端前先关闭。ID 留空时生成随机 ID，持久会话应明确指定稳定 ID。
	 *
	 * @param address tcp:// 或 ssl:// 地址
	 * @param clientId 客户端 ID，空文本生成随机 ID
	 * @param options 连接参数
	 * @param tag 关联标记
	 * @return 异步连接。首次调用创建客户端；重复连接须使用相同地址、ID 和持久存储选项，切换客户端前先关闭。ID 留空时生成随机 ID，持久会话应明确指定稳定 ID。
	 */
	@SimpleFunction
	MQTT令牌 连接(String address, String clientId, MQTT连接参数 options, String tag);

	/**
	 * 异步订阅主题过滤器；成功事件中的令牌提供服务端授权 QoS。
	 *
	 * @param topic 主题过滤器，允许 + 和 #
	 * @param qos 0、1 或 2
	 * @param tag 关联标记
	 * @return 异步订阅主题过滤器；成功事件中的令牌提供服务端授权 QoS。
	 */
	@SimpleFunction
	MQTT令牌 订阅(String topic, int qos, String tag);

	/**
	 * 异步订阅多个主题；主题和 QoS 数组长度须一致，拒绝项的授权 QoS 为 128。
	 *
	 * @param topics 非空主题过滤器数组
	 * @param qos 与主题一一对应的服务质量
	 * @param tag 关联标记
	 * @return 异步订阅多个主题；主题和 QoS 数组长度须一致，拒绝项的授权 QoS 为 128。
	 */
	@SimpleFunction
	MQTT令牌 批量订阅(String[] topics, int[] qos, String tag);

	/**
	 * 异步取消订阅，通过操作成功或失败事件确认实际结果。
	 *
	 * @param topic 主题过滤器
	 * @param tag 关联标记
	 * @return 异步取消订阅，通过操作成功或失败事件确认实际结果。
	 */
	@SimpleFunction
	MQTT令牌 取消订阅(String topic, String tag);

	/**
	 * 异步发布消息；离线缓冲启用且连接曾成功时可离线排队。发布完成不表示订阅者已经处理。
	 *
	 * @param topic 不含通配符的发布主题
	 * @param message 发送时复制内容和属性
	 * @param tag 关联标记
	 * @return 异步发布消息；离线缓冲启用且连接曾成功时可离线排队。发布完成不表示订阅者已经处理。
	 */
	@SimpleFunction
	MQTT发送令牌 发布(String topic, MQTT消息 message, String tag);

	/**
	 * 设置离线缓冲；队列非空时拒绝修改，避免静默丢弃消息。持久缓冲要求连接参数启用持久存储。
	 *
	 * @param options 缓冲选项
	 */
	@SimpleFunction
	void 设置离线缓冲(MQTT缓冲选项 options);

	/**
	 * 返回指定离线缓冲消息的独立副本，索引从 0 开始。
	 *
	 * @param index 缓冲索引
	 * @return 返回指定离线缓冲消息的独立副本，索引从 0 开始。
	 */
	@SimpleFunction
	MQTT消息 取缓冲消息(int index);

	/**
	 * 删除尚未发送的指定缓冲消息，对应发布令牌以失败结束。
	 *
	 * @param index 从 0 开始的缓冲索引
	 */
	@SimpleFunction
	void 删除缓冲消息(int index);

	/**
	 * 停止自动重连并异步正常断开，保留客户端供再次连接；等待时间为毫秒。
	 *
	 * @param quiesce 等待未完成消息的毫秒数，非负
	 * @param tag 关联标记
	 * @return 停止自动重连并异步正常断开，保留客户端供再次连接；等待时间为毫秒。
	 */
	@SimpleFunction
	MQTT令牌 断开(long quiesce, String tag);

	/**
	 * 停止自动重连并在后台强制断开；不发送断开报文时可能触发遗嘱，客户端可再次连接。
	 *
	 * @param quiesce 等待未完成消息的毫秒数，非负
	 * @param timeout 等待断开报文的毫秒数，非负
	 * @param sendPacket 是否尝试发送正常断开报文
	 * @param tag 关联标记
	 * @return 停止自动重连并在后台强制断开；不发送断开报文时可能触发遗嘱，客户端可再次连接。
	 */
	@SimpleFunction
	MQTT令牌 强制断开(long quiesce, long timeout, boolean sendPacket, String tag);

	/**
	 * 在后台停止重连、断开并释放客户端；关闭完成后可重新连接，组件销毁后不可使用。未完成发布将以失败结束。
	 *
	 * @param tag 关联标记
	 * @return 在后台停止重连、断开并释放客户端；关闭完成后可重新连接，组件销毁后不可使用。未完成发布将以失败结束。
	 */
	@SimpleFunction
	MQTT令牌 关闭(String tag);

	/* 事件 */

	/**
	 * 一次异步操作成功时触发；通过令牌的操作和标记区分请求。
	 *
	 * @param token 已完成的操作令牌
	 */
	@SimpleEvent
	void 操作成功(MQTT令牌 token);

	/**
	 * 一次异步操作失败时触发，令牌中包含错误代码及说明。
	 *
	 * @param token 失败的操作令牌
	 */
	@SimpleEvent
	void 操作失败(MQTT令牌 token);

	/**
	 * 初次连接或重连完成时触发；干净会话的订阅须由应用重新建立。
	 *
	 * @param reconnected 是否为连接曾成功后的再次连接
	 * @param server 实际连接地址
	 */
	@SimpleEvent
	void 连接完成(boolean reconnected, String server);

	/**
	 * 连接意外丢失时触发，开启自动重连后组件会继续尝试；主动断开不触发。
	 *
	 * @param error 连接丢失原因
	 */
	@SimpleEvent
	void 连接丢失(MQTT错误 error);

	/**
	 * 收到消息时触发；消息为独立副本，可在事件结束后保留。
	 *
	 * @param topic 消息主题
	 * @param message 独立消息副本
	 */
	@SimpleEvent
	void 收到消息(String topic, MQTT消息 message);

	/**
	 * Paho 完成本条消息的交付流程时触发；QoS 0 不包含服务器确认。
	 *
	 * @param token 本次发布的关联令牌
	 */
	@SimpleEvent
	void 发送完成(MQTT发送令牌 token);
}
