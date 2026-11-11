package simple.library.wsd.mpandroidchart;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.SimpleProperty;

/**
 * MPAC雷达样式；应用到图表时复制配置，之后修改此对象不影响已有图表。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class MPAC雷达样式 {

	/**
	 * 是否绘制雷达网格。
	 */
	boolean web = true;

	/**
	 * ARGB 颜色。
	 */
	int color = -7829368;

	/**
	 * ARGB 颜色。
	 */
	int innerColor = -3355444;

	/**
	 * 外网格线宽，单位 DIP。
	 */
	float width = 1f;

	/**
	 * 内网格线宽，单位 DIP。
	 */
	float innerWidth = 1f;

	/**
	 * 网格透明度，0 至 255。
	 */
	int alpha = 150;

	/**
	 * 相邻绘制网格线之间跳过的条数。
	 */
	int skip = 0;

	/**
	 * 创建默认配置。
	 */
	public MPAC雷达样式() {
	}

	/**
	 * 是否绘制雷达网格。
	 *
	 * @return 是否绘制雷达网格。
	 */
	@SimpleProperty
	public boolean 显示网格() {
		return web;
	}

	/**
	 * 是否绘制雷达网格。
	 *
	 * @param value 是否绘制雷达网格。
	 */
	@SimpleProperty
	public void 显示网格(boolean value) {
		web = value;
	}

	/**
	 * ARGB 颜色。
	 *
	 * @return ARGB 颜色。
	 */
	@SimpleProperty
	public int 外网格颜色() {
		return color;
	}

	/**
	 * ARGB 颜色。
	 *
	 * @param value ARGB 颜色。
	 */
	@SimpleProperty
	public void 外网格颜色(int value) {
		color = value;
	}

	/**
	 * ARGB 颜色。
	 *
	 * @return ARGB 颜色。
	 */
	@SimpleProperty
	public int 内网格颜色() {
		return innerColor;
	}

	/**
	 * ARGB 颜色。
	 *
	 * @param value ARGB 颜色。
	 */
	@SimpleProperty
	public void 内网格颜色(int value) {
		innerColor = value;
	}

	/**
	 * 外网格线宽，单位 DIP。
	 *
	 * @return 外网格线宽，单位 DIP。
	 */
	@SimpleProperty
	public float 外网格线宽() {
		return width;
	}

	/**
	 * 外网格线宽，单位 DIP。
	 *
	 * @param value 外网格线宽，单位 DIP。
	 * @throws IllegalArgumentException 数值、文本或数组不符合此属性约束。
	 */
	@SimpleProperty
	public void 外网格线宽(float value) {
		ChartChecks.range(value, 0.1f, 12f);
		width = value;
	}

	/**
	 * 内网格线宽，单位 DIP。
	 *
	 * @return 内网格线宽，单位 DIP。
	 */
	@SimpleProperty
	public float 内网格线宽() {
		return innerWidth;
	}

	/**
	 * 内网格线宽，单位 DIP。
	 *
	 * @param value 内网格线宽，单位 DIP。
	 * @throws IllegalArgumentException 数值、文本或数组不符合此属性约束。
	 */
	@SimpleProperty
	public void 内网格线宽(float value) {
		ChartChecks.range(value, 0.1f, 12f);
		innerWidth = value;
	}

	/**
	 * 网格透明度，0 至 255。
	 *
	 * @return 网格透明度，0 至 255。
	 */
	@SimpleProperty
	public int 网格透明度() {
		return alpha;
	}

	/**
	 * 网格透明度，0 至 255。
	 *
	 * @param value 网格透明度，0 至 255。
	 * @throws IllegalArgumentException 数值、文本或数组不符合此属性约束。
	 */
	@SimpleProperty
	public void 网格透明度(int value) {
		ChartChecks.integer(value, 0, 255);
		alpha = value;
	}

	/**
	 * 相邻绘制网格线之间跳过的条数。
	 *
	 * @return 相邻绘制网格线之间跳过的条数。
	 */
	@SimpleProperty
	public int 跳过网格线数() {
		return skip;
	}

	/**
	 * 相邻绘制网格线之间跳过的条数。
	 *
	 * @param value 相邻绘制网格线之间跳过的条数。
	 * @throws IllegalArgumentException 数值、文本或数组不符合此属性约束。
	 */
	@SimpleProperty
	public void 跳过网格线数(int value) {
		ChartChecks.integer(value, 0, 1000);
		skip = value;
	}

	/**
	 * 返回独立的配置副本，回调接口不复制。
	 *
	 * @return 返回独立的配置副本，回调接口不复制。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	@SimpleFunction
	public MPAC雷达样式 复制() {
		MPAC雷达样式 result = new MPAC雷达样式();
		result.web = web;
		result.color = color;
		result.innerColor = innerColor;
		result.width = width;
		result.innerWidth = innerWidth;
		result.alpha = alpha;
		result.skip = skip;
		return result;
	}
}
