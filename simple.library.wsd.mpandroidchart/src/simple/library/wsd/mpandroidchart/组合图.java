package simple.library.wsd.mpandroidchart;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.SimpleProperty;

/**
 * 组合折线、柱状、散点、气泡和蜡烛数据，按添加顺序编号。
 *
 * <p>仅在界面线程操作；依次初始化、设置项目名称、添加数据并构建图表。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface 组合图 extends 坐标图表 {

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
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_BOOLEAN,
		initializer = "False"
	)
	void 平滑曲线(boolean value);

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
	void 添加折线组(String name, float[] values, int color, int axis);

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
	void 添加柱状组(String name, float[] values, int color, int axis);

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
	void 添加散点组(String name, float[] values, int color, int axis);

	/**
	 * 添加一组数据，复制输入数组；校验失败时不改变原数据，构建后显示。
	 *
	 * @param name 数据组名称。
	 * @param values 与项目名称数量相同的有限数值数组。
	 * @param sizes 与项目名称等长的非负气泡大小数组。
	 * @param color 数据组的 ARGB 颜色。
	 * @param axis 0 为左轴，1 为右轴；横向柱图表示下轴和上轴。
	 * @throws IllegalArgumentException 参数无效。
	 * @throws IllegalStateException 尚未初始化、组件已销毁或未在界面线程调用。
	 */
	@SimpleFunction
	void 添加气泡组(String name, float[] values, float[] sizes, int color, int axis);

	/**
	 * 添加一组开高低收数据。
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
	void 添加蜡烛组(String name, float[] open, float[] high, float[] low, float[] close, int color, int axis);
}
