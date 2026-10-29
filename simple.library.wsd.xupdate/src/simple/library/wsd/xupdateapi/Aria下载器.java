package simple.library.wsd.xupdateapi;

import java.io.File;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import android.content.Context;

import com.xuexiang.xupdate.proxy.IUpdateHttpService;

import simple.library.wsd.xupdate.ResumableDownloader;
import simple.library.wsd.xupdate.下载回调;
import simple.library.wsd.xupdate.更新信息;
import simple.runtime.android.MainActivity;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.UsesPermissions;

/**
 * XUpdate 的 Aria 续传下载策略；设置到更新器后复用其进度、校验和后台通知。
 * 每个对象独立管理一个下载，不修改上游进程级默认下载器。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
@UsesPermissions(permissionNames =
	// 下载指定地址的更新文件。
	"android.permission.INTERNET," +
	// Aria 核对传输所需的当前网络状态。
	"android.permission.ACCESS_NETWORK_STATE"
)
public final class Aria下载器 implements ResumableDownloader {

	private final Context context;
	private File target;
	private UpdateDownload operation;
	private Map<String, String> headers = Collections.emptyMap();
	private int timeout = 15000;

	/** 创建不持有窗口的策略对象；通常由快捷更新器自动配置。 */
	public Aria下载器() {
		context = MainActivity.getContext().getApplicationContext();
	}

	@Override
	public void configure(File target, Map<String, String> headers, int timeout) {
		取消();
		this.target = target;
		this.headers = new LinkedHashMap<String, String>(headers);
		this.timeout = timeout;
	}

	/**
	 * 开始或恢复 APK 下载；缺少真实 MD5 时报告失败。
	 *
	 * @param info 下载地址与摘要信息。
	 * @param callback 本次下载的进度与结果。
	 */
	@Override
	@SimpleFunction
	public void 下载(更新信息 info, final 下载回调 callback) {
		if (info.MD5().isEmpty()) {
			callback.报告失败("断点续传必须提供真实 APK MD5。");
			return;
		}
		if (target == null) {
			target = new File(context.getCacheDir(), "xupdateapi/" + UUID.randomUUID() + ".apk");
		}
		if (operation == null) {
			operation = new UpdateDownload(context, headers, timeout);
		}
		operation.download(info.下载地址(), target.getParent(), target.getName(), new IUpdateHttpService.DownloadCallback() {
			@Override
			public void onStart() {
				// 核心更新器统一通知开始，不重复触发。
			}

			@Override
			public void onProgress(float progress, long total) {
				// Aria 探测长度或退回完整下载时可能仍携带旧总长，不以无效总长计算百分比。
				long received = Math.max(0, operation.received());
				callback.报告进度(received, total > 0 && received <= total ? total : 0);
			}

			@Override
			public void onSuccess(File file) {
				callback.提交文件(file.getAbsolutePath());
			}

			@Override
			public void onError(Throwable error) {
				callback.报告失败(error.getMessage() == null ? error.getClass().getSimpleName() : error.getMessage());
			}
		});
	}

	@Override
	public void pause(Runnable complete) {
		if (operation == null) {
			throw new IllegalArgumentException("没有可暂停的 Aria 任务。");
		}
		operation.pause(complete);
	}

	/** 取消当前任务，移除未完成记录；已完成文件留给更新器清理。 */
	@Override
	@SimpleFunction
	public void 取消() {
		if (operation != null) {
			operation.cancel();
			operation = null;
		}
	}

	/** 由所属更新器托管后台通知；传输本身只持有应用上下文。 */
	@Override
	@SimpleFunction
	public void 转入后台() {
		// 核心前台服务管理通知和任务寿命，此处不创建第二套服务或窗口引用。
	}
}
