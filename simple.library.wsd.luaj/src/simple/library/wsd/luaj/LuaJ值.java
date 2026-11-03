package simple.library.wsd.luaj;

import org.luaj.vm2.LuaValue;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.variants.Variant;

/**
 * 同一 LuaJ 实例拥有的值；nil 是有效值对象。表、函数和协程以对应子类返回。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public class LuaJ值 {

	final LuaJContext context;
	final LuaValue value;

	/**
	 * 持有所属实例与底层值。
	 *
	 * @param context 所属实例。
	 * @param value 有效底层值。
	 */
	LuaJ值(LuaJContext context, LuaValue value) {
		this.context = context;
		this.value = value;
	}

	/**
	 * 取得 Lua 类型编号，对应 LuaJ常量。
	 *
	 * @return 取得 Lua 类型编号，对应 LuaJ常量。
	 */
	@SimpleFunction
	public int 取类型() {
		return context.perform(
				new LuaJContext.Action<Integer>() {
					@Override
					public Integer run() throws Exception {
						return value.type();
					}
				});
	}

	/**
	 * 取得 Lua 类型名称。
	 *
	 * @return 取得 Lua 类型名称。
	 */
	@SimpleFunction
	public String 取类型名() {
		return context.perform(
				new LuaJContext.Action<String>() {
					@Override
					public String run() throws Exception {
						return value.typename();
					}
				});
	}

	/**
	 * 判断是否为 Lua nil。
	 *
	 * @return 判断是否为 Lua nil。
	 */
	@SimpleFunction
	public boolean 是否为空() {
		return context.perform(
				new LuaJContext.Action<Boolean>() {
					@Override
					public Boolean run() throws Exception {
						return value.isnil();
					}
				});
	}

	/**
	 * 判断是否为逻辑类型。
	 *
	 * @return 判断是否为逻辑类型。
	 */
	@SimpleFunction
	public boolean 是否逻辑() {
		return context.perform(
				new LuaJContext.Action<Boolean>() {
					@Override
					public Boolean run() throws Exception {
						return value.isboolean();
					}
				});
	}

	/**
	 * 判断能否按 LuaJ 规则作为数值使用，包含可转换的数值文本。
	 *
	 * @return 判断能否按 LuaJ 规则作为数值使用，包含可转换的数值文本。
	 */
	@SimpleFunction
	public boolean 是否数值() {
		return context.perform(
				new LuaJContext.Action<Boolean>() {
					@Override
					public Boolean run() throws Exception {
						return value.isnumber();
					}
				});
	}

	/**
	 * 判断是否可作为 32 位整数使用。
	 *
	 * @return 判断是否可作为 32 位整数使用。
	 */
	@SimpleFunction
	public boolean 是否整数() {
		return context.perform(
				new LuaJContext.Action<Boolean>() {
					@Override
					public Boolean run() throws Exception {
						return value.isint();
					}
				});
	}

	/**
	 * 判断能否按 LuaJ 规则作为字符串使用，包含数值。
	 *
	 * @return 判断能否按 LuaJ 规则作为字符串使用，包含数值。
	 */
	@SimpleFunction
	public boolean 是否文本() {
		return context.perform(
				new LuaJContext.Action<Boolean>() {
					@Override
					public Boolean run() throws Exception {
						return value.isstring();
					}
				});
	}

	/**
	 * 判断是否为表。
	 *
	 * @return 判断是否为表。
	 */
	@SimpleFunction
	public boolean 是否为表() {
		return context.perform(
				new LuaJContext.Action<Boolean>() {
					@Override
					public Boolean run() throws Exception {
						return value.istable();
					}
				});
	}

	/**
	 * 判断是否为函数。
	 *
	 * @return 判断是否为函数。
	 */
	@SimpleFunction
	public boolean 是否函数() {
		return context.perform(
				new LuaJContext.Action<Boolean>() {
					@Override
					public Boolean run() throws Exception {
						return value.isfunction();
					}
				});
	}

	/**
	 * 判断是否为 Lua 协程。
	 *
	 * @return 判断是否为 Lua 协程。
	 */
	@SimpleFunction
	public boolean 是否协程() {
		return context.perform(
				new LuaJContext.Action<Boolean>() {
					@Override
					public Boolean run() throws Exception {
						return value.isthread();
					}
				});
	}

	/**
	 * 判断是否为 Java 用户数据。
	 *
	 * @return 判断是否为 Java 用户数据。
	 */
	@SimpleFunction
	public boolean 是否用户数据() {
		return context.perform(
				new LuaJContext.Action<Boolean>() {
					@Override
					public Boolean run() throws Exception {
						return value.isuserdata();
					}
				});
	}

	/**
	 * 按 Lua 真值规则转换：只有 nil 和 false 为假。
	 *
	 * @return 按 Lua 真值规则转换：只有 nil 和 false 为假。
	 */
	@SimpleFunction
	public boolean 到逻辑() {
		return context.perform(
				new LuaJContext.Action<Boolean>() {
					@Override
					public Boolean run() throws Exception {
						return value.toboolean();
					}
				});
	}

	/**
	 * 按 LuaJ 整数转换规则读取数值；不能转换时抛出错误。
	 *
	 * @return 按 LuaJ 整数转换规则读取数值；不能转换时抛出错误。
	 */
	@SimpleFunction
	public int 到整数() {
		return context.perform(
				new LuaJContext.Action<Integer>() {
					@Override
					public Integer run() throws Exception {
						return value.checkint();
					}
				});
	}

	/**
	 * 按 LuaJ 规则读取长整数；Lua 数值是双精度，大整数可能丢失精度。
	 *
	 * @return 按 LuaJ 规则读取长整数；Lua 数值是双精度，大整数可能丢失精度。
	 */
	@SimpleFunction
	public long 到长整数() {
		return context.perform(
				new LuaJContext.Action<Long>() {
					@Override
					public Long run() throws Exception {
						return value.checklong();
					}
				});
	}

	/**
	 * 读取双精度数值；不能转换时抛出错误。
	 *
	 * @return 读取双精度数值；不能转换时抛出错误。
	 */
	@SimpleFunction
	public double 到小数() {
		return context.perform(
				new LuaJContext.Action<Double>() {
					@Override
					public Double run() throws Exception {
						return value.checkdouble();
					}
				});
	}

	/**
	 * 取得文本表示；nil 显示为 nil，表和函数显示其 Lua 表示。
	 *
	 * @return 取得文本表示；nil 显示为 nil，表和函数显示其 Lua 表示。
	 */
	@SimpleFunction
	public String 到文本() {
		return context.perform(
				new LuaJContext.Action<String>() {
					@Override
					public String run() throws Exception {
						return LuaJContext.display(value);
					}
				});
	}

	/**
	 * 取得字符串原始字节副本，保留零字节；目标须可作为字符串使用。
	 *
	 * @return 取得字符串原始字节副本，保留零字节；目标须可作为字符串使用。
	 */
	@SimpleFunction
	public byte[] 到字节集() {
		return context.perform(
				new LuaJContext.Action<byte[]>() {
					@Override
					public byte[] run() throws Exception {
						return LuaJContext.bytes(value);
					}
				});
	}

	/**
	 * 取得 Java 用户数据；目标不是用户数据时抛出错误。
	 *
	 * @return 取得 Java 用户数据；目标不是用户数据时抛出错误。
	 */
	@SimpleFunction
	public Object 到对象() {
		return context.perform(
				new LuaJContext.Action<Object>() {
					@Override
					public Object run() throws Exception {
						return value.checkuserdata();
					}
				});
	}

	/**
	 * 取得表包装；类型错误时抛出错误。
	 *
	 * @return 取得表包装；类型错误时抛出错误。
	 */
	@SimpleFunction
	public LuaJ表 作为表() {
		return context.perform(
				new LuaJContext.Action<LuaJ表>() {
					@Override
					public LuaJ表 run() throws Exception {
						return new LuaJ表(context, value.checktable());
					}
				});
	}

	/**
	 * 取得函数包装；类型错误时抛出错误。
	 *
	 * @return 取得函数包装；类型错误时抛出错误。
	 */
	@SimpleFunction
	public LuaJ函数 作为函数() {
		return context.perform(
				new LuaJContext.Action<LuaJ函数>() {
					@Override
					public LuaJ函数 run() throws Exception {
						return (LuaJ函数) context.wrap(value.checkfunction());
					}
				});
	}

	/**
	 * 取得协程包装；类型错误时抛出错误。
	 *
	 * @return 取得协程包装；类型错误时抛出错误。
	 */
	@SimpleFunction
	public LuaJ协程 作为协程() {
		return context.perform(
				new LuaJContext.Action<LuaJ协程>() {
					@Override
					public LuaJ协程 run() throws Exception {
						return new LuaJ协程(context, value.checkthread());
					}
				});
	}

	/**
	 * 取得 Lua 字符串或表的长度；含空洞表不以字段数量解释。
	 *
	 * @return 取得 Lua 字符串或表的长度；含空洞表不以字段数量解释。
	 */
	@SimpleFunction
	public int 取长度() {
		return context.perform(
				new LuaJContext.Action<Integer>() {
					@Override
					public Integer run() throws Exception {
						return value.length();
					}
				});
	}

	/**
	 * 读取元表；没有元表时返回 nil 包装。
	 *
	 * @return 读取元表；没有元表时返回 nil 包装。
	 */
	@SimpleFunction
	public LuaJ值 取元表() {
		return context.perform(
				new LuaJContext.Action<LuaJ值>() {
					@Override
					public LuaJ值 run() throws Exception {
						return context.wrap(value.getmetatable());
					}
				});
	}

	/**
	 * 读取函数或线程环境；目标不支持时抛出错误。
	 *
	 * @return 读取函数或线程环境；目标不支持时抛出错误。
	 */
	@SimpleFunction
	public LuaJ值 取环境() {
		return context.perform(
				new LuaJContext.Action<LuaJ值>() {
					@Override
					public LuaJ值 run() throws Exception {
						return context.wrap(value.getfenv());
					}
				});
	}

	/**
	 * 执行 Lua 一元负号运算，支持对应元方法。
	 *
	 * @return 执行 Lua 一元负号运算，支持对应元方法。
	 */
	@SimpleFunction
	public LuaJ值 取相反数() {
		return context.perform(
				new LuaJContext.Action<LuaJ值>() {
					@Override
					public LuaJ值 run() throws Exception {
						return context.wrap(value.neg());
					}
				});
	}

	/**
	 * 读取字段，允许触发 __index 元方法。
	 *
	 * @param key 文本、数值或同一实例的 LuaJ值；不能为 nil。
	 * @return 字段值；不存在时为 nil。
	 */
	@SimpleFunction
	public LuaJ值 取字段(Variant key) {
		return context.perform(
				new LuaJContext.Action<LuaJ值>() {
					@Override
					public LuaJ值 run() throws Exception {
						return context.wrap(value.get(context.convert(key)));
					}
				});
	}

	/**
	 * 设置字段，允许触发 __newindex 元方法。
	 *
	 * @param key 文本、数值或同一实例的 LuaJ值；不能为 nil。
	 * @param value 基本值、字节集、Java 对象或同一实例的 LuaJ值；空值表示 nil。
	 */
	@SimpleFunction
	public void 置字段(Variant key, Variant value) {
		context.perform(
				new LuaJContext.Action<Void>() {
					@Override
					public Void run() throws Exception {
						LuaJ值.this.value.set(context.convert(key), context.convert(value));
						return null;
					}
				});
	}

	/**
	 * 直接读取表字段，不触发元方法；目标须为表。
	 *
	 * @param key 文本、数值或同一实例的 LuaJ值；不能为 nil。
	 * @return 字段值；不存在时为 nil。
	 */
	@SimpleFunction
	public LuaJ值 原样取字段(Variant key) {
		return context.perform(
				new LuaJContext.Action<LuaJ值>() {
					@Override
					public LuaJ值 run() throws Exception {
						return context.wrap(value.checktable().rawget(context.convert(key)));
					}
				});
	}

	/**
	 * 直接设置表字段，不触发元方法；nil 删除字段。
	 *
	 * @param key 文本、数值或同一实例的 LuaJ值；不能为 nil。
	 * @param value 基本值、字节集、Java 对象或同一实例的 LuaJ值；空值表示 nil。
	 */
	@SimpleFunction
	public void 原样置字段(Variant key, Variant value) {
		context.perform(
				new LuaJContext.Action<Void>() {
					@Override
					public Void run() throws Exception {
						LuaJ值.this
								.value
								.checktable()
								.rawset(context.convert(key), context.convert(value));
						return null;
					}
				});
	}

	/**
	 * 设置或移除元表；必须使用返回对象，弱表模式变化可能返回另一张表。
	 *
	 * @param value 基本值、字节集、Java 对象或同一实例的 LuaJ值；空值表示 nil。
	 * @return 设置后的值；弱表模式变化时使用此返回对象。
	 */
	@SimpleFunction
	public LuaJ值 置元表(Variant value) {
		return context.perform(
				new LuaJContext.Action<LuaJ值>() {
					@Override
					public LuaJ值 run() throws Exception {
						LuaValue table = context.convert(value);
						return context.wrap(
								LuaJ值.this.value.setmetatable(
										table.isnil() ? null : table.checktable()));
					}
				});
	}

	/**
	 * 设置函数或线程环境，目标和环境须来自同一实例。
	 *
	 * @param environment 同一实例的非空环境表。
	 */
	@SimpleFunction
	public void 置环境(LuaJ表 environment) {
		context.perform(
				new LuaJContext.Action<Void>() {
					@Override
					public Void run() throws Exception {
						value.setfenv(context.unwrap(environment).checktable());
						return null;
					}
				});
	}

	/**
	 * 执行 Lua 相加运算，遵循对应元方法。
	 *
	 * @param other 同一实例的另一操作数。
	 * @return 运算结果。
	 */
	@SimpleFunction
	public LuaJ值 相加(LuaJ值 other) {
		return context.perform(
				new LuaJContext.Action<LuaJ值>() {
					@Override
					public LuaJ值 run() throws Exception {
						return context.wrap(value.add(context.unwrap(other)));
					}
				});
	}

	/**
	 * 执行 Lua 相减运算，遵循对应元方法。
	 *
	 * @param other 同一实例的另一操作数。
	 * @return 运算结果。
	 */
	@SimpleFunction
	public LuaJ值 相减(LuaJ值 other) {
		return context.perform(
				new LuaJContext.Action<LuaJ值>() {
					@Override
					public LuaJ值 run() throws Exception {
						return context.wrap(value.sub(context.unwrap(other)));
					}
				});
	}

	/**
	 * 执行 Lua 相乘运算，遵循对应元方法。
	 *
	 * @param other 同一实例的另一操作数。
	 * @return 运算结果。
	 */
	@SimpleFunction
	public LuaJ值 相乘(LuaJ值 other) {
		return context.perform(
				new LuaJContext.Action<LuaJ值>() {
					@Override
					public LuaJ值 run() throws Exception {
						return context.wrap(value.mul(context.unwrap(other)));
					}
				});
	}

	/**
	 * 执行 Lua 相除运算，遵循对应元方法。
	 *
	 * @param other 同一实例的另一操作数。
	 * @return 运算结果。
	 */
	@SimpleFunction
	public LuaJ值 相除(LuaJ值 other) {
		return context.perform(
				new LuaJContext.Action<LuaJ值>() {
					@Override
					public LuaJ值 run() throws Exception {
						return context.wrap(value.div(context.unwrap(other)));
					}
				});
	}

	/**
	 * 执行 Lua 求余运算，遵循对应元方法。
	 *
	 * @param other 同一实例的另一操作数。
	 * @return 运算结果。
	 */
	@SimpleFunction
	public LuaJ值 求余(LuaJ值 other) {
		return context.perform(
				new LuaJContext.Action<LuaJ值>() {
					@Override
					public LuaJ值 run() throws Exception {
						return context.wrap(value.mod(context.unwrap(other)));
					}
				});
	}

	/**
	 * 执行 Lua 求次方运算，遵循对应元方法。
	 *
	 * @param other 同一实例的另一操作数。
	 * @return 运算结果。
	 */
	@SimpleFunction
	public LuaJ值 求次方(LuaJ值 other) {
		return context.perform(
				new LuaJContext.Action<LuaJ值>() {
					@Override
					public LuaJ值 run() throws Exception {
						return context.wrap(value.pow(context.unwrap(other)));
					}
				});
	}

	/**
	 * 执行 Lua 连接运算，遵循对应元方法。
	 *
	 * @param other 同一实例的另一操作数。
	 * @return 运算结果。
	 */
	@SimpleFunction
	public LuaJ值 连接(LuaJ值 other) {
		return context.perform(
				new LuaJContext.Action<LuaJ值>() {
					@Override
					public LuaJ值 run() throws Exception {
						return context.wrap(value.concat(context.unwrap(other)));
					}
				});
	}

	/**
	 * 按 Lua 相等规则比较，可触发 __eq。
	 *
	 * @param other 同一实例的另一操作数。
	 * @return 比较结果。
	 */
	@SimpleFunction
	public boolean 是否相等(LuaJ值 other) {
		return context.perform(
				new LuaJContext.Action<Boolean>() {
					@Override
					public Boolean run() throws Exception {
						return value.eq_b(context.unwrap(other));
					}
				});
	}

	/**
	 * 不触发元方法比较。
	 *
	 * @param other 同一实例的另一操作数。
	 * @return 比较结果。
	 */
	@SimpleFunction
	public boolean 是否原样相等(LuaJ值 other) {
		return context.perform(
				new LuaJContext.Action<Boolean>() {
					@Override
					public Boolean run() throws Exception {
						return value.raweq(context.unwrap(other));
					}
				});
	}

	/**
	 * 按 Lua 小于规则比较，可触发元方法。
	 *
	 * @param other 同一实例的另一操作数。
	 * @return 比较结果。
	 */
	@SimpleFunction
	public boolean 是否小于(LuaJ值 other) {
		return context.perform(
				new LuaJContext.Action<Boolean>() {
					@Override
					public Boolean run() throws Exception {
						return value.lt_b(context.unwrap(other));
					}
				});
	}

	/**
	 * 按 Lua 小于等于规则比较，可触发元方法。
	 *
	 * @param other 同一实例的另一操作数。
	 * @return 比较结果。
	 */
	@SimpleFunction
	public boolean 是否不大于(LuaJ值 other) {
		return context.perform(
				new LuaJContext.Action<Boolean>() {
					@Override
					public Boolean run() throws Exception {
						return value.lteq_b(context.unwrap(other));
					}
				});
	}
}
