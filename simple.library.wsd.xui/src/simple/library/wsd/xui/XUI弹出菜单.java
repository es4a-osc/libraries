package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleDataElement;
import simple.runtime.annotations.SimpleEvent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.components.可视组件;
import simple.runtime.components.组件;

/**
 * 以当前窗口中的组件为锚点显示普通或分组菜单。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI弹出菜单 extends 组件 {

	/**
	 * 在锚点下方显示列表菜单。
	 *
	 * @param anchor 当前窗口中可见的锚点组件。
	 * @param items 项目文本。
	 * @param images 对应的图标资源标识、资产文件名或文件路径；空数组表示全部无图标，单项空文本表示该项无图标。
	 * 数组非空时，长度须与项目文本一致。
	 * @param width 菜单宽度，单位为物理像素，须大于零。
	 */
	@SimpleFunction
	void 显示列表(可视组件 anchor, String[] items, String[] images, int width);

	/**
	 * 显示可展开的分组菜单。
	 *
	 * @param anchor 锚点组件。
	 * @param groups 分组标题。
	 * @param groupImages 对应的分组图标资源标识、资产文件名或文件路径；空数组表示全部无图标，单项空文本表示该组无图标。
	 * 数组非空时，长度须与分组标题一致。
	 * @param items 平铺的子项文本。
	 * @param images 对应的子项图标，格式与分组图标相同；空数组表示全部无图标。
	 * 数组非空时，长度须与子项文本一致。
	 * @param parents 与子项对应的分组索引。
	 * @param width 菜单宽度，单位为物理像素。
	 */
	@SimpleFunction
	void 显示分组(可视组件 anchor, String[] groups, String[] groupImages, String[] items,
		String[] images, int[] parents, int width);

	/**
	 * 菜单项目被单击。
	 *
	 * @param group 分组索引；普通列表为 -1。
	 * @param index 传入项目数组中的原始索引。
	 * @param text 项目文本。
	 */
	@SimpleEvent
	void 项目被单击(int group, int index, String text);

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
	 * 设置后续菜单的首选方向及像素偏移。
	 *
	 * @param preferred 方向常量。
	 * @param x 水平像素偏移。
	 * @param top 上方显示时的纵向像素偏移。
	 * @param bottom 下方显示时的纵向像素偏移。
	 */
	@SimpleFunction
	void 置弹出位置(int preferred, int x, int top, int bottom);

	/**
	 * 设置后续菜单中禁用的条目，分组菜单按输入项目数组的位置计算。
	 *
	 * @param indices 从零开始的位置数组，空数组全部启用。
	 */
	@SimpleFunction
	void 置禁用项目(int[] indices);

	/**
	 * 菜单因选择、外部点击、返回或主动关闭而消失时触发。
	 */
	@SimpleEvent
	void 已关闭();

	/**
	 * 首选上方显示。
	 */
	@SimpleDataElement
	int 方向_上方 = 0;

	/**
	 * 首选下方显示。
	 */
	@SimpleDataElement
	int 方向_下方 = 1;

	/**
	 * 首选自动显示。
	 */
	@SimpleDataElement
	int 方向_自动 = 2;

	/**
	 * 设置后续列表或分组菜单是否显示分隔线。
	 *
	 * @param show 是否显示分隔线。
	 */
	@SimpleFunction
	void 置分隔线(boolean show);

	/**
	 * 设置后续分组菜单是否只允许一个分组展开。
	 *
	 * @param single 展开新组时是否收起原组。
	 */
	@SimpleFunction
	void 置单组展开(boolean single);

}
