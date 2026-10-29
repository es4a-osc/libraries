package simple.library.wsd.xui;

import android.view.View;

import com.xuexiang.xui.widget.shadow.ShadowTextView;

import simple.runtime.components.组件容器;

/**
 * XUI阴影文本的原生视图及 Simple 事件适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI阴影文本Impl extends XUI文本组件基类 implements XUI阴影文本 {

	// 视图在父类构造期间创建；不得用字段初始化器重置此处状态。
	private ShadowTextView view;

	/**
	 * 创建XUI阴影文本。
	 *
	 * @param container 容纳组件的 Simple 容器。
	 */
	public XUI阴影文本Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected View createView() {
		view = new ShadowTextView(ComponentSupport.createContext());

		return view;
	}
}
