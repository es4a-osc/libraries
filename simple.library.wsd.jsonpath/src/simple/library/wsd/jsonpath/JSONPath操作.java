package simple.library.wsd.jsonpath;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleDataElement;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.collections.JSON值;

/**
 * 创建 JSONPath 文档和标准 JSON 值的静态入口。
 *
 * <p>
 * 仅操作内存数据，不联网、不读写文件。大文档和复杂过滤应由调用者放到后台线程。 文档使用独立副本，不与传入的运行库 JSON值 共享修改状态。
 * </p>
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class JSONPath操作 {

	/**
	 * 使用上游默认读取选项。
	 */
	@SimpleDataElement
	public static final int 选项_默认 = 0;

	/**
	 * 将缺失叶子属性作为 JSON null 返回。
	 */
	@SimpleDataElement
	public static final int 选项_缺失叶子为空 = 1;

	/**
	 * 将确定路径的结果也包装为数组。
	 */
	@SimpleDataElement
	public static final int 选项_总是数组 = 2;

	/**
	 * 返回可复用节点路径组成的数组，不与聚合函数一起使用。
	 */
	@SimpleDataElement
	public static final int 选项_返回路径 = 4;

	/**
	 * 抑制求值错误并返回默认 null 或空数组；不抑制无效表达式。
	 */
	@SimpleDataElement
	public static final int 选项_抑制求值错误 = 8;

	/**
	 * 要求不确定路径的各个目标都具有指定属性。
	 */
	@SimpleDataElement
	public static final int 选项_要求属性 = 16;

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

	/**
	 * 检查路径语法是否可以编译，不检查特定文档中是否有匹配。
	 *
	 * @param path JSONPath 表达式。
	 * @return 语法有效时为真；空引用、空白或无效语法为假。
	 */
	@SimpleFunction
	public static boolean 路径是否有效(String path) {
		try {
			JSONPath支持.path(path);
			return true;
		} catch (IllegalArgumentException invalid) {
			return false;
		}
	}

	/**
	 * 判断表达式是否为确定路径，不依赖当前文档内容。
	 *
	 * @param path JSONPath 表达式。
	 * @return 上游判定为确定路径时为真。
	 * @throws IllegalArgumentException 表达式无效。
	 */
	@SimpleFunction
	public static boolean 是否确定路径(String path) {
		return JSONPath支持.path(path).isDefinite();
	}
}
