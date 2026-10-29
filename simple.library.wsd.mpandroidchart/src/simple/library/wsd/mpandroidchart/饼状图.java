package simple.library.wsd.mpandroidchart;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * 展示非负数据占比，非空图表至少有一个正数。
 *
 * <p>仅在界面线程操作；依次初始化、添加扇区并构建图表。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface 饼状图 extends 图表 {

	/* 图表方法 */

	/**
	 * 添加一个非负扇区，数值总和必须为正；构建后显示。
	 *
	 * @param name 扇区名称。
	 * @param value 有限非负数。
	 * @param color 数据组的 ARGB 颜色。
	 * @throws IllegalArgumentException 参数无效。
	 * @throws IllegalStateException 尚未初始化、组件已销毁或未在界面线程调用。
	 */
	@SimpleFunction
	void 添加数据(String name, float value, int color);
}
