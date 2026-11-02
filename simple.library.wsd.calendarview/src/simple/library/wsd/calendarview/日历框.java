package simple.library.wsd.calendarview;

import simple.runtime.annotations.*;
import simple.runtime.parameters.BooleanReferenceParameter;
import simple.runtime.components.*;

/**
 * 年月周日历及选择、标记、绘制；仅在主线程操作，绘制和拦截回调内不能修改日历。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface 日历框 extends 可视组件 {

	/**
	 * 当前焦点日期快照；范围及多选模式的完整选择请读取相应日期数组，尚未选择时为空引用。
	 *
	 * @return 操作结果。
	 */
	@SimpleProperty
	日历日期 当前选择();

	/**
	 * 月视图当前显示的公历年。
	 *
	 * @return 操作结果。
	 */
	@SimpleProperty
	int 显示年();

	/**
	 * 月视图当前显示的公历月。
	 *
	 * @return 操作结果。
	 */
	@SimpleProperty
	int 显示月();

	/**
	 * 当前是否显示月视图；周视图为假。
	 *
	 * @return 操作结果。
	 */
	@SimpleProperty
	boolean 是月视图();

	/**
	 * 年份快速选择视图是否已打开。
	 *
	 * @return 操作结果。
	 */
	@SimpleProperty
	boolean 年视图已打开();

	/**
	 * 复制并应用日历配置；不重建日历实例。选择模式改变时清理旧模式的选择，范围内的日期及标记保持独立。
	 *
	 * @param options 配置。
	 */
	@SimpleFunction
	void 应用配置(日历配置 options);

	/**
	 * 取得当前配置副本，后续修改副本不改变现有日历。
	 *
	 * @return 操作结果。
	 */
	@SimpleFunction
	日历配置 取得配置();

	/**
	 * 设置包含首尾的公历日期范围；日期必须真实有效且最小不晚于最大。
	 *
	 * @param minimum 最小日期。
	 * @param maximum 最大日期。
	 */
	@SimpleFunction
	void 设置日期范围(日历日期 minimum, 日历日期 maximum);

	/**
	 * 读取允许浏览和选择的最小公历日期。
	 *
	 * @return 操作结果。
	 */
	@SimpleFunction
	日历日期 取最小日期();

	/**
	 * 读取允许浏览和选择的最大公历日期。
	 *
	 * @return 操作结果。
	 */
	@SimpleFunction
	日历日期 取最大日期();

	/**
	 * 跳转到指定日期；范围外或被拦截时返回假。默认自动选择模式不执行日期拦截。
	 *
	 * @param date 日期。
	 * @param smooth 平滑滚动。
	 * @param notify 触发选择事件。
	 * @return 操作结果。
	 */
	@SimpleFunction
	boolean 跳转日期(日历日期 date, boolean smooth, boolean notify);

	/**
	 * 跳转到今天；今天超出允许范围或被拦截时返回假。
	 *
	 * @param smooth 平滑滚动。
	 * @param notify 触发选择事件。
	 * @return 操作结果。
	 */
	@SimpleFunction
	boolean 返回今天(boolean smooth, boolean notify);

	/**
	 * 向前翻动当前月、周或年视图。
	 *
	 * @param smooth 平滑滚动。
	 */
	@SimpleFunction
	void 前一页(boolean smooth);

	/**
	 * 向后翻动当前月、周或年视图。
	 *
	 * @param smooth 平滑滚动。
	 */
	@SimpleFunction
	void 后一页(boolean smooth);

	/**
	 * 滚动回当前选中日期。
	 */
	@SimpleFunction
	void 滚动到选择();

	/**
	 * 打开指定年份的快速选月视图；年份须处于当前日期范围内。
	 *
	 * @param year 年份。
	 */
	@SimpleFunction
	void 打开年视图(int year);

	/**
	 * 关闭年份快速选择视图。
	 */
	@SimpleFunction
	void 关闭年视图();

	/**
	 * 读取当前月视图中的日期快照，包括该显示方式的填充日期。
	 *
	 * @return 操作结果。
	 */
	@SimpleFunction
	日历日期[] 取当前月日期();

	/**
	 * 读取当前周视图的七个日期快照。
	 *
	 * @return 操作结果。
	 */
	@SimpleFunction
	日历日期[] 取当前周日期();

	/**
	 * 按公历顺序读取全部多选日期。
	 *
	 * @return 操作结果。
	 */
	@SimpleFunction
	日历日期[] 取多选日期();

	/**
	 * 读取当前完整范围中未被拦截的日期；未完成范围选择时为空数组。
	 *
	 * @return 操作结果。
	 */
	@SimpleFunction
	日历日期[] 取范围日期();

	/**
	 * 按公历顺序读取全部标记日期及其独立标记副本。
	 *
	 * @return 操作结果。
	 */
	@SimpleFunction
	日历日期[] 取全部标记日期();

	/**
	 * 批量加入多选，要求当前为多选模式；整批先校验范围、拦截及数量，失败不部分写入。
	 *
	 * @param dates 日期数组。
	 */
	@SimpleFunction
	void 加入多选(日历日期[] dates);

	/**
	 * 移除指定多选日期，要求当前为多选模式。
	 *
	 * @param dates 日期数组。
	 */
	@SimpleFunction
	void 移除多选(日历日期[] dates);

	/**
	 * 清除当前单选日期。
	 */
	@SimpleFunction
	void 清除单选();

	/**
	 * 清除全部多选日期。
	 */
	@SimpleFunction
	void 清除多选();

	/**
	 * 清除已选择的范围及起点。
	 */
	@SimpleFunction
	void 清除范围();

	/**
	 * 设置包含首尾的选择范围，要求范围选择模式；先校验边界、端点拦截及跨度，失败返回假。 最小跨度为 -1 且首尾同日时只设置起点。
	 *
	 * @param start 起点。
	 * @param end 终点。
	 * @return 操作结果。
	 */
	@SimpleFunction
	boolean 设置范围选择(日历日期 start, 日历日期 end);

	/**
	 * 设置范围选择起点并清除原终点，要求范围选择模式。
	 *
	 * @param date 起点。
	 * @return 操作结果。
	 */
	@SimpleFunction
	boolean 设置范围起点(日历日期 date);

	/**
	 * 为已有起点设置终点；须满足日期顺序、端点拦截和跨度限制。
	 *
	 * @param date 终点。
	 * @return 操作结果。
	 */
	@SimpleFunction
	boolean 设置范围终点(日历日期 date);

	/**
	 * 以携带标记的日期数组替换全部标记；使用日历工具附加标记构造输入。
	 *
	 * @param dates 标记日期。
	 */
	@SimpleFunction
	void 设置全部标记(日历日期[] dates);

	/**
	 * 批量增加或替换指定日期的标记，其它日期保持不变。
	 *
	 * @param dates 标记日期。
	 */
	@SimpleFunction
	void 追加标记(日历日期[] dates);

	/**
	 * 移除指定日期的全部标记。
	 *
	 * @param date 日期。
	 */
	@SimpleFunction
	void 移除标记(日历日期 date);

	/**
	 * 移除全部日期标记。
	 */
	@SimpleFunction
	void 清除标记();

	/**
	 * 替换禁用日期集合；单选、范围及多选模式生效，默认自动选择模式不拦截。
	 *
	 * @param dates 日期数组。
	 */
	@SimpleFunction
	void 设置禁用日期(日历日期[] dates);

	/**
	 * 按公历顺序读取禁用日期的副本数组。
	 *
	 * @return 操作结果。
	 */
	@SimpleFunction
	日历日期[] 取禁用日期();

	/**
	 * 是否同步触发日期拦截事件；默认关闭。处理器只计算条件，不能在拦截回调中修改日历。
	 *
	 * @param enabled 启用。
	 */
	@SimpleFunction
	void 启用日期拦截(boolean enabled);

	/**
	 * 重新读取系统日期，更新今天的标识和相关视图。
	 */
	@SimpleFunction
	void 刷新今天();

	/**
	 * 重绘月、周、年视图及星期栏，不修改选择和标记。
	 */
	@SimpleFunction
	void 请求重绘();

	/**
	 * 预先载入供绘制事件使用的图片；支持资产、绝对路径及内容 URI，返回图片编号。
	 *
	 * @param path 图片路径。
	 * @return 操作结果。
	 */
	@SimpleFunction
	int 载入绘制图片(String path);

	/**
	 * 预先载入图片字节，压缩数据上限 16 MiB；图片采样到最长边 1024 像素。
	 *
	 * @param bytes 图片数据。
	 * @return 操作结果。
	 */
	@SimpleFunction
	int 载入绘制图片字节(byte[] bytes);

	/**
	 * 释放本日历预先载入的图片，重复释放无副作用。
	 *
	 * @param imageId 图片编号。
	 */
	@SimpleFunction
	void 释放绘制图片(int imageId);

	/**
	 * 单选或默认自动选择完成时触发；用户点击为假表示程序调用或自动切换。
	 *
	 * @param date 日期。
	 * @param isClick 用户点击。
	 */
	@SimpleEvent
	void 日期选中(日历日期 date, boolean isClick);

	/**
	 * 尝试选择超出当前允许范围的日期时触发。
	 *
	 * @param date 日期。
	 */
	@SimpleEvent
	void 日期越界(日历日期 date);

	/**
	 * 选择范围的起点或终点时触发。
	 *
	 * @param date 日期。
	 * @param isEnd 是否终点。
	 */
	@SimpleEvent
	void 范围选择(日历日期 date, boolean isEnd);

	/**
	 * 范围的包含首尾天数不符合限制时触发。
	 *
	 * @param date 日期。
	 * @param tooShort 小于最小跨度。
	 */
	@SimpleEvent
	void 跨度越界(日历日期 date, boolean tooShort);

	/**
	 * 用户或程序加入、移除多选后触发，携带操作后的数量与选择状态。
	 *
	 * @param date 日期。
	 * @param count 当前数量。
	 * @param maximum 最大数量。
	 * @param selected 当前已选中。
	 */
	@SimpleEvent
	void 多选改变(日历日期 date, int count, int maximum, boolean selected);

	/**
	 * 尝试加入超过多选数量上限的日期时触发。
	 *
	 * @param date 日期。
	 * @param maximum 最大数量。
	 */
	@SimpleEvent
	void 多选数量超限(日历日期 date, int maximum);

	/**
	 * 长按有效日期时触发，是否同时选择由配置决定。
	 *
	 * @param date 日期。
	 */
	@SimpleEvent
	void 日期长按(日历日期 date);

	/**
	 * 长按超出当前允许范围的日期时触发。
	 *
	 * @param date 日期。
	 */
	@SimpleEvent
	void 长按越界(日历日期 date);

	/**
	 * 启用日期拦截后同步查询是否禁用该日；该事件可能在绘制时频繁调用，只能计算条件。
	 *
	 * @param date 日期。
	 * @param blocked 禁止。
	 */
	@SimpleEvent
	void 日期拦截(日历日期 date, BooleanReferenceParameter blocked);

	/**
	 * 尝试选择被禁用的日期时触发。
	 *
	 * @param date 日期。
	 * @param isClick 用户点击。
	 */
	@SimpleEvent
	void 日期被拦截(日历日期 date, boolean isClick);

	/**
	 * 年份选择视图切换年份时触发。
	 *
	 * @param year 年份。
	 */
	@SimpleEvent
	void 年份改变(int year);

	/**
	 * 月视图切换月份时触发。
	 *
	 * @param year 年。
	 * @param month 月。
	 */
	@SimpleEvent
	void 月份改变(int year, int month);

	/**
	 * 周视图切换到其它周时触发。
	 *
	 * @param dates 本周日期。
	 */
	@SimpleEvent
	void 周改变(日历日期[] dates);

	/**
	 * 日历在月、周视图间转换时触发。
	 *
	 * @param monthView 是否月视图。
	 */
	@SimpleEvent
	void 视图改变(boolean monthView);

	/**
	 * 年份选择视图打开或关闭时触发。
	 *
	 * @param opened 是否打开。
	 */
	@SimpleEvent
	void 年视图改变(boolean opened);

	/**
	 * 点击日历内边距时触发，坐标单位 DIP；没有相邻日期时为空引用。
	 *
	 * @param x 横坐标。
	 * @param y 纵坐标。
	 * @param monthView 是否月视图。
	 * @param adjacent 相邻日期。
	 */
	@SimpleEvent
	void 边距被点击(float x, float y, boolean monthView, 日历日期 adjacent);

	/**
	 * 启用绘制事件后同步绘制日项；阶段 0 选区、1 标记、2 文本，3 年视图选区、4 年视图标记、5 年视图文本。设默认绘制为假可替代该阶段，绘图对象仅本回调有效。
	 *
	 * @param drawing 绘图。
	 * @param date 日期。
	 * @param stage 阶段。
	 * @param selected 已选中。
	 * @param previous 前项选中。
	 * @param next 后项选中。
	 * @param useDefault 默认绘制。
	 */
	@SimpleEvent
	void 绘制日项(日历绘图 drawing, 日历日期 date, int stage, boolean selected, boolean previous, boolean next, BooleanReferenceParameter useDefault);

	/**
	 * 启用绘制事件后同步绘制标题；阶段 0 年视图月份、1 年视图星期、2 顶部星期栏。星期为 0 周日至 6 周六；月份标题的星期为 -1。
	 *
	 * @param drawing 绘图。
	 * @param year 年。
	 * @param month 月。
	 * @param stage 阶段。
	 * @param week 星期。
	 * @param useDefault 默认绘制。
	 */
	@SimpleEvent
	void 绘制标题(日历绘图 drawing, int year, int month, int stage, int week, BooleanReferenceParameter useDefault);

	/**
	 * 绘制处理发生可传播的错误时报告原因；绘图对象仍会结束有效期。
	 *
	 * @param reason 原因。
	 */
	@SimpleEvent
	void 绘制失败(String reason);

	/**
	 * 在已打开的年视图中滚动到指定年份，年份必须位于允许范围内。
	 *
	 * @param year 年。
	 * @param smooth 平滑滚动。
	 * @return 年视图已打开且请求被接受时为真。
	 */
	@SimpleFunction
	boolean 定位年份(int year, boolean smooth);

	/**
	 * 读取年视图当前显示的年份，年视图未打开时返回 0。
	 *
	 * @return 当前年份或 0。
	 */
	@SimpleProperty
	int 年视图年份();

	/**
	 * 月／周页的实际滚动进度；页序号以当前允许范围从 0 开始，像素偏移为物理像素。
	 *
	 * @param monthView 月视图。
	 * @param position 页序号。
	 * @param offset 页间比例。
	 * @param pixels 像素偏移。
	 */
	@SimpleEvent
	void 翻页进度(boolean monthView, int position, float offset, int pixels);

	/**
	 * 月／周翻页状态：0 空闲、1 手指拖动、2 自动滚动。
	 *
	 * @param monthView 月视图。
	 * @param state 状态。
	 */
	@SimpleEvent
	void 翻页状态改变(boolean monthView, int state);

}
