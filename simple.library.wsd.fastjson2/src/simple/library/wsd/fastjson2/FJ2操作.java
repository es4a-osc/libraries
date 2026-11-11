package simple.library.wsd.fastjson2;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.Date;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.collections.JSON值;
import simple.runtime.collections.集合;
import simple.runtime.variants.ObjectVariant;
import simple.runtime.variants.Variant;

/**
 * Fastjson2 Android 版入口：文本、字节、JSONB、类型化值和 Simple 对象互通；不访问网络或文件，复杂数据应在后台串行处理。
 * 普通对象内的运行库数据字段不接受内部引用；编码同样检查，循环图使用数据转值与到Simple数据整体互转。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class FJ2操作 {

	/**
	 * 静态工具类。
	 */
	private FJ2操作() {
	}

	/**
	 * 返回封装使用的官方 JAR 版本。
	 *
	 * @return 所携带 JAR 的 Maven 发布版本。
	 */
	@SimpleFunction
	public static String 取依赖版本() {
		try {
			return "2.0.66.android5";
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 解析完整 JSON 文本，包括标量根值。
	 *
	 * @param text 完整 JSON 文本；不得为空白。
	 * @return 与来源互不共享修改的类型化值。
	 * @throws IllegalArgumentException 输入、范围、字符集或配置无效，或数据不是一个完整根值。
	 */
	@SimpleFunction
	public static FJ2值 解析文本(String text) {
		try {
			return 解析文本(text, new FJ2读配置());
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 按读取配置解析完整 JSON 文本。
	 *
	 * @param text   完整 JSON 文本。
	 * @param config 独立读取配置。
	 * @return 与来源互不共享修改的类型化值。
	 * @throws IllegalArgumentException 输入、范围、字符集或配置无效，或数据不是一个完整根值。
	 */
	@SimpleFunction
	public static FJ2值 解析文本(String text, FJ2读配置 config) {
		try {
			return new FJ2值(FJ2支持.parse(text, config));
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 解析 UTF-8 JSON 字节。
	 *
	 * @param bytes UTF-8 JSON 字节。
	 * @return 与来源互不共享修改的类型化值。
	 * @throws IllegalArgumentException 输入、范围、字符集或配置无效，或数据不是一个完整根值。
	 */
	@SimpleFunction
	public static FJ2值 解析UTF8(byte[] bytes) {
		try {
			return 解析字节(bytes, "UTF-8", new FJ2读配置());
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 按字符集和读取配置解析完整 JSON 字节。
	 *
	 * @param bytes   JSON 字节，不得为空。
	 * @param charset 有效字符集名称。
	 * @param config  独立读取配置。
	 * @return 与来源互不共享修改的类型化值。
	 * @throws IllegalArgumentException 输入、范围、字符集或配置无效，或数据不是一个完整根值。
	 */
	@SimpleFunction
	public static FJ2值 解析字节(byte[] bytes, String charset, FJ2读配置 config) {
		try {
			if (bytes == null) {
				throw new IllegalArgumentException("字节集不得为空。");
			}
			try (JSONReader reader = JSONReader.of(bytes, 0, bytes.length, Charset.forName(FJ2支持.text(charset)),
					FJ2支持.readContext(config))) {
				return new FJ2值(FJ2支持.readAll(reader));
			}
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 解析完整 JSONB 数据。
	 *
	 * @param bytes 完整 JSONB 字节。
	 * @return 与来源互不共享修改的类型化值。
	 * @throws IllegalArgumentException 输入、范围、字符集或配置无效，或数据不是一个完整根值。
	 */
	@SimpleFunction
	public static FJ2值 解析JSONB(byte[] bytes) {
		try {
			return 解析JSONB(bytes, new FJ2读配置(), null);
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 按配置和外部符号表解析 JSONB。
	 *
	 * @param bytes   完整 JSONB 字节。
	 * @param config  独立读取配置。
	 * @param symbols 编码时使用的同一外部符号表；未使用则为空。
	 * @return 与来源互不共享修改的类型化值。
	 * @throws IllegalArgumentException 输入、范围、字符集或配置无效，或数据不是一个完整根值。
	 */
	@SimpleFunction
	public static FJ2值 解析JSONB(byte[] bytes, FJ2读配置 config, FJ2符号表 symbols) {
		try {
			if (bytes == null || bytes.length == 0) {
				throw new IllegalArgumentException("JSONB 字节不得为空。");
			}
			JSONReader.Context context = FJ2支持.readContext(config);
			if (symbols != null) {
				context = FJ2支持.readContext(config, symbols);
			}
			try (JSONReader reader = JSONReader.ofJSONB(bytes, context)) {
				return new FJ2值(FJ2支持.readAll(reader));
			}
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 复制运行库 JSON值，之后双方修改互不影响。
	 *
	 * @param value 运行库对象或数组，不得为空。
	 * @return 与来源互不共享修改的类型化值。
	 * @throws IllegalArgumentException 运行库 JSON值 为空或其数据无法解析。
	 */
	@SimpleFunction
	public static FJ2值 运行库JSON转值(JSON值 value) {
		try {
			if (value == null) {
				throw new IllegalArgumentException("JSON值不得为空。");
			}
			return 解析文本(value.到文本());
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 检查完整 JSON 语法；采用上游解析规则，不等同于 JSON Schema 验证。
	 *
	 * @param text 待验证文本；空引用或空白返回假。
	 * @return 文本可解析为完整根值时为真，空输入或无效数据为假。
	 */
	@SimpleFunction
	public static boolean 是否有效JSON(String text) {
		if (text == null || text.trim().length() == 0) {
			return false;
		}
		try {
			FJ2值 value = 解析文本(text);
			return true;
		} catch (IllegalArgumentException invalid) {
			return false;
		}
	}

	/**
	 * 检查完整 JSON 语法；采用上游解析规则，不等同于 JSON Schema 验证。
	 *
	 * @param text 待验证文本；空引用或空白返回假。
	 * @return 完整根值为对象时为真，否则为假。
	 */
	@SimpleFunction
	public static boolean 是否JSON对象(String text) {
		if (text == null || text.trim().length() == 0) {
			return false;
		}
		try {
			FJ2值 value = 解析文本(text);
			return value.取类型() == FJ2值.类型_对象;
		} catch (IllegalArgumentException invalid) {
			return false;
		}
	}

	/**
	 * 检查完整 JSON 语法；采用上游解析规则，不等同于 JSON Schema 验证。
	 *
	 * @param text 待验证文本；空引用或空白返回假。
	 * @return 完整根值为数组时为真，否则为假。
	 */
	@SimpleFunction
	public static boolean 是否JSON数组(String text) {
		if (text == null || text.trim().length() == 0) {
			return false;
		}
		try {
			FJ2值 value = 解析文本(text);
			return value.取类型() == FJ2值.类型_数组;
		} catch (IllegalArgumentException invalid) {
			return false;
		}
	}

	/**
	 * 空类型化值；可变参数会复制。
	 *
	 * @return 新创建的类型化值。
	 */
	@SimpleFunction
	public static FJ2值 创建空() {
		try {
			return new FJ2值(null);
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 对象类型化值；可变参数会复制。
	 *
	 * @return 新创建的类型化值。
	 */
	@SimpleFunction
	public static FJ2值 创建对象() {
		try {
			return new FJ2值(new JSONObject());
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 数组类型化值；可变参数会复制。
	 *
	 * @return 新创建的类型化值。
	 */
	@SimpleFunction
	public static FJ2值 创建数组() {
		try {
			return new FJ2值(new JSONArray());
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 文本类型化值；可变参数会复制。
	 *
	 * @param value 非空引用文本。
	 * @return 新创建的类型化值。
	 * @throws IllegalArgumentException 文本为空引用。
	 */
	@SimpleFunction
	public static FJ2值 创建文本(String value) {
		try {
			return new FJ2值(FJ2支持.text(value));
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 整数类型化值；可变参数会复制。
	 *
	 * @param value 64 位整数。
	 * @return 新创建的类型化值。
	 */
	@SimpleFunction
	public static FJ2值 创建整数(long value) {
		try {
			return new FJ2值(value);
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 小数类型化值；可变参数会复制。
	 *
	 * @param value 有限双精度数；不接受 NaN 或无穷大。
	 * @return 新创建的类型化值。
	 * @throws IllegalArgumentException 数值为 NaN 或无穷大。
	 */
	@SimpleFunction
	public static FJ2值 创建小数(double value) {
		try {
			return new FJ2值(FJ2支持.finite(value));
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 精确数值类型化值；可变参数会复制。
	 *
	 * @param value 十进制数值文本，可包含指数。
	 * @return 新创建的类型化值。
	 * @throws IllegalArgumentException 文本为空或不是所请求的十进制数值。
	 */
	@SimpleFunction
	public static FJ2值 创建精确数值(String value) {
		try {
			return new FJ2值(new BigDecimal(FJ2支持.text(value)));
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 大整数类型化值；可变参数会复制。
	 *
	 * @param value 不含小数点的十进制整数文本。
	 * @return 新创建的类型化值。
	 * @throws IllegalArgumentException 文本为空或不是所请求的十进制数值。
	 */
	@SimpleFunction
	public static FJ2值 创建大整数(String value) {
		try {
			return new FJ2值(new BigInteger(FJ2支持.text(value)));
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 逻辑类型化值；可变参数会复制。
	 *
	 * @param value 逻辑值。
	 * @return 新创建的类型化值。
	 */
	@SimpleFunction
	public static FJ2值 创建逻辑(boolean value) {
		try {
			return new FJ2值(value);
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 字节类型化值；可变参数会复制。
	 *
	 * @param value 非空字节数组，内部会复制。
	 * @return 新创建的类型化值。
	 * @throws IllegalArgumentException 字节数组为空引用。
	 */
	@SimpleFunction
	public static FJ2值 创建字节(byte[] value) {
		try {
			return new FJ2值(FJ2支持.bytes(value));
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 日期类型化值；可变参数会复制。
	 *
	 * @param value Unix 毫秒时间戳。
	 * @return 新创建的类型化值。
	 */
	@SimpleFunction
	public static FJ2值 创建日期(long value) {
		try {
			return new FJ2值(new Date(value));
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 直接载入 Simple 标量、数组、集合、哈希表或本库值，保留字节、日期及容器引用关系。
	 *
	 * @param value 运行库数据；普通业务对象请使用对象转值并传入写配置。
	 * @return 与来源隔离的类型化值。
	 * @throws IllegalArgumentException 数据含普通业务对象、组件、无效键或非有限数值，或嵌套过深。
	 */
	@SimpleFunction
	public static FJ2值 数据转值(Variant value) {
		try {
			return new FJ2值(FJ2互转.toNative(value));
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 将 JSON 数组逐项映射为与样本同类型的新对象，并按原顺序放入运行库集合。
	 *
	 * @param value    JSON 数组，其中 null 项保留为空对象变体。
	 * @param template 已创建的普通业务对象，仅取实际类型，不修改样本。
	 * @param config   读取配置及可选类型转换器。
	 * @return 包含新业务对象的集合；任一项失败时抛错，不返回部分结果。
	 * @throws IllegalArgumentException 输入非数组、样本或配置无效，或任一项映射失败。
	 */
	@SimpleFunction
	public static 集合 数组转对象集(FJ2值 value, Variant template, FJ2读配置 config) {
		try {
			FJ2支持.require(config);
			Class<?> type = FJ2支持.modelType(template);
			集合 result = new 集合();
			for (Object item : FJ2支持.array(FJ2支持.raw(value))) {
				result.加入(
						ObjectVariant.getObjectVariant(item == null ? null : FJ2支持.objectFromTree(item, type, config)));
			}
			return result;
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 将 Simple 值序列化后转换为独立 JSON 树；不接受窗口或组件作为业务数据。
	 *
	 * @param value  Simple 标量、数组、集合、哈希表、普通对象或本库值。
	 * @param config 对象映射和过滤配置；普通对象字段可用 FieldBased。
	 * @return 与来源互不共享修改的类型化值。
	 * @throws IllegalArgumentException 数据、类型样本或配置无效，或对象转换失败。
	 */
	@SimpleFunction
	public static FJ2值 对象转值(Variant value, FJ2写配置 config) {
		try {
			Object raw = FJ2支持.unbox(value);
			return new FJ2值(FJ2支持.parse(FJ2支持.stringify(raw, config), new FJ2读配置()));
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 映射为与样本同类型的新对象；返回变体可赋给对应 Simple 类型，不根据 JSON 中的类名加载类型。
	 *
	 * @param value    JSON 树。
	 * @param template 已创建的目标普通对象，仅取实际类型，不改写此实例。
	 * @param config   读取配置；可通过 FieldBased 读写字段。
	 * @return 包含目标类型新对象的变体，空对象结果保持为空引用。
	 * @throws IllegalArgumentException 数据、类型样本或配置无效，或对象转换失败。
	 */
	@SimpleFunction
	public static Variant 值转对象(FJ2值 value, Variant template, FJ2读配置 config) {
		try {
			Object sample = FJ2支持.unbox(template);
			if (sample == null) {
				throw new IllegalArgumentException("类型样本不得为空。");
			}
			Object result = FJ2支持.objectFromTree(FJ2支持.raw(value), FJ2支持.modelType(template), config);
			return ObjectVariant.getObjectVariant(result);
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 直接将 Simple 数据编码为 JSON 文本，应用本配置中的类型转换器。
	 *
	 * @param value  Simple 标量、数组、集合、哈希表或普通业务对象。
	 * @param config 包含可选类型转换器和过滤器的配置。
	 * @return 普通对象按配置编码的 JSON 文本。
	 * @throws IllegalArgumentException 数据、类型样本或配置无效，或对象转换失败。
	 */
	@SimpleFunction
	public static String 对象转JSON文本(Variant value, FJ2写配置 config) {
		try {
			return FJ2支持.stringify(FJ2支持.unbox(value), config);
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 直接编码 Simple 数据为 JSONB，保留日期和字节等二进制类型。
	 *
	 * @param value   Simple 标量、数组、集合、哈希表或普通业务对象。
	 * @param config  可含类型转换器和过滤器。
	 * @param symbols 可选外部符号表。
	 * @return 普通对象按配置编码的 JSONB 字节。
	 * @throws IllegalArgumentException 数据、类型样本或配置无效，或对象转换失败。
	 */
	@SimpleFunction
	public static byte[] 对象转JSONB(Variant value, FJ2写配置 config, FJ2符号表 symbols) {
		try {
			return FJ2支持.binary(FJ2支持.unbox(value), config, symbols);
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 直接把 JSON 文本映射为与样本同类型的新对象。
	 *
	 * @param text     完整 JSON 文本。
	 * @param template 已创建的普通业务对象，仅取类型。
	 * @param config   读取配置及可选类型转换器。
	 * @return 包含目标类型新对象的变体，空对象结果保持为空引用。
	 * @throws IllegalArgumentException 数据、类型样本或配置无效，或对象转换失败。
	 */
	@SimpleFunction
	public static Variant JSON文本转对象(String text, Variant template, FJ2读配置 config) {
		try {
			FJ2支持.nonempty(text);
			Object result;
			try (JSONReader reader = JSONReader.of(text, FJ2支持.readContext(config))) {
				result = reader.read(FJ2支持.modelType(template));
				reader.handleResolveTasks(result);
				if (!reader.isEnd() || reader.hasComma()) {
					throw new IllegalArgumentException("对象文本后存在多余数据或逗号。");
				}
			}
			return ObjectVariant.getObjectVariant(result);
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 直接将 JSONB 映射为与样本同类型的新对象，并拒绝尾随内容。
	 *
	 * @param bytes    完整 JSONB 数据。
	 * @param template 已创建的普通业务对象，仅取类型。
	 * @param config   读取配置及可选类型转换器。
	 * @param symbols  编码时使用的同一外部符号表。
	 * @return 包含目标类型新对象的变体，空对象结果保持为空引用。
	 * @throws IllegalArgumentException 数据、类型样本或配置无效，或对象转换失败。
	 */
	@SimpleFunction
	public static Variant JSONB转对象(byte[] bytes, Variant template, FJ2读配置 config, FJ2符号表 symbols) {
		try {
			byte[] copy = FJ2支持.bytes(bytes);
			try (JSONReader reader = JSONReader.ofJSONB(copy, FJ2支持.readContext(config, symbols))) {
				Class<?> type = FJ2支持.modelType(template);
				Object data = FJ2支持.readAll(reader);
				Object result = FJ2支持.objectFromTree(data, type, config);
				if (!reader.isEnd()) {
					throw new IllegalArgumentException("JSONB 后存在多余数据。");
				}
				return ObjectVariant.getObjectVariant(result);
			}
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 返回上游 JAR 内部声明的实现版本；可能与 Maven 发布标签不同。
	 *
	 * @return 上游代码中 JSON.VERSION 声明的版本。
	 */
	@SimpleFunction
	public static String 取实现版本() {
		try {
			return JSON.VERSION;
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 只解析指定文本范围，范围内必须恰好包含一个完整根值。
	 *
	 * @param text   来源文本。
	 * @param offset 从 0 开始的 UTF-16 代码单元位置。
	 * @param length 要解析的代码单元数量。
	 * @param config 读取配置。
	 * @return 与来源互不共享修改的类型化值。
	 * @throws IllegalArgumentException 输入、范围、字符集或配置无效，或数据不是一个完整根值。
	 */
	@SimpleFunction
	public static FJ2值 解析文本(String text, int offset, int length, FJ2读配置 config) {
		try {
			FJ2支持.text(text);
			if (offset < 0 || length < 0 || offset > text.length() - length) {
				throw new IllegalArgumentException("文本范围越界。");
			}
			return 解析文本(text.substring(offset, offset + length), config);
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 只解析指定字节范围，不读取范围外的数据。
	 *
	 * @param bytes   来源字节集。
	 * @param offset  从 0 开始的字节位置。
	 * @param length  要解析的字节数量。
	 * @param charset 有效字符集名称。
	 * @param config  读取配置。
	 * @return 与来源互不共享修改的类型化值。
	 * @throws IllegalArgumentException 输入、范围、字符集或配置无效，或数据不是一个完整根值。
	 */
	@SimpleFunction
	public static FJ2值 解析字节(byte[] bytes, int offset, int length, String charset, FJ2读配置 config) {
		try {
			if (bytes == null || offset < 0 || length < 0 || offset > bytes.length - length) {
				throw new IllegalArgumentException("字节范围越界。");
			}
			try (JSONReader reader = JSONReader.of(bytes, offset, length, Charset.forName(FJ2支持.text(charset)),
					FJ2支持.readContext(config))) {
				return new FJ2值(FJ2支持.readAll(reader));
			}
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 检查指定字符集的完整 JSON 字节；数据格式错误返回假，配置参数错误报错。
	 *
	 * @param bytes   JSON 文本字节；空引用或空输入返回假。
	 * @param charset 有效字符集名称。
	 * @param config  读取配置，不得为空。
	 * @return 字节可解析为完整根值时为真，空输入或无效数据为假。
	 * @throws IllegalArgumentException 配置为空或字符集名称无效。
	 */
	@SimpleFunction
	public static boolean 是否有效字节(byte[] bytes, String charset, FJ2读配置 config) {
		try {
			FJ2支持.require(config);
			Charset.forName(FJ2支持.text(charset));
			if (bytes == null || bytes.length == 0) {
				return false;
			}
			try {
				解析字节(bytes, charset, config);
				return true;
			} catch (IllegalArgumentException invalid) {
				return false;
			}
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 检查一个完整 JSONB 根值，包括尾随数据检查；不验证业务结构。
	 *
	 * @param bytes   JSONB 数据；空引用或空输入返回假。
	 * @param config  读取配置，不得为空。
	 * @param symbols 编码时的同一外部符号表；未使用则为空。
	 * @return 数据为一个完整 JSONB 根值时为真，否则为假。
	 * @throws IllegalArgumentException 读取配置为空。
	 */
	@SimpleFunction
	public static boolean 是否有效JSONB(byte[] bytes, FJ2读配置 config, FJ2符号表 symbols) {
		try {
			FJ2支持.require(config);
			if (bytes == null || bytes.length == 0) {
				return false;
			}
			try {
				解析JSONB(bytes, config, symbols);
				return true;
			} catch (IllegalArgumentException invalid) {
				return false;
			}
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 返回便于观察 JSONB 的格式化文本；该输出用于检查，不作为持久化或对象映射输入。
	 *
	 * @param bytes   完整 JSONB 字节。
	 * @param symbols 编码时的同一外部符号表；未使用则为空。
	 * @return 便于观察 JSONB 类型与符号信息的格式化文本。
	 * @throws IllegalArgumentException 数据不是完整 JSONB 或所给符号表不适用。
	 */
	@SimpleFunction
	public static String JSONB转调试文本(byte[] bytes, FJ2符号表 symbols) {
		try {
			解析JSONB(bytes, new FJ2读配置(), symbols);
			return symbols == null ? JSONB.toJSONString(bytes) : JSONB.toJSONString(bytes, symbols.table);
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}
}
