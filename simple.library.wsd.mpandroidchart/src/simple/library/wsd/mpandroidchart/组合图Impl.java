package simple.library.wsd.mpandroidchart;

import android.content.Context;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.CombinedChart;
import simple.runtime.components.组件容器;

/**
 * 组合图的 Android 实现，使用 MPAndroidChart 2.1.5。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class 组合图Impl extends ChartComponent implements 组合图 {

	/**
	 * 创建组合图。
	 *
	 * @param container 所属可视容器。
	 */
	public 组合图Impl(组件容器 container) {
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
}
