package simple.library.wsd.xhttp2;

import java.util.ArrayList;
import java.util.List;

import android.util.Base64;

/**
 * 缓存和原生响应式链之间传递的内容快照，不含请求标识、回调或本次请求计时。
 * 二进制采用 Base64 保存，避免 Gson 将每个字节展开为十进制数组而放大缓存和去重开销。
 *
 * @author 树先生 xhwsd@qq.com
 */
final class WireReply {

	int status;
	String reason = "";
	String url = "";
	String contentType = "";
	String encoded = "";
	String file = "";
	long length;
	boolean httpCache;
	List<String[]> headers = new ArrayList<String[]>();

	XHttp响应 response(String id, String key, long elapsed, boolean cached) {
		XHttp响应 result = new XHttp响应();
		result.id = id;
		result.address = url;
		result.status = status;
		result.reason = reason;
		result.mediaType = contentType;
		result.body = Base64.decode(encoded, Base64.NO_WRAP);
		result.length = length;
		result.file = file;
		result.elapsed = elapsed;
		result.cacheKey = key;
		result.cached = cached;
		HttpSupport.copy(headers, result.headers);
		if (status < 200 || status >= 300) {
			result.errorType = "HTTP";
			result.error = "HTTP " + status + " " + reason;
		}
		return result;
	}
}
