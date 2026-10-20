package simple.library.wsd.mpandroidchart;

import android.content.Context;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.ScatterChart;
import simple.runtime.components.组件容器;

/**
 * 散点图的 Android 实现，使用 MPAndroidChart 2.1.5。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class 散点图Impl extends ChartComponent implements 散点图 {

	/**
	 * 创建散点图。
	 *
	 * @param container 所属可视容器。
	 */
	public 散点图Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected Chart<?> createChart(Context context) {
		return new ScatterChart(context);
	}

	@Override
	protected int chartKind() {
		return ChartModel.SCATTER;
	}

	@Override
	public void 添加数据组(String name, float[] values, int color, int axis) {
		addSeries(ChartModel.SCATTER, name, new float[][] { values }, color, axis);
	}
}
