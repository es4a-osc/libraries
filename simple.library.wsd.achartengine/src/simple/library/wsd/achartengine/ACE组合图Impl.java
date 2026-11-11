package simple.library.wsd.achartengine;

import simple.runtime.components.组件容器;

/**
 * ACE组合图的 Android 实现，共用校验、样式和视图管理。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class ACE组合图Impl extends ChartComponent implements ACE组合图 {

	/**
	 * 创建图表组件。
	 *
	 * @param container 所属组件容器。
	 */
	public ACE组合图Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected int chartKind() {
		return ChartModel.COMBINED;
	}

	/**
	 * 添加组合图的数据组，保持添加顺序和左右轴归属。
	 *
	 * @param kind   0 折线，1 平滑曲线，2 柱，3 散点，4 气泡，5 区间柱，6 时间折线。
	 * @param name   非空引用的名称，允许空文本。
	 * @param xs     严格递增的有限数值数组，不允许重复。
	 * @param values 普通图传纵坐标；气泡按整列拼接纵坐标与大小，区间按整列拼接下限与上限。
	 * @param color  ARGB 颜色。
	 * @param axis   0 左轴，1 右轴。
	 */
	@Override
	public void 添加数据组(int kind, String name, double[] xs, double[] values, int color, int axis) {
		checkReady();
		Checks.index(kind, 0, 6);
		int type = new int[] { ChartModel.LINE, ChartModel.CUBIC, ChartModel.BAR, ChartModel.SCATTER, ChartModel.BUBBLE,
				ChartModel.RANGE, ChartModel.TIME }[kind];
		Checks.required(xs);
		Checks.required(values);
		int count = type == ChartModel.BUBBLE || type == ChartModel.RANGE ? 2 : 1;
		if ((long) xs.length * count != values.length) {
			throw new IllegalArgumentException("组合数据列长度不符。");
		}
		double[][] columns = new double[count][];
		for (int c = 0; c < count; c++) {
			columns[c] = java.util.Arrays.copyOfRange(values, c * xs.length, (c + 1) * xs.length);
		}
		addSeries(type, name, xs, columns, color, axis);
	}
}
