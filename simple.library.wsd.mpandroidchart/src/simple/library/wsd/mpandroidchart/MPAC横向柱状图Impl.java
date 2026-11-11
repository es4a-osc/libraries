package simple.library.wsd.mpandroidchart;

import android.content.Context;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import simple.runtime.components.组件容器;

/**
 * 横向柱状图的 Android 实现，使用 MPAndroidChart 3.2.0。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class MPAC横向柱状图Impl extends ChartComponent implements MPAC横向柱状图 {

	/**
	 * 创建横向柱状图。
	 *
	 * @param container 所属可视容器。
	 */
	public MPAC横向柱状图Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected Chart<?> createChart(Context context) {
		return new HorizontalBarChart(context);
	}

	@Override
	protected int chartKind() {
		return ChartModel.HORIZONTAL;
	}

	@Override
	public void 添加数据组(String name, float[] values, int color, int axis) {
		addSeries(ChartModel.HORIZONTAL, name, new float[][] { values }, color, axis);
	}

	/* 数值坐标与堆叠数据 */

	/**
	 * 添加使用数值横坐标的数据组；不同组可有不同横坐标和项目数量，不能与分类坐标混用。
	 *
	 * @param name   非空引用的数据组名称。
	 * @param xs     非空有限横坐标数组，按非递减顺序排列。
	 * @param values 非空有限数值数组。
	 * @param color  ARGB 颜色。
	 * @param axis   0 左轴，1 右轴。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	@Override
	public void 添加数值组(String name, float[] xs, float[] values, int color, int axis) {
		addNumeric(ChartModel.HORIZONTAL, name, xs, new float[][] { values }, color, axis);
	}

	/**
	 * 添加堆叠柱，支持正负层；须先设置项目名称。
	 *
	 * @param name   非空引用的数据组名称。
	 * @param values 非空有限数值数组。
	 * @param layers 各层数据按整层依次拼接，长度等于项目数乘层数。
	 * @param color  ARGB 颜色。
	 * @param axis   0 左轴，1 右轴。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	@Override
	public void 添加堆叠组(String name, float[] values, int layers, int color, int axis) {
		addStack(ChartModel.HORIZONTAL, name, null, values, layers, color, axis);
	}

	/**
	 * 添加堆叠柱，支持正负层；使用数值横坐标。
	 *
	 * @param name   非空引用的数据组名称。
	 * @param xs     非空有限横坐标数组，按非递减顺序排列。
	 * @param values 非空有限数值数组。
	 * @param layers 各层数据按整层依次拼接，长度等于项目数乘层数。
	 * @param color  ARGB 颜色。
	 * @param axis   0 左轴，1 右轴。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	@Override
	public void 添加数值堆叠组(String name, float[] xs, float[] values, int layers, int color, int axis) {
		addStack(ChartModel.HORIZONTAL, name, xs, values, layers, color, axis);
	}
}
