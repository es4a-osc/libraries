package simple.library.wsd.xutil;

import com.xuexiang.xutil.resource.ResUtils;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * 读取当前应用资源和资产文本。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class XUtil资源 {

	/**
	 * 函数集仅提供静态成员，不创建实例。
	 */
	private XUtil资源() {
	}

	/**
	 * 按名称与资源类型查询当前应用资源标识；不存在返回零。
	 *
	 * @param name 名称。
	 * @param type 类型。
	 * @return 返回的整数值。
	 */
	@SimpleFunction
	public static int 取标识(String name, String type) {
		return XUtilSupport.context().getResources().getIdentifier(name, type, XUtilSupport.context().getPackageName());
	}

	/**
	 * 读取有效资源标识对应的文本，标识无效时抛出异常。
	 *
	 * @param id 标识。
	 * @return 返回的文本。
	 */
	@SimpleFunction
	public static String 取文本(int id) {
		XUtilSupport.context();
		return ResUtils.getString(id);
	}

	/**
	 * 读取有效资源标识对应的文本数组，标识无效时抛出异常。
	 *
	 * @param id 标识。
	 * @return 返回的文本数组。
	 */
	@SimpleFunction
	public static String[] 取文本数组(int id) {
		XUtilSupport.context();
		return ResUtils.getStringArray(id);
	}

	/**
	 * 读取有效资源标识对应的整数数组，标识无效时抛出异常。
	 *
	 * @param id 标识。
	 * @return 返回的整数数组。
	 */
	@SimpleFunction
	public static int[] 取整数数组(int id) {
		XUtilSupport.context();
		return ResUtils.getIntArray(id);
	}

	/**
	 * 读取有效资源标识对应的尺寸像素，标识无效时抛出异常。
	 *
	 * @param id 标识。
	 * @return 返回的整数值。
	 */
	@SimpleFunction
	public static int 取尺寸像素(int id) {
		XUtilSupport.context();
		return ResUtils.getDimensionPixelSize(id);
	}

	/**
	 * 以 UTF-8 读取资产文本并保留原有换行；缺失时抛出异常。
	 *
	 * @param path 路径。
	 * @return 返回的文本。
	 */
	@SimpleFunction
	public static String 读资产文本(String path) {
		return XUtilSupport.readAsset(path);
	}
}
