package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleEvent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.components.可视组件;

/**
 * XUI吸顶滚动框组件。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI吸顶滚动框 extends XUI滚动容器 {

	/**
	 * 设置阴影高度。
	 *
	 * @param height 高度。
	 */
	@SimpleFunction
	void 置阴影高度(int height);

	/**
	 * 刷新吸顶属性。
	 */
	@SimpleFunction
	void 刷新吸顶属性();

	/**
	 * 设置阴影图像，使用 Simple 图片标识。
	 *
	 * @param image 图片资源标识、资产文件名或外部路径。
	 */
	@SimpleFunction
	void 置阴影图像(String image);

	/**
	 * 设置本容器后代组件的吸顶状态；目标不在本容器或无法设置标记时引发非法参数错误。
	 *
	 * @param component 本容器内部的后代可视组件。
	 * @param enabled 是否启用吸顶。
	 */
	@SimpleFunction
	void 标记吸顶(可视组件 component, boolean enabled);

	/**
	 * 通过标记吸顶注册的组件开始或结束吸顶时触发。
	 *
	 * @param component 被设置吸顶的 Simple 组件。
	 * @param sticky 是否正在吸顶。
	 */
	@SimpleEvent
	void 吸顶改变(可视组件 component, boolean sticky);
}
