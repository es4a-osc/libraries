package simple.library.wsd.xhttp2;

import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.SimpleProperty;

/**
 * 自定义解析事件的结果；事件内明确设置成功状态，未处理不会被视为成功。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class XHttp解析结果 {

	private boolean success;
	private boolean supplied;
	private int code;
	private String message = "";
	private String data = "";

	/**
	 * 取得业务是否成功。
	 *
	 * @return 业务是否成功。
	 */
	@SimpleProperty
	public synchronized boolean 成功() {
		return success;
	}

	/**
	 * 设置业务是否成功。
	 *
	 * @param value 业务是否成功。
	 */
	@SimpleProperty
	public synchronized void 成功(boolean value) {
		success = value;
		supplied = true;
	}

	/**
	 * 取得业务状态码。
	 *
	 * @return 业务状态码。
	 */
	@SimpleProperty
	public synchronized int 状态码() {
		return code;
	}

	/**
	 * 设置业务状态码。
	 *
	 * @param value 业务状态码。
	 */
	@SimpleProperty
	public synchronized void 状态码(int value) {
		code = value;
	}

	/**
	 * 取得业务消息或失败原因。
	 *
	 * @return 业务消息或失败原因。
	 */
	@SimpleProperty
	public synchronized String 消息() {
		return message;
	}

	/**
	 * 设置业务消息或失败原因。
	 *
	 * @param value 业务消息或失败原因。
	 */
	@SimpleProperty
	public synchronized void 消息(String value) {
		message = HttpSupport.text(value);
	}

	/**
	 * 取得解析后的文本或 JSON。
	 *
	 * @return 解析后的文本或 JSON。
	 */
	@SimpleProperty
	public synchronized String 数据() {
		return data;
	}

	/**
	 * 设置解析后的文本或 JSON。
	 *
	 * @param value 解析后的文本或 JSON。
	 */
	@SimpleProperty
	public synchronized void 数据(String value) {
		data = HttpSupport.text(value);
	}

	/**
	 * 事件返回后冻结结果，后续修改不影响已经交付的响应。
	 */
	synchronized XHttp解析结果 copy() {
		XHttp解析结果 result = new XHttp解析结果();
		result.success = success;
		result.supplied = supplied;
		result.code = code;
		result.message = message;
		result.data = data;
		return result;
	}

	synchronized boolean supplied() {
		return supplied;
	}
}
