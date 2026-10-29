package simple.library.wsd.xui;

import android.view.View;

import com.xuexiang.xui.widget.button.SwitchIconView;

import simple.runtime.components.组件容器;

/**
 * XUI切换图标的原生视图及 Simple 事件适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI切换图标Impl extends XUI视图组件基类 implements XUI切换图标 {

	// 视图在父类构造期间创建；不得用字段初始化器重置此处状态。
	private SwitchIconView view;

	/**
	 * 创建XUI切换图标。
	 *
	 * @param container 容纳组件的 Simple 容器。
	 */
	public XUI切换图标Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected View createView() {
		view = new SwitchIconView(ComponentSupport.createContext());

		return view;
	}

	@Override
	public void 置图标启用(boolean enabled) {
		view.setIconEnabled(enabled);
	}

	@Override
	public void 置图标启用(boolean enabled, boolean animate) {
		view.setIconEnabled(enabled, animate);
	}

	@Override
	public boolean 是否图标启用() {
		return view.isIconEnabled();
	}

	@Override
	public void 切换状态() {
		view.switchState();
	}

	@Override
	public void 切换状态(boolean animate) {
		view.switchState(animate);
	}
}
