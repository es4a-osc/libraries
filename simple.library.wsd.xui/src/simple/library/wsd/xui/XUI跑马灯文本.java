package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleEvent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * XUI跑马灯文本组件。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI跑马灯文本 extends XUI文本组件 {

	/**
	 * 开始滚动。
	 */
	@SimpleFunction
	void 开始滚动();

	/**
	 * 设置组件的宽度。
	 *
	 * @param viewWidth 视图宽度。
	 */
	@SimpleFunction
	void 置视图宽度(int viewWidth);

	/**
	 * 取得当前位置。
	 *
	 * @return 当前位置。
	 */
	@SimpleFunction
	float 取当前位置();

	/**
	 * 设置当前位置。
	 *
	 * @param coordinateX 文字滚动的横向位置，单位物理像素。
	 */
	@SimpleFunction
	void 置当前位置(float coordinateX);

	/**
	 * 取得滚动宽度。
	 *
	 * @return 滚动宽度。
	 */
	@SimpleFunction
	int 取滚动宽度();

	/**
	 * 设置滚动宽度。
	 *
	 * @param scrollWidth 滚动宽度。
	 */
	@SimpleFunction
	void 置滚动宽度(int scrollWidth);

	/**
	 * 取得速度。
	 *
	 * @return 速度。
	 */
	@SimpleFunction
	int 取速度();

	/**
	 * 设置滚动的速度。
	 *
	 * @param speed 速度。
	 */
	@SimpleFunction
	void 置速度(int speed);

	/**
	 * 是否在滚动。
	 *
	 * @return 是否在滚动。
	 */
	@SimpleFunction
	boolean 是否滚动中();

	/**
	 * 获取当前滚动集合的索引。
	 *
	 * @return 当前滚动集合的索引。
	 */
	@SimpleFunction
	int 取当前索引();

	/**
	 * 当前滚动字幕是否有需要滚动的消息。
	 *
	 * @return 当前滚动字幕是否有需要滚动的消息。
	 */
	@SimpleFunction
	boolean 是否含有显示消息();

	/**
	 * 清除内容。
	 */
	@SimpleFunction
	void 清空();

	/**
	 * 替换横向滚动公告。
	 *
	 * @param items 项目文本数组；数组内不得含有空引用。
	 */
	@SimpleFunction
	void 置公告数组(String[] items);

	/**
	 * 设置整组公告滚动完后是否继续下一轮。
	 *
	 * @param enabled 是否重复整组公告。
	 */
	@SimpleFunction
	void 置循环(boolean enabled);

	/**
	 * 取得当前公告数量。
	 *
	 * @return 当前公告数量。
	 */
	@SimpleFunction
	int 取公告数量();

	/**
	 * 在指定位置插入公告并从首条重新开始。
	 *
	 * @param index 0 至当前数量。
	 * @param text 非空公告文字。
	 */
	@SimpleFunction
	void 插入公告(int index, String text);

	/**
	 * 移除指定公告并从首条重新开始，空列表停止滚动。
	 *
	 * @param index 从零开始的公告位置。
	 */
	@SimpleFunction
	void 移除公告(int index);

	/**
	 * 一条公告开始滚动时触发。
	 *
	 * @param index 从零开始的位置。
	 * @param text 公告文字。
	 */
	@SimpleEvent
	void 滚动开始(int index, String text);

	/**
	 * 当前整组公告滚动完毕时触发。
	 */
	@SimpleEvent
	void 滚动结束();

	/**
	 * 按唯一标识新增或替换公告，截止后自动移除；零截止时间表示永久有效。
	 *
	 * @param identifier 非空唯一标识。
	 * @param text 非空公告文字。
	 * @param expiresAt Unix 毫秒时间戳，须为未来时间或零。
	 */
	@SimpleFunction
	void 追加定时公告(String identifier, String text, long expiresAt);

	/**
	 * 移除具有指定标识的公告。
	 *
	 * @param identifier 非空唯一标识。
	 * @return 移除具有指定标识的公告。
	 */
	@SimpleFunction
	boolean 按标识移除公告(String identifier);

	/**
	 * 取得指定公告的标识；普通文本公告返回空文本。
	 *
	 * @param index 从 0 开始的公告索引。
	 * @return 指定公告的标识；普通文本公告返回空文本。
	 */
	@SimpleFunction
	String 取公告标识(int index);

	/**
	 * 一条有效公告开始滚动时触发，普通文本公告的标识为空。
	 *
	 * @param identifier 公告唯一标识。
	 * @param index 当前从 0 开始的公告位置。
	 * @param text 实际显示的文字。
	 */
	@SimpleEvent
	void 公告开始(String identifier, int index, String text);

	/**
	 * 定时公告已经从数据和显示中移除后触发。
	 *
	 * @param identifier 过期公告的唯一标识。
	 */
	@SimpleEvent
	void 公告已过期(String identifier);

}
