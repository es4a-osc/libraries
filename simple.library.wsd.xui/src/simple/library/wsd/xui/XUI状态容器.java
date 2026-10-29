package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleEvent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.components.组件容器;

/**
 * XUI状态容器组件。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI状态容器 extends XUI视图组件, 组件容器 {

	/**
	 * 是否动画启用。
	 *
	 * @return 是否动画启用。
	 */
	@SimpleFunction
	boolean 是否动画启用();

	/**
	 * 设置动画启用。
	 *
	 * @param animationEnabled 动画启用。
	 */
	@SimpleFunction
	void 置动画启用(boolean animationEnabled);

	/**
	 * 设置入场动画。
	 *
	 * @param anim 动画。
	 */
	@SimpleFunction
	void 置入场动画(int anim);

	/**
	 * 设置状态切换的出场动画。
	 *
	 * @param anim 出场动画资源 ID。
	 */
	@SimpleFunction
	void 置出场动画(int anim);

	/**
	 * 显示正文布局。
	 */
	@SimpleFunction
	void 显示内容();

	/**
	 * 显示加载中布局。
	 */
	@SimpleFunction
	void 显示加载();

	/**
	 * 显示加载。
	 *
	 * @param resId 资源 ID。
	 */
	@SimpleFunction
	void 显示加载资源(int resId);

	/**
	 * 显示加载。
	 *
	 * @param message 消息。
	 */
	@SimpleFunction
	void 显示加载(String message);

	/**
	 * 显示空内容布局。
	 */
	@SimpleFunction
	void 显示空状态();

	/**
	 * 显示空状态。
	 *
	 * @param resId 资源 ID。
	 */
	@SimpleFunction
	void 显示空状态资源(int resId);

	/**
	 * 显示空状态。
	 *
	 * @param message 消息。
	 */
	@SimpleFunction
	void 显示空状态(String message);

	/**
	 * 设置子组件排列方向。
	 *
	 * @param orientation 0 为水平，1 为垂直。
	 */
	@SimpleFunction
	void 置排列方向(int orientation);

	/**
	 * 显示状态说明和重试按钮。
	 *
	 * @param text 文本内容。
	 */
	@SimpleFunction
	void 显示错误(String text);

	/**
	 * 显示状态说明和重试按钮。
	 *
	 * @param text 文本内容。
	 */
	@SimpleFunction
	void 显示无网络(String text);

	/**
	 * 显示状态说明和重试按钮。
	 *
	 * @param text 文本内容。
	 */
	@SimpleFunction
	void 显示定位关闭(String text);

	/**
	 * 错误、无网络或定位状态下用户点击重试时触发。
	 */
	@SimpleEvent
	void 请求重试();
}
