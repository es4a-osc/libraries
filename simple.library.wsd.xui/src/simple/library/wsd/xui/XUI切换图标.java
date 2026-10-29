package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * XUI切换图标组件。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI切换图标 extends XUI视图组件 {

	/**
	 * 设置图标启用。
	 *
	 * @param enabled 启用。
	 */
	@SimpleFunction
	void 置图标启用(boolean enabled);

	/**
	 * 设置图标启用。
	 *
	 * @param enabled 启用。
	 * @param animate 动画。
	 */
	@SimpleFunction
	void 置图标启用(boolean enabled, boolean animate);

	/**
	 * 是否图标启用。
	 *
	 * @return 是否图标启用。
	 */
	@SimpleFunction
	boolean 是否图标启用();

	/**
	 * 切换状态。
	 */
	@SimpleFunction
	void 切换状态();

	/**
	 * 切换状态。
	 *
	 * @param animate 动画。
	 */
	@SimpleFunction
	void 切换状态(boolean animate);
}
