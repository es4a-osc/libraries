package simple.library.wsd.mpandroidchart;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * 展示非负数据占比，非空图表至少有一个正数。
 *
 * <p>
 * 仅在界面线程操作；依次初始化、添加扇区并构建图表。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface MPAC饼状图 extends MPAC图表 {

	/* 图表方法 */

	/**
	 * 添加一个非负扇区，数值总和必须为正；构建后显示。
	 *
	 * @param name  扇区名称。
	 * @param value 有限非负数。
	 * @param color 数据组的 ARGB 颜色。
	 * @throws IllegalArgumentException 参数无效。
	 * @throws IllegalArgumentException 尚未初始化、组件已销毁或未在界面线程调用。
	 */
	@SimpleFunction
	void 添加数据(String name, float value, int color);

	/**
	 * 保存饼图配置副本，构建或刷新后生效。
	 *
	 * @param style 要复制的饼图配置。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	@SimpleFunction
	void 设置饼图样式(MPAC饼图样式 style);

	/**
	 * 更新指定扇区，构建或刷新后生效。
	 *
	 * @param index 从 0 开始的项目编号。
	 * @param name  非空引用的数据组名称。
	 * @param value 非负有限数值。
	 * @param color ARGB 颜色。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	@SimpleFunction
	void 更新扇区(int index, String name, float value, int color);

	/**
	 * 删除指定扇区，构建或刷新后生效。
	 *
	 * @param index 从 0 开始的项目编号。
	 * @throws IllegalArgumentException 参数无效或当前位置不满足要求。
	 */
	@SimpleFunction
	void 删除扇区(int index);
}
