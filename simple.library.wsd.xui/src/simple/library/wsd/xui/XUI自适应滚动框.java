package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * XUI自适应滚动框组件。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI自适应滚动框 extends XUI滚动容器 {

	/**
	 * 限制内容自适应后的最大高度。
	 *
	 * @param height 允许的最大高度，单位为物理像素，不得为负。
	 */
	@SimpleFunction
	void 置最大高度(int height);
}
