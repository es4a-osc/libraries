package simple.library.wsd.mpandroidchart;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.SimpleProperty;

/**
 * 组合折线、柱状、散点、气泡和蜡烛数据，按添加顺序编号。
 *
 * <p>
 * 仅在界面线程操作；分类数据先设置项目名称，数值坐标数据直接添加；最后构建或刷新图表。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface MPAC组合图 extends MPAC坐标图表 {

	/* 图表属性 */

	/**
	 * 是否绘制平滑折线，重新构建后生效。
	 *
	 * @return 是否绘制平滑折线，重新构建后生效。
	 */
	@SimpleProperty
	boolean 平滑曲线();

	/**
	 * 是否绘制平滑折线，重新构建后生效。
	 *
	 * @param value 是否绘制平滑折线，重新构建后生效。
	 */
	@SimpleProperty(type = SimpleProperty.PROPERTY_TYPE_BOOLEAN, initializer = "False")
	void 平滑曲线(boolean value);

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
	void 添加折线组(String name, float[] values, int color, int axis);

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
	void 添加柱状组(String name, float[] values, int color, int axis);

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
	void 添加散点组(String name, float[] values, int color, int axis);

	/**
	 * 添加一组数据，复制输入数组；校验失败时不改变原数据，构建后显示。
	 *
	 * @param name   数据组名称。
	 * @param values 与项目名称数量相同的有限数值数组。
	 * @param sizes  与项目名称等长的非负气泡大小数组。
	 * @param color  数据组的 ARGB 颜色。
	 * @param axis   0 为左轴，1 为右轴；横向柱图表示下轴和上轴。
	 * @throws IllegalArgumentException 参数无效。
	 * @throws IllegalArgumentException 尚未初始化、组件已销毁或未在界面线程调用。
	 */
	@SimpleFunction
	void 添加气泡组(String name, float[] values, float[] sizes, int color, int axis);

	/**
	 * 添加一组开高低收数据。
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
	void 添加蜡烛组(String name, float[] open, float[] high, float[] low, float[] close, int color, int axis);

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
	void 添加数值折线组(String name, float[] xs, float[] values, int color, int axis);

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
	void 添加数值柱状组(String name, float[] xs, float[] values, int color, int axis);

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
	void 添加数值散点组(String name, float[] xs, float[] values, int color, int axis);

	/**
	 * 添加使用数值横坐标的数据组；不同组可有不同横坐标和项目数量，不能与分类坐标混用。
	 *
	 * @param name   非空引用的数据组名称。
	 * @param xs     非空有限横坐标数组，按非递减顺序排列。
	 * @param values 非空有限数值数组。
	 * @param sizes  与横坐标等长的非负有限大小数组。
	 * @param color  ARGB 颜色。
	 * @param axis   0 左轴，1 右轴。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	@SimpleFunction
	void 添加数值气泡组(String name, float[] xs, float[] values, float[] sizes, int color, int axis);

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
	void 添加数值蜡烛组(String name, float[] xs, float[] open, float[] high, float[] low, float[] close, int color, int axis);

	/**
	 * 设置组合图绘制层次，须包含五个类型且不重复；构建或刷新后生效。
	 *
	 * @param order 五个编号的排列：0 柱、1 气泡、2 折线、3 蜡烛、4 散点；先绘制的位于下方。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	@SimpleFunction
	void 设置绘制顺序(int[] order);
}
