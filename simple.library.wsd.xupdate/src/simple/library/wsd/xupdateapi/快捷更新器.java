package simple.library.wsd.xupdateapi;

import simple.library.wsd.xupdate.更新器;
import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.SimpleProperty;
import simple.runtime.annotations.UsesPermissions;

/**
 * 基于 EasyUpdate 的快捷更新器，沿用核心更新器的全部事件、配置和定制能力。
 * 需要暂停续传时启用断点续传，无需手动创建或绑定 Aria 下载器。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
@UsesPermissions(permissionNames =
	// 检查版本及下载更新文件。
	"android.permission.INTERNET," +
	// 核对核心更新器的仅 Wi-Fi 条件及下载网络状态。
	"android.permission.ACCESS_NETWORK_STATE," +
	// 请求系统安装，仍需用户授权及确认。
	"android.permission.REQUEST_INSTALL_PACKAGES," +
	// 显式转入后台后维持带通知的前台服务。
	"android.permission.FOREGROUND_SERVICE"
)
public interface 快捷更新器 extends 更新器 {

	/** @return 下次下载是否使用 Aria 续传。 */
	@SimpleProperty
	boolean 断点续传();

	/**
	 * 选择下次任务的下载实现；启用时须提供真实 APK MD5，只可在空闲时更改。
	 *
	 * @param value 真使用 Aria，假恢复核心默认下载器。
	 */
	@SimpleProperty(type = SimpleProperty.PROPERTY_TYPE_BOOLEAN, initializer = "False")
	void 断点续传(boolean value);
}
