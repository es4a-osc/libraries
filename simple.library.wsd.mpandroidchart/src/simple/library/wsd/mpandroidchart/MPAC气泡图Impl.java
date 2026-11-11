package simple.library.wsd.mpandroidchart;

import android.content.Context;
import com.github.mikephil.charting.charts.BubbleChart;
import com.github.mikephil.charting.charts.Chart;
import simple.runtime.components.组件容器;

/**
 * 气泡图的 Android 实现，使用 MPAndroidChart 3.2.0。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class MPAC气泡图Impl extends ChartComponent implements MPAC气泡图 {

	/**
	 * 创建气泡图。
	 *
	 * @param container 所属可视容器。
	 */
	public MPAC气泡图Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected Chart<?> createChart(Context context) {
		return new BubbleChart(context);
	}

	@Override
	protected int chartKind() {
		return ChartModel.BUBBLE;
	}

	@Override
	public void 添加数据组(String name, float[] values, float[] sizes, int color, int axis) {
		addSeries(ChartModel.BUBBLE, name, new float[][] { values, sizes }, color, axis);
	}

	/* 数值坐标与堆叠数据 */

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
	public void 添加数值组(String name, float[] xs, float[] values, float[] sizes, int color, int axis) {
		addNumeric(ChartModel.BUBBLE, name, xs, new float[][] { values, sizes }, color, axis);
	}
}
