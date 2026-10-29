package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * XUI旋转加载组件。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI旋转加载 extends XUI视图组件 {

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
}
