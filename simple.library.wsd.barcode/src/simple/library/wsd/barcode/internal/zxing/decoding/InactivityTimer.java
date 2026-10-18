package simple.library.wsd.barcode.internal.zxing.decoding;

import android.app.Activity;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * 管理扫描界面的空闲关闭定时任务。
 */
public final class InactivityTimer {
	private static final int INACTIVITY_DELAY_SECONDS = 300;
	private final ScheduledExecutorService inactivityTimer = Executors.newSingleThreadScheduledExecutor(new DaemonThreadFactory());
	private final Activity activity;
	private ScheduledFuture<?> inactivityFuture = null;

	public InactivityTimer(Activity activity) {
		this.activity = activity;
		this.onActivity();
	}

	/**
	 * 更新最近活动时间并重设空闲关闭任务。
	 */
	public void onActivity() {
		this.cancel();
		this.inactivityFuture = this.inactivityTimer.schedule(new FinishListener(this.activity), 300L, TimeUnit.SECONDS);
	}

	/**
	 * 取消当前任务并释放相应资源。
	 */
	private void cancel() {
		if (this.inactivityFuture != null) {
			this.inactivityFuture.cancel(true);
			this.inactivityFuture = null;
		}
	}

	/**
	 * 关闭空闲计时器及其调度线程。
	 */
	public void shutdown() {
		this.cancel();
		this.inactivityTimer.shutdown();
	}

	/**
	 * 为空闲关闭任务创建守护线程。
	 */
	private static final class DaemonThreadFactory implements ThreadFactory {

		private DaemonThreadFactory() {
		}

		@Override
		public Thread newThread(Runnable runnable) {
			Thread thread = new Thread(runnable);
			thread.setDaemon(true);
			return thread;
		}
	}
}
