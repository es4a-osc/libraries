package simple.library.wsd.mpandroidchart;

import android.content.Context;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.LineChart;

import simple.runtime.components.组件容器;

/**
 * 折线图的 Android 实现，使用 MPAndroidChart 3.2.0。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class 折线图Impl extends ChartComponent implements 折线图 {

	/**
	 * 创建折线图。
	 *
	 * @param container 所属可视容器。
	 */
	public 折线图Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected Chart<?> createChart(Context context) {
		return new LineChart(context);
	}

	@Override
	protected int chartKind() {
		return ChartModel.LINE;
	}

	@Override
	public void 添加数据组(String name, float[] values, int color, int axis) {
		addSeries(ChartModel.LINE, name, new float[][] { values }, color, axis);
	}
}
