package simple.library.wsd.mpandroidchart;

import java.util.ArrayList;
import java.util.List;

/**
 * 保存图表项目和数据组，在修改模型前统一检查数组、数值和图表约束。
 *
 * @author 树先生 xhwsd@qq.com
 */
final class ChartModel {

	static final int LINE = 0;
	static final int BAR = 1;
	static final int HORIZONTAL = 2;
	static final int PIE = 3;
	static final int SCATTER = 4;
	static final int BUBBLE = 5;
	static final int RADAR = 6;
	static final int CANDLE = 7;
	static final int COMBINED = 8;

	String[] names = new String[0];
	final List<Series> series = new ArrayList<Series>();

	/**
	 * 清空名称和数据，释放模型持有的数组。
	 */
	void clear() {
		names = new String[0];
		series.clear();
	}

	/**
	 * 设置各项目的分类名称；有数据后必须先清空才能更改名称。
	 *
	 * @param labels 项目名称数组。
	 */
	void setNames(String[] labels) {
		if (labels == null || !series.isEmpty()) {
			throw new IllegalArgumentException("请在添加数据组前设置项目名称；更换名称前先清空数据。");
		}
		for (String label : labels) {
			if (label == null) {
				throw new IllegalArgumentException("项目名称不能为 null。");
			}
		}
		names = labels.clone();
	}

	/**
	 * 添加数据组，全部列校验通过后才写入模型。
	 *
	 * @param kind 数据组类型。
	 * @param name 数据组名称。
	 * @param columns 数值列；气泡为数值和大小，蜡烛为开、高、低、收。
	 * @param color 数据组颜色。
	 * @param axis 0 为左轴，1 为右轴。
	 */
	void add(int kind, String name, float[][] columns, int color, int axis) {
		if (name == null || columns == null || axis < 0 || axis > 1 || names.length == 0) {
			throw new IllegalArgumentException("请先设置非空项目名称，并提供数据组名称及有效坐标轴。");
		}
		int count = kind == CANDLE ? 4 : (kind == BUBBLE ? 2 : 1);
		if (columns.length != count) {
			throw new IllegalArgumentException("数据列数与图表类型不符。");
		}
		float[][] copied = new float[count][];
		for (int column = 0; column < count; column++) {
			if (columns[column] == null || columns[column].length != names.length) {
				throw new IllegalArgumentException("每列数据必须与项目名称数量相同。");
			}
			copied[column] = columns[column].clone();
			for (float value : copied[column]) {
				checkFinite(value);
				if ((kind == RADAR || (kind == BUBBLE && column == 1)) && value < 0) {
					throw new IllegalArgumentException("雷达数值及气泡大小不能为负数。");
				}
			}
		}
		if (kind == RADAR && names.length < 3) {
			throw new IllegalArgumentException("雷达图至少需要三个项目。");
		}
		if (kind == CANDLE) {
			for (int index = 0; index < names.length; index++) {
				float open = copied[0][index];
				float high = copied[1][index];
				float low = copied[2][index];
				float close = copied[3][index];
				if (low > Math.min(open, close) || high < Math.max(open, close) || low > high) {
					throw new IllegalArgumentException("蜡烛图的最高、最低值必须包含开盘和收盘值。");
				}
			}
		}
		series.add(new Series(kind, name, copied, color, axis));
	}

	/**
	 * 添加饼图的一个扇区，非负数据的总和不得溢出单精度范围。
	 *
	 * @param name 扇区名称。
	 * @param value 非负数值。
	 * @param color 扇区颜色。
	 */
	void addSlice(String name, float value, int color) {
		checkFinite(value);
		if (name == null || value < 0) {
			throw new IllegalArgumentException("饼状图名称不能为 null，数值不能为负数。");
		}
		double total = value;
		for (Series item : series) {
			total += item.columns[0][0];
		}
		if (total > Float.MAX_VALUE / 4) {
			throw new IllegalArgumentException("饼状图数值总和过大。");
		}
		series.add(new Series(PIE, name, new float[][] { { value } }, color, 0));
	}

	/**
	 * 拒绝无法计算占比的全零饼图。
	 */
	void checkPie() {
		for (Series item : series) {
			if (item.columns[0][0] > 0) {
				return;
			}
		}
		if (!series.isEmpty()) {
			throw new IllegalArgumentException("非空饼状图至少需要一个正数。");
		}
	}

	/**
	 * 检查单精度绘制值，留出坐标边距及中间运算空间。
	 *
	 * @param value 待检查数值。
	 */
	static void checkFinite(float value) {
		if (Float.isNaN(value) || Float.isInfinite(value) || Math.abs(value) > Float.MAX_VALUE / 4) {
			throw new IllegalArgumentException("数值必须有限，绝对值不能超过单精度最大值的四分之一。");
		}
	}

	/**
	 * 检查自定义坐标轴范围。
	 *
	 * @param axis 坐标轴编号。
	 * @param min 最小值。
	 * @param max 最大值。
	 */
	static void checkRange(int axis, float min, float max) {
		checkFinite(min);
		checkFinite(max);
		if (axis < 0 || axis > 1 || min >= max) {
			throw new IllegalArgumentException("轴编号只能为 0 或 1，最小值必须小于最大值。");
		}
	}

	/**
	 * 独立复制的数据组，不向 Simple 导出底层对象。
	 */
	static final class Series {

		final int kind;
		final String name;
		final float[][] columns;
		final int color;
		final int axis;

		/**
		 * 保存已校验的数据组。
		 *
		 * @param kind 图表类型。
		 * @param name 数据组名称。
		 * @param columns 已复制的数值列。
		 * @param color 数据组颜色。
		 * @param axis 坐标轴编号。
		 */
		Series(int kind, String name, float[][] columns, int color, int axis) {
			this.kind = kind;
			this.name = name;
			this.columns = columns;
			this.color = color;
			this.axis = axis;
		}
	}
}
