package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleEvent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.components.组件;

/**
 * 依附当前窗口的底部列表或图标网格菜单。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI底部菜单 extends 组件 {

	/**
	 * 显示底部列表菜单。
	 *
	 * @param title 标题。
	 * @param items 项目文本。
	 * @param checked 初始勾选项；-1 表示不显示勾选。
	 */
	@SimpleFunction
	void 显示列表(String title, String[] items, int checked);

	/**
	 * 显示带图标、红点和启用状态的底部列表，数组独立复制。
	 *
	 * @param title 标题。
	 * @param items 非空项目数组。
	 * @param images 与项目对应的本地图片标识，空字符串表示无图标。
	 * @param redPoints 与项目对应的红点状态。
	 * @param enabled 与项目对应的启用状态；禁用项不能选择。
	 * @param checked 初始勾选索引，-1 表示不显示勾选。
	 */
	@SimpleFunction
	void 显示列表(String title, String[] items, String[] images, boolean[] redPoints, boolean[] enabled, int checked);

	/**
	 * 配置下次显示时允许返回键、外部点击取消的方式，不影响菜单项目选择。
	 *
	 * @param back 是否允许返回键取消。
	 * @param outside 是否允许点击菜单外部取消。
	 */
	@SimpleFunction
	void 置取消方式(boolean back, boolean outside);

	/**
	 * 用户通过取消按钮、返回键或菜单外部取消时触发，随后触发已关闭。
	 */
	@SimpleEvent
	void 已取消();

	/**
	 * 菜单因选择、取消或主动关闭退出后触发一次。
	 */
	@SimpleEvent
	void 已关闭();

	/**
	 * 显示底部图标网格菜单。
	 *
	 * @param items 项目文本。
	 * @param images 与项目对应的本地图片标识。
	 * @param firstLineCount 第一行数量，剩余项目放在第二行。
	 */
	@SimpleFunction
	void 显示网格(String[] items, String[] images, int firstLineCount);

	/**
	 * 菜单条目被单击并关闭。
	 *
	 * @param index 从零开始的项目索引。
	 * @param text 项目文本。
	 */
	@SimpleEvent
	void 项目被单击(int index, String text);

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
	 * 显示带图标、红点和启用状态的网格菜单。
	 *
	 * @param items 项目文字数组。
	 * @param images 与项目一一对应的图片标识数组。
	 * @param redPoints 与项目一一对应的红点状态。
	 * @param enabled 与项目一一对应的启用状态。
	 * @param firstLineCount 首行项目数量。
	 */
	@SimpleFunction
	void 显示网格(String[] items, String[] images, boolean[] redPoints, boolean[] enabled, int firstLineCount);

	/**
	 * 设置后续底部列表项目是否居中显示。
	 *
	 * @param center 是否居中。
	 */
	@SimpleFunction
	void 置列表居中(boolean center);

	/**
	 * 设置后续网格菜单底部取消按钮文字，空文本隐藏该按钮。
	 *
	 * @param text 按钮文字。
	 */
	@SimpleFunction
	void 置底部按钮文本(String text);

}
