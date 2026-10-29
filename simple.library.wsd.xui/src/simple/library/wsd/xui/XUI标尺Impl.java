package simple.library.wsd.xui;

import android.view.View;

import com.xuexiang.xui.widget.picker.RulerView;

import simple.runtime.components.组件容器;
import simple.runtime.events.EventDispatcher;

/**
 * XUI标尺的原生视图及 Simple 事件适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI标尺Impl extends XUI视图组件基类 implements XUI标尺 {

	// 视图在父类构造期间创建；不得用字段初始化器重置此处状态。
	private RulerView view;

	/**
	 * 创建XUI标尺。
	 *
	 * @param container 容纳组件的 Simple 容器。
	 */
	public XUI标尺Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected View createView() {
		view = new RulerView(ComponentSupport.createContext());

		view.setOnChooseResultListener(new RulerView.OnChooseResultListener() {

			@Override
			public void onScrollResult(String text) {
				选择改变(text);
			}

			@Override
			public void onEndResult(String text) {
				选择完成(text);
			}
		});

		return view;
	}

	@Override
	public void 置标尺高度(int rulerHeight) {
		view.setRulerHeight(rulerHeight);
	}

	@Override
	public void 置标尺到结果间隔(int rulerToResultGap) {
		view.setRulerToResultGap(rulerToResultGap);
	}

	@Override
	public void 置刻度数量(int scaleCount) {
		view.setScaleCount(scaleCount);
	}

	@Override
	public void 置刻度间隔(int scaleGap) {
		view.setScaleGap(scaleGap);
	}

	@Override
	public void 置最小刻度(int minScale) {
		view.setMinScale(minScale);
	}

	@Override
	public void 置首个刻度(float firstScale) {
		view.setFirstScale(firstScale);
	}

	@Override
	public void 置最大刻度(int maxScale) {
		view.setMaxScale(maxScale);
	}

	@Override
	public void 置背景颜色(int bgColor) {
		view.setBgColor(bgColor);
	}

	@Override
	public void 置小刻度颜色(int smallScaleColor) {
		view.setSmallScaleColor(smallScaleColor);
	}

	@Override
	public void 置中刻度颜色(int midScaleColor) {
		view.setMidScaleColor(midScaleColor);
	}

	@Override
	public void 置大刻度颜色(int largeScaleColor) {
		view.setLargeScaleColor(largeScaleColor);
	}

	@Override
	public void 置刻度数值颜色(int scaleNumColor) {
		view.setScaleNumColor(scaleNumColor);
	}

	@Override
	public void 置结果数值颜色(int resultNumColor) {
		view.setResultNumColor(resultNumColor);
	}

	@Override
	public void 置单位(String unit) {
		view.setUnit(unit);
	}

	@Override
	public void 置单位颜色(int unitColor) {
		view.setUnitColor(unitColor);
	}

	@Override
	public void 置小刻度描边(int smallScaleStroke) {
		view.setSmallScaleStroke(smallScaleStroke);
	}

	@Override
	public void 置中刻度描边(int midScaleStroke) {
		view.setMidScaleStroke(midScaleStroke);
	}

	@Override
	public void 置大刻度描边(int largeScaleStroke) {
		view.setLargeScaleStroke(largeScaleStroke);
	}

	@Override
	public void 置结果数值文本尺寸(int resultNumTextSize) {
		view.setResultNumTextSize(resultNumTextSize);
	}

	@Override
	public void 置刻度数值文本尺寸(int scaleNumTextSize) {
		view.setScaleNumTextSize(scaleNumTextSize);
	}

	@Override
	public void 置单位文本尺寸(int unitTextSize) {
		view.setUnitTextSize(unitTextSize);
	}

	@Override
	public void 置显示刻度结果(boolean showScaleResult) {
		view.setShowScaleResult(showScaleResult);
	}

	@Override
	public void 置是否背景圆角矩形(boolean bgRoundRect) {
		view.setIsBgRoundRect(bgRoundRect);
	}

	@Override
	public void 置刻度限制(int scaleLimit) {
		view.setScaleLimit(scaleLimit);
	}

	@Override
	public void 置当前值(float value) {
		view.setCurrentValue(value);
	}

	@Override
	public float 取当前值() {
		return view.getCurrentValue();
	}

	@Override
	public String 取选择值() {
		return view.getSelectValue();
	}

	@Override
	public void 选择改变(String text) {
		EventDispatcher.dispatchEvent(this, "选择改变", text);
	}

	@Override
	public void 选择完成(String text) {
		EventDispatcher.dispatchEvent(this, "选择完成", text);
	}
}
