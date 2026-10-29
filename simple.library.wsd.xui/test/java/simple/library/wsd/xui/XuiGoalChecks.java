package simple.library.wsd.xui;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import android.app.Instrumentation;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.MetricAffectingSpan;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.viewpager.widget.ViewPager;

import com.xuexiang.xui.R;
import com.xuexiang.xui.widget.banner.widget.banner.SimpleTextBanner;
import com.xuexiang.xui.widget.guidview.GuideCaseView;
import com.xuexiang.xui.widget.imageview.preview.view.BezierBannerView;
import com.xuexiang.xui.widget.popupwindow.bar.Es4aCookie;
import com.xuexiang.xui.widget.popupwindow.good.GoodView;
import com.xuexiang.xui.widget.tabbar.TabSegment;

import simple.runtime.android.MainActivity;
import simple.runtime.components.impl.android.面板Impl;
import simple.runtime.components.组件;
import simple.runtime.events.EventDispatcher;
import simple.runtime.variants.IntegerVariant;

/**
 * 验证本轮补齐的定时公告、引导、消息样式、轮播配置及动画清理。
 * 所有事件从真实视图操作或计时任务触发，不直接调用事件入口。
 *
 * @author 树先生 xhwsd@qq.com
 */
final class XuiGoalChecks {

	private final Instrumentation instrumentation;
	private final MainActivity activity;
	private final List<组件> created = new ArrayList<组件>();
	private final Events events = new Events();
	private 面板Impl root;
	private XUI圆角按钮Impl anchor;
	private XUI文本轮播Impl banner;
	private Drawable originalBackground;
	private GoodView floating;
	private XUI循环页面框Impl pages;
	private XUI贝塞尔指示器Impl bezier;
	private File pngOutput;
	private File jpegOutput;
	private int assertions;

	/**
	 * 复用现有测试应用及其界面线程。
	 */
	XuiGoalChecks(Instrumentation instrumentation, MainActivity activity) {
		this.instrumentation = instrumentation;
		this.activity = activity;
	}

	/**
	 * 分阶段等待真实布局、过期和动画任务，保留原始失败并统一清理组件。
	 *
	 * @return 成功断言数。
	 * @throws InterruptedException 测试等待被取消。
	 */
	int run() throws InterruptedException {
		boolean passed = false;
		try {
			onUi(new Runnable() {
				@Override
				public void run() {
					prepare();
				}
			});
			Thread.sleep(650);
			verifyTabGestures();
			onUi(new Runnable() {
				@Override
				public void run() {
					verifyBezier();
					verifyGroupRestoreOrder();
					verifyEditorStyleAndSave();
					verifyBanners();
					verifyGuide();
					verifyMessages();
					verifyNotice();
					prepareExpiryAndAnimation();
				}
			});
			Thread.sleep(1200);
			onUi(new Runnable() {
				@Override
				public void run() {
					verifyEditorOutput(pngOutput, "image/png");
					events.editor.置输出格式(XUI图片编辑.输出格式_JPEG, 60);
					events.editor.保存图片(jpegOutput.getAbsolutePath());
					check(events.reentrant.取公告数量() == 0 && events.reentrantStarts == 1, "公告开始事件中清空不会触发旧索引崩溃");
					check(events.timed.取公告数量() == 1, "定时公告真实过期后只保留永久消息");
					check(events.expired.equals("临时"), "过期事件携带原消息标识");
					check(events.timed.取公告标识(0).equals("永久"), "未过期消息标识保持");
					check(events.timed.按标识移除公告("永久") && !events.timed.按标识移除公告("永久"), "按标识删除及重复删除结果准确");
					check(anchor.getView().getBackground() == originalBackground, "背景动画结束归还原 Drawable");
					check(anchor.getView().getTag(simple.library.wsd.xui.R.id.es4a_xui_background_animation) == null, "背景动画结束释放任务标签");
					check(!floating.isShowing(), "飘字按配置时长自动消失");
					verifyDetach();
				}
			});
			Thread.sleep(850);
			onUi(new Runnable() {
				@Override
				public void run() {
					verifyEditorOutput(jpegOutput, "image/jpeg");
					check(events.expired.equals("临时"), "脱离窗口不再触发过期回调");
					((ViewGroup) root.getView()).addView(events.timed.getView());
				}
			});
			Thread.sleep(100);
			onUi(new Runnable() {
				@Override
				public void run() {
					check(events.timed.取公告数量() == 0 && events.expired.equals("离开"), "重新附着清理离开期间已过期的消息");
				}
			});
			passed = true;
			return assertions;
		} finally {
			try {
				onUi(new Runnable() {
					@Override
					public void run() {
						EventDispatcher.unregisterEvent(events);
						if (pngOutput != null) {
							pngOutput.delete();
						}
						if (jpegOutput != null) {
							jpegOutput.delete();
						}
						for (int index = created.size() - 1; index >= 0; index--) {
							created.get(index).销毁();
						}
					}
				});
			} catch (AssertionError failure) {
				if (passed) {
					throw failure;
				}
				android.util.Log.e("XUI-GOAL", "cleanup", failure);
			}
		}
	}

	/**
	 * 创建已附着的组件，后续才能验证依赖真实布局的行为。
	 */
	private void prepare() {
		root = track(new 面板Impl(null) {
			@Override
			protected void addToContainer() {
				// 测试根直接归宿主窗口。
			}
		});
		root.布局(IntegerVariant.getIntegerVariant(组件.布局_单帧));
		activity.setContentView(root.getView());
		anchor = track(new XUI圆角按钮Impl(root));
		anchor.文本("目标按钮");
		anchor.宽度(IntegerVariant.getIntegerVariant(200));
		anchor.高度(IntegerVariant.getIntegerVariant(60));
		banner = track(new XUI文本轮播Impl(root));
		banner.宽度(IntegerVariant.getIntegerVariant(300));
		banner.高度(IntegerVariant.getIntegerVariant(100));
		banner.置项目数组(new String[] {"第一页", "末页"});
		banner.置自动滚动启用(false);
		banner.开始轮播();
		events.editor = track(new XUI图片编辑Impl(root));
		events.editor.宽度(IntegerVariant.getIntegerVariant(300));
		events.editor.高度(IntegerVariant.getIntegerVariant(180));
		events.editor.置图片("icon.png");
		EventDispatcher.registerEvent(events, "editor", "保存完成");
		EventDispatcher.registerEvent(events, "editor", "保存失败");
		pngOutput = new File(activity.getFilesDir(), "xui-goal-editor.png");
		jpegOutput = new File(activity.getFilesDir(), "xui-goal-editor.jpg");
		pngOutput.delete();
		jpegOutput.delete();
		pages = track(new XUI循环页面框Impl(root));
		pages.宽度(IntegerVariant.getIntegerVariant(300));
		pages.高度(IntegerVariant.getIntegerVariant(120));
		pages.置页面数组(new String[] {"甲", "乙", "丙"});
		((ViewPager) pages.getView()).setCurrentItem(1, false);
		bezier = track(new XUI贝塞尔指示器Impl(root));
		bezier.宽度(IntegerVariant.getIntegerVariant(300));
		bezier.高度(IntegerVariant.getIntegerVariant(40));
		bezier.绑定页面框(pages);
		events.tabs = track(new XUI分段标签Impl(root));
		events.tabs.宽度(IntegerVariant.getIntegerVariant(300));
		events.tabs.高度(IntegerVariant.getIntegerVariant(56));
		events.tabs.置标签数组(new String[] {"栏目甲", "栏目乙"});
		EventDispatcher.registerEvent(events, "tabs", "标签被选择");
		EventDispatcher.registerEvent(events, "tabs", "标签被重复选择");
		EventDispatcher.registerEvent(events, "tabs", "标签被双击");
	}

	/**
	 * 栏内边距采用 dp，最后一页规则随真实翻页生效。
	 */
	private void verifyBanners() {
		banner.置栏边距(7, 8, 9, 10);
		SimpleTextBanner nativeBanner = (SimpleTextBanner) banner.getView();
		View bar = field(nativeBanner, "mLlBottomBar", View.class);
		float density = activity.getResources().getDisplayMetrics().density;
		check(bar.getPaddingLeft() == (int) (7 * density + 0.5f)
			&& bar.getPaddingBottom() == (int) (10 * density + 0.5f), "轮播栏内边距实际应用");
		banner.置末页栏显示(false);
		banner.置当前页(1, false);
		check(bar.getVisibility() == View.GONE, "末页规则隐藏真实栏");
		banner.置末页栏显示(true);
		check(bar.getVisibility() == View.VISIBLE, "当前末页切换规则立即恢复栏");
		banner.置末页栏显示(false);
		check(bar.getVisibility() == View.GONE, "当前末页切换规则立即隐藏栏");
		banner.置当前页(0, false);
		check(bar.getVisibility() == View.VISIBLE, "离开末页恢复真实栏");
		banner.置指示动画(XUI文本轮播.指示动画_旋转);
		banner.置当前页(1, false);
		check(field(nativeBanner, "mSelectAnimClass", Class.class).getSimpleName().equals("RotateEnter"), "指示动画使用上游旋转实现");
		banner.置指示动画(XUI文本轮播.指示动画_无);
		check(field(nativeBanner, "mSelectAnimClass", Class.class) == null, "指示动画可以关闭");
	}

	/**
	 * 聚焦区域按窗口内容坐标换算；首次记录只在实际显示后生成。
	 */
	private void verifyGuide() {
		XUI引导Impl guide = track(new XUI引导Impl(root));
		guide.初始化();
		guide.重置显示记录("设备回归");
		guide.置仅显示一次("设备回归");
		guide.置焦点动画(false);
		guide.置焦点偏移(4, 6);
		guide.置进出场时长(0, 0);
		guide.添加区域步骤(20, 30, 160, 80, "区域提示", true);
		guide.置步骤图片(0, android.R.drawable.ic_dialog_info, 32, 32, Gravity.BOTTOM, 2, 3);
		check(!guide.是否已显示("设备回归"), "未显示前不消费首次记录");
		guide.显示();
		check(guide.是否显示() && guide.是否已显示("设备回归"), "首次真实显示并登记");
		Object session = field(guide, "session", Object.class);
		GuideCaseView overlay = field(session, "view", GuideCaseView.class);
		int[] point = new int[2];
		int[] decor = new int[2];
		root.getView().getLocationOnScreen(point);
		activity.getWindow().getDecorView().getLocationOnScreen(decor);
		check(field(overlay, "mFocusPositionX", Integer.class) == point[0] - decor[0] + 104
			&& field(overlay, "mFocusPositionY", Integer.class) == point[1] - decor[1] + 76, "区域中心及偏移只换算一次");
		check(field(overlay, "mFocusRectangleWidth", Integer.class) == 160
			&& field(overlay, "mFocusRectangleHeight", Integer.class) == 80, "区域尺寸没有被锚点尺寸覆盖");
		check(field(overlay, "mPictureWidth", Integer.class) == 32
			&& !field(overlay, "mFocusAnimationEnabled", Boolean.class), "图片和焦点动画开关应用到原生覆盖层");
		guide.关闭();
		guide.显示();
		check(!guide.是否显示(), "同一标识不重复显示");
		guide.重置显示记录("设备回归");
		guide.显示();
		check(guide.是否显示(), "重置后可以再次显示");
		guide.关闭();
		guide.重置显示记录("设备回归");
	}

	/**
	 * 验证颜色值直接落到视图，飘字保留完整宽度及可配置的动画范围。
	 */
	private void verifyMessages() {
		events.tip = track(new XUI提示Impl(root));
		events.tip.初始化();
		EventDispatcher.registerEvent(events, "tip", "操作被单击");
		events.tip.置消息条颜色(Color.RED, Color.GREEN, Color.YELLOW, Color.CYAN);
		events.tip.置消息条边距(3, 4, 5, 6);
		events.tip.显示消息条("标题", "正文", "操作", 5000, false);
		Es4aCookie cookie = field(events.tip, "bar", Es4aCookie.class);
		check(((ColorDrawable) cookie.findViewById(R.id.cookie).getBackground()).getColor() == Color.RED, "消息条背景使用 ARGB 而不是资源编号");
		check(((TextView) cookie.findViewById(R.id.tv_title)).getCurrentTextColor() == Color.GREEN
			&& ((TextView) cookie.findViewById(R.id.tv_message)).getCurrentTextColor() == Color.YELLOW, "消息条标题及正文颜色生效");
		check(((FrameLayout.LayoutParams) cookie.getLayoutParams()).leftMargin > 0, "消息条边距写入真实布局");
		cookie.findViewById(R.id.btn_action).performClick();
		check(events.actions == 1 && cookie.getParent() == null, "消息条真实操作通知一次并移除");
		events.tip.置飘字范围(10, 90);
		events.tip.置飘字透明(0.8f, 0.2f);
		events.tip.置飘字时长(180);
		events.tip.显示飘字(anchor, "+12345", Color.RED, 22);
		floating = field(events.tip, "floating", GoodView.class);
		check(floating.isShowing() && field(floating, "mDuration", Integer.class) == 180, "飘字实际显示并应用时长");
		check(field(floating, "mFromY", Integer.class) < 0 && field(floating, "mToY", Integer.class) > 0, "飘字范围转换为向上偏移");
		check(field(floating, "mFromAlpha", Float.class) == 0.8f && field(floating, "mToAlpha", Float.class) == 0.2f, "飘字透明变化生效");
		TextView label = field(floating, "mContent", TextView.class);
		check(floating.getWidth() >= Math.ceil(label.getPaint().measureText("+12345")), "多位数字完整测量不截尾");
	}

	/**
	 * 复合公告展示的三个字段与点击事件保持同一条数据。
	 */
	private void verifyNotice() {
		events.notice = track(new XUI跑马灯Impl(root));
		EventDispatcher.registerEvent(events, "notice", "复合公告被单击");
		events.notice.置复合公告(new String[] {"标题"}, new String[] {"摘要"}, new String[] {"今天"});
		ViewGroup nativeView = (ViewGroup) events.notice.getView();
		check(((TextView) nativeView.getChildAt(0).findViewById(R.id.title)).getText().toString().equals("标题"), "复合公告包含真实标题视图");
		check(((TextView) nativeView.getChildAt(0).findViewById(R.id.subTitle)).getText().toString().equals("摘要"), "复合公告摘要可见");
		nativeView.getChildAt(0).performClick();
		check(events.noticeValue.equals("标题/摘要/今天"), "复合公告真实点击返回对应三个字段");
		events.notice.置复合公告(new String[0], new String[0], new String[0]);
		check(nativeView.getChildCount() == 0, "空复合公告移除旧子视图");
	}

	/**
	 * 创建实际截止任务，重设相同标识不得产生重复消息。
	 */
	private void prepareExpiryAndAnimation() {
		events.reentrant = track(new XUI跑马灯文本Impl(root));
		EventDispatcher.registerEvent(events, "reentrant", "公告开始");
		events.reentrant.追加定时公告("清空", "开始后清空", 0);
		events.timed = track(new XUI跑马灯文本Impl(root));
		EventDispatcher.registerEvent(events, "timed", "公告已过期");
		events.timed.追加定时公告("临时", "即将过期", System.currentTimeMillis() + 400);
		events.timed.追加定时公告("永久", "旧内容", 0);
		events.timed.追加定时公告("永久", "新内容", 0);
		check(events.timed.取公告数量() == 2, "同标识替换而不重复追加");
		originalBackground = anchor.getView().getBackground();
		XUI视图工具.背景渐变(anchor, Color.RED, Color.BLUE, 180);
		check(anchor.getView().getTag(simple.library.wsd.xui.R.id.es4a_xui_background_animation) != null, "背景动画实际登记生命周期");
	}

	/**
	 * 脱离窗口时动画归还状态，定时任务暂停；重新附着后再处理过期。
	 */
	private void verifyDetach() {
		XUI视图工具.背景闪烁(anchor, Color.GREEN, 3000);
		((ViewGroup) anchor.getView().getParent()).removeView(anchor.getView());
		check(anchor.getView().getBackground() == originalBackground
			&& anchor.getView().getTag(simple.library.wsd.xui.R.id.es4a_xui_background_animation) == null, "动画目标移出窗口立即归还背景及任务");
		events.timed.追加定时公告("离开", "离开期间过期", System.currentTimeMillis() + 400);
		((ViewGroup) events.timed.getView().getParent()).removeView(events.timed.getView());
	}

	/**
	 * 检查实际文字对象的字号、颜色及背景，再走原生异步合成保存流程。
	 */
	private void verifyEditorStyleAndSave() {
		events.editor.添加样式文字("XUI", Color.GREEN, 24, "xui-qa.ttf", Color.BLUE, Gravity.CENTER);
		TextView text = findText(events.editor.getView(), "XUI");
		check(text != null && text.getCurrentTextColor() == Color.GREEN, "编辑文字样式落到实际文本视图");
		check(Math.abs(text.getTextSize() / activity.getResources().getDisplayMetrics().scaledDensity - 24) < 0.1f,
			"编辑文字字号采用 SP");
		check(((ColorDrawable) text.getBackground()).getColor() == Color.BLUE, "编辑文字背景可独立配置");
		Typeface font = Typeface.createFromAsset(activity.getAssets(), "xui-qa.ttf");
		check(font.equals(text.getTypeface()), "图片编辑真实加载并应用资产字体");
		XUI富文本工具Impl rich = track(new XUI富文本工具Impl(root));
		rich.初始化();
		rich.添加文本("XUI");
		rich.置字体("xui-qa.ttf");
		XUI透明文本Impl receiver = track(new XUI透明文本Impl(root));
		rich.应用到(receiver);
		Spanned value = (Spanned) ((TextView) receiver.getView()).getText();
		TextPaint paint = new TextPaint();
		paint.setTextSize(20);
		for (MetricAffectingSpan span : value.getSpans(0, value.length(), MetricAffectingSpan.class)) {
			span.updateMeasureState(paint);
		}
		check(font.equals(paint.getTypeface()) && Math.abs(paint.measureText("XUI") - 60) < 0.1f,
			"富文本字体实际参与字形宽度测量");
		events.editor.置保存选项(false, false);
		events.editor.置输出格式(XUI图片编辑.输出格式_PNG, 100);
		events.editor.保存图片(pngOutput.getAbsolutePath());
	}

	/**
	 * 从真实保存回调与文件解码同时确认输出编码，不能仅检查配置字段。
	 */
	private void verifyEditorOutput(File file, String mime) {
		check(events.editorFailure.isEmpty(), "图片编辑保存未发生失败：" + events.editorFailure);
		check(events.savedPath.equals(file.getAbsolutePath()) && file.isFile(), "图片编辑真实保存成功回调与文件一致");
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(file.getAbsolutePath(), options);
		check(mime.equals(options.outMimeType) && options.outWidth > 0 && options.outHeight > 0,
			"图片编辑输出编码与非空尺寸正确：" + mime);
	}

	private static TextView findText(View view, String text) {
		if (view instanceof TextView && text.contentEquals(((TextView) view).getText())) {
			return (TextView) view;
		}
		if (view instanceof ViewGroup) {
			ViewGroup group = (ViewGroup) view;
			for (int index = 0; index < group.getChildCount(); index++) {
				TextView match = findText(group.getChildAt(index), text);
				if (match != null) {
					return match;
				}
			}
		}
		return null;
	}

	/**
	 * 中间行先归还后，其余行仍须按原父容器顺序恢复。
	 */
	private void verifyGroupRestoreOrder() {
		XUI分组列表Impl group = track(new XUI分组列表Impl(root));
		XUI分组列表项Impl first = track(new XUI分组列表项Impl(root));
		XUI分组列表项Impl second = track(new XUI分组列表项Impl(root));
		XUI分组列表项Impl third = track(new XUI分组列表项Impl(root));
		first.置文本("第一行");
		second.置文本("第二行");
		third.置文本("第三行");
		ViewGroup parent = (ViewGroup) first.getView().getParent();
		int original = parent.indexOfChild(first.getView());
		int section = group.创建分组("借用顺序", "");
		group.添加分组项目(section, first);
		group.添加分组项目(section, second);
		group.添加分组项目(section, third);
		group.移除分组项目(section, 1);
		group.移除分组项目(section, 0);
		group.移除分组项目(section, 0);
		check(parent.indexOfChild(first.getView()) == original
			&& parent.indexOfChild(second.getView()) == original + 1
			&& parent.indexOfChild(third.getView()) == original + 2, "任意归还顺序仍恢复相邻行原顺序");
		group.添加分组项目(section, first);
		XUI分组列表项Impl later = track(new XUI分组列表项Impl(root));
		group.添加分组项目(section, later);
		group.移除分组项目(section, 0);
		group.移除分组项目(section, 0);
		check(parent.indexOfChild(first.getView()) < parent.indexOfChild(second.getView())
			&& parent.indexOfChild(later.getView()) > parent.indexOfChild(third.getView()), "暂借期间新增的行也保持原相邻顺序");
	}

	/**
	 * 重置须恢复实际像素形状，重复绑定与销毁均不能遗留页面监听。
	 */
	private void verifyBezier() {
		ViewPager pager = (ViewPager) pages.getView();
		BezierBannerView nativeView = (BezierBannerView) bezier.getView();
		bezier.置方向(XUI贝塞尔指示器.方向_右);
		bezier.重置进度();
		Bitmap initial = render(nativeView);
		BezierBannerView reference = new BezierBannerView(nativeView.getContext());
		reference.attachToViewpager(pager);
		reference.layout(0, 0, nativeView.getWidth(), nativeView.getHeight());
		Bitmap expected = render(reference);
		check(initial.sameAs(expected), "重置图形与原生当前页初始像素一致");
		pager.removeOnPageChangeListener(reference);
		bezier.置进度(0.6f);
		Bitmap moving = render(nativeView);
		check(!initial.sameAs(moving), "贝塞尔半程形变真实改变绘制结果");
		bezier.重置进度();
		Bitmap restored = render(nativeView);
		check(initial.sameAs(restored), "重置后不保留半程路径");
		bezier.置方向(XUI贝塞尔指示器.方向_左);
		bezier.置进度(0.6f);
		Bitmap left = render(nativeView);
		check(!moving.sameAs(left), "方向设置实际影响图形运动");
		List<?> listeners = field(pager, "mOnPageChangeListeners", List.class);
		int count = listeners.size();
		bezier.绑定页面框(pages);
		check(listeners.size() == count, "重复绑定不叠加原生页面监听");
		bezier.销毁();
		check(!listeners.contains(nativeView), "销毁解除当前页面监听");
		initial.recycle();
		expected.recycle();
		moving.recycle();
		restored.recycle();
		left.recycle();
	}

	private static Bitmap render(View view) {
		Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
		view.draw(new Canvas(bitmap));
		return bitmap;
	}

	/**
	 * 使用原生选择和实际触摸序列区分选中、重复点击和双击，不能直接调用监听器。
	 */
	private void verifyTabGestures() throws InterruptedException {
		final int[] point = new int[2];
		onUi(new Runnable() {
			@Override
			public void run() {
				TabSegment nativeTabs = (TabSegment) events.tabs.getView();
				int selected = events.tabSelected;
				nativeTabs.selectTab(1, true, true);
				check(events.tabSelected == selected + 1, "原生选择仅通知一次新选中");
				selected = events.tabSelected;
				nativeTabs.selectTab(1, true, true);
				check(events.tabSelected == selected && events.tabRepeated > 0, "重复选择只通知重复事件");
				TextView label = findText(nativeTabs, "栏目乙");
				label.getLocationOnScreen(point);
				point[0] += label.getWidth() / 2;
				point[1] += label.getHeight() / 2;
			}
		});
		for (int index = 0; index < 2; index++) {
			long now = SystemClock.uptimeMillis();
			MotionEvent down = MotionEvent.obtain(now, now, MotionEvent.ACTION_DOWN, point[0], point[1], 0);
			MotionEvent up = MotionEvent.obtain(now, now + 20, MotionEvent.ACTION_UP, point[0], point[1], 0);
			instrumentation.sendPointerSync(down);
			instrumentation.sendPointerSync(up);
			down.recycle();
			up.recycle();
			Thread.sleep(70);
		}
		onUi(new Runnable() {
			@Override
			public void run() {
				check(events.tabDoubles == 1, "真实双击只触发一次双击事件");
			}
		});
	}

	private <T extends 组件> T track(T component) {
		created.add(component);
		return component;
	}

	private void check(boolean condition, String message) {
		if (!condition) {
			throw new AssertionError(message);
		}
		assertions++;
	}

	/**
	 * 只在测试中读取必要的原生状态，不能成为产品实现的反射依赖。
	 */
	private static <T> T field(Object owner, String name, Class<T> type) {
		for (Class<?> current = owner.getClass(); current != null; current = current.getSuperclass()) {
			try {
				Field member = current.getDeclaredField(name);
				member.setAccessible(true);
				return type.cast(member.get(owner));
			} catch (NoSuchFieldException missing) {
				// 继续核对上游基类。
			} catch (ReflectiveOperationException failure) {
				throw new AssertionError(failure);
			}
		}
		throw new AssertionError("Missing field: " + name);
	}

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
	 * 记录真实定时与用户点击反馈。
	 */
	public static final class Events {
		public XUI分段标签Impl tabs;
		int tabSelected;
		int tabRepeated;
		int tabDoubles;

		public void tabs$标签被选择(int index) {
			tabSelected++;
		}

		public void tabs$标签被重复选择(int index) {
			tabRepeated++;
		}

		public void tabs$标签被双击(int index) {
			tabDoubles++;
		}

		public XUI跑马灯文本Impl reentrant;
		int reentrantStarts;

		public void reentrant$公告开始(String identifier, int index, String text) {
			reentrantStarts++;
			reentrant.清空();
		}

		public XUI图片编辑Impl editor;
		String savedPath = "";
		String editorFailure = "";

		public void editor$保存完成(String path) {
			savedPath = path;
		}

		public void editor$保存失败(String message) {
			editorFailure = message;
		}

		public XUI提示Impl tip;
		public XUI跑马灯Impl notice;
		public XUI跑马灯文本Impl timed;
		int actions;
		String expired = "";
		String noticeValue = "";

		public void tip$操作被单击() {
			actions++;
		}

		public void notice$复合公告被单击(int index, String title, String summary, String time) {
			noticeValue = title + "/" + summary + "/" + time;
		}

		public void timed$公告已过期(String identifier) {
			expired = identifier;
		}
	}
}
