package simple.library.wsd.mqtt;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.SimpleEvent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.ManifestNodes;
import simple.runtime.annotations.UsesPermissions;
import simple.runtime.components.组件;

/**
 * 基于 Eclipse Paho 的 MQTT 连接、发布和订阅。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
@SimpleComponent
@UsesPermissions(permissionNames =
	// 访问网络套接字。
	"android.permission.INTERNET," +
	// 查询当前网络连接状态。
	"android.permission.ACCESS_NETWORK_STATE," +
	// 维持 MQTT 服务运行所需的唤醒锁。
	"android.permission.WAKE_LOCK," +
	// 兼容旧系统设备标识读取；拒绝授权时使用替代标识。
	"android.permission.READ_PHONE_STATE"
)
@ManifestNodes(applicationXml =
	// 注册 Paho 后台通讯服务，禁止外部应用访问。
	"<service" +
	" android:name=\"org.eclipse.paho.android.service.MqttService\"" +
	" android:exported=\"false\"" +
	" />"
)
public interface MQTT通讯 extends 组件 {

	/* 组件事件 */

	/**
	 * 连接成功时在主线程触发。
	 */
	@SimpleEvent
	void 连接成功();

	/**
	 * 连接地址无效、权限不足或连接失败时在主线程触发。
	 */
	@SimpleEvent
	void 连接失败();

	/**
	 * 连接丢失或主动断开完成时在主线程触发。自动重连成功会再次触发连接成功。
	 */
	@SimpleEvent
	void 连接断开();

	/**
	 * 代理确认订阅成功时在主线程触发。
	 */
	@SimpleEvent
	void 订阅成功();

	/**
	 * 无有效连接、订阅参数无效或代理拒绝时在主线程触发。
	 */
	@SimpleEvent
	void 订阅失败();

	/**
	 * 当前消息完成 Paho 交付流程时在主线程触发；不表示业务端已处理。
	 */
	@SimpleEvent
	void 发送完毕();

	/**
	 * 收到消息时在主线程返回主题、原始字节集和实际 QoS。
	 *
	 * @param topic 消息主题；订阅和取消订阅时可以使用代理支持的主题过滤器。
	 * @param payload 消息的原始字节数据。
	 * @param qos QoS 等级：0、1 或 2。
	 */
	@SimpleEvent
	void 收到消息(String topic, byte[] payload, int qos);

	/* 组件方法 */

	/**
	 * 异步连接服务器，默认不自动重连、使用干净会话，心跳间隔为 60 秒。
	 *
	 * @param address 服务器地址，包含协议、主机和端口。
	 * @param username 登录用户名，可为空文本。
	 * @param password 登录密码，可为空文本。
	 * @param clientId 唯一客户端标识，不得为空。
	 */
	@SimpleFunction
	void 连接服务器(String address, String username, String password, String clientId);

	/**
	 * 以无用户名、无密码方式异步连接，默认不自动重连、使用干净会话，心跳间隔为 60 秒。
	 *
	 * <p>仅适用于服务器允许匿名连接的情况。</p>
	 *
	 * @param address 服务器地址，包含协议、主机和端口。
	 * @param clientId 唯一客户端标识，不得为空。
	 */
	@SimpleFunction
	void 连接服务器(String address, String clientId);

	/**
	 * 异步连接，例如 tcp://192.168.1.5:1883；客户端 ID 必须唯一，心跳单位秒。已有连接、连接中或清理中再次调用会通知连接失败；断开完成后再连接。
	 *
	 * @param address 服务器地址，包含协议、主机和端口。
	 * @param username 登录用户名。
	 * @param password 登录密码。
	 * @param clientId MQTT 客户端标识，同一代理上的活动客户端须保持唯一。
	 * @param reconnect 是否启用断线自动重连。
	 * @param cleanSession 是否使用干净会话，清除代理保留的订阅和待投递状态。
	 * @param keepAlive 心跳间隔，单位秒，范围 0 至 65535。
	 */
	@SimpleFunction
	void 连接服务器(String address, String username, String password, String clientId, boolean reconnect, boolean cleanSession, int keepAlive);

	/**
	 * 兼容旧接口：有权限时尝试读取旧系统设备标识；受限时使用 ANDROID_ID 或本应用安装标识，不保证是 IMEI。
	 *
	 * @return 旧系统设备标识、ANDROID_ID 或本应用安装标识；不保证是 IMEI。
	 */
	@SimpleFunction
	String 取IMEI码();

	/**
	 * 返回当前是否已建立连接。
	 *
	 * @return 当前连接已经建立且组件可用时返回真。
	 */
	@SimpleFunction
	boolean 是否已连接();

	/**
	 * 按指定 QoS 发布字节集，不将本次消息保存为保留消息。
	 *
	 * @param topic 消息主题。
	 * @param payload 消息的原始字节数据。
	 * @param qos QoS 等级：0、1 或 2。
	 * @throws IllegalStateException 尚未连接或底层发送失败。
	 */
	@SimpleFunction
	void 发送消息(String topic, byte[] payload, int qos);

	/**
	 * 以 QoS 0 发布消息且不保留；QoS 0 不提供送达确认。
	 *
	 * @param topic 消息主题。
	 * @param payload 消息原始字节。
	 */
	@SimpleFunction
	void 发送消息(String topic, byte[] payload);

	/**
	 * 向主题发布字节集；QoS 为 0、1、2，保留标记由代理处理。发送失败抛出异常。
	 *
	 * @param topic 消息主题；订阅和取消订阅时可以使用代理支持的主题过滤器。
	 * @param payload 消息的原始字节数据。
	 * @param qos QoS 等级：0、1 或 2。
	 * @param retained 是否将消息作为保留消息保存到代理。
	 * @throws IllegalStateException 尚未连接或底层操作失败。
	 */
	@SimpleFunction
	void 发送消息(String topic, byte[] payload, int qos, boolean retained);

	/**
	 * 请求以 QoS 0 订阅主题，通过原有成功或失败事件通知结果。
	 *
	 * @param topic 主题或主题过滤器。
	 */
	@SimpleFunction
	void 订阅消息(String topic);

	/**
	 * 订阅主题或主题过滤器，QoS 为 0、1、2；通过订阅成功或订阅失败通知结果。
	 *
	 * @param topic 消息主题；订阅和取消订阅时可以使用代理支持的主题过滤器。
	 * @param qos QoS 等级：0、1 或 2。
	 */
	@SimpleFunction
	void 订阅消息(String topic, int qos);

	/**
	 * 取消指定主题过滤器；失败抛出异常。
	 *
	 * @param topic 消息主题；订阅和取消订阅时可以使用代理支持的主题过滤器。
	 * @throws IllegalStateException 尚未连接或底层操作失败。
	 */
	@SimpleFunction
	void 取消订阅(String topic);

	/**
	 * 断开当前连接，之后可重新连接。
	 */
	@SimpleFunction
	void 断开连接();

	/**
	 * 永久关闭本组件并释放资源；之后不能再次连接，需要新组件。
	 */
	@SimpleFunction
	void 关闭服务();
}
