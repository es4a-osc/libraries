package simple.library.wsd.xutil;

import java.nio.charset.StandardCharsets;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * 应用私有磁盘缓存，支持文本、字节集和秒级有效期。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class XUtil缓存 {

	/**
	 * 函数集仅提供静态成员，不创建实例。
	 */
	private XUtil缓存() {
	}

	/**
	 * 写入文本缓存；有效期以秒计。
	 *
	 * @param cache 私有缓存名称，不得为空或包含路径分隔符。
	 * @param key 键。
	 * @param value 值。
	 * @param seconds 大于零为有效秒数，零表示不过期；负数拒绝。
	 */
	@SimpleFunction
	public static void 写文本(String cache, String key, String value, int seconds) {
		CacheStore.put(cache, key, value.getBytes(StandardCharsets.UTF_8), seconds, (byte) 1);
	}

	/**
	 * 读取缓存，未命中或过期返回默认值。
	 *
	 * @param cache 私有缓存名称，不得为空或包含路径分隔符。
	 * @param key 键。
	 * @param fallback 默认值。
	 * @return 返回的文本。
	 */
	@SimpleFunction
	public static String 读文本(String cache, String key, String fallback) {
		byte[] data = CacheStore.get(cache, key, null, (byte) 1);
		return data == null ? fallback : new String(data, StandardCharsets.UTF_8);
	}

	/**
	 * 写入字节集缓存；有效期以秒计。
	 *
	 * @param cache 私有缓存名称，不得为空或包含路径分隔符。
	 * @param key 键。
	 * @param value 值。
	 * @param seconds 大于零为有效秒数，零表示不过期；负数拒绝。
	 */
	@SimpleFunction
	public static void 写字节集(String cache, String key, byte[] value, int seconds) {
		CacheStore.put(cache, key, value, seconds, (byte) 2);
	}

	/**
	 * 读取缓存，未命中或过期返回默认值。
	 *
	 * @param cache 私有缓存名称，不得为空或包含路径分隔符。
	 * @param key 键。
	 * @param fallback 默认值。
	 * @return 返回的字节集。
	 */
	@SimpleFunction
	public static byte[] 读字节集(String cache, String key, byte[] fallback) {
		return CacheStore.get(cache, key, fallback, (byte) 2);
	}

	/**
	 * 操作指定缓存；删除或清空返回是否成功，容量单位为字节。
	 *
	 * @param cache 私有缓存名称，不得为空或包含路径分隔符。
	 * @param key 键。
	 * @return 判断或操作的结果。
	 */
	@SimpleFunction
	public static boolean 删除(String cache, String key) {
		return XUtilSupport.cache(cache).remove(CacheStore.key(key));
	}

	/**
	 * 操作指定缓存；删除或清空返回是否成功，容量单位为字节。
	 *
	 * @param cache 私有缓存名称，不得为空或包含路径分隔符。
	 * @return 判断或操作的结果。
	 */
	@SimpleFunction
	public static boolean 清空(String cache) {
		return XUtilSupport.cache(cache).clear();
	}

	/**
	 * 操作指定缓存；删除或清空返回是否成功，容量单位为字节。
	 *
	 * @param cache 私有缓存名称，不得为空或包含路径分隔符。
	 * @return 返回的长整数值。
	 */
	@SimpleFunction
	public static long 取字节数(String cache) {
		return XUtilSupport.cache(cache).getCacheSize();
	}

	/**
	 * 操作指定缓存；删除或清空返回是否成功，容量单位为字节。
	 *
	 * @param cache 私有缓存名称，不得为空或包含路径分隔符。
	 * @return 返回的整数值。
	 */
	@SimpleFunction
	public static int 取数量(String cache) {
		return XUtilSupport.cache(cache).getCacheCount();
	}
}
