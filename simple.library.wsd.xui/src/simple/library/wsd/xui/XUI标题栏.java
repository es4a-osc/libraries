package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleEvent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.components.可视组件;

/**
 * XUI标题栏组件。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI标题栏 extends XUI视图组件 {

	/**
	 * 设置沉浸。
	 *
	 * @param immersive 沉浸。
	 */
	@SimpleFunction
	void 置沉浸(boolean immersive);

	/**
	 * 设置左侧文字是否加粗。
	 *
	 * @param isBold 左侧文字是否加粗。
	 */
	@SimpleFunction
	void 置左侧文本粗体(boolean isBold);

	/**
	 * 设置中间文字是否加粗。
	 *
	 * @param isBold 中间文字是否加粗。
	 */
	@SimpleFunction
	void 置中间文本粗体(boolean isBold);

	/**
	 * 设置左侧文字。
	 *
	 * @param title 左侧文字。
	 */
	@SimpleFunction
	void 置左侧文本(String title);

	/**
	 * 设置左侧文字。
	 *
	 * @param resId 左侧文字资源id。
	 */
	@SimpleFunction
	void 置左侧文本资源(int resId);

	/**
	 * 设置左侧文字大小。
	 *
	 * @param size 左侧文字大小【单位px】。
	 */
	@SimpleFunction
	void 置左侧文本尺寸(float size);

	/**
	 * 设置左侧文字的最大长度。
	 *
	 * @param maxEms 左侧文字的最大长度。
	 */
	@SimpleFunction
	void 置左侧文本最大字宽(int maxEms);

	/**
	 * 设置左侧文字的最大宽度。
	 *
	 * @param maxPixels 左侧文字的最大宽度。
	 */
	@SimpleFunction
	void 置左侧文本最大宽度(int maxPixels);

	/**
	 * 设置左侧文字的Padding。
	 *
	 * @param paddingStart 内边距开始。
	 * @param paddingEnd 内边距结束。
	 */
	@SimpleFunction
	void 置左侧文本内边距(int paddingStart, int paddingEnd);

	/**
	 * 设置左侧文字的颜色。
	 *
	 * @param color 左侧文字的颜色。
	 */
	@SimpleFunction
	void 置左侧文本颜色(int color);

	/**
	 * 设置左侧文字是否可显示。
	 *
	 * @param visible 左侧文字是否可显示。
	 */
	@SimpleFunction
	void 置左侧可见(boolean visible);

	/**
	 * 设置标题文字【"\n"为垂直方向，"\t"为水平方向】。
	 *
	 * @param title 标题文字。
	 */
	@SimpleFunction
	void 置标题(String title);

	/**
	 * 设置标题和副标题的文字。
	 *
	 * @param title 标题。
	 * @param subTitle 副标题。
	 * @param orientation 对齐方式。
	 */
	@SimpleFunction
	void 置标题(String title, String subTitle, int orientation);

	/**
	 * 设置副标题的文字。
	 *
	 * @param subTitle 副标题。
	 * @param orientation 方向。
	 */
	@SimpleFunction
	void 置副标题(String subTitle, int orientation);

	/**
	 * 设置副标题的文字。
	 *
	 * @param subTitle 副标题。
	 */
	@SimpleFunction
	void 置副标题(String subTitle);

	/**
	 * 设置中间内容的对齐方式。
	 *
	 * @param gravity 中间内容的对齐方式。
	 */
	@SimpleFunction
	void 置中间对齐(int gravity);

	/**
	 * 设置标题文字。
	 *
	 * @param resId 文字资源id。
	 */
	@SimpleFunction
	void 置标题资源(int resId);

	/**
	 * 设置标题文字颜色。
	 *
	 * @param resId 文字颜色资源id。
	 */
	@SimpleFunction
	void 置标题颜色(int resId);

	/**
	 * 设置标题文字大小。
	 *
	 * @param size 文字大小【单位px】。
	 */
	@SimpleFunction
	void 置标题尺寸(float size);

	/**
	 * 设置标题背景。
	 *
	 * @param resId 标题背景资源id。
	 */
	@SimpleFunction
	void 置标题背景(int resId);

	/**
	 * 设置副标题颜色。
	 *
	 * @param resId 资源 ID。
	 */
	@SimpleFunction
	void 置副标题颜色(int resId);

	/**
	 * 设置副标题尺寸。
	 *
	 * @param size 尺寸。
	 */
	@SimpleFunction
	void 置副标题尺寸(float size);

	/**
	 * 设置分隔线颜色。
	 *
	 * @param color 颜色。
	 */
	@SimpleFunction
	void 置分隔线颜色(int color);

	/**
	 * 设置分隔线高度。
	 *
	 * @param dividerHeight 分隔线高度。
	 */
	@SimpleFunction
	void 置分隔线高度(int dividerHeight);

	/**
	 * 设置操作文本颜色。
	 *
	 * @param colorResId 颜色资源 ID。
	 */
	@SimpleFunction
	void 置操作文本颜色(int colorResId);

	/**
	 * 清空操作项。
	 */
	@SimpleFunction
	void 清空操作项();

	/**
	 * 取得操作数量。
	 *
	 * @return 操作数量。
	 */
	@SimpleFunction
	int 取操作数量();

	/**
	 * 在标题栏右侧添加文字操作并返回索引。
	 *
	 * @param text 文本内容。
	 * @return 在标题栏右侧添加文字操作并返回索引。
	 */
	@SimpleFunction
	int 添加操作(String text);

	/**
	 * 设置左侧图片图像，使用 Simple 图片标识。
	 *
	 * @param image 图片资源标识、资产文件名或外部路径。
	 */
	@SimpleFunction
	void 置左侧图片图像(String image);

	/**
	 * 设置分隔线，使用 Simple 图片标识。
	 *
	 * @param image 图片资源标识、资产文件名或外部路径。
	 */
	@SimpleFunction
	void 置分隔线(String image);

	/**
	 * 标题栏左侧区域被单击时触发。
	 */
	@SimpleEvent
	void 左侧被单击();

	/**
	 * 标题区域被单击时触发。
	 */
	@SimpleEvent
	void 标题被单击();

	/**
	 * 标题栏右侧操作项被单击时触发。
	 *
	 * @param index 添加操作项时返回的索引。
	 */
	@SimpleEvent
	void 操作被单击(int index);

	/**
	 * 移除指定操作项，后续操作项索引依次前移。
	 *
	 * @param index 从零开始的操作项位置。
	 */
	@SimpleFunction
	void 移除操作(int index);

	/**
	 * 更新指定操作项文字。
	 *
	 * @param index 从零开始的操作项位置。
	 * @param text 非空操作文字。
	 */
	@SimpleFunction
	void 置操作文本(int index, String text);

	/**
	 * 借用同一窗口的 Simple 组件显示标题，清除或销毁时恢复。
	 *
	 * @param component 同一窗口中可借用的组件。
	 */
	@SimpleFunction
	void 置自定义标题(可视组件 component);

	/**
	 * 解除自定义标题并恢复原组件。
	 */
	@SimpleFunction
	void 清除自定义标题();

}
