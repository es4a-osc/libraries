package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleEvent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * XUI滚轮组件。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI滚轮 extends XUI视图组件 {

	/**
	 * 设置是否循环滚动。
	 *
	 * @param cyclic 是否循环。
	 */
	@SimpleFunction
	void 置循环(boolean cyclic);

	/**
	 * 设置文字大小。
	 *
	 * @param size 尺寸。
	 */
	@SimpleFunction
	void 置文字大小(float size);

	/**
	 * 设置标签。
	 *
	 * @param label 标签。
	 */
	@SimpleFunction
	void 置标签(String label);

	/**
	 * 设置对齐。
	 *
	 * @param gravity 对齐。
	 */
	@SimpleFunction
	void 置对齐(int gravity);

	/**
	 * 设置是否选项。
	 *
	 * @param options 选项。
	 */
	@SimpleFunction
	void 置是否选项(boolean options);

	/**
	 * 设置文本颜色外侧。
	 *
	 * @param textColorOut 文本颜色外侧。
	 */
	@SimpleFunction
	void 置文本颜色外侧(int textColorOut);

	/**
	 * 设置文本颜色中间。
	 *
	 * @param textColorCenter 文本颜色中间。
	 */
	@SimpleFunction
	void 置文本颜色中间(int textColorCenter);

	/**
	 * 设置文本横向偏移。
	 *
	 * @param textXOffset 文本横向偏移。
	 */
	@SimpleFunction
	void 置文本横向偏移(int textXOffset);

	/**
	 * 设置分隔线颜色。
	 *
	 * @param dividerColor 分隔线颜色。
	 */
	@SimpleFunction
	void 置分隔线颜色(int dividerColor);

	/**
	 * 设置行间距倍数。
	 *
	 * @param lineSpacingMultiplier 行间距倍数。
	 */
	@SimpleFunction
	void 置行间距倍数(float lineSpacingMultiplier);

	/**
	 * 是否循环。
	 *
	 * @return 是否循环。
	 */
	@SimpleFunction
	boolean 是否循环();

	/**
	 * 取得项目高度。
	 *
	 * @return 项目高度。
	 */
	@SimpleFunction
	float 取项目高度();

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
