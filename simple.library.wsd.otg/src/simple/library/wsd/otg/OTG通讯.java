package simple.library.wsd.otg;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.SimpleEvent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.ManifestNodes;
import simple.runtime.components.组件;

/**
 * USB Host 下的 CH34x 串口通讯；支持 1a86:7523 和 1a86:5523。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
@SimpleComponent
@ManifestNodes(
	// 声明 USB Host 能力，不强制要求设备支持。
	rootXml =
		"<uses-feature" +
		" android:name=\"android.hardware.usb.host\"" +
		" android:required=\"false\"" +
		" />",
	// 注册 USB 设备接入广播及支持的设备过滤器。
	activityXml =
		"<intent-filter> " +
		"<action" +
		" android:name=\"android.hardware.usb.action.USB_DEVICE_ATTACHED\"" +
		" />" +
		"</intent-filter>" +
		"<meta-data" +
		" android:name=\"android.hardware.usb.action.USB_DEVICE_ATTACHED\"" +
		" android:resource=\"@xml/es4a_otg_device_filter\"" +
		" />"
)
public interface OTG通讯 extends 组件 {

	/* 组件事件 */

	/**
	 * 收到串口字节片段时在主线程触发，不保证一次事件对应完整协议帧。
	 *
	 * @param data 本次收到的原始字节片段。
	 */
	@SimpleEvent
	void 收到数据(byte[] data);

	/* 组件方法 */

	/**
	 * 判断设备是否提供 USB Host；不表示已经连接串口。
	 *
	 * @return 系统声明支持 USB Host 时返回真；不代表串口已经连接。
	 */
	@SimpleFunction
	boolean 是否支持OTG();

	/**
	 * 打开第一台匹配的 CH34x 设备；未授权时发起系统授权并返回假，授权后请再次调用。已打开时返回假。
	 *
	 * @return 串口已成功打开时返回真；等待授权、已打开或失败时返回假。
	 */
	@SimpleFunction
	boolean 打开设备();

	/**
	 * 关闭串口并停止当前接收会话，可重复调用。
	 */
	@SimpleFunction
	void 关闭设备();

	/**
	 * 设置波特率，使用 8 数据位、1 停止位、无校验且无流控的串口配置。
	 *
	 * @param baudRate 波特率，须为 CH34x 驱动支持的值。
	 * @throws IllegalStateException 设备未打开或驱动拒绝串口参数。
	 * @throws IllegalArgumentException 波特率不受支持。
	 */
	@SimpleFunction
	void 设置参数(int baudRate);

	/**
	 * 设置波特率、数据位 5-8、停止位 1-2、校验位 0-4（无、奇、偶、标记、空格），不启用流控；失败抛出异常。
	 *
	 * @param baudRate 波特率，须为 CH34x 驱动支持的值。
	 * @param dataBits 数据位，范围 5 至 8。
	 * @param stopBits 停止位，范围 1 至 2。
	 * @param parity 校验位：0 无、1 奇、2 偶、3 标记、4 空格。
	 * @throws IllegalArgumentException 参数不在支持范围内。
	 * @throws IllegalStateException 设备未打开或驱动拒绝串口参数。
	 */
	@SimpleFunction
	void 设置参数(int baudRate, int dataBits, int stopBits, int parity);

	/**
	 * 发送字节集并返回驱动报告的写入长度；未打开或空数据返回 0，返回值不代表对端已收到。
	 *
	 * @param data 要发送的原始字节数据。
	 * @return 驱动报告的写入字节数；未打开或数据为空时为 0，不代表对端已收到。
	 */
	@SimpleFunction
	int 发送数据(byte[] data);

	/**
	 * 将全部字节转换为小写十六进制文本，每字节后保留一个空格。
	 *
	 * @param data 待转换的字节数据；空引用返回空文本。
	 * @return 转换后的文本；空引用或空数组返回空文本。
	 */
	@SimpleFunction
	String 到十六进制(byte[] data);

	/**
	 * 转换为小写十六进制文本，每字节后保留空格；长度须在字节集范围内。
	 *
	 * @param data 待转换的字节数据。
	 * @param length 参与转换的字节数，范围 0 至数组长度。
	 * @return 小写十六进制文本，每个字节后保留一个空格；数据为空时返回空文本。
	 * @throws IllegalArgumentException 十六进制字符无效或转换长度越界。
	 */
	@SimpleFunction
	String 到十六进制(byte[] data, int length);

	/**
	 * 忽略空白，奇数位末尾补零；空文本返回空字节集，无效字符抛出异常。
	 *
	 * @param source 待解析的十六进制文本，可包含空白字符。
	 * @return 转换后的字节数组，空文本或空引用返回空数组。
	 * @throws IllegalArgumentException 十六进制字符无效或转换长度越界。
	 */
	@SimpleFunction
	byte[] 到字节集(String source);
}
