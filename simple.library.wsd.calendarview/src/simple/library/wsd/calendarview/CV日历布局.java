package simple.library.wsd.calendarview;

import simple.runtime.annotations.*;
import simple.runtime.parameters.BooleanReferenceParameter;
import simple.runtime.components.*;

/**
 * 接受一个日历框和一个 ViewGroup 内容组件（面板、滚动框、列表或网页），提供周月收缩联动。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface CV日历布局 extends 可视组件, 组件容器 {

	/**
	 * 读取日历是否已展开为月视图。
	 *
	 * @return 操作结果。
	 */
	@SimpleProperty
	boolean 已经展开();

	/**
	 * 读取是否显示上方日历。
	 *
	 * @return 操作结果。
	 */
	@SimpleProperty
	boolean 日历可见();

	/**
	 * 展开为月视图，动画时长单位毫秒；状态不允许、动画中或没有内容时返回假。
	 *
	 * @param duration 动画时长。
	 * @return 操作结果。
	 */
	@SimpleFunction
	boolean 展开(int duration);

	/**
	 * 收缩为周视图，动画时长单位毫秒；状态不允许、动画中或没有内容时返回假。
	 *
	 * @param duration 动画时长。
	 * @return 操作结果。
	 */
	@SimpleFunction
	boolean 收缩(int duration);

	/**
	 * 设置布局模式：0 周月均可，1 仅周，2 仅月。
	 *
	 * @param mode 模式。
	 */
	@SimpleFunction
	void 设置显示模式(int mode);

	/**
	 * 是否允许上下拖动日历与内容进行周月切换；不控制日历的左右翻页。
	 *
	 * @param enabled 启用。
	 */
	@SimpleFunction
	void 启用收缩手势(boolean enabled);

	/**
	 * 显示或隐藏日历部分，保留下方内容。
	 *
	 * @param visible 可见。
	 */
	@SimpleFunction
	void 显示日历(boolean visible);

	/**
	 * 读取布局内的日历组件；尚未添加时为空引用。
	 *
	 * @return 操作结果。
	 */
	@SimpleFunction
	CV日历框 取得日历();

	/**
	 * 读取布局内的内容组件；尚未添加时为空引用。
	 *
	 * @return 操作结果。
	 */
	@SimpleFunction
	可视组件 取得内容();

	/**
	 * 启用特殊内容的同步顶部判断；关闭时使用内容树的真实滚动状态。
	 *
	 * @param enabled 启用。
	 */
	@SimpleFunction
	void 启用自定义顶部判断(boolean enabled);

	/**
	 * 同步判断内容是否位于顶部；初始值为实际滚动检查结果，回调中禁止修改布局或日历。
	 *
	 * @param atTop 位于顶部。
	 */
	@SimpleEvent
	void 判断内容顶部(BooleanReferenceParameter atTop);

	/**
	 * 读取首次显示是否展开为月视图。
	 *
	 * @return 初始展开配置。
	 */
	@SimpleProperty
	boolean 初始展开();

	/**
	 * 在首次显示前配置周月联动的初始状态；设置后不改变布局显示模式。
	 *
	 * @param value 真为月视图，假为周视图。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_BOOLEAN,
		initializer = "True"
	)
	void 初始展开(boolean value);

}
