package simple.library.wsd.mpandroidchart;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.SimpleProperty;

/**
 * MPAC图例样式；应用到图表时复制配置，之后修改此对象不影响已有图表。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class MPAC图例样式 {

	/**
	 * 文字大小，单位 SP。
	 */
	float textSize = 12f;

	/**
	 * ARGB 颜色。
	 */
	int textColor = -16777216;

	/**
	 * 0 左，1 中，2 右。
	 */
	int horizontal = 0;

	/**
	 * 0 上，1 中，2 下。
	 */
	int vertical = 2;

	/**
	 * 0 横向，1 纵向。
	 */
	int orientation = 0;

	/**
	 * 是否绘制在图表内容区内。
	 */
	boolean inside = false;

	/**
	 * 是否允许图例自动换行。
	 */
	boolean wrap = true;

	/**
	 * 0 从左到右，1 从右到左。
	 */
	int direction = 0;

	/**
	 * 0 无图形，1 空位，2 默认，3 方形，4 圆形，5 线。
	 */
	int form = 3;

	/**
	 * 图例图形大小，单位 DIP。
	 */
	float formSize = 8f;

	/**
	 * 线形图例的线宽，单位 DIP。
	 */
	float formWidth = 2f;

	/**
	 * 图例项目之间的水平间距，单位 DIP。
	 */
	float xSpace = 6f;

	/**
	 * 图例项目之间的垂直间距，单位 DIP。
	 */
	float ySpace = 0f;

	/**
	 * 图形与文字的间距，单位 DIP。
	 */
	float formSpace = 5f;

	/**
	 * 堆叠图例图形的间距，单位 DIP。
	 */
	float stackSpace = 3f;

	/**
	 * 图例可占用空间的比例，0.01 至 1。
	 */
	float maxSize = 0.95f;

	/**
	 * 字体资产路径；空文本使用系统默认字体。
	 */
	String font = "";

	/**
	 * 创建默认配置。
	 */
	public MPAC图例样式() {
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
	 * 0 左，1 中，2 右。
	 *
	 * @return 0 左，1 中，2 右。
	 */
	@SimpleProperty
	public int 水平位置() {
		return horizontal;
	}

	/**
	 * 0 左，1 中，2 右。
	 *
	 * @param value 0 左，1 中，2 右。
	 * @throws IllegalArgumentException 数值、文本或数组不符合此属性约束。
	 */
	@SimpleProperty
	public void 水平位置(int value) {
		ChartChecks.integer(value, 0, 2);
		horizontal = value;
	}

	/**
	 * 0 上，1 中，2 下。
	 *
	 * @return 0 上，1 中，2 下。
	 */
	@SimpleProperty
	public int 垂直位置() {
		return vertical;
	}

	/**
	 * 0 上，1 中，2 下。
	 *
	 * @param value 0 上，1 中，2 下。
	 * @throws IllegalArgumentException 数值、文本或数组不符合此属性约束。
	 */
	@SimpleProperty
	public void 垂直位置(int value) {
		ChartChecks.integer(value, 0, 2);
		vertical = value;
	}

	/**
	 * 0 横向，1 纵向。
	 *
	 * @return 0 横向，1 纵向。
	 */
	@SimpleProperty
	public int 排列方向() {
		return orientation;
	}

	/**
	 * 0 横向，1 纵向。
	 *
	 * @param value 0 横向，1 纵向。
	 * @throws IllegalArgumentException 数值、文本或数组不符合此属性约束。
	 */
	@SimpleProperty
	public void 排列方向(int value) {
		ChartChecks.integer(value, 0, 1);
		orientation = value;
	}

	/**
	 * 是否绘制在图表内容区内。
	 *
	 * @return 是否绘制在图表内容区内。
	 */
	@SimpleProperty
	public boolean 图内绘制() {
		return inside;
	}

	/**
	 * 是否绘制在图表内容区内。
	 *
	 * @param value 是否绘制在图表内容区内。
	 */
	@SimpleProperty
	public void 图内绘制(boolean value) {
		inside = value;
	}

	/**
	 * 是否允许图例自动换行。
	 *
	 * @return 是否允许图例自动换行。
	 */
	@SimpleProperty
	public boolean 自动换行() {
		return wrap;
	}

	/**
	 * 是否允许图例自动换行。
	 *
	 * @param value 是否允许图例自动换行。
	 */
	@SimpleProperty
	public void 自动换行(boolean value) {
		wrap = value;
	}

	/**
	 * 0 从左到右，1 从右到左。
	 *
	 * @return 0 从左到右，1 从右到左。
	 */
	@SimpleProperty
	public int 排列顺序() {
		return direction;
	}

	/**
	 * 0 从左到右，1 从右到左。
	 *
	 * @param value 0 从左到右，1 从右到左。
	 * @throws IllegalArgumentException 数值、文本或数组不符合此属性约束。
	 */
	@SimpleProperty
	public void 排列顺序(int value) {
		ChartChecks.integer(value, 0, 1);
		direction = value;
	}

	/**
	 * 0 无图形，1 空位，2 默认，3 方形，4 圆形，5 线。
	 *
	 * @return 0 无图形，1 空位，2 默认，3 方形，4 圆形，5 线。
	 */
	@SimpleProperty
	public int 图形形状() {
		return form;
	}

	/**
	 * 0 无图形，1 空位，2 默认，3 方形，4 圆形，5 线。
	 *
	 * @param value 0 无图形，1 空位，2 默认，3 方形，4 圆形，5 线。
	 * @throws IllegalArgumentException 数值、文本或数组不符合此属性约束。
	 */
	@SimpleProperty
	public void 图形形状(int value) {
		ChartChecks.integer(value, 0, 5);
		form = value;
	}

	/**
	 * 图例图形大小，单位 DIP。
	 *
	 * @return 图例图形大小，单位 DIP。
	 */
	@SimpleProperty
	public float 图形大小() {
		return formSize;
	}

	/**
	 * 图例图形大小，单位 DIP。
	 *
	 * @param value 图例图形大小，单位 DIP。
	 * @throws IllegalArgumentException 数值、文本或数组不符合此属性约束。
	 */
	@SimpleProperty
	public void 图形大小(float value) {
		ChartChecks.range(value, 0f, 100000f);
		formSize = value;
	}

	/**
	 * 线形图例的线宽，单位 DIP。
	 *
	 * @return 线形图例的线宽，单位 DIP。
	 */
	@SimpleProperty
	public float 图形线宽() {
		return formWidth;
	}

	/**
	 * 线形图例的线宽，单位 DIP。
	 *
	 * @param value 线形图例的线宽，单位 DIP。
	 * @throws IllegalArgumentException 数值、文本或数组不符合此属性约束。
	 */
	@SimpleProperty
	public void 图形线宽(float value) {
		ChartChecks.range(value, 0.1f, 12f);
		formWidth = value;
	}

	/**
	 * 图例项目之间的水平间距，单位 DIP。
	 *
	 * @return 图例项目之间的水平间距，单位 DIP。
	 */
	@SimpleProperty
	public float 水平间距() {
		return xSpace;
	}

	/**
	 * 图例项目之间的水平间距，单位 DIP。
	 *
	 * @param value 图例项目之间的水平间距，单位 DIP。
	 * @throws IllegalArgumentException 数值、文本或数组不符合此属性约束。
	 */
	@SimpleProperty
	public void 水平间距(float value) {
		ChartChecks.range(value, 0f, 100000f);
		xSpace = value;
	}

	/**
	 * 图例项目之间的垂直间距，单位 DIP。
	 *
	 * @return 图例项目之间的垂直间距，单位 DIP。
	 */
	@SimpleProperty
	public float 垂直间距() {
		return ySpace;
	}

	/**
	 * 图例项目之间的垂直间距，单位 DIP。
	 *
	 * @param value 图例项目之间的垂直间距，单位 DIP。
	 * @throws IllegalArgumentException 数值、文本或数组不符合此属性约束。
	 */
	@SimpleProperty
	public void 垂直间距(float value) {
		ChartChecks.range(value, 0f, 100000f);
		ySpace = value;
	}

	/**
	 * 图形与文字的间距，单位 DIP。
	 *
	 * @return 图形与文字的间距，单位 DIP。
	 */
	@SimpleProperty
	public float 图文间距() {
		return formSpace;
	}

	/**
	 * 图形与文字的间距，单位 DIP。
	 *
	 * @param value 图形与文字的间距，单位 DIP。
	 * @throws IllegalArgumentException 数值、文本或数组不符合此属性约束。
	 */
	@SimpleProperty
	public void 图文间距(float value) {
		ChartChecks.range(value, 0f, 100000f);
		formSpace = value;
	}

	/**
	 * 堆叠图例图形的间距，单位 DIP。
	 *
	 * @return 堆叠图例图形的间距，单位 DIP。
	 */
	@SimpleProperty
	public float 堆叠间距() {
		return stackSpace;
	}

	/**
	 * 堆叠图例图形的间距，单位 DIP。
	 *
	 * @param value 堆叠图例图形的间距，单位 DIP。
	 * @throws IllegalArgumentException 数值、文本或数组不符合此属性约束。
	 */
	@SimpleProperty
	public void 堆叠间距(float value) {
		ChartChecks.range(value, 0f, 100000f);
		stackSpace = value;
	}

	/**
	 * 图例可占用空间的比例，0.01 至 1。
	 *
	 * @return 图例可占用空间的比例，0.01 至 1。
	 */
	@SimpleProperty
	public float 最大占比() {
		return maxSize;
	}

	/**
	 * 图例可占用空间的比例，0.01 至 1。
	 *
	 * @param value 图例可占用空间的比例，0.01 至 1。
	 * @throws IllegalArgumentException 数值、文本或数组不符合此属性约束。
	 */
	@SimpleProperty
	public void 最大占比(float value) {
		ChartChecks.range(value, 0.01f, 1f);
		maxSize = value;
	}

	/**
	 * 字体资产路径；空文本使用系统默认字体。
	 *
	 * @return 字体资产路径；空文本使用系统默认字体。
	 */
	@SimpleProperty
	public String 字体资产() {
		return font;
	}

	/**
	 * 字体资产路径；空文本使用系统默认字体。
	 *
	 * @param value 字体资产路径；空文本使用系统默认字体。
	 * @throws IllegalArgumentException 数值、文本或数组不符合此属性约束。
	 */
	@SimpleProperty
	public void 字体资产(String value) {
		ChartChecks.text(value);
		font = value;
	}

	/**
	 * 返回独立的配置副本，回调接口不复制。
	 *
	 * @return 返回独立的配置副本，回调接口不复制。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	@SimpleFunction
	public MPAC图例样式 复制() {
		MPAC图例样式 result = new MPAC图例样式();
		result.textSize = textSize;
		result.textColor = textColor;
		result.horizontal = horizontal;
		result.vertical = vertical;
		result.orientation = orientation;
		result.inside = inside;
		result.wrap = wrap;
		result.direction = direction;
		result.form = form;
		result.formSize = formSize;
		result.formWidth = formWidth;
		result.xSpace = xSpace;
		result.ySpace = ySpace;
		result.formSpace = formSpace;
		result.stackSpace = stackSpace;
		result.maxSize = maxSize;
		result.font = font;
		result.labels = labels.clone();
		result.colors = colors.clone();
		result.extraLabels = extraLabels.clone();
		result.extraColors = extraColors.clone();
		result.custom = custom;
		return result;
	}

	/**
	 * 自定义和附加图例项目快照。
	 */
	String[] labels = new String[0];
	int[] colors = new int[0];
	String[] extraLabels = new String[0];
	int[] extraColors = new int[0];
	boolean custom;

	/**
	 * 复制图例名称与颜色数组，二者须等长。
	 *
	 * @param names  图例文字数组。
	 * @param values 与文字等长的 ARGB 颜色数组。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	@SimpleFunction
	public void 设置自定义项目(String[] names, int[] values) {
		ChartChecks.labels(names, values);
		labels = names.clone();
		colors = values.clone();
		custom = true;
	}

	/**
	 * 复制图例名称与颜色数组，二者须等长。
	 *
	 * @param names  图例文字数组。
	 * @param values 与文字等长的 ARGB 颜色数组。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	@SimpleFunction
	public void 设置附加项目(String[] names, int[] values) {
		ChartChecks.labels(names, values);
		extraLabels = names.clone();
		extraColors = values.clone();
	}

	/**
	 * 恢复由数据组生成的自动图例。
	 *
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	@SimpleFunction
	public void 恢复自动项目() {
		custom = false;
		labels = new String[0];
		colors = new int[0];
	}
}
