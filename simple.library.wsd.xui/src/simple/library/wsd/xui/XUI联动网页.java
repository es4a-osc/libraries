package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.UsesPermissions;

/**
 * XUI联动网页组件。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
@UsesPermissions(permissionNames = "android.permission.INTERNET")
public interface XUI联动网页 extends XUI视图组件 {

	/**
	 * 获取最大的滚动距离。
	 *
	 * @return 最大的滚动距离。
	 */
	@SimpleFunction
	int 取滚动范围();

	/**
	 * webview内容滚动到底。
	 */
	@SimpleFunction
	void 滚动到底部();

	/**
	 * 平滑滚动到底部。
	 */
	@SimpleFunction
	void 平滑滚动到底部();

	/**
	 * 加载网页；网络地址需要应用声明网络权限。
	 *
	 * @param url 完整网址。
	 */
	@SimpleFunction
	void 加载网址(String url);
}
