package simple.library.wsd.xui;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import android.app.Instrumentation;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.xuexiang.xui.R;
import com.xuexiang.xui.widget.dialog.materialdialog.DialogAction;
import com.xuexiang.xui.widget.dialog.materialdialog.MaterialDialog;

import simple.runtime.android.MainActivity;
import simple.runtime.components.组件;
import simple.runtime.components.impl.android.面板Impl;
import simple.runtime.errors.运行错误;
import simple.runtime.events.EventDispatcher;
import simple.runtime.parameters.BooleanReferenceParameter;
import simple.runtime.variants.IntegerVariant;

/**
 * 在真实目标应用中检查对话框归属、确认校验、选择器事件与列表数据事务。
 *
 * @author 树先生 xhwsd@qq.com
 */
final class XuiCompletionChecks {

	private final Instrumentation instrumentation;
	private final MainActivity activity;
	private final Events events = new Events();
	private 面板Impl container;
	private XUI线性容器Impl content;
	private int assertions;

	/**
	 * 复用现有测试宿主，不另外创建 Simple 项目或修改运行库。
	 *
	 * @param instrumentation 当前测试运行器。
	 * @param activity 当前 Simple 宿主。
	 */
	XuiCompletionChecks(Instrumentation instrumentation, MainActivity activity) {
		this.instrumentation = instrumentation;
		this.activity = activity;
	}

	/**
	 * 执行新增能力的界面回归，任一步失败均中断。
	 *
	 * @return 实际通过的断言数。
	 * @throws Exception 反射、界面等待或验证失败时抛出。
	 */
	int run() throws Exception {
		onUi(new Runnable() {
			@Override
			public void run() {
				prepare();
				verifyLists();
				verifyInput();
			}
		});
		settle();
		onUi(new Runnable() {
			@Override
			public void run() {
				verifyCustomContent();
			}
		});
		settle();
		verifyCancellation();
		verifyPickers();
		onUi(new Runnable() {
			@Override
			public void run() {
				events.dialog.销毁();
				events.time.销毁();
				events.options.销毁();
				content.销毁();
				EventDispatcher.unregisterEvent(events);
			}
		});
		return assertions;
	}

	/**
	 * 建立真实可见容器并按 Simple 反射约定登记事件。
	 */
	private void prepare() {
		container = new 面板Impl(null) {
			@Override
			protected void addToContainer() {
				// 回归根容器直接交给 Activity 承载。
			}
		};
		container.布局(IntegerVariant.getIntegerVariant(组件.布局_线性));
		activity.setContentView(container.getView());
		content = new XUI线性容器Impl(container);
		content.getView().setVisibility(View.GONE);
		events.dialog = new XUI对话框Impl(container);
		events.dialog.初始化();
		events.time = new XUI日期时间选择器Impl(container);
		events.time.初始化();
		events.options = new XUI多级选择器Impl(container);
		events.options.初始化();
		for (String name : new String[] { "确认前校验", "输入已确认", "已取消", "已关闭" }) {
			EventDispatcher.registerEvent(events, "dialog", name);
		}
		for (String name : new String[] { "日期已确认", "已取消", "已关闭" }) {
			EventDispatcher.registerEvent(events, "time", name);
		}
		for (String name : new String[] { "选择已确认", "已取消", "已关闭" }) {
			EventDispatcher.registerEvent(events, "options", name);
		}
	}

	/**
	 * 两种一维列表执行同一组事务断言，避免只检查新增接口是否存在。
	 */
	private void verifyLists() {
		XUI联动列表Impl linked = new XUI联动列表Impl(container);
		XUI自适应列表Impl adaptive = new XUI自适应列表Impl(container);
		for (XUI列表 list : new XUI列表[] { linked, adaptive }) {
			String[] names = { "甲", "乙", "丙" };
			list.置项目数组(names);
			names[0] = "外部修改";
			check("甲".equals(list.取项目文本(0)), "输入数组独立复制");
			list.置选中项目(2);
			list.移除项目(0);
			check(list.取选中项目() == 1, "删除前项后选择仍指向原项目");
			list.置项目文本(1, "更新丙");
			check(list.取选中项目() == 1 && "更新丙".equals(list.取项目文本(1)), "单项更新保留选择");
			try {
				list.移除项目(9);
				throw new AssertionError("接受越界删除");
			} catch (运行错误 expected) {
				check(list.取项目数() == 2 && list.取选中项目() == 1, "越界删除不改变数据");
			}
			list.添加项目("新增");
			check(list.取项目数() == 3, "末尾添加");
			list.移除项目(1);
			check(list.取选中项目() == -1, "删除已选项后恢复未选");
			list.滚动到项目(0, false);
			list.清空项目();
			check(list.取项目数() == 0 && list.取选中项目() == -1, "清空数据和选择");
		}
		adaptive.置图文项目(new String[] { "标题" }, new String[] { "说明" }, new String[] { "" });
		adaptive.置选中项目(0);
		try {
			adaptive.置图文项目(new String[] { "错误" }, new String[0], new String[0]);
			throw new AssertionError("接受不同长度的图文数组");
		} catch (运行错误 expected) {
			check("标题".equals(adaptive.取项目文本(0)) && adaptive.取选中项目() == 0, "图文全量更新失败保持原数据");
		}
		adaptive.置项目文本(0, "新标题");
		check(adaptive.取选中项目() == 0, "图文标题更新保留选择");
		adaptive.置图文项目内容(0, "完整更新", "新说明", "");
		adaptive.添加图文项目("新增", "说明", "");
		check(adaptive.取项目数() == 2, "完整更新和添加图文");
		XUI自适应分组列表Impl groups = new XUI自适应分组列表Impl(container);
		groups.添加分组("甲组", new String[] { "一", "二" });
		groups.添加分组("乙组", new String[] { "三", "四" });
		groups.置选中项目(1, 1);
		groups.移除分组(0);
		check(groups.取选中分组() == 0 && groups.取选中项目() == 1, "删除分组后选择随项目移动");
		groups.移除项目(0, 0);
		check(groups.取选中项目() == 0 && "四".equals(groups.取项目文本(0, 0)), "删除组内前项后调整选择");
		groups.更新分组(0, "更新组", new String[] { "新" });
		check(groups.取选中项目() == -1, "更新已选分组后恢复未选");
		groups.添加项目(0, "追加");
		groups.滚动到项目(0, 1, false);
		groups.置选中项目(0, 1);
		groups.置项目文本(0, 1, "更新追加");
		check(groups.取项目数(0) == 2 && groups.取选中项目() == 1, "分组增改与定位");
		try {
			groups.更新分组(0, "错误", new String[] { null });
			throw new AssertionError("接受空引用项目");
		} catch (运行错误 expected) {
			check(groups.取项目数(0) == 2 && groups.取选中项目() == 1, "更新分组失败不改变原数据");
		}
		groups.清空分组();
		check(groups.取分组数() == 0 && groups.取选中分组() == -1, "清空分组及选择");
		linked.销毁();
		adaptive.销毁();
		groups.销毁();
	}

	/**
	 * 检查输入类型、长度按钮状态、确认阻止与成功提交后的事件。
	 */
	private void verifyInput() {
		events.log.clear();
		events.dialog.置输入类型(InputType.TYPE_CLASS_NUMBER);
		events.dialog.置输入长度(2, 4);
		events.dialog.显示输入("输入验证", "数字", "12", false);
		MaterialDialog dialog = currentDialog();
		check((dialog.getInputEditText().getInputType() & InputType.TYPE_MASK_CLASS) == InputType.TYPE_CLASS_NUMBER, "自定义输入类型");
		dialog.getInputEditText().setText("1");
		check(!dialog.getActionButton(DialogAction.POSITIVE).isEnabled(), "长度不足禁用确认");
		dialog.getInputEditText().setText("12");
		check(dialog.getActionButton(DialogAction.POSITIVE).isEnabled(), "有效长度恢复确认");
		events.block = true;
		dialog.getActionButton(DialogAction.POSITIVE).performClick();
		check(events.dialog.是否显示() && dialog.getInputEditText().getError() != null, "确认前校验阻止提交并显示错误");
		check(!events.log.contains("input"), "阻止后不发送成功结果");
		events.block = false;
		events.reopen = true;
		dialog.getActionButton(DialogAction.POSITIVE).performClick();
		check(events.log.contains("input"), "确认通过后发送结果");
		check(events.dialog.是否显示() && currentDialog() != dialog, "结果事件重新打开的新界面不被旧回调关闭");
		check(currentDialog().getActionButton(DialogAction.NEGATIVE).getVisibility() == View.GONE, "空取消文字不生成空按钮");
		events.dialog.关闭();
		events.dialog.置输入长度(0, -1);
	}

	/**
	 * 自定义内容可预先隐藏，取消及重进后恢复原顺序、可见性和参数对象。
	 */
	private void verifyCustomContent() {
		View view = content.getView();
		ViewGroup parent = (ViewGroup) view.getParent();
		ViewGroup.LayoutParams parameters = view.getLayoutParams();
		int index = parent.indexOfChild(view);
		events.dialog.显示自定义("自定义", content, "确认", "取消");
		check(view.getParent() != parent && view.getVisibility() == View.VISIBLE, "隐藏内容借入后可见");
		currentDialog().getActionButton(DialogAction.NEGATIVE).performClick();
		check(view.getParent() == parent && parent.indexOfChild(view) == index, "取消恢复父级与顺序");
		check(view.getLayoutParams() == parameters && view.getVisibility() == View.GONE, "取消恢复参数及隐藏状态");
		events.dialog.显示自定义("再次显示", content, "确认", "取消");
		try {
			events.dialog.显示自定义("无效根", container, "确认", "取消");
			throw new AssertionError("借出了窗口根");
		} catch (运行错误 expected) {
			check(events.dialog.是否显示(), "无效自定义内容不关闭现有界面");
		}
		events.dialog.关闭();
		check(view.getParent() == parent && view.getVisibility() == View.GONE, "程序关闭同样恢复隐藏内容");
	}

	/**
	 * 返回键与外部取消独立设置，程序关闭不报告用户取消。
	 *
	 * @throws Exception 界面等待或验证失败时抛出。
	 */
	private void verifyCancellation() throws Exception {
		onUi(new Runnable() {
			@Override
			public void run() {
				events.dialog.置取消方式(false, true);
				events.dialog.显示提示("取消方式", "", "确认", "取消");
				MaterialDialog dialog = currentDialog();
				dialog.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
				dialog.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
				check(events.dialog.是否显示(), "允许外部取消时仍可独立禁止返回键");
				events.log.clear();
				MotionEvent outside = MotionEvent.obtain(0, 0, MotionEvent.ACTION_OUTSIDE, -10, -10, 0);
				try {
					dialog.onTouchEvent(outside);
				} finally {
					outside.recycle();
				}
			}
		});
		settle();
		onUi(new Runnable() {
			@Override
			public void run() {
				check(events.log.size() == 2 && "cancel".equals(events.log.get(0)) && "close".equals(events.log.get(1)), "用户取消先取消后关闭");
				events.dialog.置取消方式(false, false);
				events.dialog.显示提示("程序关闭", "", "确认", "取消");
				events.log.clear();
				events.dialog.关闭();
			}
		});
		settle();
		onUi(new Runnable() {
			@Override
			public void run() {
				check(events.log.size() == 1 && "close".equals(events.log.get(0)), "程序关闭不报告取消");
			}
		});
	}

	/**
	 * 原生选择器实际显示、取消和确认，检查旧关闭回调与新显示状态隔离。
	 *
	 * @throws Exception 界面等待失败时抛出。
	 */
	private void verifyPickers() throws Exception {
		onUi(new Runnable() {
			@Override
			public void run() {
				events.time.置标题("时间标题");
				events.time.置按钮文本("采用", "放弃");
				events.time.置字号(18, 20, 16);
				events.time.置文字颜色(0xFF123456, 0xFF777777);
				events.time.置分隔线颜色(0xFFCCCCCC);
				events.time.置取消方式(true, false);
				events.time.显示();
			}
		});
		settle();
		onUi(new Runnable() {
			@Override
			public void run() {
				TextView title = activity.findViewById(R.id.tvTitle);
				TextView submit = activity.findViewById(R.id.btnSubmit);
				check("时间标题".contentEquals(title.getText()) && "采用".contentEquals(submit.getText()), "选择器标题与按钮配置生效");
				events.log.clear();
				activity.findViewById(R.id.btnCancel).performClick();
			}
		});
		settle();
		onUi(new Runnable() {
			@Override
			public void run() {
				check(events.log.size() == 2 && "timeCancel".equals(events.log.get(0)) && "timeClose".equals(events.log.get(1)), "日期取消和关闭顺序");
				events.time.显示();
				events.time.关闭();
				events.time.显示();
			}
		});
		settle();
		onUi(new Runnable() {
			@Override
			public void run() {
				check(events.time.是否显示(), "旧日期关闭回调不清除新实例");
				events.log.clear();
				activity.findViewById(R.id.btnSubmit).performClick();
			}
		});
		settle();
		onUi(new Runnable() {
			@Override
			public void run() {
				check(events.log.size() == 2 && "timeConfirmed".equals(events.log.get(0)) && "timeClose".equals(events.log.get(1)), "日期确认不误报取消");
				events.options.置独立项目(new String[] { "甲", "乙" }, new String[] { "一", "二" }, new String[0]);
				events.options.置默认选项(1, 1, 0);
				events.options.置标题("选项标题");
				events.options.置按钮文本("采用", "放弃");
				events.options.显示();
			}
		});
		settle();
		onUi(new Runnable() {
			@Override
			public void run() {
				check("选项标题".contentEquals(((TextView) activity.findViewById(R.id.tvTitle)).getText()), "多级选择器共用配置");
				events.log.clear();
				activity.findViewById(R.id.btnSubmit).performClick();
			}
		});
		settle();
		onUi(new Runnable() {
			@Override
			public void run() {
				check("乙".equals(events.options.取选中文本(0)) && "二".equals(events.options.取选中文本(1)), "确认提交默认路径");
				check(events.log.size() == 2 && "optionsConfirmed".equals(events.log.get(0)) && "optionsClose".equals(events.log.get(1)), "选项确认后仅关闭");
				events.options.显示();
				events.log.clear();
				events.options.关闭();
			}
		});
		settle();
		onUi(new Runnable() {
			@Override
			public void run() {
				check(events.log.size() == 1 && "optionsClose".equals(events.log.get(0)), "程序关闭选项不报告取消");
			}
		});
	}

	/**
	 * 读取当前会话对应的真实对话框，仅供回归使用，不进入产品接口。
	 *
	 * @return 当前原生对话框。
	 */
	private MaterialDialog currentDialog() {
		try {
			Field session = XUI对话框Impl.class.getDeclaredField("session");
			session.setAccessible(true);
			Object value = session.get(events.dialog);
			Field dialog = value.getClass().getDeclaredField("dialog");
			dialog.setAccessible(true);
			return (MaterialDialog) dialog.get(value);
		} catch (ReflectiveOperationException failure) {
			throw new AssertionError(failure);
		}
	}

	/**
	 * 等待原生退场动画及排队的关闭回调。
	 *
	 * @throws InterruptedException 测试被中断。
	 */
	private void settle() throws InterruptedException {
		Thread.sleep(600);
		instrumentation.waitForIdleSync();
	}

	/**
	 * 将界面断言失败带回运行器，不使用忽略错误的回调。
	 *
	 * @param action 本次界面操作。
	 */
	private void onUi(final Runnable action) {
		final AtomicReference<Throwable> error = new AtomicReference<Throwable>();
		instrumentation.runOnMainSync(new Runnable() {
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
	 * 记录实际执行的断言。
	 *
	 * @param condition 检查条件。
	 * @param message 条件含义。
	 */
	private void check(boolean condition, String message) {
		if (!condition) {
			throw new AssertionError(message);
		}
		assertions++;
	}

	/**
	 * 通过真实事件分发记录顺序，并验证传址阻止和确认结果重入。
	 */
	public static final class Events {
		public XUI对话框Impl dialog;
		public XUI日期时间选择器Impl time;
		public XUI多级选择器Impl options;
		final List<String> log = new ArrayList<String>();
		boolean block;
		boolean reopen;

		/**
		 * 按测试场景决定是否阻止确认。
		 *
		 * @param text 当前输入。
		 * @param cancel 传址取消标记。
		 */
		public void dialog$确认前校验(String text, BooleanReferenceParameter cancel) {
			cancel.set(block);
			if (block) {
				dialog.置输入错误("校验未通过");
			}
		}

		/**
		 * 记录确认，并按场景在回调内显示新界面。
		 *
		 * @param text 已确认的输入。
		 */
		public void dialog$输入已确认(String text) {
			log.add("input");
			if (reopen) {
				reopen = false;
				dialog.显示提示("新的对话框", "新会话应保留", "确定", "");
			}
		}

		/**
		 * 记录对话框用户取消。
		 */
		public void dialog$已取消() {
			log.add("cancel");
		}

		/**
		 * 记录对话框关闭。
		 */
		public void dialog$已关闭() {
			log.add("close");
		}

		/**
		 * 记录日期确认。
		 *
		 * @param time 已确认时间戳。
		 */
		public void time$日期已确认(long time) {
			log.add("timeConfirmed");
		}

		/**
		 * 记录日期取消。
		 */
		public void time$已取消() {
			log.add("timeCancel");
		}

		/**
		 * 记录日期关闭。
		 */
		public void time$已关闭() {
			log.add("timeClose");
		}

		/**
		 * 记录选项确认。
		 *
		 * @param a 第一列。
		 * @param b 第二列。
		 * @param c 第三列。
		 */
		public void options$选择已确认(int a, int b, int c) {
			log.add("optionsConfirmed");
		}

		/**
		 * 记录选项取消。
		 */
		public void options$已取消() {
			log.add("optionsCancel");
		}

		/**
		 * 记录选项关闭。
		 */
		public void options$已关闭() {
			log.add("optionsClose");
		}
	}
}
