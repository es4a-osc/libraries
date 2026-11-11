package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * XUI图标图片组件。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI图标图片 extends XUI图片组件 {

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
	 * 设置图标图像，使用 Simple 图片标识。
	 *
	 * @param image 图片资源标识、资产文件名或外部路径。
	 */
	@SimpleFunction
	void 置图标图像(String image);

}
