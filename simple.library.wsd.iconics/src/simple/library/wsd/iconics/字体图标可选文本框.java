package simple.library.wsd.iconics;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleEvent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * 字体图标控件。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface 字体图标可选文本框 extends 字体图标视图 {
	/**
	 * 取得选中状态。
	 *
	 * @return 取得选中状态。
	 */
	@SimpleFunction
	public boolean 是否选中();

	/**
	 * 设置选中状态，变化时通知。
	 *
	 * @param checked 选中。
	 */
	@SimpleFunction
	public void 设置选中(boolean checked);

	/**
	 * 同时设置勾选和未勾选图标；任一图像无效则不更新，空引用清除对应图标。
	 *
	 * @param checked   选中图标。
	 * @param unchecked 未选中图标。
	 */
	@SimpleFunction
	public void 设置选中图标(图标图像 checked, 图标图像 unchecked);

	/**
	 * 选中状态变化。
	 *
	 * @param checked 是否选中。
	 */
	@SimpleEvent
	void 选中改变(boolean checked);

	/**
	 * 切换当前选中状态。
	 */
	@SimpleFunction
	public void 切换选中();

	/**
	 * 读取独立状态图标快照，没有时为空。
	 *
	 * @return 读取独立状态图标快照，没有时为空。
	 */
	@SimpleFunction
	public 图标图像 取选中图标();

	/**
	 * 读取独立状态图标快照，没有时为空。
	 *
	 * @return 读取独立状态图标快照，没有时为空。
	 */
	@SimpleFunction
	public 图标图像 取未选中图标();

	/**
	 * 同时设置指定位置两种状态；0 至 5，任一图像无效则不更新，空引用清除对应图标。
	 *
	 * @param position  位置。
	 * @param checked   选中图标。
	 * @param unchecked 未选中图标。
	 */
	@SimpleFunction
	public void 设置位置选中图标(int position, 图标图像 checked, 图标图像 unchecked);

	/**
	 * 统一设置四侧两种状态图标；任一图像无效则不更新，空引用清除。
	 *
	 * @param checked   选中图标。
	 * @param unchecked 未选中图标。
	 */
	@SimpleFunction
	public void 设置四侧选中图标(图标图像 checked, 图标图像 unchecked);

	/**
	 * 读取指定侧选中图标快照，没有时为空。
	 *
	 * @param position 位置。
	 * @return 读取指定侧选中图标快照，没有时为空。
	 */
	@SimpleFunction
	public 图标图像 取位置选中图标(int position);
}
