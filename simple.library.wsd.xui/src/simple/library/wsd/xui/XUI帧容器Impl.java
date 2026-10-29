package simple.library.wsd.xui;

import android.view.View;
import android.view.ViewGroup;

import com.xuexiang.xui.widget.layout.XUIFrameLayout;

import simple.runtime.components.impl.android.视图组件容器;
import simple.runtime.components.布局;
import simple.runtime.components.组件;
import simple.runtime.components.组件容器;
import simple.runtime.errors.非法参数错误;

/**
 * XUI帧容器的原生视图及 Simple 事件适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI帧容器Impl extends XUI视图组件基类 implements XUI帧容器, 视图组件容器 {

	// 视图在父类构造期间创建；不得用字段初始化器重置此处状态。
	private XUIFrameLayout view;

	/**
	 * 创建XUI帧容器。
	 *
	 * @param container 容纳组件的 Simple 容器。
	 */
	public XUI帧容器Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected View createView() {
		view = new XUIFrameLayout(ComponentSupport.createContext());

		return view;
	}

	@Override
	public ViewGroup getLayoutManager() {
		return ComponentSupport.getChildLayout((ViewGroup) view);
	}

	@Override
	public void addComponent(组件 component) {
		ComponentSupport.addChildComponent((ViewGroup) view, component);
	}

	@Override
	public 布局 getLayout() {
		return null;
	}

	@Override
	public void 置顶部分隔线透明度(int dividerAlpha) {
		view.setTopDividerAlpha(dividerAlpha);
	}

	@Override
	public void 置底部分隔线透明度(int dividerAlpha) {
		view.setBottomDividerAlpha(dividerAlpha);
	}

	@Override
	public void 置左侧分隔线透明度(int dividerAlpha) {
		view.setLeftDividerAlpha(dividerAlpha);
	}

	@Override
	public void 置右侧分隔线透明度(int dividerAlpha) {
		view.setRightDividerAlpha(dividerAlpha);
	}

	@Override
	public void 置圆角与阴影(int radius, int shadowElevation, float shadowAlpha) {
		view.setRadiusAndShadow(radius, shadowElevation, shadowAlpha);
	}

	@Override
	public void 置圆角与阴影(int radius, int hideRadiusSide, int shadowElevation, float shadowAlpha) {
		view.setRadiusAndShadow(radius, hideRadiusSide, shadowElevation, shadowAlpha);
	}

	@Override
	public void 置圆角(int radius) {
		view.setRadius(radius);
	}

	@Override
	public void 置圆角(int radius, int hideRadiusSide) {
		view.setRadius(radius, hideRadiusSide);
	}

	@Override
	public int 取圆角() {
		return view.getRadius();
	}

	@Override
	public void 置轮廓内缩(int left, int top, int right, int bottom) {
		view.setOutlineInset(left, top, right, bottom);
	}

	@Override
	public void 置隐藏圆角边(int hideRadiusSide) {
		view.setHideRadiusSide(hideRadiusSide);
	}

	@Override
	public int 取隐藏圆角边() {
		return view.getHideRadiusSide();
	}

	@Override
	public void 置边框颜色(int borderColor) {
		view.setBorderColor(borderColor);
	}

	@Override
	public void 置边框宽度(int borderWidth) {
		view.setBorderWidth(borderWidth);
	}

	@Override
	public boolean 置宽度限制(int widthLimit) {
		return view.setWidthLimit(widthLimit);
	}

	@Override
	public boolean 置高度限制(int heightLimit) {
		return view.setHeightLimit(heightLimit);
	}

	@Override
	public void 置轮廓排除内边距(boolean outlineExcludePadding) {
		view.setOutlineExcludePadding(outlineExcludePadding);
	}

	@Override
	public void 置阴影高度(int elevation) {
		view.setShadowElevation(elevation);
	}

	@Override
	public int 取阴影高度() {
		return view.getShadowElevation();
	}

	@Override
	public void 置阴影透明度(float shadowAlpha) {
		view.setShadowAlpha(shadowAlpha);
	}

	@Override
	public float 取阴影透明度() {
		return view.getShadowAlpha();
	}

	@Override
	public void 置阴影颜色(int shadowColor) {
		view.setShadowColor(shadowColor);
	}

	@Override
	public int 取阴影颜色() {
		return view.getShadowColor();
	}

	@Override
	public void 置外部正常颜色(int color) {
		view.setOuterNormalColor(color);
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
	public void 置顶部分隔线(int start, int end, int size, int color) {
		if (start < 0 || end < 0 || size < 0) {
			throw new 非法参数错误();
		}
		view.updateTopDivider(start, end, size, color);
	}

	@Override
	public void 置底部分隔线(int start, int end, int size, int color) {
		if (start < 0 || end < 0 || size < 0) {
			throw new 非法参数错误();
		}
		view.updateBottomDivider(start, end, size, color);
	}

	@Override
	public void 置左侧分隔线(int start, int end, int size, int color) {
		if (start < 0 || end < 0 || size < 0) {
			throw new 非法参数错误();
		}
		view.updateLeftDivider(start, end, size, color);
	}

	@Override
	public void 置右侧分隔线(int start, int end, int size, int color) {
		if (start < 0 || end < 0 || size < 0) {
			throw new 非法参数错误();
		}
		view.updateRightDivider(start, end, size, color);
	}

}
