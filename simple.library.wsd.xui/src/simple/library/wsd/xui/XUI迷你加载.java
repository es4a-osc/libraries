package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * XUI迷你加载组件。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI迷你加载 extends XUI视图组件 {

	/**
	 * 设置颜色。
	 *
	 * @param color 颜色。
	 */
	@SimpleFunction
	void 置颜色(int color);

	/**
	 * 设置尺寸。
	 *
	 * @param size 尺寸。
	 */
	@SimpleFunction
	void 置尺寸(int size);

	/**
	 * 开始。
	 */
	@SimpleFunction
	void 开始();

	/**
	 * 停止。
	 */
	@SimpleFunction
	void 停止();
}
