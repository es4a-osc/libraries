package simple.library.wsd.blebluetooth;

import simple.runtime.annotations.ManifestNodes;
import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleEvent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.UsesPermissions;
import simple.runtime.collections.集合;
import simple.runtime.components.组件;

/**
 * BLE 低功耗蓝牙客户端，支持 Android 4.3（API 18）及以上系统。
 * 初始化BLE 准备扫描与服务绑定，连接及读写结果通过事件异步通知。
 * 应用需先取得当前系统要求的权限；一个应用共享一条 GATT 连接。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
@SimpleComponent
@UsesPermissions(
	permissionNames =
		// 访问蓝牙设备。
		"android.permission.BLUETOOTH," +
		// 扫描设备及设置可发现状态。
		"android.permission.BLUETOOTH_ADMIN," +
		// Android 6 至 11 的 BLE 扫描需要定位授权。
		"android.permission.ACCESS_FINE_LOCATION," +
		// Android 12 及以上的扫描权限。
		"android.permission.BLUETOOTH_SCAN," +
		// Android 12 及以上的连接权限。
		"android.permission.BLUETOOTH_CONNECT," +
		// Android 12 及以上设置可发现状态所需权限。
		"android.permission.BLUETOOTH_ADVERTISE"
)
@ManifestNodes(
	applicationXml =
		"<service" +
		" android:name=\"simple.library.wsd.blebluetooth.BluetoothLeService\"" +
		" android:enabled=\"true\"" +
		" android:exported=\"false\"" +
		" />"
)
public interface 低功耗蓝牙 extends 组件 {

	/* 组件事件 */

	/**
	 * 首次扫描到设备时触发；非 iBeacon 广播的主、次标识均为 0。
	 *
	 * @param name 设备名称，未知时为空文本。
	 * @param address 设备地址。
	 * @param majorId iBeacon 主标识（无符号 16 位整数）。
	 * @param minorId iBeacon 次标识（无符号 16 位整数）。
	 * @param bondState 配对状态：1 未配对、2 正在配对、3 已配对。
	 */
	@SimpleEvent
	void 发现设备(String name, String address, int majorId, int minorId, int bondState);

	/**
	 * 再次扫描到同一设备时通知本次信号强度。
	 *
	 * @param name 设备名称。
	 * @param address 设备地址。
	 * @param rssi 信号强度，单位 dBm。
	 */
	@SimpleEvent
	void 信号强度改变(String name, String address, int rssi);

	/**
	 * 发现远端设备的 GATT 服务后触发。
	 *
	 * @param services 每个项目为文本数组，首项为服务 UUID，其余为该服务的特征 UUID。
	 */
	@SimpleEvent
	void 发现服务(集合 services);

	/**
	 * 连接成功、断开或失败时触发。
	 *
	 * @param state 连接状态：1 已连接、2 已断开或连接失败。
	 */
	@SimpleEvent
	void 连接状态改变(int state);

	/**
	 * 特征读取结束或请求无法提交时触发。
	 *
	 * @param result 结果：1 成功、2 失败。
	 * @param serviceUuid 服务 UUID。
	 * @param characteristicUuid 特征 UUID（原类库称通道 UUID）。
	 * @param data 读取的数据，失败且没有返回数据时为空数组。
	 */
	@SimpleEvent
	void 读取数据完毕(int result, String serviceUuid, String characteristicUuid, byte[] data);

	/**
	 * 特征写入结束或请求无法提交时触发。
	 *
	 * @param result 结果：1 成功、2 失败。
	 */
	@SimpleEvent
	void 写入数据完毕(int result);

	/**
	 * 收到已订阅特征的通知或指示时触发。
	 *
	 * @param serviceUuid 服务 UUID。
	 * @param characteristicUuid 特征 UUID。
	 * @param data 收到的数据。
	 */
	@SimpleEvent
	void 通道数据改变(String serviceUuid, String characteristicUuid, byte[] data);

	/* 组件方法 */

	/**
	 * 准备扫描及绑定 GATT 服务，可重复调用；蓝牙须已开启。
	 * 服务绑定异步完成，紧随其后的连接请求会等待绑定完成。
	 *
	 * @return 支持 BLE、蓝牙已开启且绑定请求成功时返回真，否则返回假。
	 */
	@SimpleFunction
	boolean 初始化BLE();

	/**
	 * 弹出系统可发现请求，使本机可被经典蓝牙设备发现。
	 * 此操作不会启动 BLE 广播。
	 */
	@SimpleFunction
	void 置可被发现();

	/**
	 * 开始扫描 BLE 设备，重复调用不会重复注册扫描。
	 */
	@SimpleFunction
	void 开始搜索();

	/**
	 * 停止扫描，可重复调用。
	 */
	@SimpleFunction
	void 停止搜索();

	/**
	 * 连接指定 BLE 设备；服务尚未绑定时暂存请求。
	 * 连接结果通过连接状态改变事件通知。
	 *
	 * @param address 设备地址。
	 */
	@SimpleFunction
	void 连接设备(String address);

	/**
	 * 取消等待绑定的连接请求，并断开当前连接。
	 * 保留初始化状态，可再次连接设备。
	 */
	@SimpleFunction
	void 断开连接();

	/**
	 * 停止扫描、关闭连接、移除接收器并解除服务绑定。
	 * 可重复调用，释放后可重新初始化。
	 */
	@SimpleFunction
	void 释放资源();

	/**
	 * 检查指定特征是否支持读取。
	 *
	 * @param serviceUuid 服务 UUID。
	 * @param characteristicUuid 特征 UUID。
	 * @return 特征存在且支持读取时返回真，否则返回假。
	 */
	@SimpleFunction
	boolean 是否可读(String serviceUuid, String characteristicUuid);

	/**
	 * 检查指定特征是否支持有响应或无响应写入。
	 *
	 * @param serviceUuid 服务 UUID。
	 * @param characteristicUuid 特征 UUID。
	 * @return 特征存在且支持写入时返回真，否则返回假。
	 */
	@SimpleFunction
	boolean 是否可写(String serviceUuid, String characteristicUuid);

	/**
	 * 检查指定特征是否支持通知或指示。
	 *
	 * @param serviceUuid 服务 UUID。
	 * @param characteristicUuid 特征 UUID。
	 * @return 特征存在且支持通知或指示时返回真，否则返回假。
	 */
	@SimpleFunction
	boolean 是否可通知(String serviceUuid, String characteristicUuid);

	/**
	 * 按顺序读取特征；可通知的特征会先登记订阅。
	 *
	 * @param serviceUuid 服务 UUID。
	 * @param characteristicUuid 特征 UUID。
	 */
	@SimpleFunction
	void 读取数据(String serviceUuid, String characteristicUuid);

	/**
	 * 将数据复制后排入写入队列，结果通过写入数据完毕事件通知。
	 *
	 * @param serviceUuid 服务 UUID。
	 * @param characteristicUuid 特征 UUID。
	 * @param data 待写入数据。
	 */
	@SimpleFunction
	void 写入数据(String serviceUuid, String characteristicUuid, byte[] data);
}
