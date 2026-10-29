package simple.library.wsd.alerter;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.LinkedHashMap;
import java.util.Map;

import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ProgressBar;

import com.tapadoo.alerter.Alert;
import com.tapadoo.alerter.Alerter;
import com.tapadoo.alerter.OnHideAlertListener;
import com.tapadoo.alerter.OnShowAlertListener;

import simple.runtime.android.MainActivity;
import simple.runtime.components.组件容器;
import simple.runtime.components.impl.组件Impl;
import simple.runtime.components.impl.android.util.ImageUtil;
import simple.runtime.events.EventDispatcher;

/**
 * 将 Alerter 的构建、动画和回调接入 Simple 非可视组件生命周期。
 *
 * <p>每次显示重建第三方对象，防止已移除的视图被再次加入。跨实例的显示请求
 * 按界面线程顺序替换；清理只处理自身视图，不使用会隐藏其他实例的全局关闭。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class 提示条Impl extends 组件Impl implements 提示条 {

	/* 所有实例共用窗口中的显示位置，弱引用不延长组件生命周期。 */

	private static WeakReference<Session> activeSession = new WeakReference<Session>(null);
	private static long displayRevision;

	/* 宿主及下次显示采用的配置；不在属性设置时创建或替换第三方视图。 */

	private final MainActivity activity;
	private final Handler handler = new Handler(Looper.getMainLooper());
	private String title = "";
	private String text = "";
	private long duration = 3000L;
	private int backgroundColor = 0xFF424242;
	private String backgroundImage = "";
	private String icon = "";
	private String rightIcon = "";
	private int iconColor = 0xFFFFFFFF;
	private int rightIconColor = 0xFFFFFFFF;
	private int iconSize = -1;
	private int rightIconSize = -1;
	private boolean showIcon = true;
	private boolean showRightIcon = false;
	private boolean iconPulse = true;
	private boolean rightIconPulse = true;
	private boolean clickAnimation = true;
	private boolean dismissOnClick = true;
	private boolean infiniteDuration = false;
	private boolean swipeToDismiss = false;
	private boolean vibration = false;
	private boolean progress = false;
	private int progressColor = 0xFFFFFFFF;
	private boolean blockOutsideTouch = false;
	private int layoutGravity = 48;
	private int contentGravity = 16;
	private int rightIconGravity = 8388613;
	private int enterAnimation = 0;
	private int exitAnimation = 0;
	private float elevation = -1.0f;
	private String sound = "";
	private int iconPixels = -1;
	private int rightIconPixels = -1;
	private Typeface titleTypeface;
	private Typeface textTypeface;
	private Typeface buttonTypeface;
	private final Map<Integer, ButtonSpec> buttons = new LinkedHashMap<Integer, ButtonSpec>();

	/* 当前会话和宿主销毁监听。 */

	private Session session;
	private boolean destroyed;
	private final MainActivity.OnDestroyListener destroyListener = new MainActivity.OnDestroyListener() {
		@Override
		public void onDestroy() {
			// 宿主正在遍历监听列表，此时不在回调中移除监听。
			dispose(false);
		}
	};

	/**
	 * 建立提示条组件，并监听宿主销毁以移除当前界面。
	 *
	 * @param container 容纳非可视组件的窗口。
	 */
	public 提示条Impl(组件容器 container) {
		super(container);

		activity = MainActivity.getContext();
		activity.addOnDestroyListener(destroyListener);
	}

	/* 组件事件沿用接口文档，回调中的状态修改在分派前完成。 */

	@Override
	public void 被显示() {
		EventDispatcher.dispatchEvent(this, "被显示");
	}

	@Override
	public void 被隐藏() {
		EventDispatcher.dispatchEvent(this, "被隐藏");
	}

	@Override
	public void 被单击() {
		EventDispatcher.dispatchEvent(this, "被单击");
	}

	@Override
	public void 按钮被单击(int id) {
		EventDispatcher.dispatchEvent(this, "按钮被单击", id);
	}

	/* 下次显示采用的配置 */

	@Override
	public String 标题() {
		return title;
	}

	@Override
	public void 标题(String value) {
		check();
		requireText(value, "标题");
		title = value;
	}

	@Override
	public String 内容() {
		return text;
	}

	@Override
	public void 内容(String value) {
		check();
		requireText(value, "内容");
		text = value;
	}

	@Override
	public long 显示时间() {
		return duration;
	}

	@Override
	public void 显示时间(long value) {
		check();
		if (value < 0) {
			throw new IllegalArgumentException("显示时间不符合约定范围。");
		}

		duration = value;
	}

	@Override
	public int 背景颜色() {
		return backgroundColor;
	}

	@Override
	public void 背景颜色(int value) {
		check();
		backgroundColor = value;
	}

	@Override
	public String 背景图片() {
		return backgroundImage;
	}

	@Override
	public void 背景图片(String value) {
		check();
		requireText(value, "背景图片");
		backgroundImage = value;
	}

	@Override
	public String 图标() {
		return icon;
	}

	@Override
	public void 图标(String value) {
		check();
		requireText(value, "图标");
		icon = value;
	}

	@Override
	public String 右侧图标() {
		return rightIcon;
	}

	@Override
	public void 右侧图标(String value) {
		check();
		requireText(value, "右侧图标");
		rightIcon = value;
	}

	@Override
	public int 图标颜色() {
		return iconColor;
	}

	@Override
	public void 图标颜色(int value) {
		check();
		iconColor = value;
	}

	@Override
	public int 右侧图标颜色() {
		return rightIconColor;
	}

	@Override
	public void 右侧图标颜色(int value) {
		check();
		rightIconColor = value;
	}

	@Override
	public int 图标尺寸() {
		return iconSize;
	}

	@Override
	public void 图标尺寸(int value) {
		check();
		if (value < -1) {
			throw new IllegalArgumentException("图标尺寸不符合约定范围。");
		}

		iconSize = value;
		iconPixels = -1;
	}

	@Override
	public int 右侧图标尺寸() {
		return rightIconSize;
	}

	@Override
	public void 右侧图标尺寸(int value) {
		check();
		if (value < -1) {
			throw new IllegalArgumentException("右侧图标尺寸不符合约定范围。");
		}

		rightIconSize = value;
		rightIconPixels = -1;
	}

	@Override
	public boolean 显示图标() {
		return showIcon;
	}

	@Override
	public void 显示图标(boolean value) {
		check();
		showIcon = value;
	}

	@Override
	public boolean 显示右侧图标() {
		return showRightIcon;
	}

	@Override
	public void 显示右侧图标(boolean value) {
		check();
		showRightIcon = value;
	}

	@Override
	public boolean 图标动画() {
		return iconPulse;
	}

	@Override
	public void 图标动画(boolean value) {
		check();
		iconPulse = value;
	}

	@Override
	public boolean 右侧图标动画() {
		return rightIconPulse;
	}

	@Override
	public void 右侧图标动画(boolean value) {
		check();
		rightIconPulse = value;
	}

	@Override
	public boolean 单击动画() {
		return clickAnimation;
	}

	@Override
	public void 单击动画(boolean value) {
		check();
		clickAnimation = value;
	}

	@Override
	public boolean 单击关闭() {
		return dismissOnClick;
	}

	@Override
	public void 单击关闭(boolean value) {
		check();
		dismissOnClick = value;
	}

	@Override
	public boolean 始终显示() {
		return infiniteDuration;
	}

	@Override
	public void 始终显示(boolean value) {
		check();
		infiniteDuration = value;
	}

	@Override
	public boolean 滑动关闭() {
		return swipeToDismiss;
	}

	@Override
	public void 滑动关闭(boolean value) {
		check();
		swipeToDismiss = value;
	}

	@Override
	public boolean 启用震动() {
		return vibration;
	}

	@Override
	public void 启用震动(boolean value) {
		check();
		vibration = value;
	}

	@Override
	public boolean 显示进度() {
		return progress;
	}

	@Override
	public void 显示进度(boolean value) {
		check();
		progress = value;
	}

	@Override
	public int 进度颜色() {
		return progressColor;
	}

	@Override
	public void 进度颜色(int value) {
		check();
		progressColor = value;
	}

	@Override
	public boolean 禁用外部触摸() {
		return blockOutsideTouch;
	}

	@Override
	public void 禁用外部触摸(boolean value) {
		check();
		blockOutsideTouch = value;
	}

	@Override
	public int 位置() {
		return layoutGravity;
	}

	@Override
	public void 位置(int value) {
		check();
		layoutGravity = value;
	}

	@Override
	public int 内容对齐() {
		return contentGravity;
	}

	@Override
	public void 内容对齐(int value) {
		check();
		contentGravity = value;
	}

	@Override
	public int 右侧图标对齐() {
		return rightIconGravity;
	}

	@Override
	public void 右侧图标对齐(int value) {
		check();
		rightIconGravity = value;
	}

	@Override
	public int 进入动画资源() {
		return enterAnimation;
	}

	@Override
	public void 进入动画资源(int value) {
		check();
		if (value < 0) {
			throw new IllegalArgumentException("进入动画资源不符合约定范围。");
		}

		enterAnimation = value;
	}

	@Override
	public int 退出动画资源() {
		return exitAnimation;
	}

	@Override
	public void 退出动画资源(int value) {
		check();
		if (value < 0) {
			throw new IllegalArgumentException("退出动画资源不符合约定范围。");
		}

		exitAnimation = value;
	}

	@Override
	public float 阴影高度() {
		return elevation;
	}

	@Override
	public void 阴影高度(float value) {
		check();
		if (!Float.isFinite(value) || value < -1) {
			throw new IllegalArgumentException("阴影高度不符合约定范围。");
		}

		elevation = value;
	}

	@Override
	public String 提示声音() {
		return sound;
	}

	@Override
	public void 提示声音(String value) {
		check();
		requireText(value, "提示声音");
		sound = value;
	}

	@Override
	public boolean 正在显示() {
		return session != null && !session.ended;
	}

	/* 显示与交互 */

	@Override
	public void 显示() {
		check();
		if (activity.isFinishing() || activity.isDestroyed()) {
			throw new IllegalStateException("当前窗口不能显示提示条。");
		}

		// 隐藏事件允许重入；若回调又显示了更新的提示条，不覆盖该次请求。
		long revision = ++displayRevision;
		Session previous = activeSession.get();
		if (previous != null) {
			previous.owner.end(previous, true);
		}

		if (destroyed || revision != displayRevision) {
			return;
		}

		final Session next = new Session(this, dismissOnClick);
		session = next;
		activeSession = new WeakReference<Session>(next);
		try {
			Alerter builder = Alerter.create(activity);
			configure(builder, next);
			next.alert = builder.show();
			if (next.alert == null) {
				throw new IllegalStateException("提示条未能创建视图。");
			}

			// Alerter 7.2.4 只给 progressDrawable 着色，旋转进度环使用 indeterminateDrawable。
			// 最低 API 24 已支持此着色接口，在视图加入窗口前补齐本次显示配置。
			if (progress) {
				ProgressBar indicator = next.alert.findViewById(com.tapadoo.alerter.R.id.pbProgress);
				if (indicator != null) {
					indicator.setIndeterminateTintList(ColorStateList.valueOf(progressColor));
				}
			}

			// 第三方 show 会排队加入视图。清理任务也排到其后，覆盖显示后立即关闭的路径。
			next.alert.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
				@Override
				public void onViewAttachedToWindow(View view) {
					if (next.ended || destroyed) {
						scheduleCleanup(next);
					}
				}

				@Override
				public void onViewDetachedFromWindow(View view) {
					// 等 ViewGroup 完成移除，再通知用户，避免在移除过程中重入布局。
					handler.post(new Runnable() {
						@Override
						public void run() {
							end(next, true);
						}
					});
				}
			});
		} catch (RuntimeException | LinkageError error) {
			// 资源或依赖缺失必须保留真实异常，不伪造显示成功。
			end(next, false);
			throw error;
		}
	}

	@Override
	public void 关闭() {
		check();
		final Session current = session;
		if (current == null || current.ended || current.closing) {
			return;
		}

		current.closing = true;
		handler.post(new Runnable() {
			@Override
			public void run() {
				if (current.ended) {
					return;
				}

				if (current.alert == null || current.alert.getParent() == null) {
					end(current, true);
					return;
				}

				// 使用 Alert 自身的关闭逻辑及退出动画，只作用于本次视图。
				// 公开 onTouch 回调可取消自动关闭计时，onClick 负责启动原生退出动画。
				current.alert.onTouch(current.alert, true);
				current.alert.setDismissible(true);
				current.alert.onClick(current.alert);
			}
		});
	}

	@Override
	public void 添加按钮(int id, String caption, int style) {
		check();
		requireText(caption, "按钮标题");
		if (style < -1) {
			throw new IllegalArgumentException("按钮样式必须为 -1、0 或有效资源编号。");
		}

		buttons.put(id, new ButtonSpec(id, caption, style));
	}

	@Override
	public void 清空按钮() {
		check();
		buttons.clear();
	}

	@Override
	public void 更新标题(String value) {
		标题(value);
		if (session != null && session.alert != null) {
			session.alert.setTitle(value);
		}
	}

	@Override
	public void 更新内容(String value) {
		内容(value);
		if (session != null && session.alert != null) {
			session.alert.setText(value);
		}
	}

	@Override
	public void 置标题资源(int resourceId) {
		check();
		标题(activity.getString(resourceId));
	}

	@Override
	public void 置内容资源(int resourceId) {
		check();
		内容(activity.getString(resourceId));
	}

	@Override
	public void 置背景颜色资源(int resourceId) {
		check();
		背景颜色(activity.getResources().getColor(resourceId, activity.getTheme()));
	}

	@Override
	public void 置进度颜色资源(int resourceId) {
		check();
		进度颜色(activity.getResources().getColor(resourceId, activity.getTheme()));
	}

	@Override
	public void 置图标像素尺寸(int pixels) {
		check();
		iconPixels = requireSize(pixels);
	}

	@Override
	public void 置右侧图标像素尺寸(int pixels) {
		check();
		rightIconPixels = requireSize(pixels);
	}

	@Override
	public void 置背景图片资源(int resourceId) {
		背景图片(resourceImage(resourceId));
	}

	@Override
	public void 置图标资源(int resourceId) {
		图标(resourceImage(resourceId));
	}

	@Override
	public void 置右侧图标资源(int resourceId) {
		右侧图标(resourceImage(resourceId));
	}

	@Override
	public void 置标题字体(String family, int style) {
		check();
		titleTypeface = typeface(family, style);
	}

	@Override
	public void 置内容字体(String family, int style) {
		check();
		textTypeface = typeface(family, style);
	}

	@Override
	public void 置按钮字体(String family, int style) {
		check();
		buttonTypeface = typeface(family, style);
	}

	/**
	 * 移除本实例视图、取消事件并注销宿主监听；可以重复调用。
	 *
	 * @throws IllegalStateException 未销毁的组件在非界面线程调用。
	 */
	@Override
	public void 销毁() {
		if (destroyed) {
			return;
		}

		check();
		dispose(true);
	}

	/* 第三方构建与会话边界 */

	/**
	 * 把本次配置写入第三方对象，并绑定仅对本次显示有效的回调。
	 *
	 * @param builder 尚未显示的第三方构建器。
	 * @param current 本次显示会话。
	 */
	private void configure(Alerter builder, final Session current) {
		builder.setTitle(title).setText(text).setDuration(duration);
		builder.setBackgroundColorInt(backgroundColor);
		builder.showIcon(showIcon).showRightIcon(showRightIcon);
		builder.enableIconPulse(iconPulse).enableRightIconPulse(rightIconPulse);
		builder.enableClickAnimation(clickAnimation).enableInfiniteDuration(infiniteDuration);
		builder.setDismissable(dismissOnClick).enableVibration(vibration);
		builder.enableProgress(progress).setProgressColorInt(progressColor);
		builder.setLayoutGravity(layoutGravity).setContentGravity(contentGravity);
		builder.setRightIconPosition(rightIconGravity);
		builder.setIconColorFilter(iconColor).setRightIconColorFilter(rightIconColor);
		builder.setSound(sound.length() == 0 ? null : Uri.parse(sound));

		// 图片沿用运行库的资源、资产和文件标识，不暴露 Android Drawable 类型。
		if (backgroundImage.length() > 0) {
			builder.setBackgroundDrawable(image(backgroundImage));
		}
		if (icon.length() > 0) {
			builder.setIcon(image(icon));
		}
		if (rightIcon.length() > 0) {
			builder.setRightIcon(image(rightIcon));
		}

		if (iconPixels >= 0) {
			builder.setIconPixelSize(iconPixels);
		} else if (iconSize >= 0) {
			builder.setIconSize(iconSize);
		}
		if (rightIconPixels >= 0) {
			builder.setRightIconPixelSize(rightIconPixels);
		} else if (rightIconSize >= 0) {
			builder.setRightIconSize(rightIconSize);
		}

		// 所有支持系统均高于 API 21，可直接设置背景抬升高度；这不是布局高度。
		if (elevation >= 0) {
			builder.setElevation(elevation * activity.getResources().getDisplayMetrics().density);
		}
		if (swipeToDismiss) {
			builder.enableSwipeToDismiss();
		}
		if (blockOutsideTouch) {
			builder.disableOutsideTouch();
		}
		if (enterAnimation != 0) {
			builder.setEnterAnimation(enterAnimation);
		}
		if (exitAnimation != 0) {
			builder.setExitAnimation(exitAnimation);
		}

		if (titleTypeface != null) {
			builder.setTitleTypeface(titleTypeface);
		}
		if (textTypeface != null) {
			builder.setTextTypeface(textTypeface);
		}
		if (buttonTypeface != null) {
			builder.setButtonTypeface(buttonTypeface);
		}

		// 回调先核对会话，旧动画或旧视图不能更新新提示条的状态。
		builder.setOnShowListener(new OnShowAlertListener() {
			@Override
			public void onShow() {
				if (isCurrent(current) && !current.closing) {
					被显示();
				}
			}
		});
		builder.setOnHideListener(new OnHideAlertListener() {
			@Override
			public void onHide() {
				end(current, true);
			}
		});
		builder.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (!isCurrent(current) || current.closing) {
					return;
				}

				被单击();
				if (isCurrent(current) && current.dismissOnClick) {
					关闭();
				}
			}
		});

		for (final ButtonSpec button : buttons.values()) {
			int style = button.style;
			if (style == -1) {
				// 使用最终应用资源包中的默认样式；缺失时明确报告资源未完整交付。
				style = activity.getResources().getIdentifier("AlertButton", "style", activity.getPackageName());
				if (style == 0) {
					throw new IllegalStateException("提示条缺少 AlertButton 样式资源。");
				}
			}

			builder.addButton(button.caption, style, new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					if (isCurrent(current) && !current.closing) {
						按钮被单击(button.id);
					}
				}
			});
		}
	}

	/**
	 * 判断回调是否仍属于本实例有效的显示会话。
	 *
	 * @param current 产生回调的会话。
	 * @return 尚未销毁且本次显示未结束时为真。
	 */
	private boolean isCurrent(Session current) {
		return !destroyed && session == current && !current.ended;
	}

	/**
	 * 结束一次显示，先完成状态和视图清理，再允许用户事件重入。
	 *
	 * @param current 待结束的会话，空引用时不处理。
	 * @param notify 是否派发被隐藏事件。
	 */
	private void end(Session current, boolean notify) {
		if (current == null || current.ended) {
			return;
		}

		current.ended = true;
		if (session == current) {
			session = null;
		}
		if (activeSession.get() == current) {
			activeSession.clear();
		}

		cleanup(current);
		scheduleCleanup(current);
		if (notify && !destroyed) {
			被隐藏();
		}
	}

	/**
	 * 在第三方排队加入视图之后再次清理，防止关闭后仍有待加入的视图。
	 *
	 * @param current 已结束或宿主已销毁的会话。
	 */
	private void scheduleCleanup(final Session current) {
		handler.post(new Runnable() {
			@Override
			public void run() {
				cleanup(current);
			}
		});
	}

	/**
	 * 停止本次自动关闭计时和动画，只移除本次提示条视图。
	 *
	 * @param current 待清理的会话。
	 */
	private void cleanup(Session current) {
		Alert alert = current.alert;
		if (alert == null) {
			return;
		}

		alert.onTouch(alert, true);
		alert.clearAnimation();
		alert.animate().cancel();
		ViewParent parent = alert.getParent();
		if (parent instanceof ViewGroup) {
			((ViewGroup) parent).removeView(alert);
		}
	}

	/**
	 * 释放当前显示、配置资源及事件绑定，不派发用户交互事件。
	 *
	 * @param removeListener 主动销毁时注销宿主监听；宿主回调中不修改遍历中的列表。
	 */
	private void dispose(boolean removeListener) {
		if (destroyed) {
			return;
		}

		destroyed = true;
		end(session, false);
		buttons.clear();
		titleTypeface = null;
		textTypeface = null;
		buttonTypeface = null;
		if (removeListener) {
			activity.removeOnDestroyListener(destroyListener);
		}

		super.销毁();
	}

	/* 参数与运行条件 */

	/**
	 * 检查需要读写界面或配置的调用是否有效。
	 *
	 * @throws IllegalStateException 不在界面线程调用或组件已经销毁。
	 */
	private void check() {
		if (Looper.myLooper() != Looper.getMainLooper()) {
			throw new IllegalStateException("提示条只能在界面线程调用。");
		}
		if (destroyed) {
			throw new IllegalStateException("提示条已经销毁。");
		}
	}

	/**
	 * 验证公开文本参数，不将空引用静默转换为空文本。
	 *
	 * @param value 待验证的文本。
	 * @param name 用于诊断的参数名称。
	 * @throws IllegalArgumentException 文本为空引用。
	 */
	private static void requireText(String value, String name) {
		if (value == null) {
			throw new IllegalArgumentException(name + "不能是空引用。");
		}
	}

	/**
	 * 验证显式设置的像素尺寸。
	 *
	 * @param pixels 像素尺寸。
	 * @return 验证后的尺寸。
	 * @throws IllegalArgumentException 尺寸小于零。
	 */
	private static int requireSize(int pixels) {
		if (pixels < 0) {
			throw new IllegalArgumentException("图标像素尺寸不能小于零。");
		}
		return pixels;
	}

	/**
	 * 读取图片，并保留原始读取异常作为诊断原因。
	 *
	 * @param value 运行库支持的图片标识。
	 * @return 图片可绘制对象。
	 * @throws IllegalArgumentException 图片不可读取。
	 */
	private static Drawable image(String value) {
		try {
			return ImageUtil.getDrawable(value);
		} catch (IOException error) {
			throw new IllegalArgumentException("无法读取提示条图片：" + value, error);
		}
	}

	/**
	 * 将图片编号转为运行库统一的图片标识文本，0 表示使用默认配置。
	 *
	 * @param resourceId 图片资源编号。
	 * @return 非零编号对应的十进制文本，零返回空文本。
	 * @throws IllegalArgumentException 资源编号小于零。
	 */
	private static String resourceImage(int resourceId) {
		if (resourceId < 0) {
			throw new IllegalArgumentException("图片资源编号不能小于零。");
		}
		return resourceId == 0 ? "" : Integer.toString(resourceId);
	}

	/**
	 * 按系统字体族、资产或外部文件及字形创建字体。
	 *
	 * @param family 字体族、@assets/字体文件名或绝对路径；空文本使用默认字体。
	 * @param style 普通、粗体、斜体或粗斜体，对应 0 至 3。
	 * @return 可供标题、正文或按钮使用的字体。
	 * @throws IllegalArgumentException 字体族为空引用或样式不在 0 至 3。
	 */
	private static Typeface typeface(String family, int style) {
		requireText(family, "字体名称");
		if (style < 0 || style > 3) {
			throw new IllegalArgumentException("字体样式必须在 0 至 3 之间。");
		}
		Typeface base;
		if (family.startsWith("@assets/")) {
			base = Typeface.createFromAsset(MainActivity.getContext().getAssets(), family.substring(8));
		} else if (family.startsWith("/")) {
			base = Typeface.createFromFile(family);
		} else {
			base = Typeface.create(family.length() == 0 ? null : family, Typeface.NORMAL);
		}
		return Typeface.create(base, style);
	}

	/**
	 * 一次显示的视图及关闭状态，与下一次显示的配置相互独立。
	 *
	 * @author 树先生 xhwsd@qq.com
	 */
	private static final class Session {

		private final 提示条Impl owner;
		private final boolean dismissOnClick;
		private Alert alert;
		private boolean closing;
		private boolean ended;

		/**
		 * 记录本次显示的拥有者和点击关闭设置。
		 *
		 * @param owner 拥有此次显示的组件。
		 * @param dismissOnClick 点击主体后是否关闭。
		 */
		private Session(提示条Impl owner, boolean dismissOnClick) {
			this.owner = owner;
			this.dismissOnClick = dismissOnClick;
		}
	}

	/**
	 * 下次显示使用的按钮配置，标记由按钮事件原样返回。
	 *
	 * @author 树先生 xhwsd@qq.com
	 */
	private static final class ButtonSpec {

		private final int id;
		private final String caption;
		private final int style;

		/**
		 * 保存按钮标记、标题和样式，不提前创建界面控件。
		 *
		 * @param id 用户指定的按钮标记。
		 * @param caption 按钮标题。
		 * @param style 样式编号，-1 表示默认样式。
		 */
		private ButtonSpec(int id, String caption, int style) {
			this.id = id;
			this.caption = caption;
			this.style = style;
		}
	}
}
