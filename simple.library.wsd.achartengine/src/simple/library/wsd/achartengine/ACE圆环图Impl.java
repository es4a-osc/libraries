package simple.library.wsd.achartengine;

import simple.runtime.components.组件容器;

/**
 * ACE圆环图的 Android 实现，共用校验、样式和视图管理。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class ACE圆环图Impl extends ChartComponent implements ACE圆环图 {

	/**
	 * 创建图表组件。
	 *
	 * @param container 所属组件容器。
	 */
	public ACE圆环图Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected int chartKind() {
		return ChartModel.DONUT;
	}

	/**
	 * 添加一层圆环，各层允许不同项目数，数值非负且至少一个正数。
	 *
	 * @param name   非空引用的名称，允许空文本。
	 * @param names  扇区名称数组。
	 * @param values 与横坐标或分类名称等长的有限数值数组。
	 */
	@Override
	public void 添加圆环(String name, String[] names, double[] values) {
		checkReady();
		model.addRing(name, names, values);
		dirty = true;
	}

	/**
	 * 设置各圆环共用的扇区配色，构建或刷新后生效。
	 *
	 * @param colors 按扇区索引循环使用的 ARGB 颜色，不能为空。
	 */
	@Override
	public void 设置颜色集(int[] colors) {
		checkReady();
		Checks.required(colors);
		if (colors.length == 0) {
			throw new IllegalArgumentException("颜色集不能为空。");
		}
		ringColors = colors.clone();
	}

	/**
	 * 原子替换一层圆环，校验失败保留原数据。
	 *
	 * @param group  从 0 开始的数据组编号；饼图和仪表盘固定为 0。
	 * @param names  新扇区名称数组。
	 * @param values 与横坐标或分类名称等长的有限数值数组。
	 */
	@Override
	public void 更新圆环(int group, String[] names, double[] values) {
		checkReady();
		model.group(group);
		ChartModel temporary = new ChartModel();
		temporary.addRing(model.group(group).name, names, values);
		ChartModel.Series updated = temporary.group(0);
		updated.style = model.group(group).style;
		model.series.set(group, updated);
		dirty = true;
	}
}
