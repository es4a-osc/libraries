package simple.library.wsd.agentweb;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import com.download.library.DownloadImpl;
import com.download.library.DownloadListenerAdapter;
import com.download.library.Extra;
import com.download.library.ResourceRequest;

/**
 * 管理单个浏览器拥有的下载，不取消其它组件或应用任务。
 * 下载库按网址识别任务，同网址已有任务时明确拒绝，不抢占已有任务。
 *
 * @author 树先生 xhwsd@qq.com
 */
final class DownloadTasks {

	/**
	 * 将下载结果统一交给界面线程上的组件。
	 */
	interface Listener {
		/**
		 * 下载进度变化。
		 *
		 * @param url 下载网址。
		 * @param received 已接收字节数。
		 * @param total 总字节数。
		 */
		void progress(String url, long received, long total);

		/**
		 * 下载已结束。
		 *
		 * @param url 下载网址。
		 * @param path 目标路径。
		 * @param error 失败异常；成功时为空。
		 */
		void finished(String url, String path, Throwable error);

		/**
		 * 任务已接受取消请求。
		 *
		 * @param url 下载网址。
		 */
		void cancelled(String url);
	}

	private final Context context;
	private final Listener listener;
	private final Handler handler = new Handler(Looper.getMainLooper());
	private final Map<String, ResourceRequest<?>> requests = new LinkedHashMap<String, ResourceRequest<?>>();

	/**
	 * 绑定当前浏览框的下载监听器，不持有 Activity。
	 *
	 * @param context 应用上下文。
	 * @param listener 当前浏览框的监听器。
	 */
	DownloadTasks(Context context, Listener listener) {
		this.context = context.getApplicationContext();
		this.listener = listener;
	}

	/**
	 * 创建明确目标的下载；不覆盖已有文件，也不触发自动打开。
	 *
	 * @param url 已校验的 HTTP 或 HTTPS 网址。
	 * @param target 不存在的目标文件，其父目录必须可写。
	 */
	void start(final String url, final File target) {
		DownloadImpl downloader = DownloadImpl.getInstance(context);
		if (requests.containsKey(url) || downloader.exist(url)) {
			throw new IllegalStateException("此网址已有下载任务：" + url);
		}
		if (target.exists() || target.getParentFile() == null || !target.getParentFile().isDirectory()) {
			throw new IllegalArgumentException("下载文件已存在或父目录不存在。");
		}

		// 上游 target 会吞掉文件创建异常；在交付任务前原子预留目标，避免回退到默认目录。
		try {
			if (!target.createNewFile()) {
				throw new IllegalArgumentException("下载目标已被其它操作占用。");
			}
		} catch (IOException exception) {
			throw new IllegalArgumentException("无法创建下载目标文件。", exception);
		}
		final ResourceRequest<?> request = downloader.with(url).target(target).setUniquePath(false)
				.setEnableIndicator(false).closeAutoOpen().setConnectTimeOut(15000).setRetry(0);
		// 明确使用调用方路径，禁止上游再按服务器 Content-Disposition 重命名或复用其它文件。
		request.setContentDisposition("attachment");
		requests.put(url, request);
		try {
			request.enqueue(new DownloadListenerAdapter() {
				@Override
				public void onProgress(String address, final long received, final long total, long time) {
					handler.post(new Runnable() {
						@Override
						public void run() {
							if (requests.get(url) == request) {
								listener.progress(url, received, total);
							}
						}
					});
				}

				@Override
				public boolean onResult(final Throwable error, Uri path, String address, Extra extra) {
					// 上游在回调返回后清空任务字段，必须在此保存结果，不在排队任务中重读 DownloadTask。
					final File completed = path != null && "file".equals(path.getScheme())
							&& path.getPath() != null ? new File(path.getPath()) : null;
					handler.post(new Runnable() {
						@Override
						public void run() {
							if (requests.get(url) == request) {
								requests.remove(url);
								Throwable failure = error;
								if (failure == null && (!target.isFile()
										|| !target.getAbsoluteFile().equals(completed))) {
									failure = new IllegalStateException("下载未写入指定目标文件。");
								}
								listener.finished(url, target.getAbsolutePath(), failure);
							}
						}
					});
					return false;
				}
			});
		} catch (RuntimeException exception) {
			requests.remove(url);
			throw exception;
		}
	}

	/**
	 * 只取消当前浏览框创建且尚未结束的任务。
	 *
	 * @param url 任务网址。
	 * @return 是否找到并取消。
	 */
	boolean cancel(String url) {
		ResourceRequest<?> request = requests.remove(url);
		if (request == null) {
			return false;
		}
		DownloadImpl.getInstance(context).cancel(url);
		listener.cancelled(url);
		return true;
	}

	/**
	 * 取消当前浏览框的全部下载，阻止已经排队的旧进度继续派发。
	 */
	void cancelOwned() {
		for (String url : new ArrayList<String>(requests.keySet())) {
			cancel(url);
		}
		handler.removeCallbacksAndMessages(null);
	}
}
