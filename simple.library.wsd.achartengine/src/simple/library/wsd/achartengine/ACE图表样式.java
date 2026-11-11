package simple.library.wsd.achartengine;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.SimpleProperty;

/**
 * ACE图表样式，应用时复制配置，不向调用方暴露绘制器。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class ACE图表样式 {

	/**
	 * 是否显示数据组图例。
	 */
	boolean legend = true;

	/**
	 * 是否显示项目和轴标签。
	 */
	boolean labels = true;

	/**
	 * 是否显示直角坐标轴。
	 */
	boolean axes = true;

	/**
	 * 是否绘制水平网格。
	 */
	boolean gridX = false;

	/**
	 * 是否绘制垂直网格。
	 */
	boolean gridY = false;

	/**
	 * 是否为自定义文字标签绘制网格。
	 */
	boolean customGrid = false;

	/**
	 * ARGB 颜色。
	 */
	int textColor = -16777216;

	/**
	 * 轴标签大小，单位 SP。
	 */
	float textSize = 12f;

	/**
	 * 图表标题大小，单位 SP。
	 */
	float titleSize = 14f;

	/**
	 * 横纵轴标题大小，单位 SP。
	 */
	float axisTitleSize = 12f;

	/**
	 * 图例文字大小，单位 SP。
	 */
	float legendSize = 12f;

	/**
	 * ARGB 颜色。
	 */
	int axesColor = -7829368;

	/**
	 * ARGB 颜色。
	 */
	int gridColor = -3355444;

	/**
	 * 是否按图例条目计算所需高度。
	 */
	boolean fitLegend = true;

	/**
	 * 图例固定高度，单位 DIP，0 使用自动高度。
	 */
	int legendHeight = 0;

	/**
	 * 图表内容上边距，单位 DIP。
	 */
	float top = 32f;

	/**
	 * 图表内容左边距，单位 DIP。
	 */
	float left = 56f;

	/**
	 * 图表内容下边距，单位 DIP。
	 */
	float bottom = 48f;

	/**
	 * 图表内容右边距，单位 DIP。
	 */
	float right = 36f;

	/**
	 * 是否开启抗锯齿。
	 */
	boolean antialias = true;

	/**
	 * 是否允许横向拖动。
	 */
	boolean panX = true;

	/**
	 * 是否允许纵向拖动。
	 */
	boolean panY = true;

	/**
	 * 是否允许横向手势缩放。
	 */
	boolean zoomX = true;

	/**
	 * 是否允许纵向手势缩放。
	 */
	boolean zoomY = true;

	/**
	 * 是否显示上游内置放大、缩小和复位按钮。
	 */
	boolean zoomButtons = false;

	/**
	 * 内置放大和缩小按钮的倍率，须大于 1。
	 */
	float zoomRate = 1.5f;

	/**
	 * 是否允许点击选择；原生点选适用于饼图和坐标图。
	 */
	boolean click = true;

	/**
	 * 点的点击容差，单位 DIP。
	 */
	float buffer = 15f;

	/**
	 * 坐标图：0 纵向，1 横向；时间图建议纵向。
	 */
	int orientation = 0;

	/**
	 * 折线及散点标记大小，单位 DIP。
	 */
	float pointSize = 4f;

	/**
	 * 柱宽间距系数，越大柱越细。
	 */
	double barSpace = 0.4;

	/**
	 * 饼图、圆环及仪表盘的半径比例。
	 */
	float scale = 1f;

	/**
	 * 饼图和圆环开始绘制的角度。
	 */
	float startAngle = 0f;

	/**
	 * 系统字体族名称。
	 */
	String font = "sans-serif";

	/**
	 * 0 常规，1 粗体，2 斜体，3 粗斜体。
	 */
	int fontStyle = 0;

	/**
	 * 主动选择或原生点选后是否显示内置数值提示。
	 */
	boolean selection = true;

	/**
	 * 创建默认配置。
	 */
	public ACE图表样式() {
	}

	/**
	 * 是否显示数据组图例。
	 *
	 * @return 是否显示数据组图例。
	 */
	@SimpleProperty
	public boolean 显示图例() {
		return legend;
	}

	/**
	 * 是否显示数据组图例。
	 *
	 * @param value 是否显示数据组图例。
	 */
	@SimpleProperty
	public void 显示图例(boolean value) {
		legend = value;
	}

	/**
	 * 是否显示项目和轴标签。
	 *
	 * @return 是否显示项目和轴标签。
	 */
	@SimpleProperty
	public boolean 显示标签() {
		return labels;
	}

	/**
	 * 是否显示项目和轴标签。
	 *
	 * @param value 是否显示项目和轴标签。
	 */
	@SimpleProperty
	public void 显示标签(boolean value) {
		labels = value;
	}

	/**
	 * 是否显示直角坐标轴。
	 *
	 * @return 是否显示直角坐标轴。
	 */
	@SimpleProperty
	public boolean 显示坐标轴() {
		return axes;
	}

	/**
	 * 是否显示直角坐标轴。
	 *
	 * @param value 是否显示直角坐标轴。
	 */
	@SimpleProperty
	public void 显示坐标轴(boolean value) {
		axes = value;
	}

	/**
	 * 是否绘制水平网格。
	 *
	 * @return 是否绘制水平网格。
	 */
	@SimpleProperty
	public boolean 横向网格() {
		return gridX;
	}

	/**
	 * 是否绘制水平网格。
	 *
	 * @param value 是否绘制水平网格。
	 */
	@SimpleProperty
	public void 横向网格(boolean value) {
		gridX = value;
	}

	/**
	 * 是否绘制垂直网格。
	 *
	 * @return 是否绘制垂直网格。
	 */
	@SimpleProperty
	public boolean 纵向网格() {
		return gridY;
	}

	/**
	 * 是否绘制垂直网格。
	 *
	 * @param value 是否绘制垂直网格。
	 */
	@SimpleProperty
	public void 纵向网格(boolean value) {
		gridY = value;
	}

	/**
	 * 是否为自定义文字标签绘制网格。
	 *
	 * @return 是否为自定义文字标签绘制网格。
	 */
	@SimpleProperty
	public boolean 标签网格() {
		return customGrid;
	}

	/**
	 * 是否为自定义文字标签绘制网格。
	 *
	 * @param value 是否为自定义文字标签绘制网格。
	 */
	@SimpleProperty
	public void 标签网格(boolean value) {
		customGrid = value;
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
	 * 轴标签大小，单位 SP。
	 *
	 * @return 轴标签大小，单位 SP。
	 */
	@SimpleProperty
	public float 文字大小() {
		return textSize;
	}

	/**
	 * 轴标签大小，单位 SP。
	 *
	 * @param value 轴标签大小，单位 SP。
	 */
	@SimpleProperty
	public void 文字大小(float value) {
		Checks.range(value, 6, 100);
		textSize = value;
	}

	/**
	 * 图表标题大小，单位 SP。
	 *
	 * @return 图表标题大小，单位 SP。
	 */
	@SimpleProperty
	public float 标题大小() {
		return titleSize;
	}

	/**
	 * 图表标题大小，单位 SP。
	 *
	 * @param value 图表标题大小，单位 SP。
	 */
	@SimpleProperty
	public void 标题大小(float value) {
		Checks.range(value, 6, 100);
		titleSize = value;
	}

	/**
	 * 横纵轴标题大小，单位 SP。
	 *
	 * @return 横纵轴标题大小，单位 SP。
	 */
	@SimpleProperty
	public float 轴标题大小() {
		return axisTitleSize;
	}

	/**
	 * 横纵轴标题大小，单位 SP。
	 *
	 * @param value 横纵轴标题大小，单位 SP。
	 */
	@SimpleProperty
	public void 轴标题大小(float value) {
		Checks.range(value, 6, 100);
		axisTitleSize = value;
	}

	/**
	 * 图例文字大小，单位 SP。
	 *
	 * @return 图例文字大小，单位 SP。
	 */
	@SimpleProperty
	public float 图例大小() {
		return legendSize;
	}

	/**
	 * 图例文字大小，单位 SP。
	 *
	 * @param value 图例文字大小，单位 SP。
	 */
	@SimpleProperty
	public void 图例大小(float value) {
		Checks.range(value, 6, 100);
		legendSize = value;
	}

	/**
	 * ARGB 颜色。
	 *
	 * @return ARGB 颜色。
	 */
	@SimpleProperty
	public int 坐标轴颜色() {
		return axesColor;
	}

	/**
	 * ARGB 颜色。
	 *
	 * @param value ARGB 颜色。
	 */
	@SimpleProperty
	public void 坐标轴颜色(int value) {
		axesColor = value;
	}

	/**
	 * ARGB 颜色。
	 *
	 * @return ARGB 颜色。
	 */
	@SimpleProperty
	public int 网格颜色() {
		return gridColor;
	}

	/**
	 * ARGB 颜色。
	 *
	 * @param value ARGB 颜色。
	 */
	@SimpleProperty
	public void 网格颜色(int value) {
		gridColor = value;
	}

	/**
	 * 是否按图例条目计算所需高度。
	 *
	 * @return 是否按图例条目计算所需高度。
	 */
	@SimpleProperty
	public boolean 图例自适应() {
		return fitLegend;
	}

	/**
	 * 是否按图例条目计算所需高度。
	 *
	 * @param value 是否按图例条目计算所需高度。
	 */
	@SimpleProperty
	public void 图例自适应(boolean value) {
		fitLegend = value;
	}

	/**
	 * 图例固定高度，单位 DIP，0 使用自动高度。
	 *
	 * @return 图例固定高度，单位 DIP，0 使用自动高度。
	 */
	@SimpleProperty
	public int 图例高度() {
		return legendHeight;
	}

	/**
	 * 图例固定高度，单位 DIP，0 使用自动高度。
	 *
	 * @param value 图例固定高度，单位 DIP，0 使用自动高度。
	 */
	@SimpleProperty
	public void 图例高度(int value) {
		Checks.index(value, 0, 1000);
		legendHeight = value;
	}

	/**
	 * 图表内容上边距，单位 DIP。
	 *
	 * @return 图表内容上边距，单位 DIP。
	 */
	@SimpleProperty
	public float 上边距() {
		return top;
	}

	/**
	 * 图表内容上边距，单位 DIP。
	 *
	 * @param value 图表内容上边距，单位 DIP。
	 */
	@SimpleProperty
	public void 上边距(float value) {
		Checks.range(value, 0, 100000);
		top = value;
	}

	/**
	 * 图表内容左边距，单位 DIP。
	 *
	 * @return 图表内容左边距，单位 DIP。
	 */
	@SimpleProperty
	public float 左边距() {
		return left;
	}

	/**
	 * 图表内容左边距，单位 DIP。
	 *
	 * @param value 图表内容左边距，单位 DIP。
	 */
	@SimpleProperty
	public void 左边距(float value) {
		Checks.range(value, 0, 100000);
		left = value;
	}

	/**
	 * 图表内容下边距，单位 DIP。
	 *
	 * @return 图表内容下边距，单位 DIP。
	 */
	@SimpleProperty
	public float 下边距() {
		return bottom;
	}

	/**
	 * 图表内容下边距，单位 DIP。
	 *
	 * @param value 图表内容下边距，单位 DIP。
	 */
	@SimpleProperty
	public void 下边距(float value) {
		Checks.range(value, 0, 100000);
		bottom = value;
	}

	/**
	 * 图表内容右边距，单位 DIP。
	 *
	 * @return 图表内容右边距，单位 DIP。
	 */
	@SimpleProperty
	public float 右边距() {
		return right;
	}

	/**
	 * 图表内容右边距，单位 DIP。
	 *
	 * @param value 图表内容右边距，单位 DIP。
	 */
	@SimpleProperty
	public void 右边距(float value) {
		Checks.range(value, 0, 100000);
		right = value;
	}

	/**
	 * 是否开启抗锯齿。
	 *
	 * @return 是否开启抗锯齿。
	 */
	@SimpleProperty
	public boolean 抗锯齿() {
		return antialias;
	}

	/**
	 * 是否开启抗锯齿。
	 *
	 * @param value 是否开启抗锯齿。
	 */
	@SimpleProperty
	public void 抗锯齿(boolean value) {
		antialias = value;
	}

	/**
	 * 是否允许横向拖动。
	 *
	 * @return 是否允许横向拖动。
	 */
	@SimpleProperty
	public boolean 横向拖动() {
		return panX;
	}

	/**
	 * 是否允许横向拖动。
	 *
	 * @param value 是否允许横向拖动。
	 */
	@SimpleProperty
	public void 横向拖动(boolean value) {
		panX = value;
	}

	/**
	 * 是否允许纵向拖动。
	 *
	 * @return 是否允许纵向拖动。
	 */
	@SimpleProperty
	public boolean 纵向拖动() {
		return panY;
	}

	/**
	 * 是否允许纵向拖动。
	 *
	 * @param value 是否允许纵向拖动。
	 */
	@SimpleProperty
	public void 纵向拖动(boolean value) {
		panY = value;
	}

	/**
	 * 是否允许横向手势缩放。
	 *
	 * @return 是否允许横向手势缩放。
	 */
	@SimpleProperty
	public boolean 横向缩放() {
		return zoomX;
	}

	/**
	 * 是否允许横向手势缩放。
	 *
	 * @param value 是否允许横向手势缩放。
	 */
	@SimpleProperty
	public void 横向缩放(boolean value) {
		zoomX = value;
	}

	/**
	 * 是否允许纵向手势缩放。
	 *
	 * @return 是否允许纵向手势缩放。
	 */
	@SimpleProperty
	public boolean 纵向缩放() {
		return zoomY;
	}

	/**
	 * 是否允许纵向手势缩放。
	 *
	 * @param value 是否允许纵向手势缩放。
	 */
	@SimpleProperty
	public void 纵向缩放(boolean value) {
		zoomY = value;
	}

	/**
	 * 是否显示上游内置放大、缩小和复位按钮。
	 *
	 * @return 是否显示上游内置放大、缩小和复位按钮。
	 */
	@SimpleProperty
	public boolean 缩放按钮() {
		return zoomButtons;
	}

	/**
	 * 是否显示上游内置放大、缩小和复位按钮。
	 *
	 * @param value 是否显示上游内置放大、缩小和复位按钮。
	 */
	@SimpleProperty
	public void 缩放按钮(boolean value) {
		zoomButtons = value;
	}

	/**
	 * 内置放大和缩小按钮的倍率，须大于 1。
	 *
	 * @return 内置放大和缩小按钮的倍率，须大于 1。
	 */
	@SimpleProperty
	public float 缩放倍率() {
		return zoomRate;
	}

	/**
	 * 内置放大和缩小按钮的倍率，须大于 1。
	 *
	 * @param value 内置放大和缩小按钮的倍率，须大于 1。
	 */
	@SimpleProperty
	public void 缩放倍率(float value) {
		Checks.range(value, 1.01, 100);
		zoomRate = value;
	}

	/**
	 * 是否允许点击选择；原生点选适用于饼图和坐标图。
	 *
	 * @return 是否允许点击选择；原生点选适用于饼图和坐标图。
	 */
	@SimpleProperty
	public boolean 允许选择() {
		return click;
	}

	/**
	 * 是否允许点击选择；原生点选适用于饼图和坐标图。
	 *
	 * @param value 是否允许点击选择；原生点选适用于饼图和坐标图。
	 */
	@SimpleProperty
	public void 允许选择(boolean value) {
		click = value;
	}

	/**
	 * 点的点击容差，单位 DIP。
	 *
	 * @return 点的点击容差，单位 DIP。
	 */
	@SimpleProperty
	public float 点选距离() {
		return buffer;
	}

	/**
	 * 点的点击容差，单位 DIP。
	 *
	 * @param value 点的点击容差，单位 DIP。
	 */
	@SimpleProperty
	public void 点选距离(float value) {
		Checks.range(value, 1, 1000);
		buffer = value;
	}

	/**
	 * 坐标图：0 纵向，1 横向；时间图建议纵向。
	 *
	 * @return 坐标图：0 纵向，1 横向；时间图建议纵向。
	 */
	@SimpleProperty
	public int 图形方向() {
		return orientation;
	}

	/**
	 * 坐标图：0 纵向，1 横向；时间图建议纵向。
	 *
	 * @param value 坐标图：0 纵向，1 横向；时间图建议纵向。
	 */
	@SimpleProperty
	public void 图形方向(int value) {
		Checks.index(value, 0, 1);
		orientation = value;
	}

	/**
	 * 折线及散点标记大小，单位 DIP。
	 *
	 * @return 折线及散点标记大小，单位 DIP。
	 */
	@SimpleProperty
	public float 点大小() {
		return pointSize;
	}

	/**
	 * 折线及散点标记大小，单位 DIP。
	 *
	 * @param value 折线及散点标记大小，单位 DIP。
	 */
	@SimpleProperty
	public void 点大小(float value) {
		Checks.range(value, 0.1, 100);
		pointSize = value;
	}

	/**
	 * 柱宽间距系数，越大柱越细。
	 *
	 * @return 柱宽间距系数，越大柱越细。
	 */
	@SimpleProperty
	public double 柱间距() {
		return barSpace;
	}

	/**
	 * 柱宽间距系数，越大柱越细。
	 *
	 * @param value 柱宽间距系数，越大柱越细。
	 */
	@SimpleProperty
	public void 柱间距(double value) {
		Checks.range(value, 0, 100);
		barSpace = value;
	}

	/**
	 * 饼图、圆环及仪表盘的半径比例。
	 *
	 * @return 饼图、圆环及仪表盘的半径比例。
	 */
	@SimpleProperty
	public float 圆图比例() {
		return scale;
	}

	/**
	 * 饼图、圆环及仪表盘的半径比例。
	 *
	 * @param value 饼图、圆环及仪表盘的半径比例。
	 */
	@SimpleProperty
	public void 圆图比例(float value) {
		Checks.range(value, 0.01, 10);
		scale = value;
	}

	/**
	 * 饼图和圆环开始绘制的角度。
	 *
	 * @return 饼图和圆环开始绘制的角度。
	 */
	@SimpleProperty
	public float 起始角度() {
		return startAngle;
	}

	/**
	 * 饼图和圆环开始绘制的角度。
	 *
	 * @param value 饼图和圆环开始绘制的角度。
	 */
	@SimpleProperty
	public void 起始角度(float value) {
		Checks.range(value, -360, 360);
		startAngle = value;
	}

	/**
	 * 系统字体族名称。
	 *
	 * @return 系统字体族名称。
	 */
	@SimpleProperty
	public String 字体名称() {
		return font;
	}

	/**
	 * 系统字体族名称。
	 *
	 * @param value 系统字体族名称。
	 */
	@SimpleProperty
	public void 字体名称(String value) {
		Checks.text(value);
		font = value;
	}

	/**
	 * 0 常规，1 粗体，2 斜体，3 粗斜体。
	 *
	 * @return 0 常规，1 粗体，2 斜体，3 粗斜体。
	 */
	@SimpleProperty
	public int 字体风格() {
		return fontStyle;
	}

	/**
	 * 0 常规，1 粗体，2 斜体，3 粗斜体。
	 *
	 * @param value 0 常规，1 粗体，2 斜体，3 粗斜体。
	 */
	@SimpleProperty
	public void 字体风格(int value) {
		Checks.index(value, 0, 3);
		fontStyle = value;
	}

	/**
	 * 主动选择或原生点选后是否显示内置数值提示。
	 *
	 * @return 主动选择或原生点选后是否显示内置数值提示。
	 */
	@SimpleProperty
	public boolean 显示选择提示() {
		return selection;
	}

	/**
	 * 主动选择或原生点选后是否显示内置数值提示。
	 *
	 * @param value 主动选择或原生点选后是否显示内置数值提示。
	 */
	@SimpleProperty
	public void 显示选择提示(boolean value) {
		selection = value;
	}

	/**
	 * 返回独立的配置副本。
	 *
	 * @return 返回独立的配置副本。
	 */
	@SimpleFunction
	public ACE图表样式 复制() {
		ACE图表样式 result = new ACE图表样式();
		result.legend = legend;
		result.labels = labels;
		result.axes = axes;
		result.gridX = gridX;
		result.gridY = gridY;
		result.customGrid = customGrid;
		result.textColor = textColor;
		result.textSize = textSize;
		result.titleSize = titleSize;
		result.axisTitleSize = axisTitleSize;
		result.legendSize = legendSize;
		result.axesColor = axesColor;
		result.gridColor = gridColor;
		result.fitLegend = fitLegend;
		result.legendHeight = legendHeight;
		result.top = top;
		result.left = left;
		result.bottom = bottom;
		result.right = right;
		result.antialias = antialias;
		result.panX = panX;
		result.panY = panY;
		result.zoomX = zoomX;
		result.zoomY = zoomY;
		result.zoomButtons = zoomButtons;
		result.zoomRate = zoomRate;
		result.click = click;
		result.buffer = buffer;
		result.orientation = orientation;
		result.pointSize = pointSize;
		result.barSpace = barSpace;
		result.scale = scale;
		result.startAngle = startAngle;
		result.font = font;
		result.fontStyle = fontStyle;
		result.selection = selection;
		result.centerX = centerX;
		result.centerY = centerY;
		return result;
	}

	/**
	 * 饼、圆环和仪表中心的横坐标，单位 DIP；负数自动定位。
	 */
	float centerX = -1f;

	/**
	 * 饼、圆环和仪表中心的横坐标，单位 DIP；负数自动定位。
	 *
	 * @return 饼、圆环和仪表中心的横坐标，单位 DIP；负数自动定位。
	 */
	@SimpleProperty
	public float 圆图中心横坐标() {
		return centerX;
	}

	/**
	 * 饼、圆环和仪表中心的横坐标，单位 DIP；负数自动定位。
	 *
	 * @param value 饼、圆环和仪表中心的横坐标，单位 DIP；负数自动定位。
	 */
	@SimpleProperty
	public void 圆图中心横坐标(float value) {
		Checks.range(value, -1, 100000);
		centerX = value;
	}

	/**
	 * 饼、圆环和仪表中心的纵坐标，单位 DIP；负数自动定位。
	 */
	float centerY = -1f;

	/**
	 * 饼、圆环和仪表中心的纵坐标，单位 DIP；负数自动定位。
	 *
	 * @return 饼、圆环和仪表中心的纵坐标，单位 DIP；负数自动定位。
	 */
	@SimpleProperty
	public float 圆图中心纵坐标() {
		return centerY;
	}

	/**
	 * 饼、圆环和仪表中心的纵坐标，单位 DIP；负数自动定位。
	 *
	 * @param value 饼、圆环和仪表中心的纵坐标，单位 DIP；负数自动定位。
	 */
	@SimpleProperty
	public void 圆图中心纵坐标(float value) {
		Checks.range(value, -1, 100000);
		centerY = value;
	}
}
