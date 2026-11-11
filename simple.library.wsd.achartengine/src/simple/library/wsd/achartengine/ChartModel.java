package simple.library.wsd.achartengine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 保存十一类图表的独立数据快照，在替换模型前完成全部校验。
 *
 * @author 树先生 xhwsd@qq.com
 */
final class ChartModel {

	static final int PIE = 0, BAR = 1, LINE = 2, CUBIC = 3, TIME = 4, SCATTER = 5, BUBBLE = 6, RANGE = 7, COMBINED = 8,
			DONUT = 9, DIAL = 10;
	final List<Series> series = new ArrayList<Series>();
	String[] labels = new String[0];
	boolean numeric;

	/**
	 * 清空模型和坐标模式。
	 */
	void clear() {
		series.clear();
		labels = new String[0];
		numeric = false;
	}

	/**
	 * 在添加数据前设置分类名称。
	 *
	 * @param values 分类名称。
	 */
	void names(String[] values) {
		if (!series.isEmpty()) {
			throw new IllegalArgumentException("请先清空数据再更换分类名称。");
		}
		labels = Checks.names(values);
	}

	/**
	 * 检查并复制数据组横纵坐标。
	 *
	 * @param kind    图形类型。
	 * @param name    数据组名称。
	 * @param xs      递增横坐标。
	 * @param columns 数值列。
	 * @param color   ARGB 颜色。
	 * @param axis    0 左轴，1 右轴。
	 * @return 新数据组。
	 */
	private Series checked(int kind, String name, double[] xs, double[][] columns, int color, int axis) {
		Checks.text(name);
		Checks.required(xs);
		Checks.required(columns);
		Checks.index(axis, 0, 1);
		int count = kind == BUBBLE || kind == RANGE ? 2 : 1;
		if (xs.length == 0 || columns.length != count) {
			throw new IllegalArgumentException("项目不能为空，数据列数须与图表类型相符。");
		}
		double[] coordinates = xs.clone();
		for (int i = 0; i < xs.length; i++) {
			Checks.range(xs[i], -1e99, 1e99);
			if (i > 0 && xs[i] <= xs[i - 1]) {
				throw new IllegalArgumentException("横坐标必须严格递增，不能重复。");
			}
			if (kind == TIME) {
				Checks.range(xs[i], -8640000000000000d, 8640000000000000d);
				if (xs[i] != Math.rint(xs[i])) {
					throw new IllegalArgumentException("时间横坐标须为整数毫秒时间戳。");
				}
			}
			if (kind == RANGE && i + 1 < xs.length && Math.nextUp(xs[i]) >= xs[i + 1]) {
				throw new IllegalArgumentException("区间柱横坐标之间须保留可表示的间隔。");
			}
		}
		double[][] copy = new double[count][];
		for (int column = 0; column < count; column++) {
			Checks.required(columns[column]);
			if (columns[column].length != xs.length) {
				throw new IllegalArgumentException("各数值列须与横坐标等长。");
			}
			copy[column] = columns[column].clone();
			for (double value : copy[column]) {
				Checks.range(value, -1e99, 1e99);
				if ((kind == PIE || kind == DONUT || (kind == BUBBLE && column == 1)) && value < 0) {
					throw new IllegalArgumentException("饼图、圆环数值和气泡大小不能为负数。");
				}
			}
		}
		if (kind == RANGE) {
			for (int i = 0; i < xs.length; i++) {
				if (copy[0][i] > copy[1][i]) {
					throw new IllegalArgumentException("区间下限不能大于上限。");
				}
			}
		}
		return new Series(kind, name, coordinates, copy, color, axis);
	}

	/**
	 * 添加分类或数值坐标数据组；失败时不改变坐标模式。
	 *
	 * @param kind    图形类型。
	 * @param name    数据组名称。
	 * @param xs      横坐标，null 使用已有分类索引。
	 * @param columns 数值列。
	 * @param color   ARGB 颜色。
	 * @param axis    数值轴编号。
	 */
	void add(int kind, String name, double[] xs, double[][] columns, int color, int axis) {
		boolean explicit = xs != null;
		if ((!series.isEmpty() && numeric != explicit) || (!explicit && labels.length == 0)) {
			throw new IllegalArgumentException("分类数据须先设置项目名称，且不能混用坐标模式。");
		}
		double[] values = explicit ? xs : indices(labels.length);
		Series result = checked(kind, name, values, columns, color, axis);
		series.add(result);
		numeric = explicit;
		if (explicit) {
			labels = new String[0];
		}
	}

	/**
	 * 创建从一开始的分类坐标。
	 *
	 * @param count 项目数。
	 * @return 分类坐标。
	 */
	static double[] indices(int count) {
		double[] xs = new double[count];
		for (int i = 0; i < count; i++) {
			xs[i] = i + 1;
		}
		return xs;
	}

	/**
	 * 取得指定数据组。
	 *
	 * @param index 从零开始的编号。
	 * @return 数据组。
	 */
	Series group(int index) {
		Checks.index(index, 0, series.size() - 1);
		return series.get(index);
	}

	/**
	 * 以按列拼接的数值替换数据，保留名称、颜色和样式。
	 *
	 * @param index 数据组编号。
	 * @param xs    横坐标。
	 * @param flat  按列拼接的数值。
	 */
	void replace(int index, double[] xs, double[] flat) {
		Series old = group(index);
		Checks.required(xs);
		Checks.required(flat);
		if (old.kind == PIE || old.kind == DONUT || old.kind == DIAL) {
			throw new IllegalArgumentException("圆图请使用对应的数据更新接口。");
		}
		int count = old.columns.length;
		if ((long) xs.length * count != flat.length || (!numeric && xs.length != labels.length)) {
			throw new IllegalArgumentException("数据长度不符，分类图的项目数不能单独改变。");
		}
		if (!numeric && !Arrays.equals(xs, indices(labels.length))) {
			throw new IllegalArgumentException("分类图横坐标须保持原索引。");
		}
		double[][] columns = new double[count][];
		for (int i = 0; i < count; i++) {
			columns[i] = Arrays.copyOfRange(flat, i * xs.length, (i + 1) * xs.length);
		}
		Series result = checked(old.kind, old.name, xs, columns, old.color, old.axis);
		result.style = old.style;
		series.set(index, result);
	}

	/**
	 * 追加数值坐标项目。
	 *
	 * @param index  数据组编号。
	 * @param x      新横坐标。
	 * @param values 此项目各列数值。
	 */
	void append(int index, double x, double[] values) {
		Series old = group(index);
		Checks.required(values);
		if (!numeric || values.length != old.columns.length) {
			throw new IllegalArgumentException("仅数值坐标可单独追加，各列数值须齐全。");
		}
		double[] xs = Arrays.copyOf(old.xs, old.xs.length + 1);
		xs[xs.length - 1] = x;
		double[] flat = new double[xs.length * values.length];
		for (int c = 0; c < values.length; c++) {
			System.arraycopy(old.columns[c], 0, flat, c * xs.length, old.xs.length);
			flat[(c + 1) * xs.length - 1] = values[c];
		}
		replace(index, xs, flat);
		group(index).missing = Arrays.copyOf(old.missing, xs.length);
	}

	/**
	 * 删除数值坐标项目，至少保留一个。
	 *
	 * @param index 数据组编号。
	 * @param item  项目编号。
	 */
	void remove(int index, int item) {
		Series old = group(index);
		Checks.index(item, 0, old.xs.length - 1);
		if (!numeric || old.xs.length == 1) {
			throw new IllegalArgumentException("仅数值坐标可单独删除，最后一项请删除数据组。");
		}
		int count = old.xs.length - 1;
		double[] xs = new double[count];
		double[] flat = new double[count * old.columns.length];
		for (int i = 0, target = 0; i < old.xs.length; i++) {
			if (i != item) {
				xs[target] = old.xs[i];
				for (int c = 0; c < old.columns.length; c++) {
					flat[c * count + target] = old.columns[c][i];
				}
				target++;
			}
		}
		replace(index, xs, flat);
		for (int i = 0, target = 0; i < old.missing.length; i++) {
			if (i != item) {
				group(index).missing[target++] = old.missing[i];
			}
		}
	}

	/**
	 * 添加饼图扇区或仪表指针，保存独立名称、颜色与视觉类型。
	 *
	 * @param kind   圆图类型。
	 * @param name   项目名称。
	 * @param value  项目值。
	 * @param color  项目颜色。
	 * @param needle 指针形状。
	 */
	void addRound(int kind, String name, double value, int color, int needle) {
		Checks.text(name);
		Checks.range(value, -1e99, 1e99);
		Checks.index(needle, 0, 1);
		if (kind == PIE && value < 0) {
			throw new IllegalArgumentException("扇区值不能为负数。");
		}
		Series old = series.isEmpty() ? null : series.get(0);
		int count = old == null ? 1 : old.xs.length + 1;
		double[] values = old == null ? new double[1] : Arrays.copyOf(old.columns[0], count);
		String[] names = old == null ? new String[1] : Arrays.copyOf(old.names, count);
		int[] colors = old == null ? new int[1] : Arrays.copyOf(old.colors, count);
		int[] types = old == null ? new int[1] : Arrays.copyOf(old.types, count);
		values[count - 1] = value;
		names[count - 1] = name;
		colors[count - 1] = color;
		types[count - 1] = needle;
		Series result = checked(kind, "", indices(count), new double[][] { values }, color, 0);
		result.names = names;
		result.colors = colors;
		result.types = types;
		if (old == null) {
			series.add(result);
		} else {
			result.style = old.style;
			series.set(0, result);
		}
	}

	/**
	 * 更新圆图单项，失败时保留原值。
	 *
	 * @param index 项目编号。
	 * @param name  新名称。
	 * @param value 新数值。
	 * @param color 新颜色。
	 */
	void updateRound(int index, String name, double value, int color) {
		Series data = group(0);
		Checks.index(index, 0, data.xs.length - 1);
		Checks.text(name);
		Checks.range(value, -1e99, 1e99);
		if (data.kind == PIE && value < 0) {
			throw new IllegalArgumentException("扇区值不能为负数。");
		}
		data.names[index] = name;
		data.columns[0][index] = value;
		data.colors[index] = color;
	}

	/**
	 * 添加具有名称和非负数值的圆环。
	 *
	 * @param name   圆环名称。
	 * @param names  扇区名称。
	 * @param values 扇区值。
	 */
	void addRing(String name, String[] names, double[] values) {
		String[] copy = Checks.names(names);
		Series result = checked(DONUT, name, indices(copy.length), new double[][] { values }, 0, 0);
		result.names = copy;
		scale(result);
		series.add(result);
	}

	/**
	 * 取得饼图归一化基准，拒绝非空全零数据。
	 *
	 * @param data 圆图数据组。
	 * @return 最大数值。
	 */
	static double scale(Series data) {
		double max = 0;
		for (double value : data.columns[0]) {
			max = Math.max(max, value);
		}
		if (max == 0) {
			throw new IllegalArgumentException("非空饼图或圆环至少需要一个正数。");
		}
		return max;
	}

	/**
	 * 保存已复制并完成校验的数据组。
	 */
	static final class Series {
		final int kind;
		String name;
		final double[] xs;
		final double[][] columns;
		final int axis;
		int color;
		String[] names = new String[0];
		int[] colors = new int[0];
		int[] types = new int[0];
		ACE数据组样式 style;
		boolean[] missing;

		/**
		 * 创建数据组，不再次复制已经校验的数组。
		 *
		 * @param kind    图形类型。
		 * @param name    数据组名称。
		 * @param xs      横坐标。
		 * @param columns 数值列。
		 * @param color   颜色。
		 * @param axis    数值轴。
		 */
		Series(int kind, String name, double[] xs, double[][] columns, int color, int axis) {
			this.kind = kind;
			this.name = name;
			this.xs = xs;
			this.missing = new boolean[xs.length];
			this.columns = columns;
			this.color = color;
			this.axis = axis;
		}
	}
}
