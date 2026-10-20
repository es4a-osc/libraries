package simple.library.wsd.achartengine;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.components.可视组件;

/**
 * 按添加顺序展示项目数值的柱状图，支持正数、零和负数。
 *
 * <p>在界面线程依次初始化、添加数据、构建图表；追加数据后可重新构建。
 * 横轴为项目名称，不将项目数值作为横坐标，不提供多组系列或时间轴。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface 柱状图 extends 可视组件 {

	/* 图表方法 */

	/**
	 * 设置图表样式，清空已有数据和图形。
	 *
	 * @param title 图表标题。
	 * @param xTitle 横轴标题。
	 * @param yTitle 纵轴标题。
	 * @param textSize 字体大小，单位 SP，必须为有限正数。
	 * @param backgroundColor 背景的 ARGB 颜色。
	 * @param itemColor 柱条的 ARGB 颜色。
	 * @throws IllegalArgumentException 字体大小无效或标题为 null。
	 * @throws IllegalStateException 未在界面线程调用或组件已销毁。
	 */
	@SimpleFunction
	void 初始化图表(String title, String xTitle, String yTitle, float textSize,
			int backgroundColor, int itemColor);

	/**
	 * 添加一个项目，调用构建图表后显示。
	 *
	 * @param name 项目名称。
	 * @param value 可解析为有限数值的文本。
	 * @param unit 数值单位，随项目名称显示。
	 * @throws IllegalArgumentException 数值无效或参数为 null。
	 * @throws IllegalStateException 尚未初始化、未在界面线程调用或组件已销毁。
	 */
	@SimpleFunction
	void 添加数据(String name, String value, String unit);

	/**
	 * 按当前数据重建图表；没有数据时清空图形，重复构建不改变数据或坐标范围。
	 *
	 * @throws IllegalArgumentException 数值范围过大，无法生成有限坐标。
	 * @throws IllegalStateException 尚未初始化、未在界面线程调用或组件已销毁。
	 */
	@SimpleFunction
	void 构建图表();
}
