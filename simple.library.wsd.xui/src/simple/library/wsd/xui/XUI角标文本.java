package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * XUI角标文本组件。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI角标文本 extends XUI文本组件 {

	/**
	 * 设置标签高度。
	 *
	 * @param height 高度。
	 */
	@SimpleFunction
	void 置标签高度(int height);

	/**
	 * 取得标签高度。
	 *
	 * @return 标签高度。
	 */
	@SimpleFunction
	int 取标签高度();

	/**
	 * 设置标签距离。
	 *
	 * @param distance 距离。
	 */
	@SimpleFunction
	void 置标签距离(int distance);

	/**
	 * 取得标签距离。
	 *
	 * @return 标签距离。
	 */
	@SimpleFunction
	int 取标签距离();

	/**
	 * 是否标签启用。
	 *
	 * @return 是否标签启用。
	 */
	@SimpleFunction
	boolean 是否标签启用();

	/**
	 * 设置标签启用。
	 *
	 * @param enable 启用。
	 */
	@SimpleFunction
	void 置标签启用(boolean enable);

	/**
	 * 取得标签方向。
	 *
	 * @return 标签方向。
	 */
	@SimpleFunction
	int 取标签方向();

	/**
	 * 设置标签方向。
	 *
	 * @param orientation 方向。
	 */
	@SimpleFunction
	void 置标签方向(int orientation);

	/**
	 * 取得标签文本颜色。
	 *
	 * @return 标签文本颜色。
	 */
	@SimpleFunction
	int 取标签文本颜色();

	/**
	 * 设置标签文本颜色。
	 *
	 * @param textColor 文字颜色。
	 */
	@SimpleFunction
	void 置标签文本颜色(int textColor);

	/**
	 * 取得标签背景颜色。
	 *
	 * @return 标签背景颜色。
	 */
	@SimpleFunction
	int 取标签背景颜色();

	/**
	 * 设置标签背景颜色。
	 *
	 * @param backgroundColor 背景颜色。
	 */
	@SimpleFunction
	void 置标签背景颜色(int backgroundColor);

	/**
	 * 取得标签文本。
	 *
	 * @return 标签文本。
	 */
	@SimpleFunction
	String 取标签文本();

	/**
	 * 设置标签文本。
	 *
	 * @param text 文本。
	 */
	@SimpleFunction
	void 置标签文本(String text);

	/**
	 * 取得标签文本尺寸。
	 *
	 * @return 标签文本尺寸。
	 */
	@SimpleFunction
	int 取标签文本尺寸();

	/**
	 * 设置标签文本尺寸。
	 *
	 * @param textSize 文字大小。
	 */
	@SimpleFunction
	void 置标签文本尺寸(int textSize);

	/**
	 * 取得标签文本样式。
	 *
	 * @return 标签文本样式。
	 */
	@SimpleFunction
	int 取标签文本样式();

	/**
	 * 设置标签文本样式。
	 *
	 * @param textStyle 文本样式。
	 */
	@SimpleFunction
	void 置标签文本样式(int textStyle);

	/**
	 * 取得标签文本字体。
	 *
	 * @return 标签文本字体。
	 */
	@SimpleFunction
	String 取标签文本字体();

	/**
	 * 设置标签文本字体。
	 *
	 * @param textFont 文本字体。
	 */
	@SimpleFunction
	void 置标签文本字体(String textFont);
}
