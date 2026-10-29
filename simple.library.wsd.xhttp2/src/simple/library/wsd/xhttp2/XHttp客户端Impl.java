package simple.library.wsd.xhttp2;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.math.BigDecimal;
import java.net.SocketTimeoutException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.net.ssl.SSLException;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.xuexiang.xhttp2.XHttp;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.SerialDisposable;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

import okhttp3.HttpUrl;
import okhttp3.Cookie;

import simple.runtime.android.MainActivity;
import simple.runtime.components.impl.android.视图组件;
import simple.runtime.components.impl.组件Impl;
import simple.runtime.components.可视组件;
import simple.runtime.components.组件容器;
import simple.runtime.events.EventDispatcher;

/**
 * 将 XHttp2 的冷请求流接入 Simple 窗口，协调快照、界面回调、批次和取消的唯一终态。
 * 任务持有独立会话引用；界面操作不等待网络，下载提交以后拒绝再把该任务报告为取消。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XHttp客户端Impl extends 组件Impl implements XHttp客户端,
		MainActivity.OnDestroyListener, View.OnAttachStateChangeListener {

	private final MainActivity activity;
	private final Handler main = new Handler(Looper.getMainLooper());
	private final ClientSettings settings = new ClientSettings();
	private final Map<String, Task> tasks = new LinkedHashMap<String, Task>();
	private final Map<String, Batch> batches = new LinkedHashMap<String, Batch>();
	private final Set<Maintenance> maintenance = new HashSet<Maintenance>();
	private final ExecutorService workers = Executors.newFixedThreadPool(4);
	private String sessionName = UUID.randomUUID().toString();
	private boolean persistentCookies;
	private boolean namedSession;
	private SessionState session;
	private volatile boolean destroyed;
	private View owner;
	private boolean lifecycleRegistered;

	/**
	 * 创建绑定当前 Simple 窗口的独立网络客户端。
	 *
	 * @param container 所属窗口。
	 */
	public XHttp客户端Impl(组件容器 container) {
		super(container);
		activity = MainActivity.getContext();
		XHttp.init((Application) activity.getApplicationContext());
		registerLifecycle();
	}

	@Override
	public void 初始化() {
		if (owner == null) {
			owner = ((视图组件) (可视组件) getComponentContainer()).getView();
			owner.addOnAttachStateChangeListener(this);
		}
		super.初始化();
	}

	@Override
	public synchronized String 基础地址() {
		return settings.baseUrl;
	}

	@Override
	public synchronized void 基础地址(String value) {
		checkUi();
		value = HttpSupport.text(value);
		if (!value.isEmpty() && okhttp3.HttpUrl.parse(value) == null) { throw new IllegalArgumentException("基础地址无效。"); }
		settings.baseUrl = value;
	}

	@Override
	public synchronized int 默认超时() {
		return settings.timeout;
	}

	@Override
	public synchronized void 默认超时(int value) {
		checkUi();
		HttpSupport.minimum(value, 0);
		settings.timeout = value;
	}

	@Override
	public synchronized int 默认重试次数() {
		return settings.retries;
	}

	@Override
	public synchronized void 默认重试次数(int value) {
		checkUi();
		HttpSupport.range(value, 0, 100);
		settings.retries = value;
	}

	@Override
	public synchronized long 默认重试间隔() {
		return settings.retryDelay;
	}

	@Override
	public synchronized void 默认重试间隔(long value) {
		checkUi();
		HttpSupport.minimum(value, 0);
		HttpSupport.maximum(value, Integer.MAX_VALUE);
		settings.retryDelay = value;
	}

	@Override
	public synchronized long 默认重试递增间隔() {
		return settings.retryIncrease;
	}

	@Override
	public synchronized void 默认重试递增间隔(long value) {
		checkUi();
		HttpSupport.minimum(value, 0);
		HttpSupport.maximum(value, Integer.MAX_VALUE);
		settings.retryIncrease = value;
	}

	@Override
	public synchronized int 默认缓存策略() {
		return settings.cacheMode;
	}

	@Override
	public synchronized void 默认缓存策略(int value) {
		checkUi();
		HttpSupport.range(value, 0, 7);
		settings.cacheMode = value;
	}

	@Override
	public synchronized long 默认缓存秒数() {
		return settings.cacheSeconds;
	}

	@Override
	public synchronized void 默认缓存秒数(long value) {
		checkUi();
		HttpSupport.minimum(value, -1);
		settings.cacheSeconds = value;
	}

	@Override
	public synchronized long 最大响应字节() {
		return settings.maxBytes;
	}

	@Override
	public synchronized void 最大响应字节(long value) {
		checkUi();
		HttpSupport.minimum(value, 1);
		HttpSupport.maximum(value, Integer.MAX_VALUE);
		settings.maxBytes = value;
	}

	@Override
	public synchronized boolean 自动重定向() {
		return settings.redirects;
	}

	@Override
	public synchronized void 自动重定向(boolean value) {
		checkUi();
		settings.redirects = value;
	}

	@Override
	public String 发送(XHttp请求 request) {
		return submit(request, null, -1);
	}

	@Override
	public XHttp响应 同步发送(XHttp请求 request) {
		return sendSynchronously(request);
	}

	@Override
	public String 批量发送(XHttp请求[] requests, boolean serial, boolean stopOnFailure) {
		return submitBatch(requests, serial, stopOnFailure);
	}

	@Override
	public boolean 取消(String id) {
		checkUi();
		Batch batch = batches.get(id);
		if (batch != null) { return batch.cancel(); }
		Task task = tasks.get(id);
		return task != null && cancelTask(task);
	}

	@Override
	public void 取消全部() {
		checkUi();
		cancelAll();
	}

	@Override
	public boolean 是否运行(String id) {
		checkUi();
		return tasks.containsKey(id) || batches.containsKey(id);
	}

	@Override
	public int 取运行数量() {
		checkUi();
		return tasks.size();
	}

	@Override
	public void 置默认请求头(String name, String value) {
		checkUi();
		HttpSupport.setHeader(settings.headers, name, value);
	}

	@Override
	public void 删除默认请求头(String name) {
		checkUi();
		HttpSupport.remove(settings.headers, name, true);
	}

	@Override
	public void 清空默认请求头() {
		checkUi();
		settings.headers.clear();
	}

	@Override
	public void 添加默认参数(String name, String value) {
		checkUi();
		HttpSupport.add(settings.query, name, value, false);
	}

	@Override
	public void 清空默认参数() {
		checkUi();
		settings.query.clear();
	}

	@Override
	public void 置代理(String host, int port, String username, String password) {
		checkUi();
		if (!HttpSupport.text(host).isEmpty()) { HttpSupport.range(port, 1, 65535); }
		settings.proxyHost = HttpSupport.text(host);
		settings.proxyPort = port;
		settings.proxyUser = HttpSupport.text(username);
		settings.proxyPassword = HttpSupport.text(password);
	}

	@Override
	public void 置信任证书(String[] paths, boolean includeSystem) {
		checkUi();
		byte[][] data = loadAuthorities(paths);
		if (data.length == 0 && !includeSystem) { throw new IllegalArgumentException("替换系统信任至少需要一份 CA。"); }
		settings.authorities = data;
		settings.includeSystem = includeSystem;
	}

	@Override
	public void 置客户端证书(String path, String password) {
		checkUi();
		settings.clientCertificate = HttpSupport.text(path).isEmpty() ? new byte[0] : loadSmallFile(path);
		settings.certificatePassword = HttpSupport.text(password).toCharArray();
	}

	@Override
	public void 置会话(String name, boolean persistent) {
		switchSession(name, persistent);
	}

	@Override
	public void 设置Cookie(String address, String cookie) {
		checkUi();
		getSession().cookies.put(requireUrl(address), cookie);
	}

	@Override
	public String[] 取Cookie(String address) {
		checkUi();
		List<Cookie> values = getSession().cookies.loadForRequest(requireUrl(address));
		String[] result = new String[values.size()];
		for (int index = 0; index < result.length; index++) { result[index] = values.get(index).toString(); }
		return result;
	}

	@Override
	public boolean 删除Cookie(String name, String domain, String path) {
		checkUi();
		return getSession().cookies.remove(name, domain, path);
	}

	@Override
	public void 清空Cookie() {
		checkUi();
		getSession().cookies.clear();
	}

	@Override
	public void 删除缓存(String key) {
		changeCache(key, false);
	}

	@Override
	public void 清空缓存() {
		changeCache("", true);
	}

	@Override
	public void 销毁() {
		destroyClient();
	}

	@Override
	public void 请求前处理(XHttp请求 request) {
		EventDispatcher.dispatchEvent(this, "请求前处理", request);
	}

	@Override
	public void 请求开始(String id) {
		EventDispatcher.dispatchEvent(this, "请求开始", id);
	}

	@Override
	public void 收到响应(String id, XHttp响应 response) {
		EventDispatcher.dispatchEvent(this, "收到响应", id, response);
	}

	@Override
	public void 请求成功(String id, XHttp响应 response) {
		EventDispatcher.dispatchEvent(this, "请求成功", id, response);
	}

	@Override
	public void 请求失败(String id, XHttp响应 response) {
		EventDispatcher.dispatchEvent(this, "请求失败", id, response);
	}

	@Override
	public void 请求取消(String id) {
		EventDispatcher.dispatchEvent(this, "请求取消", id);
	}

	@Override
	public void 请求结束(String id) {
		EventDispatcher.dispatchEvent(this, "请求结束", id);
	}

	@Override
	public void 传输进度(String id, boolean upload, long transferred, long total) {
		EventDispatcher.dispatchEvent(this, "传输进度", id, upload, transferred, total);
	}

	@Override
	public void 解析响应(String id, XHttp响应 response, XHttp解析结果 result) {
		EventDispatcher.dispatchEvent(this, "解析响应", id, response, result);
	}

	@Override
	public void 批次完成(XHttp批结果 result) {
		EventDispatcher.dispatchEvent(this, "批次完成", result);
	}

	@Override
	public void 缓存操作完成(String key, boolean success, String error) {
		EventDispatcher.dispatchEvent(this, "缓存操作完成", key, success, error);
	}

	private void checkUi() {
		if (Looper.myLooper() != Looper.getMainLooper() || destroyed) {
			throw new IllegalStateException("客户端须在界面线程且未销毁时使用。");
		}
	}

	private void checkWindow() {
		checkUi();
		if (owner == null || !owner.isAttachedToWindow()) {
			throw new IllegalStateException("请求须在所属窗口显示后发送。");
		}
	}

	private SessionState getSession() {
		if (session == null) {
			try {
				session = SessionState.acquire(activity, sessionName, persistentCookies, !namedSession);
			} catch (IOException failure) {
				throw new IllegalStateException("无法打开网络会话。", failure);
			}
		}
		return session;
	}

	private void switchSession(String name, boolean persistent) {
		checkUi();
		if (!tasks.isEmpty()) {
			throw new IllegalStateException("运行期间不能切换会话。");
		}
		SessionState replacement;
		try {
			replacement = SessionState.acquire(activity, HttpSupport.nonempty(name), persistent, false);
		} catch (IOException failure) {
			throw new IllegalStateException("无法打开指定会话。", failure);
		}
		SessionState previous = session;
		session = replacement;
		sessionName = name;
		persistentCookies = persistent;
		namedSession = true;
		release(previous);
	}

	private static HttpUrl requireUrl(String address) {
		HttpUrl result = HttpUrl.parse(HttpSupport.nonempty(address));
		if (result == null) {
			throw new IllegalArgumentException("地址无效。");
		}
		return result;
	}

	private byte[] loadSmallFile(String path) {
		try (InputStream input = HttpSupport.open(activity, path)) {
			return HttpSupport.read(input, 8L * 1024 * 1024);
		} catch (IOException failure) {
			throw new IllegalArgumentException("无法读取证书来源。", failure);
		}
	}

	private byte[][] loadAuthorities(String[] paths) {
		if (paths == null) {
			throw new IllegalArgumentException("证书数组不能为空。");
		}
		byte[][] result = new byte[paths.length][];
		for (int index = 0; index < result.length; index++) {
			result[index] = loadSmallFile(HttpSupport.nonempty(paths[index]));
		}
		return result;
	}

	private String submit(XHttp请求 input, Batch batch, int index) {
		checkWindow();
		if (input == null) {
			throw new IllegalArgumentException("请求不能为空。");
		}
		XHttp请求 snapshot = input.copy();
		String id = snapshot.id.isEmpty() ? UUID.randomUUID().toString() : snapshot.id;
		if (tasks.containsKey(id) || batches.containsKey(id)) {
			throw new IllegalArgumentException("请求标识正在使用：" + id);
		}
		Task task = new Task(id, snapshot, settings.copy(), getSession().retain(), batch, index, false);
		tasks.put(id, task);
		begin(task);
		return id;
	}

	/**
	 * 前处理允许用户回调取消任务；回调返回后重新检查唯一终态，不能继续启动已取消的任务。
	 */
	private void begin(final Task task) {
		if (task.finished || task.started) {
			return;
		}
		task.started = true;
		try {
			请求前处理(task.request);
			task.request.id = task.id;
			if (task.finished) {
				return;
			}
			task.request = task.request.copy();
			请求开始(task.id);
			if (!task.finished) {
				workers.execute(new Runnable() {
					@Override
					public void run() {
						XHttp客户端Impl.this.run(task);
					}
				});
			}
		} catch (RuntimeException failure) {
			if (!task.finished) {
				task.last = failure(task, failure);
				finish(task);
				abandonQueued(task);
			}
			throw failure;
		}
	}

	/**
	 * 固定工作池限制并行请求数量；逐条等待界面处理后才允许上游继续发射或切到远程请求。
	 * 原生重试可改变执行线程，工作线程等待终态而不把响应预先积存在阻塞迭代队列中。
	 */
	private void run(final Task task) {
		if (!task.workClaimed.compareAndSet(false, true)) {
			return;
		}
		try {
			final CountDownLatch terminal = new CountDownLatch(1);
			final AtomicReference<Throwable> outcome = new AtomicReference<Throwable>();
			HttpEngine engine = new HttpEngine(activity, task.session, task.settings, task.request, task.control, task.id);
			engine.execute().takeUntil(task.cancelSignal).subscribe(new Observer<XHttp响应>() {
				@Override
				public void onSubscribe(Disposable disposable) {
					task.subscription.set(disposable);
					if (task.control.cancelled()) {
						terminal.countDown();
					}
				}

				@Override
				public void onNext(final XHttp响应 response) {
					try {
						task.control.check();
						onUi(task, new Callable<Void>() {
							@Override
							public Void call() {
								if (!task.finished) {
									parse(task, response);
									if (!task.finished) {
										task.last = response;
										if (!task.synchronous) {
											receive(task, response);
										}
									}
								}
								return null;
							}
						});
					} catch (Throwable failure) {
						Exceptions.throwIfFatal(failure);
						outcome.compareAndSet(null, failure);
						task.subscription.dispose();
						terminal.countDown();
					}
				}

				@Override
				public void onError(Throwable failure) {
					outcome.compareAndSet(null, failure);
					terminal.countDown();
				}

				@Override
				public void onComplete() {
					terminal.countDown();
				}
			});
			while (!terminal.await(100, TimeUnit.MILLISECONDS)) {
				task.control.check();
			}
			if (outcome.get() != null) {
				throw new RuntimeException(outcome.get());
			}
		} catch (final Throwable failure) {
			Exceptions.throwIfFatal(failure);
			// 错误转成可检查的响应，事件仍由界面线程顺序执行，不在后台直接调用 Simple。
			main.post(new Runnable() {
				@Override
				public void run() {
					if (!task.finished) {
						task.last = failure(task, failure);
						if (!destroyed && !task.synchronous && !task.control.cancelled()) {
							receive(task, task.last);
						}
					}
				}
			});
		} finally {
			release(task.session);
			main.post(new Runnable() {
				@Override
				public void run() {
					finish(task);
				}
			});
		}
	}

	private void receive(Task task, XHttp响应 response) {
		收到响应(task.id, response);
		if (!task.finished) {
			if (response.成功()) {
				请求成功(task.id, response);
			} else {
				请求失败(task.id, response);
			}
		}
	}

	private void parse(Task task, XHttp响应 response) {
		if (!response.error.isEmpty() || task.request.parser == 0) {
			return;
		}
		try {
			XHttp解析结果 result = new XHttp解析结果();
			if (task.request.parser == 2) {
				解析响应(task.id, response, result);
				if (!result.supplied()) {
					throw new IllegalArgumentException("自定义解析事件没有设置成功状态。");
				}
			} else {
				JsonElement json = new JsonParser().parse(response.文本());
				JsonElement code = HttpSupport.select(json, task.request.codePath);
				if (code == null || !code.isJsonPrimitive()) {
					throw new IllegalArgumentException("标准响应没有业务状态字段。");
				}
				result.状态码(new BigDecimal(code.getAsString()).intValueExact());
				result.成功(result.状态码() == task.request.successCode);
				result.消息(task.request.messagePath.isEmpty() ? "" : HttpSupport.jsonText(HttpSupport.select(json, task.request.messagePath)));
				result.数据(HttpSupport.jsonText(HttpSupport.select(json, task.request.dataPath)));
			}
			response.parsed = result.copy();
			if (!result.成功()) {
				response.errorType = "业务";
				response.error = result.消息().isEmpty() ? "业务处理未成功。" : result.消息();
			}
		} catch (RuntimeException failure) {
			response.errorType = "解析";
			response.error = describe(failure);
		}
	}

	private XHttp响应 sendSynchronously(final XHttp请求 input) {
		if (Looper.myLooper() == Looper.getMainLooper()) {
			throw new IllegalStateException("同步发送不能在界面线程调用。");
		}
		final AtomicReference<Task> holder = new AtomicReference<Task>();
		try {
			onUi(null, new Callable<Void>() {
				@Override
				public Void call() {
					checkWindow();
					if (input == null) {
						throw new IllegalArgumentException("请求不能为空。");
					}
					XHttp请求 snapshot = input.copy();
					String id = snapshot.id.isEmpty() ? UUID.randomUUID().toString() : snapshot.id;
					if (tasks.containsKey(id) || batches.containsKey(id)) {
						throw new IllegalArgumentException("请求标识正在使用。");
					}
					Task task = new Task(id, snapshot, settings.copy(), getSession().retain(), null, -1, true);
					holder.set(task);
					tasks.put(id, task);
					try {
						请求前处理(snapshot);
					} catch (RuntimeException failure) {
						task.last = failure(task, failure);
						finish(task);
						abandonQueued(task);
						throw failure;
					}
					task.request = snapshot.copy();
					task.request.id = id;
					return null;
				}
			});
			Task task = holder.get();
			run(task);
			onUi(null, new Callable<Void>() {
				@Override
				public Void call() {
					finish(holder.get());
					return null;
				}
			});
			return task.last;
		} catch (Exception failure) {
			Task task = holder.get();
			if (task == null) {
				throw new IllegalStateException("无法提交同步请求。", failure);
			}
			return failure(task, failure);
		}
	}

	private boolean cancelTask(Task task) {
		if (task.finished || !task.control.cancel()) {
			return false;
		}
		task.cancelSignal.onNext(Boolean.TRUE);
		task.subscription.dispose();
		task.last = failure(task, new InterruptedIOException("请求已取消。"));
		try {
			if (!destroyed && !task.synchronous) {
				请求取消(task.id);
			}
		} finally {
			finish(task);
			abandonQueued(task);
		}
		return true;
	}

	/**
	 * 未进入工作线程的任务由取消方释放；已被工作线程认领的任务只在工作线程退出时释放。
	 */
	private void abandonQueued(Task task) {
		if (task.workClaimed.compareAndSet(false, true)) {
			release(task.session);
		}
	}

	private void finish(Task task) {
		if (task.finished) {
			return;
		}
		task.finished = true;
		tasks.remove(task.id);
		if (task.last == null) {
			task.last = failure(task, new IllegalStateException("请求没有产生响应。"));
		}
		try {
			if (!destroyed && !task.synchronous) {
				请求结束(task.id);
			}
		} finally {
			if (task.batch != null) {
				task.batch.finished(task);
			}
		}
	}

	private void cancelAll() {
		for (Batch batch : new ArrayList<Batch>(batches.values())) {
			batch.cancel();
		}
		for (Task task : new ArrayList<Task>(tasks.values())) {
			cancelTask(task);
		}
	}

	private String submitBatch(XHttp请求[] inputs, boolean serial, boolean stopOnFailure) {
		checkWindow();
		if (inputs == null || inputs.length == 0) {
			throw new IllegalArgumentException("批次至少需要一个请求。");
		}
		Batch batch = new Batch(UUID.randomUUID().toString(), serial, stopOnFailure, inputs.length);
		Set<String> names = new HashSet<String>();
		List<XHttp请求> copies = new ArrayList<XHttp请求>();
		for (XHttp请求 input : inputs) {
			if (input == null) {
				throw new IllegalArgumentException("批次请求不能为空。");
			}
			XHttp请求 copy = input.copy();
			if (copy.id.isEmpty()) {
				copy.id = UUID.randomUUID().toString();
			}
			if (!names.add(copy.id) || tasks.containsKey(copy.id) || batches.containsKey(copy.id)) {
				throw new IllegalArgumentException("批次中存在重复或正在使用的请求标识。");
			}
			copies.add(copy);
		}
		ClientSettings defaults = settings.copy();
		SessionState shared = getSession();
		for (int index = 0; index < copies.size(); index++) {
			XHttp请求 copy = copies.get(index);
			Task task = new Task(copy.id, copy, defaults.copy(), shared.retain(), batch, index, false);
			batch.children.add(task);
			tasks.put(task.id, task);
		}
		batches.put(batch.id, batch);
		try {
			if (serial) {
				begin(batch.children.get(0));
			} else {
				for (Task task : batch.children) {
					begin(task);
				}
			}
		} catch (RuntimeException failure) {
			batch.cancel();
			throw failure;
		}
		return batch.id;
	}

	/**
	 * 只允许界面回调读取和修改 Simple 对象；等待者定期检查取消，超时后撤销尚未执行的回调。
	 */
	private <T> T onUi(final Task task, final Callable<T> callback) throws Exception {
		if (Looper.myLooper() == Looper.getMainLooper()) {
			return callback.call();
		}
		final CountDownLatch done = new CountDownLatch(1);
		final AtomicBoolean abandoned = new AtomicBoolean();
		final AtomicReference<T> result = new AtomicReference<T>();
		final AtomicReference<Exception> failure = new AtomicReference<Exception>();
		Runnable delivery = new Runnable() {
			@Override
			public void run() {
				try {
					if (!abandoned.get() && !destroyed && (task == null || !task.finished)) {
						result.set(callback.call());
					}
				} catch (Exception error) {
					failure.set(error);
				} finally {
					done.countDown();
				}
			}
		};
		main.post(delivery);
		long deadline = SystemClock.elapsedRealtime() + 30000;
		try {
			while (!done.await(100, TimeUnit.MILLISECONDS)) {
				if (destroyed || (task != null && task.control.cancelled())) {
					throw new InterruptedIOException("界面回调已取消。");
				}
				if (SystemClock.elapsedRealtime() >= deadline) {
					throw new SocketTimeoutException("等待界面回调超时。");
				}
			}
		} finally {
			abandoned.set(true);
			main.removeCallbacks(delivery);
		}
		if (failure.get() != null) {
			throw failure.get();
		}
		return result.get();
	}

	private static XHttp响应 failure(Task task, Throwable error) {
		// 订阅终态会用 RuntimeException 跨越受检异常边界，分类前恢复真实传输原因。
		while (error.getClass() == RuntimeException.class && error.getCause() != null) {
			error = error.getCause();
		}
		if (error instanceof HttpEngine.HttpFailure) {
			return ((HttpEngine.HttpFailure) error).reply.response(task.id, "", 0, false);
		}
		XHttp响应 response = new XHttp响应();
		response.id = task.id;
		response.address = task.request.address;
		response.error = describe(error);
		if (task.control.cancelled()) {
			response.errorType = "取消";
		} else if (error instanceof TransferFailure) {
			response.errorType = ((TransferFailure) error).category;
		} else if (error instanceof HttpSupport.LimitExceeded) {
			response.errorType = "响应";
		} else if (error instanceof SSLException || error instanceof GeneralSecurityException) {
			response.errorType = "证书";
		} else if (HttpSupport.timedOut(error)) {
			response.errorType = "超时";
		} else if (error instanceof InterruptedIOException) {
			response.errorType = "取消";
		} else if (error instanceof IllegalArgumentException) {
			response.errorType = "参数";
		} else if (task.request.cacheMode == 缓存_仅缓存 || (task.request.cacheMode == -1 && task.settings.cacheMode == 缓存_仅缓存)) {
			response.errorType = "缓存";
		} else {
			response.errorType = error instanceof IOException ? "网络" : "处理";
		}
		return response;
	}

	private static String describe(Throwable error) {
		StringBuilder result = new StringBuilder(HttpSupport.text(error.getMessage()));
		Throwable cause = error.getCause();
		for (int depth = 0; cause != null && depth < 8; depth++, cause = cause.getCause()) {
			String message = HttpSupport.text(cause.getMessage());
			if (!message.isEmpty() && result.indexOf(message) < 0) {
				result.append(result.length() == 0 ? "" : "：").append(message);
			}
		}
		return result.length() == 0 ? error.getClass().getSimpleName() : result.toString();
	}

	private void changeCache(String key, boolean all) {
		checkWindow();
		Maintenance operation = new Maintenance(getSession().retain(), key, all);
		maintenance.add(operation);
		workers.execute(operation);
	}

	private void registerLifecycle() {
		if (!lifecycleRegistered) {
			activity.addOnDestroyListener(this);
			lifecycleRegistered = true;
		}
	}

	@Override
	public void onViewAttachedToWindow(View view) {
		if (!destroyed) {
			registerLifecycle();
		}
	}

	@Override
	public void onViewDetachedFromWindow(View view) {
		cancelAll();
		if (lifecycleRegistered) {
			activity.removeOnDestroyListener(this);
			lifecycleRegistered = false;
		}
	}

	@Override
	public void onDestroy() {
		destroyClient();
	}

	private void destroyClient() {
		if (destroyed) {
			return;
		}
		checkUi();
		destroyed = true;
		cancelAll();
		// 已提交下载不再接受取消，但销毁仍须移除其界面状态，禁止后续回调进入旧窗口。
		for (Task task : new ArrayList<Task>(tasks.values())) {
			finish(task);
			abandonQueued(task);
		}
		for (Maintenance operation : new ArrayList<Maintenance>(maintenance)) {
			operation.abandon();
		}
		workers.shutdownNow();
		if (owner != null) {
			owner.removeOnAttachStateChangeListener(this);
			owner = null;
		}
		if (lifecycleRegistered) {
			activity.removeOnDestroyListener(this);
			lifecycleRegistered = false;
		}
		release(session);
		session = null;
		super.销毁();
	}

	private static void release(SessionState value) {
		if (value != null) {
			try {
				value.release();
			} catch (IOException failure) {
				Log.e("XHttp2", "关闭网络会话失败", failure);
			}
		}
	}

	/**
	 * 单次请求的唯一状态；进度只保留一份待显示数据，避免后台传输淹没界面消息队列。
	 */
	private final class Task {
		final String id;
		XHttp请求 request;
		final ClientSettings settings;
		final SessionState session;
		final Batch batch;
		final int index;
		final boolean synchronous;
		final SerialDisposable subscription = new SerialDisposable();
		final Subject<Boolean> cancelSignal = PublishSubject.<Boolean>create().toSerialized();
		final AtomicBoolean workClaimed = new AtomicBoolean();
		final AtomicReference<long[]> latestProgress = new AtomicReference<long[]>();
		final AtomicBoolean progressPending = new AtomicBoolean();
		final TransferControl control;
		volatile boolean finished;
		boolean started;
		XHttp响应 last;

		Task(String id, XHttp请求 request, ClientSettings settings, SessionState session, Batch batch, int index, boolean synchronous) {
			this.id = id;
			this.request = request;
			this.settings = settings;
			this.session = session;
			this.batch = batch;
			this.index = index;
			this.synchronous = synchronous;
			control = new TransferControl(new TransferControl.Progress() {
				@Override
				public void report(boolean upload, long transferred, long total) {
					latestProgress.set(new long[] {upload ? 1 : 0, transferred, total});
					postProgress();
				}
			});
		}

		void postProgress() {
			if (!synchronous && !finished && progressPending.compareAndSet(false, true)) {
				main.post(new Runnable() {
					@Override
					public void run() {
						long[] value = latestProgress.getAndSet(null);
						progressPending.set(false);
						if (!finished && value != null) {
							传输进度(id, value[0] == 1, value[1], value[2]);
						}
						if (latestProgress.get() != null) {
							postProgress();
						}
					}
				});
			}
		}
	}

	/**
	 * 批次统一保存每个项目的最终结果，取消剩余任务时允许重入但只分发一次完成事件。
	 */
	private final class Batch {
		final String id;
		final boolean serial;
		final boolean stopOnFailure;
		final List<Task> children = new ArrayList<Task>();
		final XHttp响应[] results;
		int completed;
		boolean stopping;
		boolean cancelled;
		boolean delivered;

		Batch(String id, boolean serial, boolean stopOnFailure, int count) {
			this.id = id;
			this.serial = serial;
			this.stopOnFailure = stopOnFailure;
			results = new XHttp响应[count];
		}

		boolean cancel() {
			if (delivered) {
				return false;
			}
			cancelled = true;
			stopping = true;
			boolean changed = false;
			for (Task child : children) {
				changed = cancelTask(child) || changed;
			}
			return changed;
		}

		void finished(Task task) {
			results[task.index] = task.last;
			completed++;
			if (!stopping && stopOnFailure && !task.last.成功()) {
				stopping = true;
				for (Task child : children) {
					cancelTask(child);
				}
			}
			if (!delivered && completed == children.size()) {
				delivered = true;
				batches.remove(id);
				XHttp批结果 result = new XHttp批结果();
				result.id = id;
				result.replies = results.clone();
				result.cancelled = cancelled;
				if (!destroyed) {
					批次完成(result);
				}
			} else if (!stopping && serial) {
				for (Task child : children) {
					if (!child.started && !child.finished) {
						begin(child);
						break;
					}
				}
			}
		}
	}

	/**
	 * 缓存操作也占有会话引用；销毁时释放尚未进入工作池的操作，不遗留缓存锁。
	 */
	private final class Maintenance implements Runnable {
		final SessionState state;
		final String key;
		final boolean all;
		final AtomicBoolean claimed = new AtomicBoolean();

		Maintenance(SessionState state, String key, boolean all) {
			this.state = state;
			this.key = key;
			this.all = all;
		}

		void abandon() {
			if (claimed.compareAndSet(false, true)) {
				release(state);
			}
		}

		@Override
		public void run() {
			if (!claimed.compareAndSet(false, true)) {
				return;
			}
			String message = "";
			try {
				if (all) {
					state.cache.clear();
					state.httpCache.evictAll();
				} else {
					state.cache.remove(key);
				}
			} catch (Exception failure) {
				message = failure.toString();
			} finally {
				release(state);
			}
			final String error = message;
			main.post(new Runnable() {
				@Override
				public void run() {
					maintenance.remove(Maintenance.this);
					if (!destroyed && owner != null && owner.isAttachedToWindow()) {
						缓存操作完成(key, error.isEmpty(), error);
					}
				}
			});
		}
	}
	@Override
	public void 置超时(int connect, int read, int write, int total) {
		checkUi();
		HttpSupport.minimum(connect, 0);
		HttpSupport.minimum(read, 0);
		HttpSupport.minimum(write, 0);
		HttpSupport.minimum(total, 0);
		settings.connectTimeout = connect;
		settings.readTimeout = read;
		settings.writeTimeout = write;
		settings.callTimeout = total;
	}

}
