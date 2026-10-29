package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleEvent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * XUI水平进度条组件。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI水平进度条 extends XUI视图组件 {

	/**
	 * 设置动画类型。
	 *
	 * @param type 类型。
	 */
	@SimpleFunction
	void 置动画类型(int type);

	/**
	 * 设置进度。
	 *
	 * @param progress 百分比进度。
	 */
	@SimpleFunction
	void 置进度(float progress);

	/**
	 * 取得进度。
	 *
	 * @return 进度。
	 */
	@SimpleFunction
	float 取进度();

	/**
	 * 设置开始进度。
	 *
	 * @param startProgress 开始进度。
	 */
	@SimpleFunction
	void 置开始进度(float startProgress);

	/**
	 * 设置结束进度。
	 *
	 * @param endProgress 结束进度。
	 */
	@SimpleFunction
	void 置结束进度(float endProgress);

	/**
	 * 设置开始颜色。
	 *
	 * @param startColor 开始颜色。
	 */
	@SimpleFunction
	void 置开始颜色(int startColor);

	/**
	 * 设置结束颜色。
	 *
	 * @param endColor 结束颜色。
	 */
	@SimpleFunction
	void 置结束颜色(int endColor);

	/**
	 * 设置轨道宽度。
	 *
	 * @param width 绘制宽度，单位为物理像素。
	 */
	@SimpleFunction
	void 置轨道宽度(int width);

	/**
	 * 设置轨道颜色。
	 *
	 * @param color 颜色。
	 */
	@SimpleFunction
	void 置轨道颜色(int color);

	/**
	 * 设置进度文本颜色。
	 *
	 * @param textColor 文字颜色。
	 */
	@SimpleFunction
	void 置进度文本颜色(int textColor);

	/**
	 * 设置进度文本尺寸。
	 *
	 * @param size 进度文本尺寸，单位为物理像素。
	 */
	@SimpleFunction
	void 置进度文本尺寸(int size);

	/**
	 * 设置进度时长。
	 *
	 * @param duration 动画时长，单位为毫秒。
	 */
	@SimpleFunction
	void 置进度时长(int duration);

	/**
	 * 设置轨道启用。
	 *
	 * @param trackAble 是否跟随触摸位置更新进度。
	 */
	@SimpleFunction
	void 置轨道启用(boolean trackAble);

	/**
	 * 设置进度文本可见性。
	 *
	 * @param visibility 可见性。
	 */
	@SimpleFunction
	void 置进度文本可见性(boolean visibility);

	/**
	 * 设置进度文本跟随。
	 *
	 * @param moved 跟随。
	 */
	@SimpleFunction
	void 置进度文本跟随(boolean moved);

	/**
	 * 开始进度动画。
	 */
	@SimpleFunction
	void 开始进度动画();

	/**
	 * 停止进度动画。
	 */
	@SimpleFunction
	void 停止进度动画();

	/**
	 * 设置进度圆角。
	 *
	 * @param radius 圆角。
	 */
	@SimpleFunction
	void 置进度圆角(int radius);

	/**
	 * 设置进度文本内边距底部。
	 *
	 * @param offset 偏移。
	 */
	@SimpleFunction
	void 置进度文本内边距底部(int offset);

	/**
	 * 进度动画开始时触发。
	 */
	@SimpleEvent
	void 进度开始();

	/**
	 * 进度动画更新时触发。
	 *
	 * @param progress 当前进度。
	 */
	@SimpleEvent
	void 进度改变(float progress);

	/**
	 * 进度动画完成时触发。
	 */
	@SimpleEvent
	void 进度完成();
}
