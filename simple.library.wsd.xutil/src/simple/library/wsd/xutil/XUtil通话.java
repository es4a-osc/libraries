package simple.library.wsd.xutil;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.pm.PackageManager;

import com.xuexiang.xutil.system.PhoneUtils;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.UsesPermissions;

/**
 * 直接拨号，必须先取得 CALL_PHONE 运行时授权；界面线程调用。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
@UsesPermissions(permissionNames =
	// 直接请求拨出电话；仍须先获得系统运行时授权。
	"android.permission.CALL_PHONE"
)
public final class XUtil通话 {

	/**
	 * 函数集仅提供静态成员，不创建实例。
	 */
	private XUtil通话() {
	}

	/**
	 * 检查当前应用是否已获得 CALL_PHONE 授权，不弹出授权框。
	 *
	 * @return 判断或操作的结果。
	 */
	@SimpleFunction
	public static boolean 是否已授权() {
		return XUtilSupport.context().checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED;
	}

	/**
	 * 直接请求拨号；未授权抛出异常，需设备支持通话。
	 *
	 * @param number 号码。
	 * @return 请求已发出返回真，无处理应用时返回假；不表示电话已接通。
	 */
	@SimpleFunction
	public static boolean 拨打(String number) {
		XUtilSupport.requireMainThread();
		if (!是否已授权()) {
			throw new SecurityException("尚未取得 CALL_PHONE 权限，请先通过权限请求组件申请");
		}
		try {
			PhoneUtils.call(XUtilSupport.phoneNumber(number));
			return true;
		} catch (ActivityNotFoundException unavailable) {
			// 平板和模拟器可能没有通话应用；按返回契约明确报告未发起。
			return false;
		}
	}
}
