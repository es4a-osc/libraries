package simple.library.wsd.jsonpath;

import java.util.List;
import java.util.Map;

import simple.runtime.annotations.SimpleDataElement;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.collections.JSON值;

/**
 * 路径查询的独立结果快照，区分未匹配与 JSON null。
 *
 * <p>确定路径返回其实际值；通配符、过滤等不确定路径返回数组。
 * 结果不随文档后续修改变化，也不会通过取成员或转换 JSON值 改写原文档。</p>
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class JSONPath结果 {

	/**
	 * 未找到目标路径。
	 */
	@SimpleDataElement
	public static final int 类型_未匹配 = -1;

	/**
	 * JSON null。
	 */
	@SimpleDataElement
	public static final int 类型_空 = 0;

	/**
	 * JSON 对象。
	 */
	@SimpleDataElement
	public static final int 类型_对象 = 1;

	/**
	 * JSON 数组。
	 */
	@SimpleDataElement
	public static final int 类型_数组 = 2;

	/**
	 * JSON 文本。
	 */
	@SimpleDataElement
	public static final int 类型_文本 = 3;

	/**
	 * JSON 数值。
	 */
	@SimpleDataElement
	public static final int 类型_数值 = 4;

	/**
	 * JSON 逻辑。
	 */
	@SimpleDataElement
	public static final int 类型_逻辑 = 5;

	/**
	 * 是否匹配到路径。
	 */
	private final boolean present;

	/**
	 * 独立结果数据；包内写入入口只可读取并复制，不得直接修改。
	 */
	final Object value;

	/**
	 * 深复制查询值，创建不随源文档变化的结果快照。
	 *
	 * @param present 是否有匹配。
	 * @param value JSON 标准值或结果数组；允许 Java 空引用表示 JSON null。
	 */
	JSONPath结果(boolean present, Object value) {
		this.present = present;
		this.value = JSONPath支持.copy(value);
	}

	/* 匹配状态与类型；未匹配不冒充 JSON null。 */

	/**
	 * 查询是否实际匹配；确定路径中的空数组仍算匹配。
	 *
	 * @return 匹配时为真；不确定查询结果为空数组时为假。
	 */
	@SimpleFunction
	public boolean 是否存在() {
		return present;
	}

	/**
	 * 返回结果类型。
	 *
	 * @return 本类类型常量之一；没有匹配时优先返回类型_未匹配。
	 */
	@SimpleFunction
	public int 取类型() {
		if (!present) {
			return 类型_未匹配;
		}
		if (value == null) {
			return 类型_空;
		}
		if (value instanceof Map) {
			return 类型_对象;
		}
		if (value instanceof List) {
			return 类型_数组;
		}
		if (value instanceof String) {
			return 类型_文本;
		}
		if (value instanceof Boolean) {
			return 类型_逻辑;
		}

		return 类型_数值;
	}

	/**
	 * 输出包含引号和转义的 JSON 文本，可再次解析。
	 *
	 * @return 当前值的紧凑 JSON 文本。
	 * @throws IllegalArgumentException 没有匹配结果。
	 */
	@SimpleFunction
	public String 到JSON文本() {
		requirePresent();

		return JSONPath支持.json(value);
	}

	/**
	 * 读取文本标量，不把数字或 null 隐式转成文本。
	 *
	 * @return 文本本身，不包含 JSON 引号。
	 * @throws IllegalArgumentException 没有匹配或不是文本。
	 */
	@SimpleFunction
	public String 取文本() {
		requirePresent();
		if (!(value instanceof String)) {
			throw new IllegalArgumentException("结果不是文本。");
		}

		return (String) value;
	}

	/**
	 * 读取整数值，拒绝截断小数和超出长整数范围的值。
	 *
	 * @return 精确的长整数。
	 * @throws IllegalArgumentException 没有匹配、不是数值或不能精确转换。
	 */
	@SimpleFunction
	public long 取长整数() {
		requirePresent();

		try {
			return JSONPath支持.number(value).longValueExact();
		} catch (ArithmeticException error) {
			throw JSONPath支持.failure("结果不能精确转换为长整数", error);
		}
	}

	/**
	 * 读取双精度小数，精度以 IEEE 754 双精度为限。
	 *
	 * @return 双精度数值。
	 * @throws IllegalArgumentException 没有匹配、不是数值或超出有限双精度范围。
	 */
	@SimpleFunction
	public double 取小数() {
		requirePresent();

		double result = JSONPath支持.number(value).doubleValue();
		if (Double.isInfinite(result)) {
			throw new IllegalArgumentException("结果超出双精度范围。");
		}

		return result;
	}

	/**
	 * 读取逻辑标量，不执行文本或数值转换。
	 *
	 * @return 逻辑值。
	 * @throws IllegalArgumentException 没有匹配或不是逻辑。
	 */
	@SimpleFunction
	public boolean 取逻辑() {
		requirePresent();
		if (!(value instanceof Boolean)) {
			throw new IllegalArgumentException("结果不是逻辑。");
		}

		return (Boolean) value;
	}

	/* 数组访问与运行库互通。 */

	/**
	 * 取得数组结果的成员数。
	 *
	 * @return 非负数组长度。
	 * @throws IllegalArgumentException 没有匹配或不是数组。
	 */
	@SimpleFunction
	public int 取成员数() {
		return array().size();
	}

	/**
	 * 取得数组成员的独立快照。
	 *
	 * @param index 从 0 开始的成员索引。
	 * @return 成员值，数组内的 null 仍算存在。
	 * @throws IllegalArgumentException 没有匹配或不是数组。
	 * @throws ArrayIndexOutOfBoundsException 索引越界。
	 */
	@SimpleFunction
	public JSONPath结果 取成员(int index) {
		List<?> items = array();
		if (index < 0 || index >= items.size()) {
			throw new ArrayIndexOutOfBoundsException(index);
		}

		return new JSONPath结果(true, items.get(index));
	}

	/**
	 * 将对象或数组转换为运行库 JSON值 的独立副本。
	 *
	 * @return 可使用现有运行库成员读写的 JSON值。
	 * @throws IllegalArgumentException 没有匹配或结果不是对象、数组。
	 */
	@SimpleFunction
	public JSON值 到JSON值() {
		requirePresent();
		if (!(value instanceof Map) && !(value instanceof List)) {
			throw new IllegalArgumentException("只有对象或数组可转换为运行库JSON值。");
		}

		return new JSON值(JSONPath支持.nativeValue(value));
	}

	/* 读取前检查：缺失和类型错误不隐式返回缺省值。 */

	/**
	 * 检查匹配状态，防止将未匹配当作 null 使用。
	 *
	 * @throws IllegalArgumentException 结果没有匹配节点。
	 */
	private void requirePresent() {
		if (!present) {
			throw new IllegalArgumentException("路径没有匹配结果。");
		}
	}

	/**
	 * 检查并取得数组模型。
	 *
	 * @return 内部数组，只供本类读取。
	 * @throws IllegalArgumentException 结果没有匹配节点或不是数组。
	 */
	private List<?> array() {
		requirePresent();
		if (!(value instanceof List)) {
			throw new IllegalArgumentException("结果不是数组。");
		}
		return (List<?>) value;
	}
}
