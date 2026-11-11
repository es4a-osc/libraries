package simple.library.wsd.xhttp2;

import simple.runtime.annotations.SimpleDataElement;
import simple.runtime.annotations.SimpleObject;

/**
 * 客户端默认配置与单次请求共用的缓存策略常量。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class XH2缓存策略 {

	/**
	 * 常量集无需创建实例。
	 */
	private XH2缓存策略() {
	}

	/**
	 * 不读写应用响应缓存。
	 */
	@SimpleDataElement
	public static final int 不缓存 = 0;

	/**
	 * 遵循 HTTP 缓存头；带凭据的请求不复用默认 HTTP 缓存。
	 */
	@SimpleDataElement
	public static final int HTTP = 1;

	/**
	 * 网络失败时读取有效缓存。
	 */
	@SimpleDataElement
	public static final int 网络优先 = 2;

	/**
	 * 有效缓存命中时不请求网络。
	 */
	@SimpleDataElement
	public static final int 缓存优先 = 3;

	/**
	 * 仅从网络获取，但保存成功的 HTTP 响应。
	 */
	@SimpleDataElement
	public static final int 仅网络 = 4;

	/**
	 * 只读取有效缓存，未命中作为缓存错误。
	 */
	@SimpleDataElement
	public static final int 仅缓存 = 5;

	/**
	 * 先返回缓存，再返回网络响应。
	 */
	@SimpleDataElement
	public static final int 缓存及网络 = 6;

	/**
	 * 先缓存再网络，完整响应内容相同则只返回一次。
	 */
	@SimpleDataElement
	public static final int 缓存及网络去重 = 7;

}
