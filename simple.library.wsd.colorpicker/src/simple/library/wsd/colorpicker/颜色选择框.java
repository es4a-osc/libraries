package simple.library.wsd.colorpicker;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleEvent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.SimpleProperty;
import simple.runtime.components.组件;

/**
 * 通过色环和明暗渐变条选择颜色的非可视组件。
 *
 * <p>所有成员在界面线程调用，显示方法立即返回，结果通过事件通知。
 * 用户按确定后才更新选中颜色；取消不会提交草稿。颜色使用 Android ARGB
 * 整数，透明度沿用打开时的初始颜色，不提供透明度调节。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface 颜色选择框 extends 组件 {

	/* 组件事件 */

	/**
	 * 用户确认颜色后触发；此时对话框已关闭，选中颜色已经更新。
	 *
	 * @param color 确认的 ARGB 颜色，可以直接赋给组件颜色属性。
	 */
	@SimpleEvent
	void 颜色选择完毕(int color);

	/**
	 * 用户取消、按返回、点击框外或调用关闭方法时触发一次。
	 * 重复显示替换旧框及销毁组件时不触发。
	 */
	@SimpleEvent
	void 取消选择();

	/* 组件属性 */

	/**
	 * 读取下次显示的对话框标题。
	 *
	 * @return 标题文本，默认是选择颜色。
	 */
	@SimpleProperty
	String 标题();

	/**
	 * 设置下次显示的对话框标题，不改变已打开的对话框。
	 *
	 * @param title 标题文本，不得为空引用；空文本表示不显示标题。
	 * @throws IllegalArgumentException 标题为空引用。
	 * @throws IllegalStateException 不在界面线程调用或组件已销毁。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_STRING,
		initializer = "\"选择颜色\""
	)
	void 标题(String title);

	/**
	 * 读取下次显示时采用的初始颜色。
	 *
	 * @return ARGB 颜色，默认是红色 &amp;HFFFF0000。
	 */
	@SimpleProperty
	int 初始颜色();

	/**
	 * 设置下次显示时采用的初始颜色，不改变当前草稿或选中颜色。
	 *
	 * @param color ARGB 颜色；透明度在本次选色过程中保持不变。
	 * @throws IllegalStateException 不在界面线程调用或组件已销毁。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_COLOR,
		initializer = "&HFFFF0000"
	)
	void 初始颜色(int color);

	/**
	 * 读取最近一次确认的颜色。
	 *
	 * @return 最近确认的 ARGB 颜色；从未确认时为红色 &amp;HFFFF0000。
	 */
	@SimpleProperty
	int 选中颜色();

	/**
	 * 查询当前是否有尚未结束的选色对话框。
	 *
	 * @return 已打开为真，否则为假。
	 */
	@SimpleProperty
	boolean 正在显示();

	/* 组件方法 */

	/**
	 * 使用标题和初始颜色打开对话框，不阻塞调用。
	 * 再次调用会关闭旧框并重新开始选色，不派发旧框的取消事件。
	 *
	 * @throws IllegalStateException 不在界面线程调用、组件已销毁或宿主无法显示对话框。
	 */
	@SimpleFunction
	void 显示();

	/**
	 * 关闭当前对话框并触发取消选择；未显示时不执行。
	 *
	 * @throws IllegalStateException 不在界面线程调用或组件已销毁。
	 */
	@SimpleFunction
	void 关闭();
}
