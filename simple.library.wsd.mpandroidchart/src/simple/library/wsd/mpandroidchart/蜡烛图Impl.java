package simple.library.wsd.mpandroidchart;

import android.content.Context;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.CandleStickChart;
import simple.runtime.components.组件容器;

/**
 * 蜡烛图的 Android 实现，使用 MPAndroidChart 2.1.5。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class 蜡烛图Impl extends ChartComponent implements 蜡烛图 {

	/**
	 * 创建蜡烛图。
	 *
	 * @param container 所属可视容器。
	 */
	public 蜡烛图Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected Chart<?> createChart(Context context) {
		return new CandleStickChart(context);
	}

	@Override
	protected int chartKind() {
		return ChartModel.CANDLE;
	}

	@Override
	public void 添加数据组(String name, float[] open, float[] high, float[] low, float[] close, int color, int axis) {
		addSeries(ChartModel.CANDLE, name, new float[][] { open, high, low, close }, color, axis);
	}
}
