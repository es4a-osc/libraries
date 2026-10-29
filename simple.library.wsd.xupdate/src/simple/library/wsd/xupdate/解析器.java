package simple.library.wsd.xupdate;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * 将业务协议转换为更新信息；自定义解析结果自行决定是否存在更新。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public interface 解析器 {

	/**
	 * 解析服务器响应。
	 *
	 * @param response 原始响应文本。
	 * @return 解析后的独立更新信息。
	 */
	@SimpleFunction
	更新信息 解析(String response);
}
