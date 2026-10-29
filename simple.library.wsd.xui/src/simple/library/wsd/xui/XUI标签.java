package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.components.可视组件;

/**
 * XUI标签组件。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI标签 extends XUI文本组件 {

	/**
	 * 将文字角标附着到目标组件上角。
	 *
	 * @param target 已加入父容器的目标组件。
	 * @param distance 角标距目标上边缘的距离，单位为 DIP。
	 * @param right 真为右上角，假为左上角。
	 */
	@SimpleFunction
	void 绑定目标(可视组件 target, int distance, boolean right);
}
