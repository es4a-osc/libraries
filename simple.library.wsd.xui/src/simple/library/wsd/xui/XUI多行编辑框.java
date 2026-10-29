package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleEvent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * XUI多行编辑框组件。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI多行编辑框 extends XUI视图组件 {

	/**
	 * 设置填充内容。
	 *
	 * @param content 内容。
	 */
	@SimpleFunction
	void 置内容文本(String content);

	/**
	 * 获取输入的内容。
	 *
	 * @return 输入的内容。
	 */
	@SimpleFunction
	String 取内容文本();

	/**
	 * 设置提示文本。
	 *
	 * @param hintText 提示文本。
	 */
	@SimpleFunction
	void 置提示文本(String hintText);

	/**
	 * 设置内容文本尺寸。
	 *
	 * @param size 尺寸。
	 */
	@SimpleFunction
	void 置内容文本尺寸(int size);

	/**
	 * 设置内容文本颜色。
	 *
	 * @param color 颜色。
	 */
	@SimpleFunction
	void 置内容文本颜色(int color);

	/**
	 * 设置提示文本颜色。
	 *
	 * @param color 颜色。
	 */
	@SimpleFunction
	void 置提示文本颜色(int color);

	/**
	 * 取得提示文本。
	 *
	 * @return 提示文本。
	 */
	@SimpleFunction
	String 取提示文本();

	/**
	 * 设置最大字数。
	 *
	 * @param max_count 允许输入的最大字符数。
	 */
	@SimpleFunction
	void 置最大字数(int max_count);

	/**
	 * 设置忽略中文或英文。
	 *
	 * @param ignoreCnOrEn 忽略中文或英文。
	 */
	@SimpleFunction
	void 置忽略中文或英文(boolean ignoreCnOrEn);

	/**
	 * 设置显示剩余字数。
	 *
	 * @param isShowSurplusNumber 显示剩余字数。
	 */
	@SimpleFunction
	void 置显示剩余字数(boolean isShowSurplusNumber);

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
	 * 内部输入内容改变时触发。
	 *
	 * @param text 文本内容。
	 */
	@SimpleEvent
	void 文本改变(String text);
}
