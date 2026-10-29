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
public final class 横向柱状图Impl extends ChartComponent implements 横向柱状图 {

	/**
	 * 创建横向柱状图。
	 *
	 * @param container 所属可视容器。
	 */
	public 横向柱状图Impl(组件容器 container) {
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
}
