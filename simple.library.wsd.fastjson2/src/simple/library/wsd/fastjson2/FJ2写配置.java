package simple.library.wsd.fastjson2;

import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TimeZone;

import com.alibaba.fastjson2.JSONWriter;

import simple.runtime.annotations.SimpleDataElement;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.variants.Variant;

/**
 * 独立的写入配置，特性名称与所附 Android JAR 一致；仅影响使用该配置的调用，不修改全局设置。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class FJ2写配置 {

	/**
	 * 当前写入特性，默认保留 null 并检测引用。
	 */
	final EnumSet<JSONWriter.Feature> features = EnumSet.noneOf(JSONWriter.Feature.class);

	/**
	 * 按普通对象类型保存的编码回调。
	 */
	final Map<Class<?>, FJ2类型转换器> adapters = new LinkedHashMap<Class<?>, FJ2类型转换器>();

	/**
	 * 日期写入模板或上游内置格式名。
	 */
	String dateFormat = "";

	/**
	 * 日期写入使用的时区 ID。
	 */
	String timeZone = TimeZone.getDefault().getID();

	/**
	 * 完整值的属性过滤器，空引用表示不使用过滤。
	 */
	FJ2过滤器 filter;

	/**
	 * 按对象字段进行映射，包括符合上游规则的非公开字段。
	 */
	@SimpleDataElement
	public static final String 特性_FieldBased = "FieldBased";

	/**
	 * 忽略未实现 Serializable 的目标类型或属性值。
	 */
	@SimpleDataElement
	public static final String 特性_IgnoreNoneSerializable = "IgnoreNoneSerializable";

	/**
	 * 遇到未实现 Serializable 的目标类型或属性值时报错。
	 */
	@SimpleDataElement
	public static final String 特性_ErrorOnNoneSerializable = "ErrorOnNoneSerializable";

	/**
	 * 按字段顺序将普通对象编码为数组。
	 */
	@SimpleDataElement
	public static final String 特性_BeanToArray = "BeanToArray";

	/**
	 * 输出对象中值为 null 的属性。
	 */
	@SimpleDataElement
	public static final String 特性_WriteNulls = "WriteNulls";

	/**
	 * 输出对象中值为 null 的属性，与 WriteNulls 使用同一标记。
	 */
	@SimpleDataElement
	public static final String 特性_WriteMapNullValue = "WriteMapNullValue";

	/**
	 * 按上游浏览器兼容规则输出超出 JavaScript 安全整数范围的数值。
	 */
	@SimpleDataElement
	public static final String 特性_BrowserCompatible = "BrowserCompatible";

	/**
	 * 按声明类型将 null 输出为相应默认值。
	 */
	@SimpleDataElement
	public static final String 特性_NullAsDefaultValue = "NullAsDefaultValue";

	/**
	 * 将逻辑值输出为 1 或 0。
	 */
	@SimpleDataElement
	public static final String 特性_WriteBooleanAsNumber = "WriteBooleanAsNumber";

	/**
	 * 将适用的非文本标量输出为文本。
	 */
	@SimpleDataElement
	public static final String 特性_WriteNonStringValueAsString = "WriteNonStringValueAsString";

	/**
	 * 在支持的输出格式中写入 Java 类型信息。
	 */
	@SimpleDataElement
	public static final String 特性_WriteClassName = "WriteClassName";

	/**
	 * 写入类型信息时省略根对象的类型名称。
	 */
	@SimpleDataElement
	public static final String 特性_NotWriteRootClassName = "NotWriteRootClassName";

	/**
	 * 写入类型信息时省略 HashMap 和 ArrayList 的类型名称。
	 */
	@SimpleDataElement
	public static final String 特性_NotWriteHashMapArrayListClassName = "NotWriteHashMapArrayListClassName";

	/**
	 * 省略值等于类型默认值的对象属性。
	 */
	@SimpleDataElement
	public static final String 特性_NotWriteDefaultValue = "NotWriteDefaultValue";

	/**
	 * 使用枚举成员名称输出枚举值。
	 */
	@SimpleDataElement
	public static final String 特性_WriteEnumsUsingName = "WriteEnumsUsingName";

	/**
	 * 使用枚举对象的 toString 结果输出枚举值。
	 */
	@SimpleDataElement
	public static final String 特性_WriteEnumUsingToString = "WriteEnumUsingToString";

	/**
	 * 忽略普通对象属性获取器抛出的异常。
	 */
	@SimpleDataElement
	public static final String 特性_IgnoreErrorGetter = "IgnoreErrorGetter";

	/**
	 * 使用换行和缩进格式化文本输出。
	 */
	@SimpleDataElement
	public static final String 特性_PrettyFormat = "PrettyFormat";

	/**
	 * 检测重复及循环引用，并按上游引用语法输出。
	 */
	@SimpleDataElement
	public static final String 特性_ReferenceDetection = "ReferenceDetection";

	/**
	 * 在 JSONB 中将属性名作为符号写入。
	 */
	@SimpleDataElement
	public static final String 特性_WriteNameAsSymbol = "WriteNameAsSymbol";

	/**
	 * 以不使用科学计数法的数字形式输出 BigDecimal。
	 */
	@SimpleDataElement
	public static final String 特性_WriteBigDecimalAsPlain = "WriteBigDecimalAsPlain";

	/**
	 * 使用单引号包围文本和属性名。
	 */
	@SimpleDataElement
	public static final String 特性_UseSingleQuotes = "UseSingleQuotes";

	/**
	 * 按上游键排序规则输出 Map 条目。
	 */
	@SimpleDataElement
	public static final String 特性_MapSortField = "MapSortField";

	/**
	 * 将值为 null 的集合属性输出为空数组。
	 */
	@SimpleDataElement
	public static final String 特性_WriteNullListAsEmpty = "WriteNullListAsEmpty";

	/**
	 * 将值为 null 的文本属性输出为空文本。
	 */
	@SimpleDataElement
	public static final String 特性_WriteNullStringAsEmpty = "WriteNullStringAsEmpty";

	/**
	 * 将值为 null 的数值属性输出为零。
	 */
	@SimpleDataElement
	public static final String 特性_WriteNullNumberAsZero = "WriteNullNumberAsZero";

	/**
	 * 将值为 null 的逻辑属性输出为假。
	 */
	@SimpleDataElement
	public static final String 特性_WriteNullBooleanAsFalse = "WriteNullBooleanAsFalse";

	/**
	 * 省略值为空数组或空集合的适用属性。
	 */
	@SimpleDataElement
	public static final String 特性_NotWriteEmptyArray = "NotWriteEmptyArray";

	/**
	 * 省略空数组或空集合，与 NotWriteEmptyArray 使用同一标记。
	 */
	@SimpleDataElement
	public static final String 特性_IgnoreEmpty = "IgnoreEmpty";

	/**
	 * 将非文本对象键作为带引号的文本输出。
	 */
	@SimpleDataElement
	public static final String 特性_WriteNonStringKeyAsString = "WriteNonStringKeyAsString";

	/**
	 * 将上游支持的 Pair 类型按普通对象输出。
	 */
	@SimpleDataElement
	public static final String 特性_WritePairAsJavaBean = "WritePairAsJavaBean";

	/**
	 * 启用以 ASCII 字符为主的文本写入优化。
	 */
	@SimpleDataElement
	public static final String 特性_OptimizedForAscii = "OptimizedForAscii";

	/**
	 * 对非 ASCII 字符使用转义形式输出。
	 */
	@SimpleDataElement
	public static final String 特性_EscapeNoneAscii = "EscapeNoneAscii";

	/**
	 * 将字节数组编码为 Base64 文本。
	 */
	@SimpleDataElement
	public static final String 特性_WriteByteArrayAsBase64 = "WriteByteArrayAsBase64";

	/**
	 * 忽略没有对应字段的属性获取器。
	 */
	@SimpleDataElement
	public static final String 特性_IgnoreNonFieldGetter = "IgnoreNonFieldGetter";

	/**
	 * 允许上游写入器使用更大的输出缓冲区上限。
	 */
	@SimpleDataElement
	public static final String 特性_LargeObject = "LargeObject";

	/**
	 * 将长整数输出为文本。
	 */
	@SimpleDataElement
	public static final String 特性_WriteLongAsString = "WriteLongAsString";

	/**
	 * 对小于号、大于号和圆括号等字符使用安全转义形式。
	 */
	@SimpleDataElement
	public static final String 特性_BrowserSecure = "BrowserSecure";

	/**
	 * 使用枚举成员的顺序号输出枚举值。
	 */
	@SimpleDataElement
	public static final String 特性_WriteEnumUsingOrdinal = "WriteEnumUsingOrdinal";

	/**
	 * 输出异常对象时写入其类型信息。
	 */
	@SimpleDataElement
	public static final String 特性_WriteThrowableClassName = "WriteThrowableClassName";

	/**
	 * 不使用引号包围文本格式中的属性名。
	 */
	@SimpleDataElement
	public static final String 特性_UnquoteFieldName = "UnquoteFieldName";

	/**
	 * 写入类型信息时省略 Set 的类型名称。
	 */
	@SimpleDataElement
	public static final String 特性_NotWriteSetClassName = "NotWriteSetClassName";

	/**
	 * 写入类型信息时省略数值类型名称或类型后缀。
	 */
	@SimpleDataElement
	public static final String 特性_NotWriteNumberClassName = "NotWriteNumberClassName";

	/**
	 * 按键排序后输出 Map 条目，已排序 Map 遵循上游规则。
	 */
	@SimpleDataElement
	public static final String 特性_SortMapEntriesByKeys = "SortMapEntriesByKeys";

	/**
	 * 格式化输出时每层缩进两个空格。
	 */
	@SimpleDataElement
	public static final String 特性_PrettyFormatWith2Space = "PrettyFormatWith2Space";

	/**
	 * 格式化输出时每层缩进四个空格。
	 */
	@SimpleDataElement
	public static final String 特性_PrettyFormatWith4Space = "PrettyFormatWith4Space";

	/**
	 * 将 java.util.Date 编码为 Unix 毫秒时间戳。
	 */
	@SimpleDataElement
	public static final String 特性_WriterUtilDateAsMillis = "WriterUtilDateAsMillis";

	/**
	 * 创建默认保留 null 并检测重复及循环引用的独立写入配置。
	 */
	public FJ2写配置() {
		features.add(JSONWriter.Feature.ReferenceDetection);
		features.add(JSONWriter.Feature.WriteNulls);
	}

	/**
	 * 返回当前 JAR 的全部可选特性名称。
	 *
	 * @return 当前 JAR 支持的全部特性名称。
	 */
	@SimpleFunction
	public String[] 取支持特性() {
		try {
			JSONWriter.Feature[] values = JSONWriter.Feature.values();
			String[] result = new String[values.length];
			for (int i = 0; i < values.length; i++) {
				result[i] = values[i].name();
			}
			return result;
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 启用或关闭单项特性；未知名称直接报错。
	 *
	 * @param name    由取支持特性返回的英文名称。
	 * @param enabled 是否启用。
	 * @throws IllegalArgumentException 名称为空或不是受支持的特性。
	 */
	@SimpleFunction
	public void 设置特性(String name, boolean enabled) {
		try {
			JSONWriter.Feature feature = JSONWriter.Feature.valueOf(FJ2支持.text(name));
			if (enabled) {
				features.add(feature);
			} else {
				features.remove(feature);
			}
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 判断单项特性是否启用。
	 *
	 * @param name 特性名称。
	 * @return 特性已启用时为真，否则为假。
	 * @throws IllegalArgumentException 名称为空或不是受支持的特性。
	 */
	@SimpleFunction
	public boolean 是否启用(String name) {
		try {
			return features.contains(JSONWriter.Feature.valueOf(FJ2支持.text(name)));
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 关闭本配置中显式启用的全部特性。
	 */
	@SimpleFunction
	public void 清空特性() {
		try {
			features.clear();
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 设置本配置内的普通对象编解码规则，不修改全局注册表；回调不得重入相同类型的转换。
	 *
	 * @param template 已创建的普通业务对象，仅取实际类型。
	 * @param adapter  同步转换接口，空引用取消此类型的转换。
	 * @throws IllegalArgumentException 类型样本为空或不是支持的普通业务对象。
	 */
	@SimpleFunction
	public void 设置类型转换器(Variant template, FJ2类型转换器 adapter) {
		try {
			Class<?> type = FJ2支持.modelType(template);
			if (adapter == null) {
				adapters.remove(type);
			} else {
				adapters.put(type, adapter);
			}
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 设置日期格式。
	 *
	 * @param value 日期格式或空文本；支持 millis、unixtime、iso8601。
	 * @throws IllegalArgumentException 日期格式为空引用。
	 */
	@SimpleFunction
	public void 设置日期格式(String value) {
		try {
			dateFormat = FJ2支持.text(value);
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 返回日期格式。
	 *
	 * @return 当前日期模板或内置格式名，未指定时为空文本。
	 */
	@SimpleFunction
	public String 取日期格式() {
		try {
			return dateFormat;
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 设置时区。
	 *
	 * @param value 有效时区 ID，例如 Asia/Shanghai、UTC。
	 * @throws IllegalArgumentException 时区为空引用或不是有效的时区 ID。
	 */
	@SimpleFunction
	public void 设置时区(String value) {
		try {
			FJ2支持.zone(value);
			timeZone = FJ2支持.text(value);
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 返回时区。
	 *
	 * @return 当前时区 ID。
	 */
	@SimpleFunction
	public String 取时区() {
		try {
			return timeZone;
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 设置属性保留、名称和值过滤器；null 属性可先由过滤器补值，再应用空值输出规则。 回调在发起序列化的线程执行，不得重入同一写入器。
	 *
	 * @param value 同步回调；空引用取消过滤。
	 */
	@SimpleFunction
	public void 设置过滤器(FJ2过滤器 value) {
		try {
			filter = value;
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 返回可独立修改的配置副本；回调接口保持同一实例。
	 *
	 * @return 可独立修改的配置副本，回调仍引用原接口实例。
	 */
	@SimpleFunction
	public FJ2写配置 复制() {
		try {
			FJ2写配置 result = new FJ2写配置();
			result.features.clear();
			result.features.addAll(features);
			result.adapters.putAll(adapters);
			result.dateFormat = dateFormat;
			result.timeZone = timeZone;
			result.filter = filter;
			return result;
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}
}
