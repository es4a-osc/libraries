package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleDataElement;
import simple.runtime.annotations.SimpleEvent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.components.可视组件;
import simple.runtime.components.组件;

/**
 * 在当前窗口组件旁显示带箭头的说明气泡。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI气泡 extends 组件 {

	/**
	 * 显示说明气泡；再次调用会替换旧气泡。
	 *
	 * @param anchor 锚点组件。
	 * @param text 说明文本。
	 * @param position 0：上；1：下；2：左；3：右。
	 * @param duration 自动关闭毫秒数；零表示点击关闭。
	 */
	@SimpleFunction
	void 显示(可视组件 anchor, String text, int position, long duration);

	/**
	 * 关闭当前显示的界面。
	 */
	@SimpleFunction
	void 关闭();

	/**
	 * 取得当前界面是否正在显示。
	 *
	 * @return 是否显示。
	 */
	@SimpleFunction
	boolean 是否显示();

	/**
	 * 设置后续气泡的背景色和文字色。
	 *
	 * @param color ARGB 背景颜色。
	 * @param text ARGB 文字颜色。
	 */
	@SimpleFunction
	void 置颜色(int color, int text);

	/**
	 * 设置后续气泡文字大小。
	 *
	 * @param size 大于零的 SP 字号。
	 */
	@SimpleFunction
	void 置文字大小(float size);

	/**
	 * 设置后续气泡四边的像素内边距。
	 *
	 * @param left 非负像素。
	 * @param top 非负像素。
	 * @param right 非负像素。
	 * @param bottom 非负像素。
	 */
	@SimpleFunction
	void 置内边距(int left, int top, int right, int bottom);

	/**
	 * 设置后续气泡圆角像素半径。
	 *
	 * @param radius 非负像素。
	 */
	@SimpleFunction
	void 置圆角(int radius);

	/**
	 * 设置气泡相对锚点的对齐方式。
	 *
	 * @param align 0 起点；1 居中。
	 */
	@SimpleFunction
	void 置对齐(int align);

	/**
	 * 借用同一窗口的 Simple 组件显示气泡，关闭时恢复组件。
	 *
	 * @param anchor 当前窗口中可见的锚点。
	 * @param content 同一窗口中可借用的内容。
	 * @param position 气泡方向常量。
	 * @param duration 自动关闭毫秒数，0 不自动关闭。
	 */
	@SimpleFunction
	void 显示自定义(可视组件 anchor, 可视组件 content, int position, long duration);

	/**
	 * 气泡实际显示时触发。
	 */
	@SimpleEvent
	void 已显示();

	/**
	 * 气泡自动、点击或主动关闭时触发。
	 */
	@SimpleEvent
	void 已关闭();

	/**
	 * 在锚点上方显示。
	 */
	@SimpleDataElement
	int 方向_上 = 0;

	/**
	 * 在锚点下方显示。
	 */
	@SimpleDataElement
	int 方向_下 = 1;

	/**
	 * 在锚点左方显示。
	 */
	@SimpleDataElement
	int 方向_左 = 2;

	/**
	 * 在锚点右方显示。
	 */
	@SimpleDataElement
	int 方向_右 = 3;

	/**
	 * 设置后续显示的气泡是否在点击后关闭。
	 *
	 * @param hide 是否点击隐藏。
	 */
	@SimpleFunction
	void 置点击隐藏(boolean hide);

}
