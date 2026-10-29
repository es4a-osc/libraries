package simple.library.wsd.mpandroidchart;

import android.content.Context;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.PieChart;

import simple.runtime.components.组件容器;

/**
 * 饼状图的 Android 实现，使用 MPAndroidChart 3.2.0。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class 饼状图Impl extends ChartComponent implements 饼状图 {

	/**
	 * 创建饼状图。
	 *
	 * @param container 所属可视容器。
	 */
	public 饼状图Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected Chart<?> createChart(Context context) {
		PieChart view = new PieChart(context);
		view.setDrawHoleEnabled(false);
		return view;
	}

	@Override
	protected int chartKind() {
		return ChartModel.PIE;
	}

	@Override
	public void 添加数据(String name, float value, int color) {
		addSlice(name, value, color);
	}
}
