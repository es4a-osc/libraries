package simple.library.wsd.xutil;

import com.xuexiang.xutil.system.DeviceUtils;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * Android 公开设备信息与应用范围标识，不读取受限 IMEI、IMSI 或硬件 MAC。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class XUtil设备 {

	/**
	 * 函数集仅提供静态成员，不创建实例。
	 */
	private XUtil设备() {
	}

	/**
	 * 取得品牌；AndroidID 按系统规则限定于签名、用户和设备，不是跨应用永久标识。
	 *
	 * @return 返回的文本。
	 */
	@SimpleFunction
	public static String 取品牌() {
		XUtilSupport.context();
		return DeviceUtils.getDeviceBrand();
	}

	/**
	 * 取得制造商；AndroidID 按系统规则限定于签名、用户和设备，不是跨应用永久标识。
	 *
	 * @return 返回的文本。
	 */
	@SimpleFunction
	public static String 取制造商() {
		XUtilSupport.context();
		return DeviceUtils.getManufacturer();
	}

	/**
	 * 取得型号；AndroidID 按系统规则限定于签名、用户和设备，不是跨应用永久标识。
	 *
	 * @return 返回的文本。
	 */
	@SimpleFunction
	public static String 取型号() {
		XUtilSupport.context();
		return DeviceUtils.getDeviceModel();
	}

	/**
	 * 取得硬件；AndroidID 按系统规则限定于签名、用户和设备，不是跨应用永久标识。
	 *
	 * @return 返回的文本。
	 */
	@SimpleFunction
	public static String 取硬件() {
		XUtilSupport.context();
		return DeviceUtils.getHardware();
	}

	/**
	 * 取得系统版本；AndroidID 按系统规则限定于签名、用户和设备，不是跨应用永久标识。
	 *
	 * @return 返回的文本。
	 */
	@SimpleFunction
	public static String 取系统版本() {
		XUtilSupport.context();
		return DeviceUtils.getSDKVersionName();
	}

	/**
	 * 取得系统API；AndroidID 按系统规则限定于签名、用户和设备，不是跨应用永久标识。
	 *
	 * @return 返回的整数值。
	 */
	@SimpleFunction
	public static int 取系统API() {
		XUtilSupport.context();
		return DeviceUtils.getSDKVersionCode();
	}

	/**
	 * 取得AndroidID；AndroidID 按系统规则限定于签名、用户和设备，不是跨应用永久标识。
	 *
	 * @return 返回的文本。
	 */
	@SimpleFunction
	public static String 取AndroidID() {
		XUtilSupport.context();
		return DeviceUtils.getAndroidID();
	}
}
