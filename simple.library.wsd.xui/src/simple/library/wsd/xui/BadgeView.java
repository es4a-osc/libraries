package simple.library.wsd.xui;

import android.content.Context;
import android.view.ViewGroup;

/**
 * 为绑定型徽标提供明确的动画取消及视图移除边界。
 *
 * @author 树先生 xhwsd@qq.com
 */
// 本库适配类与上游类型同名，父类型使用限定名以明确区分。
final class BadgeView extends com.xuexiang.xui.widget.textview.badge.BadgeView {

	/**
	 * 创建尚未绑定目标的原生徽标。
	 *
	 * @param context 使用 XUI 主题的上下文。
	 */
	BadgeView(Context context) {
		super(context);
	}

	/**
	 * 取消回调后停止动画，防止动画结束时把已销毁徽标重新绑定到目标。
	 */
	void dispose() {
		setOnDragStateChangedListener(null);
		if (mAnimator != null) {
			mAnimator.removeAllUpdateListeners();
			mAnimator.removeAllListeners();
			mAnimator.cancel();
			mAnimator = null;
		}
		if (getTargetView() != null) {
			reset();
		}
		hide(false);
		if (getParent() instanceof ViewGroup) {
			((ViewGroup) getParent()).removeView(this);
		}
	}
}
