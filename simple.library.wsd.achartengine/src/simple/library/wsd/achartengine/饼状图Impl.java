package simple.library.wsd.achartengine;

import simple.runtime.components.组件容器;

/**
 * 饼状图的 Android 实现，共用图表数据和绘制处理。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class 饼状图Impl extends ChartComponent implements 饼状图 {

	/**
	 * 创建饼状图。
	 *
	 * @param container 所属可视容器。
	 */
	public 饼状图Impl(组件容器 container) {
		super(container);
	}

	/* 图表方法 */

	@Override
	public void 初始化图表(String title, float textSize, int backgroundColor) {
		initialize(title, "", "", textSize, backgroundColor, 0);
	}

	@Override
	public void 添加数据(String name, String value, String unit, int color) {
		add(name, value, unit, color, true);
	}

	@Override
	public void 构建图表() {
		build(PIE);
	}
}
