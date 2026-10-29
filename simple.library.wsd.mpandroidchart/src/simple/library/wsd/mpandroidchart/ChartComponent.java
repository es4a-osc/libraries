package simple.library.wsd.mpandroidchart;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.charts.BubbleChart;
import com.github.mikephil.charting.charts.CandleStickChart;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
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
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.interfaces.datasets.IBubbleDataSet;
import com.github.mikephil.charting.interfaces.datasets.ICandleDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.interfaces.datasets.IRadarDataSet;
import com.github.mikephil.charting.interfaces.datasets.IScatterDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.Utils;

import simple.runtime.android.MainActivity;
import simple.runtime.components.impl.android.视图组件;
import simple.runtime.components.组件容器;
import simple.runtime.events.EventDispatcher;

/**
 * 统一处理九类图表的数据构建、选择事件和可取消动画。
 *
 * @author 树先生 xhwsd@qq.com
 */
abstract class ChartComponent extends 视图组件 implements 图表, OnChartValueSelectedListener {

	private final ChartModel model = new ChartModel();
	private final IdentityHashMap<Entry, int[]> entryLocations = new IdentityHashMap<Entry, int[]>();
	private final float[][] ranges = new float[2][];
	private Chart<?> chart;
	private TextView caption;
	private ValueAnimator animation;
	private boolean initialized;
	private boolean destroyed;
	private boolean showLegend = true;
	private boolean showValues = true;
	private boolean drag = true;
	private boolean zoom = true;
	private boolean smooth;
	private int animationDuration;
	private float fontSize = 12;
	private int foreground = Color.BLACK;
	private final IAxisValueFormatter axisFormatter = new IAxisValueFormatter() {
		@Override
		public String getFormattedValue(float value, AxisBase axis) {
			// 依据本次刻度精度格式化，缩放及左右轴切换后不沿用旧精度。
			return Utils.formatNumber(value, axis.mDecimals, false).replace(',', '.');
		}
	};

	private final IAxisValueFormatter categoryFormatter = new IAxisValueFormatter() {
		@Override
		public String getFormattedValue(float value, AxisBase axis) {
			return categoryLabel(value);
		}
	};

	/**
	 * 创建图表组件。
	 *
	 * @param container 所属可视容器。
	 */
	ChartComponent(组件容器 container) {
		super(container);
	}

	/**
	 * 创建当前类型对应的原生图表，不读取构造尚未完成的字段。
	 *
	 * @param context 视图上下文。
	 * @return 对应的原生图表。
	 */
	protected abstract Chart<?> createChart(Context context);

	/**
	 * 取得组件固定的图表类型。
	 *
	 * @return 图表类型编号。
	 */
	protected abstract int chartKind();

	@Override
	protected final View createView() {
		Context context = MainActivity.getContext();
		LinearLayout layout = new LinearLayout(context) {
			@Override
			protected void onDetachedFromWindow() {
				stopAnimation();
				chart.clearAllViewportJobs();
				super.onDetachedFromWindow();
			}
		};
		layout.setOrientation(LinearLayout.VERTICAL);
		caption = new TextView(context);
		caption.setGravity(android.view.Gravity.CENTER);
		layout.addView(caption, new LinearLayout.LayoutParams(
			ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		chart = createChart(context);
		chart.getDescription().setEnabled(false);
		chart.setNoDataText("暂无数据");
		chart.setLogEnabled(false);
		chart.setOnChartValueSelectedListener(this);
		layout.addView(chart, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
		return layout;
	}

	/* 图表属性 */

	@Override
	public final int 数据组数量() {
		checkAlive();
		return chartKind() == ChartModel.PIE ? (model.series.isEmpty() ? 0 : 1) : model.series.size();
	}

	@Override
	public final int 项目数量() {
		checkAlive();
		return chartKind() == ChartModel.PIE ? model.series.size() : model.names.length;
	}

	@Override
	public final int 动画时间() {
		return animationDuration;
	}

	@Override
	public final void 动画时间(int milliseconds) {
		checkAlive();
		if (milliseconds < 0 || milliseconds > 60000) {
			throw new IllegalArgumentException("动画时间必须在 0 至 60000 毫秒之间。");
		}
		animationDuration = milliseconds;
	}

	@Override
	public final boolean 显示图例() {
		return showLegend;
	}

	@Override
	public final void 显示图例(boolean visible) {
		checkAlive();
		showLegend = visible;
		chart.getLegend().setEnabled(visible);
		chart.invalidate();
	}

	@Override
	public final boolean 显示数值() {
		return showValues;
	}

	@Override
	public final void 显示数值(boolean visible) {
		checkAlive();
		showValues = visible;
		if (chart.getData() != null) {
			chart.getData().setDrawValues(visible);
		}
		chart.invalidate();
	}

	/**
	 * 取得坐标图是否允许拖动。
	 *
	 * @return 是否允许拖动。
	 */
	public final boolean 可否拖动() {
		return drag;
	}

	/**
	 * 设置坐标图的拖动开关。
	 *
	 * @param enabled 是否允许拖动。
	 */
	public final void 可否拖动(boolean enabled) {
		checkAlive();
		drag = enabled;
		cartesian().setDragEnabled(enabled);
	}

	/**
	 * 取得坐标图是否允许缩放。
	 *
	 * @return 是否允许缩放。
	 */
	public final boolean 可否缩放() {
		return zoom;
	}

	/**
	 * 设置坐标图缩放和双击缩放开关。
	 *
	 * @param enabled 是否允许缩放。
	 */
	public final void 可否缩放(boolean enabled) {
		checkAlive();
		zoom = enabled;
		cartesian().setScaleEnabled(enabled);
		cartesian().setPinchZoom(enabled);
		cartesian().setDoubleTapToZoomEnabled(enabled);
	}

	/**
	 * 取得折线平滑开关。
	 *
	 * @return 是否生成平滑曲线。
	 */
	public final boolean 平滑曲线() {
		return smooth;
	}

	/**
	 * 设置折线平滑开关，重新构建图表后生效。
	 *
	 * @param enabled 是否生成平滑曲线。
	 */
	public final void 平滑曲线(boolean enabled) {
		checkAlive();
		smooth = enabled;
	}

	/* 图表方法与事件 */

	@Override
	public final void 初始化图表(String title, float textSize, int backgroundColor) {
		checkAlive();
		if (title == null || Float.isNaN(textSize) || textSize < 6 || textSize > 24) {
			throw new IllegalArgumentException("标题不能为 null，字体大小必须在 6 至 24 之间。");
		}
		stopAnimation();
		fontSize = textSize;
		foreground = Color.alpha(backgroundColor) >= 128
			&& Color.red(backgroundColor) * 299 + Color.green(backgroundColor) * 587
			+ Color.blue(backgroundColor) * 114 < 128000 ? Color.WHITE : Color.BLACK;
		背景颜色(backgroundColor);
		caption.setText(title);
		caption.setVisibility(title.isEmpty() ? View.GONE : View.VISIBLE);
		caption.setTextSize(textSize);
		caption.setTextColor(foreground);
		ranges[0] = null;
		ranges[1] = null;
		initialized = true;
		清空数据();
	}

	/**
	 * 设置名称数组，用于各类带分类项目的图表。
	 *
	 * @param names 按数据数组顺序排列的项目名称。
	 */
	public final void 设置项目名称(String[] names) {
		checkReady();
		model.setNames(names);
	}

	/**
	 * 设置指定纵轴范围，重新构建后生效。
	 *
	 * @param axis 0 为左轴，1 为右轴。
	 * @param min 最小值。
	 * @param max 最大值。
	 */
	public final void 设置纵轴范围(int axis, float min, float max) {
		checkReady();
		ChartModel.checkRange(axis, min, max);
		ranges[axis] = new float[] { min, max };
	}

	/**
	 * 添加一组已复制的数值。
	 *
	 * @param kind 数据组类型。
	 * @param name 数据组名称。
	 * @param columns 数据列。
	 * @param color 数据组颜色。
	 * @param axis 坐标轴编号。
	 */
	final void addSeries(int kind, String name, float[][] columns, int color, int axis) {
		checkReady();
		model.add(kind, name, columns, color, axis);
	}

	/**
	 * 添加饼图扇区。
	 *
	 * @param name 扇区名称。
	 * @param value 非负数值。
	 * @param color 扇区颜色。
	 */
	final void addSlice(String name, float value, int color) {
		checkReady();
		model.addSlice(name, value, color);
	}

	@Override
	public final void 清空数据() {
		checkReady();
		stopAnimation();
		model.clear();
		entryLocations.clear();
		chart.clear();
	}

	@Override
	public final void 构建图表() {
		checkReady();
		if (chartKind() == ChartModel.PIE) {
			model.checkPie();
		}
		stopAnimation();
		entryLocations.clear();
		if (model.series.isEmpty()) {
			chart.clear();
			return;
		}
		buildData();
		applyStyle();
		if (chart instanceof BarChart || chart instanceof CombinedChart) {
			BarSelectionHighlighter.install(cartesian());
		}
		chart.notifyDataSetChanged();
		if (chart instanceof BarLineChartBase) {
			cartesian().fitScreen();
		}
		chart.highlightValues(null);
		startAnimation();
	}

	@Override
	public final void 项目被选中(int series, int item, float value) {
		EventDispatcher.dispatchEvent(this, "项目被选中", series, item, value);
	}

	@Override
	public final void 选择被取消() {
		EventDispatcher.dispatchEvent(this, "选择被取消");
	}

	@Override
	public final void onValueSelected(Entry entry, Highlight highlight) {
		int[] location = entryLocations.get(entry);
		if (!destroyed && location != null) {
			// 分组柱条的 X 值含组内偏移，饼图和雷达也不使用 X 值表示项目编号。
			float value = entry instanceof CandleEntry ? ((CandleEntry) entry).getClose() : entry.getY();
			项目被选中(location[0], location[1], value);
		}
	}

	@Override
	public final void onNothingSelected() {
		if (!destroyed) {
			选择被取消();
		}
	}

	@Override
	public void 销毁() {
		if (destroyed) {
			return;
		}
		checkAlive();
		destroyed = true;
		stopAnimation();
		chart.setOnChartValueSelectedListener(null);
		chart.clearAllViewportJobs();
		chart.clear();
		model.clear();
		entryLocations.clear();
		super.销毁();
	}

	/* 原生数据与渲染配置 */

	/**
	 * 根据已校验的数据组建立原生数据，不把第三方对象暴露给 Simple。
	 */
	private void buildData() {
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
				for (int index = 0; index < model.names.length; index++) {
					BarEntry entry = new BarEntry(index, item.columns[0][index]);
					entryLocations.put(entry, new int[] { group, index });
					entries.add(entry);
				}
				BarDataSet set = new BarDataSet(entries, item.name);
				configureSet(set, item);
				bars.add(set);
			} else if (item.kind == ChartModel.BUBBLE) {
				List<BubbleEntry> entries = new ArrayList<BubbleEntry>();
				for (int index = 0; index < model.names.length; index++) {
					BubbleEntry entry = new BubbleEntry(index, item.columns[0][index], item.columns[1][index]);
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
				for (int index = 0; index < model.names.length; index++) {
					CandleEntry entry = new CandleEntry(index, item.columns[1][index], item.columns[2][index],
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
				for (int index = 0; index < model.names.length; index++) {
					RadarEntry entry = new RadarEntry(item.columns[0][index]);
					entryLocations.put(entry, new int[] { group, index });
					entries.add(entry);
				}
				RadarDataSet set = new RadarDataSet(entries, item.name);
				configureSet(set, item);
				radars.add(set);
			} else {
				List<Entry> entries = new ArrayList<Entry>();
				for (int index = 0; index < model.names.length; index++) {
					Entry entry = new Entry(index, item.columns[0][index]);
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

		switch (chartKind()) {
			case ChartModel.LINE:
				((LineChart) chart).setData(new LineData(lines));
				break;
			case ChartModel.BAR:
			case ChartModel.HORIZONTAL:
				((BarChart) chart).setData(createBarData(bars));
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
					combined.setData(createBarData(bars));
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
	 * @param sets 按添加顺序组织的柱状数据组。
	 * @return 已设置柱宽和组内位置的数据。
	 */
	private BarData createBarData(List<IBarDataSet> sets) {
		BarData data = new BarData(sets);
		data.setBarWidth(0.8f / sets.size());
		if (sets.size() > 1) {
			data.groupBars(-0.5f, 0.2f, 0);
		}
		return data;
	}

	/**
	 * 按整数分类坐标查找名称，略过缩放过程中不对应项目的刻度。
	 *
	 * @param value 原生横轴坐标。
	 * @return 对应项目名称；范围外或非整数刻度返回空文本。
	 */
	private String categoryLabel(float value) {
		int index = Math.round(value);
		return index >= 0 && index < model.names.length && Math.abs(value - index) < 0.001f
			? model.names[index] : "";
	}

	/**
	 * 配置数据组颜色与左右轴归属。
	 *
	 * @param set 原生数据组。
	 * @param source 已校验的数据组。
	 */
	private void configureSet(DataSet<? extends Entry> set, ChartModel.Series source) {
		set.setColor(source.color);
		set.setAxisDependency(source.axis == 0 ? YAxis.AxisDependency.LEFT : YAxis.AxisDependency.RIGHT);
	}

	/**
	 * 把共同样式应用到当前原生图表和已构建的数据。
	 */
	private void applyStyle() {
		chart.getLegend().setEnabled(showLegend);
		chart.getLegend().setTextSize(fontSize);
		chart.getLegend().setTextColor(foreground);
		chart.getData().setValueTextColor(foreground);
		chart.getData().setValueTextSize(fontSize);
		chart.getData().setDrawValues(showValues);
		if (chart instanceof BarLineChartBase) {
			BarLineChartBase<?> view = cartesian();
			view.setDragEnabled(drag);
			view.setScaleEnabled(zoom);
			view.setPinchZoom(zoom);
			view.setDoubleTapToZoomEnabled(zoom);
			view.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
			view.getXAxis().setValueFormatter(categoryFormatter);
			view.getXAxis().setGranularity(1);
			// 给首尾项目留出半个分类槽，避免柱条、蜡烛和单点图被裁掉。
			view.getXAxis().setAxisMinimum(-0.5f);
			view.getXAxis().setAxisMaximum(model.names.length - 0.5f);
			view.getXAxis().setTextSize(fontSize);
			view.getXAxis().setTextColor(foreground);
			for (int axis = 0; axis < 2; axis++) {
				YAxis target = axis == 0 ? view.getAxisLeft() : view.getAxisRight();
				target.setTextSize(fontSize);
				target.setTextColor(foreground);
				target.setValueFormatter(axisFormatter);
				boolean used = false;
				boolean hasBubbles = false;
				for (ChartModel.Series series : model.series) {
					used |= series.axis == axis;
					hasBubbles |= series.axis == axis && series.kind == ChartModel.BUBBLE;
				}
				target.setEnabled(used);
				// 气泡按面积绘制，圆心之外还需上下留白；固定轴范围仍严格使用调用方的值。
				target.setSpaceTop(hasBubbles ? 20 : 10);
				target.setSpaceBottom(hasBubbles ? 20 : 10);
				if (ranges[axis] != null) {
					target.setAxisMinimum(ranges[axis][0]);
					target.setAxisMaximum(ranges[axis][1]);
				} else {
					target.resetAxisMinimum();
					target.resetAxisMaximum();
					if (chartKind() == ChartModel.BAR || chartKind() == ChartModel.HORIZONTAL
							|| (chartKind() == ChartModel.COMBINED && ((CombinedChart) chart).getBarData() != null)) {
						// 柱条始终以零为基线；负数仍属于有效数据，不能固定最小值为零。
						YAxis.AxisDependency dependency = axis == 0 ? YAxis.AxisDependency.LEFT : YAxis.AxisDependency.RIGHT;
						if (chart.getData().getYMin(dependency) >= 0) {
							target.setAxisMinimum(0);
						}
						if (chart.getData().getYMax(dependency) < 0) {
							target.setAxisMaximum(0);
						}
					}
				}
			}
		} else if (chart instanceof RadarChart) {
			RadarChart view = (RadarChart) chart;
			view.getXAxis().setValueFormatter(categoryFormatter);
			view.getXAxis().setTextColor(foreground);
			view.getXAxis().setTextSize(fontSize);
			view.getYAxis().setTextColor(foreground);
			view.getYAxis().setTextSize(fontSize);
			view.getYAxis().setValueFormatter(axisFormatter);
			view.getYAxis().setAxisMinimum(0);
			view.getYAxis().resetAxisMaximum();
			if (chart.getData().getYMax() == 0) {
				// 全零雷达仍需非零半径刻度，避免原生坐标变换除以零。
				view.getYAxis().setAxisMinimum(0);
				view.getYAxis().setAxisMaximum(1);
			}
		}
	}

	/**
	 * 取得当前原生直角坐标图表。
	 *
	 * @return 直角坐标图表。
	 */
	private BarLineChartBase<?> cartesian() {
		return (BarLineChartBase<?>) chart;
	}

	/**
	 * 动画由组件持有，更新数据、销毁或视图离开窗口时可取消。
	 */
	private void startAnimation() {
		if (animationDuration > 0) {
			animation = ValueAnimator.ofFloat(0, 1);
			animation.setDuration(animationDuration);
			animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
				@Override
				public void onAnimationUpdate(ValueAnimator value) {
					float phase = ((Float) value.getAnimatedValue()).floatValue();
					chart.getAnimator().setPhaseX(phase);
					chart.getAnimator().setPhaseY(phase);
					chart.invalidate();
				}
			});
			animation.start();
		}
		chart.invalidate();
	}

	/**
	 * 停止动画并恢复完整数据的绘制阶段。
	 */
	private void stopAnimation() {
		if (animation != null) {
			animation.cancel();
			animation.removeAllUpdateListeners();
			animation = null;
		}
		if (chart != null) {
			chart.getAnimator().setPhaseX(1);
			chart.getAnimator().setPhaseY(1);
		}
	}

	/**
	 * 检查初始化顺序，不把创建视图视为已设置图表数据。
	 */
	private void checkReady() {
		checkAlive();
		if (!initialized) {
			throw new IllegalStateException("请先初始化图表。");
		}
	}

	/**
	 * 组件只允许在界面线程操作，销毁后拒绝后续修改。
	 */
	private void checkAlive() {
		if (destroyed || Looper.myLooper() != Looper.getMainLooper()) {
			throw new IllegalStateException("请在界面线程操作未销毁的图表。");
		}
	}
}
