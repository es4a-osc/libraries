package simple.library.wsd.xui;

import android.view.View;

import com.xuexiang.xui.widget.textview.supertextview.SuperButton;

import simple.runtime.components.组件容器;

/**
 * XUI超级按钮的原生视图及 Simple 事件适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI超级按钮Impl extends XUI文本组件基类 implements XUI超级按钮 {

	// 视图在父类构造期间创建；不得用字段初始化器重置此处状态。
	private SuperButton view;

	/**
	 * 创建XUI超级按钮。
	 *
	 * @param container 容纳组件的 Simple 容器。
	 */
	public XUI超级按钮Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected View createView() {
		view = new SuperButton(ComponentSupport.createContext());

		return view;
	}

	@Override
	public void 置形状类型(int type) {
		view.setShapeType(type);
	}

	@Override
	public void 置文本对齐(int gravity) {
		view.setTextGravity(gravity);
	}

	@Override
	public void 置形状按下颜色(int color) {
		view.setShapeSelectorPressedColor(color);
	}

	@Override
	public void 置形状正常颜色(int color) {
		view.setShapeSelectorNormalColor(color);
	}

	@Override
	public void 置形状禁用颜色(int color) {
		view.setShapeSelectorDisableColor(color);
	}

	@Override
	public void 置形状填充颜色(int color) {
		view.setShapeSolidColor(color);
	}

	@Override
	public void 置形状描边宽度(int strokeWidth) {
		view.setShapeStrokeWidth(strokeWidth);
	}

	@Override
	public void 置形状描边颜色(int strokeColor) {
		view.setShapeStrokeColor(strokeColor);
	}

	@Override
	public void 置形状描边虚线宽度(float strokeDashWidth) {
		view.setShapeStrokeDashWidth(strokeDashWidth);
	}

	@Override
	public void 置形状描边虚线间隔(float strokeDashGap) {
		view.setShapeStrokeDashGap(strokeDashGap);
	}

	@Override
	public void 置形状圆角(float radius) {
		view.setShapeCornersRadius(radius);
	}

	@Override
	public void 置形状左上圆角(float radius) {
		view.setShapeCornersTopLeftRadius(radius);
	}

	@Override
	public void 置形状右上圆角(float radius) {
		view.setShapeCornersTopRightRadius(radius);
	}

	@Override
	public void 置形状左下圆角(float radius) {
		view.setShapeCornersBottomLeftRadius(radius);
	}

	@Override
	public void 置形状右下圆角(float radius) {
		view.setShapeCornersBottomRightRadius(radius);
	}

	@Override
	public void 置形状尺寸宽度(int sizeWidth) {
		view.setShapeSizeWidth(sizeWidth);
	}

	@Override
	public void 置形状尺寸高度(int sizeHeight) {
		view.setShapeSizeHeight(sizeHeight);
	}

	@Override
	public void 置形状渐变方向(int gradientOrientation) {
		view.setShapeGradientOrientation(gradientOrientation);
	}

	@Override
	public void 置形状渐变中心横坐标(int gradientCenterX) {
		view.setShapeGradientCenterX(gradientCenterX);
	}

	@Override
	public void 置形状渐变中心纵坐标(int gradientCenterY) {
		view.setShapeGradientCenterY(gradientCenterY);
	}

	@Override
	public void 置形状渐变半径(int gradientGradientRadius) {
		view.setShapeGradientGradientRadius(gradientGradientRadius);
	}

	@Override
	public void 置形状渐变开始颜色(int gradientStartColor) {
		view.setShapeGradientStartColor(gradientStartColor);
	}

	@Override
	public void 置形状渐变中间颜色(int gradientCenterColor) {
		view.setShapeGradientCenterColor(gradientCenterColor);
	}

	@Override
	public void 置形状渐变结束颜色(int gradientEndColor) {
		view.setShapeGradientEndColor(gradientEndColor);
	}

	@Override
	public void 置形状渐变类型(int gradientType) {
		view.setShapeGradientType(gradientType);
	}

	@Override
	public void 置形状渐变使用级别(boolean gradientUseLevel) {
		view.setShapeGradientUseLevel(gradientUseLevel);
	}

	@Override
	public void 置形状使用状态选择器(boolean useSelector) {
		view.setShapeUseSelector(useSelector);
	}

	@Override
	public void 应用形状() {
		view.setUseShape();
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
