package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleEvent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * XUI单选标签组件。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI单选标签 extends XUI视图组件 {

	/**
	 * 取得选中。
	 *
	 * @return 选中。
	 */
	@SimpleFunction
	String 取选中();

	/**
	 * 替换标签标题及对应值；数组须等长、标题不得重复，错误输入引发非法参数错误且不修改原数据。
	 *
	 * @param itemArray 显示的项目标题数组。
	 * @param valueArray 与标题对应的项目值数组。
	 */
	@SimpleFunction
	void 置项目数组(String[] itemArray, String[] valueArray);

	/**
	 * 替换标签标题及对应值；数组须等长、标题不得重复，错误输入引发非法参数错误且不修改原数据。
	 *
	 * @param items 项目数组。
	 * @param values 项目值数组。
	 * @param defaultSelection 默认选择。
	 */
	@SimpleFunction
	void 置项目数组(String[] items, String[] values, int defaultSelection);

	/**
	 * 通过值 设置选中的Tab。
	 *
	 * @param value 值。
	 */
	@SimpleFunction
	void 置选择(String value);

	/**
	 * 通过值 设置选中的Tab。
	 *
	 * @param value 值。
	 * @param isSilent 是否静默。
	 */
	@SimpleFunction
	void 置选择(String value, boolean isSilent);

	/**
	 * 通过标题设置选中的Tab。
	 *
	 * @param title 标题。
	 */
	@SimpleFunction
	void 置选择标题(String title);

	/**
	 * 通过标题设置选中的Tab。
	 *
	 * @param title 标题。
	 * @param isSilent 是否静默设置。
	 */
	@SimpleFunction
	void 置选择标题(String title, boolean isSilent);

	/**
	 * 设置颜色。
	 *
	 * @param primaryColor 主颜色。
	 * @param secondaryColor 次要颜色。
	 */
	@SimpleFunction
	void 置颜色(int primaryColor, int secondaryColor);

	/**
	 * 设置颜色。
	 *
	 * @param selectedColor 选中颜色。
	 * @param selectedTextColor 选中文本颜色。
	 * @param unselectedColor 未选中状态的背景颜色。
	 * @param unselectedTextColor 未选中状态的文字颜色。
	 */
	@SimpleFunction
	void 置颜色(int selectedColor, int selectedTextColor, int unselectedColor, int unselectedTextColor);

	/**
	 * 设置是否等宽。
	 *
	 * @param equalWidth 等宽度。
	 */
	@SimpleFunction
	void 置等宽度(boolean equalWidth);

	/**
	 * 设置是否伸展开（match_parent)。
	 *
	 * @param stretch 拉伸。
	 */
	@SimpleFunction
	void 置拉伸(boolean stretch);

	/**
	 * 标签选择改变时触发。
	 *
	 * @param title 显示的标签标题。
	 * @param value 对应项目值。
	 */
	@SimpleEvent
	void 选择改变(String title, String value);
}
