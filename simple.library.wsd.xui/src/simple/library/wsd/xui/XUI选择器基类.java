package simple.library.wsd.xui;

import android.util.DisplayMetrics;

import com.xuexiang.xui.widget.picker.wheelview.WheelView;
import com.xuexiang.xui.widget.picker.widget.BasePickerView;
import com.xuexiang.xui.widget.picker.widget.builder.OptionsPickerBuilder;
import com.xuexiang.xui.widget.picker.widget.builder.TimePickerBuilder;
import com.xuexiang.xui.widget.picker.widget.listener.OnDismissListener;

import simple.runtime.android.MainActivity;
import simple.runtime.components.组件容器;
import simple.runtime.errors.非法参数错误;

/**
 * 共用选择器配置与单次显示会话，隔离旧实例的异步关闭和新实例的显示状态。
 *
 * @author 树先生 xhwsd@qq.com
 */
abstract class XUI选择器基类 extends XUI弹层基类 implements XUI选择器 {

	private boolean centerLabel;

	private Integer wheelBackground;
	private Integer titleBackground;
	private float lineSpacing = 1.6f;
	private int dividerType;

	private String title = "";
	private String confirmText = "确定";
	private String cancelText = "取消";
	private Integer selectedColor;
	private Integer otherColor;
	private Integer titleColor;
	private Integer confirmColor;
	private Integer cancelColor;
	private Integer dividerColor;
	private int contentSize;
	private int titleSize;
	private int buttonSize;
	private boolean backCancelable = true;
	private boolean outsideCancelable = true;
	private Session session;

	/**
	 * 保存本次原生界面及关闭原因，旧会话不得清空新会话。
	 */
	private static final class Session {
		final BasePickerView view;
		boolean confirmed;
		boolean programmatic;
		boolean finished;

		/**
		 * 创建本次显示会话。
		 *
		 * @param view 本次原生界面。
		 */
		Session(BasePickerView view) {
			this.view = view;
		}
	}

	/**
	 * 创建选择器共用状态。
	 *
	 * @param container 所属 Simple 容器。
	 */
	protected XUI选择器基类(组件容器 container) {
		super(container);
	}

	@Override
	public void 置标题(String title) {
		if (title == null) {
			throw new 非法参数错误();
		}
		this.title = title;
	}

	@Override
	public void 置按钮文本(String confirm, String cancel) {
		if (confirm == null || cancel == null || confirm.isEmpty() || cancel.isEmpty()) {
			throw new 非法参数错误();
		}
		confirmText = confirm;
		cancelText = cancel;
	}

	@Override
	public void 置文字颜色(int selected, int other) {
		selectedColor = selected;
		otherColor = other;
	}

	@Override
	public void 置标题颜色(int color) {
		titleColor = color;
	}

	@Override
	public void 置按钮颜色(int confirm, int cancel) {
		confirmColor = confirm;
		cancelColor = cancel;
	}

	@Override
	public void 置分隔线颜色(int color) {
		dividerColor = color;
	}

	@Override
	public void 置字号(int content, int title, int button) {
		if (content <= 0 || title <= 0 || button <= 0) {
			throw new 非法参数错误();
		}
		contentSize = content;
		titleSize = title;
		buttonSize = button;
	}

	@Override
	public void 置取消方式(boolean back, boolean outside) {
		backCancelable = back;
		outsideCancelable = outside;
	}

	/**
	 * 应用下次显示的共用外观，未显式设置的颜色保持上游默认值。
	 *
	 * @param builder 本次界面构建器。
	 */
	protected final void configure(TimePickerBuilder builder) {
		builder.isCenterLabel(centerLabel);
		builder.setLineSpacingMultiplier(lineSpacing).setDividerType(WheelView.DividerType.values()[dividerType]);
		if (wheelBackground != null) {
			builder.setBgColor(wheelBackground);
		}
		if (titleBackground != null) {
			builder.setTitleBgColor(titleBackground);
		}
		builder.setTitleText(title).setSubmitText(confirmText).setCancelText(cancelText)
			.setOutSideCancelable(outsideCancelable);
		if (selectedColor != null) {
			builder.setTextColorCenter(selectedColor);
		}
		if (otherColor != null) {
			builder.setTextColorOut(otherColor);
		}
		if (titleColor != null) {
			builder.setTitleColor(titleColor);
		}
		if (confirmColor != null) {
			builder.setSubmitColor(confirmColor);
		}
		if (cancelColor != null) {
			builder.setCancelColor(cancelColor);
		}
		if (dividerColor != null) {
			builder.setDividerColor(dividerColor);
		}
		if (contentSize > 0) {
			builder.setContentTextSize(getWheelSize()).setTitleSize(titleSize).setSubCalSize(buttonSize);
		}
	}

	/**
	 * 应用下次显示的共用外观，未显式设置的颜色保持上游默认值。
	 *
	 * @param builder 本次界面构建器。
	 */
	protected final void configure(OptionsPickerBuilder builder) {
		builder.isCenterLabel(centerLabel);
		builder.setLineSpacingMultiplier(lineSpacing).setDividerType(WheelView.DividerType.values()[dividerType]);
		if (wheelBackground != null) {
			builder.setBgColor(wheelBackground);
		}
		if (titleBackground != null) {
			builder.setTitleBgColor(titleBackground);
		}
		builder.setTitleText(title).setSubmitText(confirmText).setCancelText(cancelText)
			.setOutSideCancelable(outsideCancelable);
		if (selectedColor != null) {
			builder.setTextColorCenter(selectedColor);
		}
		if (otherColor != null) {
			builder.setTextColorOut(otherColor);
		}
		if (titleColor != null) {
			builder.setTitleColor(titleColor);
		}
		if (confirmColor != null) {
			builder.setSubmitColor(confirmColor);
		}
		if (cancelColor != null) {
			builder.setCancelColor(cancelColor);
		}
		if (dividerColor != null) {
			builder.setDividerColor(dividerColor);
		}
		if (contentSize > 0) {
			builder.setContentTextSize(getWheelSize()).setTitleSize(titleSize).setSubCalSize(buttonSize);
		}
	}

	/**
	 * 上游滚轮按 DIP 换算字号，先补偿系统字体缩放，使共用字号接口按 SP 生效。
	 *
	 * @return 传给上游整数接口的 DIP 字号。
	 */
	private int getWheelSize() {
		DisplayMetrics metrics = MainActivity.getContext().getResources().getDisplayMetrics();
		return Math.max(1, Math.round(contentSize * metrics.scaledDensity / metrics.density));
	}

	/**
	 * 登记并显示新界面；取消与关闭只针对本次会话发送一次。
	 *
	 * @param view 完成数据和外观配置的原生界面。
	 */
	protected final void showPicker(BasePickerView view) {
		requireVisibleOwner();
		final Session current = new Session(view);
		session = current;
		view.setKeyBackCancelable(backCancelable);
		view.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss(BasePickerView source) {
				if (current.finished) {
					return;
				}
				current.finished = true;
				if (session == current) {
					session = null;
				}
				try {
					if (!current.confirmed && !current.programmatic) {
						已取消();
					}
				} finally {
					已关闭();
				}
			}
		});
		try {
			view.show();
		} catch (RuntimeException failure) {
			current.programmatic = true;
			关闭();
			throw failure;
		}
	}

	/**
	 * 拒绝旧界面的迟到变化回调及尚未显示时的构造回调。
	 *
	 * @param view 回调所属界面。
	 * @return 是否为当前显示会话。
	 */
	protected final boolean isCurrentPicker(BasePickerView view) {
		return session != null && session.view == view && !session.finished;
	}

	/**
	 * 在通知确认事件前记录关闭原因，避免重入关闭被误报为用户取消。
	 *
	 * @param view 回调所属界面。
	 * @return 是否可提交本次结果。
	 */
	protected final boolean confirmPicker(BasePickerView view) {
		if (!isCurrentPicker(view)) {
			return false;
		}
		session.confirmed = true;
		return true;
	}

	/**
	 * 查询当前界面是否显示。
	 *
	 * @return 是否显示。
	 */
	public boolean 是否显示() {
		return session != null && session.view.isShowing();
	}

	@Override
	public void 关闭() {
		Session previous = session;
		session = null;
		if (previous != null && !previous.finished) {
			previous.programmatic = true;
			// 切窗和重新显示不等待退场动画，避免旧覆盖层在新窗口继续拦截操作。
			previous.view.dismissImmediately();
		}
	}

	@Override
	public void 已取消() {
		dispatchEvent("已取消");
	}

	@Override
	public void 已关闭() {
		dispatchEvent("已关闭");
	}

	@Override
	public void 置背景颜色(int wheel, int title) {
		wheelBackground = wheel;
		titleBackground = title;
	}

	@Override
	public void 置行距(float spacing) {
		if (!Float.isFinite(spacing) || spacing < 1.2f || spacing > 2.0f) {
			throw new 非法参数错误();
		}
		lineSpacing = spacing;
	}

	@Override
	public void 置分隔线类型(int type) {
		ComponentSupport.validateIndex(type, 2);
		dividerType = type;
	}

	@Override
	public void 置单位居中(boolean center) {
		centerLabel = center;
	}

}
