package org.achartengine.chart;

import android.graphics.Canvas;
import android.graphics.Paint;
import java.util.List;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer.Orientation;

/**
 * 让组合图的子绘制器共享主图的当前范围，避免多轴数据落入子图的零号轴之外。 放在绘制器包内以使用上游的受保护扩展接口，不修改上游 JAR。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class ES4ACombinedChart extends XYChart {

	private final XYChart[] children;
	private final simple.library.wsd.achartengine.HitMap frame;

	/**
	 * 创建保持数据添加顺序的组合图。
	 *
	 * @param dataset  主图原始数据。
	 * @param renderer 具有左右轴的主绘制器。
	 * @param children 每组一个零号轴子绘制器。
	 * @param frame    本帧绘制状态。
	 */
	public ES4ACombinedChart(XYMultipleSeriesDataset dataset, XYMultipleSeriesRenderer renderer, XYChart[] children,
			simple.library.wsd.achartengine.HitMap frame) {
		super(dataset, renderer);
		this.children = children;
		this.frame = frame;
	}

	/**
	 * 将当前主图屏幕与数值范围传入子绘制器。
	 *
	 * @param index 数据组编号。
	 * @return 已同步的子绘制器。
	 */
	private XYChart child(int index) {
		XYChart child = children[index];
		double[] range = getCalcRange(mDataset.getSeriesAt(index).getScaleNumber());
		child.setScreenR(getScreenR());
		child.setCalcRange(range, 0);
		child.getRenderer().setRange(range, 0);
		return child;
	}

	@Override
	public void drawSeries(Canvas canvas, Paint paint, float[] points, SimpleSeriesRenderer renderer, float baseline,
			int index, int start) {
		child(index).drawSeries(canvas, paint, points, renderer, baseline, 0, start);
	}

	@Override
	protected void drawSeries(XYSeries series, Canvas canvas, Paint paint, List<Float> points,
			SimpleSeriesRenderer renderer, float baseline, int index, Orientation orientation, int start) {
		int actual = frame.segment(series, points.size() / 2, start);
		child(index).drawSeries(children[index].mDataset.getSeriesAt(0), canvas, paint, points, renderer, baseline, 0,
				orientation, actual);
	}

	@Override
	protected ClickableArea[] clickableAreasForPoints(float[] points, double[] values, float baseline, int index,
			int start) {
		ClickableArea[] areas = child(index).clickableAreasForPoints(points, values, baseline, 0, start);
		for (ClickableArea area : areas) {
			if (area != null && area.getRect() != null) {
				area.getRect().sort();
			}
		}
		return areas;
	}

	@Override
	public int getLegendShapeWidth(int index) {
		return children[index].getLegendShapeWidth(0);
	}

	@Override
	public void drawLegendShape(Canvas canvas, SimpleSeriesRenderer renderer, float x, float y, int index,
			Paint paint) {
		children[index].drawLegendShape(canvas, renderer, x, y, 0, paint);
	}

	@Override
	public String getChartType() {
		return "Combined";
	}
}
