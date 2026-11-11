package simple.library.wsd.mpandroidchart;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import android.graphics.Paint;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.BubbleChart;
import com.github.mikephil.charting.charts.CandleStickChart;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.BubbleData;
import com.github.mikephil.charting.data.BubbleDataSet;
import com.github.mikephil.charting.data.BubbleEntry;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.data.RadarData;
import com.github.mikephil.charting.data.RadarDataSet;
import com.github.mikephil.charting.data.RadarEntry;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.interfaces.datasets.IBubbleDataSet;
import com.github.mikephil.charting.interfaces.datasets.ICandleDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.interfaces.datasets.IRadarDataSet;
import com.github.mikephil.charting.interfaces.datasets.IScatterDataSet;

/**
 * 从已校验的数据模型创建九类原生数据，保留原始项目编号。
 *
 * @author 树先生 xhwsd@qq.com
 */
final class ChartDataBuilder {
	/**
	 * 创建不可实例化的数据构建工具。
	 */
	private ChartDataBuilder() {
	}

	/**
	 * 根据已校验的数据组建立原生数据，不把第三方对象暴露给 Simple。
	 *
	 * @param chart          原生图表。
	 * @param kind           图表类型。
	 * @param model          已校验的数据模型。
	 * @param entryLocations 原始编号映射。
	 * @param smooth         全局平滑曲线开关。
	 * @param width          柱组总宽。
	 * @param gap            柱间距。
	 * @param grouped        是否并排。
	 */
	static void build(Chart<?> chart, int kind, ChartModel model, IdentityHashMap<Entry, int[]> entryLocations,
			boolean smooth, float width, float gap, boolean grouped) {
		List<ILineDataSet> lines = new ArrayList<ILineDataSet>();
		List<IBarDataSet> bars = new ArrayList<IBarDataSet>();
		List<IScatterDataSet> scatters = new ArrayList<IScatterDataSet>();
		List<IBubbleDataSet> bubbles = new ArrayList<IBubbleDataSet>();
		List<ICandleDataSet> candles = new ArrayList<ICandleDataSet>();
		List<IRadarDataSet> radars = new ArrayList<IRadarDataSet>();
		List<PieEntry> slices = new ArrayList<PieEntry>();
		List<Integer> sliceColors = new ArrayList<Integer>();
		for (int group = 0; group < model.series.size(); group++) {
			ChartModel.Series item = model.series.get(group);
			if (item.kind == ChartModel.PIE) {
				PieEntry entry = new PieEntry(item.columns[0][0], item.name);
				entryLocations.put(entry, new int[] { 0, group });
				slices.add(entry);
				sliceColors.add(Integer.valueOf(item.color));
				continue;
			}
			if (item.kind == ChartModel.BAR || item.kind == ChartModel.HORIZONTAL) {
				List<BarEntry> entries = new ArrayList<BarEntry>();
				for (int index = 0; index < item.xs.length; index++) {
					float[] layers = new float[item.columns.length];
					for (int layer = 0; layer < layers.length; layer++) {
						layers[layer] = item.columns[layer][index];
					}
					BarEntry entry = item.stacked ? new BarEntry(item.xs[index], layers)
							: new BarEntry(item.xs[index], layers[0]);
					entryLocations.put(entry, new int[] { group, index });
					entries.add(entry);
				}
				BarDataSet set = new BarDataSet(entries, item.name);
				configureSet(set, item);
				bars.add(set);
			} else if (item.kind == ChartModel.BUBBLE) {
				List<BubbleEntry> entries = new ArrayList<BubbleEntry>();
				for (int index = 0; index < item.xs.length; index++) {
					BubbleEntry entry = new BubbleEntry(item.xs[index], item.columns[0][index], item.columns[1][index]);
					entryLocations.put(entry, new int[] { group, index });
					entries.add(entry);
				}
				// 3.2.0 仍将全零大小绘成整圆；使用非零归一化基准，让零项保持零直径。
				BubbleDataSet set = new BubbleDataSet(entries, item.name) {
					@Override
					public float getMaxSize() {
						float size = super.getMaxSize();
						return size == 0 ? 1 : size;
					}
				};
				configureSet(set, item);
				bubbles.add(set);
			} else if (item.kind == ChartModel.CANDLE) {
				List<CandleEntry> entries = new ArrayList<CandleEntry>();
				for (int index = 0; index < item.xs.length; index++) {
					CandleEntry entry = new CandleEntry(item.xs[index], item.columns[1][index], item.columns[2][index],
							item.columns[0][index], item.columns[3][index]);
					entryLocations.put(entry, new int[] { group, index });
					entries.add(entry);
				}
				CandleDataSet set = new CandleDataSet(entries, item.name);
				configureSet(set, item);
				set.setIncreasingColor(item.color);
				set.setDecreasingColor(item.color);
				set.setIncreasingPaintStyle(Paint.Style.STROKE);
				set.setDecreasingPaintStyle(Paint.Style.FILL);
				set.setShadowColorSameAsCandle(true);
				candles.add(set);
			} else if (item.kind == ChartModel.RADAR) {
				List<RadarEntry> entries = new ArrayList<RadarEntry>();
				for (int index = 0; index < item.xs.length; index++) {
					RadarEntry entry = new RadarEntry(item.columns[0][index]);
					entryLocations.put(entry, new int[] { group, index });
					entries.add(entry);
				}
				RadarDataSet set = new RadarDataSet(entries, item.name);
				configureSet(set, item);
				radars.add(set);
			} else {
				List<Entry> entries = new ArrayList<Entry>();
				for (int index = 0; index < item.xs.length; index++) {
					Entry entry = new Entry(item.xs[index], item.columns[0][index]);
					entryLocations.put(entry, new int[] { group, index });
					entries.add(entry);
				}
				if (item.kind == ChartModel.SCATTER) {
					ScatterDataSet set = new ScatterDataSet(entries, item.name);
					configureSet(set, item);
					set.setScatterShape(ScatterChart.ScatterShape.CIRCLE);
					scatters.add(set);

				} else {
					LineDataSet set = new LineDataSet(entries, item.name);
					configureSet(set, item);
					set.setLineWidth(2);
					set.setCircleColor(item.color);
					set.setMode(smooth ? LineDataSet.Mode.CUBIC_BEZIER : LineDataSet.Mode.LINEAR);
					lines.add(set);
				}
			}
		}

		switch (kind) {
		case ChartModel.LINE:
			((LineChart) chart).setData(new LineData(lines));
			break;
		case ChartModel.BAR:
		case ChartModel.HORIZONTAL:
			((BarChart) chart).setData(createBarData(bars, width, gap, grouped));
			break;
		case ChartModel.PIE:
			PieDataSet pie = new PieDataSet(slices, "");
			pie.setColors(sliceColors);
			((PieChart) chart).setData(new PieData(pie));
			break;
		case ChartModel.SCATTER:
			((ScatterChart) chart).setData(new ScatterData(scatters));
			break;
		case ChartModel.BUBBLE:
			((BubbleChart) chart).setData(new BubbleData(bubbles));
			break;
		case ChartModel.RADAR:
			((RadarChart) chart).setData(new RadarData(radars));
			break;
		case ChartModel.CANDLE:
			((CandleStickChart) chart).setData(new CandleData(candles));
			break;
		default:
			CombinedData combined = new CombinedData();
			if (!lines.isEmpty()) {
				combined.setData(new LineData(lines));
			}
			if (!bars.isEmpty()) {
				combined.setData(createBarData(bars, width, gap, grouped));
			}
			if (!scatters.isEmpty()) {
				combined.setData(new ScatterData(scatters));
			}
			if (!bubbles.isEmpty()) {
				combined.setData(new BubbleData(bubbles));
			}
			if (!candles.isEmpty()) {
				combined.setData(new CandleData(candles));
			}
			((CombinedChart) chart).setData(combined);
			break;
		}
	}

	/**
	 * 将多个柱状数据组放入同一个分类槽，保持项目中心位于整数横坐标。
	 *
	 * @param sets    按添加顺序组织的柱状数据组。
	 * @param width   柱组总宽。
	 * @param gap     柱间距。
	 * @param grouped 是否并排。
	 * @return 已设置柱宽和组内位置的数据。
	 */
	private static BarData createBarData(List<IBarDataSet> sets, float width, float gap, boolean grouped) {
		BarData data = new BarData(sets);
		int count = grouped ? sets.size() : 1;
		float single = width / count;
		data.setBarWidth(single);
		if (grouped && count > 1) {
			for (int group = 0; group < sets.size(); group++) {
				IBarDataSet set = sets.get(group);
				float offset = (group - (count - 1) / 2f) * (single + gap);
				for (int item = 0; item < set.getEntryCount(); item++) {
					BarEntry entry = set.getEntryForIndex(item);
					entry.setX(entry.getX() + offset);
				}
			}
			data.notifyDataChanged();
		}
		return data;
	}

	/**
	 * 配置数据组颜色与左右轴归属。
	 *
	 * @param set    原生数据组。
	 * @param source 已校验的数据组。
	 */
	private static void configureSet(DataSet<? extends Entry> set, ChartModel.Series source) {
		set.setColor(source.color);
		set.setAxisDependency(source.axis == 0 ? YAxis.AxisDependency.LEFT : YAxis.AxisDependency.RIGHT);
	}

}
