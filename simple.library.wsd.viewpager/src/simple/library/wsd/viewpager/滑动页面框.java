package simple.library.wsd.viewpager;

/* Simple类库标记类 */
import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleDataElement;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.SimpleEvent;
import simple.runtime.annotations.SimpleProperty;
import simple.runtime.annotations.SimpleFunction;
/* Simple基础类 */
import simple.runtime.components.可视组件;
import simple.runtime.components.组件容器;

/**
 * 滑动页面框组件
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface 滑动页面框 extends 可视组件, 组件容器 {

	/**
	 * 页面静止且没有滚动动画，值为 0。
	 */
	@SimpleDataElement
	static final int 滑动状态_未滑动 = 0;

	/**
	 * 用户正在拖动页面，值为 1。
	 */
	@SimpleDataElement
	static final int 滑动状态_正在滑动 = 1;

	/**
	 * 页面正在自动滚动到最终位置，值为 2；沿用现有名称，不表示动画已经结束。
	 */
	@SimpleDataElement
	static final int 滑动状态_滑动完毕 = 2;

	/* 事件 */

	/**
	 * 当页面被滑动时触发此事件。
	 *
	 * @param position 页面索引。
	 * @param positionOffset 偏移百分比。
	 * @param positionOffsetPixels 偏移像素位置。
	 */
	@SimpleEvent
	void 页面被滑动(int position, float positionOffset, int positionOffsetPixels);

	/**
	 * 页面滑动状态改变时触发；0 为静止，1 为手指拖动，2 为松手后或程序切换时的自动滚动。
	 *
	 * @param state 滑动状态值：0 静止、1 拖动、2 自动滚动。
	 */
	@SimpleEvent
	void 滑动状态改变(int state);

	/**
	 * 当滑动页面后选中某个页面时触发此事件。
	 *
	 * @param position 页面索引。
	 */
	@SimpleEvent
	void 页面被选择(int position);

	/* 属性 */

	/**
	 * 读取是否禁止用户通过触摸滑动切换页面。
	 *
	 * @return 禁止触摸滑动时返回真。
	 */
	@SimpleProperty
	boolean 禁止滑动();

	/**
	 * 设置是否禁止触摸滑动；仍可通过现行页面属性切换页面。
	 *
	 * @param noScroll 是否禁止触摸滑动。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_BOOLEAN,
		initializer = "False"
	)
	void 禁止滑动(boolean noScroll);

	/**
	 * 读取通过现行页面属性切换页面时是否使用平滑滚动。
	 *
	 * @return 使用平滑滚动时返回真。
	 */
	@SimpleProperty
	boolean 切换特效();

	/**
	 * 设置通过现行页面属性切换页面时是否使用平滑滚动。
	 *
	 * @param isOpen 是否启用平滑滚动。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_BOOLEAN,
		initializer = "True"
	)
	void 切换特效(boolean isOpen);

	/**
	 * 读取当前选中的页面索引。
	 *
	 * @return 页面索引，从 0 开始；没有页面时返回 -1。
	 */
	@SimpleProperty
	int 现行页面();

	/**
	 * 切换当前页面，是否平滑滚动由切换特效属性决定。
	 *
	 * @param position 目标页面索引，从 0 开始。
	 */
	@SimpleProperty
	void 现行页面(int position);

	/* 方法 */

	/**
	 * 将可视组件从原父容器移出，添加到页面列表末尾。
	 *
	 * @param component 用作页面的可视组件；可使用包含其它组件的容器。
	 */
	@SimpleFunction
	void 添加页面(可视组件 component);

	/**
	 * 删除指定索引的页面，后续页面索引随之改变。
	 *
	 * @param position 要删除的有效页面索引，从 0 开始。
	 */
	@SimpleFunction
	void 删除页面(int position);

	/**
	 * 删除全部页面。
	 */
	@SimpleFunction
	void 清空页面();

	/**
	 * 取得当前页面数量。
	 *
	 * @return 页面数量；没有页面时为 0。
	 */
	@SimpleFunction
	int 取页面数();
}
