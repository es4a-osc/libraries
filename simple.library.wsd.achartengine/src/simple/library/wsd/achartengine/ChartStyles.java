package simple.library.wsd.achartengine;

import android.graphics.Paint;
import org.achartengine.chart.PointStyle;
import org.achartengine.renderer.BasicStroke;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

/**
 * 统一转换 SP、DIP 和上游绘制器配置。
 *
 * @author 树先生 xhwsd@qq.com
 */
final class ChartStyles {
	/**
	 * 创建不可实例化的样式工具。
	 */
	private ChartStyles() {
	}

	/**
	 * 应用所有图形共用的样式。
	 *
	 * @param renderer      目标绘制器。
	 * @param style         配置副本。
	 * @param title         图表标题。
	 * @param background    背景颜色。
	 * @param density       DIP 密度。
	 * @param scaledDensity SP 密度。
	 * @param legend        图表级图例开关。
	 */
	static void common(DefaultRenderer renderer, ACE图表样式 style, String title, int background, float density,
			float scaledDensity, boolean legend) {
		renderer.setChartTitle(title);
		renderer.setChartTitleTextSize(style.titleSize * scaledDensity);
		renderer.setLabelsTextSize(style.textSize * scaledDensity);
		renderer.setLegendTextSize(style.legendSize * scaledDensity);
		renderer.setLabelsColor(style.textColor);
		renderer.setAxesColor(style.axesColor);
		renderer.setBackgroundColor(background);
		renderer.setApplyBackgroundColor(true);
		renderer.setShowLabels(style.labels);
		renderer.setShowAxes(style.axes);
		renderer.setShowLegend(legend && style.legend);
		renderer.setShowGridX(style.gridX);
		renderer.setShowGridY(style.gridY);
		renderer.setShowCustomTextGrid(style.customGrid);
		renderer.setFitLegend(style.fitLegend);
		renderer.setLegendHeight(Math.round(style.legendHeight * density));
		renderer.setAntialiasing(style.antialias);
		renderer.setPanEnabled(style.panX || style.panY);
		renderer.setZoomEnabled(style.zoomX || style.zoomY);
		renderer.setExternalZoomEnabled(true);
		renderer.setZoomButtonsVisible(style.zoomButtons);
		renderer.setZoomRate(style.zoomRate);
		renderer.setClickEnabled(style.click);
		renderer.setSelectableBuffer(Math.round(style.buffer * density));
		renderer.setScale(style.scale);
		renderer.setStartAngle(style.startAngle);
		renderer.setTextTypeface(style.font, style.fontStyle);
		renderer.setInScroll(true);
		renderer.setMargins(new int[] { Math.round(style.top * density), Math.round(style.left * density),
				Math.round(style.bottom * density), Math.round(style.right * density) });
		if (renderer instanceof XYMultipleSeriesRenderer) {
			XYMultipleSeriesRenderer xy = (XYMultipleSeriesRenderer) renderer;
			xy.setAxisTitleTextSize(style.axisTitleSize * scaledDensity);
			xy.setPanEnabled(style.panX, style.panY);
			xy.setZoomEnabled(style.zoomX, style.zoomY);
			xy.setGridColor(style.gridColor);
			xy.setXLabelsColor(style.textColor);
			xy.setYLabelsColor(0, style.textColor);
			xy.setYLabelsAlign(Paint.Align.RIGHT, 0);
			if (xy.getScalesCount() > 1) {
				xy.setYLabelsColor(1, style.textColor);
				xy.setYAxisAlign(Paint.Align.RIGHT, 1);
				xy.setYLabelsAlign(Paint.Align.LEFT, 1);
			}
			xy.setMarginsColor(background);
			xy.setPointSize(style.pointSize * density);
			xy.setBarSpacing(style.barSpace);
			xy.setOrientation(XYMultipleSeriesRenderer.Orientation.values()[style.orientation]);
		}
	}

	/**
	 * 应用数据组配色、线条、渐变和数值标签。
	 *
	 * @param renderer      目标绘制器。
	 * @param style         数据组配置。
	 * @param color         数据组颜色。
	 * @param density       DIP 密度。
	 * @param scaledDensity SP 密度。
	 * @param values        图表级数值开关。
	 */
	static void data(XYSeriesRenderer renderer, ACE数据组样式 style, int color, float density, float scaledDensity,
			boolean values) {
		renderer.setColor(color);
		renderer.setDisplayChartValues(values && style.values);
		renderer.setChartValuesTextSize(style.valueSize * scaledDensity);
		renderer.setChartValuesSpacing(style.spacing * density);
		renderer.setChartValuesTextAlign(Paint.Align.values()[style.align]);
		renderer.setLineWidth(style.lineWidth * density);
		renderer.setPointStyle(PointStyle.values()[style.pointStyle]);
		renderer.setFillPoints(style.fillPoints);
		renderer.setFillBelowLine(style.fillBelow);
		renderer.setFillBelowLineColor(style.fillColor);
		renderer.setStroke(
				style.stroke == 1 ? BasicStroke.DASHED : style.stroke == 2 ? BasicStroke.DOTTED : BasicStroke.SOLID);
		renderer.setGradientEnabled(style.gradient);
		renderer.setGradientStart(style.gradientStart, style.startColor);
		renderer.setGradientStop(style.gradientStop, style.stopColor);
	}

	/**
	 * 应用横轴或数值轴标签配置。
	 *
	 * @param renderer 多轴绘制器。
	 * @param axis     0 横轴，1 左轴，2 右轴。
	 * @param style    配置副本。
	 */
	static void axis(XYMultipleSeriesRenderer renderer, int axis, ACE轴样式 style) {
		if (axis == 0) {
			renderer.setXLabels(style.count);
			renderer.setXLabelsAngle(style.angle);
			renderer.setXLabelsColor(style.color);
			renderer.setXLabelsAlign(Paint.Align.values()[style.align]);
		} else {
			renderer.setYLabels(style.count);
			renderer.setYLabelsAngle(style.angle);
			renderer.setYLabelsColor(axis - 1, style.color);
			renderer.setYLabelsAlign(Paint.Align.values()[style.align], axis - 1);
			if (!style.title.isEmpty()) {
				renderer.setYTitle(style.title, axis - 1);
			}
		}
	}
}
