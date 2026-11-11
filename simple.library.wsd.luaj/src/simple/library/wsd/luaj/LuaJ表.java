package simple.library.wsd.luaj;

import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.variants.Variant;

/**
 * Lua 表对象，支持原始字段、元表、顺序数组与键遍历。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class LuaJ表 extends LuaJ值 {

	/**
	 * 包装当前实例的表。
	 *
	 * @param context 所属实例。
	 * @param value   有效表。
	 */
	LuaJ表(LuaJContext context, LuaTable value) {
		super(context, value);
	}

	/**
	 * 取得当前非 nil 字段总数，不等同于顺序数组长度。
	 *
	 * @return 字段总数。
	 */
	@SimpleFunction
	public int 取字段数() {
		return context.perform(() -> value.checktable().keyCount());
	}

	/**
	 * 取得当前键快照，顺序未指定；遍历期间不反映后续增删。
	 *
	 * @return 键快照数组。
	 */
	@SimpleFunction
	public LuaJ值[] 取全部键() {
		return context.perform(() -> {
			LuaValue[] keys = value.checktable().keys();
			LuaJ值[] result = new LuaJ值[keys.length];
			for (int i = 0; i < keys.length; i++) {
				result[i] = context.wrap(keys[i]);
			}
			return result;
		});
	}

	/**
	 * 按 next 语义遍历；nil 键从头开始，结果包含键和值，结束时数量为零。
	 *
	 * @param key 上一项的键；nil 从头开始，非 nil 键须来自当前表。
	 * @return 键和值，或空结果。
	 */
	@SimpleFunction
	public LuaJ参数 取下一项(Variant key) {
		return context.perform(() -> {
			Varargs pair = value.checktable().next(context.convert(key));
			return new LuaJ参数(context, pair.arg1().isnil() ? LuaValue.NONE : pair);
		});
	}

	/**
	 * 在顺序数组位置插入值并移动后续成员；位置从 1 开始。
	 *
	 * @param index 1 到当前长度加 1。
	 * @param value 基本值、字节集、Java 对象或同一实例的 LuaJ值；空值表示 nil。
	 */
	@SimpleFunction
	public void 插入(int index, Variant value) {
		context.perform(() -> {
			int size = LuaJ表.this.value.length();
			if (index < 1 || index > size + 1) {
				throw new LuaJ错误("插入位置超出顺序数组范围");
			}
			LuaJ表.this.value.checktable().insert(index, context.convert(value));
			return null;
		});
	}

	/**
	 * 删除顺序数组的指定项并移动后续成员；位置从 1 开始。
	 *
	 * @param index 1 到当前顺序数组长度。
	 * @return 被删除的值。
	 */
	@SimpleFunction
	public LuaJ值 删除(int index) {
		return context.perform(() -> {
			if (index < 1 || index > value.length()) {
				throw new LuaJ错误("删除位置超出顺序数组范围");
			}
			return context.wrap(value.checktable().remove(index));
		});
	}

	/**
	 * 按 Lua 默认小于规则对顺序数组排序。
	 */
	@SimpleFunction
	public void 排序() {
		context.perform(() -> {
			value.checktable().sort(LuaValue.NIL);
			return null;
		});
	}

	/**
	 * 使用同一实例的比较函数排序；比较函数接收两个值并返回逻辑值。
	 *
	 * @param comparator 同一实例的比较函数。
	 */
	@SimpleFunction
	public void 自定义排序(LuaJ函数 comparator) {
		context.perform(() -> {
			value.checktable().sort(context.unwrap(comparator).checkfunction());
			return null;
		});
	}

	/**
	 * 把指定顺序范围内的文本或数值连接为字符串。
	 *
	 * @param separator 成员间分隔文本。
	 * @param first     首项位置，1 到顺序数组长度加 1。
	 * @param last      包含该项，范围为 0 到顺序数组长度；首项大于末项时为空范围。
	 * @return 连接结果，空范围返回空文本。
	 */
	@SimpleFunction
	public String 连接文本(String separator, int first, int last) {
		return context.perform(() -> {
			if (first < 1 || first > value.length() + 1 || last < 0 || last > value.length()) {
				throw new LuaJ错误("连接范围无效");
			}
			return LuaJContext.display(value.checktable().concat(LuaJContext.text(separator), first, last));
		});
	}
}
