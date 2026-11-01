package simple.library.wsd.xqrcode;

import simple.runtime.annotations.*;
import simple.runtime.components.可视组件;

/**
 * 可嵌入 Simple 窗口的扫码预览；自动处理相机权限和前后台释放。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
@UsesPermissions(
	permissionNames = "android.permission.CAMERA,android.permission.FLASHLIGHT,android.permission.VIBRATE"
)
@ManifestNodes(
	rootXml = "<uses-feature android:name=\"android.hardware.camera\" android:required=\"false\" />" +
		"<uses-feature android:name=\"android.hardware.camera.autofocus\" android:required=\"false\" />",
	applicationXml = "<activity android:name=\"simple.library.wsd.xqrcode.internal.ScanActivity\" android:exported=\"false\" android:theme=\"@android:style/Theme.Material.Light.NoActionBar\" />"
)
public interface 扫码框 extends 可视组件 {

	/**
	 * 真实相机预览启动后触发，恢复前台时可能再次触发。
	 */
	@SimpleEvent
	void 扫描就绪();

	/**
	 * 识别成功后在主线程触发；连续模式按配置间隔重复触发。
	 *
	 * @param result 结果。
	 */
	@SimpleEvent
	void 识别完成(识别结果 result);

	/**
	 * 报告权限、设备及补光问题；补光或提示反馈失败不终止扫描，其余失败后须重新开始。
	 *
	 * @param reason 原因。
	 */
	@SimpleEvent
	void 扫描失败(String reason);

	/**
	 * 主动停止或扫码框移出界面时触发；销毁后不再触发。
	 */
	@SimpleEvent
	void 扫描取消();

	/**
	 * 状态变化时触发，状态包括未开始、等待界面、等待权限、正在打开相机、正在扫描、已暂停、已完成和失败。
	 *
	 * @param oldState 原状态。
	 * @param newState 新状态。
	 */
	@SimpleEvent
	void 状态改变(String oldState, String newState);

	/**
	 * 读取本实例当前状态。
	 *
	 * @return 本次操作的返回值。
	 */
	@SimpleProperty
	String 当前状态();

	/**
	 * 相机预览已经真实启动时为真。
	 *
	 * @return 操作结果。
	 */
	@SimpleProperty
	boolean 扫描中();

	/**
	 * 本次真实开启的补光状态，暂停或停止后为假。
	 *
	 * @return 操作结果。
	 */
	@SimpleProperty
	boolean 补光开启();

	/**
	 * 复制配置并请求扫描；正在处理请求时返回假，接受请求不等于识别成功。
	 *
	 * @param options 配置。
	 * @return 操作结果。
	 */
	@SimpleFunction
	boolean 开始扫描(扫码配置 options);

	/**
	 * 暂停当前请求并释放相机；没有请求时返回假。
	 *
	 * @return 操作结果。
	 */
	@SimpleFunction
	boolean 暂停扫描();

	/**
	 * 继续当前请求；自动等待前台、表面及相机权限。
	 *
	 * @return 操作结果。
	 */
	@SimpleFunction
	boolean 继续扫描();

	/**
	 * 停止当前请求并触发扫描取消；没有请求时返回假。
	 *
	 * @return 操作结果。
	 */
	@SimpleFunction
	boolean 停止扫描();

	/**
	 * 设置真实补光，设备不支持或预览未打开时返回假并报告原因。
	 *
	 * @param enabled 开启。
	 * @return 操作结果。
	 */
	@SimpleFunction
	boolean 设置补光(boolean enabled);

	/**
	 * 取得当前会话独立配置副本，修改副本不会影响进行中的扫描。
	 *
	 * @return 本次操作的返回值。
	 */
	@SimpleFunction
	扫码配置 取得配置();
}
