package simple.library.wsd.achartengine;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * 基于 AChartEngine 的折线图；在界面线程初始化、添加数据并构建。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface ACE折线图 extends ACE坐标图表 {

	/**
	 * 添加数据组并复制输入数组，构建或刷新后显示；分类和数值坐标不能混用。
	 *
	 * @param name   非空引用的名称，允许空文本。
	 * @param values 与横坐标或分类名称等长的有限数值数组。
	 * @param color  ARGB 颜色。
	 * @param axis   0 左轴，1 右轴。
	 */
	@SimpleFunction
	void 添加数据组(String name, double[] values, int color, int axis);

	/**
	 * 添加数据组并复制输入数组，构建或刷新后显示；分类和数值坐标不能混用。
	 *
	 * @param name   非空引用的名称，允许空文本。
	 * @param xs     严格递增的有限数值数组，不允许重复。
	 * @param values 与横坐标或分类名称等长的有限数值数组。
	 * @param color  ARGB 颜色。
	 * @param axis   0 左轴，1 右轴。
	 */
	@SimpleFunction
	void 添加数值组(String name, double[] xs, double[] values, int color, int axis);
}
