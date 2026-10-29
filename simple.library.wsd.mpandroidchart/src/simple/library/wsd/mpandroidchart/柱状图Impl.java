package simple.library.wsd.mpandroidchart;

import android.content.Context;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.Chart;

import simple.runtime.components.组件容器;

/**
 * 柱状图的 Android 实现，使用 MPAndroidChart 3.2.0。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class 柱状图Impl extends ChartComponent implements 柱状图 {

	/**
	 * 创建柱状图。
	 *
	 * @param container 所属可视容器。
	 */
	public 柱状图Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected Chart<?> createChart(Context context) {
		return new BarChart(context);
	}

	@Override
	protected int chartKind() {
		return ChartModel.BAR;
	}

	@Override
	public void 添加数据组(String name, float[] values, int color, int axis) {
		addSeries(ChartModel.BAR, name, new float[][] { values }, color, axis);
	}
}
