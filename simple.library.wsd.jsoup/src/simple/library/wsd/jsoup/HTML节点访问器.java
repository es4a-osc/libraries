package simple.library.wsd.jsoup;

import simple.runtime.annotations.SimpleDataElement;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * 节点遍历回调；进入和离开各回调一次，跳过和移除指令可改变后续回调。回调不要直接修改树结构。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public interface HTML节点访问器 {

	/**
	 * 继续遍历。
	 */
	@SimpleDataElement
	public static final int 继续 = 0;

	/**
	 * 进入时跳过子节点，仍触发本节点离开。
	 */
	@SimpleDataElement
	public static final int 跳过子节点 = 1;

	/**
	 * 进入时跳过子节点及本节点离开。
	 */
	@SimpleDataElement
	public static final int 跳过整节点 = 2;

	/**
	 * 移除此节点和子树，不能用于本次遍历根。
	 */
	@SimpleDataElement
	public static final int 移除节点 = 3;

	/**
	 * 立即停止，遍历返回假。
	 */
	@SimpleDataElement
	public static final int 停止 = 4;

	/**
	 * 处理节点，返回本接口的指令常量；离开阶段的两种跳过指令等同继续。
	 *
	 * @param node    当前节点视图。
	 * @param depth   相对遍历根的深度，从零开始。
	 * @param leaving 真表示离开，假表示进入。
	 * @return 遍历指令常量。
	 */
	@SimpleFunction
	public int 访问(HTML节点 node, int depth, boolean leaving);
}
