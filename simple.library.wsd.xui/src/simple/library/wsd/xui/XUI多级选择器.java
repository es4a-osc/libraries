package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleEvent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * 显示一至三级联动或独立滚轮；项目索引从零开始。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI多级选择器 extends XUI选择器 {

	/**
	 * 使用父项索引设置联动树；父项须先于子项，根父项为 -1，各分支须具有相同的一至三级深度。
	 *
	 * @param texts 平铺项目文本。
	 * @param parents 与文本对应的父项原始索引。
	 */
	@SimpleFunction
	void 置联动项目(String[] texts, int[] parents);

	/**
	 * 设置不联动的一至三列项目；第一列不可为空，未使用的后续列传空数组。
	 *
	 * @param first 第一列。
	 * @param second 第二列。
	 * @param third 第三列。
	 */
	@SimpleFunction
	void 置独立项目(String[] first, String[] second, String[] third);

	/**
	 * 设置各列初始选项；联动时是当前父项下的相对索引。
	 *
	 * @param first 第一列索引。
	 * @param second 第二列索引，未使用时为零。
	 * @param third 第三列索引，未使用时为零。
	 */
	@SimpleFunction
	void 置默认选项(int first, int second, int third);

	/**
	 * 取得最后确认的对应列文本。
	 *
	 * @param column 列索引 0～2。
	 * @return 选中文本，未使用的列为空文本。
	 */
	@SimpleFunction
	String 取选中文本(int column);

	/**
	 * 在所属窗口上显示选择器。
	 */
	@SimpleFunction
	void 显示();

	/**
	 * 用户提交选项；未使用的列为 -1。
	 *
	 * @param first 第一列相对索引。
	 * @param second 第二列相对索引。
	 * @param third 第三列相对索引。
	 */
	@SimpleEvent
	void 选择已确认(int first, int second, int third);

	/**
	 * 关闭当前显示的界面。
	 */
	@SimpleFunction
	void 关闭();

	/**
	 * 取得当前界面是否正在显示。
	 *
	 * @return 是否显示。
	 */
	@SimpleFunction
	boolean 是否显示();

	/**
	 * 用户滚动选项时触发；不替代确认事件，也不提交取值结果。
	 *
	 * @param first 第一列相对索引。
	 * @param second 第二列相对索引，未使用时为 -1。
	 * @param third 第三列相对索引，未使用时为 -1。
	 */
	@SimpleEvent
	void 选择已改变(int first, int second, int third);

	/**
	 * 设置三列单位文字，空文本隐藏对应单位。
	 *
	 * @param first 第一列单位。
	 * @param second 第二列单位。
	 * @param third 第三列单位。
	 */
	@SimpleFunction
	void 置列标签(String first, String second, String third);

	/**
	 * 分别设置三列是否循环滚动。
	 *
	 * @param first 是否循环。
	 * @param second 是否循环。
	 * @param third 是否循环。
	 */
	@SimpleFunction
	void 置循环(boolean first, boolean second, boolean third);

	/**
	 * 设置切换上级时是否将下级恢复首项，下次显示时生效。
	 *
	 * @param reset 真为恢复首项，假为尽量保留原索引并约束到有效范围。
	 */
	@SimpleFunction
	void 置下级重置(boolean reset);

}
