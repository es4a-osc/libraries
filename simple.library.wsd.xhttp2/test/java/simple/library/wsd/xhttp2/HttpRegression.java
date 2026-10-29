package simple.library.wsd.xhttp2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.os.SystemClock;
import android.widget.FrameLayout;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import simple.runtime.android.MainActivity;
import simple.runtime.components.组件;
import simple.runtime.components.impl.android.面板Impl;
import simple.runtime.events.EventDispatcher;
import simple.runtime.variants.IntegerVariant;

/**
 * 在正式 Simple 测试进程内调用公开客户端，对本机可控服务验证网络、缓存、证书和传输。
 * 不直接触发完成事件；请求结果必须来自实际服务器或真实缓存。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class HttpRegression extends Instrumentation {

	private static final String BASE = "http://127.0.0.1:18780/";
	private final Events events = new Events();
	private MainActivity activity;
	private 面板Impl container;
	private int assertions;

	@Override
	public void onCreate(Bundle arguments) {
		super.onCreate(arguments);
		start();
	}

	@Override
	public void onStart() {
		Bundle report = new Bundle();
		try {
			activity = (MainActivity) startActivitySync(new Intent().setComponent(new ComponentName(
				"simple.library.wsd.xhttp2.test", "simple.runtime.android.MainActivity")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
			ui(new Callable<Void>() {
				@Override
				public Void call() {
					container = new 面板Impl(null) {
						@Override
						protected void addToContainer() {
							// 验证根容器独立归测试 Activity 管理。
						}
					};
					container.布局(IntegerVariant.getIntegerVariant(组件.布局_线性));
					activity.setContentView(container.getView());
					newClient();
					return null;
				}
			});
			waitForIdleSync();
			verifyRequests();
			verifyParsing();
			verifyCache();
			verifyCookies();
			verifyTls();
			verifyTransfers();
			verifyBatch();
			verifyEdgeCases();
			verifyConcurrencyAndProxy();
			verifyCacheMaintenance();
			verifyUnknownUploads();
			verifyLifecycle();
			report.putString("stream", "XHTTP2_REGRESSION_PASS assertions=" + assertions);
			finish(Activity.RESULT_OK, report);
		} catch (Throwable failure) {
			report.putString("stream", "XHTTP2_REGRESSION_FAIL assertions=" + assertions + "\n" + android.util.Log.getStackTraceString(failure));
			finish(Activity.RESULT_CANCELED, report);
		} finally {
			if (activity != null) {
				runOnMainSync(new Runnable() {
					@Override
					public void run() {
						if (events.client != null) {
							events.client.销毁();
						}
					}
				});
			}
		}
	}

	private void newClient() {
		createClient(events);
	}

	private void createClient(Events receiver) {
		receiver.client = new XHttp客户端Impl(container);
		receiver.client.初始化();
		receiver.client.基础地址(BASE);
		for (String name : new String[] {"请求前处理", "请求开始", "收到响应", "请求成功", "请求失败", "请求取消", "请求结束", "解析响应", "传输进度", "批次完成", "缓存操作完成"}) {
			EventDispatcher.registerEvent(receiver, "client", name);
		}
	}

	private XHttp请求 request(String path) {
		XHttp请求 request = new XHttp请求();
		request.地址(path);
		request.标识(UUID.randomUUID().toString());
		return request;
	}

	private Run start(final XHttp请求 request) throws Exception {
		return start(events, request);
	}

	private Run start(final Events receiver, final XHttp请求 request) throws Exception {
		return ui(new Callable<Run>() {
			@Override
			public Run call() {
				Run run = new Run();
				receiver.running.put(request.标识(), run);
				receiver.client.发送(request);
				return run;
			}
		});
	}

	private Run exchange(XHttp请求 request) throws Exception {
		Run run = start(request);
		check(run.ended.await(20, TimeUnit.SECONDS), "请求在期限内进入终态：" + request.地址());
		check(run.ends == 1, "请求结束仅一次");
		return run;
	}

	private void verifyRequests() throws Exception {
		XHttp请求 query = request("echo");
		query.添加查询参数("city", "杭州 + 上海");
		query.添加查询参数("city", "深圳");
		query.添加请求头("X-Duplicate-Request", "a");
		query.添加请求头("X-Duplicate-Request", "b");
		Run first = exchange(query);
		JsonObject echo = json(first.last());
		check(first.last().成功() && first.successes == 1, "真实 GET 成功");
		check(echo.getAsJsonObject("query").getAsJsonArray("city").get(0).getAsString().equals("杭州 + 上海"), "中文及加号查询编码");
		check(echo.getAsJsonObject("query").getAsJsonArray("city").size() == 2, "保留重复查询参数");
		check(first.last().取全部响应头("X-Duplicate").length == 2, "保留重复响应头");
		check(first.last().取全部响应头("X-Duplicate")[1].equals("two"), "响应头保持顺序");
		for (String method : new String[] {"POST", "PUT", "PATCH", "DELETE", "OPTIONS"}) {
			XHttp请求 input = request("echo");
			input.方法(method);
			input.置文本请求体("中文-body", "text/plain; charset=utf-8");
			JsonObject result = json(exchange(input).last());
			check(result.get("method").getAsString().equals(method), "真实方法：" + method);
			check(result.get("body").getAsString().equals("中文-body"), "文本请求体：" + method);
		}
		XHttp请求 head = request("echo");
		head.方法("HEAD");
		XHttp响应 headReply = exchange(head).last();
		check(headReply.成功() && headReply.字节().length == 0, "HEAD 无响应体仍成功");
		XHttp请求 hook = request("echo");
		hook.标识("hook-" + UUID.randomUUID());
		JsonObject hooked = json(exchange(hook).last());
		check(hooked.get("body").getAsString().isEmpty(), "请求前处理不改变请求体");
		check(header(hooked, "Authorization").equals("Bearer dynamic-token"), "请求前处理令牌真实到达服务器");
		Run bad = exchange(request("status/503"));
		check(bad.last().状态码() == 503 && bad.last().文本().contains("controlled HTTP error"), "HTTP 错误保留真实状态和正文");
		check(bad.failures == 1 && bad.successes == 0, "HTTP 错误事件分类");
		XHttp请求 retry = request("retry?id=" + UUID.randomUUID() + "&fail=2&delay=0.3");
		retry.超时(100);
		retry.重试次数(2);
		retry.重试间隔(5);
		check(exchange(retry).last().成功(), "XHttp2 原生超时重试后成功");
		XHttp请求 increasing = request("retry?id=" + UUID.randomUUID() + "&fail=2&delay=0.25");
		increasing.超时(60);
		increasing.重试次数(2);
		increasing.重试间隔(40);
		increasing.重试递增间隔(160);
		long started = SystemClock.elapsedRealtime();
		check(exchange(increasing).last().成功(), "原生递增间隔重试最终成功");
		check(SystemClock.elapsedRealtime() - started >= 340, "两次失败后的等待包含递增间隔");
		check(increasing.复制().重试递增间隔() == 160, "请求副本保留重试递增间隔");
		XHttp请求 binary = request("echo");
		binary.方法("POST");
		byte[] bytes = new byte[] {0, 1, 127, -1};
		binary.置字节请求体(bytes, "application/octet-stream");
		bytes[0] = 8;
		check(json(exchange(binary).last()).get("base64").getAsString().equals("AAF//w=="), "二进制请求体保留原始字节且隔离来源数组");
		XHttp请求 form = request("echo");
		form.方法("POST");
		form.添加表单参数("city", "杭州 + 上海");
		form.添加表单参数("city", "深圳");
		JsonObject encoded = json(exchange(form).last());
		check(header(encoded, "Content-Type").startsWith("application/x-www-form-urlencoded"), "无文件表单采用表单编码");
		check(encoded.get("body").getAsString().contains("%2B") && encoded.get("body").getAsString().split("city=").length == 3,
			"表单保留重复字段并编码加号");
	}

	private void verifyParsing() throws Exception {
		XHttp请求 normal = request("business");
		normal.解析方式(1);
		XHttp响应 reply = exchange(normal).last();
		check(reply.成功() && reply.数据().contains("中文"), "标准业务解析");
		normal.地址("business?code=7");
		normal.标识(UUID.randomUUID().toString());
		check(exchange(normal).last().错误类型().equals("业务"), "业务失败独立于 HTTP 成功");
		XHttp请求 malformed = request("bad-json");
		malformed.解析方式(1);
		check(exchange(malformed).last().错误类型().equals("解析"), "损坏 JSON 返回解析错误");
		XHttp请求 custom = request("business");
		custom.解析方式(2);
		check(exchange(custom).last().数据().equals("已适配"), "自定义解析事件回填");
		XHttp响应 synchronous = events.client.同步发送(request("echo"));
		check(synchronous != null && synchronous.成功(), "工作线程同步发送真实请求");
	}

	private void verifyCache() throws Exception {
		XHttp请求 fill = request("stable?id=" + UUID.randomUUID());
		fill.缓存策略(XHttp客户端.缓存_仅网络);
		XHttp响应 remote = exchange(fill).last();
		check(remote.成功() && !remote.来自缓存(), "网络响应写入应用缓存");
		fill.缓存策略(XHttp客户端.缓存_仅缓存);
		fill.标识(UUID.randomUUID().toString());
		XHttp响应 cached = exchange(fill).last();
		check(cached.成功() && cached.来自缓存(), "只读缓存不请求网络");
		check(cached.文本().equals(remote.文本()) && cached.缓存键().equals(remote.缓存键()), "请求标识不污染缓存身份");
		fill.缓存策略(XHttp客户端.缓存_缓存及网络);
		fill.标识(UUID.randomUUID().toString());
		check(exchange(fill).replies.size() == 2, "缓存及网络真实双发");
		fill.缓存策略(XHttp客户端.缓存_缓存及网络去重);
		fill.标识(UUID.randomUUID().toString());
		check(exchange(fill).replies.size() == 1, "上游缓存及网络去重");
		fill.缓存策略(XHttp客户端.缓存_仅缓存);
		fill.标识(UUID.randomUUID().toString());
		fill.置请求头("Authorization", "Bearer another-user");
		check(!exchange(fill).last().成功(), "凭据变化不能命中旧缓存");
		XHttp请求 http = request("http-cache?id=" + UUID.randomUUID());
		http.缓存策略(XHttp客户端.缓存_HTTP);
		check(exchange(http).last().成功(), "HTTP 缓存首次网络请求");
		http.标识(UUID.randomUUID().toString());
		check(exchange(http).last().来自缓存(), "HTTP 缓存头驱动复用");
	}

	private void verifyCookies() throws Exception {
		ui(new Callable<Void>() {
			@Override
			public Void call() {
				events.client.设置Cookie(BASE, "same=root; Path=/; Max-Age=60");
				events.client.设置Cookie(BASE + "a/", "same=child; Path=/a; Max-Age=60");
				events.client.设置Cookie(BASE, "secure=secret; Secure; Path=/; Max-Age=60");
				return null;
			}
		});
		JsonObject a = json(exchange(request("a/echo")).last());
		String cookies = header(a, "Cookie");
		check(cookies.contains("same=child") && cookies.contains("same=root"), "同名 Cookie 按路径分别保留");
		check(cookies.indexOf("child") < cookies.indexOf("root"), "更长 Cookie 路径优先");
		check(!cookies.contains("secret"), "HTTP 不发送 Secure Cookie");
		check(!header(json(exchange(request("b/echo")).last()), "Cookie").contains("child"), "Cookie 路径约束");
		ui(new Callable<Void>() {
			@Override
			public Void call() {
				check(events.client.删除Cookie("same", "127.0.0.1", "/a"), "按域与路径删除指定 Cookie");
				check(!events.client.删除Cookie("same", "127.0.0.1", "/a"), "重复删除 Cookie 返回假");
				events.client.设置Cookie(BASE, "same=expired; Path=/; Max-Age=0");
				check(events.client.取Cookie(BASE).length == 0, "过期 Cookie 删除原值且 Secure Cookie 不匹配 HTTP");
				boolean wrongDomain = false;
				try {
					events.client.设置Cookie(BASE, "foreign=value; Domain=example.org; Path=/");
				} catch (IllegalArgumentException expected) {
					wrongDomain = true;
				}
				check(wrongDomain, "不接受其它域的 Cookie");
				return null;
			}
		});
		XHttp请求 setCookie = request("echo");
		setCookie.添加查询参数("set", "server=real; Path=/; Max-Age=60");
		check(exchange(setCookie).last().成功(), "服务端 Set-Cookie 请求成功");
		check(header(json(exchange(request("echo")).last()), "Cookie").contains("server=real"), "自动接收的服务端 Cookie 用于后续请求");
		ui(new Callable<Void>() {
			@Override
			public Void call() {
				events.client.清空Cookie();
				return null;
			}
		});
	}

	private void verifyTls() throws Exception {
		XHttp请求 untrusted = request("https://localhost:18743/echo");
		XHttp响应 rejected = exchange(untrusted).last();
		check(!rejected.成功() && rejected.错误类型().equals("证书"), "默认拒绝未信任测试 CA");
		ui(new Callable<Void>() {
			@Override
			public Void call() {
				events.client.置信任证书(new String[] {"asset://xhttp2-test-ca.pem"}, false);
				return null;
			}
		});
		check(exchange(request("https://localhost:18743/echo")).last().成功(), "显式 CA 通过正确主机名认证");
		check(!exchange(request("https://127.0.0.1:18743/echo")).last().成功(), "自定义 CA 仍拒绝错误主机名");
		check(!exchange(request("https://localhost:18744/echo")).last().成功(), "双向认证缺少客户端证书被拒绝");
		ui(new Callable<Void>() {
			@Override
			public Void call() {
				events.client.置客户端证书("asset://xhttp2-test-client.p12", "es4a-test");
				return null;
			}
		});
		check(exchange(request("https://localhost:18744/echo")).last().成功(), "PKCS12 双向认证成功");
		ui(new Callable<Void>() {
			@Override
			public Void call() {
				events.client.置信任证书(new String[0], true);
				events.client.置客户端证书("", "");
				return null;
			}
		});
	}

	private void verifyTransfers() throws Exception {
		File input = new File(activity.getCacheDir(), "xhttp2-upload.txt");
		byte[] content = "真实中文文件".getBytes(StandardCharsets.UTF_8);
		try (FileOutputStream output = new FileOutputStream(input)) {
			output.write(content);
		}
		XHttp请求 upload = request("echo");
		upload.方法("POST");
		upload.添加表单参数("label", "中文字段");
		upload.添加文件("files", input.getAbsolutePath(), "data.txt", "text/plain");
		upload.添加字节文件("files", new byte[] {0, 1, 2, -1}, "bytes.bin", "application/octet-stream");
		Run sent = exchange(upload);
		String multipart = json(sent.last()).get("body").getAsString();
		check(multipart.contains("filename=\"data.txt\"") && multipart.contains("filename=\"bytes.bin\""), "多文件上传保留文件名");
		check(multipart.contains("真实中文文件") && multipart.contains("中文字段"), "文件及表单真实到达服务端");
		check(sent.progress > 0, "上游上传进度真实触发");
		File target = new File(activity.getCacheDir(), "xhttp2-download.bin");
		try (FileOutputStream output = new FileOutputStream(target)) {
			output.write("OLD".getBytes(StandardCharsets.UTF_8));
		}
		XHttp请求 bad = request("file?size=131072");
		bad.下载路径(target.getAbsolutePath());
		bad.预期SHA256("0000000000000000000000000000000000000000000000000000000000000000");
		check(!exchange(bad).last().成功(), "错误摘要拒绝提交下载");
		check(target.length() == 3, "错误摘要保留已有目标");
		XHttp请求 good = request("file?size=131072");
		good.下载路径(target.getAbsolutePath());
		XHttp响应 downloaded = exchange(good).last();
		check(downloaded.成功() && target.length() == 131072, "流式下载提交完整文件");
		check(new File(downloaded.文件()).getCanonicalFile().equals(target.getCanonicalFile()) && downloaded.字节().length == 0,
			"下载返回同一物理文件且不重复保留大响应内存");
		try (FileInputStream actual = new FileInputStream(target)) {
			check(HttpSupport.digest(HttpSupport.read(actual, 262144)).equals(downloaded.取响应头("X-SHA256")),
				"下载实际文件内容与服务端摘要一致");
		}
		XHttp请求 slow = request("file?size=2097152&delay=0.03");
		slow.下载路径(target.getAbsolutePath());
		Run pending = start(slow);
		Thread.sleep(180);
		final String id = slow.标识();
		boolean cancelled = ui(new Callable<Boolean>() {
			@Override
			public Boolean call() {
				return events.client.取消(id);
			}
		});
		check(cancelled && pending.ended.await(3, TimeUnit.SECONDS), "活动下载可取消并进入终态");
		check(target.length() == 131072 && pending.cancels == 1, "取消不覆盖已有目标并仅通知一次");
		long deadline = SystemClock.elapsedRealtime() + 3000;
		while (temporaryDownloads(target.getParentFile()) > 0 && SystemClock.elapsedRealtime() < deadline) {
			Thread.sleep(20);
		}
		check(temporaryDownloads(target.getParentFile()) == 0, "取消清理本次下载临时文件");
		XHttp请求 truncated = request("file-short?size=2048");
		truncated.下载路径(target.getAbsolutePath());
		check(!exchange(truncated).last().成功() && target.length() == 131072, "截断响应不覆盖原下载文件");
		final XHttp请求 primary = request("file?size=1048576&delay=0.02");
		primary.下载路径(target.getAbsolutePath());
		Run downloading = start(primary);
		check(downloading.downloadProgress.await(3, TimeUnit.SECONDS), "冲突验证开始前首个下载实际写入");
		XHttp请求 collision = request("file?size=64");
		collision.下载路径(target.getAbsolutePath());
		XHttp响应 rejected = exchange(collision).last();
		check(!rejected.成功() && rejected.错误类型().equals("文件") && rejected.错误().contains("相同下载目标"),
			"拒绝并发写入同一物理下载目标");
		check(downloading.ended.await(8, TimeUnit.SECONDS) && downloading.last().成功() && target.length() == 1048576,
			"冲突请求不破坏第一个下载的完整提交");
	}

	private static int temporaryDownloads(File directory) {
		File[] values = directory.listFiles();
		int count = 0;
		if (values != null) {
			for (File value : values) {
				if (value.getName().startsWith(".xhttp-") && value.getName().endsWith(".part")) {
					count++;
				}
			}
		}
		return count;
	}

	private void verifyBatch() throws Exception {
		for (final boolean serial : new boolean[] {true, false}) {
			final XHttp请求[] requests = new XHttp请求[] {request("echo?index=0"), request("echo?index=1"), request("echo?index=2")};
			events.batchDone = new CountDownLatch(1);
			ui(new Callable<Void>() {
				@Override
				public Void call() {
					events.client.批量发送(requests, serial, false);
					return null;
				}
			});
			check(events.batchDone.await(10, TimeUnit.SECONDS), "批次进入完成状态");
			check(events.batch.成功数量() == 3, "批次请求全部成功");
			check(events.batch.响应列表()[1].标识().equals(requests[1].标识()), "批次结果保持输入顺序");
		}
		final XHttp请求[] pending = new XHttp请求[] {request("delay?seconds=2"), request("delay?seconds=2")};
		events.batchDone = new CountDownLatch(1);
		ui(new Callable<Void>() {
			@Override
			public Void call() {
				String id = events.client.批量发送(pending, false, false);
				check(events.client.是否运行(id) && events.client.取消(id), "使用批次标识取消在途并行任务");
				check(!events.client.是否运行(id), "批次取消后移除活动标识");
				return null;
			}
		});
		check(events.batchDone.await(3, TimeUnit.SECONDS) && events.batch.已取消(), "取消批次准确标记完成结果");
		check(events.batch.失败数量() == 2 && events.batch.响应列表()[0].错误类型().equals("取消")
			&& events.batch.响应列表()[1].错误类型().equals("取消"), "批次取消保留每个子请求的真实终态");
	}

	/**
	 * 不以请求成功代替配置隔离、真实过期、磁盘重开以及回调重入的行为验证。
	 */
	private void verifyEdgeCases() throws Exception {
		check(XHttp工具.网址解码(XHttp工具.网址编码("杭州 + 上海")).equals("杭州 + 上海"), "URL 编解码往返");
		check(XHttp工具.Base64解码("").length == 0, "空数据 Base64 有效");
		check(XHttp工具.HMAC_SHA256("The quick brown fox jumps over the lazy dog", "key")
			.equals("f7bc83f430538424b13298e6aa6fb143ef4d59a14946175997479dbc2d1a3cd8"), "HMAC-SHA256 标准向量");
		check(XHttp工具.取JSON值("{\"data\":{\"items\":[\"中文\"]}}", "data.items[0]").equals("中文"), "JSON 数组路径");
		boolean invalid = false;
		try {
			XHttp工具.Base64解码("!bad!");
		} catch (IllegalArgumentException expected) {
			invalid = true;
		}
		check(invalid, "非法 Base64 不静默跳过字符");
		XHttp请求 atomic = request("echo");
		atomic.置请求头("X-Atomic", "old");
		try {
			atomic.置请求头("X-Atomic", "invalid\nvalue");
			throw new AssertionError("非法请求头未拒绝");
		} catch (IllegalArgumentException expected) {
			check(atomic.取请求头("x-atomic").equals("old"), "非法替换不破坏旧请求头");
		}
		XHttp请求 snapshot = request("echo");
		snapshot.方法("POST");
		snapshot.置文本请求体("before", "text/plain");
		Run sent = start(snapshot);
		snapshot.置文本请求体("after", "text/plain");
		check(sent.ended.await(5, TimeUnit.SECONDS), "快照请求完成");
		check(json(sent.last()).get("body").getAsString().equals("before"), "发送后修改不影响请求体");
		XHttp请求 readTimeout = request("delay?seconds=0.15");
		readTimeout.超时(50);
		readTimeout.读取超时(1000);
		check(exchange(readTimeout).last().成功(), "单独读取超时覆盖统一超时");
		readTimeout.标识(UUID.randomUUID().toString());
		readTimeout.单次总超时(30);
		check(exchange(readTimeout).last().错误类型().equals("超时"), "单次总超时不误报主动取消");
		ui(new Callable<Void>() {
			@Override
			public Void call() {
				events.skipParser = true;
				return null;
			}
		});
		XHttp请求 unparsed = request("business");
		unparsed.解析方式(XHttp请求.解析_自定义);
		check(exchange(unparsed).last().错误类型().equals("解析"), "自定义解析未处理不得视为业务结果");
		ui(new Callable<Void>() {
			@Override
			public Void call() {
				events.skipParser = false;
				return null;
			}
		});
		XHttp请求 fraction = request("business-fraction");
		fraction.解析方式(XHttp请求.解析_标准);
		check(exchange(fraction).last().错误类型().equals("解析"), "小数业务码不得截断为成功整数");
		ui(new Callable<Void>() {
			@Override
			public Void call() {
				events.client.最大响应字节(8);
				return null;
			}
		});
		check(exchange(request("echo")).last().错误类型().equals("响应"), "内存响应上限有明确分类");
		ui(new Callable<Void>() {
			@Override
			public Void call() {
				events.client.最大响应字节(8L * 1024 * 1024);
				return null;
			}
		});
		XHttp请求 expiry = request("stable?id=" + UUID.randomUUID());
		expiry.缓存策略(XHttp客户端.缓存_仅网络);
		expiry.缓存秒数(1);
		check(exchange(expiry).last().成功(), "建立短期缓存");
		Thread.sleep(1150);
		expiry.缓存策略(XHttp客户端.缓存_仅缓存);
		expiry.标识(UUID.randomUUID().toString());
		check(exchange(expiry).last().错误类型().equals("缓存"), "过期缓存不能因重新载入内存而续期");
		String cachedId = UUID.randomUUID().toString();
		XHttp请求 cancelCache = request("stable?id=" + cachedId);
		cancelCache.缓存策略(XHttp请求.缓存_仅网络);
		check(exchange(cancelCache).last().成功(), "准备缓存回调取消用例");
		cancelCache.缓存策略(XHttp请求.缓存_缓存及网络);
		cancelCache.标识("cancel-cache-" + UUID.randomUUID());
		Run cacheCancelled = start(cancelCache);
		check(cacheCancelled.ended.await(3, TimeUnit.SECONDS) && cacheCancelled.cancels == 1,
			"缓存响应回调可以重入取消");
		check(cacheCancelled.replies.size() == 1 && cacheCancelled.last().来自缓存(), "取消后不继续交付远程响应");
		JsonObject stats = json(exchange(request("stats")).last());
		check(stats.get("/stable:" + cachedId).getAsInt() == 1, "取消缓存回调后服务器未收到第二次请求");
		final String named = "regression-" + UUID.randomUUID();
		ui(new Callable<Void>() {
			@Override
			public Void call() {
				events.client.置会话(named, true);
				events.client.设置Cookie(BASE, "persistent=yes; Path=/; Max-Age=60");
				return null;
			}
		});
		XHttp请求 disk = request("stable?id=" + UUID.randomUUID());
		disk.缓存策略(XHttp客户端.缓存_仅网络);
		check(exchange(disk).last().成功(), "命名会话写入磁盘");
		ui(new Callable<Void>() {
			@Override
			public Void call() {
				events.client.设置Cookie(BASE, "session=temporary; Path=/");
				events.client.销毁();
				newClient();
				events.client.置会话(named, true);
				String[] cookies = events.client.取Cookie(BASE);
				check(cookies.length == 1 && cookies[0].startsWith("persistent=yes"), "仅持久 Cookie 跨实例恢复");
				check(!cookies[0].toLowerCase(java.util.Locale.ROOT).contains("domain="), "持久化保持 hostOnly 约束");
				return null;
			}
		});
		disk.缓存策略(XHttp客户端.缓存_仅缓存);
		disk.标识(UUID.randomUUID().toString());
		check(exchange(disk).last().来自缓存(), "同名会话关闭后重新读取磁盘缓存");
		final XHttp请求[] stopped = new XHttp请求[] {request("status/503"), request("delay?seconds=2")};
		events.batchDone = new CountDownLatch(1);
		ui(new Callable<Void>() {
			@Override
			public Void call() {
				events.client.批量发送(stopped, true, true);
				return null;
			}
		});
		check(events.batchDone.await(5, TimeUnit.SECONDS), "串行失败即停完成");
		check(events.batch.失败数量() == 2 && events.batch.响应列表()[1].错误类型().equals("取消"), "后续项目取消而非假成功");
		XHttp请求 reentry = request("echo");
		reentry.标识("cancel-hook-" + UUID.randomUUID());
		Run cancelled = start(reentry);
		check(cancelled.ended.await(3, TimeUnit.SECONDS) && cancelled.cancels == 1 && cancelled.ends == 1,
			"请求前处理重入取消只产生一次终态");
		ui(new Callable<Void>() {
			@Override
			public Void call() {
				events.client.销毁();
				newClient();
				File root = new File(activity.getCacheDir(), "es4a-xhttp2");
				String[] before = root.list();
				int count = before == null ? 0 : before.length;
				for (int index = 0; index < 3; index++) {
					events.client.取Cookie(BASE);
					events.client.销毁();
					newClient();
				}
				String[] after = root.list();
				check((after == null ? 0 : after.length) == count, "匿名会话销毁不累积磁盘目录");
				return null;
			}
		});
	}

	/**
	 * 服务端屏障直接证明两个请求同时在途，代理认证通过真实中转而非仅检查配置字段。
	 */
	private void verifyConcurrencyAndProxy() throws Exception {
		final Events second = new Events();
		ui(new Callable<Void>() {
			@Override
			public Void call() {
				createClient(second);
				events.client.置默认请求头("X-Client", "first");
				second.client.置默认请求头("X-Client", "second");
				return null;
			}
		});
		try {
			String group = UUID.randomUUID().toString();
			Run first = start(events, request("barrier?group=" + group));
			Run other = start(second, request("barrier?group=" + group));
			check(first.ended.await(5, TimeUnit.SECONDS) && other.ended.await(5, TimeUnit.SECONDS), "两个客户端并发请求完成");
			check(json(first.last()).get("peak").getAsInt() >= 2 && json(other.last()).get("peak").getAsInt() >= 2,
				"服务端实际同时处理两个请求");
			check(header(json(first.last()), "X-Client").equals("first") && header(json(other.last()), "X-Client").equals("second"),
				"并发客户端默认请求头隔离");
		} finally {
			ui(new Callable<Void>() {
				@Override
				public Void call() {
					second.client.销毁();
					events.client.删除默认请求头("X-Client");
					return null;
				}
			});
		}
		ui(new Callable<Void>() {
			@Override
			public Void call() {
				events.client.置代理("127.0.0.1", 18781, "", "");
				return null;
			}
		});
		check(exchange(request("echo")).last().状态码() == 407, "代理未认证返回真实 407");
		ui(new Callable<Void>() {
			@Override
			public Void call() {
				events.client.置代理("127.0.0.1", 18781, "qa", "proxy");
				return null;
			}
		});
		XHttp响应 proxied = exchange(request("echo")).last();
		check(proxied.成功(), "HTTP 代理 Basic 认证成功");
		check(header(json(proxied), "Proxy-Authorization").isEmpty(), "代理凭据不转发给目标服务");
		ui(new Callable<Void>() {
			@Override
			public Void call() {
				events.client.置信任证书(new String[] {"asset://xhttp2-test-ca.pem"}, false);
				return null;
			}
		});
		check(exchange(request("https://localhost:18743/echo")).last().成功(), "HTTPS 通过认证 CONNECT 隧道且验证 CA");
		ui(new Callable<Void>() {
			@Override
			public Void call() {
				events.client.置代理("", 0, "", "");
				events.client.置信任证书(new String[0], true);
				return null;
			}
		});
	}

	private static JsonObject json(XHttp响应 response) {
		if (!response.成功()) {
			throw new AssertionError(response.错误类型() + " " + response.错误());
		}
		return new JsonParser().parse(response.文本()).getAsJsonObject();
	}

	/**
	 * 删除和清空都等待真实后台维护回调，再验证缓存内容而非只判断操作事件。
	 */
	private void verifyCacheMaintenance() throws Exception {
		XHttp请求 fill = request("stable?id=" + UUID.randomUUID());
		fill.缓存策略(XHttp客户端.缓存_仅网络);
		final String key = exchange(fill).last().缓存键();
		fill.缓存策略(XHttp客户端.缓存_缓存优先);
		fill.标识(UUID.randomUUID().toString());
		check(exchange(fill).last().来自缓存(), "先缓存命中真实缓存");
		ui(new Callable<Void>() {
			@Override
			public Void call() {
				events.cacheDone = new CountDownLatch(1);
				events.client.删除缓存(key);
				return null;
			}
		});
		check(events.cacheDone.await(5, TimeUnit.SECONDS) && events.cacheSuccess && events.cacheKey.equals(key), "指定缓存键删除完成");
		fill.缓存策略(XHttp客户端.缓存_仅缓存);
		fill.标识(UUID.randomUUID().toString());
		check(!exchange(fill).last().成功(), "删除后不能继续命中应用缓存");
		fill.缓存策略(XHttp客户端.缓存_仅网络);
		fill.标识(UUID.randomUUID().toString());
		check(exchange(fill).last().成功(), "删除后可重新填充");
		final XHttp请求 http = request("http-cache?id=" + UUID.randomUUID());
		http.缓存策略(XHttp客户端.缓存_HTTP);
		check(exchange(http).last().成功(), "清空前填充 HTTP 缓存");
		ui(new Callable<Void>() {
			@Override
			public Void call() {
				events.cacheDone = new CountDownLatch(1);
				events.client.清空缓存();
				return null;
			}
		});
		check(events.cacheDone.await(5, TimeUnit.SECONDS) && events.cacheSuccess && events.cacheKey.isEmpty(), "清空全部缓存完成");
		fill.缓存策略(XHttp客户端.缓存_仅缓存);
		fill.标识(UUID.randomUUID().toString());
		check(!exchange(fill).last().成功(), "清空移除应用缓存");
		http.标识(UUID.randomUUID().toString());
		check(!exchange(http).last().来自缓存(), "清空也移除 HTTP 缓存");
		XHttp请求 fallback = request("fallback?id=" + UUID.randomUUID());
		fallback.缓存策略(XHttp客户端.缓存_仅网络);
		check(exchange(fallback).last().成功(), "准备网络失败降级缓存");
		fallback.标识(UUID.randomUUID().toString());
		fallback.缓存策略(XHttp客户端.缓存_网络优先);
		check(exchange(fallback).last().来自缓存(), "服务端真实 503 后回退到有效缓存");
		fallback.标识(UUID.randomUUID().toString());
		fallback.缓存策略(XHttp客户端.缓存_仅缓存);
		fallback.缓存秒数(0);
		check(!exchange(fallback).last().成功(), "零有效期禁止复用旧缓存");
	}

	/**
	 * 未知长度必须通过实际 ContentProvider 管道传输；取消也必须终止在途上传。
	 */
	private void verifyUnknownUploads() throws Exception {
		XHttp请求 content = request("echo");
		content.方法("POST");
		content.添加文件("source", "content://simple.library.wsd.xhttp2.test.source/normal", "provider.bin", "application/octet-stream");
		Run uploaded = exchange(content);
		JsonObject echo = json(uploaded.last());
		check(echo.get("body").getAsString().contains("未知长度中文来源"), "未知长度 URI 内容完整到达服务端");
		check(header(echo, "Transfer-Encoding").equals("chunked") && !header(echo, "Content-Type").isEmpty(), "未知长度 multipart 采用分块传输");
		check(uploaded.unknownUpload, "未知上传总量明确为负一");
		content.标识(UUID.randomUUID().toString());
		content.地址("retry?id=" + UUID.randomUUID() + "&fail=1&delay=0.25");
		content.超时(60);
		content.重试次数(1);
		content.重试间隔(10);
		check(json(exchange(content).last()).get("body").getAsString().contains("未知长度中文来源"),
			"上传超时重试重新打开 URI 来源并保留完整内容");
		XHttp请求 slow = request("echo");
		slow.方法("POST");
		slow.标识("cancel-upload-" + UUID.randomUUID());
		slow.添加文件("source", "content://simple.library.wsd.xhttp2.test.source/slow", "large.bin", "application/octet-stream");
		Run cancelled = start(slow);
		check(cancelled.ended.await(5, TimeUnit.SECONDS), "未知长度上传在进度回调中及时取消");
		check(cancelled.progress > 0 && cancelled.cancels == 1 && cancelled.ends == 1 && cancelled.successes == 0,
			"取消上传不误报成功且只结束一次");
	}

	/**
	 * 用真实根视图移出窗口验证取消和重入；排队与销毁分别检查事件及会话引用回收。
	 */
	private void verifyLifecycle() throws Exception {
		final XHttp请求 single = request("delay?seconds=2");
		Run ongoing = start(single);
		ui(new Callable<Void>() {
			@Override
			public Void call() {
				boolean duplicate = false;
				try {
					events.client.发送(single);
				} catch (IllegalArgumentException expected) {
					duplicate = true;
				}
				check(duplicate && events.client.取运行数量() == 1, "拒绝重复标识且不遗留任务");
				boolean synchronous = false;
				try {
					events.client.同步发送(request("echo"));
				} catch (IllegalStateException expected) {
					synchronous = true;
				}
				check(synchronous, "界面线程禁止同步网络调用");
				activity.setContentView(new FrameLayout(activity));
				return null;
			}
		});
		check(ongoing.ended.await(3, TimeUnit.SECONDS) && ongoing.cancels == 1 && ongoing.ends == 1, "移出所属窗口取消在途请求");
		ui(new Callable<Void>() {
			@Override
			public Void call() {
				boolean detached = false;
				try {
					events.client.发送(request("echo"));
				} catch (IllegalStateException expected) {
					detached = true;
				}
				check(detached, "所属窗口未显示时拒绝发送");
				activity.setContentView(container.getView());
				return null;
			}
		});
		waitForIdleSync();
		check(exchange(request("echo")).last().成功(), "同一窗口重新显示后客户端可用");
		final List<Run> queued = new ArrayList<Run>();
		ui(new Callable<Void>() {
			@Override
			public Void call() {
				for (int index = 0; index < 8; index++) {
					XHttp请求 input = request("delay?seconds=2");
					Run run = new Run();
					events.running.put(input.标识(), run);
					queued.add(run);
					events.client.发送(input);
				}
				events.client.取消全部();
				check(events.client.取运行数量() == 0, "取消全部立即清除在途和排队任务");
				return null;
			}
		});
		for (Run run : queued) {
			check(run.ended.await(3, TimeUnit.SECONDS) && run.cancels == 1 && run.ends == 1, "排队取消仅一组终态事件");
		}
		final Events doomed = new Events();
		ui(new Callable<Void>() {
			@Override
			public Void call() {
				createClient(doomed);
				XHttp请求 input = request("delay?seconds=2");
				doomed.running.put(input.标识(), new Run());
				doomed.client.发送(input);
				doomed.client.销毁();
				return null;
			}
		});
		SystemClock.sleep(400);
		Run destroyed = doomed.running.values().iterator().next();
		check(destroyed.replies.isEmpty() && destroyed.ends == 0 && destroyed.cancels == 0, "销毁后不回调旧窗口");
		ui(new Callable<Void>() {
			@Override
			public Void call() {
				boolean refused = false;
				try {
					doomed.client.发送(request("echo"));
				} catch (IllegalStateException expected) {
					refused = true;
				}
				check(refused, "销毁后客户端不可复用");
				events.client.销毁();
				return null;
			}
		});
		Field registry = SessionState.class.getDeclaredField("OPEN");
		registry.setAccessible(true);
		long deadline = SystemClock.elapsedRealtime() + 5000;
		int open;
		do {
			synchronized (SessionState.class) {
				open = ((Map<?, ?>) registry.get(null)).size();
			}
			if (open > 0) {
				SystemClock.sleep(25);
			}
		} while (open > 0 && SystemClock.elapsedRealtime() < deadline);
		check(open == 0, "所有客户端销毁且在途调用退出后没有遗留会话句柄");
	}

	private static String header(JsonObject echo, String name) {
		String result = "";
		for (JsonElement pair : echo.getAsJsonArray("headers")) {
			if (pair.getAsJsonArray().get(0).getAsString().equalsIgnoreCase(name)) {
				result += pair.getAsJsonArray().get(1).getAsString();
			}
		}
		return result;
	}

	private <T> T ui(final Callable<T> callback) throws Exception {
		final AtomicReference<T> result = new AtomicReference<T>();
		final AtomicReference<Throwable> error = new AtomicReference<Throwable>();
		runOnMainSync(new Runnable() {
			@Override
			public void run() {
				try {
					result.set(callback.call());
				} catch (Throwable failure) {
					error.set(failure);
				}
			}
		});
		if (error.get() != null) {
			if (error.get() instanceof Exception) {
				throw (Exception) error.get();
			}
			throw new AssertionError("界面断言失败", error.get());
		}
		return result.get();
	}

	private void check(boolean value, String description) {
		if (!value) {
			throw new AssertionError(description);
		}
		assertions++;
	}

	private static final class Run {
		final CountDownLatch ended = new CountDownLatch(1);
		final CountDownLatch downloadProgress = new CountDownLatch(1);
		final List<XHttp响应> replies = new ArrayList<XHttp响应>();
		int successes;
		int failures;
		int cancels;
		int ends;
		int progress;
		boolean unknownUpload;

		XHttp响应 last() {
			if (replies.isEmpty()) {
				throw new AssertionError("没有收到响应。");
			}
			return replies.get(replies.size() - 1);
		}
	}

	/**
	 * EventDispatcher 通过公开字段找到组件，所有计数均来自真实界面事件。
	 */
	public static final class Events {
		public XHttp客户端Impl client;
		final Map<String, Run> running = new LinkedHashMap<String, Run>();
		CountDownLatch batchDone;
		XHttp批结果 batch;
		boolean skipParser;
		CountDownLatch cacheDone;
		String cacheKey;
		boolean cacheSuccess;

		public void client$请求前处理(XHttp请求 request) {
			if (request.标识().startsWith("cancel-hook-")) {
				client.取消(request.标识());
			}
			if (request.标识().startsWith("hook-")) {
				request.置请求头("Authorization", "Bearer dynamic-token");
			}
		}

		public void client$请求开始(String id) {
			if (Looper.myLooper() != Looper.getMainLooper()) {
				throw new AssertionError("事件不在界面线程。");
			}
		}

		public void client$收到响应(String id, XHttp响应 response) {
			Run run = running.get(id);
			if (run != null) {
				run.replies.add(response);
			}
			if (id.startsWith("cancel-cache-") && response.来自缓存()) {
				client.取消(id);
			}
		}

		public void client$请求成功(String id, XHttp响应 response) {
			if (running.containsKey(id)) {
				running.get(id).successes++;
			}
		}

		public void client$请求失败(String id, XHttp响应 response) {
			if (running.containsKey(id)) {
				running.get(id).failures++;
			}
		}

		public void client$请求取消(String id) {
			if (running.containsKey(id)) {
				running.get(id).cancels++;
			}
		}

		public void client$请求结束(String id) {
			Run run = running.get(id);
			if (run != null) {
				run.ends++;
				run.ended.countDown();
			}
		}

		public void client$解析响应(String id, XHttp响应 response, XHttp解析结果 result) {
			if (skipParser) {
				return;
			}
			result.成功(true);
			result.数据("已适配");
			result.消息("");
		}

		public void client$传输进度(String id, boolean upload, long transferred, long total) {
			if (running.containsKey(id)) {
				running.get(id).progress++;
				if (!upload && transferred > 0) {
					running.get(id).downloadProgress.countDown();
				}
				if (upload && total == -1) {
					running.get(id).unknownUpload = true;
				}
			}
			if (upload && transferred > 0 && id.startsWith("cancel-upload-")) {
				client.取消(id);
			}
		}

		public void client$缓存操作完成(String key, boolean success, String error) {
			cacheKey = key;
			cacheSuccess = success && error.isEmpty();
			if (cacheDone != null) {
				cacheDone.countDown();
			}
		}

		public void client$批次完成(XHttp批结果 result) {
			batch = result;
			batchDone.countDown();
		}
	}
}
