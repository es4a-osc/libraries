package simple.library.wsd.fastjson2;

import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TimeZone;

import com.alibaba.fastjson2.JSONReader;

import simple.runtime.annotations.SimpleDataElement;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.variants.Variant;

/**
 * 独立的读取配置，特性名称与所附 Android JAR 一致；仅影响使用该配置的调用，不修改全局设置。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class FJ2读配置 {

	/**
	 * 当前配置显式启用的读取特性。
	 */
	final EnumSet<JSONReader.Feature> features = EnumSet.noneOf(JSONReader.Feature.class);

	/**
	 * 按目标普通对象类型保存的解码回调。
	 */
	final Map<Class<?>, FJ2类型转换器> adapters = new LinkedHashMap<Class<?>, FJ2类型转换器>();

	/**
	 * 日期读取模板或上游内置格式名。
	 */
	String dateFormat = "";

	/**
	 * 日期读取使用的时区 ID。
	 */
	String timeZone = TimeZone.getDefault().getID();

	/**
	 * 允许读取的最大嵌套层数。
	 */
	int maxLevel = 512;

	/**
	 * 禁止使用单引号包围文本。
	 */
	@SimpleDataElement
	public static final String 特性_DisableSingleQuote = "DisableSingleQuote";

	/**
	 * 将适用的小数读取为 Double，可能降低十进制精度。
	 */
	@SimpleDataElement
	public static final String 特性_UseDoubleForDecimals = "UseDoubleForDecimals";

	/**
	 * 禁用输入中 $ref 的引用识别。
	 */
	@SimpleDataElement
	public static final String 特性_DisableReferenceDetect = "DisableReferenceDetect";

	/**
	 * 文本数组读取为文本时保留数组表示，不解包单个文本元素。
	 */
	@SimpleDataElement
	public static final String 特性_DisableStringArrayUnwrapping = "DisableStringArrayUnwrapping";

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
	 * 允许按字段顺序将数组映射为普通对象。
	 */
	@SimpleDataElement
	public static final String 特性_SupportArrayToBean = "SupportArrayToBean";

	/**
	 * 创建对象时将未赋值的文本字段初始化为空文本。
	 */
	@SimpleDataElement
	public static final String 特性_InitStringFieldAsEmpty = "InitStringFieldAsEmpty";

	/**
	 * 允许根据输入中的类型声明识别对象类型。
	 */
	@SimpleDataElement
	public static final String 特性_SupportAutoType = "SupportAutoType";

	/**
	 * 允许按上游规则匹配不同命名风格的属性名。
	 */
	@SimpleDataElement
	public static final String 特性_SupportSmartMatch = "SupportSmartMatch";

	/**
	 * 读取通用对象和数组时使用 Java Map 和 List 容器。
	 */
	@SimpleDataElement
	public static final String 特性_UseNativeObject = "UseNativeObject";

	/**
	 * 允许将类型名称转换为 Class 类型的字段值。
	 */
	@SimpleDataElement
	public static final String 特性_SupportClassForName = "SupportClassForName";

	/**
	 * 对象映射时忽略输入中的 null，不覆盖目标字段。
	 */
	@SimpleDataElement
	public static final String 特性_IgnoreSetNullValue = "IgnoreSetNullValue";

	/**
	 * 创建对象时优先使用默认构造方法。
	 */
	@SimpleDataElement
	public static final String 特性_UseDefaultConstructorAsPossible = "UseDefaultConstructorAsPossible";

	/**
	 * 在适用的浮点读取分支使用 BigDecimal 保存数值。
	 */
	@SimpleDataElement
	public static final String 特性_UseBigDecimalForFloats = "UseBigDecimalForFloats";

	/**
	 * 在适用的双精度读取分支使用 BigDecimal 保存数值。
	 */
	@SimpleDataElement
	public static final String 特性_UseBigDecimalForDoubles = "UseBigDecimalForDoubles";

	/**
	 * 输入值不能匹配目标枚举成员时报错。
	 */
	@SimpleDataElement
	public static final String 特性_ErrorOnEnumNotMatch = "ErrorOnEnumNotMatch";

	/**
	 * 读取文本值时去掉首尾空白。
	 */
	@SimpleDataElement
	public static final String 特性_TrimString = "TrimString";

	/**
	 * 输入要求的自动类型不受支持时报错。
	 */
	@SimpleDataElement
	public static final String 特性_ErrorOnNotSupportAutoType = "ErrorOnNotSupportAutoType";

	/**
	 * 将同名属性的多个值收集为数组。
	 */
	@SimpleDataElement
	public static final String 特性_DuplicateKeyValueAsArray = "DuplicateKeyValueAsArray";

	/**
	 * 允许读取未使用引号包围的属性名。
	 */
	@SimpleDataElement
	public static final String 特性_AllowUnQuotedFieldNames = "AllowUnQuotedFieldNames";

	/**
	 * 将非文本对象键按上游规则转换为文本键。
	 */
	@SimpleDataElement
	public static final String 特性_NonStringKeyAsString = "NonStringKeyAsString";

	/**
	 * 允许将 Base64 文本读取为字节数组。
	 */
	@SimpleDataElement
	public static final String 特性_Base64StringAsByteArray = "Base64StringAsByteArray";

	/**
	 * 请求上游忽略结束检查；本库完整解析入口仍检查尾随数据。
	 */
	@SimpleDataElement
	public static final String 特性_IgnoreCheckClose = "IgnoreCheckClose";

	/**
	 * 将 null 赋给目标基本类型字段时报错。
	 */
	@SimpleDataElement
	public static final String 特性_ErrorOnNullForPrimitives = "ErrorOnNullForPrimitives";

	/**
	 * 在上游支持的字段转换失败路径中使用 null。
	 */
	@SimpleDataElement
	public static final String 特性_NullOnError = "NullOnError";

	/**
	 * 忽略上游自动类型与目标类型不匹配的情况。
	 */
	@SimpleDataElement
	public static final String 特性_IgnoreAutoTypeNotMatch = "IgnoreAutoTypeNotMatch";

	/**
	 * 将非零数值转换为真，零转换为假。
	 */
	@SimpleDataElement
	public static final String 特性_NonZeroNumberCastToBooleanAsTrue = "NonZeroNumberCastToBooleanAsTrue";

	/**
	 * 忽略输入中值为 null 的对象属性。
	 */
	@SimpleDataElement
	public static final String 特性_IgnoreNullPropertyValue = "IgnoreNullPropertyValue";

	/**
	 * 输入属性在目标对象中没有对应字段或属性时报错。
	 */
	@SimpleDataElement
	public static final String 特性_ErrorOnUnknownProperties = "ErrorOnUnknownProperties";

	/**
	 * 在上游支持的读取场景中将空文本转换为 null。
	 */
	@SimpleDataElement
	public static final String 特性_EmptyStringAsNull = "EmptyStringAsNull";

	/**
	 * 采用上游的数值溢出宽松处理规则，可能发生精度损失。
	 */
	@SimpleDataElement
	public static final String 特性_NonErrorOnNumberOverflow = "NonErrorOnNumberOverflow";

	/**
	 * 将通用整数值读取为 BigInteger。
	 */
	@SimpleDataElement
	public static final String 特性_UseBigIntegerForInts = "UseBigIntegerForInts";

	/**
	 * 将通用整数值优先读取为 Long；BigInteger 选项优先。
	 */
	@SimpleDataElement
	public static final String 特性_UseLongForInts = "UseLongForInts";

	/**
	 * 创建未启用附加读取特性的独立配置。
	 */
	public FJ2读配置() {
	}

	/**
	 * 返回当前 JAR 的全部可选特性名称。
	 *
	 * @return 当前 JAR 支持的全部特性名称。
	 */
	@SimpleFunction
	public String[] 取支持特性() {
		try {
			JSONReader.Feature[] values = JSONReader.Feature.values();
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
			JSONReader.Feature feature = JSONReader.Feature.valueOf(FJ2支持.text(name));
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
			return features.contains(JSONReader.Feature.valueOf(FJ2支持.text(name)));
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
	 * 限制解析嵌套层数，默认 512。
	 *
	 * @param value 1 至 2048。
	 * @throws IllegalArgumentException 层数不在 1 至 2048 之间。
	 */
	@SimpleFunction
	public void 设置最大层数(int value) {
		try {
			if (value < 1 || value > 2048) {
				throw new IllegalArgumentException("最大层数必须在 1 至 2048 之间。");
			}
			maxLevel = value;
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 返回解析最大嵌套层数。
	 *
	 * @return 当前允许读取的最大嵌套层数。
	 */
	@SimpleFunction
	public int 取最大层数() {
		try {
			return maxLevel;
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
	public FJ2读配置 复制() {
		try {
			FJ2读配置 result = new FJ2读配置();
			result.features.clear();
			result.features.addAll(features);
			result.adapters.putAll(adapters);
			result.dateFormat = dateFormat;
			result.timeZone = timeZone;
			result.maxLevel = maxLevel;
			return result;
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}
}
