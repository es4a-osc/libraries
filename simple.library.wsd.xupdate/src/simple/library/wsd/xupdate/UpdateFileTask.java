package simple.library.wsd.xupdate;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.xuexiang.xupdate.proxy.IUpdateHttpService;

/**
 * 与窗口和提示框无关的单个文件任务；普通传输及可替换下载共用校验和终止语义。
 * 界面观察者使用弱引用；前台服务只持有任务，不持有更新器、窗口或 Activity。
 *
 * @author 树先生 xhwsd@qq.com
 */
final class UpdateFileTask {

	/** 界面观察者；后台服务通过任务状态独立刷新通知。 */
	interface Listener {
		void progress(long received, long total, int percent);
		void completed(File file);
		void failed(Exception error, boolean resumable);
		void paused();
	}

	private final Context context;
	private final 更新信息 information;
	private final Map<String, String> headers;
	private final int timeout;
	private final File target;
	private final 下载器 downloader;
	private final 校验器 verifier;
	private final Handler main = new Handler(Looper.getMainLooper());
	private final ExecutorService worker = Executors.newSingleThreadExecutor();
	private final boolean resumable;
	private WeakReference<Listener> listener;
	private UpdateTransport.Task operation;
	private ResumableDownloader resumableDownloader;
	private long sequence;
	private long received;
	private long total;
	private int state;
	private int lastNotification = -2;
	private boolean background;
	private boolean pauseReady;

	/** 固定单次任务的数据和策略，后台任务不读取更新器的可变配置。 */
	UpdateFileTask(Context context, File directory, 更新信息 information, Map<String, String> headers,
			int timeout, 下载器 downloader, 校验器 verifier, Listener listener) {
		this.context = context.getApplicationContext();
		this.information = information.copy();
		this.headers = new LinkedHashMap<String, String>(headers);
		this.timeout = timeout;
		this.resumable = downloader instanceof ResumableDownloader;
		this.downloader = downloader;
		this.verifier = verifier;
		this.listener = new WeakReference<Listener>(listener);
		target = new File(directory, UUID.randomUUID() + ".apk");
		if (resumable) {
			resumableDownloader = (ResumableDownloader) downloader;
			resumableDownloader.configure(target, this.headers, timeout);
		}
	}

	/** 开始或恢复传输，实际完成必须经过文件校验。 */
	void start() {
		final long token = ++sequence;
		state = 更新器.状态_下载;
		pauseReady = false;
		operation = new UpdateTransport.Task();
		final UpdateTransport.Task active = operation;
		final IUpdateHttpService.DownloadCallback result = new IUpdateHttpService.DownloadCallback() {
			@Override
			public void onStart() {
				// 开始事件由更新器统一发送，避免队列或续传重复触发。
			}

			@Override
			public void onProgress(float progress, long length) {
				if (accepts(token)) {
					long count = (long) (progress * length);
					report(count, length, length > 0 ? (int) Math.min(99, count * 100.0 / length) : -1);
				}
			}

			@Override
			public void onSuccess(final File file) {
				if (accepts(token)) {
					verify(token, active, file);
				}
			}

			@Override
			public void onError(Throwable error) {
				failure(token, error instanceof Exception ? (Exception) error : new IOException(error));
			}
		};
		try {
			if (!target.getParentFile().isDirectory() && !target.getParentFile().mkdirs()) {
				throw new IOException("无法创建任务下载目录。");
			}
			if (downloader != null) {
				downloader.下载(information.copy(), new 下载回调(result, new 下载回调.Progress() {
					@Override
					public void received(long count, long length) {
						if (accepts(token)) {
							report(count, length, length > 0 ? (int) Math.min(99, count * 100.0 / length) : -1);
						}
					}
				}));
			} else {
				worker.execute(new Runnable() {
					@Override
					public void run() {
						try {
							更新信息 transferInfo = information.copy();
							if (verifier != null) {
								transferInfo.MD5("");
								transferInfo.文件大小(0);
							}
							final File file = UpdateTransport.download(active, transferInfo, target, headers, timeout,
									new UpdateTransport.Progress() {
								@Override
								public void received(final long count, final long length) {
									main.post(new Runnable() {
										@Override
										public void run() {
											if (accepts(token)) {
												report(count, length, length > 0 ? (int) Math.min(99, count * 100.0 / length) : -1);
											}
										}
									});
								}
							});
							main.post(new Runnable() {
								@Override
								public void run() {
									result.onSuccess(file);
								}
							});
						} catch (final Exception exception) {
							main.post(new Runnable() {
								@Override
								public void run() {
									failure(token, exception);
								}
							});
						}
					}
				});
			}
		} catch (Exception exception) {
			failure(token, exception);
		}
	}

	/** @return 当前传输是否可以请求暂停。 */
	boolean canPause() {
		return resumable && state == 更新器.状态_下载;
	}

	/** 请求暂停；只在收到下载器的实际停止确认后允许恢复。 */
	void pause() {
		if (resumableDownloader == null || state != 更新器.状态_下载) {
			throw new IllegalArgumentException("只有接入可暂停下载器的任务可以暂停。");
		}
		sequence++;
		operation.cancel();
		state = 更新器.状态_暂停;
		resumableDownloader.pause(new Runnable() {
			@Override
			public void run() {
				pauseReady = true;
				Listener observer = listener.get();
				if (observer != null) {
					observer.paused();
				}
				UpdateBackgroundService.changed(UpdateFileTask.this);
			}
		});
	}

	/** 已暂停或网络失败后恢复同一个文件和请求快照。 */
	void resume() {
		if (state != 更新器.状态_暂停 || !pauseReady || resumableDownloader == null) {
			throw new IllegalArgumentException("请等待暂停完成或可恢复的失败后继续。");
		}
		start();
	}

	/** 显式取消移除部分下载，已完成文件由清理缓存处理。 */
	void cancel() {
		sequence++;
		if (operation != null) {
			operation.cancel();
		}
		if (downloader != null) {
			downloader.取消();
		}
		if (state != 更新器.状态_就绪) {
			target.delete();
		}
		state = 更新器.状态_空闲;
		worker.shutdownNow();
		UpdateBackgroundService.finished(this, null, null);
	}

	/** 任务移交服务后，窗口关闭只解绑观察者。 */
	void detach() {
		listener.clear();
		if (!background) {
			cancel();
		}
	}

	/** 进入带通知的后台执行，默认传输不再依赖窗口生命周期。 */
	void background() {
		if (state != 更新器.状态_下载) {
			throw new IllegalArgumentException("请在下载过程中转入后台。");
		}
		UpdateBackgroundService.attach(context, this);
		try {
			if (downloader != null) {
				downloader.转入后台();
			}
			background = true;
		} catch (RuntimeException exception) {
			UpdateBackgroundService.finished(this, null, null);
			throw exception;
		}
	}

	/** @return 是否已移交后台服务。 */
	boolean isBackground() {
		return background;
	}

	/** @return 已写入字节数。 */
	long received() {
		return received;
	}

	/** @return 已知文件总大小。 */
	long total() {
		return total;
	}

	/** @return 下载或暂停状态。 */
	int state() {
		return state;
	}

	/** 校验结束前禁止发布 100% 或最终文件。 */
	private void verify(final long token, final UpdateTransport.Task active, final File source) {
		worker.execute(new Runnable() {
			@Override
			public void run() {
				try {
					UpdateFiles.copy(source, target);
					final File file = target;
					if (verifier == null && (resumable || downloader != null)) {
						UpdateTransport.verify(active, information, file);
					} else if (!file.isFile() || file.length() == 0) {
						throw new IOException("下载器未提交有效文件。");
					}
					main.post(new Runnable() {
						@Override
						public void run() {
							if (accepts(token)) {
								try {
									// Simple 的自定义校验在界面线程调用；默认摘要计算留在工作线程。
									if (verifier != null && !verifier.校验(file.getAbsolutePath(), information.copy())) {
										throw new IOException("自定义文件校验未通过。");
									}
								} catch (Exception exception) {
									file.delete();
									failure(token, exception, false);
									return;
								}
								state = 更新器.状态_就绪;
								try {
									report(file.length(), file.length(), 100);
									// 进度事件可显式取消；取消后不继续发送完成或生成安装通知。
									if (sequence == token && state == 更新器.状态_就绪) {
										Listener observer = listener.get();
										if (observer != null) {
											observer.completed(file);
										}
										UpdateBackgroundService.finished(UpdateFileTask.this, file, null);
									}
								} finally {
									worker.shutdown();
								}
							}
						}
					});
				} catch (final Exception exception) {
					target.delete();
					main.post(new Runnable() {
						@Override
						public void run() {
							failure(token, exception, false);
						}
					});
				}
			}
		});
	}

	/** 网络失败可保留续传记录，校验失败必须终止并移除损坏文件。 */
	private void failure(long token, Exception error) {
		failure(token, error, resumable);
	}

	/** 在单一终止边界更新状态与通知，不伪造下载完成。 */
	private void failure(long token, Exception error, boolean recoverable) {
		if (accepts(token)) {
			state = recoverable ? 更新器.状态_暂停 : 更新器.状态_空闲;
			pauseReady = recoverable;
			if (!recoverable) {
				if (downloader != null) {
					downloader.取消();
				}
				worker.shutdown();
			}
			Listener observer = listener.get();
			if (observer != null) {
				observer.failed(error, recoverable);
			}
			if (recoverable && background) {
				UpdateBackgroundService.changed(this);
			} else {
				UpdateBackgroundService.finished(this, null, error.getMessage());
			}
		}
	}

	/** 只接受仍在传输或校验中的本次回调。 */
	private boolean accepts(long token) {
		return token == sequence && state == 更新器.状态_下载;
	}

	/** 主线程统一发布进度。 */
	private void report(long count, long length, int percent) {
		received = count;
		total = length;
		Listener observer = listener.get();
		if (observer != null) {
			observer.progress(count, length, percent);
		}
		if (percent != lastNotification) {
			lastNotification = percent;
			UpdateBackgroundService.changed(this);
		}
	}
}
