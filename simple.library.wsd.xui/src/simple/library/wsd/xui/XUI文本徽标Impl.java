package simple.library.wsd.xui;

import android.view.View;

import com.xuexiang.xui.widget.textview.BadgeView;

import simple.runtime.components.impl.android.视图组件;
import simple.runtime.components.可视组件;
import simple.runtime.components.组件容器;
import simple.runtime.errors.非法参数错误;

/**
 * XUI文本徽标的原生视图及 Simple 事件适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI文本徽标Impl extends XUI文本组件基类 implements XUI文本徽标 {

	// 视图在父类构造期间创建；不得用字段初始化器重置此处状态。
	private BadgeView view;

	/**
	 * 创建XUI文本徽标。
	 *
	 * @param container 容纳组件的 Simple 容器。
	 */
	public XUI文本徽标Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected View createView() {
		view = new BadgeView(ComponentSupport.createContext());

		return view;
	}

	@Override
	public void 置背景(int dipRadius, int badgeColor) {
		view.setBackground(dipRadius, badgeColor);
	}

	@Override
	public boolean 是否隐藏开空值() {
		return view.isHideOnNull();
	}

	@Override
	public void 置隐藏开空值(boolean hideOnNull) {
		view.setHideOnNull(hideOnNull);
	}

	@Override
	public void 置徽标数量(int count) {
		view.setBadgeCount(count);
	}

	@Override
	public int 取徽标数量() {
		return view.getBadgeCount();
	}

	@Override
	public void 置徽标对齐(int gravity) {
		view.setBadgeGravity(gravity);
	}

	@Override
	public int 取徽标对齐() {
		return view.getBadgeGravity();
	}

	@Override
	public void 置徽标外边距(int dipMargin) {
		view.setBadgeMargin(dipMargin);
	}

	@Override
	public void 置徽标外边距(int leftDipMargin, int topDipMargin, int rightDipMargin, int bottomDipMargin) {
		view.setBadgeMargin(leftDipMargin, topDipMargin, rightDipMargin, bottomDipMargin);
	}

	@Override
	public int[] 取徽标外边距() {
		return view.getBadgeMargin();
	}

	@Override
	public void 隐藏() {
		view.hide();
	}

	@Override
	public void 显示() {
		view.show();
	}

	@Override
	public void 绑定目标(可视组件 target) {
		View nativeTarget = ((视图组件) target).getView();
		if (nativeTarget == view || nativeTarget.getParent() == null) {
			throw new 非法参数错误();
		}
		view.setTargetView(nativeTarget);
	}
}
