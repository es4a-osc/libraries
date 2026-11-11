package simple.library.wsd.achartengine;

import android.graphics.RectF;
import org.achartengine.chart.AbstractChart;
import org.achartengine.chart.BarChart;
import org.achartengine.chart.BubbleChart;
import org.achartengine.chart.ClickableArea;
import org.achartengine.chart.CubicLineChart;
import org.achartengine.chart.DialChart;
import org.achartengine.chart.DoughnutChart;
import org.achartengine.chart.ES4ACombinedChart;
import org.achartengine.chart.LineChart;
import org.achartengine.chart.PieChart;
import org.achartengine.chart.RangeBarChart;
import org.achartengine.chart.ScatterChart;
import org.achartengine.chart.TimeChart;
import org.achartengine.chart.XYChart;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.MultipleCategorySeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.model.XYValueSeries;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.DialRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

/**
 * 由经过校验的数据创建上游图形，集中处理圆图归一化和区间柱的成对横坐标。
 *
 * @author 树先生 xhwsd@qq.com
 */
final class ChartBuilder {
	/**
	 * 创建不可实例化的图形工具。
	 */
	private ChartBuilder() {
	}

	/**
	 * 生成带稳定索引的原生数值数据组。
	 *
	 * @param data 数据快照。
	 * @param axis 原生数值轴编号。
	 * @param hits 本帧绘制状态。
	 * @return 原生数据组。
	 */
	private static XYSeries series(final ChartModel.Series data, final int axis, final HitMap hits) {
		XYSeries result;
		if (data.kind == ChartModel.BUBBLE) {
			result = new XYValueSeries(data.name) {
				@Override
				public synchronized java.util.SortedMap<Double, Double> getRange(double start, double stop,
						int beforeAfter) {
					java.util.SortedMap<Double, Double> range = super.getRange(start, stop, beforeAfter);
					hits.begin(this, range.isEmpty() ? null : range.firstKey());
					return range;
				}

				@Override
				public int getScaleNumber() {
					return axis;
				}

				@Override
				public double getMaxValue() {
					return super.getMaxValue() == 0 ? 1 : super.getMaxValue();
				}
			};
		} else {
			result = new XYSeries(data.name, axis) {
				@Override
				public synchronized java.util.SortedMap<Double, Double> getRange(double start, double stop,
						int beforeAfter) {
					java.util.SortedMap<Double, Double> range = super.getRange(start, stop, beforeAfter);
					hits.begin(this, range.isEmpty() ? null : range.firstKey());
					return range;
				}
			};
		}
		for (int i = 0; i < data.xs.length; i++) {
			if (data.kind == ChartModel.BUBBLE) {
				((XYValueSeries) result).add(data.xs[i],
						data.missing[i] ? org.achartengine.util.MathHelper.NULL_VALUE : data.columns[0][i],
						data.columns[1][i]);
			} else {
				result.add(data.xs[i],
						data.missing[i] ? org.achartengine.util.MathHelper.NULL_VALUE : data.columns[0][i]);
				if (data.kind == ChartModel.RANGE) {
					result.add(Math.nextUp(data.xs[i]),
							data.missing[i] ? org.achartengine.util.MathHelper.NULL_VALUE : data.columns[1][i]);
				}
			}
		}
		return result;
	}

	/**
	 * 为所有坐标图生成范围；留白不累计，柱图包含零点。
	 *
	 * @param model 当前模型。
	 * @param axis  数值轴编号。
	 * @return 横轴最小、最大和纵轴最小、最大。
	 */
	static double[] range(ChartModel model, int axis) {
		double xmin = Double.POSITIVE_INFINITY, xmax = Double.NEGATIVE_INFINITY;
		double ymin = Double.POSITIVE_INFINITY, ymax = Double.NEGATIVE_INFINITY;
		boolean zero = false;
		boolean bars = false;
		double step = Double.POSITIVE_INFINITY;
		for (ChartModel.Series data : model.series) {
			xmin = Math.min(xmin, data.xs[0]);
			xmax = Math.max(xmax, data.xs[data.xs.length - 1]);
			bars |= data.kind == ChartModel.BAR || data.kind == ChartModel.RANGE;
			for (int item = 1; item < data.xs.length; item++) {
				step = Math.min(step, data.xs[item] - data.xs[item - 1]);
			}
			if (data.axis != axis) {
				continue;
			}
			zero |= data.kind == ChartModel.BAR;
			for (int c = 0; c < (data.kind == ChartModel.RANGE ? 2 : 1); c++) {
				for (int item = 0; item < data.xs.length; item++) {
					if (!data.missing[item]) {
						double value = data.columns[c][item];
						ymin = Math.min(ymin, value);
						ymax = Math.max(ymax, value);
					}
				}
			}
		}
		if (!Double.isFinite(ymin)) {
			ymin = 0;
			ymax = 1;
		}
		if (zero) {
			ymin = Math.min(0, ymin);
			ymax = Math.max(0, ymax);
		}
		double xp = model.numeric ? (xmax > xmin ? (xmax - xmin) * 0.05 : Math.max(1, Math.abs(xmin) * 0.05)) : 0.5;
		if (model.numeric && bars && Double.isFinite(step)) {
			xp = Math.max(xp, step * 0.75);
		}
		double yp = ymax > ymin ? (ymax - ymin) * 0.15 : Math.max(1, Math.abs(ymin) * 0.15);
		return new double[] { xmin - xp, xmax + xp, ymin == 0 && zero ? 0 : ymin - yp,
				ymax == 0 && zero ? 0 : ymax + yp };
	}

	/**
	 * 创建指定数值图形。
	 *
	 * @param kind          图形类型。
	 * @param dataset       数据集。
	 * @param renderer      绘制器。
	 * @param cubic         曲线强度。
	 * @param barMode       柱排列。
	 * @param pattern       时间格式。
	 * @param smart         是否采用时间刻度。
	 * @param hits          本帧点击区域。
	 * @param originalGroup 原模型数据组编号，-1 使用原生编号。
	 * @return 原生图形。
	 */
	private static XYChart xy(int kind, XYMultipleSeriesDataset dataset, XYMultipleSeriesRenderer renderer, float cubic,
			int barMode, String pattern, boolean smart, final HitMap hits, final int originalGroup) {
		switch (kind) {
		case ChartModel.LINE:
			return new LineChart(dataset, renderer) {
				@Override
				protected String getLabel(double value) {
					return value == org.achartengine.util.MathHelper.NULL_VALUE ? "" : super.getLabel(value);
				}

				@Override
				protected void drawSeries(XYSeries source, android.graphics.Canvas canvas, android.graphics.Paint paint,
						java.util.List<Float> points, SimpleSeriesRenderer style, float baseline, int group,
						XYMultipleSeriesRenderer.Orientation orientation, int start) {
					int actual = hits.segment(source, points.size() / 2, start);
					super.drawSeries(source, canvas, paint, points, style, baseline, group, orientation, actual);
				}

				@Override
				protected ClickableArea[] clickableAreasForPoints(float[] points, double[] values, float baseline,
						int group, int start) {
					return hits.record(originalGroup < 0 ? group : originalGroup, super.clickableAreasForPoints(points,
							values, baseline, group,
							values.length == 0 ? start : mDataset.getSeriesAt(group).getIndexForKey(values[0])));
				}
			};
		case ChartModel.CUBIC:
			return new CubicLineChart(dataset, renderer, cubic) {
				@Override
				protected String getLabel(double value) {
					return value == org.achartengine.util.MathHelper.NULL_VALUE ? "" : super.getLabel(value);
				}

				@Override
				protected void drawSeries(XYSeries source, android.graphics.Canvas canvas, android.graphics.Paint paint,
						java.util.List<Float> points, SimpleSeriesRenderer style, float baseline, int group,
						XYMultipleSeriesRenderer.Orientation orientation, int start) {
					int actual = hits.segment(source, points.size() / 2, start);
					super.drawSeries(source, canvas, paint, points, style, baseline, group, orientation, actual);
				}

				@Override
				protected ClickableArea[] clickableAreasForPoints(float[] points, double[] values, float baseline,
						int group, int start) {
					return hits.record(originalGroup < 0 ? group : originalGroup, super.clickableAreasForPoints(points,
							values, baseline, group,
							values.length == 0 ? start : mDataset.getSeriesAt(group).getIndexForKey(values[0])));
				}
			};
		case ChartModel.SCATTER:
			return new ScatterChart(dataset, renderer) {
				@Override
				protected String getLabel(double value) {
					return value == org.achartengine.util.MathHelper.NULL_VALUE ? "" : super.getLabel(value);
				}

				@Override
				protected void drawSeries(XYSeries source, android.graphics.Canvas canvas, android.graphics.Paint paint,
						java.util.List<Float> points, SimpleSeriesRenderer style, float baseline, int group,
						XYMultipleSeriesRenderer.Orientation orientation, int start) {
					int actual = hits.segment(source, points.size() / 2, start);
					super.drawSeries(source, canvas, paint, points, style, baseline, group, orientation, actual);
				}

				@Override
				protected ClickableArea[] clickableAreasForPoints(float[] points, double[] values, float baseline,
						int group, int start) {
					return hits.record(originalGroup < 0 ? group : originalGroup, super.clickableAreasForPoints(points,
							values, baseline, group,
							values.length == 0 ? start : mDataset.getSeriesAt(group).getIndexForKey(values[0])));
				}
			};
		case ChartModel.BUBBLE:
			return new BubbleChart(dataset, renderer) {
				@Override
				protected String getLabel(double value) {
					return value == org.achartengine.util.MathHelper.NULL_VALUE ? "" : super.getLabel(value);
				}

				@Override
				protected void drawSeries(XYSeries source, android.graphics.Canvas canvas, android.graphics.Paint paint,
						java.util.List<Float> points, SimpleSeriesRenderer style, float baseline, int group,
						XYMultipleSeriesRenderer.Orientation orientation, int start) {
					int actual = hits.segment(source, points.size() / 2, start);
					super.drawSeries(source, canvas, paint, points, style, baseline, group, orientation, actual);
				}

				@Override
				protected ClickableArea[] clickableAreasForPoints(float[] points, double[] values, float baseline,
						int group, int start) {
					return hits.record(originalGroup < 0 ? group : originalGroup, super.clickableAreasForPoints(points,
							values, baseline, group,
							values.length == 0 ? start : mDataset.getSeriesAt(group).getIndexForKey(values[0])));
				}
			};
		case ChartModel.TIME:
			TimeChart time = new TimeChart(dataset, renderer) {
				@Override
				protected String getLabel(double value) {
					return value == org.achartengine.util.MathHelper.NULL_VALUE ? "" : super.getLabel(value);
				}

				@Override
				protected void drawSeries(XYSeries source, android.graphics.Canvas canvas, android.graphics.Paint paint,
						java.util.List<Float> points, SimpleSeriesRenderer style, float baseline, int group,
						XYMultipleSeriesRenderer.Orientation orientation, int start) {
					int actual = hits.segment(source, points.size() / 2, start);
					super.drawSeries(source, canvas, paint, points, style, baseline, group, orientation, actual);
				}

				@Override
				protected ClickableArea[] clickableAreasForPoints(float[] points, double[] values, float baseline,
						int group, int start) {
					return hits.record(originalGroup < 0 ? group : originalGroup, super.clickableAreasForPoints(points,
							values, baseline, group,
							values.length == 0 ? start : mDataset.getSeriesAt(group).getIndexForKey(values[0])));
				}
			};
			time.setDateFormat(pattern.isEmpty() ? null : pattern);
			time.setXAxisSmart(smart);
			return time;
		case ChartModel.RANGE:
			return new RangeBarChart(dataset, renderer, BarChart.Type.values()[barMode]) {
				@Override
				protected String getLabel(double value) {
					return value == org.achartengine.util.MathHelper.NULL_VALUE ? "" : super.getLabel(value);
				}

				@Override
				public void drawSeries(android.graphics.Canvas canvas, android.graphics.Paint paint, float[] points,
						SimpleSeriesRenderer style, float baseline, int group, int start) {
					super.drawSeries(canvas, paint, points, style, baseline, group, start % 2);
				}

				@Override
				protected void drawChartValuesText(android.graphics.Canvas canvas, XYSeries source,
						SimpleSeriesRenderer style, android.graphics.Paint paint, float[] points, int group,
						int start) {
					float half = getHalfDiffX(points, points.length, mDataset.getSeriesCount());
					for (int i = start % 2 == 0 ? 0 : 2; i + 3 < points.length; i += 4) {
						int index = start + i / 2;
						float x = points[i]
								+ (mType == Type.DEFAULT ? (2 * group - mDataset.getSeriesCount() + 1.5f) * half : 0);
						drawText(canvas, getLabel(source.getY(index + 1)), x,
								points[i + 3] - style.getChartValuesSpacing(), paint, 0);
						drawText(canvas, getLabel(source.getY(index)), x,
								points[i + 1] + style.getChartValuesTextSize() + style.getChartValuesSpacing() - 3,
								paint, 0);
					}
				}

				@Override
				protected void drawSeries(XYSeries source, android.graphics.Canvas canvas, android.graphics.Paint paint,
						java.util.List<Float> points, SimpleSeriesRenderer style, float baseline, int group,
						XYMultipleSeriesRenderer.Orientation orientation, int start) {
					int actual = hits.segment(source, points.size() / 2, start);
					super.drawSeries(source, canvas, paint, points, style, baseline, group, orientation, actual);
				}

				@Override
				protected ClickableArea[] clickableAreasForPoints(float[] points, double[] values, float baseline,
						int group, int start) {
					ClickableArea[] result = new ClickableArea[points.length / 2];
					float half = getHalfDiffX(points, points.length, mDataset.getSeriesCount());
					int actual = values.length == 0 ? start : mDataset.getSeriesAt(group).getIndexForKey(values[0]);
					for (int i = actual % 2 == 0 ? 0 : 2; i + 3 < points.length; i += 4) {
						float offset = mType == Type.DEFAULT ? (2 * group - mDataset.getSeriesCount() + 1) * half : 0;
						RectF rect = new RectF(points[i] + offset - half, points[i + 1], points[i + 2] + offset + half,
								points[i + 3]);
						rect.sort();
						result[i / 2] = new ClickableArea(rect, values[i], values[i + 1]);
						result[i / 2 + 1] = new ClickableArea(rect, values[i + 2], values[i + 3]);
					}
					return hits.record(originalGroup < 0 ? group : originalGroup, result);
				}
			};
		default:
			return new BarChart(dataset, renderer, BarChart.Type.values()[barMode]) {
				@Override
				protected String getLabel(double value) {
					return value == org.achartengine.util.MathHelper.NULL_VALUE ? "" : super.getLabel(value);
				}

				@Override
				protected void drawSeries(XYSeries source, android.graphics.Canvas canvas, android.graphics.Paint paint,
						java.util.List<Float> points, SimpleSeriesRenderer style, float baseline, int group,
						XYMultipleSeriesRenderer.Orientation orientation, int start) {
					int actual = hits.segment(source, points.size() / 2, start);
					super.drawSeries(source, canvas, paint, points, style, baseline, group, orientation, actual);
				}

				@Override
				protected ClickableArea[] clickableAreasForPoints(float[] points, double[] values, float baseline,
						int group, int start) {
					ClickableArea[] areas = super.clickableAreasForPoints(points, values, baseline, group,
							values.length == 0 ? start : mDataset.getSeriesAt(group).getIndexForKey(values[0]));
					for (ClickableArea area : areas) {
						area.getRect().sort();
					}
					return hits.record(originalGroup < 0 ? group : originalGroup, areas);
				}
			};
		}
	}

	/**
	 * 创建带左右轴及混合图形的坐标图。
	 *
	 * @param model    数据模型。
	 * @param kind     组件类型。
	 * @param renderer 已配置的公共绘制器。
	 * @param style    数据样式的单位换算环境。
	 * @param density  DIP 密度。
	 * @param sp       SP 密度。
	 * @param values   是否显示数值。
	 * @param cubic    曲线强度。
	 * @param barMode  柱排列方式。
	 * @param pattern  时间格式。
	 * @param smart    时间刻度策略。
	 * @param hits     本帧点击区域。
	 * @return 原生图形。
	 */
	static XYChart cartesian(ChartModel model, int kind, XYMultipleSeriesRenderer renderer, ACE图表样式 style,
			float density, float sp, boolean values, float cubic, int barMode, String pattern, boolean smart,
			HitMap hits) {
		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		XYChart[] children = new XYChart[model.series.size()];
		for (int group = 0; group < model.series.size(); group++) {
			ChartModel.Series data = model.series.get(group);
			dataset.addSeries(series(data, data.axis, hits));
			XYSeriesRenderer item = new XYSeriesRenderer();
			ChartStyles.data(item, data.style == null ? new ACE数据组样式() : data.style, data.color, density, sp, values);
			renderer.addSeriesRenderer(item);
			if (kind == ChartModel.COMBINED) {
				XYMultipleSeriesDataset one = new XYMultipleSeriesDataset();
				one.addSeries(series(data, 0, hits));
				XYMultipleSeriesRenderer r = new XYMultipleSeriesRenderer();
				r.addSeriesRenderer(item);
				r.setBarSpacing(style.barSpace);
				r.setPointSize(style.pointSize * density);
				children[group] = xy(data.kind, one, r, cubic, barMode, pattern, smart, hits, group);
			}
		}
		return kind == ChartModel.COMBINED ? new ES4ACombinedChart(dataset, renderer, children, hits)
				: xy(kind, dataset, renderer, cubic, barMode, pattern, smart, hits, -1);
	}

	/**
	 * 创建饼、圆环或仪表盘并保持原值仅用于查询和事件。
	 *
	 * @param model    当前模型。
	 * @param kind     图形类型。
	 * @param renderer 目标绘制器。
	 * @param palette  圆环配色。
	 * @param dial     仪表配置。
	 * @return 原生圆图。
	 */
	static AbstractChart round(ChartModel model, int kind, DefaultRenderer renderer, int[] palette, ACE仪表样式 dial) {
		if (kind == ChartModel.DONUT) {
			MultipleCategorySeries data = new MultipleCategorySeries("");
			int max = 0;
			for (ChartModel.Series ring : model.series) {
				double scale = ChartModel.scale(ring);
				double[] values = ring.columns[0].clone();
				for (int i = 0; i < values.length; i++) {
					values[i] /= scale;
				}
				data.add(ring.name, ring.names.clone(), values);
				max = Math.max(max, values.length);
			}
			for (int i = 0; i < Math.max(max, model.series.size()); i++) {
				SimpleSeriesRenderer item = new SimpleSeriesRenderer();
				item.setColor(palette[i % palette.length]);
				renderer.addSeriesRenderer(item);
			}
			return new DoughnutChart(data, renderer);
		}
		ChartModel.Series source = model.group(0);
		if (kind == ChartModel.DIAL) {
			for (double value : source.columns[0]) {
				Checks.range(value, dial.min, dial.max);
			}
		}
		double scale = kind == ChartModel.PIE ? ChartModel.scale(source) : 1;
		CategorySeries data = new CategorySeries("");
		for (int i = 0; i < source.xs.length; i++) {
			data.add(source.names[i], source.columns[0][i] / scale);
			SimpleSeriesRenderer item = new SimpleSeriesRenderer();
			item.setColor(source.colors[i]);
			renderer.addSeriesRenderer(item);
		}
		if (kind == ChartModel.PIE) {
			return new PieChart(data, renderer);
		}
		DialRenderer target = (DialRenderer) renderer;
		target.setMinValue(dial.min);
		target.setMaxValue(dial.max);
		target.setAngleMin(dial.angleMin);
		target.setAngleMax(dial.angleMax);
		target.setMinorTicksSpacing(dial.minor);
		target.setMajorTicksSpacing(dial.major);
		DialRenderer.Type[] types = new DialRenderer.Type[source.xs.length];
		for (int i = 0; i < types.length; i++) {
			types[i] = DialRenderer.Type.values()[source.types[i]];
		}
		target.setVisualTypes(types);
		return new DialChart(data, target);
	}
}
