package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * XUI形状按钮组件。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI形状按钮 extends XUI文本组件 {

	/**
	 * 设置填充图片。
	 *
	 * @param drawableId 图片资源 ID。
	 * @param space 图片与文字间距，单位为物理像素。
	 */
	@SimpleFunction
	void 置文本图像(int drawableId, int space);

	/**
	 * 设置填充颜色。
	 *
	 * @param colorId ARGB 颜色值。
	 */
	@SimpleFunction
	void 置填充颜色(int colorId);

	/**
	 * 设置圆角半径。
	 *
	 * @param leftTopRadius 左上角半径，单位为物理像素。
	 * @param leftBottomRadius 左下角半径，单位为物理像素。
	 * @param rightTopRadius 右上角半径，单位为物理像素。
	 * @param rightBottomRadius 右下角半径，单位为物理像素。
	 */
	@SimpleFunction
	void 置圆角(int leftTopRadius, int leftBottomRadius, int rightTopRadius, int rightBottomRadius);

	/**
	 * 设置边框颜色及宽度。
	 *
	 * @param strokeWidth 边框宽度，单位为物理像素。
	 * @param colorId ARGB 边框颜色值。
	 */
	@SimpleFunction
	void 置描边宽度与颜色(int strokeWidth, int colorId);

	/**
	 * 设置选中状态的颜色。
	 *
	 * @param normalTextColor 正常状态颜色。
	 * @param selectedTextColor 按下状态颜色。
	 */
	@SimpleFunction
	void 置选中文本颜色(int normalTextColor, int selectedTextColor);

	/**
	 * 设置选中状态的颜色。
	 *
	 * @param selectedSolidColor 按下状态颜色。
	 */
	@SimpleFunction
	void 置选中填充颜色(int selectedSolidColor);
}
