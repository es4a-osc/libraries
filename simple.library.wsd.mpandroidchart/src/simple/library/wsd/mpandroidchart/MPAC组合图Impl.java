package simple.library.wsd.mpandroidchart;

import android.content.Context;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.CombinedChart;
import simple.runtime.components.组件容器;

/**
 * 组合图的 Android 实现，使用 MPAndroidChart 3.2.0。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class MPAC组合图Impl extends ChartComponent implements MPAC组合图 {

	/**
	 * 创建组合图。
	 *
	 * @param container 所属可视容器。
	 */
	public MPAC组合图Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected Chart<?> createChart(Context context) {
		return new CombinedChart(context);
	}

	@Override
	protected int chartKind() {
		return ChartModel.COMBINED;
	}

	@Override
	public void 添加折线组(String name, float[] values, int color, int axis) {
		addSeries(ChartModel.LINE, name, new float[][] { values }, color, axis);
	}

	@Override
	public void 添加柱状组(String name, float[] values, int color, int axis) {
		addSeries(ChartModel.BAR, name, new float[][] { values }, color, axis);
	}

	@Override
	public void 添加散点组(String name, float[] values, int color, int axis) {
		addSeries(ChartModel.SCATTER, name, new float[][] { values }, color, axis);
	}

	@Override
	public void 添加气泡组(String name, float[] values, float[] sizes, int color, int axis) {
		addSeries(ChartModel.BUBBLE, name, new float[][] { values, sizes }, color, axis);
	}

	@Override
	public void 添加蜡烛组(String name, float[] open, float[] high, float[] low, float[] close, int color, int axis) {
		addSeries(ChartModel.CANDLE, name, new float[][] { open, high, low, close }, color, axis);
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
	public void 添加数值折线组(String name, float[] xs, float[] values, int color, int axis) {
		addNumeric(ChartModel.LINE, name, xs, new float[][] { values }, color, axis);
	}

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
	public void 添加数值柱状组(String name, float[] xs, float[] values, int color, int axis) {
		addNumeric(ChartModel.BAR, name, xs, new float[][] { values }, color, axis);
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
		addStack(ChartModel.BAR, name, null, values, layers, color, axis);
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
		addStack(ChartModel.BAR, name, xs, values, layers, color, axis);
	}

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
	public void 添加数值散点组(String name, float[] xs, float[] values, int color, int axis) {
		addNumeric(ChartModel.SCATTER, name, xs, new float[][] { values }, color, axis);
	}

	/**
	 * 添加使用数值横坐标的数据组；不同组可有不同横坐标和项目数量，不能与分类坐标混用。
	 *
	 * @param name   非空引用的数据组名称。
	 * @param xs     非空有限横坐标数组，按非递减顺序排列。
	 * @param values 非空有限数值数组。
	 * @param sizes  与横坐标等长的非负有限大小数组。
	 * @param color  ARGB 颜色。
	 * @param axis   0 左轴，1 右轴。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	@Override
	public void 添加数值气泡组(String name, float[] xs, float[] values, float[] sizes, int color, int axis) {
		addNumeric(ChartModel.BUBBLE, name, xs, new float[][] { values, sizes }, color, axis);
	}

	/**
	 * 添加使用数值横坐标的数据组；不同组可有不同横坐标和项目数量，不能与分类坐标混用。
	 *
	 * @param name  非空引用的数据组名称。
	 * @param xs    非空有限横坐标数组，按非递减顺序排列。
	 * @param open  与横坐标等长，最高不低于开收，最低不高于开收。
	 * @param high  与横坐标等长，最高不低于开收，最低不高于开收。
	 * @param low   与横坐标等长，最高不低于开收，最低不高于开收。
	 * @param close 与横坐标等长，最高不低于开收，最低不高于开收。
	 * @param color ARGB 颜色。
	 * @param axis  0 左轴，1 右轴。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	@Override
	public void 添加数值蜡烛组(String name, float[] xs, float[] open, float[] high, float[] low, float[] close, int color,
			int axis) {
		addNumeric(ChartModel.CANDLE, name, xs, new float[][] { open, high, low, close }, color, axis);
	}
}
