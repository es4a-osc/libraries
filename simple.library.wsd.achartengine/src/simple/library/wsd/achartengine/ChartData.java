package simple.library.wsd.achartengine;

import java.util.ArrayList;
import java.util.List;

/**
 * 保存已校验的图表项目并计算坐标范围，不依赖 Android 视图。
 *
 * @author 树先生 xhwsd@qq.com
 */
final class ChartData {

	private final List<Entry> entries = new ArrayList<Entry>();

	/**
	 * 添加有效项目；校验失败时不改变已有数据。
	 *
	 * @param name 项目名称。
	 * @param text 原始数值文本。
	 * @param unit 数值单位。
	 * @param color 项目颜色。
	 * @param pie 是否按饼状图限制负数。
	 */
	void add(String name, String text, String unit, int color, boolean pie) {
		if (name == null || text == null || unit == null) {
			throw new IllegalArgumentException("项目名称、数据和单位不能为 null。");
		}

		double value;
		try {
			value = Double.parseDouble(text.trim());
		} catch (NumberFormatException error) {
			throw new IllegalArgumentException("图表数据必须为数值文本。", error);
		}
		if (Double.isNaN(value) || Double.isInfinite(value) || (pie && value < 0)) {
			throw new IllegalArgumentException(pie ? "饼状图数据必须为有限非负数。" : "图表数据必须为有限数值。");
		}
		entries.add(new Entry(name, text.trim(), unit, value, color));
	}

	/** 清空全部项目。 */
	void clear() {
		entries.clear();
	}

	/**
	 * 取得项目数量。
	 *
	 * @return 项目数量。
	 */
	int size() {
		return entries.size();
	}

	/**
	 * 取得指定项目。
	 *
	 * @param index 从 0 开始的索引。
	 * @return 已校验且不可变的项目。
	 */
	Entry get(int index) {
		return entries.get(index);
	}

	/**
	 * 计算含零点和边距的纵轴范围，不修改任何项目或累计边距。
	 *
	 * @return 纵轴最小值和最大值。
	 * @throws IllegalArgumentException 数值过大，无法生成有限的坐标范围。
	 */
	double[] range() {
		double min = 0;
		double max = 0;
		for (Entry entry : entries) {
			min = Math.min(min, entry.value);
			max = Math.max(max, entry.value);
		}

		double margin = Math.max(Math.abs(min), Math.abs(max)) / 8;
		if (margin == 0) {
			margin = 1;
		}
		if (min < 0) {
			min -= margin;
		}
		max = max > 0 ? max + margin : (min == 0 ? 1 : 0);
		if (Double.isInfinite(min) || Double.isInfinite(max) || Double.isInfinite(max - min)) {
			throw new IllegalArgumentException("图表数据范围过大，无法绘制。");
		}
		return new double[] { min, max };
	}

	/**
	 * 获取饼图归一化基准，避免大数相加溢出。
	 *
	 * @return 当前项目的最大值。
	 * @throws IllegalArgumentException 非空数据全部为零。
	 */
	double pieScale() {
		double scale = 0;
		for (Entry entry : entries) {
			scale = Math.max(scale, entry.value);
		}
		if (!entries.isEmpty() && scale == 0) {
			throw new IllegalArgumentException("饼状图至少需要一个大于零的数据。");
		}
		return scale;
	}

	/** 已校验的项目值，保留原数值文本用于标签。 */
	static final class Entry {

		final String name;
		final String text;
		final String unit;
		final double value;
		final int color;

		/**
		 * 保存项目值。
		 *
		 * @param name 项目名称。
		 * @param text 数值文本。
		 * @param unit 显示单位。
		 * @param value 已校验数值。
		 * @param color 项目颜色。
		 */
		Entry(String name, String text, String unit, double value, int color) {
			this.name = name;
			this.text = text;
			this.unit = unit;
			this.value = value;
			this.color = color;
		}
	}
}
