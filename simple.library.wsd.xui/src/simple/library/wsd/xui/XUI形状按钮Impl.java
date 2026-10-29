package simple.library.wsd.xui;

import android.view.View;

import com.xuexiang.xui.widget.button.ButtonView;

import simple.runtime.components.组件容器;

/**
 * XUI形状按钮的原生视图及 Simple 事件适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI形状按钮Impl extends XUI文本组件基类 implements XUI形状按钮 {

	// 视图在父类构造期间创建；不得用字段初始化器重置此处状态。
	private ButtonView view;

	/**
	 * 创建XUI形状按钮。
	 *
	 * @param container 容纳组件的 Simple 容器。
	 */
	public XUI形状按钮Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected View createView() {
		view = new ButtonView(ComponentSupport.createContext());

		return view;
	}

	@Override
	public void 置文本图像(int drawableId, int space) {
		view.setTextDrawable(drawableId, space);
	}

	@Override
	public void 置填充颜色(int colorId) {
		view.setSolidColor(colorId);
	}

	@Override
	public void 置圆角(int leftTopRadius, int leftBottomRadius, int rightTopRadius, int rightBottomRadius) {
		view.setRadius(leftTopRadius, leftBottomRadius, rightTopRadius, rightBottomRadius);
	}

	@Override
	public void 置描边宽度与颜色(int strokeWidth, int colorId) {
		view.setStrokeColorAndWidth(strokeWidth, colorId);
	}

	@Override
	public void 置选中文本颜色(int normalTextColor, int selectedTextColor) {
		view.setSelectedTextColor(normalTextColor, selectedTextColor);
	}

	@Override
	public void 置选中填充颜色(int selectedSolidColor) {
		view.setSelectedSolidColor(selectedSolidColor);
	}
}
