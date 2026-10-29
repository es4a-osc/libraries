package simple.library.wsd.xutil;

import android.net.NetworkCapabilities;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.UsesPermissions;

/**
 * 查询默认网络的连接、互联网验证和传输类型。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
@UsesPermissions(permissionNames =
	// 查询默认网络及其连接、验证和传输能力。
	"android.permission.ACCESS_NETWORK_STATE"
)
public final class XUtil网络 {

	/**
	 * 函数集仅提供静态成员，不创建实例。
	 */
	private XUtil网络() {
	}

	/**
	 * 默认网络存在时返回真，不表示可以访问互联网。
	 *
	 * @return 判断或操作的结果。
	 */
	@SimpleFunction
	public static boolean 是否已连接() {
		return XUtilSupport.connectivity().getActiveNetwork() != null;
	}

	/**
	 * 默认网络已被系统验证可访问互联网时返回真。
	 *
	 * @return 判断或操作的结果。
	 */
	@SimpleFunction
	public static boolean 是否可访问互联网() {
		NetworkCapabilities capabilities = XUtilSupport.networkCapabilities();
		return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
	}

	/**
	 * 默认网络通过 Wi-Fi 传输时返回真。
	 *
	 * @return 判断或操作的结果。
	 */
	@SimpleFunction
	public static boolean 是否无线网络() {
		NetworkCapabilities capabilities = XUtilSupport.networkCapabilities();
		return capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
	}

	/**
	 * 默认网络通过蜂窝网络传输时返回真。
	 *
	 * @return 判断或操作的结果。
	 */
	@SimpleFunction
	public static boolean 是否移动网络() {
		NetworkCapabilities capabilities = XUtilSupport.networkCapabilities();
		return capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR);
	}

	/**
	 * 默认网络通过以太网传输时返回真。
	 *
	 * @return 判断或操作的结果。
	 */
	@SimpleFunction
	public static boolean 是否以太网() {
		NetworkCapabilities capabilities = XUtilSupport.networkCapabilities();
		return capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET);
	}

	/**
	 * 默认网络为 VPN 时返回真。
	 *
	 * @return 判断或操作的结果。
	 */
	@SimpleFunction
	public static boolean 是否VPN() {
		NetworkCapabilities capabilities = XUtilSupport.networkCapabilities();
		return capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN);
	}

	/**
	 * 查询默认网络是否被系统视为按流量计费。
	 *
	 * @return 判断或操作的结果。
	 */
	@SimpleFunction
	public static boolean 是否按流量计费() {
		return XUtilSupport.connectivity().isActiveNetworkMetered();
	}
}
