package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * XUI弧线加载组件。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI弧线加载 extends XUI视图组件 {

	/**
	 * 设置加载颜色。
	 *
	 * @param color 颜色。
	 */
	@SimpleFunction
	void 置加载颜色(int color);

	/**
	 * 取得加载颜色。
	 *
	 * @return 加载颜色。
	 */
	@SimpleFunction
	int 取加载颜色();

	/**
	 * 设置图标的缩小比例。
	 *
	 * @param iconScale 图标缩放。
	 */
	@SimpleFunction
	void 置图标缩放(float iconScale);

	/**
	 * 取得图标缩放。
	 *
	 * @return 图标缩放。
	 */
	@SimpleFunction
	float 取图标缩放();

	/**
	 * 开始旋转。
	 */
	@SimpleFunction
	void 开始();

	/**
	 * 停止旋转。
	 */
	@SimpleFunction
	void 停止();

	/**
	 * 是否开始。
	 *
	 * @return 是否开始。
	 */
	@SimpleFunction
	boolean 是否开始();

	/**
	 * 设置加载图标，使用 Simple 图片标识。
	 *
	 * @param image 图片资源标识、资产文件名或外部路径。
	 */
	@SimpleFunction
	void 置加载图标(String image);

	/**
	 * 设置每次绘制推进的角度，值越大旋转越快。
	 *
	 * @param degrees 每步推进角度，范围为 1 至 360。
	 */
	@SimpleFunction
	void 置旋转速度(int degrees);

}
