package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleDataElement;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * XUI基础文本组件。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI基础文本 extends XUI视图组件 {

	/**
	 * 设置顶部文本。
	 *
	 * @param s 显示的文本。
	 */
	@SimpleFunction
	void 置顶部文本(String s);

	/**
	 * 设置中间文本。
	 *
	 * @param s 显示的文本。
	 */
	@SimpleFunction
	void 置中间文本(String s);

	/**
	 * 设置底部文本。
	 *
	 * @param s 显示的文本。
	 */
	@SimpleFunction
	void 置底部文本(String s);

	/**
	 * 设置最大字宽。
	 *
	 * @param topMaxEms 顶部最大字宽。
	 * @param centerMaxEms 中间最大字宽。
	 * @param bottomMaxEms 底部最大字宽。
	 */
	@SimpleFunction
	void 置最大字宽(int topMaxEms, int centerMaxEms, int bottomMaxEms);

	/**
	 * 设置中间间距高度。
	 *
	 * @param centerSpaceHeight 中间间距高度。
	 */
	@SimpleFunction
	void 置中间间距高度(int centerSpaceHeight);

	/**
	 * 设置指定文字区域的资产字体与 SP 字号。
	 *
	 * @param region 区域常量。
	 * @param asset 字体资产路径，空文本使用系统字体。
	 * @param size 大于零的 SP 字号。
	 */
	@SimpleFunction
	void 置区域字体(int region, String asset, float size);

	/**
	 * 顶部文字区域。
	 */
	@SimpleDataElement
	int 区域_顶部 = 0;

	/**
	 * 中间文字区域。
	 */
	@SimpleDataElement
	int 区域_中间 = 1;

	/**
	 * 底部文字区域。
	 */
	@SimpleDataElement
	int 区域_底部 = 2;

}
