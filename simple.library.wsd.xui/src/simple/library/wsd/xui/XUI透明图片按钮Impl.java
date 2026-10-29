package simple.library.wsd.xui;

import android.view.View;

import com.xuexiang.xui.widget.alpha.XUIAlphaImageButton;

import simple.runtime.components.组件容器;

/**
 * XUI透明图片按钮的原生视图及 Simple 事件适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI透明图片按钮Impl extends XUI视图组件基类 implements XUI透明图片按钮 {

	// 视图在父类构造期间创建；不得用字段初始化器重置此处状态。
	private XUIAlphaImageButton view;

	/**
	 * 创建XUI透明图片按钮。
	 *
	 * @param container 容纳组件的 Simple 容器。
	 */
	public XUI透明图片按钮Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected View createView() {
		view = new XUIAlphaImageButton(ComponentSupport.createContext());

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

	@Override
	public void 置图片(String image) {
		view.setImageDrawable(ComponentSupport.getDrawable(image));
	}
}
