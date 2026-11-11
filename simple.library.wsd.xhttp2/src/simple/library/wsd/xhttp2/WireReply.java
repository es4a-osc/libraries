package simple.library.wsd.xhttp2;

import java.util.ArrayList;
import java.util.List;

import android.util.Base64;

/**
 * 缓存和原生响应式链之间传递的内容快照，不含请求标识、回调或本次请求计时。 二进制采用 Base64 保存，避免 Gson 将每个字节展开为十进制数组而放大缓存和去重开销。
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
	String sha256 = "";
	long length;
	boolean httpCache;
	List<String[]> headers = new ArrayList<String[]>();

	/**
	 * 把传输快照转换为本次请求的响应，附加标识、计时和缓存来源。
	 *
	 * @param id      本次请求或批次的实际唯一标识。
	 * @param key     来自响应的实际缓存键或内部会话键。
	 * @param elapsed 本次请求已经耗费的毫秒数。
	 * @param cached  是否来自应用或 HTTP 缓存。
	 * @return 把传输快照转换为本次请求的响应，附加标识、计时和缓存来源。
	 */
	XH2响应 response(String id, String key, long elapsed, boolean cached) {
		XH2响应 result = new XH2响应();
		result.id = id;
		result.address = url;
		result.status = status;
		result.reason = reason;
		result.mediaType = contentType;
		result.body = Base64.decode(encoded, Base64.NO_WRAP);
		result.length = length;
		result.file = file;
		result.sha256 = sha256;
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
