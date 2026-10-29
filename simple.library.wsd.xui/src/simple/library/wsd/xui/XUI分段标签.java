package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleDataElement;
import simple.runtime.annotations.SimpleEvent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.components.可视组件;

/**
 * XUI分段标签组件。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI分段标签 extends XUI视图组件 {

	/**
	 * 设置标签文本尺寸。
	 *
	 * @param tabTextSize 标签文本尺寸。
	 */
	@SimpleFunction
	void 置标签文本尺寸(int tabTextSize);

	/**
	 * 清空已经存在的 Tab。 一般先调用本方法清空已加上的 Tab, 然后重新 {。
	 */
	@SimpleFunction
	void 重置();

	/**
	 * 设置项目间距入场滚动模式。
	 *
	 * @param itemSpaceInScrollMode 项目间距入场滚动模式。
	 */
	@SimpleFunction
	void 置项目间距入场滚动模式(int itemSpaceInScrollMode);

	/**
	 * 设置 indicator的宽度是否随内容宽度变化。
	 *
	 * @param indicatorWidthFollowContent 指示器宽度是否随标签内容变化。
	 */
	@SimpleFunction
	void 置指示宽度调整内容(boolean indicatorWidthFollowContent);

	/**
	 * 设置 indicator 的位置。
	 *
	 * @param isIndicatorTop true 时表示 indicator 位置在 Tab 的上方, false 时表示在下方。
	 */
	@SimpleFunction
	void 置指示位置(boolean isIndicatorTop);

	/**
	 * 设置是否需要显示 indicator。
	 *
	 * @param hasIndicator 是否需要显示 indicator。
	 */
	@SimpleFunction
	void 置含有指示(boolean hasIndicator);

	/**
	 * 取得模式。
	 *
	 * @return 模式。
	 */
	@SimpleFunction
	int 取模式();

	/**
	 * 设置模式。
	 *
	 * @param mode 模式。
	 */
	@SimpleFunction
	void 置模式(int mode);

	/**
	 * 设置 Tab 正常状态下的颜色。
	 *
	 * @param defaultNormalColor 默认正常颜色。
	 */
	@SimpleFunction
	void 置默认正常颜色(int defaultNormalColor);

	/**
	 * 设置 Tab 选中状态下的颜色。
	 *
	 * @param defaultSelectedColor 默认选中颜色。
	 */
	@SimpleFunction
	void 置默认选中颜色(int defaultSelectedColor);

	/**
	 * 设置默认标签图标位置。
	 *
	 * @param defaultTabIconPosition 默认标签图标位置。
	 */
	@SimpleFunction
	void 置默认标签图标位置(int defaultTabIconPosition);

	/**
	 * 取得选中索引。
	 *
	 * @return 选中索引。
	 */
	@SimpleFunction
	int 取选中索引();

	/**
	 * 获取当前的红点数值，如果没有红点则返回 0。
	 *
	 * @param index 索引。
	 * @return 当前的红点数值，如果没有红点则返回 0。
	 */
	@SimpleFunction
	int 取标记数量(int index);

	/**
	 * 替换分段标签标题。
	 *
	 * @param items 项目文本数组；数组内不得含有空引用。
	 */
	@SimpleFunction
	void 置标签数组(String[] items);

	/**
	 * 选择分段标签。
	 *
	 * @param index 从 0 开始的项目索引。
	 */
	@SimpleFunction
	void 选择标签(int index);

	/**
	 * 关联标签与页面切换，保留已有标签标题；未设置标签时使用页面标题。
	 * 错误的组件类型引发非法参数错误。
	 *
	 * @param pager 原生视图为 AndroidX ViewPager 的页面组件。
	 */
	@SimpleFunction
	void 绑定页面框(可视组件 pager);

	/**
	 * 设置指示图像，使用 Simple 图片标识。
	 *
	 * @param image 图片资源标识、资产文件名或外部路径。
	 */
	@SimpleFunction
	void 置指示图像(String image);

	/**
	 * 标签被选择时触发。
	 *
	 * @param index 从 0 开始的项目索引。
	 */
	@SimpleEvent
	void 标签被选择(int index);

	/**
	 * 更新指定标签文字，不重建整个标签组。
	 *
	 * @param index 从零开始的标签位置。
	 * @param text 标签文字。
	 */
	@SimpleFunction
	void 置标签文本(int index, String text);

	/**
	 * 设置指定标签的普通及选中图标。
	 *
	 * @param index 从零开始的标签位置。
	 * @param normal Simple 图片标识。
	 * @param selected Simple 图片标识。
	 */
	@SimpleFunction
	void 置标签图标(int index, String normal, String selected);

	/**
	 * 设置标签徽标数量，0 隐藏。
	 *
	 * @param index 从零开始的标签位置。
	 * @param count 非负数量，0 隐藏。
	 */
	@SimpleFunction
	void 置标签徽标(int index, int count);

	/**
	 * 滚动标签宽度模式。
	 */
	@SimpleDataElement
	int 模式_滚动 = 0;

	/**
	 * 固定标签宽度模式。
	 */
	@SimpleDataElement
	int 模式_固定 = 1;

	/**
	 * 图标位于文字左侧。
	 */
	@SimpleDataElement
	int 图标位置_左 = 0;

	/**
	 * 图标位于文字上侧。
	 */
	@SimpleDataElement
	int 图标位置_上 = 1;

	/**
	 * 图标位于文字右侧。
	 */
	@SimpleDataElement
	int 图标位置_右 = 2;

	/**
	 * 图标位于文字下侧。
	 */
	@SimpleDataElement
	int 图标位置_下 = 3;

	/**
	 * 原选中标签失去选中状态时触发。
	 *
	 * @param index 从 0 开始的标签索引。
	 */
	@SimpleEvent
	void 标签取消选择(int index);

	/**
	 * 用户再次选择当前标签时触发，不表示页面发生变化。
	 *
	 * @param index 从 0 开始的标签索引。
	 */
	@SimpleEvent
	void 标签被重复选择(int index);

	/**
	 * 原生组件识别到标签双击时触发。
	 *
	 * @param index 从 0 开始的标签索引。
	 */
	@SimpleEvent
	void 标签被双击(int index);

}
