package simple.library.wsd.mpandroidchart;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.SimpleProperty;

/**
 * MPAC饼图样式；应用到图表时复制配置，之后修改此对象不影响已有图表。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class MPAC饼图样式 {

	/**
	 * 是否显示饼图中心圆孔。
	 */
	boolean hole = true;

	/**
	 * ARGB 颜色。
	 */
	int holeColor = -1;

	/**
	 * 圆孔相对图半径的百分比。
	 */
	float holeRadius = 50f;

	/**
	 * 透明环相对图半径的百分比，应不小于圆孔半径。
	 */
	float transparentRadius = 55f;

	/**
	 * ARGB 颜色。
	 */
	int transparentColor = -1;

	/**
	 * 透明环透明度，0 至 255。
	 */
	int transparentAlpha = 105;

	/**
	 * 中心文字，可含换行。
	 */
	String centerText = "";

	/**
	 * 中心文字大小，单位 SP。
	 */
	float centerSize = 12f;

	/**
	 * ARGB 颜色。
	 */
	int centerColor = -16777216;

	/**
	 * 中心文字区域相对圆孔的百分比。
	 */
	float centerRadius = 100f;

	/**
	 * 是否显示扇区名称。
	 */
	boolean entryLabels = true;

	/**
	 * ARGB 颜色。
	 */
	int entryColor = -1;

	/**
	 * 扇区名称大小，单位 SP。
	 */
	float entrySize = 13f;

	/**
	 * 是否将数值标注传给格式器前换算为百分比。
	 */
	boolean percent = false;

	/**
	 * 是否绘制圆角饼块。
	 */
	boolean rounded = false;

	/**
	 * 是否在圆孔下绘制完整饼块。
	 */
	boolean underHole = false;

	/**
	 * 饼图绘制角度，90 至 360。
	 */
	float maxAngle = 360f;

	/**
	 * 最小扇区角度，0 至 90。
	 */
	float minAngle = 0f;

	/**
	 * 创建默认配置。
	 */
	public MPAC饼图样式() {
	}

	/**
	 * 是否显示饼图中心圆孔。
	 *
	 * @return 是否显示饼图中心圆孔。
	 */
	@SimpleProperty
	public boolean 显示圆孔() {
		return hole;
	}

	/**
	 * 是否显示饼图中心圆孔。
	 *
	 * @param value 是否显示饼图中心圆孔。
	 */
	@SimpleProperty
	public void 显示圆孔(boolean value) {
		hole = value;
	}

	/**
	 * ARGB 颜色。
	 *
	 * @return ARGB 颜色。
	 */
	@SimpleProperty
	public int 圆孔颜色() {
		return holeColor;
	}

	/**
	 * ARGB 颜色。
	 *
	 * @param value ARGB 颜色。
	 */
	@SimpleProperty
	public void 圆孔颜色(int value) {
		holeColor = value;
	}

	/**
	 * 圆孔相对图半径的百分比。
	 *
	 * @return 圆孔相对图半径的百分比。
	 */
	@SimpleProperty
	public float 圆孔半径() {
		return holeRadius;
	}

	/**
	 * 圆孔相对图半径的百分比。
	 *
	 * @param value 圆孔相对图半径的百分比。
	 * @throws IllegalArgumentException 数值、文本或数组不符合此属性约束。
	 */
	@SimpleProperty
	public void 圆孔半径(float value) {
		ChartChecks.range(value, 0f, 100f);
		holeRadius = value;
	}

	/**
	 * 透明环相对图半径的百分比，应不小于圆孔半径。
	 *
	 * @return 透明环相对图半径的百分比，应不小于圆孔半径。
	 */
	@SimpleProperty
	public float 透明环半径() {
		return transparentRadius;
	}

	/**
	 * 透明环相对图半径的百分比，应不小于圆孔半径。
	 *
	 * @param value 透明环相对图半径的百分比，应不小于圆孔半径。
	 * @throws IllegalArgumentException 数值、文本或数组不符合此属性约束。
	 */
	@SimpleProperty
	public void 透明环半径(float value) {
		ChartChecks.range(value, 0f, 100f);
		transparentRadius = value;
	}

	/**
	 * ARGB 颜色。
	 *
	 * @return ARGB 颜色。
	 */
	@SimpleProperty
	public int 透明环颜色() {
		return transparentColor;
	}

	/**
	 * ARGB 颜色。
	 *
	 * @param value ARGB 颜色。
	 */
	@SimpleProperty
	public void 透明环颜色(int value) {
		transparentColor = value;
	}

	/**
	 * 透明环透明度，0 至 255。
	 *
	 * @return 透明环透明度，0 至 255。
	 */
	@SimpleProperty
	public int 透明环透明度() {
		return transparentAlpha;
	}

	/**
	 * 透明环透明度，0 至 255。
	 *
	 * @param value 透明环透明度，0 至 255。
	 * @throws IllegalArgumentException 数值、文本或数组不符合此属性约束。
	 */
	@SimpleProperty
	public void 透明环透明度(int value) {
		ChartChecks.integer(value, 0, 255);
		transparentAlpha = value;
	}

	/**
	 * 中心文字，可含换行。
	 *
	 * @return 中心文字，可含换行。
	 */
	@SimpleProperty
	public String 中心文字() {
		return centerText;
	}

	/**
	 * 中心文字，可含换行。
	 *
	 * @param value 中心文字，可含换行。
	 * @throws IllegalArgumentException 数值、文本或数组不符合此属性约束。
	 */
	@SimpleProperty
	public void 中心文字(String value) {
		ChartChecks.text(value);
		centerText = value;
	}

	/**
	 * 中心文字大小，单位 SP。
	 *
	 * @return 中心文字大小，单位 SP。
	 */
	@SimpleProperty
	public float 中心文字大小() {
		return centerSize;
	}

	/**
	 * 中心文字大小，单位 SP。
	 *
	 * @param value 中心文字大小，单位 SP。
	 * @throws IllegalArgumentException 数值、文本或数组不符合此属性约束。
	 */
	@SimpleProperty
	public void 中心文字大小(float value) {
		ChartChecks.range(value, 6f, 100f);
		centerSize = value;
	}

	/**
	 * ARGB 颜色。
	 *
	 * @return ARGB 颜色。
	 */
	@SimpleProperty
	public int 中心文字颜色() {
		return centerColor;
	}

	/**
	 * ARGB 颜色。
	 *
	 * @param value ARGB 颜色。
	 */
	@SimpleProperty
	public void 中心文字颜色(int value) {
		centerColor = value;
	}

	/**
	 * 中心文字区域相对圆孔的百分比。
	 *
	 * @return 中心文字区域相对圆孔的百分比。
	 */
	@SimpleProperty
	public float 中心文字半径() {
		return centerRadius;
	}

	/**
	 * 中心文字区域相对圆孔的百分比。
	 *
	 * @param value 中心文字区域相对圆孔的百分比。
	 * @throws IllegalArgumentException 数值、文本或数组不符合此属性约束。
	 */
	@SimpleProperty
	public void 中心文字半径(float value) {
		ChartChecks.range(value, 1f, 100f);
		centerRadius = value;
	}

	/**
	 * 是否显示扇区名称。
	 *
	 * @return 是否显示扇区名称。
	 */
	@SimpleProperty
	public boolean 显示名称() {
		return entryLabels;
	}

	/**
	 * 是否显示扇区名称。
	 *
	 * @param value 是否显示扇区名称。
	 */
	@SimpleProperty
	public void 显示名称(boolean value) {
		entryLabels = value;
	}

	/**
	 * ARGB 颜色。
	 *
	 * @return ARGB 颜色。
	 */
	@SimpleProperty
	public int 名称颜色() {
		return entryColor;
	}

	/**
	 * ARGB 颜色。
	 *
	 * @param value ARGB 颜色。
	 */
	@SimpleProperty
	public void 名称颜色(int value) {
		entryColor = value;
	}

	/**
	 * 扇区名称大小，单位 SP。
	 *
	 * @return 扇区名称大小，单位 SP。
	 */
	@SimpleProperty
	public float 名称大小() {
		return entrySize;
	}

	/**
	 * 扇区名称大小，单位 SP。
	 *
	 * @param value 扇区名称大小，单位 SP。
	 * @throws IllegalArgumentException 数值、文本或数组不符合此属性约束。
	 */
	@SimpleProperty
	public void 名称大小(float value) {
		ChartChecks.range(value, 6f, 100f);
		entrySize = value;
	}

	/**
	 * 是否将数值标注传给格式器前换算为百分比。
	 *
	 * @return 是否将数值标注传给格式器前换算为百分比。
	 */
	@SimpleProperty
	public boolean 百分比数值() {
		return percent;
	}

	/**
	 * 是否将数值标注传给格式器前换算为百分比。
	 *
	 * @param value 是否将数值标注传给格式器前换算为百分比。
	 */
	@SimpleProperty
	public void 百分比数值(boolean value) {
		percent = value;
	}

	/**
	 * 是否绘制圆角饼块。
	 *
	 * @return 是否绘制圆角饼块。
	 */
	@SimpleProperty
	public boolean 圆角饼块() {
		return rounded;
	}

	/**
	 * 是否绘制圆角饼块。
	 *
	 * @param value 是否绘制圆角饼块。
	 */
	@SimpleProperty
	public void 圆角饼块(boolean value) {
		rounded = value;
	}

	/**
	 * 是否在圆孔下绘制完整饼块。
	 *
	 * @return 是否在圆孔下绘制完整饼块。
	 */
	@SimpleProperty
	public boolean 圆孔下绘制() {
		return underHole;
	}

	/**
	 * 是否在圆孔下绘制完整饼块。
	 *
	 * @param value 是否在圆孔下绘制完整饼块。
	 */
	@SimpleProperty
	public void 圆孔下绘制(boolean value) {
		underHole = value;
	}

	/**
	 * 饼图绘制角度，90 至 360。
	 *
	 * @return 饼图绘制角度，90 至 360。
	 */
	@SimpleProperty
	public float 最大角度() {
		return maxAngle;
	}

	/**
	 * 饼图绘制角度，90 至 360。
	 *
	 * @param value 饼图绘制角度，90 至 360。
	 * @throws IllegalArgumentException 数值、文本或数组不符合此属性约束。
	 */
	@SimpleProperty
	public void 最大角度(float value) {
		ChartChecks.range(value, 90f, 360f);
		maxAngle = value;
	}

	/**
	 * 最小扇区角度，0 至 90。
	 *
	 * @return 最小扇区角度，0 至 90。
	 */
	@SimpleProperty
	public float 最小扇区角() {
		return minAngle;
	}

	/**
	 * 最小扇区角度，0 至 90。
	 *
	 * @param value 最小扇区角度，0 至 90。
	 * @throws IllegalArgumentException 数值、文本或数组不符合此属性约束。
	 */
	@SimpleProperty
	public void 最小扇区角(float value) {
		ChartChecks.range(value, 0f, 90f);
		minAngle = value;
	}

	/**
	 * 返回独立的配置副本，回调接口不复制。
	 *
	 * @return 返回独立的配置副本，回调接口不复制。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	@SimpleFunction
	public MPAC饼图样式 复制() {
		MPAC饼图样式 result = new MPAC饼图样式();
		result.hole = hole;
		result.holeColor = holeColor;
		result.holeRadius = holeRadius;
		result.transparentRadius = transparentRadius;
		result.transparentColor = transparentColor;
		result.transparentAlpha = transparentAlpha;
		result.centerText = centerText;
		result.centerSize = centerSize;
		result.centerColor = centerColor;
		result.centerRadius = centerRadius;
		result.entryLabels = entryLabels;
		result.entryColor = entryColor;
		result.entrySize = entrySize;
		result.percent = percent;
		result.rounded = rounded;
		result.underHole = underHole;
		result.maxAngle = maxAngle;
		result.minAngle = minAngle;
		return result;
	}
}
