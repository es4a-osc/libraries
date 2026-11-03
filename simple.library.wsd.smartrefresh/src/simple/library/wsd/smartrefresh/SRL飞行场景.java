package simple.library.wsd.smartrefresh;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.SimpleProperty;
import simple.runtime.components.可视组件;

/**
 * 与智能刷新框飞行样式配套的山丘场景，可与任意可视组件组成飞行图标效果。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface SRL飞行场景 extends 可视组件 {
	/**
	 * 读取场景主色。
	 *
	 * @return ARGB 颜色。
	 */
	@SimpleProperty
	int 场景主色();

	/**
	 * 设置山丘场景的主色。
	 *
	 * @param value ARGB 颜色。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_COLOR,
		initializer = "-8466743"
	)
	void 场景主色(int value);

	/**
	 * 读取手动设置的场景比例；绑定后的动画进度由上游飞行头管理。
	 *
	 * @return 手动配置值。
	 */
	@SimpleProperty
	float 场景比例();

	/**
	 * 手动设置场景的山丘与树木形变。
	 *
	 * @param value 0 至 10 的有限比例。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_SINGLE,
		initializer = "0"
	)
	void 场景比例(float value);

	/**
	 * 读取场景绘制视口高度。
	 *
	 * @return DIP；0 跟随组件高度。
	 */
	@SimpleProperty
	float 场景视口高度();

	/**
	 * 设置场景绘制视口高度，0 跟随组件高度。
	 *
	 * @param value 0 至 10000 DIP。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_SINGLE,
		initializer = "0"
	)
	void 场景视口高度(float value);

}
