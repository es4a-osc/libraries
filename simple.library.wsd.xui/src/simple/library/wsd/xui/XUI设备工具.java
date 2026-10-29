package simple.library.wsd.xui;

import android.os.Build;

import com.xuexiang.xui.utils.DeviceUtils;

import simple.runtime.android.MainActivity;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * 查询 XUI 使用的设备分类及 Android 公开设备信息。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class XUI设备工具 {

	/**
	 * 静态工具不创建实例。
	 */
	private XUI设备工具() {
	}

	/**
	 * 按设备布局配置判断是否为平板。
	 *
	 * @return 是否为平板。
	 */
	@SimpleFunction
	public static boolean 是否平板() {
		return DeviceUtils.isTablet(MainActivity.getContext());
	}

	/**
	 * 取得系统报告的设备制造商。
	 *
	 * @return 制造商名称。
	 */
	@SimpleFunction
	public static String 取制造商() {
		return Build.MANUFACTURER;
	}

	/**
	 * 取得系统报告的设备品牌。
	 *
	 * @return 品牌名称。
	 */
	@SimpleFunction
	public static String 取品牌() {
		return Build.BRAND;
	}

	/**
	 * 取得系统报告的设备型号。
	 *
	 * @return 设备型号。
	 */
	@SimpleFunction
	public static String 取型号() {
		return Build.MODEL;
	}

	/**
	 * 取得当前设备实际 Android API 等级。
	 *
	 * @return 设备 API 等级。
	 */
	@SimpleFunction
	public static int 取系统API() {
		return Build.VERSION.SDK_INT;
	}
}
