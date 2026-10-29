package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.components.组件容器;

/**
 * 在容器边缘识别返回手势，完成后触发当前 Simple 窗口的返回键事件；子组件放在容器内。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI侧滑返回容器 extends XUI视图组件, 组件容器 {

	/**
	 * 设置响应边缘。
	 *
	 * @param edge 0：左；1：右；2：两侧。
	 */
	@SimpleFunction
	void 置响应边缘(int edge);

	/**
	 * 设置手指横向移动的触发距离。
	 *
	 * @param distance 距离，单位为 dp，须大于零。
	 */
	@SimpleFunction
	void 置触发距离(int distance);
}
