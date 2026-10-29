package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleEvent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * XUI评分组件。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI评分 extends XUI视图组件 {

	/**
	 * 设置星数。
	 *
	 * @param numStars 星数。
	 */
	@SimpleFunction
	void 置星数(int numStars);

	/**
	 * 取得星数。
	 *
	 * @return 星数。
	 */
	@SimpleFunction
	int 取星数();

	/**
	 * 设置评分。
	 *
	 * @param rating 评分。
	 */
	@SimpleFunction
	void 置评分(float rating);

	/**
	 * 取得评分。
	 *
	 * @return 评分。
	 */
	@SimpleFunction
	float 取评分();

	/**
	 * 设置星宽度。
	 *
	 * @param starWidth 星宽度。
	 */
	@SimpleFunction
	void 置星宽度(int starWidth);

	/**
	 * 取得星宽度。
	 *
	 * @return 星宽度。
	 */
	@SimpleFunction
	int 取星宽度();

	/**
	 * 设置星高度。
	 *
	 * @param starHeight 星高度。
	 */
	@SimpleFunction
	void 置星高度(int starHeight);

	/**
	 * 取得星高度。
	 *
	 * @return 星高度。
	 */
	@SimpleFunction
	int 取星高度();

	/**
	 * 设置星内边距。
	 *
	 * @param ratingPadding 评分内边距。
	 */
	@SimpleFunction
	void 置星内边距(int ratingPadding);

	/**
	 * 取得星内边距。
	 *
	 * @return 星内边距。
	 */
	@SimpleFunction
	int 取星内边距();

	/**
	 * 设置最小星数。
	 *
	 * @param minimumStars 最小星数。
	 */
	@SimpleFunction
	void 置最小星数(float minimumStars);

	/**
	 * 是否指示。
	 *
	 * @return 是否指示。
	 */
	@SimpleFunction
	boolean 是否指示();

	/**
	 * 设置是否指示。
	 *
	 * @param indicator 指示。
	 */
	@SimpleFunction
	void 置是否指示(boolean indicator);

	/**
	 * 是否可滑动。
	 *
	 * @return 是否可滑动。
	 */
	@SimpleFunction
	boolean 是否可滑动();

	/**
	 * 设置可滑动。
	 *
	 * @param scrollable 可滑动。
	 */
	@SimpleFunction
	void 置可滑动(boolean scrollable);

	/**
	 * 是否可单击。
	 *
	 * @return 是否可单击。
	 */
	@SimpleFunction
	boolean 是否可单击();

	/**
	 * 设置可单击。
	 *
	 * @param clickable 可单击。
	 */
	@SimpleFunction
	void 置可单击(boolean clickable);

	/**
	 * 设置清除评分启用。
	 *
	 * @param enabled 启用。
	 */
	@SimpleFunction
	void 置清除评分启用(boolean enabled);

	/**
	 * 是否清除评分启用。
	 *
	 * @return 是否清除评分启用。
	 */
	@SimpleFunction
	boolean 是否清除评分启用();

	/**
	 * 取得步长尺寸。
	 *
	 * @return 步长尺寸。
	 */
	@SimpleFunction
	float 取步长尺寸();

	/**
	 * 设置步长尺寸。
	 *
	 * @param stepSize 步长尺寸。
	 */
	@SimpleFunction
	void 置步长尺寸(float stepSize);

	/**
	 * 设置为空图像，使用 Simple 图片标识。
	 *
	 * @param image 图片资源标识、资产文件名或外部路径。
	 */
	@SimpleFunction
	void 置为空图像(String image);

	/**
	 * 设置实心图像，使用 Simple 图片标识。
	 *
	 * @param image 图片资源标识、资产文件名或外部路径。
	 */
	@SimpleFunction
	void 置实心图像(String image);

	/**
	 * 评分改变时触发。
	 *
	 * @param rating 当前评分。
	 */
	@SimpleEvent
	void 评分改变(float rating);
}
