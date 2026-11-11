package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * XUI圆角图片组件。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI圆角图片 extends XUI图片组件 {

	/**
	 * 设置边框宽度。
	 *
	 * @param borderWidth 边框宽度。
	 */
	@SimpleFunction
	void 置边框宽度(int borderWidth);

	/**
	 * 设置边框颜色。
	 *
	 * @param borderColor 边框颜色。
	 */
	@SimpleFunction
	void 置边框颜色(int borderColor);

	/**
	 * 设置圆角。
	 *
	 * @param cornerRadius 圆角。
	 */
	@SimpleFunction
	void 置圆角(int cornerRadius);

	/**
	 * 设置选中边框颜色。
	 *
	 * @param selectedBorderColor 选中边框颜色。
	 */
	@SimpleFunction
	void 置选中边框颜色(int selectedBorderColor);

	/**
	 * 设置选中边框宽度。
	 *
	 * @param selectedBorderWidth 选中边框宽度。
	 */
	@SimpleFunction
	void 置选中边框宽度(int selectedBorderWidth);

	/**
	 * 设置选中遮罩颜色。
	 *
	 * @param selectedMaskColor 选中遮罩颜色。
	 */
	@SimpleFunction
	void 置选中遮罩颜色(int selectedMaskColor);

	/**
	 * 设置圆形。
	 *
	 * @param isCircle 是否圆形。
	 */
	@SimpleFunction
	void 置圆形(boolean isCircle);

	/**
	 * 设置椭圆。
	 *
	 * @param isOval 是否椭圆。
	 */
	@SimpleFunction
	void 置椭圆(boolean isOval);

	/**
	 * 取得边框颜色。
	 *
	 * @return 边框颜色。
	 */
	@SimpleFunction
	int 取边框颜色();

	/**
	 * 取得边框宽度。
	 *
	 * @return 边框宽度。
	 */
	@SimpleFunction
	int 取边框宽度();

	/**
	 * 取得圆角。
	 *
	 * @return 圆角。
	 */
	@SimpleFunction
	int 取圆角();

	/**
	 * 取得选中边框颜色。
	 *
	 * @return 选中边框颜色。
	 */
	@SimpleFunction
	int 取选中边框颜色();

	/**
	 * 取得选中边框宽度。
	 *
	 * @return 选中边框宽度。
	 */
	@SimpleFunction
	int 取选中边框宽度();

	/**
	 * 取得选中遮罩颜色。
	 *
	 * @return 选中遮罩颜色。
	 */
	@SimpleFunction
	int 取选中遮罩颜色();

	/**
	 * 是否圆形。
	 *
	 * @return 是否圆形。
	 */
	@SimpleFunction
	boolean 是否圆形();

	/**
	 * 是否椭圆。
	 *
	 * @return 是否椭圆。
	 */
	@SimpleFunction
	boolean 是否椭圆();

	/**
	 * 设置触摸选择模式启用。
	 *
	 * @param touchSelectModeEnabled 触摸选择模式启用。
	 */
	@SimpleFunction
	void 置触摸选择模式启用(boolean touchSelectModeEnabled);

	/**
	 * 是否触摸选择模式启用。
	 *
	 * @return 是否触摸选择模式启用。
	 */
	@SimpleFunction
	boolean 是否触摸选择模式启用();

	/**
	 * 设置选中状态并应用已配置的选中边框和遮罩。
	 *
	 * @param selected 是否选中。
	 */
	@SimpleFunction
	void 置选中(boolean selected);

	/**
	 * 取得图片当前选中状态。
	 *
	 * @return 图片当前选中状态。
	 */
	@SimpleFunction
	boolean 取选中();

}
