package simple.library.wsd.mpandroidchart;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.SimpleProperty;

/**
 * MPAC轴样式；应用到图表时复制配置，之后修改此对象不影响已有图表。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class MPAC轴样式 {

	/**
	 * 是否显示该轴。
	 */
	boolean enabled = true;

	/**
	 * 是否显示轴刻度文字。
	 */
	boolean labels = true;

	/**
	 * 是否绘制网格线。
	 */
	boolean grid = true;

	/**
	 * 是否绘制轴线。
	 */
	boolean axisLine = true;

	/**
	 * ARGB 颜色。
	 */
	int textColor = -16777216;

	/**
	 * 文字大小，单位 SP。
	 */
	float textSize = 12f;

	/**
	 * ARGB 颜色。
	 */
	int gridColor = -3355444;

	/**
	 * 网格线宽，单位 DIP。
	 */
	float gridWidth = 1f;

	/**
	 * ARGB 颜色。
	 */
	int axisColor = -7829368;

	/**
	 * 轴线宽，单位 DIP。
	 */
	float axisWidth = 1f;

	/**
	 * 建议标签数，2 至 25。
	 */
	int labelCount = 6;

	/**
	 * 是否强制使用指定标签数。
	 */
	boolean forceLabels = false;

	/**
	 * 相邻刻度最小差值，必须大于零。
	 */
	float granularity = 1f;

	/**
	 * 是否让刻度位于分组中心。
	 */
	boolean centerLabels = false;

	/**
	 * 横轴标签旋转角度。
	 */
	float rotation = 0f;

	/**
	 * 横轴：0 顶部，1 底部，2 两侧，3 顶部内侧，4 底部内侧。
	 */
	int xPosition = 1;

	/**
	 * 数值轴：0 外侧，1 内侧。
	 */
	int yPosition = 0;

	/**
	 * 仅直角坐标数值轴：是否反向增长。
	 */
	boolean inverted = false;

	/**
	 * 仅直角坐标数值轴：是否绘制零刻度线。
	 */
	boolean zeroLine = false;

	/**
	 * ARGB 颜色。
	 */
	int zeroColor = -7829368;

	/**
	 * 零线宽度，单位 DIP。
	 */
	float zeroWidth = 1f;

	/**
	 * 仅数值轴：顶部留白百分比。
	 */
	float spaceTop = 10f;

	/**
	 * 仅数值轴：底部留白百分比。
	 */
	float spaceBottom = 10f;

	/**
	 * 标签横向偏移，单位 DIP。
	 */
	float xOffset = 5f;

	/**
	 * 标签纵向偏移，单位 DIP。
	 */
	float yOffset = 5f;

	/**
	 * 网格虚线段长度，0 表示实线，单位 DIP。
	 */
	float dashLength = 0f;

	/**
	 * 网格虚线空白长度，单位 DIP。
	 */
	float dashGap = 4f;

	/**
	 * 是否先绘制限制线再绘制数据。
	 */
	boolean limitsBehind = true;

	/**
	 * 已打包的字体资产路径；空文本使用系统默认字体。
	 */
	String font = "";

	/**
	 * 轴标签格式器；空引用恢复默认格式。
	 */
	MPAC格式器 formatter = null;

	/**
	 * 创建默认配置。
	 */
	public MPAC轴样式() {
	}

	/**
	 * 是否显示该轴。
	 *
	 * @return 是否显示该轴。
	 */
	@SimpleProperty
	public boolean 启用() {
		return enabled;
	}

	/**
	 * 是否显示该轴。
	 *
	 * @param value 是否显示该轴。
	 */
	@SimpleProperty
	public void 启用(boolean value) {
		enabled = value;
	}

	/**
	 * 是否显示轴刻度文字。
	 *
	 * @return 是否显示轴刻度文字。
	 */
	@SimpleProperty
	public boolean 显示标签() {
		return labels;
	}

	/**
	 * 是否显示轴刻度文字。
	 *
	 * @param value 是否显示轴刻度文字。
	 */
	@SimpleProperty
	public void 显示标签(boolean value) {
		labels = value;
	}

	/**
	 * 是否绘制网格线。
	 *
	 * @return 是否绘制网格线。
	 */
	@SimpleProperty
	public boolean 显示网格() {
		return grid;
	}

	/**
	 * 是否绘制网格线。
	 *
	 * @param value 是否绘制网格线。
	 */
	@SimpleProperty
	public void 显示网格(boolean value) {
		grid = value;
	}

	/**
	 * 是否绘制轴线。
	 *
	 * @return 是否绘制轴线。
	 */
	@SimpleProperty
	public boolean 显示轴线() {
		return axisLine;
	}

	/**
	 * 是否绘制轴线。
	 *
	 * @param value 是否绘制轴线。
	 */
	@SimpleProperty
	public void 显示轴线(boolean value) {
		axisLine = value;
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
	 * 网格线宽，单位 DIP。
	 *
	 * @return 网格线宽，单位 DIP。
	 */
	@SimpleProperty
	public float 网格宽度() {
		return gridWidth;
	}

	/**
	 * 网格线宽，单位 DIP。
	 *
	 * @param value 网格线宽，单位 DIP。
	 * @throws IllegalArgumentException 数值、文本或数组不符合此属性约束。
	 */
	@SimpleProperty
	public void 网格宽度(float value) {
		ChartChecks.range(value, 0.1f, 12f);
		gridWidth = value;
	}

	/**
	 * ARGB 颜色。
	 *
	 * @return ARGB 颜色。
	 */
	@SimpleProperty
	public int 轴线颜色() {
		return axisColor;
	}

	/**
	 * ARGB 颜色。
	 *
	 * @param value ARGB 颜色。
	 */
	@SimpleProperty
	public void 轴线颜色(int value) {
		axisColor = value;
	}

	/**
	 * 轴线宽，单位 DIP。
	 *
	 * @return 轴线宽，单位 DIP。
	 */
	@SimpleProperty
	public float 轴线宽度() {
		return axisWidth;
	}

	/**
	 * 轴线宽，单位 DIP。
	 *
	 * @param value 轴线宽，单位 DIP。
	 * @throws IllegalArgumentException 数值、文本或数组不符合此属性约束。
	 */
	@SimpleProperty
	public void 轴线宽度(float value) {
		ChartChecks.range(value, 0.1f, 12f);
		axisWidth = value;
	}

	/**
	 * 建议标签数，2 至 25。
	 *
	 * @return 建议标签数，2 至 25。
	 */
	@SimpleProperty
	public int 标签数量() {
		return labelCount;
	}

	/**
	 * 建议标签数，2 至 25。
	 *
	 * @param value 建议标签数，2 至 25。
	 * @throws IllegalArgumentException 数值、文本或数组不符合此属性约束。
	 */
	@SimpleProperty
	public void 标签数量(int value) {
		ChartChecks.integer(value, 2, 25);
		labelCount = value;
	}

	/**
	 * 是否强制使用指定标签数。
	 *
	 * @return 是否强制使用指定标签数。
	 */
	@SimpleProperty
	public boolean 强制标签数量() {
		return forceLabels;
	}

	/**
	 * 是否强制使用指定标签数。
	 *
	 * @param value 是否强制使用指定标签数。
	 */
	@SimpleProperty
	public void 强制标签数量(boolean value) {
		forceLabels = value;
	}

	/**
	 * 相邻刻度最小差值，必须大于零。
	 *
	 * @return 相邻刻度最小差值，必须大于零。
	 */
	@SimpleProperty
	public float 最小刻度间隔() {
		return granularity;
	}

	/**
	 * 相邻刻度最小差值，必须大于零。
	 *
	 * @param value 相邻刻度最小差值，必须大于零。
	 * @throws IllegalArgumentException 数值、文本或数组不符合此属性约束。
	 */
	@SimpleProperty
	public void 最小刻度间隔(float value) {
		ChartChecks.range(value, 1e-10f, 1e+30f);
		granularity = value;
	}

	/**
	 * 是否让刻度位于分组中心。
	 *
	 * @return 是否让刻度位于分组中心。
	 */
	@SimpleProperty
	public boolean 刻度居中() {
		return centerLabels;
	}

	/**
	 * 是否让刻度位于分组中心。
	 *
	 * @param value 是否让刻度位于分组中心。
	 */
	@SimpleProperty
	public void 刻度居中(boolean value) {
		centerLabels = value;
	}

	/**
	 * 横轴标签旋转角度。
	 *
	 * @return 横轴标签旋转角度。
	 */
	@SimpleProperty
	public float 标签旋转() {
		return rotation;
	}

	/**
	 * 横轴标签旋转角度。
	 *
	 * @param value 横轴标签旋转角度。
	 * @throws IllegalArgumentException 数值、文本或数组不符合此属性约束。
	 */
	@SimpleProperty
	public void 标签旋转(float value) {
		ChartChecks.range(value, -360f, 360f);
		rotation = value;
	}

	/**
	 * 横轴：0 顶部，1 底部，2 两侧，3 顶部内侧，4 底部内侧。
	 *
	 * @return 横轴：0 顶部，1 底部，2 两侧，3 顶部内侧，4 底部内侧。
	 */
	@SimpleProperty
	public int 横轴位置() {
		return xPosition;
	}

	/**
	 * 横轴：0 顶部，1 底部，2 两侧，3 顶部内侧，4 底部内侧。
	 *
	 * @param value 横轴：0 顶部，1 底部，2 两侧，3 顶部内侧，4 底部内侧。
	 * @throws IllegalArgumentException 数值、文本或数组不符合此属性约束。
	 */
	@SimpleProperty
	public void 横轴位置(int value) {
		ChartChecks.integer(value, 0, 4);
		xPosition = value;
	}

	/**
	 * 数值轴：0 外侧，1 内侧。
	 *
	 * @return 数值轴：0 外侧，1 内侧。
	 */
	@SimpleProperty
	public int 数值轴文字位置() {
		return yPosition;
	}

	/**
	 * 数值轴：0 外侧，1 内侧。
	 *
	 * @param value 数值轴：0 外侧，1 内侧。
	 * @throws IllegalArgumentException 数值、文本或数组不符合此属性约束。
	 */
	@SimpleProperty
	public void 数值轴文字位置(int value) {
		ChartChecks.integer(value, 0, 1);
		yPosition = value;
	}

	/**
	 * 仅直角坐标数值轴：是否反向增长。
	 *
	 * @return 仅直角坐标数值轴：是否反向增长。
	 */
	@SimpleProperty
	public boolean 反转数值() {
		return inverted;
	}

	/**
	 * 仅直角坐标数值轴：是否反向增长。
	 *
	 * @param value 仅直角坐标数值轴：是否反向增长。
	 */
	@SimpleProperty
	public void 反转数值(boolean value) {
		inverted = value;
	}

	/**
	 * 仅直角坐标数值轴：是否绘制零刻度线。
	 *
	 * @return 仅直角坐标数值轴：是否绘制零刻度线。
	 */
	@SimpleProperty
	public boolean 显示零线() {
		return zeroLine;
	}

	/**
	 * 仅直角坐标数值轴：是否绘制零刻度线。
	 *
	 * @param value 仅直角坐标数值轴：是否绘制零刻度线。
	 */
	@SimpleProperty
	public void 显示零线(boolean value) {
		zeroLine = value;
	}

	/**
	 * ARGB 颜色。
	 *
	 * @return ARGB 颜色。
	 */
	@SimpleProperty
	public int 零线颜色() {
		return zeroColor;
	}

	/**
	 * ARGB 颜色。
	 *
	 * @param value ARGB 颜色。
	 */
	@SimpleProperty
	public void 零线颜色(int value) {
		zeroColor = value;
	}

	/**
	 * 零线宽度，单位 DIP。
	 *
	 * @return 零线宽度，单位 DIP。
	 */
	@SimpleProperty
	public float 零线宽度() {
		return zeroWidth;
	}

	/**
	 * 零线宽度，单位 DIP。
	 *
	 * @param value 零线宽度，单位 DIP。
	 * @throws IllegalArgumentException 数值、文本或数组不符合此属性约束。
	 */
	@SimpleProperty
	public void 零线宽度(float value) {
		ChartChecks.range(value, 0.1f, 12f);
		zeroWidth = value;
	}

	/**
	 * 仅数值轴：顶部留白百分比。
	 *
	 * @return 仅数值轴：顶部留白百分比。
	 */
	@SimpleProperty
	public float 顶部留白() {
		return spaceTop;
	}

	/**
	 * 仅数值轴：顶部留白百分比。
	 *
	 * @param value 仅数值轴：顶部留白百分比。
	 * @throws IllegalArgumentException 数值、文本或数组不符合此属性约束。
	 */
	@SimpleProperty
	public void 顶部留白(float value) {
		ChartChecks.range(value, 0f, 100f);
		spaceTop = value;
	}

	/**
	 * 仅数值轴：底部留白百分比。
	 *
	 * @return 仅数值轴：底部留白百分比。
	 */
	@SimpleProperty
	public float 底部留白() {
		return spaceBottom;
	}

	/**
	 * 仅数值轴：底部留白百分比。
	 *
	 * @param value 仅数值轴：底部留白百分比。
	 * @throws IllegalArgumentException 数值、文本或数组不符合此属性约束。
	 */
	@SimpleProperty
	public void 底部留白(float value) {
		ChartChecks.range(value, 0f, 100f);
		spaceBottom = value;
	}

	/**
	 * 标签横向偏移，单位 DIP。
	 *
	 * @return 标签横向偏移，单位 DIP。
	 */
	@SimpleProperty
	public float 横向偏移() {
		return xOffset;
	}

	/**
	 * 标签横向偏移，单位 DIP。
	 *
	 * @param value 标签横向偏移，单位 DIP。
	 * @throws IllegalArgumentException 数值、文本或数组不符合此属性约束。
	 */
	@SimpleProperty
	public void 横向偏移(float value) {
		ChartChecks.range(value, -1000f, 1000f);
		xOffset = value;
	}

	/**
	 * 标签纵向偏移，单位 DIP。
	 *
	 * @return 标签纵向偏移，单位 DIP。
	 */
	@SimpleProperty
	public float 纵向偏移() {
		return yOffset;
	}

	/**
	 * 标签纵向偏移，单位 DIP。
	 *
	 * @param value 标签纵向偏移，单位 DIP。
	 * @throws IllegalArgumentException 数值、文本或数组不符合此属性约束。
	 */
	@SimpleProperty
	public void 纵向偏移(float value) {
		ChartChecks.range(value, -1000f, 1000f);
		yOffset = value;
	}

	/**
	 * 网格虚线段长度，0 表示实线，单位 DIP。
	 *
	 * @return 网格虚线段长度，0 表示实线，单位 DIP。
	 */
	@SimpleProperty
	public float 网格虚线长度() {
		return dashLength;
	}

	/**
	 * 网格虚线段长度，0 表示实线，单位 DIP。
	 *
	 * @param value 网格虚线段长度，0 表示实线，单位 DIP。
	 * @throws IllegalArgumentException 数值、文本或数组不符合此属性约束。
	 */
	@SimpleProperty
	public void 网格虚线长度(float value) {
		ChartChecks.range(value, 0f, 100000f);
		dashLength = value;
	}

	/**
	 * 网格虚线空白长度，单位 DIP。
	 *
	 * @return 网格虚线空白长度，单位 DIP。
	 */
	@SimpleProperty
	public float 网格虚线间隔() {
		return dashGap;
	}

	/**
	 * 网格虚线空白长度，单位 DIP。
	 *
	 * @param value 网格虚线空白长度，单位 DIP。
	 * @throws IllegalArgumentException 数值、文本或数组不符合此属性约束。
	 */
	@SimpleProperty
	public void 网格虚线间隔(float value) {
		ChartChecks.range(value, 0.1f, 100000f);
		dashGap = value;
	}

	/**
	 * 是否先绘制限制线再绘制数据。
	 *
	 * @return 是否先绘制限制线再绘制数据。
	 */
	@SimpleProperty
	public boolean 限制线置底() {
		return limitsBehind;
	}

	/**
	 * 是否先绘制限制线再绘制数据。
	 *
	 * @param value 是否先绘制限制线再绘制数据。
	 */
	@SimpleProperty
	public void 限制线置底(boolean value) {
		limitsBehind = value;
	}

	/**
	 * 已打包的字体资产路径；空文本使用系统默认字体。
	 *
	 * @return 已打包的字体资产路径；空文本使用系统默认字体。
	 */
	@SimpleProperty
	public String 字体资产() {
		return font;
	}

	/**
	 * 已打包的字体资产路径；空文本使用系统默认字体。
	 *
	 * @param value 已打包的字体资产路径；空文本使用系统默认字体。
	 * @throws IllegalArgumentException 数值、文本或数组不符合此属性约束。
	 */
	@SimpleProperty
	public void 字体资产(String value) {
		ChartChecks.text(value);
		font = value;
	}

	/**
	 * 轴标签格式器；空引用恢复默认格式。
	 *
	 * @return 轴标签格式器；空引用恢复默认格式。
	 */
	@SimpleProperty
	public MPAC格式器 格式器() {
		return formatter;
	}

	/**
	 * 轴标签格式器；空引用恢复默认格式。
	 *
	 * @param value 轴标签格式器；空引用恢复默认格式。
	 */
	@SimpleProperty
	public void 格式器(MPAC格式器 value) {
		formatter = value;
	}

	/**
	 * 返回独立的配置副本，回调接口不复制。
	 *
	 * @return 返回独立的配置副本，回调接口不复制。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	@SimpleFunction
	public MPAC轴样式 复制() {
		MPAC轴样式 result = new MPAC轴样式();
		result.enabled = enabled;
		result.labels = labels;
		result.grid = grid;
		result.axisLine = axisLine;
		result.textColor = textColor;
		result.textSize = textSize;
		result.gridColor = gridColor;
		result.gridWidth = gridWidth;
		result.axisColor = axisColor;
		result.axisWidth = axisWidth;
		result.labelCount = labelCount;
		result.forceLabels = forceLabels;
		result.granularity = granularity;
		result.centerLabels = centerLabels;
		result.rotation = rotation;
		result.xPosition = xPosition;
		result.yPosition = yPosition;
		result.inverted = inverted;
		result.zeroLine = zeroLine;
		result.zeroColor = zeroColor;
		result.zeroWidth = zeroWidth;
		result.spaceTop = spaceTop;
		result.spaceBottom = spaceBottom;
		result.xOffset = xOffset;
		result.yOffset = yOffset;
		result.dashLength = dashLength;
		result.dashGap = dashGap;
		result.limitsBehind = limitsBehind;
		result.font = font;
		result.formatter = formatter;
		for (MPAC限制线 line : limits) {
			result.limits.add(line.复制());
		}
		return result;
	}

	/**
	 * 限制线配置的独立快照。
	 */
	final java.util.List<MPAC限制线> limits = new java.util.ArrayList<MPAC限制线>();

	/**
	 * 复制并添加一条限制线。
	 *
	 * @param line 要复制并加入的限制线。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	@SimpleFunction
	public void 添加限制线(MPAC限制线 line) {
		ChartChecks.required(line);
		limits.add(line.复制());
	}

	/**
	 * 移除此轴配置的全部限制线。
	 *
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	@SimpleFunction
	public void 清空限制线() {
		limits.clear();
	}
}
