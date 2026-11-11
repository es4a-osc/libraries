package simple.library.wsd.achartengine;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.SimpleProperty;

/**
 * ACE数据组样式，应用时复制配置，不向调用方暴露绘制器。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class ACE数据组样式 {

	/**
	 * 是否绘制坐标图的数据数值。
	 */
	boolean values = true;

	/**
	 * 数据标签大小，单位 SP。
	 */
	float valueSize = 12f;

	/**
	 * 数据标签与数据点的间距，单位 DIP。
	 */
	float spacing = 6f;

	/**
	 * 0 左对齐，1 居中，2 右对齐。
	 */
	int align = 1;

	/**
	 * 折线线宽，单位 DIP。
	 */
	float lineWidth = 2f;

	/**
	 * 0 斜十字，1 圆，2 三角，3 方形，4 菱形，5 点。
	 */
	int pointStyle = 1;

	/**
	 * 是否填充折线及散点标记。
	 */
	boolean fillPoints = true;

	/**
	 * 是否填充折线下方。
	 */
	boolean fillBelow = false;

	/**
	 * ARGB 颜色。
	 */
	int fillColor = -1994428384;

	/**
	 * 0 实线，1 虚线，2 点线。
	 */
	int stroke = 0;

	/**
	 * 柱图是否按数值使用渐变色。
	 */
	boolean gradient = false;

	/**
	 * 柱渐变起始数值。
	 */
	double gradientStart = 0;

	/**
	 * 柱渐变结束数值。
	 */
	double gradientStop = 10;

	/**
	 * ARGB 颜色。
	 */
	int startColor = -13662520;

	/**
	 * ARGB 颜色。
	 */
	int stopColor = -14248086;

	/**
	 * 创建默认配置。
	 */
	public ACE数据组样式() {
	}

	/**
	 * 是否绘制坐标图的数据数值。
	 *
	 * @return 是否绘制坐标图的数据数值。
	 */
	@SimpleProperty
	public boolean 显示数值() {
		return values;
	}

	/**
	 * 是否绘制坐标图的数据数值。
	 *
	 * @param value 是否绘制坐标图的数据数值。
	 */
	@SimpleProperty
	public void 显示数值(boolean value) {
		values = value;
	}

	/**
	 * 数据标签大小，单位 SP。
	 *
	 * @return 数据标签大小，单位 SP。
	 */
	@SimpleProperty
	public float 数值大小() {
		return valueSize;
	}

	/**
	 * 数据标签大小，单位 SP。
	 *
	 * @param value 数据标签大小，单位 SP。
	 */
	@SimpleProperty
	public void 数值大小(float value) {
		Checks.range(value, 6, 100);
		valueSize = value;
	}

	/**
	 * 数据标签与数据点的间距，单位 DIP。
	 *
	 * @return 数据标签与数据点的间距，单位 DIP。
	 */
	@SimpleProperty
	public float 数值间距() {
		return spacing;
	}

	/**
	 * 数据标签与数据点的间距，单位 DIP。
	 *
	 * @param value 数据标签与数据点的间距，单位 DIP。
	 */
	@SimpleProperty
	public void 数值间距(float value) {
		Checks.range(value, 0, 1000);
		spacing = value;
	}

	/**
	 * 0 左对齐，1 居中，2 右对齐。
	 *
	 * @return 0 左对齐，1 居中，2 右对齐。
	 */
	@SimpleProperty
	public int 数值对齐() {
		return align;
	}

	/**
	 * 0 左对齐，1 居中，2 右对齐。
	 *
	 * @param value 0 左对齐，1 居中，2 右对齐。
	 */
	@SimpleProperty
	public void 数值对齐(int value) {
		Checks.index(value, 0, 2);
		align = value;
	}

	/**
	 * 折线线宽，单位 DIP。
	 *
	 * @return 折线线宽，单位 DIP。
	 */
	@SimpleProperty
	public float 线宽() {
		return lineWidth;
	}

	/**
	 * 折线线宽，单位 DIP。
	 *
	 * @param value 折线线宽，单位 DIP。
	 */
	@SimpleProperty
	public void 线宽(float value) {
		Checks.range(value, 0.1, 20);
		lineWidth = value;
	}

	/**
	 * 0 斜十字，1 圆，2 三角，3 方形，4 菱形，5 点。
	 *
	 * @return 0 斜十字，1 圆，2 三角，3 方形，4 菱形，5 点。
	 */
	@SimpleProperty
	public int 点形状() {
		return pointStyle;
	}

	/**
	 * 0 斜十字，1 圆，2 三角，3 方形，4 菱形，5 点。
	 *
	 * @param value 0 斜十字，1 圆，2 三角，3 方形，4 菱形，5 点。
	 */
	@SimpleProperty
	public void 点形状(int value) {
		Checks.index(value, 0, 5);
		pointStyle = value;
	}

	/**
	 * 是否填充折线及散点标记。
	 *
	 * @return 是否填充折线及散点标记。
	 */
	@SimpleProperty
	public boolean 填充点() {
		return fillPoints;
	}

	/**
	 * 是否填充折线及散点标记。
	 *
	 * @param value 是否填充折线及散点标记。
	 */
	@SimpleProperty
	public void 填充点(boolean value) {
		fillPoints = value;
	}

	/**
	 * 是否填充折线下方。
	 *
	 * @return 是否填充折线下方。
	 */
	@SimpleProperty
	public boolean 填充线下() {
		return fillBelow;
	}

	/**
	 * 是否填充折线下方。
	 *
	 * @param value 是否填充折线下方。
	 */
	@SimpleProperty
	public void 填充线下(boolean value) {
		fillBelow = value;
	}

	/**
	 * ARGB 颜色。
	 *
	 * @return ARGB 颜色。
	 */
	@SimpleProperty
	public int 线下颜色() {
		return fillColor;
	}

	/**
	 * ARGB 颜色。
	 *
	 * @param value ARGB 颜色。
	 */
	@SimpleProperty
	public void 线下颜色(int value) {
		fillColor = value;
	}

	/**
	 * 0 实线，1 虚线，2 点线。
	 *
	 * @return 0 实线，1 虚线，2 点线。
	 */
	@SimpleProperty
	public int 线条样式() {
		return stroke;
	}

	/**
	 * 0 实线，1 虚线，2 点线。
	 *
	 * @param value 0 实线，1 虚线，2 点线。
	 */
	@SimpleProperty
	public void 线条样式(int value) {
		Checks.index(value, 0, 2);
		stroke = value;
	}

	/**
	 * 柱图是否按数值使用渐变色。
	 *
	 * @return 柱图是否按数值使用渐变色。
	 */
	@SimpleProperty
	public boolean 渐变启用() {
		return gradient;
	}

	/**
	 * 柱图是否按数值使用渐变色。
	 *
	 * @param value 柱图是否按数值使用渐变色。
	 */
	@SimpleProperty
	public void 渐变启用(boolean value) {
		gradient = value;
	}

	/**
	 * 柱渐变起始数值。
	 *
	 * @return 柱渐变起始数值。
	 */
	@SimpleProperty
	public double 渐变起值() {
		return gradientStart;
	}

	/**
	 * 柱渐变起始数值。
	 *
	 * @param value 柱渐变起始数值。
	 */
	@SimpleProperty
	public void 渐变起值(double value) {
		Checks.range(value, -1e+100, 1e+100);
		gradientStart = value;
	}

	/**
	 * 柱渐变结束数值。
	 *
	 * @return 柱渐变结束数值。
	 */
	@SimpleProperty
	public double 渐变止值() {
		return gradientStop;
	}

	/**
	 * 柱渐变结束数值。
	 *
	 * @param value 柱渐变结束数值。
	 */
	@SimpleProperty
	public void 渐变止值(double value) {
		Checks.range(value, -1e+100, 1e+100);
		gradientStop = value;
	}

	/**
	 * ARGB 颜色。
	 *
	 * @return ARGB 颜色。
	 */
	@SimpleProperty
	public int 渐变起色() {
		return startColor;
	}

	/**
	 * ARGB 颜色。
	 *
	 * @param value ARGB 颜色。
	 */
	@SimpleProperty
	public void 渐变起色(int value) {
		startColor = value;
	}

	/**
	 * ARGB 颜色。
	 *
	 * @return ARGB 颜色。
	 */
	@SimpleProperty
	public int 渐变止色() {
		return stopColor;
	}

	/**
	 * ARGB 颜色。
	 *
	 * @param value ARGB 颜色。
	 */
	@SimpleProperty
	public void 渐变止色(int value) {
		stopColor = value;
	}

	/**
	 * 返回独立的配置副本。
	 *
	 * @return 返回独立的配置副本。
	 */
	@SimpleFunction
	public ACE数据组样式 复制() {
		ACE数据组样式 result = new ACE数据组样式();
		result.values = values;
		result.valueSize = valueSize;
		result.spacing = spacing;
		result.align = align;
		result.lineWidth = lineWidth;
		result.pointStyle = pointStyle;
		result.fillPoints = fillPoints;
		result.fillBelow = fillBelow;
		result.fillColor = fillColor;
		result.stroke = stroke;
		result.gradient = gradient;
		result.gradientStart = gradientStart;
		result.gradientStop = gradientStop;
		result.startColor = startColor;
		result.stopColor = stopColor;
		return result;
	}
}
