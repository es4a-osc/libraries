package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleEvent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * XUI闪光按钮组件。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI闪光按钮 extends XUI视图组件 {

	/**
	 * 取得颜色。
	 *
	 * @return 颜色。
	 */
	@SimpleFunction
	int 取颜色();

	/**
	 * 切换。
	 */
	@SimpleFunction
	void 切换();

	/**
	 * 设置正常颜色。
	 *
	 * @param normalColor 正常颜色。
	 */
	@SimpleFunction
	void 置正常颜色(int normalColor);

	/**
	 * 设置选中颜色。
	 *
	 * @param checkedColor 选中颜色。
	 */
	@SimpleFunction
	void 置选中颜色(int checkedColor);

	/**
	 * 设置选中。
	 *
	 * @param checked 选中。
	 * @param anim 动画。
	 */
	@SimpleFunction
	void 置选中(boolean checked, boolean anim);

	/**
	 * 设置取消。
	 */
	@SimpleFunction
	void 置取消();

	/**
	 * 设置允许随机颜色。
	 *
	 * @param allowRandomColor 允许随机颜色。
	 */
	@SimpleFunction
	void 置允许随机颜色(boolean allowRandomColor);

	/**
	 * 设置动画时长。
	 *
	 * @param durationMs 动画持续时间，单位毫秒。
	 */
	@SimpleFunction
	void 置动画时长(int durationMs);

	/**
	 * 设置大闪光颜色。
	 *
	 * @param color 颜色。
	 */
	@SimpleFunction
	void 置大闪光颜色(int color);

	/**
	 * 设置单击动画时长。
	 *
	 * @param durationMs 动画持续时间，单位毫秒。
	 */
	@SimpleFunction
	void 置单击动画时长(int durationMs);

	/**
	 * 启用闪烁。
	 *
	 * @param enable 启用。
	 */
	@SimpleFunction
	void 启用闪烁(boolean enable);

	/**
	 * 设置闪光数量。
	 *
	 * @param count 数量。
	 */
	@SimpleFunction
	void 置闪光数量(int count);

	/**
	 * 设置闪光距离倍数。
	 *
	 * @param multiple 倍数。
	 */
	@SimpleFunction
	void 置闪光距离倍数(float multiple);

	/**
	 * 设置闪光旋转角度。
	 *
	 * @param angle 角度。
	 */
	@SimpleFunction
	void 置闪光旋转角度(float angle);

	/**
	 * 设置小闪光颜色。
	 *
	 * @param color 颜色。
	 */
	@SimpleFunction
	void 置小闪光颜色(int color);

	/**
	 * 设置小闪光关角度。
	 *
	 * @param angle 角度。
	 */
	@SimpleFunction
	void 置小闪光关角度(float angle);

	/**
	 * 设置闪光尺寸。
	 *
	 * @param size 尺寸。
	 */
	@SimpleFunction
	void 置闪光尺寸(int size);

	/**
	 * 设置着色颜色。
	 *
	 * @param color 颜色。
	 */
	@SimpleFunction
	void 置着色颜色(int color);

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
	 * 设置点赞动画使用的图标遮罩。
	 *
	 * @param image 图标资源标识、资产文件名或外部文件路径。
	 */
	@SimpleFunction
	void 置图片(String image);

	/**
	 * 选中状态改变时触发。
	 *
	 * @param checked 是否选中。
	 */
	@SimpleEvent
	void 选中改变(boolean checked);
}
