package simple.library.wsd.achartengine;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * 基于 AChartEngine 的组合图；在界面线程初始化、添加数据并构建。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface ACE组合图 extends ACE坐标图表 {

	/**
	 * 添加组合图的数据组，保持添加顺序和左右轴归属。
	 *
	 * @param kind   0 折线，1 平滑曲线，2 柱，3 散点，4 气泡，5 区间柱，6 时间折线。
	 * @param name   非空引用的名称，允许空文本。
	 * @param xs     严格递增的有限数值数组，不允许重复。
	 * @param values 普通图传纵坐标；气泡按整列拼接纵坐标与大小，区间按整列拼接下限与上限。
	 * @param color  ARGB 颜色。
	 * @param axis   0 左轴，1 右轴。
	 */
	@SimpleFunction
	void 添加数据组(int kind, String name, double[] xs, double[] values, int color, int axis);
}
