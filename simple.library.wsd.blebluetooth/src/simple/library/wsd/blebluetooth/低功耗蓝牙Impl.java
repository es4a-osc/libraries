package simple.library.wsd.blebluetooth;

import simple.runtime.android.MainActivity;
import simple.runtime.collections.集合;
import simple.runtime.components.组件容器;
import simple.runtime.components.impl.组件Impl;
import simple.runtime.events.EventDispatcher;
import simple.runtime.variants.ArrayVariant;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 将 BLE 扫描、GATT 服务回调转换为 Simple 事件。
 * 扫描与服务绑定分别登记，释放只撤销本组件实际取得的资源。
 *
 * @author 树先生 xhwsd@qq.com
 */
@TargetApi(18)
public final class 低功耗蓝牙Impl extends 组件Impl implements 低功耗蓝牙 {

	// 所属 Activity 与当前扫描轮次发现的设备。
	private final MainActivity activity;
	private final Set<String> discoveredAddresses = new HashSet<String>();

	// 扫描、绑定与接收器分别登记，释放时按实际取得的资源清理。
	private BluetoothAdapter adapter;
	private BluetoothLeService service;
	private boolean scanning;
	private ScanSession scanSession;
	private boolean bound;
	private boolean receiverRegistered;
	private boolean destroyed;

	// 区分重新开始的扫描，并暂存服务绑定完成前的连接请求。
	private volatile int scanGeneration;
	private String pendingAddress;

	// Activity 销毁时停止回调与资源使用，保留监听器列表的遍历稳定性。
	private final MainActivity.OnDestroyListener destroyListener = new MainActivity.OnDestroyListener() {

		@Override
		public void onDestroy() {
			// MainActivity 正在遍历销毁监听器，不能在回调内修改其列表。
			destroyed = true;
			释放资源();
		}
	};

	// 绑定成功后处理等待连接的地址，服务异常断开时清空会话引用。
	private final ServiceConnection serviceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder binder) {
			if (!bound || destroyed) {
				return;
			}

			service = ((BluetoothLeService.LocalBinder) binder).getService();
			if (!service.initialize()) {
				释放资源();
				连接状态改变(2);
				return;
			}

			if (pendingAddress != null) {
				String address = pendingAddress;
				pendingAddress = null;
				connectDevice(address);
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			service = null;
			pendingAddress = null;
			if (!destroyed) {
				连接状态改变(2);
			}
		}
	};

	// 将服务发出的应用内广播转换为组件事件。
	private final BroadcastReceiver gattReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (!receiverRegistered || destroyed || intent == null) {
				return;
			}

			String action = intent.getAction();
			if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
				连接状态改变(1);
			} else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
				连接状态改变(2);
			} else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
				if (service != null) {
					displayGattServices(service.getSupportedGattServices());
				}
			} else if (BluetoothLeService.ACTION_DATA_READ.equals(action)) {
				读取数据完毕(intent.getIntExtra(BluetoothLeService.EXTRA_RESULT, 2),
					intent.getStringExtra(BluetoothLeService.SERVICE_UUID),
					intent.getStringExtra(BluetoothLeService.CHARACTERISTIC_UUID),
					intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA));
			} else if (BluetoothLeService.ACTION_DATA_WRITE.equals(action)) {
				写入数据完毕(intent.getIntExtra(BluetoothLeService.EXTRA_RESULT, 2));
			} else if (BluetoothLeService.ACTION_DATA_CHANGED.equals(action)) {
				通道数据改变(intent.getStringExtra(BluetoothLeService.SERVICE_UUID),
					intent.getStringExtra(BluetoothLeService.CHARACTERISTIC_UUID),
					intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA));
			}
		}
	};

	/**
	 * 只接收启动时捕获的扫描代次，旧回调不能混入重新开始后的设备列表。
	 *
	 * @param device 扫描到的设备
	 * @param rssi 信号强度
	 * @param scanRecord 广播原始数据
	 * @param generation 所属扫描代次
	 */
	private void deliverScan(final BluetoothDevice device, final int rssi, byte[] scanRecord, final int generation) {
		if (device == null) {
			return;
		}

		final int[] beaconIds = parseBeaconIds(scanRecord);

		activity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (!scanning || destroyed || generation != scanGeneration) {
					return;
				}

				try {
					String address = device.getAddress();
					String name = device.getName();

					if (name == null) {
						name = "";
					}

					if (discoveredAddresses.add(address)) {
						int bondState = 1;
						if (device.getBondState() == BluetoothDevice.BOND_BONDING) {
							bondState = 2;
						} else if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
							bondState = 3;
						}

						发现设备(name, address, beaconIds[0], beaconIds[1], bondState);
					} else {
						信号强度改变(name, address, rssi);
					}
				} catch (SecurityException exception) {
					failScan(generation, -2);
				}
			}
		});
	}

	/**
	 * 创建不可视组件，并订阅所属 Activity 的销毁通知。
	 *
	 * @param container 容纳不可视组件的窗口。
	 */
	public 低功耗蓝牙Impl(组件容器 container) {
		super(container);
		activity = MainActivity.getContext();
		activity.addOnDestroyListener(destroyListener);
	}

	/* 组件事件 */

	@Override
	public void 发现设备(String name, String address, int majorId, int minorId, int bondState) {
		EventDispatcher.dispatchEvent(this, "发现设备", name, address, majorId, minorId, bondState);
	}

	@Override
	public void 信号强度改变(String name, String address, int rssi) {
		EventDispatcher.dispatchEvent(this, "信号强度改变", name, address, rssi);
	}

	@Override
	public void 发现服务(集合 services) {
		EventDispatcher.dispatchEvent(this, "发现服务", services);
	}

	@Override
	public void 连接状态改变(int state) {
		EventDispatcher.dispatchEvent(this, "连接状态改变", state);
	}

	@Override
	public void 读取数据完毕(int result, String serviceUuid, String characteristicUuid, byte[] data) {
		EventDispatcher.dispatchEvent(this, "读取数据完毕", result, serviceUuid, characteristicUuid, data);
	}

	@Override
	public void 写入数据完毕(int result) {
		EventDispatcher.dispatchEvent(this, "写入数据完毕", result);
	}

	@Override
	public void 通道数据改变(String serviceUuid, String characteristicUuid, byte[] data) {
		EventDispatcher.dispatchEvent(this, "通道数据改变", serviceUuid, characteristicUuid, data);
	}

	/* 组件方法 */

	@Override
	public boolean 初始化BLE() {
		if (destroyed || Build.VERSION.SDK_INT < 18 ||
			!activity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
			return false;
		}

		if (bound) {
			return true;
		}

		try {
			// 确认蓝牙适配器可用；初始化不会自动开启系统蓝牙。
			BluetoothManager manager = (BluetoothManager) activity.getSystemService(Context.BLUETOOTH_SERVICE);
			adapter = manager == null ? null : manager.getAdapter();
			if (adapter == null || !adapter.isEnabled()) {
				return false;
			}

			// 先接收事件，再发起异步服务绑定；失败时统一释放已取得的资源。
			activity.registerReceiver(gattReceiver, makeGattUpdateIntentFilter());
			receiverRegistered = true;

			bound = activity.bindService(new Intent(activity, BluetoothLeService.class),
				serviceConnection, Context.BIND_AUTO_CREATE);
			if (!bound) {
				释放资源();
			}

			return bound;
		} catch (RuntimeException exception) {
			释放资源();
			return false;
		}
	}

	@Override
	public void 置可被发现() {
		if (destroyed || Build.VERSION.SDK_INT < 18) {
			return;
		}

		Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);

		try {
			activity.startActivity(intent);
		} catch (RuntimeException exception) {
			// 系统未提供处理页面或权限不足时，不破坏现有 BLE 连接。
		}
	}

	@Override
	public String[] 取扫描权限() {
		// 编译 API 30 不包含新权限常量，使用权限名称字符串兼容更高版本系统。
		if (Build.VERSION.SDK_INT >= 31) {
			return new String[]{
				"android.permission.ACCESS_FINE_LOCATION",
				"android.permission.ACCESS_COARSE_LOCATION",
				"android.permission.BLUETOOTH_SCAN",
				"android.permission.BLUETOOTH_CONNECT"
			};
		}
		if (Build.VERSION.SDK_INT >= 23) {
			return new String[]{"android.permission.ACCESS_FINE_LOCATION"};
		}
		return new String[0];
	}

	@Override
	public void 开始搜索() {
		if (destroyed || scanning) {
			return;
		}

		final int token = ++scanGeneration;
		if (!bound || adapter == null) {
			failScan(token, -1);
			return;
		}
		discoveredAddresses.clear();
		scanning = true;
		try {
			scanSession = Build.VERSION.SDK_INT >= 21 ? new ModernScan(token) : new LegacyScan(token);
			if (!scanSession.start()) {
				failScan(token, -1);
			}
		} catch (SecurityException exception) {
			failScan(token, -2);
		} catch (RuntimeException exception) {
			failScan(token, -3);
		}
	}

	@Override
	public void 停止搜索() {
		// 先使已排队的回调失效，再释放本轮实际取得的扫描会话。
		scanning = false;
		scanGeneration++;
		ScanSession previous = scanSession;
		scanSession = null;
		if (previous != null) {
			try {
				previous.stop();
			} catch (RuntimeException exception) {
				// 蓝牙关闭或授权撤回后，组件仍保持停止状态。
			}
		}
	}

	@Override
	public void 搜索失败(int errorCode) {
		EventDispatcher.dispatchEvent(this, "搜索失败", errorCode);
	}

	/**
	 * 异步报告本轮失败，调用方有机会完成提交请求后的界面状态更新。
	 *
	 * @param token 请求所属扫描代次
	 * @param code 系统错误码或组件负值错误码
	 */
	private void failScan(final int token, final int code) {
		activity.getWindow().getDecorView().post(new Runnable() {
			@Override
			public void run() {
				if (!destroyed && token == scanGeneration) {
					停止搜索();
					搜索失败(code);
				}
			}
		});
	}

	/**
	 * 隔离新旧扫描类型，低版本仅加载实际使用的实现。
	 */
	private interface ScanSession {
		/**
		 * 读取扫描请求是否已提交。
		 *
		 * @return 扫描请求是否已提交
		 */
		boolean start();

		/**
		 * 停止本会话的扫描。
		 */
		void stop();
	}

	/**
	 * API 21 起使用 BluetoothLeScanner，处理单条、批量结果及异步启动失败。
	 */
	private final class ModernScan extends android.bluetooth.le.ScanCallback implements ScanSession {
		private final int token;
		private final android.bluetooth.le.BluetoothLeScanner scanner;

		/**
		 * 固定本轮扫描器和代次，不复用上轮回调。
		 *
		 * @param token 当前扫描代次
		 */
		ModernScan(int token) {
			this.token = token;
			scanner = adapter.getBluetoothLeScanner();
		}

		@Override
		public boolean start() {
			if (scanner == null) {
				return false;
			}
			scanner.startScan(this);
			return true;
		}

		@Override
		public void stop() {
			if (scanner != null) {
				scanner.stopScan(this);
			}
		}

		@Override
		public void onScanResult(int callbackType, android.bluetooth.le.ScanResult result) {
			if (result != null) {
				android.bluetooth.le.ScanRecord record = result.getScanRecord();
				deliverScan(result.getDevice(), result.getRssi(), record == null ? null : record.getBytes(), token);
			}
		}

		@Override
		public void onBatchScanResults(List<android.bluetooth.le.ScanResult> results) {
			for (android.bluetooth.le.ScanResult result : results) {
				onScanResult(0, result);
			}
		}

		@Override
		public void onScanFailed(int errorCode) {
			failScan(token, errorCode);
		}
	}

	/**
	 * API 18 至 20 没有 BluetoothLeScanner，继续使用该版本可用的扫描 API。
	 */
	private final class LegacyScan implements ScanSession, BluetoothAdapter.LeScanCallback {
		private final int token;

		/**
		 * 固定本轮旧版扫描的回调代次。
		 *
		 * @param token 当前扫描代次
		 */
		LegacyScan(int token) {
			this.token = token;
		}

		@Override
		@SuppressWarnings("deprecation")
		public boolean start() {
			return adapter.startLeScan(this);
		}

		@Override
		@SuppressWarnings("deprecation")
		public void stop() {
			adapter.stopLeScan(this);
		}

		@Override
		public void onLeScan(BluetoothDevice device, int rssi, byte[] record) {
			deliverScan(device, rssi, record, token);
		}
	}

	@Override
	public void 连接设备(String address) {
		if (destroyed || !bound || !BluetoothAdapter.checkBluetoothAddress(address)) {
			if (!destroyed) {
				连接状态改变(2);
			}

			return;
		}

		// 连接前停止扫描，未绑定完成时只保留最后一次连接请求。
		停止搜索();

		if (service == null) {
			pendingAddress = address;
		} else {
			connectDevice(address);
		}
	}

	@Override
	public void 断开连接() {
		pendingAddress = null;

		if (service != null) {
			service.disconnect();
		}
	}

	@Override
	public void 释放资源() {
		// 先停止扫描并取消等待绑定的请求，阻止后续回调重新发起连接。
		停止搜索();
		pendingAddress = null;
		discoveredAddresses.clear();

		// 关闭 GATT 会话后再撤销接收器与绑定。
		if (service != null) {
			service.close();
			service = null;
		}

		if (receiverRegistered) {
			receiverRegistered = false;

			try {
				activity.unregisterReceiver(gattReceiver);
			} catch (IllegalArgumentException exception) {
				// Activity 已清理接收器时，仍继续解除服务绑定。
			}
		}

		if (bound) {
			bound = false;

			try {
				activity.unbindService(serviceConnection);
			} catch (IllegalArgumentException exception) {
				// 绑定已被系统撤销，不重复释放。
			}
		}

		adapter = null;
	}

	@Override
	public boolean 是否可读(String serviceUuid, String characteristicUuid) {
		return service != null && service.getProperties(serviceUuid, characteristicUuid, 1);
	}

	@Override
	public boolean 是否可写(String serviceUuid, String characteristicUuid) {
		return service != null && service.getProperties(serviceUuid, characteristicUuid, 2);
	}

	@Override
	public boolean 是否可通知(String serviceUuid, String characteristicUuid) {
		return service != null && service.getProperties(serviceUuid, characteristicUuid, 3);
	}

	@Override
	public void 读取数据(String serviceUuid, String characteristicUuid) {
		if (service == null) {
			读取数据完毕(2, serviceUuid, characteristicUuid, new byte[0]);
			return;
		}

		// 订阅与读取使用同一 GATT 队列，避免配置尚未完成就提交读取。
		service.setCharacteristicNotification(serviceUuid, characteristicUuid, true);
		service.readCharacteristic(serviceUuid, characteristicUuid);
	}

	@Override
	public void 写入数据(String serviceUuid, String characteristicUuid, byte[] data) {
		if (service == null) {
			写入数据完毕(2);
			return;
		}

		service.writeCharacteristic(serviceUuid, characteristicUuid, data);
	}

	/* 生命周期与内部辅助 */

	@Override
	public void 销毁() {
		dispose();
		super.销毁();
	}

	/**
	 * 结束组件生命周期，并从 Activity 移除销毁监听器。
	 */
	private void dispose() {
		if (!destroyed) {
			destroyed = true;
			释放资源();
		}

		activity.removeOnDestroyListener(destroyListener);
	}

	/**
	 * 向已经绑定的服务发起连接，提交失败时通知组件。
	 *
	 * @param address 设备地址。
	 */
	private void connectDevice(String address) {
		if (!service.connect(address)) {
			连接状态改变(2);
		}
	}

	/**
	 * 创建本组件订阅的 GATT 事件过滤器。
	 *
	 * @return 仅包含本类库服务事件的过滤器。
	 */
	private static IntentFilter makeGattUpdateIntentFilter() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
		filter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
		filter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
		filter.addAction(BluetoothLeService.ACTION_DATA_READ);
		filter.addAction(BluetoothLeService.ACTION_DATA_CHANGED);
		filter.addAction(BluetoothLeService.ACTION_DATA_WRITE);

		return filter;
	}

	/**
	 * 将服务与特征 UUID 组织为 Simple 集合，并登记支持的通知订阅。
	 * 直接构造数组，避免使用带正则元字符的文本分隔符。
	 *
	 * @param services 远端 GATT 服务列表，为 null 时不触发事件。
	 */
	private void displayGattServices(List<BluetoothGattService> services) {
		if (services == null) {
			return;
		}

		集合 items = new 集合();

		for (BluetoothGattService gattService : services) {
			List<BluetoothGattCharacteristic> characteristics = gattService.getCharacteristics();
			String[] uuids = new String[characteristics.size() + 1];

			uuids[0] = gattService.getUuid().toString();

			// 首项保留服务 UUID，后续项目按设备提供的顺序存放特征 UUID。
			for (int index = 0; index < characteristics.size(); index++) {
				BluetoothGattCharacteristic characteristic = characteristics.get(index);
				uuids[index + 1] = characteristic.getUuid().toString();
				service.setCharacteristicNotification(characteristic, true);
			}

			items.加入(ArrayVariant.getArrayVariant(uuids));
		}

		发现服务(items);
	}

	/**
	 * 按广播 AD 字段寻找 Apple iBeacon 标识，不依赖字段的固定位置。
	 *
	 * @param record 原始扫描记录，可为 null。
	 * @return 主、次标识组成的数组；未找到完整 iBeacon 字段时均为 0。
	 */
	static int[] parseBeaconIds(byte[] record) {
		if (record != null) {
			for (int offset = 0; offset < record.length;) {
				int length = record[offset] & 0xff;
				int end = offset + length + 1;

				if (length == 0 || end > record.length) {
					break;
				}

				// 厂商字段须同时满足长度、Apple 公司标识和 iBeacon 类型标识。
				int payload = offset + 2;
				if (length >= 26 && (record[offset + 1] & 0xff) == 0xff &&
					(record[payload] & 0xff) == 0x4c && record[payload + 1] == 0 &&
					record[payload + 2] == 2 && (record[payload + 3] & 0xff) == 0x15) {
					return new int[] {
						((record[payload + 20] & 0xff) << 8) | (record[payload + 21] & 0xff),
						((record[payload + 22] & 0xff) << 8) | (record[payload + 23] & 0xff)
					};
				}

				offset = end;
			}
		}

		return new int[] { 0, 0 };
	}
}
