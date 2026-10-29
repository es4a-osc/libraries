package simple.library.wsd.mpandroidchart;

import android.graphics.RectF;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.highlight.ChartHighlighter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.highlight.IHighlighter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.MPPointD;
import com.github.mikephil.charting.utils.Transformer;
import com.github.mikephil.charting.utils.Utils;

/**
 * 按实际柱条矩形选择数据，保留直接点击组合图中折线或散点的精确命中。
 * 避免上游仅比较数据点距离时，将柱条内部点击分配给附近的其它数据组。
 *
 * @author 树先生 xhwsd@qq.com
 */
final class BarSelectionHighlighter extends ChartHighlighter<BarLineChartBase<?>> {

	private final IHighlighter fallback;
	private final RectF bounds = new RectF();

	/**
	 * 保存上游选择器，未命中柱条时仍由其处理其它图形。
	 *
	 * @param chart 当前直角坐标图表。
	 * @param fallback 图表原有选择器。
	 */
	private BarSelectionHighlighter(BarLineChartBase<?> chart, IHighlighter fallback) {
		super(chart);
		this.fallback = fallback;
	}

	/**
	 * 在数据构建后安装适配器；重复构建不叠加包装层。
	 *
	 * @param chart 柱状、横向柱状或组合图表。
	 */
	static void install(BarLineChartBase<?> chart) {
		IHighlighter original = chart.getHighlighter();
		if (original instanceof BarSelectionHighlighter) {
			original = ((BarSelectionHighlighter) original).fallback;
		}
		chart.setHighlighter(new BarSelectionHighlighter(chart, original));
	}

	@Override
	public Highlight getHighlight(float x, float y) {
		Highlight nearest = fallback.getHighlight(x, y);
		boolean combined = mChart instanceof CombinedChart;
		BarData data = combined ? ((CombinedChart) mChart).getBarData() : ((BarChart) mChart).getBarData();
		if (data == null || !mChart.getViewPortHandler().isInBounds(x, y)) {
			return nearest;
		}
		int dataIndex = combined ? ((CombinedChart) mChart).getData().getAllData().indexOf(data) : -1;
		if (combined && nearest != null && nearest.getDataIndex() != dataIndex) {
			float dx = x - nearest.getXPx();
			float dy = y - nearest.getYPx();
			float tolerance = Utils.convertDpToPixel(8);
			if (dx * dx + dy * dy <= tolerance * tolerance) {
				return nearest;
			}
		}

		// 每组只查找距离当前分类最近的条目，使用该组实际纵轴完成坐标转换。
		boolean horizontal = mChart instanceof HorizontalBarChart;
		for (int index = data.getDataSetCount() - 1; index >= 0; index--) {
			IBarDataSet set = data.getDataSetByIndex(index);
			if (!set.isVisible() || !set.isHighlightEnabled()) {
				continue;
			}
			Transformer transformer = mChart.getTransformer(set.getAxisDependency());
			MPPointD values = transformer.getValuesByTouchPoint(x, y);
			float category = (float) (horizontal ? values.y : values.x);
			MPPointD.recycleInstance(values);
			BarEntry entry = set.getEntryForXValue(category, Float.NaN, DataSet.Rounding.CLOSEST);
			if (entry == null || set.getEntryIndex(entry) >= set.getEntryCount() * mChart.getAnimator().getPhaseX()) {
				continue;
			}
			float halfWidth = data.getBarWidth() / 2;
			float value = entry.getY() * mChart.getAnimator().getPhaseY();
			if (horizontal) {
				bounds.set(Math.min(0, value), entry.getX() + halfWidth,
					Math.max(0, value), entry.getX() - halfWidth);
			} else {
				bounds.set(entry.getX() - halfWidth, Math.max(0, value),
					entry.getX() + halfWidth, Math.min(0, value));
			}
			transformer.rectValueToPixel(bounds);
			if (bounds.contains(x, y)) {
				MPPointD pixel = transformer.getPixelForValues(horizontal ? entry.getY() : entry.getX(),
					horizontal ? entry.getX() : entry.getY());
				Highlight result = new Highlight(entry.getX(), entry.getY(), (float) pixel.x, (float) pixel.y,
					index, set.getAxisDependency());
				MPPointD.recycleInstance(pixel);
				result.setDataIndex(dataIndex);
				return result;
			}
		}
		return nearest;
	}
}
