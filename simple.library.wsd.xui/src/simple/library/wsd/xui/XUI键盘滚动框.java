package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * XUI键盘滚动框组件。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI键盘滚动框 extends XUI滚动容器 {

	/**
	 * 设置滚动是否隐藏键盘。
	 *
	 * @param isScrollHideKeyboard 是否滚动隐藏键盘。
	 */
	@SimpleFunction
	void 置是否滚动隐藏键盘(boolean isScrollHideKeyboard);
}
