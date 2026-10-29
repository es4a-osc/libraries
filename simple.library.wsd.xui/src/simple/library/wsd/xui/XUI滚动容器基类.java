package simple.library.wsd.xui;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

import androidx.core.widget.NestedScrollView;

import simple.runtime.components.组件容器;

/**
 * 普通与嵌套滚动视图共用的布局后定位，销毁时撤销待执行请求。
 *
 * @author 树先生 xhwsd@qq.com
 */
abstract class XUI滚动容器基类 extends XUI视图组件基类 implements XUI滚动容器 {

	private Integer pendingPosition;
	private boolean pendingSmooth;
	private boolean released;
	private final Runnable scrollAction = new Runnable() {
		@Override
		public void run() {
			performScroll();
		}
	};
	private final View.OnLayoutChangeListener layoutListener = new View.OnLayoutChangeListener() {
		@Override
		public void onLayoutChange(View view, int left, int top, int right, int bottom,
			int oldLeft, int oldTop, int oldRight, int oldBottom) {
			if (pendingPosition != null && !released) {
				view.removeCallbacks(scrollAction);
				view.post(scrollAction);
			}
		}
	};

	/**
	 * 在具体组件创建视图后登记布局监听。
	 *
	 * @param container 所属容器。
	 */
	protected XUI滚动容器基类(组件容器 container) {
		super(container);
		getView().addOnLayoutChangeListener(layoutListener);
	}

	@Override
	public int 取滚动偏移() {
		return getView().getScrollY();
	}

	@Override
	public int 取最大滚动偏移() {
		ViewGroup view = (ViewGroup) getView();
		if (view.getHeight() == 0 || view.getChildCount() == 0) {
			return 0;
		}
		View child = view.getChildAt(0);
		int margin = 0;
		if (child.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
			margin = ((ViewGroup.MarginLayoutParams) child.getLayoutParams()).bottomMargin;
		}
		return Math.max(0, child.getBottom() + margin - view.getHeight() + view.getPaddingBottom());
	}

	@Override
	public void 滚动到(int position, boolean smooth) {
		requestScroll(Math.max(0, position), smooth);
	}

	@Override
	public void 滚动到顶(boolean smooth) {
		requestScroll(0, smooth);
	}

	@Override
	public void 滚动到底(boolean smooth) {
		requestScroll(-1, smooth);
	}

	/**
	 * 保存逻辑目标，底部使用哨兵值，避免在布局前错误地记成零。
	 *
	 * @param position 非负像素偏移，-1 表示底部。
	 * @param smooth 是否平滑滚动。
	 */
	private void requestScroll(int position, boolean smooth) {
		if (released) {
			return;
		}
		pendingPosition = position;
		pendingSmooth = smooth;
		getView().removeCallbacks(scrollAction);
		getView().post(scrollAction);
	}

	/**
	 * 仅在布局稳定时执行，尺寸尚未确定时保留目标等候下一次布局。
	 */
	private void performScroll() {
		View view = getView();
		if (released || pendingPosition == null || view.isLayoutRequested()
			|| view.getWidth() == 0 || view.getHeight() == 0) {
			return;
		}
		int maximum = 取最大滚动偏移();
		int position = pendingPosition < 0 ? maximum : Math.min(pendingPosition, maximum);
		pendingPosition = null;
		if (!pendingSmooth) {
			view.scrollTo(view.getScrollX(), position);
		} else if (view instanceof ScrollView) {
			((ScrollView) view).smoothScrollTo(view.getScrollX(), position);
		} else {
			((NestedScrollView) view).smoothScrollTo(view.getScrollX(), position);
		}
	}

	@Override
	public void 销毁() {
		if (!released) {
			released = true;
			pendingPosition = null;
			getView().removeCallbacks(scrollAction);
			getView().removeOnLayoutChangeListener(layoutListener);
		}
		super.销毁();
	}
}
