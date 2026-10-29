package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleEvent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * XUI跑马灯组件。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI跑马灯 extends XUI视图组件 {

	/**
	 * 设置翻页时间间隔。
	 *
	 * @param interval 间隔毫秒。
	 */
	@SimpleFunction
	void 置刻度间隔(int interval);

	/**
	 * 设置动画执行时间。
	 *
	 * @param animDuration 动画时长。
	 */
	@SimpleFunction
	void 置动画时长(int animDuration);

	/**
	 * 设置跑马灯的入场和出场动画。
	 *
	 * @param animInId 入场动画资源 ID。
	 * @param animOutID 离场动画资源 ID。
	 */
	@SimpleFunction
	void 置进出场动画(int animInId, int animOutID);

	/**
	 * 替换跑马灯文本。
	 *
	 * @param items 项目文本数组；数组内不得含有空引用。
	 */
	@SimpleFunction
	void 置公告数组(String[] items);

	/**
	 * 开始跑马灯。
	 */
	@SimpleFunction
	void 开始公告();

	/**
	 * 停止跑马灯。
	 */
	@SimpleFunction
	void 停止公告();

	/**
	 * 公告条目被单击时触发。
	 *
	 * @param index 从 0 开始的项目索引。
	 * @param text 文本内容。
	 */
	@SimpleEvent
	void 公告被单击(int index, String text);

	/**
	 * 替换为包含标题、摘要和时间文字的复合公告，各数组长度须一致。
	 *
	 * @param titles 公告标题数组。
	 * @param descriptions 对应摘要数组。
	 * @param times 对应时间文字数组，不自动解释时间格式。
	 */
	@SimpleFunction
	void 置复合公告(String[] titles, String[] descriptions, String[] times);

	/**
	 * 复合公告被点击时返回各个内容字段。
	 *
	 * @param index 从 0 开始的公告索引。
	 * @param title 公告标题。
	 * @param description 公告摘要。
	 * @param time 时间文字。
	 */
	@SimpleEvent
	void 复合公告被单击(int index, String title, String description, String time);

}
