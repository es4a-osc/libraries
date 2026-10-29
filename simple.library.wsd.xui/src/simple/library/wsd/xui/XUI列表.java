package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleEvent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * 一维列表共用的数据、选择及滚动接口，索引均从零开始。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public interface XUI列表 extends XUI视图组件 {

	/**
	 * 替换全部文本项目并清除选择，输入数组会独立复制。
	 *
	 * @param items 项目文本数组，可为空数组。
	 */
	@SimpleFunction
	void 置项目数组(String[] items);

	/**
	 * 取得当前项目数。
	 *
	 * @return 当前项目数。
	 */
	@SimpleFunction
	int 取项目数();

	/**
	 * 取得指定项目的主文本。
	 *
	 * @param index 从零开始的项目索引。
	 * @return 项目文本。
	 */
	@SimpleFunction
	String 取项目文本(int index);

	/**
	 * 更新主文本，保留该项说明、图片和选中状态。
	 *
	 * @param index 项目索引。
	 * @param text 新的主文本。
	 */
	@SimpleFunction
	void 置项目文本(int index, String text);

	/**
	 * 在末尾添加一个文本项目。
	 *
	 * @param text 主文本。
	 */
	@SimpleFunction
	void 添加项目(String text);

	/**
	 * 移除指定项目，自动调整选择索引；移除已选项目后恢复未选。
	 *
	 * @param index 项目索引。
	 */
	@SimpleFunction
	void 移除项目(int index);

	/**
	 * 清空全部项目和选择。
	 */
	@SimpleFunction
	void 清空项目();

	/**
	 * 设置高亮选择，不触发单击事件，也不改变滚动位置。
	 *
	 * @param index 项目索引，-1 清除选择。
	 */
	@SimpleFunction
	void 置选中项目(int index);

	/**
	 * 取得当前选择索引，-1 表示未选。
	 *
	 * @return 当前数量或索引，选择为空时为 -1。
	 */
	@SimpleFunction
	int 取选中项目();

	/**
	 * 将指定项目滚动到可见位置。
	 *
	 * @param index 项目索引。
	 * @param smooth 是否平滑滚动。
	 */
	@SimpleFunction
	void 滚动到项目(int index, boolean smooth);

	/**
	 * 用户单击项目，先更新选择状态再报告事件。
	 *
	 * @param index 项目索引。
	 * @param text 项目主文本。
	 */
	@SimpleEvent
	void 项目被单击(int index, String text);
}
