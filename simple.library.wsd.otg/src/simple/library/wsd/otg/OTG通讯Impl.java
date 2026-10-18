package simple.library.wsd.otg;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import simple.runtime.android.MainActivity;
import simple.runtime.components.组件容器;
import simple.runtime.components.impl.组件Impl;
import simple.runtime.events.EventDispatcher;
import cn.wch.ch34xuartdriver.CH34xUARTDriver;

/**
 * 管理 CH34x USB 串口会话；同步打开只在授权和初始化均完成时返回成功。
 */
public final class OTG通讯Impl extends 组件Impl implements OTG通讯 {

	private final MainActivity activity;
	private final UsbManager manager;
	private final Handler handler = new Handler(Looper.getMainLooper());
	private final String permissionAction;
	private volatile Session current;
	private boolean permissionPending;
	private boolean destroyed;

	/**
	 * 关闭或重开后，旧接收线程和排队事件不能进入新会话。
	 */
	private static final class Session {
		final CH34xUARTDriver driver;
		final int deviceId;
		Thread reader;

		Session(CH34xUARTDriver driver, int deviceId) {
			this.driver = driver;
			this.deviceId = deviceId;
		}
	}

	private final BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (permissionAction.equals(intent.getAction())) {
				permissionPending = false;
			} else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(intent.getAction())) {
				permissionPending = false;
				UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
				Session session = current;

				if (device != null && session != null && session.deviceId == device.getDeviceId()) 关闭设备();
			}
		}
	};

	private final MainActivity.OnDestroyListener destroyListener = new MainActivity.OnDestroyListener() {
		@Override
		public void onDestroy() {
			dispose(false);
			EventDispatcher.unregisterEvent(OTG通讯Impl.this);
		}
	};

	/**
	 * 使用独立授权广播，避免旧驱动在授权尚未完成时初始化空连接。
	 */
	public OTG通讯Impl(组件容器 container) {
		super(container);
		activity = MainActivity.getContext();
		manager = (UsbManager) activity.getSystemService(Context.USB_SERVICE);
		permissionAction = activity.getPackageName() + ".OTG_PERMISSION." + System.identityHashCode(this);
		IntentFilter filter = new IntentFilter(permissionAction);
		filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
		activity.registerReceiver(receiver, filter);
		activity.addOnDestroyListener(destroyListener);
	}

	/* 组件事件 */

	@Override
	public void 收到数据(byte[] data) {
		EventDispatcher.dispatchEvent(this, "收到数据", data);
	}

	/* 组件方法 */

	@Override
	public boolean 是否支持OTG() {
		return activity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_USB_HOST);
	}

	@Override
	public synchronized boolean 打开设备() {
		if (destroyed || manager == null || current != null || !是否支持OTG()) return false;

		// 固定设备遍历顺序，只处理声明支持的 CH34x 设备。
		ArrayList<UsbDevice> devices = new ArrayList<UsbDevice>(manager.getDeviceList().values());
		Collections.sort(devices, new Comparator<UsbDevice>() {
			@Override
			public int compare(UsbDevice left, UsbDevice right) {
				return left.getDeviceName().compareTo(right.getDeviceName());
			}
		});

		for (UsbDevice device : devices) {
			if (device.getVendorId() != 0x1a86 || (device.getProductId() != 0x7523 && device.getProductId() != 0x5523)) continue;
			if (!manager.hasPermission(device)) {
				if (!permissionPending) {
					// API 31 起 USB 授权结果需要可变 PendingIntent；包名约束广播接收范围。
					int flags = PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= 31 ? 0x02000000 : 0);
					PendingIntent permission = PendingIntent.getBroadcast(activity, 0,
						new Intent(permissionAction).setPackage(activity.getPackageName()), flags);
					manager.requestPermission(device, permission);
					permissionPending = true;
				}

				return false;
			}

			// 取得 USB 授权后才打开驱动并创建独立接收会话。
			final CH34xUARTDriver driver = new CH34xUARTDriver(manager, activity, permissionAction);

			try {
				driver.OpenDevice(device);
				if (!driver.isConnected() || !driver.UartInit()) {
					driver.CloseDevice();
					return false;
				}

				// 驱动打开并初始化成功后才公开会话；接收线程始终绑定本次会话。
				final Session session = new Session(driver, device.getDeviceId());
				current = session;
				session.reader = new Thread(new Runnable() {
					@Override
					public void run() {
						read(session);
					}
				}, "Simple-OTG-read");
				session.reader.start();
				return true;
			} catch (RuntimeException exception) {
				current = null;
				driver.CloseDevice();
				return false;
			}
		}

		return false;
	}

	@Override
	public synchronized void 关闭设备() {
		Session session = current;
		current = null;
		if (session != null) {
			session.reader.interrupt();

			synchronized (session) {
				try {
					session.driver.CloseDevice();
				}
				catch (RuntimeException exception) {
					/* 拔出时驱动关闭失败不保留失效会话。 */
				}
			}
		}

		handler.removeCallbacksAndMessages(null);
	}

	/**
	 * 设置波特率，使用 8 数据位、1 停止位、无校验且无流控的串口配置。
	 *
	 * @param baudRate 波特率，须为 CH34x 驱动支持的值。
	 * @throws IllegalStateException 设备未打开或驱动拒绝串口参数。
	 * @throws IllegalArgumentException 波特率不受支持。
	 */
	@Override
	public void 设置参数(int baudRate) {
		设置参数(baudRate, 8, 1, 0);
	}

	@Override
	public void 设置参数(int baudRate, int dataBits, int stopBits, int parity) {
		if (baudRate <= 0 || dataBits < 5 || dataBits > 8 || stopBits < 1 || stopBits > 2 || parity < 0 || parity > 4)
			throw new IllegalArgumentException("串口参数超出支持范围");
		Session session = current;
		if (session == null) return;

		synchronized (session) {
			if (current == session && !session.driver.SetConfig(baudRate, (byte) dataBits, (byte) stopBits, (byte) parity, (byte) 0))
				throw new IllegalStateException("设置串口参数失败");
		}
	}

	@Override
	public int 发送数据(byte[] data) {
		Session session = current;
		if (session == null || data == null || data.length == 0) return 0;

		synchronized (session) {
			return current == session ? session.driver.WriteData(data, data.length) : 0;
		}
	}

	/**
	 * 将全部字节转换为小写十六进制文本，每字节后保留一个空格。
	 *
	 * @param data 待转换的字节数据；空引用返回空文本。
	 * @return 转换后的文本；空引用或空数组返回空文本。
	 */
	@Override
	public String 到十六进制(byte[] data) {
		return 到十六进制(data, data == null ? 0 : data.length);
	}

	@Override
	public String 到十六进制(byte[] data, int length) {
		return HexCodec.encode(data, length);
	}

	@Override
	public byte[] 到字节集(String source) {
		return HexCodec.decode(source);
	}

	/* 生命周期与内部辅助 */

	/**
	 * 驱动读缓冲区不阻塞，无数据时让出 CPU；所有事件回到主线程。
	 */
	private void read(final Session session) {
		byte[] buffer = new byte[512];

		try {
			while (current == session && !Thread.currentThread().isInterrupted()) {
				int length;

				synchronized (session) {
					if (current != session || !session.driver.isConnected()) break;

					length = session.driver.ReadData(buffer, buffer.length);
				}

				if (length > 0) {
					final byte[] received = new byte[length];
					System.arraycopy(buffer, 0, received, 0, length);
					handler.post(new Runnable() {
						@Override
						public void run() {
							if (current == session) 收到数据(received);
						}
					});
				} else {
					Thread.sleep(10);
				}
			}
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
		} catch (RuntimeException exception) {
			// 拔出设备后旧线程只负责退出，不覆盖后续的新会话。
		} finally {
			synchronized (this) {
				if (current == session) 关闭设备();
			}
		}
	}

	private synchronized void dispose(boolean unregister) {
		if (!destroyed) {
			destroyed = true;
			关闭设备();
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
