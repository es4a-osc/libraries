package simple.library.wsd.jsonpath;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * 将匹配节点的独立当前值转换为新 JSON 值。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public interface JSONPath映射器 {
	/**
	 * 转换一个节点；返回值须存在，写入 JSON null 应返回解析 null 字面量的结果。
	 *
	 * @param path    可再次用于查询的具体节点路径。
	 * @param current 当前值的独立快照。
	 * @return 新值的存在结果；不得为空引用或未匹配结果。
	 */
	@SimpleFunction
	JSONPath结果 转换(String path, JSONPath结果 current);
}
