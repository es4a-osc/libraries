package simple.library.wsd.achartengine;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * 基于 AChartEngine 的区间柱状图；在界面线程初始化、添加数据并构建。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface ACE区间柱状图 extends ACE坐标图表 {

	/**
	 * 添加数据组并复制输入数组，构建或刷新后显示；分类和数值坐标不能混用。
	 *
	 * @param name  非空引用的名称，允许空文本。
	 * @param low   与横坐标等长，不大于上限。
	 * @param high  与横坐标等长，不小于下限。
	 * @param color ARGB 颜色。
	 * @param axis  0 左轴，1 右轴。
	 */
	@SimpleFunction
	void 添加数据组(String name, double[] low, double[] high, int color, int axis);

	/**
	 * 添加数据组并复制输入数组，构建或刷新后显示；分类和数值坐标不能混用。
	 *
	 * @param name  非空引用的名称，允许空文本。
	 * @param xs    严格递增的有限数值数组，不允许重复。
	 * @param low   与横坐标等长，不大于上限。
	 * @param high  与横坐标等长，不小于下限。
	 * @param color ARGB 颜色。
	 * @param axis  0 左轴，1 右轴。
	 */
	@SimpleFunction
	void 添加数值组(String name, double[] xs, double[] low, double[] high, int color, int axis);

	/**
	 * 设置柱排列，构建或刷新后生效。
	 *
	 * @param mode 0 并排，1 重叠；重叠沿用上游 STACKED 的从零点覆盖绘制，并非累加堆叠。
	 */
	@SimpleFunction
	void 设置柱排列(int mode);
}
