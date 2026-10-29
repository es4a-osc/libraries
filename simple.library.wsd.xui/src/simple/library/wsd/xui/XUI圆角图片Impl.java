package simple.library.wsd.xui;

import android.view.View;
import android.widget.ImageView;

import com.xuexiang.xui.widget.imageview.RadiusImageView;

import simple.runtime.components.组件容器;

/**
 * XUI圆角图片的原生视图及 Simple 事件适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI圆角图片Impl extends XUI视图组件基类 implements XUI圆角图片 {

	// 视图在父类构造期间创建；不得用字段初始化器重置此处状态。
	private RadiusImageView view;

	/**
	 * 创建XUI圆角图片。
	 *
	 * @param container 容纳组件的 Simple 容器。
	 */
	public XUI圆角图片Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected View createView() {
		view = new RadiusImageView(ComponentSupport.createContext());

		return view;
	}

	@Override
	public void 置边框宽度(int borderWidth) {
		view.setBorderWidth(borderWidth);
	}

	@Override
	public void 置边框颜色(int borderColor) {
		view.setBorderColor(borderColor);
	}

	@Override
	public void 置圆角(int cornerRadius) {
		view.setCornerRadius(cornerRadius);
	}

	@Override
	public void 置选中边框颜色(int selectedBorderColor) {
		view.setSelectedBorderColor(selectedBorderColor);
	}

	@Override
	public void 置选中边框宽度(int selectedBorderWidth) {
		view.setSelectedBorderWidth(selectedBorderWidth);
	}

	@Override
	public void 置选中遮罩颜色(int selectedMaskColor) {
		view.setSelectedMaskColor(selectedMaskColor);
	}

	@Override
	public void 置圆形(boolean isCircle) {
		view.setCircle(isCircle);
	}

	@Override
	public void 置椭圆(boolean isOval) {
		view.setOval(isOval);
	}

	@Override
	public int 取边框颜色() {
		return view.getBorderColor();
	}

	@Override
	public int 取边框宽度() {
		return view.getBorderWidth();
	}

	@Override
	public int 取圆角() {
		return view.getCornerRadius();
	}

	@Override
	public int 取选中边框颜色() {
		return view.getSelectedBorderColor();
	}

	@Override
	public int 取选中边框宽度() {
		return view.getSelectedBorderWidth();
	}

	@Override
	public int 取选中遮罩颜色() {
		return view.getSelectedMaskColor();
	}

	@Override
	public boolean 是否圆形() {
		return view.isCircle();
	}

	@Override
	public boolean 是否椭圆() {
		return view.isOval();
	}

	@Override
	public void 置触摸选择模式启用(boolean touchSelectModeEnabled) {
		view.setTouchSelectModeEnabled(touchSelectModeEnabled);
	}

	@Override
	public boolean 是否触摸选择模式启用() {
		return view.isTouchSelectModeEnabled();
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

	@Override
	public void 置选中(boolean selected) {
		view.setSelected(selected);
	}

	@Override
	public boolean 取选中() {
		return view.isSelected();
	}

}
