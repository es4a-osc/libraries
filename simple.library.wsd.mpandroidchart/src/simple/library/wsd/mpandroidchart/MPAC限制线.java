package simple.library.wsd.mpandroidchart;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.SimpleProperty;

/**
 * MPAC限制线；应用到图表时复制配置，之后修改此对象不影响已有图表。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class MPAC限制线 {

	/**
	 * 轴上的限制值，支持负数。
	 */
	float position = 0f;

	/**
	 * 限制线旁的文字。
	 */
	String label = "";

	/**
	 * ARGB 颜色。
	 */
	int color = -65536;

	/**
	 * 线宽，单位 DIP，0.2 至 12。
	 */
	float width = 2f;

	/**
	 * ARGB 颜色。
	 */
	int textColor = -16777216;

	/**
	 * 文字大小，单位 SP。
	 */
	float textSize = 10f;

	/**
	 * 0 右上，1 右下，2 左上，3 左下。
	 */
	int labelPosition = 0;

	/**
	 * 虚线段长度，0 表示实线，单位 DIP。
	 */
	float dashLength = 0f;

	/**
	 * 虚线空白长度，单位 DIP。
	 */
	float dashGap = 4f;

	/**
	 * 虚线起始偏移，单位 DIP。
	 */
	float dashPhase = 0f;

	/**
	 * 文字横向偏移，单位 DIP。
	 */
	float xOffset = 4f;

	/**
	 * 文字纵向偏移，单位 DIP。
	 */
	float yOffset = 4f;

	/**
	 * 是否绘制此限制线。
	 */
	boolean enabled = true;

	/**
	 * 创建默认配置。
	 */
	public MPAC限制线() {
	}

	/**
	 * 轴上的限制值，支持负数。
	 *
	 * @return 轴上的限制值，支持负数。
	 */
	@SimpleProperty
	public float 坐标() {
		return position;
	}

	/**
	 * 轴上的限制值，支持负数。
	 *
	 * @param value 轴上的限制值，支持负数。
	 * @throws IllegalArgumentException 数值、文本或数组不符合此属性约束。
	 */
	@SimpleProperty
	public void 坐标(float value) {
		ChartChecks.range(value, -1e+30f, 1e+30f);
		position = value;
	}

	/**
	 * 限制线旁的文字。
	 *
	 * @return 限制线旁的文字。
	 */
	@SimpleProperty
	public String 标签() {
		return label;
	}

	/**
	 * 限制线旁的文字。
	 *
	 * @param value 限制线旁的文字。
	 * @throws IllegalArgumentException 数值、文本或数组不符合此属性约束。
	 */
	@SimpleProperty
	public void 标签(String value) {
		ChartChecks.text(value);
		label = value;
	}

	/**
	 * ARGB 颜色。
	 *
	 * @return ARGB 颜色。
	 */
	@SimpleProperty
	public int 线颜色() {
		return color;
	}

	/**
	 * ARGB 颜色。
	 *
	 * @param value ARGB 颜色。
	 */
	@SimpleProperty
	public void 线颜色(int value) {
		color = value;
	}

	/**
	 * 线宽，单位 DIP，0.2 至 12。
	 *
	 * @return 线宽，单位 DIP，0.2 至 12。
	 */
	@SimpleProperty
	public float 线宽() {
		return width;
	}

	/**
	 * 线宽，单位 DIP，0.2 至 12。
	 *
	 * @param value 线宽，单位 DIP，0.2 至 12。
	 * @throws IllegalArgumentException 数值、文本或数组不符合此属性约束。
	 */
	@SimpleProperty
	public void 线宽(float value) {
		ChartChecks.range(value, 0.2f, 12f);
		width = value;
	}

	/**
	 * ARGB 颜色。
	 *
	 * @return ARGB 颜色。
	 */
	@SimpleProperty
	public int 文字颜色() {
		return textColor;
	}

	/**
	 * ARGB 颜色。
	 *
	 * @param value ARGB 颜色。
	 */
	@SimpleProperty
	public void 文字颜色(int value) {
		textColor = value;
	}

	/**
	 * 文字大小，单位 SP。
	 *
	 * @return 文字大小，单位 SP。
	 */
	@SimpleProperty
	public float 文字大小() {
		return textSize;
	}

	/**
	 * 文字大小，单位 SP。
	 *
	 * @param value 文字大小，单位 SP。
	 * @throws IllegalArgumentException 数值、文本或数组不符合此属性约束。
	 */
	@SimpleProperty
	public void 文字大小(float value) {
		ChartChecks.range(value, 6f, 100f);
		textSize = value;
	}

	/**
	 * 0 右上，1 右下，2 左上，3 左下。
	 *
	 * @return 0 右上，1 右下，2 左上，3 左下。
	 */
	@SimpleProperty
	public int 标签位置() {
		return labelPosition;
	}

	/**
	 * 0 右上，1 右下，2 左上，3 左下。
	 *
	 * @param value 0 右上，1 右下，2 左上，3 左下。
	 * @throws IllegalArgumentException 数值、文本或数组不符合此属性约束。
	 */
	@SimpleProperty
	public void 标签位置(int value) {
		ChartChecks.integer(value, 0, 3);
		labelPosition = value;
	}

	/**
	 * 虚线段长度，0 表示实线，单位 DIP。
	 *
	 * @return 虚线段长度，0 表示实线，单位 DIP。
	 */
	@SimpleProperty
	public float 虚线长度() {
		return dashLength;
	}

	/**
	 * 虚线段长度，0 表示实线，单位 DIP。
	 *
	 * @param value 虚线段长度，0 表示实线，单位 DIP。
	 * @throws IllegalArgumentException 数值、文本或数组不符合此属性约束。
	 */
	@SimpleProperty
	public void 虚线长度(float value) {
		ChartChecks.range(value, 0f, 100000f);
		dashLength = value;
	}

	/**
	 * 虚线空白长度，单位 DIP。
	 *
	 * @return 虚线空白长度，单位 DIP。
	 */
	@SimpleProperty
	public float 虚线间隔() {
		return dashGap;
	}

	/**
	 * 虚线空白长度，单位 DIP。
	 *
	 * @param value 虚线空白长度，单位 DIP。
	 * @throws IllegalArgumentException 数值、文本或数组不符合此属性约束。
	 */
	@SimpleProperty
	public void 虚线间隔(float value) {
		ChartChecks.range(value, 0.1f, 100000f);
		dashGap = value;
	}

	/**
	 * 虚线起始偏移，单位 DIP。
	 *
	 * @return 虚线起始偏移，单位 DIP。
	 */
	@SimpleProperty
	public float 虚线相位() {
		return dashPhase;
	}

	/**
	 * 虚线起始偏移，单位 DIP。
	 *
	 * @param value 虚线起始偏移，单位 DIP。
	 * @throws IllegalArgumentException 数值、文本或数组不符合此属性约束。
	 */
	@SimpleProperty
	public void 虚线相位(float value) {
		ChartChecks.range(value, 0f, 100000f);
		dashPhase = value;
	}

	/**
	 * 文字横向偏移，单位 DIP。
	 *
	 * @return 文字横向偏移，单位 DIP。
	 */
	@SimpleProperty
	public float 横向偏移() {
		return xOffset;
	}

	/**
	 * 文字横向偏移，单位 DIP。
	 *
	 * @param value 文字横向偏移，单位 DIP。
	 * @throws IllegalArgumentException 数值、文本或数组不符合此属性约束。
	 */
	@SimpleProperty
	public void 横向偏移(float value) {
		ChartChecks.range(value, 0f, 100000f);
		xOffset = value;
	}

	/**
	 * 文字纵向偏移，单位 DIP。
	 *
	 * @return 文字纵向偏移，单位 DIP。
	 */
	@SimpleProperty
	public float 纵向偏移() {
		return yOffset;
	}

	/**
	 * 文字纵向偏移，单位 DIP。
	 *
	 * @param value 文字纵向偏移，单位 DIP。
	 * @throws IllegalArgumentException 数值、文本或数组不符合此属性约束。
	 */
	@SimpleProperty
	public void 纵向偏移(float value) {
		ChartChecks.range(value, 0f, 100000f);
		yOffset = value;
	}

	/**
	 * 是否绘制此限制线。
	 *
	 * @return 是否绘制此限制线。
	 */
	@SimpleProperty
	public boolean 启用() {
		return enabled;
	}

	/**
	 * 是否绘制此限制线。
	 *
	 * @param value 是否绘制此限制线。
	 */
	@SimpleProperty
	public void 启用(boolean value) {
		enabled = value;
	}

	/**
	 * 返回独立的配置副本，回调接口不复制。
	 *
	 * @return 返回独立的配置副本，回调接口不复制。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	@SimpleFunction
	public MPAC限制线 复制() {
		MPAC限制线 result = new MPAC限制线();
		result.position = position;
		result.label = label;
		result.color = color;
		result.width = width;
		result.textColor = textColor;
		result.textSize = textSize;
		result.labelPosition = labelPosition;
		result.dashLength = dashLength;
		result.dashGap = dashGap;
		result.dashPhase = dashPhase;
		result.xOffset = xOffset;
		result.yOffset = yOffset;
		result.enabled = enabled;
		return result;
	}
}
