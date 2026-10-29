package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleEvent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.components.组件容器;

/**
 * XUI分组列表组件。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI分组列表 extends XUI视图组件, 组件容器 {

	/**
	 * 取得分隔样式。
	 *
	 * @return 分隔样式。
	 */
	@SimpleFunction
	int 取分隔样式();

	/**
	 * 设置分组列表的原生分隔线样式。
	 *
	 * @param separatorStyle 分隔样式。
	 */
	@SimpleFunction
	void 置分隔样式(int separatorStyle);

	/**
	 * 取得分组数量。
	 *
	 * @return 分组数量。
	 */
	@SimpleFunction
	int 取分组数量();

	/**
	 * 设置子组件排列方向。
	 *
	 * @param orientation 0 为水平，1 为垂直。
	 */
	@SimpleFunction
	void 置排列方向(int orientation);

	/**
	 * 追加带标题的文本分组。
	 *
	 * @param title XUI分组标题。
	 * @param items 项目文本数组；数组内不得含有空引用。
	 */
	@SimpleFunction
	void 添加分组(String title, String[] items);

	/**
	 * 删除指定分组，后续分组及其事件索引同步前移。
	 *
	 * @param index 从 0 开始的项目索引。
	 */
	@SimpleFunction
	void 删除分组(int index);

	/**
	 * 移除所有由添加分组建立的标题及行，保留直接加入容器的其它 Simple 子组件。
	 */
	@SimpleFunction
	void 清空分组();

	/**
	 * 分组项目被单击时触发。
	 *
	 * @param section 分组索引，从 0 开始。
	 * @param index 从 0 开始的项目索引。
	 * @param text 文本内容。
	 */
	@SimpleEvent
	void 项目被单击(int section, int index, String text);

	/**
	 * 创建一个可加入已配置列表项的空分组并返回索引。
	 *
	 * @param title 分组标题，可为空文本。
	 * @param description 分组底部说明，可为空文本。
	 * @return 创建一个可加入已配置列表项的空分组并返回索引。
	 */
	@SimpleFunction
	int 创建分组(String title, String description);

	/**
	 * 暂借同一窗口内已配置的分组列表项；删除项目、清空或销毁后归还。
	 *
	 * @param section 从 0 开始的分组索引。
	 * @param component 尚未加入其它分组的列表项。
	 */
	@SimpleFunction
	void 添加分组项目(int section, XUI分组列表项 component);

	/**
	 * 移除指定项目；暂借的组件恢复原父容器、顺序和布局参数。
	 *
	 * @param section 从 0 开始的分组索引。
	 * @param index 从 0 开始的项目索引。
	 */
	@SimpleFunction
	void 移除分组项目(int section, int index);

	/**
	 * 设置指定分组的底部说明，空文本隐藏说明。
	 *
	 * @param section 从 0 开始的分组索引。
	 * @param description 新的底部说明。
	 */
	@SimpleFunction
	void 置分组说明(int section, String description);

	/**
	 * 分组项目被长按时触发。
	 *
	 * @param section 从 0 开始的分组索引。
	 * @param index 从 0 开始的项目索引。
	 * @param text 当前项目文字。
	 */
	@SimpleEvent
	void 项目被长按(int section, int index, String text);

}
