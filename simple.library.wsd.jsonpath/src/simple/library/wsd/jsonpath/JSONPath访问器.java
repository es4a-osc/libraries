package simple.library.wsd.jsonpath;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * 逐项接收路径匹配结果，可停止后续匹配。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public interface JSONPath访问器 {
	/**
	 * 接收一次匹配；对象属性联合选择作为一个对象结果回调。
	 *
	 * @param index 从 0 开始的回调索引。
	 * @param path  可再次用于查询的具体路径或属性联合选择路径。
	 * @param value 本次结果的独立快照。
	 * @return 继续匹配时为真；返回假会停止，不撤销本次回调。
	 */
	@SimpleFunction
	boolean 访问(int index, String path, JSONPath结果 value);
}
