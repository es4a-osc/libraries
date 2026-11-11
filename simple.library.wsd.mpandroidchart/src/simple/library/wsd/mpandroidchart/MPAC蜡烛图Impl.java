package simple.library.wsd.mpandroidchart;

import android.content.Context;
import com.github.mikephil.charting.charts.CandleStickChart;
import com.github.mikephil.charting.charts.Chart;
import simple.runtime.components.组件容器;

/**
 * 蜡烛图的 Android 实现，使用 MPAndroidChart 3.2.0。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class MPAC蜡烛图Impl extends ChartComponent implements MPAC蜡烛图 {

	/**
	 * 创建蜡烛图。
	 *
	 * @param container 所属可视容器。
	 */
	public MPAC蜡烛图Impl(组件容器 container) {
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

	/* 数值坐标与堆叠数据 */

	/**
	 * 添加使用数值横坐标的数据组；不同组可有不同横坐标和项目数量，不能与分类坐标混用。
	 *
	 * @param name  非空引用的数据组名称。
	 * @param xs    非空有限横坐标数组，按非递减顺序排列。
	 * @param open  与横坐标等长，最高不低于开收，最低不高于开收。
	 * @param high  与横坐标等长，最高不低于开收，最低不高于开收。
	 * @param low   与横坐标等长，最高不低于开收，最低不高于开收。
	 * @param close 与横坐标等长，最高不低于开收，最低不高于开收。
	 * @param color ARGB 颜色。
	 * @param axis  0 左轴，1 右轴。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	@Override
	public void 添加数值组(String name, float[] xs, float[] open, float[] high, float[] low, float[] close, int color,
			int axis) {
		addNumeric(ChartModel.CANDLE, name, xs, new float[][] { open, high, low, close }, color, axis);
	}
}
