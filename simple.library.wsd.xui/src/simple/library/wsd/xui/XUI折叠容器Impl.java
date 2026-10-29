package simple.library.wsd.xui;

import android.view.View;
import android.view.ViewGroup;

import com.xuexiang.xui.widget.layout.ExpandableLayout;

import simple.runtime.components.impl.android.视图组件容器;
import simple.runtime.components.布局;
import simple.runtime.components.组件;
import simple.runtime.components.组件容器;
import simple.runtime.events.EventDispatcher;

/**
 * XUI折叠容器的原生视图及 Simple 事件适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI折叠容器Impl extends XUI视图组件基类 implements XUI折叠容器, 视图组件容器 {

	// 视图在父类构造期间创建；不得用字段初始化器重置此处状态。
	private ExpandableLayout view;

	/**
	 * 创建XUI折叠容器。
	 *
	 * @param container 容纳组件的 Simple 容器。
	 */
	public XUI折叠容器Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected View createView() {
		view = new ExpandableLayout(ComponentSupport.createContext());
		// 上游只在传入 XML 属性时赋予默认方向及视差；Java 构造须显式补齐。
		view.setOrientation(ExpandableLayout.VERTICAL);
		view.setParallax(1);

		view.setOnExpansionChangedListener(new ExpandableLayout.OnExpansionChangedListener() {

			@Override
			public void onExpansionChanged(float fraction, int state) {
				展开改变(fraction, state);
			}
		});

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
	public int 取状态() {
		return view.getState();
	}

	@Override
	public boolean 是否展开() {
		return view.isExpanded();
	}

	@Override
	public void 切换() {
		view.toggle();
	}

	@Override
	public void 切换(boolean animate) {
		view.toggle(animate);
	}

	@Override
	public void 展开() {
		view.expand();
	}

	@Override
	public void 展开(boolean animate) {
		view.expand(animate);
	}

	@Override
	public void 收起() {
		view.collapse();
	}

	@Override
	public void 收起(boolean animate) {
		view.collapse(animate);
	}

	@Override
	public void 置展开(boolean expand) {
		view.setExpanded(expand);
	}

	@Override
	public void 置展开(boolean expand, boolean animate) {
		view.setExpanded(expand, animate);
	}

	@Override
	public int 取时长() {
		return view.getDuration();
	}

	@Override
	public void 置时长(int duration) {
		view.setDuration(duration);
	}

	@Override
	public float 取展开比例() {
		return view.getExpansion();
	}

	@Override
	public void 置展开比例(float expansion) {
		view.setExpansion(expansion);
	}

	@Override
	public float 取视差() {
		return view.getParallax();
	}

	@Override
	public void 置视差(float parallax) {
		view.setParallax(parallax);
	}

	@Override
	public void 展开改变(float fraction, int state) {
		EventDispatcher.dispatchEvent(this, "展开改变", fraction, state);
	}
}
