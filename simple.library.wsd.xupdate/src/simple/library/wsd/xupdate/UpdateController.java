package simple.library.wsd.xupdate;

import static simple.library.wsd.xupdate.XU更新器.状态_空闲;
import static simple.library.wsd.xupdate.XU更新器.状态_检查;
import static simple.library.wsd.xupdate.XU更新器.状态_提示;
import static simple.library.wsd.xupdate.XU更新器.状态_下载;
import static simple.library.wsd.xupdate.XU更新器.状态_就绪;
import static simple.library.wsd.xupdate.XU更新器.状态_授权;
import static simple.library.wsd.xupdate.XU更新器.状态_安装;
import static simple.library.wsd.xupdate.XU更新器.状态_暂停;
import static simple.library.wsd.xupdate.XU更新器.阶段_检查;
import static simple.library.wsd.xupdate.XU更新器.阶段_下载;
import static simple.library.wsd.xupdate.XU更新器.阶段_安装;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;

import androidx.core.content.FileProvider;

import com.xuexiang.xupdate.XUpdate;
import com.xuexiang.xupdate.UpdateManager;
import com.xuexiang.xupdate.proxy.IUpdateHttpService;
import com.xuexiang.xupdate.entity.PromptEntity;
import com.xuexiang.xupdate.entity.UpdateEntity;
import com.xuexiang.xupdate.proxy.IPrompterProxy;
import com.xuexiang.xupdate.proxy.impl.DefaultUpdateParser;
import com.xuexiang.xupdate.service.OnFileDownloadListener;
import com.xuexiang.xupdate.utils.UpdateUtils;
import com.xuexiang.xupdate.widget.UpdateDialog;

import org.json.JSONObject;

import simple.runtime.android.MainActivity;
import simple.runtime.components.可视组件;
import simple.runtime.components.组件容器;
import simple.runtime.components.impl.组件Impl;
import simple.runtime.components.impl.android.视图组件;
import simple.runtime.events.EventDispatcher;

/**
 * 两个导出更新器共用的内部实现；独立管理每个实例的版本、网络、提示和安装状态。 下载、校验在后台执行；界面和事件仅由所属窗口的主线程操作。
 * 具体组件分别声明导出接口；共享基类不声明，避免组件发现把两个实现混在一起。
 *
 * @author 树先生 xhwsd@qq.com
 */
public abstract class UpdateController extends 组件Impl
		implements MainActivity.OnResumeListener, MainActivity.OnDestroyListener, View.OnAttachStateChangeListener {

	private static boolean initialized;
	private UpdateStages stages;
	private XU检查器 customChecker;
	private XU解析器 parser;
	private XU提示器 prompter;
	private XU下载器 downloader;
	private XU网络服务 network;
	private XU校验器 verifier;
	private XU安装器 installer;
	private boolean autoDownload;
	private boolean post = false;
	private boolean postJson;
	private boolean pauseReady;
	private UpdateFileTask fileTask;
	private UpdateFileTask detachedTask;
	private UpdateFileTask.Listener downloadEvents;
	private boolean customParser = false;
	private boolean autoPrompt = true;
	private boolean autoInstall = true;
	private boolean wifiOnly = false;
	private int timeout = 15000;
	private final MainActivity activity;
	private final Handler main = new Handler(Looper.getMainLooper());
	private final ExecutorService executor = Executors.newCachedThreadPool();
	private final Map<String, String> parameters = new LinkedHashMap<String, String>();
	private final Map<String, String> headers = new LinkedHashMap<String, String>();
	private final File directory;
	private final PromptEntity prompt = new PromptEntity().setIgnoreDownloadError(true);
	private View owner;
	private UpdateDialog dialog;
	private UpdateTransport.Task task;
	private XU更新信息 current;
	private File readyFile;
	private File pendingInstall;
	private long sequence;
	private int state;
	private boolean destroyed;
	private boolean lifecycleRegistered;
	private boolean suppressDismiss;

	/**
	 * 创建绑定到指定 Simple 窗口的更新器。
	 *
	 * @param container 所属 Simple 窗口。
	 */
	protected UpdateController(组件容器 container) {
		super(container);
		activity = MainActivity.getContext();
		directory = new File(activity.getCacheDir(), "xupdate/" + UUID.randomUUID());
		if (!initialized) {
			XUpdate.get().init((Application) activity.getApplicationContext());
			initialized = true;
		}
		registerLifecycle();
	}

	@Override
	public void 初始化() {
		if (owner == null) {
			可视组件 component = (可视组件) getComponentContainer();
			owner = ((视图组件) component).getView();
			owner.addOnAttachStateChangeListener(this);
		}
		super.初始化();
	}

	/**
	 * 异步检查版本；已有任务时拒绝启动第二个任务。
	 *
	 * @param address 版本检查接口的完整 HTTP 或 HTTPS 地址。
	 */
	public void 检查更新(final String address) {
		checkIdle();
		final UpdateTransport.Task active = begin(状态_检查);
		final long token = sequence;
		检查开始();
		if (!accepts(token)) {
			return;
		}
		try {
			checkNetwork();
			stages = createStages(address, token, active);
			stages.check();
		} catch (Exception exception) {
			fail(阶段_检查, exception);
		}
	}

	/**
	 * 解析已有响应；默认解析器比较本机版本，自定义解析器自行决定是否更新。
	 *
	 * @param response 默认 XUpdate JSON，或由解析响应事件处理的自定义内容。
	 */
	public void 解析更新(String response) {
		checkIdle();
		UpdateTransport.Task active = begin(状态_检查);
		stages = createStages("", sequence, active);
		stages.response(response);
	}

	/**
	 * 显示 XUpdate 原生更新提示，使用传入信息的快照。
	 *
	 * @param info 包含版本名称、下载地址和更新内容的信息。
	 */
	public void 显示更新(XU更新信息 info) {
		checkIdle();
		XU更新信息 snapshot = validate(info);
		begin(状态_提示);
		current = snapshot;
		showPrompt(sequence);
	}

	/**
	 * 直接下载，不要求存在更高版本；进度和已校验文件通过事件返回。
	 *
	 * @param info 具有下载地址和可选校验信息的版本信息。
	 */
	public void 下载更新(XU更新信息 info) {
		checkIdle();
		current = validateDownload(info);
		UpdateTransport.Task active = begin(状态_下载);
		stages = createStages("", sequence, active);
		stages.download(current);
	}

	/**
	 * 请求系统安装；需要时先打开安装来源授权页，恢复后继续。
	 *
	 * @param path 可读取的本地 APK 绝对路径。
	 */
	public void 安装更新(String path) {
		checkThread();
		if (state == 状态_检查 || state == 状态_下载 || state == 状态_暂停 || pendingInstall != null) {
			throw new IllegalArgumentException("请先完成或取消当前任务。");
		}
		File file = new File(path);
		if (!file.isAbsolute() || !file.isFile() || !file.canRead()) {
			throw new IllegalArgumentException("请选择可读取的本地 APK 文件。");
		}
		requestInstall(file);
	}

	/**
	 * 取消请求、下载或待授权安装并关闭提示，移除未完成续传记录；显式后台下载同样取消。
	 */
	public void 取消() {
		checkThread();
		boolean active = task != null || dialog != null || pendingInstall != null || fileTask != null
				|| detachedTask != null;
		if (detachedTask != null) {
			File completed = detachedTask.completedFile();
			if (completed != null) {
				readyFile = completed;
			}
			detachedTask.cancel();
			detachedTask = null;
		}
		cancelWork();
		if (active) {
			已取消();
		}
	}

	/**
	 * 暂停已接入可暂停下载器的任务，保留当前文件及请求配置；停止后触发已暂停。 尚未下载或下载器不支持暂停时拒绝调用。
	 */
	public void 暂停下载() {
		checkThread();
		if (fileTask == null || !fileTask.canPause()) {
			throw new IllegalArgumentException("没有可暂停的下载任务。");
		}
		state = 状态_暂停;
		pauseReady = false;
		fileTask.pause();
	}

	/**
	 * 恢复已暂停或网络失败的续传任务；沿用开始下载时的地址、请求头和安装配置。 须等待已暂停事件或可恢复的失败事件，不得在停止尚未完成时调用。
	 */
	public void 继续下载() {
		checkThread();
		if (fileTask == null || state != 状态_暂停 || !pauseReady) {
			throw new IllegalArgumentException("请等待暂停完成或可恢复失败后继续。");
		}
		try {
			checkNetwork();
			state = 状态_下载;
			pauseReady = false;
			下载开始();
			fileTask.resume();
		} catch (IOException exception) {
			state = 状态_暂停;
			pauseReady = true;
			失败(阶段_下载, exception.getMessage());
		}
	}

	/**
	 * 下载中关闭提示并转入后台通知；不表示允许静默安装。
	 */
	public void 转入后台() {
		checkThread();
		if (fileTask == null || state != 状态_下载) {
			throw new IllegalArgumentException("请在下载过程中转入后台。");
		}
		try {
			fileTask.background();
		} catch (RuntimeException exception) {
			失败(阶段_下载, exception.getMessage());
			return;
		}
		if (dialog != null) {
			suppressDismiss = true;
			dialog.dismiss();
			dialog = null;
			suppressDismiss = false;
		}
	}

	/**
	 * 移除本更新器的已下载文件和续传记录，不影响其它实例。 下载或安装进行中拒绝调用，请先暂停或取消。
	 */
	public void 清理缓存() {
		checkThread();
		if (detachedActive()) {
			throw new IllegalArgumentException("请先取消后台任务，再清理本实例缓存。");
		}
		if (task != null || dialog != null || pendingInstall != null || state == 状态_安装 || state == 状态_下载
				|| (state == 状态_暂停 && !pauseReady)) {
			throw new IllegalArgumentException("请先完成、暂停或取消当前任务。");
		}
		if (fileTask != null) {
			fileTask.cancel();
			fileTask = null;
		}
		if (detachedTask != null) {
			detachedTask.cancel();
			detachedTask = null;
		}
		cancelWork();
		readyFile = null;
		File[] files = directory.listFiles();
		if (files != null) {
			for (File file : files) {
				if (!file.delete() && file.exists()) {
					throw new IllegalArgumentException("无法移除本更新器的缓存：" + file.getName());
				}
			}
		}
	}

	/**
	 * 新增或替换版本检查参数；GET 使用查询串，POST 使用表单。
	 *
	 * @param name  非空参数名。
	 * @param value 参数文本，使用 UTF-8 编码。
	 */
	public void 置请求参数(String name, String value) {
		checkThread();
		if (name == null || name.isEmpty() || value == null) {
			throw new IllegalArgumentException("请求参数名称不能为空，值不能为 null。");
		}
		parameters.put(name, value);
	}

	/**
	 * 新增或替换检查与下载请求头；配置在任务开始时固定。
	 *
	 * @param name  合法 HTTP 请求头名称。
	 * @param value 不含换行的请求头值。
	 */
	public void 置请求头(String name, String value) {
		checkThread();
		if (name == null || !name.matches("[!#$%&'*+.^_`|~0-9A-Za-z-]+") || value == null || value.indexOf('\r') >= 0
				|| value.indexOf('\n') >= 0) {
			throw new IllegalArgumentException("请求头名称或值无效。");
		}
		headers.put(name, value);
	}

	/**
	 * 清空请求参数与请求头，不改变其它配置。
	 */
	public void 清空请求配置() {
		checkThread();
		parameters.clear();
		headers.clear();
	}

	/**
	 * 设置下次更新框的颜色、顶部图片及尺寸。
	 *
	 * @param themeColor  ARGB 颜色；-1 使用上游默认。
	 * @param textColor   按钮文字颜色；0 使用上游默认。
	 * @param topImage    drawable 资源标识；空文本使用上游默认。
	 * @param widthRatio  屏幕宽度比例，0 至 1；0 使用默认。
	 * @param heightRatio 屏幕高度比例，0 至 1；0 适应内容。
	 */
	public void 置提示样式(int themeColor, int textColor, String topImage, float widthRatio, float heightRatio) {
		checkThread();
		if (!Float.isFinite(widthRatio) || !Float.isFinite(heightRatio) || widthRatio < 0 || widthRatio > 1
				|| heightRatio < 0 || heightRatio > 1) {
			throw new IllegalArgumentException("提示框比例应在 0 至 1 之间。");
		}
		int image = 0;
		if (topImage != null && !topImage.isEmpty()) {
			String name = topImage.replaceFirst("^@", "");
			image = activity.getResources().getIdentifier(name, "drawable", activity.getPackageName());
			if (image == 0) {
				throw new IllegalArgumentException("未找到顶部图片：" + topImage);
			}
		}
		prompt.setThemeColor(themeColor).setButtonTextColor(textColor).setTopResId(image == 0 ? -1 : image)
				.setWidthRatio(widthRatio == 0 ? -1 : widthRatio).setHeightRatio(heightRatio == 0 ? -1 : heightRatio);
	}

	/**
	 * 清除 XUpdate 为当前应用记录的忽略版本。
	 */
	public void 清除忽略版本() {
		checkThread();
		UpdateUtils.saveIgnoreVersion(activity, "");
	}

	/**
	 * 当前状态，使用状态_* 常量比较。
	 *
	 * @return 当前状态，使用状态_* 常量比较。
	 */
	public int 状态() {
		checkThread();
		return detachedTask == null ? state : detachedTask.state();
	}

	/**
	 * 最后一次下载校验通过的私有 APK 路径，尚无文件时为空文本。
	 *
	 * @return 最后一次下载校验通过的私有 APK 路径，尚无文件时为空文本。
	 */
	public String 已下载文件() {
		checkThread();
		File completed = detachedTask == null ? null : detachedTask.completedFile();
		File available = completed == null ? readyFile : completed;
		return available == null ? "" : available.getAbsolutePath();
	}

	/**
	 * 创建独立的官方流程构建器，扩展层可替换入口而不复制检查、提示或安装流程。
	 *
	 * @param context 当前提示宿主。
	 * @param address 检查地址。
	 * @return 尚未配置本次策略的原生构建器。
	 */
	protected UpdateManager.Builder createUpdateBuilder(Context context, String address) {
		return XUpdate.newBuild(context, address);
	}

	/**
	 * 为一次请求组装官方流程，所有策略和参数均固定到本次实例。
	 *
	 * @param address 版本检查地址。
	 * @param token   当前任务序号。
	 * @param active  本次网络取消句柄。
	 * @return 由官方接口组合的更新调度器。
	 */
	private UpdateStages createStages(String address, final long token, UpdateTransport.Task active) {
		final XU解析器 selectedParser = parser;
		final XU网络服务 selectedNetwork = network;
		final Map<String, Object> params = new LinkedHashMap<String, Object>();
		params.putAll(parameters);
		final IUpdateHttpService defaults = new UpdateTransport(main, executor, active, headers, timeout, postJson);
		final String requestHeaders = new JSONObject(headers).toString();
		final boolean requestJson = postJson;
		IUpdateHttpService transport = defaults;
		if (selectedNetwork != null) {
			transport = new IUpdateHttpService() {
				@Override
				public void asyncGet(String url, Map<String, Object> values, Callback callback) {
					send(false, url, values, callback);
				}

				@Override
				public void asyncPost(String url, Map<String, Object> values, Callback callback) {
					send(true, url, values, callback);
				}

				/**
				 * 仅传递快照，第三方网络请求通过标准回调返回。
				 */
				private void send(boolean post, String url, Map<String, Object> values, final Callback callback) {
					selectedNetwork.请求(post, requestJson, url, new JSONObject(values).toString(), requestHeaders,
							new XU检查回调(new XU检查回调.Result() {
								@Override
								public void response(String value) {
									callback.onSuccess(value);
								}

								@Override
								public void information(XU更新信息 value) {
									callback.onError(new IOException("网络服务应提交原始响应；直接更新信息请使用检查器。"));
								}

								@Override
								public void failure(String message) {
									callback.onError(new IOException(message));
								}
							}));
				}

				@Override
				public void download(String url, String path, String name, DownloadCallback callback) {
					defaults.download(url, path, name, callback);
				}

				@Override
				public void cancelDownload(String url) {
					defaults.cancelDownload(url);
				}
			};
		}
		return new UpdateStages(createUpdateBuilder(activity, address), address, post, directory.getAbsolutePath(),
				transport, params, customChecker, new UpdateStages.Host() {
					@Override
					public boolean accepts() {
						return UpdateController.this.accepts(token);
					}

					@Override
					public UpdateEntity parse(String response) throws Exception {
						if (selectedParser != null) {
							XU更新信息 result = selectedParser.解析(response);
							return result == null ? null : UpdateStages.toEntity(result);
						}
						if (customParser) {
							XU更新信息 result = new XU更新信息();
							解析响应(response, result);
							return UpdateStages.toEntity(result);
						}
						return new DefaultUpdateParser().parseJson(response);
					}

					@Override
					public void checked() {
						if (accepts()) {
							检查结束();
						}
					}

					@Override
					public void found(XU更新信息 info) {
						task = null;
						current = validate(info);
						state = 状态_就绪;
						发现更新(current.copy());
						if (accepts()) {
							if (autoDownload) {
								UpdateController.this.download(token, null);
							} else if (autoPrompt) {
								showPrompt(token);
							}
						}
					}

					@Override
					public void none() {
						task = null;
						state = 状态_空闲;
						无需更新();
					}

					@Override
					public void failure(Exception error) {
						if (accepts()) {
							fail(阶段_检查, error);
						}
					}

					@Override
					public void download(XU更新信息 info, OnFileDownloadListener listener) {
						if (accepts()) {
							current = validateDownload(info);
							UpdateController.this.download(token, listener);
						}
					}

					@Override
					public void cancelDownload() {
						UpdateController.this.取消();
					}

					@Override
					public void background() {
						UpdateController.this.转入后台();
					}

					@Override
					public boolean ignored(XU更新信息 info) {
						return !info.强制更新() && info.允许忽略() && UpdateUtils.isIgnoreVersion(activity, info.版本名());
					}
				});
	}

	/**
	 * 验证并复制供提示或下载使用的版本数据。
	 *
	 * @param info 调用方提供的信息。
	 * @return 不与调用方共享修改的数据。
	 * @throws IllegalArgumentException 信息缺失或字段不合法。
	 */
	private XU更新信息 validate(XU更新信息 info) {
		if (info == null || !info.有更新() || info.版本名().isEmpty()) {
			throw new IllegalArgumentException("请提供有效的更新信息和版本名称。");
		}
		return validateDownload(info);
	}

	/**
	 * 下载只验证文件信息，不与更新状态或本机版本绑定。
	 *
	 * @param info 待下载版本信息。
	 * @return 满足下载地址、大小及摘要约束的信息副本。
	 */
	private XU更新信息 validateDownload(XU更新信息 info) {
		if (info == null || info.文件大小() < 0
				|| (verifier == null && !info.MD5().isEmpty() && !info.MD5().matches("[0-9A-Fa-f]{32}"))) {
			throw new IllegalArgumentException("下载信息、文件大小或摘要格式无效。");
		}
		try {
			UpdateTransport.url(info.下载地址());
		} catch (IOException exception) {
			throw new IllegalArgumentException(exception.getMessage(), exception);
		}
		return info.copy();
	}

	/**
	 * 显示原生更新框，仅使用上游公开的提示代理，不替换进程全局监听器。
	 *
	 * @param token 所属任务序号。
	 */
	private void showPrompt(final long token) {
		if (prompter != null) {
			state = 状态_就绪;
			prompter.显示(current.copy());
			return;
		}
		final UpdateDialog[] reference = new UpdateDialog[1];
		try {
			task = null;
			UpdateEntity entity = new UpdateEntity().setHasUpdate(true).setVersionCode(current.版本号())
					.setVersionName(current.版本名()).setUpdateContent(current.更新内容()).setDownloadUrl(current.下载地址())
					.setSize(current.文件大小() / 1024).setMd5(current.MD5()).setForce(current.强制更新())
					.setIsIgnorable(current.允许忽略() && !current.强制更新()).setApkCacheDir(directory.getAbsolutePath())
					.setIsSilent(false).setIsAutoInstall(false);
			Context context = new ContextThemeWrapper(activity, R.style.XUpdateSimpleTheme);
			dialog = UpdateDialog.newInstance(context, entity, new IPrompterProxy() {
				@Override
				public String getUrl() {
					// 提示状态由本实例管理，空键使上游跳过进程级地址状态表。
					return "";
				}

				@Override
				public void startDownload(UpdateEntity value, OnFileDownloadListener listener) {
					if (!destroyed && dialog == reference[0] && state != 状态_下载) {
						if (state == 状态_暂停 && fileTask != null) {
							继续下载();
						} else {
							download(sequence, listener);
						}
					}
				}

				@Override
				public void backgroundDownload() {
					转入后台();
				}

				@Override
				public void cancelDownload() {
					if (dialog == reference[0]) {
						取消();
					}
				}

				@Override
				public void recycle() {
					// 上游关闭或忽略只回收界面，下载结果及调用方配置仍由本实例持有。
				}
			}, prompt);
			reference[0] = dialog;
			dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface value) {
					if (!suppressDismiss && accepts(token) && state == 状态_提示) {
						state = 状态_空闲;
					}
					if (dialog == value) {
						dialog = null;
					}
				}
			});
			state = 状态_提示;
			dialog.show();
			applyPromptSize();
		} catch (Exception exception) {
			fail(阶段_检查, exception);
		}
	}

	/**
	 * 将用户指定的比例应用到可见卡片，避免上游图片固有宽度限制卡片宽度。 高度只约束提示窗口，内容超过可用空间时由上游滚动区承载。
	 */
	private void applyPromptSize() {
		Window window = dialog.getWindow();
		if (window == null || (prompt.getWidthRatio() <= 0 && prompt.getHeightRatio() <= 0)) {
			return;
		}
		DisplayMetrics metrics = activity.getResources().getDisplayMetrics();
		int width = prompt.getWidthRatio() > 0 ? Math.round(metrics.widthPixels * prompt.getWidthRatio())
				: window.getAttributes().width;
		int height = prompt.getHeightRatio() > 0 ? Math.round(metrics.heightPixels * prompt.getHeightRatio())
				: window.getAttributes().height;
		window.setLayout(width, height);
		if (prompt.getWidthRatio() > 0) {
			View image = dialog.findViewById(com.xuexiang.xupdate.R.id.iv_top);
			ViewGroup card = (ViewGroup) image.getParent();
			ViewGroup.LayoutParams layout = card.getLayoutParams();
			layout.width = ViewGroup.LayoutParams.MATCH_PARENT;
			card.setLayoutParams(layout);
			ViewGroup.LayoutParams imageLayout = image.getLayoutParams();
			imageLayout.width = ViewGroup.LayoutParams.MATCH_PARENT;
			image.setLayoutParams(imageLayout);
		}
	}

	/**
	 * 提示和独立下载共用文件任务；任务只弱引用界面观察者。
	 *
	 * @param token    当前流程序号。
	 * @param listener 原生提示框的进度接收者。
	 */
	private void download(final long token, final OnFileDownloadListener listener) {
		if (fileTask != null) {
			fileTask.cancel();
		}
		try {
			checkNetwork();
			state = 状态_下载;
			task = null;
			downloadEvents = createDownloadEvents(token, listener, autoInstall);
			fileTask = new UpdateFileTask(activity.getApplicationContext(), directory, current, headers, timeout,
					downloader, verifier, downloadEvents);
			if (listener != null) {
				listener.onStart();
			}
			下载开始();
			if (accepts(token)) {
				fileTask.start();
			}
		} catch (Exception exception) {
			fail(阶段_下载, exception);
		}
	}

	/**
	 * 为本次界面会话创建结果桥接；任务仅弱引用桥接，离页后不持有窗口。
	 *
	 * @param token    当前界面任务序号。
	 * @param listener 原生提示框观察者；独立页面或重新附着时为空。
	 * @param install  非后台任务完成后是否请求系统安装。
	 * @return 当前会话的下载事件桥接。
	 */
	private UpdateFileTask.Listener createDownloadEvents(final long token, final OnFileDownloadListener listener,
			final boolean install) {
		return new UpdateFileTask.Listener() {
			@Override
			public void progress(long received, long total, int percent) {
				if (accepts(token) && owner != null && owner.isShown()) {
					if (listener != null && dialog != null) {
						listener.onProgress(percent < 0 ? 0 : percent / 100f, total);
					}
					下载进度(received, total, percent);
				}
			}

			@Override
			public void completed(File file) {
				if (accepts(token)) {
					readyFile = file;
					state = 状态_就绪;
					if (listener != null && dialog != null) {
						listener.onCompleted(file);
						bindInstallButton(file);
					}
					下载完成(file.getAbsolutePath());
					// 后台完成交给通知，由用户决定何时打开安装界面。
					if (accepts(token) && install && !fileTask.isBackground() && pendingInstall == null
							&& state != 状态_安装) {
						requestInstall(file);
					}
				}
			}

			@Override
			public void failed(Exception error, boolean recoverable) {
				if (accepts(token)) {
					state = recoverable ? 状态_暂停 : 状态_空闲;
					pauseReady = recoverable;
					if (listener != null && dialog != null) {
						listener.onError(error);
					}
					bindResumeButton();
					失败(阶段_下载, error.getMessage());
				}
			}

			@Override
			public void paused() {
				if (accepts(token)) {
					state = 状态_暂停;
					pauseReady = true;
					bindResumeButton();
					已暂停();
				}
			}
		};
	}

	/**
	 * 暂停或可恢复失败后保留原生提示框的继续入口，不要求调用方关闭更新框。
	 */
	private void bindResumeButton() {
		if (dialog != null && dialog.isShowing() && state == 状态_暂停 && pauseReady) {
			Button button = dialog.findViewById(com.xuexiang.xupdate.R.id.btn_update);
			button.setText("继续下载");
			button.setVisibility(View.VISIBLE);
			button.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					继续下载();
				}
			});
		}
	}

	/**
	 * 强制更新框完成后继续显示安装按钮，使其走本实例的授权流程。
	 *
	 * @param file 已校验 APK。
	 */
	private void bindInstallButton(final File file) {
		if (dialog != null && dialog.isShowing()) {
			Button button = dialog.findViewById(com.xuexiang.xupdate.R.id.btn_update);
			button.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					requestInstall(file);
				}
			});
		}
	}

	/**
	 * 检查 APK 所属包，必要时进入安装来源授权，再交给系统安装器。
	 *
	 * @param file 本实例已完成校验的文件。
	 */
	private void requestInstall(File file) {
		try {
			PackageInfo archive = activity.getPackageManager().getPackageArchiveInfo(file.getPath(), 0);
			if (archive == null) {
				throw new IOException("请选择有效的 APK 文件。");
			}
			if (installer != null) {
				if (!installer.安装(file.getAbsolutePath())) {
					throw new IOException("自定义安装器未接管安装请求。");
				}
				state = 状态_安装;
				已请求安装(file.getAbsolutePath());
				return;
			}
			File cache = new File(activity.getCacheDir(), "xupdate").getCanonicalFile();
			if (!file.getCanonicalPath().startsWith(cache.getPath() + File.separator)) {
				stageInstall(file);
				return;
			}
			if (Build.VERSION.SDK_INT >= 26 && !activity.getPackageManager().canRequestPackageInstalls()) {
				pendingInstall = file;
				state = 状态_授权;
				activity.startActivity(new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
						Uri.parse("package:" + activity.getPackageName())));
				安装授权(file.getAbsolutePath());
				return;
			}
			launchInstaller(file);
		} catch (Exception exception) {
			fail(阶段_安装, exception);
		}
	}

	/**
	 * 将外部可读 APK 复制到私有共享目录，不扩大 FileProvider 的暴露范围。
	 *
	 * @param source 可读取的原始 APK 文件。
	 */
	private void stageInstall(final File source) {
		final long token = ++sequence;
		state = 状态_安装;
		final File destination = new File(directory, UUID.randomUUID() + ".apk");
		executor.execute(() -> {
			try {
				UpdateFiles.copy(source, destination);
				main.post(() -> {
					if (accepts(token)) {
						requestInstall(destination);
					} else {
						destination.delete();
					}
				});
			} catch (final Exception exception) {
				main.post(() -> {
					if (accepts(token)) {
						fail(阶段_安装, exception);
					}
				});
			}
		});
	}

	/**
	 * 以只读的内容 URI 请求系统安装，保留系统最终确认。
	 *
	 * @param file 已校验 APK。
	 */
	private void launchInstaller(File file) {
		Uri uri = FileProvider.getUriForFile(activity, activity.getPackageName() + ".updateFileProvider", file);
		Intent intent = new Intent(Intent.ACTION_VIEW).setDataAndType(uri, "application/vnd.android.package-archive")
				.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		activity.startActivity(intent);
		pendingInstall = null;
		state = 状态_安装;
		已请求安装(file.getAbsolutePath());
	}

	@Override
	public void onResume() {
		if (pendingInstall != null && !destroyed) {
			File file = pendingInstall;
			pendingInstall = null;
			if (Build.VERSION.SDK_INT >= 26 && !activity.getPackageManager().canRequestPackageInstalls()) {
				fail(阶段_安装, new IOException("未允许本应用请求安装，请授权后重试。"));
			} else {
				requestInstall(file);
			}
		} else if (state == 状态_安装) {
			state = 状态_就绪;
		}
	}

	@Override
	public void onDestroy() {
		销毁();
	}

	@Override
	public void onViewAttachedToWindow(View view) {
		if (!destroyed) {
			registerLifecycle();
			restoreBackgroundTask();
		}
	}

	@Override
	public void onViewDetachedFromWindow(View view) {
		releaseWindow();
		unregisterLifecycle();
	}

	@Override
	public void 销毁() {
		if (destroyed) {
			return;
		}
		releaseWindow();
		destroyed = true;
		detachedTask = null;
		executor.shutdownNow();
		unregisterLifecycle();
		if (owner != null) {
			owner.removeOnAttachStateChangeListener(this);
			owner = null;
		}
		super.销毁();
	}

	/**
	 * 注册活动生命周期；重复初始化或视图重新附着不得重复登记。
	 */
	private void registerLifecycle() {
		if (!lifecycleRegistered) {
			activity.addOnDestroyListener(this);
			activity.addOnResumeListener(this);
			lifecycleRegistered = true;
		}
	}

	/**
	 * 解除活动对离开窗口的引用，允许不再使用的窗口被回收。
	 */
	private void unregisterLifecycle() {
		if (lifecycleRegistered) {
			activity.removeOnDestroyListener(this);
			activity.removeOnResumeListener(this);
			lifecycleRegistered = false;
		}
	}

	/**
	 * 在开始操作前验证窗口和线程，拒绝销毁或离开窗口后的调用。
	 *
	 * @throws IllegalArgumentException 当前线程或实例状态不允许操作。
	 */
	protected final void checkThread() {
		if (Looper.myLooper() != Looper.getMainLooper() || destroyed) {
			// 使用 Simple 可映射的异常类型，使调用方能够在错误处理块中恢复。
			throw new IllegalArgumentException("更新器须在界面线程且未销毁时调用。");
		}
	}

	/**
	 * 配置可在窗口初始化阶段设置，不要求视图已经附着；活动任务期间不可换策略。
	 */
	private void checkConfiguration() {
		checkThread();
		if (detachedActive() || task != null || dialog != null || pendingInstall != null || state == 状态_下载
				|| state == 状态_安装 || state == 状态_暂停) {
			throw new IllegalArgumentException("请先完成或取消当前任务，再替换更新策略。");
		}
	}

	/**
	 * 拒绝同一实例的重入任务，允许独立实例分别工作。
	 *
	 * @throws IllegalArgumentException 所属窗口不可见或已有任务。
	 */
	private void checkIdle() {
		checkThread();
		if (owner == null || !owner.isAttachedToWindow() || !owner.isShown()) {
			throw new IllegalArgumentException("请在所属 Simple 窗口显示后调用更新器。");
		}
		if (detachedActive() || task != null || dialog != null || pendingInstall != null || state == 状态_下载
				|| state == 状态_安装 || state == 状态_暂停) {
			throw new IllegalArgumentException("请先完成或取消当前更新任务。");
		}
	}

	/**
	 * 为新操作分配序号，后续取消和重入均会使旧回调失效。
	 *
	 * @param next 新状态。
	 * @return 新的网络取消句柄。
	 */
	private UpdateTransport.Task begin(int next) {
		sequence++;
		state = next;
		task = new UpdateTransport.Task();
		return task;
	}

	/**
	 * 判断主线程中的结果是否仍属于当前任务。
	 *
	 * @param token 任务开始时的序号。
	 * @return 未销毁且序号仍匹配。
	 */
	private boolean accepts(long token) {
		return !destroyed && token == sequence;
	}

	/**
	 * 解绑窗口观察者；显式后台任务由服务持有，其余任务沿用取消语义。
	 */
	private void releaseWindow() {
		if (fileTask != null && fileTask.isBackground()) {
			fileTask.detach();
			detachedTask = fileTask;
			fileTask = null;
		}
		cancelWork();
	}

	/**
	 * 判断离页托管的任务是否仍未终止，防止删除在用文件或替换活动策略。
	 *
	 * @return 后台任务正在下载或暂停时为真。
	 */
	private boolean detachedActive() {
		return detachedTask != null && (detachedTask.state() == 状态_下载 || detachedTask.state() == 状态_暂停);
	}

	/**
	 * 缓存窗口重新显示时接回原任务；使用新序号过滤旧桥接并回放当前状态。
	 */
	private void restoreBackgroundTask() {
		if (detachedTask == null) {
			return;
		}
		final UpdateFileTask restored = detachedTask;
		detachedTask = null;
		fileTask = restored;
		state = restored.state();
		pauseReady = restored.canResume();
		final long token = ++sequence;
		downloadEvents = createDownloadEvents(token, null, false);
		restored.attach(downloadEvents);
		final File completed = restored.completedFile();
		final boolean publishCompletion = completed != null && !completed.equals(readyFile);
		if (completed != null) {
			readyFile = completed;
		}
		main.post(() -> {
			if (!accepts(token) || fileTask != restored || owner == null || !owner.isShown()) {
				return;
			}
			if (publishCompletion) {
				downloadEvents.completed(completed);
			} else if (restored.state() == 状态_下载 || restored.state() == 状态_暂停) {
				long total = restored.total();
				downloadEvents.progress(restored.received(), total,
						total > 0 ? (int) Math.min(99, restored.received() * 100.0 / total) : -1);
				if (restored.canResume()) {
					downloadEvents.paused();
				}
			}
		});
	}

	/**
	 * 静默取消当前任务、关闭原生界面并清除待授权安装。
	 */
	private void cancelWork() {
		sequence++;
		if (stages != null) {
			stages.cancel();
			stages = null;
		}
		if (network != null) {
			network.取消();
		}
		if (fileTask != null) {
			fileTask.cancel();
			fileTask = null;
		}
		downloadEvents = null;
		pauseReady = false;
		if (task != null) {
			task.cancel();
			task = null;
		}
		pendingInstall = null;
		state = 状态_空闲;
		if (dialog != null) {
			suppressDismiss = true;
			dialog.dismiss();
			dialog = null;
			suppressDismiss = false;
		}
	}

	/**
	 * 终止失败流程并发送可重试的错误说明。
	 *
	 * @param stage     检查、下载或安装阶段。
	 * @param exception 实际异常。
	 */
	private void fail(int stage, Exception exception) {
		cancelWork();
		String message = exception.getMessage();
		失败(stage, message == null ? exception.getClass().getSimpleName() : message);
	}

	/**
	 * 核对开始请求时的网络要求，不申请位置或 Wi-Fi 控制权限。
	 *
	 * @throws IOException 未满足仅 Wi-Fi 条件。
	 */
	private void checkNetwork() throws IOException {
		if (wifiOnly) {
			ConnectivityManager manager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkCapabilities capabilities = manager.getNetworkCapabilities(manager.getActiveNetwork());
			if (capabilities == null || !capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
				throw new IOException("当前网络不是 Wi-Fi。");
			}
		}
	}

	/**
	 * 替换本实例的检查器；仅可在空闲时设置，空对象恢复默认实现。
	 *
	 * @param value 自定义检查器。
	 */
	public void 置检查器(XU检查器 value) {
		checkConfiguration();
		customChecker = value;
	}

	/**
	 * 替换本实例的解析器；仅可在空闲时设置，空对象恢复默认实现。
	 *
	 * @param value 自定义解析器。
	 */
	public void 置解析器(XU解析器 value) {
		checkConfiguration();
		parser = value;
	}

	/**
	 * 替换本实例的提示器；仅可在空闲时设置，空对象恢复默认实现。
	 *
	 * @param value 自定义提示器。
	 */
	public void 置提示器(XU提示器 value) {
		checkConfiguration();
		prompter = value;
	}

	/**
	 * 替换本实例的下载器；仅可在空闲时设置，空对象恢复默认实现。
	 *
	 * @param value 自定义下载器。
	 */
	public void 置下载器(XU下载器 value) {
		checkConfiguration();
		downloader = value;
	}

	/**
	 * 替换本实例的网络服务；仅可在空闲时设置，空对象恢复默认实现。
	 *
	 * @param value 自定义网络服务。
	 */
	public void 置网络服务(XU网络服务 value) {
		checkConfiguration();
		network = value;
	}

	/**
	 * 替换本实例的校验器；仅可在空闲时设置，空对象恢复默认实现。
	 *
	 * @param value 自定义校验器。
	 */
	public void 置校验器(XU校验器 value) {
		checkConfiguration();
		verifier = value;
	}

	/**
	 * 替换本实例的安装器；仅可在空闲时设置，空对象恢复默认实现。
	 *
	 * @param value 自定义安装器。
	 */
	public void 置安装器(XU安装器 value) {
		checkConfiguration();
		installer = value;
	}

	/**
	 * 取得发现更新后是否跳过提示直接下载，默认关闭。
	 *
	 * @return 是否自动下载。
	 */
	public boolean 自动下载() {
		checkThread();
		return autoDownload;
	}

	/**
	 * 设置是否自动下载；是否请求安装仍由下载后安装独立控制。
	 *
	 * @param value 是否自动下载。
	 */
	public void 自动下载(boolean value) {
		checkThread();
		autoDownload = value;
	}

	/**
	 * 检查请求已经结束；之后仍可能解析、提示或下载，不代表更新完成。
	 */
	public void 检查结束() {
		EventDispatcher.dispatchEvent(this, "检查结束");
	}

	/**
	 * 取得是否用 POST 表单检查；默认为 GET。
	 *
	 * @return 是否用 POST 表单检查；默认为 GET。
	 */
	public boolean 使用POST() {
		checkThread();
		return post;
	}

	/**
	 * 设置是否用 POST 表单检查；默认为 GET。
	 *
	 * @param value 是否用 POST 表单检查；默认为 GET。
	 */
	public void 使用POST(boolean value) {
		checkThread();
		post = value;
	}

	/**
	 * 取得 POST 是否发送 JSON 对象；默认为假，只在使用 POST 时生效。
	 *
	 * @return 是否使用 JSON 请求体。
	 */
	public boolean POST使用JSON() {
		checkThread();
		return postJson;
	}

	/**
	 * 设置 POST 是否发送 UTF-8 JSON 对象，参数值保持文本；假表示表单。
	 *
	 * @param value 是否使用 JSON 请求体。
	 */
	public void POST使用JSON(boolean value) {
		checkThread();
		postJson = value;
	}

	/**
	 * 下载已经停止且续传记录已保留，可以调用继续下载；不表示下载完成。
	 */
	public void 已暂停() {
		EventDispatcher.dispatchEvent(this, "已暂停");
	}

	/**
	 * 取得是否通过解析响应事件填写更新信息。
	 *
	 * @return 是否通过解析响应事件填写更新信息。
	 */
	public boolean 自定义解析() {
		checkThread();
		return customParser;
	}

	/**
	 * 设置是否通过解析响应事件填写更新信息。
	 *
	 * @param value 是否通过解析响应事件填写更新信息。
	 */
	public void 自定义解析(boolean value) {
		checkThread();
		customParser = value;
	}

	/**
	 * 取得检查发现更新后是否自动显示更新框。
	 *
	 * @return 检查发现更新后是否自动显示更新框。
	 */
	public boolean 自动提示() {
		checkThread();
		return autoPrompt;
	}

	/**
	 * 设置检查发现更新后是否自动显示更新框。
	 *
	 * @param value 检查发现更新后是否自动显示更新框。
	 */
	public void 自动提示(boolean value) {
		checkThread();
		autoPrompt = value;
	}

	/**
	 * 取得下载完成后是否请求系统安装；设置为假可先取得 APK。
	 *
	 * @return 下载完成后是否请求系统安装；设置为假可先取得 APK。
	 */
	public boolean 下载后安装() {
		checkThread();
		return autoInstall;
	}

	/**
	 * 设置下载完成后是否请求系统安装；设置为假可先取得 APK。
	 *
	 * @param value 下载完成后是否请求系统安装；设置为假可先取得 APK。
	 */
	public void 下载后安装(boolean value) {
		checkThread();
		autoInstall = value;
	}

	/**
	 * 取得开始检查和下载时是否要求当前网络为 Wi-Fi；不自动切换网络。
	 *
	 * @return 开始检查和下载时是否要求当前网络为 Wi-Fi；不自动切换网络。
	 */
	public boolean 仅WiFi() {
		checkThread();
		return wifiOnly;
	}

	/**
	 * 设置开始检查和下载时是否要求当前网络为 Wi-Fi；不自动切换网络。
	 *
	 * @param value 开始检查和下载时是否要求当前网络为 Wi-Fi；不自动切换网络。
	 */
	public void 仅WiFi(boolean value) {
		checkThread();
		wifiOnly = value;
	}

	/**
	 * 取得连接及连续读取的超时毫秒数，范围 1000 至 120000。
	 *
	 * @return 连接及连续读取的超时毫秒数，范围 1000 至 120000。
	 */
	public int 请求超时() {
		checkThread();
		return timeout;
	}

	/**
	 * 设置连接及连续读取的超时毫秒数，范围 1000 至 120000。
	 *
	 * @param value 连接及连续读取的超时毫秒数，范围 1000 至 120000。
	 */
	public void 请求超时(int value) {
		checkThread();
		if (value < 1000 || value > 120000) {
			throw new IllegalArgumentException("超时应为 1000 至 120000 毫秒。");
		}
		timeout = value;
	}

	/**
	 * 版本请求即将发出，事件在界面线程触发。
	 */
	public void 检查开始() {
		EventDispatcher.dispatchEvent(this, "检查开始");
	}

	/**
	 * 启用自定义解析后触发；默认有更新为假，必须明确填写有效信息。
	 *
	 * @param response 原始响应内容。
	 * @param info     在本事件内填写；事件返回后读取其快照。
	 */
	public void 解析响应(String response, XU更新信息 info) {
		EventDispatcher.dispatchEvent(this, "解析响应", response, info);
	}

	/**
	 * 存在高于本机的未忽略版本，之后按自动提示配置决定是否显示更新框。
	 *
	 * @param info 独立的版本信息，修改不影响当前提示或下载。
	 */
	public void 发现更新(XU更新信息 info) {
		EventDispatcher.dispatchEvent(this, "发现更新", info);
	}

	/**
	 * 服务端无更新、本机版本已足够或此版本被用户忽略。
	 */
	public void 无需更新() {
		EventDispatcher.dispatchEvent(this, "无需更新");
	}

	/**
	 * 下载任务已启动，进度事件在界面线程返回。
	 */
	public void 下载开始() {
		EventDispatcher.dispatchEvent(this, "下载开始");
	}

	/**
	 * 下载期间的进度；只有写入完成且校验通过后才报告 100%。
	 *
	 * @param received 已接收的字节数。
	 * @param total    预期字节数；未知时为零。
	 * @param percent  0 至 100；总大小未知时为 -1。
	 */
	public void 下载进度(long received, long total, int percent) {
		EventDispatcher.dispatchEvent(this, "下载进度", received, total, percent);
	}

	/**
	 * APK 下载和可选大小、MD5 校验完成；不表示已经安装。
	 *
	 * @param path 已写入完成且校验通过的私有缓存 APK 绝对路径。
	 */
	public void 下载完成(String path) {
		EventDispatcher.dispatchEvent(this, "下载完成", path);
	}

	/**
	 * 已打开允许此来源安装应用的系统设置；拒绝后报告失败，可重新请求。
	 *
	 * @param path 等待本应用安装来源授权的已校验 APK。
	 */
	public void 安装授权(String path) {
		EventDispatcher.dispatchEvent(this, "安装授权", path);
	}

	/**
	 * 系统安装界面已打开；用户仍可拒绝或取消，不表示安装成功。
	 *
	 * @param path 交给系统安装器的 APK 路径。
	 */
	public void 已请求安装(String path) {
		EventDispatcher.dispatchEvent(this, "已请求安装", path);
	}

	/**
	 * 活动任务被用户或程序取消；切换窗口时静默释放，不回调旧窗口。
	 */
	public void 已取消() {
		EventDispatcher.dispatchEvent(this, "已取消");
	}

	/**
	 * 请求、解析、下载、校验或安装请求失败。
	 *
	 * @param stage   阶段_检查、阶段_下载或阶段_安装。
	 * @param message 失败原因，任务已停止；可修正配置后重试。
	 */
	public void 失败(int stage, String message) {
		EventDispatcher.dispatchEvent(this, "失败", stage, message);
	}

}
