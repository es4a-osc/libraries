package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.components.可视组件;

/**
 * XUI下拉菜单组件。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI下拉菜单 extends XUI视图组件 {

	/**
	 * 设置当前菜单标签的文字。
	 *
	 * @param text 文本。
	 */
	@SimpleFunction
	void 置标签菜单文本(String text);

	/**
	 * 设置菜单标签是否接受单击。
	 *
	 * @param clickable 可单击。
	 */
	@SimpleFunction
	void 置标签菜单可单击(boolean clickable);

	/**
	 * 下拉菜单是否处于展开状态。
	 *
	 * @return 下拉菜单是否处于展开状态。
	 */
	@SimpleFunction
	boolean 是否显示中();

	/**
	 * 收起当前展开的菜单，保留标签和正文；未展开时不执行操作。
	 */
	@SimpleFunction
	void 关闭菜单();

	/**
	 * 设置下拉菜单及正文；组件从原父容器移出，数量不符、重复或祖先组件会被拒绝。
	 *
	 * @param titles 菜单标题，数量须与菜单内容一致。
	 * @param menus 每个下拉菜单的内容组件。
	 * @param content 菜单下方的正文组件。
	 */
	@SimpleFunction
	void 置菜单(String[] titles, 可视组件[] menus, 可视组件 content);
}
