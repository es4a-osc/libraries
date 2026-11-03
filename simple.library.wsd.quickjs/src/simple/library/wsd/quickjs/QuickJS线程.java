package simple.library.wsd.quickjs;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * 独立运行时工作线程，按 FIFO 传递引擎可序列化数据的副本；不共享 JS 值引用。
 * 不支持转移列表或 SharedArrayBuffer；进程内同时最多 16 个工作线程。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class QuickJS线程 {
	private static final Semaphore SLOTS = new Semaphore(16);
	final int id;
	private final QuickJS实例 receiver;
	private final QuickJS线程回调 listener;
	private final ThreadPoolExecutor executor;
	private final AtomicBoolean stopped = new AtomicBoolean();
	private final AtomicBoolean cleaned = new AtomicBoolean();
	private volatile QuickJS实例 context;
	private volatile String lastError = "";

	/**
	 * 创建独立线程，初始化和后续消息在同一串行队列中执行。
	 *
	 * @param receiver 接收消息的实例。
	 * @param id 接收实例内的线程编号。
	 * @param listener 消息回调。
	 */
	QuickJS线程(int id, QuickJS实例 receiver, QuickJS线程回调 listener) {
		if (!SLOTS.tryAcquire()) {
			throw new QuickJS错误("QuickJS 工作线程已达到 16 个的上限");
		}
		this.id = id;
		this.receiver = receiver;
		this.listener = listener;
		try {
			executor = new ThreadPoolExecutor(
					1, 1, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), new ThreadFactory() {
						@Override
						public Thread newThread(Runnable task) {
							Thread thread = new Thread(task, "ES4A-QuickJS-Worker");
							thread.setDaemon(true);
							return thread;
						}
					});
		} catch (RuntimeException | Error error) {
			SLOTS.release();
			throw error;
		}
	}

	/**
	 * 登记到接收实例后才启动，避免极短脚本先结束再加入活动表。
	 *
	 * @param code 初始源码。
	 * @param filename 来源名称。
	 * @param module 是否按模块执行。
	 */
	void start(final String code, final String filename, final boolean module) {
		try {
			executor.execute(new Runnable() {
				@Override
				public void run() {
					initialize(code, filename, module);
				}
			});
		} catch (RuntimeException | Error error) {
			stopped.set(true);
			cleanup();
			executor.shutdown();
			throw error;
		}
	}

	/**
	 * 初始化独立引擎；消息及错误只以复制后的数据或文本离开当前运行时。
	 *
	 * @param code 源码。
	 * @param filename 来源名称。
	 * @param module 是否为模块。
	 */
	private void initialize(String code, String filename, boolean module) {
		try {
			if (stopped.get()) {
				return;
			}
			final QuickJS实例 child = new QuickJS实例();
			context = child;
			if (stopped.get()) {
				return;
			}
			child.置加载器(new QuickJS加载器() {
				@Override
				public QuickJS模块 加载模块(String name) {
					QuickJS模块 result = new QuickJS模块();
					result.置源码(QuickJS实例.readWorkerSource(name));
					return result;
				}
			});
			child.启用计时器();
			child.workerBase = filename;
			child.启用工作线程();
			synchronized (child.runtime.lock) {
				child.runtime.tasks.setErrorHandler(new RuntimeTasks.ErrorHandler() {
					@Override
					public void failed(QuickJS错误 error) {
						if (!stopped.get()) {
							report(error);
						}
					}
				});
			}

			// 模块为严格模式，必须预先声明事件槽，才能直接给 onmessage 赋值。
			QuickJS对象 global = child.取全局对象();
			try {
				global.置成员("self", global);
				global.置简单成员("onmessage", null);
				global.置简单成员("onerror", null);
			} finally {
				global.释放();
			}

			child.注册函数("postMessage", new ManagedCallback() {
				@Override
				public QuickJS值 处理调用(QuickJS值 self, QuickJS参数 arguments) {
					if (arguments.取数量() > 1 && !arguments.取值(1).是否未定义() &&
							(!arguments.取值(1).是否数组() || arguments.取值(1).到数组().取长度() != 0)) {
						throw new QuickJS错误("工作线程不支持转移列表");
					}
					byte[] data = child.序列化值(arguments.取数量() == 0 ? null : arguments.取值(0));
					deliver(data);
					return null;
				}
			});
			child.注册函数("close", new ManagedCallback() {
				@Override
				public QuickJS值 处理调用(QuickJS值 self, QuickJS参数 arguments) {
					终止();
					return null;
				}
			});

			if (module) {
				QuickJS值 result = child.执行模块(code, filename);
				try {
					awaitModule(result);
				} finally {
					result.释放();
				}
			} else {
				child.执行代码(code, filename).释放();
			}
		} catch (RuntimeException error) {
			if (!stopped.get()) {
				report(error);
				终止();
			}
		} finally {
			if (stopped.get()) {
				cleanup();
			}
		}
	}

	/**
	 * 等待模块初始化完成后再处理入站消息；顶层 await 由独立调度线程推进。
	 * 等待发生在引擎锁外，终止请求可随时结束等待。
	 *
	 * @param result 模块求值结果，当前方法不取得其所有权。
	 */
	private void awaitModule(QuickJS值 result) {
		if (!result.是否承诺()) {
			return;
		}
		QuickJS承诺 promise = result.到承诺();
		while (!stopped.get() && promise.取状态() == 0) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException error) {
				Thread.currentThread().interrupt();
				throw new QuickJS错误("工作线程初始化已中断");
			}
		}

		if (!stopped.get() && promise.取状态() == 2) {
			QuickJS值 reason = promise.取结果();
			try {
				context.抛出异常(reason);
			} finally {
				reason.释放();
			}
		}
	}

	/**
	 * 发送当前接收实例的值，入队前复制数据；不支持函数和共享缓冲区。
	 *
	 * @param value 当前接收实例的值；空引用表示 undefined。
	 */
	@SimpleFunction
	public void 发送(QuickJS值 value) {
		if (stopped.get() || receiver.closed) {
			throw new QuickJS错误("工作线程已经结束");
		}
		final byte[] data = receiver.序列化值(value);
		try {
			executor.execute(new Runnable() {
				@Override
				public void run() {
					QuickJS实例 child = context;
					if (stopped.get() || child == null) {
						return;
					}
					try {
						QuickJS值 value = child.反序列化值(data);
						try {
							child.dispatchMessage(value);
						} finally {
							value.释放();
						}
					} catch (RuntimeException error) {
						if (!stopped.get()) {
							report(error);
						}
					}
				}
			});
		} catch (java.util.concurrent.RejectedExecutionException error) {
			throw new QuickJS错误("工作线程已经结束");
		}
	}

	/**
	 * 在接收运行时的调度线程中反序列化并通知，回调不会同步重入另一运行时。
	 *
	 * @param data 引擎产生的可信数据副本。
	 */
	private void deliver(final byte[] data) {
		if (stopped.get() || receiver.closed) {
			return;
		}
		if (!receiver.runtime.tasks.post(new Runnable() {
				@Override
				public void run() {
					if (receiver.closed) {
						return;
					}
					try {
						receiver.runtime.perform(receiver, new QuickJS运行时.Action<Void>() {
							@Override
							public Void run() {
								QuickJS值 value = receiver.反序列化值(data);
								try {
									if (listener != null) {
										listener.收到消息(value);
									}
								} finally {
									value.释放();
								}
								return null;
							}
						});
					} catch (RuntimeException error) {
						receiver.runtime.tasks.record(error);
					}
				}
			})) {
			throw new QuickJS错误("接收实例的自动调度已停止");
		}
	}

	/**
	 * 记录实际错误并异步通知接收者。
	 *
	 * @param error 实际异常。
	 */
	private void report(RuntimeException error) {
		lastError = error.toString() + (error instanceof QuickJS错误 ? "\n" + ((QuickJS错误)error).取堆栈() : "");
		final String message = lastError;
		receiver.runtime.tasks.post(new Runnable() {
			@Override
			public void run() {
				if (!receiver.closed && listener != null) {
					try {
						listener.发生错误(message);
					} catch (RuntimeException error) {
						receiver.runtime.tasks.record(error);
					}
				}
			}
		});
	}

	/**
	 * 请求中断当前 JS，丢弃待发消息并异步释放工作线程资源；可重复调用。
	 */
	@SimpleFunction
	public void 终止() {
		if (!stopped.compareAndSet(false, true)) {
			return;
		}
		QuickJS实例 child = context;
		if (child != null) {
			child.runtime.取消执行();
		}
		executor.getQueue().clear();
		try {
			executor.execute(new Runnable() {
				@Override
				public void run() {
					cleanup();
				}
			});
		} finally {
			executor.shutdown();
		}
	}

	/**
	 * 释放独立引擎及并发名额，最后异步通知结束。
	 */
	private void cleanup() {
		if (!cleaned.compareAndSet(false, true)) {
			return;
		}
		try {
			QuickJS实例 child = context;
			if (child != null) {
				child.销毁();
			}
		} finally {
			SLOTS.release();
			receiver.forgetWorker(this);
			if (!receiver.runtime.tasks.post(new Runnable() {
					@Override
					public void run() {
						try {
							if (!receiver.closed && listener != null) {
								listener.已经结束();
							}
						} catch (RuntimeException error) {
							receiver.runtime.tasks.record(error);
						} finally {
							if (listener != null) {
								listener.releaseInternal();
							}
						}
					}
				})) {
				if (listener != null) {
					listener.releaseInternal();
				}
			}
		}
	}

	/**
	 * 判断线程是否仍接受消息，终止请求提交后即返回假。
	 *
	 * @return 仍活动时为真。
	 */
	@SimpleFunction
	public boolean 是否运行() {
		return !stopped.get();
	}

	/**
	 * 读取最近工作线程错误。
	 *
	 * @return 错误文本；尚无错误时为空。
	 */
	@SimpleFunction
	public String 取错误() {
		return lastError;
	}
}
