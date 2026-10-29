package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleDataElement;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.components.可视组件;

/**
 * XUI贝塞尔指示器组件。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI贝塞尔指示器 extends XUI视图组件 {

	/**
	 * 设置已绑定页面框的切换进度；零值恢复当前页的初始指示图形。
	 *
	 * @param progress 0 至 1 的切换进度。
	 */
	@SimpleFunction
	void 置进度(float progress);

	/**
	 * 跟随页面框显示贝塞尔指示器。
	 *
	 * @param pager AndroidX 页面框组件。
	 */
	@SimpleFunction
	void 绑定页面框(可视组件 pager);

	/**
	 * 设置指示图形滚动方向。
	 *
	 * @param direction 方向常量。
	 */
	@SimpleFunction
	void 置方向(int direction);

	/**
	 * 将已绑定页面框的运动进度和图形恢复为当前页的初始状态。
	 */
	@SimpleFunction
	void 重置进度();

	/**
	 * 向左运动。
	 */
	@SimpleDataElement
	int 方向_左 = 1;

	/**
	 * 向右运动。
	 */
	@SimpleDataElement
	int 方向_右 = 2;

}
