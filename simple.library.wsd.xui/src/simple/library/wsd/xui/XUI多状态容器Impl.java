package simple.library.wsd.xui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.xuexiang.xui.R;
import com.xuexiang.xui.widget.statelayout.MultipleStatusView;

import simple.runtime.components.impl.android.视图组件容器;
import simple.runtime.components.可视组件;
import simple.runtime.components.布局;
import simple.runtime.components.组件;
import simple.runtime.components.组件容器;
import simple.runtime.errors.非法参数错误;
import simple.runtime.events.EventDispatcher;

/**
 * XUI多状态容器的原生视图及 Simple 事件适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI多状态容器Impl extends XUI视图组件基类 implements XUI多状态容器, 视图组件容器 {

	private FrameLayout[] stateHosts;
	private BorrowedContent[] stateContents;

	// 视图在父类构造期间创建；不得用字段初始化器重置此处状态。
	private MultipleStatusView view;

	// 用户内容与内建状态视图分别管理。
	private LinearLayout content;

	/**
	 * 创建XUI多状态容器。
	 *
	 * @param container 容纳组件的 Simple 容器。
	 */
	public XUI多状态容器Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected View createView() {
		view = new MultipleStatusView(ComponentSupport.createContext());

		view.setOnRetryClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View clicked) {
				请求重试();
			}
		});

		content = new LinearLayout(view.getContext());
		content.setOrientation(LinearLayout.VERTICAL);
		view.addView(content);

		content.setId(View.generateViewId());
		stateHosts = new FrameLayout[5];
		stateContents = new BorrowedContent[5];
		int[] layouts = {0, R.layout.msv_layout_loading_view, R.layout.msv_layout_empty_view,
			R.layout.msv_layout_error_view, R.layout.msv_layout_no_network_view};
		for (int state = 1; state <= 4; state++) {
			FrameLayout host = new FrameLayout(view.getContext());
			host.setId(View.generateViewId());
			LayoutInflater.from(view.getContext()).inflate(layouts[state], host, true);
			stateHosts[state] = host;
		}
		ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(-1, -1);
		view.showLoading(stateHosts[1], params);
		view.showEmpty(stateHosts[2], new ViewGroup.LayoutParams(-1, -1));
		view.showError(stateHosts[3], new ViewGroup.LayoutParams(-1, -1));
		view.showNoNetwork(stateHosts[4], new ViewGroup.LayoutParams(-1, -1));
		view.showContent();
		return view;
	}

	@Override
	public ViewGroup getLayoutManager() {
		return ComponentSupport.getChildLayout((ViewGroup) content);
	}

	@Override
	public void addComponent(组件 component) {
		ComponentSupport.addChildComponent((ViewGroup) content, component);
	}

	@Override
	public 布局 getLayout() {
		return null;
	}

	@Override
	public void 显示空状态() {
		view.showEmpty();
	}

	@Override
	public void 显示错误() {
		view.showError();
	}

	@Override
	public void 显示加载() {
		view.showLoading();
	}

	@Override
	public void 显示无网络() {
		view.showNoNetwork();
	}

	@Override
	public void 显示内容() {
		view.showContent();
	}

	@Override
	public void 请求重试() {
		EventDispatcher.dispatchEvent(this, "请求重试");
	}

	@Override
	public int 取当前状态() {
		return view.getViewStatus();
	}

	@Override
	public void 置状态内容(int state, 可视组件 component) {
		if (state < 1 || state > 4) {
			throw new 非法参数错误();
		}
		View target = ComponentOwnership.requireSibling(view, component);
		清除状态内容(state);
		BorrowedContent borrowed = new BorrowedContent(target, view.getRootView());
		FrameLayout host = stateHosts[state];
		try {
			host.removeAllViews();
			host.addView(target, new FrameLayout.LayoutParams(-1, -1));
			stateContents[state] = borrowed;
		} catch (RuntimeException failure) {
			borrowed.restore();
			restoreDefault(state);
			throw failure;
		}
	}

	@Override
	public void 清除状态内容(int state) {
		if (state < 1 || state > 4) {
			throw new 非法参数错误();
		}
		if (stateContents[state] != null) {
			BorrowedContent borrowed = stateContents[state];
			stateContents[state] = null;
			borrowed.restore();
			restoreDefault(state);
		}
	}

	/**
	 * 重建对应状态的内建视图，不替换上游保存的状态宿主。
	 *
	 * @param state 已校验的状态常量。
	 */
	private void restoreDefault(int state) {
		int[] layouts = {0, R.layout.msv_layout_loading_view, R.layout.msv_layout_empty_view,
			R.layout.msv_layout_error_view, R.layout.msv_layout_no_network_view};
		stateHosts[state].removeAllViews();
		LayoutInflater.from(view.getContext()).inflate(layouts[state], stateHosts[state], true);
	}

	@Override
	public void 销毁() {
		for (int state = 1; state <= 4; state++) {
			清除状态内容(state);
		}
		super.销毁();
	}

}
