package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleEvent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * XUI开关组件。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI开关 extends XUI视图组件 {

	/**
	 * 设置选中无事件。
	 *
	 * @param checked 选中。
	 */
	@SimpleFunction
	void 置选中无事件(boolean checked);

	/**
	 * 设置选中立即无事件。
	 *
	 * @param checked 选中。
	 */
	@SimpleFunction
	void 置选中立即无事件(boolean checked);

	/**
	 * 设置选中立即。
	 *
	 * @param checked 选中。
	 */
	@SimpleFunction
	void 置选中立即(boolean checked);

	/**
	 * 取得动画毫秒。
	 *
	 * @return 动画毫秒。
	 */
	@SimpleFunction
	long 取动画毫秒();

	/**
	 * 设置动画毫秒。
	 *
	 * @param animationDuration 动画毫秒。
	 */
	@SimpleFunction
	void 置动画毫秒(long animationDuration);

	/**
	 * 取得滑块范围比例。
	 *
	 * @return 滑块范围比例。
	 */
	@SimpleFunction
	float 取滑块范围比例();

	/**
	 * 设置滑块范围比例。
	 *
	 * @param thumbRangeRatio 滑块范围比例。
	 */
	@SimpleFunction
	void 置滑块范围比例(float thumbRangeRatio);

	/**
	 * 设置滑块外边距。
	 *
	 * @param left 左侧。
	 * @param top 顶部。
	 * @param right 右侧。
	 * @param bottom 底部。
	 */
	@SimpleFunction
	void 置滑块外边距(float left, float top, float right, float bottom);

	/**
	 * 设置滑块尺寸。
	 *
	 * @param width 宽度。
	 * @param height 高度。
	 */
	@SimpleFunction
	void 置滑块尺寸(int width, int height);

	/**
	 * 取得滑块宽度。
	 *
	 * @return 滑块宽度。
	 */
	@SimpleFunction
	float 取滑块宽度();

	/**
	 * 取得滑块高度。
	 *
	 * @return 滑块高度。
	 */
	@SimpleFunction
	float 取滑块高度();

	/**
	 * 取得滑块圆角。
	 *
	 * @return 滑块圆角。
	 */
	@SimpleFunction
	float 取滑块圆角();

	/**
	 * 设置滑块圆角。
	 *
	 * @param thumbRadius 滑块圆角。
	 */
	@SimpleFunction
	void 置滑块圆角(float thumbRadius);

	/**
	 * 取得背景圆角。
	 *
	 * @return 背景圆角。
	 */
	@SimpleFunction
	float 取背景圆角();

	/**
	 * 设置背景圆角。
	 *
	 * @param backRadius 返回圆角。
	 */
	@SimpleFunction
	void 置背景圆角(float backRadius);

	/**
	 * 是否渐变背景。
	 *
	 * @return 是否渐变背景。
	 */
	@SimpleFunction
	boolean 是否渐变背景();

	/**
	 * 设置渐变背景。
	 *
	 * @param fadeBack 渐变返回。
	 */
	@SimpleFunction
	void 置渐变背景(boolean fadeBack);

	/**
	 * 取得着色颜色。
	 *
	 * @return 着色颜色。
	 */
	@SimpleFunction
	int 取着色颜色();

	/**
	 * 设置着色颜色。
	 *
	 * @param tintColor 着色颜色。
	 */
	@SimpleFunction
	void 置着色颜色(int tintColor);

	/**
	 * 取得文本开。
	 *
	 * @return 文本开。
	 */
	@SimpleFunction
	String 取文本开();

	/**
	 * 取得文本关。
	 *
	 * @return 文本关。
	 */
	@SimpleFunction
	String 取文本关();

	/**
	 * 设置文本滑块内缩。
	 *
	 * @param textThumbInset 文本滑块内缩。
	 */
	@SimpleFunction
	void 置文本滑块内缩(int textThumbInset);

	/**
	 * 设置文本扩展。
	 *
	 * @param textExtra 文本扩展。
	 */
	@SimpleFunction
	void 置文本扩展(int textExtra);

	/**
	 * 设置文本调整。
	 *
	 * @param textAdjust 文本调整。
	 */
	@SimpleFunction
	void 置文本调整(int textAdjust);

	/**
	 * 取得选中状态。
	 *
	 * @return 选中状态。
	 */
	@SimpleFunction
	boolean 取选中();

	/**
	 * 设置选中状态。
	 *
	 * @param checked 是否选中。
	 */
	@SimpleFunction
	void 置选中(boolean checked);

	/**
	 * 设置滑块图像，使用 Simple 图片标识。
	 *
	 * @param image 图片资源标识、资产文件名或外部路径。
	 */
	@SimpleFunction
	void 置滑块图像(String image);

	/**
	 * 设置背景图像，使用 Simple 图片标识。
	 *
	 * @param image 图片资源标识、资产文件名或外部路径。
	 */
	@SimpleFunction
	void 置背景图像(String image);

	/**
	 * 设置背景颜色。
	 *
	 * @param color ARGB 颜色。
	 */
	@SimpleFunction
	void 置背景颜色(int color);

	/**
	 * 设置滑块颜色颜色。
	 *
	 * @param color ARGB 颜色。
	 */
	@SimpleFunction
	void 置滑块颜色(int color);

	/**
	 * 选中状态改变时触发。
	 *
	 * @param checked 是否选中。
	 */
	@SimpleEvent
	void 选中改变(boolean checked);

	/**
	 * 设置选中和未选中状态的文字。
	 *
	 * @param on 选中状态文字。
	 * @param off 未选中状态文字。
	 */
	@SimpleFunction
	void 置状态文本(String on, String off);

}
