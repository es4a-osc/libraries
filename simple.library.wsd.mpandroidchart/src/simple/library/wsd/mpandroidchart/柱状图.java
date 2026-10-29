package simple.library.wsd.mpandroidchart;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * 按分类展示多组并列柱状数据。
 *
 * <p>仅在界面线程操作；依次初始化、设置项目名称、添加数据并构建图表。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface 柱状图 extends 坐标图表 {

	/* 图表方法 */

	/**
	 * 添加一组数据，复制输入数组；校验失败时不改变原数据，构建后显示。
	 *
	 * @param name 数据组名称。
	 * @param values 与项目名称数量相同的有限数值数组。
	 * @param color 数据组的 ARGB 颜色。
	 * @param axis 0 为左轴，1 为右轴；横向柱图表示下轴和上轴。
	 * @throws IllegalArgumentException 参数无效。
	 * @throws IllegalStateException 尚未初始化、组件已销毁或未在界面线程调用。
	 */
	@SimpleFunction
	void 添加数据组(String name, float[] values, int color, int axis);
}
