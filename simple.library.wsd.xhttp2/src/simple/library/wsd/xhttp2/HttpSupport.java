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

	private HttpSupport() {
	}

	/**
	 * 可选文本采用空文本，必要文本在对应操作边界单独校验。
	 */
	static String text(String value) {
		return value == null ? "" : value;
	}

	static String nonempty(String value) {
		if (value == null || value.isEmpty()) {
			throw new IllegalArgumentException("文本不能为空。");
		}
		return value;
	}

	static byte[] bytes(byte[] value) {
		if (value == null) {
			throw new IllegalArgumentException("字节数组不能为空。");
		}
		return value.clone();
	}

	static void minimum(long value, long minimum) {
		if (value < minimum) {
			throw new IllegalArgumentException("数值不能小于 " + minimum + "。");
		}
	}

	static void maximum(long value, long maximum) {
		if (value > maximum) {
			throw new IllegalArgumentException("数值不能大于 " + maximum + "。");
		}
	}

	static void range(int value, int minimum, int maximum) {
		if (value < minimum || value > maximum) {
			throw new IllegalArgumentException("数值须处于 " + minimum + " 至 " + maximum + "。");
		}
	}

	static String method(String value) {
		String result = nonempty(value).toUpperCase(Locale.ROOT);
		if (!result.matches("GET|POST|PUT|PATCH|DELETE|HEAD|OPTIONS")) {
			throw new IllegalArgumentException("不支持的 HTTP 方法：" + value);
		}
		return result;
	}

	static MediaType media(String value) {
		MediaType result = MediaType.parse(nonempty(value));
		if (result == null) {
			throw new IllegalArgumentException("媒体类型无效。");
		}
		return result;
	}

	static String decode(byte[] bytes, String type) {
		MediaType media = type.isEmpty() ? null : MediaType.parse(type);
		return new String(bytes, media == null ? StandardCharsets.UTF_8 : media.charset(StandardCharsets.UTF_8));
	}

	static void add(List<String[]> target, String name, String value, boolean header) {
		nonempty(name);
		if (header) {
			// 复用实际传输层的字段校验，拒绝换行注入及无效名称。
			new Headers.Builder().add(name, text(value));
		}
		target.add(new String[] {name, text(value)});
	}

	/**
	 * 替换先校验新值，再删除旧值，非法输入不得破坏原有请求配置。
	 */
	static void setHeader(List<String[]> target, String name, String value) {
		new Headers.Builder().add(nonempty(name), text(value));
		remove(target, name, true);
		target.add(new String[] {name, text(value)});
	}

	static void remove(List<String[]> target, String name, boolean ignoreCase) {
		for (int index = target.size() - 1; index >= 0; index--) {
			String field = target.get(index)[0];
			if (ignoreCase ? field.equalsIgnoreCase(name) : field.equals(name)) {
				target.remove(index);
			}
		}
	}

	static String first(List<String[]> pairs, String name) {
		for (String[] pair : pairs) {
			if (pair[0].equalsIgnoreCase(name)) {
				return pair[1];
			}
		}
		return "";
	}

	static void copy(List<String[]> source, List<String[]> destination) {
		for (String[] pair : source) {
			destination.add(pair.clone());
		}
	}

	/**
	 * 证书和上传来源统一支持绝对路径、应用资产及已授权的 content URI。
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
	 */
	static byte[] read(InputStream input, long limit) throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		byte[] buffer = new byte[16384];
		long size = 0;
		int count;
		while ((count = input.read(buffer)) != -1) {
			size += count;
			if (size > limit) {
				throw new LimitExceeded("内容超过允许的内存字节数：" + limit);
			}
			output.write(buffer, 0, count);
		}
		return output.toByteArray();
	}

	static String digest(byte[] bytes) {
		try {
			return hex(MessageDigest.getInstance("SHA-256").digest(bytes));
		} catch (NoSuchAlgorithmException impossible) {
			throw new IllegalStateException("系统未提供 SHA-256。", impossible);
		}
	}

	static String hex(byte[] bytes) {
		char[] digits = "0123456789abcdef".toCharArray();
		char[] result = new char[bytes.length * 2];
		for (int index = 0; index < bytes.length; index++) {
			result[index * 2] = digits[(bytes[index] & 255) >>> 4];
			result[index * 2 + 1] = digits[bytes[index] & 15];
		}
		return new String(result);
	}

	static String sha256Text(String value) {
		String result = text(value).toLowerCase(Locale.ROOT);
		if (!result.isEmpty() && !result.matches("[a-f0-9]{64}")) {
			throw new IllegalArgumentException("SHA-256 须为 64 位十六进制文本。");
		}
		return result;
	}

	/**
	 * OkHttp 的单次总超时使用 InterruptedIOException，与主动取消须明确区分。
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

	static String jsonText(JsonElement value) {
		if (value == null || value.isJsonNull()) {
			return "";
		}
		return value.isJsonPrimitive() && value.getAsJsonPrimitive().isString() ? value.getAsString() : value.toString();
	}

	static final class LimitExceeded extends IOException {
		private static final long serialVersionUID = 1L;

		LimitExceeded(String message) {
			super(message);
		}
	}
}
