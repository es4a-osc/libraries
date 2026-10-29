package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleDataElement;
import simple.runtime.annotations.SimpleEvent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.components.组件;

/**
 * 日期和多级选择器共用的外观、取消控制及关闭事件；配置在下次显示时应用。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public interface XUI选择器 extends 组件 {

	/**
	 * 设置下次显示时的标题。
	 *
	 * @param title 标题文本。
	 */
	@SimpleFunction
	void 置标题(String title);

	/**
	 * 设置下次显示时的确认和取消按钮文字。
	 *
	 * @param confirm 确认文字，不可为空。
	 * @param cancel 取消文字，不可为空。
	 */
	@SimpleFunction
	void 置按钮文本(String confirm, String cancel);

	/**
	 * 设置选中项和其它项的 ARGB 文字颜色。
	 *
	 * @param selected 选中项颜色。
	 * @param other 其它项颜色。
	 */
	@SimpleFunction
	void 置文字颜色(int selected, int other);

	/**
	 * 设置标题的 ARGB 颜色。
	 *
	 * @param color 标题颜色。
	 */
	@SimpleFunction
	void 置标题颜色(int color);

	/**
	 * 设置确认和取消按钮的 ARGB 颜色。
	 *
	 * @param confirm 确认按钮颜色。
	 * @param cancel 取消按钮颜色。
	 */
	@SimpleFunction
	void 置按钮颜色(int confirm, int cancel);

	/**
	 * 设置滚轮分隔线的 ARGB 颜色。
	 *
	 * @param color 分隔线颜色。
	 */
	@SimpleFunction
	void 置分隔线颜色(int color);

	/**
	 * 设置滚轮、标题和按钮的字号，单位为 SP。
	 *
	 * @param content 滚轮字号，须大于零。
	 * @param title 标题字号，须大于零。
	 * @param button 按钮字号，须大于零。
	 */
	@SimpleFunction
	void 置字号(int content, int title, int button);

	/**
	 * 设置返回键和点击外部是否取消，下次显示时生效；取消按钮仍可使用。
	 *
	 * @param back 是否允许返回键取消。
	 * @param outside 是否允许点击外部取消。
	 */
	@SimpleFunction
	void 置取消方式(boolean back, boolean outside);

	/**
	 * 用户通过取消按钮、返回键或外部点击放弃选择；随后触发已关闭。
	 */
	@SimpleEvent
	void 已取消();

	/**
	 * 界面因确认、取消、程序关闭或切窗而关闭；销毁后不再发送事件。
	 */
	@SimpleEvent
	void 已关闭();

	/**
	 * 设置后续选择器的滚轮及标题栏背景色。
	 *
	 * @param wheel ARGB 颜色。
	 * @param title ARGB 颜色。
	 */
	@SimpleFunction
	void 置背景颜色(int wheel, int title);

	/**
	 * 设置后续选择器的滚轮行距倍数。
	 *
	 * @param spacing 1.2 至 2.0。
	 */
	@SimpleFunction
	void 置行距(float spacing);

	/**
	 * 设置后续选择器的分隔线长度方式。
	 *
	 * @param type 分隔线类型常量。
	 */
	@SimpleFunction
	void 置分隔线类型(int type);

	/**
	 * 填满的分隔线。
	 */
	@SimpleDataElement
	int 分隔线_填满 = 0;

	/**
	 * 适应文字的分隔线。
	 */
	@SimpleDataElement
	int 分隔线_适应文字 = 1;

	/**
	 * 设置单位是否仅在中间选中行显示，下次显示时生效。
	 *
	 * @param center 真为只在选中行显示，假为各行均显示。
	 */
	@SimpleFunction
	void 置单位居中(boolean center);

}
