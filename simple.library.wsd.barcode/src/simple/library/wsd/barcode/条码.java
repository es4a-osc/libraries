package simple.library.wsd.barcode;

import simple.runtime.annotations.ManifestNodes;
import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleEvent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.UsesPermissions;
import simple.runtime.components.组件;

/**
 * 二维码与条码的生成、图片识别和相机扫描。
 * 保留原组件的十个方法及扫描完毕事件；ZBar 仅附带原始 armeabi 二进制。
 */
@SimpleComponent
@SimpleObject
@UsesPermissions(permissionNames =
	// 扫描相机与闪光灯。
	"android.permission.CAMERA," +
	// 保留闪光灯访问声明，实际访问仍受相机和系统策略限制。
	"android.permission.FLASHLIGHT"
)
@ManifestNodes(
	rootXml =
		// 仅生成图片的应用也可安装到没有相机的设备。
		"<uses-feature android:name=\"android.hardware.camera\" android:required=\"false\" />" +
		"<uses-feature android:name=\"android.hardware.camera.autofocus\" android:required=\"false\" />",
	applicationXml =
		// 扫描界面只由本应用显式启动。
		"<activity android:name=\"simple.library.wsd.barcode.internal.zbar.scan.ScanCaptureAct\" " +
		"android:screenOrientation=\"portrait\" android:exported=\"false\" />" +
		"<activity android:name=\"simple.library.wsd.barcode.internal.zxing.CaptureActivity\" " +
		"android:screenOrientation=\"portrait\" android:exported=\"false\" />"
)
public interface 条码 extends 组件 {

	/* 事件 */

	/**
	 * 扫描得到有效内容时触发；连续模式下可以多次触发。
	 *
	 * @param result 扫描内容
	 */
	@SimpleEvent
	void 扫描完毕(String result);

	/* 方法 */

	/**
	 * 生成 CODE_128 条形码，返回 PNG 数据；内容为空时返回空引用。
	 *
	 * @param content 条形码内容，应使用 CODE_128 支持的字符
	 * @param width 目标宽度，像素
	 * @param height 目标高度，像素
	 * @return PNG 图片字节数组
	 */
	@SimpleFunction
	byte[] 生成条形码(String content, int width, int height);

	/**
	 * 生成指定正方形尺寸的二维码图片，沿用完整方法的格式和失败约定。
	 *
	 * @param content 要编码的文本。
	 * @param size 正方形图片的边长，单位像素。
	 * @return 二维码图片的字节集。
	 */
	@SimpleFunction
	byte[] 生成二维码(String content, int size);

	/**
	 * 使用 UTF-8 内容生成二维码，返回 PNG 数据。
	 *
	 * @param content 二维码内容
	 * @param width 目标宽度，像素
	 * @param height 目标高度，像素
	 * @return PNG 数据，编码失败或内容为空时为空数组
	 */
	@SimpleFunction
	byte[] 生成二维码(String content, int width, int height);

	/**
	 * 生成带中心图标的正方形二维码图片，沿用完整方法的格式和失败约定。
	 *
	 * @param content 要编码的文本。
	 * @param logoPath 中心图标路径，与完整方法一致。
	 * @param size 正方形图片的边长，单位像素。
	 * @return 二维码图片的字节集。
	 */
	@SimpleFunction
	byte[] 生成二维码2(String content, String logoPath, int size);

	/**
	 * 生成带中心图标的二维码，保留原组件的图标缩放方式。
	 *
	 * @param content 二维码内容
	 * @param logoPath 图标文件路径，或应用资产名称
	 * @param width 目标宽度，像素
	 * @param height 目标高度，像素
	 * @return PNG 数据，编码失败或内容为空时为空数组
	 */
	@SimpleFunction
	byte[] 生成二维码2(String content, String logoPath, int width, int height);

	/**
	 * 使用 ZBar 识别图片中的二维码或条码，需要匹配的原生库。
	 *
	 * @param imagePath 绝对路径、应用私有文件相对路径或资产名称
	 * @return 最后一个识别结果，没有结果时为空文本
	 */
	@SimpleFunction
	String 识别二维码(String imagePath);

	/**
	 * 设置随后打开的扫描界面；沿用原组件的应用内共享设置。
	 *
	 * @param style 1 为 ZBar 界面，2 为 ZXing 界面，其它值按 1 处理
	 */
	@SimpleFunction
	void 置扫描框样式(int style);

	/**
	 * 设置扫描成功后是否继续扫描，默认关闭。
	 *
	 * @param enabled 是否连续扫描
	 */
	@SimpleFunction
	void 置连续扫描(boolean enabled);

	/**
	 * 设置扫描界面启动后是否开启闪光灯，默认关闭。
	 *
	 * @param enabled 是否开启闪光灯，设备需具备相应能力
	 */
	@SimpleFunction
	void 置闪光灯开启(boolean enabled);

	/**
	 * 设置扫描成功后是否播放提示音，默认关闭。
	 *
	 * @param enabled 是否播放提示音
	 */
	@SimpleFunction
	void 置播放音效(boolean enabled);

	/**
	 * 打开扫描界面；调用前应取得相机权限。
	 */
	@SimpleFunction
	void 开始扫描();

	/**
	 * 通知本应用的扫描界面停止并关闭。
	 */
	@SimpleFunction
	void 停止扫描();
}
