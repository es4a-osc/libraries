package simple.library.wsd.mpandroidchart;

import android.graphics.Paint;
import android.graphics.Typeface;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarLineScatterCandleBubbleDataSet;
import com.github.mikephil.charting.data.BubbleDataSet;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.LineRadarDataSet;
import com.github.mikephil.charting.data.LineScatterCandleRadarDataSet;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.ScatterDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

/**
 * 集中应用配置快照，隔离格式回调和第三方绘制对象。
 *
 * @author 树先生 xhwsd@qq.com
 */
final class ChartStyles {

	/**
	 * 格式回调期间禁止重入图表，避免绘制过程中修改集合。
	 */
	private static final ThreadLocal<Boolean> FORMATTING = new ThreadLocal<Boolean>();

	/**
	 * 创建不可实例化的样式工具。
	 */
	private ChartStyles() {
	}

	/**
	 * 检查当前是否正在调用格式器。
	 *
	 * @return 是否处于格式回调。
	 */
	static boolean formatting() {
		return Boolean.TRUE.equals(FORMATTING.get());
	}

	/**
	 * 调用格式器并可靠释放重入标志。
	 *
	 * @param formatter Simple 格式器。
	 * @param value     待格式化数值。
	 * @return 非空文本。
	 */
	static String format(MPAC格式器 formatter, float value) {
		Boolean previous = FORMATTING.get();
		FORMATTING.set(Boolean.TRUE);
		try {
			String text = formatter.格式化(value);
			return text == null ? "" : text;
		} finally {
			if (previous == null) {
				FORMATTING.remove();
			} else {
				FORMATTING.set(previous);
			}
		}
	}

	/**
	 * 验证并加载字体；先加载成功再保存配置，失败不修改图表。
	 *
	 * @param chart 所属图表。
	 * @param path  已打包的字体资产路径。
	 * @return 对应字体，空路径返回默认字体。
	 */
	static Typeface font(Chart<?> chart, String path) {
		try {
			return path.isEmpty() ? Typeface.DEFAULT : Typeface.createFromAsset(chart.getContext().getAssets(), path);
		} catch (RuntimeException error) {
			throw new IllegalArgumentException("无法加载字体资产：" + path, error);
		}
	}

	/**
	 * 应用轴配置及限制线。
	 *
	 * @param target   原生轴。
	 * @param style    配置快照。
	 * @param typeface 已加载的字体。
	 */
	static void axis(AxisBase target, final MPAC轴样式 style, Typeface typeface) {
		target.setEnabled(style.enabled);
		target.setDrawLabels(style.labels);
		target.setDrawGridLines(style.grid);
		target.setDrawAxisLine(style.axisLine);
		target.setTextColor(style.textColor);
		target.setTextSize(style.textSize);
		target.setTypeface(typeface);
		target.setGridColor(style.gridColor);
		target.setGridLineWidth(style.gridWidth);
		target.setAxisLineColor(style.axisColor);
		target.setAxisLineWidth(style.axisWidth);
		target.setLabelCount(style.labelCount, style.forceLabels);
		target.setGranularity(style.granularity);
		target.setCenterAxisLabels(style.centerLabels);
		target.setXOffset(style.xOffset);
		target.setYOffset(style.yOffset);
		if (style.dashLength > 0) {
			target.enableGridDashedLine(style.dashLength, style.dashGap, 0);
		} else {
			target.disableGridDashedLine();
		}
		if (style.formatter != null) {
			target.setValueFormatter(new IAxisValueFormatter() {
				@Override
				public String getFormattedValue(float value, AxisBase axis) {
					return format(style.formatter, value);
				}
			});
		}
		if (target instanceof XAxis) {
			XAxis axis = (XAxis) target;
			axis.setPosition(XAxis.XAxisPosition.values()[style.xPosition]);
			axis.setLabelRotationAngle(style.rotation);
		} else if (target instanceof YAxis) {
			YAxis axis = (YAxis) target;
			axis.setPosition(YAxis.YAxisLabelPosition.values()[style.yPosition]);
			axis.setInverted(style.inverted);
			axis.setDrawZeroLine(style.zeroLine);
			axis.setZeroLineColor(style.zeroColor);
			axis.setZeroLineWidth(style.zeroWidth);
			axis.setSpaceTop(style.spaceTop);
			axis.setSpaceBottom(style.spaceBottom);
		}
		target.removeAllLimitLines();
		target.setDrawLimitLinesBehindData(style.limitsBehind);
		for (MPAC限制线 source : style.limits) {
			if (!source.enabled) {
				continue;
			}
			LimitLine line = new LimitLine(source.position, source.label);
			line.setLineColor(source.color);
			line.setLineWidth(source.width);
			line.setTextColor(source.textColor);
			line.setTextSize(source.textSize);
			line.setTypeface(typeface);
			line.setXOffset(source.xOffset);
			line.setYOffset(source.yOffset);
			line.setLabelPosition(LimitLine.LimitLabelPosition.values()[source.labelPosition]);
			if (source.dashLength > 0) {
				line.enableDashedLine(source.dashLength, source.dashGap, source.dashPhase);
			}
			target.addLimitLine(line);
		}
	}

	/**
	 * 应用图例排列、自定义条目和附加条目。
	 *
	 * @param target   原生图例。
	 * @param style    配置快照。
	 * @param typeface 已加载的字体。
	 */
	static void legend(Legend target, MPAC图例样式 style, Typeface typeface) {
		target.setTextColor(style.textColor);
		target.setTextSize(style.textSize);
		target.setTypeface(typeface);
		target.setHorizontalAlignment(Legend.LegendHorizontalAlignment.values()[style.horizontal]);
		target.setVerticalAlignment(Legend.LegendVerticalAlignment.values()[style.vertical]);
		target.setOrientation(Legend.LegendOrientation.values()[style.orientation]);
		target.setDirection(Legend.LegendDirection.values()[style.direction]);
		target.setDrawInside(style.inside);
		target.setWordWrapEnabled(style.wrap);
		target.setForm(Legend.LegendForm.values()[style.form]);
		target.setFormSize(style.formSize);
		target.setFormLineWidth(style.formWidth);
		target.setXEntrySpace(style.xSpace);
		target.setYEntrySpace(style.ySpace);
		target.setFormToTextSpace(style.formSpace);
		target.setStackSpace(style.stackSpace);
		target.setMaxSizePercent(style.maxSize);
		if (style.custom) {
			LegendEntry[] entries = new LegendEntry[style.labels.length];
			for (int i = 0; i < entries.length; i++) {
				entries[i] = new LegendEntry(style.labels[i], Legend.LegendForm.DEFAULT, Float.NaN, Float.NaN, null,
						style.colors[i]);
			}
			target.setCustom(entries);
		} else {
			target.resetCustom();
		}
		target.setExtra(style.extraColors, style.extraLabels);
	}

	/**
	 * 将数据组配置应用到对应类型，统一处理数值与高亮。
	 *
	 * @param set    原生数据组。
	 * @param style  配置快照。
	 * @param values 图表级数值开关。
	 */
	static void data(DataSet<? extends Entry> set, final MPAC数据组样式 style, boolean values) {
		if (style.colors.length > 0) {
			set.setColors(style.colors);
		}
		set.setVisible(style.visible);
		set.setDrawValues(values && style.values);
		set.setValueTextColor(style.valueColor);
		set.setValueTextSize(style.valueSize);
		set.setHighlightEnabled(style.highlight);
		if (style.formatter != null) {
			set.setValueFormatter(new IValueFormatter() {
				@Override
				public String getFormattedValue(float value, Entry entry, int index, ViewPortHandler handler) {
					return format(style.formatter, value);
				}
			});
		}
		if (set instanceof BarLineScatterCandleBubbleDataSet) {
			((BarLineScatterCandleBubbleDataSet<?>) set).setHighLightColor(style.highlightColor);
		}
		if (set instanceof LineScatterCandleRadarDataSet) {
			LineScatterCandleRadarDataSet<?> target = (LineScatterCandleRadarDataSet<?>) set;
			target.setHighlightLineWidth(style.highlightWidth);
			target.setDrawHorizontalHighlightIndicator(style.horizontalHighlight);
			target.setDrawVerticalHighlightIndicator(style.verticalHighlight);
		}
		if (set instanceof LineRadarDataSet) {
			LineRadarDataSet<?> target = (LineRadarDataSet<?>) set;
			target.setLineWidth(style.lineWidth);
			target.setDrawFilled(style.filled);
			target.setFillColor(style.fillColor);
			target.setFillAlpha(style.fillAlpha);
		}
		if (set instanceof LineDataSet) {
			LineDataSet target = (LineDataSet) set;
			if (style.lineMode >= 0) {
				target.setMode(LineDataSet.Mode.values()[style.lineMode]);
			}
			target.setCubicIntensity(style.cubic);
			target.setDrawCircles(style.circles);
			target.setCircleRadius(style.circleRadius);
			target.setCircleHoleRadius(style.holeRadius);
			target.setCircleHoleColor(style.holeColor);
			target.setDrawCircleHole(style.circleHole);
			if (style.circleColors.length > 0) {
				target.setCircleColors(style.circleColors);
			}
			if (style.dashLength > 0) {
				target.enableDashedLine(style.dashLength, style.dashGap, style.dashPhase);
			} else {
				target.disableDashedLine();
			}
		}
		if (set instanceof ScatterDataSet) {
			ScatterDataSet target = (ScatterDataSet) set;
			target.setScatterShape(ScatterChart.ScatterShape.values()[style.scatterShape]);
			target.setScatterShapeSize(style.scatterSize);
			target.setScatterShapeHoleRadius(style.scatterHole);
			target.setScatterShapeHoleColor(style.scatterHoleColor);
		}
		if (set instanceof BubbleDataSet) {
			BubbleDataSet target = (BubbleDataSet) set;
			target.setNormalizeSizeEnabled(style.normalize);
			target.setHighlightCircleWidth(style.highlightWidth);
		}
		if (set instanceof BarDataSet) {
			BarDataSet target = (BarDataSet) set;
			target.setBarBorderWidth(style.barBorderWidth);
			target.setBarBorderColor(style.barBorder);
			target.setBarShadowColor(style.barShadow);
			if (style.stackLabels.length > 0) {
				target.setStackLabels(style.stackLabels);
			}
		}
		if (set instanceof CandleDataSet) {
			CandleDataSet target = (CandleDataSet) set;
			target.setIncreasingColor(style.increasing);
			target.setDecreasingColor(style.decreasing);
			target.setNeutralColor(style.neutral);
			target.setIncreasingPaintStyle(style.increaseHollow ? Paint.Style.STROKE : Paint.Style.FILL);
			target.setDecreasingPaintStyle(style.decreaseHollow ? Paint.Style.STROKE : Paint.Style.FILL);
			target.setShadowColorSameAsCandle(style.shadowSame);
			target.setShadowColor(style.shadowColor);
			target.setShadowWidth(style.lineWidth);
			target.setBarSpace(style.candleSpace);
			target.setShowCandleBar(style.candleBar);
		}
		if (set instanceof PieDataSet) {
			PieDataSet target = (PieDataSet) set;
			target.setSliceSpace(style.sliceSpace);
			target.setSelectionShift(style.selectionShift);
			target.setXValuePosition(PieDataSet.ValuePosition.values()[style.xValuePosition]);
			target.setYValuePosition(PieDataSet.ValuePosition.values()[style.yValuePosition]);
			target.setValueLineColor(style.valueLineColor);
			target.setValueLineWidth(style.valueLineWidth);
			target.setValueLinePart1OffsetPercentage(style.valueLineOffset);
			target.setValueLinePart1Length(style.valueLineFirst);
			target.setValueLinePart2Length(style.valueLineSecond);
			target.setValueLineVariableLength(style.variableLine);
			target.setUseValueColorForLine(style.sliceLineColor);
		}
	}

	/**
	 * 应用饼图专属配置。
	 *
	 * @param target 原生饼图。
	 * @param style  配置快照。
	 */
	static void pie(PieChart target, MPAC饼图样式 style) {
		target.setDrawHoleEnabled(style.hole);
		target.setHoleColor(style.holeColor);
		target.setHoleRadius(style.holeRadius);
		target.setTransparentCircleRadius(style.transparentRadius);
		target.setTransparentCircleColor(style.transparentColor);
		target.setTransparentCircleAlpha(style.transparentAlpha);
		target.setCenterText(style.centerText);
		target.setDrawCenterText(!style.centerText.isEmpty());
		target.setCenterTextSize(style.centerSize);
		target.setCenterTextColor(style.centerColor);
		target.setCenterTextRadiusPercent(style.centerRadius);
		target.setDrawEntryLabels(style.entryLabels);
		target.setEntryLabelColor(style.entryColor);
		target.setEntryLabelTextSize(style.entrySize);
		target.setUsePercentValues(style.percent);
		target.setDrawRoundedSlices(style.rounded);
		target.setDrawSlicesUnderHole(style.underHole);
		target.setMaxAngle(style.maxAngle);
		target.setMinAngleForSlices(style.minAngle);
	}

	/**
	 * 应用雷达网格配置。
	 *
	 * @param target 原生雷达图。
	 * @param style  配置快照。
	 */
	static void radar(RadarChart target, MPAC雷达样式 style) {
		target.setDrawWeb(style.web);
		target.setWebColor(style.color);
		target.setWebColorInner(style.innerColor);
		target.setWebLineWidth(style.width);
		target.setWebLineWidthInner(style.innerWidth);
		target.setWebAlpha(style.alpha);
		target.setSkipWebLineCount(style.skip);
	}
}
