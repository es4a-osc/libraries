package simple.library.wsd.xupdate;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * 接管 APK 安装请求；返回值只表示是否接管，不能表示系统已经安装成功。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public interface 安装器 {

	/**
	 * 请求安装有效的本地 APK。
	 *
	 * @param path 可读取的 APK 绝对路径。
	 * @return 是否已经接管安装请求。
	 */
	@SimpleFunction
	boolean 安装(String path);
}
