package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleEvent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * XUI自适应分组列表组件。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI自适应分组列表 extends XUI视图组件 {

	/**
	 * 追加文本分组。
	 *
	 * @param title XUI分组标题。
	 * @param items 项目文本数组；数组内不得含有空引用。
	 */
	@SimpleFunction
	void 添加分组(String title, String[] items);

	/**
	 * 移除所有分组。
	 */
	@SimpleFunction
	void 清空分组();

	/**
	 * 展开指定分组。
	 *
	 * @param index 从 0 开始的项目索引。
	 */
	@SimpleFunction
	void 展开分组(int index);

	/**
	 * 收起指定分组。
	 *
	 * @param index 从 0 开始的项目索引。
	 */
	@SimpleFunction
	void 收起分组(int index);

	/**
	 * 限制内容自适应后的最大高度。
	 *
	 * @param height 允许的最大高度，单位为物理像素，不得为负。
	 */
	@SimpleFunction
	void 置最大高度(int height);

	/**
	 * 分组子项目被单击时触发。
	 *
	 * @param group 分组索引，从 0 开始。
	 * @param index 从 0 开始的项目索引。
	 * @param text 文本内容。
	 */
	@SimpleEvent
	void 项目被单击(int group, int index, String text);

	/**
	 * 取得当前分组数。
	 *
	 * @return 当前数量或索引，选择为空时为 -1。
	 */
	@SimpleFunction
	int 取分组数();

	/**
	 * 取得指定分组的项目数。
	 *
	 * @param group 分组索引。
	 * @return 当前数量或索引，选择为空时为 -1。
	 */
	@SimpleFunction
	int 取项目数(int group);

	/**
	 * 取得指定子项文本。
	 *
	 * @param group 分组索引。
	 * @param index 组内项目索引。
	 * @return 项目文本。
	 */
	@SimpleFunction
	String 取项目文本(int group, int index);

	/**
	 * 更新一项文本，保留选择。
	 *
	 * @param group 分组索引。
	 * @param index 组内项目索引。
	 * @param text 新的文本。
	 */
	@SimpleFunction
	void 置项目文本(int group, int index, String text);

	/**
	 * 替换指定分组的标题和项目，清除该分组的原选择。
	 *
	 * @param group 分组索引。
	 * @param title 标题。
	 * @param items 组内项目数组，可为空数组。
	 */
	@SimpleFunction
	void 更新分组(int group, String title, String[] items);

	/**
	 * 移除分组并调整选择索引，已选分组被移除后恢复未选。
	 *
	 * @param group 分组索引。
	 */
	@SimpleFunction
	void 移除分组(int group);

	/**
	 * 在指定分组末尾添加文本。
	 *
	 * @param group 分组索引。
	 * @param text 项目文本。
	 */
	@SimpleFunction
	void 添加项目(int group, String text);

	/**
	 * 移除子项并调整选择索引，被移除的选择恢复为未选。
	 *
	 * @param group 分组索引。
	 * @param index 组内项目索引。
	 */
	@SimpleFunction
	void 移除项目(int group, int index);

	/**
	 * 高亮指定子项，不触发单击事件；两个索引均为 -1 时清除选择。
	 *
	 * @param group 分组索引。
	 * @param index 组内项目索引。
	 */
	@SimpleFunction
	void 置选中项目(int group, int index);

	/**
	 * 取得已选分组索引，-1 表示未选。
	 *
	 * @return 当前数量或索引，选择为空时为 -1。
	 */
	@SimpleFunction
	int 取选中分组();

	/**
	 * 取得已选子项索引，-1 表示未选。
	 *
	 * @return 当前数量或索引，选择为空时为 -1。
	 */
	@SimpleFunction
	int 取选中项目();

	/**
	 * 展开指定分组并将子项滚动到可见位置。
	 *
	 * @param group 分组索引。
	 * @param index 组内项目索引。
	 * @param smooth 是否平滑滚动。
	 */
	@SimpleFunction
	void 滚动到项目(int group, int index, boolean smooth);
}
