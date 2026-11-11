package simple.library.wsd.agentweb;

import java.net.IDN;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import android.webkit.MimeTypeMap;

/**
 * 网页输入的同源、请求头和文件类型校验，不保留页面或 Activity。
 *
 * @author 树先生 xhwsd@qq.com
 */
final class BrowserInput {

	/**
	 * 工具类不创建实例。
	 */
	private BrowserInput() {
	}

	/**
	 * 将网络网址归一为同源请求头的键，忽略路径、凭据及默认端口。
	 *
	 * @param value 完整 HTTP 或 HTTPS 网址。
	 * @return 小写协议和主机组成的源，保留非默认端口。
	 * @throws IllegalArgumentException 网址不完整、协议或端口无效。
	 */
	static String origin(String value) {
		try {
			URI uri = new URI(value == null ? "" : value);
			String scheme = uri.getScheme();
			if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
				throw new IllegalArgumentException("此操作仅接受完整的 HTTP 或 HTTPS 网址。");
			}
			URL url = uri.toURL();
			String host = url.getHost();
			if (host.isEmpty() || url.getPort() < -1 || url.getPort() == 0 || url.getPort() > 65535) {
				throw new IllegalArgumentException("网址主机或端口无效。");
			}
			host = (host.startsWith("[") ? host : IDN.toASCII(host)).toLowerCase(Locale.ROOT);
			scheme = scheme.toLowerCase(Locale.ROOT);
			int port = url.getPort();
			boolean defaultPort = port == -1 || ("https".equals(scheme) && port == 443)
					|| ("http".equals(scheme) && port == 80);
			return scheme + "://" + host + (defaultPort ? "" : ":" + port);
		} catch (URISyntaxException | MalformedURLException error) {
			throw new IllegalArgumentException("网址格式无效。", error);
		}
	}

	/**
	 * 校验请求头名称，不接受空名称或分隔控制字符。
	 *
	 * @param name HTTP 字段名称。
	 */
	static void headerName(String name) {
		if (name == null || !name.matches("[!#$%&'*+.^_`|~0-9A-Za-z-]+")) {
			throw new IllegalArgumentException("请求头名称必须为有效的 HTTP 字段名称。");
		}
	}

	/**
	 * 校验请求头值，禁止换行等控制字符。
	 *
	 * @param value 请求头值，允许空文本。
	 */
	static void headerValue(String value) {
		if (value == null) {
			throw new IllegalArgumentException("请求头值不能是空引用。");
		}
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			if ((c < 32 && c != '\t') || c == 127) {
				throw new IllegalArgumentException("请求头值不能包含换行等控制字符。");
			}
		}
	}

	/**
	 * 查找同名请求头的实际拼写，HTTP 字段名称忽略大小写。
	 *
	 * @param headers 已有请求头。
	 * @param name    已校验名称。
	 * @return 已有键名，没有时返回新名称。
	 */
	static String key(Map<String, String> headers, String name) {
		for (String key : headers.keySet()) {
			if (key.equalsIgnoreCase(name)) {
				return key;
			}
		}
		return name;
	}

	/**
	 * 在独立副本中合并本次加载的附加请求头，失败不改变已有配置。
	 *
	 * @param defaults 已配置的同源请求头。
	 * @param names    本次请求头名称数组。
	 * @param values   与名称一一对应的值数组。
	 * @return 合并后的独立请求头。
	 */
	static Map<String, String> merge(Map<String, String> defaults, String[] names, String[] values) {
		if (names == null || values == null || names.length != values.length) {
			throw new IllegalArgumentException("请求头名称和值数组必须非空并且数量相同。");
		}
		Map<String, String> result = new LinkedHashMap<String, String>(defaults);
		for (int i = 0; i < names.length; i++) {
			headerName(names[i]);
			headerValue(values[i]);
			result.put(key(result, names[i]), values[i]);
		}
		return result;
	}

	/**
	 * 将网页 accept 项拆分为系统选择器可接受的 MIME 类型，扩展名按系统映射解析。
	 *
	 * @param accepted WebView 提供的类型数组，允许逗号分隔、通配符和扩展名。
	 * @return 去重后的 MIME 类型；空数组表示不限制类型。
	 */
	static String[] mimeTypes(String[] accepted) {
		Set<String> result = new LinkedHashSet<String>();
		if (accepted != null) {
			for (String group : accepted) {
				if (group == null) {
					continue;
				}
				for (String value : group.split(",")) {
					String type = value.trim().toLowerCase(Locale.ROOT);
					if (type.startsWith(".") && type.length() > 1) {
						type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(type.substring(1));
					}
					if ("*/*".equals(type)) {
						return new String[0];
					}
					if (type != null && type.matches("[a-z0-9!#$&^_.+-]+/([a-z0-9!#$&^_.+-]+|\\*)")) {
						result.add(type);
					}
				}
			}
		}
		return result.toArray(new String[0]);
	}
}
