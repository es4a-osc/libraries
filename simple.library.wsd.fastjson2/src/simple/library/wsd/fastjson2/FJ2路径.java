package simple.library.wsd.fastjson2;

import com.alibaba.fastjson2.JSONPath;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * 官方 Android 版基础 JSONPath 编译对象；支持根、属性和非负下标，不支持过滤、通配符、切片、递归和聚合。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class FJ2路径 {

	/**
	 * 已编译的基础路径表达式。
	 */
	private final JSONPath path;

	/**
	 * 保存编译后的路径。
	 *
	 * @param path 已验证的路径。
	 */
	private FJ2路径(JSONPath path) {
		this.path = path;
	}

	/**
	 * 编译并验证基础路径；不支持的语法报错。
	 *
	 * @param path 以 $ 开头的路径。
	 * @return 可重复用于查询的基础路径对象。
	 * @throws IllegalArgumentException 路径为空、语法无效或使用了不支持的路径能力。
	 */
	@SimpleFunction
	public static FJ2路径 编译(String path) {
		try {
			FJ2支持.text(path);
			if (!path.startsWith("$") || path.trim().length() == 0) {
				throw new IllegalArgumentException("路径必须以 $ 开头。");
			}
			return new FJ2路径(JSONPath.of(path));
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 返回原路径表达式。
	 *
	 * @return 创建路径时使用的表达式。
	 */
	@SimpleFunction
	public String 取表达式() {
		try {
			return path.toString();
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 返回匹配值的独立副本；未找到与匹配 null 均为空值。
	 *
	 * @param root 文档根，空引用表示 JSON null。
	 * @return 查询结果的独立副本，未匹配与匹配 null 均为空值。
	 * @throws IllegalArgumentException 根值不适用于该路径或结果不能转换为本库值。
	 */
	@SimpleFunction
	public FJ2值 查询(FJ2值 root) {
		try {
			return new FJ2值(path.eval(FJ2支持.raw(root)));
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}
}
