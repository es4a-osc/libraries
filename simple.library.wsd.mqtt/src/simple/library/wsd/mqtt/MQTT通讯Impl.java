package simple.library.wsd.mqtt;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import simple.runtime.android.MainActivity;
import simple.runtime.components.组件容器;
import simple.runtime.components.impl.组件Impl;
import simple.runtime.events.EventDispatcher;

/**
 * 保留 Paho Android 服务接入；组件拥有当前客户端和清理队列，事件只在主线程分派。
 */
public final class MQTT通讯Impl extends 组件Impl implements MQTT通讯 {

	private final MainActivity activity;
	private final ExecutorService cleanup = Executors.newSingleThreadExecutor();
	private MqttAndroidClient current;
	private IMqttToken connectToken;
	private boolean releasing;
	private long generation;
	private boolean closed;
	private boolean destroyed;

	private final MainActivity.OnDestroyListener destroyListener = new MainActivity.OnDestroyListener() {

		@Override
		public void onDestroy() {
			dispose(false);
			EventDispatcher.unregisterEvent(MQTT通讯Impl.this);
		}
	};

	public MQTT通讯Impl(组件容器 container) {
		super(container);
		activity = MainActivity.getContext();
		activity.addOnDestroyListener(destroyListener);
	}

	/* 组件事件 */

	@Override
	public void 连接成功() {
		EventDispatcher.dispatchEvent(this, "连接成功");
	}

	@Override
	public void 连接失败() {
		EventDispatcher.dispatchEvent(this, "连接失败");
	}

	@Override
	public void 连接断开() {
		EventDispatcher.dispatchEvent(this, "连接断开");
	}

	@Override
	public void 订阅成功() {
		EventDispatcher.dispatchEvent(this, "订阅成功");
	}

	@Override
	public void 订阅失败() {
		EventDispatcher.dispatchEvent(this, "订阅失败");
	}

	@Override
	public void 发送完毕() {
		EventDispatcher.dispatchEvent(this, "发送完毕");
	}

	@Override
	public void 收到消息(String topic, byte[] payload, int qos) {
		EventDispatcher.dispatchEvent(this, "收到消息", topic, payload, qos);
	}

	/* 组件方法 */

	/**
	 * 异步连接服务器，默认不自动重连、使用干净会话，心跳间隔为 60 秒。
	 *
	 * @param address 服务器地址，包含协议、主机和端口。
	 * @param username 登录用户名，可为空文本。
	 * @param password 登录密码，可为空文本。
	 * @param clientId 唯一客户端标识，不得为空。
	 */
	@Override
	public void 连接服务器(String address, String username, String password, String clientId) {
		连接服务器(address, username, password, clientId, false, true, 60);
	}

	/**
	 * 以无用户名、无密码方式异步连接，默认不自动重连、使用干净会话，心跳间隔为 60 秒。
	 *
	 * <p>仅适用于服务器允许匿名连接的情况。</p>
	 *
	 * @param address 服务器地址，包含协议、主机和端口。
	 * @param clientId 唯一客户端标识，不得为空。
	 */
	@Override
	public void 连接服务器(String address, String clientId) {
		连接服务器(address, "", "", clientId, false, true, 60);
	}

	@Override
	public synchronized void 连接服务器(String address, String username, String password, String clientId,
		boolean reconnect, boolean cleanSession, int keepAlive) {
		if (closed || destroyed) return;
		if (current != null || releasing) {
			post(generation, new Runnable() {

				@Override
				public void run() {
					连接失败();
				}
			});
			return;
		}

		final long id = ++generation;

		try {
			if (address == null || !(address.startsWith("tcp://") || address.startsWith("ssl://"))
				|| clientId == null || clientId.length() == 0 || keepAlive < 0 || keepAlive > 65535)
				throw new IllegalArgumentException("MQTT 连接参数无效");

			// 配置连接会话；客户端在网络回调中报告连接结果。
			MqttConnectOptions options = new MqttConnectOptions();
			options.setAutomaticReconnect(reconnect);
			options.setCleanSession(cleanSession);
			options.setKeepAliveInterval(keepAlive);
			options.setConnectionTimeout(15);
			if (username != null && username.length() > 0) options.setUserName(username);
			if (password != null && password.length() > 0) options.setPassword(password.toCharArray());

			// 登记当前客户端，再安装回调，避免回调到达时尚未记录连接身份。
			final MqttAndroidClient client = new MqttAndroidClient(activity.getApplicationContext(), address, clientId);
			current = client;
			client.setCallback(new MqttCallbackExtended() {

				@Override
				public void connectComplete(boolean reconnecting, String serverURI) {
					post(id, new Runnable() {

						@Override
						public void run() {
							连接成功();
						}
					});
				}

				@Override
				public void connectionLost(Throwable cause) {
					post(id, new Runnable() {

						@Override
						public void run() {
							连接断开();
						}
					});
				}

				@Override
				public void messageArrived(final String topic, final MqttMessage message) {
					final byte[] payload = message.getPayload().clone();
					final int qos = message.getQos();

					post(id, new Runnable() {

						@Override
						public void run() {
							收到消息(topic, payload, qos);
						}
					});
				}

				@Override
				public void deliveryComplete(IMqttDeliveryToken token) {
					post(id, new Runnable() {

						@Override
						public void run() {
							发送完毕();
						}
					});
				}
			});

			// 提交异步连接；成功事件统一由 connectComplete 分派。
			connectToken = client.connect(options, null, new IMqttActionListener() {

				@Override
				public void onSuccess(IMqttToken token) {
					// 成功事件由 connectComplete 统一分派；清理线程等待令牌完成后关闭。
				}

				@Override
				public void onFailure(IMqttToken token, Throwable cause) {
					synchronized (MQTT通讯Impl.this) {
						if (generation == id && current == client) {
							current = null;
							release(client, connectToken, null);
							connectToken = null;
						}
					}

					post(id, new Runnable() {

						@Override
						public void run() {
							连接失败();
						}
					});
				}
			});
		} catch (MqttException | RuntimeException exception) {
			MqttAndroidClient failed = current;
			current = null;
			release(failed, connectToken, null);
			connectToken = null;
			post(id, new Runnable() {

				@Override
				public void run() {
					连接失败();
				}
			});
		}
	}

	@Override
	public synchronized boolean 是否已连接() {
		return !closed && !destroyed && current != null && current.isConnected();
	}

	/**
	 * 请求以 QoS 0 订阅主题，通过原有成功或失败事件通知结果。
	 *
	 * @param topic 主题或主题过滤器。
	 */
	@Override
	public void 订阅消息(String topic) {
		订阅消息(topic, 0);
	}

	@Override
	public synchronized void 订阅消息(String topic, int qos) {
		final long id = generation;

		try {
			if (!是否已连接()) throw new IllegalStateException("尚未连接 MQTT 代理");

			current.subscribe(topic, qos, null, new IMqttActionListener() {

				@Override
				public void onSuccess(IMqttToken token) {
					post(id, new Runnable() {

						@Override
						public void run() {
							订阅成功();
						}
					});
				}

				@Override
				public void onFailure(IMqttToken token, Throwable cause) {
					post(id, new Runnable() {

						@Override
						public void run() {
							订阅失败();
						}
					});
				}
			});
		} catch (MqttException | RuntimeException exception) {
			post(id, new Runnable() {

				@Override
				public void run() {
					订阅失败();
				}
			});
		}
	}

	@Override
	public synchronized void 取消订阅(String topic) {
		if (!是否已连接()) throw new IllegalStateException("尚未连接 MQTT 代理");

		try {
			current.unsubscribe(topic);
		}
		catch (MqttException exception) {
			throw new IllegalStateException("MQTT 取消订阅失败", exception);
		}
	}

	/**
	 * 按指定 QoS 发布字节集，不将本次消息保存为保留消息。
	 *
	 * @param topic 消息主题。
	 * @param payload 消息的原始字节数据。
	 * @param qos QoS 等级：0、1 或 2。
	 * @throws IllegalStateException 尚未连接或底层发送失败。
	 */
	@Override
	public void 发送消息(String topic, byte[] payload, int qos) {
		发送消息(topic, payload, qos, false);
	}

	/**
	 * 以 QoS 0 发布消息且不保留；QoS 0 不提供送达确认。
	 *
	 * @param topic 消息主题。
	 * @param payload 消息原始字节。
	 */
	@Override
	public void 发送消息(String topic, byte[] payload) {
		发送消息(topic, payload, 0, false);
	}

	@Override
	public synchronized void 发送消息(String topic, byte[] payload, int qos, boolean retained) {
		if (!是否已连接()) throw new IllegalStateException("尚未连接 MQTT 代理");

		try {
			current.publish(topic, payload, qos, retained);
		}
		catch (MqttException exception) {
			throw new IllegalStateException("MQTT 发送失败", exception);
		}
	}

	@Override
	public synchronized void 断开连接() {
		if (closed || destroyed) return;

		// 先推进会话代次，使旧连接尚未执行的回调失效。
		final long id = ++generation;
		MqttAndroidClient previous = current;

		current = null;
		if (previous != null) release(previous, connectToken, new Runnable() {

			@Override
			public void run() {
				post(id, new Runnable() {

					@Override
					public void run() {
						连接断开();
					}
				});
			}
		});
		connectToken = null;
	}

	@Override
	public synchronized void 关闭服务() {
		if (closed) return;

		closed = true;
		generation++;
		release(current, connectToken, null);
		connectToken = null;
		current = null;
	}

	@Override
	public String 取IMEI码() {
		if (Build.VERSION.SDK_INT < 29) {
			try {
				TelephonyManager manager = (TelephonyManager) activity.getSystemService(Context.TELEPHONY_SERVICE);
				String value = null;
				if (manager != null) {
					if (Build.VERSION.SDK_INT >= 26) {
						if (manager.getPhoneType() == TelephonyManager.PHONE_TYPE_CDMA) {
							value = manager.getMeid();
						} else if (manager.getPhoneType() == TelephonyManager.PHONE_TYPE_GSM) {
							value = manager.getImei();
						}
					} else {
						value = readLegacyDeviceId(manager);
					}
				}

				if (value != null && value.length() > 0) return value;
			} catch (SecurityException exception) {
				/* 无电话权限时使用下面的替代标识。 */
			}
		}

		// 旧系统设备标识不可用时依次使用 ANDROID_ID 和本应用安装标识。
		String value = Settings.Secure.getString(activity.getContentResolver(), Settings.Secure.ANDROID_ID);
		if (value != null && value.length() > 0 && !"9774d56d682e549c".equals(value)) return value;

		// 设备标识不可用时持久保存安装标识，避免重复读取时生成不同的标识。
		SharedPreferences preferences = activity.getSharedPreferences("simple.mqtt.identity", Context.MODE_PRIVATE);
		value = preferences.getString("installation", null);
		if (value == null) {
			value = UUID.randomUUID().toString();
			preferences.edit().putString("installation", value).apply();
		}

		return value;
	}

	/**
	 * API 25 及以下没有分别读取 IMEI、MEID 的接口。
	 *
	 * @param manager 已取得的电话服务
	 * @return 设备标识，受权限及设备能力限制时可能为空
	 */
	@SuppressWarnings("deprecation")
	private static String readLegacyDeviceId(TelephonyManager manager) {
		return manager.getDeviceId();
	}

	/* 生命周期与内部辅助 */

	/**
	 * 清理期间拒绝新连接，避免旧 Android 服务句柄关闭同 ID 的后续连接。
	 *
	 * @param client 底层 FTP 客户端
	 * @param connecting 是否正在连接
	 * @param completed 本次操作是否完成
	 */
	private void release(final MqttAndroidClient client, final IMqttToken connecting, final Runnable completed) {
		if (client == null) return;

		releasing = true;
		cleanup.execute(new Runnable() {

			@Override
			public void run() {
				try {
					if (connecting != null && !connecting.isComplete()) {
						try {
							connecting.waitForCompletion(16000);
						}
						catch (MqttException exception) {
							/* 连接失败也继续释放绑定和客户端。 */
						}
					}

					// 即使网络已断开也请求 disconnect，以停止自动重连并移除服务句柄。
					try {
						client.disconnect().waitForCompletion(3000);
					}
					catch (MqttException | RuntimeException exception) {
						Log.w("SimpleMQTT", "MQTT 断开未正常完成");
					}
				} finally {
					try {
						client.close();
					}
					catch (RuntimeException exception) {
						Log.w("SimpleMQTT", "MQTT 关闭未正常完成");
					}

					try {
						client.unregisterResources();
					}
					finally {
						synchronized (MQTT通讯Impl.this) {
							releasing = false;
						}
					}
				}

				if (completed != null) completed.run();
			}
		});
	}

	/**
	 * 将当前会话的回调投递到主线程，丢弃过期结果。
	 *
	 * @param id 操作或任务标识
	 * @param callback 需要投递的回调
	 */
	private void post(final long id, final Runnable callback) {
		activity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				synchronized (MQTT通讯Impl.this) {
					if (destroyed || closed || generation != id) return;
				}

				callback.run();
			}
		});
	}

	/**
	 * 释放连接、线程及按要求移除生命周期监听器。
	 *
	 * @param unregister 是否注销生命周期监听器
	 */
	private synchronized void dispose(boolean unregister) {
		关闭服务();
		destroyed = true;
		cleanup.shutdown();
		if (unregister) activity.removeOnDestroyListener(destroyListener);
	}

	@Override
	public void 销毁() {
		dispose(true);
		super.销毁();
	}
}
