package simple.library.wsd.downloader.internal;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;

/**
 * 通过真实本机 HTTP 验证断点、响应校验、状态竞态、持久化恢复和文件完整性。
 * 仅运行于主机 JDK，不交付到 Android 类库，也不替代设备端 SQLite 和界面验证。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class DownloadEngineTest {

	private static final byte[] DATA = new byte[524288];
	private static int checks;
	private static final AtomicInteger ranges = new AtomicInteger();
	private static final AtomicInteger callbacks = new AtomicInteger();
	private static String base;
	private static File root;

	private static final class MemoryStore implements DownloadEngine.Store {

		final Map<Integer, DownloadEngine.Record> records = new LinkedHashMap<Integer, DownloadEngine.Record>();

		@Override
		public synchronized List<DownloadEngine.Record> load() {
			List<DownloadEngine.Record> out = new ArrayList<DownloadEngine.Record>();
			for (DownloadEngine.Record r : records.values()) {
				out.add(r.copy());
			}
			return out;
		}

		@Override
		public synchronized void save(DownloadEngine.Record record) {
			records.put(record.id, record.copy());
		}
	}

	private static DownloadEngine engine(MemoryStore store) throws IOException {
		return new DownloadEngine(store, new DownloadEngine.Listener() {
			@Override
			public void progress(DownloadEngine.Record record) {
				// 进度通过任务快照断言；本监听器只统计完成回调。
			}

			@Override
			public void finished(DownloadEngine.Record record, boolean success) {
				callbacks.incrementAndGet();
			}
		});
	}

	private static void check(boolean value, String label) {
		if (!value) {
			throw new AssertionError(label);
		}
		checks++;
	}

	private static void await(BooleanSupplier condition) throws Exception {
		long until = System.currentTimeMillis() + 12000;
		while (!condition.getAsBoolean() && System.currentTimeMillis() < until) {
			Thread.sleep(10);
		}
		if (!condition.getAsBoolean()) {
			throw new AssertionError("等待任务状态超时。");
		}
	}

	private static int finish(DownloadEngine e, String endpoint, String name, boolean success) throws Exception {
		int id = e.start(base + endpoint, new File(root, name));
		await(() -> e.info(id).state == DownloadEngine.COMPLETE || e.info(id).state == DownloadEngine.FAILED);
		check(e.info(id).state == (success ? DownloadEngine.COMPLETE : DownloadEngine.FAILED), endpoint + " terminal state: " + e.info(id).error);
		return id;
	}

	private static void payload(HttpExchange x) throws IOException {
		String mode = x.getRequestURI().getPath();
		String range = x.getRequestHeaders().getFirst("Range");
		int start = 0;
		if (range != null && !mode.equals("/ignore")) {
			ranges.incrementAndGet();
			start = Integer.parseInt(range.substring(6, range.length() - 1));
		}
		if (mode.equals("/redirect")) {
			x.getResponseHeaders().add("Location", "/data");
			x.sendResponseHeaders(302, -1);
			x.close();
			return;
		}
		if (mode.equals("/missing")) {
			x.sendResponseHeaders(404, -1);
			x.close();
			return;
		}
		if (mode.equals("/large")) {
			x.getResponseHeaders().add("Content-Length", "3221225472");
			x.sendResponseHeaders(200, 3221225472L);
			try {
				x.getResponseBody().write(DATA, 0, 8192);
				x.getResponseBody().flush();
			} finally {
				x.close();
			}
			return;
		}
		x.getResponseHeaders().add("ETag", mode.equals("/changed") && range != null ? "\"new\"" : "\"fixture\"");
		if (range != null && !mode.equals("/ignore")) {
			x.getResponseHeaders().add("Content-Range",
				"bytes " + (mode.equals("/bad-range") ? start + 1 : start)
					+ "-" + (DATA.length - 1) + "/" + DATA.length);
		}
		boolean partial = range != null && !mode.equals("/ignore");
		x.sendResponseHeaders(partial ? 206 : 200, mode.equals("/chunked") ? 0 : DATA.length - start);
		OutputStream out = x.getResponseBody();
		try {
			int limit = mode.equals("/truncated") ? 32768 : DATA.length;
			for (int i = start; i < limit; i += 8192) {
				out.write(DATA, i, Math.min(8192, limit - i));
				out.flush();
				try {
					Thread.sleep(6);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					break;
				}
			}
		} catch (IOException expectedDisconnect) {
			// 暂停、取消和异常响应测试会主动结束连接。
		} finally {
			out.close();
			x.close();
		}
	}

	public static void main(String[] args) throws Exception {
		root = new File(args[0]);
		root.mkdirs();
		for (int i = 0; i < DATA.length; i++) {
			DATA[i] = (byte) (i * 31 + i / 257);
		}
		HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		ExecutorService httpThreads = Executors.newCachedThreadPool();
		server.setExecutor(httpThreads);
		server.createContext("/", new HttpHandler() {
			@Override
			public void handle(HttpExchange exchange) throws IOException {
				payload(exchange);
			}
		});
		server.start();
		base = "http://127.0.0.1:" + server.getAddress().getPort();
		MemoryStore store = new MemoryStore();
		DownloadEngine e = engine(store);
		try {
			int id = finish(e, "/data", "完整.bin", true);
			check(id == 1, "任务从1开始");
			check(Arrays.equals(DATA, Files.readAllBytes(new File(root, "完整.bin").toPath())), "普通下载内容完整");
			check(e.info(id).done == DATA.length && e.info(id).total == DATA.length, "完成字节数");
			try {
				e.start(base + "/data", new File(root, "完整.bin"));
				throw new AssertionError("覆盖目标");
			} catch (IOException expected) {
				checks++;
			}
			try {
				e.pause(-1);
				throw new AssertionError("负编号");
			} catch (IllegalArgumentException expected) {
				checks++;
			}
			try {
				e.start("file:/tmp/data", new File(root, "scheme.bin"));
				throw new AssertionError("不支持的协议");
			} catch (IOException expected) {
				checks++;
			}
			final DownloadEngine first = e;
			int paused = e.start(base + "/data", new File(root, "resume.bin"));
			await(() -> first.info(paused).done > 32768);
			e.pause(paused);
			long saved = e.info(paused).done;
			Thread.sleep(100);
			check(e.info(paused).state == DownloadEngine.PAUSED && e.info(paused).done == saved, "暂停后不继续写入");
			check(e.start(base + "/data", new File(root, "resume.bin")) == paused, "相同任务复用编号");
			await(() -> first.info(paused).state == DownloadEngine.COMPLETE);
			check(ranges.get() > 0, "续传发送Range");
			check(Arrays.equals(DATA, Files.readAllBytes(new File(root, "resume.bin").toPath())), "续传文件完整");
			int rapid = e.start(base + "/data", new File(root, "rapid.bin"));
			await(() -> first.info(rapid).done > 32768);
			e.pause(rapid);
			e.resume(rapid);
			await(() -> first.info(rapid).state == DownloadEngine.COMPLETE);
			check(Arrays.equals(DATA, Files.readAllBytes(new File(root, "rapid.bin").toPath())), "立即暂停继续无并发破坏");
			for (String mode : new String[] { "/ignore", "/bad-range", "/changed" }) {
				String name = mode.substring(1) + ".bin";
				int task = e.start(base + mode, new File(root, name));
				await(() -> first.info(task).done > 32768);
				e.pause(task);
				e.resume(task);
				await(() -> first.info(task).state == DownloadEngine.COMPLETE || first.info(task).state == DownloadEngine.FAILED);
				if (mode.equals("/ignore")) {
					check(Arrays.equals(DATA, Files.readAllBytes(new File(root, name).toPath())), "忽略Range时完整重下");
				} else {
					check(e.info(task).state == DownloadEngine.FAILED && !new File(root, name).exists(), mode + "拒绝发布错误文件");
				}
			}
			finish(e, "/missing", "404.bin", false);
			finish(e, "/truncated", "short.bin", false);
			check(!new File(root, "short.bin").exists(), "截断文件不发布");
			finish(e, "/chunked", "chunked.bin", true);
			check(Arrays.equals(DATA, Files.readAllBytes(new File(root, "chunked.bin").toPath())), "未知大小响应完整");
			finish(e, "/redirect", "redirect.bin", true);
			int large = e.start(base + "/large", new File(root, "large.bin"));
			await(() -> first.info(large).total == 3221225472L);
			check(e.info(large).total > Integer.MAX_VALUE, "大于2GB长度不溢出");
			e.cancel(large);
			int cancelled = e.start(base + "/data", new File(root, "cancel.bin"));
			await(() -> first.info(cancelled).done > 32768);
			int callbackBefore = callbacks.get();
			e.cancel(cancelled);
			Thread.sleep(500);
			check(e.info(cancelled).state == DownloadEngine.CANCELLED && !new File(root, "cancel.bin").exists(), "取消后无目标文件");
			check(!new File(root, ".cancel.bin.es4a-" + cancelled + ".part").exists(), "取消清理临时文件");
			check(callbacks.get() == callbackBefore, "取消没有旧完成回调");
			int restore = e.start(base + "/data", new File(root, "restore.bin"));
			await(() -> first.info(restore).done > 32768);
			e.close();
			Thread.sleep(100);
			e = engine(store);
			final DownloadEngine restored = e;
			check(e.info(restore).state == DownloadEngine.PAUSED, "重启恢复暂停状态");
			check(e.start(base + "/data", new File(root, "restore.bin")) == restore, "重启保留任务编号");
			await(() -> restored.info(restore).state == DownloadEngine.COMPLETE);
			check(Arrays.equals(DATA, Files.readAllBytes(new File(root, "restore.bin").toPath())), "重启续传内容完整");
			int next = e.start(base + "/missing", new File(root, "next.bin"));
			check(next > restore, "编号单调递增");
			System.out.println("DownloadEngineTest passed: " + checks + " checks");
		} finally {
			e.close();
			server.stop(0);
			httpThreads.shutdownNow();
		}
	}
}
