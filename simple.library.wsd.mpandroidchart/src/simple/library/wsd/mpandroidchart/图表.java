package simple.library.wsd.mpandroidchart;

import simple.runtime.annotations.SimpleEvent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.SimpleProperty;
import simple.runtime.components.可视组件;

/**
 * MPAndroidChart 图表共同的样式、构建流程和选择事件。
 *
 * <p>仅在界面线程操作；先初始化，再按各图表的数据接口添加数据并构建图表。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public interface 图表 extends 可视组件 {

	/* 图表事件 */

	/**
	 * 数据项目被选中；编号从零开始，饼图数据组编号为 0，蜡烛图返回收盘值。
	 *
	 * @param series 数据组添加顺序编号。
	 * @param item 项目索引；饼图为扇区索引。
	 * @param value 当前数值。
	 */
	@SimpleEvent
	void 项目被选中(int series, int item, float value);

	/**
	 * 用户取消当前高亮选择。
	 */
	@SimpleEvent
	void 选择被取消();

	/* 图表属性 */

	/**
	 * 已添加的数据组数量；饼图有扇区时为 1，否则为 0。
	 *
	 * @return 当前模型中的数据组数量，不要求已经构建图形。
	 * @throws IllegalStateException 组件已销毁或未在界面线程调用。
	 */
	@SimpleProperty
	int 数据组数量();

	/**
	 * 分类项目数量；饼图返回已经添加的扇区数量。
	 *
	 * @return 当前项目数量，清空数据后为 0。
	 * @throws IllegalStateException 组件已销毁或未在界面线程调用。
	 */
	@SimpleProperty
	int 项目数量();

	/**
	 * 构建动画时长，0 至 60000 毫秒，0 表示关闭。
	 *
	 * @return 构建动画时长，0 至 60000 毫秒，0 表示关闭。
	 */
	@SimpleProperty
	int 动画时间();

	/**
	 * 构建动画时长，0 至 60000 毫秒，0 表示关闭。
	 *
	 * @param value 构建动画时长，0 至 60000 毫秒，0 表示关闭。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_INTEGER,
		initializer = "0"
	)
	void 动画时间(int value);

	/**
	 * 是否显示数据组图例。
	 *
	 * @return 是否显示数据组图例。
	 */
	@SimpleProperty
	boolean 显示图例();

	/**
	 * 是否显示数据组图例。
	 *
	 * @param value 是否显示数据组图例。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_BOOLEAN,
		initializer = "True"
	)
	void 显示图例(boolean value);

	/**
	 * 是否显示图上数值；气泡标注大小，蜡烛标注最高值，过密时底层图表会减少标注。
	 *
	 * @return 是否显示图上数值；气泡标注大小，蜡烛标注最高值，过密时底层图表会减少标注。
	 */
	@SimpleProperty
	boolean 显示数值();

	/**
	 * 是否显示图上数值；气泡标注大小，蜡烛标注最高值，过密时底层图表会减少标注。
	 *
	 * @param value 是否显示图上数值；气泡标注大小，蜡烛标注最高值，过密时底层图表会减少标注。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_BOOLEAN,
		initializer = "True"
	)
	void 显示数值(boolean value);

	/* 图表方法 */

	/**
	 * 在界面线程初始化样式，清空数据和自定义轴范围；后续数据操作必须先初始化。
	 *
	 * @param title 图表标题，允许空文本。
	 * @param textSize 单位 SP，范围 6 至 24。
	 * @param backgroundColor ARGB 颜色。
	 * @throws IllegalArgumentException 参数无效。
	 * @throws IllegalStateException 尚未初始化、组件已销毁或未在界面线程调用。
	 */
	@SimpleFunction
	void 初始化图表(String title, float textSize, int backgroundColor);

	/**
	 * 清空名称、数据和图形，保留样式及自定义轴范围。
	 *
	 * @throws IllegalArgumentException 参数无效。
	 * @throws IllegalStateException 尚未初始化、组件已销毁或未在界面线程调用。
	 */
	@SimpleFunction
	void 清空数据();

	/**
	 * 按当前数据重新绘制；无数据时清空，饼图不能全部为零。
	 *
	 * @throws IllegalArgumentException 参数无效。
	 * @throws IllegalStateException 尚未初始化、组件已销毁或未在界面线程调用。
	 */
	@SimpleFunction
	void 构建图表();
}
