package simple.library.wsd.jsonpath;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.collections.JSON值;

/**
 * 创建 JSONPath 文档和标准 JSON 值的静态入口。
 *
 * <p>仅操作内存数据，不联网、不读写文件。大文档和复杂过滤应由调用者放到后台线程。
 * 文档使用独立副本，不与传入的运行库 JSON值 共享修改状态。</p>
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class JSONPath操作 {

	/**
	 * 静态入口，不创建实例。
	 */
	private JSONPath操作() {
	}

	/* 文档创建：解析或复制输入，不共享调用方的可变数据。 */

	/**
	 * 严格解析 JSON 文本并创建可读写文档。
	 *
	 * @param json 完整 JSON 文本，可为对象、数组或标准标量。
	 * @return 独立文档，支持路径查询和修改。
	 * @throws IllegalArgumentException 文本为空引用或不是合法 JSON。
	 */
	@SimpleFunction
	public static JSONPath文档 解析文本(String json) {
		return new JSONPath文档(JSONPath支持.parse(json));
	}

	/**
	 * 从运行库 JSON值 复制数据，之后双方修改互不影响。
	 *
	 * @param value 运行库 JSON 值，不得为空引用。
	 * @return 独立的 JSONPath 文档。
	 * @throws IllegalArgumentException 参数无效、包含循环引用或非 JSON 数据。
	 */
	@SimpleFunction
	public static JSONPath文档 载入JSON值(JSON值 value) {
		if (value == null) {
			throw new IllegalArgumentException("JSON值不得为空引用。");
		}

		return new JSONPath文档(JSONPath支持.copy(value.getObject()));
	}

	/* 值创建：保留标量类型，供修改、新增和数组追加使用。 */

	/**
	 * 将 JSON 字面量解析为可用于修改或添加的值。
	 *
	 * @param json 完整 JSON 字面量，例如 10、true、null、引号包围的文本或对象数组。
	 * @return 保留原类型的值，JSON null 仍属于存在的结果。
	 * @throws IllegalArgumentException 文本不是合法 JSON。
	 */
	@SimpleFunction
	public static JSONPath结果 解析值(String json) {
		return new JSONPath结果(true, JSONPath支持.parse(json));
	}
}
