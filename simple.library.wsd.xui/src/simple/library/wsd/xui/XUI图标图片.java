package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleDataElement;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * XUI图标图片组件。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI图标图片 extends XUI视图组件 {

	/**
	 * 设置是否显示图标。
	 *
	 * @param isShowIcon 是否显示图标。
	 */
	@SimpleFunction
	void 置是否显示图标(boolean isShowIcon);

	/**
	 * 设置图标的缩放比例。
	 *
	 * @param iconScale 图标缩放。
	 */
	@SimpleFunction
	void 置图标缩放(float iconScale);

	/**
	 * 设置图片，空文本清除图片。
	 *
	 * @param image 图片资源标识、资产文件名或外部文件路径。
	 */
	@SimpleFunction
	void 置图片(String image);

	/**
	 * 设置图标图像，使用 Simple 图片标识。
	 *
	 * @param image 图片资源标识、资产文件名或外部路径。
	 */
	@SimpleFunction
	void 置图标图像(String image);

	/**
	 * 设置图片与视图边界之间的缩放方式。
	 *
	 * @param mode 图片缩放模式常量。
	 */
	@SimpleFunction
	void 置图片缩放模式(int mode);

	/**
	 * 矩阵显示图片。
	 */
	@SimpleDataElement
	int 缩放模式_矩阵 = 0;

	/**
	 * 拉伸显示图片。
	 */
	@SimpleDataElement
	int 缩放模式_拉伸 = 1;

	/**
	 * 适应起点显示图片。
	 */
	@SimpleDataElement
	int 缩放模式_适应起点 = 2;

	/**
	 * 适应居中显示图片。
	 */
	@SimpleDataElement
	int 缩放模式_适应居中 = 3;

	/**
	 * 适应末尾显示图片。
	 */
	@SimpleDataElement
	int 缩放模式_适应末尾 = 4;

	/**
	 * 居中显示图片。
	 */
	@SimpleDataElement
	int 缩放模式_居中 = 5;

	/**
	 * 居中裁剪显示图片。
	 */
	@SimpleDataElement
	int 缩放模式_居中裁剪 = 6;

	/**
	 * 居中缩小显示图片。
	 */
	@SimpleDataElement
	int 缩放模式_居中缩小 = 7;

}
