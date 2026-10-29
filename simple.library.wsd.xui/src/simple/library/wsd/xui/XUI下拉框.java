package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleEvent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * XUI下拉框组件。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI下拉框 extends XUI文本组件 {

	/**
	 * 展开。
	 */
	@SimpleFunction
	void 展开();

	/**
	 * 收起。
	 */
	@SimpleFunction
	void 收起();

	/**
	 * 设置箭头颜色。
	 *
	 * @param color 颜色。
	 */
	@SimpleFunction
	void 置箭头颜色(int color);

	/**
	 * 设置下拉最大高度。
	 *
	 * @param height 高度。
	 */
	@SimpleFunction
	void 置下拉最大高度(int height);

	/**
	 * 设置下拉高度。
	 *
	 * @param height 高度。
	 */
	@SimpleFunction
	void 置下拉高度(int height);

	/**
	 * 替换项目数据，按传入顺序显示。
	 *
	 * @param itemsValue 完整项目数组；空数组清空项目。
	 */
	@SimpleFunction
	void 置项目数组(String[] itemsValue);

	/**
	 * 取得项目总数。
	 *
	 * @return 项目总数。
	 */
	@SimpleFunction
	int 取项目数();

	/**
	 * 选择指定项目；越界索引引发索引超出界限错误。
	 *
	 * @param index 从 0 开始的项目索引。
	 */
	@SimpleFunction
	void 选择项目(int index);

	/**
	 * 取得选中索引；没有项目时返回 -1。
	 *
	 * @return 选中索引；没有项目时返回 -1。
	 */
	@SimpleFunction
	int 取选中索引();

	/**
	 * 项目被选择时触发。
	 *
	 * @param index 从 0 开始的项目索引。
	 * @param text 文本内容。
	 */
	@SimpleEvent
	void 项目被选择(int index, String text);
}
