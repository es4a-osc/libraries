package simple.library.wsd.achartengine;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * 基于 AChartEngine 的仪表盘；在界面线程初始化、添加数据并构建。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface ACE仪表盘 extends ACE图表 {

	/**
	 * 添加一个扇区或指针；饼图值须非负。
	 *
	 * @param name   非空引用的名称，允许空文本。
	 * @param value  有限数值。
	 * @param color  ARGB 颜色。
	 * @param needle 0 三角，1 箭头。
	 */
	@SimpleFunction
	void 添加数据(String name, double value, int color, int needle);

	/**
	 * 更新指定扇区或指针，构建或刷新后生效。
	 *
	 * @param index 从 0 开始的项目编号。
	 * @param name  非空引用的名称，允许空文本。
	 * @param value 有限数值。
	 * @param color ARGB 颜色。
	 */
	@SimpleFunction
	void 更新数据(int index, String name, double value, int color);

	/**
	 * 删除指定扇区或指针，构建或刷新后生效。
	 *
	 * @param index 从 0 开始的项目编号。
	 */
	@SimpleFunction
	void 删除数据(int index);

	/**
	 * 设置量程、刻度和角度，拒绝超过 2000 条的刻度循环，构建或刷新后生效。
	 *
	 * @param style 要复制的表盘配置。
	 */
	@SimpleFunction
	void 设置仪表样式(ACE仪表样式 style);
}
