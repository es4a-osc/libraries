package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleEvent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * XUI复选框组件。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI复选框 extends XUI视图组件 {

	/**
	 * 切换。
	 */
	@SimpleFunction
	void 切换();

	/**
	 * 静默设置。
	 *
	 * @param checked 选中。
	 */
	@SimpleFunction
	void 置选中静默(boolean checked);

	/**
	 * 设置选中。
	 *
	 * @param checked 选中。
	 * @param animate 动画。
	 */
	@SimpleFunction
	void 置选中(boolean checked, boolean animate);

	/**
	 * 取得选中状态。
	 *
	 * @return 选中状态。
	 */
	@SimpleFunction
	boolean 取选中();

	/**
	 * 设置选中状态。
	 *
	 * @param checked 是否选中。
	 */
	@SimpleFunction
	void 置选中(boolean checked);

	/**
	 * 选中状态改变时触发。
	 *
	 * @param checked 是否选中。
	 */
	@SimpleEvent
	void 选中改变(boolean checked);
}
