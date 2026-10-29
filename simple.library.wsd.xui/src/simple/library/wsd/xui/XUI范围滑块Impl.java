package simple.library.wsd.xui;

import android.view.LayoutInflater;
import android.view.View;

import com.xuexiang.xui.widget.picker.XRangeSlider;

import simple.runtime.components.组件容器;
import simple.runtime.errors.非法参数错误;
import simple.runtime.events.EventDispatcher;

/**
 * XUI范围滑块的原生视图及 Simple 事件适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI范围滑块Impl extends XUI视图组件基类 implements XUI范围滑块 {

	// 视图在父类构造期间创建；不得用字段初始化器重置此处状态。
	private XRangeSlider view;

	/**
	 * 创建XUI范围滑块。
	 *
	 * @param container 容纳组件的 Simple 容器。
	 */
	public XUI范围滑块Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected View createView() {
		view = (XRangeSlider) LayoutInflater.from(ComponentSupport.createContext()).inflate(R.layout.es4a_xui_range_slider, null);

		view.setOnRangeSliderListener(new XRangeSlider.OnRangeSliderListener() {

			@Override
			public void onMinChanged(XRangeSlider slider, int value) {
				范围改变(slider.getSelectedMin(), slider.getSelectedMax());
			}

			@Override
			public void onMaxChanged(XRangeSlider slider, int value) {
				范围改变(slider.getSelectedMin(), slider.getSelectedMax());
			}
		});

		return view;
	}

	@Override
	public int 取选中下限() {
		return view.getSelectedMin();
	}

	@Override
	public int 取选中上限() {
		return view.getSelectedMax();
	}

	@Override
	public void 置初始范围(int startingMin, int startingMax) {
		if (startingMin < view.getMin() || startingMax > view.getMax() || startingMin > startingMax) {
			throw new 非法参数错误();
		}
		view.setStartingMinMax(startingMin, startingMax);
	}

	@Override
	public int 取最小值() {
		return view.getMin();
	}

	@Override
	public void 置最小值(int min) {
		if (min >= view.getMax()) {
			throw new 非法参数错误();
		}
		view.setMin(min);
	}

	@Override
	public int 取最大值() {
		return view.getMax();
	}

	@Override
	public void 置最大值(int max) {
		if (max <= view.getMin()) {
			throw new 非法参数错误();
		}
		view.setMax(max);
	}

	@Override
	public void 重置() {
		view.reset();
	}

	@Override
	public void 范围改变(int minimum, int maximum) {
		EventDispatcher.dispatchEvent(this, "范围改变", minimum, maximum);
	}
}
