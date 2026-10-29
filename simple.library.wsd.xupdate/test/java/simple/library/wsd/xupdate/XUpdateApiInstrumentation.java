package simple.library.wsd.xupdate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.atomic.AtomicReference;

import android.app.Activity;
import android.app.Instrumentation;
import android.app.Notification;
import android.app.NotificationManager;
import android.service.notification.StatusBarNotification;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.ViewGroup;

import com.xuexiang.xupdate.widget.UpdateDialog;
import simple.library.wsd.xupdateapi.Aria下载器;
import simple.library.wsd.xupdateapi.快捷更新器;
import simple.library.wsd.xupdateapi.快捷更新器Impl;

import com.arialyy.aria.http.ConnectionHelp;
import com.arialyy.aria.http.HttpTaskOption;

import javax.net.ssl.SSLHandshakeException;

import org.json.JSONArray;
import org.json.JSONObject;

import simple.runtime.android.MainActivity;
import simple.runtime.components.组件;
import simple.runtime.components.impl.android.面板Impl;
import simple.runtime.events.EventDispatcher;
import simple.runtime.variants.IntegerVariant;

/**
 * 在真实 Simple 测试应用进程中验证 EasyUpdate 请求与 Aria 续传，不模拟成功回调。
 * 本机服务返回的 Range 记录和下载文件摘要作为独立证据。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUpdateApiInstrumentation extends Instrumentation {

	private static final String BASE = "http://127.0.0.1:18765";
	private MainActivity activity;
	private 面板Impl container;
	private Events events;
	private Events second;
	private int assertions;
	private boolean useCore;
	private boolean strategyOnly;

	@Override
	public void onCreate(Bundle arguments) {
		super.onCreate(arguments);
		strategyOnly = arguments != null && "strategy".equals(arguments.getString("only"));
		start();
	}

	@Override
	public void onStart() {
		Bundle result = new Bundle();
		try {
			activity = (MainActivity) startActivitySync(new Intent().setComponent(new ComponentName(
					"simple.library.wsd.xupdate.test", "simple.runtime.android.MainActivity"))
					.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
			waitForIdleSync();
			if (strategyOnly) {
				verifyStrategyInjection();
				result.putString("stream", "XUPDATE_STRATEGY_PASS assertions=" + assertions);
				finish(Activity.RESULT_OK, result);
				return;
			}
			verifyRequests();
			verifyPlainDownload();
			verifyTls();
			verifyMissingDigest();
			verifyResume("/resume", true);
			verifyResume("/resume-norange", false);
			verifyCancellation();
			verifyChecksum();
			verifyNetworkRecovery();
			verifyPromptResume();
			verifyIsolation();
			verifyDetach();
			verifyDecoupledStages();
			verifyBackground();
			verifyStrategyInjection();
			result.putString("stream", "XUPDATE_API_PASS assertions=" + assertions
					+ " requests/json/form/plain/tls-rejection/range/resume/no-range/cancel/md5/network-recovery/prompt-resume/isolation/detach/custom-stages/independent-download/local-install/background");
			finish(Activity.RESULT_OK, result);
		} catch (Throwable failure) {
			result.putString("stream", "XUPDATE_API_FAIL " + Log.getStackTraceString(failure));
			finish(Activity.RESULT_CANCELED, result);
		}
	}

	/**
	 * 每个场景使用新的私有目录，旧窗口被实际移出视图树。
	 */
	private void prepare() {
		onUi(new Runnable() {
			@Override
			public void run() {
				if (events != null) {
					events.update.销毁();
					EventDispatcher.unregisterEvent(events);
				}
				if (second != null) {
					second.update.销毁();
					EventDispatcher.unregisterEvent(second);
					second = null;
				}
				container = new 面板Impl(null) {
					@Override
					protected void addToContainer() {
						// 此处为实际 Activity 测试根，不插入正式窗口的已有布局。
					}
				};
				container.布局(IntegerVariant.getIntegerVariant(组件.布局_线性));
				activity.setContentView(container.getView());
				events = createEvents();
			}
		});
		waitForIdleSync();
	}

	/**
	 * 注册真实 Simple 事件接收者；方法命名与 EventDispatcher 协议一致。
	 *
	 * @return 当前容器的新更新器及事件记录。
	 */
	private Events createEvents() {
		Events value = new Events();
		value.update = useCore ? new 更新器Impl(container) : new 快捷更新器Impl(container);
		value.update.初始化();
		value.update.自动提示(false);
		value.update.下载后安装(false);
		for (String name : new String[] { "发现更新", "无需更新", "下载进度", "下载完成", "失败", "已暂停", "已取消" }) {
			EventDispatcher.registerEvent(value, "update", name);
		}
		return value;
	}

	/**
	 * 核对真实 GET、表单、JSON、响应失败及双实例请求。
	 *
	 * @throws Exception 请求或等待失败。
	 */
	private void verifyRequests() throws Exception {
		for (final String endpoint : new String[] { "/normal", "/post", "/json", "/none", "/malformed" }) {
			prepare();
			onUi(new Runnable() {
				@Override
				public void run() {
					events.update.使用POST(endpoint.equals("/post") || endpoint.equals("/json"));
					events.update.POST使用JSON(endpoint.equals("/json"));
					if (endpoint.equals("/post") || endpoint.equals("/json")) {
						events.update.置请求参数("城市", "杭州 + 上海");
						events.update.置请求头("X-Update-Test", "isolated");
					}
					events.update.检查更新(BASE + endpoint);
				}
			});
			await(events, endpoint.equals("/none") ? "none" : endpoint.equals("/malformed") ? "failed" : "found", 12000);
			check(events.completed == 0, "检查不误报下载完成：" + endpoint);
			if (endpoint.equals("/post") || endpoint.equals("/json")) {
				check(events.info.版本名().equals(endpoint.equals("/post") ? "POST已验证" : "JSON已验证"), "真实请求体：" + endpoint);
			}
		}
	}

	/**
	 * 普通下载仍允许省略 MD5，不能被新续传模式改变原有可选校验规则。
	 *
	 * @throws Exception 普通下载失败。
	 */
	private void verifyPlainDownload() throws Exception {
		prepare();
		onUi(new Runnable() {
			@Override
			public void run() {
				events.update.检查更新(BASE + "/normal");
			}
		});
		await(events, "found", 12000);
		onUi(new Runnable() {
			@Override
			public void run() {
				events.info.MD5("");
				events.update.下载更新(events.info);
			}
		});
		await(events, "complete", 30000);
		check(events.failed == 0 && new File(events.path).isFile(), "普通下载可省略 MD5");
	}

	/**
	 * 使用本机真实自签名 TLS 服务，确认请求和 Aria 均拒绝未受信任证书。
	 *
	 * @throws Exception TLS 验证失败。
	 */
	private void verifyTls() throws Exception {
		HttpURLConnection connection = ConnectionHelp.handleConnection(new URL("https://127.0.0.1:18766/apk"),
				new HttpTaskOption());
		connection.setConnectTimeout(3000);
		connection.setReadTimeout(3000);
		boolean rejected = false;
		try {
			connection.getInputStream().close();
		} catch (SSLHandshakeException expected) {
			rejected = true;
		} finally {
			connection.disconnect();
		}
		check(rejected, "本地 Aria HTTP 连接拒绝未受信任证书");
		prepare();
		onUi(new Runnable() {
			@Override
			public void run() {
				events.update.检查更新("https://127.0.0.1:18766/normal");
			}
		});
		await(events, "failed", 12000);
		check(events.found == 0 && events.completed == 0, "EasyUpdate 请求拒绝未受信任证书");
		prepare();
		onUi(new Runnable() {
			@Override
			public void run() {
				events.update.检查更新(BASE + "/normal");
			}
		});
		await(events, "found", 12000);
		onUi(new Runnable() {
			@Override
			public void run() {
				events.info.下载地址("https://127.0.0.1:18766/apk");
				((快捷更新器) events.update).断点续传(true);
				events.update.请求超时(1000);
				events.update.下载更新(events.info);
			}
		});
		await(events, "failed", 20000);
		check(events.completed == 0 && events.bytes == 0, "实际 Aria 下载拒绝未受信任证书");
	}

	/**
	 * 续传必须拒绝缺失摘要，不能沿用未验证的部分文件。
	 *
	 * @throws Exception 检查或参数验证失败。
	 */
	private void verifyMissingDigest() throws Exception {
		prepare();
		onUi(new Runnable() {
			@Override
			public void run() {
				events.update.检查更新(BASE + "/normal");
			}
		});
		await(events, "found", 12000);
		onUi(new Runnable() {
			@Override
			public void run() {
				events.info.MD5("");
				((快捷更新器) events.update).断点续传(true);
				events.update.下载更新(events.info);
			}
		});
		await(events, "failed", 12000);
		check(events.failed == 1 && events.message.contains("MD5") && events.completed == 0,
				"续传缺少 MD5 明确失败");
	}

	/**
	 * 实际暂停后核对回调停止、恢复请求及完整文件校验；无 Range 服务也须正确完成。
	 *
	 * @param endpoint 服务端版本接口。
	 * @param range 是否要求观察非零 Range 起点。
	 * @throws Exception 设备或服务端验证失败。
	 */
	private void verifyResume(String endpoint, boolean range) throws Exception {
		prepare();
		request("/reset");
		startDownload(endpoint);
		await(events, "progress", 15000);
		check(events.bytes > 0, "实际接收字节");
		onUi(new Runnable() {
			@Override
			public void run() {
				events.update.暂停下载();
			}
		});
		await(events, "paused", 12000);
		int progress = events.progress;
		SystemClock.sleep(800);
		check(events.progress == progress && events.completed == 0, "暂停后无进度和完成回调");
		check(state() == 更新器.状态_暂停, "暂停状态");
		JSONArray before = new JSONArray(request("/stats"));
		final int count = before.length();
		onUi(new Runnable() {
			@Override
			public void run() {
				events.update.继续下载();
			}
		});
		await(events, "complete", 90000);
		check(events.failed == 0 && events.completed == 1 && events.percent == 100, "续传校验后只完成一次：" + endpoint);
		File file = new File(events.path);
		check(file.isFile() && file.length() > 0, "校验后最终文件存在");
		String evidence = request("/stats");
		JSONArray after = new JSONArray(evidence);
		try (FileOutputStream output = new FileOutputStream(new File(activity.getFilesDir(),
				"xupdate-api-" + (range ? "range" : "no-range") + ".json"))) {
			output.write(evidence.getBytes(StandardCharsets.UTF_8));
		}
		boolean resumed = false;
		for (int index = count; index < after.length(); index++) {
			JSONObject entry = after.getJSONObject(index);
			if (entry.getLong("start") > 0 && entry.getInt("status") == 206) {
				resumed = true;
			}
			check("download-isolated".equals(entry.getString("header")), "续传请求头沿用快照");
		}
		check(!range || resumed, "暂停后产生新的非零 Range 请求");
		onUi(new Runnable() {
			@Override
			public void run() {
				events.update.清理缓存();
			}
		});
		check(!file.exists() && state() == 更新器.状态_空闲, "清理移除已完成文件及状态");
		Log.i("XUPDATE-API", "resume verified " + endpoint + " newRequests=" + (after.length() - count));
	}

	/**
	 * 取消后等待原生任务退出，再核对未完成文件已经移除。
	 *
	 * @throws Exception 下载或取消验证失败。
	 */
	private void verifyCancellation() throws Exception {
		prepare();
		startDownload("/resume");
		await(events, "progress", 15000);
		final File directory = directory();
		onUi(new Runnable() {
			@Override
			public void run() {
				events.update.取消();
			}
		});
		SystemClock.sleep(1800);
		check(events.cancelled == 1 && events.completed == 0, "取消无完成回调");
		check(directory.listFiles() == null || directory.listFiles().length == 0, "取消移除本实例分片和记录文件");
		check(state() == 更新器.状态_空闲, "取消回到空闲");
	}

	/**
	 * 下载完成后的整文件 MD5 失败不能发布成功文件或保留坏缓存。
	 *
	 * @throws Exception 校验验证失败。
	 */
	private void verifyChecksum() throws Exception {
		prepare();
		startDownload("/resume-badmd5");
		await(events, "failed", 90000);
		SystemClock.sleep(500);
		check(events.completed == 0 && events.message.contains("MD5"), "MD5 失败不回调完成");
		check(directory().listFiles() == null || directory().listFiles().length == 0, "校验失败移除坏文件");
	}

	/**
	 * 下载网络失败后保留记录；服务恢复后继续同一任务，不伪造成功或丢弃失败事件。
	 *
	 * @throws Exception 恢复验证失败。
	 */
	private void verifyNetworkRecovery() throws Exception {
		prepare();
		request("/reset");
		onUi(new Runnable() {
			@Override
			public void run() {
				events.update.请求超时(1000);
			}
		});
		startDownload("/resume-recover");
		await(events, "failed", 20000);
		check(events.completed == 0 && state() == 更新器.状态_暂停, "网络失败保留可恢复状态");
		request("/allow-recovery");
		onUi(new Runnable() {
			@Override
			public void run() {
				events.update.继续下载();
			}
		});
		// 已记录的首次失败不是本次继续下载的失败。
		long deadline = SystemClock.uptimeMillis() + 90000;
		while (events.completed == 0 && events.failed == 1 && SystemClock.uptimeMillis() < deadline) {
			SystemClock.sleep(40);
		}
		check(events.completed == 1 && events.failed == 1, "服务恢复后继续并完成校验：" + events.message);
	}

	/**
	 * 原生更新框下载暂停后，必须能通过真实“继续下载”按钮恢复并完成。
	 *
	 * @throws Exception 提示框或下载验证失败。
	 */
	private void verifyPromptResume() throws Exception {
		prepare();
		onUi(new Runnable() {
			@Override
			public void run() {
				events.update.检查更新(BASE + "/resume");
			}
		});
		await(events, "found", 12000);
		onUi(new Runnable() {
			@Override
			public void run() {
				((快捷更新器) events.update).断点续传(true);
				events.update.显示更新(events.info);
				prompt().findViewById(com.xuexiang.xupdate.R.id.btn_update).performClick();
			}
		});
		await(events, "progress", 15000);
		onUi(new Runnable() {
			@Override
			public void run() {
				events.update.暂停下载();
			}
		});
		await(events, "paused", 12000);
		onUi(new Runnable() {
			@Override
			public void run() {
				check(prompt().findViewById(com.xuexiang.xupdate.R.id.btn_update).isShown(), "原生框显示继续入口");
				prompt().findViewById(com.xuexiang.xupdate.R.id.btn_update).performClick();
			}
		});
		await(events, "complete", 90000);
		check(events.failed == 0 && events.completed == 1, "原生框按钮实际继续并完成");
	}

	/**
	 * 只读取当前原生更新框以执行真实按钮点击。
	 *
	 * @return 当前更新框。
	 */
	private UpdateDialog prompt() {
		try {
			Field field = UpdateController.class.getDeclaredField("dialog");
			field.setAccessible(true);
			return (UpdateDialog) field.get(events.update);
		} catch (ReflectiveOperationException exception) {
			throw new AssertionError(exception);
		}
	}

	/**
	 * 不同实例同地址检查独立；同地址下载并发明确拒绝且不取消原任务。
	 *
	 * @throws Exception 并发验证失败。
	 */
	private void verifyIsolation() throws Exception {
		prepare();
		onUi(new Runnable() {
			@Override
			public void run() {
				second = createEvents();
				events.update.检查更新(BASE + "/normal");
				second.update.检查更新(BASE + "/normal");
			}
		});
		await(events, "found", 12000);
		await(second, "found", 12000);
		check(events.failed == 0 && second.failed == 0, "同地址检查互不阻塞");
		onUi(new Runnable() {
			@Override
			public void run() {
				events.info.下载地址(BASE + "/rangeapk");
				second.info.下载地址(BASE + "/rangeapk");
				((快捷更新器) events.update).断点续传(true);
				((快捷更新器) second.update).断点续传(true);
				events.update.下载更新(events.info);
				second.update.下载更新(second.info);
			}
		});
		await(second, "failed", 12000);
		await(events, "progress", 15000);
		check(second.message.contains("相同地址") && events.failed == 0, "下载冲突明确失败且不接管原实例");
		onUi(new Runnable() {
			@Override
			public void run() {
				second.update.取消();
			}
		});
		check(state() == 更新器.状态_下载, "取消冲突实例不影响正在下载的实例");
	}

	/**
	 * 从真实视图树移除所属容器，验证静默取消及旧事件隔离。
	 *
	 * @throws Exception 生命周期验证失败。
	 */
	private void verifyDetach() throws Exception {
		prepare();
		startDownload("/resume");
		await(events, "progress", 15000);
		onUi(new Runnable() {
			@Override
			public void run() {
				((ViewGroup) container.getView().getParent()).removeView(container.getView());
			}
		});
		int count = events.progress;
		SystemClock.sleep(1800);
		check(events.progress == count && events.completed == 0 && events.cancelled == 0, "离开窗口后不发送旧事件");
		check(state() == 更新器.状态_空闲, "离开窗口静默取消");
	}

	/**
	 * 从本地版本接口取得真实摘要和地址，再由实际导出方法启动续传下载。
	 *
	 * @param endpoint 版本接口。
	 * @throws Exception 检查或下载启动失败。
	 */
	private void startDownload(final String endpoint) throws Exception {
		onUi(new Runnable() {
			@Override
			public void run() {
				((快捷更新器) events.update).断点续传(true);
				events.update.置请求头("X-Update-Test", "download-isolated");
				events.update.检查更新(BASE + endpoint);
			}
		});
		await(events, "found", 12000);
		onUi(new Runnable() {
			@Override
			public void run() {
				events.update.下载更新(events.info);
			}
		});
	}

	/**
	 * 核对可替换环节和分步入口，不依赖伪造的高版本来允许文件下载。
	 *
	 * @throws Exception 网络、文件或回调验证失败。
	 */
	private void verifyDecoupledStages() throws Exception {
		prepare();
		onUi(new Runnable() {
			@Override
			public void run() {
				更新信息 info = new 更新信息();
				info.下载地址(BASE + "/apk");
				events.update.下载更新(info);
			}
		});
		await(events, "complete", 30000);
		check(events.failed == 0, "无版本号、无有更新标记也能独立下载");
		final File fixture = new File(activity.getFilesDir(), "supplied-update.apk");
		UpdateFiles.copy(new File(events.path), fixture);
		final int[] observed = new int[6];
		prepare();
		onUi(new Runnable() {
			@Override
			public void run() {
				events.update.置检查器(new 检查器() {
					@Override
					public void 检查(String address, 检查回调 callback) {
						observed[0]++;
						更新信息 info = information();
						info.文件大小(12345);
						callback.提交信息(info);
						callback.报告失败("重复结果不应生效");
					}

					@Override
					public void 取消() {
						// 此同步用例没有外部任务需要取消。
					}
				});
				events.update.检查更新(BASE + "/not-requested");
			}
		});
		await(events, "found", 12000);
		check(events.found == 1 && events.failed == 0 && observed[0] == 1, "自定义检查器单次提交");
		check(events.info.版本号() == 0 && events.info.文件大小() == 12345, "自定义更新判断和精确字节数保留");
		prepare();
		onUi(new Runnable() {
			@Override
			public void run() {
				events.update.置解析器(new 解析器() {
					@Override
					public 更新信息 解析(String response) {
						observed[1]++;
						return information();
					}
				});
				events.update.置提示器(new 提示器() {
					@Override
					public void 显示(更新信息 info) {
						observed[2]++;
					}
				});
				events.update.自动提示(true);
				events.update.解析更新("业务协议");
			}
		});
		await(events, "found", 12000);
		waitForIdleSync();
		check(observed[1] == 1 && observed[2] == 1, "自定义解析器与提示器各自接管");
		prepare();
		final String response = request("/normal");
		onUi(new Runnable() {
			@Override
			public void run() {
				events.update.置网络服务(new 网络服务() {
					@Override
					public void 请求(boolean post, boolean json, String address, String parameters, String headers, 检查回调 callback) {
						observed[3]++;
						callback.提交响应(response);
					}

					@Override
					public void 取消() {
						// 响应取自上方真实 HTTP 请求，无活动网络连接。
					}
				});
				events.update.检查更新(BASE + "/provided-response");
			}
		});
		await(events, "found", 12000);
		check(observed[3] == 1, "网络服务可以独立替换而保留默认解析");
		prepare();
		onUi(new Runnable() {
			@Override
			public void run() {
				events.update.置下载器(new 下载器() {
					@Override
					public void 下载(更新信息 info, 下载回调 callback) {
						callback.报告进度(fixture.length(), fixture.length());
						callback.提交文件(fixture.getAbsolutePath());
					}

					@Override
					public void 取消() {
						// 用例提交的是实际已关闭的本地文件。
					}

					@Override
					public void 转入后台() {
						// 此用例同步完成，不产生后台任务。
					}
				});
				events.update.置校验器(new 校验器() {
					@Override
					public boolean 校验(String path, 更新信息 info) {
						observed[4]++;
						return new File(path).length() == fixture.length();
					}
				});
				events.update.下载更新(information());
			}
		});
		await(events, "complete", 12000);
		check(observed[4] == 1 && !events.path.equals(fixture.getPath()), "替换下载器的文件经独立校验并进入私有目录");
		prepare();
		onUi(new Runnable() {
			@Override
			public void run() {
				events.update.置安装器(new 安装器() {
					@Override
					public boolean 安装(String path) {
						observed[5]++;
						return false;
					}
				});
				events.update.安装更新(fixture.getAbsolutePath());
			}
		});
		await(events, "failed", 12000);
		check(observed[5] == 1 && fixture.isFile(), "独立本地安装进入可替换安装器，不要求本实例下载");
	}

	/**
	 * 核心更新器通过公开策略接口接入 API；同时验证普通下载的暂停拒绝不污染状态。
	 *
	 * @throws Exception 真实传输或事件验证失败。
	 */
	private void verifyStrategyInjection() throws Exception {
		useCore = true;
		prepare();
		onUi(new Runnable() {
			@Override
			public void run() {
				events.update.置下载器(new Aria下载器());
				events.update.检查更新(BASE + "/resume");
			}
		});
		await(events, "found", 12000);
		onUi(new Runnable() {
			@Override
			public void run() {
				events.update.下载更新(events.info);
			}
		});
		await(events, "progress", 15000);
		onUi(new Runnable() {
			@Override
			public void run() {
				events.update.暂停下载();
			}
		});
		await(events, "paused", 12000);
		check(state() == 更新器.状态_暂停, "核心组件经策略接口收到实际暂停");
		onUi(new Runnable() {
			@Override
			public void run() {
				events.update.继续下载();
			}
		});
		await(events, "complete", 90000);
		check(events.completed == 1 && events.failed == 0 && events.percent == 100,
				"核心组件与 API 策略联合完成真实下载和校验");
		prepare();
		onUi(new Runnable() {
			@Override
			public void run() {
				events.update.检查更新(BASE + "/slowdownload");
			}
		});
		await(events, "found", 12000);
		onUi(new Runnable() {
			@Override
			public void run() {
				events.update.下载更新(events.info);
				boolean rejected = false;
				try {
					events.update.暂停下载();
				} catch (IllegalArgumentException expected) {
					rejected = true;
				}
				check(rejected && events.update.状态() == 更新器.状态_下载,
						"普通下载拒绝暂停且不改变正在下载状态");
			}
		});
		await(events, "complete", 30000);
		check(events.completed == 1 && events.failed == 0, "暂停拒绝后普通下载仍正常完成");
		useCore = false;
	}

	/**
	 * 使用低于本机版本的信息验证自定义决策不会被外层覆盖。
	 *
	 * @return 不执行本机版本比较的业务结果。
	 */
	private 更新信息 information() {
		更新信息 info = new 更新信息();
		info.有更新(true);
		info.版本号(0);
		info.版本名("业务决策版本");
		info.下载地址(BASE + "/apk");
		return info;
	}

	/**
	 * 关闭实际窗口后等待带通知的下载完成，验证文件任务不依赖界面观察者。
	 *
	 * @throws Exception 下载或通知验证失败。
	 */
	private void verifyBackground() throws Exception {
		prepare();
		startDownload("/slowdownload");
		await(events, "progress", 12000);
		final File cache = directory();
		onUi(new Runnable() {
			@Override
			public void run() {
				events.update.转入后台();
				activity.setContentView(new android.widget.FrameLayout(activity));
			}
		});
		long deadline = SystemClock.uptimeMillis() + 30000;
		boolean complete = false;
		while (SystemClock.uptimeMillis() < deadline) {
			NotificationManager manager = (NotificationManager) activity.getSystemService(Activity.NOTIFICATION_SERVICE);
			for (StatusBarNotification notification : manager.getActiveNotifications()) {
				if ("更新下载完成".contentEquals(notification.getNotification().extras.getCharSequence(Notification.EXTRA_TITLE, ""))) {
					complete = true;
				}
			}
			if (complete) {
				break;
			}
			SystemClock.sleep(100);
		}
		check(complete, "窗口关闭后真实下载完成并生成安装通知");
		check(events.completed == 0, "后台任务不回写已离开的窗口");
		File[] files = cache.listFiles();
		check(files != null && files.length == 1 && files[0].length() > 0, "后台结果保留在任务私有目录");
		check(state() == 更新器.状态_空闲, "旧窗口状态已经解除任务绑定");
	}

	/**
	 * 等待实际事件，出现意外失败时立即保留错误详情。
	 *
	 * @param source 事件记录。
	 * @param event 所需事件名称。
	 * @param timeout 最长等待毫秒数。
	 */
	private void await(Events source, String event, long timeout) {
		long end = SystemClock.uptimeMillis() + timeout;
		while (SystemClock.uptimeMillis() < end) {
			boolean ready = event.equals("found") ? source.found > 0 : event.equals("none") ? source.none > 0
					: event.equals("paused") ? source.paused > 0 : event.equals("complete") ? source.completed > 0
					: event.equals("progress") ? source.bytes > 32768 : source.failed > 0;
			if (ready) {
				assertions++;
				return;
			}
			if (source.failed > 0 && !event.equals("failed")) {
				throw new AssertionError("等待 " + event + " 时失败：" + source.message);
			}
			SystemClock.sleep(40);
		}
		throw new AssertionError("等待事件超时：" + event + "；" + source.message);
	}

	/**
	 * 在界面线程读取当前状态。
	 *
	 * @return 导出状态值。
	 */
	private int state() {
		final int[] value = new int[1];
		onUi(new Runnable() {
			@Override
			public void run() {
				value[0] = events.update.状态();
			}
		});
		return value[0];
	}

	/**
	 * 仅读取实例目录用于检查原生文件，不改变实现内部状态。
	 *
	 * @return 当前实例缓存目录。
	 * @throws Exception 反射字段不可读取。
	 */
	private File directory() throws Exception {
		Field field = UpdateController.class.getDeclaredField("directory");
		field.setAccessible(true);
		return (File) field.get(events.update);
	}

	/**
	 * 读取本地服务器的独立 Range 证据。
	 *
	 * @param path 本地测试端点。
	 * @return UTF-8 JSON。
	 * @throws Exception 网络失败。
	 */
	private String request(String path) throws Exception {
		HttpURLConnection connection = (HttpURLConnection) new URL(BASE + path).openConnection();
		try (InputStream input = connection.getInputStream()) {
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			byte[] buffer = new byte[4096];
			int length;
			while ((length = input.read(buffer)) != -1) {
				output.write(buffer, 0, length);
			}
			return new String(output.toByteArray(), StandardCharsets.UTF_8);
		} finally {
			connection.disconnect();
		}
	}

	/**
	 * 不让界面线程异常被测试运行器吞掉。
	 *
	 * @param action 需要执行的实际操作。
	 */
	private void onUi(final Runnable action) {
		final AtomicReference<Throwable> failure = new AtomicReference<Throwable>();
		runOnMainSync(new Runnable() {
			@Override
			public void run() {
				try {
					action.run();
				} catch (Throwable exception) {
					failure.set(exception);
				}
			}
		});
		if (failure.get() != null) {
			throw new AssertionError(failure.get());
		}
	}

	/**
	 * 累加实际断言；失败即停止，不将未执行场景记为通过。
	 *
	 * @param condition 实际检查结果。
	 * @param message 失败说明。
	 */
	private void check(boolean condition, String message) {
		if (!condition) {
			throw new AssertionError(message);
		}
		assertions++;
	}

	/**
	 * EventDispatcher 的真实接收者，计数仅由实际回调更新。
	 *
	 * @author 树先生 xhwsd@qq.com
	 */
	public static final class Events {

		public 更新器 update;
		volatile 更新信息 info;
		volatile int found;
		volatile int none;
		volatile int failed;
		volatile int paused;
		volatile int completed;
		volatile int cancelled;
		volatile int progress;
		volatile long bytes;
		volatile int percent;
		volatile String path = "";
		volatile String message = "";

		/**
		 * 记录版本检查结果。
		 *
		 * @param value 发现更新的真实信息。
		 */
		public void update$发现更新(更新信息 value) {
			info = value;
			found++;
		}

		/**
		 * 记录无需更新。
		 */
		public void update$无需更新() {
			none++;
		}

		/**
		 * 记录真实下载进度。
		 *
		 * @param received 已下载字节数。
		 * @param total 总字节数。
		 * @param value 百分比。
		 */
		public void update$下载进度(long received, long total, int value) {
			bytes = received;
			percent = value;
			progress++;
		}

		/**
		 * 记录通过校验的下载结果。
		 *
		 * @param value 已校验文件。
		 */
		public void update$下载完成(String value) {
			path = value;
			completed++;
		}

		/**
		 * 记录失败阶段和原因。
		 *
		 * @param stage 失败阶段。
		 * @param value 失败原因。
		 */
		public void update$失败(int stage, String value) {
			message = value;
			failed++;
		}

		/**
		 * 记录原生停止确认。
		 */
		public void update$已暂停() {
			paused++;
		}

		/**
		 * 记录显式取消。
		 */
		public void update$已取消() {
			cancelled++;
		}
	}
}
