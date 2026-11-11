package simple.library.wsd.iconics;

import simple.runtime.annotations.SimpleDataElement;
import simple.runtime.annotations.SimpleObject;

/**
 * 字体图标常量。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class 图标常量 {
	/**
	 * 静态常量不创建实例。
	 */
	private 图标常量() {
	}

	/**
	 * 状态_启用。
	 */
	@SimpleDataElement
	public static final int 状态_启用 = android.R.attr.state_enabled;

	/**
	 * 状态_按下。
	 */
	@SimpleDataElement
	public static final int 状态_按下 = android.R.attr.state_pressed;

	/**
	 * 状态_选中。
	 */
	@SimpleDataElement
	public static final int 状态_选中 = android.R.attr.state_checked;

	/**
	 * 状态_选择。
	 */
	@SimpleDataElement
	public static final int 状态_选择 = android.R.attr.state_selected;

	/**
	 * 状态_焦点。
	 */
	@SimpleDataElement
	public static final int 状态_焦点 = android.R.attr.state_focused;

	/**
	 * 位置_背景。
	 */
	@SimpleDataElement
	public static final int 位置_背景 = -2;

	/**
	 * 位置_图片。
	 */
	@SimpleDataElement
	public static final int 位置_图片 = -1;

	/**
	 * 位置_左。
	 */
	@SimpleDataElement
	public static final int 位置_左 = 0;

	/**
	 * 位置_上。
	 */
	@SimpleDataElement
	public static final int 位置_上 = 1;

	/**
	 * 位置_右。
	 */
	@SimpleDataElement
	public static final int 位置_右 = 2;

	/**
	 * 位置_下。
	 */
	@SimpleDataElement
	public static final int 位置_下 = 3;

	/**
	 * 位置_起始。
	 */
	@SimpleDataElement
	public static final int 位置_起始 = 4;

	/**
	 * 位置_结束。
	 */
	@SimpleDataElement
	public static final int 位置_结束 = 5;
}
