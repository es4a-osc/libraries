package simple.library.wsd.achartengine;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.SimpleProperty;

/**
 * ACE轴样式，应用时复制配置，不向调用方暴露绘制器。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class ACE轴样式 {

	/**
	 * 轴的建议刻度数量，0 隐藏自动刻度。
	 */
	int count = 5;

	/**
	 * 轴标签旋转角度。
	 */
	float angle = 0f;

	/**
	 * ARGB 颜色。
	 */
	int color = -16777216;

	/**
	 * 0 左，1 中，2 右；纵轴默认为靠近轴线的右对齐。
	 */
	int align = 2;

	/**
	 * 纵轴标题；横轴标题通过设置轴标题设置。
	 */
	String title = "";

	/**
	 * 创建默认配置。
	 */
	public ACE轴样式() {
	}

	/**
	 * 轴的建议刻度数量，0 隐藏自动刻度。
	 *
	 * @return 轴的建议刻度数量，0 隐藏自动刻度。
	 */
	@SimpleProperty
	public int 标签数量() {
		return count;
	}

	/**
	 * 轴的建议刻度数量，0 隐藏自动刻度。
	 *
	 * @param value 轴的建议刻度数量，0 隐藏自动刻度。
	 */
	@SimpleProperty
	public void 标签数量(int value) {
		Checks.index(value, 0, 25);
		count = value;
	}

	/**
	 * 轴标签旋转角度。
	 *
	 * @return 轴标签旋转角度。
	 */
	@SimpleProperty
	public float 标签角度() {
		return angle;
	}

	/**
	 * 轴标签旋转角度。
	 *
	 * @param value 轴标签旋转角度。
	 */
	@SimpleProperty
	public void 标签角度(float value) {
		Checks.range(value, -360, 360);
		angle = value;
	}

	/**
	 * ARGB 颜色。
	 *
	 * @return ARGB 颜色。
	 */
	@SimpleProperty
	public int 文字颜色() {
		return color;
	}

	/**
	 * ARGB 颜色。
	 *
	 * @param value ARGB 颜色。
	 */
	@SimpleProperty
	public void 文字颜色(int value) {
		color = value;
	}

	/**
	 * 0 左，1 中，2 右；纵轴默认为靠近轴线的右对齐。
	 *
	 * @return 0 左，1 中，2 右；纵轴默认为靠近轴线的右对齐。
	 */
	@SimpleProperty
	public int 文字对齐() {
		return align;
	}

	/**
	 * 0 左，1 中，2 右；纵轴默认为靠近轴线的右对齐。
	 *
	 * @param value 0 左，1 中，2 右；纵轴默认为靠近轴线的右对齐。
	 */
	@SimpleProperty
	public void 文字对齐(int value) {
		Checks.index(value, 0, 2);
		align = value;
	}

	/**
	 * 纵轴标题；横轴标题通过设置轴标题设置。
	 *
	 * @return 纵轴标题；横轴标题通过设置轴标题设置。
	 */
	@SimpleProperty
	public String 标题() {
		return title;
	}

	/**
	 * 纵轴标题；横轴标题通过设置轴标题设置。
	 *
	 * @param value 纵轴标题；横轴标题通过设置轴标题设置。
	 */
	@SimpleProperty
	public void 标题(String value) {
		Checks.text(value);
		title = value;
	}

	/**
	 * 返回独立的配置副本。
	 *
	 * @return 返回独立的配置副本。
	 */
	@SimpleFunction
	public ACE轴样式 复制() {
		ACE轴样式 result = new ACE轴样式();
		result.count = count;
		result.angle = angle;
		result.color = color;
		result.align = align;
		result.title = title;
		return result;
	}
}
