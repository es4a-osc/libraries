package simple.library.wsd.mqttclient;

import android.os.Handler;
import android.os.Looper;
import android.util.AtomicFile;
import android.util.Base64;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;
import org.json.JSONArray;
import org.json.JSONObject;

import simple.runtime.android.MainActivity;
import simple.runtime.components.组件容器;
import simple.runtime.components.impl.组件Impl;
import simple.runtime.events.EventDispatcher;

/**
 * MQTT 客户端实现。主线程维护操作状态，网络由 Paho 执行，阻塞清理放入专用线程。
 * 自动重连和离线队列由组件持有，避免关闭后仍存在重试或无法关联的队列令牌。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class MQTT客户端Impl extends 组件Impl implements MQTT客户端, MainActivity.OnDestroyListener {

	/* 内部状态 */

	/** 事件所属的活动及应用私有目录入口。 */
	private final MainActivity activity;
	/** 本组件专用的主线程调度器，不移除其它组件的任务。 */
	private final Handler handler = new Handler(Looper.getMainLooper());
	/** 执行强制断开和关闭操作的串行线程。 */
	private final ExecutorService cleanup = Executors.newSingleThreadExecutor();
	/** 当前客户端，仅在关闭完成后替换。 */
	private MqttAsyncClient client;
	/** 初次连接或重连使用的参数副本。 */
	private MqttConnectOptions connectionOptions;
	/** 尚未完成的底层连接令牌，关闭时在后台等待它结束。 */
	private IMqttToken connectingToken;
	/** 当前客户端会话代次，用于丢弃关闭前的网络回调。 */
	private long generation;
	/** 本组件单调递增的操作序号。 */
	private long sequence;
	/** 客户端状态，见 MQTT常量。 */
	private int state;
	/** 组件是否已经销毁。 */
	private boolean destroyed;
	/** 当前客户端是否至少连接成功过一次。 */
	private boolean everConnected;
	/** 当前连接是否允许自动重连。 */
	private boolean retryEnabled;
	/** 重试退避间隔，范围为 1 至 128 秒。 */
	private int retryDelay = 1000;
	/** 是否启用协议数据持久存储，创建客户端时固定。 */
	private boolean persistent;
	/** 提交时复制的缓冲配置。 */
	private MQTT缓冲选项 bufferOptions = new MQTT缓冲选项();
	/** 按提交顺序持有的离线消息，包括正在补发的队首。 */
	private final ArrayList<QueuedMessage> queue = new ArrayList<>();
	/** 尚未发出终态事件的操作，用于关闭时明确取消。 */
	private final Set<MQTT令牌> pending = new LinkedHashSet<>();
	/** 当前客户端的离线队列持久文件，不包含连接密码。 */
	private AtomicFile bufferFile;
	/** 当前客户端的持久队列是否已完整加载，加载失败时保留重试条件。 */
	private boolean queueRestored;
	/** 已排定的重连任务，主动断开和销毁时移除。 */
	private Runnable retryTask;

	/* 构造与内部方法 */

	/**
	 * 建立组件并绑定宿主销毁通知。
	 *
	 * @param container 组件所属容器
	 */
	public MQTT客户端Impl(组件容器 container) {
		super(container);
		activity = MainActivity.getContext();
		activity.addOnDestroyListener(this);
	}

	/**
	 * 校验调用线程和生命周期。
	 *
	 * @throws IllegalStateException 非主线程调用或组件已经销毁
	 */
	private void check() {
		if (Looper.myLooper() != Looper.getMainLooper() || destroyed) {
			throw new IllegalStateException("MQTT组件须在主线程使用，且不能已经销毁");
		}
	}

	/**
	 * 拒绝连接中或清理中的冲突请求。
	 *
	 * @throws IllegalStateException 当前不能提交新操作
	 */
	private void idle() {
		check();
		if (state == MQTT常量.状态_连接中 || state == MQTT常量.状态_断开中 || state == MQTT常量.状态_关闭中) {
			throw new IllegalStateException("请等待当前连接或清理操作结束");
		}
	}

	/**
	 * 为一项异步操作建立可关联的令牌。
	 *
	 * @param operation 操作名称
	 * @param tag 调用方标记
	 * @param topics 目标主题
	 * @return 新的未完成令牌
	 */
	private MQTT令牌 token(String operation, String tag, String... topics) {
		MQTT令牌 result = new MQTT令牌(++sequence, operation, tag, topics);
		pending.add(result);
		return result;
	}

	/**
	 * 将当前客户端的结果排入主线程；即使回调来自主线程，也延后至调用返回后执行。
	 *
	 * @param epoch 客户端代次
	 * @param action 待执行的状态修改和事件
	 */
	private void post(long epoch, Runnable action) {
		handler.post(() -> {
			if (!destroyed && generation == epoch) {
				action.run();
			}
		});
	}

	/**
	 * 只发送一次操作终态事件，并复制底层确认信息。
	 *
	 * @param token 关联令牌
	 * @param nativeToken 底层令牌，可为空
	 * @param error 失败原因，成功时为空
	 */
	private void finish(MQTT令牌 token, IMqttToken nativeToken, Throwable error) {
		if (token.done) {
			return;
		}
		if (nativeToken != null) {
			token.messageId = nativeToken.getMessageId();
			int[] granted = nativeToken.getGrantedQos();
			token.granted = granted == null ? new int[0] : granted.clone();
		}
		token.done = true;
		token.success = error == null;
		token.error = error == null ? null : new MQTT错误(error);
		pending.remove(token);
		if (error == null) {
			操作成功(token);
		} else {
			操作失败(token);
		}
	}

	/**
	 * 统一异步操作回调，不在网络线程执行 Simple 事件。
	 *
	 * @param epoch 客户端代次
	 * @param token 操作令牌
	 * @param success 成功时的状态更新
	 * @param failure 失败时的状态更新
	 * @return 底层异步操作监听器
	 */
	private IMqttActionListener listener(long epoch, MQTT令牌 token, Runnable success, Runnable failure) {
		return new IMqttActionListener() {
			@Override
			public void onSuccess(IMqttToken result) {
				post(epoch, () -> {
					if (success != null) {
						success.run();
					}
					finish(token, result, null);
				});
			}

			@Override
			public void onFailure(IMqttToken result, Throwable error) {
				post(epoch, () -> {
					if (failure != null) {
						failure.run();
					}
					finish(token, result, error);
				});
			}
		};
	}

	/**
	 * 撤销已排定的重连，不影响其它主线程任务。
	 */
	private void cancelRetry() {
		if (retryTask != null) {
			handler.removeCallbacks(retryTask);
			retryTask = null;
		}
	}

	/**
	 * 对成功连接后发生的异常掉线执行指数退避重试。
	 */
	private void scheduleRetry() {
		if (!retryEnabled || !everConnected || destroyed || state != MQTT常量.状态_已断开) {
			return;
		}
		cancelRetry();
		retryTask = () -> {
			retryTask = null;
			if (retryEnabled && state == MQTT常量.状态_已断开) {
				startConnect(token("自动重连", "自动重连"));
			}
		};
		handler.postDelayed(retryTask, retryDelay);
		retryDelay = Math.min(retryDelay * 2, 128000);
	}

	/**
	 * 使用已复制参数提交初次连接或重新连接。
	 *
	 * @param request 连接操作的关联令牌
	 */
	private void startConnect(MQTT令牌 request) {
		state = MQTT常量.状态_连接中;
		long epoch = generation;
		try {
			connectingToken = client.connect(connectionOptions, null, listener(epoch, request,
				() -> state = MQTT常量.状态_已连接,
				() -> {
					state = MQTT常量.状态_已断开;
					scheduleRetry();
				}));
		} catch (MqttException | RuntimeException error) {
			state = MQTT常量.状态_已断开;
			post(epoch, () -> finish(request, null, error));
			scheduleRetry();
		}
	}

	/**
	 * 安装当前客户端的主线程回调，消息在网络线程立即复制。
	 *
	 * @param epoch 客户端代次
	 */
	private void installCallbacks(long epoch) {
		client.setCallback(new MqttCallbackExtended() {
			@Override
			public void connectComplete(boolean reconnect, String serverURI) {
				post(epoch, () -> {
					boolean again = everConnected;
					everConnected = true;
					state = MQTT常量.状态_已连接;
					retryDelay = 1000;
					连接完成(again, serverURI);
					drain();
				});
			}

			@Override
			public void connectionLost(Throwable cause) {
				post(epoch, () -> {
					if (state == MQTT常量.状态_断开中 || state == MQTT常量.状态_关闭中) {
						return;
					}
					state = MQTT常量.状态_已断开;
					连接丢失(new MQTT错误(cause));
					scheduleRetry();
				});
			}

			@Override
			public void messageArrived(String topic, MqttMessage message) {
				MQTT消息 copy = new MQTT消息(message);
				post(epoch, () -> 收到消息(topic, copy));
			}

			@Override
			public void deliveryComplete(IMqttDeliveryToken nativeToken) {
				// 每次发布的 action listener 是唯一完成入口，避免同一消息重复发出完成事件。
			}
		});
	}

	/**
	 * 原子保存离线队列；应用异常退出后补发仍可能重复，业务须自行去重。
	 *
	 * @throws Exception 无法保存队列
	 */
	private void persistQueue() throws Exception {
		if (bufferFile == null || !bufferOptions.持久化()) {
			return;
		}
		JSONArray data = new JSONArray();
		for (QueuedMessage entry : queue) {
			MQTT消息 message = entry.token.消息();
			data.put(new JSONObject().put("topic", entry.topic).put("tag", entry.token.tag)
				.put("payload", Base64.encodeToString(message.内容(), Base64.NO_WRAP))
				.put("qos", message.QoS()).put("retained", message.保留()));
		}
		FileOutputStream stream = bufferFile.startWrite();
		try {
			stream.write(data.toString().getBytes(StandardCharsets.UTF_8));
			bufferFile.finishWrite(stream);
		} catch (Exception error) {
			bufferFile.failWrite(stream);
			throw error;
		}
	}

	/**
	 * 为同一个服务器和客户端恢复已保存的离线队列。
	 *
	 * @throws Exception 队列损坏或不符合当前容量限制
	 */
	private void restoreQueue() throws Exception {
		if (!bufferOptions.启用() || !bufferOptions.持久化() || !bufferFile.getBaseFile().exists()) {
			return;
		}
		JSONArray data = new JSONArray(new String(bufferFile.readFully(), StandardCharsets.UTF_8));
		if (data.length() > bufferOptions.容量()) {
			throw new IllegalStateException("已保存的离线队列超过当前容量，未删除原文件");
		}
		ArrayList<QueuedMessage> restored = new ArrayList<>();
		for (int index = 0; index < data.length(); index++) {
			JSONObject item = data.getJSONObject(index);
			String topic = item.getString("topic");
			MqttTopic.validate(topic, false);
			MQTT消息 message = new MQTT消息();
			message.内容(Base64.decode(item.getString("payload"), Base64.NO_WRAP));
			message.QoS(item.getInt("qos"));
			message.保留(item.getBoolean("retained"));
			MQTT发送令牌 token = new MQTT发送令牌(++sequence, item.optString("tag"), topic, message);
			restored.add(new QueuedMessage(topic, token));
		}
		queue.addAll(restored);
		for (QueuedMessage entry : restored) {
			pending.add(entry.token);
		}
	}

	/**
	 * 将消息排入离线队列，保存失败时回滚本次队列修改。
	 *
	 * @param entry 新消息
	 * @throws Exception 容量不足或无法持久保存
	 */
	private void enqueue(QueuedMessage entry) throws Exception {
		QueuedMessage removed = null;
		if (queue.size() >= bufferOptions.容量()) {
			if (!bufferOptions.淘汰最旧消息() || queue.get(0).sending) {
				throw new IllegalStateException("离线缓冲已满");
			}
			removed = queue.remove(0);
		}
		queue.add(entry);
		try {
			persistQueue();
		} catch (Exception error) {
			queue.remove(entry);
			if (removed != null) {
				queue.add(0, removed);
			}
			throw error;
		}
		if (removed != null) {
			finish(removed.token, null, new IllegalStateException("缓冲已满，本条旧消息已被淘汰"));
		}
	}

	/**
	 * 按顺序补发队首，只有交付完成后才移除持久队列记录。
	 */
	private void drain() {
		if (state != MQTT常量.状态_已连接 || queue.isEmpty() || queue.get(0).sending) {
			return;
		}
		send(queue.get(0));
	}

	/**
	 * 提交单条发布；队列中的消息发生网络失败时保留供下次连接补发。
	 *
	 * @param entry 发布消息
	 */
	private void send(QueuedMessage entry) {
		entry.sending = true;
		long epoch = generation;
		try {
			client.publish(entry.topic, entry.token.消息().snapshot(), null, new IMqttActionListener() {
				@Override
				public void onSuccess(IMqttToken result) {
					post(epoch, () -> {
						if (queue.remove(entry)) {
							try {
								persistQueue();
							} catch (Exception error) {
								finish(entry.token, result, error);
								drain();
								return;
							}
						}
						finish(entry.token, result, null);
						发送完成(entry.token);
						drain();
					});
				}

				@Override
				public void onFailure(IMqttToken result, Throwable error) {
					post(epoch, () -> {
						entry.sending = false;
						if (!queue.contains(entry)) {
							finish(entry.token, result, error);
						} else if (state == MQTT常量.状态_已连接 && client.isConnected()) {
							// 非网络掉线错误不能无限重试同一条消息。
							queue.remove(entry);
							try {
								persistQueue();
							} catch (Exception persistenceError) {
								error.addSuppressed(persistenceError);
							}
							finish(entry.token, result, error);
							drain();
						}
					});
				}
			});
		} catch (MqttException | RuntimeException error) {
			entry.sending = false;
			if (!queue.contains(entry)) {
				post(epoch, () -> finish(entry.token, null, error));
			}
		}
	}

	/**
	 * 在后台结束连接，再释放文件锁；不在 Simple 事件中等待网络。
	 *
	 * @param previous 待关闭客户端
	 * @param connecting 正在进行的连接令牌
	 * @param timeout 连接等待超时，单位秒
	 * @throws MqttException 无法释放客户端
	 */
	private static void release(MqttAsyncClient previous, IMqttToken connecting, int timeout) throws MqttException {
		if (connecting != null && !connecting.isComplete()) {
			try {
				connecting.waitForCompletion((timeout + 2L) * 1000L);
			} catch (MqttException error) {
				// 连接失败后仍需执行强制断开和文件锁释放。
			}
		}
		previous.setCallback(null);
		previous.disconnectForcibly(0, 1000, true);
		previous.close(true);
	}

	/* 属性 */

	@Override
	public boolean 已连接() {
		check();
		return client != null && client.isConnected() && state == MQTT常量.状态_已连接;
	}

	@Override
	public boolean 已创建() {
		check();
		return client != null;
	}

	@Override
	public int 状态() {
		check();
		return state;
	}

	@Override
	public String 服务器地址() {
		check();
		return client == null ? "" : client.getServerURI();
	}

	@Override
	public String 客户端ID() {
		check();
		return client == null ? "" : client.getClientId();
	}

	@Override
	public int 缓冲数量() {
		check();
		return queue.size();
	}

	/* 方法 */

	@Override
	public MQTT令牌 连接(String address, String clientId, MQTT连接参数 options, String tag) {
		idle();
		if (已连接()) {
			throw new IllegalStateException("客户端已经连接");
		}
		URI uri = URI.create(address);
		if (!("tcp".equals(uri.getScheme()) || "ssl".equals(uri.getScheme())) || uri.getHost() == null
			|| uri.getUserInfo() != null || uri.getQuery() != null || uri.getFragment() != null
			|| !(uri.getPath() == null || uri.getPath().isEmpty())) {
			throw new IllegalArgumentException("服务器地址须为 tcp:// 或 ssl://，不包含账号、路径或查询参数");
		}
		java.util.Objects.requireNonNull(options, "连接参数");
		String actualId = clientId == null || clientId.isEmpty()
			? (client == null ? "es4a-" + UUID.randomUUID() : client.getClientId()) : clientId;
		if (client != null && (!address.equals(client.getServerURI()) || !actualId.equals(client.getClientId())
			|| persistent != options.持久存储())) {
			throw new IllegalStateException("改变服务器、ID 或持久存储前须等待关闭操作完成");
		}
		cancelRetry();
		connectionOptions = options.snapshot();
		retryEnabled = options.自动重连();
		MQTT令牌 request = token("连接", tag);
		try {
			if (client == null) {
				persistent = options.持久存储();
				if (bufferOptions.持久化() && !persistent) {
					throw new IllegalArgumentException("持久缓冲要求启用持久存储");
				}
				String key = UUID.nameUUIDFromBytes((address + "\n" + actualId).getBytes(StandardCharsets.UTF_8)).toString();
				File directory = new File(activity.getFilesDir(), "mqtt/" + key);
				if (!directory.isDirectory() && !directory.mkdirs()) {
					throw new IllegalStateException("无法创建应用私有 MQTT 目录");
				}
				MqttClientPersistence store = persistent ? new MqttDefaultFilePersistence(directory.getPath()) : new MemoryPersistence();
				client = new MqttAsyncClient(address, actualId, store);
				bufferFile = new AtomicFile(new File(directory, "offline.json"));
				generation++;
				everConnected = false;
				state = MQTT常量.状态_已断开;
				installCallbacks(generation);
				queueRestored = false;
			}
			if (!queueRestored) {
				restoreQueue();
				queueRestored = true;
			}
			startConnect(request);
		} catch (Exception error) {
			state = client == null ? MQTT常量.状态_未创建 : MQTT常量.状态_已断开;
			post(generation, () -> finish(request, null, error));
		}
		return request;
	}

	@Override
	public MQTT令牌 订阅(String topic, int qos, String tag) {
		return 批量订阅(new String[] {topic}, new int[] {qos}, tag);
	}

	@Override
	public MQTT令牌 批量订阅(String[] topics, int[] qos, String tag) {
		check();
		if (!已连接() || topics == null || qos == null || topics.length == 0 || topics.length != qos.length) {
			throw new IllegalArgumentException("订阅要求已连接，且主题和 QoS 数组长度一致并非空");
		}
		String[] names = topics.clone();
		int[] levels = qos.clone();
		for (int index = 0; index < names.length; index++) {
			MqttTopic.validate(names[index], true);
			MqttMessage.validateQos(levels[index]);
		}
		MQTT令牌 request = token("订阅", tag, names);
		try {
			client.subscribe(names, levels, null, listener(generation, request, null, null));
		} catch (MqttException | RuntimeException error) {
			post(generation, () -> finish(request, null, error));
		}
		return request;
	}

	@Override
	public MQTT令牌 取消订阅(String topic, String tag) {
		check();
		if (!已连接()) {
			throw new IllegalStateException("客户端尚未连接");
		}
		MqttTopic.validate(topic, true);
		MQTT令牌 request = token("取消订阅", tag, topic);
		try {
			client.unsubscribe(topic, null, listener(generation, request, null, null));
		} catch (MqttException | RuntimeException error) {
			post(generation, () -> finish(request, null, error));
		}
		return request;
	}

	@Override
	public MQTT发送令牌 发布(String topic, MQTT消息 message, String tag) {
		check();
		if (state == MQTT常量.状态_断开中 || state == MQTT常量.状态_关闭中) {
			throw new IllegalStateException("请等待清理操作结束后再发布");
		}
		MqttTopic.validate(topic, false);
		java.util.Objects.requireNonNull(message, "消息");
		if (client == null || (!已连接() && (!everConnected || !bufferOptions.启用()))) {
			throw new IllegalStateException("离线发布需要客户端曾连接成功且已启用缓冲");
		}
		MQTT发送令牌 request = new MQTT发送令牌(++sequence, tag, topic, message);
		pending.add(request);
		QueuedMessage entry = new QueuedMessage(topic, request);
		if (!已连接() || !queue.isEmpty()) {
			try {
				enqueue(entry);
				drain();
			} catch (Exception error) {
				post(generation, () -> finish(request, null, error));
			}
		} else {
			send(entry);
		}
		return request;
	}

	@Override
	public void 设置离线缓冲(MQTT缓冲选项 options) {
		idle();
		java.util.Objects.requireNonNull(options, "缓冲选项");
		if (!queue.isEmpty()) {
			throw new IllegalStateException("队列非空，不能修改缓冲配置");
		}
		if (options.持久化() && client != null && !persistent) {
			throw new IllegalArgumentException("持久缓冲要求启用持久存储");
		}
		MQTT缓冲选项 copy = new MQTT缓冲选项();
		copy.启用(options.启用());
		copy.容量(options.容量());
		copy.持久化(options.持久化());
		copy.淘汰最旧消息(options.淘汰最旧消息());
		bufferOptions = copy;
	}

	@Override
	public MQTT消息 取缓冲消息(int index) {
		check();
		return queue.get(index).token.消息();
	}

	@Override
	public void 删除缓冲消息(int index) {
		idle();
		if (已连接()) {
			throw new IllegalStateException("请先断开再删除缓冲消息，避免与补发并发");
		}
		QueuedMessage entry = queue.remove(index);
		try {
			persistQueue();
		} catch (Exception error) {
			queue.add(index, entry);
			throw new IllegalStateException("删除缓冲消息未能持久保存", error);
		}
		post(generation, () -> finish(entry.token, null, new IllegalStateException("缓冲消息已由调用方删除")));
	}

	@Override
	public MQTT令牌 断开(long quiesce, String tag) {
		idle();
		if (quiesce < 0) {
			throw new IllegalArgumentException("等待时间不得为负数");
		}
		retryEnabled = false;
		cancelRetry();
		MQTT令牌 request = token("断开", tag);
		if (!已连接()) {
			post(generation, () -> finish(request, null, null));
			return request;
		}
		state = MQTT常量.状态_断开中;
		try {
			client.disconnect(quiesce, null, listener(generation, request,
				() -> state = MQTT常量.状态_已断开,
				() -> state = client.isConnected() ? MQTT常量.状态_已连接 : MQTT常量.状态_已断开));
		} catch (MqttException | RuntimeException error) {
			state = client.isConnected() ? MQTT常量.状态_已连接 : MQTT常量.状态_已断开;
			post(generation, () -> finish(request, null, error));
		}
		return request;
	}

	@Override
	public MQTT令牌 强制断开(long quiesce, long timeout, boolean sendPacket, String tag) {
		idle();
		if (client == null || quiesce < 0 || timeout < 0) {
			throw new IllegalArgumentException("要求已经创建客户端，且等待与超时不得为负数");
		}
		retryEnabled = false;
		cancelRetry();
		state = MQTT常量.状态_断开中;
		MQTT令牌 request = token("强制断开", tag);
		long epoch = generation;
		MqttAsyncClient current = client;
		cleanup.execute(() -> {
			try {
				current.disconnectForcibly(quiesce, timeout, sendPacket);
				post(epoch, () -> {
					state = MQTT常量.状态_已断开;
					finish(request, null, null);
				});
			} catch (MqttException | RuntimeException error) {
				post(epoch, () -> {
					state = current.isConnected() ? MQTT常量.状态_已连接 : MQTT常量.状态_已断开;
					finish(request, null, error);
				});
			}
		});
		return request;
	}

	@Override
	public MQTT令牌 关闭(String tag) {
		check();
		if (state == MQTT常量.状态_关闭中 || state == MQTT常量.状态_断开中) {
			throw new IllegalStateException("请等待清理操作结束");
		}
		retryEnabled = false;
		cancelRetry();
		long epoch = ++generation;
		MQTT令牌 request = token("关闭", tag);
		MqttAsyncClient previous = client;
		IMqttToken connecting = connectingToken;
		int timeout = connectionOptions == null ? 15 : connectionOptions.getConnectionTimeout();
		state = MQTT常量.状态_关闭中;
		cleanup.execute(() -> {
			Throwable failure = null;
			try {
				if (previous != null) {
					release(previous, connecting, timeout);
				}
			} catch (MqttException | RuntimeException error) {
				failure = error;
			}
			Throwable result = failure;
			post(epoch, () -> {
				for (MQTT令牌 old : new ArrayList<>(pending)) {
					if (old != request) {
						finish(old, null, new IllegalStateException("客户端已经关闭，本次操作已取消"));
					}
				}
				queue.clear();
				if (result == null) {
					client = null;
					bufferFile = null;
					connectingToken = null;
					everConnected = false;
					state = MQTT常量.状态_未创建;
				} else {
					state = previous != null && previous.isConnected() ? MQTT常量.状态_已连接 : MQTT常量.状态_已断开;
				}
				finish(request, null, result);
			});
		});
		return request;
	}

	/* 事件 */

	@Override
	public void 操作成功(MQTT令牌 token) {
		EventDispatcher.dispatchEvent(this, "操作成功", token);
	}

	@Override
	public void 操作失败(MQTT令牌 token) {
		EventDispatcher.dispatchEvent(this, "操作失败", token);
	}

	@Override
	public void 连接完成(boolean reconnected, String server) {
		EventDispatcher.dispatchEvent(this, "连接完成", reconnected, server);
	}

	@Override
	public void 连接丢失(MQTT错误 error) {
		EventDispatcher.dispatchEvent(this, "连接丢失", error);
	}

	@Override
	public void 收到消息(String topic, MQTT消息 message) {
		EventDispatcher.dispatchEvent(this, "收到消息", topic, message);
	}

	@Override
	public void 发送完成(MQTT发送令牌 token) {
		EventDispatcher.dispatchEvent(this, "发送完成", token);
	}

	/* 生命周期与内部类型 */

	@Override
	public void onDestroy() {
		销毁();
	}

	@Override
	public void 销毁() {
		if (destroyed) {
			return;
		}
		check();
		boolean alreadyClosing = state == MQTT常量.状态_关闭中;
		destroyed = true;
		state = MQTT常量.状态_已销毁;
		generation++;
		retryEnabled = false;
		handler.removeCallbacksAndMessages(null);
		activity.removeOnDestroyListener(this);
		MqttAsyncClient previous = alreadyClosing ? null : client;
		IMqttToken connecting = connectingToken;
		int timeout = connectionOptions == null ? 15 : connectionOptions.getConnectionTimeout();
		client = null;
		for (MQTT令牌 token : pending) {
			token.done = true;
			token.error = new MQTT错误(new IllegalStateException("组件已销毁"));
		}
		pending.clear();
		queue.clear();
		cleanup.execute(() -> {
			if (previous != null) {
				try {
					release(previous, connecting, timeout);
				} catch (MqttException | RuntimeException error) {
					Log.w("SimpleMQTT", "MQTT 客户端销毁清理失败", error);
				}
			}
		});
		cleanup.shutdown();
		super.销毁();
	}

	/** 保存一项发布的主题、令牌和补发状态。 */
	private static final class QueuedMessage {
		/** 发布主题。 */
		final String topic;
		/** 发布关联令牌及消息快照。 */
		final MQTT发送令牌 token;
		/** 是否正在等待当前发布完成。 */
		boolean sending;

		/**
		 * 建立一条队列记录。
		 *
		 * @param topic 发布主题
		 * @param token 发布令牌
		 */
		QueuedMessage(String topic, MQTT发送令牌 token) {
			this.topic = topic;
			this.token = token;
		}
	}
}
