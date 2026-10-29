package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * XUI清除编辑框组件。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI清除编辑框 extends XUI文本组件 {

	/**
	 * 设置扩展单击区域尺寸。
	 *
	 * @param extraClickArea 扩展单击区域。
	 */
	@SimpleFunction
	void 置扩展单击区域尺寸(int extraClickArea);

	/**
	 * 设置晃动动画。
	 */
	@SimpleFunction
	void 抖动();

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
