package simple.library.wsd.fastjson2;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Deque;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;

import simple.runtime.variants.ObjectVariant;
import simple.runtime.variants.Variant;

/**
 * 类型、图复制、配置和读写边界；不向 Simple 暴露原生 Java 容器。
 *
 * @author 树先生 xhwsd@qq.com
 */
final class FJ2支持 {

	/**
	 * 库内基础读取注册表，避免改写应用的全局注册。
	 */
	private static final ObjectReaderProvider READERS = baseReaderProvider();

	/**
	 * 库内基础写入注册表，避免改写应用的全局注册。
	 */
	private static final ObjectWriterProvider WRITERS = baseWriterProvider();

	/**
	 * 当前线程正在执行的类型转换，用于拒绝相同方向的递归重入。
	 */
	private static final ThreadLocal<Set<String>> CODECS = new ThreadLocal<Set<String>>() {
		/**
		 * 为首次访问当前线程创建独立的重入记录。
		 *
		 * @return 当前线程使用的空记录集合。
		 */
		@Override
		protected Set<String> initialValue() {
			return new HashSet<String>();
		}
	};

	/**
	 * 当前线程正在执行的过滤器，按实例身份判断重入。
	 */
	private static final ThreadLocal<Set<FJ2过滤器>> FILTERS = new ThreadLocal<Set<FJ2过滤器>>() {
		/**
		 * 为首次访问当前线程创建独立的重入记录。
		 *
		 * @return 当前线程使用的空记录集合。
		 */
		@Override
		protected Set<FJ2过滤器> initialValue() {
			return Collections.newSetFromMap(new IdentityHashMap<FJ2过滤器, Boolean>());
		}
	};

	/**
	 * 静态工具类。
	 */
	private FJ2支持() {
	}

	/**
	 * 将 Java 和上游异常转换为 Simple 可以处理的异常。
	 *
	 * @param error 原始异常。
	 * @return 保留原因的非法参数异常或原有 Simple 运行错误。
	 */
	static RuntimeException failure(RuntimeException error) {
		if (error instanceof IllegalArgumentException || error instanceof simple.runtime.errors.运行错误) {
			return error;
		}
		return new IllegalArgumentException("Fastjson2 操作失败：" + error.getMessage(), error);
	}

	/**
	 * 检查非空引用文本。
	 *
	 * @param value 文本。
	 * @return 原文本。
	 */
	static String text(String value) {
		if (value == null) {
			throw new IllegalArgumentException("文本不得为空引用。");
		}
		return value;
	}

	/**
	 * 检查非空白输入。
	 *
	 * @param value 输入文本。
	 */
	static void nonempty(String value) {
		if (text(value).trim().length() == 0) {
			throw new IllegalArgumentException("JSON 文本不得为空白。");
		}
	}

	/**
	 * 校验配置引用。
	 *
	 * @param value 待校验配置。
	 * @param <T>   配置类型。
	 * @return 原配置。
	 */
	static <T> T require(T value) {
		if (value == null) {
			throw new IllegalArgumentException("配置不得为空引用。");
		}
		return value;
	}

	/**
	 * 验证时区，避免错误名称被静默替换为 GMT。
	 *
	 * @param value 时区 ID。
	 */
	static void zone(String value) {
		text(value);
		if (!Arrays.asList(TimeZone.getAvailableIDs()).contains(value)) {
			throw new IllegalArgumentException("未知时区：" + value);
		}
	}

	/**
	 * 检查有限浮点数。
	 *
	 * @param value 浮点数。
	 * @return 原浮点数。
	 */
	static double finite(double value) {
		if (Double.isInfinite(value) || Double.isNaN(value)) {
			throw new IllegalArgumentException("JSON 数值必须有限。");
		}
		return value;
	}

	/**
	 * 复制字节参数。
	 *
	 * @param value 非空字节集。
	 * @return 独立字节集。
	 */
	static byte[] bytes(byte[] value) {
		if (value == null) {
			throw new IllegalArgumentException("字节集不得为空。");
		}
		return value.clone();
	}

	/**
	 * 提取类型化值。
	 *
	 * @param value 类型化值。
	 * @return 内部值，空引用表示 null。
	 */
	static Object raw(FJ2值 value) {
		return value == null ? null : value.value;
	}

	/**
	 * 区分所有公开值类型。
	 *
	 * @param value 内部值。
	 * @return 类型标识。
	 */
	static int kind(Object value) {
		if (value == null) {
			return 0;
		}
		if (value instanceof Map) {
			return 1;
		}
		if (value instanceof List) {
			return 2;
		}
		if (value instanceof String) {
			return 3;
		}
		if (value instanceof Number) {
			return 4;
		}
		if (value instanceof Boolean) {
			return 5;
		}
		if (value instanceof byte[]) {
			return 6;
		}
		if (value instanceof Date) {
			return 7;
		}
		throw new IllegalArgumentException("不支持的 JSON 值类型：" + value.getClass().getName());
	}

	/**
	 * 校验并取得对象容器。
	 *
	 * @param value 内部值。
	 * @return 对象容器。
	 */
	@SuppressWarnings("unchecked")
	static Map<String, Object> object(Object value) {
		if (!(value instanceof Map)) {
			throw new IllegalArgumentException("当前值不是对象。");
		}
		return (Map<String, Object>) value;
	}

	/**
	 * 校验并取得数组容器。
	 *
	 * @param value 内部值。
	 * @return 数组容器。
	 */
	@SuppressWarnings("unchecked")
	static List<Object> array(Object value) {
		if (!(value instanceof List)) {
			throw new IllegalArgumentException("当前值不是数组。");
		}
		return (List<Object>) value;
	}

	/**
	 * 精确转换数值，不从文本或逻辑隐式转换。
	 *
	 * @param value 数值。
	 * @return 精确十进制数。
	 */
	static BigDecimal number(Object value) {
		if (!(value instanceof Number)) {
			throw new IllegalArgumentException("当前值不是数值。");
		}
		return new BigDecimal(value.toString());
	}

	/**
	 * 复制完整值图，保留重复和循环引用关系。
	 *
	 * @param value 来源值。
	 * @return 独立图。
	 */
	static Object copy(Object value) {
		return copy(value, new IdentityHashMap<Object, Object>(), 0);
	}

	/**
	 * 通过显式任务栈复制已校验值图，保留重复和循环引用。
	 *
	 * @param value 来源值。
	 * @param memo  已复制对象。
	 * @param depth 当前深度。
	 * @return 独立值。
	 */
	static Object copy(Object value, IdentityHashMap<Object, Object> memo, int depth) {
		Deque<Object[]> tasks = new ArrayDeque<Object[]>();
		Object result = copyPart(value, memo, tasks, depth);
		while (!tasks.isEmpty()) {
			Object[] task = tasks.pop();
			Object source = task[0];
			Object target = task[1];
			int current = (Integer) task[2];
			if (source instanceof Map) {
				for (Map.Entry<?, ?> item : ((Map<?, ?>) source).entrySet()) {
					if (!(item.getKey() instanceof String)) {
						throw new IllegalArgumentException("JSON 对象键必须是文本。");
					}
					object(target).put((String) item.getKey(), copyPart(item.getValue(), memo, tasks, current + 1));
				}
			} else {
				for (Object item : array(source)) {
					array(target).add(copyPart(item, memo, tasks, current + 1));
				}
			}
		}
		return result;
	}

	/**
	 * 创建读取上下文。
	 *
	 * @param config 读取配置。
	 * @return 独立上下文。
	 */
	static JSONReader.Context readContext(FJ2读配置 config) {
		return readContext(config, null);
	}

	/**
	 * 创建带符号表的读取上下文。
	 *
	 * @param config  读取配置。
	 * @param symbols 可选符号表。
	 * @return 独立上下文。
	 */
	static JSONReader.Context readContext(FJ2读配置 config, FJ2符号表 symbols) {
		require(config);
		JSONReader.Feature[] features = config.features.toArray(new JSONReader.Feature[0]);
		JSONReader.Context context = new JSONReader.Context(readerProvider(config),
				symbols == null ? null : symbols.table, features);
		context.setDateFormat(config.dateFormat);
		context.setTimeZone(TimeZone.getTimeZone(config.timeZone));
		context.setZoneId(com.alibaba.fastjson2.time.ZoneId.of(config.timeZone));
		context.setMaxLevel(config.maxLevel);
		context.setFeatures(JSONReader.Feature.of(features));
		return context;
	}

	/**
	 * 创建带同步过滤适配器的写入上下文。
	 *
	 * @param config 写入配置。
	 * @return 独立上下文。
	 */
	static JSONWriter.Context writeContext(FJ2写配置 config) {
		require(config);
		JSONWriter.Context context = JSONFactory.createWriteContext(writerProvider(config),
				config.features.toArray(new JSONWriter.Feature[0]));
		long mask = 0;
		for (JSONWriter.Feature feature : config.features) {
			mask |= feature.mask;
		}
		context.setFeatures(mask);
		context.setDateFormat(config.dateFormat);
		context.setTimeZone(TimeZone.getTimeZone(config.timeZone));
		context.setZoneId(com.alibaba.fastjson2.time.ZoneId.of(config.timeZone));
		return context;
	}

	/**
	 * 从样本提取普通业务对象类型，避免注册基础值导致递归编码。
	 *
	 * @param template 类型样本。
	 * @return 普通对象类型。
	 */
	static Class<?> modelType(Variant template) {
		Object value = unbox(template);
		if (value == null || value instanceof String || value instanceof Number || value instanceof Boolean
				|| value instanceof Date || value instanceof Map || value instanceof Collection
				|| value.getClass().isArray() || value instanceof FJ2值 || value instanceof Class
				|| value.getClass() == Object.class || value instanceof Variant) {
			throw new IllegalArgumentException("类型样本必须是已创建的普通业务对象。");
		}
		return value.getClass();
	}

	/**
	 * 将用户解码结果校验为注册类型。
	 *
	 * @param reader  当前读取器。
	 * @param type    目标类型。
	 * @param adapter 同步转换器。
	 * @return 用户创建的新对象。
	 */
	static Object decode(JSONReader reader, Class<?> type, FJ2类型转换器 adapter) {
		Object data = reader.readAny();
		return decoded(data, type, adapter);
	}

	/**
	 * 校验用户解码结果的实际类型。
	 *
	 * @param data    输入值图。
	 * @param type    目标类型。
	 * @param adapter 用户转换器。
	 * @return 同类型结果或空引用。
	 */
	static Object decoded(Object data, Class<?> type, FJ2类型转换器 adapter) {
		String key = type.getName() + ":decode";
		enterCodec(key);
		try {
			Object result = unbox(adapter.解码(new FJ2值(data)));
			if (result != null && !type.isInstance(result)) {
				throw new IllegalArgumentException("类型转换器返回了错误类型：" + result.getClass().getName());
			}
			return result;
		} finally {
			leaveCodec(key);
		}
	}

	/**
	 * 从值图映射普通对象，避开上游 Android JSONB 中文字段哈希读取差异。
	 *
	 * @param data   输入值图。
	 * @param type   目标对象类型。
	 * @param config 独立读取配置。
	 * @return 新对象。
	 */
	static Object objectFromTree(Object data, Class<?> type, FJ2读配置 config) {
		require(config);
		FJ2类型转换器 adapter = config.adapters.get(type);
		if (adapter != null) {
			return decoded(data, type, adapter);
		}
		// 用明确的文本表示桥接日期和二进制，后续仍由上游对象映射器处理字段、嵌套类型及配置。
		FJ2写配置 bridge = new FJ2写配置();
		bridge.dateFormat = "millis";
		try (JSONReader reader = JSONReader.of(stringify(data, bridge), readContext(config))) {
			Object result = reader.read(type);
			reader.handleResolveTasks(result);
			if (!reader.isEnd()) {
				throw new IllegalArgumentException("对象数据未完整读取。");
			}
			return result;
		}
	}

	/**
	 * 创建支持运行库数据字段的独立读取注册表。
	 *
	 * @return 不依赖上游全局状态的注册表。
	 */
	private static ObjectReaderProvider baseReaderProvider() {
		return new ObjectReaderProvider() {
			/**
			 * 为运行库字段使用数据读取器，其余类型使用上游对象映射器。
			 *
			 * @param type       目标声明类型。
			 * @param fieldBased 是否按字段映射。
			 * @return 目标类型的读取器。
			 */
			@Override
			public ObjectReader getObjectReader(Type type, boolean fieldBased) {
				ObjectReader reader = FJ2互转.reader(type);
				return reader == null ? super.getObjectReader(type, fieldBased) : reader;
			}
		};
	}

	/**
	 * 创建支持运行库变体及数据容器的独立写入注册表。
	 *
	 * @return 已安装运行库适配器的注册表。
	 */
	private static ObjectWriterProvider baseWriterProvider() {
		ObjectWriterProvider provider = new ObjectWriterProvider();
		FJ2互转.registerWriters(provider);
		return provider;
	}

	/**
	 * 创建独立读取注册表，兼容普通及 FieldBased 查找。
	 *
	 * @param config 读取配置。
	 * @return 本次调用的注册表，无自定义规则时复用库内基础注册表。
	 */
	static ObjectReaderProvider readerProvider(FJ2读配置 config) {
		if (config.adapters.isEmpty()) {
			return READERS;
		}
		final Map<Type, ObjectReader> readers = new HashMap<Type, ObjectReader>();
		for (Map.Entry<Class<?>, FJ2类型转换器> item : config.adapters.entrySet()) {
			final Class<?> type = item.getKey();
			final FJ2类型转换器 adapter = item.getValue();
			readers.put(type, new ObjectReader<Object>() {
				/**
				 * 将上游数据交给当前类型的解码回调，并校验回调结果类型。
				 *
				 * @param reader    当前上游读取器。
				 * @param fieldType 上游传入的声明类型。
				 * @param fieldName 上游传入的属性名或索引。
				 * @param features  本次调用携带的上游特性。
				 * @return 注册类型的新对象或空引用。
				 */
				@Override
				public Object readObject(JSONReader reader, Type fieldType, Object fieldName, long features) {
					return decode(reader, type, adapter);
				}

				/**
				 * 将上游数据交给当前类型的解码回调，并校验回调结果类型。
				 *
				 * @param reader    当前上游读取器。
				 * @param fieldType 上游传入的声明类型。
				 * @param fieldName 上游传入的属性名或索引。
				 * @param features  本次调用携带的上游特性。
				 * @return 注册类型的新对象或空引用。
				 */
				@Override
				public Object readJSONBObject(JSONReader reader, Type fieldType, Object fieldName, long features) {
					return decode(reader, type, adapter);
				}
			});
		}
		return new ObjectReaderProvider() {
			/**
			 * 优先使用当前配置注册的解码器，其余类型交给基础读取逻辑。
			 *
			 * @param type       需要查找的目标类型。
			 * @param fieldBased 是否按字段模式查找。
			 * @return 适用于目标类型及字段模式的读取器。
			 */
			@Override
			public ObjectReader getObjectReader(Type type, boolean fieldBased) {
				ObjectReader reader = readers.get(type);
				if (reader == null) {
					reader = FJ2互转.reader(type);
				}
				return reader == null ? super.getObjectReader(type, fieldBased) : reader;
			}
		};
	}

	/**
	 * 创建本次调用的写入注册表，不改写上游全局注册表。
	 *
	 * @param config 写入配置。
	 * @return 独立注册表或无自定义规则时的库内基础注册表。
	 */
	static ObjectWriterProvider writerProvider(FJ2写配置 config) {
		if (config.adapters.isEmpty() && !datePattern(config.dateFormat)) {
			return WRITERS;
		}
		ObjectWriterProvider provider = baseWriterProvider();
		if (datePattern(config.dateFormat)) {
			ObjectWriter<Object> dates = dateWriter(config.dateFormat, config.timeZone);
			for (Class<?> type : new Class<?>[] { Date.class, Calendar.class, GregorianCalendar.class }) {
				provider.register(type, dates, false);
				provider.register(type, dates, true);
			}
		}
		for (Map.Entry<Class<?>, FJ2类型转换器> item : config.adapters.entrySet()) {
			final FJ2类型转换器 adapter = item.getValue();
			final String key = item.getKey().getName() + ":encode";
			ObjectWriter<Object> writer = new ObjectWriter<Object>() {
				/**
				 * 调用当前类型的编码回调，并写入回调返回的值树。
				 *
				 * @param writer    当前上游写入器。
				 * @param object    待写入的数据对象。
				 * @param fieldName 上游传入的属性名或索引。
				 * @param fieldType 上游传入的声明类型。
				 * @param features  本次调用携带的上游特性。
				 */
				@Override
				public void write(JSONWriter writer, Object object, Object fieldName, Type fieldType, long features) {
					enterCodec(key);
					try {
						writer.writeAny(raw(adapter.编码(ObjectVariant.getObjectVariant(object))));
					} finally {
						leaveCodec(key);
					}
				}
			};
			provider.register(item.getKey(), writer, false);
			provider.register(item.getKey(), writer, true);
		}
		return provider;
	}

	/**
	 * 解析完整文本并拒绝尾随内容。
	 *
	 * @param text   JSON 文本。
	 * @param config 读取配置。
	 * @return 原生值。
	 */
	static Object parse(String text, FJ2读配置 config) {
		nonempty(text);
		try (JSONReader reader = JSONReader.of(text, readContext(config))) {
			return readAll(reader);
		}
	}

	/**
	 * 完整读取一个根值并解析引用。
	 *
	 * @param reader 文本或 JSONB 读取器。
	 * @return 已解析的根值。
	 */
	static Object readAll(JSONReader reader) {
		if (reader.isEnd()) {
			throw new IllegalArgumentException("输入中没有 JSON 值。");
		}
		Object value = reader.readAny();
		reader.handleResolveTasks(value);
		if (!reader.isEnd() || (!reader.isJSONB() && reader.hasComma())) {
			throw new IllegalArgumentException("根值后存在多余内容或逗号。");
		}
		return value;
	}

	/**
	 * 按独立配置写文本。
	 *
	 * @param value  待写值。
	 * @param config 配置。
	 * @return JSON 文本。
	 */
	static String stringify(Object value, FJ2写配置 config) {
		require(config);
		if (config.filter != null) {
			value = filterForWrite(value, config);
			config = config.复制();
			config.filter = null;
		}
		ensureSafe(value, config);
		try (JSONWriter writer = JSONWriter.of(writeContext(config))) {
			writer.setRootObject(value);
			writer.writeAny(value);
			return writer.toString();
		}
	}

	/**
	 * 按独立配置写 JSONB。
	 *
	 * @param value   待写值。
	 * @param config  配置。
	 * @param symbols 可选外部符号表。
	 * @return JSONB 字节。
	 */
	static byte[] binary(Object value, FJ2写配置 config, FJ2符号表 symbols) {
		require(config);
		if (config.filter != null) {
			value = filterForWrite(value, config);
			config = config.复制();
			config.filter = null;
		}
		ensureSafe(value, config);
		try (JSONWriter writer = JSONWriter.ofJSONB(writeContext(config), symbols == null ? null : symbols.table)) {
			writer.setRootObject(value);
			writer.writeAny(value);
			return writer.getBytes();
		}
	}

	/**
	 * 在独立值图上按名称、保留和值的顺序执行 JSONB 过滤。
	 *
	 * @param value    当前值。
	 * @param callback 用户过滤器。
	 * @param memo     已处理的容器。
	 * @return 过滤后的独立值图。
	 */
	static Object filtered(Object value, FJ2过滤器 callback, IdentityHashMap<Object, Object> memo) {
		Deque<Object[]> tasks = new ArrayDeque<Object[]>();
		Object result = filterPart(value, memo, tasks, 0);
		while (!tasks.isEmpty()) {
			Object[] task = tasks.pop();
			Object source = task[0];
			Object target = task[1];
			int depth = (Integer) task[2];
			if (source instanceof Map) {
				Set<String> names = new HashSet<String>();
				for (Map.Entry<String, Object> item : object(source).entrySet()) {
					String name = filterName(callback, item.getKey());
					if (!filterKeep(callback, name, new FJ2值(item.getValue()))) {
						continue;
					}
					if (!names.add(name)) {
						throw new IllegalArgumentException("过滤器产生重名属性：" + name);
					}
					FJ2值 snapshot = new FJ2值(item.getValue());
					FJ2值 changed = filterValue(callback, name, snapshot);
					Object replacement = raw(changed);
					if (changed == snapshot && sameGraph(item.getValue(), replacement)) {
						replacement = item.getValue();
					}
					object(target).put(name, filterPart(replacement, memo, tasks, depth + 1));
				}
			} else {
				for (Object item : array(source)) {
					array(target).add(filterPart(item, memo, tasks, depth + 1));
				}
			}
		}
		return result;
	}

	/**
	 * 比较值图的类型、内容、顺序及可变节点引用关系，不通过文本序列化判断变化。
	 *
	 * @param expected 来源值图。
	 * @param actual   回调返回的快照。
	 * @return 两图在类型及结构上完全一致时为真。
	 */
	static boolean sameGraph(Object expected, Object actual) {
		IdentityHashMap<Object, Object> forward = new IdentityHashMap<Object, Object>();
		IdentityHashMap<Object, Object> reverse = new IdentityHashMap<Object, Object>();
		Deque<Object[]> tasks = new ArrayDeque<Object[]>();
		tasks.push(new Object[] { expected, actual });
		while (!tasks.isEmpty()) {
			Object[] task = tasks.pop();
			Object left = task[0];
			Object right = task[1];
			int type = kind(left);
			if (type != kind(right)) {
				return false;
			}
			if (type == 1 || type == 2 || type == 6 || type == 7) {
				if (forward.containsKey(left)) {
					if (forward.get(left) != right) {
						return false;
					}
					continue;
				}
				if (reverse.containsKey(right)) {
					return false;
				}
				forward.put(left, right);
				reverse.put(right, left);
			}
			if (type == 1) {
				Map<String, Object> first = object(left);
				Map<String, Object> second = object(right);
				if (first.size() != second.size()) {
					return false;
				}
				Iterator<Map.Entry<String, Object>> other = second.entrySet().iterator();
				for (Map.Entry<String, Object> entry : first.entrySet()) {
					Map.Entry<String, Object> match = other.next();
					if (!entry.getKey().equals(match.getKey())) {
						return false;
					}
					tasks.push(new Object[] { entry.getValue(), match.getValue() });
				}
			} else if (type == 2) {
				List<Object> first = array(left);
				List<Object> second = array(right);
				if (first.size() != second.size()) {
					return false;
				}
				for (int i = 0; i < first.size(); i++) {
					tasks.push(new Object[] { first.get(i), second.get(i) });
				}
			} else if (type == 6) {
				if (!Arrays.equals((byte[]) left, (byte[]) right)) {
					return false;
				}
			} else if (type == 7) {
				if (((Date) left).getTime() != ((Date) right).getTime()) {
					return false;
				}
			} else if (left != null && (left.getClass() != right.getClass() || !left.equals(right))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 解包 Simple 数据并隔离可变容器，普通业务对象交给对象映射器。
	 *
	 * @param value Simple 变体。
	 * @return Java 数据。
	 */
	static Object unbox(Variant value) {
		return FJ2互转.toNative(value);
	}

	/**
	 * 校验并复制非空字节输入。
	 *
	 * @param value 字节集。
	 * @return 独立非空副本。
	 */
	static byte[] nonemptyBytes(byte[] value) {
		byte[] result = bytes(value);
		if (result.length == 0) {
			throw new IllegalArgumentException("输入字节不得为空。");
		}
		return result;
	}

	/**
	 * 为一个值分配复制目标，把子容器留给显式任务栈处理。
	 *
	 * @param value 来源值。
	 * @param memo  复制记录。
	 * @param tasks 待复制容器。
	 * @param depth 当前深度。
	 * @return 复制结果或已分配的容器。
	 */
	static Object copyPart(Object value, IdentityHashMap<Object, Object> memo, Deque<Object[]> tasks, int depth) {
		if (memo.containsKey(value)) {
			return memo.get(value);
		}
		if (depth > 2048) {
			throw new IllegalArgumentException("值嵌套超过 2048 层。");
		}
		int type = kind(value);
		Object result = value;
		if (type == 1 || type == 2) {
			result = type == 1 ? new JSONObject() : new JSONArray();
			memo.put(value, result);
			tasks.push(new Object[] { value, result, depth });
		} else if (type == 6) {
			result = ((byte[]) value).clone();
			memo.put(value, result);
		} else if (type == 7) {
			result = new Date(((Date) value).getTime());
			memo.put(value, result);
		} else if (value instanceof Float || value instanceof Double) {
			finite(((Number) value).doubleValue());
		}
		return result;
	}

	/**
	 * 分配过滤结果容器并限制回调不断扩张值图的情况。
	 *
	 * @param value 来源值。
	 * @param memo  已处理容器。
	 * @param tasks 待处理容器。
	 * @param depth 当前深度。
	 * @return 过滤容器或标量副本。
	 */
	static Object filterPart(Object value, IdentityHashMap<Object, Object> memo, Deque<Object[]> tasks, int depth) {
		if (memo.containsKey(value)) {
			return memo.get(value);
		}
		if (depth > 512) {
			throw new IllegalArgumentException("过滤结果嵌套超过 512 层。");
		}
		if (value instanceof Map || value instanceof List) {
			Object result = value instanceof Map ? new JSONObject() : new JSONArray();
			memo.put(value, result);
			tasks.push(new Object[] { value, result, depth });
			return result;
		}
		return copy(value);
	}

	/**
	 * 在最终编码前对独立值图执行属性过滤；普通对象先按类型映射转换为值图。
	 *
	 * @param value  数据对象或值图。
	 * @param config 包含过滤器的配置。
	 * @return 独立过滤结果。
	 */
	static Object filterForWrite(Object value, FJ2写配置 config) {
		Object data;
		if (isValueGraph(value)) {
			// 已有值图不经过中间编码，空值及日期、字节和引用关系直接交给过滤器。
			data = copy(value);
		} else {
			FJ2写配置 plain = config.复制();
			plain.filter = null;
			// 保留原字段，最终是否输出 null 必须在过滤器有机会补值之后判断。
			plain.features.add(JSONWriter.Feature.WriteNulls);
			try (JSONReader reader = JSONReader.ofJSONB(binary(value, plain, null), readContext(new FJ2读配置()))) {
				data = readAll(reader);
			}
		}
		return filtered(data, config.filter, new IdentityHashMap<Object, Object>());
	}

	/**
	 * 检查数据是否只含可直接复制的 JSON 值，循环容器只访问一次。
	 *
	 * @param value 待过滤的数据。
	 * @return 不含需要普通对象映射的节点时为真。
	 */
	private static boolean isValueGraph(Object value) {
		if (value == null) {
			return true;
		}
		Deque<Object> pending = new ArrayDeque<Object>();
		IdentityHashMap<Object, Boolean> visited = new IdentityHashMap<Object, Boolean>();
		pending.push(value);
		while (!pending.isEmpty()) {
			Object current = pending.pop();
			if (current instanceof Map || current instanceof List) {
				if (visited.put(current, true) != null) {
					continue;
				}
				Iterable<?> children = current instanceof Map ? ((Map<?, ?>) current).values() : (List<?>) current;
				for (Object child : children) {
					if (child != null) {
						pending.push(child);
					}
				}
			} else if (!(current instanceof String || current instanceof Number || current instanceof Boolean
					|| current instanceof byte[] || current instanceof Date)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 进入单项类型转换回调，拒绝同类型同方向重入。
	 *
	 * @param key 类型与转换方向。
	 */
	static void enterCodec(String key) {
		if (!CODECS.get().add(key)) {
			throw new IllegalArgumentException("类型转换器不允许重入：" + key);
		}
	}

	/**
	 * 退出转换回调并释放线程状态。
	 *
	 * @param key 类型与转换方向。
	 */
	static void leaveCodec(String key) {
		Set<String> active = CODECS.get();
		active.remove(key);
		if (active.isEmpty()) {
			CODECS.remove();
		}
	}

	/**
	 * 进入过滤回调，防止回调再次触发自身。
	 *
	 * @param callback 当前过滤器。
	 */
	static void enterFilter(FJ2过滤器 callback) {
		if (!FILTERS.get().add(callback)) {
			throw new IllegalArgumentException("过滤器不允许重入自身。");
		}
	}

	/**
	 * 退出过滤回调并释放线程状态。
	 *
	 * @param callback 当前过滤器。
	 */
	static void leaveFilter(FJ2过滤器 callback) {
		Set<FJ2过滤器> active = FILTERS.get();
		active.remove(callback);
		if (active.isEmpty()) {
			FILTERS.remove();
		}
	}

	/**
	 * 执行保留属性回调。
	 *
	 * @param callback 用户接口。
	 * @param name     当前名称。
	 * @param value    独立快照。
	 * @return 是否保留。
	 */
	static boolean filterKeep(FJ2过滤器 callback, String name, FJ2值 value) {
		enterFilter(callback);
		try {
			return callback.保留属性(name, value);
		} finally {
			leaveFilter(callback);
		}
	}

	/**
	 * 执行名称转换回调。
	 *
	 * @param callback 用户接口。
	 * @param name     原名称。
	 * @return 非空引用的新名称。
	 */
	static String filterName(FJ2过滤器 callback, String name) {
		enterFilter(callback);
		try {
			return text(callback.转换名称(name));
		} finally {
			leaveFilter(callback);
		}
	}

	/**
	 * 执行值转换回调。
	 *
	 * @param callback 用户接口。
	 * @param name     当前名称。
	 * @param value    独立快照。
	 * @return 用户返回的值，允许空引用。
	 */
	static FJ2值 filterValue(FJ2过滤器 callback, String name, FJ2值 value) {
		enterFilter(callback);
		try {
			return callback.转换值(name, value);
		} finally {
			leaveFilter(callback);
		}
	}

	/**
	 * 判断是否需要按显式日期模板格式化。
	 *
	 * @param format 日期模板或上游内置格式名。
	 * @return 是否为日期模板。
	 */
	static boolean datePattern(String format) {
		return !format.isEmpty() && !format.equals("millis") && !format.equals("unixtime") && !format.equals("iso8601");
	}

	/**
	 * 创建遵守配置时区的日期写入器，修正上游 Android 模板格式化使用默认时区的行为。
	 *
	 * @param format 日期模板。
	 * @param zone   时区 ID。
	 * @return 本次调用专用的日期写入器。
	 */
	static ObjectWriter<Object> dateWriter(final String format, final String zone) {
		return new ObjectWriter<Object>() {
			/**
			 * 按当前模板和时区写入日期；JSONB 使用毫秒时间戳。
			 *
			 * @param writer    当前上游写入器。
			 * @param object    待写入的数据对象。
			 * @param fieldName 上游传入的属性名或索引。
			 * @param fieldType 上游传入的声明类型。
			 * @param features  本次调用携带的上游特性。
			 */
			@Override
			public void write(JSONWriter writer, Object object, Object fieldName, Type fieldType, long features) {
				if (object == null) {
					writer.writeNull();
					return;
				}
				long millis = object instanceof Calendar ? ((Calendar) object).getTimeInMillis()
						: ((Date) object).getTime();
				if (writer.jsonb) {
					writer.writeMillis(millis);
				} else {
					java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat(format);
					formatter.setTimeZone(TimeZone.getTimeZone(zone));
					writer.writeString(formatter.format(new Date(millis)));
				}
			}
		};
	}

	/**
	 * 关闭引用检测时提前拒绝容器图中的循环，避免交给上游递归导致栈溢出。
	 *
	 * @param value  数据值或已解包数组。
	 * @param config 写入配置。
	 */
	static void ensureSafe(Object value, FJ2写配置 config) {
		if (config.features.contains(JSONWriter.Feature.ReferenceDetection)) {
			return;
		}
		IdentityHashMap<Object, Boolean> colors = new IdentityHashMap<Object, Boolean>();
		Deque<Object[]> tasks = new ArrayDeque<Object[]>();
		tasks.push(new Object[] { value, false });
		while (!tasks.isEmpty()) {
			Object[] task = tasks.pop();
			Object current = task[0];
			if (!(current instanceof Map) && !(current instanceof List) && !(current instanceof Object[])) {
				continue;
			}
			if ((Boolean) task[1]) {
				colors.put(current, true);
				continue;
			}
			Boolean previous = colors.get(current);
			if (previous != null) {
				if (!previous) {
					throw new IllegalArgumentException("值包含循环引用，请启用 ReferenceDetection。");
				}
				continue;
			}
			colors.put(current, false);
			tasks.push(new Object[] { current, true });
			Iterable<?> children = current instanceof Map ? ((Map<?, ?>) current).values()
					: current instanceof List ? (List<?>) current : Arrays.asList((Object[]) current);
			for (Object item : children) {
				tasks.push(new Object[] { item, false });
			}
		}
	}
}
