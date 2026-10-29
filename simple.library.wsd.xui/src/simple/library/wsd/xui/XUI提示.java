package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleEvent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.components.可视组件;
import simple.runtime.components.组件;

/**
 * 显示带状态图标的吐司、顶部或底部消息条，以及锚点飘字。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI提示 extends 组件 {

	/**
	 * 显示 XUI 风格的短时消息；应用须处于前台。
	 *
	 * @param text 消息文本。
	 * @param style 0：普通；1：信息；2：成功；3：警告；4：错误。
	 * @param longDuration 是否使用系统长时长。
	 */
	@SimpleFunction
	void 显示吐司(String text, int style, boolean longDuration);

	/**
	 * 显示依附当前 Activity 的消息条；切换所属窗口时自动关闭。
	 *
	 * @param title 标题。
	 * @param message 正文。
	 * @param action 操作按钮文本，可为空。
	 * @param duration 显示毫秒数，须大于零。
	 * @param bottom 是否位于底部。
	 */
	@SimpleFunction
	void 显示消息条(String title, String message, String action, long duration, boolean bottom);

	/**
	 * 在锚点上方显示向上漂浮的文字。
	 *
	 * @param anchor 当前窗口中的锚点。
	 * @param text 飘字内容。
	 * @param color ARGB 文字颜色。
	 * @param size 文字大小，单位为 sp，须大于零。
	 */
	@SimpleFunction
	void 显示飘字(可视组件 anchor, String text, int color, int size);

	/**
	 * 取消本组件显示的吐司、消息条和飘字。
	 */
	@SimpleFunction
	void 关闭();

	/**
	 * 消息条的操作按钮被单击。
	 */
	@SimpleEvent
	void 操作被单击();

	/**
	 * 显示可操作的底部提示条。
	 *
	 * @param text 提示文本。
	 * @param action 操作按钮文本，可为空。
	 * @param duration 显示毫秒数；-1 表示持续至用户操作。
	 */
	@SimpleFunction
	void 显示操作条(String text, String action, int duration);

	/**
	 * 设置下次显示的消息条背景、标题、正文和操作文字颜色。
	 *
	 * @param background ARGB 背景颜色。
	 * @param title ARGB 标题颜色。
	 * @param message ARGB 正文颜色。
	 * @param action ARGB 操作文字颜色。
	 */
	@SimpleFunction
	void 置消息条颜色(int background, int title, int message, int action);

	/**
	 * 设置下次显示的消息条图标；空文本隐藏图标。
	 *
	 * @param image 资源图片或本地图片。
	 */
	@SimpleFunction
	void 置消息条图标(String image);

	/**
	 * 设置下次显示的消息条四周边距。
	 *
	 * @param left 非负边距，单位 dp。
	 * @param top 非负边距，单位 dp。
	 * @param right 非负边距，单位 dp。
	 * @param bottom 非负边距，单位 dp。
	 */
	@SimpleFunction
	void 置消息条边距(int left, int top, int right, int bottom);

	/**
	 * 设置下次显示的操作条背景、正文和操作文字颜色。
	 *
	 * @param background ARGB 背景颜色。
	 * @param text ARGB 正文颜色。
	 * @param action ARGB 操作文字颜色。
	 */
	@SimpleFunction
	void 置操作条颜色(int background, int text, int action);

	/**
	 * 设置飘字相对锚点上缘的起止高度；向上为正。
	 *
	 * @param start 非负高度，单位 dp。
	 * @param end 不得小于起始高度，单位 dp。
	 */
	@SimpleFunction
	void 置飘字范围(int start, int end);

	/**
	 * 设置下次飘字的起止不透明度。
	 *
	 * @param from 0 至 1；0 为完全透明。
	 * @param to 0 至 1；1 为完全不透明。
	 */
	@SimpleFunction
	void 置飘字透明(float from, float to);

	/**
	 * 设置下次飘字的动画时长。
	 *
	 * @param duration 毫秒数，须大于零。
	 */
	@SimpleFunction
	void 置飘字时长(int duration);

}
