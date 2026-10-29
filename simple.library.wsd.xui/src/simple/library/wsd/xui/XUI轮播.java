package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleDataElement;
import simple.runtime.annotations.SimpleEvent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.UsesPermissions;

/**
 * XUI轮播组件。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
@UsesPermissions(permissionNames = "android.permission.INTERNET")
public interface XUI轮播 extends XUI视图组件 {

	/**
	 * 设置自动播放。
	 *
	 * @param isAutoPlaying 是否自动播放。
	 */
	@SimpleFunction
	void 置自动播放(boolean isAutoPlaying);

	/**
	 * 是否播放。
	 *
	 * @return 是否播放。
	 */
	@SimpleFunction
	boolean 是否播放();

	/**
	 * 设置显示指示。
	 *
	 * @param showIndicator 显示指示。
	 */
	@SimpleFunction
	void 置显示指示(boolean showIndicator);

	/**
	 * 设置中间缩放。
	 *
	 * @param centerScale 中间缩放。
	 */
	@SimpleFunction
	void 置中间缩放(float centerScale);

	/**
	 * 设置移动速度。
	 *
	 * @param moveSpeed 移动速度。
	 */
	@SimpleFunction
	void 置移动速度(float moveSpeed);

	/**
	 * 设置项目间距。
	 *
	 * @param itemSpace 项目间距。
	 */
	@SimpleFunction
	void 置项目间距(int itemSpace);

	/**
	 * 设置轮播间隔时间。
	 *
	 * @param autoPlayDuration 时间毫秒。
	 */
	@SimpleFunction
	void 置自动播放时长(int autoPlayDuration);

	/**
	 * 替换文本条目。
	 *
	 * @param items 项目文本数组；数组内不得含有空引用。
	 */
	@SimpleFunction
	void 置项目数组(String[] items);

	/**
	 * 项目被单击时触发。
	 *
	 * @param index 从 0 开始的项目索引。
	 * @param text 文本内容。
	 */
	@SimpleEvent
	void 项目被单击(int index, String text);

	/**
	 * 回收式轮播指示器位置改变时触发。
	 *
	 * @param index 从 0 开始的项目索引。
	 */
	@SimpleEvent
	void 页面被选择(int index);

	/**
	 * 设置固定尺寸的图片条目，支持本地及网络地址。
	 *
	 * @param images 图片标识数组。
	 * @param width 单个条目宽度，单位为物理像素。
	 * @param height 单个条目高度，单位为物理像素。
	 */
	@SimpleFunction
	void 置图片项目(String[] images, int width, int height);

	/**
	 * 取得从零开始的当前页，无数据时返回 -1。
	 *
	 * @return 从零开始的当前页，无数据时返回 -1。
	 */
	@SimpleFunction
	int 取当前页();

	/**
	 * 切换到指定页面。
	 *
	 * @param index 从零开始的页码。
	 * @param smooth 是否平滑切换。
	 */
	@SimpleFunction
	void 置当前页(int index, boolean smooth);

	/**
	 * 设置轮播排列方向。
	 *
	 * @param orientation 0 横向；1 纵向。
	 */
	@SimpleFunction
	void 置排列方向(int orientation);

	/**
	 * 横向排列。
	 */
	@SimpleDataElement
	int 方向_横向 = 0;

	/**
	 * 纵向排列。
	 */
	@SimpleDataElement
	int 方向_纵向 = 1;

}
