package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleEvent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.components.可视组件;
import simple.runtime.components.组件;

/**
 * 绑定到其它可视组件的可徽标，不占用布局空间；调用绑定目标后显示。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI徽标 extends 组件 {

	/**
	 * 重置。
	 */
	@SimpleFunction
	void 重置();

	/**
	 * 隐藏。
	 *
	 * @param animate 动画。
	 */
	@SimpleFunction
	void 隐藏(boolean animate);

	/**
	 * 设置徽标数。
	 *
	 * @param badgeNumber 徽标数。
	 */
	@SimpleFunction
	void 置徽标数(int badgeNumber);

	/**
	 * 取得徽标数。
	 *
	 * @return 徽标数。
	 */
	@SimpleFunction
	int 取徽标数();

	/**
	 * 设置徽标文本。
	 *
	 * @param badgeText 徽标文本。
	 */
	@SimpleFunction
	void 置徽标文本(String badgeText);

	/**
	 * 取得徽标文本。
	 *
	 * @return 徽标文本。
	 */
	@SimpleFunction
	String 取徽标文本();

	/**
	 * 设置精确模式。
	 *
	 * @param isExact 是否精确。
	 */
	@SimpleFunction
	void 置精确模式(boolean isExact);

	/**
	 * 是否精确模式。
	 *
	 * @return 是否精确模式。
	 */
	@SimpleFunction
	boolean 是否精确模式();

	/**
	 * 设置显示阴影。
	 *
	 * @param showShadow 显示阴影。
	 */
	@SimpleFunction
	void 置显示阴影(boolean showShadow);

	/**
	 * 是否显示阴影。
	 *
	 * @return 是否显示阴影。
	 */
	@SimpleFunction
	boolean 是否显示阴影();

	/**
	 * 设置徽标背景颜色。
	 *
	 * @param color 颜色。
	 */
	@SimpleFunction
	void 置徽标背景颜色(int color);

	/**
	 * 取得徽标背景颜色。
	 *
	 * @return 徽标背景颜色。
	 */
	@SimpleFunction
	int 取徽标背景颜色();

	/**
	 * 设置徽标文本颜色。
	 *
	 * @param color 颜色。
	 */
	@SimpleFunction
	void 置徽标文本颜色(int color);

	/**
	 * 取得徽标文本颜色。
	 *
	 * @return 徽标文本颜色。
	 */
	@SimpleFunction
	int 取徽标文本颜色();

	/**
	 * 设置徽标文本尺寸。
	 *
	 * @param size 尺寸。
	 * @param isSpValue 为真时使用 SP，为假时使用物理像素。
	 */
	@SimpleFunction
	void 置徽标文本尺寸(float size, boolean isSpValue);

	/**
	 * 取得徽标文本尺寸。
	 *
	 * @param isSpValue 为真时使用 SP，为假时使用物理像素。
	 * @return 徽标文本尺寸。
	 */
	@SimpleFunction
	float 取徽标文本尺寸(boolean isSpValue);

	/**
	 * 设置徽标内边距。
	 *
	 * @param padding 内边距。
	 * @param isDpValue 为真时使用 DIP，为假时使用物理像素。
	 */
	@SimpleFunction
	void 置徽标内边距(float padding, boolean isDpValue);

	/**
	 * 取得徽标内边距。
	 *
	 * @param isDpValue 为真时使用 DIP，为假时使用物理像素。
	 * @return 徽标内边距。
	 */
	@SimpleFunction
	float 取徽标内边距(boolean isDpValue);

	/**
	 * 是否可拖动。
	 *
	 * @return 是否可拖动。
	 */
	@SimpleFunction
	boolean 是否可拖动();

	/**
	 * 设置徽标对齐。
	 *
	 * @param gravity 对齐。
	 */
	@SimpleFunction
	void 置徽标对齐(int gravity);

	/**
	 * 取得徽标对齐。
	 *
	 * @return 徽标对齐。
	 */
	@SimpleFunction
	int 取徽标对齐();

	/**
	 * 设置对齐偏移。
	 *
	 * @param offset 偏移。
	 * @param isDpValue 为真时使用 DIP，为假时使用物理像素。
	 */
	@SimpleFunction
	void 置对齐偏移(float offset, boolean isDpValue);

	/**
	 * 设置对齐偏移。
	 *
	 * @param offsetX 偏移横向。
	 * @param offsetY 偏移纵向。
	 * @param isDpValue 为真时使用 DIP，为假时使用物理像素。
	 */
	@SimpleFunction
	void 置对齐偏移(float offsetX, float offsetY, boolean isDpValue);

	/**
	 * 取得对齐偏移横向。
	 *
	 * @param isDpValue 为真时使用 DIP，为假时使用物理像素。
	 * @return 对齐偏移横向。
	 */
	@SimpleFunction
	float 取对齐偏移横向(boolean isDpValue);

	/**
	 * 取得对齐偏移纵向。
	 *
	 * @param isDpValue 为真时使用 DIP，为假时使用物理像素。
	 * @return 对齐偏移纵向。
	 */
	@SimpleFunction
	float 取对齐偏移纵向(boolean isDpValue);

	/**
	 * 把徽标附着到指定组件；目标没有父容器或为自身时拒绝操作。
	 *
	 * @param target 徽标附着的可视组件；组件必须已加入父容器。
	 */
	@SimpleFunction
	void 绑定目标(可视组件 target);

	/**
	 * 设置徽标背景，使用 Simple 图片标识。
	 *
	 * @param image 图片资源标识、资产文件名或外部路径。
	 */
	@SimpleFunction
	void 置徽标背景(String image);

	/**
	 * 徽标拖动状态改变时触发。
	 *
	 * @param state 1 开始、2 范围内拖动、3 超出范围、4 取消、5 消除成功。
	 */
	@SimpleEvent
	void 拖动状态改变(int state);

	/**
	 * 设置徽标背景描边；零宽度取消描边。
	 *
	 * @param color ARGB 描边颜色。
	 * @param width 非负描边宽度，单位为 DP。
	 */
	@SimpleFunction
	void 置描边(int color, float width);

}
