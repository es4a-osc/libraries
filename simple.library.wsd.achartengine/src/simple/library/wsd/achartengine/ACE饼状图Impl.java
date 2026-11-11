package simple.library.wsd.achartengine;

import simple.runtime.components.组件容器;

/**
 * ACE饼状图的 Android 实现，共用校验、样式和视图管理。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class ACE饼状图Impl extends ChartComponent implements ACE饼状图 {

	/**
	 * 创建图表组件。
	 *
	 * @param container 所属组件容器。
	 */
	public ACE饼状图Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected int chartKind() {
		return ChartModel.PIE;
	}

	/**
	 * 添加一个扇区或指针；饼图值须非负。
	 *
	 * @param name  非空引用的名称，允许空文本。
	 * @param value 有限数值。
	 * @param color ARGB 颜色。
	 */
	@Override
	public void 添加数据(String name, double value, int color) {
		checkReady();
		model.addRound(ChartModel.PIE, name, value, color, 0);
		dirty = true;
	}

	/**
	 * 更新指定扇区或指针，构建或刷新后生效。
	 *
	 * @param index 从 0 开始的项目编号。
	 * @param name  非空引用的名称，允许空文本。
	 * @param value 有限数值。
	 * @param color ARGB 颜色。
	 */
	@Override
	public void 更新数据(int index, String name, double value, int color) {
		checkReady();
		model.updateRound(index, name, value, color);
		dirty = true;
	}

	/**
	 * 删除指定扇区或指针，构建或刷新后生效。
	 *
	 * @param index 从 0 开始的项目编号。
	 */
	@Override
	public void 删除数据(int index) {
		checkReady();
		removeRound(index);
	}
}
