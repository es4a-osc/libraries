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
	 * @param value   有效底层值。
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
		return context.perform(() -> value.type());
	}

	/**
	 * 取得 Lua 类型名称。
	 *
	 * @return 取得 Lua 类型名称。
	 */
	@SimpleFunction
	public String 取类型名() {
		return context.perform(() -> value.typename());
	}

	/**
	 * 判断是否为 Lua nil。
	 *
	 * @return 判断是否为 Lua nil。
	 */
	@SimpleFunction
	public boolean 是否为空() {
		return context.perform(() -> value.isnil());
	}

	/**
	 * 判断是否为逻辑类型。
	 *
	 * @return 判断是否为逻辑类型。
	 */
	@SimpleFunction
	public boolean 是否逻辑() {
		return context.perform(() -> value.isboolean());
	}

	/**
	 * 判断能否按 LuaJ 规则作为数值使用，包含可转换的数值文本。
	 *
	 * @return 判断能否按 LuaJ 规则作为数值使用，包含可转换的数值文本。
	 */
	@SimpleFunction
	public boolean 是否数值() {
		return context.perform(() -> value.isnumber());
	}

	/**
	 * 判断是否可作为 32 位整数使用。
	 *
	 * @return 判断是否可作为 32 位整数使用。
	 */
	@SimpleFunction
	public boolean 是否整数() {
		return context.perform(() -> value.isint());
	}

	/**
	 * 判断能否按 LuaJ 规则作为字符串使用，包含数值。
	 *
	 * @return 判断能否按 LuaJ 规则作为字符串使用，包含数值。
	 */
	@SimpleFunction
	public boolean 是否文本() {
		return context.perform(() -> value.isstring());
	}

	/**
	 * 判断是否为表。
	 *
	 * @return 判断是否为表。
	 */
	@SimpleFunction
	public boolean 是否为表() {
		return context.perform(() -> value.istable());
	}

	/**
	 * 判断是否为函数。
	 *
	 * @return 判断是否为函数。
	 */
	@SimpleFunction
	public boolean 是否函数() {
		return context.perform(() -> value.isfunction());
	}

	/**
	 * 判断是否为 Lua 协程。
	 *
	 * @return 判断是否为 Lua 协程。
	 */
	@SimpleFunction
	public boolean 是否协程() {
		return context.perform(() -> value.isthread());
	}

	/**
	 * 判断是否为 Java 用户数据。
	 *
	 * @return 判断是否为 Java 用户数据。
	 */
	@SimpleFunction
	public boolean 是否用户数据() {
		return context.perform(() -> value.isuserdata());
	}

	/**
	 * 按 Lua 真值规则转换：只有 nil 和 false 为假。
	 *
	 * @return 按 Lua 真值规则转换：只有 nil 和 false 为假。
	 */
	@SimpleFunction
	public boolean 到逻辑() {
		return context.perform(() -> value.toboolean());
	}

	/**
	 * 按 LuaJ 整数转换规则读取数值；不能转换时抛出错误。
	 *
	 * @return 按 LuaJ 整数转换规则读取数值；不能转换时抛出错误。
	 */
	@SimpleFunction
	public int 到整数() {
		return context.perform(() -> value.checkint());
	}

	/**
	 * 按 LuaJ 规则读取长整数；Lua 数值是双精度，大整数可能丢失精度。
	 *
	 * @return 按 LuaJ 规则读取长整数；Lua 数值是双精度，大整数可能丢失精度。
	 */
	@SimpleFunction
	public long 到长整数() {
		return context.perform(() -> value.checklong());
	}

	/**
	 * 读取双精度数值；不能转换时抛出错误。
	 *
	 * @return 读取双精度数值；不能转换时抛出错误。
	 */
	@SimpleFunction
	public double 到小数() {
		return context.perform(() -> value.checkdouble());
	}

	/**
	 * 取得文本表示；nil 显示为 nil，表和函数显示其 Lua 表示。
	 *
	 * @return 取得文本表示；nil 显示为 nil，表和函数显示其 Lua 表示。
	 */
	@SimpleFunction
	public String 到文本() {
		return context.perform(() -> LuaJContext.display(value));
	}

	/**
	 * 取得字符串原始字节副本，保留零字节；目标须可作为字符串使用。
	 *
	 * @return 取得字符串原始字节副本，保留零字节；目标须可作为字符串使用。
	 */
	@SimpleFunction
	public byte[] 到字节集() {
		return context.perform(() -> LuaJContext.bytes(value));
	}

	/**
	 * 取得 Java 用户数据；目标不是用户数据时抛出错误。
	 *
	 * @return 取得 Java 用户数据；目标不是用户数据时抛出错误。
	 */
	@SimpleFunction
	public Object 到对象() {
		return context.perform(() -> value.checkuserdata());
	}

	/**
	 * 取得表包装；类型错误时抛出错误。
	 *
	 * @return 取得表包装；类型错误时抛出错误。
	 */
	@SimpleFunction
	public LuaJ表 作为表() {
		return context.perform(() -> new LuaJ表(context, value.checktable()));
	}

	/**
	 * 取得函数包装；类型错误时抛出错误。
	 *
	 * @return 取得函数包装；类型错误时抛出错误。
	 */
	@SimpleFunction
	public LuaJ函数 作为函数() {
		return context.perform(() -> (LuaJ函数) context.wrap(value.checkfunction()));
	}

	/**
	 * 取得协程包装；类型错误时抛出错误。
	 *
	 * @return 取得协程包装；类型错误时抛出错误。
	 */
	@SimpleFunction
	public LuaJ协程 作为协程() {
		return context.perform(() -> new LuaJ协程(context, value.checkthread()));
	}

	/**
	 * 取得 Lua 字符串或表的长度；含空洞表不以字段数量解释。
	 *
	 * @return 取得 Lua 字符串或表的长度；含空洞表不以字段数量解释。
	 */
	@SimpleFunction
	public int 取长度() {
		return context.perform(() -> value.length());
	}

	/**
	 * 读取元表；没有元表时返回 nil 包装。
	 *
	 * @return 读取元表；没有元表时返回 nil 包装。
	 */
	@SimpleFunction
	public LuaJ值 取元表() {
		return context.perform(() -> context.wrap(value.getmetatable()));
	}

	/**
	 * 读取函数或线程环境；目标不支持时抛出错误。
	 *
	 * @return 读取函数或线程环境；目标不支持时抛出错误。
	 */
	@SimpleFunction
	public LuaJ值 取环境() {
		return context.perform(() -> context.wrap(value.getfenv()));
	}

	/**
	 * 执行 Lua 一元负号运算，支持对应元方法。
	 *
	 * @return 执行 Lua 一元负号运算，支持对应元方法。
	 */
	@SimpleFunction
	public LuaJ值 取相反数() {
		return context.perform(() -> context.wrap(value.neg()));
	}

	/**
	 * 读取字段，允许触发 __index 元方法。
	 *
	 * @param key Lua 键，可为可转换的基本值、对象或同实例值；写入时不能为 nil 或 NaN。
	 * @return 字段值；不存在时为 nil。
	 */
	@SimpleFunction
	public LuaJ值 取字段(Variant key) {
		return context.perform(() -> context.wrap(value.get(context.convert(key))));
	}

	/**
	 * 设置字段，允许触发 __newindex 元方法。
	 *
	 * @param key   Lua 键，可为可转换的基本值、对象或同实例值；写入时不能为 nil 或 NaN。
	 * @param value 基本值、字节集、Java 对象或同一实例的 LuaJ值；空值表示 nil。
	 */
	@SimpleFunction
	public void 置字段(Variant key, Variant value) {
		context.perform(() -> {
			LuaJ值.this.value.set(context.convert(key), context.convert(value));
			return null;
		});
	}

	/**
	 * 直接读取表字段，不触发元方法；目标须为表。
	 *
	 * @param key Lua 键，可为可转换的基本值、对象或同实例值；写入时不能为 nil 或 NaN。
	 * @return 字段值；不存在时为 nil。
	 */
	@SimpleFunction
	public LuaJ值 原样取字段(Variant key) {
		return context.perform(() -> context.wrap(value.checktable().rawget(context.convert(key))));
	}

	/**
	 * 直接设置表字段，不触发元方法；nil 删除字段。
	 *
	 * @param key   Lua 键，可为可转换的基本值、对象或同实例值；写入时不能为 nil 或 NaN。
	 * @param value 基本值、字节集、Java 对象或同一实例的 LuaJ值；空值表示 nil。
	 */
	@SimpleFunction
	public void 原样置字段(Variant key, Variant value) {
		context.perform(() -> {
			// 上游 rawset 假定调用者已经校验键，不能让 nil 或 NaN 进入哈希表。
			LuaJ值.this.value.checktable().rawset(context.convert(key).checkvalidkey(), context.convert(value));
			return null;
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
		return context.perform(() -> {
			LuaValue table = context.convert(value);
			return context.wrap(LuaJ值.this.value.setmetatable(table.isnil() ? null : table.checktable()));
		});
	}

	/**
	 * 设置函数或线程环境，目标和环境须来自同一实例。
	 *
	 * @param environment 同一实例的非空环境表。
	 */
	@SimpleFunction
	public void 置环境(LuaJ表 environment) {
		context.perform(() -> {
			value.setfenv(context.unwrap(environment).checktable());
			return null;
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
		return context.perform(() -> context.wrap(value.add(context.unwrap(other))));
	}

	/**
	 * 执行 Lua 相减运算，遵循对应元方法。
	 *
	 * @param other 同一实例的另一操作数。
	 * @return 运算结果。
	 */
	@SimpleFunction
	public LuaJ值 相减(LuaJ值 other) {
		return context.perform(() -> context.wrap(value.sub(context.unwrap(other))));
	}

	/**
	 * 执行 Lua 相乘运算，遵循对应元方法。
	 *
	 * @param other 同一实例的另一操作数。
	 * @return 运算结果。
	 */
	@SimpleFunction
	public LuaJ值 相乘(LuaJ值 other) {
		return context.perform(() -> context.wrap(value.mul(context.unwrap(other))));
	}

	/**
	 * 执行 Lua 相除运算，遵循对应元方法。
	 *
	 * @param other 同一实例的另一操作数。
	 * @return 运算结果。
	 */
	@SimpleFunction
	public LuaJ值 相除(LuaJ值 other) {
		return context.perform(() -> context.wrap(value.div(context.unwrap(other))));
	}

	/**
	 * 执行 Lua 求余运算，遵循对应元方法。
	 *
	 * @param other 同一实例的另一操作数。
	 * @return 运算结果。
	 */
	@SimpleFunction
	public LuaJ值 求余(LuaJ值 other) {
		return context.perform(() -> context.wrap(value.mod(context.unwrap(other))));
	}

	/**
	 * 执行 Lua 求次方运算，遵循对应元方法。
	 *
	 * @param other 同一实例的另一操作数。
	 * @return 运算结果。
	 */
	@SimpleFunction
	public LuaJ值 求次方(LuaJ值 other) {
		return context.perform(() -> context.wrap(value.pow(context.unwrap(other))));
	}

	/**
	 * 执行 Lua 连接运算，遵循对应元方法。
	 *
	 * @param other 同一实例的另一操作数。
	 * @return 运算结果。
	 */
	@SimpleFunction
	public LuaJ值 连接(LuaJ值 other) {
		return context.perform(() -> context.wrap(value.concat(context.unwrap(other))));
	}

	/**
	 * 按 Lua 相等规则比较，可触发 __eq。
	 *
	 * @param other 同一实例的另一操作数。
	 * @return 比较结果。
	 */
	@SimpleFunction
	public boolean 是否相等(LuaJ值 other) {
		return context.perform(() -> value.eq_b(context.unwrap(other)));
	}

	/**
	 * 不触发元方法比较。
	 *
	 * @param other 同一实例的另一操作数。
	 * @return 比较结果。
	 */
	@SimpleFunction
	public boolean 是否原样相等(LuaJ值 other) {
		return context.perform(() -> value.raweq(context.unwrap(other)));
	}

	/**
	 * 按 Lua 小于规则比较，可触发元方法。
	 *
	 * @param other 同一实例的另一操作数。
	 * @return 比较结果。
	 */
	@SimpleFunction
	public boolean 是否小于(LuaJ值 other) {
		return context.perform(() -> value.lt_b(context.unwrap(other)));
	}

	/**
	 * 按 Lua 小于等于规则比较，可触发元方法。
	 *
	 * @param other 同一实例的另一操作数。
	 * @return 比较结果。
	 */
	@SimpleFunction
	public boolean 是否不大于(LuaJ值 other) {
		return context.perform(() -> value.lteq_b(context.unwrap(other)));
	}

	/**
	 * 不传参数调用函数，保留全部结果。
	 *
	 * @return 全部返回值。
	 */
	@SimpleFunction
	public LuaJ参数 调用() {
		return context.perform(() -> new LuaJ参数(context, value.invoke()));
	}

	/**
	 * 传入 Simple 参数并调用，保留全部结果及 nil 位置。
	 *
	 * @param arguments 按顺序传入的参数；空引用或空数组表示没有参数，空元素保留为 nil。
	 * @return 全部返回值。
	 */
	@SimpleFunction
	public LuaJ参数 带参调用(Variant[] arguments) {
		return context.perform(() -> new LuaJ参数(context, value.invoke(context.arguments(arguments))));
	}

	/**
	 * 使用同一实例的参数列表调用，保留全部结果。
	 *
	 * @param arguments 同一实例的参数列表；空引用表示无参数。
	 * @return 全部返回值。
	 */
	@SimpleFunction
	public LuaJ参数 使用参数调用(LuaJ参数 arguments) {
		return context.perform(() -> {
			if (arguments != null && arguments.context != context) {
				throw new LuaJ错误("参数列表属于另一个 LuaJ 实例");
			}
			return new LuaJ参数(context, value.invoke(arguments == null ? LuaValue.NONE : arguments.values));
		});
	}

	/**
	 * 按 Lua 冒号调用规则调用成员函数，自动把当前值作为首个 self 实参。
	 *
	 * @param name      单个成员名称。
	 * @param arguments 额外实参；空引用表示无额外实参。
	 * @return 完整返回值。
	 * @throws LuaJ错误 参数、转换或 Lua 操作失败。
	 */
	@SimpleFunction
	public LuaJ参数 调用方法(String name, Variant[] arguments) {
		return context.perform(
				() -> new LuaJ参数(context, value.invokemethod(LuaJContext.text(name), context.arguments(arguments))));
	}

	/**
	 * 按 Lua 真值规则取反，只有 nil 和 false 为假。
	 *
	 * @return Lua 真值的反值。
	 * @throws LuaJ错误 参数、转换或 Lua 操作失败。
	 */
	@SimpleFunction
	public boolean 逻辑非() {
		return context.perform(() -> !value.toboolean());
	}

	/**
	 * 按 Lua 真值规则选择两个值之一，返回值不强制转成逻辑。两参数在传入前已计算，不延迟执行右侧表达式。
	 *
	 * @param other 同实例的另一个值；空引用视为 nil。
	 * @return 按 Lua 规则选出的值。
	 * @throws LuaJ错误 参数、转换或 Lua 操作失败。
	 */
	@SimpleFunction
	public LuaJ值 逻辑与(LuaJ值 other) {
		return context.perform(() -> context.wrap(value.and(context.unwrap(other))));
	}

	/**
	 * 按 Lua 真值规则选择两个值之一，返回值不强制转成逻辑。两参数在传入前已计算，不延迟执行右侧表达式。
	 *
	 * @param other 同实例的另一个值；空引用视为 nil。
	 * @return 按 Lua 规则选出的值。
	 * @throws LuaJ错误 参数、转换或 Lua 操作失败。
	 */
	@SimpleFunction
	public LuaJ值 逻辑或(LuaJ值 other) {
		return context.perform(() -> context.wrap(value.or(context.unwrap(other))));
	}

}
