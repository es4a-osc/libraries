package simple.library.wsd.xupdate;

import java.io.IOException;
import java.util.Map;

import com.xuexiang.xupdate.UpdateManager;
import com.xuexiang.xupdate.entity.PromptEntity;
import com.xuexiang.xupdate.entity.UpdateEntity;
import com.xuexiang.xupdate.proxy.IUpdateDownloader;
import com.xuexiang.xupdate.proxy.IUpdateHttpService;
import com.xuexiang.xupdate.proxy.IUpdatePrompter;
import com.xuexiang.xupdate.proxy.IUpdateProxy;
import com.xuexiang.xupdate.proxy.impl.AbstractUpdateParser;
import com.xuexiang.xupdate.proxy.impl.DefaultUpdateChecker;
import com.xuexiang.xupdate.service.OnFileDownloadListener;

/**
 * 把实例检查、解析、提示和下载适配到官方 UpdateManager；流程不改写进程级默认实现。
 * 各环节通过官方接口组合，取消后的结果由所属实例拒绝。
 *
 * @author 树先生 xhwsd@qq.com
 */
final class UpdateStages {

	/** 窗口适配层消费结果，内部接口不进入 Simple 清单。 */
	interface Host {
		boolean accepts();
		UpdateEntity parse(String response) throws Exception;
		void checked();
		void found(更新信息 info);
		void none();
		void failure(Exception error);
		void download(更新信息 info, OnFileDownloadListener listener);
		void cancelDownload();
		void background();
		boolean ignored(更新信息 info);
	}

	private final Host host;
	private final 检查器 checker;
	private final Map<String, Object> parameters;
	private final UpdateManager manager;

	/**
	 * 为一次流程创建官方调度器和独立适配器。
	 *
	 * @param builder 本次流程独立使用的原生构建器。
	 * @param address 检查地址，直接解析时可为空。
	 * @param post 是否使用 POST。
	 * @param directory APK 缓存目录。
	 * @param transport 本次网络服务。
	 * @param parameters 检查参数快照。
	 * @param checker 可替换检查器，空值使用默认网络检查。
	 * @param host 本次流程的事件和生命周期边界。
	 */
	UpdateStages(UpdateManager.Builder builder, String address, boolean post, String directory,
			IUpdateHttpService transport, Map<String, Object> parameters, 检查器 checker, Host host) {
		this.host = host;
		this.checker = checker;
		this.parameters = parameters;
		manager = builder.updateUrl(address)
				.isGet(!post).isWifiOnly(false).isAutoMode(false)
				.apkCacheDir(directory).params(parameters).updateHttpService(transport)
				.updateChecker(new Checker()).updateParser(new Parser())
				.updatePrompter(new Prompter()).updateDownLoader(new Downloader()).build();
	}

	/** 开始检查，网络条件和开始事件由宿主在调用前处理。 */
	void check() {
		manager.checkVersion();
	}

	/**
	 * 把外部已有响应送入同一解析和分流链。
	 *
	 * @param response 原始响应。
	 */
	void response(String response) {
		new Checker().processCheckResult(response, manager);
	}

	/**
	 * 直接提交已经解析的信息，不执行默认解析器的本机版本判断。
	 *
	 * @param info 更新信息。
	 */
	void information(更新信息 info) {
		if (!host.accepts()) {
			return;
		}
		try {
			if (info == null) {
				host.failure(new IOException("解析器未返回更新信息。"));
			} else if (!info.有更新() || host.ignored(info)) {
				host.none();
			} else {
				manager.findNewVersion(toEntity(info), manager);
			}
		} catch (Exception exception) {
			host.failure(exception);
		}
	}

	/** 取消仅属于本次流程的可替换检查器。 */
	void cancel() {
		if (checker != null) {
			checker.取消();
		}
	}

	/**
	 * 保留官方更新信息媒介，大小在边界处由字节转换为 KB。
	 *
	 * @param info 独立的更新信息。
	 * @return 提示和下载接口使用的原生对象。
	 */
	static UpdateEntity toEntity(更新信息 info) {
		return new Entity(info).setHasUpdate(info.有更新()).setVersionCode(info.版本号())
				.setVersionName(info.版本名()).setUpdateContent(info.更新内容())
				.setDownloadUrl(info.下载地址()).setSize(info.文件大小() / 1024)
				.setMd5(info.MD5()).setForce(info.强制更新())
				.setIsIgnorable(info.允许忽略() && !info.强制更新())
				.setIsSilent(false).setIsAutoInstall(false);
	}


	/**
	 * 直接下载不经过版本检查；仍由官方调度器选取下载实现。
	 *
	 * @param info 下载地址与校验信息。
	 */
	void download(更新信息 info) {
		manager.startDownload(toEntity(info), null);
	}

	/** 保留 Simple 以字节表示的精确大小，不从官方界面使用的 KB 反向恢复。 */
	private static 更新信息 snapshot(UpdateEntity entity) {
		return entity instanceof Entity ? ((Entity) entity).information.copy() : 更新信息.fromEntity(entity);
	}

	/** 原生通信媒介携带精确快照，内部类型不导出。 */
	private static final class Entity extends UpdateEntity {
		private final 更新信息 information;

		Entity(更新信息 information) {
			this.information = information.copy();
		}
	}

	/** 官方检查接口的实例适配，避免以 URL 为键的全局检查状态串入。 */
	private final class Checker extends DefaultUpdateChecker {
		@Override
		public void checkVersion(boolean isGet, String url, Map<String, Object> values, final IUpdateProxy proxy) {
			final 检查回调 result = new 检查回调(new 检查回调.Result() {
				@Override
				public void response(String value) {
					if (host.accepts()) {
						proxy.onAfterCheck();
						processCheckResult(value, proxy);
					}
				}

				@Override
				public void information(更新信息 value) {
					if (host.accepts()) {
						proxy.onAfterCheck();
						UpdateStages.this.information(value);
					}
				}

				@Override
				public void failure(String message) {
					if (host.accepts()) {
						proxy.onAfterCheck();
						host.failure(new IOException(message));
					}
				}
			});
			if (checker != null) {
				checker.检查(url, result);
				return;
			}
			IUpdateHttpService.Callback callback = new IUpdateHttpService.Callback() {
				@Override
				public void onSuccess(String response) {
					result.提交响应(response);
				}

				@Override
				public void onError(Throwable error) {
					result.报告失败(error.getMessage());
				}
			};
			// 仅使用开始时的参数快照，不继承其它代码注入的全局参数。
			if (isGet) {
				proxy.getIUpdateHttpService().asyncGet(url, parameters, callback);
			} else {
				proxy.getIUpdateHttpService().asyncPost(url, parameters, callback);
			}
		}

		@Override
		public void onAfterCheck() {
			host.checked();
		}

		@Override
		public void processCheckResult(String result, IUpdateProxy proxy) {
			if (host.accepts()) {
				try {
					UpdateEntity entity = proxy.parseJson(result);
					information(entity == null ? null : snapshot(entity));
				} catch (Exception exception) {
					host.failure(exception);
				}
			}
		}
	}

	/** 解析策略只负责协议转换，不混入下载、提示或安装状态。 */
	private final class Parser extends AbstractUpdateParser {
		@Override
		public UpdateEntity parseJson(String response) throws Exception {
			return host.parse(response);
		}
	}

	/** 官方提示接口只把已确认的信息交给当前宿主。 */
	private final class Prompter implements IUpdatePrompter {
		@Override
		public void showPrompt(UpdateEntity entity, IUpdateProxy proxy, PromptEntity prompt) {
			if (host.accepts()) {
				host.found(snapshot(entity));
			}
		}
	}

	/** 官方下载接口与实际传输实现分离，默认下载与替换下载共用结果校验。 */
	private final class Downloader implements IUpdateDownloader {
		@Override
		public void startDownload(UpdateEntity entity, OnFileDownloadListener listener) {
			host.download(snapshot(entity), listener);
		}

		@Override
		public void cancelDownload() {
			host.cancelDownload();
		}

		@Override
		public void backgroundDownload() {
			host.background();
		}
	}
}
