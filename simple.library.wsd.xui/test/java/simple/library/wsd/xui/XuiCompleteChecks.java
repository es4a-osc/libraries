package simple.library.wsd.xui;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import android.app.Instrumentation;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.text.Spanned;
import android.text.style.AlignmentSpan;
import android.text.style.LeadingMarginSpan;
import android.text.style.ReplacementSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.xuexiang.xui.R;
import com.xuexiang.xui.widget.banner.recycler.BannerLayout;
import com.xuexiang.xui.widget.dialog.materialdialog.DialogAction;
import com.xuexiang.xui.widget.dialog.materialdialog.MaterialDialog;
import com.xuexiang.xui.widget.grouplist.XUICommonListItemView;
import com.xuexiang.xui.widget.imageview.crop.CropImageView;
import com.xuexiang.xui.widget.popupwindow.popup.XUISimplePopup;
import com.xuexiang.xui.widget.progress.materialprogressbar.MaterialProgressBar;
import com.xuexiang.xui.widget.picker.widget.OptionsPickerView;
import com.xuexiang.xui.widget.picker.wheelview.WheelView;
import com.xuexiang.xui.widget.picker.wheelview.listener.OnItemSelectedListener;
import com.xuexiang.xui.widget.tabbar.TabSegment;
import com.xuexiang.xui.widget.textview.supertextview.SuperTextView;

import simple.runtime.android.MainActivity;
import simple.runtime.components.impl.android.面板Impl;
import simple.runtime.components.组件;
import simple.runtime.events.EventDispatcher;
import simple.runtime.variants.IntegerVariant;

/**
 * 对完整补齐范围进行设备行为回归，检查状态、真实子视图、事件及内容归还。
 *
 * @author 树先生 xhwsd@qq.com
 */
final class XuiCompleteChecks {

	private final Instrumentation instrumentation;
	private final MainActivity activity;
	private final List<组件> created = new ArrayList<组件>();
	private final Events events = new Events();
	private 面板Impl container;
	private int assertions;
	private XUI图片轮播Impl images;
	private XUI文本轮播Impl texts;
	private XUI引导轮播Impl guide;
	private XUI轮播Impl cards;
	private XUI图片裁剪Impl crop;
	private XUI缩放图片Impl photo;
	private XUI联动滚动容器Impl linkage;
	private XUI圆角按钮Impl anchor;
	private XUI气泡Impl tooltip;
	private 面板Impl custom;
	private ViewGroup originalParent;
	private ViewGroup.LayoutParams originalParams;
	private View earlyTip;

	/**
	 * 复用目标应用的真实窗口。
	 *
	 * @param instrumentation 当前运行器。
	 * @param activity Simple 宿主。
	 */
	XuiCompleteChecks(Instrumentation instrumentation, MainActivity activity) {
		this.instrumentation = instrumentation;
		this.activity = activity;
	}

	/**
	 * 按依赖顺序执行；布局和动画等待放在界面线程之外。
	 *
	 * @return 实际成功的断言数。
	 * @throws Exception 运行器等待失败。
	 */
	int run() throws Exception {
		boolean passed = false;
		try {
			onUi(new Runnable() {
				@Override
				public void run() {
					prepare();
					verifyGoalCore();
					verifyFlow();
					verifyComposites();
					verifyLogAndStates();
					verifyTabsAndTitle();
					verifyRichAndAppearance();
					verifyTheme();
					prepareBannersAndImages();
					prepareGoalGrid();
				}
			});
			settle();
			onUi(new Runnable() {
				@Override
				public void run() {
					verifyBannersAndImages();
					verifyGoalImages();
					verifyDialog();
					verifyPopup();
					verifyPickerConfiguration();
					prepareTooltip();
				}
			});
			settle();
			onUi(new Runnable() {
				@Override
				public void run() {
					check(!events.bottom.是否显示(), "网格退出动画后已关闭");
					check(events.imageFailures > 0, "缺失图片通过真实加载失败事件通知");
					check(events.tooltipShows == 1, "气泡实际显示事件仅一次");
					tooltip.关闭();
					check(custom.getView().getParent() == originalParent, "气泡内容归还原父容器");
					check(custom.getView().getLayoutParams() == originalParams, "气泡内容恢复原布局参数对象");
					check(custom.getView().getVisibility() == View.GONE, "气泡内容恢复隐藏状态");
					check(events.tooltipCloses == 1, "气泡关闭仅通知一次");
					check(!tooltip.是否显示(), "主动关闭后气泡不再显示");
				}
			});
			settle();
			onUi(new Runnable() {
				@Override
				public void run() {
					check(events.tooltipCloses == 1, "退出动画回调不重复关闭事件");
					check(cards.取当前页() == 1, "卡片轮播布局后页码定位正确");
					verifySearchAndMarquee();
					tooltip.显示(anchor, "立即关闭", 1, 0);
					Object pending = field(tooltip, "session", Object.class);
					earlyTip = field(pending, "view", View.class);
					tooltip.关闭();
					check(earlyTip.getVisibility() == View.GONE, "尚未附着的气泡立即关闭后保持隐藏");
				}
			});
			Thread.sleep(850);
			onUi(new Runnable() {
				@Override
				public void run() {
					check(!earlyTip.isAttachedToWindow(), "迟到的气泡已移出窗口");
					check(events.tooltipShows == 1 && events.tooltipCloses == 2, "快速关闭不发迟到显示或重复关闭");
				}
			});
			passed = true;
			return assertions;
		} finally {
			try {
				onUi(new Runnable() {
				@Override
				public void run() {
					XUI主题工具.重置组件配置();
					EventDispatcher.unregisterEvent(events);
					for (int index = created.size() - 1; index >= 0; index--) {
						created.get(index).销毁();
					}
				}
				});
			} catch (AssertionError cleanup) {
				if (passed) {
					throw cleanup;
				}
				android.util.Log.e("XUI-COMPLETE-ALL", "cleanup", cleanup);
			}
		}
	}

	/**
	 * 创建独立可见容器并保留所有待销毁组件。
	 */
	private void prepare() {
		container = track(new 面板Impl(null) {
			@Override
			protected void addToContainer() {
				// 宿主直接承载本次回归根。
			}
		});
		container.布局(IntegerVariant.getIntegerVariant(组件.布局_单帧));
		activity.setContentView(container.getView());
	}

	/**
	 * 验证三种选择模式以及数据修改后的索引语义。
	 */
	private void verifyFlow() {
		XUI流式标签Impl flow = track(new XUI流式标签Impl(container));
		flow.置项目数组(new String[] {"甲", "乙", "丙"});
		flow.置选择模式(XUI流式标签.选择模式_多选);
		flow.置选中位置数组(new int[] {0, 2});
		check(Arrays.equals(flow.取选中索引数组(), new int[] {0, 2}), "多选实际选中两项");
		flow.添加标签("丁");
		check(flow.取标签文本(3).equals("丁"), "追加标签文字正确");
		check(flow.取选中索引数组().length == 2, "追加不清除原选择");
		flow.移除标签(0);
		check(Arrays.equals(flow.取选中索引数组(), new int[] {1}), "删除选中项并前移后项选择");
		flow.置选择模式(XUI流式标签.选择模式_单选);
		check(flow.取选中索引数组().length == 0, "切换模式清除选择");
		boolean rejected = false;
		try {
			flow.置选中位置数组(new int[] {0, 1});
		} catch (RuntimeException failure) {
			rejected = true;
		}
		check(rejected, "单选拒绝多个索引");
		flow.置选择模式(XUI流式标签.选择模式_不选择);
		check(flow.取选择模式() == 0, "无选择模式可读取");
	}

	/**
	 * 验证程序化创建的编辑、开关、复选框及借用附属内容。
	 */
	private void verifyComposites() {
		events.superText = track(new XUI超级文本Impl(container));
		register("superText", "附属选中改变", "编辑文本改变", "区域被单击");
		events.superText.置左侧文本("左侧");
		events.superText.置中间编辑文本("初始值");
		check(events.superText.取中间编辑值().equals("初始值"), "编辑值来自真实编辑框");
		check(!events.superText.是否编辑为空(), "编辑非空状态正确");
		EditText editor = field(events.superText, "editor", EditText.class);
		check(editor.getParent() == events.superText.getView(), "编辑框实际加入视图树");
		editor.setText("用户新值");
		check(events.editText.equals("用户新值"), "文字变化事件回传实际输入");
		events.superText.置开关是否选中(true, true);
		check(events.superText.取开关是否选中() && events.accessoryChanges == 0, "静默设置开关不触发事件");
		CompoundButton accessory = field(events.superText, "accessory", CompoundButton.class);
		check(accessory.getParent() == events.superText.getView(), "开关实际可见");
		accessory.performClick();
		check(events.accessoryChanges == 1 && !events.superText.取开关是否选中(), "原生点击改变开关并发送事件");
		events.superText.置复选框选中(true, false);
		check(events.superText.取复选框选中() && events.accessoryType == 2, "复选框状态与类型事件一致");
		((SuperTextView) events.superText.getView()).getLeftTextView().performClick();
		check(events.region.equals("左侧"), "区域事件来自实际子文本");
		events.superText.置编辑模式(false);
		check(editor.getVisibility() == View.GONE, "关闭编辑模式隐藏输入框");
		events.superText.置编辑模式(true);
		check(events.superText.取中间编辑值().equals("用户新值"), "切换模式保留输入");

		events.row = track(new XUI分组列表项Impl(container));
		register("row", "开关改变");
		events.row.置附属类型(XUI分组列表项.附属类型_开关);
		events.row.置开关选中(true, true);
		check(events.row.取开关选中() && events.rowChanges == 0, "列表开关静默设置");
		((XUICommonListItemView) events.row.getView()).getSwitch().performClick();
		check(events.rowChanges == 1 && !events.row.取开关选中(), "列表开关真实点击事件");
		面板Impl borrowed = track(new 面板Impl(container));
		borrowed.可视(false);
		ViewGroup parent = (ViewGroup) borrowed.getView().getParent();
		events.row.置自定义附属(borrowed);
		check(borrowed.getView().getParent() != parent, "列表自定义附属被实际借出");
		events.row.置附属类型(0);
		check(borrowed.getView().getParent() == parent && borrowed.getView().getVisibility() == View.GONE,
			"切换附属类型恢复原布局和可见性");
	}

	/**
	 * 验证分组真实组件的归属、事件、销毁，以及校验规则和进度样式的状态保持。
	 */
	private void verifyGoalCore() {
		XUI分组列表Impl group = track(new XUI分组列表Impl(container));
		events.group = group;
		EventDispatcher.registerEvent(events, "group", "项目被单击");
		EventDispatcher.registerEvent(events, "group", "项目被长按");
		XUI分组列表项Impl row = track(new XUI分组列表项Impl(container));
		row.置文本("已配置项目");
		row.置详情文本("详情保持");
		row.置附属类型(2);
		row.置开关选中(true, true);
		ViewGroup parent = (ViewGroup) row.getView().getParent();
		ViewGroup.LayoutParams parameters = row.getView().getLayoutParams();
		int originalPosition = parent.indexOfChild(row.getView());
		int index = group.创建分组("设置", "说明文字");
		group.添加分组项目(index, row);
		check(row.getView().getParent() == group.getView(), "配置好的行进入真实 Section");
		check(row.取详情文本().equals("详情保持") && row.取开关选中(), "原详情与开关保持");
		row.getView().performClick();
		check(!row.取开关选中() && events.groupClicks == 1, "开关行点击切换并通知组事件");
		row.置文本("更新后文字");
		row.getView().performLongClick();
		check(events.groupLongClicks == 1 && events.groupText.equals("更新后文字"), "行长按通知当前文字");
		boolean rejected = false;
		try {
			group.添加分组项目(index, row);
		} catch (ComponentStateException expected) {
			rejected = true;
		}
		check(rejected && row.groupOwner == group, "重复加入不改变原归属");
		group.置分组说明(index, "更新说明");
		check(row.getView().getParent() == group.getView(), "重建说明不重建组件");
		group.移除分组项目(index, 0);
		check(row.getView().getParent() == parent, "移除行归还父容器");
		check(row.getView().getLayoutParams() == parameters, "移除行恢复原布局参数身份");
		check(parent.indexOfChild(row.getView()) == originalPosition, "移除行恢复原位置");
		row.getView().performClick();
		check(events.groupClicks == 1, "归还后的行不继续通知旧组事件");
		group.添加分组项目(index, row);
		row.销毁();
		group.置分组说明(index, "销毁后仍可更新");
		check(row.getView().getParent() == null, "组重建不复活已销毁组件");
		group.清空分组();
		check(group.取分组数量() == 0, "清空分组清除模型");

		XUI材质编辑框Impl material = track(new XUI材质编辑框Impl(container));
		material.置清除按钮(true);
		material.添加必填规则("不能为空");
		material.添加正则规则("[0-9]{3}", "请输入三位数字");
		material.文本("ab");
		check(!material.校验(), "材质输入规则实际参与校验");
		material.文本("123");
		check(material.校验(), "符合正则规则时通过");
		material.清空校验规则();
		material.文本("abc");
		check(material.校验(), "清空规则后不残留正则");
		XUI校验编辑框Impl validated = track(new XUI校验编辑框Impl(container));
		events.validated = validated;
		EventDispatcher.registerEvent(events, "validated", "校验失败");
		validated.添加必填规则("请输入值");
		validated.文本("");
		validated.校验();
		check(events.validationFailures > 0, "原生校验失败回调转发到 Simple");

		XUI材质进度条Impl progress = track(new XUI材质进度条Impl(container));
		progress.最大进度(100);
		progress.进度(35);
		progress.置次级进度(70);
		progress.置进度颜色(Color.RED);
		View identity = progress.getView();
		progress.置进度样式(XUI材质进度条.进度样式_水平);
		check(progress.getView() == identity, "切换进度样式保留视图身份");
		check(progress.取进度样式() == 1, "水平样式可读");
		MaterialProgressBar nativeProgress = (MaterialProgressBar) progress.getView();
		check(nativeProgress.getProgressDrawable() instanceof com.xuexiang.xui.widget.progress.materialprogressbar.HorizontalProgressDrawable, "水平样式实际切换原生绘制对象");
		check(progress.进度() == 35 && progress.取次级进度() == 70, "切换保留主次级任务进度");
		check(nativeProgress.getSupportProgressTintList().getDefaultColor() == Color.RED, "切换保留着色");
		progress.置不确定进度(true);
		progress.置进度样式(XUI材质进度条.进度样式_环形);
		check(progress.是否不确定进度(), "切换样式保留等待模式");
		progress.置不确定进度(false);
		progress.最大进度(40);
		check(progress.取次级进度() == 40 && nativeProgress.getSecondaryProgress() == 40, "最大值收缩同步缓冲进度");
		// 仅检查数值会漏掉替换 Drawable 后层级没有恢复的问题，直接验证绘制色带。
		progress.置进度样式(XUI材质进度条.进度样式_水平);
		progress.最大进度(100);
		progress.进度(35);
		progress.置次级进度(70);
		progress.置进度颜色(Color.RED);
		progress.置次级进度颜色(Color.GREEN);
		progress.置进度背景颜色(Color.BLUE);
		progress.置显示进度背景(true);
		progress.置使用固有内边距(false);
		progress.置不确定进度(true);
		progress.置进度样式(XUI材质进度条.进度样式_环形);
		progress.置进度样式(XUI材质进度条.进度样式_水平);
		progress.置不确定进度(false);
		LayerDrawable layers = (LayerDrawable) nativeProgress.getProgressDrawable();
		check(layers.findDrawableByLayerId(android.R.id.progress).getLevel() == 3500,
			"等待模式切换样式后恢复实际前景层级");
		check(layers.findDrawableByLayerId(android.R.id.secondaryProgress).getLevel() == 7000,
			"等待模式切换样式后恢复实际缓冲层级");
		Bitmap rendered = Bitmap.createBitmap(200, 16, Bitmap.Config.ARGB_8888);
		layers.setBounds(0, 0, 200, 16);
		layers.draw(new Canvas(rendered));
		check(rendered.getPixel(20, 8) == Color.RED && rendered.getPixel(100, 8) == Color.GREEN,
			"水平材质进度实际画出前景与缓冲色带");
		rendered.recycle();
	}

	/**
	 * 在真实布局中触发九宫格加载，重复地址的图片仍应保留独立位置。
	 */
	private void prepareGoalGrid() {
		events.grid = track(new XUI九宫格图片Impl(container));
		register("grid", "图片被长按", "图片加载完成", "图片加载失败");
		events.grid.置占位图片("@drawable/icon", "@drawable/icon");
		events.grid.置图片数组(new String[] {"@drawable/icon", "@drawable/icon", "goal-missing-image.png"});
		View grid = events.grid.getView();
		grid.measure(View.MeasureSpec.makeMeasureSpec(600, View.MeasureSpec.EXACTLY),
			View.MeasureSpec.makeMeasureSpec(300, View.MeasureSpec.EXACTLY));
		grid.layout(0, 0, 600, 300);
		((ViewGroup) grid).getChildAt(1).performLongClick();
		check(events.gridLongIndex == 1, "九宫格重复地址长按返回真实位置");
	}

	/**
	 * 确认异步图片反馈、选中外观与可见区域输出均实际生效。
	 */
	private void verifyGoalImages() {
		check(events.gridLoaded.contains(0) && events.gridLoaded.contains(1), "重复地址按两个位置分别完成加载");
		check(events.gridFailed.contains(2), "缺失图片通知失败位置");
		XUI圆角图片Impl rounded = track(new XUI圆角图片Impl(container));
		rounded.置选中(true);
		check(rounded.取选中() && rounded.getView().isSelected(), "圆角图片程序驱动选中外观");
		photo.置图片缩放模式(XUI缩放图片.图片缩放_居中裁剪);
		check(((com.xuexiang.xui.widget.imageview.photoview.PhotoView) photo.getView()).getScaleType() == android.widget.ImageView.ScaleType.CENTER_CROP,
			"缩放图片设置实际适配模式");
		String path = new java.io.File(activity.getFilesDir(), "goal-visible.png").getAbsolutePath();
		photo.保存可见图片(path);
		android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeFile(path);
		check(bitmap != null && bitmap.getWidth() == photo.getView().getWidth()
			&& bitmap.getHeight() == photo.getView().getHeight(), "可见区域保存具有真实视图尺寸");
		bitmap.recycle();
		check(photo.取显示区域().length == 4, "保存不回收视图持有的缓存或破坏图片");
		android.graphics.Bitmap jpeg = android.graphics.Bitmap.createBitmap(20, 10, android.graphics.Bitmap.Config.ARGB_8888);
		String jpegPath = new java.io.File(activity.getFilesDir(), "goal-encoding.jpg").getAbsolutePath();
		try {
			ComponentSupport.saveImage(jpeg, jpegPath, android.graphics.Bitmap.CompressFormat.JPEG, 60);
		} finally {
			jpeg.recycle();
		}
		android.graphics.BitmapFactory.Options metadata = new android.graphics.BitmapFactory.Options();
		metadata.inJustDecodeBounds = true;
		android.graphics.BitmapFactory.decodeFile(jpegPath, metadata);
		check("image/jpeg".equals(metadata.outMimeType), "输出编码按明确格式而非扩展名伪装");
	}

	/**
	 * 验证分级日志和可重复替换的四种状态内容。
	 */
	private void verifyLogAndStates() {
		XUI日志文本Impl logger = track(new XUI日志文本Impl(container));
		for (int level = 0; level <= 4; level++) {
			logger.追加日志("级别" + level, level);
		}
		check(((TextView) logger.getView()).getCurrentTextColor() != Color.WHITE, "普通日志在默认浅色主题可见");
		View logView = logger.getView();
		logView.scrollTo(0, 120);
		logView.measure(View.MeasureSpec.makeMeasureSpec(600, View.MeasureSpec.EXACTLY),
				View.MeasureSpec.makeMeasureSpec(600, View.MeasureSpec.EXACTLY));
		logView.layout(0, 0, 600, 600);
		check(logView.getScrollY() == 0, "首次布局修正旧高度产生的越界滚动");
		String text = ((TextView) logger.getView()).getText().toString();
		for (int level = 0; level <= 4; level++) {
			check(text.contains("级别" + level), "对应级别日志可见 " + level);
		}
		logger.清空日志();
		check(((TextView) logger.getView()).getText().length() == 0, "日志清空");
		XUI多状态容器Impl states = track(new XUI多状态容器Impl(container));
		面板Impl first = track(new 面板Impl(container));
		面板Impl second = track(new 面板Impl(container));
		first.可视(false);
		ViewGroup parent = (ViewGroup) first.getView().getParent();
		states.显示错误();
		check(states.取当前状态() == XUI多状态容器.状态_错误, "默认错误状态");
		states.置状态内容(XUI多状态容器.状态_错误, first);
		check(first.getView().getParent() != parent, "默认状态显示后仍可替换自定义内容");
		states.置状态内容(XUI多状态容器.状态_错误, second);
		check(first.getView().getParent() == parent && first.getView().getVisibility() == View.GONE,
			"第二次替换归还第一份内容");
		states.显示内容();
		check(states.取当前状态() == 0, "返回正常内容状态");
		states.清除状态内容(XUI多状态容器.状态_错误);
		check(second.getView().getParent() == parent, "清除状态归还第二份内容");
		states.显示加载();
		check(states.取当前状态() == 1, "加载状态可读");
		states.显示空状态();
		check(states.取当前状态() == 2, "空状态可读");
		states.显示无网络();
		check(states.取当前状态() == 4, "无网络状态可读");
	}

	/**
	 * 验证标签修改和标题操作位置不依赖旧索引。
	 */
	private void verifyTabsAndTitle() {
		XUI分段标签Impl tabs = track(new XUI分段标签Impl(container));
		tabs.置标签数组(new String[] {"甲", "乙"});
		tabs.置标签文本(1, "更新乙");
		tabs.置标签徽标(1, 3);
		check(tabs.取标记数量(1) == 3, "分段徽标数量正确");
		tabs.置标签图标(1, "icon.png", "icon.png");
		TabSegment.Tab tab = ((TabSegment) tabs.getView()).getTab(1);
		check(tab.getText().toString().equals("更新乙") && tab.getNormalIcon() != null, "更新图标保留文字");
		check(tabs.取标记数量(1) == 3, "更新图标保留徽标");
		tabs.置标签徽标(1, 0);
		check(tabs.取标记数量(1) == 0, "徽标可隐藏");
		events.title = track(new XUI标题栏Impl(container));
		register("title", "操作被单击");
		events.title.添加操作("首项");
		events.title.添加操作("第二项");
		events.title.移除操作(0);
		events.title.置操作文本(0, "更新操作");
		List<?> actions = field(events.title, "actions", List.class);
		((View) actions.get(0)).performClick();
		check(events.actionIndex == 0, "删除前项后的点击索引正确");
		check(((TextView) actions.get(0)).getText().toString().equals("更新操作"), "操作文字实际更新");
		面板Impl titleContent = track(new 面板Impl(container));
		ViewGroup parent = (ViewGroup) titleContent.getView().getParent();
		events.title.置自定义标题(titleContent);
		check(titleContent.getView().getParent() != parent, "标题内容实际借出");
		events.title.清除自定义标题();
		check(titleContent.getView().getParent() == parent, "标题内容恢复");
	}

	/**
	 * 验证富文本段落样式、图片尺寸及材质进度着色。
	 */
	private void verifyRichAndAppearance() {
		XUI富文本工具Impl rich = track(new XUI富文本工具Impl(container));
		rich.初始化();
		rich.添加文本("完整段落\n");
		rich.置段落对齐(1);
		rich.置段落行高(60);
		rich.置段落缩进(10, 20);
		rich.置引用样式(Color.BLUE, 4, 6);
		rich.置项目符号(Color.RED, 3, 8);
		rich.添加图片("icon.png", 32, 24, 2);
		rich.置图片间距(5, 7);
		XUI基础文本Impl text = track(new XUI基础文本Impl(container));
		text.置顶部文本("标题");
		text.置区域字体(0, "", 21);
		check(Math.abs(((com.xuexiang.xui.widget.textview.supertextview.BaseTextView) text.getView())
			.getTopTextView().getTextSize() / activity.getResources().getDisplayMetrics().scaledDensity - 21) < 0.1f,
			"区域字号使用 SP");
		// 使用现有 Simple 文本组件接收真实 Spannable。
		XUI透明文本Impl receiver = track(new XUI透明文本Impl(container));
		rich.应用到(receiver);
		Spanned spans = (Spanned) ((TextView) receiver.getView()).getText();
		check(spans.getSpans(0, spans.length(), AlignmentSpan.class).length == 1, "段落对齐 Span 已应用");
		check(spans.getSpans(0, spans.length(), LeadingMarginSpan.class).length >= 1, "缩进或项目符号 Span 已应用");
		ReplacementSpan[] images = spans.getSpans(0, spans.length(), ReplacementSpan.class);
		check(images.length == 1 && field(images[0], "mDrawable", Drawable.class).getBounds().width() == 44
			&& field(images[0], "mDrawable", Drawable.class).getBounds().height() == 24, "富文本图片实际尺寸");
		rich.置图片间距(10, 12);
		rich.应用到(receiver);
		Spanned updated = (Spanned) ((TextView) receiver.getView()).getText();
		ReplacementSpan[] updatedImages = updated.getSpans(0, updated.length(), ReplacementSpan.class);
		check(field(images[0], "mDrawable", Drawable.class).getBounds().width() == 44
			&& field(updatedImages[0], "mDrawable", Drawable.class).getBounds().width() == 54,
			"重建图片间距不污染已经应用的旧富文本");
		XUI图片编辑Impl editor = track(new XUI图片编辑Impl(container));
		check(!editor.是否有编辑(), "空图片编辑状态");
		editor.添加文字("文字", Color.BLUE);
		check(editor.是否有编辑(), "添加文字形成实际编辑");
		editor.撤销();
		check(!editor.是否有编辑(), "全部撤销后不把重做历史当作生效编辑");
		editor.重做();
		check(editor.是否有编辑(), "重做恢复生效编辑");
		editor.隐藏编辑框();
		editor.清空编辑();
		check(!editor.是否有编辑(), "清除编辑状态");
		editor.置滤镜("GRAY_SCALE");
		check(editor.是否有编辑(), "滤镜计入生效编辑");
		editor.清空编辑();
		check(!editor.是否有编辑(), "清除同时复位滤镜");
		XUI材质进度条Impl progress = track(new XUI材质进度条Impl(container));
		progress.置进度颜色(Color.RED);
		progress.置等待颜色(Color.GREEN);
		MaterialProgressBar nativeProgress = (MaterialProgressBar) progress.getView();
		check(nativeProgress.getSupportProgressTintList().getDefaultColor() == Color.RED, "确定进度实际着色");
		check(nativeProgress.getSupportIndeterminateTintList().getDefaultColor() == Color.GREEN, "等待进度实际着色");
		events.loading = track(new XUI加载框Impl(container));
		register("loading", "已取消");
		events.loading.置提示文本("正在处理");
		events.loading.置可取消(false);
		events.loading.显示();
		events.loading.getView().performClick();
		check(events.loading.是否加载() && events.loadingCancels == 0, "不可取消加载不响应点击取消");
		events.loading.置可取消(true);
		events.loading.getView().performClick();
		check(!events.loading.是否加载() && events.loadingCancels == 1, "点击加载框实际取消并通知一次");
		XUI帧容器Impl frame = track(new XUI帧容器Impl(container));
		frame.置顶部分隔线(3, 4, 2, Color.RED);
		frame.置底部分隔线(3, 4, 2, Color.BLUE);
		frame.置左侧分隔线(3, 4, 2, Color.GREEN);
		frame.置右侧分隔线(3, 4, 2, Color.BLACK);
		check(frame.getView() != null, "四边分隔线配置接受有效参数");
	}

	/**
	 * 验证样式配置独立于宿主，错误字体不会污染后续配置。
	 */
	private void verifyTheme() {
		int hostColor = XUI主题工具.取主色();
		int style = activity.getResources().getIdentifier("XuiCompletionTheme", "style", activity.getPackageName());
		check(style != 0, "测试样式已打包");
		XUI主题工具.置组件主题(style);
		check(XUI主题工具.取组件主题() == style, "组件主题配置可读取");
		check(XUI主题工具.取组件颜色属性(android.R.attr.colorAccent, 0) == 0xFF13579B, "组件主题覆盖实际生效");
		check(XUI主题工具.取主色() == hostColor, "组件主题配置未改变 Activity 主题");
		boolean rejected = false;
		try {
			XUI主题工具.置默认字体("definitely-missing-font.ttf");
		} catch (RuntimeException failure) {
			rejected = true;
		}
		check(rejected && XUI主题工具.取默认字体().isEmpty(), "字体加载失败保留原配置");
		XUI主题工具.重置组件配置();
		check(XUI主题工具.取组件主题() == 0 && XUI主题工具.取默认字体().isEmpty(), "恢复组件默认配置");
	}

	/**
	 * 准备需要真实测量尺寸的轮播、裁剪、缩放及联动组件。
	 */
	private void prepareBannersAndImages() {
		images = track(new XUI图片轮播Impl(container));
		events.images = images;
		register("images", "图片加载失败");
		texts = track(new XUI文本轮播Impl(container));
		guide = track(new XUI引导轮播Impl(container));
		cards = track(new XUI轮播Impl(container));
		for (View target : new View[] {images.getView(), texts.getView(), guide.getView(), cards.getView()}) {
			target.setLayoutParams(new FrameLayout.LayoutParams(600, 160));
		}
		images.置项目数组(new String[] {"icon.png", "icon.png"});
		images.置自动滚动启用(false);
		texts.置项目数组(new String[] {"甲", "乙"});
		texts.置自动滚动启用(false);
		guide.置项目数组(new String[] {"icon.png", "icon.png"});
		guide.置自动滚动启用(false);
		cards.置项目数组(new String[] {"甲", "乙"});
		cards.置自动播放(false);
		crop = track(new XUI图片裁剪Impl(container));
		crop.getView().setLayoutParams(new FrameLayout.LayoutParams(500, 300));
		crop.置图片("icon.png");
		photo = track(new XUI缩放图片Impl(container));
		photo.getView().setLayoutParams(new FrameLayout.LayoutParams(500, 300));
		photo.置图片("icon.png");
		linkage = track(new XUI联动滚动容器Impl(container));
		linkage.getView().setLayoutParams(new FrameLayout.LayoutParams(500, 160));
		XUI联动线性容器Impl child = track(new XUI联动线性容器Impl(linkage));
		child.getView().setLayoutParams(new ViewGroup.LayoutParams(500, 500));
	}

	/**
	 * 验证真实页码、图像边界与安全主动定位。
	 */
	private void verifyBannersAndImages() {
		images.置当前页(1, false);
		texts.置当前页(1, false);
		guide.置当前页(1, false);
		cards.置当前页(1, false);
		check(images.取当前页() == 1, "图片轮播定位真实页");
		check(texts.取当前页() == 1, "文字轮播定位真实页");
		check(guide.取当前页() == 1, "引导轮播定位真实页");
		check(cards.getView() instanceof BannerLayout, "卡片轮播保留原生容器");
		images.置占位图片("icon.png", "icon.png");
		images.置项目数组(new String[] {"definitely-missing-banner-image.png"});
		images.置当前页(0, false);
		float[] rect = crop.取裁剪区域();
		check(rect.length == 4 && rect[2] > rect[0] && rect[3] > rect[1], "裁剪区域为有效原图边界");
		crop.镜像图片(true);
		crop.镜像图片(false);
		check(((CropImageView) crop.getView()).getImageView().getDrawable() != null, "镜像后仍有图片");
		photo.重置缩放();
		check(photo.取显示区域().length == 4, "缩放图片显示区域可读取");
		check(Math.abs(photo.取缩放() - 1) < 0.01f, "缩放重置");
		photo.置缩放(2.0f, 100, 100, false);
		check(Math.abs(photo.取缩放() - 2) < 0.01f, "重置后继续缩放可用");
		linkage.滚动到子项(0);
		linkage.滚动到(0);
		check(linkage.取滚动位置() == 0, "联动主动回到顶部");
	}

	/**
	 * 验证复选提示、确认前读取以及中立按钮真实点击。
	 */
	private void verifyDialog() {
		events.dialog = track(new XUI对话框Impl(container));
		events.dialog.初始化();
		register("dialog", "中立按钮被单击");
		events.dialog.置中立按钮("稍后");
		events.dialog.置复选提示("记住", true);
		events.dialog.置禁用项目(new int[] {1});
		events.dialog.显示单选("选择", new String[] {"甲", "乙", "丙"}, 0);
		check(events.dialog.取单选位置() == 0, "确认前读取单选值");
		events.dialog.更新标题("已更新标题");
		events.dialog.置单选位置(2);
		check(events.dialog.取单选位置() == 2, "显示中更新单选值");
		events.dialog.更新按钮文本(XUI对话框.按钮_确认, "提交选择");
		events.dialog.置按钮启用(XUI对话框.按钮_确认, false);
		check(events.dialog.取复选提示选中(), "复选提示初始选中");
		Object session = field(events.dialog, "session", Object.class);
		MaterialDialog dialog = field(session, "dialog", MaterialDialog.class);
		check(dialog.getTitleView().getText().toString().equals("已更新标题"), "标题更新到原生视图");
		check(dialog.getActionButton(DialogAction.POSITIVE).getText().toString().equals("提交选择")
			&& !dialog.getActionButton(DialogAction.POSITIVE).isEnabled(), "已有按钮动态文字与禁用生效");
		dialog.getActionButton(DialogAction.NEUTRAL).performClick();
		check(events.neutral == 1 && !events.dialog.是否显示(), "中立按钮真实点击并关闭");
		events.dialog.置禁用项目(new int[0]);
		events.dialog.显示多选("多选", new String[] {"甲", "乙", "丙"}, new int[] {0, 2});
		check(Arrays.equals(events.dialog.取多选位置数组(), new int[] {0, 2}), "确认前读取多选值");
		events.dialog.置多选位置数组(new int[] {1});
		check(Arrays.equals(events.dialog.取多选位置数组(), new int[] {1}), "显示中更新多选值");
		events.dialog.关闭();
	}

	/**
	 * 验证禁用条目不会通过回调路径被选择。
	 */
	private void verifyPopup() {
		anchor = track(new XUI圆角按钮Impl(container));
		anchor.getView().setLayoutParams(new FrameLayout.LayoutParams(500, 100));
		anchor.getView().layout(0, 200, 500, 300);
		events.popup = track(new XUI弹出菜单Impl(container));
		events.popup.初始化();
		register("popup", "已关闭");
		events.popup.置禁用项目(new int[] {1});
		events.popup.置弹出位置(1, 4, 3, 2);
		events.popup.显示列表(anchor, new String[] {"启用", "禁用"}, new String[0], 300);
		XUISimplePopup<?> popup = field(events.popup, "popup", XUISimplePopup.class);
		check(popup.getListView().getAdapter().isEnabled(0), "普通菜单启用项");
		check(!popup.getListView().getAdapter().isEnabled(1), "普通菜单禁用项");
		events.popup.关闭();
		check(events.popupCloses == 1, "菜单关闭事件一次");
		events.popup.显示分组(anchor, new String[] {"分组"}, new String[0],
			new String[] {"启用", "禁用"}, new String[0], new int[] {0, 0}, 300);
		com.xuexiang.xui.widget.popupwindow.popup.XUISimpleExpandablePopup<?> grouped =
			field(events.popup, "popup", com.xuexiang.xui.widget.popupwindow.popup.XUISimpleExpandablePopup.class);
		check(grouped.getExpandableListView().getExpandableListAdapter().isChildSelectable(0, 0), "分组菜单启用子项");
		check(!grouped.getExpandableListView().getExpandableListAdapter().isChildSelectable(0, 1), "分组菜单禁用子项");
		events.popup.关闭();
		events.bottom = track(new XUI底部菜单Impl(container));
		events.bottom.初始化();
		register("bottom", "项目被单击");
		events.bottom.显示网格(new String[] {"启用", "禁用"}, new String[] {"icon.png", "icon.png"},
			new boolean[] {true, false}, new boolean[] {true, false}, 2);
		Object current = field(events.bottom, "session", Object.class);
		com.xuexiang.xui.widget.dialog.bottomsheet.BottomSheet sheet = field(current, "sheet", com.xuexiang.xui.widget.dialog.bottomsheet.BottomSheet.class);
		View disabled = sheet.getWindow().getDecorView().findViewWithTag(Integer.valueOf(1));
		check(disabled != null && !disabled.isEnabled() && disabled.getAlpha() < 1, "网格禁用项显示禁用状态");
		disabled.performClick();
		check(events.bottomSelections == 0 && events.bottom.是否显示(), "禁用网格不选择也不关闭");
		View enabled = sheet.getWindow().getDecorView().findViewWithTag(Integer.valueOf(0));
		enabled.performClick();
		check(events.bottomSelections == 1, "启用网格实际选择；次数=" + events.bottomSelections);
	}

	/**
	 * 验证两类选择器的新增配置可共同构建真实弹层。
	 */
	private void verifyPickerConfiguration() {
		XUI日期时间选择器Impl date = track(new XUI日期时间选择器Impl(container));
		date.初始化();
		date.置列标签(new String[] {"年", "月", "日", "时", "分", "秒"});
		date.置循环(true);
		date.置背景颜色(Color.WHITE, Color.LTGRAY);
		date.置行距(1.8f);
		date.置分隔线类型(1);
		date.置单位居中(true);
		date.显示();
		check(date.是否显示(), "日期自定义列标签及样式可显示");
		date.关闭();
		XUI多级选择器Impl options = track(new XUI多级选择器Impl(container));
		options.初始化();
		options.置独立项目(new String[] {"甲", "乙"}, new String[] {"一", "二"}, new String[] {"A", "B"});
		options.置列标签("项", "号", "级");
		options.置循环(true, false, true);
		options.置行距(1.6f);
		options.置下级重置(true);
		options.置单位居中(true);
		options.显示();
		check(options.是否显示(), "多级独立列循环及单位可显示");
		options.关闭();
		for (boolean reset : new boolean[] {false, true}) {
			options.置联动项目(new String[] {"甲", "甲一", "甲二", "乙", "乙一", "乙二"}, new int[] {-1, 0, 0, -1, 3, 3});
			options.置默认选项(0, 1, 0);
			options.置下级重置(reset);
			options.置单位居中(reset);
			options.显示();
			Object session = field(options, "session", Object.class);
			OptionsPickerView<?> picker = field(session, "view", OptionsPickerView.class);
			Object wheels = field(picker, "wheelOptions", Object.class);
			WheelView first = field(wheels, "wvOption1", WheelView.class);
			WheelView second = field(wheels, "wvOption2", WheelView.class);
			check(second.getCurrentItem() == 1, "联动切换前保留指定的下级选项");
			// 从原生滚轮的选中回调边界驱动联动，不直接调用 Simple 的选择事件。
			first.setCurrentItem(1);
			field(first, "onItemSelectedListener", OnItemSelectedListener.class).onItemSelected(1);
			check(second.getCurrentItem() == (reset ? 0 : 1), reset ? "上级变化后下级重置首项" : "上级变化后保留仍有效的下级索引");
			check(field(second, "isCenterLabel", Boolean.class).booleanValue() == reset, "单位文字显示策略写入真实滚轮");
			options.关闭();
		}
	}

	/**
	 * 为异步显示事件和关闭恢复验证准备自定义气泡。
	 */
	private void prepareTooltip() {
		custom = track(new 面板Impl(container));
		custom.可视(false);
		originalParent = (ViewGroup) custom.getView().getParent();
		originalParams = custom.getView().getLayoutParams();
		tooltip = track(new XUI气泡Impl(container));
		events.tooltip = tooltip;
		tooltip.初始化();
		register("tooltip", "已显示", "已关闭");
		tooltip.置颜色(Color.DKGRAY, Color.WHITE);
		tooltip.置文字大小(18);
		tooltip.置内边距(10, 10, 10, 10);
		tooltip.置圆角(6);
		tooltip.置对齐(1);
		tooltip.显示自定义(anchor, custom, 1, 0);
		check(custom.getView().getParent() != originalParent, "气泡内容实际借出");
	}

	/**
	 * 验证搜索展开收起通知及公告增删的数据行为。
	 */
	private void verifySearchAndMarquee() {
		events.search = track(new XUI搜索框Impl(container));
		register("search", "已展开", "已收起");
		events.search.展开搜索(false);
		events.search.关闭搜索();
		check(events.searchShows == 1 && events.searchCloses == 1, "搜索展开收起事件一致");
		events.search.置候选(new String[] {"候选甲", "候选乙"});
		events.search.置候选(new String[0]);
		events.search.显示候选();
		android.widget.ListView suggestions = events.search.getView().findViewById(R.id.suggestion_list);
		check(suggestions.getAdapter() == null, "空候选真正清理旧适配器");
		XUI跑马灯文本Impl marquee = track(new XUI跑马灯文本Impl(container));
		marquee.置公告数组(new String[] {"甲", "乙"});
		check(marquee.取公告数量() == 2, "公告数据初始化");
		marquee.插入公告(1, "新增");
		check(marquee.取公告数量() == 3, "指定位置插入公告");
		marquee.移除公告(0);
		check(marquee.取公告数量() == 2, "删除公告");
		marquee.置公告数组(new String[0]);
		check(marquee.取公告数量() == 0, "空数组清除旧公告");
	}

	/**
	 * 将组件计入逆序资源释放范围。
	 */
	private <T extends 组件> T track(T component) {
		if (component instanceof 面板Impl && ((面板Impl) component).getView() == null) {
			((面板Impl) component).布局(IntegerVariant.getIntegerVariant(组件.布局_单帧));
		}
		created.add(component);
		return component;
	}

	/**
	 * 登记目标字段上的真实事件。
	 */
	private void register(String field, String... names) {
		for (String name : names) {
			EventDispatcher.registerEvent(events, field, name);
		}
	}

	/**
	 * 等待实际布局和短动画，避免在界面线程阻塞。
	 */
	private void settle() throws InterruptedException {
		Thread.sleep(650);
		onUi(new Runnable() {
			@Override
			public void run() {
				// 与前序界面消息同步。
			}
		});
	}

	/**
	 * 将界面线程中的失败完整传回测试运行器。
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
	 * 读取必要的会话或原生子视图，只供设备断言使用。
	 */
	private static <T> T field(Object owner, String name, Class<T> type) {
		for (Class<?> current = owner.getClass(); current != null; current = current.getSuperclass()) {
			try {
				Field member = current.getDeclaredField(name);
				member.setAccessible(true);
				return type.cast(member.get(owner));
			} catch (NoSuchFieldException missing) {
				// 继续查找公共基类的私有会话。
			} catch (ReflectiveOperationException failure) {
				throw new AssertionError(failure);
			}
		}
		throw new AssertionError("Missing field: " + name);
	}

	/**
	 * 仅累计实际通过的断言。
	 */
	private void check(boolean condition, String message) {
		if (!condition) {
			throw new AssertionError(message);
		}
		assertions++;
	}

	/**
	 * 独立记录每个真实事件，避免直接调用事件方法伪造验证。
	 */
	public static final class Events {
		public XUI九宫格图片Impl grid;
		int gridLongIndex = -1;
		final java.util.Set<Integer> gridLoaded = new java.util.HashSet<Integer>();
		final java.util.Set<Integer> gridFailed = new java.util.HashSet<Integer>();

		public void grid$图片被长按(int index, String image) {
			gridLongIndex = index;
		}

		public void grid$图片加载完成(int index, String image) {
			gridLoaded.add(index);
		}

		public void grid$图片加载失败(int index, String image) {
			gridFailed.add(index);
		}

		public XUI分组列表Impl group;
		public XUI校验编辑框Impl validated;
		int groupClicks;
		int groupLongClicks;
		int validationFailures;
		String groupText = "";

		public void group$项目被单击(int section, int index, String text) {
			groupClicks++;
		}

		public void group$项目被长按(int section, int index, String text) {
			groupLongClicks++;
			groupText = text;
		}

		public void validated$校验失败(String input, String message) {
			validationFailures++;
		}

		public XUI图片轮播Impl images;
		int imageFailures;
		public XUI超级文本Impl superText;
		public XUI分组列表项Impl row;
		public XUI标题栏Impl title;
		public XUI对话框Impl dialog;
		public XUI弹出菜单Impl popup;
		public XUI气泡Impl tooltip;
		public XUI搜索框Impl search;
		public XUI底部菜单Impl bottom;
		public XUI加载框Impl loading;
		int bottomSelections;
		int loadingCancels;
		int accessoryChanges;
		int accessoryType;
		int rowChanges;
		int actionIndex = -1;
		int neutral;
		int popupCloses;
		int tooltipShows;
		int tooltipCloses;
		int searchShows;
		int searchCloses;
		String editText = "";
		String region = "";

		public void images$图片加载失败(String image) {
			imageFailures++;
		}

		public void superText$附属选中改变(int type, boolean checked) {
			accessoryChanges++;
			accessoryType = type;
		}

		public void superText$编辑文本改变(String text) {
			editText = text;
		}

		public void superText$区域被单击(String name) {
			region = name;
		}

		public void row$开关改变(boolean checked) {
			rowChanges++;
		}

		public void title$操作被单击(int index) {
			actionIndex = index;
		}

		public void dialog$中立按钮被单击() {
			neutral++;
		}

		public void popup$已关闭() {
			popupCloses++;
		}

		public void tooltip$已显示() {
			tooltipShows++;
		}

		public void tooltip$已关闭() {
			tooltipCloses++;
		}

		public void bottom$项目被单击(int index, String text) {
			bottomSelections++;
		}

		public void loading$已取消() {
			loadingCancels++;
		}

		public void search$已展开() {
			searchShows++;
		}

		public void search$已收起() {
			searchCloses++;
		}
	}
}
