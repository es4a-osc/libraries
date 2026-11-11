package simple.library.wsd.mpandroidchart;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * 横向展示多组柱状数据；数值轴沿水平方向。
 *
 * <p>
 * 仅在界面线程操作；分类数据先设置项目名称，数值坐标数据直接添加；最后构建或刷新图表。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface MPAC横向柱状图 extends MPAC坐标图表 {

	/* 图表方法 */

	/**
	 * 添加一组数据，复制输入数组；校验失败时不改变原数据，构建后显示。
	 *
	 * @param name   数据组名称。
	 * @param values 与项目名称数量相同的有限数值数组。
	 * @param color  数据组的 ARGB 颜色。
	 * @param axis   0 为左轴，1 为右轴；横向柱图表示下轴和上轴。
	 * @throws IllegalArgumentException 参数无效。
	 * @throws IllegalArgumentException 尚未初始化、组件已销毁或未在界面线程调用。
	 */
	@SimpleFunction
	void 添加数据组(String name, float[] values, int color, int axis);

	/* 数值坐标与堆叠数据 */

	/**
	 * 添加使用数值横坐标的数据组；不同组可有不同横坐标和项目数量，不能与分类坐标混用。
	 *
	 * @param name   非空引用的数据组名称。
	 * @param xs     非空有限横坐标数组，按非递减顺序排列。
	 * @param values 非空有限数值数组。
	 * @param color  ARGB 颜色。
	 * @param axis   0 左轴，1 右轴。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	@SimpleFunction
	void 添加数值组(String name, float[] xs, float[] values, int color, int axis);

	/**
	 * 添加堆叠柱，支持正负层；须先设置项目名称。
	 *
	 * @param name   非空引用的数据组名称。
	 * @param values 非空有限数值数组。
	 * @param layers 各层数据按整层依次拼接，长度等于项目数乘层数。
	 * @param color  ARGB 颜色。
	 * @param axis   0 左轴，1 右轴。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	@SimpleFunction
	void 添加堆叠组(String name, float[] values, int layers, int color, int axis);

	/**
	 * 添加堆叠柱，支持正负层；使用数值横坐标。
	 *
	 * @param name   非空引用的数据组名称。
	 * @param xs     非空有限横坐标数组，按非递减顺序排列。
	 * @param values 非空有限数值数组。
	 * @param layers 各层数据按整层依次拼接，长度等于项目数乘层数。
	 * @param color  ARGB 颜色。
	 * @param axis   0 左轴，1 右轴。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	@SimpleFunction
	void 添加数值堆叠组(String name, float[] xs, float[] values, int layers, int color, int axis);
}
