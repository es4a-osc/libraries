package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.components.可视组件;

/**
 * XUI文本徽标组件。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI文本徽标 extends XUI文本组件 {

	/**
	 * 设置背景。
	 *
	 * @param dipRadius 圆角半径，单位 DIP。
	 * @param badgeColor 徽标颜色。
	 */
	@SimpleFunction
	void 置背景(int dipRadius, int badgeColor);

	/**
	 * 是否隐藏开空值。
	 *
	 * @return 是否隐藏开空值。
	 */
	@SimpleFunction
	boolean 是否隐藏开空值();

	/**
	 * 设置隐藏开空值。
	 *
	 * @param hideOnNull 隐藏开空值。
	 */
	@SimpleFunction
	void 置隐藏开空值(boolean hideOnNull);

	/**
	 * 设置徽标数量。
	 *
	 * @param count 数量。
	 */
	@SimpleFunction
	void 置徽标数量(int count);

	/**
	 * 取得徽标数量。
	 *
	 * @return 徽标数量。
	 */
	@SimpleFunction
	int 取徽标数量();

	/**
	 * 设置徽标对齐。
	 *
	 * @param gravity 对齐。
	 */
	@SimpleFunction
	void 置徽标对齐(int gravity);

	/**
	 * 取得徽标对齐。
	 *
	 * @return 徽标对齐。
	 */
	@SimpleFunction
	int 取徽标对齐();

	/**
	 * 设置徽标外边距。
	 *
	 * @param dipMargin 外边距，单位 DIP。
	 */
	@SimpleFunction
	void 置徽标外边距(int dipMargin);

	/**
	 * 设置徽标外边距。
	 *
	 * @param leftDipMargin 左侧外边距，单位 DIP。
	 * @param topDipMargin 顶部外边距，单位 DIP。
	 * @param rightDipMargin 右侧外边距，单位 DIP。
	 * @param bottomDipMargin 底部外边距，单位 DIP。
	 */
	@SimpleFunction
	void 置徽标外边距(int leftDipMargin, int topDipMargin, int rightDipMargin, int bottomDipMargin);

	/**
	 * 取得徽标外边距。
	 *
	 * @return 徽标外边距。
	 */
	@SimpleFunction
	int[] 取徽标外边距();

	/**
	 * 隐藏。
	 */
	@SimpleFunction
	void 隐藏();

	/**
	 * 显示。
	 */
	@SimpleFunction
	void 显示();

	/**
	 * 把徽标附着到指定组件；目标没有父容器或为自身时拒绝操作。
	 *
	 * @param target 徽标附着的可视组件；组件必须已加入父容器。
	 */
	@SimpleFunction
	void 绑定目标(可视组件 target);
}
