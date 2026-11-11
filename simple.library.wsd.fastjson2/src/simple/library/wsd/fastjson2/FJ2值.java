package simple.library.wsd.fastjson2;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson2.JSONObject;

import simple.runtime.annotations.SimpleDataElement;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.collections.JSON值;
import simple.runtime.variants.Variant;

/**
 * JSON、JSONB 的类型化值。子节点和传入值均复制；修改子节点后须显式写回父节点。索引从 0 开始，同一实例串行使用。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class FJ2值 {

	/**
	 * 当前值图；Java 空引用表示 JSON null，包内写入入口负责复制。
	 */
	Object value;

	/**
	 * 空类型标识。
	 */
	@SimpleDataElement
	public static final int 类型_空 = 0;

	/**
	 * 对象类型标识。
	 */
	@SimpleDataElement
	public static final int 类型_对象 = 1;

	/**
	 * 数组类型标识。
	 */
	@SimpleDataElement
	public static final int 类型_数组 = 2;

	/**
	 * 文本类型标识。
	 */
	@SimpleDataElement
	public static final int 类型_文本 = 3;

	/**
	 * 数值类型标识。
	 */
	@SimpleDataElement
	public static final int 类型_数值 = 4;

	/**
	 * 逻辑类型标识。
	 */
	@SimpleDataElement
	public static final int 类型_逻辑 = 5;

	/**
	 * 字节类型标识。
	 */
	@SimpleDataElement
	public static final int 类型_字节 = 6;

	/**
	 * 日期类型标识。
	 */
	@SimpleDataElement
	public static final int 类型_日期 = 7;

	/**
	 * 创建 JSON null 值。
	 */
	public FJ2值() {
	}

	/**
	 * 复制上游值，隔离可变数据。
	 *
	 * @param value JSON 或 JSONB 支持的值。
	 */
	FJ2值(Object value) {
		this.value = FJ2支持.copy(value);
	}

	/**
	 * 复制为 Simple 数据；对象变为哈希表，数组变为集合，日期和字节保留对应类型。
	 *
	 * @return 与当前值隔离的变体，保留容器的共享和循环引用。
	 * @throws IllegalArgumentException 整数超出长整数范围，或小数无法保持十进制文本往返精度。
	 */
	@SimpleFunction
	public Variant 到Simple数据() {
		try {
			return FJ2互转.toSimple(value);
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 返回值类型常量。
	 *
	 * @return 与类型常量对应的值类型标识。
	 */
	@SimpleFunction
	public int 取类型() {
		try {
			return FJ2支持.kind(value);
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 判断是否为 JSON null；缺失属性须另用包含属性判断。
	 *
	 * @return 当前值为 JSON null 时为真。
	 */
	@SimpleFunction
	public boolean 是否为空() {
		try {
			return value == null;
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 返回独立深副本。
	 *
	 * @return 保持值图引用关系的独立深副本。
	 * @throws IllegalArgumentException 值图超出支持的复制深度。
	 */
	@SimpleFunction
	public FJ2值 复制() {
		try {
			return new FJ2值(value);
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 序列化为 JSON 文本。
	 *
	 * @return 包含完整根值的 JSON 文本。
	 * @throws IllegalArgumentException 编码配置或字符集无效，或当前值不能按所选格式编码。
	 */
	@SimpleFunction
	public String 到JSON文本() {
		try {
			return FJ2支持.stringify(value, new FJ2写配置());
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 按指定配置序列化为 JSON 文本。
	 *
	 * @param config 写入配置，不得为空。
	 * @return 使用给定配置编码的 JSON 文本。
	 * @throws IllegalArgumentException 编码配置或字符集无效，或当前值不能按所选格式编码。
	 */
	@SimpleFunction
	public String 到JSON文本(FJ2写配置 config) {
		try {
			return FJ2支持.stringify(value, config);
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 编码为 UTF-8 JSON 字节。
	 *
	 * @return 完整 JSON 文本的 UTF-8 字节。
	 * @throws IllegalArgumentException 编码配置或字符集无效，或当前值不能按所选格式编码。
	 */
	@SimpleFunction
	public byte[] 到UTF8字节() {
		try {
			return 到JSON文本().getBytes(Charset.forName("UTF-8"));
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 按指定字符集编码 JSON 文本。
	 *
	 * @param charset 有效字符集名称。
	 * @param config  写入配置。
	 * @return 使用指定字符集编码的 JSON 文本字节。
	 * @throws IllegalArgumentException 编码配置或字符集无效，或当前值不能按所选格式编码。
	 */
	@SimpleFunction
	public byte[] 到JSON字节(String charset, FJ2写配置 config) {
		try {
			return 到JSON文本(config).getBytes(Charset.forName(FJ2支持.text(charset)));
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 编码为 JSONB 二进制。
	 *
	 * @return 完整 JSONB 数据。
	 * @throws IllegalArgumentException 编码配置或字符集无效，或当前值不能按所选格式编码。
	 */
	@SimpleFunction
	public byte[] 到JSONB() {
		try {
			return FJ2支持.binary(value, new FJ2写配置(), null);
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 按配置和可选外部符号表编码 JSONB。
	 *
	 * @param config  写入配置。
	 * @param symbols 空引用表示不用外部符号表；解码须使用相同表。
	 * @return 使用给定配置和符号表编码的 JSONB 数据。
	 * @throws IllegalArgumentException 编码配置或字符集无效，或当前值不能按所选格式编码。
	 */
	@SimpleFunction
	public byte[] 到JSONB(FJ2写配置 config, FJ2符号表 symbols) {
		try {
			return FJ2支持.binary(value, config, symbols);
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 读取文本标量，不隐式转换其他类型。
	 *
	 * @return 文本标量的原内容。
	 * @throws IllegalArgumentException 当前值不是文本。
	 */
	@SimpleFunction
	public String 取文本() {
		try {
			if (!(value instanceof String)) {
				throw new IllegalArgumentException("当前值不是文本。");
			}
			return (String) value;
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 读取逻辑标量，不隐式转换文本或数值。
	 *
	 * @return 逻辑标量的值。
	 * @throws IllegalArgumentException 当前值不是逻辑。
	 */
	@SimpleFunction
	public boolean 取逻辑() {
		try {
			if (!(value instanceof Boolean)) {
				throw new IllegalArgumentException("当前值不是逻辑。");
			}
			return (Boolean) value;
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 精确读取 64 位整数，拒绝小数和溢出数值。
	 *
	 * @return 精确的 64 位整数。
	 * @throws IllegalArgumentException 当前值不是数值、包含小数或超出 64 位整数范围。
	 */
	@SimpleFunction
	public long 取整数() {
		try {
			return FJ2支持.number(value).longValueExact();
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 读取有限的双精度数值，精度以 IEEE 754 双精度为限。
	 *
	 * @return 有限的双精度数值。
	 * @throws IllegalArgumentException 当前值不是数值或不能表示为有限双精度数。
	 */
	@SimpleFunction
	public double 取小数() {
		try {
			double result = FJ2支持.number(value).doubleValue();
			if (Double.isInfinite(result) || Double.isNaN(result)) {
				throw new IllegalArgumentException("数值超出有限双精度范围。");
			}
			return result;
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 以十进制文本读取数值，避免大整数或高精度小数降精度。
	 *
	 * @return 保留大整数及小数精度的十进制数值文本。
	 * @throws IllegalArgumentException 当前值不是数值。
	 */
	@SimpleFunction
	public String 取精确数值() {
		try {
			return FJ2支持.number(value).toString();
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 读取字节数组的独立副本。
	 *
	 * @return 内部字节数组的独立副本。
	 * @throws IllegalArgumentException 当前值不是字节数组。
	 */
	@SimpleFunction
	public byte[] 取字节() {
		try {
			if (!(value instanceof byte[])) {
				throw new IllegalArgumentException("当前值不是字节。");
			}
			return ((byte[]) value).clone();
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 读取日期值对应的 Unix 毫秒时间戳。
	 *
	 * @return 日期值对应的 Unix 毫秒时间戳。
	 * @throws IllegalArgumentException 当前值不是日期。
	 */
	@SimpleFunction
	public long 取时间戳() {
		try {
			if (!(value instanceof Date)) {
				throw new IllegalArgumentException("当前值不是日期。");
			}
			return ((Date) value).getTime();
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 返回对象属性数或数组元素数；标量报错。
	 *
	 * @return 对象的属性数或数组的元素数。
	 * @throws IllegalArgumentException 当前值不是对象或数组。
	 */
	@SimpleFunction
	public int 取数量() {
		try {
			if (value instanceof Map) {
				return ((Map<?, ?>) value).size();
			}
			return FJ2支持.array(value).size();
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 返回对象属性名，保持当前迭代顺序。
	 *
	 * @return 按当前属性顺序排列的名称数组。
	 * @throws IllegalArgumentException 当前值不是对象。
	 */
	@SimpleFunction
	public String[] 取属性名() {
		try {
			return FJ2支持.object(value).keySet().toArray(new String[0]);
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 判断属性是否存在，值为 null 也返回真。
	 *
	 * @param name 属性名称。
	 * @return 对象含有指定属性时为真，值为 null 也视为存在。
	 * @throws IllegalArgumentException 当前值不是对象或名称为空。
	 */
	@SimpleFunction
	public boolean 包含属性(String name) {
		try {
			return FJ2支持.object(value).containsKey(FJ2支持.text(name));
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 返回对象属性的独立副本；属性不存在报错。
	 *
	 * @param name 属性名称。
	 * @return 指定属性的独立副本。
	 * @throws IllegalArgumentException 当前值不是对象、名称为空或属性不存在。
	 */
	@SimpleFunction
	public FJ2值 取属性(String name) {
		try {
			Map<String, Object> map = FJ2支持.object(value);
			if (!map.containsKey(FJ2支持.text(name))) {
				throw new IllegalArgumentException("属性不存在：" + name);
			}
			return new FJ2值(map.get(name));
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 新增或替换对象属性，复制传入值。
	 *
	 * @param name 属性名称。
	 * @param item 空引用表示 JSON null。
	 * @throws IllegalArgumentException 当前值类型、参数或待复制数据不符合操作要求。
	 */
	@SimpleFunction
	public void 设置属性(String name, FJ2值 item) {
		try {
			Object copied = FJ2支持.copy(FJ2支持.raw(item));
			FJ2支持.object(value).put(FJ2支持.text(name), copied);
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 删除对象属性，返回删除前是否存在。
	 *
	 * @param name 属性名称。
	 * @return 删除前存在该属性时为真，否则为假。
	 * @throws IllegalArgumentException 当前值类型、参数或待复制数据不符合操作要求。
	 */
	@SimpleFunction
	public boolean 删除属性(String name) {
		try {
			Map<String, Object> map = FJ2支持.object(value);
			boolean exists = map.containsKey(FJ2支持.text(name));
			map.remove(name);
			return exists;
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 返回数组元素的独立副本；越界报错。
	 *
	 * @param index 从 0 开始的索引。
	 * @return 指定元素的独立副本。
	 * @throws IllegalArgumentException 当前值不是数组、索引越界或传入值不能复制。
	 */
	@SimpleFunction
	public FJ2值 取项目(int index) {
		try {
			return new FJ2值(FJ2支持.array(value).get(index));
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 设置项目，复制传入值；失败时不改变数组。
	 *
	 * @param index 已有项目的索引。
	 * @param item  空引用表示 JSON null。
	 * @throws IllegalArgumentException 当前值不是数组、索引越界或传入值不能复制。
	 */
	@SimpleFunction
	public void 设置项目(int index, FJ2值 item) {
		try {
			Object copied = FJ2支持.copy(FJ2支持.raw(item));
			List<Object> list = FJ2支持.array(value);
			list.set(index, copied);
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 插入项目，复制传入值；失败时不改变数组。
	 *
	 * @param index 0 至当前数量之间。
	 * @param item  空引用表示 JSON null。
	 * @throws IllegalArgumentException 当前值不是数组、索引越界或传入值不能复制。
	 */
	@SimpleFunction
	public void 插入项目(int index, FJ2值 item) {
		try {
			Object copied = FJ2支持.copy(FJ2支持.raw(item));
			List<Object> list = FJ2支持.array(value);
			list.add(index, copied);
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 追加项目，复制传入值；失败时不改变数组。
	 *
	 * @param item 空引用表示 JSON null。
	 * @throws IllegalArgumentException 当前值类型、参数或待复制数据不符合操作要求。
	 */
	@SimpleFunction
	public void 追加项目(FJ2值 item) {
		try {
			Object copied = FJ2支持.copy(FJ2支持.raw(item));
			List<Object> list = FJ2支持.array(value);
			list.add(copied);
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 删除数组元素并返回其独立值。
	 *
	 * @param index 已有项目的索引。
	 * @return 被删除元素的独立副本。
	 * @throws IllegalArgumentException 当前值不是数组、索引越界或传入值不能复制。
	 */
	@SimpleFunction
	public FJ2值 删除项目(int index) {
		try {
			return new FJ2值(FJ2支持.array(value).remove(index));
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 清空对象属性或数组元素；标量报错。
	 *
	 * @throws IllegalArgumentException 当前值类型、参数或待复制数据不符合操作要求。
	 */
	@SimpleFunction
	public void 清空() {
		try {
			if (value instanceof Map) {
				FJ2支持.object(value).clear();
			} else {
				FJ2支持.array(value).clear();
			}
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 复制来源全部属性，覆盖同名属性；来源类型错误时不修改。
	 *
	 * @param other 对象值，不得为空。
	 * @throws IllegalArgumentException 当前值类型、参数或待复制数据不符合操作要求。
	 */
	@SimpleFunction
	public void 合并对象(FJ2值 other) {
		try {
			Map<String, Object> copied = FJ2支持.object(FJ2支持.copy(FJ2支持.raw(other)));
			FJ2支持.object(value).putAll(copied);
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 通过官方基础 JSONPath 查询；未找到与匹配 null 均返回空值。
	 *
	 * @param path 支持根、属性和非负数组下标；不支持过滤、通配符和聚合。
	 * @return 查询结果的独立副本，未匹配与匹配 null 均为空值。
	 * @throws IllegalArgumentException 当前值类型、参数或待复制数据不符合操作要求。
	 */
	@SimpleFunction
	public FJ2值 查询(String path) {
		try {
			return FJ2路径.编译(path).查询(this);
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 将对象或数组复制为运行库 JSON值，转换后的数值能力遵循运行库约定。
	 *
	 * @return 与当前值互不影响的运行库 JSON值。
	 * @throws IllegalArgumentException 当前值不是对象或数组，或不能转换为运行库 JSON值。
	 */
	@SimpleFunction
	public JSON值 到运行库JSON() {
		try {
			if (!(value instanceof Map) && !(value instanceof List)) {
				throw new IllegalArgumentException("运行库 JSON值 仅接受对象或数组。");
			}
			return new JSON值(到JSON文本());
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 返回数组全部元素的独立快照，索引从 0 开始。
	 *
	 * @return 按原顺序排列的元素快照数组。
	 * @throws IllegalArgumentException 当前值类型、参数或待复制数据不符合操作要求。
	 */
	@SimpleFunction
	public FJ2值[] 取全部项目() {
		try {
			List<Object> items = FJ2支持.array(value);
			FJ2值[] result = new FJ2值[items.size()];
			for (int i = 0; i < result.length; i++) {
				result[i] = new FJ2值(items.get(i));
			}
			return result;
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 取得属性快照；仅属性不存在时使用默认值，已存在的 null 不替换。
	 *
	 * @param name     对象属性名。
	 * @param fallback 缺少属性时使用，空引用表示 JSON null。
	 * @return 属性值的副本，属性不存在时为默认值的副本。
	 * @throws IllegalArgumentException 当前值不是对象、名称为空或默认值不能复制。
	 */
	@SimpleFunction
	public FJ2值 取默认属性(String name, FJ2值 fallback) {
		try {
			Map<String, Object> map = FJ2支持.object(value);
			FJ2支持.text(name);
			return new FJ2值(map.containsKey(name) ? map.get(name) : FJ2支持.raw(fallback));
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 深复制来源数组全部元素后追加；支持追加自身，来源无效时不修改。
	 *
	 * @param items 数组值，不得为空。
	 * @throws IllegalArgumentException 当前值类型、参数或待复制数据不符合操作要求。
	 */
	@SimpleFunction
	public void 追加数组(FJ2值 items) {
		try {
			List<Object> copied = FJ2支持.array(FJ2支持.copy(FJ2支持.raw(items)));
			FJ2支持.array(value).addAll(copied);
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 重命名对象属性并保留原属性位置；缺少原属性返回假，禁止覆盖时失败不修改。
	 *
	 * @param oldName   要改名的属性。
	 * @param newName   非空引用的新名称。
	 * @param overwrite 重名时是否允许覆盖已有属性。
	 * @return 原属性存在并完成改名时为真；缺少原属性时为假。
	 * @throws IllegalArgumentException 当前值不是对象、名称为空，或目标属性重名且不允许覆盖。
	 */
	@SimpleFunction
	public boolean 重命名属性(String oldName, String newName, boolean overwrite) {
		try {
			Map<String, Object> map = FJ2支持.object(value);
			FJ2支持.text(oldName);
			FJ2支持.text(newName);
			if (!map.containsKey(oldName)) {
				return false;
			}
			if (oldName.equals(newName)) {
				return true;
			}
			if (!overwrite && map.containsKey(newName)) {
				throw new IllegalArgumentException("目标属性已存在。");
			}
			Map<String, Object> renamed = new LinkedHashMap<String, Object>();
			for (Map.Entry<String, Object> item : map.entrySet()) {
				if (item.getKey().equals(oldName)) {
					renamed.put(newName, item.getValue());
				} else if (!item.getKey().equals(newName)) {
					renamed.put(item.getKey(), item.getValue());
				}
			}
			map.clear();
			map.putAll(renamed);
			return true;
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 用新值的深副本替换当前值；复制失败不修改。
	 *
	 * @param other 空引用表示 JSON null。
	 * @throws IllegalArgumentException 当前值类型、参数或待复制数据不符合操作要求。
	 */
	@SimpleFunction
	public void 替换为(FJ2值 other) {
		try {
			Object copied = FJ2支持.copy(FJ2支持.raw(other));
			value = copied;
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 返回过滤后的独立值图，保留未修改的循环引用；保留的属性名不能重复，结果最多嵌套 512 层。
	 *
	 * @param callback 同步过滤接口，不得为空。
	 * @return 保持源数据不变的过滤结果。
	 * @throws IllegalArgumentException 过滤器为空、产生重名属性或结果超过支持的深度。
	 */
	@SimpleFunction
	public FJ2值 过滤(FJ2过滤器 callback) {
		try {
			if (callback == null) {
				throw new IllegalArgumentException("过滤器不得为空。");
			}
			return new FJ2值(FJ2支持.filtered(value, callback, new IdentityHashMap<Object, Object>()));
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 将数值、数字文本或逻辑转换为 64 位整数；逻辑为 1 或 0，拒绝溢出和非整数。
	 *
	 * @return 精确转换的 64 位整数，逻辑值对应 1 或 0。
	 * @throws IllegalArgumentException 当前值不能按所请求的目标类型或精度进行转换。
	 */
	@SimpleFunction
	public long 转换整数() {
		try {
			if (value instanceof Boolean) {
				return ((Boolean) value) ? 1 : 0;
			}
			if (value instanceof String) {
				return new BigDecimal(((String) value).trim()).longValueExact();
			}
			return 取整数();
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 将数值、数字文本或逻辑转换为有限双精度数，结果受双精度精度限制。
	 *
	 * @return 转换后的有限双精度数。
	 * @throws IllegalArgumentException 当前值不能按所请求的目标类型或精度进行转换。
	 */
	@SimpleFunction
	public double 转换小数() {
		try {
			if (value instanceof Boolean) {
				return ((Boolean) value) ? 1.0 : 0.0;
			}
			if (value instanceof String) {
				return FJ2支持.finite(Double.parseDouble(((String) value).trim()));
			}
			return 取小数();
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 按上游 JSONObject 规则转换逻辑、文本或数值；不能转换时直接报错，不把 null 当作假。
	 *
	 * @return 按上游规则转换的逻辑值。
	 * @throws IllegalArgumentException 当前值不能按所请求的目标类型或精度进行转换。
	 */
	@SimpleFunction
	public boolean 转换逻辑() {
		try {
			if (value == null) {
				throw new IllegalArgumentException("null 不能转换为逻辑。");
			}
			JSONObject holder = new JSONObject();
			holder.put("value", value);
			Boolean result = holder.getBoolean("value");
			if (result == null) {
				throw new IllegalArgumentException("当前值不能转换为逻辑。");
			}
			return result;
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 文本返回原内容，对象和数组返回 JSON，数值和逻辑返回对应文本；null 返回 null 字面量。
	 *
	 * @return 文本原内容，或其他值的 JSON 表示。
	 * @throws IllegalArgumentException 当前值不能按所请求的目标类型或精度进行转换。
	 */
	@SimpleFunction
	public String 转换文本() {
		try {
			return value instanceof String ? (String) value : 到JSON文本();
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}
}
