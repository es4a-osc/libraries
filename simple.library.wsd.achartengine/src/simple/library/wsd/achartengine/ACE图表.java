package simple.library.wsd.achartengine;

import simple.runtime.annotations.SimpleEvent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.SimpleProperty;

/**
 * 图表共用配置、数据与交互接口；数据绝对值不超过 1e99，轴界限不超过 1e100。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public interface ACE图表 extends simple.runtime.components.可视组件 {

	/**
	 * 初始化基础样式，清空数据、轴范围和配置；后续操作须先初始化。
	 *
	 * @param title    图表标题，允许空文本。
	 * @param textSize 6 至 100 SP。
	 * @param color    ARGB 颜色。
	 */
	@SimpleFunction
	void 初始化图表(String title, float textSize, int color);

	/**
	 * 清空数据与图形，保留样式、轴标签和范围。
	 */
	@SimpleFunction
	void 清空数据();

	/**
	 * 重建图表并恢复设置的轴范围；无数据时清空图形。
	 */
	@SimpleFunction
	void 构建图表();

	/**
	 * 重建图表，保留当前缩放和平移范围。
	 */
	@SimpleFunction
	void 刷新图表();

	/**
	 * 保存样式副本，构建或刷新后生效。
	 *
	 * @param style 要复制的配置，不能为 null。
	 */
	@SimpleFunction
	void 设置图表样式(ACE图表样式 style);

	/**
	 * 保存数据组样式副本，构建或刷新后生效。
	 *
	 * @param group 从 0 开始的数据组编号；饼图和仪表盘固定为 0。
	 * @param style 要复制的数据组配置。
	 */
	@SimpleFunction
	void 设置数据组样式(int group, ACE数据组样式 style);

	/**
	 * 返回独立的数据组样式副本，未设置时返回默认配置。
	 *
	 * @param group 从 0 开始的数据组编号；饼图和仪表盘固定为 0。
	 * @return 返回独立的数据组样式副本，未设置时返回默认配置。
	 */
	@SimpleFunction
	ACE数据组样式 取数据组样式(int group);

	/**
	 * 返回数据组名称；饼图和仪表盘返回空文本。
	 *
	 * @param group 从 0 开始的数据组编号；饼图和仪表盘固定为 0。
	 * @return 返回数据组名称；饼图和仪表盘返回空文本。
	 */
	@SimpleFunction
	String 取数据组名称(int group);

	/**
	 * 修改数据组名称，构建或刷新后生效。
	 *
	 * @param group 从 0 开始的数据组编号；饼图和仪表盘固定为 0。
	 * @param name  非空引用的名称，允许空文本。
	 */
	@SimpleFunction
	void 设置数据组名称(int group, String name);

	/**
	 * 返回名称数组副本；数值坐标返回空数组。
	 *
	 * @param group 从 0 开始的数据组编号；饼图和仪表盘固定为 0。
	 * @return 返回名称数组副本；数值坐标返回空数组。
	 */
	@SimpleFunction
	String[] 取项目名称(int group);

	/**
	 * 返回横坐标副本，分类和圆图为从 1 开始的项目索引。
	 *
	 * @param group 从 0 开始的数据组编号；饼图和仪表盘固定为 0。
	 * @return 返回横坐标副本，分类和圆图为从 1 开始的项目索引。
	 */
	@SimpleFunction
	double[] 取横坐标(int group);

	/**
	 * 返回指定列的独立数值数组。
	 *
	 * @param group  从 0 开始的数据组编号；饼图和仪表盘固定为 0。
	 * @param column 普通图为 0；气泡 0 纵坐标、1 大小；区间柱 0 下限、1 上限。
	 * @return 返回指定列的独立数值数组。
	 */
	@SimpleFunction
	double[] 取数据组数值(int group, int column);

	/**
	 * 删除指定数据组；饼图或仪表盘删除全部项目，构建或刷新后生效。
	 *
	 * @param group 从 0 开始的数据组编号；饼图和仪表盘固定为 0。
	 */
	@SimpleFunction
	void 删除数据组(int group);

	/**
	 * 主动选择已构建项目，显示文字提示；上游没有程序化高亮效果。
	 *
	 * @param group  从 0 开始的数据组编号；饼图和仪表盘固定为 0。
	 * @param index  从 0 开始的项目编号。
	 * @param notify 是否派发项目被选中事件。
	 */
	@SimpleFunction
	void 选择项目(int group, int index, boolean notify);

	/**
	 * 取消当前选择和文字提示。
	 *
	 * @param notify 是否派发选择被取消事件。
	 */
	@SimpleFunction
	void 取消选择(boolean notify);

	/**
	 * 返回数据组和项目编号的副本；未选择或模型已修改时返回空数组。
	 *
	 * @return 返回数据组和项目编号的副本；未选择或模型已修改时返回空数组。
	 */
	@SimpleFunction
	int[] 取选中项目();

	/**
	 * 导出已完成布局的图表截图，返回压缩字节，无需存储权限。
	 *
	 * @param format  0 PNG，1 JPEG。
	 * @param quality 0 至 100；PNG 不使用质量参数。
	 * @return 导出已完成布局的图表截图，返回压缩字节，无需存储权限。
	 */
	@SimpleFunction
	byte[] 导出图片(int format, int quality);

	/**
	 * 调整已构建图表的显示比例；复位恢复构建时的初始范围。
	 */
	@SimpleFunction
	void 放大();

	/**
	 * 调整已构建图表的显示比例；复位恢复构建时的初始范围。
	 */
	@SimpleFunction
	void 缩小();

	/**
	 * 调整已构建图表的显示比例；复位恢复构建时的初始范围。
	 */
	@SimpleFunction
	void 重置视图();

	/**
	 * 取得当前模型中的数据组数量。
	 *
	 * @return 数据组数量
	 */
	@SimpleProperty
	int 数据组数量();

	/**
	 * 取得当前模型中的项目数量。
	 *
	 * @return 项目数量
	 */
	@SimpleProperty
	int 项目数量();

	/**
	 * 是否显示图例，同时受图表样式控制。
	 *
	 * @return 是否显示图例，同时受图表样式控制。
	 */
	@SimpleProperty
	boolean 显示图例();

	/**
	 * 是否显示图例，同时受图表样式控制。
	 *
	 * @param value 是否显示图例，同时受图表样式控制。
	 */
	@SimpleProperty(type = SimpleProperty.PROPERTY_TYPE_BOOLEAN, initializer = "True")
	void 显示图例(boolean value);

	/**
	 * 是否显示坐标图数值，同时受数据组样式控制。
	 *
	 * @return 是否显示坐标图数值，同时受数据组样式控制。
	 */
	@SimpleProperty
	boolean 显示数值();

	/**
	 * 是否显示坐标图数值，同时受数据组样式控制。
	 *
	 * @param value 是否显示坐标图数值，同时受数据组样式控制。
	 */
	@SimpleProperty(type = SimpleProperty.PROPERTY_TYPE_BOOLEAN, initializer = "True")
	void 显示数值(boolean value);

	/**
	 * 项目被主动选择或原生点选；编号从 0 开始，圆环和仪表盘仅支持主动选择。
	 *
	 * @param group 从 0 开始的数据组编号；饼图和仪表盘固定为 0。
	 * @param index 从 0 开始的项目编号。
	 * @param x     原模型横坐标。
	 * @param y     普通图为纵坐标，区间为上限，饼图及仪表盘为原始值。
	 */
	@SimpleEvent
	void 项目被选中(int group, int index, double x, double y);

	/**
	 * 选择被取消或点击空白位置。
	 */
	@SimpleEvent
	void 选择被取消();

	/**
	 * 手势平移已改变可见范围。
	 */
	@SimpleEvent
	void 图表被拖动();

	/**
	 * 原生缩放手势或按钮已改变图表范围。
	 *
	 * @param zoomIn True 放大，False 缩小。
	 * @param rate   本次缩放倍率。
	 */
	@SimpleEvent
	void 图表被缩放(boolean zoomIn, float rate);

	/**
	 * 原生复位已恢复图表初始范围。
	 */
	@SimpleEvent
	void 视图被重置();
}
