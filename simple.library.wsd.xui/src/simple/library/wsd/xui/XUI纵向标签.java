package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleDataElement;
import simple.runtime.annotations.SimpleEvent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.components.可视组件;

/**
 * XUI纵向标签组件。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI纵向标签 extends XUI视图组件 {

	/**
	 * 清空标签。
	 */
	@SimpleFunction
	void 清空标签();

	/**
	 * 取得标签数量。
	 *
	 * @return 标签数量。
	 */
	@SimpleFunction
	int 取标签数量();

	/**
	 * 取得选中标签位置。
	 *
	 * @return 选中标签位置。
	 */
	@SimpleFunction
	int 取选中标签位置();

	/**
	 * 设置标签徽标。
	 *
	 * @param tabPosition 标签位置。
	 * @param badgeNum 徽标数值。
	 */
	@SimpleFunction
	void 置标签徽标数(int tabPosition, int badgeNum);

	/**
	 * 设置标签徽标。
	 *
	 * @param tabPosition 标签位置。
	 * @param badgeText 徽标文本。
	 */
	@SimpleFunction
	void 置标签徽标文本(int tabPosition, String badgeText);

	/**
	 * 设置标签模式。
	 *
	 * @param mode 模式。
	 */
	@SimpleFunction
	void 置标签模式(int mode);

	/**
	 * 设置标签外边距。
	 *
	 * @param margin 外边距。
	 */
	@SimpleFunction
	void 置标签外边距(int margin);

	/**
	 * 设置标签高度。
	 *
	 * @param height 高度。
	 */
	@SimpleFunction
	void 置标签高度(int height);

	/**
	 * 设置指示颜色。
	 *
	 * @param color 颜色。
	 */
	@SimpleFunction
	void 置指示颜色(int color);

	/**
	 * 设置指示宽度。
	 *
	 * @param width 宽度。
	 */
	@SimpleFunction
	void 置指示宽度(int width);

	/**
	 * 设置指示圆角。
	 *
	 * @param corners 圆角。
	 */
	@SimpleFunction
	void 置指示圆角(int corners);

	/**
	 * 设置指示对齐。
	 *
	 * @param gravity 对齐。
	 */
	@SimpleFunction
	void 置指示对齐(int gravity);

	/**
	 * 替换纵向标签标题。
	 *
	 * @param items 项目文本数组；数组内不得含有空引用。
	 */
	@SimpleFunction
	void 置标签数组(String[] items);

	/**
	 * 选择纵向标签。
	 *
	 * @param index 从 0 开始的项目索引。
	 */
	@SimpleFunction
	void 选择标签(int index);

	/**
	 * 把标签切换与现有页面框关联；错误的组件类型引发非法参数错误。
	 *
	 * @param pager 原生视图为 AndroidX ViewPager 的页面组件。
	 */
	@SimpleFunction
	void 绑定页面框(可视组件 pager);

	/**
	 * 标签被选择时触发。
	 *
	 * @param index 从 0 开始的项目索引。
	 */
	@SimpleEvent
	void 标签被选择(int index);

	/**
	 * 更新指定纵向标签文字。
	 *
	 * @param index 从零开始的标签位置。
	 * @param text 标签文字。
	 */
	@SimpleFunction
	void 置标签文本(int index, String text);

	/**
	 * 设置纵向标签的普通、选中图标及尺寸。
	 *
	 * @param index 从零开始的位置。
	 * @param normal Android 图片资源标识。
	 * @param selected Android 图片资源标识。
	 * @param width 正数像素。
	 * @param height 正数像素。
	 * @param gravity Android Gravity 方位。
	 */
	@SimpleFunction
	void 置标签图标资源(int index, int normal, int selected, int width, int height, int gravity);

	/**
	 * 固定纵向标签模式。
	 */
	@SimpleDataElement
	int 模式_固定 = 10;

	/**
	 * 滚动纵向标签模式。
	 */
	@SimpleDataElement
	int 模式_滚动 = 11;

}
