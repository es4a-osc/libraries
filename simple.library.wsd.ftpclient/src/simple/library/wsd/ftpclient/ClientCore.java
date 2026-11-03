package simple.library.wsd.ftpclient;

import android.os.Handler;
import android.os.Looper;
import java.io.Closeable;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import simple.runtime.android.MainActivity;
import simple.runtime.components.impl.组件Impl;
import simple.runtime.components.组件容器;
import simple.runtime.events.EventDispatcher;

/**
 * 连接的串行占用、任务事件、取消、独占数据流及下载提交；不混合协议专属命令。
 *
 * @author 树先生 xhwsd@qq.com
 */
abstract class ClientCore extends 组件Impl {
	/**
	 * 可报告实际网络错误的串行操作。
	 *
	 * @param <T> 返回类型。
	 *
	 * @author 树先生 xhwsd@qq.com
	 */
	interface Action<T> {
		/**
		 * 执行一次受连接占用保护的操作。
		 *
		 * @return 实际操作结果。
		 * @throws Exception 网络或文件操作失败。
		 */
		T run() throws Exception;
	}

	/**
	 * 接收任务取消及进度状态的后台操作。
	 *
	 * @author 树先生 xhwsd@qq.com
	 */
	interface Job {
		/**
		 * 执行已经受理的任务。
		 *
		 * @param task 本次任务。
		 * @throws Exception 网络或文件操作失败。
		 */
		void run(Task task) throws Exception;
	}

	/**
	 * 一个受理编号对应一个终态，不将取消误报为成功。
	 *
	 * @author 树先生 xhwsd@qq.com
	 */
	static final class Task {
		final long id;
		final int mode;
		final String source;
		final String target;
		volatile boolean cancelled;
		boolean finished;
		long completed;
		long total = -1;
		long lastProgress;
		boolean directory;

		/**
		 * 保存不可变的任务身份。
		 *
		 * @param id 本次编号。
		 * @param mode -1 连接、0 下载、1 上传。
		 * @param source 来源路径。
		 * @param target 目标路径。
		 */
		Task(long id, int mode, String source, String target) {
			this.id = id;
			this.mode = mode;
			this.source = source;
			this.target = target;
		}
	}

	final RemoteSession remote;
	final MainActivity activity;
	final Handler handler = new Handler(Looper.getMainLooper());
	final AtomicBoolean occupied = new AtomicBoolean();
	final AtomicBoolean disposed = new AtomicBoolean();
	final AtomicLong sequence = new AtomicLong();
	final ExecutorService worker = Executors.newSingleThreadExecutor();
	final Map<String, File> partials = new ConcurrentHashMap<>();
	volatile String lastError = "";
	volatile Task active;
	volatile Closeable dataStream;
	volatile StreamLease lease;
	int bufferSize = 65536;
	private final MainActivity.OnDestroyListener destroyListener =
			new simple.runtime.android.MainActivity.OnDestroyListener() {
				@Override
				public void onDestroy() {
					dispose();
				}
			};

	/**
	 * 将会话绑定到当前 Android 宿主生命周期。
	 *
	 * @param container 所属窗口容器。
	 * @param remote 当前协议会话。
	 */
	ClientCore(组件容器 container, RemoteSession remote) {
		super(container);
		this.remote = remote;
		activity = MainActivity.getContext();
		activity.addOnDestroyListener(destroyListener);
	}

	/**
	 * 检查同步网络调用线程。
	 */
	static void background() {
		if (Looper.myLooper() == Looper.getMainLooper()) {
			throw new FTP错误("同步网络及流操作须在后台线程调用。");
		}
	}

	/**
	 * 校验将进入协议命令的文本。
	 *
	 * @param value 路径或参数。
	 * @return 原文本。
	 */
	static String text(String value) {
		if (value == null
				|| value.indexOf('\r') >= 0
				|| value.indexOf('\n') >= 0
				|| value.indexOf('\0') >= 0) {
			throw new FTP错误("参数不能包含空值、换行或零字符。");
		}
		return value;
	}

	/**
	 * 取得串行占用，拒绝并发命令及打开数据流时的其它操作。
	 */
	void enter() {
		synchronized (occupied) {
			if (disposed.get()) {
				throw failure(new FTP错误("客户端已销毁。"));
			}
			if (!occupied.compareAndSet(false, true)) {
				throw failure(new FTP错误("连接正在执行任务或持有数据流。"));
			}
			if (disposed.get()) {
				occupied.set(false);
				throw failure(new FTP错误("客户端已销毁。"));
			}
			lastError = "";
		}
	}

	/**
	 * 保存并转换真实失败原因。
	 *
	 * @param error 实际错误。
	 * @return 可由 Simple 捕获的错误。
	 */
	FTP错误 failure(Exception error) {
		lastError =
				error.getMessage() == null ? error.getClass().getSimpleName() : error.getMessage();
		return error instanceof FTP错误 ? (FTP错误) error : new FTP错误(lastError);
	}

	/**
	 * 串行执行同步网络操作。
	 *
	 * @param <T> 返回类型。
	 * @param action 具体操作。
	 * @return 实际结果。
	 */
	<T> T operation(Action<T> action) {
		try {
			background();
		} catch (FTP错误 error) {
			throw failure(error);
		}
		return setting(action);
	}

	/**
	 * 串行读取或修改本地配置；调用者负责需要联网时的线程检查。
	 *
	 * @param <T> 返回类型。
	 * @param action 具体操作。
	 * @return 配置结果。
	 */
	<T> T setting(Action<T> action) {
		enter();
		try {
			return action.run();
		} catch (Exception error) {
			throw failure(error);
		} finally {
			occupied.set(false);
			if (disposed.get()) {
				closeNetwork();
			}
		}
	}

	/**
	 * 在界面线程分派事件，销毁后抑制所有回调。
	 *
	 * @param name 事件名。
	 * @param values 事件参数快照。
	 */
	void event(String name, Object... values) {
		if (!disposed.get()) {
			handler.post(
					new Runnable() {
						@Override
						public void run() {
							if (!disposed.get()) {
								EventDispatcher.dispatchEvent(ClientCore.this, name, values);
							}
						}
					});
		}
	}

	/**
	 * 受理独占连接的后台任务。
	 *
	 * @param mode -1 连接、0 下载、1 上传。
	 * @param source 来源路径。
	 * @param target 目标路径。
	 * @param job 实际任务。
	 * @return 已受理编号；受理不等于执行成功。
	 */
	long begin(int mode, String source, String target, Job job) {
		enter();
		Task task = new Task(sequence.incrementAndGet(), mode, source, target);
		active = task;
		try {
			worker.execute(
					new Runnable() {
						@Override
						public void run() {
							String error = "";
							boolean success = false;
							try {
								check(task);
								job.run(task);
								synchronized (task) {
									check(task);
									task.finished = true;
									success = true;
								}
							} catch (Exception failed) {
								error =
										task.cancelled
												? "传输已取消，连接已断开。"
												: failure(failed).getMessage();
								lastError = error;
							} finally {
								synchronized (task) {
									task.finished = true;
								}
								dataStream = null;
								synchronized (occupied) {
									if (!success || task.cancelled || disposed.get()) {
										closeNetwork();
									}
									active = null;
									occupied.set(false);
								}
							}
							if (mode < 0) {
								event("连接完成", task.id, success, error);
							} else {
								event(
										"传输完成",
										task.id,
										mode,
										source,
										target,
										success,
										task.cancelled,
										error);
							}
						}
					});
			return task.id;
		} catch (RuntimeException failed) {
			active = null;
			occupied.set(false);
			throw failure(failed);
		}
	}

	/**
	 * 检查任务是否允许继续。
	 *
	 * @param task 当前任务。
	 * @throws IOException 已取消或销毁。
	 */
	void check(Task task) throws IOException {
		if (disposed.get() || task.cancelled || Thread.currentThread().isInterrupted()) {
			throw new IOException("任务已取消。");
		}
	}

	/**
	 * 按长整数累计实际传输进度，并限制通知频率。
	 *
	 * @param task 当前任务。
	 * @param amount 新增的字节数。
	 * @param force 是否强制发出最终进度。
	 * @throws IOException 已取消。
	 */
	void progress(Task task, long amount, boolean force) throws IOException {
		check(task);
		task.completed += amount;
		long now = android.os.SystemClock.uptimeMillis();
		if (force || now - task.lastProgress >= 100) {
			task.lastProgress = now;
			long completed = task.completed;
			handler.post(
					new Runnable() {
						@Override
						public void run() {
							if (!disposed.get() && !task.cancelled) {
								EventDispatcher.dispatchEvent(
										ClientCore.this,
										"传输进度",
										task.id,
										task.mode,
										task.source,
										task.target,
										completed,
										task.total);
							}
						}
					});
		}
	}

	/**
	 * 非阻塞请求取消当前文件传输，同时关闭网络以唤醒阻塞读写。
	 *
	 * @return 是否提交了取消请求。
	 */
	boolean cancel() {
		Task task = active;
		if (task == null || task.mode < 0) {
			return false;
		}
		synchronized (task) {
			if (task.finished || task.cancelled) {
				return false;
			}
			task.cancelled = true;
		}
		new Thread(
						new Runnable() {
							@Override
							public void run() {
								synchronized (occupied) {
									if (active == task) {
										closeNetwork();
									}
								}
							}
						},
						"FileTransferCancel")
				.start();
		return true;
	}

	/**
	 * 关闭实际网络资源，不伪造传输终态。
	 */
	void closeNetwork() {
		try {
			remote.disconnect();
		} catch (Exception ignored) {
			// 正在取消或销毁时，最终任务结果仍由执行线程发出。
		}
		Closeable stream = dataStream;
		if (stream != null) {
			try {
				stream.close();
			} catch (IOException ignored) {
			}
		}
	}

	/**
	 * 复制一段实际数据并累计进度。
	 *
	 * @param input 输入数据。
	 * @param output 输出数据。
	 * @param task 当前任务。
	 * @throws Exception 传输或取消失败。
	 */
	void copy(InputStream input, OutputStream output, Task task) throws Exception {
		byte[] buffer = new byte[bufferSize];
		int count;
		while ((count = input.read(buffer)) >= 0) {
			check(task);
			if (count == 0) {
				continue;
			}
			output.write(buffer, 0, count);
			progress(task, count, false);
		}
		output.flush();
	}

	/**
	 * 上传单个文件，偏移前的数据必须已存在于远程文件。
	 *
	 * @param task 当前任务。
	 * @param local 本地文件。
	 * @param target 远程目标。
	 * @param offset 续传偏移。
	 * @throws Exception 实际文件或网络失败。
	 */
	void upload(Task task, File local, String target, long offset) throws Exception {
		if (offset > 0 && !byteExact()) {
			throw new IOException("FTP 偏移续传要求二进制文件类型。");
		}
		if (LocalFiles.isSymbolicLink(local) || !local.isFile()) {
			throw new IOException("上传源必须是普通文件。");
		}
		long length = LocalFiles.size(local);
		if (offset < 0 || offset > length) {
			throw new IOException("上传偏移超出本地文件。");
		}
		if (offset > 0 && remote.stat(target).尺寸() != offset) {
			throw new IOException("续传偏移须等于远程已有文件尺寸。");
		}
		if (!task.directory) {
			task.total = byteExact() ? length : -1;
			task.completed = offset;
		}
		try (InputStream input = new FileInputStream(local)) {
			long skipped = 0;
			while (skipped < offset) {
				long count = input.skip(offset - skipped);
				if (count <= 0) {
					throw new EOFException("无法定位上传偏移。");
				}
				skipped += count;
			}
			try (OutputStream output = remote.write(target, offset)) {
				dataStream = output;
				copy(input, output, task);
			}
			remote.finish();
		}
		progress(task, 0, true);
	}

	/**
	 * 下载到本实例拥有的临时文件，服务器确认后再替换目标；失败保留断点。
	 *
	 * @param task 当前任务。
	 * @param source 远程文件。
	 * @param local 本地目标。
	 * @param offset 续传偏移。
	 * @throws Exception 文件、网络或最终确认失败。
	 */
	void download(Task task, String source, File local, long offset) throws Exception {
		if (offset > 0 && !byteExact()) {
			throw new IOException("FTP 偏移续传要求二进制文件类型。");
		}
		local = local.getAbsoluteFile();
		if (LocalFiles.isSymbolicLink(local)) {
			throw new IOException("下载目标不能是链接。");
		}
		long total = remote.stat(source).尺寸();
		if (offset < 0 || offset > total) {
			throw new IOException("下载偏移超出远程文件。");
		}
		File parent = local.getParentFile();
		LocalFiles.createDirectories(parent);
		String key = local.toString();
		File part = partials.get(key);
		if (offset == 0 || part == null) {
			if (part != null) {
				LocalFiles.deleteIfExists(part);
			}
			part = LocalFiles.createTempFile(parent, ".ftp-", ".part");
			partials.put(key, part);
			if (offset > 0) {
				if (!local.isFile() || LocalFiles.size(local) < offset) {
					throw new IOException("本地文件没有足够的续传前缀。");
				}
				LocalFiles.copy(local, part);
			}
		}
		if (LocalFiles.size(part) < offset) {
			throw new IOException("本地断点文件小于指定偏移。");
		}
		try (RandomAccessFile file = new RandomAccessFile(part, "rw")) {
			file.setLength(offset);
		}
		if (!task.directory) {
			task.total = byteExact() ? total : -1;
			task.completed = offset;
		}
		long before = task.completed;
		try (InputStream input = remote.read(source, offset);
				OutputStream output = new FileOutputStream(part, true)) {
			dataStream = input;
			copy(input, output, task);
		}
		remote.finish();
		if (byteExact() && task.completed - before != total - offset) {
			throw new IOException("实际接收字节数与远程尺寸不同。");
		}
		synchronized (task) {
			check(task);
			LocalFiles.move(part, local);
			partials.remove(key);
			if (!task.directory) {
				task.finished = true;
			}
		}
		progress(task, 0, true);
	}

	/**
	 * 查询本实例保存的下载断点，未产生断点时读取已有目标尺寸。
	 *
	 * @param file 本地目标路径。
	 * @return 可继续接收的字节偏移。
	 */
	long downloadOffset(String file) {
		try {
			File target = localFile(file);
			File part = partials.get(target.toString());
			return part != null && part.exists()
					? LocalFiles.size(part)
					: target.exists() ? LocalFiles.size(target) : 0;
		} catch (IOException error) {
			throw failure(error);
		}
	}

	/**
	 * 校验服务器地址及端口。
	 *
	 * @param host 主机名或 IP。
	 * @param port 端口。
	 */
	static void address(String host, int port) {
		if (text(host).trim().isEmpty() || host.contains("://") || port < 1 || port > 65535) {
			throw new FTP错误("服务器地址或端口无效。");
		}
	}

	/**
	 * 校验可配置的网络超时。
	 *
	 * @param value 毫秒超时。
	 */
	static void timeout(int value) {
		if (value < 1 || value > 300000) {
			throw new FTP错误("超时须为 1 至 300000 毫秒。");
		}
	}

	/**
	 * 将相对本地路径解析到应用私有 files 目录，与运行库文件函数保持一致。
	 *
	 * @param value 相对或绝对路径。
	 * @return 实际绝对文件路径。
	 */
	static File localFile(String value) {
		File file = new File(text(value));
		return (file.isAbsolute() ? file : new File(MainActivity.getContext().getFilesDir(), value))
				.getAbsoluteFile();
	}

	/**
	 * 判断传输是否保持字节与服务器尺寸的一一对应。
	 *
	 * @return SFTP 或 FTP 二进制传输为真，文本转换模式为假。
	 */
	boolean byteExact() {
		return !(remote instanceof FtpSession) || ((FtpSession) remote).fileType == 2;
	}

	/**
	 * 校验递归列表中的单个名称，防止目录条目越过本地目标。
	 *
	 * @param name 单个条目名称。
	 * @return 原始名称。
	 * @throws IOException 名称无效。
	 */
	static String entryName(String name) throws IOException {
		text(name);
		if (name.isEmpty()
				|| name.equals(".")
				|| name.equals("..")
				|| name.contains("/")
				|| name.contains("\\")) {
			throw new IOException("目录条目不是有效的单个名称。");
		}
		return name;
	}

	/**
	 * 递归上传到新目录；链接及过深结构拒绝传输。
	 *
	 * @param task 任务状态。
	 * @param local 本地目录。
	 * @param target 远程目标目录。
	 * @param depth 当前递归深度。
	 * @throws Exception 文件、网络或取消失败。
	 */
	void uploadTree(Task task, File local, String target, int depth) throws Exception {
		check(task);
		if (depth > 128 || LocalFiles.isSymbolicLink(local) || !local.isDirectory()) {
			throw new IOException("目录过深、不是目录或包含链接。");
		}
		task.directory = true;
		task.total = -1;
		remote.mutate("mkdir", target, "");
		{
			for (File entry : LocalFiles.list(local)) {
				check(task);
				if (LocalFiles.isSymbolicLink(entry)) {
					throw new IOException("递归上传拒绝链接。");
				}
				String name = entryName(entry.getName());
				String remoteFile = target + "/" + name;
				if (entry.isDirectory()) {
					uploadTree(task, entry, remoteFile, depth + 1);
				} else {
					upload(task, entry, remoteFile, 0);
				}
			}
		}
	}

	/**
	 * 递归下载到新目录，不跟随远程链接。
	 *
	 * @param task 任务状态。
	 * @param source 远程目录。
	 * @param local 新建的本地目录。
	 * @param depth 当前递归深度。
	 * @throws Exception 文件、网络或取消失败。
	 */
	void downloadTree(Task task, String source, File local, int depth) throws Exception {
		check(task);
		if (depth > 128) {
			throw new IOException("递归目录超过 128 层。");
		}
		task.directory = true;
		task.total = -1;
		LocalFiles.createDirectory(local);
		for (FTP远程文件 entry : remote.list(source)) {
			check(task);
			if (entry.名称().equals(".") || entry.名称().equals("..")) {
				continue;
			}
			String name = entryName(entry.名称());
			if (entry.为链接() || entry.为未知()) {
				throw new IOException("递归下载拒绝链接或未知条目。");
			}
			if (entry.为目录()) {
				downloadTree(task, source + "/" + name, new File(local, name), depth + 1);
			} else {
				download(task, source + "/" + name, new File(local, name), 0);
			}
		}
	}

	/**
	 * 打开独占连接的 FTP 输入流。
	 *
	 * @param file 远程文件。
	 * @param offset 起始字节偏移。
	 * @return 受控输入流。
	 */
	FTP输入流 openInput(String file, long offset) {
		background();
		if (offset > 0 && !byteExact()) {
			throw new FTP错误("FTP 偏移续传要求二进制文件类型。");
		}
		if (offset < 0) {
			throw new FTP错误("偏移不能为负。");
		}
		enter();
		try {
			StreamLease opened = new StreamLease(this, remote.read(file, offset));
			if (disposed.get()) {
				opened.stream.close();
				throw new IOException("客户端已销毁。");
			}
			lease = opened;
			dataStream = opened.stream;
			return new FTP输入流(opened);
		} catch (Exception error) {
			closeNetwork();
			occupied.set(false);
			throw failure(error);
		}
	}

	/**
	 * 打开独占连接的 FTP 输出流。
	 *
	 * @param file 远程文件。
	 * @param offset 起始字节偏移。
	 * @return 受控输出流。
	 */
	FTP输出流 openOutput(String file, long offset) {
		background();
		if (offset > 0 && !byteExact()) {
			throw new FTP错误("FTP 偏移续传要求二进制文件类型。");
		}
		if (offset < 0) {
			throw new FTP错误("偏移不能为负。");
		}
		enter();
		try {
			if (offset > 0 && remote.stat(file).尺寸() != offset) {
				throw new IOException("续传偏移须等于远程已有尺寸。");
			}
			StreamLease opened = new StreamLease(this, remote.write(file, offset));
			if (disposed.get()) {
				opened.stream.close();
				throw new IOException("客户端已销毁。");
			}
			lease = opened;
			dataStream = opened.stream;
			return new FTP输出流(opened);
		} catch (Exception error) {
			closeNetwork();
			occupied.set(false);
			throw failure(error);
		}
	}

	/**
	 * 释放组件、任务、确认对话框及拥有的断点文件。
	 */
	void dispose() {
		if (!disposed.compareAndSet(false, true)) {
			return;
		}
		Task task = active;
		if (task != null) {
			task.cancelled = true;
		}
		activity.removeOnDestroyListener(destroyListener);
		handler.removeCallbacksAndMessages(null);
		EventDispatcher.unregisterEvent(this);
		worker.shutdownNow();
		new Thread(
						new Runnable() {
							@Override
							public void run() {
								closeNetwork();
								try {
									worker.awaitTermination(15, TimeUnit.SECONDS);
								} catch (InterruptedException error) {
									Thread.currentThread().interrupt();
								}
								for (File part : partials.values()) {
									try {
										LocalFiles.deleteIfExists(part);
									} catch (IOException ignored) {
										// 断点属于本实例；无法删除时仍不再对外提供该路径。
									}
								}
								partials.clear();
							}
						},
						"FileTransferDispose")
				.start();
	}

	@Override
	public void 销毁() {
		dispose();
	}
}
