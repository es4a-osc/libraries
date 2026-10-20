package simple.library.wsd.mpandroidchart;

import android.content.Context;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.RadarChart;
import simple.runtime.components.组件容器;

/**
 * 雷达图的 Android 实现，使用 MPAndroidChart 2.1.5。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class 雷达图Impl extends ChartComponent implements 雷达图 {

	/**
	 * 创建雷达图。
	 *
	 * @param container 所属可视容器。
	 */
	public 雷达图Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected Chart<?> createChart(Context context) {
		return new RadarChart(context);
	}

	@Override
	protected int chartKind() {
		return ChartModel.RADAR;
	}

	@Override
	public void 添加数据组(String name, float[] values, int color) {
		addSeries(ChartModel.RADAR, name, new float[][] { values }, color, 0);
	}
}
