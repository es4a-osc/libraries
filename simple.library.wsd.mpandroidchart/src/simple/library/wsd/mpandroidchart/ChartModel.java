package simple.library.wsd.mpandroidchart;

import java.util.ArrayList;
import java.util.Arrays;
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
	boolean numeric;
	final List<Series> series = new ArrayList<Series>();

	/**
	 * 清空名称和数据，释放模型持有的数组。
	 */
	void clear() {
		names = new String[0];
		series.clear();
		numeric = false;
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
	 * @param kind    数据组类型。
	 * @param name    数据组名称。
	 * @param columns 数值列；气泡为数值和大小，蜡烛为开、高、低、收。
	 * @param color   数据组颜色。
	 * @param axis    0 为左轴，1 为右轴。
	 */
	void add(int kind, String name, float[][] columns, int color, int axis) {
		if (numeric) {
			throw new IllegalArgumentException("分类与数值横坐标不能混用，请先清空数据。");
		}
		series.add(checked(kind, name, columns, color, axis, names.length, false));
	}

	/**
	 * 校验全部数值列后创建独立数据组。
	 *
	 * @param kind    图形类型。
	 * @param name    数据组名称。
	 * @param columns 数值列。
	 * @param color   ARGB 颜色。
	 * @param axis    轴编号。
	 * @param length  项目数。
	 * @param stacked 是否为堆叠柱。
	 * @return 完成校验和复制的数据组。
	 */
	private Series checked(int kind, String name, float[][] columns, int color, int axis, int length, boolean stacked) {
		if (name == null || columns == null || axis < 0 || axis > 1 || length == 0) {
			throw new IllegalArgumentException("请先设置非空项目名称，并提供数据组名称及有效坐标轴。");
		}
		int count = stacked ? columns.length : kind == CANDLE ? 4 : (kind == BUBBLE ? 2 : 1);
		if (columns.length != count || count == 0 || (stacked && kind != BAR && kind != HORIZONTAL)) {
			throw new IllegalArgumentException("数据列数与图表类型不符。");
		}
		float[][] copied = new float[count][];
		for (int column = 0; column < count; column++) {
			if (columns[column] == null || columns[column].length != length) {
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
		if (kind == RADAR && length < 3) {
			throw new IllegalArgumentException("雷达图至少需要三个项目。");
		}
		if (kind == CANDLE) {
			for (int index = 0; index < length; index++) {
				float open = copied[0][index];
				float high = copied[1][index];
				float low = copied[2][index];
				float close = copied[3][index];
				if (low > Math.min(open, close) || high < Math.max(open, close) || low > high) {
					throw new IllegalArgumentException("蜡烛图的最高、最低值必须包含开盘和收盘值。");
				}
			}
		}
		if (stacked) {
			for (int i = 0; i < length; i++) {
				double positive = 0;
				double negative = 0;
				for (float[] column : copied) {
					positive += Math.max(0, column[i]);
					negative += Math.min(0, column[i]);
				}
				checkFinite((float) positive);
				checkFinite((float) negative);
			}
		}
		Series result = new Series(kind, name, copied, color, axis);
		result.stacked = stacked;
		return result;
	}

	/**
	 * 检查并复制非递减数值横坐标。
	 *
	 * @param xs 横坐标数组。
	 * @return 独立横坐标。
	 */
	private static float[] coordinates(float[] xs) {
		ChartChecks.required(xs);
		if (xs.length == 0) {
			throw new IllegalArgumentException("至少需要一个项目。");
		}
		float[] copy = xs.clone();
		for (int i = 0; i < copy.length; i++) {
			checkFinite(copy[i]);
			if (i > 0 && copy[i] < copy[i - 1]) {
				throw new IllegalArgumentException("横坐标须按非递减顺序排列。");
			}
		}
		return copy;
	}

	/**
	 * 添加显式数值横坐标的数据组，失败不修改模型。
	 *
	 * @param kind    图形类型。
	 * @param name    数据组名称。
	 * @param xs      非递减横坐标。
	 * @param columns 等长数值列。
	 * @param color   ARGB 颜色。
	 * @param axis    轴编号。
	 */
	void addXY(int kind, String name, float[] xs, float[][] columns, int color, int axis) {
		if (kind == PIE || kind == RADAR || (!series.isEmpty() && !numeric)) {
			throw new IllegalArgumentException("此图形不支持数值坐标，或已存在分类数据。");
		}
		float[] copy = coordinates(xs);
		Series value = checked(kind, name, columns, color, axis, copy.length, false);
		value.xs = copy;
		series.add(value);
		numeric = true;
		names = new String[0];
	}

	/**
	 * 把按列展开的数据拆成独立数值列。
	 *
	 * @param flat  每列依次展开的数值。
	 * @param rows  每列项目数。
	 * @param count 列数。
	 * @return 独立数值列。
	 */
	static float[][] split(float[] flat, int rows, int count) {
		ChartChecks.required(flat);
		if (rows <= 0 || count <= 0 || (long) rows * count != flat.length) {
			throw new IllegalArgumentException("项目数乘以列数须等于数组长度。");
		}
		float[][] result = new float[count][];
		for (int i = 0; i < count; i++) {
			result[i] = Arrays.copyOfRange(flat, i * rows, (i + 1) * rows);
		}
		return result;
	}

	/**
	 * 添加堆叠柱数据，支持分类或显式数值坐标。
	 *
	 * @param kind   柱图类型。
	 * @param name   名称。
	 * @param xs     数值横坐标；空引用使用当前分类。
	 * @param flat   按层整列展开的数值。
	 * @param layers 堆叠层数。
	 * @param color  ARGB 颜色。
	 * @param axis   轴编号。
	 */
	void addStack(int kind, String name, float[] xs, float[] flat, int layers, int color, int axis) {
		boolean explicit = xs != null;
		if ((!series.isEmpty() && numeric != explicit) || (!explicit && names.length == 0)) {
			throw new IllegalArgumentException("请先设置分类名称，且不能混用坐标模式。");
		}
		float[] copy;
		if (explicit) {
			copy = coordinates(xs);
		} else {
			copy = new float[names.length];
			for (int i = 0; i < copy.length; i++) {
				copy[i] = i;
			}
		}
		Series value = checked(kind, name, split(flat, copy.length, layers), color, axis, copy.length, true);
		value.xs = copy;
		series.add(value);
		numeric = explicit;
		if (explicit) {
			names = new String[0];
		}
	}

	/**
	 * 取得数据组并统一检查索引。
	 *
	 * @param index 数据组编号。
	 * @return 模型数据组。
	 */
	Series group(int index) {
		ChartChecks.integer(index, 0, series.size() - 1);
		return series.get(index);
	}

	/**
	 * 原子替换已有数据组，保留样式及名称。
	 *
	 * @param index 数据组编号。
	 * @param xs    横坐标。
	 * @param flat  按列展开的数值。
	 * @param count 列数，普通组 1、气泡 2、蜡烛 4、堆叠为层数。
	 */
	void replace(int index, float[] xs, float[] flat, int count) {
		Series old = group(index);
		float[] copy = coordinates(xs);
		if (old.kind == PIE || (!numeric && copy.length != names.length)) {
			throw new IllegalArgumentException("饼图请更新扇区；分类数据须与名称等长。");
		}
		if (!numeric) {
			for (int i = 0; i < copy.length; i++) {
				if (copy[i] != i) {
					throw new IllegalArgumentException("分类横坐标必须是连续索引。");
				}
			}
		}
		Series value = checked(old.kind, old.name, split(flat, copy.length, count), old.color, old.axis, copy.length,
				old.stacked);
		value.xs = copy;
		value.style = old.style;
		series.set(index, value);
	}

	/**
	 * 为数值坐标数据组追加项目。
	 *
	 * @param index  数据组编号。
	 * @param x      非递减横坐标。
	 * @param values 各列数值。
	 */
	void append(int index, float x, float[] values) {
		Series old = group(index);
		ChartChecks.required(values);
		if (!numeric || values.length != old.columns.length) {
			throw new IllegalArgumentException("追加项目只用于数值坐标，数值个数须等于列数。");
		}
		float[] xs = Arrays.copyOf(old.xs, old.xs.length + 1);
		xs[xs.length - 1] = x;
		float[] flat = new float[xs.length * values.length];
		for (int c = 0; c < values.length; c++) {
			System.arraycopy(old.columns[c], 0, flat, c * xs.length, old.xs.length);
			flat[(c + 1) * xs.length - 1] = values[c];
		}
		replace(index, xs, flat, values.length);
	}

	/**
	 * 删除数值坐标中的项目，至少保留一个。
	 *
	 * @param index 数据组编号。
	 * @param item  项目编号。
	 */
	void remove(int index, int item) {
		Series old = group(index);
		ChartChecks.integer(item, 0, old.xs.length - 1);
		if (!numeric || old.xs.length == 1) {
			throw new IllegalArgumentException("仅数值坐标可单独删除项目，最后一项请删除数据组。");
		}
		int rows = old.xs.length - 1;
		float[] xs = new float[rows];
		float[] flat = new float[rows * old.columns.length];
		for (int i = 0, target = 0; i < old.xs.length; i++) {
			if (i != item) {
				xs[target] = old.xs[i];
				for (int c = 0; c < old.columns.length; c++) {
					flat[c * rows + target] = old.columns[c][i];
				}
				target++;
			}
		}
		replace(index, xs, flat, old.columns.length);
	}

	/**
	 * 添加饼图的一个扇区，非负数据的总和不得溢出单精度范围。
	 *
	 * @param name  扇区名称。
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
	 * 更新扇区，校验失败不改变原扇区。
	 *
	 * @param index 扇区编号。
	 * @param name  新名称。
	 * @param value 非负数值。
	 * @param color ARGB 颜色。
	 */
	void updateSlice(int index, String name, float value, int color) {
		group(index);
		ChartChecks.text(name);
		checkFinite(value);
		if (value < 0) {
			throw new IllegalArgumentException("扇区值不能为负数。");
		}
		double total = value;
		for (int i = 0; i < series.size(); i++) {
			if (i != index) {
				total += series.get(i).columns[0][0];
			}
		}
		if (total > Float.MAX_VALUE / 4) {
			throw new IllegalArgumentException("饼状图数值总和过大。");
		}
		series.set(index, new Series(PIE, name, new float[][] { { value } }, color, 0));
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
	 * @param min  最小值。
	 * @param max  最大值。
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
		float[] xs;
		boolean stacked;
		MPAC数据组样式 style;
		String name;
		final float[][] columns;
		int color;
		int axis;

		/**
		 * 保存已校验的数据组。
		 *
		 * @param kind    图表类型。
		 * @param name    数据组名称。
		 * @param columns 已复制的数值列。
		 * @param color   数据组颜色。
		 * @param axis    坐标轴编号。
		 */
		Series(int kind, String name, float[][] columns, int color, int axis) {
			this.kind = kind;
			xs = new float[columns[0].length];
			for (int i = 0; i < xs.length; i++) {
				xs[i] = i;
			}
			this.name = name;
			this.columns = columns;
			this.color = color;
			this.axis = axis;
		}
	}
}
