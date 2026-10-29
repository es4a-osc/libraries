package simple.library.wsd.xui;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.PopupWindow;

import com.xuexiang.xui.widget.popupwindow.status.Status;
import com.xuexiang.xui.widget.popupwindow.status.StatusView;

import simple.runtime.components.组件容器;
import simple.runtime.errors.非法参数错误;

/**
 * 状态层的 XUI 实现与 Simple 生命周期适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI状态层Impl extends XUI弹层基类 implements XUI状态层 {

	/**
	 * 创建XUI状态层。
	 *
	 * @param container 所属 Simple 窗口。
	 */
	public XUI状态层Impl(组件容器 container) {
		super(container);
	}

	private PopupWindow popup;
	private StatusView statusView;
	private final Handler handler = new Handler(Looper.getMainLooper());
	private final Runnable dismissTask = new Runnable() {
		@Override
		public void run() {
			关闭();
		}
	};

	@Override
	public void 显示(int state, long duration) {
		View root = requireVisibleOwner();
		ComponentSupport.validateIndex(state, 3);
		if (duration < 0) {
			throw new 非法参数错误();
		}
		关闭();
		// 上游样式只定义自动隐藏开关，四种状态的布局须由调用方显式提供。
		statusView = new StatusView(ComponentSupport.createContext(), R.layout.es4a_xui_status_complete,
			R.layout.es4a_xui_status_error, R.layout.es4a_xui_status_loading, R.layout.es4a_xui_status_error);
		// 自动关闭由本组件统一持有，避免上游延迟任务在窗口离开后继续运行。
		statusView.setHideOnComplete(false);
		int size = Math.round(180 * root.getResources().getDisplayMetrics().density);
		popup = new PopupWindow(statusView, size, size, false);
		popup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		popup.showAtLocation(root, Gravity.CENTER, 0, 0);
		Status[] states = {Status.LOADING, Status.COMPLETE, Status.ERROR};
		statusView.setStatus(states[state]);
		if (duration > 0) {
			handler.postDelayed(dismissTask, duration);
		}
	}

	@Override
	public boolean 是否显示() {
		return popup != null && popup.isShowing();
	}

	@Override
	public void 关闭() {
		handler.removeCallbacks(dismissTask);
		if (statusView != null) {
			statusView.clearAnimation();
			statusView = null;
		}
		if (popup != null) {
			popup.dismiss();
			popup = null;
		}
	}

}
