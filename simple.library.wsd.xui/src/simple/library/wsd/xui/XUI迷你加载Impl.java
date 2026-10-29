package simple.library.wsd.xui;

import android.view.View;

import com.xuexiang.xui.widget.progress.loading.MiniLoadingView;

import simple.runtime.components.组件容器;

/**
 * XUI迷你加载的原生视图及 Simple 事件适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI迷你加载Impl extends XUI视图组件基类 implements XUI迷你加载 {

	// 视图在父类构造期间创建；不得用字段初始化器重置此处状态。
	private MiniLoadingView view;

	/**
	 * 创建XUI迷你加载。
	 *
	 * @param container 容纳组件的 Simple 容器。
	 */
	public XUI迷你加载Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected View createView() {
		view = new MiniLoadingView(ComponentSupport.createContext());

		return view;
	}

	@Override
	public void 置颜色(int color) {
		view.setColor(color);
	}

	@Override
	public void 置尺寸(int size) {
		view.setSize(size);
	}

	@Override
	public void 开始() {
		view.start();
	}

	@Override
	public void 停止() {
		view.stop();
	}
}
