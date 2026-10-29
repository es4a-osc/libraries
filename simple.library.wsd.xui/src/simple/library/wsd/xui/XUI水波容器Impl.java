package simple.library.wsd.xui;

import android.view.View;
import android.view.ViewGroup;

import com.xuexiang.xui.widget.button.RippleView;

import simple.runtime.components.impl.android.视图组件容器;
import simple.runtime.components.布局;
import simple.runtime.components.组件;
import simple.runtime.components.组件容器;
import simple.runtime.errors.非法参数错误;
import simple.runtime.events.EventDispatcher;

/**
 * XUI水波容器的原生视图及 Simple 事件适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI水波容器Impl extends XUI视图组件基类 implements XUI水波容器, 视图组件容器 {

	// 视图在父类构造期间创建；不得用字段初始化器重置此处状态。
	private RippleView view;

	/**
	 * 创建XUI水波容器。
	 *
	 * @param container 容纳组件的 Simple 容器。
	 */
	public XUI水波容器Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected View createView() {
		view = new RippleView(ComponentSupport.createContext());

		view.setOnRippleCompleteListener(new RippleView.OnRippleCompleteListener() {

			@Override
			public void onComplete(RippleView ripple) {
				水波完成();
			}
		});

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
	public void 置水波颜色(int rippleColor) {
		view.setRippleColor(rippleColor);
	}

	@Override
	public int 取水波颜色() {
		return view.getRippleColor();
	}

	@Override
	public boolean 是否居中() {
		return view.isCentered();
	}

	@Override
	public void 置居中(boolean isCentered) {
		view.setCentered(isCentered);
	}

	@Override
	public int 取水波内边距() {
		return view.getRipplePadding();
	}

	@Override
	public void 置水波内边距(int ripplePadding) {
		view.setRipplePadding(ripplePadding);
	}

	@Override
	public boolean 是否缩放() {
		return view.isZooming();
	}

	@Override
	public void 置缩放(boolean hasToZoom) {
		view.setZooming(hasToZoom);
	}

	@Override
	public float 取缩放比例() {
		return view.getZoomScale();
	}

	@Override
	public void 置缩放比例(float zoomScale) {
		view.setZoomScale(zoomScale);
	}

	@Override
	public int 取缩放时长() {
		return view.getZoomDuration();
	}

	@Override
	public void 置缩放时长(int zoomDuration) {
		view.setZoomDuration(zoomDuration);
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
	public int 取帧频率() {
		return view.getFrameRate();
	}

	@Override
	public void 置帧频率(int frameRate) {
		view.setFrameRate(frameRate);
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
	public void 置水波类型(int type) {
		if (type < 0 || type > 2) {
			throw new 非法参数错误();
		}
		view.setRippleType(RippleView.RippleType.values()[type]);
	}

	@Override
	public int 取水波类型() {
		return view.getRippleType().ordinal();
	}

	@Override
	public void 水波完成() {
		EventDispatcher.dispatchEvent(this, "水波完成");
	}
}
