package simple.library.wsd.calendarview;

import android.os.Looper;
import android.view.*;
import simple.runtime.android.MainActivity;
import simple.runtime.components.*;
import simple.runtime.components.impl.android.*;
import simple.library.wsd.calendarview.internal.NativeCalendar;
import simple.library.wsd.calendarview.internal.NativeLayout;

/**
 * 以两个直接子组件接入原生日历收缩布局。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class 日历布局Impl extends 视图组件 implements 日历布局, 视图组件容器, MainActivity.OnDestroyListener {
	private 日历框 calendar;
	private 可视组件 content;
	private boolean destroyed;
	private boolean visible = true;
	private int mode;

	/**
	 * 创建日历布局及活动销毁监听。
	 *
	 * @param container 所属 Simple 容器。
	 */
	public 日历布局Impl(组件容器 container) {
		super(container);
		MainActivity.getContext().addOnDestroyListener(this);
	}

	@Override
	protected View createView() {
		return new NativeLayout(MainActivity.getContext());
	}

	@Override
	public void addComponent(组件 component) {
		check();
		if (!(component instanceof 视图组件)) {
			throw new IllegalArgumentException("日历布局只接受日历框和一个可视内容容器");
		}
		View child = ((视图组件) component).getView();
		if (component == calendar || component == content) {
			return;
		}
		if (child == null || child.getParent() != null) {
			throw new IllegalArgumentException("子组件无效或已有父容器");
		}
		if (component instanceof 日历框Impl) {
			if (calendar != null) {
				throw new IllegalArgumentException("日历布局只能有一个直接日历框");
			}
			view().calendar((NativeCalendar) child);
			calendar = (日历框) component;
		} else {
			if (content != null || !(child instanceof ViewGroup)) {
				throw new IllegalArgumentException("日历布局只能有一个内容容器，请用面板包容普通控件");
			}
			view().content((ViewGroup) child);
			content = (可视组件) component;
		}
	}

	@Override
	public 布局 getLayout() {
		check();
		return null;
	}

	@Override
	public ViewGroup getLayoutManager() {
		check();
		return view();
	}

	@Override
	public boolean 已经展开() {
		check();
		return view().ready() && view().isExpand();
	}

	@Override
	public boolean 日历可见() {
		check();
		return visible;
	}

	@Override
	public boolean 展开(int duration) {
		check();
		duration(duration);
		return mode != 1 && visible && view().ready() && !view().isExpand() && view().expand(duration);
	}

	@Override
	public boolean 收缩(int duration) {
		check();
		duration(duration);
		return mode != 2 && visible && view().ready() && view().isExpand() && view().shrink(duration);
	}

	@Override
	public void 设置显示模式(int value) {
		check();
		if (value < 0 || value > 2) {
			throw new IllegalArgumentException("日历布局模式为 0、1 或 2");
		}
		mode = value;
		view().es4aShowMode(value);
	}

	@Override
	public void 启用收缩手势(boolean enabled) {
		check();
		view().es4aGesture(enabled);
	}

	@Override
	public void 显示日历(boolean value) {
		check();
		if (!view().ready()) {
			throw new simple.library.wsd.calendarview.日历错误("请先添加日历框与内容容器并显示布局");
		}
		visible = value;
		if (value) {
			view().showCalendarView();
		} else {
			view().hideCalendarView();
		}
	}

	@Override
	public 日历框 取得日历() {
		check();
		return calendar;
	}

	@Override
	public 可视组件 取得内容() {
		check();
		return content;
	}

	@Override
	public void onDestroy() {
		销毁();
	}

	@Override
	public void 销毁() {
		if (!destroyed) {
			destroyed = true;
			MainActivity.getContext().removeOnDestroyListener(this);
			view().close();
			calendar = null;
			content = null;
			super.销毁();
		}
	}

	/**
	 * 检查主线程与存活状态。
	 */
	private void check() {
		if (destroyed || Looper.myLooper() != Looper.getMainLooper()) {
			throw new simple.library.wsd.calendarview.日历错误("日历布局只能在主线程且销毁前使用");
		}
	}

	/**
	 * 校验动画时长。
	 *
	 * @param value 毫秒数。
	 */
	private static void duration(int value) {
		if (value < 0 || value > 10000) {
			throw new IllegalArgumentException("动画时长须在 0 至 10000 毫秒内");
		}
	}

	/**
	 * 取得固定原生布局。
	 *
	 * @return 本实例布局。
	 */
	private NativeLayout view() {
		return (NativeLayout) getView();
	}
}
