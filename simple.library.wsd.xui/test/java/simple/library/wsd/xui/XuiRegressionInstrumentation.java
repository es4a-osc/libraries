package simple.library.wsd.xui;

import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.xuexiang.xui.R;

import simple.runtime.android.MainActivity;
import simple.runtime.components.组件;
import simple.runtime.components.可视组件;
import simple.runtime.components.impl.android.面板Impl;
import simple.runtime.errors.运行错误;
import simple.runtime.events.EventDispatcher;
import simple.runtime.variants.IntegerVariant;

/**
 * 在正式 Simple 测试应用进程中验证校验、菜单、状态错误和系统识别结果分发。
 * 外部结果由测试专用 Activity 提供，不能据此认定设备具备真实语音识别能力。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XuiRegressionInstrumentation extends Instrumentation {

	private MainActivity activity;
	private 面板Impl container;
	private final Events events = new Events();
	private int assertions;
	private boolean completeOnly;

	@Override
	public void onCreate(Bundle arguments) {
		super.onCreate(arguments);
		completeOnly = arguments != null && "complete".equals(arguments.getString("suite"));
		start();
	}

	@Override
	public void onStart() {
		Bundle result = new Bundle();
		try {
			Intent launch = new Intent().setComponent(new ComponentName("simple.library.wsd.xui.test",
				"simple.runtime.android.MainActivity")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			activity = (MainActivity) startActivitySync(launch);
			waitForIdleSync();
			if (completeOnly) {
				assertions = new XuiCompleteChecks(this, activity).run();
				assertions += new XuiGoalChecks(this, activity).run();
				result.putString("stream", "XUI_COMPLETION_PASS assertions=" + assertions);
				finish(Activity.RESULT_OK, result);
				return;
			}
			onUi(new Runnable() {
				@Override
				public void run() {
					prepareContainer();
					verifyValidation();
					verifyMenu();
					verifyStateError();
					events.search = new XUI搜索框Impl(container);
					events.search.高度(IntegerVariant.getIntegerVariant(160));
					events.search.宽度(IntegerVariant.getIntegerVariant(组件.长度_匹配父级));
					EventDispatcher.registerEvent(events, "search", "搜索提交");
					EventDispatcher.registerEvent(events, "search", "语音搜索已取消");
					EventDispatcher.registerEvent(events, "search", "语音搜索失败");
					events.search.展开搜索(false);
				}
			});
			waitForIdleSync();
			verifyVoice("success");
			verifyVoice("cancel");
			verifyVoice("empty");
			verifyVoice("malformed");
			verifyVoice("duplicate");
			verifyVoice("icon");
			verifyVoice("disable");
			verifyVoice("detach");
			// 恢复刚被移出的视图，再检查销毁期间尚未返回的请求。
			onUi(new Runnable() {
				@Override
				public void run() {
					((ViewGroup) container.getView()).addView(events.search.getView());
				}
			});
			waitForIdleSync();
			verifyVoice("destroy");
			onUi(new Runnable() {
				@Override
				public void run() {
					int before = listenerCount();
					events.search.销毁();
					events.search.销毁();
					check(listenerCount() == before, "重复销毁不误删其它实例的监听");
					EventDispatcher.unregisterEvent(events);
				}
			});
			assertions += new XuiCompletionChecks(this, activity).run();
			assertions += new XuiInteractionChecks(this, activity).run();
			assertions += new XuiCompleteChecks(this, activity).run();
			assertions += new XuiGoalChecks(this, activity).run();
			result.putString("stream", "XUI_REGRESSION_PASS assertions=" + assertions
				+ " validation/menu/state/voice-success/cancel/empty/malformed/duplicate/icon/disable/detach/destroy/dialog/pickers/lists/scroll/richtext/bottommenu/guide");
			finish(Activity.RESULT_OK, result);
		} catch (Throwable failure) {
			result.putString("stream", "XUI_REGRESSION_FAIL " + android.util.Log.getStackTraceString(failure));
			finish(Activity.RESULT_CANCELED, result);
		}
	}

	/**
	 * 使用 Simple 原生容器协议，将组件置于真实 Activity 的可见视图树。
	 */
	private void prepareContainer() {
		container = new 面板Impl(null) {
			@Override
			protected void addToContainer() {
				// 验证根容器直接归 Activity，不加入其它 Simple 窗口。
			}
		};
		container.布局(IntegerVariant.getIntegerVariant(组件.布局_线性));
		activity.setContentView(container.getView());
	}

	/**
	 * 核对组合规则、空错误文本、清空状态与非法表达式的失败原子性。
	 */
	private void verifyValidation() {
		XUI校验编辑框Impl input = new XUI校验编辑框Impl(container);
		input.文本("");
		input.添加必填规则("必填");
		check(!input.校验() && "必填".equals(input.取错误消息()), "必填规则");
		input.添加长度规则(2, 4, "长度");
		input.添加正则规则("[A-Z]+", "格式");
		input.文本("ab");
		check(!input.校验() && "格式".equals(input.取错误消息()), "正则整段匹配");
		try {
			input.添加正则规则("[", "无效");
			throw new AssertionError("非法表达式被接受");
		} catch (IllegalArgumentException expected) {
			assertions++;
		}
		input.文本("AB");
		check(input.校验() && input.是否输入有效(), "非法表达式不污染已有规则");
		input.文本("ABCDE");
		check(!input.校验() && "长度".equals(input.取错误消息()), "规则按顺序失败");
		input.清空校验规则();
		check(input.是否输入有效() && "".equals(input.取错误消息()), "清空规则和错误外观");
		input.销毁();
	}

	/**
	 * 实际点击原生菜单标签，核对关闭及再次展开均可用。
	 */
	private void verifyMenu() {
		XUI下拉菜单Impl menu = new XUI下拉菜单Impl(container);
		XUI基础文本Impl choice = new XUI基础文本Impl(container);
		XUI基础文本Impl body = new XUI基础文本Impl(container);
		menu.置菜单(new String[] { "筛选" }, new 可视组件[] { choice }, body);
		ViewGroup nativeMenu = (ViewGroup) menu.getView();
		View tab = ((ViewGroup) nativeMenu.getChildAt(0)).getChildAt(0);
		tab.performClick();
		check(menu.是否显示中(), "原生标签展开菜单");
		menu.置标签菜单文本("已选");
		menu.关闭菜单();
		menu.关闭菜单();
		check(!menu.是否显示中(), "关闭可重复调用");
		tab.performClick();
		check(menu.是否显示中(), "关闭后可以重新展开");
		menu.关闭菜单();
		menu.销毁();
		choice.销毁();
		body.销毁();
	}

	/**
	 * 未初始化或不可见的弹层调用应转换成 Simple 可处理的错误。
	 */
	private void verifyStateError() {
		XUI对话框Impl dialog = new XUI对话框Impl(container);
		try {
			dialog.显示提示("不应显示", "", "确定", "");
			throw new AssertionError("无所属可见窗口仍显示弹层");
		} catch (运行错误 expected) {
			check(运行错误.convertToRuntimeError(expected) == expected, "Simple 错误转换保留原错误");
			check(expected.getMessage().contains("窗口"), "保留状态错误的具体原因");
		} finally {
			dialog.销毁();
		}
	}

	/**
	 * 通过真实外部 Activity 往返测试回填、取消、空结果和迟到回调。
	 *
	 * @param mode 本次识别结果或竞态场景。
	 * @throws Exception 配置测试提供者或等待结果失败时抛出。
	 */
	private void verifyVoice(final String mode) throws Exception {
		String providerMode = "cancel".equals(mode) || "empty".equals(mode) || "malformed".equals(mode) ? mode : "success";
		try (ParcelFileDescriptor command = getUiAutomation().executeShellCommand(
			"am start -W -n simple.library.wsd.xui.regression/simple.library.wsd.xui.VoiceResultActivity --es configure " + providerMode);
			FileInputStream stream = new FileInputStream(command.getFileDescriptor())) {
			byte[] buffer = new byte[1024];
			while (stream.read(buffer) != -1) {
				// 读完 am 的输出，确保测试提供者已保存配置。
			}
		}
		waitForIdleSync();
		final AtomicReference<Boolean> resumed = new AtomicReference<Boolean>(false);
		long resumeDeadline = SystemClock.elapsedRealtime() + 5000;
		do {
			Thread.sleep(100);
			onUi(new Runnable() {
				@Override
				public void run() {
					resumed.set(activity.hasWindowFocus());
				}
			});
		} while (!resumed.get() && SystemClock.elapsedRealtime() < resumeDeadline);
		check(resumed.get(), "配置识别结果后目标 Activity 已重新获得焦点");
		events.reset();
		onUi(new Runnable() {
			@Override
			public void run() {
				events.search.置语音搜索(true);
				events.search.置搜索词("icon".equals(mode) ? "" : "原搜索词", false);
				check(events.search.是否支持语音搜索(), "识别提供者对应用可见");
				if ("icon".equals(mode)) {
					View button = events.search.getView().findViewById(R.id.action_voice_btn);
					check(button.getVisibility() == View.VISIBLE, "启用后空输入显示语音按钮");
					check(button.performClick(), "点击原生语音按钮启动识别");
				} else {
					check(events.search.开始语音搜索(), "启动识别 " + mode);
				}
				if ("disable".equals(mode)) {
					events.search.置语音搜索(false);
				} else if ("detach".equals(mode)) {
					((ViewGroup) events.search.getView().getParent()).removeView(events.search.getView());
				} else if ("destroy".equals(mode)) {
					int count = listenerCount();
					events.search.销毁();
					check(listenerCount() == count - 1, "销毁释放结果监听");
				} else if ("duplicate".equals(mode)) {
					check(!events.search.开始语音搜索() && events.failed == 1, "拒绝重复启动但保留当前请求");
					events.search.onActivityResult(123, Activity.RESULT_CANCELED, null);
					check(events.cancelled == 0, "忽略其它组件的结果");
				}
			}
		});
		if ("disable".equals(mode) || "detach".equals(mode) || "destroy".equals(mode)) {
			Thread.sleep(1200);
		} else {
			// 辅助 Activity 冷启动耗时不固定，等待实际结果，不能用单次固定延迟认定失败。
			final AtomicReference<Boolean> returned = new AtomicReference<Boolean>(false);
			long deadline = SystemClock.elapsedRealtime() + 5000;
			do {
				Thread.sleep(100);
				onUi(new Runnable() {
					@Override
					public void run() {
						if ("success".equals(mode) || "duplicate".equals(mode) || "icon".equals(mode)) {
							returned.set(events.submitted > 0);
						} else if ("cancel".equals(mode)) {
							returned.set(events.cancelled > 0);
						} else {
							returned.set(events.failed > 0);
						}
					}
				});
			} while (!returned.get() && SystemClock.elapsedRealtime() < deadline);
		}
		waitForIdleSync();
		onUi(new Runnable() {
			@Override
			public void run() {
				EditText input = events.search.getView().findViewById(R.id.searchTextView);
				if ("success".equals(mode) || "duplicate".equals(mode) || "icon".equals(mode)) {
					check(events.submitted == 1 && "语音回填验证".equals(input.getText().toString()),
						"成功结果回填并提交一次：mode=" + mode + ", submitted=" + events.submitted
						+ ", cancelled=" + events.cancelled + ", failed=" + events.failed + ", input=" + input.getText());
				} else if ("cancel".equals(mode)) {
					check(events.cancelled == 1 && events.submitted == 0, "取消事件且不提交");
				} else if ("empty".equals(mode) || "malformed".equals(mode)) {
					check(events.failed == 1 && events.submitted == 0, "空结果报告失败");
				} else {
					check(events.submitted == 0 && events.cancelled == 0 && events.failed == 0, "迟到结果被丢弃 " + mode);
				}
				if (!"success".equals(mode) && !"duplicate".equals(mode) && !"icon".equals(mode)) {
					check("原搜索词".equals(input.getText().toString()), "非成功结果保留原搜索词");
				}
			}
		});
	}

	/**
	 * 检查监听数量，确认释放没有误删其它组件的监听。
	 *
	 * @return 当前宿主结果监听总数。
	 */
	private int listenerCount() {
		try {
			Field field = MainActivity.class.getDeclaredField("onActivityResultListeners");
			field.setAccessible(true);
			return ((List<?>) field.get(activity)).size();
		} catch (ReflectiveOperationException failure) {
			throw new AssertionError(failure);
		}
	}

	/**
	 * 在界面线程运行检查，把失败带回测试线程，避免使目标应用直接崩溃。
	 *
	 * @param action 界面操作或断言。
	 */
	private void onUi(final Runnable action) {
		final AtomicReference<Throwable> error = new AtomicReference<Throwable>();
		runOnMainSync(new Runnable() {
			@Override
			public void run() {
				try {
					action.run();
				} catch (Throwable failure) {
					error.set(failure);
				}
			}
		});
		if (error.get() != null) {
			throw new AssertionError(error.get());
		}
	}

	/**
	 * 记录真实执行的断言，任何失败立即终止本次回归。
	 *
	 * @param condition 实际条件。
	 * @param message 断言含义。
	 */
	private void check(boolean condition, String message) {
		if (!condition) {
			throw new AssertionError(message);
		}
		assertions++;
	}

	/**
	 * 按 Simple 实际事件反射约定记录回调次数，防止只验证视图文本。
	 */
	public static final class Events {
		public XUI搜索框Impl search;
		volatile int submitted;
		volatile int cancelled;
		volatile int failed;

		/**
		 * 清零当前场景的事件计数。
		 */
		void reset() {
			submitted = 0;
			cancelled = 0;
			failed = 0;
		}

		/**
		 * 记录实际提交。
		 *
		 * @param text 实际提交的识别文字。
		 */
		public void search$搜索提交(String text) {
			submitted++;
		}

		/**
		 * 记录用户取消事件。
		 */
		public void search$语音搜索已取消() {
			cancelled++;
		}

		/**
		 * 记录识别失败。
		 *
		 * @param message 组件报告的失败原因。
		 */
		public void search$语音搜索失败(String message) {
			failed++;
		}
	}
}
