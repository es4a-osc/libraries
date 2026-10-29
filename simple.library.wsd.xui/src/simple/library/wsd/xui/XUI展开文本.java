package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * XUI展开文本组件。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI展开文本 extends XUI文本组件 {

	/**
	 * 设置裁剪长度。
	 *
	 * @param trimLength 裁剪长度。
	 */
	@SimpleFunction
	void 置裁剪长度(int trimLength);

	/**
	 * 设置颜色可单击文本。
	 *
	 * @param colorClickableText 颜色可单击文本。
	 */
	@SimpleFunction
	void 置颜色可单击文本(int colorClickableText);

	/**
	 * 设置裁剪收起文本。
	 *
	 * @param trimCollapsedText 裁剪收起文本。
	 */
	@SimpleFunction
	void 置裁剪收起文本(String trimCollapsedText);

	/**
	 * 设置裁剪展开文本。
	 *
	 * @param trimExpandedText 裁剪展开文本。
	 */
	@SimpleFunction
	void 置裁剪展开文本(String trimExpandedText);

	/**
	 * 设置裁剪模式。
	 *
	 * @param trimMode 裁剪模式。
	 */
	@SimpleFunction
	void 置裁剪模式(int trimMode);

	/**
	 * 设置裁剪行数。
	 *
	 * @param trimLines 裁剪行数。
	 */
	@SimpleFunction
	void 置裁剪行数(int trimLines);
}
