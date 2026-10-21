package simple.library.wsd.jsonpath;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.JsonPathException;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.PathNotFoundException;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.collections.JSON值;

/**
 * 同步执行 JSONPath 查询及事务式内存修改的文档对象。
 *
 * <p>通过 JSONPath操作 创建。一个文档及其调用应在同一线程串行使用。
 * 每次修改在独立副本上完成，成功后替换当前文档；失败不会留下部分修改。
 * 不自动创建路径中缺失的父对象，不执行脚本或访问网络。</p>
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class JSONPath文档 {

	/**
	 * 当前文档模型，只在成功修改后替换。
	 */
	private Object root;

	/**
	 * 保存标准 JSON 模型。
	 *
	 * @param root 由本库解析或复制得到的数据。
	 */
	JSONPath文档(Object root) {
		this.root = root;
	}

	/* 查询：缺失路径返回未匹配结果，错误表达式仍报告错误。 */

	/**
	 * 按路径查询，结果为独立快照。
	 *
	 * @param path JSONPath 表达式，根节点为 $。
	 * @return 确定路径的实际值或不确定路径的结果数组；缺失时是否存在为假。
	 * @throws IllegalArgumentException 表达式或查询操作无效。
	 */
	@SimpleFunction
	public JSONPath结果 查询(String path) {
		JsonPath compiled = JSONPath支持.path(path);

		// 上游拒绝 Java 空根；本库仍须区分合法 JSON null 和缺失路径。
		if (root == null) {
			return new JSONPath结果("$".equals(path.trim()), null);
		}

		try {
			Object value = compiled.read(root, JSONPath支持.CONFIG);
			boolean present = compiled.isDefinite() || !(value instanceof List) || !((List<?>) value).isEmpty();
			return new JSONPath结果(present, value);
		} catch (PathNotFoundException error) {
			return new JSONPath结果(false, null);
		} catch (JsonPathException error) {
			throw JSONPath支持.failure("JSONPath查询失败", error);
		}
	}

	/**
	 * 判断路径是否存在；匹配到 JSON null 或确定路径空数组仍为真。
	 *
	 * @param path JSONPath 表达式。
	 * @return 查询存在时为真。
	 * @throws IllegalArgumentException 表达式无效。
	 */
	@SimpleFunction
	public boolean 是否存在(String path) {
		return 查询(path).是否存在();
	}

	/**
	 * 取得匹配节点的具体路径，便于定位或逐项处理。
	 *
	 * @param path 节点选择表达式，不支持 length 等聚合函数。
	 * @return 具体路径数组，无匹配时为空数组。
	 * @throws IllegalArgumentException 表达式或路径模式无效。
	 */
	@SimpleFunction
	public String[] 查询路径(String path) {
		List<String> result = paths(root, path);
		return result.toArray(new String[result.size()]);
	}

	/**
	 * 序列化当前完整文档，包括修改结果。
	 *
	 * @return 紧凑 JSON 文本，不写入文件。
	 */
	@SimpleFunction
	public String 到JSON文本() {
		return JSONPath支持.json(root);
	}

	/**
	 * 复制当前文档，之后修改互不影响。
	 *
	 * @return 独立文档。
	 */
	@SimpleFunction
	public JSONPath文档 复制() {
		return new JSONPath文档(JSONPath支持.copy(root));
	}

	/**
	 * 取得整个文档对应的运行库 JSON值 副本。
	 *
	 * @return 独立的对象或数组。
	 * @throws IllegalArgumentException 文档根是标量，运行库不提供相应标量读取接口。
	 */
	@SimpleFunction
	public JSON值 到JSON值() {
		return new JSONPath结果(true, root).到JSON值();
	}

	/* 按路径修改；新增属性使用独立入口，不隐式生成中间层。 */

	/**
	 * 用指定 JSON 值替换匹配节点，支持通配符和过滤器批量选择。
	 *
	 * @param path 节点选择表达式。
	 * @param value 本库解析值或查询取得的存在结果；按值深复制。
	 * @return 实际匹配并处理的节点数，无匹配为 0。
	 * @throws IllegalArgumentException 参数、表达式或目标类型无效。
	 */
	@SimpleFunction
	public int 修改(String path, JSONPath结果 value) {
		return change(0, path, resultValue(value), null, null);
	}

	/**
	 * 用运行库 JSON值 的副本替换匹配节点。
	 *
	 * @param path 节点选择表达式。
	 * @param value 运行库 JSON 值，不得为空引用。
	 * @return 实际匹配并处理的节点数。
	 * @throws IllegalArgumentException 参数或表达式无效。
	 */
	@SimpleFunction
	public int 修改JSON值(String path, JSON值 value) {
		if (value == null) {
			throw new IllegalArgumentException("JSON值不得为空引用。");
		}
		return change(0, path, JSONPath支持.copy(value.getObject()), null, null);
	}

	/**
	 * 用文本替换所有匹配节点。
	 *
	 * @param path 节点选择表达式。
	 * @param value 新文本值。
	 * @return 实际匹配并处理的节点数。
	 * @throws IllegalArgumentException 参数或表达式无效。
	 */
	@SimpleFunction
	public int 修改文本(String path, String value) {
		return change(0, path, JSONPath支持.text(value, "文本"), null, null);
	}

	/**
	 * 用长整数替换所有匹配节点。
	 *
	 * @param path 节点选择表达式。
	 * @param value 新长整数值。
	 * @return 实际匹配并处理的节点数。
	 * @throws IllegalArgumentException 参数或表达式无效。
	 */
	@SimpleFunction
	public int 修改整数(String path, long value) {
		return change(0, path, JSONPath支持.copy(value), null, null);
	}

	/**
	 * 用双精度小数替换所有匹配节点。
	 *
	 * @param path 节点选择表达式。
	 * @param value 有限的双精度小数值，不接受 NaN 或无穷大。
	 * @return 实际匹配并处理的节点数。
	 * @throws IllegalArgumentException 参数或表达式无效。
	 */
	@SimpleFunction
	public int 修改小数(String path, double value) {
		return change(0, path, JSONPath支持.copy(value), null, null);
	}

	/**
	 * 用逻辑替换所有匹配节点。
	 *
	 * @param path 节点选择表达式。
	 * @param value 新逻辑值。
	 * @return 实际匹配并处理的节点数。
	 * @throws IllegalArgumentException 参数或表达式无效。
	 */
	@SimpleFunction
	public int 修改逻辑(String path, boolean value) {
		return change(0, path, JSONPath支持.copy(value), null, null);
	}

	/**
	 * 将匹配节点设为 JSON null；不会删除节点。
	 *
	 * @param path 节点选择表达式。
	 * @return 实际匹配并处理的节点数。
	 * @throws IllegalArgumentException 表达式无效。
	 */
	@SimpleFunction
	public int 修改空(String path) {
		return change(0, path, null, null, null);
	}

	/**
	 * 在匹配对象内新增或覆盖属性，不自动生成父对象。
	 *
	 * @param path 选择一个或多个对象的表达式。
	 * @param key 不得为空引用或全为空白的属性名。
	 * @param value 存在的结果值，按值复制。
	 * @return 处理的对象数量。
	 * @throws IllegalArgumentException 参数无效或任何目标不是对象。
	 */
	@SimpleFunction
	public int 新增属性(String path, String key, JSONPath结果 value) {
		return change(1, path, resultValue(value), requireKey(key), null);
	}

	/**
	 * 在每个匹配数组末尾添加一个值；数组参数作为单个成员添加。
	 *
	 * @param path 选择一个或多个数组的表达式。
	 * @param value 存在的结果值，按值复制。
	 * @return 处理的数组数量。
	 * @throws IllegalArgumentException 参数无效或任何目标不是数组。
	 */
	@SimpleFunction
	public int 添加成员(String path, JSONPath结果 value) {
		return change(2, path, resultValue(value), null, null);
	}

	/**
	 * 删除匹配的属性或数组成员；数组后续索引会前移。
	 *
	 * @param path 要删除的节点，不允许选择整个文档根。
	 * @return 删除节点数量，无匹配为 0。
	 * @throws IllegalArgumentException 表达式无效或尝试删除根节点。
	 */
	@SimpleFunction
	public int 删除(String path) {
		return change(3, path, null, null, null);
	}

	/**
	 * 在匹配对象内重命名已有属性，不覆盖另一个已存在属性。
	 *
	 * @param path 对象选择表达式。
	 * @param oldKey 已有属性名称，不得为空引用或全为空白。
	 * @param newKey 新属性名称，不得为空引用或全为空白。
	 * @return 实际处理的对象数；旧名不存在或新名冲突时整体失败。
	 * @throws IllegalArgumentException 参数、对象类型或名称冲突检查失败。
	 */
	@SimpleFunction
	public int 重命名属性(String path, String oldKey, String newKey) {
		return change(4, path, null, requireKey(oldKey), requireKey(newKey));
	}

	/* 内部事务：先定位，再复制、检查和调用上游写入接口，最后一次提交。 */

	/**
	 * 统一执行写操作，禁止一部分目标成功、一部分失败。
	 *
	 * @param operation 0 替换、1 属性、2 数组、3 删除、4 重命名。
	 * @param path 节点表达式。
	 * @param value 已校验的新值。
	 * @param key 属性名或原名称。
	 * @param newKey 新名称。
	 * @return 匹配并处理的节点数量，无匹配时为 0。
	 * @throws IllegalArgumentException 表达式、目标类型、名称检查或上游写操作失败；当前文档保持不变。
	 */
	private int change(int operation, String path, Object value, String key, String newKey) {
		// 定位并处理禁止删除根、无匹配及整个根值替换的情况。
		List<String> matches = paths(root, path);
		if (operation == 3 && matches.contains("$")) {
			throw new IllegalArgumentException("不允许删除文档根节点。");
		}
		if (matches.isEmpty()) {
			return 0;
		}
		if (matches.size() == 1 && "$".equals(matches.get(0)) && operation == 0) {
			root = JSONPath支持.copy(value);
			return 1;
		}

		// 所有后续检查和修改只操作副本，成功之前不替换当前根。
		Object copy = JSONPath支持.copy(root);
		try {
			DocumentContext context = JsonPath.using(JSONPath支持.CONFIG).parse(copy);

			// 先检查全部目标，避免混合目标在中途才发生类型或名称冲突。
			for (String match : matches) {
				Object target = context.read(match);
				if ((operation == 1 || operation == 4) && !(target instanceof Map)) {
					throw new IllegalArgumentException("目标必须是JSON对象。");
				}
				if (operation == 2 && !(target instanceof List)) {
					throw new IllegalArgumentException("目标必须是JSON数组。");
				}
				if (operation == 4) {
					Map<?, ?> object = (Map<?, ?>) target;
					if (!object.containsKey(key)) {
						throw new IllegalArgumentException("原属性不存在：" + key);
					}
					if (!key.equals(newKey) && object.containsKey(newKey)) {
						throw new IllegalArgumentException("新属性名已存在：" + newKey);
					}
				}
			}

			// 沿用上游对批量选择和数组删除顺序的处理。
			switch (operation) {
				case 0:
					context.set(path, JSONPath支持.copy(value));
					break;
				case 1:
					context.put(path, key, JSONPath支持.copy(value));
					break;
				case 2:
					context.add(path, JSONPath支持.copy(value));
					break;
				case 3:
					context.delete(path);
					break;
				default:
					if (!key.equals(newKey)) {
						context.renameKey(path, key, newKey);
					}
					break;
			}

			// 断开上游批量写入可能产生的共享容器引用后再提交。
			root = JSONPath支持.copy(context.json());
			return matches.size();
		} catch (JsonPathException error) {
			throw JSONPath支持.failure("JSONPath修改失败", error);
		}
	}

	/**
	 * 按路径模式读取具体节点路径。
	 *
	 * @param model 待查询模型。
	 * @param path 表达式。
	 * @return 按上游查询顺序返回的具体路径；无匹配时为空列表。
	 * @throws IllegalArgumentException 表达式无效或不适用于节点路径模式。
	 */
	private static List<String> paths(Object model, String path) {
		JsonPath compiled = JSONPath支持.path(path);

		// AS_PATH_LIST 同样不能直接接收 Java 空根，单独保留 JSON null 根路径。
		if (model == null) {
			List<String> result = new ArrayList<String>();
			if ("$".equals(path.trim())) {
				result.add("$");
			}
			return result;
		}
		try {
			List<?> values = compiled.read(model, JSONPath支持.CONFIG.addOptions(Option.AS_PATH_LIST));
			List<String> result = new ArrayList<String>();
			for (Object value : values) {
				result.add((String) value);
			}
			return result;
		} catch (PathNotFoundException error) {
			return new ArrayList<String>();
		} catch (JsonPathException error) {
			throw JSONPath支持.failure("无法取得节点路径", error);
		}
	}

	/**
	 * 检查上游写入接口要求的非空属性名。
	 *
	 * @param key 属性名称。
	 * @return 原属性名，不改变合法名称的内容。
	 * @throws IllegalArgumentException 属性名为空引用或全为空白。
	 */
	private static String requireKey(String key) {
		JSONPath支持.text(key, "属性名");
		if (key.trim().length() == 0) {
			throw new IllegalArgumentException("写入属性名不得为空白。");
		}
		return key;
	}

	/**
	 * 取得存在结果的内部值供复制，不允许把未匹配写成 null。
	 *
	 * @param value 新值。
	 * @return 只供写入过程读取并复制的标准 JSON 数据。
	 * @throws IllegalArgumentException 结果为空引用或没有匹配。
	 */
	private static Object resultValue(JSONPath结果 value) {
		if (value == null || !value.是否存在()) {
			throw new IllegalArgumentException("新值必须是存在的JSONPath结果；写入null请解析null字面量。");
		}
		return value.value;
	}
}
