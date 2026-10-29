package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleDataElement;
import simple.runtime.annotations.SimpleEvent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.SimpleProperty;
import simple.runtime.components.可视组件;

/**
 * XUI 可视组件共用的常量、交互及无障碍接口。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public interface XUI视图组件 extends 可视组件 {

	/**
	 * 关闭轮播页面的附加切换效果。
	 */
	@SimpleDataElement
	String 轮播效果_无 = "None";

	/**
	 * 使用深度切换效果。
	 */
	@SimpleDataElement
	String 轮播效果_深度 = "Depth";

	/**
	 * 使用淡入滑动切换效果。
	 */
	@SimpleDataElement
	String 轮播效果_淡入滑动 = "FadeSlide";

	/**
	 * 使用流动切换效果。
	 */
	@SimpleDataElement
	String 轮播效果_流动 = "Flow";

	/**
	 * 使用向下旋转切换效果。
	 */
	@SimpleDataElement
	String 轮播效果_向下旋转 = "RotateDown";

	/**
	 * 使用向上旋转切换效果。
	 */
	@SimpleDataElement
	String 轮播效果_向上旋转 = "RotateUp";

	/**
	 * 使用缩小滑动切换效果。
	 */
	@SimpleDataElement
	String 轮播效果_缩小滑动 = "ZoomOutSlide";

	/**
	 * 取得是否接受用户操作。
	 *
	 * @return 是否启用。
	 */
	@SimpleProperty
	boolean 启用();

	/**
	 * 设置是否接受用户操作。
	 *
	 * @param enabled 是否启用。
	 */
	@SimpleProperty(type = SimpleProperty.PROPERTY_TYPE_BOOLEAN)
	void 启用(boolean enabled);

	/**
	 * 取得无障碍服务读取的组件含义。
	 *
	 * @return 内容描述。
	 */
	@SimpleProperty
	String 内容描述();

	/**
	 * 设置无障碍服务读取的组件含义。
	 *
	 * @param description 内容描述。
	 */
	@SimpleProperty
	void 内容描述(String description);

	/**
	 * 设置内容内边距，单位为物理像素。
	 *
	 * @param left 左侧内边距。
	 * @param top 顶部内边距。
	 * @param right 右侧内边距。
	 * @param bottom 底部内边距。
	 */
	@SimpleFunction
	void 置内容边距(int left, int top, int right, int bottom);

	/**
	 * 组件被单击时触发。
	 */
	@SimpleEvent
	void 被单击();

	/**
	 * 组件被长按时触发。
	 */
	@SimpleEvent
	void 被长按();

	/**
	 * 输入焦点改变时触发。
	 *
	 * @param focused 是否获得焦点。
	 */
	@SimpleEvent
	void 焦点改变(boolean focused);
}
