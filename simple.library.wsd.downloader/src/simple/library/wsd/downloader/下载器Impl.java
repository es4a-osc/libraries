package simple.library.wsd.downloader;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import simple.library.wsd.downloader.internal.DownloadEngine;
import simple.runtime.android.MainActivity;
import simple.runtime.collections.集合;
import simple.runtime.components.组件容器;
import simple.runtime.components.impl.组件Impl;
import simple.runtime.events.EventDispatcher;
import simple.runtime.variants.StringVariant;

/**
 * 下载器 Android 适配；同一应用的组件共享任务记录和传输，最后一个组件退出时保存断点。
 * 事件切换到主线程，并检查任务代次，避免取消后收到旧任务成功或进度回调。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class 下载器Impl extends 组件Impl implements 下载器 {

	private static Manager shared;
	private final MainActivity activity;
	private final Manager manager;
	private boolean destroyed;
	private final MainActivity.OnDestroyListener destroyListener = new MainActivity.OnDestroyListener() {

		@Override
		public void onDestroy() {
			// Activity 正在遍历销毁监听器，不能在回调内移除当前监听器。
			dispose(false);
			EventDispatcher.unregisterEvent(下载器Impl.this);
		}
	};

	/**
	 * 创建下载器并恢复应用私有任务记录。
	 *
	 * @param container 容纳非可视组件的窗口。
	 * @throws IllegalStateException 下载记录读取或恢复失败。
	 */
	public 下载器Impl(组件容器 container) {
		super(container);
		activity = MainActivity.getContext();

		synchronized (下载器Impl.class) {
			try {
				if (shared == null) {
					shared = new Manager(activity);
				}

				manager = shared;
				manager.clients.add(this);
			} catch (IOException e) {
				throw new IllegalStateException(e.getMessage(), e);
			}
		}

		activity.addOnDestroyListener(destroyListener);
	}

	@Override
	public void 进度改变(int taskId, long total, long downloaded, int percent) {
		EventDispatcher.dispatchEvent(this, "进度改变", taskId, total, downloaded, percent);
	}

	@Override
	public void 下载完毕(int taskId, boolean success) {
		EventDispatcher.dispatchEvent(this, "下载完毕", taskId, success);
	}

	@Override
	public int 开始下载(String address, String path) {
		try {
			check();
			return manager.engine.start(address, resolve(path));
		} catch (IOException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	@Override
	public void 暂停下载(int taskId) {
		try {
			check();
			manager.engine.pause(taskId);
		} catch (IOException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	@Override
	public void 继续下载(int taskId) {
		try {
			check();
			resolve(manager.engine.info(taskId).path);
			manager.engine.resume(taskId);
		} catch (IOException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	@Override
	public void 取消下载(int taskId) {
		try {
			check();
			manager.engine.cancel(taskId);
		} catch (IOException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	@Override
	public 集合 取未完成记录() {
		check();
		集合 result = new 集合();

		for (DownloadEngine.Record record : manager.engine.unfinished()) {
			result.加入(StringVariant.getStringVariant(record.url));
		}

		return result;
	}

	@Override
	public String[] 取任务信息(int taskId) {
		check();
		DownloadEngine.Record record = manager.engine.info(taskId);
		String[] states = { "等待", "下载", "暂停", "完成", "失败", "取消" };

		return new String[] {
			record.url,
			record.path,
			states[record.state],
			Long.toString(record.total),
			Long.toString(record.done),
			record.error
		};
	}

	@Override
	public void 销毁() {
		dispose(true);
		super.销毁();
	}

	/**
	 * 释放连接、线程及按要求移除生命周期监听器。
	 *
	 * @param removeListener 是否移除生命周期监听器
	 */
	private void dispose(boolean removeListener) {
		synchronized (下载器Impl.class) {
			if (destroyed) {
				return;
			}

			destroyed = true;
			manager.clients.remove(this);
			if (removeListener) {
				activity.removeOnDestroyListener(destroyListener);
			}

			if (manager.clients.isEmpty()) {
				try {
					manager.engine.close();
				} catch (IOException e) {
					Log.e("ES4A下载器", "无法保存下载断点", e);
				}

				manager.store.close();
				shared = null;
			}
		}
	}

	/**
	 * 校验输入是否满足本操作的约束。
	 */
	private void check() {
		if (destroyed) {
			throw new IllegalStateException("下载器已经销毁。");
		}
	}

	/**
	 * 解析下载目标文件并准备所需的应用外部目录。
	 *
	 * @param path 文件或目录路径
	 * @return 解析后的文件对象
	 * @throws IOException 访问文件、流或网络资源失败时
	 */
	private File resolve(String path) throws IOException {
		if (path == null || path.trim().length() == 0) {
			throw new IOException("保存路径不能为空。");
		}

		File file = new File(path);
		if (file.isAbsolute()) {
			File target = file.getCanonicalFile();
			// 应用外部私有目录由系统创建，兼容分区存储及多个可用存储卷。
			// 不猜测 /Android/data 路径；其它绝对路径仍由调用方保证访问授权。
			activity.getExternalFilesDirs(null);

			return target;
		}

		File root = activity.getFilesDir().getCanonicalFile();
		File target = new File(root, path).getCanonicalFile();

		if (!target.getPath().startsWith(root.getPath() + File.separator)) {
			throw new IOException("相对保存路径不能越出应用内部文件目录。");
		}

		return target;
	}

	/**
	 * 管理下载器实例共享的下载引擎、任务数据库和组件列表。
	 * 将下载回调投递到主线程，检查任务是否仍有效后，再分发组件事件。
	 */
	private static final class Manager implements DownloadEngine.Listener {

		final DownloadStore store;
		final DownloadEngine engine;
		final List<下载器Impl> clients = new ArrayList<下载器Impl>();
		final Handler handler = new Handler(Looper.getMainLooper());

		Manager(MainActivity activity) throws IOException {
			store = new DownloadStore(activity.getApplicationContext());

			try {
				engine = new DownloadEngine(store, this);
			} catch (IOException e) {
				store.close();
				throw e;
			}
		}

		@Override
		public void progress(DownloadEngine.Record record) {
			dispatch(record, false, false);
		}

		@Override
		public void finished(DownloadEngine.Record record, boolean success) {
			dispatch(record, true, success);
		}

		/**
		 * 将下载进度或完成状态分发给对应组件。
		 *
		 * @param record 当前下载记录
		 * @param finished 任务是否已经结束
		 * @param success 本次操作是否成功
		 */
		private void dispatch(final DownloadEngine.Record record, final boolean finished, final boolean success) {
			handler.post(new Runnable() {

				@Override
				public void run() {
					if (!engine.current(record)) {
						return;
					}

					List<下载器Impl> targets;

					synchronized (下载器Impl.class) {
						targets = new ArrayList<下载器Impl>(clients);
					}

					for (下载器Impl client : targets) {
						if (client.destroyed) {
							continue;
						}

						if (finished) {
							client.下载完毕(record.id, success);
						} else {
							client.进度改变(record.id, record.total, record.done,
								record.total < 0 ? -1
									: record.total == 0 ? 100
									: (int) Math.min(100, (double) record.done / record.total * 100));
						}
					}
				}
			});
		}
	}
}
