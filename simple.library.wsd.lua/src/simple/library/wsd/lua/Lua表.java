package simple.library.wsd.lua;

import java.util.ArrayList;
import java.util.List;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.variants.Variant;

/**
 * Lua 表的稳定引用，支持原始字段读写及键快照遍历。
 *
 * <p>字段访问使用 raw 操作，不触发 __index、__newindex 或长度元方法。
 * 数值键遵循 Lua 规则，数组习惯从 1 开始；键数组的 Java 索引从 0 开始。</p>
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public class Lua表 extends Lua值 {

	/**
	 * 接管已确认类型为表的注册表引用。
	 *
	 * @param context 引用所属的解释器上下文。
	 * @param reference 已建立的表引用编号。
	 */
	Lua表(LuaContext context, int reference) {
		super(context, reference);
	}

	/* 原始成员读写 */

	/**
	 * 按键读取原始成员，不存在时返回 nil 引用，不改变原栈。
	 *
	 * @param key 文本、数值或其它合法 Lua 键；不能为 nil 或 NaN。
	 * @return 独立值引用，使用后释放。
	 */
	@SimpleFunction
	public Lua值 取成员(Variant key) {
		synchronized (context) {
			context.requireOpen();

			int top = context.state.getTop();

			try {
				context.reserve(3);
				requireValid();
				push(context);
				pushKey(key);
				context.state.rawGet(-2);
				return context.capture(-1);
			} finally {
				context.state.setTop(top);
			}
		}
	}

	/**
	 * 按键设置原始成员，结束后恢复原栈。
	 *
	 * @param key 不能为 nil 或 NaN；Lua值键必须来自同一实例。
	 * @param value 基本值、字节集、对象或同一实例的 Lua值；nil 表示删除成员。
	 */
	@SimpleFunction
	public void 置成员(Variant key, Variant value) {
		synchronized (context) {
			context.requireOpen();

			int top = context.state.getTop();

			try {
				context.reserve(3);
				requireValid();
				push(context);
				pushKey(key);
				context.pushVariant(value);
				context.state.rawSet(-3);
			} finally {
				context.state.setTop(top);
			}
		}
	}

	/**
	 * 移除指定键，不触发元方法。
	 *
	 * @param key 要删除的成员键。
	 */
	@SimpleFunction
	public void 移除成员(Variant key) {
		置成员(key, null);
	}

	/* 数组边界与键遍历 */

	/**
	 * 取得 Lua 5.1 原始数组边界；有空洞的表不保证返回成员总数。
	 *
	 * @return Lua 原始长度；字典成员不计入数组长度。
	 */
	@SimpleFunction
	public int 取长度() {
		synchronized (context) {
			context.requireOpen();

			int top = context.state.getTop();

			try {
				context.reserve(3);
				requireValid();
				push(context);
				return context.state.objLen(-1);
			} finally {
				context.state.setTop(top);
			}
		}
	}

	/**
	 * 取得当前全部键的独立引用快照，顺序由 Lua 决定，结果逐项释放。
	 *
	 * @return 键引用数组；不是实时迭代器，数组索引从 0 开始。
	 */
	@SimpleFunction
	public Lua值[] 取键() {
		synchronized (context) {
			requireValid();

			int top = context.state.getTop();
			List<Lua值> keys = new ArrayList<Lua值>();

			try {
				context.reserve(3);
				push(context);
				int tableIndex = context.state.getTop();
				context.state.pushNil();
				while (context.state.next(tableIndex) != 0) {
					keys.add(context.capture(-2));
					context.state.pop(1);
				}

				return keys.toArray(new Lua值[keys.size()]);
			} catch (RuntimeException failure) {
				LuaContext.release(keys);
				throw failure;
			} finally {
				context.state.setTop(top);
			}
		}
	}

	/* 表键校验 */

	/**
	 * 压入合法表键，拒绝把 nil 或 NaN 交给未保护的原生写操作。
	 *
	 * @param key 可转换为合法 Lua 键的 Simple 变体。
	 */
	private void pushKey(Variant key) {
		context.pushVariant(key);
		if (context.state.isNil(-1)
			|| (context.state.type(-1) == 类型_数值 && Double.isNaN(context.state.toNumber(-1)))) {
			throw LuaContext.error("Lua 表键不能为 nil 或 NaN");
		}
	}
}
