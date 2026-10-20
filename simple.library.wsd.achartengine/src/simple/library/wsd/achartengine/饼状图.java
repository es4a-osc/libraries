package simple.library.wsd.achartengine;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.components.可视组件;

/**
 * 展示各项目数值占比的饼状图。
 *
 * <p>在界面线程依次初始化、添加数据、构建图表；追加数据后可重新构建。
 * 数据必须为有限非负数，非空图表至少包含一个正数。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface 饼状图 extends 可视组件 {

	/* 图表方法 */

	/**
	 * 设置图表样式，清空已有数据和图形。
	 *
	 * @param title 图表标题。
	 * @param textSize 字体大小，单位 SP，必须为有限正数。
	 * @param backgroundColor 背景的 ARGB 颜色。
	 * @throws IllegalArgumentException 字体大小无效或标题为 null。
	 * @throws IllegalStateException 未在界面线程调用或组件已销毁。
	 */
	@SimpleFunction
	void 初始化图表(String title, float textSize, int backgroundColor);

	/**
	 * 添加一个项目，调用构建图表后显示。
	 *
	 * @param name 项目名称。
	 * @param value 可解析为有限非负数的数值文本。
	 * @param unit 数值单位，用于显示标签。
	 * @param color 该项目的 ARGB 颜色。
	 * @throws IllegalArgumentException 数值不是有限非负数或参数为 null。
	 * @throws IllegalStateException 尚未初始化、未在界面线程调用或组件已销毁。
	 */
	@SimpleFunction
	void 添加数据(String name, String value, String unit, int color);

	/**
	 * 按当前数据重建图表；没有数据时清空图形，重复构建不改变数据。
	 *
	 * @throws IllegalArgumentException 非空数据全部为零。
	 * @throws IllegalStateException 尚未初始化、未在界面线程调用或组件已销毁。
	 */
	@SimpleFunction
	void 构建图表();
}
