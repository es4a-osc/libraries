package simple.library.wsd.xutil;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import com.xuexiang.xutil.data.ACache;
import com.xuexiang.xutil.security.EncryptUtils;

/**
 * 适配 ACache 的空值编码及键名，避免空字节集被上游直接忽略。
 *
 * @author 树先生 xhwsd@qq.com
 */
final class CacheStore {

	/**
	 * 缓存适配只由函数集调用。
	 */
	private CacheStore() {
	}

	/**
	 * 将原键转换为稳定摘要，避免上游直接使用常见字符串哈希碰撞键。
	 *
	 * @param key 原键。
	 * @return 内部键。
	 */
	static String key(String key) {
		if (key == null || key.isEmpty()) {
			throw new IllegalArgumentException("缓存键不能为空");
		}
		return EncryptUtils.encryptSHA256ToString(key.getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * 存入带类型前缀的缓存并检查实际写入结果，不把上游吞掉的 I/O 失败当作成功。
	 *
	 * @param name 缓存名。
	 * @param key 原键。
	 * @param value 数据。
	 * @param seconds 有效期。
	 * @param type 数据类型标记。
	 */
	static synchronized void put(String name, String key, byte[] value, int seconds, byte type) {
		if (value == null) {
			throw new IllegalArgumentException("缓存值不能为 null");
		}
		ACache cache = XUtilSupport.cache(name);
		String id = key(key);
		byte[] encoded = new byte[value.length + 1];
		encoded[0] = type;
		System.arraycopy(value, 0, encoded, 1, value.length);
		cache.put(id, encoded, XUtilSupport.cacheTtl(seconds));
		if (!Arrays.equals(cache.getBytes(id), encoded)) {
			throw new IllegalStateException("缓存写入失败或立即被淘汰");
		}
	}

	/**
	 * 按类型读取缓存，类型不同或过期均返回默认值。
	 *
	 * @param name 缓存名。
	 * @param key 原键。
	 * @param fallback 缺省值。
	 * @param type 数据类型标记。
	 * @return 解码后的数据。
	 */
	static synchronized byte[] get(String name, String key, byte[] fallback, byte type) {
		byte[] data = XUtilSupport.cache(name).getBytes(key(key));
		if (data == null || data.length == 0 || data[0] != type) {
			return fallback;
		}
		return Arrays.copyOfRange(data, 1, data.length);
	}
}
