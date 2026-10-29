package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleEvent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * XUI滑块组件。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI滑块 extends XUI视图组件 {

	/**
	 * 取得选中值。
	 *
	 * @return 选中值。
	 */
	@SimpleFunction
	int 取选中值();

	/**
	 * 设置默认值。
	 *
	 * @param value 可选范围内的默认值。
	 */
	@SimpleFunction
	void 置默认值(int value);

	/**
	 * 取得最小值。
	 *
	 * @return 最小值。
	 */
	@SimpleFunction
	int 取最小值();

	/**
	 * 设置最小值。
	 *
	 * @param min 可选最小值，须小于当前最大值。
	 */
	@SimpleFunction
	void 置最小值(int min);

	/**
	 * 取得最大值。
	 *
	 * @return 最大值。
	 */
	@SimpleFunction
	int 取最大值();

	/**
	 * 设置最大值。
	 *
	 * @param max 可选最大值，须大于当前最小值。
	 */
	@SimpleFunction
	void 置最大值(int max);

	/**
	 * 设置标尺主刻度之间的数值间隔。
	 *
	 * @param rulerInterval 主刻度数值间隔，须不小于 2；不是时间间隔。
	 */
	@SimpleFunction
	void 置刻度间隔(int rulerInterval);

	/**
	 * 重置。
	 */
	@SimpleFunction
	void 重置();

	/**
	 * 滑块数值改变时触发。
	 *
	 * @param value 滑块选中的数值。
	 */
	@SimpleEvent
	void 数值改变(int value);
}
