package simple.library.wsd.luaj;

import java.io.ByteArrayOutputStream;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.LuaClosure;
import org.luaj.vm2.compiler.DumpState;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.variants.Variant;

/**
 * 同一实例拥有的 Lua 函数或已编译代码块，调用保留全部返回值。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class LuaJ函数 extends LuaJ值 {

	/**
	 * 包装当前实例的函数。
	 *
	 * @param context 所属实例。
	 * @param value   有效函数。
	 */
	LuaJ函数(LuaJContext context, LuaValue value) {
		super(context, value);
	}

	/**
	 * 导出 Lua 代码的本版本字节码；宿主 Java 函数不能导出。不保存闭包捕获值或全局环境，重新加载使用目标实例环境。
	 *
	 * @param stripDebug 是否移除源码行号等调试信息。
	 * @return 独立字节数组。
	 * @throws LuaJ错误 参数、转换或 Lua 操作失败。
	 */
	@SimpleFunction
	public byte[] 导出字节码(boolean stripDebug) {
		return context.perform(() -> {
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			int status = DumpState.dump(value.checkclosure().p, output, stripDebug);
			if (status != 0) {
				throw new LuaJ错误("导出字节码失败：" + status);
			}
			return output.toByteArray();
		});
	}

	/**
	 * 取得 Lua 闭包捕获值的数量；宿主 Java 函数不支持。
	 *
	 * @return 非负捕获值数量。
	 * @throws LuaJ错误 不是 Lua 代码函数、位置无效或值属于另一实例。
	 */
	@SimpleFunction
	public int 取捕获值数() {
		return context.perform(() -> {
			return value.checkclosure().upValues.length;
		});
	}

	/**
	 * 取得当前捕获值的同实例视图；重新加载的字节码初始捕获值为 nil。
	 *
	 * @param index 从 1 开始的捕获值位置。
	 * @return 捕获值，包括有效的 nil 包装。
	 * @throws LuaJ错误 不是 Lua 代码函数、位置无效或值属于另一实例。
	 */
	@SimpleFunction
	public LuaJ值 取捕获值(int index) {
		return context.perform(() -> {
			LuaClosure closure = value.checkclosure();
			if (index < 1 || index > closure.upValues.length) {
				throw new LuaJ错误("捕获值位置超出范围。");
			}
			return context.wrap(closure.upValues[index - 1].getValue());
		});
	}

	/**
	 * 取得捕获变量的调试名称；导出时移除调试信息后名称可能为空。
	 *
	 * @param index 从 1 开始的捕获值位置。
	 * @return 变量名；没有调试名称时为空文本。
	 * @throws LuaJ错误 不是 Lua 代码函数、位置无效或值属于另一实例。
	 */
	@SimpleFunction
	public String 取捕获值名称(int index) {
		return context.perform(() -> {
			LuaClosure closure = value.checkclosure();
			if (index < 1 || index > closure.upValues.length) {
				throw new LuaJ错误("捕获值位置超出范围。");
			}
			return closure.p.upvalues != null && index <= closure.p.upvalues.length
					&& closure.p.upvalues[index - 1] != null ? LuaJContext.display(closure.p.upvalues[index - 1]) : "";
		});
	}

	/**
	 * 设置现有捕获值，可能影响共享同一捕获变量的其它闭包；可用于恢复字节码不保存的捕获值。
	 *
	 * @param index       从 1 开始的捕获值位置。
	 * @param replacement 新值；空值表示 nil，引用必须属于同一实例。
	 * @throws LuaJ错误 不是 Lua 代码函数、位置无效或值属于另一实例。
	 */
	@SimpleFunction
	public void 置捕获值(int index, Variant replacement) {
		context.perform(() -> {
			LuaClosure closure = value.checkclosure();
			if (index < 1 || index > closure.upValues.length) {
				throw new LuaJ错误("捕获值位置超出范围。");
			}
			closure.upValues[index - 1].setValue(context.convert(replacement));
			return null;
		});
	}

}
