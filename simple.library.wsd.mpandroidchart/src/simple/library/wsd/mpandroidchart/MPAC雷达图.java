package simple.library.wsd.mpandroidchart;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * 至少三个分类项目，展示多组非负指标。
 *
 * <p>
 * 仅在界面线程操作；依次初始化、设置项目名称、添加数据并构建图表。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface MPAC雷达图 extends MPAC图表 {

	/* 图表方法 */

	/**
	 * 设置分类项目名称；横坐标按数组索引排列。
	 *
	 * @param names 按数据顺序排列的项目名称；添加数据前设置，更换前先清空。
	 * @throws IllegalArgumentException 参数无效。
	 * @throws IllegalArgumentException 尚未初始化、组件已销毁或未在界面线程调用。
	 */
	@SimpleFunction
	void 设置项目名称(String[] names);

	/**
	 * 添加一组数据，复制输入数组；校验失败时不改变原数据，构建后显示。
	 *
	 * @param name   数据组名称。
	 * @param values 与项目名称数量相同的有限数值数组。
	 * @param color  数据组的 ARGB 颜色。
	 * @throws IllegalArgumentException 参数无效。
	 * @throws IllegalArgumentException 尚未初始化、组件已销毁或未在界面线程调用。
	 */
	@SimpleFunction
	void 添加数据组(String name, float[] values, int color);

	/**
	 * 保存雷达网格配置副本，构建或刷新后生效。
	 *
	 * @param style 要复制的雷达配置。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	@SimpleFunction
	void 设置雷达样式(MPAC雷达样式 style);

	/**
	 * 保存雷达轴配置副本，构建或刷新后生效。
	 *
	 * @param axis  0 项目轴，1 数值轴。
	 * @param style 要复制的轴配置。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	@SimpleFunction
	void 设置雷达轴样式(int axis, MPAC轴样式 style);

	/**
	 * 设置雷达数值轴范围，构建或刷新后生效。
	 *
	 * @param min 非负有限最小值，必须小于最大值。
	 * @param max 有限最大值。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	@SimpleFunction
	void 设置数值范围(float min, float max);
}
