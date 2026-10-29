package simple.library.wsd.xui;

import com.xuexiang.xui.utils.ResUtils;

import simple.runtime.android.MainActivity;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * 读取应用及合并类库中的 Android 资源。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class XUI资源工具 {

	/**
	 * 静态工具不创建实例。
	 */
	private XUI资源工具() {
	}

	/**
	 * 按资源类型和名称查询应用资源。
	 *
	 * @param name 不带前缀的资源名称。
	 * @param type 资源类型，例如 string、color、dimen。
	 * @return 资源 ID；不存在时为零。
	 */
	@SimpleFunction
	public static int 取资源标识(String name, String type) {
		return MainActivity.getContext().getResources().getIdentifier(name, type, MainActivity.getContext().getPackageName());
	}

	/**
	 * 读取字符串资源。
	 *
	 * @param id 字符串资源 ID。
	 * @return 字符串资源。
	 */
	@SimpleFunction
	public static String 取文本(int id) {
		return ResUtils.getString(MainActivity.getContext(), id);
	}

	/**
	 * 读取字符串数组资源。
	 *
	 * @param id 数组资源 ID。
	 * @return 独立的文本数组。
	 */
	@SimpleFunction
	public static String[] 取文本数组(int id) {
		return ResUtils.getStringArray(MainActivity.getContext(), id).clone();
	}

	/**
	 * 读取颜色资源。
	 *
	 * @param id 颜色资源 ID。
	 * @return ARGB 颜色。
	 */
	@SimpleFunction
	public static int 取颜色(int id) {
		return ResUtils.getColor(MainActivity.getContext(), id);
	}

	/**
	 * 读取尺寸资源并换算为物理像素。
	 *
	 * @param id 尺寸资源 ID。
	 * @return 尺寸像素值。
	 */
	@SimpleFunction
	public static float 取尺寸(int id) {
		return ResUtils.getDimens(MainActivity.getContext(), id);
	}
}
