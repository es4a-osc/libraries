package simple.library.wsd.bluetoothprinter;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import com.qs.helper.printer.BtService;
import com.qs.helper.printer.PrintService;
import com.qs.helper.printer.PrinterClass;
import simple.runtime.android.MainActivity;
import simple.runtime.components.组件容器;
import simple.runtime.components.impl.组件Impl;
import simple.runtime.events.EventDispatcher;

/**
 * 保留原库打印指令；扫描由组件管理，底层 PrinterAPI 的全局连接只允许一个组件占用。
 */
public final class 蓝牙打印机Impl extends 组件Impl implements 蓝牙打印机 {

	private static 蓝牙打印机Impl owner;
	private final MainActivity activity;
	private final BluetoothAdapter adapter;
	private PrinterClass printer;
	private Handler messages;
	private boolean scanning;
	private boolean destroyed;
	private int generation;

	private final BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (destroyed || !scanning) return;
			if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				if (device != null) {
					try {
						发现设备(device.getName() == null ? "" : device.getName(), device.getAddress(), device.getBondState() == BluetoothDevice.BOND_BONDED);
					}
					catch (SecurityException exception) {
						/* 搜索期间权限被撤销，不读取设备信息。 */
					}
				}
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(intent.getAction())) {
				scanning = false;
				搜索完毕();
			}
		}
	};

	private final MainActivity.OnDestroyListener destroyListener = new MainActivity.OnDestroyListener() {
		@Override
		public void onDestroy() {
			dispose(false);
			EventDispatcher.unregisterEvent(蓝牙打印机Impl.this);
		}
	};

	public 蓝牙打印机Impl(组件容器 container) {
		super(container);
		activity = MainActivity.getContext();
		adapter = BluetoothAdapter.getDefaultAdapter();
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		activity.registerReceiver(receiver, filter);
		activity.addOnDestroyListener(destroyListener);
	}

	/* 组件事件 */

	@Override
	public void 搜索完毕() {
		EventDispatcher.dispatchEvent(this, "搜索完毕");
	}

	@Override
	public void 发现设备(String name, String address, boolean bonded) {
		EventDispatcher.dispatchEvent(this, "发现设备", name, address, bonded);
	}

	@Override
	public void 连接成功() {
		EventDispatcher.dispatchEvent(this, "连接成功");
	}

	@Override
	public void 连接失败() {
		EventDispatcher.dispatchEvent(this, "连接失败");
	}

	@Override
	public void 丢失连接() {
		EventDispatcher.dispatchEvent(this, "丢失连接");
	}

	/* 组件方法 */

	@Override
	public void 开启蓝牙() {
		permission(false);
		if (!adapter.isEnabled()) activity.startActivity(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
	}

	@Override
	public void 关闭蓝牙() {
		断开连接();
		activity.startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));
	}

	@Override
	public boolean 是否已开启蓝牙() {
		try {
			permission(false);
			return adapter.isEnabled();
		}
		catch (IllegalStateException | SecurityException exception) {
			return false;
		}
	}

	@Override
	public boolean 是否已连接() {
		return !destroyed && printer != null && printer.getState() == PrinterClass.STATE_CONNECTED;
	}

	@Override
	public void 搜索设备() {
		permission(true);
		if (!adapter.isEnabled()) throw new IllegalStateException("请先开启蓝牙");
		if (scanning) return;

		scanning = true;
		if (!adapter.startDiscovery()) {
			scanning = false;
			搜索完毕();
		}
	}

	@Override
	public void 停止搜索() {
		if (!scanning) return;

		permission(true);
		adapter.cancelDiscovery();

		// 结束事件统一由 ACTION_DISCOVERY_FINISHED 派发，避免重复通知。
	}

	@Override
	public String[][] 取已配对设备() {
		permission(false);
		ArrayList<BluetoothDevice> devices = new ArrayList<BluetoothDevice>(adapter.getBondedDevices());
		Collections.sort(devices, new Comparator<BluetoothDevice>() {
			@Override
			public int compare(BluetoothDevice left, BluetoothDevice right) {
				return left.getAddress().compareTo(right.getAddress());
			}
		});
		String[][] result = new String[devices.size()][2];

		for (int index = 0; index < devices.size(); index++) {
			BluetoothDevice device = devices.get(index);
			result[index][0] = device.getName() == null ? "" : device.getName();
			result[index][1] = device.getAddress();
		}

		return result;
	}

	@Override
	public void 连接设备(String address) {
		try {
			permission(false);
			if (!adapter.isEnabled() || !BluetoothAdapter.checkBluetoothAddress(address)) throw new IllegalArgumentException("无效的蓝牙地址或蓝牙未开启");

			停止搜索();

			// 底层驱动共享静态连接，同一时刻只允许一个组件占用。
			synchronized (蓝牙打印机Impl.class) {
				if (owner != null && owner != this) throw new IllegalStateException("另一个打印组件正在占用连接");

				断开连接();
				owner = this;
				final int id = ++generation;
				messages = new Handler(Looper.getMainLooper()) {
					@Override
					public void handleMessage(Message message) {
						if (destroyed || generation != id) return;
						if (message.what == PrinterClass.MESSAGE_STATE_CHANGE) {
							switch (message.arg1) {
								case PrinterClass.SUCCESS_CONNECT: 连接成功();
								break;
								case PrinterClass.FAILED_CONNECT: 断开连接();
								连接失败();
								break;
								case PrinterClass.LOSE_CONNECT: 断开连接();
								丢失连接();
								break;
								default: break;
							}
						} else if (message.what == PrinterClass.MESSAGE_READ && message.obj instanceof byte[] && message.arg1 > 0) {
							// 沿用打印机的 XOFF/XON 字节约定，更新驱动共享的流控状态。
							byte[] data = (byte[]) message.obj;
							if (data.length > 0 && data[0] == 0x13) PrintService.isFUll = true;
							else if (data.length > 0 && data[0] == 0x11) PrintService.isFUll = false;
						}
					}
				};
				printer = new BtService(activity, messages, new Handler(Looper.getMainLooper()));
				if (!printer.connect(address)) {
					断开连接();
					连接失败();
				}
			}
		} catch (RuntimeException exception) {
			连接失败();
		}
	}

	@Override
	public void 断开连接() {
		synchronized (蓝牙打印机Impl.class) {
			generation++;
			if (messages != null) messages.removeCallbacksAndMessages(null);
			if (owner == this) {
				try {
					if (BtService.mBTService != null) BtService.mBTService.stop();
				}
				catch (RuntimeException exception) {
					/* 权限撤销或底层关闭失败仍须释放组件占用。 */
				}
				finally {
					BtService.mBTService = null;
					owner = null;
					printer = null;
				}
			}
		}
	}

	@Override
	public void 文本(String text) {
		send(text.getBytes(Charset.forName("GBK")));
	}

	@Override
	public void 条码(String text) {
		if (text == null || text.length() == 0 || text.length() > 255) throw new IllegalArgumentException("条码长度须为 1-255");

		for (int index = 0; index < text.length(); index++) if (text.charAt(index) > 127) throw new IllegalArgumentException("条码须为 ASCII");
		byte[] value = text.getBytes(Charset.forName("US-ASCII"));
		send(new byte[] {29, 69, 67, 1, 29, 104, 100, 29, 72, 2, 29, 119, 2, 27, 97, 1});

		// 按 ESC/POS 的 CODE128 命令拼接长度和 ASCII 内容。
		byte[] command = new byte[value.length + 4];
		command[0] = 29;
		command[1] = 107;
		command[2] = 73;
		command[3] = (byte) value.length;
		System.arraycopy(value, 0, command, 4, value.length);
		send(command);
		send(new byte[] {27, 97, 0});
	}

	@Override
	public void 图片(byte[] data, int width, int height) {
		if (data == null || width <= 0 || height <= 0 || width > 4096 || height > 4096) throw new IllegalArgumentException("图片或尺寸无效");

		// 图片解码、缩放及位图编码分步处理，最终释放所有临时图片。
		Bitmap source = BitmapFactory.decodeByteArray(data, 0, data.length);
		if (source == null) throw new IllegalArgumentException("无法解码图片");

		Bitmap scaled = null;

		try {
			scaled = Bitmap.createScaledBitmap(source, width, height, true);
			int[] pixels = new int[width * height];
			scaled.getPixels(pixels, 0, width, 0, 0, width, height);
			send(EscPosImage.encode(pixels, width, height));
		} finally {
			if (scaled != null && scaled != source) scaled.recycle();
			source.recycle();
		}
	}

	@Override
	public void 指令(byte[] data) {
		send(data);
	}

	@Override
	public void 重置设置() {
		send(new byte[] {27, 64});
	}

	@Override
	public void 打印当前内容() {
		send(new byte[] {13});
	}

	@Override
	public void 换行() {
		send(new byte[] {10});
	}

	@Override
	public void 下划线(boolean enabled) {
		send(new byte[] {28, 45, (byte) (enabled ? 1 : 0)});
	}

	@Override
	public void 反白(boolean enabled) {
		send(new byte[] {29, 66, (byte) (enabled ? 1 : 0)});
	}

	@Override
	public void 粗体(boolean enabled) {
		send(new byte[] {27, 69, (byte) (enabled ? 1 : 0)});
	}

	@Override
	public void 行距(int value) {
		send(new byte[] {27, 51, (byte) unsigned(value)});
	}

	@Override
	public void 字体大小(int value) {
		if (value != 1 && value != 2) throw new IllegalArgumentException("字号只接受 1 或 2");

		send(new byte[] {29, 33, (byte) (value == 1 ? 0 : 17)});
	}

	@Override
	public void 字体大小2(int value) {
		send(new byte[] {29, 33, (byte) unsigned(value)});
	}

	@Override
	public void 对齐方式(int value) {
		if (value < 1 || value > 3) throw new IllegalArgumentException("对齐方式只接受 1、2、3");

		send(new byte[] {27, 97, (byte) (value - 1)});
	}

	@Override
	public void 水平制表() {
		send(new byte[] {9});
	}

	@Override
	public void 水平制表值(byte value) {
		send(new byte[] {27, 68, value, 0});
	}

	/* 生命周期与内部辅助 */

	private void permission(boolean scan) {
		if (destroyed || adapter == null) throw new IllegalStateException("组件已销毁或设备不支持蓝牙");
		if (Build.VERSION.SDK_INT >= 23) {
			String connect = Build.VERSION.SDK_INT >= 31 ? "android.permission.BLUETOOTH_CONNECT" : Manifest.permission.BLUETOOTH;
			String discovery = Build.VERSION.SDK_INT >= 31 ? "android.permission.BLUETOOTH_SCAN" : Manifest.permission.ACCESS_FINE_LOCATION;

			if (activity.checkSelfPermission(connect) != PackageManager.PERMISSION_GRANTED
				|| (scan && activity.checkSelfPermission(discovery) != PackageManager.PERMISSION_GRANTED))
				throw new SecurityException("请先通过权限请求组件授予蓝牙通讯或扫描权限");
		}
	}

	private void send(byte[] command) {
		if (!是否已连接()) throw new IllegalStateException("请先连接打印机");
		if (command == null || !printer.write(command)) throw new IllegalStateException("打印指令写入失败");
	}

	private static int unsigned(int value) {
		if (value < 0 || value > 255) throw new IllegalArgumentException("参数范围为 0-255");

		return value;
	}

	private void dispose(boolean unregister) {
		if (!destroyed) {
			destroyed = true;
			if (scanning) {
				try {
					adapter.cancelDiscovery();
				} catch (SecurityException exception) {
					/* 权限可能已撤销。 */
				}
			}

			scanning = false;
			断开连接();
			activity.unregisterReceiver(receiver);
		}

		if (unregister) activity.removeOnDestroyListener(destroyListener);
	}

	@Override
	public void 销毁() {
		dispose(true);
		super.销毁();
	}
}
