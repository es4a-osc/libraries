package simple.library.wsd.quickjs;

import java.lang.reflect.Array;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import simple.runtime.collections.哈希表;
import simple.runtime.collections.集合;
import simple.runtime.variants.ObjectVariant;
import simple.runtime.variants.Variant;

/**
 * Simple 与 JS 数据图转换，统一处理集合、哈希表、数组、循环引用和临时值释放。
 *
 * @author 树先生 xhwsd@qq.com
 */
final class ValueConversion {

	/**
	 * 禁止创建工具对象。
	 */
	private ValueConversion() {
	}

	/**
	 * 将宿主数据图复制到实例中；成功或失败均回收转换过程持有的临时引用。
	 *
	 * @param context 目标实例。
	 * @param input 宿主数据。
	 * @return 独立 JS 引用。
	 */
	static QuickJS值 toJS(final QuickJS实例 context, final Object input) {
		return context.runtime.perform(context, new QuickJS运行时.Action<QuickJS值>() {
			@Override
			public QuickJS值 run() {
				List<QuickJS值> owned = new ArrayList<QuickJS值>();
				try {
					return convert(context, input, new IdentityHashMap<Object, QuickJS值>(), owned, 0).保留();
				} finally {
					for (QuickJS值 value : owned) {
						value.释放();
					}
				}
			}
		});
	}

	/**
	 * 转换一个节点，容器先登记再遍历，保留重复引用和循环关系。
	 *
	 * @param context 目标实例。
	 * @param input 当前节点。
	 * @param memo 已转换容器。
	 * @param owned 本次持有的临时值。
	 * @param depth 嵌套深度。
	 * @return 本次转换范围内的借用引用。
	 */
	private static QuickJS值 convert(QuickJS实例 context, Object input, IdentityHashMap<Object, QuickJS值> memo,
			List<QuickJS值> owned, int depth) {
		if (depth > 128 || owned.size() > 100000) {
			throw new QuickJS错误("数据转换超过 128 层或 100000 个节点");
		}
		if (input instanceof Variant) {
			return convert(context, context.unbox((Variant)input), memo, owned, depth + 1);
		}
		if (input instanceof QuickJS值) {
			context.pointer((QuickJS值)input);
			return (QuickJS值)input;
		}
		if (memo.containsKey(input)) {
			return memo.get(input);
		}

		Object data = input;
		if (input instanceof 集合) {
			data = ((集合)input).getList();
		} else if (input instanceof 哈希表) {
			data = ((哈希表)input).getMap();
		}

		QuickJS值 value;
		if (input == QuickJS实例.UNDEFINED) {
			value = context.创建未定义();
		} else if (input == null) {
			value = context.创建空值();
		} else if (input instanceof Boolean) {
			value = context.创建逻辑((Boolean)input);
		} else if (input instanceof String || input instanceof Character) {
			value = context.创建文本(input.toString());
		} else if (input instanceof BigInteger ||
				input instanceof Long &&
						(((Long)input).longValue() > 9007199254740991L ||
								((Long)input).longValue() < -9007199254740991L)) {
			value = context.创建大整数(input.toString());
		} else if (input instanceof Number) {
			value = context.创建小数(((Number)input).doubleValue());
		} else if (input instanceof byte[]) {
			value = context.创建缓冲区((byte[])input);
		} else if (data instanceof Map) {
			value = context.创建对象();
		} else if (data instanceof List || data.getClass().isArray()) {
			value = context.创建数组();
		} else {
			throw new QuickJS错误("不能转换宿主类型，请使用创建宿主对象显式绑定：" + input.getClass().getName());
		}
		owned.add(value);

		if (data instanceof Map) {
			memo.put(input, value);
			memo.put(data, value);
			QuickJS对象 object = (QuickJS对象)value;
			for (Map.Entry<?, ?> entry : ((Map<?, ?>)data).entrySet()) {
				if (!(entry.getKey() instanceof String)) {
					throw new QuickJS错误("哈希表转换仅接受字符串键");
				}
				// 使用数据属性，避免 __proto__ 等键触发原型 setter。
				context.apply("dataProperty", object, entry.getKey(),
							   convert(context, entry.getValue(), memo, owned, depth + 1))
						.释放();
			}
		} else if (!(input instanceof byte[]) && (data instanceof List || data != null && data.getClass().isArray())) {
			memo.put(input, value);
			memo.put(data, value);
			int count = data instanceof List ? ((List<?>)data).size() : Array.getLength(data);
			for (int i = 0; i < count; i++) {
				Object item = data instanceof List ? ((List<?>)data).get(i) : Array.get(data, i);
				((QuickJS数组)value).置项目(i, convert(context, item, memo, owned, depth + 1));
			}
		}
		return value;
	}

	/**
	 * 深度转换为 Simple 集合及哈希表，保留循环；特殊对象保留独立 JS 引用。
	 *
	 * @param source 源 JS 值。
	 * @return Simple 数据图。
	 */
	static Variant toSimple(final QuickJS值 source) {
		return source.context.runtime.perform(source.context, new QuickJS运行时.Action<Variant>() {
			@Override
			public Variant run() {
				List<QuickJS值> retained = new ArrayList<QuickJS值>();
				try {
					return convertBack(source, new HashMap<Long, Variant>(), retained, 0);
				} catch (RuntimeException | Error error) {
					for (QuickJS值 value : retained) {
						value.释放();
					}
					throw error;
				}
			}
		});
	}

	/**
	 * 为宿主调用转换数据，并将特殊 JS 引用交给调用边界统一释放。
	 *
	 * @param source 源值。
	 * @param retained 本次调用持有的特殊值。
	 * @return Simple 数据。
	 */
	static Variant toSimple(QuickJS值 source, List<QuickJS值> retained) {
		return convertBack(source, new HashMap<Long, Variant>(), retained, 0);
	}

	/**
	 * 转换 JS 数据节点，身份仅用于内部循环检测，不向调用方暴露指针。
	 *
	 * @param source 当前值。
	 * @param memo 已转换对象。
	 * @param retained 特殊对象的独立引用。
	 * @param depth 嵌套深度。
	 * @return 对应 Simple 变体。
	 */
	private static Variant convertBack(QuickJS值 source, Map<Long, Variant> memo, List<QuickJS值> retained, int depth) {
		if (depth > 128 || memo.size() > 100000) {
			throw new QuickJS错误("数据转换超过 128 层或 100000 个容器");
		}
		int type = source.取类型();
		if (type <= 5) {
			return source.到简单值();
		}
		long identity = Native.identity(source.context.handle, source.context.pointer(source));
		if (identity != 0 && memo.containsKey(identity)) {
			return memo.get(identity);
		}

		if (type == 8) {
			QuickJS数组 array = source.到数组();
			long count = array.取长度();
			if (count > 100000) {
				throw new QuickJS错误("转换数组长度不能超过 100000");
			}
			集合 collection = new 集合();
			Variant result = ObjectVariant.getObjectVariant(collection);
			memo.put(identity, result);
			for (long i = 0; i < count; i++) {
				QuickJS值 item = array.取项目(i);
				try {
					collection.加入(convertBack(item, memo, retained, depth + 1));
				} finally {
					item.释放();
				}
			}
			return result;
		}

		QuickJS值 plain = source.context.apply("plain", source);
		boolean isPlain;
		try {
			isPlain = plain.到逻辑();
		} finally {
			plain.释放();
		}
		if (isPlain) {
			哈希表 table = new 哈希表();
			Variant result = ObjectVariant.getObjectVariant(table);
			memo.put(identity, result);
			QuickJS对象 object = source.到对象();
			QuickJS数组 keys = object.取可枚举键();
			try {
				long count = keys.取长度();
				if (count > 100000) {
					throw new QuickJS错误("转换对象的成员数不能超过 100000");
				}
				for (long i = 0; i < count; i++) {
					QuickJS值 key = keys.取项目(i);
					try {
						String name = key.到文本();
						QuickJS值 item = object.取成员(name);
						try {
							table.加入(name, convertBack(item, memo, retained, depth + 1));
						} finally {
							item.释放();
						}
					} finally {
						key.释放();
					}
				}
			} finally {
				keys.释放();
			}
			return result;
		}

		QuickJS值 reference = source.保留();
		retained.add(reference);
		Variant result = ObjectVariant.getObjectVariant(reference);
		if (identity != 0) {
			memo.put(identity, result);
		}
		return result;
	}
}
