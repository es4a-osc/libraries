package simple.library.wsd.xutil;

import android.content.ActivityNotFoundException;

import com.xuexiang.xutil.system.PhoneUtils;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * 查询电话及 SIM 类型或打开拨号界面；不读取设备受限标识。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class XUtil电话 {

	/**
	 * 函数集仅提供静态成员，不创建实例。
	 */
	private XUtil电话() {
	}

	/**
	 * 查询是否电话设备，无电话能力时由系统返回缺省信息。
	 *
	 * @return 判断或操作的结果。
	 */
	@SimpleFunction
	public static boolean 是否电话设备() {
		XUtilSupport.context();
		return PhoneUtils.isPhone();
	}

	/**
	 * 查询取电话类型，无电话能力时由系统返回缺省信息。
	 *
	 * @return 返回的整数值。
	 */
	@SimpleFunction
	public static int 取电话类型() {
		XUtilSupport.context();
		return PhoneUtils.getPhoneType();
	}

	/**
	 * 查询SIM是否就绪，无电话能力时由系统返回缺省信息。
	 *
	 * @return 判断或操作的结果。
	 */
	@SimpleFunction
	public static boolean SIM是否就绪() {
		XUtilSupport.context();
		return PhoneUtils.isSimCardReady();
	}

	/**
	 * 查询取运营商，无电话能力时由系统返回缺省信息。
	 *
	 * @return 返回的文本。
	 */
	@SimpleFunction
	public static String 取运营商() {
		XUtilSupport.context();
		return PhoneUtils.getSimOperatorName();
	}

	/**
	 * 打开系统拨号界面供用户确认；不会直接拨出电话。
	 *
	 * @param number 号码。
	 * @return 界面请求已发出返回真，无可处理的拨号应用时返回假。
	 */
	@SimpleFunction
	public static boolean 打开拨号(String number) {
		XUtilSupport.requireMainThread();
		XUtilSupport.context();
		try {
			PhoneUtils.dial(XUtilSupport.phoneNumber(number));
			return true;
		} catch (ActivityNotFoundException unavailable) {
			// 不把缺少拨号应用当成成功，也不依赖受包可见性限制的预查询。
			return false;
		}
	}
}
