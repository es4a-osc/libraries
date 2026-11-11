package simple.library.wsd.accessibility;

import simple.runtime.annotations.ManifestNodes;
import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleEvent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.SimpleProperty;
import simple.runtime.annotations.UsesPermissions;
import simple.runtime.components.组件;
import simple.runtime.parameters.BooleanReferenceParameter;

/**
 * 系统无障碍服务的非可视入口。需用户手动启用服务；启用不等于连接。组件与控制器在主线程使用，回调也在主线程触发。销毁组件只注销订阅，不撤销系统授权。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
@SimpleComponent
@UsesPermissions(permissionNames = "android.permission.USE_FINGERPRINT")
@ManifestNodes(
	applicationXml = "<service" + " android:name=\"simple.library.wsd.accessibility.AccessibilityListener\""
		+ " android:enabled=\"true\"" + " android:exported=\"true\"" + " android:label=\"@string/app_name\""
		+ " android:permission=\"android.permission.BIND_ACCESSIBILITY_SERVICE\">" + "<intent-filter>"
		+ "<action android:name=\"android.accessibilityservice.AccessibilityService\" />" + "</intent-filter>"
		+ "<meta-data" + " android:name=\"android.accessibilityservice\""
		+ " android:resource=\"@xml/es4a_accessibility_service\" />" + "</service>"
)
public interface 无障碍服务 extends 组件 {

	/* 属性 */

	/**
	 * 本应用服务是否在系统中启用，不表示已连接。
	 *
	 * @return 本应用服务是否在系统中启用，不表示已连接。
	 */
	@SimpleProperty
	boolean 已启用();

	/**
	 * 本应用的无障碍服务是否已连接并可执行操作。
	 *
	 * @return 本应用的无障碍服务是否已连接并可执行操作。
	 */
	@SimpleProperty
	boolean 已连接();

	/**
	 * 系统是否启用了任意无障碍服务。
	 *
	 * @return 系统是否启用了任意无障碍服务。
	 */
	@SimpleProperty
	boolean 系统无障碍已启用();

	/**
	 * 系统是否已启用触摸探索。
	 *
	 * @return 系统是否已启用触摸探索。
	 */
	@SimpleProperty
	boolean 触摸探索已启用();

	/**
	 * 本组件是否订阅事件，关闭不影响服务连接和主动查询。
	 *
	 * @return 本组件是否订阅事件，关闭不影响服务连接和主动查询。
	 */
	@SimpleProperty
	boolean 事件监听();

	/**
	 * 启用或暂停本组件的全部无障碍及字幕事件回调。
	 *
	 * @param enabled 是否继续接收事件
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_BOOLEAN,
		initializer = "True"
	)
	void 事件监听(boolean enabled);

	/**
	 * 系统字幕首选项是否启用，不代表应用自动绘制字幕。
	 *
	 * @return 系统字幕首选项是否启用，不代表应用自动绘制字幕。
	 */
	@SimpleProperty
	boolean 字幕已启用();

	/**
	 * 系统字幕字体比例，1 表示默认字号。
	 *
	 * @return 系统字幕字体比例，1 表示默认字号。
	 */
	@SimpleProperty
	float 字幕字体比例();

	/**
	 * 系统字幕首选语言的 BCP 47 标签，未指定时为空文本。
	 *
	 * @return 系统字幕首选语言的 BCP 47 标签，未指定时为空文本。
	 */
	@SimpleProperty
	String 字幕语言();

	/**
	 * 设备是否支持系统导航区的辅助按钮，需要 API 27 及以上；不表示当前按钮可用。
	 *
	 * @return 系统报告的辅助按钮支持状态。
	 */
	@SimpleProperty
	boolean 辅助按钮受支持();

	/* 方法 */

	/**
	 * 打开系统无障碍设置，由用户手动授权。
	 *
	 * @return 设置页面已打开时为真；没有可用页面时为假，不代表授权成功
	 */
	@SimpleFunction
	boolean 打开设置();

	/**
	 * 取得当前活动窗口的根节点，使用后释放。
	 *
	 * @return 根节点；服务未连接或无法读取时为空值
	 */
	@SimpleFunction
	无障碍节点 取根节点();

	/**
	 * 取得可交互窗口列表，使用后释放；需在服务配置中保留访问交互窗口标志。
	 *
	 * @return 窗口列表；服务未连接或无窗口时数量为零
	 */
	@SimpleFunction
	无障碍窗口列表 取窗口列表();

	/**
	 * 在系统允许访问的窗口中查找焦点，使用后释放。
	 *
	 * @param type 输入焦点或无障碍焦点常量
	 * @return 对应节点；未连接或无焦点时为空值
	 */
	@SimpleFunction
	无障碍节点 查找焦点(int type);

	/**
	 * 执行系统全局操作，动作编号不是键盘键码。
	 *
	 * @param action 全局动作常量；锁屏和截图需要 API 28
	 * @return 系统报告接受操作时为真，否则为假
	 */
	@SimpleFunction
	boolean 执行全局动作(int action);

	/**
	 * 请求禁用当前服务；此后须由用户回到设置页重新启用。
	 *
	 * @return 已向当前服务提交禁用请求时为真，服务未连接时为假
	 */
	@SimpleFunction
	boolean 禁用服务();

	/**
	 * 读取当前服务的配置快照。
	 *
	 * @return 服务信息快照；未连接时为空值
	 */
	@SimpleFunction
	无障碍服务信息 取服务信息();

	/**
	 * 提交可动态修改的服务配置。静态能力仍由 XML 决定；多个组件共用同一系统服务，后提交者生效。
	 *
	 * @param info 由当前服务取得并修改的快照
	 * @return 已提交配置时为真，未连接时为假
	 */
	@SimpleFunction
	boolean 应用配置(无障碍服务信息 info);

	/**
	 * 列出系统报告的已安装无障碍服务。
	 *
	 * @return 服务信息快照数组
	 */
	@SimpleFunction
	无障碍服务信息[] 取已安装服务();

	/**
	 * 列出系统报告的已启用无障碍服务。
	 *
	 * @return 服务信息快照数组
	 */
	@SimpleFunction
	无障碍服务信息[] 取已启用服务();

	/**
	 * 请求中断系统当前启用的无障碍反馈，不禁用服务。
	 */
	@SimpleFunction
	void 中断所有反馈();

	/**
	 * 向系统提交无障碍事件副本；系统可能按事件内容与权限过滤，不保证其它服务收到。
	 *
	 * @param event 仍然有效的事件；通常来自复制事件
	 */
	@SimpleFunction
	void 发送事件(无障碍事件 event);

	/**
	 * 触发本组件最近收到通知中的内容操作。
	 *
	 * @return 内容操作已发送时为真；未接收通知或操作失效时为假
	 */
	@SimpleFunction
	boolean 打开最近通知();

	/**
	 * 取得控制器，改变事件由本组件报告。
	 *
	 * @return 绑定当前连接的控制器；未连接时为空值
	 */
	@SimpleFunction
	无障碍软键盘控制器 取软键盘控制器();

	/**
	 * 取得控制器，改变事件由本组件报告。
	 *
	 * @return 绑定当前连接的控制器；未连接时为空值
	 */
	@SimpleFunction
	无障碍放大控制器 取放大控制器();

	/**
	 * 取得控制器，改变事件由本组件报告。需要 API 26 及以上。
	 *
	 * @return 绑定当前连接的控制器；未连接时为空值
	 */
	@SimpleFunction
	无障碍按钮控制器 取辅助按钮控制器();

	/**
	 * 取得控制器，改变事件由本组件报告。需要 API 26 及以上。
	 *
	 * @return 绑定当前连接的控制器；未连接时为空值
	 */
	@SimpleFunction
	无障碍指纹控制器 取指纹控制器();

	/**
	 * 向屏幕提交手势。提交成功后由手势完成或手势取消事件报告结果；新手势可能取消正在执行的手势。
	 *
	 * @param gesture   至少含一笔的手势描述
	 * @param requestId 由调用者指定，用于关联完成或取消事件
	 * @return 系统接受提交时为真，未连接或系统拒绝时为假；不代表执行完成
	 */
	@SimpleFunction
	boolean 派发手势(无障碍手势 gesture, int requestId);

	/**
	 * 取得系统字幕首选样式快照。
	 *
	 * @return 颜色、边缘及字体样式数据
	 */
	@SimpleFunction
	无障碍字幕样式 取字幕样式();

	/**
	 * 取得当前可用的系统全局动作，需要 API 30 及以上。
	 *
	 * @return 动作描述数组；服务未连接时为空数组，通过执行全局动作使用其中的编号。
	 */
	@SimpleFunction
	无障碍动作[] 取系统动作();

	/**
	 * 按用户的辅助功能设置计算界面内容应至少保留的时间。
	 *
	 * @param original     非负的原显示时长，单位为毫秒。
	 * @param contentFlags 显示内容掩码，使用无障碍常量中的显示内容常量，可组合。
	 * @return 建议显示时长，单位为毫秒；系统未提供额外建议时返回原时长。
	 */
	@SimpleFunction
	int 取建议显示时长(int original, int contentFlags);

	/* 事件 */

	/**
	 * 系统服务连接状态变化时触发，不以设置页启用状态代替连接状态。
	 *
	 * @param connected 真表示已连接，假表示已断开
	 */
	@SimpleEvent
	void 连接改变(boolean connected);

	/**
	 * 系统要求中断当前无障碍反馈时触发，不表示服务解绑。
	 */
	@SimpleEvent
	void 服务被中断();

	/**
	 * 系统收到无障碍事件时触发；事件处理内持续修改界面可能引起反馈循环。
	 *
	 * @param event 仅在该次回调中有效；保留时先复制事件
	 */
	@SimpleEvent
	void 收到事件(无障碍事件 event);

	/**
	 * 系统允许过滤按键时触发。默认不拦截，不影响音量等原始行为。
	 *
	 * @param code    Android 按键代码
	 * @param action  按下为 0，抬起为 1
	 * @param handled 设为真表示拦截本次按键，默认假
	 */
	@SimpleEvent
	void 收到按键(int code, int action, BooleanReferenceParameter handled);

	/**
	 * 收到系统触摸探索手势时触发；需要用户启用触摸探索及服务配置请求相应模式。
	 *
	 * @param gesture 系统识别的触摸探索手势编号
	 * @param handled 设为真表示处理本次手势，默认假
	 */
	@SimpleEvent
	void 收到触摸手势(int gesture, BooleanReferenceParameter handled);

	/**
	 * 提交的手势执行完成。
	 *
	 * @param requestId 派发时指定的编号
	 */
	@SimpleEvent
	void 手势完成(int requestId);

	/**
	 * 已提交的手势执行被取消。
	 *
	 * @param requestId 派发时指定的编号
	 */
	@SimpleEvent
	void 手势取消(int requestId);

	/**
	 * 软键盘显示策略改变。
	 *
	 * @param mode 当前显示策略
	 */
	@SimpleEvent
	void 软键盘模式改变(int mode);

	/**
	 * 系统放大区域、比例或中心改变；区域参数是外接矩形。
	 *
	 * @param bounds 可放大区域的屏幕像素外接矩形
	 * @param scale  当前倍率
	 * @param x      未缩放屏幕像素横坐标
	 * @param y      未缩放屏幕像素纵坐标
	 */
	@SimpleEvent
	void 放大状态改变(无障碍边界 bounds, float scale, float x, float y);

	/**
	 * 辅助按钮被单击，需要 API 26 及以上。
	 */
	@SimpleEvent
	void 辅助按钮被单击();

	/**
	 * 辅助按钮可用性改变，需要 API 26 及以上。
	 *
	 * @param available 当前是否可用
	 */
	@SimpleEvent
	void 辅助按钮可用性改变(boolean available);

	/**
	 * 指纹传感器报告手势，需要 API 26 及受支持硬件。
	 *
	 * @param gesture 指纹滑动方向常量
	 */
	@SimpleEvent
	void 收到指纹手势(int gesture);

	/**
	 * 指纹手势可用性变化，需要 API 26 及受支持硬件。
	 *
	 * @param available 当前是否可用
	 */
	@SimpleEvent
	void 指纹手势可用性改变(boolean available);

	/**
	 * 任意系统无障碍服务启用状态改变。
	 *
	 * @param enabled 系统无障碍是否启用
	 */
	@SimpleEvent
	void 系统启用状态改变(boolean enabled);

	/**
	 * 系统触摸探索启用状态改变。
	 *
	 * @param enabled 是否启用触摸探索
	 */
	@SimpleEvent
	void 触摸探索状态改变(boolean enabled);

	/**
	 * 系统当前可用的全局动作列表发生变化，需要 API 30 及以上。
	 */
	@SimpleEvent
	void 系统动作改变();

	/**
	 * 系统字幕启用、语言、字号或样式改变后触发，通过属性重新读取。
	 */
	@SimpleEvent
	void 字幕设置改变();
}
