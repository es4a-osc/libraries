package simple.library.wsd.xupdate;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * 替换版本检查过程；通过回调提交原始响应或已经解析的更新信息。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public interface 检查器 {

	/**
	 * 开始异步检查。
	 *
	 * @param address 检查地址。
	 * @param callback 本次检查的单次结果回调。
	 */
	@SimpleFunction
	void 检查(String address, 检查回调 callback);

	/**
	 * 取消本检查器持有的请求。
	 */
	@SimpleFunction
	void 取消();
}
