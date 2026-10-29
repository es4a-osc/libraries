package simple.library.wsd.xui;

import android.view.View;

import com.xuexiang.xui.widget.progress.loading.RotateLoadingView;

import simple.runtime.components.组件容器;

/**
 * XUI旋转加载的原生视图及 Simple 事件适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI旋转加载Impl extends XUI视图组件基类 implements XUI旋转加载 {

	// 视图在父类构造期间创建；不得用字段初始化器重置此处状态。
	private RotateLoadingView view;

	/**
	 * 创建XUI旋转加载。
	 *
	 * @param container 容纳组件的 Simple 容器。
	 */
	public XUI旋转加载Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected View createView() {
		view = new RotateLoadingView(ComponentSupport.createContext());

		return view;
	}

	@Override
	public void 置加载颜色(int color) {
		view.setLoadingColor(color);
	}

	@Override
	public int 取加载颜色() {
		return view.getLoadingColor();
	}

	@Override
	public void 开始() {
		view.start();
	}

	@Override
	public void 停止() {
		view.stop();
	}

	@Override
	public boolean 是否开始() {
		return view.isStart();
	}

	@Override
	public void 置加载图标(String image) {
		view.setLoadingIcon(ComponentSupport.getDrawable(image));
	}
}
