package simple.library.wsd.jsonpath;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.internal.ES4APathAccess;
import com.jayway.jsonpath.internal.EvaluationAbortException;
import com.jayway.jsonpath.internal.PathRef;
import com.jayway.jsonpath.internal.path.CompiledPath;
import com.jayway.jsonpath.internal.path.EvaluationContextImpl;
import com.jayway.jsonpath.internal.path.PathCompiler;
import com.jayway.jsonpath.spi.json.JsonSmartJsonProvider;

/**
 * 基于实际节点引用定位、计数和生成可复用路径，保留上游匹配顺序。
 *
 * <p>
 * 定位时禁止改写已有容器。仅固定适配随库交付的 JsonPath 2.10.0 内部定位入口， 不复制上游解析器，也不依赖反射。
 * </p>
 *
 * @author 树先生 xhwsd@qq.com
 */
final class JSONPath选择 {
	/**
	 * 一个容器在当前独立 JSON 树中的位置。
	 */
	private static final class Location {
		final String path;
		final int depth;

		/**
		 * 保存容器位置。
		 *
		 * @param path  已转义路径。
		 * @param depth 所在层数。
		 */
		Location(String path, int depth) {
			this.path = path;
			this.depth = depth;
		}
	}

	/**
	 * 一次匹配对应的实际节点；写操作以具体路径去重。
	 */
	static final class Node {
		final PathRef reference;
		final Object parent;
		final Object key;
		final Object value;
		final String path;
		final int depth;
		final boolean root;

		/**
		 * 保存节点定位，不把原始路径文本重新解析为节点。
		 *
		 * @param reference 上游引用。
		 * @param parent    父容器。
		 * @param key       属性名或索引。
		 * @param value     定位时的值。
		 * @param path      可复用的具体路径。
		 * @param depth     节点深度。
		 * @param root      是否为整个文档根。
		 */
		Node(PathRef reference, Object parent, Object key, Object value, String path, int depth, boolean root) {
			this.reference = reference;
			this.parent = parent;
			this.key = key;
			this.value = value;
			this.path = path;
			this.depth = depth;
			this.root = root;
		}
	}

	final List<String> paths = new ArrayList<String>();
	final Map<String, Node> nodes = new LinkedHashMap<String, Node>();
	private final IdentityHashMap<Object, Location> locations = new IdentityHashMap<Object, Location>();
	private final Object model;
	private final Configuration configuration;
	private final JSONPath访问器 visitor;
	private int visits;

	/**
	 * 创建独立定位会话。
	 *
	 * @param model   标准 JSON 模型。
	 * @param options 当前查询选项。
	 * @param visitor 可选结果访问器。
	 */
	private JSONPath选择(Object model, Configuration options, JSONPath访问器 visitor) {
		this.model = model;
		this.visitor = visitor;
		index(model);
		this.configuration = Configuration.builder().jsonProvider(new ReadOnlyProvider(locations))
				.mappingProvider(JSONPath支持.CONFIG.mappingProvider())
				.options(options.getOptions().toArray(new Option[0])).build();
	}

	/**
	 * 内存定位节点，缺失错误由调用入口决定返回空还是报告失败。
	 *
	 * @param model      标准 JSON 模型。
	 * @param expression 原始表达式。
	 * @param options    查询选项。
	 * @param visitor    可选逐项回调。
	 * @return 本次定位会话。
	 * @throws IllegalArgumentException 表达式无效或选择了函数生成的数据。
	 */
	static JSONPath选择 select(Object model, String expression, Configuration options, JSONPath访问器 visitor) {
		JSONPath支持.text(expression, "路径");
		if (expression.trim().isEmpty()) {
			throw new IllegalArgumentException("路径不得为空白。");
		}
		CompiledPath compiled = (CompiledPath) PathCompiler.compile(expression);
		if (compiled.isFunctionPath()) {
			throw new IllegalArgumentException("函数结果不是可定位的文档节点。");
		}
		JSONPath选择 result = new JSONPath选择(model, options, visitor);
		if (model == null) {
			if ("$".equals(expression.trim())) {
				try {
					result.capture(PathRef.createRoot(null), null);
				} catch (EvaluationAbortException stopped) {
					// 根 null 同样允许访问器在第一次回调时停止。
				}
			} else {
				throw new PathNotFoundException("JSON null 根只有 $ 本身可匹配。");
			}
			return result;
		}

		// 使用上游同一定位入口，只在记录结果前取得实际节点引用及顺序。
		EvaluationContextImpl context = new EvaluationContextImpl(compiled, model, result.configuration, true) {
			/**
			 * 记录一次上游匹配，结果值仍由上游上下文管理。
			 *
			 * @param path      上游原始路径文本。
			 * @param operation 实际节点引用。
			 * @param value     匹配结果。
			 */
			@Override
			public void addResult(String path, PathRef operation, Object value) {
				if (result.capture(operation, value)) {
					super.addResult(path, operation, value);
				}
			}
		};
		try {
			compiled.getRoot().evaluate("", PathRef.createRoot(model), model, context);
		} catch (EvaluationAbortException stopped) {
			// 访问器返回假属于正常停止，保留已经完成的回调和匹配结果。
		}
		return result;
	}

	/**
	 * 获取实际回调次数，包括返回假的最后一次回调。
	 *
	 * @return 已调用的次数。
	 */
	int visits() {
		return visits;
	}

	/**
	 * 为原有容器建立安全路径索引，同时作为只读定位的保护集合。
	 *
	 * @param root 根值。
	 * @throws IllegalArgumentException 当前模型存在共享或循环容器。
	 */
	private void index(Object root) {
		if (!(root instanceof Map) && !(root instanceof List)) {
			return;
		}
		Deque<Object> pending = new ArrayDeque<Object>();
		locations.put(root, new Location("$", 0));
		pending.push(root);
		while (!pending.isEmpty()) {
			Object container = pending.pop();
			Location location = locations.get(container);
			if (container instanceof Map) {
				for (Map.Entry<?, ?> item : ((Map<?, ?>) container).entrySet()) {
					indexChild(item.getValue(), location.path + "[" + JSONPath支持.json(item.getKey()) + "]",
							location.depth + 1, pending);
				}
			} else {
				List<?> array = (List<?>) container;
				for (int i = 0; i < array.size(); i++) {
					indexChild(array.get(i), location.path + "[" + i + "]", location.depth + 1, pending);
				}
			}
		}
	}

	/**
	 * 索引一个容器子值，标量不需要父路径索引。
	 *
	 * @param value   子值。
	 * @param path    子值的安全路径。
	 * @param depth   子值深度。
	 * @param pending 待遍历容器。
	 */
	private void indexChild(Object value, String path, int depth, Deque<Object> pending) {
		if (value instanceof Map || value instanceof List) {
			if (locations.put(value, new Location(path, depth)) != null) {
				throw new IllegalArgumentException("JSON模型包含共享或循环容器。");
			}
			pending.push(value);
		}
	}

	/**
	 * 记录一次匹配及其实际节点，过滤并非真实节点的缺失叶子。
	 *
	 * @param reference 上游节点引用。
	 * @param value     上游结果值。
	 * @return 本次匹配有效时为真。
	 * @throws IllegalArgumentException 当前结果不能映射回原始 JSON 树。
	 */
	private boolean capture(PathRef reference, Object value) {
		if (reference == PathRef.NO_OP) {
			throw new IllegalArgumentException("函数结果不是可定位的文档节点。");
		}
		List<Node> group = new ArrayList<Node>();
		boolean root = ES4APathAccess.isRoot(reference);
		Object parent = ES4APathAccess.parent(reference);
		Object key = ES4APathAccess.accessor(reference);
		if (root) {
			group.add(new Node(reference, null, null, model, "$", 0, true));
		} else if (ES4APathAccess.isMultiProperty(reference)) {
			// 上游合并结果保留实际属性名，避免拆解可能含 && 的拼接访问键。
			for (Object property : ((Map<?, ?>) value).keySet()) {
				Node node = node(PathRef.create(parent, (String) property), parent, property);
				if (node != null) {
					group.add(node);
				}
			}
		} else {
			Node node = node(reference, parent, key);
			if (node != null) {
				group.add(node);
			}
		}
		if (group.isEmpty()) {
			return false;
		}
		String path = group.get(0).path;
		if (group.size() > 1) {
			Location location = locations.get(parent);
			StringBuilder union = new StringBuilder(location.path).append('[');
			for (int i = 0; i < group.size(); i++) {
				if (i > 0) {
					union.append(',');
				}
				union.append(JSONPath支持.json(group.get(i).key));
			}
			path = union.append(']').toString();
		}
		paths.add(path);
		for (Node node : group) {
			if (!nodes.containsKey(node.path)) {
				nodes.put(node.path, node);
			}
		}
		if (visitor != null && !visitor.访问(visits++, path, new JSONPath结果(true, value))) {
			throw new EvaluationAbortException();
		}
		return true;
	}

	/**
	 * 创建单个实际属性或数组成员的定位信息。
	 *
	 * @param reference 上游节点引用。
	 * @param parent    父对象或数组。
	 * @param key       属性名或数组索引。
	 * @return 实际节点；允许跳过的缺失叶子返回空。
	 */
	private Node node(PathRef reference, Object parent, Object key) {
		Location location = locations.get(parent);
		if (location == null) {
			throw new IllegalArgumentException("表达式生成的数据不能作为文档节点。");
		}
		if (parent instanceof Map) {
			Map<?, ?> object = (Map<?, ?>) parent;
			if (!object.containsKey(key)) {
				if (configuration.containsOption(Option.REQUIRE_PROPERTIES)
						&& !configuration.containsOption(Option.SUPPRESS_EXCEPTIONS)) {
					throw new PathNotFoundException("目标属性不存在：" + key);
				}
				if (!configuration.containsOption(Option.DEFAULT_PATH_LEAF_TO_NULL)) {
					return null;
				}
			}
			return new Node(reference, parent, key, object.get(key), location.path + "[" + JSONPath支持.json(key) + "]",
					location.depth + 1, false);
		}
		List<?> array = (List<?>) parent;
		int index = ((Number) key).intValue();
		if (index < 0) {
			index += array.size();
			reference = PathRef.create(parent, index);
		}
		if (index < 0 || index >= array.size()) {
			return null;
		}
		return new Node(reference, parent, index, array.get(index), location.path + "[" + index + "]",
				location.depth + 1, false);
	}

	/**
	 * 定位表达式只能读取已有容器；新建的结果容器仍由上游正常填充。
	 */
	private static final class ReadOnlyProvider extends JsonSmartJsonProvider {
		private final IdentityHashMap<Object, Location> protectedContainers;

		/**
		 * 绑定本次定位中的原始容器集合。
		 *
		 * @param containers 受保护容器。
		 */
		ReadOnlyProvider(IdentityHashMap<Object, Location> containers) {
			protectedContainers = containers;
		}

		/**
		 * 拒绝定位期间改变已有模型。
		 *
		 * @param target 目标容器。
		 */
		private void check(Object target) {
			if (protectedContainers.containsKey(target)) {
				throw new IllegalArgumentException("节点选择表达式不能修改文档内容。");
			}
		}

		@Override
		public void setArrayIndex(Object array, int index, Object value) {
			check(array);
			super.setArrayIndex(array, index, value);
		}

		@Override
		public void setProperty(Object object, Object key, Object value) {
			check(object);
			super.setProperty(object, key, value);
		}

		@Override
		public void removeProperty(Object object, Object key) {
			check(object);
			super.removeProperty(object, key);
		}
	}
}
