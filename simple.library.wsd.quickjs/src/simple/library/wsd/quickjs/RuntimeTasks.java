package simple.library.wsd.quickjs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * 单个运行时的串行任务调度和计时器；所有 JS 引用仍由运行时锁保护。
 *
 * @author 树先生 xhwsd@qq.com
 */
final class RuntimeTasks {
	private final QuickJS运行时 runtime;
	private final Object submission = new Object();
	private volatile ScheduledThreadPoolExecutor executor;
	private final Map<Integer, Timer> timers = new HashMap<Integer, Timer>();
	private int nextTimer = 1;
	private int batch = 100;
	private boolean draining;
	private volatile QuickJS错误 lastError;
	private ErrorHandler errorHandler;

	/**
	 * 将独立工作线程的后台失败转发给其宿主，不同步调用其它引擎。
	 */
	interface ErrorHandler {
		/**
		 * 接收已经记录的实际失败。
		 *
		 * @param error 实际错误。
		 */
		void failed(QuickJS错误 error);
	}

	/**
	 * 设置内部错误转发器，调用方已取得运行时锁。
	 *
	 * @param handler 可选转发器。
	 */
	void setErrorHandler(ErrorHandler handler) {
		errorHandler = handler;
	}

	/**
	 * 建立调度状态，尚不启动线程。
	 *
	 * @param runtime 所属运行时。
	 */
	RuntimeTasks(QuickJS运行时 runtime) {
		this.runtime = runtime;
	}

	/**
	 * 启动调度线程；调用方已取得运行时锁。
	 *
	 * @param maximum 每轮最多执行的引擎任务数。
	 */
	void start(int maximum) {
		if (maximum <= 0 || maximum > 100000) {
			throw new QuickJS错误("自动调度批量须为 1 至 100000");
		}
		batch = maximum;
		synchronized (submission) {
			if (executor == null) {
				executor = new ScheduledThreadPoolExecutor(1, new ThreadFactory() {
					@Override
					public Thread newThread(Runnable task) {
						Thread thread = new Thread(task, "ES4A-QuickJS-Tasks");
						thread.setDaemon(true);
						return thread;
					}
				});
				executor.setRemoveOnCancelPolicy(true);
			}
		}
		wake();
	}

	/**
	 * 判断自动调度是否已经启动。
	 *
	 * @return 已启动时为真。
	 */
	boolean active() {
		return executor != null;
	}

	/**
	 * 在运行时锁内检查队列，按需投递一轮执行，不使用轮询线程。
	 */
	void wake() {
		if (executor == null || draining || runtime.是否已销毁() || !runtime.hasPendingInternal()) {
			return;
		}
		draining = true;
		if (!post(new Runnable() {
				@Override
				public void run() {
					synchronized (runtime.lock) {
						try {
							if (executor != null && !runtime.是否已销毁()) {
								runtime.执行任务(batch);
							}
						} catch (RuntimeException error) {
							record(error);
						} finally {
							draining = false;
							wake();
						}
					}
				}
			})) {
			draining = false;
		}
	}

	/**
	 * 从其它线程投递串行任务，不同步进入另一运行时。
	 *
	 * @param action 待执行任务。
	 * @return 调度仍活动并已接收任务时为真。
	 */
	boolean post(Runnable action) {
		synchronized (submission) {
			if (executor == null) {
				return false;
			}
			try {
				executor.execute(action);
				return true;
			} catch (RejectedExecutionException error) {
				return false;
			}
		}
	}

	/**
	 * 登记拥有独立引用的计时器，调用方已取得运行时锁。
	 *
	 * @param context 所属实例。
	 * @param function 回调函数。
	 * @param delay 延迟毫秒。
	 * @param repeat 是否重复。
	 * @param arguments 回调参数。
	 * @return 正整数计时器编号。
	 */
	int timer(QuickJS实例 context, QuickJS函数 function, long delay, boolean repeat, QuickJS值[] arguments) {
		if (executor == null || nextTimer == Integer.MAX_VALUE) {
			throw new QuickJS错误("计时器调度已停止或编号已用尽");
		}
		if (delay < 0 || delay > Integer.MAX_VALUE) {
			throw new QuickJS错误("计时器延迟须为 0 至 2147483647 毫秒");
		}
		Timer timer = new Timer(nextTimer++, context, function, repeat, arguments);
		timers.put(timer.id, timer);
		try {
			ScheduledThreadPoolExecutor current = executor;
			if (current == null) {
				throw new QuickJS错误("计时器调度已停止");
			}
			timer.future = repeat
					? current.scheduleWithFixedDelay(timer, delay, Math.max(1, delay), TimeUnit.MILLISECONDS)
					: current.schedule(timer, delay, TimeUnit.MILLISECONDS);
			return timer.id;
		} catch (RuntimeException error) {
			cancel(timer.id, context);
			throw error;
		}
	}

	/**
	 * 取消所属实例的计时器；不存在时无操作。
	 *
	 * @param id 计时器编号。
	 * @param context 所属实例。
	 */
	void cancel(int id, QuickJS实例 context) {
		Timer timer = timers.get(id);
		if (timer == null || timer.context != context) {
			return;
		}
		timers.remove(id);
		if (timer.future != null) {
			timer.future.cancel(false);
		}
		timer.function.释放();
		for (QuickJS值 argument : timer.arguments) {
			argument.释放();
		}
	}

	/**
	 * 清理指定实例的计时器，调用方已取得运行时锁。
	 *
	 * @param context 所属实例。
	 */
	void closeContext(QuickJS实例 context) {
		for (Timer timer : new ArrayList<Timer>(timers.values())) {
			if (timer.context == context) {
				cancel(timer.id, context);
			}
		}
	}

	/**
	 * 停止全部定时任务并释放线程；不等待已经进入 JS 的当前调用。
	 */
	void stop() {
		errorHandler = null;
		for (Timer timer : new ArrayList<Timer>(timers.values())) {
			cancel(timer.id, timer.context);
		}
		synchronized (submission) {
			ScheduledThreadPoolExecutor current = executor;
			executor = null;
			if (current != null) {
				current.shutdownNow();
			}
		}
		draining = false;
	}

	/**
	 * 保存后台失败，使调用者可读取实际错误和堆栈。
	 *
	 * @param error 实际失败。
	 */
	void record(RuntimeException error) {
		lastError = error instanceof QuickJS错误 ? (QuickJS错误)error : new QuickJS错误(error.toString());
		android.util.Log.e("QuickJS", "后台任务失败", error);

		if (errorHandler != null) {
			try {
				errorHandler.failed(lastError);
			} catch (RuntimeException failure) {
				android.util.Log.e("QuickJS", "转发后台错误失败", failure);
			}
		}
	}

	/**
	 * 读取最近后台任务异常。
	 *
	 * @return 最近异常；尚无异常时为空。
	 */
	QuickJS错误 error() {
		return lastError;
	}

	/**
	 * 清除保存的后台异常。
	 */
	void clearError() {
		lastError = null;
	}

	/**
	 * 一个可取消的宿主计时器，不在没有 JS 工作时持续唤醒。
	 */
	private final class Timer implements Runnable {
		final int id;
		final QuickJS实例 context;
		final QuickJS函数 function;
		final boolean repeat;
		final QuickJS值[] arguments;
		ScheduledFuture<?> future;

		/**
		 * 保留回调及参数，保证原引用释放后计时器仍可执行。
		 *
		 * @param id 编号。
		 * @param context 所属实例。
		 * @param function 回调。
		 * @param repeat 是否重复。
		 * @param source 参数。
		 */
		Timer(int id, QuickJS实例 context, QuickJS函数 function, boolean repeat, QuickJS值[] source) {
			this.id = id;
			this.context = context;
			this.repeat = repeat;
			this.function = function.保留().到函数();
			arguments = new QuickJS值[source.length];
			try {
				for (int i = 0; i < source.length; i++) {
					arguments[i] = source[i].保留();
				}
			} catch (RuntimeException | Error error) {
				this.function.释放();
				for (QuickJS值 value : arguments) {
					if (value != null) {
						value.释放();
					}
				}
				throw error;
			}
		}

		@Override
		public void run() {
			synchronized (runtime.lock) {
				if (context.closed || timers.get(id) != this || executor == null) {
					return;
				}
				try {
					context.op(Native.CALL, function, null, null, null, 0, 0, arguments, null).释放();
				} catch (RuntimeException error) {
					record(error);
				} finally {
					if (!repeat) {
						cancel(id, context);
					}
				}
			}
		}
	}
}
