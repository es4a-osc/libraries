package simple.library.wsd.achartengine;

import simple.runtime.components.组件容器;

/**
 * ACE时间图的 Android 实现，共用校验、样式和视图管理。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class ACE时间图Impl extends ChartComponent implements ACE时间图 {

	/**
	 * 创建图表组件。
	 *
	 * @param container 所属组件容器。
	 */
	public ACE时间图Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected int chartKind() {
		return ChartModel.TIME;
	}

	/**
	 * 添加数据组并复制输入数组，构建或刷新后显示；时间横坐标使用整数毫秒时间戳。
	 *
	 * @param name   非空引用的名称，允许空文本。
	 * @param xs     严格递增的有限数值数组，不允许重复。
	 * @param values 与横坐标或分类名称等长的有限数值数组。
	 * @param color  ARGB 颜色。
	 * @param axis   0 左轴，1 右轴。
	 */
	@Override
	public void 添加数值组(String name, double[] xs, double[] values, int color, int axis) {
		addSeries(ChartModel.TIME, name, xs, new double[][] { values }, color, axis);
	}

	/**
	 * 设置时间轴格式，格式错误时拒绝修改，构建或刷新后生效。
	 *
	 * @param pattern SimpleDateFormat 格式，空文本自动选择；按设备时区显示。
	 * @param smart   是否按时间跨度生成时间刻度。
	 */
	@Override
	public void 设置时间格式(String pattern, boolean smart) {
		checkReady();
		Checks.text(pattern);
		if (!pattern.isEmpty()) {
			new java.text.SimpleDateFormat(pattern, java.util.Locale.getDefault());
		}
		datePattern = pattern;
		smartTime = smart;
	}
}
