package simple.library.wsd.xhttp2;

import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.SimpleProperty;

/**
 * 一组请求的只读完成结果，按输入顺序保存每个请求的最终响应；缓存双发时取该请求最后一份响应。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class XHttp批结果 {

	String id = "";
	XHttp响应[] replies = new XHttp响应[0];
	boolean cancelled;

	/**
	 * 取得批次标识。
	 *
	 * @return 批次标识。
	 */
	@SimpleProperty
	public synchronized String 标识() {
		return id;
	}

	/**
	 * 取得按输入顺序保存的响应快照，取消或提前停止的项目为取消响应。
	 *
	 * @return 按输入顺序保存的响应快照，取消或提前停止的项目为取消响应。
	 */
	@SimpleProperty
	public synchronized XHttp响应[] 响应列表() {
		return replies.clone();
	}

	/**
	 * 取得批次是否被主动取消。
	 *
	 * @return 批次是否被主动取消。
	 */
	@SimpleProperty
	public synchronized boolean 已取消() {
		return cancelled;
	}

	/**
	 * 取得成功的请求数量。
	 *
	 * @return 成功的请求数量。
	 */
	@SimpleProperty
	public synchronized int 成功数量() {
		int count = 0;
		for (XHttp响应 reply : replies) {
			if (reply != null && reply.成功()) {
				count++;
			}
		}
		return count;
	}

	/**
	 * 取得未成功的请求数量，包含取消或提前停止。
	 *
	 * @return 未成功的请求数量，包含取消或提前停止。
	 */
	@SimpleProperty
	public synchronized int 失败数量() {
		return replies.length - 成功数量();
	}

}
