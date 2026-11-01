package simple.library.wsd.smartrefresh;

import android.view.View;
import android.view.ViewGroup;

import simple.runtime.components.可视组件;
import simple.runtime.components.impl.android.视图组件;

/**
 * 暂借同一窗口中的自定义头尾内容，替换或销毁时恢复原位置和布局参数。
 *
 * @author 树先生 xhwsd@qq.com
 */
final class BorrowedView {
	final View view;
	private final ViewGroup parent;
	private final int index;
	private final ViewGroup.LayoutParams parameters;
	private final int visibility;

	/**
	 * 校验归属后移出内容，拒绝刷新框自身、其祖先或内部内容。
	 *
	 * @param component 要借用的同窗口可视组件。
	 * @param host 借用内容的刷新框。
	 */
	BorrowedView(可视组件 component, SmartRefreshView host) {
		if (!(component instanceof 视图组件)) {
			throw new IllegalArgumentException("必须提供本窗口的可视组件。");
		}
		view = ((视图组件) component).getView();
		if (view == null || !(view.getParent() instanceof ViewGroup)
				|| view.getRootView() != host.getRootView()) {
			throw new IllegalArgumentException("内容必须拥有同窗口的原父容器。");
		}
		for (View node = host; node != null; node = node.getParent() instanceof View ? (View) node.getParent() : null) {
			if (node == view) {
				throw new IllegalArgumentException("不能借用刷新框自身或其祖先。");
			}
		}
		for (View node = view; node != null; node = node.getParent() instanceof View ? (View) node.getParent() : null) {
			if (node == host) {
				throw new IllegalArgumentException("不能借用刷新框内部的内容。");
			}
		}
		parent = (ViewGroup) view.getParent();
		index = parent.indexOfChild(view);
		parameters = view.getLayoutParams();
		visibility = view.getVisibility();
		parent.removeView(view);
		view.setVisibility(View.VISIBLE);
		view.setLayoutParams(new ViewGroup.MarginLayoutParams(parameters));
	}

	/**
	 * 从当前临时容器移出视图，供另一个头尾外壳使用。
	 */
	void detach() {
		if (view.getParent() instanceof ViewGroup) {
			((ViewGroup) view.getParent()).removeView(view);
		}
	}

	/**
	 * 恢复原父容器、顺序、可见性和原布局参数。
	 */
	void restore() {
		detach();
		view.setVisibility(visibility);
		parent.addView(view, Math.min(index, parent.getChildCount()), parameters);
	}
}
