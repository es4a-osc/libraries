package simple.library.wsd.xupdateapi;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.common.HttpOption;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.scheduler.TaskSchedulers;
import com.arialyy.aria.core.task.DownloadTask;
import com.xuexiang.xupdate.aria.AriaDownloadServiceProxyImpl;
import com.xuexiang.xupdate.proxy.IUpdateHttpService;

/**
 * XUpdateAPI 的 Aria 下载适配：按任务 ID 和私有路径隔离监听，暂停保留记录，取消移除记录。
 * 不修改 Aria 全局超时；每个实例以无进度定时器约束连接和读取等待。
 *
 * @author 树先生 xhwsd@qq.com
 */
final class UpdateDownload extends AriaDownloadServiceProxyImpl {

	// Aria 默认按秒汇报进度，额外允许一个报告周期，避免 1 秒超时与报告消息竞态。
	private static final int REPORT_INTERVAL = 1000;

	// Aria 的活动队列以 URL 为键，同地址并发必须明确拒绝，不能串用另一个实例的任务。
	private static final Map<String, UpdateDownload> ACTIVE = new HashMap<String, UpdateDownload>();
	private final Handler main = new Handler(Looper.getMainLooper());
	private final Map<String, String> headers;
	private final int timeout;
	private IUpdateHttpService.DownloadCallback callback;
	private File file;
	private String address;
	private long taskId = -1;
	private long received;
	private long lastReceived;
	private Runnable paused;
	private boolean registered;
	private boolean stopping;
	private boolean removing;
	private boolean completed;
	private final Runnable watchdog = new Runnable() {
		@Override
		public void run() {
			if (callback != null && !stopping) {
				if (received == lastReceived) {
					final IUpdateHttpService.DownloadCallback target = callback;
					pause(new Runnable() {
						@Override
						public void run() {
							target.onError(new IOException("APK 下载等待超时，可稍后继续。"));
						}
					});
				} else {
					lastReceived = received;
					main.postDelayed(this, timeout + REPORT_INTERVAL);
				}
			}
		}
	};

	/**
	 * 创建使用应用上下文的独立下载代理，不持有窗口或 Activity。
	 *
	 * @param context 应用上下文。
	 * @param headers 本次请求头快照。
	 * @param timeout 无数据进展的最长等待毫秒数。
	 */
	UpdateDownload(Context context, Map<String, String> headers, int timeout) {
		super(context.getApplicationContext());
		this.headers = new HashMap<String, String>(headers);
		this.timeout = timeout;
	}

	@Override
	public void download(String url, String path, String fileName, IUpdateHttpService.DownloadCallback target) {
		if (stopping) {
			throw new IllegalArgumentException("下载正在停止，请在暂停事件后继续。");
		}
		UpdateDownload other = ACTIVE.get(url);
		if (other != null && other != this) {
			throw new IllegalArgumentException("另一个更新器正在下载相同地址，请等待其完成或暂停。");
		}
		address = url;
		file = new File(path, fileName);
		if (!file.getParentFile().isDirectory() && !file.getParentFile().mkdirs()) {
			throw new IllegalArgumentException("无法创建私有下载目录。");
		}
		callback = target;
		removing = false;
		completed = false;
		ACTIVE.put(url, this);
		Aria.download(this).register();
		registered = true;
		List<DownloadEntity> records = Aria.download(this).getDownloadEntity(url);
		taskId = -1;
		if (records != null) {
			for (DownloadEntity record : records) {
				if (file.getAbsolutePath().equals(record.getFilePath())) {
					taskId = record.getId();
					received = record.getCurrentProgress();
					if (record.isComplete() && file.isFile()) {
						completed = true;
						recycle();
						callback = null;
						target.onSuccess(file);
						return;
					}
					break;
				}
			}
		}
		HttpOption option = new HttpOption();
		if (!headers.isEmpty()) {
			option.addHeaders(headers);
		}
		lastReceived = received;
		main.postDelayed(watchdog, timeout + REPORT_INTERVAL);
		if (taskId < 0) {
			taskId = Aria.download(this).load(url).setFilePath(file.getAbsolutePath())
					.option(option).ignoreCheckPermissions().create();
		} else {
			Aria.download(this).load(taskId).option(option).ignoreCheckPermissions().resume();
		}
		if (taskId < 0) {
			recycle();
			callback = null;
			throw new IllegalArgumentException("Aria 未能创建下载任务。");
		}
	}

	/**
	 * 请求暂停，原生停止确认后再通知调用方，避免停止与立即继续相互覆盖。
	 *
	 * @param complete 停止完成通知，可为空。
	 */
	void pause(Runnable complete) {
		paused = complete;
		cancelDownload(address);
	}

	@Override
	public void cancelDownload(String url) {
		callback = null;
		main.removeCallbacks(watchdog);
		if (registered && taskId >= 0) {
			stopping = true;
			Aria.download(this).load(taskId).ignoreCheckPermissions().stop();
		} else {
			finishStop();
		}
	}

	/**
	 * 取消并移除本实例的记录及临时文件；不会触及其他更新器的文件。
	 */
	void clear() {
		callback = null;
		paused = null;
		removing = true;
		main.removeCallbacks(watchdog);
		if (taskId >= 0) {
			if (registered) {
				stopping = true;
				Aria.download(this).load(taskId).ignoreCheckPermissions().cancel(true);
			} else {
				Aria.download(this).load(taskId).ignoreCheckPermissions().cancel(true);
				taskId = -1;
				recycle();
			}
		} else {
			recycle();
		}
	}

	/**
	 * 停止本次流程；已完成文件留给安装或显式清理，未完成记录则移除。
	 */
	void cancel() {
		if (completed) {
			callback = null;
			paused = null;
			if (taskId >= 0) {
				// 已完成时只移除记录，保留已经验证的文件供稍后安装或显式清理。
				Aria.download(this).load(taskId).ignoreCheckPermissions().cancel(false);
				taskId = -1;
			}
			recycle();
		} else {
			clear();
		}
	}

	/**
	 * 返回原生任务实际写入的字节数，避免从浮点百分比反推造成精度丢失。
	 *
	 * @return 已写入字节数。
	 */
	long received() {
		return received;
	}

	/**
	 * 用任务 ID 和路径共同核对回调来源，同 URL 的其它任务不会进入当前实例。
	 *
	 * @param task 原生任务。
	 * @return 是否属于本下载代理。
	 */
	private boolean matches(DownloadTask task) {
		return task != null && task.getEntity().getId() == taskId && file != null
				&& file.getAbsolutePath().equals(task.getFilePath());
	}

	/**
	 * 释放地址租约和监听，停止后的记录仍可由后续 download 恢复。
	 */
	private void recycle() {
		main.removeCallbacks(watchdog);
		if (registered) {
			Aria.download(this).unRegister();
			TaskSchedulers.getInstance().unRegister(this);
			registered = false;
		}
		if (ACTIVE.get(address) == this) {
			ACTIVE.remove(address);
		}
	}

	/**
	 * 在收到原生停止确认后通知暂停完成。
	 */
	private void finishStop() {
		stopping = false;
		if (removing) {
			taskId = -1;
		}
		recycle();
		Runnable notification = paused;
		paused = null;
		if (notification != null) {
			notification.run();
		}
	}

	@Override
	public void onTaskRunning(DownloadTask task) {
		if (matches(task) && callback != null && !stopping) {
			received = task.getCurrentProgress();
			callback.onProgress(task.getPercent() / 100f, task.getFileSize());
		}
	}

	@Override
	public void onTaskComplete(DownloadTask task) {
		if (matches(task)) {
			completed = true;
			IUpdateHttpService.DownloadCallback target = callback;
			callback = null;
			received = task.getCurrentProgress();
			recycle();
			if (target != null && !stopping) {
				target.onSuccess(file);
			} else if (stopping) {
				finishStop();
			}
		}
	}

	@Override
	public void onTaskFail(DownloadTask task, Exception exception) {
		if (matches(task)) {
			IUpdateHttpService.DownloadCallback target = callback;
			callback = null;
			recycle();
			if (stopping) {
				finishStop();
			} else if (target != null) {
				target.onError(exception == null ? new IOException("Aria 下载失败。") : exception);
			}
		}
	}

	@Override
	public void onTaskStop(DownloadTask task) {
		if (matches(task)) {
			received = task.getCurrentProgress();
			finishStop();
		}
	}

	@Override
	public void onTaskCancel(DownloadTask task) {
		if (matches(task)) {
			finishStop();
		}
	}

	@Override
	public void onPre(DownloadTask task) {
		// 组件在调用 download 前统一发送下载开始事件，原生排队不重复发送。
	}

	@Override
	public void onNoSupportBreakPoint(DownloadTask task) {
		// Aria 对不支持 Range 的服务器使用完整下载，最终仍必须通过大小和 MD5 校验。
	}
}
