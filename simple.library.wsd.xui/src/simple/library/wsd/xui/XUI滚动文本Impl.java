package simple.library.wsd.xui;

import android.view.View;

import com.xuexiang.xui.widget.textview.AutoMoveTextView;

import simple.runtime.components.组件容器;

/**
 * XUI滚动文本的原生视图及 Simple 事件适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI滚动文本Impl extends XUI文本组件基类 implements XUI滚动文本 {

	// 视图在父类构造期间创建；不得用字段初始化器重置此处状态。
	private AutoMoveTextView view;

	/**
	 * 创建XUI滚动文本。
	 *
	 * @param container 容纳组件的 Simple 容器。
	 */
	public XUI滚动文本Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected View createView() {
		view = new AutoMoveTextView(ComponentSupport.createContext());

		return view;
	}

	@Override
	public boolean 是否获得焦点() {
		return view.isFocused();
	}
}
