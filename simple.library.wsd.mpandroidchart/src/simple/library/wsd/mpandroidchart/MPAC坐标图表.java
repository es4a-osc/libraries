package simple.library.wsd.mpandroidchart;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.SimpleProperty;

/**
 * 具有分类横轴和左右数值轴的图表公共接口。
 *
 * <p>
 * 仅在界面线程操作；分类数据先设置项目名称，数值坐标数据直接添加；最后构建或刷新图表。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public interface MPAC坐标图表 extends MPAC图表 {

	/* 图表属性 */

	/**
	 * 是否允许拖动坐标图。
	 *
	 * @return 是否允许拖动坐标图。
	 */
	@SimpleProperty
	boolean 可否拖动();

	/**
	 * 是否允许拖动坐标图。
	 *
	 * @param value 是否允许拖动坐标图。
	 */
	@SimpleProperty(type = SimpleProperty.PROPERTY_TYPE_BOOLEAN, initializer = "True")
	void 可否拖动(boolean value);

	/**
	 * 是否允许手势缩放和双击缩放。
	 *
	 * @return 是否允许手势缩放和双击缩放。
	 */
	@SimpleProperty
	boolean 可否缩放();

	/**
	 * 是否允许手势缩放和双击缩放。
	 *
	 * @param value 是否允许手势缩放和双击缩放。
	 */
	@SimpleProperty(type = SimpleProperty.PROPERTY_TYPE_BOOLEAN, initializer = "True")
	void 可否缩放(boolean value);

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
	 * 设置指定数值轴范围，重新构建后生效；初始化图表可恢复自动范围。
	 *
	 * @param axis 0 为左轴，1 为右轴；横向柱图表示下轴和上轴。
	 * @param min  有限数值，必须小于最大值。
	 * @param max  有限数值。
	 * @throws IllegalArgumentException 参数无效。
	 * @throws IllegalArgumentException 尚未初始化、组件已销毁或未在界面线程调用。
	 */
	@SimpleFunction
	void 设置纵轴范围(int axis, float min, float max);

	/* 完整配置与数据操作 */

	/**
	 * 保存轴配置副本，构建或刷新后生效。
	 *
	 * @param axis  0 分类或横轴，1 左轴，2 右轴；横向柱图数值轴位于下方和上方。
	 * @param style 要复制的轴配置。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	@SimpleFunction
	void 设置轴样式(int axis, MPAC轴样式 style);

	/**
	 * 固定横轴范围，构建或刷新后生效。
	 *
	 * @param min 有限数值，必须小于最大值。
	 * @param max 有限数值。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	@SimpleFunction
	void 设置横轴范围(float min, float max);

	/**
	 * 恢复指定坐标轴的自动范围，构建或刷新后生效。
	 *
	 * @param axis 0 横轴，1 左轴，2 右轴。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	@SimpleFunction
	void 恢复自动范围(int axis);

	/**
	 * 以内容中心缩放已构建图表。
	 *
	 * @param x 相对当前视图的倍数，必须大于零。
	 * @param y 相对当前视图的倍数，必须大于零。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	@SimpleFunction
	void 缩放视图(float x, float y);

	/**
	 * 移动已构建图表的可见区域。
	 *
	 * @param x    视图左边缘横坐标。
	 * @param y    视图中心纵坐标。
	 * @param axis 0 左轴，1 右轴。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	@SimpleFunction
	void 移动视图(float x, float y, int axis);

	/**
	 * 限制已构建图表的可见数据跨度。
	 *
	 * @param min 必须大于零，且不大于最大跨度。
	 * @param max 可见区域的数据跨度。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	@SimpleFunction
	void 设置可见横轴范围(float min, float max);

	/**
	 * 限制已构建图表的可见数据跨度。
	 *
	 * @param min  必须大于零，且不大于最大跨度。
	 * @param max  可见区域的数据跨度。
	 * @param axis 0 左轴，1 右轴。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	@SimpleFunction
	void 设置可见纵轴范围(float min, float max, int axis);

	/**
	 * 恢复显示全部数据，清除可见跨度限制。
	 *
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	@SimpleFunction
	void 重置视图();

	/**
	 * 返回可见横轴最小值、最大值、横向缩放倍数、纵向缩放倍数。
	 *
	 * @return 返回可见横轴最小值、最大值、横向缩放倍数、纵向缩放倍数。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	@SimpleFunction
	float[] 取视图范围();

	/**
	 * 转换已布局图表的坐标，返回横纵坐标数组；像素坐标单位为 DIP，不含标题。
	 *
	 * @param x    数据横坐标。
	 * @param y    数据纵坐标。
	 * @param axis 0 左轴，1 右轴。
	 * @return 转换已布局图表的坐标，返回横纵坐标数组；像素坐标单位为 DIP，不含标题。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	@SimpleFunction
	float[] 数据坐标转像素(float x, float y, int axis);

	/**
	 * 转换已布局图表的坐标，返回横纵坐标数组；像素坐标单位为 DIP，不含标题。
	 *
	 * @param x    相对原生图表左上角的 DIP 横坐标，不含标题。
	 * @param y    相对原生图表左上角的 DIP 纵坐标，不含标题。
	 * @param axis 0 左轴，1 右轴。
	 * @return 转换已布局图表的坐标，返回横纵坐标数组；像素坐标单位为 DIP，不含标题。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	@SimpleFunction
	float[] 像素转数据坐标(float x, float y, int axis);

	/**
	 * 设置柱宽和并排方式，数值横坐标保持原值，构建或刷新后生效。
	 *
	 * @param width   同一横坐标下全部柱的总宽，数据单位，必须大于零。
	 * @param gap     分组柱之间的空白，数据单位，不能为负数。
	 * @param grouped True 并排，False 重叠；仅柱图和含柱的组合图有效。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	@SimpleFunction
	void 设置柱布局(float width, float gap, boolean grouped);

	/**
	 * 主动选择已构建的堆叠柱中的一层。
	 *
	 * @param group  从 0 开始的数据组编号。
	 * @param index  从 0 开始的项目编号。
	 * @param layer  从 0 开始的层编号。
	 * @param notify 是否触发项目被选中事件。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	@SimpleFunction
	void 选择堆叠层(int group, int index, int layer, boolean notify);
}
