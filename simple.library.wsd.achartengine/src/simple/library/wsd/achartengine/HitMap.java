package simple.library.wsd.achartengine;

import java.util.ArrayList;
import java.util.List;
import android.graphics.Matrix;
import android.graphics.RectF;
import org.achartengine.chart.ClickableArea;
import org.achartengine.model.SeriesSelection;

/**
 * 记录本帧实际绘制的点击区域，绕开上游对缺失点空区域和缩放后相对编号的假设。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class HitMap {

	private final List<Hit> hits = new ArrayList<Hit>();
	private final java.util.IdentityHashMap<org.achartengine.model.XYSeries, Integer> cursors = new java.util.IdentityHashMap<org.achartengine.model.XYSeries, Integer>();

	/**
	 * 清空上一帧的区域。
	 */
	void clear() {
		hits.clear();
		cursors.clear();
	}

	/**
	 * 从实际可见范围的首项开始登记本帧分段位置。
	 *
	 * @param series 原生数据组。
	 * @param first  范围内首项横坐标，null 表示空范围。
	 */
	void begin(org.achartengine.model.XYSeries series, Double first) {
		cursors.put(series, first == null ? 0 : series.getIndexForKey(first));
	}

	/**
	 * 取得缺失值分段后的原数组编号，避免标签和气泡大小沿用前一段编号。
	 *
	 * @param series   原生数据组。
	 * @param count    本段有效项目数。
	 * @param fallback 没有遍历主数据组时使用的父图编号。
	 * @return 当前段的原始起始编号。
	 */
	public int segment(org.achartengine.model.XYSeries series, int count, int fallback) {
		Integer cursor = cursors.get(series);
		if (cursor == null) {
			return fallback;
		}
		int start = cursor.intValue();
		while (start < series.getItemCount() && series.getY(start) == org.achartengine.util.MathHelper.NULL_VALUE) {
			start++;
		}
		cursors.put(series, start + count);
		return start;
	}

	/**
	 * 保存原生区域及其实际横坐标，忽略没有绘制的数据。
	 *
	 * @param group 原模型数据组编号。
	 * @param areas 原生区域。
	 * @return 同一原生区域数组。
	 */
	ClickableArea[] record(int group, ClickableArea[] areas) {
		for (ClickableArea area : areas) {
			if (area != null && area.getRect() != null) {
				hits.add(new Hit(group, area));
			}
		}
		return areas;
	}

	/**
	 * 按从上到下的绘制顺序命中，并逆变换横向图形的画布坐标。
	 *
	 * @param x       点击横像素。
	 * @param y       点击纵像素。
	 * @param width   图表宽度。
	 * @param height  图表高度。
	 * @param rotated 是否采用横向图形。
	 * @return 命中的原始坐标；未命中返回 null。
	 */
	SeriesSelection find(float x, float y, int width, int height, boolean rotated) {
		if (rotated && width > 0 && height > 0) {
			float scale = (float) height / width;
			float shift = Math.abs(width - height) / 2;
			if (scale < 1) {
				shift *= -1;
			}
			Matrix forward = new Matrix();
			forward.setRotate(90, width / 2f, height / 2f);
			forward.preTranslate(-shift, shift);
			forward.preScale(scale, 1 / scale);
			Matrix inverse = new Matrix();
			if (!forward.invert(inverse)) {
				return null;
			}
			float[] point = { x, y };
			inverse.mapPoints(point);
			x = point[0];
			y = point[1];
		}
		for (int i = hits.size() - 1; i >= 0; i--) {
			Hit hit = hits.get(i);
			if (hit.rect.contains(x, y)) {
				return new SeriesSelection(hit.group, 0, hit.x, hit.y);
			}
		}
		return null;
	}

	/**
	 * 保存一块不依赖上游可变数组的点击区域。
	 */
	private static final class Hit {
		final int group;
		final RectF rect;
		final double x;
		final double y;

		/**
		 * 复制区域与原数值。
		 *
		 * @param group 数据组编号。
		 * @param area  本帧绘制区域。
		 */
		Hit(int group, ClickableArea area) {
			this.group = group;
			this.rect = new RectF(area.getRect());
			this.rect.sort();
			this.x = area.getX();
			this.y = area.getY();
		}
	}
}
