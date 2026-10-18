package simple.library.wsd.downloader.internal;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 文件下载状态机；网络和断点校验与 Android 组件、SQLite 分离，便于真实 HTTP 回归。
 * 所有任务状态和文件写入由同一把锁协调，暂停、取消和关闭后旧任务不能继续写入。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class DownloadEngine {

	public static final int WAITING = 0;
	public static final int RUNNING = 1;
	public static final int PAUSED = 2;
	public static final int COMPLETE = 3;
	public static final int FAILED = 4;
	public static final int CANCELLED = 5;
	private static final Pattern CONTENT_RANGE = Pattern.compile("bytes (\\d+)-(\\d+)/(\\d+|\\*)");

	/**
	 * 提供下载记录的读取和保存接口。
	 * 保存操作可能在引擎持锁时调用，实现不得反向调用引擎或保留传入记录的可变引用。
	 */
	public interface Store {

		/**
		 * 按任务编号升序读取记录。
		 *
		 * @return 由引擎接管的独立记录列表。
		 * @throws IOException 无法读取持久化记录。
		 */
		List<Record> load() throws IOException;

		/**
		 * 按稳定编号保存或更新一条记录。
		 *
		 * @param record 本次保存的记录副本。
		 * @throws IOException 无法保存记录。
		 */
		void save(Record record) throws IOException;
	}

	/**
	 * 引擎持锁通知记录副本；接收者应投递事件，避免在锁内执行界面业务或阻塞等待。
	 * 延迟消费前通过 current 校验代次和状态，丢弃暂停、取消或重启前的旧通知。
	 */
	public interface Listener {

		/**
		 * 接收传输开始、周期检查或下载完成时的进度。
		 *
		 * @param record 通知时的独立记录副本。
		 */
		void progress(Record record);

		/**
		 * 接收成功或失败结果；暂停和取消不调用此方法。
		 *
		 * @param record 通知时的独立记录副本。
		 * @param success 是否已发布完整目标文件。
		 */
		void finished(Record record, boolean success);
	}

	/**
	 * 保存任务状态、文件信息和续传校验信息。
	 * 记录可修改；持久化和回调使用副本，下载线程只修改引擎持有的原记录。
	 */
	public static final class Record {

		/**
		 * 从 1 开始的稳定任务编号。
		 */
		public int id;

		/**
		 * 完整 HTTP/HTTPS 网址。
		 */
		public String url = "";

		/**
		 * 目标文件的规范绝对路径。
		 */
		public String path = "";

		/**
		 * 引擎状态常量，默认 WAITING。
		 */
		public int state;

		/**
		 * 总字节数；未知时为 -1。
		 */
		public long total = -1;

		/**
		 * 最近一次记录的已写入字节数，续传时以实际临时文件长度为准。
		 */
		public long done;

		/**
		 * 原始 ETag 响应头；弱标识不用于 If-Range。
		 */
		public String etag = "";

		/**
		 * Last-Modified 响应头，作为强 ETag 不可用时的续传校验标识。
		 */
		public String modified = "";

		/**
		 * 最近错误；无错误时为空文本。
		 */
		public String error = "";

		/**
		 * 进程内任务代次，用于隔离旧传输和延迟回调。
		 */
		public long generation;

		/**
		 * 创建不共享可变状态的记录副本。
		 *
		 * @return 当前字段值的独立副本。
		 */
		public Record copy() {
			Record other = new Record();
			other.id = id;
			other.url = url;
			other.path = path;
			other.state = state;
			other.total = total;
			other.done = done;
			other.etag = etag;
			other.modified = modified;
			other.error = error;
			other.generation = generation;
			return other;
		}
	}

	private final Store store;
	private final Listener listener;
	private final Map<Integer, Record> records = new LinkedHashMap<Integer, Record>();
	private final Map<Integer, Transfer> transfers = new LinkedHashMap<Integer, Transfer>();
	private final ExecutorService executor = Executors.newFixedThreadPool(2);
	private int nextId = 1;
	private boolean closed;

	/**
	 * 加载历史记录，将上次未收尾的等待、下载状态恢复为暂停。
	 *
	 * @param store 应用级持久化实现。
	 * @param listener 持锁调用的事件接收者。
	 * @throws IOException 记录编号无效，或记录读取、恢复保存失败。
	 */
	public DownloadEngine(Store store, Listener listener) throws IOException {
		this.store = store;
		this.listener = listener;

		for (Record record : store.load()) {
			if (record.id < 1 || record.id == Integer.MAX_VALUE || records.containsKey(record.id)) {
				throw new IOException("下载记录的任务编号无效。");
			}

			if (record.state == WAITING || record.state == RUNNING) {
				record.state = PAUSED;
			}

			records.put(record.id, record);
			nextId = Math.max(nextId, record.id + 1);
			store.save(record.copy());
		}
	}

	/**
	 * 创建异步任务；相同网址和目标的未完成任务复用稳定编号。
	 *
	 * @param address HTTP/HTTPS 完整网址。
	 * @param target 目标文件，不能覆盖已有文件。
	 * @return 新任务或复用任务的稳定编号。
	 * @throws IOException 引擎已关闭、网址或路径无效、目标冲突、编号用尽，或保存失败。
	 */
	public synchronized int start(String address, File target) throws IOException {
		ensureOpen();
		validateUrl(address);
		File file = target.getCanonicalFile();
		if (file.getParentFile() == null || file.getName().length() == 0) {
			throw new IOException("保存路径必须指向文件。");
		}

		for (Record record : records.values()) {
			if (!record.path.equals(file.getPath()) || record.state == CANCELLED) {
				continue;
			}

			if (record.state == COMPLETE) {
				continue;
			}

			if (!record.url.equals(address)) {
				throw new IOException("另一个下载任务正在使用相同保存路径。");
			}

			resume(record.id);
			return record.id;
		}

		if (file.exists()) {
			throw new IOException("目标文件已经存在，请使用其它文件名。");
		}

		if (nextId == Integer.MAX_VALUE) {
			throw new IOException("下载任务编号已用尽。");
		}

		Record record = new Record();
		record.id = nextId++;
		record.url = address;
		record.path = file.getPath();
		store.save(record.copy());
		records.put(record.id, record);
		queue(record);
		return record.id;
	}

	/**
	 * 读取任务快照，不执行网络请求。
	 *
	 * @param id 稳定任务编号。
	 * @return 当前任务的独立副本。
	 * @throws IllegalArgumentException 任务编号不存在。
	 */
	public synchronized Record info(int id) {
		return required(id).copy();
	}

	/**
	 * 按编号返回等待、下载、暂停和失败任务。
	 *
	 * @return 不包含已完成和取消任务的独立副本列表。
	 */
	public synchronized List<Record> unfinished() {
		List<Record> result = new ArrayList<Record>();

		for (Record record : records.values()) {
			if (record.state != COMPLETE && record.state != CANCELLED) {
				result.add(record.copy());
			}
		}

		return result;
	}

	/**
	 * 校验延迟回调的快照是否仍对应当前任务状态。
	 *
	 * @param snapshot 回调携带的记录副本。
	 * @return 引擎未关闭，且编号、代次和状态均匹配时为 true。
	 */
	public synchronized boolean current(Record snapshot) {
		Record record = records.get(snapshot.id);
		return !closed && record != null
			&& record.generation == snapshot.generation && record.state == snapshot.state;
	}

	/**
	 * 暂停等待或下载中的任务；其它状态保持不变。
	 *
	 * @param id 稳定任务编号。
	 * @throws IllegalArgumentException 任务编号不存在。
	 * @throws IOException 引擎已关闭或记录保存失败。
	 */
	public synchronized void pause(int id) throws IOException {
		ensureOpen();
		Record record = required(id);
		if (record.state != WAITING && record.state != RUNNING) {
			return;
		}

		record.state = PAUSED;
		record.generation++;
		stop(record);
		store.save(record.copy());
	}

	/**
	 * 将暂停或失败任务重新排队；旧传输未收尾时等待其释放文件后再启动。
	 *
	 * @param id 稳定任务编号。
	 * @throws IllegalArgumentException 任务编号不存在。
	 * @throws IOException 引擎已关闭、任务已完成或取消，或记录保存失败。
	 */
	public synchronized void resume(int id) throws IOException {
		ensureOpen();
		Record record = required(id);
		if (record.state == COMPLETE || record.state == CANCELLED) {
			throw new IOException("已完成或已取消的任务不能继续。");
		}

		if (record.state == WAITING || record.state == RUNNING) {
			if (!transfers.containsKey(id)) {
				queue(record);
			}

			return;
		}

		record.state = WAITING;
		record.error = "";
		record.generation++;
		store.save(record.copy());
		if (!transfers.containsKey(id)) {
			queue(record);
		}
	}

	/**
	 * 取消任务，仅删除本任务临时文件；活动传输持有文件时延后清理。
	 *
	 * @param id 稳定任务编号。
	 * @throws IllegalArgumentException 任务编号不存在。
	 * @throws IOException 引擎已关闭、临时文件删除失败，或记录保存失败。
	 */
	public synchronized void cancel(int id) throws IOException {
		ensureOpen();
		Record record = required(id);
		if (record.state == COMPLETE || record.state == CANCELLED) {
			return;
		}

		record.state = CANCELLED;
		record.generation++;
		stop(record);
		File part = part(record);
		if (!transfers.containsKey(id) && part.exists() && !part.delete()) {
			throw new IOException("无法删除下载临时文件。");
		}

		record.done = 0;
		store.save(record.copy());
	}

	/**
	 * 关闭引擎并保存活动任务为暂停状态；重复关闭不执行额外操作。
	 *
	 * @throws IOException 活动任务记录保存失败；仍会停止传输并关闭线程池。
	 */
	public synchronized void close() throws IOException {
		if (closed) {
			return;
		}

		closed = true;
		IOException failure = null;

		for (Record record : records.values()) {
			if (record.state == WAITING || record.state == RUNNING) {
				record.state = PAUSED;
				record.generation++;
				stop(record);

				try {
					store.save(record.copy());
				} catch (IOException e) {
					failure = e;
				}
			}
		}

		executor.shutdownNow();
		if (failure != null) {
			throw failure;
		}
	}

	/**
	 * 将下载记录加入调度队列。
	 *
	 * @param record 当前下载记录
	 * @throws IOException 访问文件、流或网络资源失败时
	 */
	private void queue(Record record) throws IOException {
		record.state = WAITING;
		record.generation++;
		store.save(record.copy());
		Transfer transfer = new Transfer(record, record.generation);
		transfers.put(record.id, transfer);
		executor.execute(transfer);
	}

	/**
	 * 停止指定下载任务。
	 *
	 * @param record 当前下载记录
	 */
	private void stop(Record record) {
		// 网络请求由超时或下一次分块读返回收尾，避免在界面线程关闭阻塞中的流。
		Transfer transfer = transfers.get(record.id);
		if (transfer != null) {
			transfer.stopped = true;
		}
	}

	/**
	 * 确保下载引擎尚未关闭。
	 *
	 * @throws IOException 访问文件、流或网络资源失败时
	 */
	private void ensureOpen() throws IOException {
		if (closed) {
			throw new IOException("下载器已经关闭。");
		}
	}

	/**
	 * 查找下载任务，不存在时拒绝操作。
	 *
	 * @param id 操作或任务标识
	 * @return 已登记的下载任务
	 */
	private Record required(int id) {
		Record record = records.get(id);
		if (record == null) {
			throw new IllegalArgumentException("下载任务不存在：" + id);
		}

		return record;
	}

	/**
	 * 校验下载地址使用受支持的 HTTP 协议。
	 *
	 * @param address 资源或服务器地址
	 * @return 校验通过的下载地址
	 * @throws IOException 访问文件、流或网络资源失败时
	 */
	private static URL validateUrl(String address) throws IOException {
		URL url = new URL(address);
		if (!url.getProtocol().equals("http") && !url.getProtocol().equals("https")) {
			throw new IOException("下载地址只支持 HTTP 和 HTTPS。");
		}

		return url;
	}

	/**
	 * 取得下载记录对应的分段临时文件。
	 *
	 * @param record 当前下载记录
	 * @return 下载记录对应的分段临时文件
	 */
	private static File part(Record record) {
		File target = new File(record.path);
		return new File(target.getParentFile(), "." + target.getName() + ".es4a-" + record.id + ".part");
	}

	/**
	 * 将空引用规范为空文本。
	 *
	 * @param value 需要将空引用规范为空文本的输入。
	 * @return 校验或规范化后的文本
	 */
	private static String text(String value) {
		return value == null ? "" : value;
	}

	/**
	 * 执行一次下载传输，创建时记录当前任务代次。
	 * 任务暂停、取消或重新排队后，旧传输不能继续写入文件或通知完成。
	 */
	private final class Transfer implements Runnable {

		private final Record record;
		private final long generation;
		private volatile boolean stopped;

		/**
		 * 绑定本次下载记录和任务代次，防止旧任务更新新状态。
		 *
		 * @param record 当前下载记录
		 * @param generation 本轮任务代次，用于排除过期结果
		 */
		Transfer(Record record, long generation) {
			this.record = record;
			this.generation = generation;
		}

		/**
		 * 判断当前下载代次是否仍有效且未停止。
		 *
		 * @return 当前下载代次仍有效且未停止时为 {@code true}，否则为 {@code false}
		 */
		private boolean active() {
			return !closed && !stopped && record.generation == generation;
		}

		@Override
		public void run() {
			try {
				synchronized (DownloadEngine.this) {
					if (!active()) {
						return;
					}

					record.state = RUNNING;
					store.save(record.copy());
				}

				download();
			} catch (Exception e) {
				synchronized (DownloadEngine.this) {
					if (active()) {
						record.state = FAILED;
						record.error = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();

						try {
							store.save(record.copy());
						} catch (IOException saveError) {
							record.error += "；无法保存断点：" + saveError.getMessage();
						}

						listener.finished(record.copy(), false);
					}
				}
			} finally {
				synchronized (DownloadEngine.this) {
					transfers.remove(record.id);
					if (record.state == CANCELLED) {
						File partial = part(record);
						if (partial.exists() && !partial.delete()) {
							record.error = "任务已取消，但无法删除临时文件。";
							if (!closed) {
								try {
									store.save(record.copy());
								} catch (IOException ignored) {
									// 清理错误已保留在内存中；保存失败不能阻止传输收尾。
								}
							}
						}
					}

					if (!closed && record.state == WAITING) {
						try {
							queue(record);
						} catch (IOException e) {
							record.state = FAILED;
							record.error = e.getMessage();
							listener.finished(record.copy(), false);
						}
					}
				}
			}
		}

		/**
		 * 按续传记录接收响应并写入分段文件。
		 *
		 * @throws IOException 访问文件、流或网络资源失败时
		 */
		private void download() throws IOException {
			// 目标文件与临时文件分开管理；已有最终文件不能被本次下载覆盖。
			File target = new File(record.path);
			File partial = part(record);

			synchronized (DownloadEngine.this) {
				if (!active()) {
					return;
				}

				if (target.exists()) {
					throw new IOException("目标文件已经存在，下载器不会覆盖它。");
				}

				File parent = target.getParentFile();
				if (!parent.isDirectory() && !parent.mkdirs()) {
					throw new IOException("无法创建保存目录。");
				}
			}

			// 临时文件实际长度是断点；缺少可用文件标识时从头下载。
			long offset;
			String validator;

			synchronized (DownloadEngine.this) {
				offset = partial.length();
				validator = !record.etag.startsWith("W/") && record.etag.length() > 0
					? record.etag : record.modified;
				if (validator.length() == 0) {
					offset = 0;
				}
			}

			HttpURLConnection connection = null;
			InputStream input = null;
			RandomAccessFile output = null;

			try {
				connection = connect(record.url, offset, validator);
				int status = connection.getResponseCode();

				// 服务端拒绝旧范围时只重试一次完整请求，重新校验响应。
				if (status == 416 && offset > 0) {
					connection.disconnect();
					offset = 0;
					connection = connect(record.url, 0, "");
					status = connection.getResponseCode();
				}

				// 先验证响应范围、长度和文件标识，再允许写入临时文件。
				long expected;
				if (status == 206) {
					Matcher range = CONTENT_RANGE.matcher(text(connection.getHeaderField("Content-Range")));
					if (!range.matches()) {
						throw new IOException("续传响应缺少有效的 Content-Range。");
					}

					long start = Long.parseLong(range.group(1));
					long end = Long.parseLong(range.group(2));

					expected = range.group(3).equals("*") ? -1 : Long.parseLong(range.group(3));
					if (start != offset || end < start || expected < 0 || end != expected - 1) {
						throw new IOException("续传范围与本地断点不一致。");
					}

					String length = connection.getHeaderField("Content-Length");
					if (length != null && Long.parseLong(length) != end - start + 1) {
						throw new IOException("续传响应长度与范围不一致。");
					}

					String etag = text(connection.getHeaderField("ETag"));
					String modified = text(connection.getHeaderField("Last-Modified"));

					if (offset > 0 && ((record.etag.length() > 0
							&& etag.length() > 0 && !record.etag.equals(etag))
						|| (record.etag.length() == 0 && record.modified.length() > 0
							&& modified.length() > 0 && !record.modified.equals(modified)))) {
						throw new IOException("服务器文件已变化，请取消后重新下载。");
					}
				} else if (status == 200) {
					offset = 0;
					String length = connection.getHeaderField("Content-Length");
					expected = length == null ? -1 : Long.parseLong(length);
					if (expected < -1) {
						throw new IOException("服务器返回了无效文件大小。");
					}
				} else {
					throw new IOException("下载请求失败，HTTP " + status);
				}

				// 断点按原始字节计数，压缩传输会使本地偏移与响应长度失去可比性。
				String encoding = text(connection.getHeaderField("Content-Encoding"));
				if (encoding.length() > 0 && !encoding.equalsIgnoreCase("identity")) {
					throw new IOException("服务器未按原始字节传输文件，无法校验下载断点。");
				}

				synchronized (DownloadEngine.this) {
					if (!active()) {
						return;
					}

					// 在当前任务仍有效时打开临时文件，并持久化本次续传起点。
					output = new RandomAccessFile(partial, "rw");
					output.setLength(offset);
					output.seek(offset);
					record.done = offset;
					record.total = expected;
					record.etag = text(connection.getHeaderField("ETag"));
					record.modified = text(connection.getHeaderField("Last-Modified"));
					store.save(record.copy());
					listener.progress(record.copy());
				}

				// 逐块写入并定期保存断点；每次写入前检查任务是否已被暂停或替换。
				input = connection.getInputStream();
				byte[] buffer = new byte[32768];
				long checkpoint = System.nanoTime();
				int count;

				while ((count = input.read(buffer)) != -1) {
					synchronized (DownloadEngine.this) {
						if (!active()) {
							return;
						}

						if (record.total >= 0 && count > record.total - record.done) {
							throw new IOException("收到的数据超过服务器声明的文件大小。");
						}

						output.write(buffer, 0, count);
						record.done += count;
						long now = System.nanoTime();
						if (now - checkpoint >= 200000000L) {
							store.save(record.copy());
							listener.progress(record.copy());
							checkpoint = now;
						}
					}
				}

				synchronized (DownloadEngine.this) {
					if (!active()) {
						return;
					}

					if (record.total >= 0 && record.done != record.total) {
						throw new IOException("连接提前结束，文件未下载完整。");
					}

					// 完整性校验通过后落盘并改名，只有最终文件就位才发布完成结果。
					output.getFD().sync();
					output.close();
					output = null;
					if (target.exists() || !partial.renameTo(target)) {
						throw new IOException("无法保存下载结果，临时文件已保留。");
					}

					record.total = record.done;
					record.state = COMPLETE;
					record.error = "";
					store.save(record.copy());
					listener.progress(record.copy());
					listener.finished(record.copy(), true);
				}
			} finally {
				if (input != null) {
					try {
						input.close();
					} catch (IOException ignored) {
						// 收尾关闭失败不覆盖原始下载结果。
					}
				}

				if (output != null) {
					try {
						output.close();
					} catch (IOException ignored) {
						// 保留临时文件和原始错误，供下次继续或取消。
					}
				}

				if (connection != null) {
					connection.disconnect();
				}
			}
		}

		/**
		 * 创建 HTTP 连接并设置续传请求头。
		 *
		 * @param address 资源或服务器地址
		 * @param offset 起始偏移量
		 * @param validator 续传校验标识
		 * @return 创建 HTTP 连接并设置续传请求头
		 * @throws IOException 访问文件、流或网络资源失败时
		 */
		private HttpURLConnection connect(String address, long offset, String validator) throws IOException {
			URL url = validateUrl(address);

			for (int redirects = 0; redirects <= 5; redirects++) {
				synchronized (DownloadEngine.this) {
					if (!active()) {
						throw new IOException("任务已停止。");
					}
				}

				HttpURLConnection connection = (HttpURLConnection) url.openConnection();

				try {
					connection.setConnectTimeout(10000);
					connection.setReadTimeout(10000);
					connection.setInstanceFollowRedirects(false);
					connection.setRequestProperty("Accept-Encoding", "identity");
					if (offset > 0) {
						connection.setRequestProperty("Range", "bytes=" + offset + "-");
						connection.setRequestProperty("If-Range", validator);
					}

					int status = connection.getResponseCode();
					if (status != 301 && status != 302 && status != 303 && status != 307 && status != 308) {
						return connection;
					}

					String location = connection.getHeaderField("Location");
					if (location == null) {
						throw new IOException("重定向响应缺少地址。");
					}

					URL next = validateUrl(new URL(url, location).toString());
					if (url.getProtocol().equals("https") && next.getProtocol().equals("http")) {
						throw new IOException("拒绝将 HTTPS 下载重定向到 HTTP。");
					}

					url = next;
				} catch (IOException e) {
					connection.disconnect();
					throw e;
				}

				connection.disconnect();
			}

			throw new IOException("下载重定向次数过多。");
		}
	}
}
