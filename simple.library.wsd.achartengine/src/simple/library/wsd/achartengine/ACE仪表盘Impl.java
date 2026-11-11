package simple.library.wsd.achartengine;

import simple.runtime.components.组件容器;

/**
 * ACE仪表盘的 Android 实现，共用校验、样式和视图管理。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class ACE仪表盘Impl extends ChartComponent implements ACE仪表盘 {

	/**
	 * 创建图表组件。
	 *
	 * @param container 所属组件容器。
	 */
	public ACE仪表盘Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected int chartKind() {
		return ChartModel.DIAL;
	}

	/**
	 * 添加一个扇区或指针；饼图值须非负。
	 *
	 * @param name   非空引用的名称，允许空文本。
	 * @param value  有限数值。
	 * @param color  ARGB 颜色。
	 * @param needle 0 三角，1 箭头。
	 */
	@Override
	public void 添加数据(String name, double value, int color, int needle) {
		checkReady();
		model.addRound(ChartModel.DIAL, name, value, color, needle);
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

	/**
	 * 设置量程、刻度和角度，拒绝超过 2000 条的刻度循环，构建或刷新后生效。
	 *
	 * @param style 要复制的表盘配置。
	 */
	@Override
	public void 设置仪表样式(ACE仪表样式 style) {
		checkReady();
		Checks.required(style);
		Checks.bounds(style.min, style.max);
		if (style.angleMin == style.angleMax || (style.max - style.min) / style.minor > 2000
				|| (style.max - style.min) / style.major > 2000 || style.min + style.minor == style.min
				|| style.min + style.major == style.min) {
			throw new IllegalArgumentException("仪表角度或刻度间距无效。");
		}
		dialStyle = style.复制();
	}
}
