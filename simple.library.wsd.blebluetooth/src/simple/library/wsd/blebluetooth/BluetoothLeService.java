package simple.library.wsd.blebluetooth;

import android.annotation.TargetApi;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * 应用内绑定的 GATT 服务；状态修改和操作队列在主线程执行。
 * Android GATT 操作异步完成，每次只提交一项，回调结束后推进队列。
 *
 * @author 树先生 xhwsd@qq.com
 */
@TargetApi(18)
public final class BluetoothLeService extends Service {

	// 服务广播使用独立命名空间，组件只订阅本类库的事件。
	private static final String PREFIX = "simple.library.wsd.blebluetooth.";
	static final String ACTION_GATT_CONNECTED = PREFIX + "CONNECTED";
	static final String ACTION_GATT_DISCONNECTED = PREFIX + "DISCONNECTED";
	static final String ACTION_GATT_SERVICES_DISCOVERED = PREFIX + "SERVICES_DISCOVERED";
	static final String ACTION_DATA_READ = PREFIX + "DATA_READ";
	static final String ACTION_DATA_WRITE = PREFIX + "DATA_WRITE";
	static final String ACTION_DATA_CHANGED = PREFIX + "DATA_CHANGED";

	// 数据、结果和特征身份使用独立的载荷字段。
	static final String EXTRA_DATA = PREFIX + "DATA";
	static final String EXTRA_RESULT = PREFIX + "RESULT";
	static final String SERVICE_UUID = PREFIX + "SERVICE_UUID";
	static final String CHARACTERISTIC_UUID = PREFIX + "CHARACTERISTIC_UUID";

	// 标准客户端特征配置描述符，用于启用通知或指示。
	private static final UUID CLIENT_CONFIGURATION = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

	// 队列操作类型；组件结果仍使用 1 成功、2 失败。
	private static final int READ = 1;
	private static final int WRITE = 2;
	private static final int SUBSCRIBE = 3;
	private static final long OPERATION_TIMEOUT = 10000;

	// 主线程负责会话状态、队列推进和超时处理。
	private final Handler handler = new Handler(Looper.getMainLooper());
	private final IBinder binder = new LocalBinder();

	// 待执行队列与已登记的订阅；activeOperation 表示唯一在途操作。
	private final ArrayDeque<GattOperation> operations = new ArrayDeque<GattOperation>();
	private final Set<String> subscriptions = new HashSet<String>();

	// 当前连接身份用于拒绝已关闭连接发出的迟到回调。
	private BluetoothAdapter adapter;
	private BluetoothGatt gatt;
	private boolean connected;
	private GattOperation activeOperation;

	private final Runnable operationTimeout = new Runnable() {

		@Override
		public void run() {
			if (activeOperation != null) {
				// 超时后关闭连接，防止迟到回调被误认为下一次同特征操作的结果。
				close();
				broadcastUpdate(ACTION_GATT_DISCONNECTED);
			}
		}
	};

	// Android 回调转交主线程后再检查连接身份，避免旧连接修改新会话。
	private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {

		@Override
		public void onConnectionStateChange(final BluetoothGatt source, final int status, final int newState) {
			handler.post(new Runnable() {

				@Override
				public void run() {
					if (source != gatt) {
						return;
					}

					if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED) {
						connected = true;
						broadcastUpdate(ACTION_GATT_CONNECTED);

						// 建立连接后发现服务，特征操作依赖远端返回的服务定义。
						try {
							if (!source.discoverServices()) {
								disconnect();
							}
						} catch (RuntimeException exception) {
							close();
							broadcastUpdate(ACTION_GATT_DISCONNECTED);
						}
					} else if (status != BluetoothGatt.GATT_SUCCESS ||
						newState == BluetoothProfile.STATE_DISCONNECTED) {
						close();
						broadcastUpdate(ACTION_GATT_DISCONNECTED);
					}
				}
			});
		}

		@Override
		public void onServicesDiscovered(final BluetoothGatt source, final int status) {
			handler.post(new Runnable() {

				@Override
				public void run() {
					if (source == gatt && status == BluetoothGatt.GATT_SUCCESS) {
						broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
					}
				}
			});
		}

		@Override
		public void onCharacteristicRead(final BluetoothGatt source,
			final BluetoothGattCharacteristic characteristic, final int status) {
			// 在回调线程取得数据快照，防止后续回调改写特征的共享值。
			final byte[] data = copyValue(characteristic);

			handler.post(new Runnable() {

				@Override
				public void run() {
					completeOperation(source, READ, characteristic, status, data);
				}
			});
		}

		@Override
		public void onCharacteristicWrite(final BluetoothGatt source,
			final BluetoothGattCharacteristic characteristic, final int status) {
			handler.post(new Runnable() {

				@Override
				public void run() {
					completeOperation(source, WRITE, characteristic, status, null);
				}
			});
		}

		@Override
		public void onDescriptorWrite(final BluetoothGatt source,
			final BluetoothGattDescriptor descriptor, final int status) {
			handler.post(new Runnable() {

				@Override
				public void run() {
					if (CLIENT_CONFIGURATION.equals(descriptor.getUuid())) {
						completeOperation(source, SUBSCRIBE, descriptor.getCharacteristic(), status, null);
					}
				}
			});
		}

		@Override
		public void onCharacteristicChanged(final BluetoothGatt source,
			final BluetoothGattCharacteristic characteristic) {
			// 在回调线程取得数据快照，防止后续回调改写特征的共享值。
			final byte[] data = copyValue(characteristic);

			handler.post(new Runnable() {

				@Override
				public void run() {
					if (source == gatt && connected) {
						broadcastData(ACTION_DATA_CHANGED, characteristic, 1, data);
					}
				}
			});
		}
	};

	/* 服务生命周期 */

	/**
	 * 向本应用中的组件提供服务实例。
	 */
	public final class LocalBinder extends Binder {

		/**
		 * 返回当前绑定的服务。
		 *
		 * @return 当前 GATT 服务实例。
		 */
		BluetoothLeService getService() {
			return BluetoothLeService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		close();
		return super.onUnbind(intent);
	}

	@Override
	public void onDestroy() {
		close();
		handler.removeCallbacksAndMessages(null);
		super.onDestroy();
	}

	/* GATT 连接与操作 */

	/**
	 * 取得系统蓝牙适配器，并检查蓝牙是否已开启。
	 *
	 * @return 适配器存在、蓝牙已开启且调用权限有效时返回真。
	 */
	public boolean initialize() {
		BluetoothManager manager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		adapter = manager == null ? null : manager.getAdapter();

		try {
			return adapter != null && adapter.isEnabled();
		} catch (SecurityException exception) {
			return false;
		}
	}

	/**
	 * 关闭旧连接后异步连接指定设备，结果通过连接状态广播通知。
	 *
	 * @param address 设备地址。
	 * @return GATT 连接请求已创建时返回真，不表示连接已经成功。
	 */
	public boolean connect(String address) {
		if (adapter == null || !BluetoothAdapter.checkBluetoothAddress(address)) {
			return false;
		}

		// 先使旧连接及其队列失效，再创建新的 GATT 会话。
		close();

		try {
			BluetoothDevice device = adapter.getRemoteDevice(address);
			if (Build.VERSION.SDK_INT >= 23) {
				gatt = device.connectGatt(this, false, gattCallback, BluetoothDevice.TRANSPORT_LE);
			} else {
				gatt = device.connectGatt(this, false, gattCallback);
			}

			return gatt != null;
		} catch (RuntimeException exception) {
			return false;
		}
	}

	/**
	 * 请求断开当前连接；调用失败时立即关闭会话并通知组件。
	 */
	public void disconnect() {
		if (gatt != null) {
			try {
				gatt.disconnect();
			} catch (RuntimeException exception) {
				close();
				broadcastUpdate(ACTION_GATT_DISCONNECTED);
			}
		}
	}

	/**
	 * 使当前连接失效，清理超时和订阅，并将未完成的读写报告为失败。
	 * 可重复调用，后续旧连接回调不会再更新会话。
	 */
	public void close() {
		// 先切断回调对当前会话的访问，再处理失败结果和系统资源。
		BluetoothGatt previous = gatt;
		gatt = null;
		connected = false;
		handler.removeCallbacks(operationTimeout);
		subscriptions.clear();

		// 所有未完成读写都结束为失败，避免上层等待已经失效的操作。
		if (activeOperation != null) {
			reportResult(activeOperation, 2, null);
			activeOperation = null;
		}

		while (!operations.isEmpty()) {
			reportResult(operations.remove(), 2, null);
		}

		if (previous != null) {
			try {
				previous.close();
			} catch (RuntimeException exception) {
				// 授权撤回时仍清空会话，迟到回调不再影响后续连接。
			}
		}
	}

	/**
	 * 检查指定特征的读取、写入或通知能力。
	 *
	 * @param serviceUuid 服务 UUID。
	 * @param characteristicUuid 特征 UUID。
	 * @param property 能力类型：1 读取、2 写入、3 通知或指示。
	 * @return 特征存在且支持指定能力时返回真，否则返回假。
	 */
	public boolean getProperties(String serviceUuid, String characteristicUuid, int property) {
		BluetoothGattCharacteristic characteristic = findCharacteristic(serviceUuid, characteristicUuid);
		if (characteristic == null) {
			return false;
		}

		int mask;
		if (property == 1) {
			mask = BluetoothGattCharacteristic.PROPERTY_READ;
		} else if (property == 2) {
			mask = BluetoothGattCharacteristic.PROPERTY_WRITE | BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE;
		} else if (property == 3) {
			mask = BluetoothGattCharacteristic.PROPERTY_NOTIFY | BluetoothGattCharacteristic.PROPERTY_INDICATE;
		} else {
			return false;
		}

		return (characteristic.getProperties() & mask) != 0;
	}

	/**
	 * 将特征读取排入队列；特征不存在或不支持读取时立即报告失败。
	 *
	 * @param serviceUuid 服务 UUID。
	 * @param characteristicUuid 特征 UUID。
	 */
	public void readCharacteristic(String serviceUuid, String characteristicUuid) {
		BluetoothGattCharacteristic characteristic = findCharacteristic(serviceUuid, characteristicUuid);
		if (characteristic == null ||
			(characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_READ) == 0) {
			Intent intent = new Intent(ACTION_DATA_READ);
			intent.putExtra(SERVICE_UUID, serviceUuid);
			intent.putExtra(CHARACTERISTIC_UUID, characteristicUuid);
			intent.putExtra(EXTRA_DATA, new byte[0]);
			intent.putExtra(EXTRA_RESULT, 2);
			sendUpdate(intent);
			return;
		}

		// 只登记操作；已有在途请求时由完成回调负责推进。
		operations.add(new GattOperation(READ, characteristic, null, null));
		startNextOperation();
	}

	/**
	 * 复制待写入数据并排入队列，避免调用方修改数组影响尚未执行的写入。
	 *
	 * @param serviceUuid 服务 UUID。
	 * @param characteristicUuid 特征 UUID。
	 * @param data 待写入数据，为 null 时报告失败。
	 */
	public void writeCharacteristic(String serviceUuid, String characteristicUuid, byte[] data) {
		BluetoothGattCharacteristic characteristic = findCharacteristic(serviceUuid, characteristicUuid);
		if (data == null || characteristic == null || !getProperties(serviceUuid, characteristicUuid, 2)) {
			broadcastWriteResult(2);
			return;
		}

		operations.add(new GattOperation(WRITE, characteristic, null, data.clone()));
		startNextOperation();
	}

	/**
	 * 按服务与特征 UUID 登记通知订阅。
	 *
	 * @param serviceUuid 服务 UUID。
	 * @param characteristicUuid 特征 UUID。
	 * @param enabled 真表示登记订阅；假不发起操作。
	 */
	public void setCharacteristicNotification(String serviceUuid, String characteristicUuid, boolean enabled) {
		setCharacteristicNotification(findCharacteristic(serviceUuid, characteristicUuid), enabled);
	}

	/**
	 * 为支持通知或指示的特征登记订阅，仅写入标准 CCCD 描述符。
	 * 同一特征重复登记时不重复提交配置。
	 *
	 * @param characteristic 远端特征，可为 null。
	 * @param enabled 真表示登记订阅；假不发起操作。
	 */
	public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled) {
		if (gatt == null || !connected || characteristic == null || !enabled) {
			return;
		}

		int properties = characteristic.getProperties();
		if ((properties & (BluetoothGattCharacteristic.PROPERTY_NOTIFY |
			BluetoothGattCharacteristic.PROPERTY_INDICATE)) == 0) {
			return;
		}

		// 仅登记有效且尚未订阅的 CCCD，避免无关描述符或重复配置。
		BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CLIENT_CONFIGURATION);
		if (descriptor == null || !subscriptions.add(characteristicKey(characteristic))) {
			return;
		}

		// 优先使用通知；仅支持指示时启用 indication。
		byte[] value = (properties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0
			? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
			: BluetoothGattDescriptor.ENABLE_INDICATION_VALUE;

		operations.add(new GattOperation(SUBSCRIBE, characteristic, descriptor, value.clone()));
		startNextOperation();
	}

	/**
	 * 取得当前连接已发现的服务。
	 *
	 * @return 远端服务列表，未连接时返回 null。
	 */
	public List<BluetoothGattService> getSupportedGattServices() {
		return gatt == null || !connected ? null : gatt.getServices();
	}

	/* 操作队列与事件转换 */

	/**
	 * 根据 UUID 查找当前连接的特征，不存在或 UUID 无效时返回 null。
	 *
	 * @param serviceUuid 服务 UUID。
	 * @param characteristicUuid 特征 UUID。
	 * @return 找到的特征，或 null。
	 */
	private BluetoothGattCharacteristic findCharacteristic(String serviceUuid, String characteristicUuid) {
		if (gatt == null || !connected || serviceUuid == null || characteristicUuid == null) {
			return null;
		}

		try {
			BluetoothGattService service = gatt.getService(UUID.fromString(serviceUuid));
			return service == null ? null : service.getCharacteristic(UUID.fromString(characteristicUuid));
		} catch (IllegalArgumentException exception) {
			return null;
		}
	}

	/**
	 * 没有在途操作时提交队首请求，等待对应回调后再推进。
	 * 提交失败的请求立即报告失败，不阻塞后面的操作。
	 */
	private void startNextOperation() {
		while (activeOperation == null && gatt != null && connected && !operations.isEmpty()) {
			GattOperation operation = operations.remove();
			activeOperation = operation;
			boolean started = false;

			// 系统一次只接收一项操作，写类型按远端特征能力选择。
			try {
				if (operation.type == READ) {
					started = gatt.readCharacteristic(operation.characteristic);
				} else if (operation.type == WRITE) {
					int writeType = (operation.characteristic.getProperties() &
						BluetoothGattCharacteristic.PROPERTY_WRITE) != 0
						? BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
						: BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE;
					operation.characteristic.setWriteType(writeType);
					started = operation.characteristic.setValue(operation.data) &&
						gatt.writeCharacteristic(operation.characteristic);
				} else {
					started = gatt.setCharacteristicNotification(operation.characteristic, true) &&
						operation.descriptor.setValue(operation.data) &&
						gatt.writeDescriptor(operation.descriptor);
				}
			} catch (RuntimeException exception) {
				// 请求未提交时，返回失败并继续后面的独立操作。
			}

			// 提交成功后等待回调；提交失败则释放队首并处理下一项。
			if (started) {
				handler.postDelayed(operationTimeout, OPERATION_TIMEOUT);
				return;
			}

			activeOperation = null;
			reportResult(operation, 2, null);
		}
	}

	/**
	 * 校验回调的连接、操作类型与特征身份，完成匹配操作并推进队列。
	 *
	 * @param source 发出回调的连接。
	 * @param type 回调对应的操作类型。
	 * @param characteristic 回调对应的特征。
	 * @param status Android GATT 返回状态。
	 * @param data 读取结果快照，其它操作为 null。
	 */
	private void completeOperation(BluetoothGatt source, int type,
		BluetoothGattCharacteristic characteristic, int status, byte[] data) {
		if (source != gatt || activeOperation == null || activeOperation.type != type ||
			activeOperation.characteristic != characteristic) {
			return;
		}

		handler.removeCallbacks(operationTimeout);
		GattOperation completed = activeOperation;
		activeOperation = null;
		reportResult(completed, status == BluetoothGatt.GATT_SUCCESS ? 1 : 2, data);

		startNextOperation();
	}

	/**
	 * 转换读写结果为组件广播；订阅失败时撤销登记，以便后续重试。
	 *
	 * @param operation 已结束的操作。
	 * @param result 结果：1 成功、2 失败。
	 * @param data 读取的数据，可为 null。
	 */
	private void reportResult(GattOperation operation, int result, byte[] data) {
		if (operation.type == READ) {
			broadcastData(ACTION_DATA_READ, operation.characteristic, result,
				data == null ? new byte[0] : data);
		} else if (operation.type == WRITE) {
			broadcastWriteResult(result);
		} else if (result != 1) {
			subscriptions.remove(characteristicKey(operation.characteristic));
		}
	}

	/**
	 * 发送不带载荷的服务状态广播。
	 *
	 * @param action 状态事件标识。
	 */
	private void broadcastUpdate(String action) {
		sendUpdate(new Intent(action));
	}

	/**
	 * 发送特征写入结果。
	 *
	 * @param result 结果：1 成功、2 失败。
	 */
	private void broadcastWriteResult(int result) {
		Intent intent = new Intent(ACTION_DATA_WRITE);
		intent.putExtra(EXTRA_RESULT, result);

		sendUpdate(intent);
	}

	/**
	 * 发送包含服务 UUID、特征 UUID 和字节数据的事件。
	 *
	 * @param action 数据事件标识。
	 * @param characteristic 数据所属的特征。
	 * @param result 结果：1 成功、2 失败。
	 * @param data 事件数据。
	 */
	private void broadcastData(String action, BluetoothGattCharacteristic characteristic, int result, byte[] data) {
		Intent intent = new Intent(action);
		intent.putExtra(SERVICE_UUID, characteristic.getService().getUuid().toString());
		intent.putExtra(CHARACTERISTIC_UUID, characteristic.getUuid().toString());
		intent.putExtra(EXTRA_RESULT, result);
		intent.putExtra(EXTRA_DATA, data);

		sendUpdate(intent);
	}

	/**
	 * 将服务广播的接收范围限定为本应用。
	 *
	 * @param intent 待发送的事件。
	 */
	private void sendUpdate(Intent intent) {
		intent.setPackage(getPackageName());

		sendBroadcast(intent);
	}

	/**
	 * 组合服务、特征和实例编号，区分同一连接中的订阅目标。
	 *
	 * @param characteristic 远端特征。
	 * @return 特征的订阅标识。
	 */
	private static String characteristicKey(BluetoothGattCharacteristic characteristic) {
		return characteristic.getService().getUuid() + "/" + characteristic.getUuid() + "/" +
			characteristic.getInstanceId();
	}

	/**
	 * 复制特征当前数据，避免主线程处理事件前共享值已经变化。
	 *
	 * @param characteristic 提供数据的特征。
	 * @return 数据快照，特征没有值时返回空数组。
	 */
	private static byte[] copyValue(BluetoothGattCharacteristic characteristic) {
		byte[] value = characteristic.getValue();

		return value == null ? new byte[0] : value.clone();
	}

	/**
	 * 保存单次 GATT 操作的目标与数据快照。
	 */
	private static final class GattOperation {
		private final int type;
		private final BluetoothGattCharacteristic characteristic;
		private final BluetoothGattDescriptor descriptor;
		private final byte[] data;

		/**
		 * 创建待读取、写入或订阅的操作。
		 *
		 * @param type 操作类型。
		 * @param characteristic 目标特征。
		 * @param descriptor 订阅所用的 CCCD，其它操作为 null。
		 * @param data 待写入数据或订阅配置，读取时为 null。
		 */
		private GattOperation(int type, BluetoothGattCharacteristic characteristic,
			BluetoothGattDescriptor descriptor, byte[] data) {
			this.type = type;
			this.characteristic = characteristic;
			this.descriptor = descriptor;
			this.data = data;
		}
	}
}
