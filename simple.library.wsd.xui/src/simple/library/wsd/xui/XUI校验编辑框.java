package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleEvent;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * XUI校验编辑框组件。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI校验编辑框 extends XUI文本组件 {

	/**
	 * 添加必填规则；规则按添加顺序执行，显示第一项失败的提示。
	 *
	 * @param error 输入为空时的提示，不可为空。
	 */
	@SimpleFunction
	void 添加必填规则(String error);

	/**
	 * 添加包含上下限的长度规则，按 Java UTF-16 字符单元计数。
	 *
	 * @param minimum 最小长度，不小于零。
	 * @param maximum 最大长度，不小于最小长度。
	 * @param error 长度不符合要求时的提示，不可为空。
	 */
	@SimpleFunction
	void 添加长度规则(int minimum, int maximum, String error);

	/**
	 * 添加整段文本匹配的正则规则；表达式无效时不改变已有规则。
	 *
	 * @param expression Java 正则表达式，是否接受空文本由表达式决定。
	 * @param error 匹配失败时的提示，不可为空。
	 */
	@SimpleFunction
	void 添加正则规则(String expression, String error);

	/**
	 * 清空全部规则及当前错误提示；没有规则时校验通过。
	 */
	@SimpleFunction
	void 清空校验规则();

	/**
	 * 设置扩展单击区域尺寸。
	 *
	 * @param extraClickArea 扩展单击区域。
	 */
	@SimpleFunction
	void 置扩展单击区域尺寸(int extraClickArea);

	/**
	 * 校验输入的合法性。
	 *
	 * @return 校验输入的合法性。
	 */
	@SimpleFunction
	boolean 校验();

	/**
	 * 输入的内容是否有效。
	 *
	 * @return 输入的内容是否有效。
	 */
	@SimpleFunction
	boolean 是否输入有效();

	/**
	 * 设置错误。
	 *
	 * @param error 错误。
	 */
	@SimpleFunction
	void 置错误(String error);

	/**
	 * 取得错误消息。
	 *
	 * @return 错误消息。
	 */
	@SimpleFunction
	String 取错误消息();

	/**
	 * 获取输入的内容。
	 *
	 * @return 输入的内容。
	 */
	@SimpleFunction
	String 取输入值();

	/**
	 * 输入的内容是否为空。
	 *
	 * @return 输入的内容是否为空。
	 */
	@SimpleFunction
	boolean 是否为空();

	/**
	 * 输入的内容是否不为空。
	 *
	 * @return 输入的内容是否不为空。
	 */
	@SimpleFunction
	boolean 是否非空();

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

	/**
	 * 主动或自动校验发现输入无效时触发。
	 *
	 * @param input 发生校验时的完整输入。
	 * @param message 第一个失败规则的提示。
	 */
	@SimpleEvent
	void 校验失败(String input, String message);

}
