package simple.library.wsd.xui;

import android.view.View;

import com.xuexiang.xui.widget.button.shadowbutton.ShadowButton;

import simple.runtime.components.组件容器;

/**
 * XUI阴影按钮的原生视图及 Simple 事件适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI阴影按钮Impl extends XUI文本组件基类 implements XUI阴影按钮 {

	// 视图在父类构造期间创建；不得用字段初始化器重置此处状态。
	private ShadowButton view;

	/**
	 * 创建XUI阴影按钮。
	 *
	 * @param container 容纳组件的 Simple 容器。
	 */
	public XUI阴影按钮Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected View createView() {
		view = new ShadowButton(ComponentSupport.createContext());

		return view;
	}

	@Override
	public int 取按下颜色() {
		return view.getPressedColor();
	}

	@Override
	public void 置按下颜色(int pressedColor) {
		view.setPressedColor(pressedColor);
	}

	@Override
	public void 置未按下颜色(int color) {
		view.setUnpressedColor(color);
	}

	@Override
	public int 取形状类型() {
		return view.getShapeType();
	}

	@Override
	public void 置形状类型(int shapeType) {
		view.setShapeType(shapeType);
	}

	@Override
	public int 取圆角() {
		return view.getRadius();
	}

	@Override
	public void 置圆角(int radius) {
		view.setRadius(radius);
	}
}
