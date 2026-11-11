package simple.library.wsd.mpandroidchart;

import android.content.Context;
import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.renderer.YAxisRendererRadarChart;

/**
 * 保留显式雷达范围，避免上游将固定最大值扩展到下一刻度。
 *
 * @author 树先生 xhwsd@qq.com
 */
final class BoundedRadarChart extends RadarChart {

	/**
	 * 创建具有固定范围约束的雷达图。
	 *
	 * @param context 视图上下文。
	 */
	BoundedRadarChart(Context context) {
		super(context);
		mYAxisRenderer = new YAxisRendererRadarChart(getViewPortHandler(), getYAxis(), this) {
			@Override
			protected void computeAxisValues(float min, float max) {
				YAxis axis = getYAxis();
				boolean fixedMin = axis.isAxisMinCustom();
				boolean fixedMax = axis.isAxisMaxCustom();
				super.computeAxisValues(min, max);
				float lower = fixedMin ? min : axis.mAxisMinimum;
				float upper = fixedMax ? max : axis.mAxisMaximum;
				int count = 0;
				for (int i = 0; i < axis.mEntryCount; i++) {
					float value = axis.mEntries[i];
					if (value >= lower && value <= upper) {
						axis.mEntries[count++] = value;
					}
				}
				axis.mEntryCount = count;
				axis.mAxisMinimum = lower;
				axis.mAxisMaximum = upper;
				axis.mAxisRange = upper - lower;
			}
		};
	}
}
