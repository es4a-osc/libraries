package simple.library.wsd.xui;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import com.xuexiang.xui.widget.guidview.DismissListener;
import com.xuexiang.xui.widget.guidview.FocusShape;
import com.xuexiang.xui.widget.guidview.GuideCaseView;

import simple.runtime.android.MainActivity;
import simple.runtime.components.可视组件;
import simple.runtime.components.组件容器;
import simple.runtime.errors.非法参数错误;

/**
 * 按显示会话管理引导步骤、样式和推进，隔离旧动画回调及事件中的重入操作。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI引导Impl extends XUI弹层基类 implements XUI引导 {

	private String onceIdentifier = "";
	private int offsetX;
	private int offsetY;
	private boolean focusAnimated = true;
	private int enterDuration = 200;
	private int exitDuration;

	private final List<Step> steps = new ArrayList<Step>();
	private Session session;
	private Integer maskColor;
	private Integer textColor;
	private int textSize;
	private int textGravity;
	private Integer borderColor;
	private int borderWidth;
	private int focusPadding;
	private int cornerRadius;
	private boolean clickAdvance = true;

	/**
	 * 一个步骤保存目标及说明，不复制或改变目标组件。
	 */
	private static final class Step {
		可视组件 target;
		String text;
		boolean rectangle;
		int[] region;
		int image;
		int imageWidth;
		int imageHeight;
		int imageGravity;
		int imageOffsetX;
		int imageOffsetY;
	}

	/**
	 * 本次显示的数据和样式快照，回调只处理它所属的会话与视图。
	 */
	private static final class Session {
		List<Step> steps;
		GuideCaseView view;
		int index;
		Integer maskColor;
		Integer textColor;
		int textSize;
		int textGravity;
		Integer borderColor;
		int borderWidth;
		int padding;
		int radius;
		boolean clickAdvance;
		String onceIdentifier;
		int offsetX;
		int offsetY;
		boolean focusAnimated;
		int enterDuration;
		int exitDuration;
	}

	/**
	 * 创建引导组件。
	 *
	 * @param container 所属 Simple 窗口。
	 */
	public XUI引导Impl(组件容器 container) {
		super(container);
	}

	@Override
	public void 添加步骤(可视组件 target, String text, boolean rectangle) {
		if (session != null || text == null) {
			throw new 非法参数错误();
		}
		getNativeView(target);
		Step step = new Step();
		step.target = target;
		step.text = text;
		step.rectangle = rectangle;
		steps.add(step);
	}

	@Override
	public void 清空步骤() {
		关闭();
		steps.clear();
	}

	@Override
	public int 取步骤数() {
		return steps.size();
	}

	@Override
	public void 置遮罩颜色(int color) {
		maskColor = color;
	}

	@Override
	public void 置文字样式(int color, int size, int gravity) {
		if (size <= 0) {
			throw new 非法参数错误();
		}
		textColor = color;
		textSize = size;
		textGravity = gravity;
	}

	@Override
	public void 置焦点样式(int color, int width, int padding, int radius) {
		if (width < 0 || padding < 0 || radius < 0) {
			throw new 非法参数错误();
		}
		borderColor = color;
		borderWidth = width;
		focusPadding = padding;
		cornerRadius = radius;
	}

	@Override
	public void 置点击推进(boolean enabled) {
		clickAdvance = enabled;
	}

	@Override
	public void 显示() {
		requireVisibleOwner();
		if (!onceIdentifier.isEmpty() && preferences().getBoolean(onceIdentifier, false)) {
			return;
		}
		if (steps.isEmpty()) {
			throw new 非法参数错误();
		}
		for (Step step : steps) {
			if (step.target != null) {
				requireAnchor(step.target);
			}
		}
		关闭();
		Session current = new Session();
		current.steps = new ArrayList<Step>(steps);
		current.maskColor = maskColor;
		current.textColor = textColor;
		current.textSize = textSize;
		current.textGravity = textGravity;
		current.borderColor = borderColor;
		current.borderWidth = borderWidth;
		current.padding = focusPadding;
		current.radius = cornerRadius;
		current.clickAdvance = clickAdvance;
		current.onceIdentifier = onceIdentifier;
		current.offsetX = offsetX;
		current.offsetY = offsetY;
		current.focusAnimated = focusAnimated;
		current.enterDuration = enterDuration;
		current.exitDuration = exitDuration;
		session = current;
		showStep(current);
	}

	/**
	 * 构建本步骤，焦点外扩使用覆盖层坐标，避免状态栏与窗口偏移被重复扣除。
	 *
	 * @param current 当前显示会话。
	 */
	private void showStep(final Session current) {
		if (session != current) {
			return;
		}
		if (current.index == current.steps.size()) {
			session = null;
			引导已完成();
			return;
		}
		Step step = current.steps.get(current.index);
		try {
			View target = step.target == null ? requireVisibleOwner() : requireAnchor(step.target);
			final GuideCaseView[] reference = new GuideCaseView[1];
			// 上游默认揭示动画依赖下一次预绘制，首帧前退出后仍可能启动并崩溃。
			// 视图淡入可随移除安全终止，同时保留进入反馈。
			AlphaAnimation enter = new AlphaAnimation(0.0f, 1.0f);
			enter.setDuration(current.enterDuration);
			GuideCaseView.Builder builder = new GuideCaseView.Builder(MainActivity.getContext())
				.focusOn(target).fitWindowsAuto().title(step.text)
				.focusShape(step.rectangle ? FocusShape.ROUNDED_RECTANGLE : FocusShape.CIRCLE)
				.enterAnimation(enter).closeOnTouch(current.clickAdvance).dismissListener(new DismissListener() {
					@Override
					public void onDismiss(String id) {
						finishStep(current, reference[0]);
					}

					@Override
					public void onSkipped(String id) {
						// 不使用上游持久化标识，跳过由公开方法显式控制。
					}
				});
			AlphaAnimation exit = new AlphaAnimation(1.0f, 0.0f);
			exit.setDuration(current.exitDuration);
			exit.setAnimationListener(new Animation.AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {
					// 推进只发生在退出完成时，动画期间保留当前步骤。
				}

				@Override
				public void onAnimationEnd(Animation animation) {
					// 上游的自定义退出动画不会自动移除覆盖层，须在此触发原生关闭回调。
					// 主动关闭、跳过或事件中重开后，旧动画不能再影响新的显示会话。
					GuideCaseView source = reference[0];
					if (session == current && current.view == source && source.getParent() != null) {
						source.removeView();
					}
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
					// 退出动画不重复，也不在重复回调内推进步骤。
				}
			});
			builder.exitAnimation(exit);
			if (!current.focusAnimated) {
				builder.disableFocusAnimation();
			}
			if (step.image != 0) {
				if (step.imageWidth > 0) {
					builder.picture(step.image, step.imageWidth, step.imageHeight);
				} else {
					builder.picture(step.image);
				}
				builder.pictureGravity(step.imageGravity, step.imageOffsetX, step.imageOffsetY);
			}
			if (current.maskColor != null) {
				// 上游把零当作未设置；透明黑改用同样全透明的非零 RGB 值。
				builder.backgroundColor(current.maskColor == 0 ? 1 : current.maskColor);
			}
			if (current.textColor != null) {
				SpannableString title = new SpannableString(step.text);
				title.setSpan(new ForegroundColorSpan(current.textColor), 0, title.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				builder.title(title).titleSize(current.textSize, TypedValue.COMPLEX_UNIT_SP).titleGravity(current.textGravity);
			}
			if (current.borderColor != null) {
				builder.focusBorderColor(current.borderColor).focusBorderSize(current.borderWidth).roundRectRadius(current.radius);
			}
			if (current.padding > 0 || current.offsetX != 0 || current.offsetY != 0 || step.region != null) {
				int[] point = new int[2];
				int[] origin = new int[2];
				target.getLocationOnScreen(point);
				MainActivity.getContext().getWindow().getDecorView().getLocationOnScreen(origin);
				int width = step.region == null ? target.getWidth() : step.region[2];
				int height = step.region == null ? target.getHeight() : step.region[3];
				int x = point[0] - origin[0] + (step.region == null ? width / 2 : step.region[0] + width / 2) + current.offsetX;
				int y = point[1] - origin[1] + (step.region == null ? height / 2 : step.region[1] + height / 2) + current.offsetY;
				if (step.rectangle) {
					builder.focusRectAtPosition(x, y, width + 2 * current.padding, height + 2 * current.padding);
				} else {
					int radius = (int) Math.ceil(Math.hypot(width, height) / 2.0) + current.padding;
					builder.focusCircleAtPosition(x, y, radius);
				}
			}
			reference[0] = builder.build();
			current.view = reference[0];
			current.view.show();
			if (!current.view.isAttachedToWindow()) {
				throw new ComponentStateException("当前窗口已有其它引导，请先关闭后重试。");
			}
			// 只在确实显示后登记，验证失败或被其它覆盖层拒绝不消耗首次记录。
			if (current.index == 0 && !current.onceIdentifier.isEmpty()) {
				preferences().edit().putBoolean(current.onceIdentifier, true).apply();
			}
			步骤已显示(current.index);
		} catch (RuntimeException failure) {
			if (session == current) {
				关闭();
			}
			throw failure;
		}
	}

	/**
	 * 先移除旧视图引用再发事件；事件中关闭、跳过或重开时不继续推进旧会话。
	 *
	 * @param current 回调所属会话。
	 * @param source 回调所属视图。
	 */
	private void finishStep(Session current, GuideCaseView source) {
		if (session != current || current.view != source) {
			return;
		}
		current.view = null;
		步骤已关闭(current.index);
		if (session == current) {
			current.index++;
			showStep(current);
		}
	}

	@Override
	public void 下一步() {
		Session current = session;
		if (current != null && current.view != null) {
			current.view.removeView();
		}
	}

	@Override
	public void 跳过() {
		if (session != null) {
			int index = session.index;
			关闭();
			引导已跳过(index);
		}
	}

	@Override
	public int 取当前步骤() {
		return session == null ? -1 : session.index;
	}

	@Override
	public boolean 是否显示() {
		return session != null && session.view != null && session.view.isAttachedToWindow();
	}

	@Override
	public void 关闭() {
		Session previous = session;
		session = null;
		if (previous != null && previous.view != null && previous.view.getParent() != null) {
			previous.view.clearAnimation();
			previous.view.removeView();
		}
	}

	@Override
	public void 步骤已关闭(int index) {
		dispatchEvent("步骤已关闭", index);
	}

	@Override
	public void 引导已完成() {
		dispatchEvent("引导已完成");
	}

	@Override
	public void 步骤已显示(int index) {
		dispatchEvent("步骤已显示", index);
	}

	@Override
	public void 引导已跳过(int index) {
		dispatchEvent("引导已跳过", index);
	}

	@Override
	public void 置仅显示一次(String identifier) {
		if (identifier == null || (!identifier.isEmpty() && identifier.trim().isEmpty())) {
			throw new 非法参数错误();
		}
		onceIdentifier = identifier;
	}

	@Override
	public boolean 是否已显示(String identifier) {
		validateIdentifier(identifier);
		return preferences().getBoolean(identifier, false);
	}

	@Override
	public void 重置显示记录(String identifier) {
		validateIdentifier(identifier);
		preferences().edit().remove(identifier).apply();
	}

	@Override
	public void 置焦点偏移(int x, int y) {
		offsetX = x;
		offsetY = y;
	}

	@Override
	public void 置焦点动画(boolean enabled) {
		focusAnimated = enabled;
	}

	@Override
	public void 置进出场时长(int enter, int exit) {
		if (enter < 0 || exit < 0) {
			throw new 非法参数错误();
		}
		enterDuration = enter;
		exitDuration = exit;
	}

	@Override
	public void 添加区域步骤(int left, int top, int width, int height, String text, boolean rectangle) {
		if (session != null || text == null || left < 0 || top < 0 || width <= 0 || height <= 0) {
			throw new 非法参数错误();
		}
		Step step = new Step();
		step.text = text;
		step.rectangle = rectangle;
		step.region = new int[] {left, top, width, height};
		steps.add(step);
	}

	@Override
	public void 置步骤图片(int index, int image, int width, int height, int gravity, int x, int y) {
		ComponentSupport.validateIndex(index, steps.size());
		if (session != null || width < 0 || height < 0 || (width == 0) != (height == 0)) {
			throw new 非法参数错误();
		}
		if (image != 0) {
			try {
				String type = MainActivity.getContext().getResources().getResourceTypeName(image);
				if (!"drawable".equals(type) && !"mipmap".equals(type)) {
					throw new 非法参数错误();
				}
			} catch (Resources.NotFoundException failure) {
				throw new 非法参数错误();
			}
		}
		Step step = steps.get(index);
		step.image = image;
		step.imageWidth = width;
		step.imageHeight = height;
		step.imageGravity = gravity;
		step.imageOffsetX = x;
		step.imageOffsetY = y;
	}

	/**
	 * 记录限于当前应用与 XUI 引导命名空间，不清除调用方其它偏好设置。
	 *
	 * @return 本库专用偏好存储。
	 */
	private static SharedPreferences preferences() {
		return MainActivity.getContext().getSharedPreferences("simple.library.wsd.xui.guide", Context.MODE_PRIVATE);
	}

	/**
	 * 查询和重置必须指定真实标识，避免空键与每次显示模式混淆。
	 *
	 * @param identifier 引导标识。
	 */
	private static void validateIdentifier(String identifier) {
		if (identifier == null || identifier.trim().isEmpty()) {
			throw new 非法参数错误();
		}
	}

}
