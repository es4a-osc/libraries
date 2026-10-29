package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleEvent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * XUI范围滑块组件。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI范围滑块 extends XUI视图组件 {

	/**
	 * 取得当前选择的下限。
	 *
	 * @return 当前选择的下限。
	 */
	@SimpleFunction
	int 取选中下限();

	/**
	 * 取得当前选择的上限。
	 *
	 * @return 当前选择的上限。
	 */
	@SimpleFunction
	int 取选中上限();

	/**
	 * 设置初始选择范围，上下限须在可选范围内且下限不大于上限。
	 *
	 * @param startingMin 初始选择下限。
	 * @param startingMax 初始选择上限。
	 */
	@SimpleFunction
	void 置初始范围(int startingMin, int startingMax);

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
	 * 重置。
	 */
	@SimpleFunction
	void 重置();

	/**
	 * 任一范围端点改变时触发。
	 *
	 * @param minimum 当前选择的下限。
	 * @param maximum 当前选择的上限。
	 */
	@SimpleEvent
	void 范围改变(int minimum, int maximum);
}
