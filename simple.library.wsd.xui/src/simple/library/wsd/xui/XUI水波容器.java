package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleEvent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.components.组件容器;

/**
 * XUI水波容器组件。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI水波容器 extends XUI视图组件, 组件容器 {

	/**
	 * 设置水波颜色。
	 *
	 * @param rippleColor 水波颜色。
	 */
	@SimpleFunction
	void 置水波颜色(int rippleColor);

	/**
	 * 取得水波颜色。
	 *
	 * @return 水波颜色。
	 */
	@SimpleFunction
	int 取水波颜色();

	/**
	 * 是否居中。
	 *
	 * @return 是否居中。
	 */
	@SimpleFunction
	boolean 是否居中();

	/**
	 * 设置居中。
	 *
	 * @param isCentered 是否居中。
	 */
	@SimpleFunction
	void 置居中(boolean isCentered);

	/**
	 * 取得水波内边距。
	 *
	 * @return 水波内边距。
	 */
	@SimpleFunction
	int 取水波内边距();

	/**
	 * 设置水波内边距。
	 *
	 * @param ripplePadding 水波内边距。
	 */
	@SimpleFunction
	void 置水波内边距(int ripplePadding);

	/**
	 * 是否缩放。
	 *
	 * @return 是否缩放。
	 */
	@SimpleFunction
	boolean 是否缩放();

	/**
	 * 设置缩放。
	 *
	 * @param hasToZoom 含有到缩放。
	 */
	@SimpleFunction
	void 置缩放(boolean hasToZoom);

	/**
	 * 取得触摸反馈的缩放比例。
	 *
	 * @return 缩放比例。
	 */
	@SimpleFunction
	float 取缩放比例();

	/**
	 * 设置触摸反馈的缩放比例。
	 *
	 * @param zoomScale 缩放比例。
	 */
	@SimpleFunction
	void 置缩放比例(float zoomScale);

	/**
	 * 取得缩放时长。
	 *
	 * @return 缩放时长。
	 */
	@SimpleFunction
	int 取缩放时长();

	/**
	 * 设置缩放时长。
	 *
	 * @param zoomDuration 缩放时长。
	 */
	@SimpleFunction
	void 置缩放时长(int zoomDuration);

	/**
	 * 取得水波时长。
	 *
	 * @return 水波时长。
	 */
	@SimpleFunction
	int 取水波时长();

	/**
	 * 设置水波时长。
	 *
	 * @param rippleDuration 水波时长。
	 */
	@SimpleFunction
	void 置水波时长(int rippleDuration);

	/**
	 * 取得帧频率。
	 *
	 * @return 帧频率。
	 */
	@SimpleFunction
	int 取帧频率();

	/**
	 * 设置帧频率。
	 *
	 * @param frameRate 帧频率。
	 */
	@SimpleFunction
	void 置帧频率(int frameRate);

	/**
	 * 取得水波透明度。
	 *
	 * @return 水波透明度。
	 */
	@SimpleFunction
	int 取水波透明度();

	/**
	 * 设置水波透明度。
	 *
	 * @param rippleAlpha 水波透明度。
	 */
	@SimpleFunction
	void 置水波透明度(int rippleAlpha);

	/**
	 * 设置水波动画类型。
	 *
	 * @param type 0 基础水波、1 双重水波、2 矩形水波。
	 */
	@SimpleFunction
	void 置水波类型(int type);

	/**
	 * 取得水波类型：0 基础、1 双重、2 矩形。
	 *
	 * @return 水波类型：0 基础、1 双重、2 矩形。
	 */
	@SimpleFunction
	int 取水波类型();

	/**
	 * 水波动画结束时触发。
	 */
	@SimpleEvent
	void 水波完成();
}
