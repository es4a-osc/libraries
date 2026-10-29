package simple.library.wsd.xui;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import android.app.Instrumentation;
import android.graphics.Color;
import android.os.SystemClock;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.xuexiang.xui.R;
import com.xuexiang.xui.widget.dialog.bottomsheet.BottomSheet;
import com.xuexiang.xui.widget.guidview.GuideCaseView;

import simple.runtime.android.MainActivity;
import simple.runtime.components.组件;
import simple.runtime.components.impl.android.面板Impl;
import simple.runtime.errors.运行错误;
import simple.runtime.events.EventDispatcher;
import simple.runtime.variants.IntegerVariant;

/**
 * 在真实 Activity 中检查滚动定位、富文本点击、菜单快照及引导推进的边界行为。
 *
 * @author 树先生 xhwsd@qq.com
 */
final class XuiInteractionChecks {

	private final Instrumentation instrumentation;
	private final MainActivity activity;
	private final Events events = new Events();
	private 面板Impl container;
	private XUI滚动容器基类 scroll;
	private View scrollContent;
	private XUI圆角按钮Impl anchor;
	private int assertions;

	/**
	 * 复用现有测试宿主。
	 *
	 * @param instrumentation 当前测试运行器。
	 * @param activity 当前 Simple Activity。
	 */
	XuiInteractionChecks(Instrumentation instrumentation, MainActivity activity) {
		this.instrumentation = instrumentation;
		this.activity = activity;
	}

	/**
	 * 运行全部新增场景；原生异步回调在界面线程之外等待。
	 *
	 * @return 实际执行成功的断言数。
	 * @throws Exception 设备验证失败。
	 */
	int run() throws Exception {
		onUi(new Runnable() {
			@Override
			public void run() {
				prepare();
			}
		});
		settle();
		verifyScrolls();
		onUi(new Runnable() {
			@Override
			public void run() {
				Log.i("XUI-INTERACTION", "rich-start");
				verifyRichText();
				showMenu();
			}
		});
		settle();
		Log.i("XUI-INTERACTION", "menu-start");
		verifyMenu();
		Log.i("XUI-INTERACTION", "guide-start");
		verifyGuide();
		verifyGuideTouchAdvance();
		onUi(new Runnable() {
			@Override
			public void run() {
				events.menu.销毁();
				events.guide.销毁();
				events.rich.销毁();
				anchor.销毁();
				EventDispatcher.unregisterEvent(events);
			}
		});
		return assertions;
	}

	/**
	 * 设置独立可见根容器并登记真实 Simple 事件。
	 */
	private void prepare() {
		container = new 面板Impl(null) {
			@Override
			protected void addToContainer() {
				// 根容器由回归宿主直接承载。
			}
		};
		container.布局(IntegerVariant.getIntegerVariant(组件.布局_单帧));
		activity.setContentView(container.getView());
		anchor = new XUI圆角按钮Impl(container);
		anchor.文本("引导目标");
		FrameLayout.LayoutParams layout = new FrameLayout.LayoutParams(400, 100);
		layout.leftMargin = 40;
		layout.topMargin = 200;
		anchor.getView().setLayoutParams(layout);
		events.menu = new XUI底部菜单Impl(container);
		events.menu.初始化();
		events.rich = new XUI富文本工具Impl(container);
		events.rich.初始化();
		events.guide = new XUI引导Impl(container);
		events.guide.初始化();
		for (String name : new String[] { "项目被单击", "已取消", "已关闭" }) {
			EventDispatcher.registerEvent(events, "menu", name);
		}
		EventDispatcher.registerEvent(events, "rich", "片段被单击");
		for (String name : new String[] { "步骤已显示", "步骤已关闭", "引导已完成", "引导已跳过" }) {
			EventDispatcher.registerEvent(events, "guide", name);
		}
	}

	/**
	 * 五种滚动视图都覆盖布局前到底、目标合并、边界限制及销毁后请求。
	 *
	 * @throws Exception 界面等待失败。
	 */
	private void verifyScrolls() throws Exception {
		for (int i = 0; i < 5; i++) {
			final int kind = i;
			Log.i("XUI-INTERACTION", "scroll-start:" + kind);
			onUi(new Runnable() {
				@Override
				public void run() {
					switch (kind) {
					case 0:
						scroll = new XUI滚动框Impl(container);
						break;
					case 1:
						scroll = new XUI自适应滚动框Impl(container);
						break;
					case 2:
						scroll = new XUI键盘滚动框Impl(container);
						break;
					case 3:
						scroll = new XUI吸顶滚动框Impl(container);
						break;
					default:
						scroll = new XUI联动滚动框Impl(container);
						break;
					}
					scroll.getView().setLayoutParams(new FrameLayout.LayoutParams(600, 160));
					scrollContent = new TextView(activity);
					scrollContent.setMinimumHeight(900);
					((ViewGroup) scroll.getView()).addView(scrollContent, new FrameLayout.LayoutParams(600, 900));
					scroll.滚动到底(false);
				}
			});
			settle();
			onUi(new Runnable() {
				@Override
				public void run() {
					check(scroll.取最大滚动偏移() > 0, "内容形成实际滚动范围 kind=" + kind + " view=" + scroll.getView().getWidth() + "x" + scroll.getView().getHeight() + " child=" + scrollContent.getHeight() + " bottom=" + scrollContent.getBottom() + " root=" + container.getView().getHeight());
					check(scroll.取滚动偏移() == scroll.取最大滚动偏移(), "布局前到底使用布局后尺寸");
					scroll.滚动到(Integer.MAX_VALUE, false);
					scroll.滚动到(-1, false);
				}
			});
			settle();
			onUi(new Runnable() {
				@Override
				public void run() {
					check(scroll.取滚动偏移() == 0, "只执行最后一次目标并限制负数");
					scroll.滚动到(180, false);
				}
			});
			settle();
			onUi(new Runnable() {
				@Override
				public void run() {
					check(scroll.取滚动偏移() == 180, "精确像素定位");
					scrollContent.setMinimumHeight(1200);
					scrollContent.requestLayout();
					scroll.滚动到底(false);
				}
			});
			settle();
			onUi(new Runnable() {
				@Override
				public void run() {
					check(scroll.取滚动偏移() == scroll.取最大滚动偏移() && scroll.取滚动偏移() > 900, "内容增高后重新计算底部");
					scroll.滚动到顶(true);
				}
			});
			settle();
			onUi(new Runnable() {
				@Override
				public void run() {
					check(scroll.取滚动偏移() == 0, "平滑回到顶部");
					scroll.滚动到底(false);
					scroll.销毁();
					scroll.滚动到(200, false);
				}
			});
			settle();
			onUi(new Runnable() {
				@Override
				public void run() {
					check(scroll.取滚动偏移() == 0, "销毁撤销排队并忽略后续定位");
				}
			});
		}
	}

	/**
	 * 图片前后链接的区间、应用后值快照及关闭后的回调失效。
	 */
	private void verifyRichText() {
		events.rich.添加文本("前缀");
		events.rich.添加图片("icon.png");
		events.rich.添加文本("用户协议");
		events.rich.置链接("https://example.com/agreement");
		events.rich.应用到(anchor);
		TextView target = (TextView) anchor.getView();
		Spanned text = (Spanned) target.getText();
		ClickableSpan[] spans = text.getSpans(0, text.length(), ClickableSpan.class);
		check(spans.length == 1 && "用户协议".contentEquals(text.subSequence(text.getSpanStart(spans[0]), text.getSpanEnd(spans[0]))), "图片之后链接区间准确");
		check(target.getMovementMethod() instanceof LinkMovementMethod, "文本启用片段点击");
		events.rich.置点击标识("后来修改");
		spans[0].onClick(target);
		check(events.log.contains("link:https://example.com/agreement:用户协议"), "回传应用时网址和文字快照");
		events.rich.关闭();
		int count = events.log.size();
		spans[0].onClick(target);
		check(events.log.size() == count, "关闭后旧片段不再分发");
		events.rich.添加文本("新的协议");
		events.rich.置点击标识("agreement");
		events.rich.应用到(anchor);
		text = (Spanned) target.getText();
		text.getSpans(0, text.length(), ClickableSpan.class)[0].onClick(target);
		check(events.log.contains("link:agreement:新的协议"), "重建后新片段可以分发");
		events.rich.清空();
		events.rich.添加文本("普通文本");
		events.rich.应用到(anchor);
		check(target.getMovementMethod() == null, "普通文本移除链接移动处理");
	}

	/**
	 * 为本次菜单准备图标、红点、禁用和已选中项。
	 */
	private void showMenu() {
		events.log.clear();
		events.menu.显示列表("菜单验证", new String[] { "可用", "禁用", "已选" },
			new String[] { "icon.png", "", "" }, new boolean[] { true, false, false },
			new boolean[] { true, false, true }, 2);
	}

	/**
	 * 检查实际菜单行、无效输入原子性及选择、取消、关闭事件。
	 *
	 * @throws Exception 界面等待失败。
	 */
	private void verifyMenu() throws Exception {
		onUi(new Runnable() {
			@Override
			public void run() {
				BottomSheet sheet = currentSheet();
				ListView list = sheet.findViewById(R.id.listview);
				View first = list.getChildAt(0);
				View disabled = list.getChildAt(1);
				check(first.findViewById(R.id.bottom_dialog_list_item_img).getVisibility() == View.VISIBLE, "列表本地图标可见");
				View point = first.findViewById(R.id.bottom_dialog_list_item_point);
				check(point.getVisibility() == View.VISIBLE && point.getWidth() > 0 && point.getHeight() > 0, "列表红点实际占有可见尺寸");
				check(!disabled.isEnabled() && !list.getAdapter().isEnabled(1), "禁用项不允许选择");
				disabled.performClick();
				check(events.log.isEmpty() && events.menu.是否显示(), "禁用点击不通知且不关闭");
				try {
					events.menu.显示列表("错误", new String[] { "一" }, new String[0], new boolean[1], new boolean[1], -1);
					throw new AssertionError("接受了长度不一致的数组");
				} catch (运行错误 expected) {
					check(currentSheet() == sheet && sheet.isShowing(), "无效输入不关闭原菜单");
				}
				events.reopenMenu = true;
				first.performClick();
				check(events.log.contains("item:0:可用") && currentSheet() != sheet, "选择通知可重入显示新菜单");
			}
		});
		settle();
		onUi(new Runnable() {
			@Override
			public void run() {
				check(events.menu.是否显示() && !events.log.contains("cancel"), "旧关闭回调不关闭新菜单且选择不是取消");
				events.menu.关闭();
			}
		});
		settle();
		onUi(new Runnable() {
			@Override
			public void run() {
				events.log.clear();
				events.menu.置取消方式(false, true);
				events.menu.显示列表("取消验证", new String[] { "项目" }, -1);
				currentSheet().dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
				currentSheet().dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
				check(events.menu.是否显示(), "外部可取消时仍独立禁止返回键");
				currentSheet().cancel();
				currentSheet().cancel();
			}
		});
		settle();
		onUi(new Runnable() {
			@Override
			public void run() {
				check(events.log.size() == 2 && "cancel".equals(events.log.get(0)) && "close".equals(events.log.get(1)), "用户取消先取消后关闭且各一次");
				events.log.clear();
				events.menu.显示网格(new String[] { "图标" }, new String[] { "icon.png" }, 1);
				View cancel = currentSheet().findViewById(R.id.bottom_sheet_close_button);
				check(cancel.getVisibility() == View.VISIBLE, "网格取消按钮可见");
				cancel.performClick();
			}
		});
		settle();
		onUi(new Runnable() {
			@Override
			public void run() {
				check(events.log.size() == 2 && "cancel".equals(events.log.get(0)) && "close".equals(events.log.get(1)), "网格取消按钮也先取消后关闭");
			}
		});
	}

	/**
	 * 验证样式和会话快照、手动推进、跳过及步骤事件中的重开。
	 *
	 * @throws Exception 界面等待失败。
	 */
	private void verifyGuide() throws Exception {
		onUi(new Runnable() {
			@Override
			public void run() {
				events.log.clear();
				events.guide.添加步骤(anchor, "第一步", true);
				events.guide.添加步骤(anchor, "第二步", false);
				events.guide.置遮罩颜色(0x99000000);
				events.guide.置文字样式(Color.YELLOW, 22, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
				events.guide.置焦点样式(Color.CYAN, 3, 14, 10);
				events.guide.置点击推进(false);
				events.guide.显示();
			}
		});
		settle();
		onUi(new Runnable() {
			@Override
			public void run() {
				GuideCaseView first = currentGuide();
				check(events.guide.取当前步骤() == 0 && events.guide.是否显示(), "显示后当前步骤可查询");
				check(first.getFocusWidth() == anchor.getView().getWidth() + 28, "矩形焦点按像素向外扩展");
				int[] point = new int[2];
				anchor.getView().getLocationOnScreen(point);
				check(Math.abs(first.getFocusCenterY() - point[1] - anchor.getView().getHeight() / 2) <= 1, "焦点与目标屏幕位置一致");
				first.performClick();
				check(events.guide.取当前步骤() == 0, "关闭点击推进后点击不换步骤");
				events.guide.置点击推进(true);
				events.guide.下一步();
				check(events.guide.取当前步骤() == 1, "手动进入下一步");
				currentGuide().performClick();
				check(events.guide.取当前步骤() == 1, "中途修改不改变本次推进快照");
				events.guide.下一步();
				check(events.guide.取当前步骤() == -1 && events.log.contains("complete"), "最后一步完成后恢复未显示状态");
				events.guide.显示();
				events.guide.跳过();
				check(events.log.contains("skip:0") && !events.guide.是否显示(), "显式跳过回传当前步骤");
				events.guide.置点击推进(false);
				events.guide.显示();
				events.reopenGuide = true;
				events.guide.下一步();
				check(events.guide.取当前步骤() == 0, "步骤关闭事件重开后旧会话不推进");
				events.guide.关闭();
				check(!events.guide.是否显示(), "主动关闭幂等退出");
			}
		});
		settle();
	}

	/**
	 * 在屏幕高亮位置注入真实触摸，等待退出动画后验证推进、结束和关闭重入。
	 * 程序调用下一步会绕开原生 hide/退出动画，不能替代本项检查。
	 *
	 * @throws Exception 界面或触摸注入失败。
	 */
	private void verifyGuideTouchAdvance() throws Exception {
		for (final int duration : new int[] {0, 150}) {
			onUi(new Runnable() {
				@Override
				public void run() {
					events.guide.清空步骤();
					events.log.clear();
					events.guide.置点击推进(true);
					events.guide.置进出场时长(0, duration);
					events.guide.添加步骤(anchor, "触摸第一步", true);
					events.guide.添加步骤(anchor, "触摸第二步", true);
					events.guide.显示();
				}
			});
			settle();
			tapGuideTarget();
			settle();
			onUi(new Runnable() {
				@Override
				public void run() {
					check(events.guide.取当前步骤() == 1 && events.guide.是否显示(), "实际点击在退出动画后进入第二步：" + duration);
					check(events.log.equals(Arrays.asList("show:0", "step:0", "show:1")), "一次点击只关闭并推进一个步骤");
				}
			});
			tapGuideTarget();
			settle();
			onUi(new Runnable() {
				@Override
				public void run() {
					check(!events.guide.是否显示() && events.guide.取当前步骤() == -1, "第二次点击移除最终遮罩");
					check(events.log.equals(Arrays.asList("show:0", "step:0", "show:1", "step:1", "complete")), "真实点击的完成事件顺序且仅一次");
				}
			});
		}
		onUi(new Runnable() {
			@Override
			public void run() {
				events.log.clear();
				events.guide.置进出场时长(0, 350);
				events.guide.显示();
			}
		});
		settle();
		tapGuideTarget();
		onUi(new Runnable() {
			@Override
			public void run() {
				events.guide.关闭();
				events.guide.显示();
			}
		});
		settle();
		onUi(new Runnable() {
			@Override
			public void run() {
				check(events.guide.取当前步骤() == 0 && events.guide.是否显示(), "退出动画中关闭并重开后旧回调不推进新会话");
				check(events.log.equals(Arrays.asList("show:0", "show:0")), "关闭重开不产生旧步骤结束或完成事件");
				events.guide.关闭();
				events.guide.置进出场时长(0, 0);
				events.log.clear();
				events.guide.显示();
				events.reopenGuide = true;
			}
		});
		settle();
		tapGuideTarget();
		settle();
		onUi(new Runnable() {
			@Override
			public void run() {
				check(events.guide.取当前步骤() == 0 && events.guide.是否显示(), "真实触摸结束事件中重开后保留新步骤");
				check(events.log.equals(Arrays.asList("show:0", "step:0", "show:0")), "事件重入只产生新会话显示通知");
				events.guide.关闭();
			}
		});
	}

	/**
	 * 按实际目标的屏幕坐标点击，既不直接调用引导事件，也不跳过退出动画。
	 *
	 * @throws Exception 触摸注入失败。
	 */
	private void tapGuideTarget() throws Exception {
		final int[] position = new int[2];
		onUi(new Runnable() {
			@Override
			public void run() {
				anchor.getView().getLocationOnScreen(position);
				position[0] += anchor.getView().getWidth() / 2;
				position[1] += anchor.getView().getHeight() / 2;
			}
		});
		long down = SystemClock.uptimeMillis();
		MotionEvent press = MotionEvent.obtain(down, down, MotionEvent.ACTION_DOWN, position[0], position[1], 0);
		MotionEvent release = MotionEvent.obtain(down, down + 50, MotionEvent.ACTION_UP, position[0], position[1], 0);
		try {
			instrumentation.sendPointerSync(press);
			instrumentation.sendPointerSync(release);
		} finally {
			press.recycle();
			release.recycle();
		}
	}

	/**
	 * 读取私有会话仅用于验证原生界面，不让测试影响产品公开接口。
	 *
	 * @param object 被检查对象。
	 * @param name 字段名。
	 * @return 字段值。
	 */
	private Object field(Object object, String name) {
		try {
			Field field = object.getClass().getDeclaredField(name);
			field.setAccessible(true);
			return field.get(object);
		} catch (ReflectiveOperationException failure) {
			throw new AssertionError(failure);
		}
	}

	/**
	 * 取得当前菜单的原生对话框。
	 *
	 * @return 原生底部菜单。
	 */
	private BottomSheet currentSheet() {
		return (BottomSheet) field(field(events.menu, "session"), "sheet");
	}

	/**
	 * 取得当前步骤的原生覆盖层。
	 *
	 * @return 原生引导视图。
	 */
	private GuideCaseView currentGuide() {
		return (GuideCaseView) field(field(events.guide, "session"), "view");
	}

	/**
	 * 等待原生布局、平滑滚动和关闭通知。
	 *
	 * @throws InterruptedException 验证被中断。
	 */
	private void settle() throws InterruptedException {
		Thread.sleep(600);
		onUi(new Runnable() {
			@Override
			public void run() {
				// 同步到界面队列即可，避免把持续动画误认为界面无法验证。
			}
		});
	}

	/**
	 * 将界面线程失败传回运行器，禁止吞掉异步断言。
	 *
	 * @param action 本次操作。
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
	 * 只累计实际通过的检查。
	 *
	 * @param condition 检查条件。
	 * @param message 场景说明。
	 */
	private void check(boolean condition, String message) {
		if (!condition) {
			throw new AssertionError(message);
		}
		assertions++;
	}

	/**
	 * 真实 Simple 事件接收器，记录顺序并触发所需的重入场景。
	 */
	public static final class Events {
		public XUI底部菜单Impl menu;
		public XUI富文本工具Impl rich;
		public XUI引导Impl guide;
		final List<String> log = new ArrayList<String>();
		boolean reopenMenu;
		boolean reopenGuide;

		/**
		 * 记录菜单选择并按场景重开。
		 *
		 * @param index 项目索引。
		 * @param text 项目文字。
		 */
		public void menu$项目被单击(int index, String text) {
			log.add("item:" + index + ":" + text);
			if (reopenMenu) {
				reopenMenu = false;
				menu.显示列表("新菜单", new String[] { "新项目" }, -1);
			}
		}

		/**
		 * 记录取消。
		 */
		public void menu$已取消() {
			log.add("cancel");
		}

		/**
		 * 记录关闭。
		 */
		public void menu$已关闭() {
			log.add("close");
		}

		/**
		 * 记录富文本片段。
		 *
		 * @param tag 标识或网址。
		 * @param text 片段文字。
		 */
		public void rich$片段被单击(String tag, String text) {
			log.add("link:" + tag + ":" + text);
		}

		/**
		 * 记录步骤显示。
		 *
		 * @param index 步骤索引。
		 */
		public void guide$步骤已显示(int index) {
			log.add("show:" + index);
		}

		/**
		 * 在关闭步骤事件内重开引导，检查旧逻辑不再继续。
		 *
		 * @param index 步骤索引。
		 */
		public void guide$步骤已关闭(int index) {
			log.add("step:" + index);
			if (reopenGuide) {
				reopenGuide = false;
				guide.显示();
			}
		}

		/**
		 * 记录完成。
		 */
		public void guide$引导已完成() {
			log.add("complete");
		}

		/**
		 * 记录显式跳过。
		 *
		 * @param index 跳过时的步骤索引。
		 */
		public void guide$引导已跳过(int index) {
			log.add("skip:" + index);
		}
	}
}
