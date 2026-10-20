package simple.library.wsd.pullrefresh;

import android.animation.ValueAnimator;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.LinearLayout;
import android.widget.ScrollView;

/**
 * 管理双向拉动手势、头尾提示和回弹：60 DIP 提示、2.5 倍阻尼和 150 毫秒回弹。
 * 刷新和加载互斥，取消手势不触发请求；脱离窗口时撤销动画及延迟滚动。
 *
 * @author 树先生 xhwsd@qq.com
 */
final class PullRefreshView extends LinearLayout {

	/* 拉动方向与阻尼 */

	static final int NONE = 0;
	static final int DOWN = 1;
	static final int UP = 2;
	private static final float FRICTION = 2.5f;

	/* 内容视图与头尾提示 */

	private final ScrollView scroll;
	private final LoadingIndicator header;
	private final LoadingIndicator footer;
	private final int indicatorHeight;
	private final int touchSlop;

	/* 手势、运行状态与回弹 */

	private Listener listener;
	private ValueAnimator rebound;
	private float startX;
	private float startY;
	private int pointerId = -1;
	private int dragDirection;
	private int active;
	private boolean downEnabled = true;
	private boolean upEnabled = true;
	private boolean disposed;

	/* 布局完成后执行的端点滚动任务 */

	private final Runnable scrollTop = new Runnable() {
		@Override
		public void run() {
			scroll.scrollTo(0, 0);
		}
	};

	private final Runnable scrollBottom = new Runnable() {
		@Override
		public void run() {
			scroll.fullScroll(View.FOCUS_DOWN);
		}
	};

	/**
	 * 创建刷新视图及唯一滚动区域。
	 *
	 * @param context Android 上下文。
	 */
	PullRefreshView(Context context) {
		super(context);
		setOrientation(VERTICAL);
		indicatorHeight = Math.round(60 * getResources().getDisplayMetrics().density);
		touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

		// 头尾提示共用固定高度，触发阈值与回弹停留位置保持一致。
		header = new LoadingIndicator(context, true);
		footer = new LoadingIndicator(context, false);

		// 短内容也填满可视区域，以便在任一端接收拉动手势。
		scroll = new ScrollView(context) {
			@Override
			protected void onScrollChanged(int x, int y, int oldX, int oldY) {
				super.onScrollChanged(x, y, oldX, oldY);

				if (!disposed && listener != null) {
					listener.scrolled(y, atTop() || atBottom());
				}
			}
		};
		scroll.setFillViewport(true);

		// 初始隐藏头尾，拉动时只改变外层偏移，不改动内容布局。
		addView(header, new LinearLayout.LayoutParams(-1, indicatorHeight));
		addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
		addView(footer, new LinearLayout.LayoutParams(-1, indicatorHeight));
		setPadding(0, -indicatorHeight, 0, -indicatorHeight);
	}

	/**
	 * 将内容布局安装到滚动区域。
	 *
	 * @param content Simple 布局视图。
	 */
	void setContent(View content) {
		scroll.removeAllViews();
		scroll.addView(content, new ScrollView.LayoutParams(-1, -2));
	}

	/**
	 * 设置组件接收器，销毁时可以清空。
	 *
	 * @param value 接收器或 null。
	 */
	void setListener(Listener value) {
		listener = value;
	}

	/**
	 * 查询指定方向是否启用。
	 *
	 * @param direction DOWN 或 UP。
	 * @return 是否启用。
	 */
	boolean enabled(int direction) {
		return direction == DOWN ? downEnabled : upEnabled;
	}

	/**
	 * 设置方向开关，关闭正在执行的方向会同时停止对应动画和状态。
	 *
	 * @param direction DOWN 或 UP。
	 * @param value 是否启用。
	 */
	void enabled(int direction, boolean value) {
		if (direction == DOWN) {
			downEnabled = value;
		} else {
			upEnabled = value;
		}

		// 关闭当前方向时一并结束操作，避免提示停留在等待状态。
		if (!value && (active == direction || dragDirection == direction)) {
			finish();
		}
	}

	/**
	 * 取得当前刷新或加载方向。
	 *
	 * @return NONE、DOWN 或 UP。
	 */
	int active() {
		return active;
	}

	/**
	 * 结束刷新或加载，恢复提示及滚动位置。
	 */
	void finish() {
		active = NONE;
		dragDirection = NONE;
		header.showState(false, false);
		footer.showState(false, false);
		animateOffset(0);
	}

	/**
	 * 等内容布局完成后滚动到指定端点；同类待执行任务只保留一次。
	 *
	 * @param bottom 是否滚动到底部。
	 */
	void scrollToEdge(boolean bottom) {
		scroll.removeCallbacks(scrollTop);
		scroll.removeCallbacks(scrollBottom);
		scroll.post(bottom ? scrollBottom : scrollTop);
	}

	/**
	 * 取得指定方向的状态提示或辅助文本。
	 *
	 * @param direction DOWN 或 UP。
	 * @param state 0 为拉动提示，1 为释放提示，2 为处理提示，3 为辅助文本。
	 * @return 当前文本。
	 */
	String text(int direction, int state) {
		return indicator(direction).text(state);
	}

	/**
	 * 更新指定方向的文案，不改变手势和处理状态。
	 *
	 * @param direction DOWN 或 UP。
	 * @param state 0 为拉动提示，1 为释放提示，2 为处理提示，3 为辅助文本。
	 * @param text 提示文本。
	 * @throws IllegalArgumentException 文本为 null。
	 */
	void text(int direction, int state, String text) {
		indicator(direction).text(state, text);
	}

	/* 手势接管与松手触发 */

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		if (disposed || active != NONE || !isEnabled()) {
			return false;
		}

		int action = event.getActionMasked();
		if (action == MotionEvent.ACTION_DOWN) {
			startX = event.getX();
			startY = event.getY();
			pointerId = event.getPointerId(0);
			dragDirection = NONE;
		} else if (action == MotionEvent.ACTION_MOVE && pointerId != -1) {
			int pointer = event.findPointerIndex(pointerId);
			if (pointer < 0) {
				cancelGesture();
				return false;
			}

			// 仅在边界接管纵向拉动，横向移动及内容区内滚动继续交给子视图。
			float dy = event.getY(pointer) - startY;
			if (Math.abs(dy) > touchSlop && Math.abs(dy) > Math.abs(event.getX(pointer) - startX)) {
				if (dy > 0 && downEnabled && atTop()) {
					dragDirection = DOWN;
				} else if (dy < 0 && upEnabled && atBottom()) {
					dragDirection = UP;
				}

				if (dragDirection != NONE) {
					stopRebound();
					getParent().requestDisallowInterceptTouchEvent(true);
					return true;
				}
			}
		} else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL
				|| action == MotionEvent.ACTION_POINTER_DOWN || action == MotionEvent.ACTION_POINTER_UP) {
			cancelGesture();
		}

		return false;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (disposed || active != NONE || dragDirection == NONE) {
			return false;
		}

		int action = event.getActionMasked();
		if (action == MotionEvent.ACTION_MOVE) {
			int pointer = event.findPointerIndex(pointerId);
			if (pointer < 0) {
				cancelGesture();
				return true;
			}

			// 阻尼后的位移用于显示提示，并限制最大拉动距离。
			float dy = event.getY(pointer) - startY;
			float distance = Math.max(0, dragDirection == DOWN ? dy : -dy) / FRICTION;
			int offset = Math.round(Math.min(distance, indicatorHeight * 3));

			scrollTo(0, dragDirection == DOWN ? -offset : offset);
			indicator(dragDirection).showState(false, offset >= indicatorHeight);
		} else if (action == MotionEvent.ACTION_UP) {
			int direction = dragDirection;
			boolean ready = Math.abs(getScrollY()) >= indicatorHeight && enabled(direction);
			dragDirection = NONE;
			pointerId = -1;

			// 先固定运行状态再回调，事件处理过程可立即查询状态或结束操作。
			if (ready) {
				active = direction;
				indicator(direction).showState(true, false);
				animateOffset(direction == DOWN ? -indicatorHeight : indicatorHeight);

				if (listener != null) {
					listener.started(direction);
				}
			} else {
				cancelGesture();
			}
		} else if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_POINTER_DOWN
				|| action == MotionEvent.ACTION_POINTER_UP) {
			cancelGesture();
		}

		return true;
	}

	/* 生命周期与状态恢复 */

	/**
	 * 释放组件持有的动画、滚动任务和监听，不再派发事件。
	 */
	void dispose() {
		disposed = true;
		listener = null;
		reset();
	}

	@Override
	protected void onDetachedFromWindow() {
		reset();
		super.onDetachedFromWindow();
	}

	/**
	 * 选择提示视图。
	 *
	 * @param direction 方向。
	 * @return 头部或尾部。
	 */
	private LoadingIndicator indicator(int direction) {
		return direction == DOWN ? header : footer;
	}

	/**
	 * 取得滚动顶部状态。
	 *
	 * @return 是否位于顶部。
	 */
	private boolean atTop() {
		return scroll.getScrollY() <= 0;
	}

	/**
	 * 取得滚动底部状态，内容较短时也允许上拉加载。
	 *
	 * @return 是否位于底部。
	 */
	private boolean atBottom() {
		View child = scroll.getChildAt(0);
		return child != null && scroll.getScrollY() >= child.getBottom() - scroll.getHeight();
	}

	/**
	 * 取消未完成的手势，不能将取消动作当作松手刷新。
	 */
	private void cancelGesture() {
		pointerId = -1;
		dragDirection = NONE;

		if (active == NONE) {
			header.showState(false, false);
			footer.showState(false, false);
			animateOffset(0);
		}
	}

	/**
	 * 取消排队的界面工作并立即回到稳定状态。
	 */
	private void reset() {
		stopRebound();
		scroll.removeCallbacks(scrollTop);
		scroll.removeCallbacks(scrollBottom);

		active = NONE;
		dragDirection = NONE;
		pointerId = -1;

		header.showState(false, false);
		footer.showState(false, false);
		scrollTo(0, 0);
	}

	/**
	 * 平滑回弹到目标位置；回调只更新视图，不触发刷新事件。
	 *
	 * @param target 目标纵向偏移，单位像素。
	 */
	private void animateOffset(int target) {
		stopRebound();

		if (getScrollY() == target) {
			return;
		}

		rebound = ValueAnimator.ofInt(getScrollY(), target);
		rebound.setDuration(150);
		rebound.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				scrollTo(0, ((Integer) animation.getAnimatedValue()).intValue());
			}
		});

		rebound.start();
	}

	/**
	 * 释放上次回弹，防止不同动画同时操作同一偏移。
	 */
	private void stopRebound() {
		if (rebound != null) {
			rebound.cancel();
			rebound.removeAllUpdateListeners();
			rebound = null;
		}
	}

	/**
	 * 底层视图与 Simple 组件之间的事件边界。
	 */
	interface Listener {

		/**
		 * 手势开始一个操作。
		 *
		 * @param direction DOWN 或 UP。
		 */
		void started(int direction);

		/**
		 * 内容发生滚动。
		 *
		 * @param y 当前纵向位置，单位像素。
		 * @param edge 是否到达顶部或底部。
		 */
		void scrolled(int y, boolean edge);
	}
}
