package simple.library.wsd.luaj;

import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.WeakTable;
import org.luaj.vm2.lib.DebugLib;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.variants.Variant;

/**
 * Lua 5.1 脚本环境；加载不执行，执行和调用保留全部返回值。同步操作由调用线程执行，耗时脚本须放在后台。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class LuaJ实例 {

	final LuaJContext context;

	/**
	 * 创建包含标准库及 Java 互操作能力的独立全局环境；不提供安全沙箱。
	 */
	public LuaJ实例() {
		context = new LuaJContext();
	}

	/**
	 * 编译代码并返回未执行的函数。
	 *
	 * @param code   完整的 Lua 5.1 代码。
	 * @param source 供错误信息定位的名称，例如 script。
	 * @return 未执行的代码块。
	 */
	@SimpleFunction
	public LuaJ函数 加载代码(String code, String source) {
		return context.perform(() -> context.load(code, source));
	}

	/**
	 * 加载并执行代码，保留中间及末尾 nil。
	 *
	 * @param code 完整的 Lua 5.1 代码。
	 * @return 全部结果；无返回值时数量为零。
	 */
	@SimpleFunction
	public LuaJ参数 执行代码(String code) {
		return context.perform(() -> new LuaJ参数(context, context.load(code, "script").value.invoke()));
	}

	/**
	 * 按指定编码编译文件，不立即执行。相对路径以应用私有 files 目录为基准。
	 *
	 * @param path     可读取的绝对或应用私有相对文件路径。
	 * @param encoding 有效字符集名称，例如 UTF-8。
	 * @return 未执行的代码块。
	 */
	@SimpleFunction
	public LuaJ函数 加载文件(String path, String encoding) {
		return context.perform(() -> context.loadFile(path, encoding));
	}

	/**
	 * 按指定编码读取并执行文件，保留全部返回值。
	 *
	 * @param path     可读取的绝对或应用私有相对文件路径。
	 * @param encoding 有效字符集名称，例如 UTF-8。
	 * @return 全部结果。
	 */
	@SimpleFunction
	public LuaJ参数 执行文件(String path, String encoding) {
		return context.perform(() -> new LuaJ参数(context, context.loadFile(path, encoding).value.invoke()));
	}

	/**
	 * 按单个名称读取原始全局字段；不存在时返回 nil 值。
	 *
	 * @param name 单个全局名称，不解析点号路径。
	 * @return 对应值；不存在时仍返回 nil 包装。
	 */
	@SimpleFunction
	public LuaJ值 取全局(String name) {
		return context.perform(() -> context.wrap(context.globals.rawget(LuaJContext.text(name))));
	}

	/**
	 * 按单个名称设置原始全局字段；设置 nil 删除字段。
	 *
	 * @param name  单个全局名称，不解析点号路径。
	 * @param value 基本值、字节集、Java 对象或同一实例的 LuaJ值；空值表示 nil。
	 */
	@SimpleFunction
	public void 置全局(String name, Variant value) {
		context.perform(() -> {
			context.globals.rawset(LuaJContext.text(name), context.convert(value));
			return null;
		});
	}

	/**
	 * 取得指定全局函数；目标不是函数时抛出错误。
	 *
	 * @param name 单个全局名称，不解析点号路径。
	 * @return 同一实例的函数对象。
	 */
	@SimpleFunction
	public LuaJ函数 取函数(String name) {
		return context.perform(() -> {
			LuaValue function = context.globals.rawget(LuaJContext.text(name));
			function.checkfunction();
			return (LuaJ函数) context.wrap(function);
		});
	}

	/**
	 * 转换 Simple 值；字节集复制为 Lua 字符串，长整数按双精度表示，可能丢失精度。
	 *
	 * @param value 基本值、字节集、Java 对象或同一实例的 LuaJ值；空值表示 nil。
	 * @return 同一实例的 Lua 值。
	 */
	@SimpleFunction
	public LuaJ值 创建值(Variant value) {
		return context.perform(() -> context.wrap(context.convert(value)));
	}

	/**
	 * 建立参数列表，空元素和末尾 nil 保留位置。
	 *
	 * @param arguments 按顺序传入的参数；空引用或空数组表示没有参数，空元素保留为 nil。
	 * @return 独立参数快照。
	 */
	@SimpleFunction
	public LuaJ参数 创建参数(Variant[] arguments) {
		return context.perform(() -> new LuaJ参数(context, context.arguments(arguments)));
	}

	/**
	 * 创建空表。
	 *
	 * @return 新表。
	 */
	@SimpleFunction
	public LuaJ表 创建表() {
		return context.perform(() -> new LuaJ表(context, new LuaTable()));
	}

	/**
	 * 创建使用弱键或弱值的表；条目的保留由垃圾回收决定。
	 *
	 * @param weakKeys   是否使用弱键。
	 * @param weakValues 是否使用弱值。
	 * @return 新弱表。
	 */
	@SimpleFunction
	public LuaJ表 创建弱表(boolean weakKeys, boolean weakValues) {
		return context.perform(() -> {
			LuaTable table = new WeakTable(weakKeys, weakValues);
			LuaTable meta = new LuaTable();
			meta.rawset(LuaJContext.text("__mode"), LuaJContext.text((weakKeys ? "k" : "") + (weakValues ? "v" : "")));
			return new LuaJ表(context, (LuaTable) table.setmetatable(meta));
		});
	}

	/**
	 * 以同一实例的函数创建协程，恢复时才开始执行；挂起后应由调用者继续恢复至结束。
	 *
	 * @param function 同一实例的非空函数。
	 * @return 尚未运行的协程。
	 */
	@SimpleFunction
	public LuaJ协程 创建协程(LuaJ函数 function) {
		return context.perform(() -> {
			LuaValue functionValue = context.unwrap(function);
			functionValue.checkfunction();
			return new LuaJ协程(context, context.thread(functionValue));
		});
	}

	/**
	 * 创建用于串接文本及原始字节的缓冲区。
	 *
	 * @return 新缓冲区。
	 */
	@SimpleFunction
	public LuaJ缓冲区 创建缓冲区() {
		return context.perform(() -> new LuaJ缓冲区(context));
	}

	/**
	 * 注册同步 Simple 宿主回调，同名全局值被替换；回调在实际 Lua 执行线程调用，不能创建或重入其它实例。
	 *
	 * @param name     单个全局名称，不解析点号路径。
	 * @param callback 继承 LuaJ回调并覆写处理调用的非空对象。
	 */
	@SimpleFunction
	public void 注册函数(String name, LuaJ回调 callback) {
		context.perform(() -> {
			if (callback == null) {
				throw new LuaJ错误("宿主回调不能为空");
			}
			context.globals.rawset(LuaJContext.text(name), context.function(callback).value);
			return null;
		});
	}

	/**
	 * 取得本实例最近一次失败的说明；成功操作不会清除，清除错误方法可显式重置。
	 *
	 * @return 最近失败说明；没有错误时为空文本。
	 */
	@SimpleFunction
	public String 取错误信息() {
		return context.lastError;
	}

	/**
	 * 清除本实例保存的最近失败说明。
	 */
	@SimpleFunction
	public void 清除错误() {
		context.lastError = "";
	}

	/**
	 * 从独立字节快照加载代码，接受 UTF-8 BOM；只编译，不执行。不保证与其他 Lua 实现的字节码兼容。
	 *
	 * @param data   Lua 源码原始字节，或本版本 LuaJ 生成的字节码。
	 * @param source 错误定位使用的来源名称。
	 * @return 尚未执行的代码块。
	 * @throws LuaJ错误 参数、转换或 Lua 操作失败。
	 */
	@SimpleFunction
	public LuaJ函数 加载字节集(byte[] data, String source) {
		return context.perform(() -> context.loadBytes(data, source));
	}

	/**
	 * 加载并执行原始字节，保留全部返回值。只运行可信内容。
	 *
	 * @param data 源码或本版本字节码。
	 * @return 包含 nil 位置的全部返回值。
	 * @throws LuaJ错误 参数、转换或 Lua 操作失败。
	 */
	@SimpleFunction
	public LuaJ参数 执行字节集(byte[] data) {
		return context.perform(() -> new LuaJ参数(context, context.loadBytes(data, "bytes").value.invoke()));
	}

	/**
	 * 读取并加载 APK 资产，不执行；文件操作应在后台完成。
	 *
	 * @param name APK 资产相对路径；源码应为 UTF-8，也接受本版本字节码。
	 * @return 尚未执行的代码块。
	 * @throws LuaJ错误 参数、转换或 Lua 操作失败。
	 */
	@SimpleFunction
	public LuaJ函数 加载资产(String name) {
		return context.perform(() -> context.loadAsset(name));
	}

	/**
	 * 读取并执行资产，保留全部返回值；应在后台线程调用。
	 *
	 * @param name APK 资产中的 UTF-8 源码或本版本字节码路径。
	 * @return 全部返回值。
	 * @throws LuaJ错误 参数、转换或 Lua 操作失败。
	 */
	@SimpleFunction
	public LuaJ参数 执行资产(String name) {
		return context.perform(() -> new LuaJ参数(context, context.loadAsset(name).value.invoke()));
	}

	/**
	 * 创建不占用全局名称的宿主函数，可传给 Lua 或用作表成员。
	 *
	 * @param callback 继承 LuaJ回调并覆写处理调用的非空对象。
	 * @return 同实例的宿主函数。
	 * @throws LuaJ错误 参数、转换或 Lua 操作失败。
	 */
	@SimpleFunction
	public LuaJ函数 创建回调函数(LuaJ回调 callback) {
		return context.perform(() -> context.function(callback));
	}

	/**
	 * 取得当前实例实际的全局环境表；修改会影响后续执行。
	 *
	 * @return 当前全局表视图。
	 * @throws LuaJ错误 参数、转换或 Lua 操作失败。
	 */
	@SimpleFunction
	public LuaJ表 取全局表() {
		return context.perform(() -> new LuaJ表(context, context.globals));
	}

	/**
	 * 装载 Lua debug 标准库，供脚本读取栈、闭包信息和设置钩子。重复调用会替换 debug 表；不是安全隔离或强制中断机制。
	 *
	 * @throws LuaJ错误 参数、转换或 Lua 操作失败。
	 */
	@SimpleFunction
	public void 启用调试库() {
		context.perform(() -> {
			context.globals.load(new DebugLib());
			return null;
		});
	}

}
