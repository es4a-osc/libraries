package simple.library.wsd.mpandroidchart;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.SimpleProperty;

/**
 * MPAC图表样式；应用到图表时复制配置，之后修改此对象不影响已有图表。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class MPAC图表样式 {

	/**
	 * 没有数据时显示的文字。
	 */
	String noData = "暂无数据";

	/**
	 * 是否响应手势。
	 */
	boolean touch = true;

	/**
	 * 是否允许点击数据项目进行高亮。
	 */
	boolean tapHighlight = true;

	/**
	 * 直角坐标图是否在拖动时更新高亮。
	 */
	boolean dragHighlight = true;

	/**
	 * 选中数据时是否显示内置数值标记。
	 */
	boolean markers = false;

	/**
	 * ARGB 颜色。
	 */
	int markerColor = -13421773;

	/**
	 * ARGB 颜色。
	 */
	int markerTextColor = -1;

	/**
	 * 标记文字大小，单位 SP。
	 */
	float markerSize = 12f;

	/**
	 * 是否启用拖动或旋转的惯性。
	 */
	boolean deceleration = true;

	/**
	 * 惯性摩擦系数，0 至 0.999。
	 */
	float friction = 0.9f;

	/**
	 * 额外左边距，单位 DIP。
	 */
	float left = 0f;

	/**
	 * 额外上边距，单位 DIP。
	 */
	float top = 0f;

	/**
	 * 额外右边距，单位 DIP。
	 */
	float right = 0f;

	/**
	 * 额外下边距，单位 DIP。
	 */
	float bottom = 0f;

	/**
	 * 直角坐标图是否绘制内容边框。
	 */
	boolean borders = false;

	/**
	 * ARGB 颜色。
	 */
	int borderColor = -16777216;

	/**
	 * 内容边框宽，单位 DIP。
	 */
	float borderWidth = 1f;

	/**
	 * 直角坐标图是否绘制内容背景。
	 */
	boolean gridBackground = false;

	/**
	 * ARGB 颜色。
	 */
	int gridColor = -986896;

	/**
	 * 拖动直角坐标图时按可见数据自动计算纵轴范围。
	 */
	boolean autoScale = false;

	/**
	 * 是否允许横向手势缩放，同时受可否缩放控制。
	 */
	boolean scaleX = true;

	/**
	 * 是否允许纵向手势缩放，同时受可否缩放控制。
	 */
	boolean scaleY = true;

	/**
	 * 是否允许双击缩放，同时受可否缩放控制。
	 */
	boolean doubleTap = true;

	/**
	 * 是否允许双指同时缩放两轴。
	 */
	boolean pinch = true;

	/**
	 * 是否允许横向拖动，同时受可否拖动控制。
	 */
	boolean dragX = true;

	/**
	 * 是否允许纵向拖动，同时受可否拖动控制。
	 */
	boolean dragY = true;

	/**
	 * 饼图或雷达图是否允许手势旋转。
	 */
	boolean rotation = true;

	/**
	 * 饼图或雷达图的初始旋转角度。
	 */
	float rotationAngle = 270f;

	/**
	 * 直角坐标图同时绘制数值标注的上限。
	 */
	int maxValues = 100;

	/**
	 * 图表文字字体资产路径；空文本使用系统默认。
	 */
	String font = "";

	/**
	 * 内容区描述文字，空文本隐藏。
	 */
	String description = "";

	/**
	 * 描述文字大小，单位 SP。
	 */
	float descriptionSize = 10f;

	/**
	 * ARGB 颜色。
	 */
	int descriptionColor = -16777216;

	/**
	 * 创建默认配置。
	 */
	public MPAC图表样式() {
	}

	/**
	 * 没有数据时显示的文字。
	 *
	 * @return 没有数据时显示的文字。
	 */
	@SimpleProperty
	public String 空数据提示() {
		return noData;
	}

	/**
	 * 没有数据时显示的文字。
	 *
	 * @param value 没有数据时显示的文字。
	 * @throws IllegalArgumentException 数值、文本或数组不符合此属性约束。
	 */
	@SimpleProperty
	public void 空数据提示(String value) {
		ChartChecks.text(value);
		noData = value;
	}

	/**
	 * 是否响应手势。
	 *
	 * @return 是否响应手势。
	 */
	@SimpleProperty
	public boolean 允许触摸() {
		return touch;
	}

	/**
	 * 是否响应手势。
	 *
	 * @param value 是否响应手势。
	 */
	@SimpleProperty
	public void 允许触摸(boolean value) {
		touch = value;
	}

	/**
	 * 是否允许点击数据项目进行高亮。
	 *
	 * @return 是否允许点击数据项目进行高亮。
	 */
	@SimpleProperty
	public boolean 点击高亮() {
		return tapHighlight;
	}

	/**
	 * 是否允许点击数据项目进行高亮。
	 *
	 * @param value 是否允许点击数据项目进行高亮。
	 */
	@SimpleProperty
	public void 点击高亮(boolean value) {
		tapHighlight = value;
	}

	/**
	 * 直角坐标图是否在拖动时更新高亮。
	 *
	 * @return 直角坐标图是否在拖动时更新高亮。
	 */
	@SimpleProperty
	public boolean 拖动高亮() {
		return dragHighlight;
	}

	/**
	 * 直角坐标图是否在拖动时更新高亮。
	 *
	 * @param value 直角坐标图是否在拖动时更新高亮。
	 */
	@SimpleProperty
	public void 拖动高亮(boolean value) {
		dragHighlight = value;
	}

	/**
	 * 选中数据时是否显示内置数值标记。
	 *
	 * @return 选中数据时是否显示内置数值标记。
	 */
	@SimpleProperty
	public boolean 显示标记() {
		return markers;
	}

	/**
	 * 选中数据时是否显示内置数值标记。
	 *
	 * @param value 选中数据时是否显示内置数值标记。
	 */
	@SimpleProperty
	public void 显示标记(boolean value) {
		markers = value;
	}

	/**
	 * ARGB 颜色。
	 *
	 * @return ARGB 颜色。
	 */
	@SimpleProperty
	public int 标记背景() {
		return markerColor;
	}

	/**
	 * ARGB 颜色。
	 *
	 * @param value ARGB 颜色。
	 */
	@SimpleProperty
	public void 标记背景(int value) {
		markerColor = value;
	}

	/**
	 * ARGB 颜色。
	 *
	 * @return ARGB 颜色。
	 */
	@SimpleProperty
	public int 标记文字颜色() {
		return markerTextColor;
	}

	/**
	 * ARGB 颜色。
	 *
	 * @param value ARGB 颜色。
	 */
	@SimpleProperty
	public void 标记文字颜色(int value) {
		markerTextColor = value;
	}

	/**
	 * 标记文字大小，单位 SP。
	 *
	 * @return 标记文字大小，单位 SP。
	 */
	@SimpleProperty
	public float 标记文字大小() {
		return markerSize;
	}

	/**
	 * 标记文字大小，单位 SP。
	 *
	 * @param value 标记文字大小，单位 SP。
	 * @throws IllegalArgumentException 数值、文本或数组不符合此属性约束。
	 */
	@SimpleProperty
	public void 标记文字大小(float value) {
		ChartChecks.range(value, 6f, 100f);
		markerSize = value;
	}

	/**
	 * 是否启用拖动或旋转的惯性。
	 *
	 * @return 是否启用拖动或旋转的惯性。
	 */
	@SimpleProperty
	public boolean 惯性滑动() {
		return deceleration;
	}

	/**
	 * 是否启用拖动或旋转的惯性。
	 *
	 * @param value 是否启用拖动或旋转的惯性。
	 */
	@SimpleProperty
	public void 惯性滑动(boolean value) {
		deceleration = value;
	}

	/**
	 * 惯性摩擦系数，0 至 0.999。
	 *
	 * @return 惯性摩擦系数，0 至 0.999。
	 */
	@SimpleProperty
	public float 惯性摩擦() {
		return friction;
	}

	/**
	 * 惯性摩擦系数，0 至 0.999。
	 *
	 * @param value 惯性摩擦系数，0 至 0.999。
	 * @throws IllegalArgumentException 数值、文本或数组不符合此属性约束。
	 */
	@SimpleProperty
	public void 惯性摩擦(float value) {
		ChartChecks.range(value, 0f, 0.999f);
		friction = value;
	}

	/**
	 * 额外左边距，单位 DIP。
	 *
	 * @return 额外左边距，单位 DIP。
	 */
	@SimpleProperty
	public float 额外左边距() {
		return left;
	}

	/**
	 * 额外左边距，单位 DIP。
	 *
	 * @param value 额外左边距，单位 DIP。
	 * @throws IllegalArgumentException 数值、文本或数组不符合此属性约束。
	 */
	@SimpleProperty
	public void 额外左边距(float value) {
		ChartChecks.range(value, 0f, 100000f);
		left = value;
	}

	/**
	 * 额外上边距，单位 DIP。
	 *
	 * @return 额外上边距，单位 DIP。
	 */
	@SimpleProperty
	public float 额外上边距() {
		return top;
	}

	/**
	 * 额外上边距，单位 DIP。
	 *
	 * @param value 额外上边距，单位 DIP。
	 * @throws IllegalArgumentException 数值、文本或数组不符合此属性约束。
	 */
	@SimpleProperty
	public void 额外上边距(float value) {
		ChartChecks.range(value, 0f, 100000f);
		top = value;
	}

	/**
	 * 额外右边距，单位 DIP。
	 *
	 * @return 额外右边距，单位 DIP。
	 */
	@SimpleProperty
	public float 额外右边距() {
		return right;
	}

	/**
	 * 额外右边距，单位 DIP。
	 *
	 * @param value 额外右边距，单位 DIP。
	 * @throws IllegalArgumentException 数值、文本或数组不符合此属性约束。
	 */
	@SimpleProperty
	public void 额外右边距(float value) {
		ChartChecks.range(value, 0f, 100000f);
		right = value;
	}

	/**
	 * 额外下边距，单位 DIP。
	 *
	 * @return 额外下边距，单位 DIP。
	 */
	@SimpleProperty
	public float 额外下边距() {
		return bottom;
	}

	/**
	 * 额外下边距，单位 DIP。
	 *
	 * @param value 额外下边距，单位 DIP。
	 * @throws IllegalArgumentException 数值、文本或数组不符合此属性约束。
	 */
	@SimpleProperty
	public void 额外下边距(float value) {
		ChartChecks.range(value, 0f, 100000f);
		bottom = value;
	}

	/**
	 * 直角坐标图是否绘制内容边框。
	 *
	 * @return 直角坐标图是否绘制内容边框。
	 */
	@SimpleProperty
	public boolean 绘制边框() {
		return borders;
	}

	/**
	 * 直角坐标图是否绘制内容边框。
	 *
	 * @param value 直角坐标图是否绘制内容边框。
	 */
	@SimpleProperty
	public void 绘制边框(boolean value) {
		borders = value;
	}

	/**
	 * ARGB 颜色。
	 *
	 * @return ARGB 颜色。
	 */
	@SimpleProperty
	public int 边框颜色() {
		return borderColor;
	}

	/**
	 * ARGB 颜色。
	 *
	 * @param value ARGB 颜色。
	 */
	@SimpleProperty
	public void 边框颜色(int value) {
		borderColor = value;
	}

	/**
	 * 内容边框宽，单位 DIP。
	 *
	 * @return 内容边框宽，单位 DIP。
	 */
	@SimpleProperty
	public float 边框宽度() {
		return borderWidth;
	}

	/**
	 * 内容边框宽，单位 DIP。
	 *
	 * @param value 内容边框宽，单位 DIP。
	 * @throws IllegalArgumentException 数值、文本或数组不符合此属性约束。
	 */
	@SimpleProperty
	public void 边框宽度(float value) {
		ChartChecks.range(value, 0.1f, 12f);
		borderWidth = value;
	}

	/**
	 * 直角坐标图是否绘制内容背景。
	 *
	 * @return 直角坐标图是否绘制内容背景。
	 */
	@SimpleProperty
	public boolean 绘制背景网格() {
		return gridBackground;
	}

	/**
	 * 直角坐标图是否绘制内容背景。
	 *
	 * @param value 直角坐标图是否绘制内容背景。
	 */
	@SimpleProperty
	public void 绘制背景网格(boolean value) {
		gridBackground = value;
	}

	/**
	 * ARGB 颜色。
	 *
	 * @return ARGB 颜色。
	 */
	@SimpleProperty
	public int 网格背景颜色() {
		return gridColor;
	}

	/**
	 * ARGB 颜色。
	 *
	 * @param value ARGB 颜色。
	 */
	@SimpleProperty
	public void 网格背景颜色(int value) {
		gridColor = value;
	}

	/**
	 * 拖动直角坐标图时按可见数据自动计算纵轴范围。
	 *
	 * @return 拖动直角坐标图时按可见数据自动计算纵轴范围。
	 */
	@SimpleProperty
	public boolean 自动可见范围() {
		return autoScale;
	}

	/**
	 * 拖动直角坐标图时按可见数据自动计算纵轴范围。
	 *
	 * @param value 拖动直角坐标图时按可见数据自动计算纵轴范围。
	 */
	@SimpleProperty
	public void 自动可见范围(boolean value) {
		autoScale = value;
	}

	/**
	 * 是否允许横向手势缩放，同时受可否缩放控制。
	 *
	 * @return 是否允许横向手势缩放，同时受可否缩放控制。
	 */
	@SimpleProperty
	public boolean 横向缩放() {
		return scaleX;
	}

	/**
	 * 是否允许横向手势缩放，同时受可否缩放控制。
	 *
	 * @param value 是否允许横向手势缩放，同时受可否缩放控制。
	 */
	@SimpleProperty
	public void 横向缩放(boolean value) {
		scaleX = value;
	}

	/**
	 * 是否允许纵向手势缩放，同时受可否缩放控制。
	 *
	 * @return 是否允许纵向手势缩放，同时受可否缩放控制。
	 */
	@SimpleProperty
	public boolean 纵向缩放() {
		return scaleY;
	}

	/**
	 * 是否允许纵向手势缩放，同时受可否缩放控制。
	 *
	 * @param value 是否允许纵向手势缩放，同时受可否缩放控制。
	 */
	@SimpleProperty
	public void 纵向缩放(boolean value) {
		scaleY = value;
	}

	/**
	 * 是否允许双击缩放，同时受可否缩放控制。
	 *
	 * @return 是否允许双击缩放，同时受可否缩放控制。
	 */
	@SimpleProperty
	public boolean 双击缩放() {
		return doubleTap;
	}

	/**
	 * 是否允许双击缩放，同时受可否缩放控制。
	 *
	 * @param value 是否允许双击缩放，同时受可否缩放控制。
	 */
	@SimpleProperty
	public void 双击缩放(boolean value) {
		doubleTap = value;
	}

	/**
	 * 是否允许双指同时缩放两轴。
	 *
	 * @return 是否允许双指同时缩放两轴。
	 */
	@SimpleProperty
	public boolean 捏合缩放() {
		return pinch;
	}

	/**
	 * 是否允许双指同时缩放两轴。
	 *
	 * @param value 是否允许双指同时缩放两轴。
	 */
	@SimpleProperty
	public void 捏合缩放(boolean value) {
		pinch = value;
	}

	/**
	 * 是否允许横向拖动，同时受可否拖动控制。
	 *
	 * @return 是否允许横向拖动，同时受可否拖动控制。
	 */
	@SimpleProperty
	public boolean 横向拖动() {
		return dragX;
	}

	/**
	 * 是否允许横向拖动，同时受可否拖动控制。
	 *
	 * @param value 是否允许横向拖动，同时受可否拖动控制。
	 */
	@SimpleProperty
	public void 横向拖动(boolean value) {
		dragX = value;
	}

	/**
	 * 是否允许纵向拖动，同时受可否拖动控制。
	 *
	 * @return 是否允许纵向拖动，同时受可否拖动控制。
	 */
	@SimpleProperty
	public boolean 纵向拖动() {
		return dragY;
	}

	/**
	 * 是否允许纵向拖动，同时受可否拖动控制。
	 *
	 * @param value 是否允许纵向拖动，同时受可否拖动控制。
	 */
	@SimpleProperty
	public void 纵向拖动(boolean value) {
		dragY = value;
	}

	/**
	 * 饼图或雷达图是否允许手势旋转。
	 *
	 * @return 饼图或雷达图是否允许手势旋转。
	 */
	@SimpleProperty
	public boolean 饼雷达旋转() {
		return rotation;
	}

	/**
	 * 饼图或雷达图是否允许手势旋转。
	 *
	 * @param value 饼图或雷达图是否允许手势旋转。
	 */
	@SimpleProperty
	public void 饼雷达旋转(boolean value) {
		rotation = value;
	}

	/**
	 * 饼图或雷达图的初始旋转角度。
	 *
	 * @return 饼图或雷达图的初始旋转角度。
	 */
	@SimpleProperty
	public float 旋转角度() {
		return rotationAngle;
	}

	/**
	 * 饼图或雷达图的初始旋转角度。
	 *
	 * @param value 饼图或雷达图的初始旋转角度。
	 * @throws IllegalArgumentException 数值、文本或数组不符合此属性约束。
	 */
	@SimpleProperty
	public void 旋转角度(float value) {
		ChartChecks.range(value, -360000f, 360000f);
		rotationAngle = value;
	}

	/**
	 * 直角坐标图同时绘制数值标注的上限。
	 *
	 * @return 直角坐标图同时绘制数值标注的上限。
	 */
	@SimpleProperty
	public int 最大标注数量() {
		return maxValues;
	}

	/**
	 * 直角坐标图同时绘制数值标注的上限。
	 *
	 * @param value 直角坐标图同时绘制数值标注的上限。
	 * @throws IllegalArgumentException 数值、文本或数组不符合此属性约束。
	 */
	@SimpleProperty
	public void 最大标注数量(int value) {
		ChartChecks.integer(value, 0, 1000000);
		maxValues = value;
	}

	/**
	 * 图表文字字体资产路径；空文本使用系统默认。
	 *
	 * @return 图表文字字体资产路径；空文本使用系统默认。
	 */
	@SimpleProperty
	public String 字体资产() {
		return font;
	}

	/**
	 * 图表文字字体资产路径；空文本使用系统默认。
	 *
	 * @param value 图表文字字体资产路径；空文本使用系统默认。
	 * @throws IllegalArgumentException 数值、文本或数组不符合此属性约束。
	 */
	@SimpleProperty
	public void 字体资产(String value) {
		ChartChecks.text(value);
		font = value;
	}

	/**
	 * 内容区描述文字，空文本隐藏。
	 *
	 * @return 内容区描述文字，空文本隐藏。
	 */
	@SimpleProperty
	public String 描述文字() {
		return description;
	}

	/**
	 * 内容区描述文字，空文本隐藏。
	 *
	 * @param value 内容区描述文字，空文本隐藏。
	 * @throws IllegalArgumentException 数值、文本或数组不符合此属性约束。
	 */
	@SimpleProperty
	public void 描述文字(String value) {
		ChartChecks.text(value);
		description = value;
	}

	/**
	 * 描述文字大小，单位 SP。
	 *
	 * @return 描述文字大小，单位 SP。
	 */
	@SimpleProperty
	public float 描述大小() {
		return descriptionSize;
	}

	/**
	 * 描述文字大小，单位 SP。
	 *
	 * @param value 描述文字大小，单位 SP。
	 * @throws IllegalArgumentException 数值、文本或数组不符合此属性约束。
	 */
	@SimpleProperty
	public void 描述大小(float value) {
		ChartChecks.range(value, 6f, 100f);
		descriptionSize = value;
	}

	/**
	 * ARGB 颜色。
	 *
	 * @return ARGB 颜色。
	 */
	@SimpleProperty
	public int 描述颜色() {
		return descriptionColor;
	}

	/**
	 * ARGB 颜色。
	 *
	 * @param value ARGB 颜色。
	 */
	@SimpleProperty
	public void 描述颜色(int value) {
		descriptionColor = value;
	}

	/**
	 * 返回独立的配置副本，回调接口不复制。
	 *
	 * @return 返回独立的配置副本，回调接口不复制。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	@SimpleFunction
	public MPAC图表样式 复制() {
		MPAC图表样式 result = new MPAC图表样式();
		result.noData = noData;
		result.touch = touch;
		result.tapHighlight = tapHighlight;
		result.dragHighlight = dragHighlight;
		result.markers = markers;
		result.markerColor = markerColor;
		result.markerTextColor = markerTextColor;
		result.markerSize = markerSize;
		result.deceleration = deceleration;
		result.friction = friction;
		result.left = left;
		result.top = top;
		result.right = right;
		result.bottom = bottom;
		result.borders = borders;
		result.borderColor = borderColor;
		result.borderWidth = borderWidth;
		result.gridBackground = gridBackground;
		result.gridColor = gridColor;
		result.autoScale = autoScale;
		result.scaleX = scaleX;
		result.scaleY = scaleY;
		result.doubleTap = doubleTap;
		result.pinch = pinch;
		result.dragX = dragX;
		result.dragY = dragY;
		result.rotation = rotation;
		result.rotationAngle = rotationAngle;
		result.maxValues = maxValues;
		result.font = font;
		result.description = description;
		result.descriptionSize = descriptionSize;
		result.descriptionColor = descriptionColor;
		result.valuesAbove = valuesAbove;
		result.barShadows = barShadows;
		result.fullBar = fullBar;
		return result;
	}

	/**
	 * 是否将柱状图数值绘制在柱顶外侧。
	 */
	boolean valuesAbove = true;

	/**
	 * 是否将柱状图数值绘制在柱顶外侧。
	 *
	 * @return 是否将柱状图数值绘制在柱顶外侧。
	 */
	@SimpleProperty
	public boolean 柱值置顶() {
		return valuesAbove;
	}

	/**
	 * 是否将柱状图数值绘制在柱顶外侧。
	 *
	 * @param value 是否将柱状图数值绘制在柱顶外侧。
	 */
	@SimpleProperty
	public void 柱值置顶(boolean value) {
		valuesAbove = value;
	}

	/**
	 * 是否在柱后绘制满高阴影。
	 */
	boolean barShadows = false;

	/**
	 * 是否在柱后绘制满高阴影。
	 *
	 * @return 是否在柱后绘制满高阴影。
	 */
	@SimpleProperty
	public boolean 柱阴影() {
		return barShadows;
	}

	/**
	 * 是否在柱后绘制满高阴影。
	 *
	 * @param value 是否在柱后绘制满高阴影。
	 */
	@SimpleProperty
	public void 柱阴影(boolean value) {
		barShadows = value;
	}

	/**
	 * 堆叠柱是否整体高亮；关闭时高亮点击的层。
	 */
	boolean fullBar = false;

	/**
	 * 堆叠柱是否整体高亮；关闭时高亮点击的层。
	 *
	 * @return 堆叠柱是否整体高亮；关闭时高亮点击的层。
	 */
	@SimpleProperty
	public boolean 整柱高亮() {
		return fullBar;
	}

	/**
	 * 堆叠柱是否整体高亮；关闭时高亮点击的层。
	 *
	 * @param value 堆叠柱是否整体高亮；关闭时高亮点击的层。
	 */
	@SimpleProperty
	public void 整柱高亮(boolean value) {
		fullBar = value;
	}
}
