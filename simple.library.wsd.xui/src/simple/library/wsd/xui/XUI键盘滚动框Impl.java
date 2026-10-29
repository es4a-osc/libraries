package simple.library.wsd.xui;

import android.view.View;
import android.view.ViewGroup;

import com.xuexiang.xui.widget.XUIKeyboardScrollView;

import simple.runtime.components.impl.android.视图组件容器;
import simple.runtime.components.布局;
import simple.runtime.components.组件;
import simple.runtime.components.组件容器;

/**
 * XUI键盘滚动框的原生视图及 Simple 事件适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI键盘滚动框Impl extends XUI滚动容器基类 implements XUI键盘滚动框, 视图组件容器 {

	// 视图在父类构造期间创建；不得用字段初始化器重置此处状态。
	private XUIKeyboardScrollView view;

	/**
	 * 创建XUI键盘滚动框。
	 *
	 * @param container 容纳组件的 Simple 容器。
	 */
	public XUI键盘滚动框Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected View createView() {
		view = new XUIKeyboardScrollView(ComponentSupport.createContext());

		return view;
	}

	@Override
	public ViewGroup getLayoutManager() {
		return ComponentSupport.getChildLayout((ViewGroup) view);
	}

	@Override
	public void addComponent(组件 component) {
		ComponentSupport.addChildComponent((ViewGroup) view, component);
	}

	@Override
	public 布局 getLayout() {
		return null;
	}

	@Override
	public void 置是否滚动隐藏键盘(boolean isScrollHideKeyboard) {
		view.setIsScrollHideKeyboard(isScrollHideKeyboard);
	}
}
