package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * XUI密码输入框组件。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI密码输入框 extends XUI文本组件 {

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
