package simple.library.wsd.luaj;

import java.io.ByteArrayOutputStream;
import org.luaj.vm2.LuaString;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * 同一实例的文本及二进制串接缓冲区，取得结果时复制原始字节。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class LuaJ缓冲区 {

	final LuaJContext context;
	// 保持字节顺序，避开上游旧 Buffer 在首部插入后追加的扩容问题。
	private ByteArrayOutputStream buffer = new ByteArrayOutputStream();

	/**
	 * 创建空缓冲区。
	 *
	 * @param context 所属实例。
	 */
	LuaJ缓冲区(LuaJContext context) {
		this.context = context;
	}

	/**
	 * 把 UTF-8 文本追加到末尾。
	 *
	 * @param text 待追加文本。
	 */
	@SimpleFunction
	public void 添加文本(String text) {
		context.perform(() -> {
			byte[] bytes = LuaJContext.text(text).m_bytes;
			buffer.write(bytes, 0, bytes.length);
			return null;
		});
	}

	/**
	 * 把原始字节追加到末尾，不进行文本解码。
	 *
	 * @param bytes 非空字节数组。
	 */
	@SimpleFunction
	public void 添加字节集(byte[] bytes) {
		context.perform(() -> {
			if (bytes == null) {
				throw new LuaJ错误("字节集不能为空引用");
			}
			buffer.write(bytes, 0, bytes.length);
			return null;
		});
	}

	/**
	 * 追加同一实例的文本或数值，遵循 Lua 串接规则。
	 *
	 * @param value 同一实例的值。
	 */
	@SimpleFunction
	public void 添加值(LuaJ值 value) {
		context.perform(() -> {
			byte[] bytes = LuaJContext.bytes(context.unwrap(value));
			buffer.write(bytes, 0, bytes.length);
			return null;
		});
	}

	/**
	 * 把 UTF-8 文本插入首部。
	 *
	 * @param text 待插入文本。
	 */
	@SimpleFunction
	public void 插入首部(String text) {
		context.perform(() -> {
			byte[] prefix = LuaJContext.bytes(LuaJContext.text(text));
			byte[] suffix = buffer.toByteArray();
			buffer = new ByteArrayOutputStream();
			buffer.write(prefix, 0, prefix.length);
			buffer.write(suffix, 0, suffix.length);
			return null;
		});
	}

	/**
	 * 取得当前 Lua 字符串结果。
	 *
	 * @return 当前字符串值。
	 */
	@SimpleFunction
	public LuaJ值 取值() {
		return context.perform(() -> context.wrap(LuaString.valueOf(buffer.toByteArray())));
	}

	/**
	 * 按 UTF-8 取得当前文本。
	 *
	 * @return 当前文本。
	 */
	@SimpleFunction
	public String 到文本() {
		return context.perform(() -> {
			return new String(buffer.toByteArray(), java.nio.charset.StandardCharsets.UTF_8);
		});
	}

	/**
	 * 取得当前原始字节副本，保留零字节。
	 *
	 * @return 独立字节数组。
	 */
	@SimpleFunction
	public byte[] 到字节集() {
		return context.perform(() -> buffer.toByteArray());
	}

	/**
	 * 移除全部缓冲内容。
	 */
	@SimpleFunction
	public void 清空() {
		context.perform(() -> {
			buffer.reset();
			return null;
		});
	}
}
