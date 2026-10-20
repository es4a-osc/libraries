package simple.library.wsd.mpandroidchart;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.SimpleComponent;

/**
 * 按分类展示开盘、最高、最低和收盘；上涨空心、下跌实心。
 *
 * <p>仅在界面线程操作；依次初始化、设置项目名称、添加数据并构建图表。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface 蜡烛图 extends 坐标图表 {

	/* 图表方法 */

	/**
	 * 添加一组开高低收数据，各数组长度须与项目名称相同。
	 *
	 * @param name 数据组名称。
	 * @param open 与项目名称等长；最低值不得高于开盘、收盘，最高值不得低于两者。
	 * @param high 与项目名称等长；最低值不得高于开盘、收盘，最高值不得低于两者。
	 * @param low 与项目名称等长；最低值不得高于开盘、收盘，最高值不得低于两者。
	 * @param close 与项目名称等长；最低值不得高于开盘、收盘，最高值不得低于两者。
	 * @param color 数据组的 ARGB 颜色。
	 * @param axis 0 为左轴，1 为右轴；横向柱图表示下轴和上轴。
	 * @throws IllegalArgumentException 参数无效。
	 * @throws IllegalStateException 尚未初始化、组件已销毁或未在界面线程调用。
	 */
	@SimpleFunction
	void 添加数据组(String name, float[] open, float[] high, float[] low, float[] close, int color, int axis);
}
