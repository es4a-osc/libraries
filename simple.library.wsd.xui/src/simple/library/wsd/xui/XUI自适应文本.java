package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * XUI自适应文本组件。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI自适应文本 extends XUI文本组件 {

	/**
	 * 设置行数。
	 *
	 * @param lines 行数。
	 */
	@SimpleFunction
	void 置行数(int lines);

	/**
	 * 设置最大行数。
	 *
	 * @param maxLines 最大行数。
	 */
	@SimpleFunction
	void 置最大行数(int maxLines);

	/**
	 * 是否启用适应。
	 *
	 * @return 是否启用适应。
	 */
	@SimpleFunction
	boolean 是否启用适应();

	/**
	 * 设置是否开启自适应字体大小的功能。
	 *
	 * @param enableFit 是否开启。
	 */
	@SimpleFunction
	void 置启用适应(boolean enableFit);

	/**
	 * 获取最大字体大小【单位： px】。
	 *
	 * @return 最大字体大小【单位： px】。
	 */
	@SimpleFunction
	float 取最大文本尺寸();

	/**
	 * 设置自适应的最大字体大小【单位： sp】。
	 *
	 * @param size 字体大小。
	 */
	@SimpleFunction
	void 置最大文本尺寸(float size);

	/**
	 * 设置自适应的最大字体大小。
	 *
	 * @param unit 单位：px or sp。
	 * @param size 字体大小。
	 */
	@SimpleFunction
	void 置最大文本尺寸(int unit, float size);

	/**
	 * 获取最小字体大小【单位： px】。
	 *
	 * @return 最小字体大小【单位： px】。
	 */
	@SimpleFunction
	float 取最小文本尺寸();

	/**
	 * 设置自适应的最小字体大小【单位：sp】。
	 *
	 * @param minSize 字体大小。
	 */
	@SimpleFunction
	void 置最小文本尺寸(int minSize);

	/**
	 * 设置自适应的最小字体大小。
	 *
	 * @param unit 单位：px or sp。
	 * @param minSize 字体大小。
	 */
	@SimpleFunction
	void 置最小文本尺寸(int unit, float minSize);

	/**
	 * 取得精度。
	 *
	 * @return 精度。
	 */
	@SimpleFunction
	float 取精度();

	/**
	 * 设置自适应的调整精度（精度值越小，越精准，耗时越长）。
	 *
	 * @param precision 字体调整精度。
	 */
	@SimpleFunction
	void 置精度(float precision);
}
