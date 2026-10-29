package simple.library.wsd.agentweb;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.GeolocationPermissions;
import android.webkit.JavascriptInterface;
import android.webkit.PermissionRequest;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.LinearLayout;

import com.just.agentweb.AgentWeb;
import com.just.agentweb.AgentWebSettingsImpl;
import com.just.agentweb.DefaultWebClient;
import com.just.agentweb.WebChromeClient;
import com.just.agentweb.WebIndicator;
import com.just.agentweb.WebListenerManager;
import com.just.agentweb.WebViewClient;

import simple.runtime.android.MainActivity;
import simple.runtime.components.组件容器;
import simple.runtime.components.impl.android.视图组件;
import simple.runtime.events.EventDispatcher;
import simple.runtime.parameters.BooleanReferenceParameter;

/**
 * AgentWeb 可视组件及 Simple 事件、下载所有权和窗口生命周期适配。
 * 不替换 AgentWeb 的内部客户端，所有增强回调通过其委托链接入。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class 浏览器Impl extends 视图组件 implements 浏览器,
		MainActivity.OnPauseListener, MainActivity.OnResumeListener, MainActivity.OnDestroyListener,
		MainActivity.OnActivityResultListener, View.OnAttachStateChangeListener {

	// 宿主是普通 Activity；独立请求区间与运行库现有组件区分，每次选择使用新编号。
	private static final AtomicInteger NEXT_FILE_REQUEST = new AtomicInteger(0x41570000);

	// createView 在父类构造期间执行，以下状态不得再用字段初始化器覆盖。
	private MainActivity activity;
	private LinearLayout root;
	private AgentWeb agent;
	private WebView browser;
	private Handler handler;
	private DownloadTasks downloads;
	private BrowserFullscreen fullscreen;
	private Map<String, MessageBridge> bridges;
	private PendingFile pendingFile;
	private int fileRequest;
	private volatile boolean destroyed;
	private volatile long generation;

	/**
	 * 创建浏览框，并订阅本宿主的暂停、恢复和销毁事件。
	 *
	 * @param container 容纳浏览框的 Simple 容器。
	 */
	public 浏览器Impl(组件容器 container) {
		super(container);
		root.addOnAttachStateChangeListener(this);
		activity.addOnPauseListener(this);
		activity.addOnResumeListener(this);
		activity.addOnDestroyListener(this);
		activity.addOnActivityResultListener(this);
	}

	@Override
	protected View createView() {
		checkThread();
		activity = MainActivity.getContext();
		handler = new Handler(Looper.getMainLooper());
		bridges = new LinkedHashMap<String, MessageBridge>();
		root = new LinearLayout(activity);
		root.setOrientation(LinearLayout.VERTICAL);
		fullscreen = new BrowserFullscreen(activity);

		// 构建期间只装配客户端，页面由显式加载操作决定。
		agent = AgentWeb.with(activity)
				.setAgentWebParent(root, new LinearLayout.LayoutParams(-1, -1))
				.useDefaultIndicator(0xff1976d2)
				.setSecurityType(AgentWeb.SecurityType.STRICT_CHECK)
				.setOpenOtherPageWays(DefaultWebClient.OpenOtherPageWays.DISALLOW)
				.isInterceptUnkownUrl(true)
				.setAgentWebWebSettings(new AgentWebSettingsImpl() {
					@Override
					public WebListenerManager setDownloader(WebView view, DownloadListener ignored) {
						return super.setDownloader(view, new DownloadListener() {
							@Override
							public void onDownloadStart(String url, String userAgent, String disposition,
									String mime, long length) {
								下载已请求(url, safe(userAgent), safe(disposition), safe(mime), length);
							}
						});
					}
				})
				.setWebViewClient(new BrowserClient())
				.setWebChromeClient(new BrowserChrome())
				.createAgentWeb().ready().get();
		browser = agent.getWebCreator().getWebView();
		browser.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
		CookieManager.getInstance().setAcceptThirdPartyCookies(browser, false);
		downloads = new DownloadTasks(activity, new DownloadTasks.Listener() {
			@Override
			public void progress(String url, long received, long total) {
				下载进度(url, received, total);
			}

			@Override
			public void finished(String url, String path, Throwable error) {
				if (error == null) {
					下载完成(url, path);
				} else {
					下载失败(url, safe(error.getMessage()));
				}
			}

			@Override
			public void cancelled(String url) {
				下载已取消(url);
			}
		});
		return root;
	}

	@Override
	public void 加载网址(String url) {
		requireLive();
		if (!isPageUrl(url)) {
			throw new IllegalArgumentException("请提供完整网页地址或可访问的本地地址。");
		}
		agent.getUrlLoader().loadUrl(url);
	}

	@Override
	public void 加载HTML(String html, String baseUrl) {
		requireLive();
		browser.loadDataWithBaseURL(baseUrl == null || baseUrl.isEmpty() ? "about:blank" : baseUrl,
				html, "text/html", "UTF-8", null);
	}

	@Override
	public boolean 后退() {
		requireLive();
		if (fullscreen.exit()) {
			全屏改变(false);
			return true;
		}
		return agent.back();
	}

	@Override
	public boolean 前进() {
		requireLive();
		if (!browser.canGoForward()) {
			return false;
		}
		browser.goForward();
		return true;
	}

	@Override
	public void 刷新() {
		requireLive();
		browser.reload();
	}

	@Override
	public void 停止载入() {
		requireLive();
		browser.stopLoading();
	}

	@Override
	public boolean 可否后退() {
		requireLive();
		return browser.canGoBack();
	}

	@Override
	public boolean 可否前进() {
		requireLive();
		return browser.canGoForward();
	}

	@Override
	public String 取地址() {
		requireLive();
		return safe(browser.getUrl());
	}

	@Override
	public String 取标题() {
		requireLive();
		return safe(browser.getTitle());
	}

	@Override
	public void 置JavaScript(boolean enabled) {
		requireLive();
		browser.getSettings().setJavaScriptEnabled(enabled);
	}

	@Override
	public void 置UserAgent(String value) {
		requireLive();
		browser.getSettings().setUserAgentString(value);
	}

	@Override
	public String 取UserAgent() {
		requireLive();
		return browser.getSettings().getUserAgentString();
	}

	@Override
	public void 置缩放(boolean enabled, boolean buttons) {
		requireLive();
		WebSettings settings = browser.getSettings();
		settings.setSupportZoom(enabled);
		settings.setBuiltInZoomControls(enabled);
		settings.setDisplayZoomControls(buttons);
	}

	@Override
	public void 置缓存模式(int mode) {
		requireLive();
		if (mode != -1 && mode != 1 && mode != 2 && mode != 3) {
			throw new IllegalArgumentException("缓存模式必须为 -1、1、2 或 3。");
		}
		browser.getSettings().setCacheMode(mode);
	}

	@Override
	public void 清除缓存(boolean disk) {
		requireLive();
		browser.clearCache(disk);
	}

	@Override
	public void 清除历史() {
		requireLive();
		browser.clearHistory();
	}

	@Override
	public void 置Cookie(final String url, String value) {
		requireLive();
		http(url);
		CookieManager.getInstance().setCookie(url, value, new ValueCallback<Boolean>() {
			@Override
			public void onReceiveValue(Boolean success) {
				CookieManager.getInstance().flush();
				Cookie已写入(url, Boolean.TRUE.equals(success));
			}
		});
	}

	@Override
	public String 取Cookie(String url) {
		requireLive();
		http(url);
		return safe(CookieManager.getInstance().getCookie(url));
	}

	@Override
	public void 清除Cookie() {
		requireLive();
		CookieManager.getInstance().removeAllCookies(new ValueCallback<Boolean>() {
			@Override
			public void onReceiveValue(Boolean removed) {
				CookieManager.getInstance().flush();
				Cookie已清除();
			}
		});
	}

	@Override
	public void 置第三方Cookie(boolean enabled) {
		requireLive();
		CookieManager.getInstance().setAcceptThirdPartyCookies(browser, enabled);
	}

	@Override
	public void 执行脚本(String script, final String mark) {
		requireLive();
		final long document = generation;
		agent.getJsAccessEntrace().callJs(script, new ValueCallback<String>() {
			@Override
			public void onReceiveValue(String result) {
				if (document == generation && active()) {
					脚本返回(mark, safe(result));
				}
			}
		});
	}

	@Override
	public void 注册接口(String name) {
		requireLive();
		if (name == null || !name.matches("[A-Za-z_$][A-Za-z0-9_$]*")) {
			throw new IllegalArgumentException("消息接口名称必须为合法 JavaScript 标识符。");
		}
		MessageBridge bridge = new MessageBridge(name);
		bridges.put(name, bridge);
		browser.addJavascriptInterface(bridge, name);
	}

	@Override
	public void 移除接口(String name) {
		requireLive();
		bridges.remove(name);
		browser.removeJavascriptInterface(name);
	}

	@Override
	public void 下载文件(String url, String path) {
		requireLive();
		http(url);
		if (path == null || path.isEmpty()) {
			throw new IllegalArgumentException("下载目标路径不能为空。");
		}
		File target = new File(path);
		if (!target.isAbsolute()) {
			target = new File(activity.getFilesDir(), path);
		}
		downloads.start(url, target);
	}

	@Override
	public boolean 取消下载(String url) {
		requireLive();
		return downloads.cancel(url);
	}

	@Override
	public void 置进度颜色(int color) {
		requireLive();
		((WebIndicator) agent.getWebCreator().offer()).setColor(color);
	}

	@Override
	public boolean 是否全屏() {
		requireLive();
		return fullscreen.isShowing();
	}

	@Override
	public void 退出全屏() {
		requireLive();
		if (fullscreen.exit()) {
			全屏改变(false);
		}
	}

	@Override
	public boolean 是否已销毁() {
		checkThread();
		return destroyed;
	}

	/**
	 * 网页导航、主框架错误和证书回调；保留 AgentWeb 内部错误页委托。
	 */
	private final class BrowserClient extends WebViewClient {

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
			BooleanReferenceParameter block = new BooleanReferenceParameter(false);
			if (request.isForMainFrame()) {
				即将跳转(request.getUrl().toString(), block);
			}
			// 上游把 file 链接视为未知协议，也会主动启动部分外部应用。
			// 本组件只承载 WebView 页面，放行本地页内导航并统一拒绝外部协议。
			return block.get() || !isPageUrl(request.getUrl().toString());
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap icon) {
			generation++;
			cancelFile();
			super.onPageStarted(view, url, icon);
			开始载入(safe(url));
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);
			载入完毕(safe(url));
		}

		@Override
		public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
			super.onReceivedError(view, request, error);
			if (request.isForMainFrame()) {
				载入失败(error.getErrorCode(), error.getDescription().toString(), request.getUrl().toString());
			}
		}

		@Override
		public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse response) {
			super.onReceivedHttpError(view, request, response);
			if (request.isForMainFrame()) {
				HTTP错误(response.getStatusCode(), request.getUrl().toString());
			}
		}

		@Override
		public void onReceivedSslError(WebView view, SslErrorHandler result, SslError error) {
			// 证书校验失败始终取消，不通过上游对话框提供绕过校验的入口。
			result.cancel();
			载入失败(-11, "网页证书校验失败。", error.getUrl());
		}
	}

	/**
	 * 页面标题、进度、文件选择和视频全屏回调，不覆盖 AgentWeb 其它 Chrome 逻辑。
	 */
	private final class BrowserChrome extends WebChromeClient {

		@Override
		public void onReceivedTitle(WebView view, String title) {
			super.onReceivedTitle(view, title);
			标题改变(safe(title));
		}

		@Override
		public void onProgressChanged(WebView view, int progress) {
			super.onProgressChanged(view, progress);
			进度改变(progress);
		}

		@Override
		public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback, FileChooserParams params) {
			cancelFile();
			if (!active()) {
				callback.onReceiveValue(null);
				return true;
			}
			final PendingFile request = new PendingFile(callback, generation);
			pendingFile = request;
			Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
			intent.addCategory(Intent.CATEGORY_OPENABLE);
			intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
			intent.setType("*/*");
			intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, params.getMode() == FileChooserParams.MODE_OPEN_MULTIPLE);
			List<String> types = new ArrayList<String>();
			String[] accepted = params.getAcceptTypes();
			if (accepted != null) {
				for (String type : accepted) {
					if (type != null && type.contains("/")) {
						types.add(type);
					}
				}
			}
			if (types.size() == 1) {
				intent.setType(types.get(0));
			} else if (!types.isEmpty()) {
				intent.putExtra(Intent.EXTRA_MIME_TYPES, types.toArray(new String[0]));
			}
			try {
				fileRequest = NEXT_FILE_REQUEST.getAndIncrement();
				activity.startActivityForResult(intent, fileRequest);
			} catch (RuntimeException exception) {
				request.onReceiveValue(null);
			}
			return true;
		}

		@Override
		public void onShowCustomView(View view, CustomViewCallback callback) {
			if (!active()) {
				callback.onCustomViewHidden();
			} else if (fullscreen.enter(view, callback)) {
				全屏改变(true);
			}
		}

		@Override
		public void onHideCustomView() {
			if (fullscreen.exit()) {
				全屏改变(false);
			}
		}

		@Override
		public void onPermissionRequest(PermissionRequest request) {
			// 本封装不代替应用授权网页摄像头或麦克风。
			request.deny();
		}

		@Override
		public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
			callback.invoke(origin, false, false);
		}
	}

	/**
	 * 一次文件选择的完成门，阻止取消、切窗与迟到的活动结果重复提交同一回调。
	 */
	private final class PendingFile implements ValueCallback<Uri[]> {

		private ValueCallback<Uri[]> callback;
		private final long document;

		/**
		 * 绑定本次页面及其文件回调。
		 *
		 * @param callback WebView 的一次性文件选择回调。
		 * @param document 创建时的页面代数。
		 */
		PendingFile(ValueCallback<Uri[]> callback, long document) {
			this.callback = callback;
			this.document = document;
		}

		@Override
		public void onReceiveValue(Uri[] values) {
			if (callback == null) {
				return;
			}
			ValueCallback<Uri[]> result = callback;
			callback = null;
			if (pendingFile == this) {
				pendingFile = null;
			}
			boolean valid = active() && document == generation;
			result.onReceiveValue(valid ? values : null);
			if (valid) {
				String[] paths = new String[values == null ? 0 : values.length];
				for (int i = 0; i < paths.length; i++) {
					paths[i] = values[i].toString();
				}
				文件选择完成(paths);
			}
		}
	}

	/**
	 * 固定字符串消息桥，不把任意 Java 对象或反射入口暴露给网页。
	 */
	public final class MessageBridge {

		private final String name;

		/**
		 * 创建与注册名称绑定的消息桥。
		 *
		 * @param name JavaScript 接口名称。
		 */
		MessageBridge(String name) {
			this.name = name;
		}

		/**
		 * 接收网页文本并切回界面线程，丢弃已离开页面的排队消息。
		 *
		 * @param message 网页消息。
		 */
		@JavascriptInterface
		public void postMessage(final String message) {
			final long document = generation;
			handler.post(new Runnable() {
				@Override
				public void run() {
					if (document == generation && active() && bridges.get(name) == MessageBridge.this) {
						接口调用(name, safe(message));
					}
				}
			});
		}
	}

	/**
	 * 取消仍未完成的网页文件选择。
	 */
	private void cancelFile() {
		fileRequest = 0;
		if (pendingFile != null) {
			pendingFile.onReceiveValue(null);
		}
	}

	@Override
	public void onActivityResult(int requestCode, final int resultCode, final Intent data) {
		if (destroyed || fileRequest == 0 || fileRequest != requestCode || pendingFile == null) {
			return;
		}
		final PendingFile request = pendingFile;
		fileRequest = 0;
		// 等宿主结束活动结果派发并恢复窗口后，再提交 WebView 文件回调。
		handler.post(new Runnable() {
			@Override
			public void run() {
				List<Uri> values = new ArrayList<Uri>();
				if (resultCode == Activity.RESULT_OK && data != null) {
					ClipData clips = data.getClipData();
					if (clips != null) {
						for (int i = 0; i < clips.getItemCount(); i++) {
							values.add(clips.getItemAt(i).getUri());
						}
					} else if (data.getData() != null) {
						values.add(data.getData());
					}
				}
				request.onReceiveValue(values.isEmpty() ? null : values.toArray(new Uri[0]));
			}
		});
	}

	/**
	 * 校验接口调用线程和实例存活状态。
	 */
	private void requireLive() {
		checkThread();
		if (destroyed) {
			throw new IllegalStateException("浏览器已销毁。");
		}
	}

	/**
	 * 所有公开操作都要求在 Android 界面线程执行。
	 */
	private static void checkThread() {
		if (Looper.myLooper() != Looper.getMainLooper()) {
			throw new IllegalStateException("浏览器必须在界面线程调用。");
		}
	}

	/**
	 * 查询组件仍属于当前可见窗口。
	 *
	 * @return 是否可向所属窗口派发异步事件。
	 */
	private boolean active() {
		return !destroyed && root.isAttachedToWindow() && root.isShown();
	}

	/**
	 * 将可空的平台文本转换为 Simple 文本。
	 *
	 * @param value 平台返回值。
	 * @return 非空文本。
	 */
	private static String safe(String value) {
		return value == null ? "" : value;
	}

	/**
	 * 统一主动加载与页内导航的协议边界，不将网页链接交给外部应用。
	 *
	 * @param value 完整页面地址。
	 * @return 是否属于 WebView 可承载的页面协议。
	 */
	private static boolean isPageUrl(String value) {
		if (value == null || value.isEmpty()) {
			return false;
		}
		try {
			URI uri = new URI(value);
			String scheme = uri.getScheme();
			if ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme)) {
				return uri.getRawAuthority() != null;
			}
			return "file".equalsIgnoreCase(scheme) || "content".equalsIgnoreCase(scheme)
					|| "about".equalsIgnoreCase(scheme) || "data".equalsIgnoreCase(scheme)
					|| "blob".equalsIgnoreCase(scheme);
		} catch (URISyntaxException exception) {
			return false;
		}
	}

	/**
	 * 校验网络地址，不将其它协议交给下载或 Cookie 模块。
	 *
	 * @param url HTTP 或 HTTPS 网址。
	 */
	private static void http(String url) {
		String scheme = url == null ? "" : Uri.parse(url).getScheme();
		if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
			throw new IllegalArgumentException("此操作仅接受 HTTP 或 HTTPS 网址。");
		}
	}

	/**
	 * 统一事件派发边界，隐藏窗口和销毁实例不再接收旧的异步结果。
	 *
	 * @param name 事件名称。
	 * @param values 事件参数。
	 */
	private void emit(String name, Object... values) {
		if (active()) {
			EventDispatcher.dispatchEvent(this, name, values);
		}
	}

	@Override
	public void 即将跳转(String url, BooleanReferenceParameter block) {
		emit("即将跳转", url, block);
	}

	@Override
	public void 开始载入(String url) {
		emit("开始载入", url);
	}

	@Override
	public void 载入完毕(String url) {
		emit("载入完毕", url);
	}

	@Override
	public void 载入失败(int code, String message, String url) {
		emit("载入失败", code, message, url);
	}

	@Override
	public void HTTP错误(int status, String url) {
		emit("HTTP错误", status, url);
	}

	@Override
	public void 标题改变(String title) {
		emit("标题改变", title);
	}

	@Override
	public void 进度改变(int progress) {
		emit("进度改变", progress);
	}

	@Override
	public void 脚本返回(String mark, String result) {
		emit("脚本返回", mark, result);
	}

	@Override
	public void 接口调用(String name, String message) {
		emit("接口调用", name, message);
	}

	@Override
	public void 文件选择完成(String[] uris) {
		emit("文件选择完成", (Object) uris);
	}

	@Override
	public void 下载已请求(String url, String userAgent, String disposition, String mime, long length) {
		emit("下载已请求", url, userAgent, disposition, mime, length);
	}

	@Override
	public void 下载进度(String url, long received, long total) {
		emit("下载进度", url, received, total);
	}

	@Override
	public void 下载完成(String url, String path) {
		emit("下载完成", url, path);
	}

	@Override
	public void 下载失败(String url, String message) {
		emit("下载失败", url, message);
	}

	@Override
	public void 下载已取消(String url) {
		emit("下载已取消", url);
	}

	@Override
	public void 全屏改变(boolean fullscreen) {
		emit("全屏改变", fullscreen);
	}

	@Override
	public void Cookie已写入(String url, boolean success) {
		emit("Cookie已写入", url, success);
	}

	@Override
	public void Cookie已清除() {
		emit("Cookie已清除");
	}

	@Override
	public void onPause() {
		if (!destroyed) {
			browser.onPause();
		}
	}

	@Override
	public void onResume() {
		if (active()) {
			browser.onResume();
		}
	}

	@Override
	public void onViewAttachedToWindow(View view) {
		if (!destroyed && activity.isInForeground()) {
			browser.onResume();
		}
	}

	@Override
	public void onViewDetachedFromWindow(View view) {
		if (!destroyed) {
			generation++;
			cancelFile();
			fullscreen.exit();
			downloads.cancelOwned();
			browser.stopLoading();
			browser.onPause();
		}
	}

	@Override
	public void 销毁() {
		checkThread();
		if (destroyed) {
			return;
		}
		destroyed = true;
		generation++;
		cancelFile();
		fullscreen.exit();
		downloads.cancelOwned();
		handler.removeCallbacksAndMessages(null);
		root.removeOnAttachStateChangeListener(this);
		activity.removeOnPauseListener(this);
		activity.removeOnResumeListener(this);
		activity.removeOnDestroyListener(this);
		activity.removeOnActivityResultListener(this);

		// 不使用上游会影响所有 WebView 的 pauseTimers/resumeTimers，逐实例释放。
		for (String name : bridges.keySet()) {
			browser.removeJavascriptInterface(name);
		}
		bridges.clear();
		browser.stopLoading();
		browser.setWebChromeClient(null);
		browser.setWebViewClient(null);
		ViewGroup parent = (ViewGroup) browser.getParent();
		if (parent != null) {
			parent.removeView(browser);
		}
		browser.removeAllViews();
		browser.destroy();
		root.removeAllViews();
		agent = null;
		super.销毁();
	}

	@Override
	public void onDestroy() {
		销毁();
	}
}
