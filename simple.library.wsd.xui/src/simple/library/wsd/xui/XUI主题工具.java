package simple.library.wsd.xui;

import com.xuexiang.xui.utils.ThemeUtils;

import simple.runtime.android.MainActivity;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * 配置后续 XUI 组件的主题与字体，并查询当前 Activity 主题。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class XUI主题工具 {

	/**
	 * 静态工具不创建实例。
	 */
	private XUI主题工具() {
	}

	/**
	 * 读取当前主题主色。
	 *
	 * @return 主色。
	 */
	@SimpleFunction
	public static int 取主色() {
		return ThemeUtils.getMainThemeColor(MainActivity.getContext());
	}

	/**
	 * 查询当前配置是否为夜间模式。
	 *
	 * @return 是否为夜间模式。
	 */
	@SimpleFunction
	public static boolean 是否夜间模式() {
		return ThemeUtils.isNightMode(MainActivity.getContext());
	}

	/**
	 * 读取主题颜色属性。
	 *
	 * @param attribute Android 属性资源 ID。
	 * @param fallback 缺失时返回的颜色。
	 * @return 解析后的颜色。
	 */
	@SimpleFunction
	public static int 取颜色属性(int attribute, int fallback) {
		return ThemeUtils.resolveColor(MainActivity.getContext(), attribute, fallback);
	}

	/**
	 * 读取主题整数属性。
	 *
	 * @param attribute 属性资源 ID。
	 * @param fallback 缺失时返回的整数。
	 * @return 解析后的整数。
	 */
	@SimpleFunction
	public static int 取整数属性(int attribute, int fallback) {
		return ThemeUtils.resolveInt(MainActivity.getContext(), attribute, fallback);
	}

	/**
	 * 读取主题小数属性。
	 *
	 * @param attribute 属性资源 ID。
	 * @param fallback 缺失时返回的小数。
	 * @return 解析后的小数。
	 */
	@SimpleFunction
	public static float 取小数属性(int attribute, float fallback) {
		return ThemeUtils.resolveFloat(MainActivity.getContext(), attribute, fallback);
	}

	/**
	 * 读取主题文本属性。
	 *
	 * @param attribute 属性资源 ID。
	 * @param fallback 缺失时返回的文本。
	 * @return 解析后的文本。
	 */
	@SimpleFunction
	public static String 取文本属性(int attribute, String fallback) {
		return ThemeUtils.resolveString(MainActivity.getContext(), attribute, fallback);
	}

	/**
	 * 读取主题尺寸属性。
	 *
	 * @param attribute 属性资源 ID。
	 * @param fallback 缺失时返回的像素值。
	 * @return 物理像素尺寸。
	 */
	@SimpleFunction
	public static int 取尺寸属性(int attribute, int fallback) {
		return ThemeUtils.resolveDimension(MainActivity.getContext(), attribute, fallback);
	}

	/**
	 * 设置后续 XUI 组件及弹层的样式覆盖，不改变当前窗口或已有组件。
	 *
	 * @param style Android 样式资源，0 恢复默认。
	 */
	@SimpleFunction
	public static void 置组件主题(int style) {
		ThemeSupport.setOverlay(style);
	}

	/**
	 * 取得后续 XUI 组件使用的样式覆盖资源，0 表示默认。
	 *
	 * @return 后续 XUI 组件使用的样式覆盖资源，0 表示默认。
	 */
	@SimpleFunction
	public static int 取组件主题() {
		return ThemeSupport.getOverlay();
	}

	/**
	 * 设置后续 XUI 组件文字的资产字体，不改已有组件。
	 *
	 * @param asset 字体资产路径，空文本恢复默认。
	 */
	@SimpleFunction
	public static void 置默认字体(String asset) {
		ThemeSupport.setFont(asset);
	}

	/**
	 * 取得后续 XUI 组件的字体资产路径，空文本表示默认。
	 *
	 * @return 后续 XUI 组件的字体资产路径，空文本表示默认。
	 */
	@SimpleFunction
	public static String 取默认字体() {
		return ThemeSupport.getFont();
	}

	/**
	 * 恢复后续 XUI 组件的默认主题与字体。
	 */
	@SimpleFunction
	public static void 重置组件配置() {
		ThemeSupport.setOverlay(0);
		ThemeSupport.setFont("");
	}

	/**
	 * 按当前组件主题配置读取颜色属性。
	 *
	 * @param attribute Android 颜色属性资源。
	 * @param fallback 属性缺失时的颜色。
	 * @return 按当前组件主题配置读取颜色属性。
	 */
	@SimpleFunction
	public static int 取组件颜色属性(int attribute, int fallback) {
		return ThemeUtils.resolveColor(ThemeSupport.createContext(), attribute, fallback);
	}

}
