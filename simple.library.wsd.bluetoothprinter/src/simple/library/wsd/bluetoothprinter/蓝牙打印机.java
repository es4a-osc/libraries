package simple.library.wsd.bluetoothprinter;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.SimpleEvent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.ManifestNodes;
import simple.runtime.annotations.UsesPermissions;
import simple.runtime.components.组件;

/**
 * 经典蓝牙打印机连接和原库 ESC/POS 指令；非 BLE 打印协议。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
@SimpleComponent
@UsesPermissions(
	permissionNames =
		// 访问经典蓝牙设备。
		"android.permission.BLUETOOTH," +
		// 搜索设备及管理经典蓝牙连接。
		"android.permission.BLUETOOTH_ADMIN," +
		// 旧目标版本扫描蓝牙设备所需的定位权限。
		"android.permission.ACCESS_FINE_LOCATION," +
		// Android 12 及以上的设备扫描权限。
		"android.permission.BLUETOOTH_SCAN," +
		// Android 12 及以上的设备连接权限。
		"android.permission.BLUETOOTH_CONNECT"
)
@ManifestNodes(
	rootXml =
		// 声明经典蓝牙能力，不强制要求设备支持。
		"<uses-feature" +
		" android:name=\"android.hardware.bluetooth\"" +
		" android:required=\"false\"" +
		" />"
)
public interface 蓝牙打印机 extends 组件 {

	/* 组件事件 */

	/**
	 * 搜索自然结束或停止时在主线程触发。
	 */
	@SimpleEvent
	void 搜索完毕();

	/**
	 * 搜索发现设备时返回名称、地址和配对状态；发现设备不表示其支持打印协议。
	 *
	 * @param name 设备名称，设备未提供名称时可能为空。
	 * @param address 目标蓝牙设备的 MAC 地址。
	 * @param bonded 设备是否已经与本机配对。
	 */
	@SimpleEvent
	void 发现设备(String name, String address, boolean bonded);

	/**
	 * 连接成功时在主线程触发。
	 */
	@SimpleEvent
	void 连接成功();

	/**
	 * 连接地址无效、权限不足或连接失败时在主线程触发。
	 */
	@SimpleEvent
	void 连接失败();

	/**
	 * 蓝牙打印连接意外丢失时在主线程触发。
	 */
	@SimpleEvent
	void 丢失连接();

	/* 组件方法 */

	/**
	 * 请求系统开启经典蓝牙，由用户在系统界面确认。
	 */
	@SimpleFunction
	void 开启蓝牙();

	/**
	 * 停止当前通讯并打开系统蓝牙设置，由用户关闭蓝牙。
	 */
	@SimpleFunction
	void 关闭蓝牙();

	/**
	 * 判断本机是否支持且已开启蓝牙；权限不足时返回假。
	 *
	 * @return 蓝牙可用且已经开启时返回真，权限不足时返回假。
	 */
	@SimpleFunction
	boolean 是否已开启蓝牙();

	/**
	 * 返回当前是否已建立连接。
	 *
	 * @return 当前连接已经建立且组件可用时返回真。
	 */
	@SimpleFunction
	boolean 是否已连接();

	/**
	 * 搜索附近经典蓝牙设备；应先授予当前系统要求的蓝牙及定位权限并开启蓝牙。
	 */
	@SimpleFunction
	void 搜索设备();

	/**
	 * 停止当前设备搜索。
	 */
	@SimpleFunction
	void 停止搜索();

	/**
	 * 返回二维文本数组，每行为名称、MAC 地址；没有设备时为空数组。
	 *
	 * @return 设备名称和 MAC 地址组成的二维数组，没有配对设备时为空数组。
	 */
	@SimpleFunction
	String[][] 取已配对设备();

	/**
	 * 异步连接指定 MAC 地址的蓝牙打印机；失败通过连接失败通知。
	 *
	 * @param address 目标蓝牙设备的 MAC 地址。
	 */
	@SimpleFunction
	void 连接设备(String address);

	/**
	 * 断开当前打印连接，不关闭手机蓝牙。
	 */
	@SimpleFunction
	void 断开连接();

	/**
	 * 以 GBK 编码发送文本，打印机需支持相应字符集；未连接或写入失败抛出异常。
	 *
	 * @param text 要打印的文本，须符合本方法声明的编码及长度约束。
	 * @throws IllegalStateException 尚未连接或打印指令写入失败。
	 */
	@SimpleFunction
	void 文本(String text);

	/**
	 * 发送原库 Code128 指令，内容须为 ASCII 且不超过 255 字节；型号与代码集支持由打印机决定。
	 *
	 * @param text 要打印的文本，须符合本方法声明的编码及长度约束。
	 * @throws IllegalStateException 尚未连接或打印指令写入失败。
	 */
	@SimpleFunction
	void 条码(String text);

	/**
	 * 解码图片字节集并缩放至指定像素尺寸，转换为 24 点 ESC/POS 位图；尺寸须大于 0，纸宽由打印机决定。
	 *
	 * @param data 图片编码数据。
	 * @param width 目标图片宽度，单位像素，范围 1 至 4096。
	 * @param height 目标图片高度，单位像素，范围 1 至 4096。
	 * @throws IllegalStateException 尚未连接或打印指令写入失败。
	 */
	@SimpleFunction
	void 图片(byte[] data, int width, int height);

	/**
	 * 原样发送打印机指令字节集；仅使用目标型号支持的指令。
	 *
	 * @param data 要原样发送的打印指令字节数据。
	 * @throws IllegalStateException 尚未连接或打印指令写入失败。
	 */
	@SimpleFunction
	void 指令(byte[] data);

	/**
	 * 发送 ESC @ 重置设置。
	 *
	 * @throws IllegalStateException 尚未连接或打印指令写入失败。
	 */
	@SimpleFunction
	void 重置设置();

	/**
	 * 发送回车指令 0D，具体走纸及打印行为由型号决定。
	 *
	 * @throws IllegalStateException 尚未连接或打印指令写入失败。
	 */
	@SimpleFunction
	void 打印当前内容();

	/**
	 * 发送换行指令 0A。
	 *
	 * @throws IllegalStateException 尚未连接或打印指令写入失败。
	 */
	@SimpleFunction
	void 换行();

	/**
	 * 切换原库中文下划线指令。
	 *
	 * @param enabled 是否启用此打印效果。
	 * @throws IllegalStateException 尚未连接或打印指令写入失败。
	 */
	@SimpleFunction
	void 下划线(boolean enabled);

	/**
	 * 切换反白打印。
	 *
	 * @param enabled 是否启用此打印效果。
	 * @throws IllegalStateException 尚未连接或打印指令写入失败。
	 */
	@SimpleFunction
	void 反白(boolean enabled);

	/**
	 * 切换粗体打印。
	 *
	 * @param enabled 是否启用此打印效果。
	 * @throws IllegalStateException 尚未连接或打印指令写入失败。
	 */
	@SimpleFunction
	void 粗体(boolean enabled);

	/**
	 * 设置行距，范围 0-255。
	 *
	 * @param value 行距指令参数，范围 0 至 255。
	 * @throws IllegalStateException 尚未连接或打印指令写入失败。
	 */
	@SimpleFunction
	void 行距(int value);

	/**
	 * 1 为正常大小，2 为双倍宽高。
	 *
	 * @param value 字号模式：1 为正常，2 为双倍宽高。
	 * @throws IllegalStateException 尚未连接或打印指令写入失败。
	 */
	@SimpleFunction
	void 字体大小(int value);

	/**
	 * 发送 GS ! 字号参数；范围 0-255，具体合法位由打印机决定。
	 *
	 * @param value GS ! 指令参数，范围 0 至 255；合法位由打印机决定。
	 * @throws IllegalStateException 尚未连接或打印指令写入失败。
	 */
	@SimpleFunction
	void 字体大小2(int value);

	/**
	 * 设置对齐方式：1 左对齐、2 居中、3 右对齐。
	 *
	 * @param value 对齐模式：1 左对齐、2 居中、3 右对齐。
	 * @throws IllegalStateException 尚未连接或打印指令写入失败。
	 */
	@SimpleFunction
	void 对齐方式(int value);

	/**
	 * 发送水平制表符 09。
	 *
	 * @throws IllegalStateException 尚未连接或打印指令写入失败。
	 */
	@SimpleFunction
	void 水平制表();

	/**
	 * 设置一个水平制表位置，0 结束设置；字节参数按无符号值发送。
	 *
	 * @param value 制表位置，按无符号字节发送；0 表示结束设置。
	 * @throws IllegalStateException 尚未连接或打印指令写入失败。
	 */
	@SimpleFunction
	void 水平制表值(byte value);
}
