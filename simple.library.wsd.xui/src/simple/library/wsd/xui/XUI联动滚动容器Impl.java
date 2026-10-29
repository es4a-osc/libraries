package simple.library.wsd.xui;

import android.view.View;
import android.view.ViewGroup;

import com.xuexiang.xui.widget.layout.linkage.LinkageScrollLayout;

import simple.runtime.components.impl.android.视图组件容器;
import simple.runtime.components.布局;
import simple.runtime.components.组件;
import simple.runtime.components.组件容器;
import simple.runtime.errors.非法参数错误;

/**
 * XUI联动滚动容器的原生视图及 Simple 事件适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI联动滚动容器Impl extends XUI视图组件基类 implements XUI联动滚动容器, 视图组件容器 {

	// 视图在父类构造期间创建；不得用字段初始化器重置此处状态。
	private LinkageScrollLayout view;

	/**
	 * 创建XUI联动滚动容器。
	 *
	 * @param container 容纳组件的 Simple 容器。
	 */
	public XUI联动滚动容器Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected View createView() {
		view = new LinkageContainerView(ComponentSupport.createContext());

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
	public void 滚动到子项(int index) {
		ComponentSupport.validateIndex(index, view.getChildCount());
		view.gotoChild(index);
	}

	@Override
	public void 滚动到(int position) {
		if (position < 0) {
			throw new 非法参数错误();
		}
		view.scrollTo(0, position, false);
	}

	@Override
	public void 滚动距离(int distance) {
		view.scrollBy(0, distance);
	}

	@Override
	public int 取滚动位置() {
		return view.getScrollY();
	}

}
