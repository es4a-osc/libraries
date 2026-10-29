package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleEvent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.components.可视组件;
import simple.runtime.components.组件;

/**
 * 以当前窗口内的组件为焦点显示连续操作引导。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI引导 extends 组件 {

	/**
	 * 添加一个引导步骤，显示顺序与添加顺序一致。
	 *
	 * @param target 焦点组件。
	 * @param text 说明文本。
	 * @param rectangle 真为矩形焦点，假为圆形。
	 */
	@SimpleFunction
	void 添加步骤(可视组件 target, String text, boolean rectangle);

	/**
	 * 关闭当前引导并清空全部步骤。
	 */
	@SimpleFunction
	void 清空步骤();

	/**
	 * 从第一步开始引导，采用本次显示时的配置快照。
	 */
	@SimpleFunction
	void 显示();

	/**
	 * 取得已添加的步骤数。
	 *
	 * @return 步骤数量。
	 */
	@SimpleFunction
	int 取步骤数();

	/**
	 * 一个步骤关闭后触发。
	 *
	 * @param index 步骤索引。
	 */
	@SimpleEvent
	void 步骤已关闭(int index);

	/**
	 * 全部步骤通过点击或下一步完成后触发；主动关闭和跳过不触发。
	 */
	@SimpleEvent
	void 引导已完成();

	/**
	 * 设置下次引导的遮罩颜色。
	 *
	 * @param color ARGB 颜色，可包含透明度。
	 */
	@SimpleFunction
	void 置遮罩颜色(int color);

	/**
	 * 设置下次引导的说明文字样式。
	 *
	 * @param color ARGB 文字颜色。
	 * @param size 字号，单位为 sp，须大于零。
	 * @param gravity Android 对齐标志，可使用运行库对齐常量。
	 */
	@SimpleFunction
	void 置文字样式(int color, int size, int gravity);

	/**
	 * 设置下次引导的焦点外观。
	 *
	 * @param color ARGB 边框颜色。
	 * @param width 边框宽度，单位为物理像素，不得为负。
	 * @param padding 焦点向目标外扩的距离，单位为物理像素，不得为负。
	 * @param radius 矩形焦点的圆角半径，单位为物理像素，不得为负。
	 */
	@SimpleFunction
	void 置焦点样式(int color, int width, int padding, int radius);

	/**
	 * 设置下次引导是否允许点击遮罩推进；关闭时由下一步方法推进。
	 *
	 * @param enabled 是否允许点击推进，默认允许。
	 */
	@SimpleFunction
	void 置点击推进(boolean enabled);

	/**
	 * 立即结束当前步骤并进入下一步；最后一步结束后触发引导已完成。
	 */
	@SimpleFunction
	void 下一步();

	/**
	 * 结束当前引导并触发引导已跳过，不触发引导已完成。
	 */
	@SimpleFunction
	void 跳过();

	/**
	 * 取得正在显示的步骤索引。
	 *
	 * @return 从零开始的索引，未显示时为 -1。
	 */
	@SimpleFunction
	int 取当前步骤();

	/**
	 * 步骤完成显示后触发，可据此更新外部操作提示。
	 *
	 * @param index 从零开始的步骤索引。
	 */
	@SimpleEvent
	void 步骤已显示(int index);

	/**
	 * 调用跳过方法结束引导后触发。
	 *
	 * @param index 跳过时所在的步骤索引。
	 */
	@SimpleEvent
	void 引导已跳过(int index);

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
	 * 为后续整轮引导设置持久化标识；已记录的标识不再显示，空文本表示每次显示。
	 *
	 * @param identifier 应用内唯一的引导标识。
	 */
	@SimpleFunction
	void 置仅显示一次(String identifier);

	/**
	 * 查询指定持久化标识是否已经实际显示过。
	 *
	 * @param identifier 非空引导标识。
	 * @return 查询指定持久化标识是否已经实际显示过。
	 */
	@SimpleFunction
	boolean 是否已显示(String identifier);

	/**
	 * 清除指定标识的首次显示记录，不关闭当前引导。
	 *
	 * @param identifier 非空引导标识。
	 */
	@SimpleFunction
	void 重置显示记录(String identifier);

	/**
	 * 设置后续引导焦点相对目标或指定区域的偏移。
	 *
	 * @param x 水平物理像素偏移，可为负数。
	 * @param y 垂直物理像素偏移，可为负数。
	 */
	@SimpleFunction
	void 置焦点偏移(int x, int y);

	/**
	 * 设置后续引导是否播放焦点脉动动画。
	 *
	 * @param enabled 是否启用焦点动画。
	 */
	@SimpleFunction
	void 置焦点动画(boolean enabled);

	/**
	 * 设置后续引导进入与退出的淡入淡出时长。
	 *
	 * @param enter 非负进入毫秒数。
	 * @param exit 非负退出毫秒数。
	 */
	@SimpleFunction
	void 置进出场时长(int enter, int exit);

	/**
	 * 添加相对 Simple 当前窗口内容区的指定区域引导。
	 *
	 * @param left 区域左边界，单位为物理像素。
	 * @param top 区域顶边界，单位为物理像素。
	 * @param width 大于零的区域宽度。
	 * @param height 大于零的区域高度。
	 * @param text 步骤说明，可为空文本。
	 * @param rectangle 真为矩形，假为覆盖区域的圆形。
	 */
	@SimpleFunction
	void 添加区域步骤(int left, int top, int width, int height, String text, boolean rectangle);

	/**
	 * 为尚未显示的步骤设置提示图片，图片替代该步文字；零资源标识恢复文字。
	 *
	 * @param index 从 0 开始的步骤索引。
	 * @param image 应用内 drawable 或 mipmap 资源标识。
	 * @param width 与高同时为零表示采用图片原始尺寸，否则须均大于零。
	 * @param height 图片高度，单位为物理像素。
	 * @param gravity Android Gravity 对齐值。
	 * @param x 图片横向物理像素偏移。
	 * @param y 图片纵向物理像素偏移。
	 */
	@SimpleFunction
	void 置步骤图片(int index, int image, int width, int height, int gravity, int x, int y);

}
