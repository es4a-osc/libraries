package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * XUI密码编辑框组件。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI密码编辑框 extends XUI文本组件 {

	/**
	 * 设置扩展单击区域尺寸。
	 *
	 * @param extraClickArea 扩展单击区域。
	 */
	@SimpleFunction
	void 置扩展单击区域尺寸(int extraClickArea);

	/**
	 * 设置密码输入框的样式。
	 *
	 * @param isAsteriskStyle 是否星号样式。
	 */
	@SimpleFunction
	void 置是否星号样式(boolean isAsteriskStyle);

	/**
	 * 设置错误。
	 *
	 * @param error 错误。
	 */
	@SimpleFunction
	void 置错误(String error);

	/**
	 * 设置输入提示文本。
	 *
	 * @param text 文本内容。
	 */
	@SimpleFunction
	void 置提示文本(String text);

	/**
	 * 取得当前输入内容。
	 *
	 * @return 当前输入内容。
	 */
	@SimpleFunction
	String 取输入文本();

	/**
	 * 设置输入类型。
	 *
	 * @param type Android InputType 标志组合。
	 */
	@SimpleFunction
	void 置输入类型(int type);

	/**
	 * 设置光标位置，允许位于文本末尾。
	 *
	 * @param index 从 0 开始的项目索引。
	 */
	@SimpleFunction
	void 置光标位置(int index);
}
