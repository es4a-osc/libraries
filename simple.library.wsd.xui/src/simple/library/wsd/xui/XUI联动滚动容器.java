package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.components.组件容器;

/**
 * 纵向联动滚动容器；直接子项须为联动组件，普通组件应放入联动线性容器内部。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI联动滚动容器 extends XUI视图组件, 组件容器 {

	/**
	 * 滚动到指定联动子组件的起点。
	 *
	 * @param index 从零开始的直接子组件位置。
	 */
	@SimpleFunction
	void 滚动到子项(int index);

	/**
	 * 滚动到纵向像素位置。
	 *
	 * @param position 非负像素偏移量。
	 */
	@SimpleFunction
	void 滚动到(int position);

	/**
	 * 按指定像素距离滚动，正数向下。
	 *
	 * @param distance 像素距离。
	 */
	@SimpleFunction
	void 滚动距离(int distance);

	/**
	 * 取得当前纵向滚动像素位置。
	 *
	 * @return 当前纵向滚动像素位置。
	 */
	@SimpleFunction
	int 取滚动位置();

}
