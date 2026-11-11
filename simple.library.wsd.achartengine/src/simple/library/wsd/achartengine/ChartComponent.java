package simple.library.wsd.achartengine;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.achartengine.GraphicalView;
import org.achartengine.chart.AbstractChart;
import org.achartengine.chart.XYChart;
import org.achartengine.model.SeriesSelection;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.DialRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.tools.PanListener;
import org.achartengine.tools.ZoomEvent;
import org.achartengine.tools.ZoomListener;
import simple.runtime.android.MainActivity;
import simple.runtime.components.impl.android.视图组件;
import simple.runtime.components.组件容器;
import simple.runtime.events.EventDispatcher;

/**
 * 共用生命周期、数据更新、视图替换和事件，不重复维护各图形的公共实现。
 *
 * @author 树先生 xhwsd@qq.com
 */
abstract class ChartComponent extends 视图组件 implements ACE图表 {
	final ChartModel model = new ChartModel();
	private ACE图表样式 chartStyle = new ACE图表样式();
	ACE仪表样式 dialStyle = new ACE仪表样式();
	private final ACE轴样式[] axisStyles = new ACE轴样式[3];
	private final double[][] ranges = new double[3][];
	private final List<TreeMap<Double, String>> labels = new ArrayList<TreeMap<Double, String>>();
	private String title = "";
	private String xTitle = "";
	private final String[] yTitles = { "", "" };
	private boolean styleChanged;
	private boolean initialized;
	private boolean destroyed;
	boolean dirty = true;
	private boolean showLegend = true;
	private boolean showValues = true;
	int barMode;
	float cubic = 0.33f;
	String datePattern = "";
	boolean smartTime = true;
	private double[] panLimits = new double[0];
	private double[] zoomLimits = new double[0];
	int[] ringColors = { 0xff3086c8, 0xff269b68, 0xffe9a13b, 0xff9b59b6 };
	private GraphicalView view;
	private AbstractChart nativeChart;
	private DefaultRenderer renderer;
	private TextView selection;
	private int[] selected = new int[0];
	private boolean drawn;
	private HitMap hitMap;
	private float touchX;
	private float touchY;
	private final boolean[] rangeChanged = new boolean[3];

	/**
	 * 创建共享图表状态。
	 *
	 * @param container 所属组件容器。
	 */
	ChartComponent(组件容器 container) {
		super(container);
		for (int i = 0; i < 3; i++) {
			labels.add(new TreeMap<Double, String>());
		}
	}

	/**
	 * 取得组件固定的图形类型。
	 *
	 * @return 图形类型编号。
	 */
	protected abstract int chartKind();

	@Override
	protected View createView() {
		FrameLayout layout = new FrameLayout(MainActivity.getContext());
		selection = new TextView(layout.getContext());
		selection.setTextColor(Color.WHITE);
		selection.setBackgroundColor(0xdd333333);
		selection.setPadding(8, 4, 8, 4);
		selection.setVisibility(View.GONE);
		return layout;
	}

	/**
	 * 初始化外观和模型，保留可视组件的通用属性。
	 *
	 * @param title    图表标题。
	 * @param textSize 文字大小，单位 SP。
	 * @param color    背景颜色。
	 */
	private void initialize(String title, float textSize, int color) {
		Checks.text(title);
		Checks.range(textSize, 6, 100);
		this.title = title;
		chartStyle = new ACE图表样式();
		dialStyle = new ACE仪表样式();
		chartStyle.textSize = textSize;
		chartStyle.titleSize = textSize;
		chartStyle.legendSize = textSize;
		chartStyle.axisTitleSize = textSize;
		int brightness = Color.red(color) * 299 + Color.green(color) * 587 + Color.blue(color) * 114;
		chartStyle.textColor = Color.alpha(color) >= 128 && brightness < 128000 ? Color.WHITE : Color.BLACK;
		chartStyle.axesColor = chartStyle.textColor;
		背景颜色(color);
		model.clear();
		Arrays.fill(axisStyles, null);
		Arrays.fill(ranges, null);
		Arrays.fill(rangeChanged, false);
		styleChanged = false;
		for (Map<Double, String> axis : labels) {
			axis.clear();
		}
		xTitle = "";
		yTitles[0] = "";
		yTitles[1] = "";
		barMode = 0;
		cubic = 0.33f;
		datePattern = "";
		smartTime = true;
		panLimits = new double[0];
		zoomLimits = new double[0];
		ringColors = new int[] { 0xff3086c8, 0xff269b68, 0xffe9a13b, 0xff9b59b6 };
		initialized = true;
		dirty = true;
		clearView();
	}

	/**
	 * 添加一组已复制的分类或数值坐标数据。
	 *
	 * @param kind    图形类型。
	 * @param name    数据组名称。
	 * @param xs      横坐标，null 使用分类索引。
	 * @param columns 数值列。
	 * @param color   数据组颜色。
	 * @param axis    数值轴编号。
	 */
	final void addSeries(int kind, String name, double[] xs, double[][] columns, int color, int axis) {
		checkReady();
		model.add(kind, name, xs, columns, color, axis);
		dirty = true;
	}

	/**
	 * 检查四项坐标边界。
	 *
	 * @param bounds 横轴和纵轴的最小、最大值。
	 */
	private void checkBounds(double[] bounds) {
		Checks.required(bounds);
		if (bounds.length != 4) {
			throw new IllegalArgumentException("范围必须包含四项。");
		}
		Checks.bounds(bounds[0], bounds[1]);
		Checks.bounds(bounds[2], bounds[3]);
	}

	/**
	 * 创建新图形后替换旧视图，构建失败保留旧画面。
	 *
	 * @param keepRange 是否保留当前可见范围。
	 */
	private void render(boolean keepRange) {
		checkReady();
		if (model.series.isEmpty()) {
			clearView();
			dirty = false;
			return;
		}
		double[][] previous = keepRange && renderer instanceof XYMultipleSeriesRenderer
				? new double[][] { currentRange(0), currentRange(1) }
				: null;
		final float initialScale = chartStyle.scale;
		DefaultRenderer next = isRound() ? (chartKind() == ChartModel.DIAL ? new DialRenderer() {
			@Override
			public float getOriginalScale() {
				return initialScale;
			}
		} : new DefaultRenderer() {
			@Override
			public float getOriginalScale() {
				return initialScale;
			}
		}) : new XYMultipleSeriesRenderer(2);
		ChartStyles.common(next, chartStyle, title, 背景颜色(), density(),
				getView().getResources().getDisplayMetrics().scaledDensity, showLegend);
		final HitMap nextHits = new HitMap();
		float previousScale = renderer == null ? initialScale : renderer.getScale();
		org.achartengine.chart.RoundChart previousRound = nativeChart instanceof org.achartengine.chart.RoundChart
				? (org.achartengine.chart.RoundChart) nativeChart
				: null;
		boolean keepRound = keepRange && !styleChanged && drawn && previousRound != null;
		AbstractChart chart;
		if (next instanceof XYMultipleSeriesRenderer) {
			XYMultipleSeriesRenderer xy = (XYMultipleSeriesRenderer) next;
			xy.setXTitle(xTitle);
			xy.setYTitle(yTitles[0], 0);
			xy.setYTitle(yTitles[1], 1);
			xy.setXLabels(model.numeric ? 5 : 0);
			xy.setYLabels(5);
			for (int axis = 0; axis < 2; axis++) {
				double[] initial = ChartBuilder.range(model, axis);
				if (ranges[0] != null) {
					initial[0] = ranges[0][0];
					initial[1] = ranges[0][1];
				}
				if (ranges[axis + 1] != null) {
					initial[2] = ranges[axis + 1][0];
					initial[3] = ranges[axis + 1][1];
				}
				double[] range = previous == null ? initial.clone() : previous[axis];
				if (rangeChanged[0]) {
					range[0] = initial[0];
					range[1] = initial[1];
				}
				if (rangeChanged[axis + 1]) {
					range[2] = initial[2];
					range[3] = initial[3];
				}
				checkBounds(range);
				xy.setRange(range, axis);
				xy.setInitialRange(initial, axis);
			}

			xy.setPanLimits(panLimits.length == 0 ? null : panLimits.clone());
			xy.setZoomLimits(zoomLimits.length == 0 ? null : zoomLimits.clone());
			for (int i = 0; i < model.labels.length; i++) {
				xy.addXTextLabel(i + 1, model.labels[i]);
			}
			for (int axis = 0; axis < 3; axis++) {
				if (axisStyles[axis] != null) {
					ChartStyles.axis(xy, axis, axisStyles[axis]);
				}
				for (Map.Entry<Double, String> label : labels.get(axis).entrySet()) {
					if (axis == 0) {
						xy.addXTextLabel(label.getKey(), label.getValue());
					} else {
						xy.addYTextLabel(label.getKey(), label.getValue(), axis - 1);
					}
				}
			}
			chart = ChartBuilder.cartesian(model, chartKind(), xy, chartStyle, density(),
					getView().getResources().getDisplayMetrics().scaledDensity, showValues, cubic, barMode, datePattern,
					smartTime, nextHits);
		} else {
			chart = ChartBuilder.round(model, chartKind(), next, ringColors, dialStyle);
			org.achartengine.chart.RoundChart round = (org.achartengine.chart.RoundChart) chart;
			if (keepRound) {
				next.setScale(previousScale);
				round.setCenterX(previousRound.getCenterX());
				round.setCenterY(previousRound.getCenterY());
			} else {
				if (chartStyle.centerX >= 0) {
					round.setCenterX(Math.round(chartStyle.centerX * density()));
				}
				if (chartStyle.centerY >= 0) {
					round.setCenterY(Math.round(chartStyle.centerY * density()));
				}
			}
		}
		final GraphicalView created = new GraphicalView(getView().getContext(), chart) {
			@Override
			public boolean onTouchEvent(android.view.MotionEvent event) {
				if (event.getActionMasked() == android.view.MotionEvent.ACTION_DOWN) {
					touchX = event.getX();
					touchY = event.getY();
				}
				if (getParent() != null) {
					getParent().requestDisallowInterceptTouchEvent(
							event.getActionMasked() != android.view.MotionEvent.ACTION_UP
									&& event.getActionMasked() != android.view.MotionEvent.ACTION_CANCEL);
				}
				return super.onTouchEvent(event);
			}

			@Override
			protected void onDraw(Canvas canvas) {
				nextHits.clear();
				super.onDraw(canvas);
				if (view == this) {
					drawn = true;
				}
			}
		};
		created.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View clicked) {
				if (destroyed || dirty || view != created || !chartStyle.click) {
					return;
				}
				SeriesSelection hit = nativeChart instanceof XYChart
						? hitMap.find(touchX, touchY, created.getWidth(), created.getHeight(),
								chartStyle.orientation == 1)
						: created.getCurrentSeriesAndPoint();
				if (hit == null) {
					取消选择(true);
					return;
				}
				int group = hit.getSeriesIndex();
				int item = hit.getPointIndex();
				if (chartKind() == ChartModel.PIE) {
					group = 0;
				}
				if (group < 0 || group >= model.series.size()) {
					return;
				}
				if (nativeChart instanceof XYChart) {
					ChartModel.Series data = model.group(group);
					item = Arrays.binarySearch(data.xs, hit.getXValue());
					if (item < 0 && data.kind == ChartModel.RANGE) {
						item = -item - 2;
					}
					if (item < 0 || item >= data.xs.length || data.missing[item]) {
						return;
					}
				}
				select(group, item, true);
			}
		});
		created.addPanListener(new PanListener() {
			@Override
			public void panApplied() {
				if (!destroyed && view == created) {
					图表被拖动();
				}
			}
		});
		created.addZoomListener(new ZoomListener() {
			@Override
			public void zoomApplied(ZoomEvent event) {
				if (!destroyed && view == created) {
					图表被缩放(event.isZoomIn(), event.getZoomRate());
				}
			}

			@Override
			public void zoomReset() {
				if (!destroyed && view == created) {
					if (isRound()) {
						render(false);
					}
					视图被重置();
				}
			}
		}, true, true);
		clearView();
		nativeChart = chart;
		hitMap = nextHits;
		Arrays.fill(rangeChanged, false);
		styleChanged = false;
		renderer = next;
		view = created;
		dirty = false;
		drawn = false;
		FrameLayout layout = (FrameLayout) getView();
		layout.addView(created,
				new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
		FrameLayout.LayoutParams hint = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.TOP | Gravity.RIGHT);
		layout.addView(selection, hint);
	}

	/**
	 * 清除视图与选择引用，不改变模型。
	 */
	private void clearView() {
		if (view != null) {
			view.setOnClickListener(null);
			view.clearAnimation();
		}
		((FrameLayout) getView()).removeAllViews();
		view = null;
		renderer = null;
		nativeChart = null;
		hitMap = null;
		drawn = false;
		selected = new int[0];
		selection.setVisibility(View.GONE);
	}

	/**
	 * 按模型编号选择项目，并使用原始数值派发事件。
	 *
	 * @param group  数据组编号。
	 * @param index  项目编号。
	 * @param notify 是否派发事件。
	 */
	private void select(int group, int index, boolean notify) {
		ChartModel.Series data = model.group(group);
		Checks.index(index, 0, data.xs.length - 1);
		if (data.missing[index]) {
			throw new IllegalArgumentException("缺失项目不能选择。");
		}
		selected = new int[] { group, index };
		double value = data.columns[data.kind == ChartModel.RANGE ? 1 : 0][index];
		selection.setText(data.name + " [" + index + "] " + value);
		selection.setTextSize(chartStyle.textSize);
		selection.setVisibility(chartStyle.selection ? View.VISIBLE : View.GONE);
		if (notify) {
			项目被选中(group, index, data.xs[index], value);
		}
	}

	/**
	 * 删除圆图单项并保持其它项目的颜色和类型。
	 *
	 * @param index 项目编号。
	 */
	final void removeRound(int index) {
		ChartModel.Series data = model.group(0);
		Checks.index(index, 0, data.xs.length - 1);
		ChartModel temporary = new ChartModel();
		for (int i = 0; i < data.xs.length; i++) {
			if (i != index) {
				temporary.addRound(data.kind, data.names[i], data.columns[0][i], data.colors[i], data.types[i]);
			}
		}
		if (temporary.series.isEmpty()) {
			model.clear();
		} else {
			temporary.group(0).style = data.style;
			model.series.set(0, temporary.group(0));
		}
		dirty = true;
	}

	/**
	 * 检查当前是否为圆图。
	 *
	 * @return 是否是饼、圆环或仪表盘。
	 */
	private boolean isRound() {
		return chartKind() == ChartModel.PIE || chartKind() == ChartModel.DONUT || chartKind() == ChartModel.DIAL;
	}

	/**
	 * 取得已构建的坐标绘制器。
	 *
	 * @return 数值图绘制器。
	 */
	private XYMultipleSeriesRenderer xyRenderer() {
		if (!(renderer instanceof XYMultipleSeriesRenderer)) {
			throw new IllegalArgumentException("此操作仅适用于坐标图。");
		}
		return (XYMultipleSeriesRenderer) renderer;
	}

	/**
	 * 取得当前数值范围副本。
	 *
	 * @param axis 数值轴编号。
	 * @return 四项范围。
	 */
	private double[] currentRange(int axis) {
		XYMultipleSeriesRenderer r = xyRenderer();
		return new double[] { r.getXAxisMin(axis), r.getXAxisMax(axis), r.getYAxisMin(axis), r.getYAxisMax(axis) };
	}

	/**
	 * 取得 DIP 转换密度。
	 *
	 * @return 屏幕密度。
	 */
	private float density() {
		return getView().getResources().getDisplayMetrics().density;
	}

	/**
	 * 检查模型与已经构建的原生数据一致。
	 */
	private void requireBuilt() {
		if (dirty || view == null) {
			throw new IllegalArgumentException("请先构建或刷新图表。");
		}
	}

	/**
	 * 检查依赖像素布局的导出和坐标换算是否就绪。
	 */
	private void requireDrawn() {
		requireBuilt();
		if (!drawn || view.getWidth() <= 0 || view.getHeight() <= 0) {
			throw new IllegalArgumentException("请等待图表完成绘制。");
		}
	}

	/**
	 * 检查生命周期和界面线程。
	 */
	private void checkAlive() {
		if (destroyed || Looper.myLooper() != Looper.getMainLooper()) {
			throw new IllegalArgumentException("请在界面线程操作未销毁的图表。");
		}
	}

	/**
	 * 检查初始化顺序。
	 */
	final void checkReady() {
		checkAlive();
		if (!initialized) {
			throw new IllegalArgumentException("请先初始化图表。");
		}
	}

	@Override
	public void 销毁() {
		if (destroyed) {
			return;
		}
		checkAlive();
		clearView();
		model.clear();
		destroyed = true;
		super.销毁();
	}

	/**
	 * 初始化基础样式，清空数据、轴范围和配置；后续操作须先初始化。
	 *
	 * @param title    图表标题，允许空文本。
	 * @param textSize 6 至 100 SP。
	 * @param color    ARGB 颜色。
	 */
	public void 初始化图表(String title, float textSize, int color) {
		checkAlive();
		initialize(title, textSize, color);
	}

	/**
	 * 清空数据与图形，保留样式、轴标签和范围。
	 */
	public void 清空数据() {
		checkReady();
		model.clear();
		dirty = true;
		clearView();
	}

	/**
	 * 重建图表并恢复设置的轴范围；无数据时清空图形。
	 */
	public void 构建图表() {
		checkReady();
		render(false);
	}

	/**
	 * 重建图表，保留当前缩放和平移范围。
	 */
	public void 刷新图表() {
		checkReady();
		render(true);
	}

	/**
	 * 保存样式副本，构建或刷新后生效。
	 *
	 * @param style 要复制的配置，不能为 null。
	 */
	public void 设置图表样式(ACE图表样式 style) {
		checkReady();
		Checks.required(style);
		chartStyle = style.复制();
		styleChanged = true;
	}

	/**
	 * 保存数据组样式副本，构建或刷新后生效。
	 *
	 * @param group 从 0 开始的数据组编号；饼图和仪表盘固定为 0。
	 * @param style 要复制的数据组配置。
	 */
	public void 设置数据组样式(int group, ACE数据组样式 style) {
		checkReady();
		Checks.required(style);
		if (style.gradient) {
			Checks.bounds(style.gradientStart, style.gradientStop);
		}
		model.group(group).style = style.复制();
	}

	/**
	 * 返回独立的数据组样式副本，未设置时返回默认配置。
	 *
	 * @param group 从 0 开始的数据组编号；饼图和仪表盘固定为 0。
	 * @return 返回独立的数据组样式副本，未设置时返回默认配置。
	 */
	public ACE数据组样式 取数据组样式(int group) {
		checkReady();
		ACE数据组样式 style = model.group(group).style;
		return style == null ? new ACE数据组样式() : style.复制();
	}

	/**
	 * 返回数据组名称；饼图和仪表盘返回空文本。
	 *
	 * @param group 从 0 开始的数据组编号；饼图和仪表盘固定为 0。
	 * @return 返回数据组名称；饼图和仪表盘返回空文本。
	 */
	public String 取数据组名称(int group) {
		checkReady();
		return model.group(group).name;
	}

	/**
	 * 修改数据组名称，构建或刷新后生效。
	 *
	 * @param group 从 0 开始的数据组编号；饼图和仪表盘固定为 0。
	 * @param name  非空引用的名称，允许空文本。
	 */
	public void 设置数据组名称(int group, String name) {
		checkReady();
		Checks.text(name);
		model.group(group).name = name;
		dirty = true;
	}

	/**
	 * 返回名称数组副本；数值坐标返回空数组。
	 *
	 * @param group 从 0 开始的数据组编号；饼图和仪表盘固定为 0。
	 * @return 返回名称数组副本；数值坐标返回空数组。
	 */
	public String[] 取项目名称(int group) {
		checkReady();
		ChartModel.Series data = model.group(group);
		return data.names.length > 0 ? data.names.clone() : model.labels.clone();
	}

	/**
	 * 返回横坐标副本，分类和圆图为从 1 开始的项目索引。
	 *
	 * @param group 从 0 开始的数据组编号；饼图和仪表盘固定为 0。
	 * @return 返回横坐标副本，分类和圆图为从 1 开始的项目索引。
	 */
	public double[] 取横坐标(int group) {
		checkReady();
		return model.group(group).xs.clone();
	}

	/**
	 * 返回指定列的独立数值数组。
	 *
	 * @param group  从 0 开始的数据组编号；饼图和仪表盘固定为 0。
	 * @param column 普通图为 0；气泡 0 纵坐标、1 大小；区间柱 0 下限、1 上限。
	 * @return 返回指定列的独立数值数组。
	 */
	public double[] 取数据组数值(int group, int column) {
		checkReady();
		ChartModel.Series data = model.group(group);
		Checks.index(column, 0, data.columns.length - 1);
		return data.columns[column].clone();
	}

	/**
	 * 删除指定数据组；饼图或仪表盘删除全部项目，构建或刷新后生效。
	 *
	 * @param group 从 0 开始的数据组编号；饼图和仪表盘固定为 0。
	 */
	public void 删除数据组(int group) {
		checkReady();
		model.group(group);
		model.series.remove(group);
		if (model.series.isEmpty()) {
			model.numeric = false;
		}
		dirty = true;
	}

	/**
	 * 主动选择已构建项目，显示文字提示；上游没有程序化高亮效果。
	 *
	 * @param group  从 0 开始的数据组编号；饼图和仪表盘固定为 0。
	 * @param index  从 0 开始的项目编号。
	 * @param notify 是否派发项目被选中事件。
	 */
	public void 选择项目(int group, int index, boolean notify) {
		checkReady();
		requireBuilt();
		select(group, index, notify);
	}

	/**
	 * 取消当前选择和文字提示。
	 *
	 * @param notify 是否派发选择被取消事件。
	 */
	public void 取消选择(boolean notify) {
		checkReady();
		selected = new int[0];
		selection.setVisibility(View.GONE);
		if (notify) {
			选择被取消();
		}
	}

	/**
	 * 返回数据组和项目编号的副本；未选择或模型已修改时返回空数组。
	 *
	 * @return 返回数据组和项目编号的副本；未选择或模型已修改时返回空数组。
	 */
	public int[] 取选中项目() {
		checkReady();
		return dirty ? new int[0] : selected.clone();
	}

	/**
	 * 导出已完成布局的图表截图，返回压缩字节，无需存储权限。
	 *
	 * @param format  0 PNG，1 JPEG。
	 * @param quality 0 至 100；PNG 不使用质量参数。
	 * @return 导出已完成布局的图表截图，返回压缩字节，无需存储权限。
	 */
	public byte[] 导出图片(int format, int quality) {
		checkReady();
		requireDrawn();
		Checks.index(format, 0, 1);
		Checks.index(quality, 0, 100);
		Bitmap bitmap = Bitmap.createBitmap(getView().getWidth(), getView().getHeight(), Bitmap.Config.ARGB_8888);
		try {
			Canvas canvas = new Canvas(bitmap);
			canvas.drawColor(Color.WHITE);
			getView().draw(canvas);
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
	 * 调整已构建图表的显示比例；复位恢复构建时的初始范围。
	 */
	public void 放大() {
		checkReady();
		requireBuilt();
		view.zoomIn();
	}

	/**
	 * 调整已构建图表的显示比例；复位恢复构建时的初始范围。
	 */
	public void 缩小() {
		checkReady();
		requireBuilt();
		view.zoomOut();
	}

	/**
	 * 调整已构建图表的显示比例；复位恢复构建时的初始范围。
	 */
	public void 重置视图() {
		checkReady();
		requireBuilt();
		if (isRound()) {
			render(false);
			视图被重置();
		} else {
			view.zoomReset();
		}
	}

	/**
	 * 设置分类索引名称，数值坐标数据不需要调用。
	 *
	 * @param names 分类名称，添加数据前设置；更换名称前先清空。
	 */
	public void 设置项目名称(String[] names) {
		checkReady();
		model.names(names);
		dirty = true;
	}

	/**
	 * 设置横轴和左右轴标题，构建或刷新后生效。
	 *
	 * @param x     横轴标题。
	 * @param left  左数值轴标题。
	 * @param right 右数值轴标题。
	 */
	public void 设置轴标题(String x, String left, String right) {
		checkReady();
		Checks.text(x);
		Checks.text(left);
		Checks.text(right);
		xTitle = x;
		yTitles[0] = left;
		yTitles[1] = right;
	}

	/**
	 * 保存轴样式副本，构建或刷新后生效；数值轴刻度数量和角度为上游共享设置。
	 *
	 * @param axis  0 横轴，1 左数值轴，2 右数值轴。
	 * @param style 要复制的轴配置。
	 */
	public void 设置轴样式(int axis, ACE轴样式 style) {
		checkReady();
		Checks.index(axis, 0, 2);
		Checks.required(style);
		axisStyles[axis] = style.复制();
	}

	/**
	 * 固定指定坐标轴范围，构建或刷新后生效。
	 *
	 * @param axis 0 横轴，1 左数值轴，2 右数值轴。
	 * @param min  有限数值，必须小于最大值。
	 * @param max  有限数值。
	 */
	public void 设置轴范围(int axis, double min, double max) {
		checkReady();
		Checks.index(axis, 0, 2);
		Checks.bounds(min, max);
		ranges[axis] = new double[] { min, max };
		rangeChanged[axis] = true;
	}

	/**
	 * 恢复指定轴按数据自动计算范围，下一次构建图表后生效。
	 *
	 * @param axis 0 横轴，1 左数值轴，2 右数值轴。
	 */
	public void 恢复自动范围(int axis) {
		checkReady();
		Checks.index(axis, 0, 2);
		ranges[axis] = null;
		rangeChanged[axis] = true;
	}

	/**
	 * 设置指定刻度位置的文字标签，相同坐标替换，构建或刷新后生效。
	 *
	 * @param axis  0 横轴，1 左数值轴，2 右数值轴。
	 * @param value 有限轴坐标。
	 * @param text  非空引用的文字。
	 */
	public void 添加轴文字(int axis, double value, String text) {
		checkReady();
		Checks.index(axis, 0, 2);
		Checks.finite(value);
		Checks.text(text);
		labels.get(axis).put(value, text);
	}

	/**
	 * 清空自定义轴文字，分类名称保留。
	 *
	 * @param axis 0 横轴，1 左数值轴，2 右数值轴。
	 */
	public void 清空轴文字(int axis) {
		checkReady();
		Checks.index(axis, 0, 2);
		labels.get(axis).clear();
	}

	/**
	 * 替换整组数据并保留样式，分类图须保持项目数和原索引，刷新或构建后生效。
	 *
	 * @param group  从 0 开始的数据组编号；饼图和仪表盘固定为 0。
	 * @param xs     严格递增的有限数值数组，不允许重复。
	 * @param values 按整列拼接；气泡先纵坐标后大小，区间先下限后上限。
	 */
	public void 更新数据组(int group, double[] xs, double[] values) {
		checkReady();
		model.replace(group, xs, values);
		dirty = true;
	}

	/**
	 * 向数值横坐标数据组追加一项，刷新或构建后生效。
	 *
	 * @param group  从 0 开始的数据组编号；饼图和仪表盘固定为 0。
	 * @param x      必须大于本组最后横坐标。
	 * @param values 此项目各列数值，顺序与更新数据组一致。
	 */
	public void 追加项目(int group, double x, double[] values) {
		checkReady();
		model.append(group, x, values);
		dirty = true;
	}

	/**
	 * 删除数值横坐标数据组中的一项，至少保留一项，刷新或构建后生效。
	 *
	 * @param group 从 0 开始的数据组编号；饼图和仪表盘固定为 0。
	 * @param index 从 0 开始的项目编号。
	 */
	public void 删除项目(int group, int index) {
		checkReady();
		model.remove(group, index);
		dirty = true;
	}

	/**
	 * 直接设置已构建图表的可见范围；横轴范围对两条数值轴同步。
	 *
	 * @param axis   0 左轴，1 右轴。
	 * @param bounds 横轴最小、横轴最大、纵轴最小、纵轴最大，必须分别递增。
	 */
	public void 设置可见范围(int axis, double[] bounds) {
		checkReady();
		requireBuilt();
		Checks.index(axis, 0, 1);
		checkBounds(bounds);
		XYMultipleSeriesRenderer r = xyRenderer();
		r.setRange(bounds.clone(), axis);
		for (int scale = 0; scale < 2; scale++) {
			r.setXAxisMin(bounds[0], scale);
			r.setXAxisMax(bounds[1], scale);
		}
		view.invalidate();
	}

	/**
	 * 返回横轴最小、最大和指定纵轴最小、最大。
	 *
	 * @param axis 0 左轴，1 右轴。
	 * @return 返回横轴最小、最大和指定纵轴最小、最大。
	 */
	public double[] 取可见范围(int axis) {
		checkReady();
		requireBuilt();
		Checks.index(axis, 0, 1);
		return currentRange(axis);
	}

	/**
	 * 保存拖动边界限制；缩放跨度为正数数据跨度，构建或刷新后生效。
	 *
	 * @param bounds 四个数值：横轴最小、最大和纵轴最小、最大；空数组取消限制。
	 */
	public void 设置拖动边界(double[] bounds) {
		checkReady();
		Checks.required(bounds);
		if (bounds.length != 0) {
			checkBounds(bounds);
		}
		panLimits = bounds.clone();
	}

	/**
	 * 保存缩放跨度限制；缩放跨度为正数数据跨度，构建或刷新后生效。
	 *
	 * @param bounds 四个数值：横轴最小、最大和纵轴最小、最大；空数组取消限制。
	 */
	public void 设置缩放跨度(double[] bounds) {
		checkReady();
		Checks.required(bounds);
		if (bounds.length != 0) {
			checkBounds(bounds);
			if (bounds[0] <= 0 || bounds[2] <= 0) {
				throw new IllegalArgumentException("缩放跨度必须为正数。");
			}
		}
		zoomLimits = bounds.clone();
	}

	/**
	 * 转换已完成绘制的纵向图表坐标，返回横纵坐标；像素坐标单位 DIP，横向图形不支持此换算。
	 *
	 * @param x    数据坐标或相对图表左上角的 DIP 坐标。
	 * @param y    数据坐标或相对图表左上角的 DIP 坐标。
	 * @param axis 0 左轴，1 右轴。
	 * @return 转换已完成绘制的纵向图表坐标，返回横纵坐标；像素坐标单位 DIP，横向图形不支持此换算。
	 */
	public double[] 数据坐标转像素(double x, double y, int axis) {
		checkReady();
		if (chartStyle.orientation != 0) {
			throw new IllegalArgumentException("坐标换算仅支持纵向图形。");
		}
		requireDrawn();
		Checks.index(axis, 0, 1);
		Checks.finite(x);
		Checks.finite(y);
		XYChart chart = (XYChart) nativeChart;
		double[] p = chart.toScreenPoint(new double[] { x, y }, axis);
		p[0] /= density();
		p[1] /= density();
		return p;
	}

	/**
	 * 转换已完成绘制的纵向图表坐标，返回横纵坐标；像素坐标单位 DIP，横向图形不支持此换算。
	 *
	 * @param x    数据坐标或相对图表左上角的 DIP 坐标。
	 * @param y    数据坐标或相对图表左上角的 DIP 坐标。
	 * @param axis 0 左轴，1 右轴。
	 * @return 转换已完成绘制的纵向图表坐标，返回横纵坐标；像素坐标单位 DIP，横向图形不支持此换算。
	 */
	public double[] 像素转数据坐标(double x, double y, int axis) {
		checkReady();
		if (chartStyle.orientation != 0) {
			throw new IllegalArgumentException("坐标换算仅支持纵向图形。");
		}
		requireDrawn();
		Checks.index(axis, 0, 1);
		Checks.finite(x);
		Checks.finite(y);
		XYChart chart = (XYChart) nativeChart;
		return chart.toRealPoint((float) (x * density()), (float) (y * density()), axis);
	}

	/**
	 * 取得数据组数量。
	 *
	 * @return 数据组数量
	 */
	@Override
	public int 数据组数量() {
		checkAlive();
		return model.series.size();
	}

	/**
	 * 取得项目数量。
	 *
	 * @return 项目数量
	 */
	@Override
	public int 项目数量() {
		checkAlive();
		int count = 0;
		for (ChartModel.Series data : model.series) {
			count = Math.max(count, data.xs.length);
		}
		return count;
	}

	/**
	 * 是否显示图例，同时受图表样式控制。
	 *
	 * @return 是否显示图例，同时受图表样式控制。
	 */
	@Override
	public boolean 显示图例() {
		checkAlive();
		return showLegend;
	}

	/**
	 * 是否显示图例，同时受图表样式控制。
	 *
	 * @param value 是否显示图例，同时受图表样式控制。
	 */
	@Override
	public void 显示图例(boolean value) {
		checkAlive();
		showLegend = value;
		if (view != null) {
			render(true);
		}
	}

	/**
	 * 是否显示坐标图数值，同时受数据组样式控制。
	 *
	 * @return 是否显示坐标图数值，同时受数据组样式控制。
	 */
	@Override
	public boolean 显示数值() {
		checkAlive();
		return showValues;
	}

	/**
	 * 是否显示坐标图数值，同时受数据组样式控制。
	 *
	 * @param value 是否显示坐标图数值，同时受数据组样式控制。
	 */
	@Override
	public void 显示数值(boolean value) {
		checkAlive();
		showValues = value;
		if (view != null) {
			render(true);
		}
	}

	/**
	 * 项目被主动选择或原生点选；编号从 0 开始，圆环和仪表盘仅支持主动选择。
	 *
	 * @param group 从 0 开始的数据组编号；饼图和仪表盘固定为 0。
	 * @param index 从 0 开始的项目编号。
	 * @param x     原模型横坐标。
	 * @param y     普通图为纵坐标，区间为上限，饼图及仪表盘为原始值。
	 */
	@Override
	public void 项目被选中(int group, int index, double x, double y) {
		EventDispatcher.dispatchEvent(this, "项目被选中", group, index, x, y);
	}

	/**
	 * 选择被取消或点击空白位置。
	 */
	@Override
	public void 选择被取消() {
		EventDispatcher.dispatchEvent(this, "选择被取消");
	}

	/**
	 * 手势平移已改变可见范围。
	 */
	@Override
	public void 图表被拖动() {
		EventDispatcher.dispatchEvent(this, "图表被拖动");
	}

	/**
	 * 原生缩放手势或按钮已改变图表范围。
	 *
	 * @param zoomIn True 放大，False 缩小。
	 * @param rate   本次缩放倍率。
	 */
	@Override
	public void 图表被缩放(boolean zoomIn, float rate) {
		EventDispatcher.dispatchEvent(this, "图表被缩放", zoomIn, rate);
	}

	/**
	 * 原生复位已恢复图表初始范围。
	 */
	@Override
	public void 视图被重置() {
		EventDispatcher.dispatchEvent(this, "视图被重置");
	}

	/**
	 * 将指定项目标记为缺失，不改变原数值；构建或刷新后生效，缺失点不能主动选择。
	 *
	 * @param group   从零开始的数据组编号。
	 * @param index   从零开始的项目编号。
	 * @param missing True 留空，False 恢复原始数值。
	 */

	public void 设置项目缺失(int group, int index, boolean missing) {
		checkReady();
		ChartModel.Series data = model.group(group);
		Checks.index(index, 0, data.xs.length - 1);
		data.missing[index] = missing;
		dirty = true;
	}

	/**
	 * 查询指定项目的缺失标记。
	 *
	 * @param group 从零开始的数据组编号。
	 * @param index 从零开始的项目编号。
	 * @return 查询指定项目的缺失标记。
	 */

	public boolean 项目是否缺失(int group, int index) {
		checkReady();
		ChartModel.Series data = model.group(group);
		Checks.index(index, 0, data.xs.length - 1);
		return data.missing[index];
	}
}
