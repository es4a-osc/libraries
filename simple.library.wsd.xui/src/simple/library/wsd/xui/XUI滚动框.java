package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleEvent;
import simple.runtime.annotations.SimpleObject;

/**
 * XUI滚动框组件。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI滚动框 extends XUI滚动容器 {

	/**
	 * 滚动位置改变时触发。
	 *
	 * @param left 当前水平偏移，单位为物理像素。
	 * @param top 当前垂直偏移，单位为物理像素。
	 * @param oldLeft 先前水平偏移。
	 * @param oldTop 先前垂直偏移。
	 */
	@SimpleEvent
	void 滚动改变(int left, int top, int oldLeft, int oldTop);
}
