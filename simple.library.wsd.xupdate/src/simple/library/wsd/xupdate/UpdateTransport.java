package simple.library.wsd.xupdate;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.concurrent.ExecutorService;

import android.os.Handler;

import org.json.JSONObject;

import com.xuexiang.xupdate.proxy.IUpdateHttpService;

/**
 * 提供不依赖第三方网络库的版本请求和流式 APK 下载。
 * 每次任务使用独立取消句柄，后台线程不得直接触发 Simple 事件。
 *
 * @author 树先生 xhwsd@qq.com
 */
final class UpdateTransport implements IUpdateHttpService {

	private final Handler main;
	private final ExecutorService executor;
	private final Task task;
	private final Map<String, String> headers;
	private final int timeout;
	private final boolean postJson;

	/**
	 * 固定一次请求的取消句柄与配置，不读写上游全局 HTTP 客户端。
	 *
	 * @param main 界面线程处理器。
	 * @param executor 实例后台执行器。
	 * @param task 本次请求取消句柄。
	 * @param headers 独立请求头快照。
	 * @param timeout 请求超时毫秒数。
	 * @param postJson POST 是否使用 JSON。
	 */
	UpdateTransport(Handler main, ExecutorService executor, Task task, Map<String, String> headers,
			int timeout, boolean postJson) {
		this.main = main;
		this.executor = executor;
		this.task = task;
		this.headers = new LinkedHashMap<String, String>(headers);
		this.timeout = timeout;
		this.postJson = postJson;
	}

	@Override
	public void asyncGet(String url, Map<String, Object> params, Callback callback) {
		send(url, params, false, callback);
	}

	@Override
	public void asyncPost(String url, Map<String, Object> params, Callback callback) {
		send(url, params, true, callback);
	}

	/**
	 * 将 EasyUpdate 的请求协议接到实例后台任务，回调统一切回界面线程。
	 *
	 * @param url 请求地址。
	 * @param params 参数快照。
	 * @param post 是否使用 POST。
	 * @param callback 上游请求回调。
	 */
	private void send(final String url, Map<String, Object> params, final boolean post, final Callback callback) {
		final Map<String, String> values = new LinkedHashMap<String, String>();
		for (Map.Entry<String, Object> entry : params.entrySet()) {
			values.put(entry.getKey(), String.valueOf(entry.getValue()));
		}
		executor.execute(new Runnable() {
			@Override
			public void run() {
				try {
					final String result = request(task, url, post, values, headers, timeout, postJson);
					main.post(new Runnable() {
						@Override
						public void run() {
							callback.onSuccess(result);
						}
					});
				} catch (final Exception exception) {
					main.post(new Runnable() {
						@Override
						public void run() {
							callback.onError(exception);
						}
					});
				}
			}
		});
	}

	@Override
	public void download(final String url, final String path, final String fileName, final DownloadCallback callback) {
		callback.onStart();
		executor.execute(new Runnable() {
			@Override
			public void run() {
				try {
					更新信息 info = new 更新信息();
					info.下载地址(url);
					File directory = new File(path);
					if (!directory.isDirectory() && !directory.mkdirs()) {
						throw new IOException("无法创建下载目录。");
					}
					final File result = UpdateTransport.download(task, info, new File(directory, fileName),
							headers, timeout, new Progress() {
						@Override
						public void received(final long received, final long total) {
							main.post(new Runnable() {
								@Override
								public void run() {
									callback.onProgress(total > 0 ? (float) received / total : 0, total);
								}
							});
						}
					});
					main.post(new Runnable() {
						@Override
						public void run() {
							callback.onSuccess(result);
						}
					});
				} catch (final Exception exception) {
					main.post(new Runnable() {
						@Override
						public void run() {
							callback.onError(exception);
						}
					});
				}
			}
		});
	}

	@Override
	public void cancelDownload(String url) {
		task.cancel();
	}

	/**
	 * 将取消状态与当前连接绑定，处理取消发生在连接创建前后的竞态。
	 *
	 * @author 树先生 xhwsd@qq.com
	 */
	static final class Task {

		private volatile boolean cancelled;
		private HttpURLConnection connection;

		/**
		 * 创建尚未取消的网络任务。
		 */
		Task() {
		}

		/**
		 * 登记当前连接；取消先于登记时立即中断。
		 *
		 * @param value 即将使用的连接。
		 * @throws IOException 任务已经取消。
		 */
		synchronized void attach(HttpURLConnection value) throws IOException {
			check();
			connection = value;
		}

		/**
		 * 中断当前连接并拒绝后续传输。
		 */
		synchronized void cancel() {
			cancelled = true;
			if (connection != null) {
				connection.disconnect();
			}
		}

		/**
		 * 检查任务是否仍允许继续。
		 *
		 * @throws IOException 已经取消时中断传输。
		 */
		void check() throws IOException {
			if (cancelled || Thread.currentThread().isInterrupted()) {
				throw new IOException("更新任务已取消。");
			}
		}
	}

	/**
	 * 接收后台下载进度，由调用方负责切换线程和过滤旧任务。
	 *
	 * @author 树先生 xhwsd@qq.com
	 */
	interface Progress {

		/**
		 * 接收已经写入临时文件的数据量；此时尚未完成摘要校验。
		 *
		 * @param received 已下载字节数。
		 * @param total 预期字节数，未知为零。
		 */
		void received(long received, long total);
	}

	/**
	 * 校验网络地址，拒绝非 HTTP(S) 协议及缺失主机名的地址。
	 *
	 * @param address 外部提供的地址。
	 * @return 已解析的绝对地址。
	 * @throws IOException 地址格式或协议无效。
	 */
	static URL url(String address) throws IOException {
		URL url = new URL(address);
		if ((!"http".equals(url.getProtocol()) && !"https".equals(url.getProtocol()))
				|| url.getHost().isEmpty() || url.getUserInfo() != null) {
			throw new IOException("地址必须为不含用户凭据的 HTTP 或 HTTPS 地址。");
		}
		return url;
	}

	/**
	 * 发出有限大小的 UTF-8 版本检查请求。
	 *
	 * @param task 本次取消句柄。
	 * @param address 完整版本接口地址。
	 * @param post 是否发送表单 POST。
	 * @param parameters 本次请求参数快照。
	 * @param headers 本次请求头快照。
	 * @param timeout 连接和读取超时毫秒数。
	 * @param postJson POST 是否使用 JSON 对象。
	 * @return 完整响应文本，最多 2 MiB。
	 * @throws IOException 请求失败、取消或响应过大。
	 */
	static String request(Task task, String address, boolean post, Map<String, String> parameters,
			Map<String, String> headers, int timeout, boolean postJson) throws IOException {
		StringBuilder form = new StringBuilder();
		for (Map.Entry<String, String> entry : parameters.entrySet()) {
			if (form.length() > 0) {
				form.append('&');
			}
			form.append(URLEncoder.encode(entry.getKey(), "UTF-8")).append('=')
					.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
		}
		URL target = url(address);
		if (!post && form.length() > 0) {
			String separator = target.getQuery() == null ? "?" : "&";
			String base = address.split("#", 2)[0];
			target = url(base + separator + form);
		}

		HttpURLConnection connection = open(task, target, headers, timeout);
		try {
			if (post) {
				byte[] body = (postJson ? new JSONObject(parameters).toString() : form.toString())
						.getBytes(StandardCharsets.UTF_8);
				connection.setRequestMethod("POST");
				connection.setDoOutput(true);
				connection.setRequestProperty("Content-Type", postJson ? "application/json; charset=UTF-8"
						: "application/x-www-form-urlencoded; charset=UTF-8");
				connection.setFixedLengthStreamingMode(body.length);
				try (OutputStream output = connection.getOutputStream()) {
					output.write(body);
				}
			}
			checkResponse(connection);
			try (InputStream input = connection.getInputStream();
					ByteArrayOutputStream output = new ByteArrayOutputStream()) {
				byte[] buffer = new byte[8192];
				int length;
				while ((length = input.read(buffer)) != -1) {
					task.check();
					if (output.size() + length > 2 * 1024 * 1024) {
						throw new IOException("版本响应超过 2 MiB。");
					}
					output.write(buffer, 0, length);
				}
				return new String(output.toByteArray(), StandardCharsets.UTF_8);
			}
		} finally {
			connection.disconnect();
		}
	}

	/**
	 * 下载至临时文件，完整性校验通过后原子改名为可安装文件。
	 *
	 * @param task 本次取消句柄。
	 * @param info 不再修改的版本快照。
	 * @param target 最终 APK 文件，每次任务使用唯一名称。
	 * @param headers 本次下载请求头。
	 * @param timeout 连接和连续读取超时毫秒数。
	 * @param progress 后台进度接收器。
	 * @return 已完成校验的文件。
	 * @throws IOException 下载、大小校验或 MD5 校验失败。
	 */
	static File download(Task task, 更新信息 info, File target, Map<String, String> headers,
			int timeout, Progress progress) throws IOException {
		File temporary = new File(target.getPath() + ".part");
		HttpURLConnection connection = open(task, url(info.下载地址()), headers, timeout);
		try {
			checkResponse(connection);
			long contentLength = connection.getContentLengthLong();
			long expected = info.文件大小();
			if (expected > 0 && contentLength > 0 && expected != contentLength) {
				throw new IOException("APK 长度与版本信息不符。");
			}
			long total = expected > 0 ? expected : Math.max(contentLength, 0);
			MessageDigest digest = md5();
			long received = 0;
			long lastProgress = 0;
			try (InputStream input = connection.getInputStream();
					FileOutputStream output = new FileOutputStream(temporary)) {
				byte[] buffer = new byte[32768];
				int length;
				while ((length = input.read(buffer)) != -1) {
					task.check();
					output.write(buffer, 0, length);
					digest.update(buffer, 0, length);
					received += length;
					if (total > 0 && received > total) {
						throw new IOException("APK 超出声明大小。");
					}
					long now = System.nanoTime();
					if (now - lastProgress >= 100000000L) {
						progress.received(received, total);
						lastProgress = now;
					}
				}
				output.getFD().sync();
			}

			// 进度到达末尾不等于有效 APK，校验完成前不公开最终文件。
			task.check();
			if (received == 0 || (total > 0 && received != total)) {
				throw new IOException("APK 未完整下载。");
			}
			if (!info.MD5().isEmpty() && !info.MD5().equalsIgnoreCase(hex(digest.digest()))) {
				throw new IOException("APK 的 MD5 校验失败。");
			}
			if (!temporary.renameTo(target)) {
				throw new IOException("无法保存已校验的 APK。");
			}
			return target;
		} finally {
			connection.disconnect();
			if (temporary.exists() && !temporary.delete()) {
				temporary.deleteOnExit();
			}
		}
	}

	/**
	 * 校验下载文件的实际长度和完整 MD5，不以下载器完成事件代替校验。
	 *
	 * @param task 校验阶段取消句柄。
	 * @param info 更新信息快照。
	 * @param file 已关闭写入的文件。
	 * @throws IOException 文件为空、长度或摘要不符，或已经取消。
	 */
	static void verify(Task task, 更新信息 info, File file) throws IOException {
		task.check();
		if (!file.isFile() || file.length() == 0 || (info.文件大小() > 0 && file.length() != info.文件大小())) {
			throw new IOException("APK 长度与版本信息不符。");
		}
		MessageDigest digest = md5();
		try (InputStream input = new FileInputStream(file)) {
			byte[] buffer = new byte[32768];
			int length;
			while ((length = input.read(buffer)) != -1) {
				task.check();
				digest.update(buffer, 0, length);
			}
		}
		task.check();
		if (!info.MD5().isEmpty() && !info.MD5().equalsIgnoreCase(hex(digest.digest()))) {
			throw new IOException("APK 的 MD5 校验失败。");
		}
	}

	/**
	 * 创建具有一致超时、请求头和取消约束的连接。
	 *
	 * @param task 本次取消句柄。
	 * @param url 已校验的地址。
	 * @param headers 请求头快照。
	 * @param timeout 超时毫秒数。
	 * @return 尚未发出请求的连接。
	 * @throws IOException 连接创建失败或任务已取消。
	 */
	private static HttpURLConnection open(Task task, URL url, Map<String, String> headers,
			int timeout) throws IOException {
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setConnectTimeout(timeout);
		connection.setReadTimeout(timeout);
		connection.setUseCaches(false);
		connection.setRequestProperty("Accept-Encoding", "identity");
		for (Map.Entry<String, String> header : headers.entrySet()) {
			connection.setRequestProperty(header.getKey(), header.getValue());
		}
		task.attach(connection);
		return connection;
	}

	/**
	 * 拒绝非成功 HTTP 响应，不将错误页面当作更新文件。
	 *
	 * @param connection 已发出的连接。
	 * @throws IOException 响应不在 200 至 299 范围内。
	 */
	private static void checkResponse(HttpURLConnection connection) throws IOException {
		int status = connection.getResponseCode();
		if (status < 200 || status >= 300) {
			throw new IOException("HTTP 请求失败：" + status);
		}
	}

	/**
	 * 获取与上游协议一致的摘要算法。
	 *
	 * @return 新的 MD5 摘要实例。
	 * @throws IOException 系统没有提供该摘要算法。
	 */
	private static MessageDigest md5() throws IOException {
		try {
			return MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException exception) {
			throw new IOException("无法创建 MD5 校验器。", exception);
		}
	}

	/**
	 * 将摘要编码为固定长度的小写十六进制文本。
	 *
	 * @param value 摘要字节。
	 * @return 每字节两个字符的文本。
	 */
	private static String hex(byte[] value) {
		StringBuilder result = new StringBuilder(value.length * 2);
		for (byte item : value) {
			result.append(Character.forDigit((item >>> 4) & 15, 16));
			result.append(Character.forDigit(item & 15, 16));
		}
		return result.toString();
	}
}
