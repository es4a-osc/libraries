package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleEvent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.UsesPermissions;

/**
 * XUI九宫格图片组件。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
@UsesPermissions(permissionNames = "android.permission.INTERNET")
public interface XUI九宫格图片 extends XUI视图组件 {

	/**
	 * 设置宫格间距。
	 *
	 * @param gap 宫格间距 px。
	 */
	@SimpleFunction
	void 置间隔(int gap);

	/**
	 * 设置显示风格。
	 *
	 * @param showStyle 显示风格。
	 */
	@SimpleFunction
	void 置显示样式(int showStyle);

	/**
	 * 设置只有一张图片时的尺寸大小。
	 *
	 * @param singleImgSize 单张图片的尺寸大小。
	 */
	@SimpleFunction
	void 置单图尺寸(int singleImgSize);

	/**
	 * 设置最大图片数。
	 *
	 * @param maxSize 最大图片数。
	 */
	@SimpleFunction
	void 置最大尺寸(int maxSize);

	/**
	 * 替换九宫格图片。
	 *
	 * @param images 图片标识或网络地址数组。
	 */
	@SimpleFunction
	void 置图片数组(String[] images);

	/**
	 * 网格图片被单击时触发。
	 *
	 * @param index 从 0 开始的项目索引。
	 * @param image 图片标识或网络地址。
	 */
	@SimpleEvent
	void 图片被单击(int index, String image);

	/**
	 * 用户长按单张图片时触发。
	 *
	 * @param index 从 0 开始的图片索引。
	 * @param image 调用方传入的原始图片标识或地址。
	 */
	@SimpleEvent
	void 图片被长按(int index, String image);

	/**
	 * 一轮数据内该位置图片首次完成加载时触发。
	 *
	 * @param index 从 0 开始的图片索引。
	 * @param image 调用方传入的原始图片标识或地址。
	 */
	@SimpleEvent
	void 图片加载完成(int index, String image);

	/**
	 * 一轮数据内该位置图片首次加载失败时触发。
	 *
	 * @param index 从 0 开始的图片索引。
	 * @param image 调用方传入的原始图片标识或地址。
	 */
	@SimpleEvent
	void 图片加载失败(int index, String image);

	/**
	 * 设置后续加载使用的占位及失败图片，空文本表示不显示对应图片。
	 *
	 * @param loading 本地图片或资源标识。
	 * @param error 本地图片或资源标识。
	 */
	@SimpleFunction
	void 置占位图片(String loading, String error);

}
