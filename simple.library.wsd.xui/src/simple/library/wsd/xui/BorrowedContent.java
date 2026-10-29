package simple.library.wsd.xui;

import android.view.View;
import android.view.ViewGroup;

import simple.runtime.errors.非法参数错误;

/**
 * 暂借自定义内容宿主的 Simple 视图，关闭时恢复原父容器、顺序和布局参数。
 *
 * @author 树先生 xhwsd@qq.com
 */
final class BorrowedContent {

	final View view;
	private final ViewGroup parent;
	private final int index;
	private final ViewGroup.LayoutParams parameters;
	private final int visibility;
	private boolean restored;

	/**
	 * 在任何移动之前记录完整归属；窗口根视图不可作为自定义内容借出。
	 *
	 * @param view 已校验属于当前窗口的内容。
	 * @param owner 所属窗口根视图。
	 */
	BorrowedContent(View view, View owner) {
		if (view == owner || !(view.getParent() instanceof ViewGroup)) {
			throw new 非法参数错误();
		}
		this.view = view;
		parent = (ViewGroup) view.getParent();
		index = parent.indexOfChild(view);
		parameters = view.getLayoutParams();
		visibility = view.getVisibility();
		parent.removeView(view);
		// 原参数对象留给原容器；弹层只能改动副本，不能污染权重、边距等字段。
		view.setLayoutParams(new ViewGroup.LayoutParams(parameters));
		view.setVisibility(View.VISIBLE);
	}

	/**
	 * 幂等恢复归属，避免自定义内容宿主布局参数污染原页面。
	 */
	void restore() {
		if (restored) {
			return;
		}
		restored = true;
		if (view.getParent() instanceof ViewGroup) {
			((ViewGroup) view.getParent()).removeView(view);
		}
		view.setVisibility(visibility);
		parent.addView(view, Math.min(index, parent.getChildCount()), parameters);
	}
}
