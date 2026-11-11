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
 * 与窗口和提示框无关的单个文件任务；普通传输及可替换下载共用校验和终止语义。 界面观察者使用弱引用；前台服务只持有任务，不持有更新器、窗口或 Activity。
 *
 * @author 树先生 xhwsd@qq.com
 */
final class UpdateFileTask {

	/**
	 * 界面观察者；后台服务通过任务状态独立刷新通知。
	 */
	interface Listener {
		/**
		 * 接收主线程发布的字节进度，校验完成后才会报告百分之百。
		 *
		 * @param received 已收到的字节数。
		 * @param total    总字节数；未知时为零。
		 * @param percent  百分比；未知时为负一。
		 */
		void progress(long received, long total, int percent);

		/**
		 * 接收已复制至任务私有路径且通过校验的文件。
		 *
		 * @param file 本次完整文件。
		 */
		void completed(File file);

		/**
		 * 接收实际失败及是否保留可续传状态。
		 *
		 * @param error     失败异常。
		 * @param resumable 是否仍可在暂停状态继续下载。
		 */
		void failed(Exception error, boolean resumable);

		/**
		 * 下载器实际暂停后通知界面，此时才允许继续。
		 */
		void paused();
	}

	private final Context context;
	private final XU更新信息 information;
	private final Map<String, String> headers;
	private final int timeout;
	private final File target;
	private final XU下载器 downloader;
	private final XU校验器 verifier;
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

	/**
	 * 固定单次任务的数据和策略，后台任务不读取更新器的可变配置。
	 *
	 * @param context     应用上下文，不持有窗口。
	 * @param directory   本次任务专属下载目录。
	 * @param information 本次下载与校验的信息快照。
	 * @param headers     请求头快照。
	 * @param timeout     请求最长等待时间，单位毫秒。
	 * @param downloader  可替换下载器；为空时使用默认流式下载。
	 * @param verifier    可替换校验器；为空时使用默认大小和摘要校验。
	 * @param listener    接收界面事件的观察者，内部使用弱引用。
	 */
	UpdateFileTask(Context context, File directory, XU更新信息 information, Map<String, String> headers, int timeout,
			XU下载器 downloader, XU校验器 verifier, Listener listener) {
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

	/**
	 * 开始或恢复传输，实际完成必须经过文件校验。
	 */
	void start() {
		final long token = ++sequence;
		state = XU更新器.状态_下载;
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
				downloader.下载(information.copy(), new XU下载回调(result, new XU下载回调.Progress() {
					@Override
					public void received(long count, long length) {
						if (accepts(token)) {
							report(count, length, length > 0 ? (int) Math.min(99, count * 100.0 / length) : -1);
						}
					}
				}));
			} else {
				worker.execute(() -> {
					try {
						XU更新信息 transferInfo = information.copy();
						if (verifier != null) {
							transferInfo.MD5("");
							transferInfo.文件大小(0);
						}
						final File file = UpdateTransport.download(active, transferInfo, target, headers, timeout,
								new UpdateTransport.Progress() {
									@Override
									public void received(final long count, final long length) {
										main.post(() -> {
											if (accepts(token)) {
												report(count, length,
														length > 0 ? (int) Math.min(99, count * 100.0 / length) : -1);
											}
										});
									}
								});
						main.post(() -> {
							result.onSuccess(file);
						});
					} catch (final Exception exception) {
						main.post(() -> {
							failure(token, exception);
						});
					}
				});
			}
		} catch (Exception exception) {
			failure(token, exception);
		}
	}

	/**
	 * @return 当前传输是否可以请求暂停。
	 */
	boolean canPause() {
		return resumable && state == XU更新器.状态_下载;
	}

	/**
	 * 请求暂停；只在收到下载器的实际停止确认后允许恢复。
	 */
	void pause() {
		if (resumableDownloader == null || state != XU更新器.状态_下载) {
			throw new IllegalArgumentException("只有接入可暂停下载器的任务可以暂停。");
		}
		sequence++;
		operation.cancel();
		state = XU更新器.状态_暂停;
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

	/**
	 * 已暂停或网络失败后恢复同一个文件和请求快照。
	 */
	void resume() {
		if (state != XU更新器.状态_暂停 || !pauseReady || resumableDownloader == null) {
			throw new IllegalArgumentException("请等待暂停完成或可恢复的失败后继续。");
		}
		start();
	}

	/**
	 * 显式取消移除部分下载，已完成文件由清理缓存处理。
	 */
	void cancel() {
		sequence++;
		if (operation != null) {
			operation.cancel();
		}
		if (downloader != null) {
			downloader.取消();
		}
		if (state != XU更新器.状态_就绪) {
			target.delete();
		}
		state = XU更新器.状态_空闲;
		worker.shutdownNow();
		UpdateBackgroundService.finished(this, null, null);
	}

	/**
	 * 任务移交服务后，窗口关闭只解绑观察者。
	 */
	void detach() {
		listener.clear();
		if (!background) {
			cancel();
		}
	}

	/**
	 * 将已有任务的观察者接回重新显示的窗口，不重新启动或复制传输。
	 *
	 * @param observer 当前窗口会话的下载事件桥接。
	 */
	void attach(Listener observer) {
		listener = new WeakReference<>(observer);
	}

	/**
	 * 查询已完成且仍存在的私有文件。
	 *
	 * @return 完成校验后的文件；尚未完成或文件已失效时为空。
	 */
	File completedFile() {
		return state == XU更新器.状态_就绪 && target.isFile() ? target : null;
	}

	/**
	 * 查询下载器是否已经确认暂停，避免在停止过程中提前恢复。
	 *
	 * @return 已暂停且可继续时为真。
	 */
	boolean canResume() {
		return state == XU更新器.状态_暂停 && pauseReady;
	}

	/**
	 * 进入带通知的后台执行，默认传输不再依赖窗口生命周期。
	 */
	void background() {
		if (state != XU更新器.状态_下载) {
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

	/**
	 * 查询任务是否已移交后台服务。
	 *
	 * @return 是否已移交后台服务。
	 */
	boolean isBackground() {
		return background;
	}

	/**
	 * 读取当前任务已写入的字节数。
	 *
	 * @return 已写入字节数。
	 */
	long received() {
		return received;
	}

	/**
	 * 读取当前已知的文件总大小。
	 *
	 * @return 已知文件总大小。
	 */
	long total() {
		return total;
	}

	/**
	 * 读取任务当前的下载、暂停或终止状态。
	 *
	 * @return 下载或暂停状态。
	 */
	int state() {
		return state;
	}

	/**
	 * 校验结束前禁止发布 100% 或最终文件。
	 *
	 * @param token  本次有效任务序号。
	 * @param active 可取消的文件传输句柄。
	 * @param source 下载器提交的完整来源文件。
	 */
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
								state = XU更新器.状态_就绪;
								try {
									report(file.length(), file.length(), 100);
									// 进度事件可显式取消；取消后不继续发送完成或生成安装通知。
									if (sequence == token && state == XU更新器.状态_就绪) {
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
					main.post(() -> {
						failure(token, exception, false);
					});
				}
			}
		});
	}

	/**
	 * 网络失败可保留续传记录，校验失败必须终止并移除损坏文件。
	 *
	 * @param token 本次任务序号。
	 * @param error 实际下载或校验异常。
	 */
	private void failure(long token, Exception error) {
		failure(token, error, resumable);
	}

	/**
	 * 在单一终止边界更新状态与通知，不伪造下载完成。
	 *
	 * @param token       本次任务序号。
	 * @param error       实际失败异常。
	 * @param recoverable 是否保留文件与暂停状态以允许续传。
	 */
	private void failure(long token, Exception error, boolean recoverable) {
		if (accepts(token)) {
			state = recoverable ? XU更新器.状态_暂停 : XU更新器.状态_空闲;
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

	/**
	 * 只接受仍在传输或校验中的本次回调。
	 *
	 * @param token 回调携带的任务序号。
	 * @return 序号仍匹配且任务仍处于下载或校验阶段时为真。
	 */
	private boolean accepts(long token) {
		return token == sequence && state == XU更新器.状态_下载;
	}

	/**
	 * 主线程统一发布进度。
	 *
	 * @param count   已接收字节数。
	 * @param length  总字节数，未知时为零。
	 * @param percent 百分比，未知时为负一。
	 */
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
