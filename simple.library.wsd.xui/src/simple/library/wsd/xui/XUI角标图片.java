package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleDataElement;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * XUI角标图片组件。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI角标图片 extends XUI视图组件 {

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
	 * 是否标签可见。
	 *
	 * @return 是否标签可见。
	 */
	@SimpleFunction
	boolean 是否标签可见();

	/**
	 * 设置标签可见。
	 *
	 * @param enable 启用。
	 */
	@SimpleFunction
	void 置标签可见(boolean enable);

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
	 * 设置标签背景透明度。
	 *
	 * @param alpha 透明度。
	 */
	@SimpleFunction
	void 置标签背景透明度(int alpha);

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

	/**
	 * 设置图片，空文本清除图片。
	 *
	 * @param image 图片资源标识、资产文件名或外部文件路径。
	 */
	@SimpleFunction
	void 置图片(String image);

	/**
	 * 设置图片与视图边界之间的缩放方式。
	 *
	 * @param mode 图片缩放模式常量。
	 */
	@SimpleFunction
	void 置图片缩放模式(int mode);

	/**
	 * 矩阵显示图片。
	 */
	@SimpleDataElement
	int 缩放模式_矩阵 = 0;

	/**
	 * 拉伸显示图片。
	 */
	@SimpleDataElement
	int 缩放模式_拉伸 = 1;

	/**
	 * 适应起点显示图片。
	 */
	@SimpleDataElement
	int 缩放模式_适应起点 = 2;

	/**
	 * 适应居中显示图片。
	 */
	@SimpleDataElement
	int 缩放模式_适应居中 = 3;

	/**
	 * 适应末尾显示图片。
	 */
	@SimpleDataElement
	int 缩放模式_适应末尾 = 4;

	/**
	 * 居中显示图片。
	 */
	@SimpleDataElement
	int 缩放模式_居中 = 5;

	/**
	 * 居中裁剪显示图片。
	 */
	@SimpleDataElement
	int 缩放模式_居中裁剪 = 6;

	/**
	 * 居中缩小显示图片。
	 */
	@SimpleDataElement
	int 缩放模式_居中缩小 = 7;

}
