package simple.library.wsd.xui;

import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.xuexiang.xui.widget.statelayout.StatefulLayout;

import simple.runtime.components.impl.android.视图组件容器;
import simple.runtime.components.布局;
import simple.runtime.components.组件;
import simple.runtime.components.组件容器;
import simple.runtime.events.EventDispatcher;

/**
 * XUI状态容器的原生视图及 Simple 事件适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI状态容器Impl extends XUI视图组件基类 implements XUI状态容器, 视图组件容器 {

	// 视图在父类构造期间创建；不得用字段初始化器重置此处状态。
	private StatefulLayout view;

	// 状态模板和用户内容使用不同布局。
	private LinearLayout content;

	/**
	 * 创建XUI状态容器。
	 *
	 * @param container 容纳组件的 Simple 容器。
	 */
	public XUI状态容器Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected View createView() {
		view = new StatefulLayout(ComponentSupport.createContext());

		content = new LinearLayout(view.getContext());
		content.setOrientation(LinearLayout.VERTICAL);
		view.addView(content);
		view.attachTemplate();
		view.setAnimationEnabled(false);
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
	public boolean 是否动画启用() {
		return view.isAnimationEnabled();
	}

	@Override
	public void 置动画启用(boolean animationEnabled) {
		view.setAnimationEnabled(animationEnabled);
	}

	@Override
	public void 置入场动画(int anim) {
		view.setInAnimation(anim);
	}

	@Override
	public void 置出场动画(int anim) {
		view.setOutAnimation(anim);
	}

	@Override
	public void 显示内容() {
		view.showContent();
	}

	@Override
	public void 显示加载() {
		view.showLoading();
	}

	@Override
	public void 显示加载资源(int resId) {
		view.showLoading(resId);
	}

	@Override
	public void 显示加载(String message) {
		view.showLoading(message);
	}

	@Override
	public void 显示空状态() {
		view.showEmpty();
	}

	@Override
	public void 显示空状态资源(int resId) {
		view.showEmpty(resId);
	}

	@Override
	public void 显示空状态(String message) {
		view.showEmpty(message);
	}

	@Override
	public void 置排列方向(int orientation) {
		view.setOrientation(orientation);
	}

	@Override
	public void 显示错误(String text) {
		view.showError(text, new View.OnClickListener() {
			@Override
			public void onClick(View clicked) {
				请求重试();
			}
		});
	}

	@Override
	public void 显示无网络(String text) {
		view.showOffline(text, new View.OnClickListener() {
			@Override
			public void onClick(View clicked) {
				请求重试();
			}
		});
	}

	@Override
	public void 显示定位关闭(String text) {
		view.showLocationOff(text, new View.OnClickListener() {
			@Override
			public void onClick(View clicked) {
				请求重试();
			}
		});
	}

	@Override
	public void 请求重试() {
		EventDispatcher.dispatchEvent(this, "请求重试");
	}
}
