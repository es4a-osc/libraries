package simple.library.wsd.calendarview.internal;

import android.content.Context;
import android.view.*;
import android.widget.LinearLayout;
import simple.library.wsd.calendarview.vendor.CalendarLayout;

/**
 * 两个 Simple 直接子组件的周月联动布局。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class NativeLayout extends CalendarLayout {
	private NativeCalendar calendar;
	private ViewGroup content;
	private boolean disposed;
	private TopListener topListener;
	private int queryDepth;

	/**
	 * 创建尚未绑定子组件的布局。
	 *
	 * @param context 活动上下文。
	 */
	public NativeLayout(Context context) {
		super(context, null);
	}

	/**
	 * 放入本布局唯一的日历子组件。
	 *
	 * @param value 日历原生视图。
	 */
	public void calendar(NativeCalendar value) {
		if (value.configuration().填满高度()) {
			throw new IllegalArgumentException("收缩日历不能设置填满高度");
		}
		calendar = value;
		addView(value, 0, new LinearLayout.LayoutParams(-1, -2));
		bind();
	}

	/**
	 * 放入本布局唯一的可滚动内容或面板。
	 *
	 * @param value 内容原生容器。
	 */
	public void content(ViewGroup value) {
		content = value;
		addView(value, new LinearLayout.LayoutParams(-1, -1));
		bind();
	}

	/**
	 * 在两个槽位准备好后连接上游内部视图。
	 */
	private void bind() {
		if (calendar != null && content != null) {
			es4aBind(calendar, content);
			if (isAttachedToWindow()) {
				es4aInitializeStatus();
			}
		}
	}

	/**
	 * 查询日历与内容是否均已绑定。
	 *
	 * @return 布局可执行收缩操作时为真。
	 */
	public boolean ready() {
		return calendar != null && content != null && isAttachedToWindow() && !disposed;
	}

	@Override
	protected void onMeasure(int widthSpec, int heightSpec) {
		if (calendar == null || content == null) {
			setMeasuredDimension(MeasureSpec.getSize(widthSpec), MeasureSpec.getSize(heightSpec));
			return;
		}
		super.onMeasure(widthSpec, heightSpec);
	}

	@Override
	protected boolean isScrollTop() {
		boolean result = content == null || !canScrollUp(content);
		if (disposed || topListener == null) {
			return result;
		}
		queryDepth++;
		try {
			return topListener.atTop(result);
		} finally {
			queryDepth--;
		}
	}

	/**
	 * 检查直接滚动控件或面板内的滚动内容是否离开顶部。
	 *
	 * @param view 当前内容节点。
	 * @return 有可见内容还能向上滚动时为真。
	 */
	private static boolean canScrollUp(View view) {
		if (view.getVisibility() != VISIBLE) {
			return false;
		}
		if (view.canScrollVertically(-1)) {
			return true;
		}
		if (view instanceof ViewGroup) {
			ViewGroup group = (ViewGroup) view;
			for (int i = 0; i < group.getChildCount(); i++) {
				if (canScrollUp(group.getChildAt(i))) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		return !disposed && calendar != null && content != null && super.dispatchTouchEvent(event);
	}

	/**
	 * 终止动画、释放触摸跟踪并移除子视图。
	 */
	public void close() {
		if (!disposed) {
			disposed = true;
			topListener = null;
			es4aDispose();
			removeAllViews();
			calendar = null;
			content = null;
		}
	}

	/**
	 * 连接特殊内容的同步顶部判定。
	 *
	 * @param listener 判定接收者。
	 */
	public void topListener(TopListener listener) {
		topListener = listener;
	}

	/**
	 * 查询本布局是否位于同步顶部判断栈中。
	 *
	 * @return 当前正在判定时为真。
	 */
	public boolean querying() {
		return queryDepth != 0;
	}

	/**
	 * 特殊内容的顶部判断协议。
	 *
	 * @author 树先生 xhwsd@qq.com
	 */
	public interface TopListener {
		/**
		 * 对默认顶部状态进行同步修正。
		 *
		 * @param defaultValue 原生内容树的顶部状态。
		 * @return 调用方确认的顶部状态。
		 */
		boolean atTop(boolean defaultValue);
	}

}
