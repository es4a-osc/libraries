package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleEvent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * XUI加载框组件。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI加载框 extends XUI视图组件 {

	/**
	 * 设置图标的缩小比例。
	 *
	 * @param iconScale 图标缩放。
	 */
	@SimpleFunction
	void 置图标缩放(float iconScale);

	/**
	 * 设置loading的图标。
	 *
	 * @param iconResId 图标资源 ID。
	 */
	@SimpleFunction
	void 置加载图标资源(int iconResId);

	/**
	 * 显示。
	 */
	@SimpleFunction
	void 显示();

	/**
	 * 隐藏。
	 */
	@SimpleFunction
	void 隐藏();

	/**
	 * 是否加载。
	 *
	 * @return 是否加载。
	 */
	@SimpleFunction
	boolean 是否加载();

	/**
	 * 设置可取消。
	 *
	 * @param cancelable 可取消。
	 */
	@SimpleFunction
	void 置可取消(boolean cancelable);

	/**
	 * 设置加载图标，使用 Simple 图片标识。
	 *
	 * @param image 图片资源标识、资产文件名或外部路径。
	 */
	@SimpleFunction
	void 置加载图标(String image);

	/**
	 * 设置加载提示文字，空文本隐藏提示。
	 *
	 * @param text 提示文字。
	 */
	@SimpleFunction
	void 置提示文本(String text);

	/**
	 * 用户取消加载时触发。
	 */
	@SimpleEvent
	void 已取消();

}
