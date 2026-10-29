package simple.library.wsd.xhttp2;

import java.util.ArrayList;
import java.util.List;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.SimpleProperty;

/**
 * 单次响应的只读快照；非成功 HTTP 状态仍保留响应头和响应体，网络错误的状态码为零。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class XHttp响应 {

	String id = "";
	String address = "";
	int status;
	String reason = "";
	String mediaType = "";
	byte[] body = new byte[0];
	boolean cached;
	String file = "";
	long length;
	long elapsed;
	String cacheKey = "";
	String error = "";
	String errorType = "";
	XHttp解析结果 parsed;
	final List<String[]> headers = new ArrayList<String[]>();

	/**
	 * 取得请求标识。
	 *
	 * @return 请求标识。
	 */
	@SimpleProperty
	public synchronized String 标识() {
		return id;
	}

	/**
	 * 取得最终响应地址；网络错误时为请求地址。
	 *
	 * @return 最终响应地址；网络错误时为请求地址。
	 */
	@SimpleProperty
	public synchronized String 地址() {
		return address;
	}

	/**
	 * 取得HTTP 状态码；网络错误时为零。
	 *
	 * @return HTTP 状态码；网络错误时为零。
	 */
	@SimpleProperty
	public synchronized int 状态码() {
		return status;
	}

	/**
	 * 取得HTTP 状态说明。
	 *
	 * @return HTTP 状态说明。
	 */
	@SimpleProperty
	public synchronized String 原因() {
		return reason;
	}

	/**
	 * 取得响应 Content-Type。
	 *
	 * @return 响应 Content-Type。
	 */
	@SimpleProperty
	public synchronized String 媒体类型() {
		return mediaType;
	}

	/**
	 * 取得响应内容副本；文件下载不在内存保留内容。
	 *
	 * @return 响应内容副本；文件下载不在内存保留内容。
	 */
	@SimpleProperty
	public synchronized byte[] 字节() {
		return body.clone();
	}

	/**
	 * 取得按响应字符集解码的文本；没有字符集时采用 UTF-8。
	 *
	 * @return 按响应字符集解码的文本；没有字符集时采用 UTF-8。
	 */
	@SimpleProperty
	public synchronized String 文本() {
		return HttpSupport.decode(body, mediaType);
	}

	/**
	 * 取得响应是否来自缓存。
	 *
	 * @return 响应是否来自缓存。
	 */
	@SimpleProperty
	public synchronized boolean 来自缓存() {
		return cached;
	}

	/**
	 * 取得已成功提交的下载文件路径。
	 *
	 * @return 已成功提交的下载文件路径。
	 */
	@SimpleProperty
	public synchronized String 文件() {
		return file;
	}

	/**
	 * 取得响应或下载的实际字节数。
	 *
	 * @return 响应或下载的实际字节数。
	 */
	@SimpleProperty
	public synchronized long 长度() {
		return length;
	}

	/**
	 * 取得本次操作已经耗费的毫秒数。
	 *
	 * @return 本次操作已经耗费的毫秒数。
	 */
	@SimpleProperty
	public synchronized long 用时() {
		return elapsed;
	}

	/**
	 * 取得实际缓存键，可用于删除对应缓存。
	 *
	 * @return 实际缓存键，可用于删除对应缓存。
	 */
	@SimpleProperty
	public synchronized String 缓存键() {
		return cacheKey;
	}

	/**
	 * 取得网络、HTTP 或解析错误；成功时为空。
	 *
	 * @return 网络、HTTP 或解析错误；成功时为空。
	 */
	@SimpleProperty
	public synchronized String 错误() {
		return error;
	}

	/**
	 * 取得空文本或 参数、网络、超时、证书、HTTP、解析、业务、文件、取消、缓存、响应、处理。
	 *
	 * @return 空文本或 参数、网络、超时、证书、HTTP、解析、业务、文件、取消、缓存、响应、处理。
	 */
	@SimpleProperty
	public synchronized String 错误类型() {
		return errorType;
	}

	/**
	 * 取得HTTP 和业务均成功且没有本地处理错误。
	 *
	 * @return HTTP 和业务均成功且没有本地处理错误。
	 */
	@SimpleProperty
	public synchronized boolean 成功() {
		return error.isEmpty() && status >= 200 && status < 300 && (parsed == null || parsed.成功());
	}

	/**
	 * 取得标准或自定义解析产生的业务状态码。
	 *
	 * @return 标准或自定义解析产生的业务状态码。
	 */
	@SimpleProperty
	public synchronized int 业务码() {
		return parsed == null ? 0 : parsed.状态码();
	}

	/**
	 * 取得标准或自定义解析后的文本；原始方式返回完整响应文本。
	 *
	 * @return 标准或自定义解析后的文本；原始方式返回完整响应文本。
	 */
	@SimpleProperty
	public synchronized String 数据() {
		return parsed == null ? 文本() : parsed.数据();
	}

	/**
	 * 取得业务消息或错误描述。
	 *
	 * @return 业务消息或错误描述。
	 */
	@SimpleProperty
	public synchronized String 消息() {
		return parsed == null ? error : parsed.消息();
	}

	/**
	 * 取得首个同名响应头，名称不区分大小写。
	 *
	 * @param name 字段名称。
	 * @return 字段内容，不存在时为空文本。
	 */
	@SimpleFunction
	public synchronized String 取响应头(String name) {
		return HttpSupport.first(headers, name);
	}

	/**
	 * 取得全部同名响应头，保持服务端返回顺序。
	 *
	 * @param name 字段名称。
	 * @return 字段内容数组。
	 */
	@SimpleFunction
	public synchronized String[] 取全部响应头(String name) {
		List<String> result = new ArrayList<String>();
		for (String[] pair : headers) {
			if (pair[0].equalsIgnoreCase(name)) {
				result.add(pair[1]);
			}
		}
		return result.toArray(new String[result.size()]);
	}

	/**
	 * 取得响应头名称数组，重复名称保留。
	 *
	 * @return 响应头名称。
	 */
	@SimpleFunction
	public synchronized String[] 取响应头名称() {
		String[] result = new String[headers.size()];
		for (int index = 0; index < result.length; index++) {
			result[index] = headers.get(index)[0];
		}
		return result;
	}

}
