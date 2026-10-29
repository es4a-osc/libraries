package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.components.组件容器;

/**
 * XUI联动线性容器组件。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI联动线性容器 extends XUI视图组件, 组件容器 {

	/**
	 * 设置子组件排列方向。
	 *
	 * @param orientation 0 为水平，1 为垂直。
	 */
	@SimpleFunction
	void 置排列方向(int orientation);
}
