package simple.library.wsd.achartengine;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * 基于 AChartEngine 的圆环图；在界面线程初始化、添加数据并构建。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface ACE圆环图 extends ACE图表 {

	/**
	 * 添加一层圆环，各层允许不同项目数，数值非负且至少一个正数。
	 *
	 * @param name   非空引用的名称，允许空文本。
	 * @param names  扇区名称数组。
	 * @param values 与横坐标或分类名称等长的有限数值数组。
	 */
	@SimpleFunction
	void 添加圆环(String name, String[] names, double[] values);

	/**
	 * 设置各圆环共用的扇区配色，构建或刷新后生效。
	 *
	 * @param colors 按扇区索引循环使用的 ARGB 颜色，不能为空。
	 */
	@SimpleFunction
	void 设置颜色集(int[] colors);

	/**
	 * 原子替换一层圆环，校验失败保留原数据。
	 *
	 * @param group  从 0 开始的数据组编号；饼图和仪表盘固定为 0。
	 * @param names  新扇区名称数组。
	 * @param values 与横坐标或分类名称等长的有限数值数组。
	 */
	@SimpleFunction
	void 更新圆环(int group, String[] names, double[] values);
}
