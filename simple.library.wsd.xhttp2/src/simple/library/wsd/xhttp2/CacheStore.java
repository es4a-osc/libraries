package simple.library.wsd.xhttp2;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import com.google.gson.Gson;
import com.jakewharton.disklrucache.DiskLruCache;
import com.xuexiang.xhttp2.cache.RxCache;

/**
 * 持有可显式关闭的命名空间磁盘缓存，向每次请求的 XHttp2 策略提供独立的内存视图。
 * 上游 RxCache 没有关闭磁盘缓存的接口，故不为每个请求反复打开其磁盘实现。
 * 有效期依据原始保存时间核对，不能因读取并注入策略内存而重新计算有效期。
 *
 * @author 树先生 xhwsd@qq.com
 */
final class CacheStore implements AutoCloseable {

	private final File directory;
	private DiskLruCache disk;
	private final Gson gson = new Gson();

	CacheStore(File directory) throws IOException {
		this.directory = directory;
		disk = open();
	}

	/**
	 * 每次请求只有一个键，内存视图不共享可变 TTL 或结果，缓存选版继续复用原生 IStrategy。
	 */
	synchronized RxCache view(String key, long seconds) throws IOException {
		RxCache result = new RxCache.Builder().isDiskCache(false).memoryMaxSize(1).build();
		WireReply cached = load(key, seconds);
		if (cached != null) {
			result.save(key, cached).blockingFirst();
		}
		return result;
	}

	private WireReply load(String key, long seconds) throws IOException {
		if (seconds == 0) {
			return null;
		}
		try (DiskLruCache.Snapshot snapshot = disk.get(validKey(key))) {
			if (snapshot == null) {
				return null;
			}
			long saved = Long.parseLong(snapshot.getString(1));
			if (seconds >= 0 && (System.currentTimeMillis() - saved) / 1000 >= seconds) {
				return null;
			}
			return gson.fromJson(new InputStreamReader(snapshot.getInputStream(0), StandardCharsets.UTF_8), WireReply.class);
		} catch (NumberFormatException malformed) {
			throw new IOException("缓存时间记录损坏。", malformed);
		}
	}

	synchronized void save(String key, WireReply reply) throws IOException {
		String valid = validKey(key);
		DiskLruCache.Editor editor = disk.edit(valid);
		if (editor == null) {
			throw new IOException("缓存条目正在写入。");
		}
		try {
			try (OutputStreamWriter writer = new OutputStreamWriter(editor.newOutputStream(0), StandardCharsets.UTF_8)) {
				gson.toJson(reply, writer);
			}
			editor.set(1, Long.toString(System.currentTimeMillis()));
			editor.commit();
			disk.flush();
		} catch (IOException | RuntimeException failure) {
			editor.abortUnlessCommitted();
			throw failure;
		}
	}

	synchronized boolean remove(String key) throws IOException {
		boolean removed = disk.remove(validKey(key));
		disk.flush();
		return removed;
	}

	synchronized void clear() throws IOException {
		// 所有读写均由本实例串行执行，关闭并重新打开即可复用原生目录清理能力。
		disk.delete();
		disk = open();
	}

	private DiskLruCache open() throws IOException {
		return DiskLruCache.open(directory, 1, 2, 50L * 1024 * 1024);
	}

	private static String validKey(String key) {
		if (key == null || !key.matches("[a-f0-9]{64}")) {
			throw new IllegalArgumentException("缓存键必须来自响应中的实际缓存键。");
		}
		return key;
	}

	@Override
	public synchronized void close() throws IOException {
		disk.close();
	}

	/**
	 * 默认匿名会话不跨客户端生命周期保留磁盘内容，命名会话仍仅关闭句柄。
	 */
	synchronized void discard() throws IOException {
		disk.delete();
		if (directory.exists() && !directory.delete()) {
			throw new IOException("无法清除匿名响应缓存目录。");
		}
	}
}
