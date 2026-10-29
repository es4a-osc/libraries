package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleDataElement;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.SimpleProperty;

/**
 * Material 风格的圆形进度组件，支持确定进度与不确定进度。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI材质进度条 extends XUI视图组件 {

	/**
	 * 设置是否使用循环动画表示尚不能确定总量的任务；切换模式时保留进度值。
	 *
	 * @param indeterminate 真表示不确定进度，假表示按进度值显示。
	 */
	@SimpleFunction
	void 置不确定进度(boolean indeterminate);

	/**
	 * 取得是否使用不确定进度。
	 *
	 * @return 是否使用循环进度动画。
	 */
	@SimpleFunction
	boolean 是否不确定进度();

	/**
	 * 取得当前进度值。
	 *
	 * @return 从零到最大进度的值。
	 */
	@SimpleProperty
	int 进度();

	/**
	 * 设置当前进度；不确定模式下保存该值，切换为确定模式后显示。
	 *
	 * @param value 从零到最大进度的值，越界时拒绝设置。
	 */
	@SimpleProperty(type = SimpleProperty.PROPERTY_TYPE_INTEGER)
	void 进度(int value);

	/**
	 * 取得最大进度。
	 *
	 * @return 大于零的最大值，默认为 100。
	 */
	@SimpleProperty
	int 最大进度();

	/**
	 * 设置最大进度，缩小范围时当前进度随之截断。
	 *
	 * @param value 大于零的最大值。
	 */
	@SimpleProperty(type = SimpleProperty.PROPERTY_TYPE_INTEGER)
	void 最大进度(int value);

	/**
	 * 取得进度样式。
	 *
	 * @return 本组件的圆形样式标识 0。
	 */
	@SimpleFunction
	int 取进度样式();

	/**
	 * 取得使用固有内边距。
	 *
	 * @return 使用固有内边距。
	 */
	@SimpleFunction
	boolean 是否使用固有内边距();

	/**
	 * 设置使用固有内边距。
	 *
	 * @param useIntrinsicPadding 使用固有内边距。
	 */
	@SimpleFunction
	void 置使用固有内边距(boolean useIntrinsicPadding);

	/**
	 * 取得显示进度背景。
	 *
	 * @return 显示进度背景。
	 */
	@SimpleFunction
	boolean 是否显示进度背景();

	/**
	 * 设置显示进度背景。
	 *
	 * @param show 显示。
	 */
	@SimpleFunction
	void 置显示进度背景(boolean show);

	/**
	 * 设置确定进度的着色。
	 *
	 * @param color ARGB 颜色。
	 */
	@SimpleFunction
	void 置进度颜色(int color);

	/**
	 * 设置不确定进度的着色。
	 *
	 * @param color ARGB 颜色。
	 */
	@SimpleFunction
	void 置等待颜色(int color);

	/**
	 * 设置进度背景的着色。
	 *
	 * @param color ARGB 颜色。
	 */
	@SimpleFunction
	void 置进度背景颜色(int color);

	/**
	 * 设置次级进度的着色。
	 *
	 * @param color ARGB 颜色。
	 */
	@SimpleFunction
	void 置次级进度颜色(int color);

	/**
	 * 切换环形或水平外观，保留进度、缓冲进度、模式及着色。
	 *
	 * @param style 进度样式常量。
	 */
	@SimpleFunction
	void 置进度样式(int style);

	/**
	 * 设置缓冲等次级任务进度；值须位于零和最大进度之间。
	 *
	 * @param value 次级进度值。
	 */
	@SimpleFunction
	void 置次级进度(int value);

	/**
	 * 取得设置的次级任务进度。
	 *
	 * @return 设置的次级任务进度。
	 */
	@SimpleFunction
	int 取次级进度();

	/**
	 * 环形进度。
	 */
	@SimpleDataElement
	int 进度样式_环形 = 0;

	/**
	 * 水平进度。
	 */
	@SimpleDataElement
	int 进度样式_水平 = 1;

}
