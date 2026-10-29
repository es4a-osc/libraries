package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleEvent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.components.组件容器;

/**
 * XUI折叠容器组件。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI折叠容器 extends XUI视图组件, 组件容器 {

	/**
	 * 取得展开状态：0 已收起、1 收起中、2 展开中、3 已展开。
	 *
	 * @return 展开状态：0 已收起、1 收起中、2 展开中、3 已展开。
	 */
	@SimpleFunction
	int 取状态();

	/**
	 * 是否展开。
	 *
	 * @return 是否展开。
	 */
	@SimpleFunction
	boolean 是否展开();

	/**
	 * 切换。
	 */
	@SimpleFunction
	void 切换();

	/**
	 * 切换。
	 *
	 * @param animate 动画。
	 */
	@SimpleFunction
	void 切换(boolean animate);

	/**
	 * 伸展。
	 */
	@SimpleFunction
	void 展开();

	/**
	 * 展开。
	 *
	 * @param animate 动画。
	 */
	@SimpleFunction
	void 展开(boolean animate);

	/**
	 * 收缩。
	 */
	@SimpleFunction
	void 收起();

	/**
	 * 收起。
	 *
	 * @param animate 动画。
	 */
	@SimpleFunction
	void 收起(boolean animate);

	/**
	 * 设置展开。
	 *
	 * @param expand 是否展开。
	 */
	@SimpleFunction
	void 置展开(boolean expand);

	/**
	 * 设置展开。
	 *
	 * @param expand 是否展开。
	 * @param animate 动画。
	 */
	@SimpleFunction
	void 置展开(boolean expand, boolean animate);

	/**
	 * 取得时长。
	 *
	 * @return 时长。
	 */
	@SimpleFunction
	int 取时长();

	/**
	 * 设置时长。
	 *
	 * @param duration 时长。
	 */
	@SimpleFunction
	void 置时长(int duration);

	/**
	 * 取得展开比例。
	 *
	 * @return 展开比例。
	 */
	@SimpleFunction
	float 取展开比例();

	/**
	 * 设置展开比例。
	 *
	 * @param expansion 展开比例。
	 */
	@SimpleFunction
	void 置展开比例(float expansion);

	/**
	 * 取得视差。
	 *
	 * @return 视差。
	 */
	@SimpleFunction
	float 取视差();

	/**
	 * 设置视差。
	 *
	 * @param parallax 视差。
	 */
	@SimpleFunction
	void 置视差(float parallax);

	/**
	 * 折叠容器展开比例或状态改变时触发。
	 *
	 * @param fraction 展开比例，0 收起、1 展开。
	 * @param state 0 已收起、1 收起中、2 展开中、3 已展开。
	 */
	@SimpleEvent
	void 展开改变(float fraction, int state);
}
