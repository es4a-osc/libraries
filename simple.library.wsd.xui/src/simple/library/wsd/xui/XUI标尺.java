package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleEvent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * XUI标尺组件。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI标尺 extends XUI视图组件 {

	/**
	 * 设置标尺高度。
	 *
	 * @param rulerHeight 标尺高度。
	 */
	@SimpleFunction
	void 置标尺高度(int rulerHeight);

	/**
	 * 设置标尺到结果间隔。
	 *
	 * @param rulerToResultGap 标尺到结果间隔。
	 */
	@SimpleFunction
	void 置标尺到结果间隔(int rulerToResultGap);

	/**
	 * 设置缩放数量。
	 *
	 * @param scaleCount 缩放数量。
	 */
	@SimpleFunction
	void 置刻度数量(int scaleCount);

	/**
	 * 设置缩放间隔。
	 *
	 * @param scaleGap 缩放间隔。
	 */
	@SimpleFunction
	void 置刻度间隔(int scaleGap);

	/**
	 * 设置最小缩放。
	 *
	 * @param minScale 最小缩放。
	 */
	@SimpleFunction
	void 置最小刻度(int minScale);

	/**
	 * 设置首个缩放。
	 *
	 * @param firstScale 首个缩放。
	 */
	@SimpleFunction
	void 置首个刻度(float firstScale);

	/**
	 * 设置最大缩放。
	 *
	 * @param maxScale 最大缩放。
	 */
	@SimpleFunction
	void 置最大刻度(int maxScale);

	/**
	 * 设置背景颜色。
	 *
	 * @param bgColor 背景颜色。
	 */
	@SimpleFunction
	void 置背景颜色(int bgColor);

	/**
	 * 设置小缩放颜色。
	 *
	 * @param smallScaleColor 小缩放颜色。
	 */
	@SimpleFunction
	void 置小刻度颜色(int smallScaleColor);

	/**
	 * 设置中缩放颜色。
	 *
	 * @param midScaleColor 中缩放颜色。
	 */
	@SimpleFunction
	void 置中刻度颜色(int midScaleColor);

	/**
	 * 设置大缩放颜色。
	 *
	 * @param largeScaleColor 大缩放颜色。
	 */
	@SimpleFunction
	void 置大刻度颜色(int largeScaleColor);

	/**
	 * 设置缩放数值颜色。
	 *
	 * @param scaleNumColor 缩放数值颜色。
	 */
	@SimpleFunction
	void 置刻度数值颜色(int scaleNumColor);

	/**
	 * 设置结果数值颜色。
	 *
	 * @param resultNumColor 结果数值颜色。
	 */
	@SimpleFunction
	void 置结果数值颜色(int resultNumColor);

	/**
	 * 设置单位。
	 *
	 * @param unit 单位。
	 */
	@SimpleFunction
	void 置单位(String unit);

	/**
	 * 设置单位颜色。
	 *
	 * @param unitColor 单位颜色。
	 */
	@SimpleFunction
	void 置单位颜色(int unitColor);

	/**
	 * 设置小缩放描边。
	 *
	 * @param smallScaleStroke 小缩放描边。
	 */
	@SimpleFunction
	void 置小刻度描边(int smallScaleStroke);

	/**
	 * 设置中缩放描边。
	 *
	 * @param midScaleStroke 中缩放描边。
	 */
	@SimpleFunction
	void 置中刻度描边(int midScaleStroke);

	/**
	 * 设置大缩放描边。
	 *
	 * @param largeScaleStroke 大缩放描边。
	 */
	@SimpleFunction
	void 置大刻度描边(int largeScaleStroke);

	/**
	 * 设置结果数值文本尺寸。
	 *
	 * @param resultNumTextSize 结果数值文本尺寸。
	 */
	@SimpleFunction
	void 置结果数值文本尺寸(int resultNumTextSize);

	/**
	 * 设置缩放数值文本尺寸。
	 *
	 * @param scaleNumTextSize 缩放数值文本尺寸。
	 */
	@SimpleFunction
	void 置刻度数值文本尺寸(int scaleNumTextSize);

	/**
	 * 设置单位文本尺寸。
	 *
	 * @param unitTextSize 单位文本尺寸。
	 */
	@SimpleFunction
	void 置单位文本尺寸(int unitTextSize);

	/**
	 * 设置显示缩放结果。
	 *
	 * @param showScaleResult 显示缩放结果。
	 */
	@SimpleFunction
	void 置显示刻度结果(boolean showScaleResult);

	/**
	 * 设置是否背景圆角矩形。
	 *
	 * @param bgRoundRect 背景圆角矩形。
	 */
	@SimpleFunction
	void 置是否背景圆角矩形(boolean bgRoundRect);

	/**
	 * 设置缩放限制。
	 *
	 * @param scaleLimit 缩放限制。
	 */
	@SimpleFunction
	void 置刻度限制(int scaleLimit);

	/**
	 * 设置刻度尺的值。
	 *
	 * @param value 值。
	 */
	@SimpleFunction
	void 置当前值(float value);

	/**
	 * 获取当前刻度尺的刻度。
	 *
	 * @return 当前刻度尺的刻度。
	 */
	@SimpleFunction
	float 取当前值();

	/**
	 * 获取选择值。
	 *
	 * @return 选择值。
	 */
	@SimpleFunction
	String 取选择值();

	/**
	 * 标尺滑动过程中返回当前刻度文本。
	 *
	 * @param text 文本内容。
	 */
	@SimpleEvent
	void 选择改变(String text);

	/**
	 * 标尺滑动停止时返回选中刻度文本。
	 *
	 * @param text 文本内容。
	 */
	@SimpleEvent
	void 选择完成(String text);
}
