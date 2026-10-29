package simple.library.wsd.xutil;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.xuexiang.xutil.net.NetworkUtils;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.UsesPermissions;

/**
 * 域名与本机 IP 查询；可能阻塞，必须在工作线程调用。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
@UsesPermissions(permissionNames =
	// 允许工作线程发起域名解析及查询本机网络接口地址。
	"android.permission.INTERNET"
)
public final class XUtil域名 {

	/**
	 * 函数集仅提供静态成员，不创建实例。
	 */
	private XUtil域名() {
	}

	/**
	 * 解析域名的全部地址；失败抛出异常，禁止在界面线程执行。
	 *
	 * @param host 域名。
	 * @return 返回的文本数组。
	 */
	@SimpleFunction
	public static String[] 解析(String host) {
		XUtilSupport.requireWorkerThread();
		try {
			InetAddress[] addresses = InetAddress.getAllByName(host);
			String[] values = new String[addresses.length];
			for (int i = 0; i < addresses.length; i++) {
				values[i] = addresses[i].getHostAddress();
			}
			return values;
		} catch (UnknownHostException failure) {
			throw XUtilSupport.ioFailure(failure);
		}
	}

	/**
	 * 查找本机网络接口地址；无符合地址时返回空文本。
	 *
	 * @param ipv4 使用IPv4。
	 * @return 返回的文本。
	 */
	@SimpleFunction
	public static String 取本机IP(boolean ipv4) {
		XUtilSupport.requireWorkerThread();
		return XUtilSupport.text(NetworkUtils.getIPAddress(ipv4));
	}
}
