package simple.library.wsd.mpandroidchart;

import android.graphics.Canvas;
import android.graphics.Paint;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.components.IMarker;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;

/**
 * 在选中位置上方绘制内置数值提示，并将提示限制在图表边界内。
 *
 * @author 树先生 xhwsd@qq.com
 */
final class ChartMarker implements IMarker {

	private final Chart<?> chart;
	private final MPAC图表样式 style;
	private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private final MPPointF offset = new MPPointF();
	private String text = "";

	/**
	 * 创建与图表一起释放的数值提示。
	 *
	 * @param chart 所属图表。
	 * @param style 配置快照。
	 */
	ChartMarker(Chart<?> chart, MPAC图表样式 style) {
		this.chart = chart;
		this.style = style;
		paint.setTextSize(style.markerSize * chart.getResources().getDisplayMetrics().scaledDensity);
		paint.setTypeface(ChartStyles.font(chart, style.font));
	}

	@Override
	public MPPointF getOffset() {
		return offset;
	}

	@Override
	public MPPointF getOffsetForDrawingAtPoint(float x, float y) {
		return offset;
	}

	@Override
	public void refreshContent(Entry entry, Highlight highlight) {
		float value = entry instanceof CandleEntry ? ((CandleEntry) entry).getClose() : entry.getY();
		text = Float.toString(value);
	}

	@Override
	public void draw(Canvas canvas, float x, float y) {
		float padding = 6 * chart.getResources().getDisplayMetrics().density;
		float width = Math.min(chart.getWidth(), paint.measureText(text) + padding * 2);
		Paint.FontMetrics metrics = paint.getFontMetrics();
		float height = metrics.descent - metrics.ascent + padding * 2;
		float left = Math.max(0, Math.min(chart.getWidth() - width, x - width / 2));
		float top = Math.max(0, Math.min(chart.getHeight() - height, y - height - padding));
		paint.setColor(style.markerColor);
		canvas.drawRoundRect(left, top, left + width, top + height, padding, padding, paint);
		paint.setColor(style.markerTextColor);
		canvas.drawText(text, left + padding, top + padding - metrics.ascent, paint);
	}
}
