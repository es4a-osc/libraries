package simple.library.wsd.xui;

import android.view.View;
import android.widget.ImageView;

import com.xuexiang.xui.widget.textview.label.LabelImageView;

import simple.runtime.components.组件容器;

/**
 * XUI角标图片的原生视图及 Simple 事件适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI角标图片Impl extends XUI视图组件基类 implements XUI角标图片 {

	// 视图在父类构造期间创建；不得用字段初始化器重置此处状态。
	private LabelImageView view;

	/**
	 * 创建XUI角标图片。
	 *
	 * @param container 容纳组件的 Simple 容器。
	 */
	public XUI角标图片Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected View createView() {
		view = new LabelImageView(ComponentSupport.createContext());

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
	public void 置标签背景透明度(int alpha) {
		view.setLabelBackgroundAlpha(alpha);
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

	@Override
	public void 置图片(String image) {
		view.setImageDrawable(ComponentSupport.getDrawable(image));
	}

	@Override
	public void 置图片缩放模式(int mode) {
		ComponentSupport.validateIndex(mode, ImageView.ScaleType.values().length);
		view.setScaleType(ImageView.ScaleType.values()[mode]);
	}

}
