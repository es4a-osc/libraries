package simple.library.wsd.xui;

import android.view.View;

import com.xuexiang.xui.widget.textview.label.LabelButtonView;

import simple.runtime.components.组件容器;

/**
 * XUI角标按钮的原生视图及 Simple 事件适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI角标按钮Impl extends XUI文本组件基类 implements XUI角标按钮 {

	// 视图在父类构造期间创建；不得用字段初始化器重置此处状态。
	private LabelButtonView view;

	/**
	 * 创建XUI角标按钮。
	 *
	 * @param container 容纳组件的 Simple 容器。
	 */
	public XUI角标按钮Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected View createView() {
		view = new LabelButtonView(ComponentSupport.createContext());

		return view;
	}

	@Override
	public void 置标签高度(int height) {
		view.setLabelHeight(height);
	}

	@Override
	public int 取标签高度() {
		return view.getLabelHeight();
	}

	@Override
	public void 置标签距离(int distance) {
		view.setLabelDistance(distance);
	}

	@Override
	public int 取标签距离() {
		return view.getLabelDistance();
	}

	@Override
	public boolean 是否标签可见() {
		return view.isLabelVisual();
	}

	@Override
	public void 置标签可见(boolean enable) {
		view.setLabelVisual(enable);
	}

	@Override
	public int 取标签方向() {
		return view.getLabelOrientation();
	}

	@Override
	public void 置标签方向(int orientation) {
		view.setLabelOrientation(orientation);
	}

	@Override
	public int 取标签文本颜色() {
		return view.getLabelTextColor();
	}

	@Override
	public void 置标签文本颜色(int textColor) {
		view.setLabelTextColor(textColor);
	}

	@Override
	public int 取标签背景颜色() {
		return view.getLabelBackgroundColor();
	}

	@Override
	public void 置标签背景颜色(int backgroundColor) {
		view.setLabelBackgroundColor(backgroundColor);
	}

	@Override
	public String 取标签文本() {
		return view.getLabelText();
	}

	@Override
	public void 置标签文本(String text) {
		view.setLabelText(text);
	}

	@Override
	public int 取标签文本尺寸() {
		return view.getLabelTextSize();
	}

	@Override
	public void 置标签文本尺寸(int textSize) {
		view.setLabelTextSize(textSize);
	}

	@Override
	public int 取标签文本样式() {
		return view.getLabelTextStyle();
	}

	@Override
	public void 置标签文本样式(int textStyle) {
		view.setLabelTextStyle(textStyle);
	}

	@Override
	public String 取标签文本字体() {
		return view.getLabelTextFont();
	}

	@Override
	public void 置标签文本字体(String textFont) {
		view.setLabelTextFont(textFont);
	}
}
