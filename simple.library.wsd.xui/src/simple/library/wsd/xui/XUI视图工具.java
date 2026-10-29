package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleDataElement;

import android.graphics.drawable.Drawable;

import android.animation.ValueAnimator;

import android.animation.AnimatorListenerAdapter;

import android.animation.Animator;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.view.View;
import android.widget.ImageView;

import com.xuexiang.xui.utils.DrawableUtils;
import com.xuexiang.xui.utils.ViewUtils;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.components.可视组件;
import simple.runtime.errors.非法参数错误;

/**
 * 组件动画、触摸区域、背景与截图工具；涉及界面的方法须在主线程调用。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class XUI视图工具 {

	/**
	 * 静态工具不创建实例。
	 */
	private XUI视图工具() {
	}

	/**
	 * 取得组件在屏幕中的矩形。
	 *
	 * @param component 已布局的可视组件。
	 * @return 依次为左、上、宽、高，单位为像素。
	 */
	@SimpleFunction
	public static int[] 取屏幕位置(可视组件 component) {
		Rect rect = ViewUtils.calculateViewScreenLocation(XUI弹层基类.getNativeView(component));
		return new int[] {rect.left, rect.top, rect.width(), rect.height()};
	}

	/**
	 * 显示组件并播放淡入动画。
	 *
	 * @param component 目标组件。
	 * @param duration 动画毫秒数，须为非负数。
	 */
	@SimpleFunction
	public static void 淡入(可视组件 component, int duration) {
		if (duration < 0) {
			throw new 非法参数错误();
		}
		ViewUtils.fadeIn(XUI弹层基类.getNativeView(component), duration, null);
	}

	/**
	 * 播放淡出动画并隐藏组件。
	 *
	 * @param component 目标组件。
	 * @param duration 动画毫秒数，须为非负数。
	 */
	@SimpleFunction
	public static void 淡出(可视组件 component, int duration) {
		if (duration < 0) {
			throw new 非法参数错误();
		}
		ViewUtils.fadeOut(XUI弹层基类.getNativeView(component), duration, null);
	}

	/**
	 * 扩展组件触摸命中区域；同一父容器的后一次调用会替换前一次扩展。
	 *
	 * @param component 已布局的目标组件。
	 * @param pixels 四周扩展的物理像素数，须为非负数。
	 */
	@SimpleFunction
	public static void 扩展触摸区域(可视组件 component, int pixels) {
		if (pixels < 0) {
			throw new 非法参数错误();
		}
		ViewUtils.expendTouchArea(XUI弹层基类.getNativeView(component), pixels);
	}

	/**
	 * 替换背景颜色并保留现有内容内边距。
	 *
	 * @param component 目标组件。
	 * @param color ARGB 颜色。
	 */
	@SimpleFunction
	public static void 置背景颜色(可视组件 component, int color) {
		ViewUtils.setBackgroundColorKeepPadding(XUI弹层基类.getNativeView(component), color);
	}

	/**
	 * 设置带圆角的纯色背景，保留内容内边距。
	 *
	 * @param component 目标组件。
	 * @param radius 圆角像素值，须为非负数。
	 * @param color ARGB 颜色。
	 */
	@SimpleFunction
	public static void 置圆角背景(可视组件 component, int radius, int color) {
		if (radius < 0) {
			throw new 非法参数错误();
		}
		ViewUtils.setBackgroundKeepingPadding(XUI弹层基类.getNativeView(component), DrawableUtils.getSolidRectDrawable(radius, color));
	}

	/**
	 * 设置普通与按压状态背景，保留内容内边距。
	 *
	 * @param component 目标组件。
	 * @param radius 圆角像素值。
	 * @param normal 普通颜色。
	 * @param pressed 按压颜色。
	 */
	@SimpleFunction
	public static void 置按压背景(可视组件 component, int radius, int normal, int pressed) {
		if (radius < 0) {
			throw new 非法参数错误();
		}
		ViewUtils.setBackgroundKeepingPadding(XUI弹层基类.getNativeView(component), DrawableUtils.getDrawable(radius, pressed, normal));
	}

	/**
	 * 对图片组件设置着色。
	 *
	 * @param component 原生视图为 ImageView 的组件。
	 * @param color ARGB 着色。
	 */
	@SimpleFunction
	public static void 置图片着色(可视组件 component, int color) {
		View view = XUI弹层基类.getNativeView(component);
		if (!(view instanceof ImageView)) {
			throw new 非法参数错误();
		}
		ViewUtils.setImageViewTintColor((ImageView) view, color);
	}

	/**
	 * 把组件当前绘制结果保存为 PNG；不包含独立 Surface 或弹出窗口。
	 *
	 * @param component 已完成布局的组件。
	 * @param path 输出路径，目录须已存在。
	 * @param scale 缩放倍率，须大于零。
	 */
	@SimpleFunction
	public static void 保存截图(可视组件 component, String path, float scale) {
		View view = XUI弹层基类.getNativeView(component);
		if (!(scale > 0) || Float.isInfinite(scale) || view.getWidth() <= 0 || view.getHeight() <= 0) {
			throw new 非法参数错误();
		}
		Bitmap bitmap = DrawableUtils.createBitmapFromView(view, scale);
		if (bitmap == null) {
			throw new ComponentStateException("无法创建组件截图。");
		}
		try {
			ComponentSupport.saveImage(bitmap, ComponentSupport.resolveOutputFile(path).getAbsolutePath());
		} finally {
			bitmap.recycle();
		}
	}

	/**
	 * 显示组件并按指定方向播放滑入动画。
	 *
	 * @param component 已完成布局的目标组件。
	 * @param direction 方向常量。
	 * @param duration 非负动画时长，单位为毫秒。
	 */
	@SimpleFunction
	public static void 滑入(可视组件 component, int direction, int duration) {
		View target = requireAnimationTarget(component, duration);
		if (direction < 0 || direction >= ViewUtils.Direction.values().length) {
			throw new 非法参数错误();
		}
		ViewUtils.slideIn(target, duration, null, ViewUtils.Direction.values()[direction]);
	}

	/**
	 * 按指定方向播放滑出动画并隐藏组件。
	 *
	 * @param component 已完成布局的目标组件。
	 * @param direction 方向常量。
	 * @param duration 非负动画时长，单位为毫秒。
	 */
	@SimpleFunction
	public static void 滑出(可视组件 component, int direction, int duration) {
		View target = requireAnimationTarget(component, duration);
		if (direction < 0 || direction >= ViewUtils.Direction.values().length) {
			throw new 非法参数错误();
		}
		ViewUtils.slideOut(target, duration, null, ViewUtils.Direction.values()[direction]);
	}

	/**
	 * 在原背景上播放颜色闪烁，结束或组件移出窗口时恢复原背景。
	 *
	 * @param component 已完成布局的目标组件。
	 * @param color ARGB 闪烁颜色。
	 * @param duration 非负总时长，单位为毫秒。
	 */
	@SimpleFunction
	public static void 背景闪烁(可视组件 component, int color, int duration) {
		View target = requireAnimationTarget(component, duration);
		BackgroundAnimation.start(target, color & 0x00FFFFFF, color, duration, true);
	}

	/**
	 * 播放两种背景色之间的渐变，结束或组件移出窗口时恢复原背景。
	 *
	 * @param component 已完成布局的目标组件。
	 * @param start ARGB 开始颜色。
	 * @param end ARGB 结束颜色。
	 * @param duration 非负总时长，单位为毫秒。
	 */
	@SimpleFunction
	public static void 背景渐变(可视组件 component, int start, int end, int duration) {
		View target = requireAnimationTarget(component, duration);
		BackgroundAnimation.start(target, start, end, duration, false);
	}

	/**
	 * 停止本工具启动的背景动画并恢复原背景。
	 *
	 * @param component 目标组件。
	 */
	@SimpleFunction
	public static void 停止背景动画(可视组件 component) {
		View target = XUI弹层基类.getNativeView(component);
		Object running = target.getTag(R.id.es4a_xui_background_animation);
		if (running instanceof BackgroundAnimation) {
			((BackgroundAnimation) running).animator.cancel();
		}
	}

	/**
	 * 从左向右。
	 */
	@SimpleDataElement
	public static final int 方向_向右 = 0;

	/**
	 * 从上向下。
	 */
	@SimpleDataElement
	public static final int 方向_向下 = 1;

	/**
	 * 从右向左。
	 */
	@SimpleDataElement
	public static final int 方向_向左 = 2;

	/**
	 * 从下向上。
	 */
	@SimpleDataElement
	public static final int 方向_向上 = 3;

	/**
	 * 拒绝未附着或尚无尺寸的动画目标，避免返回成功但看不到任何移动。
	 *
	 * @param component 目标组件。
	 * @param duration 毫秒数。
	 * @return 当前可用原生视图。
	 */
	private static View requireAnimationTarget(可视组件 component, int duration) {
		View target = XUI弹层基类.getNativeView(component);
		if (duration < 0 || !target.isAttachedToWindow() || target.getWidth() <= 0 || target.getHeight() <= 0) {
			throw new 非法参数错误();
		}
		return target;
	}

	/**
	 * 每个视图仅持有一次背景动画；替换、结束、取消和移出窗口都走同一释放边界。
	 */
	private static final class BackgroundAnimation implements View.OnAttachStateChangeListener {
		private final View view;
		private final Drawable background;
		private final ValueAnimator animator;
		private boolean finished;

		/**
		 * 在开始之前记录原背景，清理不能覆盖后来替换的新动画。
		 */
		private BackgroundAnimation(View view, int start, int end, int duration, boolean blink) {
			this.view = view;
			background = view.getBackground();
			animator = ValueAnimator.ofArgb(start, end);
			animator.setDuration(blink ? duration / 2L : duration);
			if (blink) {
				animator.setRepeatCount(1);
				animator.setRepeatMode(ValueAnimator.REVERSE);
			}
			animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
				@Override
				public void onAnimationUpdate(ValueAnimator running) {
					if (!finished) {
						ViewUtils.setBackgroundColorKeepPadding(BackgroundAnimation.this.view, (Integer) running.getAnimatedValue());
					}
				}
			});
			animator.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator running) {
					finish();
				}

				@Override
				public void onAnimationCancel(Animator running) {
					finish();
				}
			});
		}

		/**
		 * 先取消前一项，再记录本次背景，避免将中间动画色当作原背景。
		 */
		static void start(View view, int first, int last, int duration, boolean blink) {
			Object previous = view.getTag(R.id.es4a_xui_background_animation);
			if (previous instanceof BackgroundAnimation) {
				((BackgroundAnimation) previous).animator.cancel();
			}
			BackgroundAnimation current = new BackgroundAnimation(view, first, last, duration, blink);
			view.setTag(R.id.es4a_xui_background_animation, current);
			view.addOnAttachStateChangeListener(current);
			current.animator.start();
		}

		/**
		 * 幂等归还背景与监听，旧回调不得清除新动画的状态。
		 */
		private void finish() {
			if (finished) {
				return;
			}
			finished = true;
			view.removeOnAttachStateChangeListener(this);
			if (view.getTag(R.id.es4a_xui_background_animation) == this) {
				ViewUtils.setBackgroundKeepingPadding(view, background);
				view.setTag(R.id.es4a_xui_background_animation, null);
			}
			animator.removeAllUpdateListeners();
			animator.removeAllListeners();
		}

		@Override
		public void onViewAttachedToWindow(View target) {
		}

		@Override
		public void onViewDetachedFromWindow(View target) {
			animator.cancel();
		}
	}

}
