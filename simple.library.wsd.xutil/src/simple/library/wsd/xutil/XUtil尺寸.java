package simple.library.wsd.xutil;

import com.xuexiang.xutil.display.DensityUtils;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * 设备显示密度、屏幕尺寸及 DIP、SP、像素转换。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class XUtil尺寸 {

	/**
	 * 函数集仅提供静态成员，不创建实例。
	 */
	private XUtil尺寸() {
	}

	/**
	 * 按当前显示配置转换单位。
	 *
	 * @param value 数值。
	 * @return 返回的整数值。
	 */
	@SimpleFunction
	public static int DP到像素(float value) {
		return DensityUtils.dip2px(XUtilSupport.context(), value);
	}

	/**
	 * 按当前显示配置转换单位。
	 *
	 * @param value 数值。
	 * @return 返回的整数值。
	 */
	@SimpleFunction
	public static int 像素到DP(float value) {
		return DensityUtils.px2dip(XUtilSupport.context(), value);
	}

	/**
	 * 按当前显示配置转换单位。
	 *
	 * @param value 数值。
	 * @return 返回的整数值。
	 */
	@SimpleFunction
	public static int SP到像素(float value) {
		return DensityUtils.sp2px(XUtilSupport.context(), value);
	}

	/**
	 * 按当前显示配置转换单位。
	 *
	 * @param value 数值。
	 * @return 返回的整数值。
	 */
	@SimpleFunction
	public static int 像素到SP(float value) {
		return DensityUtils.px2sp(XUtilSupport.context(), value);
	}

	/**
	 * 取得当前显示配置的屏幕宽度；宽高单位为像素。
	 *
	 * @return 返回的整数值。
	 */
	@SimpleFunction
	public static int 取屏幕宽度() {
		XUtilSupport.context();
		return DensityUtils.getScreenWidth();
	}

	/**
	 * 取得当前显示配置的屏幕高度；宽高单位为像素。
	 *
	 * @return 返回的整数值。
	 */
	@SimpleFunction
	public static int 取屏幕高度() {
		XUtilSupport.context();
		return DensityUtils.getScreenHeight();
	}

	/**
	 * 取得当前显示配置的密度；宽高单位为像素。
	 *
	 * @return 返回的单精度数值。
	 */
	@SimpleFunction
	public static float 取密度() {
		XUtilSupport.context();
		return DensityUtils.getScreenDensity();
	}
}
