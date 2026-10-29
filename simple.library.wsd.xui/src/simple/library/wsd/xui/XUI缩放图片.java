package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleDataElement;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleEvent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * XUI缩放图片组件。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI缩放图片 extends XUI视图组件 {

	/**
	 * 设置旋转到。
	 *
	 * @param rotationDegree 旋转角度。
	 */
	@SimpleFunction
	void 置旋转到(float rotationDegree);

	/**
	 * 取得最小缩放。
	 *
	 * @return 最小缩放。
	 */
	@SimpleFunction
	float 取最小缩放();

	/**
	 * 取得中间缩放。
	 *
	 * @return 中间缩放。
	 */
	@SimpleFunction
	float 取中间缩放();

	/**
	 * 取得最大缩放。
	 *
	 * @return 最大缩放。
	 */
	@SimpleFunction
	float 取最大缩放();

	/**
	 * 取得缩放。
	 *
	 * @return 缩放。
	 */
	@SimpleFunction
	float 取缩放();

	/**
	 * 设置图片滑到边缘时是否允许父容器拦截触摸事件。
	 *
	 * @param allow 是否允许父容器拦截触摸事件。
	 */
	@SimpleFunction
	void 置允许边缘拦截(boolean allow);

	/**
	 * 设置最小缩放。
	 *
	 * @param minimumScale 最小缩放。
	 */
	@SimpleFunction
	void 置最小缩放(float minimumScale);

	/**
	 * 设置中间缩放。
	 *
	 * @param mediumScale 中间缩放。
	 */
	@SimpleFunction
	void 置中间缩放(float mediumScale);

	/**
	 * 设置最大缩放。
	 *
	 * @param maximumScale 最大缩放。
	 */
	@SimpleFunction
	void 置最大缩放(float maximumScale);

	/**
	 * 设置缩放级别。
	 *
	 * @param minimumScale 最小缩放。
	 * @param mediumScale 中间缩放。
	 * @param maximumScale 最大缩放。
	 */
	@SimpleFunction
	void 置缩放级别(float minimumScale, float mediumScale, float maximumScale);

	/**
	 * 设置缩放。
	 *
	 * @param scale 缩放。
	 */
	@SimpleFunction
	void 置缩放(float scale);

	/**
	 * 设置缩放。
	 *
	 * @param scale 缩放。
	 * @param animate 动画。
	 */
	@SimpleFunction
	void 置缩放(float scale, boolean animate);

	/**
	 * 设置缩放。
	 *
	 * @param scale 缩放。
	 * @param focalX 缩放中心的横坐标，使用组件坐标系。
	 * @param focalY 缩放中心的纵坐标，使用组件坐标系。
	 * @param animate 动画。
	 */
	@SimpleFunction
	void 置缩放(float scale, float focalX, float focalY, boolean animate);

	/**
	 * 设置可缩放。
	 *
	 * @param zoomable 可缩放。
	 */
	@SimpleFunction
	void 置可缩放(boolean zoomable);

	/**
	 * 设置缩放过渡时长。
	 *
	 * @param milliseconds 动画持续时间，单位毫秒。
	 */
	@SimpleFunction
	void 置缩放过渡时长(int milliseconds);

	/**
	 * 设置图片，空文本清除图片。
	 *
	 * @param image 图片资源标识、资产文件名或外部文件路径。
	 */
	@SimpleFunction
	void 置图片(String image);

	/**
	 * 图片缩放手势更新时触发。
	 *
	 * @param factor 本次缩放倍数。
	 * @param focusX 缩放焦点横坐标，单位为物理像素。
	 * @param focusY 缩放焦点纵坐标，单位为物理像素。
	 */
	@SimpleEvent
	void 缩放改变(float factor, float focusX, float focusY);

	/**
	 * 图片内容被单击时触发。
	 *
	 * @param x 图片内部横向位置比例，0~1。
	 * @param y 图片内部纵向位置比例，0~1。
	 */
	@SimpleEvent
	void 图片被单击(float x, float y);

	/**
	 * 图片内容之外的视图区域被单击时触发。
	 */
	@SimpleEvent
	void 图片外被单击();

	/**
	 * 恢复图片的初始缩放、旋转和平移。
	 */
	@SimpleFunction
	void 重置缩放();

	/**
	 * 取得视图像素坐标的左、上、右、下四项图片边界，未显示图片时返回空数组。
	 *
	 * @return 视图像素坐标的左、上、右、下四项图片边界，未显示图片时返回空数组。
	 */
	@SimpleFunction
	float[] 取显示区域();

	/**
	 * 设置图片在视图中的基础适配模式，随后缩放手势以该模式为基础。
	 *
	 * @param mode 图片缩放模式常量；不接受 MATRIX。
	 */
	@SimpleFunction
	void 置图片缩放模式(int mode);

	/**
	 * 拉伸填满视图，不保持原始宽高比。
	 */
	@SimpleDataElement
	int 图片缩放_拉伸 = 1;

	/**
	 * 保持比例并靠起点适配。
	 */
	@SimpleDataElement
	int 图片缩放_适应起点 = 2;

	/**
	 * 保持比例并居中适配。
	 */
	@SimpleDataElement
	int 图片缩放_适应中心 = 3;

	/**
	 * 保持比例并靠终点适配。
	 */
	@SimpleDataElement
	int 图片缩放_适应终点 = 4;

	/**
	 * 按原始尺寸居中。
	 */
	@SimpleDataElement
	int 图片缩放_居中 = 5;

	/**
	 * 保持比例铺满并裁剪。
	 */
	@SimpleDataElement
	int 图片缩放_居中裁剪 = 6;

	/**
	 * 居中显示，过大时缩小。
	 */
	@SimpleDataElement
	int 图片缩放_居中缩小 = 7;

	/**
	 * 图片平移、旋转、适配或缩放导致显示区域改变时触发。
	 *
	 * @param left 显示区域左边界，单位为视图物理像素。
	 * @param top 显示区域顶边界。
	 * @param right 显示区域右边界。
	 * @param bottom 显示区域底边界。
	 */
	@SimpleEvent
	void 显示区域改变(float left, float top, float right, float bottom);

	/**
	 * 将当前视图内可见的图片区域保存为 PNG，视图须已完成布局。
	 *
	 * @param path 输出路径，父目录须存在；相对路径基于应用内部目录。
	 */
	@SimpleFunction
	void 保存可见图片(String path);

}
