package simple.library.wsd.jsonpath;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import com.jayway.jsonpath.Configuration;
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
 * <p>
 * 通过 JSONPath操作 创建。一个文档及其调用应在同一线程串行使用。 每次修改在独立副本上完成，成功后替换当前文档；失败不会留下部分修改。
 * 同一节点被重复选择时只写一次，查询仍保留匹配顺序。回调内可查询或复制，不能重入修改当前文档。 不自动创建路径中缺失的父对象，不执行脚本或访问网络。
 * </p>
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
	 * 写事务或访问回调正在执行；不支持回调内重入修改同一文档。
	 */
	private boolean busy;

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
			Object value = compiled.read(JSONPath支持.queryModel(root, path), JSONPath支持.CONFIG);
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

	/**
	 * 按上游读取选项查询；成功返回的默认 null 和空数组仍是可读取结果。
	 *
	 * @param path    JSONPath 表达式。
	 * @param options JSONPath操作 中选项常量的位组合。
	 * @return 配置后的存在结果；检测原始节点应使用是否存在。
	 * @throws IllegalArgumentException 选项、表达式无效或求值失败且未抑制。
	 */
	@SimpleFunction
	public JSONPath结果 按选项查询(String path, int options) {
		JsonPath compiled = JSONPath支持.path(path);
		Configuration configuration = JSONPath支持.options(options);
		boolean paths = configuration.containsOption(Option.AS_PATH_LIST);
		if (root == null) {
			if ("$".equals(path.trim())) {
				List<Object> wrapped = new ArrayList<Object>();
				wrapped.add(paths ? "$" : null);
				return new JSONPath结果(true,
						paths || configuration.containsOption(Option.ALWAYS_RETURN_LIST) ? wrapped : null);
			}
			if (configuration.containsOption(Option.SUPPRESS_EXCEPTIONS)) {
				return new JSONPath结果(true,
						configuration.containsOption(Option.ALWAYS_RETURN_LIST) || paths ? new ArrayList<Object>()
								: null);
			}
			throw new IllegalArgumentException("JSON null 根只有 $ 本身可匹配。");
		}
		try {
			Object model = JSONPath支持.queryModel(root, path);
			Object value = compiled.read(model, configuration);
			if (paths && value instanceof List && !((List<?>) value).isEmpty()) {
				value = JSONPath选择.select(model, path, configuration, null).paths;
			}
			return new JSONPath结果(true, value);
		} catch (JsonPathException error) {
			throw JSONPath支持.failure("按选项查询失败", error);
		}
	}

	/**
	 * 限制节点查询的命中数；确定路径选中的整个数组仍算一次命中。
	 *
	 * @param path    节点选择表达式，不支持聚合函数结果。
	 * @param maximum 大于零的最多匹配数。
	 * @return 已捕获匹配的独立结果，没有匹配时是否存在为假。
	 * @throws IllegalArgumentException 上限或表达式无效。
	 */
	@SimpleFunction
	public JSONPath结果 查询前几项(String path, int maximum) {
		if (maximum <= 0) {
			throw new IllegalArgumentException("最多匹配数必须大于零。");
		}
		JsonPath compiled = JSONPath支持.path(path);
		if (((com.jayway.jsonpath.internal.path.CompiledPath) com.jayway.jsonpath.internal.path.PathCompiler
				.compile(path)).isFunctionPath()) {
			throw new IllegalArgumentException("数量限制只用于节点选择，不支持聚合函数结果。");
		}
		if (root == null) {
			return new JSONPath结果("$".equals(path.trim()), null);
		}
		try {
			Object value = JsonPath.using(JSONPath支持.CONFIG).parse(JSONPath支持.queryModel(root, path)).limit(maximum)
					.read(compiled);
			boolean present = compiled.isDefinite() || !(value instanceof List) || !((List<?>) value).isEmpty();
			return new JSONPath结果(present, value);
		} catch (PathNotFoundException missing) {
			return new JSONPath结果(false, null);
		} catch (JsonPathException error) {
			throw JSONPath支持.failure("限量查询失败", error);
		}
	}

	/**
	 * 按匹配顺序访问独立结果，可由访问器返回假停止后续匹配。
	 *
	 * @param path    节点选择表达式，不支持聚合函数结果。
	 * @param visitor 逐项访问器，不得为空引用。
	 * @return 实际回调次数，包含返回假的最后一次回调。
	 * @throws IllegalArgumentException 参数无效或回调中重入当前文档。
	 */
	@SimpleFunction
	public int 访问(String path, JSONPath访问器 visitor) {
		if (visitor == null) {
			throw new IllegalArgumentException("访问器不得为空引用。");
		}
		requireIdle();
		busy = true;
		try {
			return JSONPath选择.select(root, path, JSONPath支持.CONFIG, visitor).visits();
		} catch (PathNotFoundException missing) {
			return 0;
		} catch (JsonPathException error) {
			throw JSONPath支持.failure("访问匹配节点失败", error);
		} finally {
			busy = false;
		}
	}

	/**
	 * 根据当前值转换匹配节点；同一节点只处理一次，全部转换成功后提交。
	 *
	 * @param path   节点选择表达式。
	 * @param mapper 转换回调，不得为空引用；返回值必须是存在结果。
	 * @return 实际转换的不同节点数。
	 * @throws IllegalArgumentException 参数无效、返回值无效或回调中重入当前文档修改。
	 */
	@SimpleFunction
	public int 映射(String path, JSONPath映射器 mapper) {
		if (mapper == null) {
			throw new IllegalArgumentException("映射器不得为空引用。");
		}
		return change(5, path, null, null, null, mapper);
	}

	/* 按路径修改；新增属性使用独立入口，不隐式生成中间层。 */

	/**
	 * 用指定 JSON 值替换匹配节点，支持通配符和过滤器批量选择。
	 *
	 * @param path  节点选择表达式。
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
	 * @param path  节点选择表达式。
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
	 * @param path  节点选择表达式。
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
	 * @param path  节点选择表达式。
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
	 * @param path  节点选择表达式。
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
	 * @param path  节点选择表达式。
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
	 * @param path  选择一个或多个对象的表达式。
	 * @param key   属性名，允许空文本和空白，不得为空引用。
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
	 * @param path  选择一个或多个数组的表达式。
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
	 * @param path   对象选择表达式。
	 * @param oldKey 已有属性名称，不得为空引用，允许空文本和空白。
	 * @param newKey 新属性名称，不得为空引用，允许空文本和空白。
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
	 * @param path      节点表达式。
	 * @param value     已校验的新值。
	 * @param key       属性名或原名称。
	 * @param newKey    新名称。
	 * @return 匹配并处理的节点数量，无匹配时为 0。
	 * @throws IllegalArgumentException 表达式、目标类型、名称检查或上游写操作失败；当前文档保持不变。
	 */

	private int change(int operation, String path, Object value, String key, String newKey) {
		return change(operation, path, value, key, newKey, null);
	}

	/**
	 * 在独立候选模型上定位、校验并提交一次修改，失败时释放重入保护。
	 *
	 * @param operation 0 替换、1 属性、2 数组、3 删除、4 重命名、5 映射。
	 * @param path      节点选择表达式。
	 * @param value     待写入值。
	 * @param key       属性名或原名称。
	 * @param newKey    新名称。
	 * @param mapper    映射操作的转换器。
	 * @return 实际处理的不同节点数。
	 * @throws IllegalArgumentException 参数无效、目标类型不符或在回调中重入修改。
	 */
	private int change(int operation, String path, Object value, String key, String newKey, JSONPath映射器 mapper) {
		requireIdle();
		busy = true;
		try {
			Object candidate = JSONPath支持.copy(root);
			JSONPath选择 selection;
			try {
				selection = JSONPath选择.select(candidate, path, JSONPath支持.CONFIG, null);
			} catch (PathNotFoundException missing) {
				return 0;
			}
			List<JSONPath选择.Node> targets = new ArrayList<JSONPath选择.Node>(selection.nodes.values());
			if (targets.isEmpty()) {
				return 0;
			}

			// 在执行任何写入或用户转换之前，先检查全部目标。
			for (JSONPath选择.Node node : targets) {
				if (operation == 3 && node.root) {
					throw new IllegalArgumentException("不允许删除文档根节点。");
				}
				if ((operation == 1 || operation == 4) && !(node.value instanceof Map)) {
					throw new IllegalArgumentException("目标必须是JSON对象。");
				}
				if (operation == 2 && !(node.value instanceof List)) {
					throw new IllegalArgumentException("目标必须是JSON数组。");
				}
				if (operation == 4) {
					Map<?, ?> object = (Map<?, ?>) node.value;
					if (!object.containsKey(key)) {
						throw new IllegalArgumentException("原属性不存在：" + key);
					}
					if (!key.equals(newKey) && object.containsKey(newKey)) {
						throw new IllegalArgumentException("新属性名已存在：" + newKey);
					}
				}
			}
			if (operation == 3) {
				orderDeletion(targets);
			}

			for (JSONPath选择.Node node : targets) {
				Object replacement = value;
				if (operation == 5) {
					replacement = resultValue(mapper.转换(node.path, new JSONPath结果(true, node.value)));
				}
				switch (operation) {
				case 0:
				case 5:
					if (node.root) {
						candidate = JSONPath支持.copy(replacement);
					} else {
						node.reference.set(JSONPath支持.copy(replacement), JSONPath支持.CONFIG);
					}
					break;
				case 1:
					node.reference.put(key, JSONPath支持.copy(value), JSONPath支持.CONFIG);
					break;
				case 2:
					node.reference.add(JSONPath支持.copy(value), JSONPath支持.CONFIG);
					break;
				case 3:
					node.reference.delete(JSONPath支持.CONFIG);
					break;
				default:
					if (!key.equals(newKey)) {
						node.reference.renameKey(key, newKey, JSONPath支持.CONFIG);
					}
					break;
				}
			}
			// 输入和每次写入均已独立复制，候选模型无需再复制整个文档。
			root = candidate;
			return targets.size();
		} catch (JsonPathException error) {
			throw JSONPath支持.failure("JSONPath修改失败", error);
		} finally {
			busy = false;
		}
	}

	/**
	 * 子节点先删除，同一数组索引从大到小处理，避免索引前移。
	 *
	 * @param targets 本次不同目标节点。
	 */
	private static void orderDeletion(List<JSONPath选择.Node> targets) {
		final IdentityHashMap<Object, Integer> parents = new IdentityHashMap<Object, Integer>();
		for (JSONPath选择.Node node : targets) {
			if (!parents.containsKey(node.parent)) {
				parents.put(node.parent, parents.size());
			}
		}
		targets.sort(new Comparator<JSONPath选择.Node>() {
			@Override
			public int compare(JSONPath选择.Node left, JSONPath选择.Node right) {
				int depth = Integer.compare(right.depth, left.depth);
				if (depth != 0) {
					return depth;
				}
				int parent = Integer.compare(parents.get(left.parent), parents.get(right.parent));
				if (parent != 0) {
					return parent;
				}
				if (left.key instanceof Number && right.key instanceof Number) {
					return Integer.compare(((Number) right.key).intValue(), ((Number) left.key).intValue());
				}
				return 0;
			}
		});
	}

	/**
	 * 拒绝在当前文档的转换或访问回调中再次开始修改。
	 *
	 * @throws IllegalArgumentException 当前文档正在执行事务或访问回调。
	 */
	private void requireIdle() {
		if (busy) {
			throw new IllegalArgumentException("不能在当前文档的回调中重入修改或访问。");
		}
	}

	/**
	 * 按路径模式读取具体节点路径。
	 *
	 * @param model 待查询模型。
	 * @param path  表达式。
	 * @return 按上游查询顺序返回的具体路径；无匹配时为空列表。
	 * @throws IllegalArgumentException 表达式无效或不适用于节点路径模式。
	 */

	private static List<String> paths(Object model, String path) {
		try {
			return JSONPath选择.select(model, path, JSONPath支持.CONFIG, null).paths;
		} catch (PathNotFoundException missing) {
			return new ArrayList<String>();
		} catch (JsonPathException error) {
			throw JSONPath支持.failure("无法取得节点路径", error);
		}
	}

	/**
	 * 检查 JSON 属性名不为空引用，保留合法的空文本和空白。
	 *
	 * @param key 属性名称。
	 * @return 原属性名，不改变合法名称的内容。
	 * @throws IllegalArgumentException 属性名为空引用。
	 */
	private static String requireKey(String key) {
		JSONPath支持.text(key, "属性名");
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
