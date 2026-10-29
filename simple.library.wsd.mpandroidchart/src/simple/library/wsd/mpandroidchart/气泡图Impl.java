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
public final class 气泡图Impl extends ChartComponent implements 气泡图 {

	/**
	 * 创建气泡图。
	 *
	 * @param container 所属可视容器。
	 */
	public 气泡图Impl(组件容器 container) {
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
}
