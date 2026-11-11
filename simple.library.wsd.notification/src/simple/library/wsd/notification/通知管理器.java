package simple.library.wsd.notification;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleEvent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.UsesPermissions;
import simple.runtime.components.组件;

/**
 * 创建、发送和管理应用通知的非可视组件。 通知点击与操作回调由当前窗口中的管理器实例接收。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
@SimpleComponent
@UsesPermissions(permissionNames =
// 允许应用程序发布通知。
"android.permission.POST_NOTIFICATIONS")
public interface 通知管理器 extends 组件 {

	/* 事件 */

	/**
	 * 用户单击此组件发送的通知时触发。
	 *
	 * @param notificationId 被单击的通知编号
	 */
	@SimpleEvent
	void 通知被单击(int notificationId);

	/**
	 * 用户点击通知操作按钮时触发；组件所在窗口仍需保持活动。
	 *
	 * @param notificationId 发送时使用的通知编号
	 * @param mark           添加按钮时设置的标记
	 */
	@SimpleEvent
	void 按钮被单击(int notificationId, String mark);

	/**
	 * 用户提交内联回复时触发；组件所在窗口仍需保持活动。
	 *
	 * @param notificationId 发送时使用的通知编号
	 * @param mark           添加回复时设置的标记
	 * @param replyText      用户提交的文字
	 */
	@SimpleEvent
	void 收到回复(int notificationId, String mark, String replyText);

	/**
	 * 用户清除通知时触发；程序删除和清空通知不会触发。 回调要求组件及其宿主 Activity 仍存活。
	 *
	 * @param notificationId 被清除的通知编号
	 */
	@SimpleEvent
	void 通知被清除(int notificationId);

	/* 方法 */

	/**
	 * 创建使用内置小图标的通知构建器；可继续设置标题、正文和自定义图标。
	 *
	 * @return 可继续定制的通知构建器
	 */
	@SimpleFunction
	通知构建器 创建通知();

	/**
	 * 发布或更新通知。同一编号再次发送会更新已有通知。
	 *
	 * @param notificationId 用于更新、点击事件和删除通知的编号
	 * @param notification   已定制的通知构建器
	 */
	@SimpleFunction
	void 发送通知(int notificationId, 通知构建器 notification);

	/**
	 * 删除指定编号的通知。
	 *
	 * @param notificationId 要删除的通知编号
	 */
	@SimpleFunction
	void 删除通知(int notificationId);

	/**
	 * 清除本应用发布的所有通知。
	 */
	@SimpleFunction
	void 清空通知();

	/**
	 * 查询本应用的系统通知开关；单独的渠道仍可能被禁用。
	 *
	 * @return 是否允许本应用发布通知
	 */
	@SimpleFunction
	boolean 是否允许通知();

	/**
	 * 取得本应用当前活动通知的编号；需要 Android API 23。
	 *
	 * @return 活动通知编号快照
	 */
	@SimpleFunction
	int[] 取活动通知编号();

	/**
	 * 打开本应用的通知设置页面；返回不表示用户已授权。
	 */
	@SimpleFunction
	void 打开通知设置();

}
