package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleDataElement;
import simple.runtime.annotations.SimpleEvent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * XUI流式标签组件。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI流式标签 extends XUI视图组件 {

	/**
	 * 设置单选模式下点击是否可取消选中。
	 *
	 * @param singleCancelable 单选允许取消。
	 */
	@SimpleFunction
	void 置单选允许取消(boolean singleCancelable);

	/**
	 * 清除标签数据。
	 */
	@SimpleFunction
	void 清空标签();

	/**
	 * 清除标签选中状态。
	 */
	@SimpleFunction
	void 清空选择();

	/**
	 * 子View个数。
	 *
	 * @param width 宽度。
	 */
	@SimpleFunction
	void 置子项宽度(int width);

	/**
	 * 设置初始化选中的标签索引。
	 *
	 * @param ps 从 0 开始的选中项目索引数组。
	 */
	@SimpleFunction
	void 置选中位置数组(int[] ps);

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
	 * 取得全部选中索引。
	 *
	 * @return 全部选中索引。
	 */
	@SimpleFunction
	int[] 取选中索引数组();

	/**
	 * 项目被选择时触发。
	 *
	 * @param index 从 0 开始的项目索引。
	 * @param text 文本内容。
	 */
	@SimpleEvent
	void 项目被选择(int index, String text);

	/**
	 * 标签选择集合改变时触发。
	 *
	 * @param indices 全部选中索引，从 0 开始。
	 */
	@SimpleEvent
	void 选择改变(int[] indices);

	/**
	 * 切换选择模式并清除原选择；0 不选择，1 单选，2 多选。
	 *
	 * @param mode 选择模式常量。
	 */
	@SimpleFunction
	void 置选择模式(int mode);

	/**
	 * 取得当前选择模式。
	 *
	 * @return 当前选择模式。
	 */
	@SimpleFunction
	int 取选择模式();

	/**
	 * 追加标签，保留原选择。
	 *
	 * @param text 标签文字，不得为空引用。
	 */
	@SimpleFunction
	void 添加标签(String text);

	/**
	 * 删除指定标签，删除项清除选择，其后选择索引前移。
	 *
	 * @param index 从零开始的标签索引。
	 */
	@SimpleFunction
	void 移除标签(int index);

	/**
	 * 取得指定标签文字。
	 *
	 * @param index 从零开始的标签索引。
	 * @return 指定标签文字。
	 */
	@SimpleFunction
	String 取标签文本(int index);

	/**
	 * 选择模式，不选择。
	 */
	@SimpleDataElement
	int 选择模式_不选择 = 0;

	/**
	 * 选择模式，单选。
	 */
	@SimpleDataElement
	int 选择模式_单选 = 1;

	/**
	 * 选择模式，多选。
	 */
	@SimpleDataElement
	int 选择模式_多选 = 2;

}
