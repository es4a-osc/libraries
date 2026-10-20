package simple.library.wsd.achartengine;

import simple.runtime.components.组件容器;

/**
 * 柱状图的 Android 实现，共用图表数据和绘制处理。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class 柱状图Impl extends ChartComponent implements 柱状图 {

	/**
	 * 创建柱状图。
	 *
	 * @param container 所属可视容器。
	 */
	public 柱状图Impl(组件容器 container) {
		super(container);
	}

	/* 图表方法 */

	@Override
	public void 初始化图表(String title, String xTitle, String yTitle, float textSize,
			int backgroundColor, int itemColor) {
		initialize(title, xTitle, yTitle, textSize, backgroundColor, itemColor);
	}

	@Override
	public void 添加数据(String name, String value, String unit) {
		add(name, value, unit, 0, false);
	}

	@Override
	public void 构建图表() {
		build(BAR);
	}
}
