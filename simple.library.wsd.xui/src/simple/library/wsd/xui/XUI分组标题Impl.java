package simple.library.wsd.xui;

import android.view.View;

import com.xuexiang.xui.widget.grouplist.XUIGroupListSectionHeaderFooterView;

import simple.runtime.components.组件容器;

/**
 * XUI分组标题的原生视图及 Simple 事件适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI分组标题Impl extends XUI视图组件基类 implements XUI分组标题 {

	// 视图在父类构造期间创建；不得用字段初始化器重置此处状态。
	private XUIGroupListSectionHeaderFooterView view;

	/**
	 * 创建XUI分组标题。
	 *
	 * @param container 容纳组件的 Simple 容器。
	 */
	public XUI分组标题Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected View createView() {
		view = new XUIGroupListSectionHeaderFooterView(ComponentSupport.createContext());

		return view;
	}

	@Override
	public void 置文本对齐(int gravity) {
		view.setTextGravity(gravity);
	}

	@Override
	public void 置文本(String text) {
		view.setText(text);
	}
}
