package simple.library.wsd.xqrcode;

import simple.runtime.annotations.*;
import simple.runtime.components.组件;

/**
 * 二维码生成、图片识别与独立相机扫码。所有成员在主线程调用，任务失败通过事件报告。
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
public interface 二维码 extends 组件 {

	/**
	 * 异步生成成功后在主线程触发，图片为 PNG 数据。
	 *
	 * @param requestId 请求序号。
	 * @param image 图片数据。
	 */
	@SimpleEvent
	void 生成完成(int requestId, byte[] image);

	/**
	 * 异步图片识别成功后在主线程触发。
	 *
	 * @param requestId 请求序号。
	 * @param result 结果。
	 */
	@SimpleEvent
	void 识别完成(int requestId, 识别结果 result);

	/**
	 * 异步任务失败时触发，不会同时触发成功或取消。
	 *
	 * @param requestId 请求序号。
	 * @param operation 操作。
	 * @param reason 原因。
	 */
	@SimpleEvent
	void 任务失败(int requestId, String operation, String reason);

	/**
	 * 成功取消异步任务或用户取消选图时触发；销毁不再触发事件。
	 *
	 * @param requestId 请求序号。
	 * @param operation 操作。
	 */
	@SimpleEvent
	void 任务取消(int requestId, String operation);

	/**
	 * 独立扫码页的真实相机预览启动后触发；恢复前台时可再次触发。
	 *
	 * @param requestId 请求序号。
	 */
	@SimpleEvent
	void 扫描准备就绪(int requestId);

	/**
	 * 独立扫码页识别成功时触发；连续模式可重复触发。
	 *
	 * @param requestId 请求序号。
	 * @param result 结果。
	 */
	@SimpleEvent
	void 扫描得到结果(int requestId, 识别结果 result);

	/**
	 * 独立扫码页报告权限、设备或补光失败；补光或提示反馈失败不结束现有预览。
	 *
	 * @param requestId 请求序号。
	 * @param reason 原因。
	 */
	@SimpleEvent
	void 扫描失败(int requestId, String reason);

	/**
	 * 返回或主动停止独立扫码页时触发；销毁后不触发。
	 *
	 * @param requestId 请求序号。
	 */
	@SimpleEvent
	void 扫描取消(int requestId);

	/**
	 * 独立扫码页尚未结束的请求序号；没有请求时为 0。
	 *
	 * @return 本次操作的返回值。
	 */
	@SimpleProperty
	int 当前扫描序号();

	/**
	 * 尚未完成或取消的生成、图片识别及选图请求数量。
	 *
	 * @return 本次操作的返回值。
	 */
	@SimpleProperty
	int 异步任务数();

	/**
	 * 同步生成二维码 PNG；输入或配置无效时抛出错误，大图建议使用异步生成。
	 *
	 * @param content 内容。
	 * @param options 配置。
	 * @return 本次操作的返回值。
	 */
	@SimpleFunction
	byte[] 生成二维码(String content, 二维码配置 options);

	/**
	 * 复制配置并提交后台生成，返回正整数请求序号；队列满时通过任务失败报告。
	 *
	 * @param content 内容。
	 * @param options 配置。
	 * @return 本次操作的返回值。
	 */
	@SimpleFunction
	int 异步生成(String content, 二维码配置 options);

	/**
	 * 同步识别资产、文件或 content URI；仅使用格式、字符集及返回图像设置。无结果时抛出错误。
	 *
	 * @param path 图片路径。
	 * @param options 配置。
	 * @return 本次操作的返回值。
	 */
	@SimpleFunction
	识别结果 识别图片(String path, 扫码配置 options);

	/**
	 * 同步识别图像字节；输入压缩数据不超过 32 MiB，无结果时抛出错误。
	 *
	 * @param data 图片数据。
	 * @param options 配置。
	 * @return 本次操作的返回值。
	 */
	@SimpleFunction
	识别结果 识别字节图片(byte[] data, 扫码配置 options);

	/**
	 * 后台识别资产、文件或 content URI，以识别完成或任务失败返回结果。
	 *
	 * @param path 图片路径。
	 * @param options 配置。
	 * @return 本次操作的返回值。
	 */
	@SimpleFunction
	int 异步识别图片(String path, 扫码配置 options);

	/**
	 * 复制图像数据及配置后后台识别，返回请求序号。
	 *
	 * @param data 图片数据。
	 * @param options 配置。
	 * @return 本次操作的返回值。
	 */
	@SimpleFunction
	int 异步识别字节图片(byte[] data, 扫码配置 options);

	/**
	 * 打开系统文档选择器并后台识别所选图片；同时只能有一个选图请求，忙时返回 0。
	 *
	 * @param options 配置。
	 * @return 本次操作的返回值。
	 */
	@SimpleFunction
	int 选择图片识别(扫码配置 options);

	/**
	 * 取消指定异步或选图请求并丢弃在途结果；存在该请求时返回真。取消选图不关闭系统选择器，返回前不能再次选图。
	 *
	 * @param requestId 请求序号。
	 * @return 操作结果。
	 */
	@SimpleFunction
	boolean 取消任务(int requestId);

	/**
	 * 打开普通 Activity 扫码页并自动请求相机权限；已有本组件请求时返回 0，否则返回正整数请求序号。
	 *
	 * @param options 配置。
	 * @return 本次操作的返回值。
	 */
	@SimpleFunction
	int 开始扫描(扫码配置 options);

	/**
	 * 关闭指定独立扫码页并取消请求；过期或其它实例的序号返回假。
	 *
	 * @param requestId 请求序号。
	 * @return 操作结果。
	 */
	@SimpleFunction
	boolean 停止扫描(int requestId);

	/**
	 * 暂停指定请求并释放相机；请求有效且操作被接受时返回真。
	 *
	 * @param requestId 请求序号。
	 * @return 操作结果。
	 */
	@SimpleFunction
	boolean 暂停扫描(int requestId);

	/**
	 * 继续指定请求，等待前台及权限后恢复预览。
	 *
	 * @param requestId 请求序号。
	 * @return 操作结果。
	 */
	@SimpleFunction
	boolean 继续扫描(int requestId);

	/**
	 * 设置指定请求的真实补光；预览未打开、请求过期或硬件不支持时返回假。
	 *
	 * @param requestId 请求序号。
	 * @param enabled 开启。
	 * @return 操作结果。
	 */
	@SimpleFunction
	boolean 设置扫描补光(int requestId, boolean enabled);
}
