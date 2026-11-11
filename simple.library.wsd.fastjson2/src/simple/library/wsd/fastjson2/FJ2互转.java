package simple.library.wsd.fastjson2;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.Calendar;
import java.util.Date;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;

import simple.runtime.collections.JSON值;
import simple.runtime.collections.哈希表;
import simple.runtime.collections.集合;
import simple.runtime.variants.ArrayVariant;
import simple.runtime.variants.BooleanVariant;
import simple.runtime.variants.ByteVariant;
import simple.runtime.variants.DateVariant;
import simple.runtime.variants.DoubleVariant;
import simple.runtime.variants.IntegerVariant;
import simple.runtime.variants.LongVariant;
import simple.runtime.variants.ObjectVariant;
import simple.runtime.variants.ShortVariant;
import simple.runtime.variants.SingleVariant;
import simple.runtime.variants.StringVariant;
import simple.runtime.variants.UninitializedVariant;
import simple.runtime.variants.Variant;

/**
 * 运行库数据与 JSON 值图之间的互转边界。
 *
 * <p>
 * 通过任务栈处理数组、集合和哈希表，保留共享与循环引用。 普通业务对象仍交给上游对象映射器处理，不反射运行库容器内部字段。
 * </p>
 *
 * @author 树先生 xhwsd@qq.com
 */
final class FJ2互转 {

	/**
	 * 静态互转工具。
	 */
	private FJ2互转() {
	}

	/**
	 * 解包运行库数据，创建独立的上游容器。
	 *
	 * @param value 变体、运行库容器或普通数据对象。
	 * @return 上游可编码的数据，普通业务对象保持原实例。
	 * @throws IllegalArgumentException 键无效、数值非有限、嵌套过深或数据包含组件。
	 */
	static Object toNative(Object value) {
		IdentityHashMap<Object, Object> memo = new IdentityHashMap<Object, Object>();
		Deque<Object[]> tasks = new ArrayDeque<Object[]>();
		Object result = nativePart(value, memo, tasks, 0);
		while (!tasks.isEmpty()) {
			Object[] task = tasks.pop();
			Object source = task[0];
			Object target = task[1];
			int depth = (Integer) task[2];
			if (source instanceof Map) {
				for (Map.Entry<?, ?> item : ((Map<?, ?>) source).entrySet()) {
					if (!(item.getKey() instanceof String)) {
						throw new IllegalArgumentException("JSON 对象键必须是非空引用文本。");
					}
					FJ2支持.object(target).put((String) item.getKey(),
							nativePart(item.getValue(), memo, tasks, depth + 1));
				}
			} else if (source instanceof List) {
				for (Object item : (List<?>) source) {
					FJ2支持.array(target).add(nativePart(item, memo, tasks, depth + 1));
				}
			} else {
				for (int i = 0; i < Array.getLength(source); i++) {
					FJ2支持.array(target).add(nativePart(Array.get(source, i), memo, tasks, depth + 1));
				}
			}
		}
		return result;
	}

	/**
	 * 解包一个值并为其子容器安排后续任务。
	 *
	 * @param input 当前输入。
	 * @param memo  已解包的实例。
	 * @param tasks 待处理容器。
	 * @param depth 当前深度。
	 * @return 标量、业务对象或已分配的容器。
	 */
	private static Object nativePart(Object input, IdentityHashMap<Object, Object> memo, Deque<Object[]> tasks,
			int depth) {
		Object value = input;
		while (value instanceof Variant) {
			Variant variant = (Variant) value;
			if (variant instanceof UninitializedVariant) {
				return null;
			} else if (variant instanceof StringVariant) {
				return variant.getString();
			} else if (variant instanceof BooleanVariant) {
				return variant.getBoolean();
			} else if (variant instanceof ByteVariant || variant instanceof ShortVariant
					|| variant instanceof IntegerVariant) {
				return variant.getInteger();
			} else if (variant instanceof LongVariant) {
				return variant.getLong();
			} else if (variant instanceof SingleVariant || variant instanceof DoubleVariant) {
				return FJ2支持.finite(variant.getDouble());
			} else if (variant instanceof DateVariant) {
				value = variant.getDate();
			} else if (variant instanceof ArrayVariant) {
				value = variant.getArray();
			} else if (variant instanceof ObjectVariant) {
				value = variant.getObject();
			} else {
				throw new IllegalArgumentException("不支持的 Simple 变体类型。");
			}
		}
		if (value == null) {
			return null;
		}
		if (memo.containsKey(value)) {
			return memo.get(value);
		}
		if (depth > 2048) {
			throw new IllegalArgumentException("数据嵌套超过 2048 层。");
		}
		if (value instanceof simple.runtime.components.组件) {
			throw new IllegalArgumentException("不能将窗口或组件作为业务 JSON 数据。");
		}
		if (value instanceof FJ2值) {
			return FJ2支持.raw((FJ2值) value);
		}
		if (value instanceof JSON值) {
			Object result = FJ2支持.parse(((JSON值) value).到文本(), new FJ2读配置());
			memo.put(value, result);
			return result;
		}
		if (value instanceof Calendar || value instanceof Date) {
			long time = value instanceof Calendar ? ((Calendar) value).getTimeInMillis() : ((Date) value).getTime();
			Date result = new Date(time);
			memo.put(value, result);
			return result;
		}
		if (value instanceof byte[]) {
			byte[] result = ((byte[]) value).clone();
			memo.put(value, result);
			return result;
		}
		Object source = value;
		if (value instanceof 集合) {
			source = ((集合) value).getList();
		} else if (value instanceof 哈希表) {
			source = ((哈希表) value).getMap();
		}
		if (source instanceof Map || source instanceof List || source.getClass().isArray()) {
			Object result = source instanceof Map ? new JSONObject() : new JSONArray();
			memo.put(value, result);
			memo.put(source, result);
			tasks.push(new Object[] { source, result, depth });
			return result;
		}
		if (value instanceof Float || value instanceof Double) {
			FJ2支持.finite(((Number) value).doubleValue());
		}
		return value;
	}

	/**
	 * 将值图复制为 Simple 标量、集合或哈希表。
	 *
	 * @param value 本库支持的值图。
	 * @return 与值图隔离的 Simple 变体。
	 * @throws IllegalArgumentException 整数超出长整数范围，或小数不能在十进制文本往返中保持精度。
	 */
	static Variant toSimple(Object value) {
		IdentityHashMap<Object, Variant> memo = new IdentityHashMap<Object, Variant>();
		Deque<Object[]> tasks = new ArrayDeque<Object[]>();
		Variant result = simplePart(value, memo, tasks, 0);
		while (!tasks.isEmpty()) {
			Object[] task = tasks.pop();
			Object source = task[0];
			Object target = task[1];
			int depth = (Integer) task[2];
			if (source instanceof Map) {
				for (Map.Entry<String, Object> item : FJ2支持.object(source).entrySet()) {
					((哈希表) target).加入(item.getKey(), simplePart(item.getValue(), memo, tasks, depth + 1));
				}
			} else {
				for (Object item : FJ2支持.array(source)) {
					((集合) target).加入(simplePart(item, memo, tasks, depth + 1));
				}
			}
		}
		return result;
	}

	/**
	 * 转换单个值并为子容器分配任务。
	 *
	 * @param value 来源值。
	 * @param memo  已转换的实例。
	 * @param tasks 待处理容器。
	 * @param depth 当前深度。
	 * @return 对应的 Simple 变体。
	 */
	private static Variant simplePart(Object value, IdentityHashMap<Object, Variant> memo, Deque<Object[]> tasks,
			int depth) {
		if (memo.containsKey(value)) {
			return memo.get(value);
		}
		if (depth > 2048) {
			throw new IllegalArgumentException("数据嵌套超过 2048 层。");
		}
		int kind = FJ2支持.kind(value);
		Variant result;
		if (kind == FJ2值.类型_空) {
			return ObjectVariant.getObjectVariant(null);
		} else if (kind == FJ2值.类型_文本) {
			return StringVariant.getStringVariant((String) value);
		} else if (kind == FJ2值.类型_逻辑) {
			return BooleanVariant.getBooleanVariant((Boolean) value);
		} else if (kind == FJ2值.类型_数值) {
			BigDecimal number = FJ2支持.number(value);
			if (number.stripTrailingZeros().scale() <= 0) {
				return LongVariant.getLongVariant(number.longValueExact());
			}
			double converted = FJ2支持.finite(number.doubleValue());
			if (BigDecimal.valueOf(converted).compareTo(number) != 0) {
				throw new IllegalArgumentException("小数不能无损转换为 Simple 双精度值，请读取精确数值文本。");
			}
			return DoubleVariant.getDoubleVariant(converted);
		} else if (kind == FJ2值.类型_字节) {
			result = ArrayVariant.getArrayVariant(((byte[]) value).clone());
		} else if (kind == FJ2值.类型_日期) {
			Calendar date = Calendar.getInstance();
			date.setTimeInMillis(((Date) value).getTime());
			result = DateVariant.getDateVariant(date);
		} else {
			Object container = kind == FJ2值.类型_对象 ? new 哈希表() : new 集合();
			result = ObjectVariant.getObjectVariant(container);
			tasks.push(new Object[] { value, container, depth });
		}
		memo.put(value, result);
		return result;
	}

	/**
	 * 为普通对象字段中的运行库数据类型提供读取器。
	 *
	 * @param type 声明类型。
	 * @return 受支持类型的读取器；其他类型返回空引用。
	 */
	static ObjectReader<Object> reader(final Type type) {
		if (type != Variant.class && type != 集合.class && type != 哈希表.class && type != FJ2值.class
				&& type != JSON值.class) {
			return null;
		}
		return new ObjectReader<Object>() {
			/**
			 * 读取完整字段值并转换为其声明的运行库类型。
			 *
			 * @param reader    当前上游读取器。
			 * @param fieldType 上游传入的字段类型。
			 * @param fieldName 上游传入的字段名称。
			 * @param features  本次调用的读取特性。
			 * @return 对应声明类型的数据。
			 */
			@Override
			public Object readObject(JSONReader reader, Type fieldType, Object fieldName, long features) {
				return readField(reader, type);
			}

			/**
			 * 读取 JSONB 字段值并转换为其声明的运行库类型。
			 *
			 * @param reader    当前上游读取器。
			 * @param fieldType 上游传入的字段类型。
			 * @param fieldName 上游传入的字段名称。
			 * @param features  本次调用的读取特性。
			 * @return 对应声明类型的数据。
			 */
			@Override
			public Object readJSONBObject(JSONReader reader, Type fieldType, Object fieldName, long features) {
				return readField(reader, type);
			}
		};
	}

	/**
	 * 读取字段并检查运行库容器形状。
	 *
	 * @param reader 当前上游读取器。
	 * @param type   声明的运行库类型。
	 * @return 匹配的字段值。
	 */
	private static Object readField(JSONReader reader, Type type) {
		// 字段读到的是局部子树，不能用它作为整个文档的引用根，否则会静默解析到错误目标。
		boolean disabled = reader.getContext().isEnabled(JSONReader.Feature.DisableReferenceDetect);
		Object data;
		reader.getContext().config(JSONReader.Feature.DisableReferenceDetect, true);
		try {
			data = reader.readAny();
		} finally {
			reader.getContext().config(JSONReader.Feature.DisableReferenceDetect, disabled);
		}
		if (!disabled) {
			requireFieldTree(data);
		}
		if (type == Variant.class) {
			return toSimple(data);
		}
		if (type == FJ2值.class) {
			return new FJ2值(data);
		}
		if (data == null) {
			return null;
		}
		if (type == JSON值.class) {
			if (!(data instanceof Map) && !(data instanceof List)) {
				throw new IllegalArgumentException("运行库 JSON值 字段只能接收对象或数组。");
			}
			return new JSON值(FJ2支持.stringify(data, new FJ2写配置()));
		}
		if ((type == 集合.class && !(data instanceof List)) || (type == 哈希表.class && !(data instanceof Map))) {
			throw new IllegalArgumentException("JSON 数据不匹配目标集合或哈希表字段。");
		}
		Object result = toSimple(data).getObject();
		if (!((Class<?>) type).isInstance(result)) {
			throw new IllegalArgumentException("JSON 数据不匹配目标集合或哈希表字段。");
		}
		return result;
	}

	/**
	 * 拒绝局部运行库字段内的文档引用，避免将引用目标误当作当前字段的子节点。
	 *
	 * @param data 字段子树。
	 * @throws IllegalArgumentException 字段中含需要按整个文档解析的引用标记。
	 */
	private static void requireFieldTree(Object data) {
		Deque<Object> pending = new ArrayDeque<Object>();
		if (data != null) {
			pending.push(data);
		}
		while (!pending.isEmpty()) {
			Object value = pending.pop();
			if (value instanceof Map) {
				Map<?, ?> object = (Map<?, ?>) value;
				if (object.size() == 1 && object.get("$ref") instanceof String) {
					throw new IllegalArgumentException("普通对象的运行库数据字段内不接受 $ref；引用图请使用 FJ2值 或到Simple数据转换。");
				}
				for (Object item : object.values()) {
					if (item != null) {
						pending.push(item);
					}
				}
			} else if (value instanceof List) {
				for (Object item : (List<?>) value) {
					if (item != null) {
						pending.push(item);
					}
				}
			}
		}
	}

	/**
	 * 检查普通对象中的运行库数据字段能否按本字段的局部子树读取。
	 *
	 * @param data       已解包的字段数据。
	 * @param references 当前写入器是否会为共享容器生成引用。
	 * @param name       字段名称或数组索引，用于定位错误。
	 * @throws IllegalArgumentException 字段含循环，或将生成局部解码不支持的内部引用。
	 */
	private static void requireWritableField(Object data, boolean references, Object name) {
		IdentityHashMap<Object, Boolean> colors = new IdentityHashMap<Object, Boolean>();
		Deque<Object[]> tasks = new ArrayDeque<Object[]>();
		tasks.push(new Object[] { data, false });
		while (!tasks.isEmpty()) {
			Object[] task = tasks.pop();
			Object value = task[0];
			if (!(value instanceof Map) && !(value instanceof List)) {
				continue;
			}
			if ((Boolean) task[1]) {
				colors.put(value, true);
				continue;
			}
			Boolean visited = colors.get(value);
			if (visited != null) {
				if (!visited || references) {
					throw new IllegalArgumentException("运行库数据字段“" + name + "”包含不能局部还原的内部引用；引用图请使用数据转值与到Simple数据整体互转。");
				}
				continue;
			}
			colors.put(value, false);
			tasks.push(new Object[] { value, true });
			Iterable<?> children = value instanceof Map ? ((Map<?, ?>) value).values() : (List<?>) value;
			for (Object item : children) {
				tasks.push(new Object[] { item, false });
			}
		}
	}

	/**
	 * 为变体实现和运行库数据容器安装共用写入器。
	 *
	 * @param provider 当前调用使用的独立注册表。
	 */
	static void registerWriters(ObjectWriterProvider provider) {
		ObjectWriter<Object> adapter = writer();
		for (Class<?> type : new Class<?>[] { Variant.class, ObjectVariant.class, UninitializedVariant.class,
				StringVariant.class, BooleanVariant.class, ByteVariant.class, ShortVariant.class, IntegerVariant.class,
				LongVariant.class, SingleVariant.class, DoubleVariant.class, DateVariant.class, ArrayVariant.class,
				集合.class, 哈希表.class, JSON值.class, FJ2值.class }) {
			provider.register(type, adapter, false);
			provider.register(type, adapter, true);
		}
	}

	/**
	 * 将运行库数据字段委托给正常 JSON 值编码，避免反射 Variant 的转换方法。
	 *
	 * @return 可供各运行库数据类型共用的写入器。
	 */
	static ObjectWriter<Object> writer() {
		return new ObjectWriter<Object>() {
			/**
			 * 解包当前字段并写入其数据值。
			 *
			 * @param writer    当前上游写入器。
			 * @param object    待写入的运行库数据。
			 * @param fieldName 上游传入的字段名称。
			 * @param fieldType 上游传入的字段类型。
			 * @param features  本次调用的写入特性。
			 */
			@Override
			public void write(JSONWriter writer, Object object, Object fieldName, Type fieldType, long features) {
				Object data = toNative(object);
				requireWritableField(data, writer.isEnabled(JSONWriter.Feature.ReferenceDetection), fieldName);
				if ((data instanceof Map || data instanceof List) && writer.isRefDetect(object)
						&& writer.getPath(object).equals(writer.getPath())) {
					// 用数据容器替换当前字段的引用身份；父写入器仍负责离开这个字段路径。
					if ("$".equals(writer.getPath())) {
						writer.setRootObject(data);
					} else {
						writer.popPath(object);
						String reference = fieldName instanceof Integer ? writer.setPath((Integer) fieldName, data)
								: writer.setPath(String.valueOf(fieldName), data);
						if (reference != null) {
							writer.writeReference(reference);
							return;
						}
					}
				}
				writer.writeAny(data);
			}
		};
	}
}
