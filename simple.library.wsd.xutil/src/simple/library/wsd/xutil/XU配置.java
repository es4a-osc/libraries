package simple.library.wsd.xutil;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.xuexiang.xutil.data.SPUtils;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * 按配置名读写应用私有 SharedPreferences，不需要存储权限。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class XU配置 {

	/**
	 * 函数集仅提供静态成员，不创建实例。
	 */
	private XU配置() {
	}

	/**
	 * 同步保存文本；返回提交是否成功。
	 *
	 * @param store 应用私有配置名称，不得为空或包含路径分隔符。
	 * @param key   键。
	 * @param value 值。
	 * @return 判断或操作的结果。
	 */
	@SimpleFunction
	public static boolean 写文本(String store, String key, String value) {
		return SPUtils.putString(XUtilSupport.preferences(store), key, value);
	}

	/**
	 * 读取文本；不存在时返回默认值。
	 *
	 * @param store    应用私有配置名称，不得为空或包含路径分隔符。
	 * @param key      键。
	 * @param fallback 默认值。
	 * @return 返回的文本。
	 */
	@SimpleFunction
	public static String 读文本(String store, String key, String fallback) {
		return SPUtils.getString(XUtilSupport.preferences(store), key, fallback);
	}

	/**
	 * 同步保存整数；返回提交是否成功。
	 *
	 * @param store 应用私有配置名称，不得为空或包含路径分隔符。
	 * @param key   键。
	 * @param value 值。
	 * @return 判断或操作的结果。
	 */
	@SimpleFunction
	public static boolean 写整数(String store, String key, int value) {
		return SPUtils.putInt(XUtilSupport.preferences(store), key, value);
	}

	/**
	 * 读取整数；不存在时返回默认值。
	 *
	 * @param store    应用私有配置名称，不得为空或包含路径分隔符。
	 * @param key      键。
	 * @param fallback 默认值。
	 * @return 返回的整数值。
	 */
	@SimpleFunction
	public static int 读整数(String store, String key, int fallback) {
		return SPUtils.getInt(XUtilSupport.preferences(store), key, fallback);
	}

	/**
	 * 同步保存长整数；返回提交是否成功。
	 *
	 * @param store 应用私有配置名称，不得为空或包含路径分隔符。
	 * @param key   键。
	 * @param value 值。
	 * @return 判断或操作的结果。
	 */
	@SimpleFunction
	public static boolean 写长整数(String store, String key, long value) {
		return SPUtils.putLong(XUtilSupport.preferences(store), key, value);
	}

	/**
	 * 读取长整数；不存在时返回默认值。
	 *
	 * @param store    应用私有配置名称，不得为空或包含路径分隔符。
	 * @param key      键。
	 * @param fallback 默认值。
	 * @return 返回的长整数值。
	 */
	@SimpleFunction
	public static long 读长整数(String store, String key, long fallback) {
		return SPUtils.getLong(XUtilSupport.preferences(store), key, fallback);
	}

	/**
	 * 同步保存逻辑；返回提交是否成功。
	 *
	 * @param store 应用私有配置名称，不得为空或包含路径分隔符。
	 * @param key   键。
	 * @param value 值。
	 * @return 判断或操作的结果。
	 */
	@SimpleFunction
	public static boolean 写逻辑(String store, String key, boolean value) {
		return SPUtils.putBoolean(XUtilSupport.preferences(store), key, value);
	}

	/**
	 * 读取逻辑；不存在时返回默认值。
	 *
	 * @param store    应用私有配置名称，不得为空或包含路径分隔符。
	 * @param key      键。
	 * @param fallback 默认值。
	 * @return 判断或操作的结果。
	 */
	@SimpleFunction
	public static boolean 读逻辑(String store, String key, boolean fallback) {
		return SPUtils.getBoolean(XUtilSupport.preferences(store), key, fallback);
	}

	/**
	 * 同步保存单精度；返回提交是否成功。
	 *
	 * @param store 应用私有配置名称，不得为空或包含路径分隔符。
	 * @param key   键。
	 * @param value 值。
	 * @return 判断或操作的结果。
	 */
	@SimpleFunction
	public static boolean 写单精度(String store, String key, float value) {
		return SPUtils.putFloat(XUtilSupport.preferences(store), key, value);
	}

	/**
	 * 读取单精度；不存在时返回默认值。
	 *
	 * @param store    应用私有配置名称，不得为空或包含路径分隔符。
	 * @param key      键。
	 * @param fallback 默认值。
	 * @return 返回的单精度数值。
	 */
	@SimpleFunction
	public static float 读单精度(String store, String key, float fallback) {
		return SPUtils.getFloat(XUtilSupport.preferences(store), key, fallback);
	}

	/**
	 * 检查键是否存在。
	 *
	 * @param store 应用私有配置名称，不得为空或包含路径分隔符。
	 * @param key   键。
	 * @return 判断或操作的结果。
	 */
	@SimpleFunction
	public static boolean 是否存在(String store, String key) {
		return SPUtils.contains(XUtilSupport.preferences(store), key);
	}

	/**
	 * 移除键并返回提交是否成功。
	 *
	 * @param store 应用私有配置名称，不得为空或包含路径分隔符。
	 * @param key   键。
	 * @return 判断或操作的结果。
	 */
	@SimpleFunction
	public static boolean 删除(String store, String key) {
		return SPUtils.remove(XUtilSupport.preferences(store), key);
	}

	/**
	 * 清空指定配置并返回提交是否成功。
	 *
	 * @param store 应用私有配置名称，不得为空或包含路径分隔符。
	 * @return 判断或操作的结果。
	 */
	@SimpleFunction
	public static boolean 清空(String store) {
		return SPUtils.clear(XUtilSupport.preferences(store));
	}

	/**
	 * 返回配置键名的稳定排序快照。
	 *
	 * @param store 配置名。
	 * @return 返回配置键名的稳定排序快照。
	 */
	@SimpleFunction
	public static String[] 取键名(String store) {
		String[] keys = XUtilSupport.preferences(store).getAll().keySet().toArray(new String[0]);
		Arrays.sort(keys);
		return keys;
	}

	/**
	 * 保存去重后的文本集合；同步提交并返回实际结果。
	 *
	 * @param store  配置名。
	 * @param key    键名。
	 * @param values 文本数组。
	 * @return 保存去重后的文本集合；同步提交并返回实际结果。
	 */
	@SimpleFunction
	public static boolean 写文本集(String store, String key, String[] values) {
		Set<String> copy = new HashSet<String>(Arrays.asList(values));
		if (copy.contains(null)) {
			throw new IllegalArgumentException("文本集成员不能为空引用");
		}
		return XUtilSupport.preferences(store).edit().putStringSet(key, copy).commit();
	}

	/**
	 * 读取文本集合的排序快照；键不存在返回空数组。
	 *
	 * @param store 配置名。
	 * @param key   键名。
	 * @return 读取文本集合的排序快照；键不存在返回空数组。
	 */
	@SimpleFunction
	public static String[] 读文本集(String store, String key) {
		Set<String> value;
		try {
			value = XUtilSupport.preferences(store).getStringSet(key, java.util.Collections.emptySet());
		} catch (ClassCastException mismatch) {
			return new String[0];
		}
		String[] result = value.toArray(new String[0]);
		Arrays.sort(result);
		return result;
	}
}
