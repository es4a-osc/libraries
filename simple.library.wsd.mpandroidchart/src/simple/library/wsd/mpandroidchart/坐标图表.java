package simple.library.wsd.mpandroidchart;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.SimpleProperty;

/**
 * 具有分类横轴和左右数值轴的图表公共接口。
 *
 * <p>仅在界面线程操作；依次初始化、设置项目名称、添加数据并构建图表。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public interface 坐标图表 extends 图表 {

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
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_BOOLEAN,
		initializer = "True"
	)
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
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_BOOLEAN,
		initializer = "True"
	)
	void 可否缩放(boolean value);

	/* 图表方法 */

	/**
	 * 设置分类项目名称；横坐标按数组索引排列。
	 *
	 * @param names 按数据顺序排列的项目名称；添加数据前设置，更换前先清空。
	 * @throws IllegalArgumentException 参数无效。
	 * @throws IllegalStateException 尚未初始化、组件已销毁或未在界面线程调用。
	 */
	@SimpleFunction
	void 设置项目名称(String[] names);

	/**
	 * 设置指定数值轴范围，重新构建后生效；初始化图表可恢复自动范围。
	 *
	 * @param axis 0 为左轴，1 为右轴；横向柱图表示下轴和上轴。
	 * @param min 有限数值，必须小于最大值。
	 * @param max 有限数值。
	 * @throws IllegalArgumentException 参数无效。
	 * @throws IllegalStateException 尚未初始化、组件已销毁或未在界面线程调用。
	 */
	@SimpleFunction
	void 设置纵轴范围(int axis, float min, float max);
}
