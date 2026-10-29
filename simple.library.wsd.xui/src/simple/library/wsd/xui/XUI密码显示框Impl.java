package simple.library.wsd.xui;

import android.view.View;

import com.xuexiang.xui.widget.edittext.verify.PwdTextView;

import simple.runtime.components.组件容器;

/**
 * XUI密码显示框的原生视图及 Simple 事件适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI密码显示框Impl extends XUI文本组件基类 implements XUI密码显示框 {

	// 视图在父类构造期间创建；不得用字段初始化器重置此处状态。
	private PwdTextView view;

	/**
	 * 创建XUI密码显示框。
	 *
	 * @param container 容纳组件的 Simple 容器。
	 */
	public XUI密码显示框Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected View createView() {
		view = new PwdTextView(ComponentSupport.createContext());

		return view;
	}

	@Override
	public void 清空密码() {
		view.clearPassword();
	}
}
