package simple.library.wsd.xui;

import android.view.View;

import com.xuexiang.xui.widget.button.shadowbutton.RippleShadowShadowButton;

import simple.runtime.components.组件容器;

/**
 * XUI水波阴影按钮的原生视图及 Simple 事件适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI水波阴影按钮Impl extends XUI文本组件基类 implements XUI水波阴影按钮 {

	// 视图在父类构造期间创建；不得用字段初始化器重置此处状态。
	private RippleShadowShadowButton view;

	/**
	 * 创建XUI水波阴影按钮。
	 *
	 * @param container 容纳组件的 Simple 容器。
	 */
	public XUI水波阴影按钮Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected View createView() {
		view = new RippleShadowShadowButton(ComponentSupport.createContext());

		return view;
	}

	@Override
	public int 取圆角() {
		return view.getRoundRadius();
	}

	@Override
	public void 置圆角(int roundRadius) {
		view.setRoundRadius(roundRadius);
	}

	@Override
	public int 取水波颜色() {
		return view.getRippleColor();
	}

	@Override
	public void 置水波颜色(int rippleColor) {
		view.setRippleColor(rippleColor);
	}

	@Override
	public int 取水波时长() {
		return view.getRippleDuration();
	}

	@Override
	public void 置水波时长(int rippleDuration) {
		view.setRippleDuration(rippleDuration);
	}

	@Override
	public int 取水波圆角() {
		return view.getRippleRadius();
	}

	@Override
	public void 置水波圆角(int rippleRadius) {
		view.setRippleRadius(rippleRadius);
	}

	@Override
	public int 取水波透明度() {
		return view.getRippleAlpha();
	}

	@Override
	public void 置水波透明度(int rippleAlpha) {
		view.setRippleAlpha(rippleAlpha);
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
}
