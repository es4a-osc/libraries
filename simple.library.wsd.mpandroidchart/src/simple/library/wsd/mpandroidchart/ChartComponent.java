package simple.library.wsd.mpandroidchart;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.charts.PieRadarChartBase;
import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.ChartData;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener.ChartGesture;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.MPPointD;
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
abstract class ChartComponent extends 视图组件 implements MPAC图表, OnChartValueSelectedListener, OnChartGestureListener {

	private final ChartModel model = new ChartModel();
	private final IdentityHashMap<Entry, int[]> entryLocations = new IdentityHashMap<Entry, int[]>();
	private final float[][] ranges = new float[2][];

	/**
	 * 应用时保存的独立配置。
	 */
	private MPAC图表样式 chartStyle;
	private MPAC图例样式 legendStyle;
	private MPAC饼图样式 pieStyle;
	private MPAC雷达样式 radarStyle;
	private MPAC数据组样式 pieDataStyle;
	private final MPAC轴样式[] axisStyles = new MPAC轴样式[3];
	private float[] xRange;
	private float barWidth = 0.8f;
	private float barGap;
	private boolean groupedBars = true;
	private int animationAxes;
	private int animationEasing;
	private int[] combinedOrder = { 0, 1, 2, 3, 4 };
	/**
	 * 原生数据快照是否仍对应当前模型。
	 */
	private boolean dataDirty = true;
	private Highlight manualHighlight;
	private int[] manualLocation;
	private static final Easing.EasingFunction[] EASINGS = { Easing.Linear, Easing.EaseInQuad, Easing.EaseOutQuad,
			Easing.EaseInOutQuad, Easing.EaseInCubic, Easing.EaseOutCubic, Easing.EaseInOutCubic, Easing.EaseInQuart,
			Easing.EaseOutQuart, Easing.EaseInOutQuart, Easing.EaseInSine, Easing.EaseOutSine, Easing.EaseInOutSine,
			Easing.EaseInExpo, Easing.EaseOutExpo, Easing.EaseInOutExpo, Easing.EaseInCirc, Easing.EaseOutCirc,
			Easing.EaseInOutCirc, Easing.EaseInElastic, Easing.EaseOutElastic, Easing.EaseInOutElastic,
			Easing.EaseInBack, Easing.EaseOutBack, Easing.EaseInOutBack, Easing.EaseInBounce, Easing.EaseOutBounce,
			Easing.EaseInOutBounce };

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
		layout.addView(caption, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT));
		chart = createChart(context);
		chart.getDescription().setEnabled(false);
		chart.setNoDataText("暂无数据");
		chart.setLogEnabled(false);
		chart.setOnChartValueSelectedListener(this);
		chart.setOnChartGestureListener(this);
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
		int count = model.names.length;
		for (ChartModel.Series group : model.series) {
			count = Math.max(count, group.xs.length);
		}
		return chartKind() == ChartModel.PIE ? model.series.size() : count;
	}

	@Override
	public final int 动画时间() {
		checkAlive();
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
		checkAlive();
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
		checkAlive();
		return showValues;
	}

	@Override
	public final void 显示数值(boolean visible) {
		checkAlive();
		showValues = visible;
		if (chart.getData() != null) {
			applyDataStyles();
		}
		chart.invalidate();
	}

	/**
	 * 取得坐标图是否允许拖动。
	 *
	 * @return 是否允许拖动。
	 */
	public final boolean 可否拖动() {
		checkAlive();
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
		cartesian().setDragXEnabled(enabled && (chartStyle == null || chartStyle.dragX));
		cartesian().setDragYEnabled(enabled && (chartStyle == null || chartStyle.dragY));
	}

	/**
	 * 取得坐标图是否允许缩放。
	 *
	 * @return 是否允许缩放。
	 */
	public final boolean 可否缩放() {
		checkAlive();
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
		cartesian().setScaleXEnabled(enabled && (chartStyle == null || chartStyle.scaleX));
		cartesian().setScaleYEnabled(enabled && (chartStyle == null || chartStyle.scaleY));
		cartesian().setPinchZoom(enabled && (chartStyle == null || chartStyle.pinch));
		cartesian().setDoubleTapToZoomEnabled(enabled && (chartStyle == null || chartStyle.doubleTap));
	}

	/**
	 * 取得折线平滑开关。
	 *
	 * @return 是否生成平滑曲线。
	 */
	public final boolean 平滑曲线() {
		checkAlive();
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
		foreground = Color.alpha(backgroundColor) >= 128 && Color.red(backgroundColor) * 299
				+ Color.green(backgroundColor) * 587 + Color.blue(backgroundColor) * 114 < 128000 ? Color.WHITE
						: Color.BLACK;
		背景颜色(backgroundColor);
		caption.setText(title);
		caption.setVisibility(title.isEmpty() ? View.GONE : View.VISIBLE);
		caption.setTextSize(textSize);
		caption.setTextColor(foreground);
		ranges[0] = null;
		ranges[1] = null;
		xRange = null;
		chartStyle = new MPAC图表样式();
		legendStyle = new MPAC图例样式();
		legendStyle.textColor = foreground;
		legendStyle.textSize = fontSize;
		pieStyle = new MPAC饼图样式();
		radarStyle = new MPAC雷达样式();
		pieDataStyle = null;
		chart.setNoDataText(chartStyle.noData);
		if (chart instanceof BarLineChartBase) {
			ChartStyles.axis(cartesian().getXAxis(), new MPAC轴样式(), Typeface.DEFAULT);
			ChartStyles.axis(cartesian().getAxisLeft(), new MPAC轴样式(), Typeface.DEFAULT);
			ChartStyles.axis(cartesian().getAxisRight(), new MPAC轴样式(), Typeface.DEFAULT);
		} else if (chart instanceof RadarChart) {
			ChartStyles.axis(((RadarChart) chart).getXAxis(), new MPAC轴样式(), Typeface.DEFAULT);
			ChartStyles.axis(((RadarChart) chart).getYAxis(), new MPAC轴样式(), Typeface.DEFAULT);
		}
		java.util.Arrays.fill(axisStyles, null);
		barWidth = 0.8f;
		barGap = 0;
		combinedOrder = new int[] { 0, 1, 2, 3, 4 };
		groupedBars = true;
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
		dataDirty = true;
	}

	/**
	 * 设置指定纵轴范围，重新构建后生效。
	 *
	 * @param axis 0 为左轴，1 为右轴。
	 * @param min  最小值。
	 * @param max  最大值。
	 */
	public final void 设置纵轴范围(int axis, float min, float max) {
		checkReady();
		ChartModel.checkRange(axis, min, max);
		ranges[axis] = new float[] { min, max };
	}

	/**
	 * 添加一组已复制的数值。
	 *
	 * @param kind    数据组类型。
	 * @param name    数据组名称。
	 * @param columns 数据列。
	 * @param color   数据组颜色。
	 * @param axis    坐标轴编号。
	 */
	final void addSeries(int kind, String name, float[][] columns, int color, int axis) {
		checkReady();
		model.add(kind, name, columns, color, axis);
		dataDirty = true;
	}

	/**
	 * 添加饼图扇区。
	 *
	 * @param name  扇区名称。
	 * @param value 非负数值。
	 * @param color 扇区颜色。
	 */
	final void addSlice(String name, float value, int color) {
		checkReady();
		model.addSlice(name, value, color);
		dataDirty = true;
	}

	@Override
	public final void 清空数据() {
		checkReady();
		stopAnimation();
		model.clear();
		dataDirty = true;
		manualHighlight = null;
		manualLocation = null;
		entryLocations.clear();
		chart.clear();
	}

	@Override
	public final void 构建图表() {
		checkReady();
		render(true);
	}

	/**
	 * 重建原生数据并根据调用方式保留或重置视图。
	 *
	 * @param reset 是否恢复全图并播放动画。
	 */
	private void render(boolean reset) {
		if (chartKind() == ChartModel.PIE) {
			model.checkPie();
		}
		stopAnimation();
		entryLocations.clear();
		manualHighlight = null;
		manualLocation = null;
		if (model.series.isEmpty()) {
			chart.clear();
			dataDirty = false;
			return;
		}
		if (chart instanceof CombinedChart) {
			CombinedChart.DrawOrder[] order = new CombinedChart.DrawOrder[5];
			for (int i = 0; i < order.length; i++) {
				order[i] = CombinedChart.DrawOrder.values()[combinedOrder[i]];
			}
			((CombinedChart) chart).setDrawOrder(order);
		}
		ChartDataBuilder.build(chart, chartKind(), model, entryLocations, smooth, barWidth, barGap, groupedBars);
		applyStyle();
		applyAdvancedStyles();
		if (chart instanceof BarChart || chart instanceof CombinedChart) {
			BarSelectionHighlighter.install(cartesian());
		}
		chart.notifyDataSetChanged();
		measureAxisLabels();
		if (reset && chart instanceof BarLineChartBase) {
			cartesian().fitScreen();
		}
		chart.highlightValues(null);
		dataDirty = false;
		if (reset) {
			startAnimation();
		} else {
			chart.invalidate();
		}
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
		manualHighlight = null;
		manualLocation = null;
		int[] location = entryLocations.get(entry);
		if (!destroyed && !dataDirty && location != null) {
			// 分组柱条的 X 值含组内偏移，饼图和雷达也不使用 X 值表示项目编号。
			float value = entry instanceof CandleEntry ? ((CandleEntry) entry).getClose() : entry.getY();
			项目被选中(location[0], location[1], value);
		}
	}

	@Override
	public final void onNothingSelected() {
		manualHighlight = null;
		manualLocation = null;
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
		chart.setOnChartGestureListener(null);
		chart.setMarker(null);
		chart.clearAllViewportJobs();
		chart.clear();
		model.clear();
		dataDirty = true;
		manualHighlight = null;
		manualLocation = null;
		entryLocations.clear();
		super.销毁();
	}

	/* 原生数据与渲染配置 */

	/**
	 * 按整数分类坐标查找名称，略过缩放过程中不对应项目的刻度。
	 *
	 * @param value 原生横轴坐标。
	 * @return 对应项目名称；范围外或非整数刻度返回空文本。
	 */
	private String categoryLabel(float value) {
		int index = Math.round(value);
		return index >= 0 && index < model.names.length && Math.abs(value - index) < 0.001f ? model.names[index] : "";
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
			view.getXAxis().setValueFormatter(model.numeric ? axisFormatter : categoryFormatter);
			view.getXAxis().setGranularity(1);
			// 给首尾项目留出半个分类槽，避免柱条、蜡烛和单点图被裁掉。
			view.getXAxis().setAxisMinimum(model.numeric ? chart.getData().getXMin() - barWidth / 2 : -0.5f);
			view.getXAxis().setAxisMaximum(
					model.numeric ? chart.getData().getXMax() + barWidth / 2 : model.names.length - 0.5f);
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
						YAxis.AxisDependency dependency = axis == 0 ? YAxis.AxisDependency.LEFT
								: YAxis.AxisDependency.RIGHT;
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
	 * 按当前格式器的实际绘制宽度保留轴标签空间，避免刷新后沿用旧宽度。
	 */
	private void measureAxisLabels() {
		if (!(chart instanceof BarLineChartBase)
				|| chart instanceof com.github.mikephil.charting.charts.HorizontalBarChart) {
			return;
		}
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		for (int axis = 0; axis < 2; axis++) {
			YAxis target = axis == 0 ? cartesian().getAxisLeft() : cartesian().getAxisRight();
			paint.setTextSize(target.getTextSize());
			paint.setTypeface(target.getTypeface());
			float width = Math.max(
					paint.measureText(target.getValueFormatter().getFormattedValue(target.mAxisMinimum, target)),
					paint.measureText(target.getValueFormatter().getFormattedValue(target.mAxisMaximum, target)));
			for (int i = 0; i < target.mEntryCount; i++) {
				width = Math.max(width, paint.measureText(target.getFormattedLabel(i)));
			}
			target.setMinWidth((width + target.getXOffset() * 2) / density() + 1);
		}
		cartesian().calculateOffsets();
	}

	/**
	 * 取得当前原生直角坐标图表。
	 *
	 * @return 直角坐标图表。
	 */
	private BarLineChartBase<?> cartesian() {
		if (!(chart instanceof BarLineChartBase)) {
			throw new IllegalArgumentException("此操作仅适用于直角坐标图。");
		}
		return (BarLineChartBase<?>) chart;
	}

	/**
	 * 动画由组件持有，更新数据、销毁或视图离开窗口时可取消。
	 */
	private void startAnimation() {
		if (animationDuration > 0) {
			animation = ValueAnimator.ofFloat(0, 1);
			animation.setDuration(animationDuration);
			animation.setInterpolator(EASINGS[animationEasing]);
			animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
				@Override
				public void onAnimationUpdate(ValueAnimator value) {
					float phase = ((Float) value.getAnimatedValue()).floatValue();
					chart.getAnimator().setPhaseX(animationAxes == 2 ? 1 : phase);
					chart.getAnimator().setPhaseY(animationAxes == 1 ? 1 : phase);
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
			throw new IllegalArgumentException("请先初始化图表。");
		}
	}

	/**
	 * 组件只允许在界面线程操作，销毁后拒绝后续修改。
	 */
	private void checkAlive() {
		if (ChartStyles.formatting()) {
			throw new IllegalArgumentException("格式器回调中不能操作图表。");
		}
		if (destroyed || Looper.myLooper() != Looper.getMainLooper()) {
			throw new IllegalArgumentException("请在界面线程操作未销毁的图表。");
		}
	}

	/**
	 * 重新绘制当前数据和配置，保留当前缩放和平移，不播放构建动画。
	 *
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	public void 刷新图表() {
		checkReady();
		render(false);
	}

	/**
	 * 保存配置副本，构建或刷新后生效。
	 *
	 * @param style 要复制的配置；不能为 null。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	public void 设置图表样式(MPAC图表样式 style) {
		checkReady();
		ChartChecks.required(style);
		ChartStyles.font(chart, style.font);
		chartStyle = style.复制();
		chart.setNoDataText(style.noData);
		chart.invalidate();
	}

	/**
	 * 保存配置副本，构建或刷新后生效。
	 *
	 * @param style 要复制的配置；不能为 null。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	public void 设置图例样式(MPAC图例样式 style) {
		checkReady();
		ChartChecks.required(style);
		ChartStyles.font(chart, style.font);
		legendStyle = style.复制();
	}

	/**
	 * 保存数据组样式副本，构建或刷新后生效。
	 *
	 * @param group 从 0 开始的数据组编号；饼图固定为 0。
	 * @param style 要复制的配置；不能为 null。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	public void 设置数据组样式(int group, MPAC数据组样式 style) {
		checkReady();
		ChartChecks.required(style);
		if (chartKind() == ChartModel.PIE) {
			ChartChecks.integer(group, 0, 数据组数量() - 1);
			pieDataStyle = style.复制();
		} else {
			model.group(group).style = style.复制();
		}
	}

	/**
	 * 返回数据组配置的独立副本；尚未设置时返回默认配置。
	 *
	 * @param group 从 0 开始的数据组编号；饼图固定为 0。
	 * @return 返回数据组配置的独立副本；尚未设置时返回默认配置。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	public MPAC数据组样式 取数据组样式(int group) {
		checkReady();
		MPAC数据组样式 style;
		if (chartKind() == ChartModel.PIE) {
			ChartChecks.integer(group, 0, 数据组数量() - 1);
			style = pieDataStyle;
		} else {
			style = model.group(group).style;
		}
		return style == null ? new MPAC数据组样式() : style.复制();
	}

	/**
	 * 取得数据组名称；饼图返回空文本，扇区名称由取项目名称取得。
	 *
	 * @param group 从 0 开始的数据组编号；饼图固定为 0。
	 * @return 取得数据组名称；饼图返回空文本，扇区名称由取项目名称取得。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	public String 取数据组名称(int group) {
		checkReady();
		if (chartKind() == ChartModel.PIE) {
			ChartChecks.integer(group, 0, 数据组数量() - 1);
			return "";
		}
		return model.group(group).name;
	}

	/**
	 * 取得分类名称副本；数值横坐标返回空数组，饼图返回扇区名称。
	 *
	 * @return 取得分类名称副本；数值横坐标返回空数组，饼图返回扇区名称。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	public String[] 取项目名称() {
		checkReady();
		if (chartKind() != ChartModel.PIE) {
			return model.names.clone();
		}
		String[] names = new String[model.series.size()];
		for (int i = 0; i < names.length; i++) {
			names[i] = model.series.get(i).name;
		}
		return names;
	}

	/**
	 * 取得数据组横坐标副本；分类图和饼图返回项目索引。
	 *
	 * @param group 从 0 开始的数据组编号；饼图固定为 0。
	 * @return 取得数据组横坐标副本；分类图和饼图返回项目索引。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	public float[] 取横坐标(int group) {
		checkReady();
		if (chartKind() == ChartModel.PIE) {
			ChartChecks.integer(group, 0, 数据组数量() - 1);
			float[] xs = new float[model.series.size()];
			for (int i = 0; i < xs.length; i++) {
				xs[i] = i;
			}
			return xs;
		}
		return model.group(group).xs.clone();
	}

	/**
	 * 取得指定列的数值副本。
	 *
	 * @param group  从 0 开始的数据组编号；饼图固定为 0。
	 * @param column 普通图和饼图为 0；气泡 0 数值、1 大小；蜡烛 0 开、1 高、2 低、3 收；堆叠为层号。
	 * @return 取得指定列的数值副本。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	public float[] 取数据组数值(int group, int column) {
		checkReady();
		if (chartKind() == ChartModel.PIE) {
			ChartChecks.integer(group, 0, 数据组数量() - 1);
			ChartChecks.integer(column, 0, 0);
			float[] result = new float[model.series.size()];
			for (int i = 0; i < result.length; i++) {
				result[i] = model.series.get(i).columns[0][0];
			}
			return result;
		}
		ChartModel.Series data = model.group(group);
		ChartChecks.integer(column, 0, data.columns.length - 1);
		return data.columns[column].clone();
	}

	/**
	 * 修改数据组名称、原颜色和轴归属，构建或刷新后生效；饼图请使用更新扇区。
	 *
	 * @param group 从 0 开始的数据组编号；饼图固定为 0。
	 * @param name  非空引用的数据组名称。
	 * @param color ARGB 颜色。
	 * @param axis  0 左轴，1 右轴。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	public void 设置数据组信息(int group, String name, int color, int axis) {
		checkReady();
		requireSeries();
		ChartChecks.text(name);
		ChartChecks.integer(axis, 0, 1);
		if (chartKind() == ChartModel.RADAR && axis != 0) {
			throw new IllegalArgumentException("雷达图仅有一条数值轴。");
		}
		ChartModel.Series data = model.group(group);
		data.name = name;
		data.color = color;
		data.axis = axis;
		dataDirty = true;
	}

	/**
	 * 替换整组数据并保留样式；分类图横坐标必须仍为原索引，数值图可改变项目数量；饼图请使用更新扇区。
	 *
	 * @param group   从 0 开始的数据组编号；饼图固定为 0。
	 * @param xs      非空有限横坐标数组，按非递减顺序排列。
	 * @param values  非空有限数值数组。
	 * @param columns 列数必须与原数据组一致。数值按整列依次拼接，例如气泡先全部数值再全部大小。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	public void 更新数据组(int group, float[] xs, float[] values, int columns) {
		checkReady();
		requireSeries();
		if (columns != model.group(group).columns.length) {
			throw new IllegalArgumentException("数据列数必须与原数据组一致。");
		}
		model.replace(group, xs, values, columns);
		dataDirty = true;
	}

	/**
	 * 向数值横坐标数据组追加一个项目，构建或刷新后生效；分类图请替换整组数据。
	 *
	 * @param group  从 0 开始的数据组编号；饼图固定为 0。
	 * @param x      不小于本组最后横坐标的有限数值。
	 * @param values 此项目的各列数值，顺序与更新数据组一致。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	public void 追加项目(int group, float x, float[] values) {
		checkReady();
		requireSeries();
		model.append(group, x, values);
		dataDirty = true;
	}

	/**
	 * 删除数值横坐标数据组的一个项目，至少保留一个；构建或刷新后生效。
	 *
	 * @param group 从 0 开始的数据组编号；饼图固定为 0。
	 * @param index 从 0 开始的项目编号。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	public void 删除项目(int group, int index) {
		checkReady();
		requireSeries();
		model.remove(group, index);
		dataDirty = true;
	}

	/**
	 * 删除整组数据，构建或刷新后生效；饼图删除全部扇区。
	 *
	 * @param group 从 0 开始的数据组编号；饼图固定为 0。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	public void 删除数据组(int group) {
		checkReady();
		if (chartKind() == ChartModel.PIE) {
			ChartChecks.integer(group, 0, 数据组数量() - 1);
			model.clear();
			dataDirty = true;
			manualHighlight = null;
			manualLocation = null;
		} else {
			model.group(group);
			model.series.remove(group);
			dataDirty = true;
			if (model.series.isEmpty()) {
				model.numeric = false;
			}
		}
	}

	/**
	 * 主动选择已构建的项目；数据变更后须先构建或刷新，重复横坐标按项目编号精确选择。
	 *
	 * @param group  从 0 开始的数据组编号；饼图固定为 0。
	 * @param index  从 0 开始的项目编号。
	 * @param notify 是否触发项目被选中事件。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	public void 选择项目(int group, int index, boolean notify) {
		checkReady();
		select(group, index, -1, notify);
	}

	/**
	 * 取消当前高亮。
	 *
	 * @param notify 是否触发选择被取消事件。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	public void 取消选择(boolean notify) {
		checkReady();
		manualHighlight = null;
		manualLocation = null;
		chart.highlightValue((Highlight) null, notify);
	}

	/**
	 * 返回数据组、项目、堆叠层三个编号；未选择时返回空数组，非堆叠层为 -1。
	 *
	 * @return 返回数据组、项目、堆叠层三个编号；未选择时返回空数组，非堆叠层为 -1。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	public int[] 取选中项目() {
		checkReady();
		if (dataDirty) {
			return new int[0];
		}
		Highlight[] selected = chart.getHighlighted();
		if (selected == null || selected.length == 0 || chart.getData() == null) {
			return new int[0];
		}
		if (selected[0] == manualHighlight && manualLocation != null) {
			return manualLocation.clone();
		}
		Entry entry = chart.getData().getEntryForHighlight(selected[0]);
		int[] location = entryLocations.get(entry);
		return location == null ? new int[0] : new int[] { location[0], location[1], selected[0].getStackIndex() };
	}

	/**
	 * 导出包含标题的当前组件截图；需已完成布局，返回压缩字节，不写文件，不需要存储权限。
	 *
	 * @param format  0 PNG，1 JPEG。
	 * @param quality 0 至 100；PNG 不使用质量参数。
	 * @return 导出包含标题的当前组件截图；需已完成布局，返回压缩字节，不写文件，不需要存储权限。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	public byte[] 导出图片(int format, int quality) {
		checkReady();
		ChartChecks.integer(format, 0, 1);
		ChartChecks.integer(quality, 0, 100);
		View view = getView();
		if (view.getWidth() <= 0 || view.getHeight() <= 0) {
			throw new IllegalArgumentException("请等待图表完成布局后再导出图片。");
		}
		Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
		try {
			Canvas canvas = new Canvas(bitmap);
			canvas.drawColor(Color.WHITE);
			view.draw(canvas);
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			if (!bitmap.compress(format == 0 ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG, quality,
					output)) {
				throw new IllegalArgumentException("图片编码失败。");
			}
			return output.toByteArray();
		} finally {
			bitmap.recycle();
		}
	}

	/**
	 * 设置构建动画的坐标方向及缓动方式，时长由动画时间属性控制。
	 *
	 * @param axes   0 双轴，1 横轴，2 纵轴。
	 * @param easing 0 至 27：0 Linear, 1 EaseInQuad, 2 EaseOutQuad, 3 EaseInOutQuad, 4 EaseInCubic, 5
	 *               EaseOutCubic, 6 EaseInOutCubic, 7 EaseInQuart, 8 EaseOutQuart, 9 EaseInOutQuart, 10
	 *               EaseInSine, 11 EaseOutSine, 12 EaseInOutSine, 13 EaseInExpo, 14 EaseOutExpo, 15
	 *               EaseInOutExpo, 16 EaseInCirc, 17 EaseOutCirc, 18 EaseInOutCirc, 19 EaseInElastic,
	 *               20 EaseOutElastic, 21 EaseInOutElastic, 22 EaseInBack, 23 EaseOutBack, 24
	 *               EaseInOutBack, 25 EaseInBounce, 26 EaseOutBounce, 27 EaseInOutBounce。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	public void 设置动画(int axes, int easing) {
		checkReady();
		ChartChecks.integer(axes, 0, 2);
		ChartChecks.integer(easing, 0, EASINGS.length - 1);
		stopAnimation();
		animationAxes = axes;
		animationEasing = easing;
	}

	/**
	 * 停止当前构建动画并显示完整数据。
	 *
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	public void 停止动画() {
		checkReady();
		stopAnimation();
		chart.invalidate();
	}

	/**
	 * 保存轴配置副本，构建或刷新后生效。
	 *
	 * @param axis  0 分类或横轴，1 左轴，2 右轴；横向柱图数值轴位于下方和上方。
	 * @param style 要复制的轴配置。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	public void 设置轴样式(int axis, MPAC轴样式 style) {
		checkReady();
		ChartChecks.integer(axis, 0, 2);
		ChartChecks.required(style);
		ChartStyles.font(chart, style.font);
		axisStyles[axis] = style.复制();
	}

	/**
	 * 固定横轴范围，构建或刷新后生效。
	 *
	 * @param min 有限数值，必须小于最大值。
	 * @param max 有限数值。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	public void 设置横轴范围(float min, float max) {
		checkReady();
		ChartModel.checkRange(0, min, max);
		xRange = new float[] { min, max };
	}

	/**
	 * 恢复指定坐标轴的自动范围，构建或刷新后生效。
	 *
	 * @param axis 0 横轴，1 左轴，2 右轴。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	public void 恢复自动范围(int axis) {
		checkReady();
		ChartChecks.integer(axis, 0, 2);
		if (axis == 0) {
			xRange = null;
		} else {
			ranges[axis - 1] = null;
		}
	}

	/**
	 * 以内容中心缩放已构建图表。
	 *
	 * @param x 相对当前视图的倍数，必须大于零。
	 * @param y 相对当前视图的倍数，必须大于零。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	public void 缩放视图(float x, float y) {
		checkReady();
		requireBuilt();
		ChartChecks.range(x, 0.000001f, 1000000f);
		ChartChecks.range(y, 0.000001f, 1000000f);
		cartesian().zoomToCenter(x, y);
	}

	/**
	 * 移动已构建图表的可见区域。
	 *
	 * @param x    视图左边缘横坐标。
	 * @param y    视图中心纵坐标。
	 * @param axis 0 左轴，1 右轴。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	public void 移动视图(float x, float y, int axis) {
		checkReady();
		requireBuilt();
		ChartModel.checkFinite(x);
		ChartModel.checkFinite(y);
		ChartChecks.integer(axis, 0, 1);
		cartesian().moveViewTo(x, y, axis == 0 ? YAxis.AxisDependency.LEFT : YAxis.AxisDependency.RIGHT);
	}

	/**
	 * 限制已构建图表的可见数据跨度。
	 *
	 * @param min 必须大于零，且不大于最大跨度。
	 * @param max 可见区域的数据跨度。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	public void 设置可见横轴范围(float min, float max) {
		checkReady();
		requireBuilt();
		ChartChecks.range(min, 0.000001f, 1e30f);
		ChartChecks.range(max, min, 1e30f);
		cartesian().setVisibleXRange(min, max);
	}

	/**
	 * 限制已构建图表的可见数据跨度。
	 *
	 * @param min  必须大于零，且不大于最大跨度。
	 * @param max  可见区域的数据跨度。
	 * @param axis 0 左轴，1 右轴。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	public void 设置可见纵轴范围(float min, float max, int axis) {
		checkReady();
		requireBuilt();
		ChartChecks.range(min, 0.000001f, 1e30f);
		ChartChecks.range(max, min, 1e30f);
		ChartChecks.integer(axis, 0, 1);
		cartesian().setVisibleYRange(min, max, axis == 0 ? YAxis.AxisDependency.LEFT : YAxis.AxisDependency.RIGHT);
	}

	/**
	 * 恢复显示全部数据，清除可见跨度限制。
	 *
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	public void 重置视图() {
		checkReady();
		requireBuilt();
		cartesian().getViewPortHandler().setMaximumScaleX(Float.MAX_VALUE);
		cartesian().getViewPortHandler().setMaximumScaleY(Float.MAX_VALUE);
		cartesian().fitScreen();
	}

	/**
	 * 返回可见横轴最小值、最大值、横向缩放倍数、纵向缩放倍数。
	 *
	 * @return 返回可见横轴最小值、最大值、横向缩放倍数、纵向缩放倍数。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	public float[] 取视图范围() {
		checkReady();
		requireBuilt();
		return new float[] { cartesian().getLowestVisibleX(), cartesian().getHighestVisibleX(), cartesian().getScaleX(),
				cartesian().getScaleY() };
	}

	/**
	 * 转换已布局图表的坐标，返回横纵坐标数组；像素坐标单位为 DIP，不含标题。
	 *
	 * @param x    数据横坐标。
	 * @param y    数据纵坐标。
	 * @param axis 0 左轴，1 右轴。
	 * @return 转换已布局图表的坐标，返回横纵坐标数组；像素坐标单位为 DIP，不含标题。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	public float[] 数据坐标转像素(float x, float y, int axis) {
		checkReady();
		requireBuilt();
		ChartModel.checkFinite(x);
		ChartModel.checkFinite(y);
		ChartChecks.integer(axis, 0, 1);
		float density = chart.getResources().getDisplayMetrics().density;
		MPPointD point = cartesian().getPixelForValues(x, y,
				axis == 0 ? YAxis.AxisDependency.LEFT : YAxis.AxisDependency.RIGHT);
		try {
			return new float[] { (float) point.x / density, (float) point.y / density };
		} finally {
			MPPointD.recycleInstance(point);
		}
	}

	/**
	 * 转换已布局图表的坐标，返回横纵坐标数组；像素坐标单位为 DIP，不含标题。
	 *
	 * @param x    相对原生图表左上角的 DIP 横坐标，不含标题。
	 * @param y    相对原生图表左上角的 DIP 纵坐标，不含标题。
	 * @param axis 0 左轴，1 右轴。
	 * @return 转换已布局图表的坐标，返回横纵坐标数组；像素坐标单位为 DIP，不含标题。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	public float[] 像素转数据坐标(float x, float y, int axis) {
		checkReady();
		requireBuilt();
		ChartModel.checkFinite(x);
		ChartModel.checkFinite(y);
		ChartChecks.integer(axis, 0, 1);
		float density = chart.getResources().getDisplayMetrics().density;
		MPPointD point = cartesian().getValuesByTouchPoint(x * density, y * density,
				axis == 0 ? YAxis.AxisDependency.LEFT : YAxis.AxisDependency.RIGHT);
		try {
			return new float[] { (float) point.x, (float) point.y };
		} finally {
			MPPointD.recycleInstance(point);
		}
	}

	/**
	 * 设置柱宽和并排方式，数值横坐标保持原值，构建或刷新后生效。
	 *
	 * @param width   同一横坐标下全部柱的总宽，数据单位，必须大于零。
	 * @param gap     分组柱之间的空白，数据单位，不能为负数。
	 * @param grouped True 并排，False 重叠；仅柱图和含柱的组合图有效。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	public void 设置柱布局(float width, float gap, boolean grouped) {
		checkReady();
		ChartChecks.range(width, 0.000001f, 1e30f);
		ChartChecks.range(gap, 0, 1e30f);
		barWidth = width;
		barGap = gap;
		groupedBars = grouped;
	}

	/**
	 * 保存饼图配置副本，构建或刷新后生效。
	 *
	 * @param style 要复制的饼图配置。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	public void 设置饼图样式(MPAC饼图样式 style) {
		checkReady();
		ChartChecks.required(style);
		if (style.transparentRadius < style.holeRadius) {
			throw new IllegalArgumentException("透明环半径不能小于圆孔半径。");
		}
		pieStyle = style.复制();
	}

	/**
	 * 更新指定扇区，构建或刷新后生效。
	 *
	 * @param index 从 0 开始的项目编号。
	 * @param name  非空引用的数据组名称。
	 * @param value 非负有限数值。
	 * @param color ARGB 颜色。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	public void 更新扇区(int index, String name, float value, int color) {
		checkReady();
		model.updateSlice(index, name, value, color);
		dataDirty = true;
	}

	/**
	 * 删除指定扇区，构建或刷新后生效。
	 *
	 * @param index 从 0 开始的项目编号。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	public void 删除扇区(int index) {
		checkReady();
		model.group(index);
		model.series.remove(index);
		dataDirty = true;
	}

	/**
	 * 保存雷达网格配置副本，构建或刷新后生效。
	 *
	 * @param style 要复制的雷达配置。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	public void 设置雷达样式(MPAC雷达样式 style) {
		checkReady();
		ChartChecks.required(style);
		radarStyle = style.复制();
	}

	/**
	 * 保存雷达轴配置副本，构建或刷新后生效。
	 *
	 * @param axis  0 项目轴，1 数值轴。
	 * @param style 要复制的轴配置。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	public void 设置雷达轴样式(int axis, MPAC轴样式 style) {
		checkReady();
		ChartChecks.integer(axis, 0, 1);
		ChartChecks.required(style);
		ChartStyles.font(chart, style.font);
		axisStyles[axis] = style.复制();
	}

	/**
	 * 设置雷达数值轴范围，构建或刷新后生效。
	 *
	 * @param min 非负有限最小值，必须小于最大值。
	 * @param max 有限最大值。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	public void 设置数值范围(float min, float max) {
		checkReady();
		ChartModel.checkRange(0, min, max);
		if (min < 0) {
			throw new IllegalArgumentException("雷达数值轴最小值不能为负数。");
		}
		ranges[0] = new float[] { min, max };
	}

	/**
	 * 派发图表手势事件。
	 *
	 * @param gesture 0 无，1 拖动，2 横向缩放，3 纵向缩放，4 双轴缩放，5 旋转，6 单击，7 双击，8 长按，9 惯性。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	@Override
	public void 手势开始(int gesture) {
		EventDispatcher.dispatchEvent(this, "手势开始", gesture);
	}

	/**
	 * 派发图表手势事件。
	 *
	 * @param gesture 与手势开始事件相同。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	@Override
	public void 手势结束(int gesture) {
		EventDispatcher.dispatchEvent(this, "手势结束", gesture);
	}

	/**
	 * 派发图表手势事件。
	 *
	 * @param x 相对图表内容视图的 DIP 横坐标。
	 * @param y 相对图表内容视图的 DIP 纵坐标。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	@Override
	public void 图表单击(float x, float y) {
		EventDispatcher.dispatchEvent(this, "图表单击", x, y);
	}

	/**
	 * 派发图表手势事件。
	 *
	 * @param x DIP 横坐标。
	 * @param y DIP 纵坐标。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	@Override
	public void 图表双击(float x, float y) {
		EventDispatcher.dispatchEvent(this, "图表双击", x, y);
	}

	/**
	 * 派发图表手势事件。
	 *
	 * @param x DIP 横坐标。
	 * @param y DIP 纵坐标。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	@Override
	public void 图表长按(float x, float y) {
		EventDispatcher.dispatchEvent(this, "图表长按", x, y);
	}

	/**
	 * 派发图表手势事件。
	 *
	 * @param x 本次 DIP 位移。
	 * @param y 本次 DIP 位移。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	@Override
	public void 图表拖动(float x, float y) {
		EventDispatcher.dispatchEvent(this, "图表拖动", x, y);
	}

	/**
	 * 派发图表手势事件。
	 *
	 * @param x 本次缩放倍数。
	 * @param y 本次缩放倍数。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	@Override
	public void 图表缩放(float x, float y) {
		EventDispatcher.dispatchEvent(this, "图表缩放", x, y);
	}

	/**
	 * 派发图表手势事件。
	 *
	 * @param x DIP 每秒。
	 * @param y DIP 每秒。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	@Override
	public void 图表惯性(float x, float y) {
		EventDispatcher.dispatchEvent(this, "图表惯性", x, y);
	}

	/**
	 * 检查已构建且对应当前模型的数据快照。
	 */
	private void requireBuilt() {
		if (dataDirty || chart.getData() == null || chart.getWidth() <= 0 || chart.getHeight() <= 0) {
			throw new IllegalArgumentException("请先构建或刷新图表并等待布局完成。");
		}
	}

	/**
	 * 检查不适用于饼图的数据组操作。
	 */
	private void requireSeries() {
		if (chartKind() == ChartModel.PIE) {
			throw new IllegalArgumentException("饼图请使用扇区接口。");
		}
	}

	/**
	 * 添加堆叠柱并标记数据快照过期。
	 *
	 * @param kind   柱图类型。
	 * @param name   数据组名称。
	 * @param xs     数值横坐标，null 表示分类坐标。
	 * @param values 按层排列的数值。
	 * @param layers 堆叠层数。
	 * @param color  ARGB 颜色。
	 * @param axis   数值轴编号。
	 */
	final void addStack(int kind, String name, float[] xs, float[] values, int layers, int color, int axis) {
		checkReady();
		model.addStack(kind, name, xs, values, layers, color, axis);
		dataDirty = true;
	}

	/**
	 * 添加数值横坐标数据组。
	 *
	 * @param kind    图形类型。
	 * @param name    数据组名称。
	 * @param xs      数值横坐标。
	 * @param columns 数值列。
	 * @param color   数据组颜色。
	 * @param axis    数值轴编号。
	 */
	final void addNumeric(int kind, String name, float[] xs, float[][] columns, int color, int axis) {
		checkReady();
		model.addXY(kind, name, xs, columns, color, axis);
		dataDirty = true;
	}

	/**
	 * 应用每组独立配置；组合图按数据对象映射回原添加顺序。
	 */
	private void applyDataStyles() {
		chart.getData().setDrawValues(showValues);
		for (IDataSet<?> data : chart.getData().getDataSets()) {
			if (data.getEntryCount() == 0) {
				continue;
			}
			int[] location = entryLocations.get(data.getEntryForIndex(0));
			MPAC数据组样式 style = chartKind() == ChartModel.PIE ? pieDataStyle
					: location == null ? null : model.series.get(location[0]).style;
			if (style != null) {
				ChartStyles.data((DataSet<?>) data, style, showValues);
			}
		}
	}

	/**
	 * 应用图表、坐标轴和图例配置，明确范围优先于自动计算。
	 */
	private void applyAdvancedStyles() {
		if (chartStyle != null) {
			MPAC图表样式 style = chartStyle;
			Typeface typeface = ChartStyles.font(chart, style.font);
			caption.setTypeface(typeface);
			chart.getLegend().setTypeface(typeface);
			chart.getData().setValueTypeface(typeface);
			chart.setNoDataText(style.noData);
			chart.setNoDataTextTypeface(typeface);
			chart.setTouchEnabled(style.touch);
			chart.setHighlightPerTapEnabled(style.tapHighlight);
			chart.setDragDecelerationEnabled(style.deceleration);
			chart.setDragDecelerationFrictionCoef(style.friction);
			chart.setExtraOffsets(style.left, style.top, style.right, style.bottom);
			chart.getDescription().setText(style.description);
			chart.getDescription().setEnabled(!style.description.isEmpty());
			chart.getDescription().setTextColor(style.descriptionColor);
			chart.getDescription().setTextSize(style.descriptionSize);
			chart.getDescription().setTypeface(typeface);
			chart.setDrawMarkers(style.markers);
			chart.setMarker(style.markers ? new ChartMarker(chart, style) : null);
			if (chart instanceof BarLineChartBase) {
				BarLineChartBase<?> view = cartesian();
				view.setHighlightPerDragEnabled(style.dragHighlight);
				view.setDrawBorders(style.borders);
				view.setBorderColor(style.borderColor);
				view.setBorderWidth(style.borderWidth);
				view.setDrawGridBackground(style.gridBackground);
				view.setGridBackgroundColor(style.gridColor);
				view.setAutoScaleMinMaxEnabled(style.autoScale);
				view.setScaleXEnabled(zoom && style.scaleX);
				view.setScaleYEnabled(zoom && style.scaleY);
				view.setDragXEnabled(drag && style.dragX);
				view.setDragYEnabled(drag && style.dragY);
				view.setDoubleTapToZoomEnabled(zoom && style.doubleTap);
				view.setPinchZoom(zoom && style.pinch);
				view.setMaxVisibleValueCount(style.maxValues);
				view.getXAxis().setTypeface(typeface);
				view.getAxisLeft().setTypeface(typeface);
				view.getAxisRight().setTypeface(typeface);
			} else if (chart instanceof PieRadarChartBase) {
				((PieRadarChartBase<?>) chart).setRotationEnabled(style.rotation);
				((PieRadarChartBase<?>) chart).setRotationAngle(style.rotationAngle);
			}
		}
		if (chartStyle != null && chart instanceof BarChart) {
			BarChart view = (BarChart) chart;
			view.setDrawValueAboveBar(chartStyle.valuesAbove);
			view.setDrawBarShadow(chartStyle.barShadows);
			view.setHighlightFullBarEnabled(chartStyle.fullBar);
		}
		if (chartStyle != null && chart instanceof CombinedChart) {
			CombinedChart view = (CombinedChart) chart;
			view.setDrawValueAboveBar(chartStyle.valuesAbove);
			view.setDrawBarShadow(chartStyle.barShadows);
			view.setHighlightFullBarEnabled(chartStyle.fullBar);
		}
		if (legendStyle != null) {
			ChartStyles.legend(chart.getLegend(), legendStyle, ChartStyles.font(chart, legendStyle.font));
		}
		if (chart instanceof BarLineChartBase) {
			for (int axis = 0; axis < 3; axis++) {
				if (axisStyles[axis] != null) {
					ChartStyles.axis(
							axis == 0 ? cartesian().getXAxis()
									: axis == 1 ? cartesian().getAxisLeft() : cartesian().getAxisRight(),
							axisStyles[axis], ChartStyles.font(chart, axisStyles[axis].font));
				}
			}
			if (xRange != null) {
				cartesian().getXAxis().setAxisMinimum(xRange[0]);
				cartesian().getXAxis().setAxisMaximum(xRange[1]);
			}
		} else if (chart instanceof RadarChart) {
			RadarChart view = (RadarChart) chart;
			if (radarStyle != null) {
				ChartStyles.radar(view, radarStyle);
			}
			for (int axis = 0; axis < 2; axis++) {
				if (axisStyles[axis] != null) {
					ChartStyles.axis(axis == 0 ? view.getXAxis() : view.getYAxis(), axisStyles[axis],
							ChartStyles.font(chart, axisStyles[axis].font));
				}
			}
			if (ranges[0] != null) {
				view.getYAxis().setAxisMinimum(ranges[0][0]);
				view.getYAxis().setAxisMaximum(ranges[0][1]);
			}
		} else if (pieStyle != null) {
			ChartStyles.pie((PieChart) chart, pieStyle);
		}
		applyDataStyles();
	}

	/**
	 * 按原始编号定位原生数据，支持组合图和分组柱偏移。
	 *
	 * @param group  数据组编号。
	 * @param index  项目编号。
	 * @param layer  堆叠层编号，-1 表示整项。
	 * @param notify 是否派发选择事件。
	 */
	private void select(int group, int index, int layer, boolean notify) {
		requireBuilt();
		ChartChecks.integer(group, 0, 数据组数量() - 1);
		ChartChecks.integer(index, 0,
				chartKind() == ChartModel.PIE ? model.series.size() - 1 : model.group(group).xs.length - 1);
		ChartData<?> data = chart.getData();
		java.util.List<ChartData<?>> children = new java.util.ArrayList<ChartData<?>>();
		if (data instanceof CombinedData) {
			for (Object child : ((CombinedData) data).getAllData()) {
				children.add((ChartData<?>) child);
			}
		} else {
			children.add(data);
		}
		for (int child = 0; child < children.size(); child++) {
			ChartData<?> part = children.get(child);
			for (int setIndex = 0; setIndex < part.getDataSetCount(); setIndex++) {
				IDataSet<?> set = part.getDataSetByIndex(setIndex);
				for (int entryIndex = 0; entryIndex < set.getEntryCount(); entryIndex++) {
					Entry entry = set.getEntryForIndex(entryIndex);
					int[] location = entryLocations.get(entry);
					if (location != null && location[0] == group && location[1] == index) {
						if (!set.isVisible() || !set.isHighlightEnabled()) {
							throw new IllegalArgumentException("此数据组不可见或不允许高亮。");
						}
						Highlight selected = new Highlight(
								chartKind() == ChartModel.PIE || chartKind() == ChartModel.RADAR ? index : entry.getX(),
								entry.getY(), 0, 0, setIndex, layer, set.getAxisDependency());
						if (data instanceof CombinedData) {
							selected.setDataIndex(child);
						}
						chart.highlightValue(selected, false);
						manualHighlight = selected;
						manualLocation = new int[] { group, index, layer };
						if (notify) {
							项目被选中(group, index,
									entry instanceof CandleEntry ? ((CandleEntry) entry).getClose() : entry.getY());
						}
						return;
					}
				}
			}
		}
		throw new IllegalArgumentException("未找到已构建的项目。");
	}

	@Override
	public void onChartGestureStart(MotionEvent event, ChartGesture gesture) {
		if (!destroyed) {
			手势开始(gesture.ordinal());
		}
	}

	@Override
	public void onChartGestureEnd(MotionEvent event, ChartGesture gesture) {
		if (!destroyed) {
			手势结束(gesture.ordinal());
		}
	}

	@Override
	public void onChartLongPressed(MotionEvent event) {
		if (!destroyed) {
			图表长按(event.getX() / density(), event.getY() / density());
		}
	}

	@Override
	public void onChartDoubleTapped(MotionEvent event) {
		if (!destroyed) {
			图表双击(event.getX() / density(), event.getY() / density());
		}
	}

	@Override
	public void onChartSingleTapped(MotionEvent event) {
		if (!destroyed) {
			图表单击(event.getX() / density(), event.getY() / density());
		}
	}

	@Override
	public void onChartFling(MotionEvent start, MotionEvent end, float x, float y) {
		if (!destroyed) {
			图表惯性(x / density(), y / density());
		}
	}

	@Override
	public void onChartScale(MotionEvent event, float x, float y) {
		if (!destroyed) {
			图表缩放(x, y);
		}
	}

	@Override
	public void onChartTranslate(MotionEvent event, float x, float y) {
		if (!destroyed) {
			图表拖动(x / density(), y / density());
		}
	}

	/**
	 * 取得 DIP 转换密度。
	 *
	 * @return 当前显示密度。
	 */
	private float density() {
		return chart.getResources().getDisplayMetrics().density;
	}

	/**
	 * 主动选择已构建的堆叠柱中的一层。
	 *
	 * @param group  从 0 开始的数据组编号。
	 * @param index  从 0 开始的项目编号。
	 * @param layer  从 0 开始的层编号。
	 * @param notify 是否触发项目被选中事件。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */

	public void 选择堆叠层(int group, int index, int layer, boolean notify) {
		checkReady();
		ChartModel.Series data = model.group(group);
		if (!data.stacked) {
			throw new IllegalArgumentException("此组不是堆叠柱。");
		}
		ChartChecks.integer(layer, 0, data.columns.length - 1);
		select(group, index, layer, notify);
	}

	/**
	 * 设置组合图绘制层次，须包含五个类型且不重复；构建或刷新后生效。
	 *
	 * @param order 五个编号的排列：0 柱、1 气泡、2 折线、3 蜡烛、4 散点；先绘制的位于下方。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */

	public void 设置绘制顺序(int[] order) {
		checkReady();
		ChartChecks.order(order);
		combinedOrder = order.clone();
	}
}
