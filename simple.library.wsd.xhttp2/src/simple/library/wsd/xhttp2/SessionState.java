package simple.library.wsd.xhttp2;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.util.Log;

import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.EventListener;
import okhttp3.OkHttpClient;

/**
 * 同名会话共用一份 Cookie 和缓存句柄，最后一个使用者退出后关闭磁盘句柄并释放注册项。 客户端默认使用独立随机会话；明确指定同一名称才共享状态，不引用 XHttp2 全局 Cookie
 * 单例。
 *
 * @author 树先生 xhwsd@qq.com
 */
final class SessionState {

	private static final Map<String, SessionState> OPEN = new HashMap<String, SessionState>();
	final CookieStore cookies;
	final CacheStore cache;
	final Cache httpCache;
	final OkHttpClient baseClient;
	private final String key;
	private final boolean persistCookies;
	private final boolean temporary;
	final File directory;
	private final long responseCapacity;
	private final long httpCapacity;
	private int references;

	/**
	 * 创建会话专属 Cookie、两种缓存和连接池。
	 *
	 * @param context       用于访问应用文件及资产的上下文。
	 * @param key           来自响应的实际缓存键或内部会话键。
	 * @param persistent    是否持久化有有效期的 Cookie。
	 * @param temporary     本次临时文件或匿名会话标记。
	 * @param responseBytes 响应缓存容量字节数。
	 * @param httpBytes     HTTP 缓存容量字节数。
	 * @throws IOException 当前读取、校验或传输无法完成。
	 */
	private SessionState(Context context, String key, boolean persistent, boolean temporary, long responseBytes,
			long httpBytes) throws IOException {
		this.key = key;
		persistCookies = persistent;
		this.temporary = temporary;
		responseCapacity = responseBytes;
		httpCapacity = httpBytes;
		String suffix = HttpSupport.digest(key.getBytes(StandardCharsets.UTF_8));
		directory = new File(context.getCacheDir(), "es4a-xhttp2/" + suffix);
		cookies = new CookieStore(
				persistent ? context.getSharedPreferences("es4a-xhttp2-" + suffix, Context.MODE_PRIVATE) : null);
		cache = new CacheStore(new File(directory, "responses"), responseBytes);
		httpCache = new Cache(new File(directory, "http"), httpBytes);
		baseClient = new OkHttpClient.Builder().cookieJar(cookies).cache(httpCache)
				.eventListenerFactory(new EventListener.Factory() {
					@Override
					public EventListener create(Call call) {
						return new EventListener() {
							private boolean held;

							@Override
							public synchronized void callStart(Call call) {
								SessionState.this.retain();
								held = true;
							}

							@Override
							public void callEnd(Call call) {
								finished();
							}

							@Override
							public void callFailed(Call call, IOException failure) {
								finished();
							}

							/**
							 * 在真实调用结束时最多释放一次会话引用。
							 */
							private synchronized void finished() {
								if (held) {
									held = false;
									try {
										SessionState.this.release();
									} catch (IOException failure) {
										Log.e("XHttp2", "传输退出后关闭会话失败", failure);
									}
								}
							}
						};
					}
				}).build();
	}

	/**
	 * 取得或创建同名会话，拒绝不同持久化或容量设置混用。
	 *
	 * @param context       用于访问应用文件及资产的上下文。
	 * @param name          字段或会话名称。
	 * @param persistent    是否持久化有有效期的 Cookie。
	 * @param temporary     本次临时文件或匿名会话标记。
	 * @param responseBytes 响应缓存容量字节数。
	 * @param httpBytes     HTTP 缓存容量字节数。
	 * @return 或创建同名会话，拒绝不同持久化或容量设置混用。
	 * @throws IOException 当前读取、校验或传输无法完成。
	 */
	static synchronized SessionState acquire(Context context, String name, boolean persistent, boolean temporary,
			long responseBytes, long httpBytes) throws IOException {
		String key = context.getPackageName() + ":" + HttpSupport.nonempty(name);
		SessionState result = OPEN.get(key);
		if (result == null) {
			result = new SessionState(context.getApplicationContext(), key, persistent, temporary, responseBytes,
					httpBytes);
			OPEN.put(key, result);
		} else if (result.persistCookies != persistent || result.temporary != temporary
				|| result.responseCapacity != responseBytes || result.httpCapacity != httpBytes) {
			throw new IllegalStateException("同名活动会话的持久化和缓存容量设置必须一致。");
		}
		synchronized (result) {
			result.references++;
		}
		return result;
	}

	/**
	 * 每个已提交任务额外占有引用，防止客户端销毁先于工作线程退出而关闭缓存。
	 *
	 * @return 独立的结果值或快照，语义与方法说明一致。
	 */
	synchronized SessionState retain() {
		if (references <= 0) {
			throw new IllegalStateException("会话已经关闭。");
		}
		references++;
		return this;
	}

	/**
	 * 释放会话引用，最后一个使用者负责关闭资源和清理匿名目录。
	 *
	 * @throws IOException 当前读取、校验或传输无法完成。
	 */
	void release() throws IOException {
		synchronized (SessionState.class) {
			synchronized (this) {
				if (--references > 0) {
					return;
				}
				if (references < 0) {
					throw new IllegalStateException("会话重复释放。");
				}
				OPEN.remove(key);
			}
			try {
				if (temporary) {
					cache.discard();
				} else {
					cache.close();
				}
			} finally {
				try {
					if (temporary) {
						File http = new File(directory, "http");
						// OkHttp 的磁盘缓存延迟创建，未启用 HTTP 缓存时不能删除不存在的目录。
						if (http.isDirectory()) {
							httpCache.delete();
						} else {
							httpCache.close();
						}
						if (http.exists() && !http.delete()) {
							throw new IOException("无法清除匿名 HTTP 缓存目录。");
						}
						if (directory.exists() && !directory.delete()) {
							throw new IOException("无法清除匿名会话目录。");
						}
					} else {
						httpCache.close();
					}
				} finally {
					baseClient.connectionPool().evictAll();
					baseClient.dispatcher().executorService().shutdown();
				}
			}
		}
	}
}
