package simple.library.wsd.wsclient;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleEvent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.UsesPermissions;
import simple.runtime.components.组件;

/**
 * 支持 ws:// 和 wss:// 的不可视 WebSocket 文本客户端。
 * 一个组件维护一条连接，网络事件在 Android 主线程派发。
 */
@SimpleObject
@SimpleComponent
@UsesPermissions(permissionNames =
	// 访问网络套接字。
	"android.permission.INTERNET"
)
public interface WS客户端 extends 组件 {

	/* 组件事件 */

	/**
	 * WebSocket 握手成功后触发，此时可以发送文本。
	 */
	@SimpleEvent
	void 连接成功();

	/**
	 * 连接失败、网络故障、无有效连接或发送被拒绝时触发。
	 *
	 * @param message 失败原因，包括系统网络策略或服务端握手错误。
	 */
	@SimpleEvent
	void 出现错误(String message);

	/**
	 * 收到服务器的文本消息时触发，二进制消息不派发此事件。
	 *
	 * @param message 收到的完整文本消息。
	 */
	@SimpleEvent
	void 收到数据(String message);

	/**
	 * 正常关闭握手完成后触发；网络故障通过出现错误事件通知。
	 */
	@SimpleEvent
	void 连接断开();

	/* 组件方法 */

	/**
	 * 为之后发起的连接添加请求头，同名请求头可以重复添加。
	 *
	 * @param name 请求头名称，如 Authorization。
	 * @param value 请求头对应的文本值。
	 */
	@SimpleFunction
	void 添加协议头(String name, String value);

	/**
	 * 删除之后发起的连接中指定名称的全部请求头，不影响已经建立的连接。
	 *
	 * @param name 请求头名称，如 Authorization。
	 */
	@SimpleFunction
	void 删除协议头(String name);

	/**
	 * 异步连接服务器，成功后触发连接成功事件。
	 * 再次调用会取消旧连接，旧连接的迟到回调不再派发。
	 *
	 * @param url 以 ws:// 或 wss:// 开头的服务器地址。
	 */
	@SimpleFunction
	void 打开连接(String url);

	/**
	 * 将文本加入发送队列；应在连接成功事件后调用。
	 * 返回不表示服务器已经收到消息；发送被拒绝时触发出现错误事件。
	 *
	 * @param message 要发送的完整文本消息。
	 */
	@SimpleFunction
	void 发送数据(String message);

	/**
	 * 发起正常关闭握手；连接尚在建立时同样有效，无连接时不执行操作。
	 */
	@SimpleFunction
	void 关闭连接();
}
