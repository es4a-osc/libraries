package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * XUI透明图片按钮组件。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI透明图片按钮 extends XUI视图组件 {

	/**
	 * 设置按压时是否改变透明度。
	 *
	 * @param changeAlphaWhenPress 按压时是否改变透明度。
	 */
	@SimpleFunction
	void 置按压透明反馈(boolean changeAlphaWhenPress);

	/**
	 * 设置禁用时是否改变透明度。
	 *
	 * @param changeAlphaWhenDisable 禁用时是否改变透明度。
	 */
	@SimpleFunction
	void 置禁用透明反馈(boolean changeAlphaWhenDisable);

	/**
	 * 设置图片，空文本清除图片。
	 *
	 * @param image 图片资源标识、资产文件名或外部文件路径。
	 */
	@SimpleFunction
	void 置图片(String image);
}
