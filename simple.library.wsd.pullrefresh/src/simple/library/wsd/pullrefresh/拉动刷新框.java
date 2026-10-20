package simple.library.wsd.pullrefresh;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleEvent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.SimpleProperty;
import simple.runtime.components.面板;
import simple.runtime.errors.未初始化实例错误;

/**
 * 将普通子组件组成可滚动内容，通过上下拉手势启动应用定义的操作。
 *
 * <p>子组件直接放入本容器，使用继承的布局属性；具体业务由应用完成，
 * 操作结束后调用停止刷新。所有界面操作均在主线程执行。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface 拉动刷新框 extends 面板 {

	/* 组件事件 */

	/**
	 * 顶部下拉达到阈值并松手、进入刷新等待状态时触发；不是手指开始拉动。
	 */
	@SimpleEvent
	void 下拉刷新开始();

	/**
	 * 底部上拉达到阈值并松手、进入刷新等待状态时触发；与下拉刷新互斥。
	 */
	@SimpleEvent
	void 上拉刷新开始();

	/**
	 * 内容纵向滚动时触发，包括程序调用滚动方法。
	 *
	 * @param distance 当前纵向滚动位置，单位像素，顶部为 0。
	 * @param edge 是否已到顶部或底部；内容较短时两端重合。
	 */
	@SimpleEvent
	void 被滚动(int distance, boolean edge);

	/* 组件属性 */

	/**
	 * 取得顶部下拉开关。
	 *
	 * @return 是否允许顶部下拉操作。
	 */
	@SimpleProperty
	boolean 允许下拉();

	/**
	 * 设置顶部下拉开关；关闭时结束正在进行的下拉操作，不取消业务任务。
	 *
	 * @param enabled 是否启用，默认为真。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_BOOLEAN,
		initializer = "True"
	)
	void 允许下拉(boolean enabled);

	/**
	 * 取得底部上拉开关。
	 *
	 * @return 是否允许底部上拉操作。
	 */
	@SimpleProperty
	boolean 允许上拉();

	/**
	 * 设置底部上拉开关；关闭时结束正在进行的上拉操作，不取消业务任务。
	 *
	 * @param enabled 是否启用，默认为真。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_BOOLEAN,
		initializer = "True"
	)
	void 允许上拉(boolean enabled);

	/**
	 * 查询是否正在等待应用完成下拉操作。
	 *
	 * @return 是否正在等待下拉操作完成；与正在上拉刷新互斥。
	 */
	@SimpleProperty
	boolean 正在下拉刷新();

	/**
	 * 查询是否正在等待应用完成上拉操作。
	 *
	 * @return 是否正在等待上拉操作完成；与正在下拉刷新互斥。
	 */
	@SimpleProperty
	boolean 正在上拉刷新();

	/* 上下拉提示与辅助文本 */

	/**
	 * 取得下拉未达到触发阈值时的提示。
	 *
	 * @return 当前文本；空文本表示不显示对应文字。
	 */
	@SimpleProperty
	String 下拉提示文本();

	/**
	 * 设置下拉未达到触发阈值时的提示，立即更新当前提示。
	 *
	 * @param text 提示文本，允许空文本，不允许 null。
	 * @throws IllegalArgumentException 文本为 null 或未在主线程调用。
	 * @throws 未初始化实例错误 组件已销毁。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_STRING,
		initializer = "\"下拉刷新\""
	)
	void 下拉提示文本(String text);

	/**
	 * 取得下拉达到阈值、等待松手时的提示。
	 *
	 * @return 当前文本；空文本表示不显示对应文字。
	 */
	@SimpleProperty
	String 下拉释放文本();

	/**
	 * 设置下拉达到阈值、等待松手时的提示，立即更新当前提示。
	 *
	 * @param text 提示文本，允许空文本，不允许 null。
	 * @throws IllegalArgumentException 文本为 null 或未在主线程调用。
	 * @throws 未初始化实例错误 组件已销毁。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_STRING,
		initializer = "\"松开刷新\""
	)
	void 下拉释放文本(String text);

	/**
	 * 取得下拉松手后等待业务完成时的提示。
	 *
	 * @return 当前文本；空文本表示不显示对应文字。
	 */
	@SimpleProperty
	String 下拉处理文本();

	/**
	 * 设置下拉松手后等待业务完成时的提示，立即更新当前提示。
	 *
	 * @param text 提示文本，允许空文本，不允许 null。
	 * @throws IllegalArgumentException 文本为 null 或未在主线程调用。
	 * @throws 未初始化实例错误 组件已销毁。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_STRING,
		initializer = "\"正在刷新\""
	)
	void 下拉处理文本(String text);

	/**
	 * 取得下拉辅助文本；由应用维护，不自动生成时间或操作结果。
	 *
	 * @return 当前文本；空文本表示不显示对应文字。
	 */
	@SimpleProperty
	String 下拉辅助文本();

	/**
	 * 设置下拉辅助文本；不解释文本内容，立即更新当前提示。
	 *
	 * @param text 提示文本，允许空文本，不允许 null。
	 * @throws IllegalArgumentException 文本为 null 或未在主线程调用。
	 * @throws 未初始化实例错误 组件已销毁。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_STRING,
		initializer = "\"\""
	)
	void 下拉辅助文本(String text);

	/**
	 * 取得上拉未达到触发阈值时的提示。
	 *
	 * @return 当前文本；空文本表示不显示对应文字。
	 */
	@SimpleProperty
	String 上拉提示文本();

	/**
	 * 设置上拉未达到触发阈值时的提示，立即更新当前提示。
	 *
	 * @param text 提示文本，允许空文本，不允许 null。
	 * @throws IllegalArgumentException 文本为 null 或未在主线程调用。
	 * @throws 未初始化实例错误 组件已销毁。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_STRING,
		initializer = "\"上拉加载更多\""
	)
	void 上拉提示文本(String text);

	/**
	 * 取得上拉达到阈值、等待松手时的提示。
	 *
	 * @return 当前文本；空文本表示不显示对应文字。
	 */
	@SimpleProperty
	String 上拉释放文本();

	/**
	 * 设置上拉达到阈值、等待松手时的提示，立即更新当前提示。
	 *
	 * @param text 提示文本，允许空文本，不允许 null。
	 * @throws IllegalArgumentException 文本为 null 或未在主线程调用。
	 * @throws 未初始化实例错误 组件已销毁。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_STRING,
		initializer = "\"松开加载\""
	)
	void 上拉释放文本(String text);

	/**
	 * 取得上拉松手后等待业务完成时的提示。
	 *
	 * @return 当前文本；空文本表示不显示对应文字。
	 */
	@SimpleProperty
	String 上拉处理文本();

	/**
	 * 设置上拉松手后等待业务完成时的提示，立即更新当前提示。
	 *
	 * @param text 提示文本，允许空文本，不允许 null。
	 * @throws IllegalArgumentException 文本为 null 或未在主线程调用。
	 * @throws 未初始化实例错误 组件已销毁。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_STRING,
		initializer = "\"正在加载\""
	)
	void 上拉处理文本(String text);

	/**
	 * 取得上拉辅助文本；由应用维护，不自动生成时间或操作结果。
	 *
	 * @return 当前文本；空文本表示不显示对应文字。
	 */
	@SimpleProperty
	String 上拉辅助文本();

	/**
	 * 设置上拉辅助文本；不解释文本内容，立即更新当前提示。
	 *
	 * @param text 提示文本，允许空文本，不允许 null。
	 * @throws IllegalArgumentException 文本为 null 或未在主线程调用。
	 * @throws 未初始化实例错误 组件已销毁。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_STRING,
		initializer = "\"\""
	)
	void 上拉辅助文本(String text);

	/* 组件方法 */

	/**
	 * 结束当前方向的等待状态并收回提示；空闲时可重复调用，不触发开始事件。
	 *
	 * <p>不会取消应用的业务任务，也不自动修改辅助文本或记录更新时间。
	 *
	 * @throws 未初始化实例错误 组件已销毁。
	 * @throws IllegalArgumentException 未在主线程调用。
	 */
	@SimpleFunction
	void 停止刷新();

	/**
	 * 等待本轮布局完成后滚动到顶部，不触发下拉刷新。
	 *
	 * @throws 未初始化实例错误 组件已销毁。
	 * @throws IllegalArgumentException 未在主线程调用。
	 */
	@SimpleFunction
	void 滚动到顶部();

	/**
	 * 等待本轮布局完成后滚动到底部，不触发上拉加载。
	 *
	 * @throws 未初始化实例错误 组件已销毁。
	 * @throws IllegalArgumentException 未在主线程调用。
	 */
	@SimpleFunction
	void 滚动到底部();
}
