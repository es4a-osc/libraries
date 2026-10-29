package simple.library.wsd.xui;

import android.view.View;

import com.xuexiang.xui.widget.shadow.ShadowButton;

import simple.runtime.components.组件容器;

/**
 * XUI扩散阴影按钮的原生视图及 Simple 事件适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI扩散阴影按钮Impl extends XUI文本组件基类 implements XUI扩散阴影按钮 {

	// 视图在父类构造期间创建；不得用字段初始化器重置此处状态。
	private ShadowButton view;

	/**
	 * 创建XUI扩散阴影按钮。
	 *
	 * @param container 容纳组件的 Simple 容器。
	 */
	public XUI扩散阴影按钮Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected View createView() {
		view = new ShadowButton(ComponentSupport.createContext());

		return view;
	}
}
