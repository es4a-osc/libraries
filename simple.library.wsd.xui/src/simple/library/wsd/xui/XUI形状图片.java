package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * XUI形状图片组件。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI形状图片 extends XUI图片组件 {

	/**
	 * 设置着色颜色。
	 *
	 * @param color 颜色。
	 */
	@SimpleFunction
	void 置着色颜色(int color);

	/**
	 * 设置图标图像，使用 Simple 图片标识。
	 *
	 * @param image 图片资源标识、资产文件名或外部路径。
	 */
	@SimpleFunction
	void 置图标图像(String image);

}
