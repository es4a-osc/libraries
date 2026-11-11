package simple.library.wsd.xhttp2;

import java.io.File;
import java.io.EOFException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.InetSocketAddress;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.net.ssl.SSLException;

import android.content.Context;
import android.os.SystemClock;
import android.util.Base64;

import com.xuexiang.xhttp2.cache.RxCache;
import com.xuexiang.xhttp2.cache.model.CacheResult;
import com.xuexiang.xhttp2.cache.stategy.CacheAndRemoteDistinctStrategy;
import com.xuexiang.xhttp2.cache.stategy.CacheAndRemoteStrategy;
import com.xuexiang.xhttp2.cache.stategy.FirstCacheStategy;
import com.xuexiang.xhttp2.cache.stategy.FirstRemoteStrategy;
import com.xuexiang.xhttp2.cache.stategy.IStrategy;
import com.xuexiang.xhttp2.cache.stategy.NoStrategy;
import com.xuexiang.xhttp2.cache.stategy.OnlyCacheStrategy;
import com.xuexiang.xhttp2.cache.stategy.OnlyRemoteStrategy;
import com.xuexiang.xhttp2.callback.impl.IProgressResponseCallBack;
import com.xuexiang.xhttp2.request.body.UploadProgressRequestBody;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

import okhttp3.Authenticator;
import okhttp3.Credentials;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.Route;

import retrofit2.Response;

/**
 * 请求快照到 XHttp2/Retrofit 响应链的适配，保留响应元数据、二进制和可取消的文件流。 本层只在工作线程订阅，不直接分发 Simple 事件，也不读取窗口的可变配置。
 *
 * @author 树先生 xhwsd@qq.com
 */
final class HttpEngine {

	private static final ConcurrentHashMap<String, Object> DOWNLOADS = new ConcurrentHashMap<String, Object>();
	private final Context context;
	private final SessionState session;
	private final ClientSettings settings;
	private final XH2请求 request;
	private final TransferControl control;
	private final String id;
	private final long started = SystemClock.elapsedRealtime();
	private String cacheKey;
	private int cacheMode;
	private long cacheSeconds;
	private HttpUrl url;

	/**
	 * 绑定本次传输上下文或失败响应，保留独立请求状态。
	 *
	 * @param context  用于访问应用文件及资产的上下文。
	 * @param session  本次持有引用的会话。
	 * @param settings 已复制的默认配置。
	 * @param request  本次请求配置或单项优先值。
	 * @param control  共享的取消和提交状态。
	 * @param id       本次请求或批次的实际唯一标识。
	 */
	HttpEngine(Context context, SessionState session, ClientSettings settings, XH2请求 request, TransferControl control,
			String id) {
		this.context = context;
		this.session = session;
		this.settings = settings;
		this.request = request;
		this.control = control;
		this.id = id;
	}

	/**
	 * defer 保证文件、缓存和证书处理在订阅线程执行；每次重试仍由 Retrofit 克隆真实调用。
	 *
	 * @return 独立的结果值或快照，语义与方法说明一致。
	 */
	Observable<XH2响应> execute() {
		return Observable.defer(new Callable<ObservableSource<XH2响应>>() {
			@Override
			public ObservableSource<XH2响应> call() throws Exception {
				control.check();
				prepare();
				Observable<WireReply> remote = remote();
				RxCache memory;
				try {
					memory = cacheMode < 2 ? new RxCache.Builder().isDiskCache(false).memoryMaxSize(1).build()
							: session.cache.view(cacheKey, cacheSeconds, cacheMode == XH2缓存策略.仅缓存);
				} catch (Exception failure) {
					throw new TransferFailure("缓存", "读取响应缓存失败。", failure);
				}
				return strategy(cacheMode).execute(memory, cacheKey, -1, remote, WireReply.class)
						.map(new Function<CacheResult<WireReply>, XH2响应>() {
							@Override
							public XH2响应 apply(CacheResult<WireReply> result) throws Exception {
								control.check();
								return result.data.response(id, cacheKey, SystemClock.elapsedRealtime() - started,
										result.isFromCache || result.data.httpCache);
							}
						});
			}
		});
	}

	/**
	 * 解析地址和字段，校验请求体约束并生成缓存身份。
	 *
	 * @throws Exception 当前读取、校验或传输无法完成。
	 */
	private void prepare() throws Exception {
		HttpUrl base = settings.baseUrl.isEmpty() ? null : HttpUrl.parse(settings.baseUrl);
		url = base == null ? HttpUrl.parse(request.address) : base.resolve(request.address);
		if (url == null || (!url.isHttps() && !url.scheme().equals("http"))) {
			throw new IllegalArgumentException("请求地址不是有效的 HTTP 或 HTTPS 地址。");
		}
		HttpUrl.Builder builder = url.newBuilder();
		List<String[]> query = new ArrayList<String[]>();
		HttpSupport.copy(settings.query, query);
		HttpSupport.copy(request.query, query);
		for (String[] pair : query) {
			builder.addQueryParameter(pair[0], pair[1]);
		}
		url = builder.build();
		cacheMode = request.cacheMode < 0 ? settings.cacheMode : request.cacheMode;
		cacheSeconds = request.cacheSeconds == -2 ? settings.cacheSeconds : request.cacheSeconds;
		if (!request.download.isEmpty() && (cacheMode != 0 || request.parser != 0)) {
			throw new IllegalArgumentException("文件下载只支持原始响应和不缓存模式。");
		}
		if (!request.files.isEmpty() && cacheMode != 0) {
			throw new IllegalArgumentException("文件上传不使用响应缓存。");
		}
		if ((request.method.equals("GET") || request.method.equals("HEAD"))
				&& (request.body != null || !request.form.isEmpty() || !request.files.isEmpty())) {
			throw new IllegalArgumentException("GET 和 HEAD 不支持请求体。");
		}
		StringBuilder material = new StringBuilder(request.method).append('\n').append(url).append('\n');
		appendKey(material.append("defaults\n"), settings.headers);
		appendKey(material.append("headers\n"), request.headers);
		appendKey(material.append("form\n"), request.form);
		material.append(session.cookies.loadForRequest(url)).append('\n').append(request.mediaType).append('\n')
				.append(request.cacheKey).append('\n')
				.append(request.body == null ? "" : HttpSupport.digest(request.body)).append('\n')
				.append(settings.proxyHost).append(':').append(settings.proxyPort).append('\n')
				.append(settings.socksProxy).append('\n').append(settings.proxyUser).append('\n')
				.append(settings.proxyPassword).append('\n').append(settings.includeSystem).append('\n')
				.append(HttpSupport.digest(settings.clientCertificate));
		for (byte[] authority : settings.authorities) {
			material.append('\n').append(HttpSupport.digest(authority));
		}
		cacheKey = HttpSupport.digest(material.toString().getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * 按长度和原始顺序编码字段，避免分隔符造成缓存键冲突。
	 *
	 * @param result 接收字段编码的缓冲区。
	 * @param pairs  按顺序保存的字段对。
	 */
	private static void appendKey(StringBuilder result, List<String[]> pairs) {
		for (String[] pair : pairs) {
			// 长度前缀避免任意字段中的换行或分隔符使两个请求产生相同序列。
			result.append(pair[0].length()).append(':').append(pair[0]).append(pair[1].length()).append(':')
					.append(pair[1]).append('\n');
		}
	}

	/**
	 * 构建独立客户端并执行带重试的远程请求。
	 *
	 * @return 构建独立客户端并执行带重试的远程请求。
	 * @throws Exception 当前读取、校验或传输无法完成。
	 */
	private Observable<WireReply> remote() throws Exception {
		OkHttpClient.Builder builder = session.baseClient.newBuilder();
		int timeout = request.timeout < 0 ? settings.timeout : request.timeout;
		int connect = timeout(request.connectTimeout, settings.connectTimeout, timeout);
		int read = timeout(request.readTimeout, settings.readTimeout, timeout);
		int write = timeout(request.writeTimeout, settings.writeTimeout, timeout);
		int total = request.callTimeout < 0 ? settings.callTimeout : request.callTimeout;
		builder.connectTimeout(connect, TimeUnit.MILLISECONDS).readTimeout(read, TimeUnit.MILLISECONDS)
				.writeTimeout(write, TimeUnit.MILLISECONDS).callTimeout(total, TimeUnit.MILLISECONDS)
				.followRedirects(settings.redirects).followSslRedirects(settings.redirects);
		boolean credentials = !HttpSupport.first(settings.headers, "Authorization").isEmpty()
				|| !HttpSupport.first(request.headers, "Authorization").isEmpty()
				|| !HttpSupport.first(settings.headers, "Cookie").isEmpty()
				|| !HttpSupport.first(request.headers, "Cookie").isEmpty()
				|| !session.cookies.loadForRequest(url).isEmpty();
		// 显式身份请求走带身份键的应用缓存；HTTP 默认缓存不跨凭据复用未声明 Vary 的响应。
		if (cacheMode != 1 || credentials) {
			builder.cache(null);
		}
		try {
			StrictTls.apply(builder, settings.authorities, settings.includeSystem, settings.clientCertificate,
					settings.certificatePassword);
		} catch (Exception failure) {
			throw new TransferFailure("证书", "初始化证书配置失败。", failure);
		}
		if (!settings.proxyHost.isEmpty()) {
			builder.proxy(new Proxy(settings.socksProxy ? Proxy.Type.SOCKS : Proxy.Type.HTTP,
					new InetSocketAddress(settings.proxyHost, settings.proxyPort)));
			if (!settings.socksProxy && !settings.proxyUser.isEmpty()) {
				builder.proxyAuthenticator(new Authenticator() {
					@Override
					public Request authenticate(Route route, okhttp3.Response response) {
						if (response.request().header("Proxy-Authorization") != null) {
							return null;
						}
						return response.request().newBuilder().header("Proxy-Authorization",
								Credentials.basic(settings.proxyUser, settings.proxyPassword)).build();
					}
				});
			}
		} else {
			builder.proxy(Proxy.NO_PROXY);
		}
		builder.addInterceptor(new Interceptor() {
			@Override
			public okhttp3.Response intercept(Chain chain) throws IOException {
				control.check();
				Request.Builder outgoing = chain.request().newBuilder();
				for (String[] pair : settings.headers) {
					outgoing.addHeader(pair[0], pair[1]);
				}
				// 单次字段覆盖同名默认字段，单次内部重复值仍保持原顺序。
				for (String[] pair : request.headers) {
					outgoing.removeHeader(pair[0]);
				}
				for (String[] pair : request.headers) {
					outgoing.addHeader(pair[0], pair[1]);
				}
				return chain.proceed(outgoing.build());
			}
		});
		final ScopedRequest scoped = new ScopedRequest(builder.build(),
				request.retries < 0 ? settings.retries : request.retries,
				request.retryDelay < 0 ? settings.retryDelay : request.retryDelay,
				request.retryIncrease < 0 ? settings.retryIncrease : request.retryIncrease);
		HttpService service = scoped.create(HttpService.class);
		Observable<WireReply> source;
		if (request.method.equals("HEAD")) {
			source = service.head(url.toString()).map(new Function<Response<Void>, WireReply>() {
				@Override
				public WireReply apply(Response<Void> response) throws Exception {
					return read(response, null);
				}
			});
		} else {
			source = call(service, body()).map(new Function<Response<ResponseBody>, WireReply>() {
				@Override
				public WireReply apply(Response<ResponseBody> response) throws Exception {
					return read(response, response.body());
				}
			});
		}
		final AtomicReference<Throwable> original = new AtomicReference<Throwable>();
		// XHttp2 的通用错误转换会缩减部分异常详情；重试结束后恢复最后一次原始原因和 HTTP 响应。
		return scoped.call(source.onErrorResumeNext(new Function<Throwable, ObservableSource<? extends WireReply>>() {
			@Override
			public ObservableSource<? extends WireReply> apply(Throwable failure) {
				original.set(failure);
				if (HttpSupport.timedOut(failure) && !(failure instanceof SocketTimeoutException)) {
					SocketTimeoutException normalized = new SocketTimeoutException(failure.getMessage());
					normalized.initCause(failure);
					return Observable.error(normalized);
				}
				return Observable.error(failure);
			}
		})).onErrorResumeNext(new Function<Throwable, ObservableSource<? extends WireReply>>() {
			@Override
			public ObservableSource<? extends WireReply> apply(Throwable failure) {
				return Observable.error(original.get() == null ? failure : original.get());
			}
		}).doOnNext(new Consumer<WireReply>() {
			@Override
			public void accept(WireReply value) throws Exception {
				control.check();
				if (cacheMode >= 2) {
					try {
						session.cache.save(cacheKey, value);
					} catch (IOException failure) {
						throw new TransferFailure("缓存", "保存响应缓存失败。", failure);
					}
				}
			}
		});
	}

	/**
	 * 构建普通、表单或多文件请求体并包装上传进度。
	 *
	 * @return 构建普通、表单或多文件请求体并包装上传进度。
	 */
	private RequestBody body() {
		RequestBody result;
		if (request.body != null) {
			if (!request.form.isEmpty() || !request.files.isEmpty()) {
				throw new IllegalArgumentException("原始请求体与表单或文件不能混用。");
			}
			result = RequestBody.create(HttpSupport.media(request.mediaType), request.body);
		} else if (!request.files.isEmpty()) {
			MultipartBody.Builder multipart = new MultipartBody.Builder().setType(MultipartBody.FORM);
			for (String[] pair : request.form) {
				multipart.addFormDataPart(pair[0], pair[1]);
			}
			for (UploadPart part : request.files) {
				multipart.addFormDataPart(part.name, part.filename, part.body(context, control));
			}
			result = multipart.build();
		} else if (!request.form.isEmpty()) {
			FormBody.Builder form = new FormBody.Builder();
			for (String[] pair : request.form) {
				form.add(pair[0], pair[1]);
			}
			result = form.build();
		} else {
			result = RequestBody.create(null, new byte[0]);
		}
		return new UploadProgressRequestBody(result, new IProgressResponseCallBack() {
			@Override
			public void onResponseProgress(long transferred, long total, boolean done) {
				control.progress.report(true, transferred, total);
			}
		});
	}

	/**
	 * 依次选取单次、客户端或统一超时。
	 *
	 * @param request  本次请求配置或单项优先值。
	 * @param client   客户端配置或次级默认值。
	 * @param fallback 没有单项配置时采用的值。
	 * @return 依次选取单次、客户端或统一超时。
	 */
	private static int timeout(int request, int client, int fallback) {
		return request >= 0 ? request : client >= 0 ? client : fallback;
	}

	/**
	 * 按 HTTP 方法选择相应 Retrofit 调用。
	 *
	 * @param service 当前独立的 Retrofit 接口。
	 * @param body    响应体或已构建的请求体。
	 * @return 按 HTTP 方法选择相应 Retrofit 调用。
	 */
	private Observable<Response<ResponseBody>> call(HttpService service, RequestBody body) {
		switch (request.method) {
		case "GET":
			return service.get(url.toString());
		case "POST":
			return service.post(url.toString(), body);
		case "PUT":
			return service.put(url.toString(), body);
		case "PATCH":
			return service.patch(url.toString(), body);
		case "DELETE":
			return service.delete(url.toString(), body);
		case "OPTIONS":
			return service.options(url.toString(), body);
		default:
			throw new IllegalArgumentException("HTTP 方法无效。");
		}
	}

	/**
	 * 读取响应元数据和内容，保留 HTTP 失败及本地处理错误。
	 *
	 * @param response   保留状态码及响应头的原始响应。
	 * @param successful 成功状态下的响应体，可为空。
	 * @return 读取响应元数据和内容，保留 HTTP 失败及本地处理错误。
	 * @throws Exception 当前读取、校验或传输无法完成。
	 */
	private WireReply read(Response<?> response, ResponseBody successful) throws Exception {
		control.check();
		WireReply result = new WireReply();
		result.status = response.code();
		result.reason = response.message();
		result.contentType = HttpSupport.text(response.headers().get("Content-Type"));
		result.url = response.raw().request().url().toString();
		result.httpCache = response.raw().cacheResponse() != null && response.raw().networkResponse() == null;
		Headers headers = response.headers();
		for (int index = 0; index < headers.size(); index++) {
			result.headers.add(new String[] { headers.name(index), headers.value(index) });
		}
		try (ResponseBody body = successful == null ? response.errorBody() : successful) {
			if (body != null) {
				result.contentType = body.contentType() == null ? "" : body.contentType().toString();
				if (response.isSuccessful() && !request.download.isEmpty()) {
					try {
						download(body, result);
					} catch (InterruptedIOException | SocketException | ProtocolException | EOFException
							| SSLException transport) {
						throw transport;
					} catch (IOException failure) {
						throw new TransferFailure("文件", "处理下载文件失败。", failure);
					}
				} else if (!request.method.equals("HEAD")) {
					try (InputStream input = body.byteStream()) {
						byte[] bytes = HttpSupport.read(input, settings.maxBytes, control, body.contentLength());
						result.length = bytes.length;
						result.sha256 = HttpSupport.digest(bytes);
						result.encoded = Base64.encodeToString(bytes, Base64.NO_WRAP);
					}
				}
			}
		} catch (HttpSupport.LimitExceeded limit) {
			TransferFailure failure = new TransferFailure("响应", limit.getMessage(), limit);
			failure.reply = result;
			failure.key = cacheKey;
			throw failure;
		} catch (TransferFailure failure) {
			failure.reply = result;
			failure.key = cacheKey;
			throw failure;
		}
		if (!response.isSuccessful()) {
			throw new HttpFailure(result, cacheKey);
		}
		return result;
	}

	/**
	 * 流式写入同目录临时文件，核对长度和摘要后原子提交。
	 *
	 * @param body  响应体或已构建的请求体。
	 * @param reply 已读取的原始响应快照。
	 * @throws Exception 当前读取、校验或传输无法完成。
	 */
	private void download(ResponseBody body, WireReply reply) throws Exception {
		File target = new File(request.download);
		if (request.download.contains("://")) {
			throw new IOException("下载目标须为文件路径。");
		}
		if (!target.isAbsolute()) {
			File base = context.getFilesDir().getCanonicalFile();
			target = new File(base, request.download).getCanonicalFile();
			if (!target.getPath().startsWith(base.getPath() + File.separator)) {
				throw new IOException("相对下载路径不能越出应用私有目录。");
			}
		}
		target = target.getCanonicalFile();
		String identity = target.getPath();
		Object owner = new Object();
		if (DOWNLOADS.putIfAbsent(identity, owner) != null) {
			throw new IOException("另一个任务正在写入相同下载目标。");
		}
		File temporary = null;
		try {
			File parent = target.getParentFile();
			if (parent == null || (!parent.isDirectory() && !parent.mkdirs())) {
				throw new IOException("无法创建下载目录。");
			}
			temporary = File.createTempFile(".xhttp-", ".part", parent);
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			long total = body.contentLength();
			long size = 0;
			long lastProgress = 0;
			try (InputStream input = body.byteStream(); FileOutputStream output = new FileOutputStream(temporary)) {
				byte[] buffer = new byte[32768];
				int count;
				while ((count = input.read(buffer)) != -1) {
					control.check();
					output.write(buffer, 0, count);
					digest.update(buffer, 0, count);
					size += count;
					long now = SystemClock.elapsedRealtime();
					if (now - lastProgress >= 100) {
						control.progress.report(false, size, total);
						lastProgress = now;
					}
				}
				output.getFD().sync();
			}
			if (total >= 0 && size != total) {
				throw new IOException("下载字节数与响应长度不一致。");
			}
			String actual = HttpSupport.hex(digest.digest());
			if (!request.sha256.isEmpty() && !request.sha256.equals(actual)) {
				throw new IOException("下载 SHA-256 校验失败。");
			}
			control.commit(temporary, target);
			reply.file = target.getAbsolutePath();
			reply.sha256 = actual;
			reply.length = size;
			control.progress.report(false, size, total);
		} finally {
			try {
				if (temporary != null && temporary.exists() && !temporary.delete()) {
					throw new IOException("无法清理本次下载临时文件：" + temporary);
				}
			} finally {
				DOWNLOADS.remove(identity, owner);
			}
		}
	}

	/**
	 * 将导出的整数策略映射为上游缓存策略。
	 *
	 * @param mode 导出缓存策略对应的整数。
	 * @return 将导出的整数策略映射为上游缓存策略。
	 */
	private static IStrategy strategy(int mode) {
		switch (mode) {
		case 0:
		case 1:
			return new NoStrategy();
		case 2:
			return new FirstRemoteStrategy();
		case 3:
			return new FirstCacheStategy();
		case 4:
			return new OnlyRemoteStrategy();
		case 5:
			return new OnlyCacheStrategy();
		case 6:
			return new CacheAndRemoteStrategy();
		case 7:
			return new CacheAndRemoteDistinctStrategy();
		default:
			throw new IllegalArgumentException("缓存策略无效。");
		}
	}

	/**
	 * 保留非成功 HTTP 的完整内容，供客户端生成可检查的失败响应，而非丢失为一段异常文本。
	 *
	 * @author 树先生 xhwsd@qq.com
	 */
	static final class HttpFailure extends IOException {
		private static final long serialVersionUID = 1L;
		final WireReply reply;
		final String key;

		/**
		 * 绑定本次传输上下文或失败响应，保留独立请求状态。
		 *
		 * @param reply 已读取的原始响应快照。
		 * @param key   来自响应的实际缓存键或内部会话键。
		 */
		HttpFailure(WireReply reply, String key) {
			super("HTTP " + reply.status + " " + reply.reason);
			this.reply = reply;
			this.key = key;
		}
	}
}
