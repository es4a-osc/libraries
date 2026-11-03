package simple.library.wsd.quickjs;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * 独立 JavaScript 堆、作业队列和资源限制；同一运行时的调用串行执行。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class QuickJS运行时 {
	static final ThreadLocal<QuickJS运行时> ACTIVE = new ThreadLocal<QuickJS运行时>();
	final Object lock = new Object();
	final RuntimeTasks tasks = new RuntimeTasks(this);
	private final Object lifetime = new Object();
	private final Set<QuickJS实例> contexts = new LinkedHashSet<QuickJS实例>();
	private volatile long handle;
	private volatile boolean executing;
	private long timeout = 10000;
	private int depth;

	/**
	 * 有返回值的内部受控调用。
	 *
	 * @param <T> 返回类型。
	 */
	interface Action<T> {
		/**
		 * 执行已经取得运行时锁的操作。
		 *
		 * @return 操作结果。
		 */
		T run();
	}

	/**
	 * 创建运行时，默认 JS 堆上限 64 MiB、顶层调用时间限制 10 秒。
	 */
	public QuickJS运行时() {
		if (ACTIVE.get() != null) {
			throw new QuickJS错误("JS 执行或宿主回调中不能创建其它运行时");
		}
		try {
			handle = Native.runtime(this);
			Native.configure(handle, 0, 64L * 1024 * 1024);
			Native.configure(handle, 1, 256 * 1024);
		} catch (NativeFailure error) {
			throw QuickJS错误.from(error);
		} catch (LinkageError error) {
			throw new QuickJS错误("当前设备无法加载 QuickJS 原生库：" + error.getMessage());
		}
	}

	/**
	 * 在运行时锁内执行；只在最外层设置和撤销中断状态。
	 *
	 * @param context 可选所属实例。
	 * @param action 实际操作。
	 * @param <T> 返回类型。
	 * @return 操作结果。
	 */
	<T> T perform(QuickJS实例 context, Action<T> action) {
		checkAccess();
		synchronized (lock) {
			if (handle == 0) {
				throw new QuickJS错误("QuickJS 运行时已销毁");
			}
			if (context != null && context.closed) {
				throw new QuickJS错误("QuickJS 实例已销毁");
			}
			QuickJS运行时 previous = ACTIVE.get();
			if (previous != null && previous != this) {
				throw new QuickJS错误("JS 执行或宿主回调中不能重入其它运行时");
			}
			boolean outer = depth++ == 0;
			if (outer) {
				ACTIVE.set(this);
				synchronized (lifetime) {
					Native.begin(handle, timeout);
					executing = true;
				}
			}
			try {
				return action.run();
			} catch (NativeFailure error) {
				QuickJS错误 converted = QuickJS错误.from(error);
				if (context != null) {
					context.rememberFailure(converted);
				}
				throw converted;
			} finally {
				depth--;
				if (outer) {
					synchronized (lifetime) {
						executing = false;
						if (handle != 0) {
							Native.stop(handle);
						}
					}
					if (previous == null) {
						ACTIVE.remove();
					} else {
						ACTIVE.set(previous);
					}
					tasks.wake();
				}
			}
		}
	}

	/**
	 * 在获取监视器之前拒绝跨运行时回调重入，避免反向获取锁造成死锁。
	 */
	void checkAccess() {
		QuickJS运行时 active = ACTIVE.get();
		if (active != null && active != this) {
			throw new QuickJS错误("JS 执行或宿主回调中不能重入其它运行时");
		}
	}

	/**
	 * 登记实例到本运行时。
	 *
	 * @param context 新实例。
	 * @return 对应原生上下文。
	 */
	long attach(final QuickJS实例 context) {
		return perform(null, new Action<Long>() {
			@Override
			public Long run() {
				long pointer = Native.context(handle, context);
				contexts.add(context);
				return pointer;
			}
		});
	}

	/**
	 * 从运行时移除已关闭的实例。
	 *
	 * @param context 已关闭实例。
	 */
	void detach(QuickJS实例 context) {
		contexts.remove(context);
	}

	/**
	 * 创建共享当前堆和作业队列、具有独立全局对象的实例。
	 *
	 * @return 新实例。
	 */
	@SimpleFunction
	public QuickJS实例 创建实例() {
		return new QuickJS实例(this, false);
	}

	/**
	 * 设置每次最外层 JS 调用的时间限制；不强行终止阻塞的 Java 宿主调用或 Atomics.wait 等待。
	 *
	 * @param milliseconds 毫秒，0 不限制，最大 86400000。
	 */
	@SimpleFunction
	public void 置执行时限(final long milliseconds) {
		if (milliseconds < 0 || milliseconds > 86400000) {
			throw new QuickJS错误("执行时限须为 0 至 86400000 毫秒");
		}
		checkAccess();
		synchronized (lock) {
			if (handle == 0) {
				throw new QuickJS错误("QuickJS 运行时已销毁");
			}
			timeout = milliseconds;
		}
	}

	/**
	 * 读取当前执行时限。
	 *
	 * @return 毫秒，0 不限制。
	 */
	@SimpleFunction
	public long 取执行时限() {
		checkAccess();
		synchronized (lock) {
			return timeout;
		}
	}

	/**
	 * 从其它线程请求中断正在执行的 JS；在引擎下一次中断检查时生效。
	 *
	 * @return 是否存在正在执行的调用并已提交中断。
	 */
	@SimpleFunction
	public boolean 取消执行() {
		synchronized (lifetime) {
			if (handle == 0 || !executing) {
				return false;
			}
			Native.cancel(handle);
			return true;
		}
	}

	/**
	 * 调整原生运行时资源参数。
	 *
	 * @param operation 参数编号。
	 * @param value 参数值。
	 */
	private void configure(final int operation, final long value) {
		if (value < 0) {
			throw new QuickJS错误("资源限制不能为负数");
		}
		perform(null, new Action<Void>() {
			@Override
			public Void run() {
				Native.configure(handle, operation, value);
				return null;
			}
		});
	}

	/**
	 * 设置原生 JS 堆内存上限，不包含 Java 对象和应用其它内存。
	 *
	 * @param bytes 字节数；0 不限制。
	 */
	@SimpleFunction
	public void 置内存上限(long bytes) {
		configure(0, bytes);
	}

	/**
	 * 设置 JS 栈大小上限。
	 *
	 * @param bytes 字节数；0 不限制，过大值可能导致宿主线程栈耗尽。
	 */
	@SimpleFunction
	public void 置栈上限(long bytes) {
		configure(1, bytes);
	}

	/**
	 * 设置触发循环垃圾回收的阈值。
	 *
	 * @param bytes 字节数。
	 */
	@SimpleFunction
	public void 置回收阈值(long bytes) {
		configure(2, bytes);
	}

	/**
	 * 执行循环垃圾回收，不会释放仍被引用的值。
	 */
	@SimpleFunction
	public void 回收垃圾() {
		configure(3, 0);
	}

	/**
	 * 设置是否允许 Atomics.wait 阻塞；默认关闭，启用后须使用有限等待时间，执行中断不会唤醒等待。
	 *
	 * @param allowed 是否允许阻塞。
	 */
	@SimpleFunction
	public void 置允许阻塞(boolean allowed) {
		configure(4, allowed ? 1 : 0);
	}

	/**
	 * 执行 Promise 等待执行任务；不自动等待未来的网络或计时器事件。
	 *
	 * @param maximum 本次最多执行数量，必须大于零。
	 * @return 实际执行数量。
	 */
	@SimpleFunction
	public int 执行任务(final int maximum) {
		if (maximum <= 0) {
			throw new QuickJS错误("待办执行上限必须大于零");
		}
		return perform(null, new Action<Integer>() {
			@Override
			public Integer run() {
				return Native.jobs(handle, maximum);
			}
		});
	}

	/**
	 * 判断运行时是否仍有排队作业。
	 *
	 * @return 是否存在待执行任务。
	 */
	@SimpleFunction
	public boolean 是否有任务() {
		return perform(null, new Action<Boolean>() {
			@Override
			public Boolean run() {
				return Native.pending(handle);
			}
		});
	}

	/**
	 * 取得独立的原生内存统计快照。
	 *
	 * @return 内存统计。
	 */
	@SimpleFunction
	public QuickJS内存 取内存统计() {
		return perform(null, new Action<QuickJS内存>() {
			@Override
			public QuickJS内存 run() {
				return new QuickJS内存(Native.memory(handle));
			}
		});
	}

	/**
	 * 判断是否已经销毁。
	 *
	 * @return 是否已经销毁。
	 */
	@SimpleFunction
	public boolean 是否已销毁() {
		return handle == 0;
	}

	/**
	 * 释放全部实例、引用和任务队列；正在执行时须先从其它线程请求取消。
	 */
	@SimpleFunction
	public void 销毁() {
		checkAccess();
		synchronized (lock) {
			if (handle == 0) {
				return;
			}
			if (depth != 0) {
				throw new QuickJS错误("JS 执行或回调中不能销毁运行时");
			}
			tasks.stop();
			for (QuickJS实例 context : new ArrayList<QuickJS实例>(contexts)) {
				context.closeInternal();
			}
			synchronized (lifetime) {
				long pointer = handle;
				handle = 0;
				Native.closeRuntime(pointer);
			}
		}
	}

	/**
	 * 检查原生任务队列，调用方已经取得运行时锁。
	 *
	 * @return 存在待执行任务时为真。
	 */
	boolean hasPendingInternal() {
		return handle != 0 && Native.pending(handle);
	}

	/**
	 * 启动串行后台调度，自动推进 Promise 和异步模块；回调在调度线程执行。
	 *
	 * @param maximum 每轮执行上限，1 至 100000。
	 */
	@SimpleFunction
	public void 启动自动调度(int maximum) {
		checkAccess();
		synchronized (lock) {
			if (handle == 0) {
				throw new QuickJS错误("运行时已销毁");
			}
			tasks.start(maximum);
		}
	}

	/**
	 * 停止自动调度、工作线程及全部计时器；剩余 Promise 任务仍可手动执行。
	 */
	@SimpleFunction
	public void 停止自动调度() {
		checkAccess();
		synchronized (lock) {
			for (QuickJS实例 context : contexts) {
				context.stopWorkers();
			}
			tasks.stop();
		}
	}

	/**
	 * 判断自动调度是否活动。
	 *
	 * @return 已启动时为真。
	 */
	@SimpleFunction
	public boolean 是否自动调度() {
		return tasks.active();
	}

	/**
	 * 读取最近后台任务失败，Promise 拒绝通知仍由实例监听处理。
	 *
	 * @return 实际错误；尚无失败时为空。
	 */
	@SimpleFunction
	public QuickJS错误 取任务错误() {
		return tasks.error();
	}

	/**
	 * 清除最近后台任务失败。
	 */
	@SimpleFunction
	public void 清除任务错误() {
		tasks.clearError();
	}
}
