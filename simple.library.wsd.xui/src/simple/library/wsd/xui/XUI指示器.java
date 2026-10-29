package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleEvent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.components.可视组件;

/**
 * XUI指示器组件。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI指示器 extends XUI视图组件 {

	/**
	 * 设置tab的标题。
	 *
	 * @param tabTitles tab的标题。
	 */
	@SimpleFunction
	void 置标签标题数组(String[] tabTitles);

	/**
	 * 把标签切换与现有页面框关联；错误的组件类型引发非法参数错误。
	 *
	 * @param pager 原生视图为 AndroidX ViewPager 的页面组件。
	 */
	@SimpleFunction
	void 绑定页面框(可视组件 pager);

	/**
	 * 标签被选择时触发。
	 *
	 * @param index 从 0 开始的项目索引。
	 */
	@SimpleEvent
	void 标签被选择(int index);
}
