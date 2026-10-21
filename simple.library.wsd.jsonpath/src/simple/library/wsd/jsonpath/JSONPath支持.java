package simple.library.wsd.jsonpath;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.JsonPathException;
import com.jayway.jsonpath.spi.json.JsonSmartJsonProvider;
import com.jayway.jsonpath.spi.mapper.JsonSmartMappingProvider;

import net.minidev.json.JSONValue;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

/**
 * 本库的参数检查、JSON 模型复制及原生 JSON 互通边界，不向 Simple 导出。
 *
 * <p>只接受 JSON 标准值；配置固定在本库内，不改变 JsonPath 全局默认配置。</p>
 *
 * @author 树先生 xhwsd@qq.com
 */
final class JSONPath支持 {

	/* 解析配置：限定在本库内部，不改动进程级默认配置。 */

	/**
	 * 本库独立配置；不使用 Android 不兼容的 JsonOrgJsonProvider。
	 */
	static final Configuration CONFIG = Configuration.builder()
			.jsonProvider(new JsonSmartJsonProvider())
			.mappingProvider(new JsonSmartMappingProvider())
			.build();

	/**
	 * 静态辅助类。
	 */
	private JSONPath支持() {
	}

	/* 参数及表达式检查：把上游错误统一映射为非法参数错误。 */

	/**
	 * 检查文本参数不为空引用，允许空文本。
	 *
	 * @param text 参数文本。
	 * @param name 参数名称。
	 * @return 原文本，允许空文本。
	 * @throws IllegalArgumentException 文本参数为空引用。
	 */
	static String text(String text, String name) {
		if (text == null) {
			throw new IllegalArgumentException(name + "不得为空引用。");
		}

		return text;
	}

	/**
	 * 编译表达式，统一对外参数错误。
	 *
	 * @param path JSONPath 表达式。
	 * @return 已编译路径。
	 * @throws IllegalArgumentException 路径为空引用、全为空白或表达式无效。
	 */
	static JsonPath path(String path) {
		text(path, "路径");
		if (path.trim().length() == 0) {
			throw new IllegalArgumentException("路径不得为空白。");
		}

		try {
			return JsonPath.compile(path);
		} catch (JsonPathException error) {
			throw failure("路径表达式无效", error);
		}
	}

	/* JSON 文本处理：解析与序列化均保留标准 JSON 类型。 */

	/**
	 * 严格解析 JSON 文本，包括标量根值。
	 *
	 * @param json JSON 文本。
	 * @return JSON 标准模型；JSON null 对应 Java 空引用。
	 * @throws IllegalArgumentException 文本为空引用或不是合法 JSON。
	 */
	static Object parse(String json) {
		try {
			return new JSONParser(JSONParser.MODE_STRICTEST).parse(text(json, "JSON文本"));
		} catch (ParseException error) {
			throw failure("JSON文本无效", error);
		}
	}

	/**
	 * 将普通错误转换为 Simple 可处理的非法参数错误。
	 *
	 * @param message 操作说明。
	 * @param error 原始原因。
	 * @return 包含原因链的错误。
	 */
	static IllegalArgumentException failure(String message, Exception error) {
		return new IllegalArgumentException(message + "：" + error.getMessage(), error);
	}

	/**
	 * 将 JSON 标准模型序列化为紧凑文本。
	 *
	 * @param value 已校验的模型。
	 * @return JSON 文本，包括引号及 null 字面量。
	 */
	static String json(Object value) {
		return JSONValue.toJSONString(value);
	}

	/* 模型复制：隔离输入、查询快照和写入事务中的可变容器。 */

	/**
	 * 深复制 JSON 模型，同时支持 Android 原生对象和数组。
	 *
	 * @param value 源模型。
	 * @return 独立的 Map/List/标量模型。
	 * @throws IllegalArgumentException 包含循环引用、非文本属性名、非有限浮点数或不支持的类型。
	 */
	static Object copy(Object value) {
		return copy(value, new IdentityHashMap<Object, Boolean>());
	}

	/**
	 * 复制一层模型；检测当前递归链中的循环引用。
	 *
	 * @param value 当前值。
	 * @param active 正在复制的容器。
	 * @return 独立模型；同一容器在不同分支出现时分别复制。
	 * @throws IllegalArgumentException 数据不是受支持的 JSON 模型，或原生 JSON 读取失败。
	 */
	private static Object copy(Object value, IdentityHashMap<Object, Boolean> active) {
		// 先统一 null 和标准标量，再处理需要逐层复制的容器。
		if (value == null || value == JSONObject.NULL) {
			return null;
		}
		if (value instanceof String || value instanceof Boolean) {
			return value;
		}
		if (value instanceof Number) {
			double number = ((Number) value).doubleValue();
			if ((value instanceof Double || value instanceof Float) && (Double.isNaN(number) || Double.isInfinite(number))) {
				throw new IllegalArgumentException("JSON数值不得为 NaN 或无穷大。");
			}
			return value;
		}

		// 只检查当前祖先链，允许同一容器由不同分支引用。
		if (active.put(value, Boolean.TRUE) != null) {
			throw new IllegalArgumentException("JSON包含循环引用。");
		}

		try {
			// 原生 JSON 容器转为 Map/List，避免依赖 Android 之外的 org.json 接口。
			if (value instanceof JSONObject) {
				JSONObject object = (JSONObject) value;
				Map<String, Object> result = new LinkedHashMap<String, Object>();
				Iterator<String> keys = object.keys();
				while (keys.hasNext()) {
					String key = keys.next();
					result.put(key, copy(object.get(key), active));
				}
				return result;
			}
			if (value instanceof JSONArray) {
				JSONArray array = (JSONArray) value;
				List<Object> result = new ArrayList<Object>();
				for (int i = 0; i < array.length(); i++) {
					result.add(copy(array.get(i), active));
				}
				return result;
			}

			// 上游模型同样深复制；属性名必须保持为 JSON 文本键。
			if (value instanceof Map) {
				Map<String, Object> result = new LinkedHashMap<String, Object>();
				for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
					if (!(entry.getKey() instanceof String)) {
						throw new IllegalArgumentException("JSON对象属性名必须为文本。");
					}
					result.put((String) entry.getKey(), copy(entry.getValue(), active));
				}
				return result;
			}
			if (value instanceof List) {
				List<Object> result = new ArrayList<Object>();
				for (Object item : (List<?>) value) {
					result.add(copy(item, active));
				}
				return result;
			}

			throw new IllegalArgumentException("不支持的JSON值类型：" + value.getClass().getName());
		} catch (JSONException error) {
			throw failure("读取JSON值失败", error);
		} finally {
			// 离开当前容器后移出祖先链，不把重复引用误判为循环。
			active.remove(value);
		}
	}

	/* 运行库互通及数值读取：不把对象、标量或 null 隐式混用。 */

	/**
	 * 将独立模型转换为 Android JSONObject/JSONArray。
	 *
	 * @param value 已校验的 JSON 值。
	 * @return 原生 JSON 模型，JSON null 使用 JSONObject.NULL。
	 * @throws IllegalArgumentException 原生 JSON 容器构造或写入失败。
	 */
	static Object nativeValue(Object value) {
		if (value == null) {
			return JSONObject.NULL;
		}
		try {
			if (value instanceof Map) {
				JSONObject result = new JSONObject();
				for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
					result.put((String) entry.getKey(), nativeValue(entry.getValue()));
				}
				return result;
			}
			if (value instanceof List) {
				JSONArray result = new JSONArray();
				for (Object item : (List<?>) value) {
					result.put(nativeValue(item));
				}
				return result;
			}
			return value;
		} catch (JSONException error) {
			throw failure("转换JSON值失败", error);
		}
	}

	/**
	 * 校验并取得 JSON 数字。
	 *
	 * @param value 当前值。
	 * @return 按当前数值文本构造的十进制值，不恢复输入此前已丢失的精度。
	 * @throws IllegalArgumentException 当前值不是数值，或其文本不能转换为十进制数。
	 */
	static BigDecimal number(Object value) {
		if (!(value instanceof Number)) {
			throw new IllegalArgumentException("结果不是数值。");
		}
		return new BigDecimal(value.toString());
	}
}
