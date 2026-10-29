package simple.library.wsd.xupdate;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * 替换版本检查的网络传输；请求配置以 JSON 对象文本传递，值为文本。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public interface 网络服务 {

	/**
	 * 发出异步请求；只提交响应，不执行更新判断。
	 *
	 * @param post 是否使用 POST，假表示 GET。
	 * @param json POST 是否使用 JSON 请求体。
	 * @param address 请求地址。
	 * @param parameters 参数 JSON 对象文本。
	 * @param headers 请求头 JSON 对象文本。
	 * @param callback 本次请求的结果回调。
	 */
	@SimpleFunction
	void 请求(boolean post, boolean json, String address, String parameters, String headers, 检查回调 callback);

	/**
	 * 取消本服务持有的检查请求。
	 */
	@SimpleFunction
	void 取消();
}
