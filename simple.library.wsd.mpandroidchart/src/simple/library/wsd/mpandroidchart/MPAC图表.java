package simple.library.wsd.mpandroidchart;

import simple.runtime.annotations.SimpleEvent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.SimpleProperty;
import simple.runtime.components.可视组件;

/**
 * MPAndroidChart 图表共同的样式、构建流程和选择事件。
 *
 * <p>
 * 仅在界面线程操作；先初始化，再按各图表的数据接口添加数据并构建图表。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public interface MPAC图表 extends 可视组件 {

	/* 图表事件 */

	/**
	 * 数据项目被选中；编号从零开始，饼图组号为 0，蜡烛返回收盘值，堆叠柱返回各层代数和。
	 *
	 * @param series 数据组添加顺序编号。
	 * @param item   项目索引；饼图为扇区索引。
	 * @param value  纵坐标数值；蜡烛为收盘值，堆叠柱为各层代数和，具体层可用取选中项目和取数据组数值读取。
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
	 * @throws IllegalArgumentException 组件已销毁或未在界面线程调用。
	 */
	@SimpleProperty
	int 数据组数量();

	/**
	 * 各数据组最大的项目数量；饼图返回已经添加的扇区数量。
	 *
	 * @return 当前项目数量，清空数据后为 0。
	 * @throws IllegalArgumentException 组件已销毁或未在界面线程调用。
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
	@SimpleProperty(type = SimpleProperty.PROPERTY_TYPE_INTEGER, initializer = "0")
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
	@SimpleProperty(type = SimpleProperty.PROPERTY_TYPE_BOOLEAN, initializer = "True")
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
	@SimpleProperty(type = SimpleProperty.PROPERTY_TYPE_BOOLEAN, initializer = "True")
	void 显示数值(boolean value);

	/* 图表方法 */

	/**
	 * 在界面线程初始化样式，清空数据和自定义轴范围；后续数据操作必须先初始化。
	 *
	 * @param title           图表标题，允许空文本。
	 * @param textSize        单位 SP，范围 6 至 24。
	 * @param backgroundColor ARGB 颜色。
	 * @throws IllegalArgumentException 参数无效。
	 * @throws IllegalArgumentException 尚未初始化、组件已销毁或未在界面线程调用。
	 */
	@SimpleFunction
	void 初始化图表(String title, float textSize, int backgroundColor);

	/**
	 * 清空名称、数据和图形，保留样式及自定义轴范围。
	 *
	 * @throws IllegalArgumentException 参数无效。
	 * @throws IllegalArgumentException 尚未初始化、组件已销毁或未在界面线程调用。
	 */
	@SimpleFunction
	void 清空数据();

	/**
	 * 按当前数据重新绘制；无数据时清空，饼图不能全部为零。
	 *
	 * @throws IllegalArgumentException 参数无效。
	 * @throws IllegalArgumentException 尚未初始化、组件已销毁或未在界面线程调用。
	 */
	@SimpleFunction
	void 构建图表();

	/* 完整配置与数据操作 */

	/**
	 * 重新绘制当前数据和配置，保留当前缩放和平移，不播放构建动画。
	 *
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	@SimpleFunction
	void 刷新图表();

	/**
	 * 保存配置副本，构建或刷新后生效。
	 *
	 * @param style 要复制的配置；不能为 null。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	@SimpleFunction
	void 设置图表样式(MPAC图表样式 style);

	/**
	 * 保存配置副本，构建或刷新后生效。
	 *
	 * @param style 要复制的配置；不能为 null。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	@SimpleFunction
	void 设置图例样式(MPAC图例样式 style);

	/**
	 * 保存数据组样式副本，构建或刷新后生效。
	 *
	 * @param group 从 0 开始的数据组编号；饼图固定为 0。
	 * @param style 要复制的配置；不能为 null。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	@SimpleFunction
	void 设置数据组样式(int group, MPAC数据组样式 style);

	/**
	 * 返回数据组配置的独立副本；尚未设置时返回默认配置。
	 *
	 * @param group 从 0 开始的数据组编号；饼图固定为 0。
	 * @return 返回数据组配置的独立副本；尚未设置时返回默认配置。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	@SimpleFunction
	MPAC数据组样式 取数据组样式(int group);

	/**
	 * 取得数据组名称；饼图返回空文本，扇区名称由取项目名称取得。
	 *
	 * @param group 从 0 开始的数据组编号；饼图固定为 0。
	 * @return 取得数据组名称；饼图返回空文本，扇区名称由取项目名称取得。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	@SimpleFunction
	String 取数据组名称(int group);

	/**
	 * 取得分类名称副本；数值横坐标返回空数组，饼图返回扇区名称。
	 *
	 * @return 取得分类名称副本；数值横坐标返回空数组，饼图返回扇区名称。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	@SimpleFunction
	String[] 取项目名称();

	/**
	 * 取得数据组横坐标副本；分类图和饼图返回项目索引。
	 *
	 * @param group 从 0 开始的数据组编号；饼图固定为 0。
	 * @return 取得数据组横坐标副本；分类图和饼图返回项目索引。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	@SimpleFunction
	float[] 取横坐标(int group);

	/**
	 * 取得指定列的数值副本。
	 *
	 * @param group  从 0 开始的数据组编号；饼图固定为 0。
	 * @param column 普通图和饼图为 0；气泡 0 数值、1 大小；蜡烛 0 开、1 高、2 低、3 收；堆叠为层号。
	 * @return 取得指定列的数值副本。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	@SimpleFunction
	float[] 取数据组数值(int group, int column);

	/**
	 * 修改数据组名称、原颜色和轴归属，构建或刷新后生效；饼图请使用更新扇区。
	 *
	 * @param group 从 0 开始的数据组编号；饼图固定为 0。
	 * @param name  非空引用的数据组名称。
	 * @param color ARGB 颜色。
	 * @param axis  0 左轴，1 右轴。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	@SimpleFunction
	void 设置数据组信息(int group, String name, int color, int axis);

	/**
	 * 替换整组数据并保留样式；分类图横坐标必须仍为原索引，数值图可改变项目数量；饼图请使用更新扇区。
	 *
	 * @param group   从 0 开始的数据组编号；饼图固定为 0。
	 * @param xs      非空有限横坐标数组，按非递减顺序排列。
	 * @param values  非空有限数值数组。
	 * @param columns 列数必须与原数据组一致。数值按整列依次拼接，例如气泡先全部数值再全部大小。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	@SimpleFunction
	void 更新数据组(int group, float[] xs, float[] values, int columns);

	/**
	 * 向数值横坐标数据组追加一个项目，构建或刷新后生效；分类图请替换整组数据。
	 *
	 * @param group  从 0 开始的数据组编号；饼图固定为 0。
	 * @param x      不小于本组最后横坐标的有限数值。
	 * @param values 此项目的各列数值，顺序与更新数据组一致。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	@SimpleFunction
	void 追加项目(int group, float x, float[] values);

	/**
	 * 删除数值横坐标数据组的一个项目，至少保留一个；构建或刷新后生效。
	 *
	 * @param group 从 0 开始的数据组编号；饼图固定为 0。
	 * @param index 从 0 开始的项目编号。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	@SimpleFunction
	void 删除项目(int group, int index);

	/**
	 * 删除整组数据，构建或刷新后生效；饼图删除全部扇区。
	 *
	 * @param group 从 0 开始的数据组编号；饼图固定为 0。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	@SimpleFunction
	void 删除数据组(int group);

	/**
	 * 主动选择已构建的项目；数据变更后须先构建或刷新，重复横坐标按项目编号精确选择。
	 *
	 * @param group  从 0 开始的数据组编号；饼图固定为 0。
	 * @param index  从 0 开始的项目编号。
	 * @param notify 是否触发项目被选中事件。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	@SimpleFunction
	void 选择项目(int group, int index, boolean notify);

	/**
	 * 取消当前高亮。
	 *
	 * @param notify 是否触发选择被取消事件。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	@SimpleFunction
	void 取消选择(boolean notify);

	/**
	 * 返回数据组、项目、堆叠层三个编号；未选择时返回空数组，非堆叠层为 -1。
	 *
	 * @return 返回数据组、项目、堆叠层三个编号；未选择时返回空数组，非堆叠层为 -1。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	@SimpleFunction
	int[] 取选中项目();

	/**
	 * 导出包含标题的当前组件截图；需已完成布局，返回压缩字节，不写文件，不需要存储权限。
	 *
	 * @param format  0 PNG，1 JPEG。
	 * @param quality 0 至 100；PNG 不使用质量参数。
	 * @return 导出包含标题的当前组件截图；需已完成布局，返回压缩字节，不写文件，不需要存储权限。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	@SimpleFunction
	byte[] 导出图片(int format, int quality);

	/**
	 * 设置构建动画的坐标方向及缓动方式，时长由动画时间属性控制。
	 *
	 * @param axes   0 双轴，1 横轴，2 纵轴。
	 * @param easing 0 至 27：0 Linear, 1 EaseInQuad, 2 EaseOutQuad, 3 EaseInOutQuad, 4 EaseInCubic, 5
	 *               EaseOutCubic, 6 EaseInOutCubic, 7 EaseInQuart, 8 EaseOutQuart, 9 EaseInOutQuart, 10
	 *               EaseInSine, 11 EaseOutSine, 12 EaseInOutSine, 13 EaseInExpo, 14 EaseOutExpo, 15
	 *               EaseInOutExpo, 16 EaseInCirc, 17 EaseOutCirc, 18 EaseInOutCirc, 19 EaseInElastic,
	 *               20 EaseOutElastic, 21 EaseInOutElastic, 22 EaseInBack, 23 EaseOutBack, 24
	 *               EaseInOutBack, 25 EaseInBounce, 26 EaseOutBounce, 27 EaseInOutBounce。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	@SimpleFunction
	void 设置动画(int axes, int easing);

	/**
	 * 停止当前构建动画并显示完整数据。
	 *
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	@SimpleFunction
	void 停止动画();

	/**
	 * 响应图表手势。
	 *
	 * @param gesture 0 无，1 拖动，2 横向缩放，3 纵向缩放，4 双轴缩放，5 旋转，6 单击，7 双击，8 长按，9 惯性。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	@SimpleEvent
	void 手势开始(int gesture);

	/**
	 * 响应图表手势。
	 *
	 * @param gesture 与手势开始事件相同。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	@SimpleEvent
	void 手势结束(int gesture);

	/**
	 * 响应图表手势。
	 *
	 * @param x 相对图表内容视图的 DIP 横坐标。
	 * @param y 相对图表内容视图的 DIP 纵坐标。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	@SimpleEvent
	void 图表单击(float x, float y);

	/**
	 * 响应图表手势。
	 *
	 * @param x DIP 横坐标。
	 * @param y DIP 纵坐标。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	@SimpleEvent
	void 图表双击(float x, float y);

	/**
	 * 响应图表手势。
	 *
	 * @param x DIP 横坐标。
	 * @param y DIP 纵坐标。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	@SimpleEvent
	void 图表长按(float x, float y);

	/**
	 * 响应图表手势。
	 *
	 * @param x 本次 DIP 位移。
	 * @param y 本次 DIP 位移。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	@SimpleEvent
	void 图表拖动(float x, float y);

	/**
	 * 响应图表手势。
	 *
	 * @param x 本次缩放倍数。
	 * @param y 本次缩放倍数。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	@SimpleEvent
	void 图表缩放(float x, float y);

	/**
	 * 响应图表手势。
	 *
	 * @param x DIP 每秒。
	 * @param y DIP 每秒。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	@SimpleEvent
	void 图表惯性(float x, float y);
}
