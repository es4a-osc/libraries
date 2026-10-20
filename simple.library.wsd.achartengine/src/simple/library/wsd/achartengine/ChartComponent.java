package simple.library.wsd.achartengine;

import android.content.Context;
import android.graphics.Color;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import org.achartengine.ChartFactory;
import org.achartengine.chart.BarChart;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import simple.runtime.android.MainActivity;
import simple.runtime.components.组件容器;
import simple.runtime.components.impl.android.视图组件;

/**
 * 共用图表数据、样式、视图替换与销毁处理，不向 Simple 导出独立类型。
 *
 * @author 树先生 xhwsd@qq.com
 */
abstract class ChartComponent extends 视图组件 {

	static final int PIE = 0;
	static final int BAR = 1;
	static final int LINE = 2;

	private final ChartData data = new ChartData();
	private boolean initialized;
	private boolean destroyed;
	private String title;
	private String xTitle;
	private String yTitle;
	private float textSize;
	private int itemColor;

	/**
	 * 创建图表组件。
	 *
	 * @param container 所属可视容器。
	 */
	ChartComponent(组件容器 container) {
		super(container);
	}

	@Override
	protected View createView() {
		return new FrameLayout(MainActivity.getContext());
	}

	/**
	 * 设置样式并清空数据；全部参数校验成功后才替换原状态。
	 *
	 * @param title 图表标题。
	 * @param xTitle 横轴标题。
	 * @param yTitle 纵轴标题。
	 * @param textSize 字体大小，单位 SP。
	 * @param backgroundColor 背景颜色。
	 * @param itemColor 柱条或折线颜色。
	 */
	final void initialize(String title, String xTitle, String yTitle, float textSize,
			int backgroundColor, int itemColor) {
		checkThread();
		if (title == null || xTitle == null || yTitle == null) {
			throw new IllegalArgumentException("图表标题不能为 null。");
		}
		float pixels = textSize * getView().getResources().getDisplayMetrics().scaledDensity;
		if (textSize <= 0 || Float.isNaN(pixels) || Float.isInfinite(pixels)) {
			throw new IllegalArgumentException("图表字体大小必须为有限正数。");
		}

		this.title = title;
		this.xTitle = xTitle;
		this.yTitle = yTitle;
		this.textSize = pixels;
		this.itemColor = itemColor;
		背景颜色(backgroundColor);
		data.clear();
		((FrameLayout) getView()).removeAllViews();
		initialized = true;
	}

	/**
	 * 添加一个有效项目。
	 *
	 * @param name 项目名称。
	 * @param value 数值文本。
	 * @param unit 显示单位。
	 * @param color 项目颜色。
	 * @param pie 是否为饼状图。
	 */
	final void add(String name, String value, String unit, int color, boolean pie) {
		checkInitialized();
		data.add(name, value, unit, color, pie);
	}

	/**
	 * 创建新图形后再替换旧视图，失败时保留现有画面。
	 *
	 * @param type 饼状图、柱状图或折线图。
	 */
	final void build(int type) {
		checkInitialized();
		FrameLayout layout = (FrameLayout) getView();
		View chart = null;
		if (data.size() > 0) {
			chart = type == PIE ? createPie(layout.getContext()) : createCartesian(layout.getContext(), type);
		}
		layout.removeAllViews();
		if (chart != null) {
			layout.addView(chart, new FrameLayout.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
		}
	}

	@Override
	public void 销毁() {
		if (destroyed) {
			return;
		}
		checkThread();
		destroyed = true;
		data.clear();
		((FrameLayout) getView()).removeAllViews();
		super.销毁();
	}

	/* 绘制与状态检查 */

	/**
	 * 生成饼图，归一化只改变绘制数值，不改变显示标签和存储值。
	 *
	 * @param context 图表视图上下文。
	 * @return 新饼图视图。
	 */
	private View createPie(Context context) {
		double scale = data.pieScale();
		CategorySeries series = new CategorySeries(title);
		DefaultRenderer renderer = new DefaultRenderer();
		configure(renderer);
		for (int index = 0; index < data.size(); index++) {
			ChartData.Entry entry = data.get(index);
			series.add(entry.name + " (" + entry.text + entry.unit + ")", entry.value / scale);
			SimpleSeriesRenderer item = new SimpleSeriesRenderer();
			item.setColor(entry.color);
			renderer.addSeriesRenderer(item);
		}
		return ChartFactory.getPieChartView(context, series, renderer);
	}

	/**
	 * 以项目顺序为横坐标生成柱状图或折线图。
	 *
	 * @param context 图表视图上下文。
	 * @param type 柱状图或折线图。
	 * @return 新图表视图。
	 */
	private View createCartesian(Context context, int type) {
		double[] range = data.range();
		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
		configure(renderer);
		renderer.setAxisTitleTextSize(textSize);
		renderer.setXTitle(xTitle);
		renderer.setYTitle(yTitle);
		renderer.setXAxisMin(0.5);
		renderer.setXAxisMax(data.size() + 0.5);
		renderer.setYAxisMin(range[0]);
		renderer.setYAxisMax(range[1]);
		renderer.setXLabels(0);
		renderer.setYLabels(5);
		renderer.setAxesColor(contrastColor());
		renderer.setXLabelsColor(contrastColor());
		renderer.setYLabelsColor(0, contrastColor());
		renderer.setMarginsColor(背景颜色());
		renderer.setPanEnabled(false, false);
		renderer.setZoomEnabled(false, false);
		renderer.setShowLegend(false);
		renderer.setBarSpacing(0.4);
		renderer.setMargins(new int[] { dp(32), dp(64), dp(48), dp(20) });

		XYSeries series = new XYSeries(title);
		for (int index = 0; index < data.size(); index++) {
			ChartData.Entry entry = data.get(index);
			series.add(index + 1, entry.value);
			renderer.addXTextLabel(index + 1, entry.name + (entry.unit.length() == 0 ? "" : " (" + entry.unit + ")"));
		}
		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		dataset.addSeries(series);
		XYSeriesRenderer item = new XYSeriesRenderer();
		item.setColor(itemColor);
		item.setDisplayChartValues(true);
		item.setChartValuesTextSize(textSize);
		item.setLineWidth(dp(2));
		item.setPointStyle(PointStyle.CIRCLE);
		item.setFillPoints(true);
		renderer.addSeriesRenderer(item);
		if (type == BAR) {
			return ChartFactory.getBarChartView(context, dataset, renderer, BarChart.Type.DEFAULT);
		}
		return ChartFactory.getLineChartView(context, dataset, renderer);
	}

	/**
	 * 配置三类图表共用的样式。
	 *
	 * @param renderer 图表渲染器。
	 */
	private void configure(DefaultRenderer renderer) {
		renderer.setChartTitle(title);
		renderer.setChartTitleTextSize(textSize);
		renderer.setLabelsTextSize(textSize);
		renderer.setLegendTextSize(textSize);
		renderer.setLabelsColor(contrastColor());
		renderer.setBackgroundColor(背景颜色());
		renderer.setApplyBackgroundColor(true);
		renderer.setPanEnabled(false);
		renderer.setZoomEnabled(false);
		renderer.setAntialiasing(true);
	}

	/**
	 * 根据不透明背景亮度选择文字颜色；透明背景按浅色处理。
	 *
	 * @return 黑色或白色。
	 */
	private int contrastColor() {
		int color = 背景颜色();
		int brightness = Color.red(color) * 299 + Color.green(color) * 587 + Color.blue(color) * 114;
		return Color.alpha(color) >= 128 && brightness < 128000 ? Color.WHITE : Color.BLACK;
	}

	/**
	 * 将图表内部间距从 DIP 转为像素。
	 *
	 * @param value DIP 数值。
	 * @return 像素数值。
	 */
	private int dp(int value) {
		return Math.round(value * getView().getResources().getDisplayMetrics().density);
	}

	/** 检查调用线程、组件寿命和初始化顺序。 */
	private void checkInitialized() {
		checkThread();
		if (!initialized) {
			throw new IllegalStateException("请先初始化图表。");
		}
	}

	/** 图表不跨线程读写，销毁后的引用不再接受操作。 */
	private void checkThread() {
		if (Looper.myLooper() != Looper.getMainLooper()) {
			throw new IllegalStateException("请在界面线程操作图表。");
		}
		if (destroyed) {
			throw new IllegalStateException("图表组件已销毁。");
		}
	}
}
