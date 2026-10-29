package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.components.组件容器;

/**
 * 纵向滚动容器的共用定位接口，位置与范围均采用物理像素。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public interface XUI滚动容器 extends XUI视图组件, 组件容器 {

	/**
	 * 取得相对内容顶部的当前滚动偏移。
	 *
	 * @return 当前物理像素偏移。
	 */
	@SimpleFunction
	int 取滚动偏移();

	/**
	 * 取得布局后内容可滚动的最大偏移，未布局或内容不足一屏时为零。
	 *
	 * @return 最大物理像素偏移。
	 */
	@SimpleFunction
	int 取最大滚动偏移();

	/**
	 * 布局完成后滚动到指定位置；排队期间只保留最后一次目标。
	 *
	 * @param position 相对顶部的物理像素偏移，超出范围时限制到有效边界。
	 * @param smooth 是否平滑滚动。
	 */
	@SimpleFunction
	void 滚动到(int position, boolean smooth);

	/**
	 * 布局完成后滚动到顶部。
	 *
	 * @param smooth 是否平滑滚动。
	 */
	@SimpleFunction
	void 滚动到顶(boolean smooth);

	/**
	 * 布局完成后滚动到底部，使用执行时的实际内容尺寸。
	 *
	 * @param smooth 是否平滑滚动。
	 */
	@SimpleFunction
	void 滚动到底(boolean smooth);
}
