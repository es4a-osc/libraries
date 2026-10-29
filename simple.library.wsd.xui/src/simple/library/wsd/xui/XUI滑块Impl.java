package simple.library.wsd.xui;

import android.view.LayoutInflater;
import android.view.View;

import com.xuexiang.xui.widget.picker.XSeekBar;

import simple.runtime.components.组件容器;
import simple.runtime.errors.非法参数错误;
import simple.runtime.events.EventDispatcher;

/**
 * XUI滑块的原生视图及 Simple 事件适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI滑块Impl extends XUI视图组件基类 implements XUI滑块 {

	// 视图在父类构造期间创建；不得用字段初始化器重置此处状态。
	private XSeekBar view;

	/**
	 * 创建XUI滑块。
	 *
	 * @param container 容纳组件的 Simple 容器。
	 */
	public XUI滑块Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected View createView() {
		view = (XSeekBar) LayoutInflater.from(ComponentSupport.createContext()).inflate(R.layout.es4a_xui_seek_bar, null);

		view.setOnSeekBarListener(new XSeekBar.OnSeekBarListener() {

			@Override
			public void onValueChanged(XSeekBar slider, int value) {
				数值改变(value);
			}
		});

		return view;
	}

	@Override
	public int 取选中值() {
		return view.getSelectedNumber();
	}

	@Override
	public void 置默认值(int value) {
		if (value < view.getMin() || value > view.getMax()) {
			throw new 非法参数错误();
		}
		view.setDefaultValue(value);
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
	public void 置刻度间隔(int rulerInterval) {
		// 上游绘制会计算 interval / 2 并用于取余，零或一会导致除零。
		if (rulerInterval < 2) {
			throw new 非法参数错误();
		}
		view.setInterval(rulerInterval);
	}

	@Override
	public void 重置() {
		view.reset();
	}

	@Override
	public void 数值改变(int value) {
		EventDispatcher.dispatchEvent(this, "数值改变", value);
	}
}
