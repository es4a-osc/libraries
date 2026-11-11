package simple.library.wsd.mpandroidchart;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.SimpleProperty;

/**
 * MPAC数据组样式；应用到图表时复制配置，之后修改此对象不影响已有图表。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class MPAC数据组样式 {

	/**
	 * 按项目循环使用的 ARGB 颜色，空数组保留数据组原颜色。
	 */
	int[] colors = new int[0];

	/**
	 * 是否绘制此数据组。
	 */
	boolean visible = true;

	/**
	 * 是否绘制此组数值，同时受图表显示数值属性控制。
	 */
	boolean values = true;

	/**
	 * ARGB 颜色。
	 */
	int valueColor = -16777216;

	/**
	 * 数值文字大小，单位 SP。
	 */
	float valueSize = 12f;

	/**
	 * 本组数值格式器；空引用使用上游默认格式。
	 */
	MPAC格式器 formatter = null;

	/**
	 * 是否允许选择并高亮该数据组。
	 */
	boolean highlight = true;

	/**
	 * ARGB 颜色。
	 */
	int highlightColor = -26624;

	/**
	 * 直角坐标图的高亮线宽，单位 DIP。
	 */
	float highlightWidth = 1f;

	/**
	 * 直角坐标图是否显示水平高亮线。
	 */
	boolean horizontalHighlight = true;

	/**
	 * 直角坐标图是否显示垂直高亮线。
	 */
	boolean verticalHighlight = true;

	/**
	 * 折线、雷达和蜡烛阴影线宽，单位 DIP。
	 */
	float lineWidth = 2f;

	/**
	 * 折线和雷达是否绘制填充区域。
	 */
	boolean filled = false;

	/**
	 * ARGB 颜色。
	 */
	int fillColor = -13662520;

	/**
	 * 填充透明度，0 至 255。
	 */
	int fillAlpha = 85;

	/**
	 * -1 跟随平滑曲线；0 折线，1 阶梯，2 贝塞尔，3 水平贝塞尔。
	 */
	int lineMode = -1;

	/**
	 * 贝塞尔曲线强度，0.05 至 1。
	 */
	float cubic = 0.2f;

	/**
	 * 折线是否绘制数据圆点。
	 */
	boolean circles = true;

	/**
	 * 折线数据圆点半径，单位 DIP。
	 */
	float circleRadius = 4f;

	/**
	 * 折线圆点的 ARGB 颜色；空数组保留数据组原颜色。
	 */
	int[] circleColors = new int[0];

	/**
	 * 是否绘制圆点中心孔。
	 */
	boolean circleHole = true;

	/**
	 * 圆点中心孔半径，单位 DIP。
	 */
	float holeRadius = 2f;

	/**
	 * ARGB 颜色。
	 */
	int holeColor = -1;

	/**
	 * 折线虚线段长度，0 表示实线，单位 DIP。
	 */
	float dashLength = 0f;

	/**
	 * 折线虚线空白长度，单位 DIP。
	 */
	float dashGap = 4f;

	/**
	 * 折线虚线起始偏移，单位 DIP。
	 */
	float dashPhase = 0f;

	/**
	 * 0 方形，1 圆形，2 三角，3 十字，4 斜十字，5 上箭头，6 下箭头。
	 */
	int scatterShape = 1;

	/**
	 * 散点形状大小，单位 DIP。
	 */
	float scatterSize = 10f;

	/**
	 * 散点内部孔半径，单位 DIP。
	 */
	float scatterHole = 0f;

	/**
	 * ARGB 颜色。
	 */
	int scatterHoleColor = -1;

	/**
	 * 是否按最大气泡大小归一化绘制面积。
	 */
	boolean normalize = true;

	/**
	 * 饼块间隙，单位 DIP，0 至 20。
	 */
	float sliceSpace = 0f;

	/**
	 * 饼块选中时的偏移，单位 DIP。
	 */
	float selectionShift = 18f;

	/**
	 * 0 饼块内，1 饼块外。
	 */
	int xValuePosition = 0;

	/**
	 * 0 饼块内，1 饼块外。
	 */
	int yValuePosition = 0;

	/**
	 * ARGB 颜色。
	 */
	int valueLineColor = -16777216;

	/**
	 * 饼图外侧标注引导线宽，单位 DIP。
	 */
	float valueLineWidth = 1f;

	/**
	 * 引导线起点相对半径的百分比。
	 */
	float valueLineOffset = 75f;

	/**
	 * 首段长度相对半径的比例。
	 */
	float valueLineFirst = 0.3f;

	/**
	 * 末段长度相对半径的比例。
	 */
	float valueLineSecond = 0.4f;

	/**
	 * 引导线末段长度是否随角度变化。
	 */
	boolean variableLine = true;

	/**
	 * 引导线是否使用对应饼块颜色。
	 */
	boolean sliceLineColor = false;

	/**
	 * ARGB 颜色。
	 */
	int increasing = -14248086;

	/**
	 * ARGB 颜色。
	 */
	int decreasing = -65536;

	/**
	 * ARGB 颜色。
	 */
	int neutral = -7829368;

	/**
	 * 上涨蜡烛是否绘制为空心。
	 */
	boolean increaseHollow = true;

	/**
	 * 下跌蜡烛是否绘制为空心。
	 */
	boolean decreaseHollow = false;

	/**
	 * 蜡烛阴影是否随涨跌块颜色。
	 */
	boolean shadowSame = true;

	/**
	 * ARGB 颜色。
	 */
	int shadowColor = -7829368;

	/**
	 * ARGB 颜色。
	 */
	int barBorder = -16777216;

	/**
	 * 柱边框宽度，单位 DIP。
	 */
	float barBorderWidth = 0f;

	/**
	 * ARGB 颜色。
	 */
	int barShadow = -3355444;

	/**
	 * 堆叠各层的图例标签；空数组使用上游默认标签。
	 */
	String[] stackLabels = new String[0];

	/**
	 * 创建默认配置。
	 */
	public MPAC数据组样式() {
	}

	/**
	 * 按项目循环使用的 ARGB 颜色，空数组保留数据组原颜色。
	 *
	 * @return 按项目循环使用的 ARGB 颜色，空数组保留数据组原颜色。
	 */
	@SimpleProperty
	public int[] 颜色集() {
		return colors.clone();
	}

	/**
	 * 按项目循环使用的 ARGB 颜色，空数组保留数据组原颜色。
	 *
	 * @param value 按项目循环使用的 ARGB 颜色，空数组保留数据组原颜色。
	 * @throws IllegalArgumentException 数值、文本或数组不符合此属性约束。
	 */
	@SimpleProperty
	public void 颜色集(int[] value) {
		ChartChecks.array(value);
		colors = value.clone();
	}

	/**
	 * 是否绘制此数据组。
	 *
	 * @return 是否绘制此数据组。
	 */
	@SimpleProperty
	public boolean 可见() {
		return visible;
	}

	/**
	 * 是否绘制此数据组。
	 *
	 * @param value 是否绘制此数据组。
	 */
	@SimpleProperty
	public void 可见(boolean value) {
		visible = value;
	}

	/**
	 * 是否绘制此组数值，同时受图表显示数值属性控制。
	 *
	 * @return 是否绘制此组数值，同时受图表显示数值属性控制。
	 */
	@SimpleProperty
	public boolean 显示数值() {
		return values;
	}

	/**
	 * 是否绘制此组数值，同时受图表显示数值属性控制。
	 *
	 * @param value 是否绘制此组数值，同时受图表显示数值属性控制。
	 */
	@SimpleProperty
	public void 显示数值(boolean value) {
		values = value;
	}

	/**
	 * ARGB 颜色。
	 *
	 * @return ARGB 颜色。
	 */
	@SimpleProperty
	public int 数值颜色() {
		return valueColor;
	}

	/**
	 * ARGB 颜色。
	 *
	 * @param value ARGB 颜色。
	 */
	@SimpleProperty
	public void 数值颜色(int value) {
		valueColor = value;
	}

	/**
	 * 数值文字大小，单位 SP。
	 *
	 * @return 数值文字大小，单位 SP。
	 */
	@SimpleProperty
	public float 数值大小() {
		return valueSize;
	}

	/**
	 * 数值文字大小，单位 SP。
	 *
	 * @param value 数值文字大小，单位 SP。
	 * @throws IllegalArgumentException 数值、文本或数组不符合此属性约束。
	 */
	@SimpleProperty
	public void 数值大小(float value) {
		ChartChecks.range(value, 6f, 100f);
		valueSize = value;
	}

	/**
	 * 本组数值格式器；空引用使用上游默认格式。
	 *
	 * @return 本组数值格式器；空引用使用上游默认格式。
	 */
	@SimpleProperty
	public MPAC格式器 格式器() {
		return formatter;
	}

	/**
	 * 本组数值格式器；空引用使用上游默认格式。
	 *
	 * @param value 本组数值格式器；空引用使用上游默认格式。
	 */
	@SimpleProperty
	public void 格式器(MPAC格式器 value) {
		formatter = value;
	}

	/**
	 * 是否允许选择并高亮该数据组。
	 *
	 * @return 是否允许选择并高亮该数据组。
	 */
	@SimpleProperty
	public boolean 允许高亮() {
		return highlight;
	}

	/**
	 * 是否允许选择并高亮该数据组。
	 *
	 * @param value 是否允许选择并高亮该数据组。
	 */
	@SimpleProperty
	public void 允许高亮(boolean value) {
		highlight = value;
	}

	/**
	 * ARGB 颜色。
	 *
	 * @return ARGB 颜色。
	 */
	@SimpleProperty
	public int 高亮颜色() {
		return highlightColor;
	}

	/**
	 * ARGB 颜色。
	 *
	 * @param value ARGB 颜色。
	 */
	@SimpleProperty
	public void 高亮颜色(int value) {
		highlightColor = value;
	}

	/**
	 * 直角坐标图的高亮线宽，单位 DIP。
	 *
	 * @return 直角坐标图的高亮线宽，单位 DIP。
	 */
	@SimpleProperty
	public float 高亮线宽() {
		return highlightWidth;
	}

	/**
	 * 直角坐标图的高亮线宽，单位 DIP。
	 *
	 * @param value 直角坐标图的高亮线宽，单位 DIP。
	 * @throws IllegalArgumentException 数值、文本或数组不符合此属性约束。
	 */
	@SimpleProperty
	public void 高亮线宽(float value) {
		ChartChecks.range(value, 0.1f, 12f);
		highlightWidth = value;
	}

	/**
	 * 直角坐标图是否显示水平高亮线。
	 *
	 * @return 直角坐标图是否显示水平高亮线。
	 */
	@SimpleProperty
	public boolean 水平高亮线() {
		return horizontalHighlight;
	}

	/**
	 * 直角坐标图是否显示水平高亮线。
	 *
	 * @param value 直角坐标图是否显示水平高亮线。
	 */
	@SimpleProperty
	public void 水平高亮线(boolean value) {
		horizontalHighlight = value;
	}

	/**
	 * 直角坐标图是否显示垂直高亮线。
	 *
	 * @return 直角坐标图是否显示垂直高亮线。
	 */
	@SimpleProperty
	public boolean 垂直高亮线() {
		return verticalHighlight;
	}

	/**
	 * 直角坐标图是否显示垂直高亮线。
	 *
	 * @param value 直角坐标图是否显示垂直高亮线。
	 */
	@SimpleProperty
	public void 垂直高亮线(boolean value) {
		verticalHighlight = value;
	}

	/**
	 * 折线、雷达和蜡烛阴影线宽，单位 DIP。
	 *
	 * @return 折线、雷达和蜡烛阴影线宽，单位 DIP。
	 */
	@SimpleProperty
	public float 线宽() {
		return lineWidth;
	}

	/**
	 * 折线、雷达和蜡烛阴影线宽，单位 DIP。
	 *
	 * @param value 折线、雷达和蜡烛阴影线宽，单位 DIP。
	 * @throws IllegalArgumentException 数值、文本或数组不符合此属性约束。
	 */
	@SimpleProperty
	public void 线宽(float value) {
		ChartChecks.range(value, 0.1f, 10f);
		lineWidth = value;
	}

	/**
	 * 折线和雷达是否绘制填充区域。
	 *
	 * @return 折线和雷达是否绘制填充区域。
	 */
	@SimpleProperty
	public boolean 填充启用() {
		return filled;
	}

	/**
	 * 折线和雷达是否绘制填充区域。
	 *
	 * @param value 折线和雷达是否绘制填充区域。
	 */
	@SimpleProperty
	public void 填充启用(boolean value) {
		filled = value;
	}

	/**
	 * ARGB 颜色。
	 *
	 * @return ARGB 颜色。
	 */
	@SimpleProperty
	public int 填充颜色() {
		return fillColor;
	}

	/**
	 * ARGB 颜色。
	 *
	 * @param value ARGB 颜色。
	 */
	@SimpleProperty
	public void 填充颜色(int value) {
		fillColor = value;
	}

	/**
	 * 填充透明度，0 至 255。
	 *
	 * @return 填充透明度，0 至 255。
	 */
	@SimpleProperty
	public int 填充透明度() {
		return fillAlpha;
	}

	/**
	 * 填充透明度，0 至 255。
	 *
	 * @param value 填充透明度，0 至 255。
	 * @throws IllegalArgumentException 数值、文本或数组不符合此属性约束。
	 */
	@SimpleProperty
	public void 填充透明度(int value) {
		ChartChecks.integer(value, 0, 255);
		fillAlpha = value;
	}

	/**
	 * -1 跟随平滑曲线；0 折线，1 阶梯，2 贝塞尔，3 水平贝塞尔。
	 *
	 * @return -1 跟随平滑曲线；0 折线，1 阶梯，2 贝塞尔，3 水平贝塞尔。
	 */
	@SimpleProperty
	public int 折线模式() {
		return lineMode;
	}

	/**
	 * -1 跟随平滑曲线；0 折线，1 阶梯，2 贝塞尔，3 水平贝塞尔。
	 *
	 * @param value -1 跟随平滑曲线；0 折线，1 阶梯，2 贝塞尔，3 水平贝塞尔。
	 * @throws IllegalArgumentException 数值、文本或数组不符合此属性约束。
	 */
	@SimpleProperty
	public void 折线模式(int value) {
		ChartChecks.integer(value, -1, 3);
		lineMode = value;
	}

	/**
	 * 贝塞尔曲线强度，0.05 至 1。
	 *
	 * @return 贝塞尔曲线强度，0.05 至 1。
	 */
	@SimpleProperty
	public float 曲线强度() {
		return cubic;
	}

	/**
	 * 贝塞尔曲线强度，0.05 至 1。
	 *
	 * @param value 贝塞尔曲线强度，0.05 至 1。
	 * @throws IllegalArgumentException 数值、文本或数组不符合此属性约束。
	 */
	@SimpleProperty
	public void 曲线强度(float value) {
		ChartChecks.range(value, 0.05f, 1f);
		cubic = value;
	}

	/**
	 * 折线是否绘制数据圆点。
	 *
	 * @return 折线是否绘制数据圆点。
	 */
	@SimpleProperty
	public boolean 显示圆点() {
		return circles;
	}

	/**
	 * 折线是否绘制数据圆点。
	 *
	 * @param value 折线是否绘制数据圆点。
	 */
	@SimpleProperty
	public void 显示圆点(boolean value) {
		circles = value;
	}

	/**
	 * 折线数据圆点半径，单位 DIP。
	 *
	 * @return 折线数据圆点半径，单位 DIP。
	 */
	@SimpleProperty
	public float 圆点半径() {
		return circleRadius;
	}

	/**
	 * 折线数据圆点半径，单位 DIP。
	 *
	 * @param value 折线数据圆点半径，单位 DIP。
	 * @throws IllegalArgumentException 数值、文本或数组不符合此属性约束。
	 */
	@SimpleProperty
	public void 圆点半径(float value) {
		ChartChecks.range(value, 1f, 100f);
		circleRadius = value;
	}

	/**
	 * 折线圆点的 ARGB 颜色；空数组保留数据组原颜色。
	 *
	 * @return 折线圆点的 ARGB 颜色；空数组保留数据组原颜色。
	 */
	@SimpleProperty
	public int[] 圆点颜色集() {
		return circleColors.clone();
	}

	/**
	 * 折线圆点的 ARGB 颜色；空数组保留数据组原颜色。
	 *
	 * @param value 折线圆点的 ARGB 颜色；空数组保留数据组原颜色。
	 * @throws IllegalArgumentException 数值、文本或数组不符合此属性约束。
	 */
	@SimpleProperty
	public void 圆点颜色集(int[] value) {
		ChartChecks.array(value);
		circleColors = value.clone();
	}

	/**
	 * 是否绘制圆点中心孔。
	 *
	 * @return 是否绘制圆点中心孔。
	 */
	@SimpleProperty
	public boolean 空心圆点() {
		return circleHole;
	}

	/**
	 * 是否绘制圆点中心孔。
	 *
	 * @param value 是否绘制圆点中心孔。
	 */
	@SimpleProperty
	public void 空心圆点(boolean value) {
		circleHole = value;
	}

	/**
	 * 圆点中心孔半径，单位 DIP。
	 *
	 * @return 圆点中心孔半径，单位 DIP。
	 */
	@SimpleProperty
	public float 圆心半径() {
		return holeRadius;
	}

	/**
	 * 圆点中心孔半径，单位 DIP。
	 *
	 * @param value 圆点中心孔半径，单位 DIP。
	 * @throws IllegalArgumentException 数值、文本或数组不符合此属性约束。
	 */
	@SimpleProperty
	public void 圆心半径(float value) {
		ChartChecks.range(value, 0.5f, 100f);
		holeRadius = value;
	}

	/**
	 * ARGB 颜色。
	 *
	 * @return ARGB 颜色。
	 */
	@SimpleProperty
	public int 圆心颜色() {
		return holeColor;
	}

	/**
	 * ARGB 颜色。
	 *
	 * @param value ARGB 颜色。
	 */
	@SimpleProperty
	public void 圆心颜色(int value) {
		holeColor = value;
	}

	/**
	 * 折线虚线段长度，0 表示实线，单位 DIP。
	 *
	 * @return 折线虚线段长度，0 表示实线，单位 DIP。
	 */
	@SimpleProperty
	public float 虚线长度() {
		return dashLength;
	}

	/**
	 * 折线虚线段长度，0 表示实线，单位 DIP。
	 *
	 * @param value 折线虚线段长度，0 表示实线，单位 DIP。
	 * @throws IllegalArgumentException 数值、文本或数组不符合此属性约束。
	 */
	@SimpleProperty
	public void 虚线长度(float value) {
		ChartChecks.range(value, 0f, 100000f);
		dashLength = value;
	}

	/**
	 * 折线虚线空白长度，单位 DIP。
	 *
	 * @return 折线虚线空白长度，单位 DIP。
	 */
	@SimpleProperty
	public float 虚线间隔() {
		return dashGap;
	}

	/**
	 * 折线虚线空白长度，单位 DIP。
	 *
	 * @param value 折线虚线空白长度，单位 DIP。
	 * @throws IllegalArgumentException 数值、文本或数组不符合此属性约束。
	 */
	@SimpleProperty
	public void 虚线间隔(float value) {
		ChartChecks.range(value, 0.1f, 100000f);
		dashGap = value;
	}

	/**
	 * 折线虚线起始偏移，单位 DIP。
	 *
	 * @return 折线虚线起始偏移，单位 DIP。
	 */
	@SimpleProperty
	public float 虚线相位() {
		return dashPhase;
	}

	/**
	 * 折线虚线起始偏移，单位 DIP。
	 *
	 * @param value 折线虚线起始偏移，单位 DIP。
	 * @throws IllegalArgumentException 数值、文本或数组不符合此属性约束。
	 */
	@SimpleProperty
	public void 虚线相位(float value) {
		ChartChecks.range(value, 0f, 100000f);
		dashPhase = value;
	}

	/**
	 * 0 方形，1 圆形，2 三角，3 十字，4 斜十字，5 上箭头，6 下箭头。
	 *
	 * @return 0 方形，1 圆形，2 三角，3 十字，4 斜十字，5 上箭头，6 下箭头。
	 */
	@SimpleProperty
	public int 散点形状() {
		return scatterShape;
	}

	/**
	 * 0 方形，1 圆形，2 三角，3 十字，4 斜十字，5 上箭头，6 下箭头。
	 *
	 * @param value 0 方形，1 圆形，2 三角，3 十字，4 斜十字，5 上箭头，6 下箭头。
	 * @throws IllegalArgumentException 数值、文本或数组不符合此属性约束。
	 */
	@SimpleProperty
	public void 散点形状(int value) {
		ChartChecks.integer(value, 0, 6);
		scatterShape = value;
	}

	/**
	 * 散点形状大小，单位 DIP。
	 *
	 * @return 散点形状大小，单位 DIP。
	 */
	@SimpleProperty
	public float 散点大小() {
		return scatterSize;
	}

	/**
	 * 散点形状大小，单位 DIP。
	 *
	 * @param value 散点形状大小，单位 DIP。
	 * @throws IllegalArgumentException 数值、文本或数组不符合此属性约束。
	 */
	@SimpleProperty
	public void 散点大小(float value) {
		ChartChecks.range(value, 0.1f, 1000f);
		scatterSize = value;
	}

	/**
	 * 散点内部孔半径，单位 DIP。
	 *
	 * @return 散点内部孔半径，单位 DIP。
	 */
	@SimpleProperty
	public float 散点孔半径() {
		return scatterHole;
	}

	/**
	 * 散点内部孔半径，单位 DIP。
	 *
	 * @param value 散点内部孔半径，单位 DIP。
	 * @throws IllegalArgumentException 数值、文本或数组不符合此属性约束。
	 */
	@SimpleProperty
	public void 散点孔半径(float value) {
		ChartChecks.range(value, 0f, 100f);
		scatterHole = value;
	}

	/**
	 * ARGB 颜色。
	 *
	 * @return ARGB 颜色。
	 */
	@SimpleProperty
	public int 散点孔颜色() {
		return scatterHoleColor;
	}

	/**
	 * ARGB 颜色。
	 *
	 * @param value ARGB 颜色。
	 */
	@SimpleProperty
	public void 散点孔颜色(int value) {
		scatterHoleColor = value;
	}

	/**
	 * 是否按最大气泡大小归一化绘制面积。
	 *
	 * @return 是否按最大气泡大小归一化绘制面积。
	 */
	@SimpleProperty
	public boolean 气泡归一化() {
		return normalize;
	}

	/**
	 * 是否按最大气泡大小归一化绘制面积。
	 *
	 * @param value 是否按最大气泡大小归一化绘制面积。
	 */
	@SimpleProperty
	public void 气泡归一化(boolean value) {
		normalize = value;
	}

	/**
	 * 饼块间隙，单位 DIP，0 至 20。
	 *
	 * @return 饼块间隙，单位 DIP，0 至 20。
	 */
	@SimpleProperty
	public float 饼块间距() {
		return sliceSpace;
	}

	/**
	 * 饼块间隙，单位 DIP，0 至 20。
	 *
	 * @param value 饼块间隙，单位 DIP，0 至 20。
	 * @throws IllegalArgumentException 数值、文本或数组不符合此属性约束。
	 */
	@SimpleProperty
	public void 饼块间距(float value) {
		ChartChecks.range(value, 0f, 20f);
		sliceSpace = value;
	}

	/**
	 * 饼块选中时的偏移，单位 DIP。
	 *
	 * @return 饼块选中时的偏移，单位 DIP。
	 */
	@SimpleProperty
	public float 饼块选中偏移() {
		return selectionShift;
	}

	/**
	 * 饼块选中时的偏移，单位 DIP。
	 *
	 * @param value 饼块选中时的偏移，单位 DIP。
	 * @throws IllegalArgumentException 数值、文本或数组不符合此属性约束。
	 */
	@SimpleProperty
	public void 饼块选中偏移(float value) {
		ChartChecks.range(value, 0f, 100f);
		selectionShift = value;
	}

	/**
	 * 0 饼块内，1 饼块外。
	 *
	 * @return 0 饼块内，1 饼块外。
	 */
	@SimpleProperty
	public int 饼名称位置() {
		return xValuePosition;
	}

	/**
	 * 0 饼块内，1 饼块外。
	 *
	 * @param value 0 饼块内，1 饼块外。
	 * @throws IllegalArgumentException 数值、文本或数组不符合此属性约束。
	 */
	@SimpleProperty
	public void 饼名称位置(int value) {
		ChartChecks.integer(value, 0, 1);
		xValuePosition = value;
	}

	/**
	 * 0 饼块内，1 饼块外。
	 *
	 * @return 0 饼块内，1 饼块外。
	 */
	@SimpleProperty
	public int 饼数值位置() {
		return yValuePosition;
	}

	/**
	 * 0 饼块内，1 饼块外。
	 *
	 * @param value 0 饼块内，1 饼块外。
	 * @throws IllegalArgumentException 数值、文本或数组不符合此属性约束。
	 */
	@SimpleProperty
	public void 饼数值位置(int value) {
		ChartChecks.integer(value, 0, 1);
		yValuePosition = value;
	}

	/**
	 * ARGB 颜色。
	 *
	 * @return ARGB 颜色。
	 */
	@SimpleProperty
	public int 饼引导线颜色() {
		return valueLineColor;
	}

	/**
	 * ARGB 颜色。
	 *
	 * @param value ARGB 颜色。
	 */
	@SimpleProperty
	public void 饼引导线颜色(int value) {
		valueLineColor = value;
	}

	/**
	 * 饼图外侧标注引导线宽，单位 DIP。
	 *
	 * @return 饼图外侧标注引导线宽，单位 DIP。
	 */
	@SimpleProperty
	public float 饼引导线宽() {
		return valueLineWidth;
	}

	/**
	 * 饼图外侧标注引导线宽，单位 DIP。
	 *
	 * @param value 饼图外侧标注引导线宽，单位 DIP。
	 * @throws IllegalArgumentException 数值、文本或数组不符合此属性约束。
	 */
	@SimpleProperty
	public void 饼引导线宽(float value) {
		ChartChecks.range(value, 0.1f, 12f);
		valueLineWidth = value;
	}

	/**
	 * 引导线起点相对半径的百分比。
	 *
	 * @return 引导线起点相对半径的百分比。
	 */
	@SimpleProperty
	public float 饼引导线起点() {
		return valueLineOffset;
	}

	/**
	 * 引导线起点相对半径的百分比。
	 *
	 * @param value 引导线起点相对半径的百分比。
	 * @throws IllegalArgumentException 数值、文本或数组不符合此属性约束。
	 */
	@SimpleProperty
	public void 饼引导线起点(float value) {
		ChartChecks.range(value, 0f, 100f);
		valueLineOffset = value;
	}

	/**
	 * 首段长度相对半径的比例。
	 *
	 * @return 首段长度相对半径的比例。
	 */
	@SimpleProperty
	public float 饼引导线首段() {
		return valueLineFirst;
	}

	/**
	 * 首段长度相对半径的比例。
	 *
	 * @param value 首段长度相对半径的比例。
	 * @throws IllegalArgumentException 数值、文本或数组不符合此属性约束。
	 */
	@SimpleProperty
	public void 饼引导线首段(float value) {
		ChartChecks.range(value, 0f, 5f);
		valueLineFirst = value;
	}

	/**
	 * 末段长度相对半径的比例。
	 *
	 * @return 末段长度相对半径的比例。
	 */
	@SimpleProperty
	public float 饼引导线末段() {
		return valueLineSecond;
	}

	/**
	 * 末段长度相对半径的比例。
	 *
	 * @param value 末段长度相对半径的比例。
	 * @throws IllegalArgumentException 数值、文本或数组不符合此属性约束。
	 */
	@SimpleProperty
	public void 饼引导线末段(float value) {
		ChartChecks.range(value, 0f, 5f);
		valueLineSecond = value;
	}

	/**
	 * 引导线末段长度是否随角度变化。
	 *
	 * @return 引导线末段长度是否随角度变化。
	 */
	@SimpleProperty
	public boolean 饼引导线可变() {
		return variableLine;
	}

	/**
	 * 引导线末段长度是否随角度变化。
	 *
	 * @param value 引导线末段长度是否随角度变化。
	 */
	@SimpleProperty
	public void 饼引导线可变(boolean value) {
		variableLine = value;
	}

	/**
	 * 引导线是否使用对应饼块颜色。
	 *
	 * @return 引导线是否使用对应饼块颜色。
	 */
	@SimpleProperty
	public boolean 饼引导线随块色() {
		return sliceLineColor;
	}

	/**
	 * 引导线是否使用对应饼块颜色。
	 *
	 * @param value 引导线是否使用对应饼块颜色。
	 */
	@SimpleProperty
	public void 饼引导线随块色(boolean value) {
		sliceLineColor = value;
	}

	/**
	 * ARGB 颜色。
	 *
	 * @return ARGB 颜色。
	 */
	@SimpleProperty
	public int 上涨颜色() {
		return increasing;
	}

	/**
	 * ARGB 颜色。
	 *
	 * @param value ARGB 颜色。
	 */
	@SimpleProperty
	public void 上涨颜色(int value) {
		increasing = value;
	}

	/**
	 * ARGB 颜色。
	 *
	 * @return ARGB 颜色。
	 */
	@SimpleProperty
	public int 下跌颜色() {
		return decreasing;
	}

	/**
	 * ARGB 颜色。
	 *
	 * @param value ARGB 颜色。
	 */
	@SimpleProperty
	public void 下跌颜色(int value) {
		decreasing = value;
	}

	/**
	 * ARGB 颜色。
	 *
	 * @return ARGB 颜色。
	 */
	@SimpleProperty
	public int 平盘颜色() {
		return neutral;
	}

	/**
	 * ARGB 颜色。
	 *
	 * @param value ARGB 颜色。
	 */
	@SimpleProperty
	public void 平盘颜色(int value) {
		neutral = value;
	}

	/**
	 * 上涨蜡烛是否绘制为空心。
	 *
	 * @return 上涨蜡烛是否绘制为空心。
	 */
	@SimpleProperty
	public boolean 上涨空心() {
		return increaseHollow;
	}

	/**
	 * 上涨蜡烛是否绘制为空心。
	 *
	 * @param value 上涨蜡烛是否绘制为空心。
	 */
	@SimpleProperty
	public void 上涨空心(boolean value) {
		increaseHollow = value;
	}

	/**
	 * 下跌蜡烛是否绘制为空心。
	 *
	 * @return 下跌蜡烛是否绘制为空心。
	 */
	@SimpleProperty
	public boolean 下跌空心() {
		return decreaseHollow;
	}

	/**
	 * 下跌蜡烛是否绘制为空心。
	 *
	 * @param value 下跌蜡烛是否绘制为空心。
	 */
	@SimpleProperty
	public void 下跌空心(boolean value) {
		decreaseHollow = value;
	}

	/**
	 * 蜡烛阴影是否随涨跌块颜色。
	 *
	 * @return 蜡烛阴影是否随涨跌块颜色。
	 */
	@SimpleProperty
	public boolean 烛影随块色() {
		return shadowSame;
	}

	/**
	 * 蜡烛阴影是否随涨跌块颜色。
	 *
	 * @param value 蜡烛阴影是否随涨跌块颜色。
	 */
	@SimpleProperty
	public void 烛影随块色(boolean value) {
		shadowSame = value;
	}

	/**
	 * ARGB 颜色。
	 *
	 * @return ARGB 颜色。
	 */
	@SimpleProperty
	public int 烛影颜色() {
		return shadowColor;
	}

	/**
	 * ARGB 颜色。
	 *
	 * @param value ARGB 颜色。
	 */
	@SimpleProperty
	public void 烛影颜色(int value) {
		shadowColor = value;
	}

	/**
	 * ARGB 颜色。
	 *
	 * @return ARGB 颜色。
	 */
	@SimpleProperty
	public int 柱边框颜色() {
		return barBorder;
	}

	/**
	 * ARGB 颜色。
	 *
	 * @param value ARGB 颜色。
	 */
	@SimpleProperty
	public void 柱边框颜色(int value) {
		barBorder = value;
	}

	/**
	 * 柱边框宽度，单位 DIP。
	 *
	 * @return 柱边框宽度，单位 DIP。
	 */
	@SimpleProperty
	public float 柱边框宽度() {
		return barBorderWidth;
	}

	/**
	 * 柱边框宽度，单位 DIP。
	 *
	 * @param value 柱边框宽度，单位 DIP。
	 * @throws IllegalArgumentException 数值、文本或数组不符合此属性约束。
	 */
	@SimpleProperty
	public void 柱边框宽度(float value) {
		ChartChecks.range(value, 0f, 12f);
		barBorderWidth = value;
	}

	/**
	 * ARGB 颜色。
	 *
	 * @return ARGB 颜色。
	 */
	@SimpleProperty
	public int 柱阴影颜色() {
		return barShadow;
	}

	/**
	 * ARGB 颜色。
	 *
	 * @param value ARGB 颜色。
	 */
	@SimpleProperty
	public void 柱阴影颜色(int value) {
		barShadow = value;
	}

	/**
	 * 堆叠各层的图例标签；空数组使用上游默认标签。
	 *
	 * @return 堆叠各层的图例标签；空数组使用上游默认标签。
	 */
	@SimpleProperty
	public String[] 堆叠标签() {
		return stackLabels.clone();
	}

	/**
	 * 堆叠各层的图例标签；空数组使用上游默认标签。
	 *
	 * @param value 堆叠各层的图例标签；空数组使用上游默认标签。
	 * @throws IllegalArgumentException 数值、文本或数组不符合此属性约束。
	 */
	@SimpleProperty
	public void 堆叠标签(String[] value) {
		ChartChecks.array(value);
		stackLabels = value.clone();
	}

	/**
	 * 返回独立的配置副本，回调接口不复制。
	 *
	 * @return 返回独立的配置副本，回调接口不复制。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	@SimpleFunction
	public MPAC数据组样式 复制() {
		MPAC数据组样式 result = new MPAC数据组样式();
		result.colors = colors.clone();
		result.visible = visible;
		result.values = values;
		result.valueColor = valueColor;
		result.valueSize = valueSize;
		result.formatter = formatter;
		result.highlight = highlight;
		result.highlightColor = highlightColor;
		result.highlightWidth = highlightWidth;
		result.horizontalHighlight = horizontalHighlight;
		result.verticalHighlight = verticalHighlight;
		result.lineWidth = lineWidth;
		result.filled = filled;
		result.fillColor = fillColor;
		result.fillAlpha = fillAlpha;
		result.lineMode = lineMode;
		result.cubic = cubic;
		result.circles = circles;
		result.circleRadius = circleRadius;
		result.circleColors = circleColors.clone();
		result.circleHole = circleHole;
		result.holeRadius = holeRadius;
		result.holeColor = holeColor;
		result.dashLength = dashLength;
		result.dashGap = dashGap;
		result.dashPhase = dashPhase;
		result.scatterShape = scatterShape;
		result.scatterSize = scatterSize;
		result.scatterHole = scatterHole;
		result.scatterHoleColor = scatterHoleColor;
		result.normalize = normalize;
		result.sliceSpace = sliceSpace;
		result.selectionShift = selectionShift;
		result.xValuePosition = xValuePosition;
		result.yValuePosition = yValuePosition;
		result.valueLineColor = valueLineColor;
		result.valueLineWidth = valueLineWidth;
		result.valueLineOffset = valueLineOffset;
		result.valueLineFirst = valueLineFirst;
		result.valueLineSecond = valueLineSecond;
		result.variableLine = variableLine;
		result.sliceLineColor = sliceLineColor;
		result.increasing = increasing;
		result.decreasing = decreasing;
		result.neutral = neutral;
		result.increaseHollow = increaseHollow;
		result.decreaseHollow = decreaseHollow;
		result.shadowSame = shadowSame;
		result.shadowColor = shadowColor;
		result.barBorder = barBorder;
		result.barBorderWidth = barBorderWidth;
		result.barShadow = barShadow;
		result.stackLabels = stackLabels.clone();
		result.candleSpace = candleSpace;
		result.candleBar = candleBar;
		return result;
	}

	/**
	 * 蜡烛两侧的留白比例，0 至 0.45。
	 */
	float candleSpace = 0.1f;

	/**
	 * 蜡烛两侧的留白比例，0 至 0.45。
	 *
	 * @return 蜡烛两侧的留白比例，0 至 0.45。
	 */
	@SimpleProperty
	public float 蜡烛间距() {
		return candleSpace;
	}

	/**
	 * 蜡烛两侧的留白比例，0 至 0.45。
	 *
	 * @param value 蜡烛两侧的留白比例，0 至 0.45。
	 */
	@SimpleProperty
	public void 蜡烛间距(float value) {
		ChartChecks.range(value, 0, 0.45f);
		candleSpace = value;
	}

	/**
	 * True 绘制蜡烛实体，False 绘制开高低收线。
	 */
	boolean candleBar = true;

	/**
	 * True 绘制蜡烛实体，False 绘制开高低收线。
	 *
	 * @return True 绘制蜡烛实体，False 绘制开高低收线。
	 */
	@SimpleProperty
	public boolean 绘制蜡烛实体() {
		return candleBar;
	}

	/**
	 * True 绘制蜡烛实体，False 绘制开高低收线。
	 *
	 * @param value True 绘制蜡烛实体，False 绘制开高低收线。
	 */
	@SimpleProperty
	public void 绘制蜡烛实体(boolean value) {
		candleBar = value;
	}
}
