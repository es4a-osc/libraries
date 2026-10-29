package simple.library.wsd.xui;

import com.xuexiang.xui.utils.DensityUtils;

import simple.runtime.android.MainActivity;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * XUI 尺寸转换与显示区域查询。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class XUI密度工具 {

	/**
	 * 静态工具不创建实例。
	 */
	private XUI密度工具() {
	}

	/**
	 * 把 dp 转换为物理像素。
	 *
	 * @param value 待转换的长度。
	 * @return 四舍五入后的整数长度。
	 */
	@SimpleFunction
	public static int DP到像素(float value) {
		return DensityUtils.dp2px(MainActivity.getContext(), value);
	}

	/**
	 * 把物理像素转换为 dp。
	 *
	 * @param value 待转换的长度。
	 * @return 四舍五入后的整数长度。
	 */
	@SimpleFunction
	public static int 像素到DP(float value) {
		return DensityUtils.px2dp(MainActivity.getContext(), value);
	}

	/**
	 * 把 sp 转换为物理像素。
	 *
	 * @param value 待转换的长度。
	 * @return 四舍五入后的整数长度。
	 */
	@SimpleFunction
	public static int SP到像素(float value) {
		return DensityUtils.sp2px(MainActivity.getContext(), value);
	}

	/**
	 * 把物理像素转换为 sp。
	 *
	 * @param value 待转换的长度。
	 * @return 四舍五入后的整数长度。
	 */
	@SimpleFunction
	public static int 像素到SP(float value) {
		return DensityUtils.px2sp(MainActivity.getContext(), value);
	}

	/**
	 * 取得屏幕宽度，单位为物理像素。
	 *
	 * @param real 是否包含系统装饰占用的显示区域。
	 * @return 屏幕宽度，单位为物理像素。
	 */
	@SimpleFunction
	public static int 取屏幕宽度(boolean real) {
		return DensityUtils.getDisplayWidth(MainActivity.getContext(), real);
	}

	/**
	 * 取得屏幕高度，单位为物理像素。
	 *
	 * @param real 是否包含系统装饰占用的显示区域。
	 * @return 屏幕高度，单位为物理像素。
	 */
	@SimpleFunction
	public static int 取屏幕高度(boolean real) {
		return DensityUtils.getDisplayHeight(MainActivity.getContext(), real);
	}

	/**
	 * 取得每 dp 对应的像素倍率。
	 *
	 * @return 屏幕密度。
	 */
	@SimpleFunction
	public static float 取密度() {
		return DensityUtils.getDisplayMetrics(MainActivity.getContext()).density;
	}

	/**
	 * 取得受系统字体缩放影响的 sp 像素倍率。
	 *
	 * @return 字体密度。
	 */
	@SimpleFunction
	public static float 取字体密度() {
		return DensityUtils.getDisplayMetrics(MainActivity.getContext()).scaledDensity;
	}

	/**
	 * 取得逻辑屏幕 DPI。
	 *
	 * @return 逻辑 DPI。
	 */
	@SimpleFunction
	public static int 取屏幕DPI() {
		return DensityUtils.getScreenDpi(MainActivity.getContext());
	}

	/**
	 * 取得系统导航栏高度。
	 *
	 * @return 高度，单位为物理像素。
	 */
	@SimpleFunction
	public static int 取导航栏高度() {
		return DensityUtils.getNavigationBarHeight(MainActivity.getContext());
	}

	/**
	 * 判断设备是否存在系统导航栏。
	 *
	 * @return 是否存在。
	 */
	@SimpleFunction
	public static boolean 是否存在导航栏() {
		return DensityUtils.isNavigationBarExist(MainActivity.getContext());
	}
}
