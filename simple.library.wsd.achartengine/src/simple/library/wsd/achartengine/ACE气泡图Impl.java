package simple.library.wsd.achartengine;

import simple.runtime.components.组件容器;

/**
 * ACE气泡图的 Android 实现，共用校验、样式和视图管理。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class ACE气泡图Impl extends ChartComponent implements ACE气泡图 {

	/**
	 * 创建图表组件。
	 *
	 * @param container 所属组件容器。
	 */
	public ACE气泡图Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected int chartKind() {
		return ChartModel.BUBBLE;
	}

	/**
	 * 添加数据组并复制输入数组，构建或刷新后显示；分类和数值坐标不能混用。
	 *
	 * @param name   非空引用的名称，允许空文本。
	 * @param values 与横坐标或分类名称等长的有限数值数组。
	 * @param sizes  与纵坐标等长的有限非负大小。
	 * @param color  ARGB 颜色。
	 * @param axis   0 左轴，1 右轴。
	 */
	@Override
	public void 添加数据组(String name, double[] values, double[] sizes, int color, int axis) {
		addSeries(ChartModel.BUBBLE, name, null, new double[][] { values, sizes }, color, axis);
	}

	/**
	 * 添加数据组并复制输入数组，构建或刷新后显示；分类和数值坐标不能混用。
	 *
	 * @param name   非空引用的名称，允许空文本。
	 * @param xs     严格递增的有限数值数组，不允许重复。
	 * @param values 与横坐标或分类名称等长的有限数值数组。
	 * @param sizes  与纵坐标等长的有限非负大小。
	 * @param color  ARGB 颜色。
	 * @param axis   0 左轴，1 右轴。
	 */
	@Override
	public void 添加数值组(String name, double[] xs, double[] values, double[] sizes, int color, int axis) {
		addSeries(ChartModel.BUBBLE, name, xs, new double[][] { values, sizes }, color, axis);
	}
}
