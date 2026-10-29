package simple.library.wsd.xutil;

import com.xuexiang.xutil.app.AppUtils;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * 当前应用版本、包名及可见应用信息；Android 11 起查询受包可见性限制。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class XUtil应用 {

	/**
	 * 函数集仅提供静态成员，不创建实例。
	 */
	private XUtil应用() {
	}

	/**
	 * 查询当前应用的包名。
	 *
	 * @return 返回的文本。
	 */
	@SimpleFunction
	public static String 取包名() {
		XUtilSupport.context();
		return AppUtils.getAppPackageName();
	}

	/**
	 * 查询当前应用的名称。
	 *
	 * @return 返回的文本。
	 */
	@SimpleFunction
	public static String 取名称() {
		XUtilSupport.context();
		return AppUtils.getAppName();
	}

	/**
	 * 查询当前应用的版本名。
	 *
	 * @return 返回的文本。
	 */
	@SimpleFunction
	public static String 取版本名() {
		XUtilSupport.context();
		return AppUtils.getAppVersionName();
	}

	/**
	 * 查询当前应用的版本号。
	 *
	 * @return 返回的整数值。
	 */
	@SimpleFunction
	public static int 取版本号() {
		XUtilSupport.context();
		return AppUtils.getAppVersionCode();
	}

	/**
	 * 查询当前应用的否调试版。
	 *
	 * @return 判断或操作的结果。
	 */
	@SimpleFunction
	public static boolean 是否调试版() {
		XUtilSupport.context();
		return AppUtils.isAppDebug();
	}

	/**
	 * 查询当前应用的安装路径。
	 *
	 * @return 返回的文本。
	 */
	@SimpleFunction
	public static String 取安装路径() {
		XUtilSupport.context();
		return AppUtils.getAppPath();
	}

	/**
	 * 判断指定包是否对当前应用可见且已安装；不可见不等于设备上不存在。
	 *
	 * @param packageName 包名。
	 * @return 判断或操作的结果。
	 */
	@SimpleFunction
	public static boolean 是否已安装(String packageName) {
		XUtilSupport.context();
		return AppUtils.isInstallApp(packageName);
	}
}
