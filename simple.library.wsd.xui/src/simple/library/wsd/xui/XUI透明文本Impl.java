package simple.library.wsd.xui;

import android.view.View;

import com.xuexiang.xui.widget.alpha.XUIAlphaTextView;

import simple.runtime.components.组件容器;

/**
 * XUI透明文本的原生视图及 Simple 事件适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI透明文本Impl extends XUI文本组件基类 implements XUI透明文本 {

	// 视图在父类构造期间创建；不得用字段初始化器重置此处状态。
	private XUIAlphaTextView view;

	/**
	 * 创建XUI透明文本。
	 *
	 * @param container 容纳组件的 Simple 容器。
	 */
	public XUI透明文本Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected View createView() {
		view = new XUIAlphaTextView(ComponentSupport.createContext());

		return view;
	}

	@Override
	public void 置按压透明反馈(boolean changeAlphaWhenPress) {
		view.setChangeAlphaWhenPress(changeAlphaWhenPress);
	}

	@Override
	public void 置禁用透明反馈(boolean changeAlphaWhenDisable) {
		view.setChangeAlphaWhenDisable(changeAlphaWhenDisable);
	}
}
