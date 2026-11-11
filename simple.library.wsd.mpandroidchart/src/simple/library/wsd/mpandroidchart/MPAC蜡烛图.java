package simple.library.wsd.mpandroidchart;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * 按分类展示开盘、最高、最低和收盘；上涨空心、下跌实心。
 *
 * <p>
 * 仅在界面线程操作；分类数据先设置项目名称，数值坐标数据直接添加；最后构建或刷新图表。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface MPAC蜡烛图 extends MPAC坐标图表 {

	/* 图表方法 */

	/**
	 * 添加一组开高低收数据，各数组长度须与项目名称相同。
	 *
	 * @param name  数据组名称。
	 * @param open  与项目名称等长；最低值不得高于开盘、收盘，最高值不得低于两者。
	 * @param high  与项目名称等长；最低值不得高于开盘、收盘，最高值不得低于两者。
	 * @param low   与项目名称等长；最低值不得高于开盘、收盘，最高值不得低于两者。
	 * @param close 与项目名称等长；最低值不得高于开盘、收盘，最高值不得低于两者。
	 * @param color 数据组的 ARGB 颜色。
	 * @param axis  0 为左轴，1 为右轴；横向柱图表示下轴和上轴。
	 * @throws IllegalArgumentException 参数无效。
	 * @throws IllegalArgumentException 尚未初始化、组件已销毁或未在界面线程调用。
	 */
	@SimpleFunction
	void 添加数据组(String name, float[] open, float[] high, float[] low, float[] close, int color, int axis);

	/* 数值坐标与堆叠数据 */

	/**
	 * 添加使用数值横坐标的数据组；不同组可有不同横坐标和项目数量，不能与分类坐标混用。
	 *
	 * @param name  非空引用的数据组名称。
	 * @param xs    非空有限横坐标数组，按非递减顺序排列。
	 * @param open  与横坐标等长，最高不低于开收，最低不高于开收。
	 * @param high  与横坐标等长，最高不低于开收，最低不高于开收。
	 * @param low   与横坐标等长，最高不低于开收，最低不高于开收。
	 * @param close 与横坐标等长，最高不低于开收，最低不高于开收。
	 * @param color ARGB 颜色。
	 * @param axis  0 左轴，1 右轴。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	@SimpleFunction
	void 添加数值组(String name, float[] xs, float[] open, float[] high, float[] low, float[] close, int color, int axis);
}
