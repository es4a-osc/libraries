package simple.library.wsd.xhttp2;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Locale;

import android.content.Context;
import android.net.Uri;

import com.google.gson.JsonElement;

import okhttp3.Headers;
import okhttp3.MediaType;

/**
 * 请求字段、文件来源、摘要及 JSON 路径的共享规则，不持有客户端或窗口状态。
 *
 * @author 树先生 xhwsd@qq.com
 */
final class HttpSupport {

	/**
	 * 静态辅助方法集，不创建实例。
	 */
	private HttpSupport() {
	}

	/**
	 * 可选文本采用空文本，必要文本在对应操作边界单独校验。
	 *
	 * @param value 待校验或转换的值。
	 * @return 独立的结果值或快照，语义与方法说明一致。
	 */
	static String text(String value) {
		return value == null ? "" : value;
	}

	/**
	 * 检查必需文本并返回原值，不裁剪字段内容。
	 *
	 * @param value 待校验或转换的值。
	 * @return 检查必需文本并返回原值，不裁剪字段内容。
	 */
	static String nonempty(String value) {
		if (value == null || value.isEmpty()) {
			throw new IllegalArgumentException("文本不能为空。");
		}
		return value;
	}

	/**
	 * 拒绝空引用并复制字节内容。
	 *
	 * @param value 待校验或转换的值。
	 * @return 拒绝空引用并复制字节内容。
	 */
	static byte[] bytes(byte[] value) {
		if (value == null) {
			throw new IllegalArgumentException("字节数组不能为空。");
		}
		return value.clone();
	}

	/**
	 * 检查数值没有低于允许下限。
	 *
	 * @param value   待校验或转换的值。
	 * @param minimum 包含在允许范围内的最小值。
	 */
	static void minimum(long value, long minimum) {
		if (value < minimum) {
			throw new IllegalArgumentException("数值不能小于 " + minimum + "。");
		}
	}

	/**
	 * 检查数值没有超过允许上限。
	 *
	 * @param value   待校验或转换的值。
	 * @param maximum 包含在允许范围内的最大值。
	 */
	static void maximum(long value, long maximum) {
		if (value > maximum) {
			throw new IllegalArgumentException("数值不能大于 " + maximum + "。");
		}
	}

	/**
	 * 检查整数处于包含两端的合法范围。
	 *
	 * @param value   待校验或转换的值。
	 * @param minimum 包含在允许范围内的最小值。
	 * @param maximum 包含在允许范围内的最大值。
	 */
	static void range(int value, int minimum, int maximum) {
		if (value < minimum || value > maximum) {
			throw new IllegalArgumentException("数值须处于 " + minimum + " 至 " + maximum + "。");
		}
	}

	/**
	 * 规范 HTTP 方法并拒绝当前未支持的方法。
	 *
	 * @param value 待校验或转换的值。
	 * @return 规范 HTTP 方法并拒绝当前未支持的方法。
	 */
	static String method(String value) {
		String result = nonempty(value).toUpperCase(Locale.ROOT);
		if (!result.matches("GET|POST|PUT|PATCH|DELETE|HEAD|OPTIONS")) {
			throw new IllegalArgumentException("不支持的 HTTP 方法：" + value);
		}
		return result;
	}

	/**
	 * 解析非空媒体类型，无效时抛出参数错误。
	 *
	 * @param value 待校验或转换的值。
	 * @return 解析非空媒体类型，无效时抛出参数错误。
	 */
	static MediaType media(String value) {
		MediaType result = MediaType.parse(nonempty(value));
		if (result == null) {
			throw new IllegalArgumentException("媒体类型无效。");
		}
		return result;
	}

	/**
	 * 按响应字符集解码内容，未指定时采用 UTF-8。
	 *
	 * @param bytes 原始字节内容。
	 * @param type  有效的 MIME 类型及可选字符集。
	 * @return 按响应字符集解码内容，未指定时采用 UTF-8。
	 */
	static String decode(byte[] bytes, String type) {
		MediaType media = type.isEmpty() ? null : MediaType.parse(type);
		return new String(bytes, media == null ? StandardCharsets.UTF_8 : media.charset(StandardCharsets.UTF_8));
	}

	/**
	 * 先校验字段再追加，保留同名字段和原有顺序。
	 *
	 * @param target 接收字段或文件内容的目标。
	 * @param name   字段或会话名称。
	 * @param value  待校验或转换的值。
	 * @param header 完整 Set-Cookie 字段内容。
	 */
	static void add(List<String[]> target, String name, String value, boolean header) {
		nonempty(name);
		if (header) {
			// 复用实际传输层的字段校验，拒绝换行注入及无效名称。
			new Headers.Builder().add(name, text(value));
		}
		target.add(new String[] { name, text(value) });
	}

	/**
	 * 替换先校验新值，再删除旧值，非法输入不得破坏原有请求配置。
	 *
	 * @param target 接收字段或文件内容的目标。
	 * @param name   字段或会话名称。
	 * @param value  待校验或转换的值。
	 */
	static void setHeader(List<String[]> target, String name, String value) {
		new Headers.Builder().add(nonempty(name), text(value));
		remove(target, name, true);
		target.add(new String[] { name, text(value) });
	}

	/**
	 * 删除全部同名字段，按指定规则比较名称。
	 *
	 * @param target     接收字段或文件内容的目标。
	 * @param name       字段或会话名称。
	 * @param ignoreCase 是否忽略名称的大小写。
	 */
	static void remove(List<String[]> target, String name, boolean ignoreCase) {
		for (int index = target.size() - 1; index >= 0; index--) {
			String field = target.get(index)[0];
			if (ignoreCase ? field.equalsIgnoreCase(name) : field.equals(name)) {
				target.remove(index);
			}
		}
	}

	/**
	 * 取得首个同名请求头，不存在时返回空文本。
	 *
	 * @param pairs 按顺序保存的字段对。
	 * @param name  字段或会话名称。
	 * @return 首个同名请求头，不存在时返回空文本。
	 */
	static String first(List<String[]> pairs, String name) {
		for (String[] pair : pairs) {
			if (pair[0].equalsIgnoreCase(name)) {
				return pair[1];
			}
		}
		return "";
	}

	/**
	 * 校验后替换全部同名参数，保留其它字段。
	 *
	 * @param pairs 按顺序保存的字段对。
	 * @param name  字段或会话名称。
	 * @param value 待校验或转换的值。
	 */
	static void set(List<String[]> pairs, String name, String value) {
		nonempty(name);
		remove(pairs, name, false);
		add(pairs, name, value, false);
	}

	/**
	 * 按原顺序取得全部同名字段值，结果数组独立于原集合。
	 *
	 * @param pairs      按顺序保存的字段对。
	 * @param name       字段或会话名称。
	 * @param ignoreCase 是否忽略名称的大小写。
	 * @return 按原顺序取得全部同名字段值，结果数组独立于原集合。
	 */
	static String[] values(List<String[]> pairs, String name, boolean ignoreCase) {
		nonempty(name);
		List<String> result = new ArrayList<String>();
		for (String[] pair : pairs) {
			if (ignoreCase ? pair[0].equalsIgnoreCase(name) : pair[0].equals(name)) {
				result.add(pair[1]);
			}
		}
		return result.toArray(new String[result.size()]);
	}

	/**
	 * 取得不重复名称，保留首次出现的大小写及顺序。
	 *
	 * @param pairs      按顺序保存的字段对。
	 * @param ignoreCase 是否忽略名称的大小写。
	 * @return 不重复名称，保留首次出现的大小写及顺序。
	 */
	static String[] names(List<String[]> pairs, boolean ignoreCase) {
		Set<String> seen = new LinkedHashSet<String>();
		List<String> result = new ArrayList<String>();
		for (String[] pair : pairs) {
			String key = ignoreCase ? pair[0].toLowerCase(Locale.ROOT) : pair[0];
			if (seen.add(key)) {
				result.add(pair[0]);
			}
		}
		return result.toArray(new String[result.size()]);
	}

	/**
	 * 逐项复制字段数组，避免不同请求共享可变记录。
	 *
	 * @param source      被复制的有序字段集合。
	 * @param destination 接收独立字段副本的集合。
	 */
	static void copy(List<String[]> source, List<String[]> destination) {
		for (String[] pair : source) {
			destination.add(pair.clone());
		}
	}

	/**
	 * 证书和上传来源统一支持绝对路径、应用资产及已授权的 content URI。
	 *
	 * @param context 用于访问应用文件及资产的上下文。
	 * @param path    文件、URI 或成员路径，遵循对应操作的约束。
	 * @return 独立的结果值或快照，语义与方法说明一致。
	 * @throws IOException 来源无法打开。
	 */
	static InputStream open(Context context, String path) throws IOException {
		if (path.startsWith("asset://")) {
			return context.getAssets().open(path.substring(8));
		}
		if (path.startsWith("content://")) {
			InputStream input = context.getContentResolver().openInputStream(Uri.parse(path));
			if (input == null) {
				throw new IOException("无法打开内容来源：" + path);
			}
			return input;
		}
		File file = new File(path);
		if (!file.isAbsolute()) {
			throw new IOException("文件路径必须为绝对路径或受支持的 URI。");
		}
		return new FileInputStream(file);
	}

	/**
	 * 有界读取只服务于内存响应和小型配置文件，下载另走流式提交路径。
	 *
	 * @param input 请求配置或已打开的输入流。
	 * @param limit 允许读入内存的最大字节数。
	 * @return 独立的结果值或快照，语义与方法说明一致。
	 * @throws IOException 输入流无法读取或内容超出上限。
	 */
	static byte[] read(InputStream input, long limit) throws IOException {
		return read(input, limit, null, -1);
	}

	/**
	 * 有界读取字节内容，按需检查取消并分发进度。
	 *
	 * @param input   请求配置或已打开的输入流。
	 * @param limit   允许读入内存的最大字节数。
	 * @param control 共享的取消和提交状态。
	 * @param total   总字节数，未知时为 -1。
	 * @return 有界读取字节内容，按需检查取消并分发进度。
	 * @throws IOException 当前读取、校验或传输无法完成。
	 */
	static byte[] read(InputStream input, long limit, TransferControl control, long total) throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		byte[] buffer = new byte[16384];
		long size = 0;
		long lastProgress = 0;
		int count;
		while ((count = input.read(buffer)) != -1) {
			if (control != null) {
				control.check();
			}
			size += count;
			if (size > limit) {
				throw new LimitExceeded("内容超过允许的内存字节数：" + limit);
			}
			output.write(buffer, 0, count);
			if (control != null) {
				long now = android.os.SystemClock.elapsedRealtime();
				if (now - lastProgress >= 100) {
					control.progress.report(false, size, total);
					lastProgress = now;
				}
			}
		}
		if (control != null) {
			control.check();
			control.progress.report(false, size, total);
		}
		return output.toByteArray();
	}

	/**
	 * 计算字节内容的小写 SHA-256 摘要。
	 *
	 * @param bytes 原始字节内容。
	 * @return 计算字节内容的小写 SHA-256 摘要。
	 */
	static String digest(byte[] bytes) {
		try {
			return hex(MessageDigest.getInstance("SHA-256").digest(bytes));
		} catch (NoSuchAlgorithmException impossible) {
			throw new IllegalStateException("系统未提供 SHA-256。", impossible);
		}
	}

	/**
	 * 将字节内容转换为小写十六进制文本。
	 *
	 * @param bytes 原始字节内容。
	 * @return 将字节内容转换为小写十六进制文本。
	 */
	static String hex(byte[] bytes) {
		char[] digits = "0123456789abcdef".toCharArray();
		char[] result = new char[bytes.length * 2];
		for (int index = 0; index < bytes.length; index++) {
			result[index * 2] = digits[(bytes[index] & 255) >>> 4];
			result[index * 2 + 1] = digits[bytes[index] & 15];
		}
		return new String(result);
	}

	/**
	 * 校验可选 SHA-256 摘要并规范为小写。
	 *
	 * @param value 待校验或转换的值。
	 * @return 校验可选 SHA-256 摘要并规范为小写。
	 */
	static String sha256Text(String value) {
		String result = text(value).toLowerCase(Locale.ROOT);
		if (!result.isEmpty() && !result.matches("[a-f0-9]{64}")) {
			throw new IllegalArgumentException("SHA-256 须为 64 位十六进制文本。");
		}
		return result;
	}

	/**
	 * OkHttp 的单次总超时使用 InterruptedIOException，与主动取消须明确区分。
	 *
	 * @param error 待分类或显示的原始异常。
	 * @return 是否满足当前条件。
	 */
	static boolean timedOut(Throwable error) {
		if (error instanceof java.net.SocketTimeoutException) {
			return true;
		}
		String message = text(error.getMessage()).toLowerCase(Locale.ROOT);
		return error instanceof java.io.InterruptedIOException
				&& (message.contains("timeout") || message.contains("timed out") || message.contains("超时"));
	}

	/**
	 * 明确的成员路径替代向 Simple 暴露 Gson/Java 泛型；缺失字段保留为 null。
	 *
	 * @param root JSON 根值。
	 * @param path 文件、URI 或成员路径，遵循对应操作的约束。
	 * @return 独立的结果值或快照，语义与方法说明一致。
	 */
	static JsonElement select(JsonElement root, String path) {
		if (path.isEmpty()) {
			return root;
		}
		JsonElement current = root;
		for (String part : path.split("\\.", -1)) {
			if (part.isEmpty()) {
				throw new IllegalArgumentException("JSON 路径包含空成员。");
			}
			int bracket = part.indexOf('[');
			String key = bracket < 0 ? part : part.substring(0, bracket);
			if (!key.isEmpty()) {
				if (current == null || !current.isJsonObject()) {
					return null;
				}
				current = current.getAsJsonObject().get(key);
			}
			while (bracket >= 0) {
				int close = part.indexOf(']', bracket);
				if (close < 0 || !part.substring(bracket + 1, close).matches("[0-9]+")) {
					throw new IllegalArgumentException("JSON 数组路径无效。");
				}
				int index = Integer.parseInt(part.substring(bracket + 1, close));
				if (current == null || !current.isJsonArray() || index >= current.getAsJsonArray().size()) {
					return null;
				}
				current = current.getAsJsonArray().get(index);
				bracket = close + 1;
				if (bracket == part.length()) {
					break;
				}
				if (part.charAt(bracket) != '[') {
					throw new IllegalArgumentException("JSON 数组路径尾部无效。");
				}
			}
		}
		return current;
	}

	/**
	 * 把 JSON 值转换为文本，字符串去除 JSON 引号，空值返回空文本。
	 *
	 * @param value 待校验或转换的值。
	 * @return 把 JSON 值转换为文本，字符串去除 JSON 引号，空值返回空文本。
	 */
	static String jsonText(JsonElement value) {
		if (value == null || value.isJsonNull()) {
			return "";
		}
		return value.isJsonPrimitive() && value.getAsJsonPrimitive().isString() ? value.getAsString()
				: value.toString();
	}

	/**
	 * 内容超过配置的内存读取上限。
	 *
	 * @author 树先生 xhwsd@qq.com
	 */
	static final class LimitExceeded extends IOException {
		private static final long serialVersionUID = 1L;

		/**
		 * 静态辅助方法集，不创建实例。
		 *
		 * @param message 可读的错误说明。
		 */
		LimitExceeded(String message) {
			super(message);
		}
	}
}
