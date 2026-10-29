package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleEvent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * XUI循环页面框组件。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI循环页面框 extends XUI视图组件 {

	/**
	 * 设置边界缓存。
	 *
	 * @param flag 是否启用边界缓存。
	 */
	@SimpleFunction
	void 置边界缓存(boolean flag);

	/**
	 * 替换循环页面文本，每页使用独立视图。
	 *
	 * @param items 按顺序显示的页面文本。
	 */
	@SimpleFunction
	void 置页面数组(String[] items);

	/**
	 * 切换到指定页面。
	 *
	 * @param index 从 0 开始的项目索引。
	 */
	@SimpleFunction
	void 选择页面(int index);

	/**
	 * 取得当前页面索引；没有页面时返回 -1。
	 *
	 * @return 当前页面索引；没有页面时返回 -1。
	 */
	@SimpleFunction
	int 取当前页面();

	/**
	 * 当前页面改变时触发。
	 *
	 * @param index 从 0 开始的项目索引。
	 */
	@SimpleEvent
	void 页面改变(int index);
}
