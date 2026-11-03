package simple.library.wsd.smartrefresh;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleDataElement;
import simple.runtime.parameters.BooleanReferenceParameter;
import simple.runtime.parameters.SingleReferenceParameter;
import simple.runtime.annotations.SimpleEvent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.SimpleProperty;
import simple.runtime.components.可视组件;
import simple.runtime.components.组件容器;

/**
 * 接收一个内容组件，提供内置及自定义头尾、二楼和独立请求序号的智能刷新容器。
 *
 * <p>所有成员在主线程调用，销毁后拒绝访问。多个普通控件应放入一个内容容器，
 * 列表和网页直接作为内容；业务由应用执行，不由组件代发网络请求。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface SRL下拉刷新框 extends 可视组件, 组件容器 {

	/**
	 * 进入刷新状态时触发；应用完成业务后用同一请求序号调用完成刷新。
	 *
	 * @param requestId 本次请求的正整数序号。
	 */
	@SimpleEvent
	void 开始刷新(int requestId);

	/**
	 * 进入加载状态时触发；应用完成业务后用同一请求序号调用完成加载。
	 *
	 * @param requestId 本次请求的正整数序号。
	 */
	@SimpleEvent
	void 开始加载(int requestId);

	/**
	 * 仍在界面中时通过主线程队列报告状态变化，不代表应用业务成功。
	 *
	 * @param oldState 变化前的状态。
	 * @param newState 变化后的状态。
	 */
	@SimpleEvent
	void 状态改变(String oldState, String newState);

	/**
	 * 当前请求因主动取消或视图移出界面而失效时触发；应用自行取消任务。组件销毁时不再发出事件。
	 *
	 * @param requestId 被取消的请求序号。
	 */
	@SimpleEvent
	void 操作取消(int requestId);

	/**
	 * 取得是否允许发起下拉刷新；关闭不取消已经开始的业务。
	 *
	 * @return 是否允许发起下拉刷新；关闭不取消已经开始的业务。
	 */
	@SimpleProperty
	boolean 允许下拉();

	/**
	 * 设置是否允许发起下拉刷新；关闭不取消已经开始的业务。
	 *
	 * @param value 是否允许发起下拉刷新；关闭不取消已经开始的业务。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_BOOLEAN,
		initializer = "True"
	)
	void 允许下拉(boolean value);

	/**
	 * 取得是否允许发起上拉加载；关闭不取消已经开始的业务。
	 *
	 * @return 是否允许发起上拉加载；关闭不取消已经开始的业务。
	 */
	@SimpleProperty
	boolean 允许上拉();

	/**
	 * 设置是否允许发起上拉加载；关闭不取消已经开始的业务。
	 *
	 * @param value 是否允许发起上拉加载；关闭不取消已经开始的业务。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_BOOLEAN,
		initializer = "True"
	)
	void 允许上拉(boolean value);

	/**
	 * 取得内容滚动到底部时是否自动加载，默认关闭。
	 *
	 * @return 内容滚动到底部时是否自动加载，默认关闭。
	 */
	@SimpleProperty
	boolean 自动加载更多();

	/**
	 * 设置内容滚动到底部时是否自动加载，默认关闭。
	 *
	 * @param value 内容滚动到底部时是否自动加载，默认关闭。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_BOOLEAN,
		initializer = "False"
	)
	void 自动加载更多(boolean value);

	/**
	 * 取得内容未填满可视区域时是否允许上拉加载。
	 *
	 * @return 内容未填满可视区域时是否允许上拉加载。
	 */
	@SimpleProperty
	boolean 内容不足时允许加载();

	/**
	 * 设置内容未填满可视区域时是否允许上拉加载。
	 *
	 * @param value 内容未填满可视区域时是否允许上拉加载。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_BOOLEAN,
		initializer = "False"
	)
	void 内容不足时允许加载(boolean value);

	/**
	 * 取得是否允许内容滚动到边界后回弹。
	 *
	 * @return 是否允许内容滚动到边界后回弹。
	 */
	@SimpleProperty
	boolean 越界回弹();

	/**
	 * 设置是否允许内容滚动到边界后回弹。
	 *
	 * @param value 是否允许内容滚动到边界后回弹。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_BOOLEAN,
		initializer = "True"
	)
	void 越界回弹(boolean value);

	/**
	 * 取得是否允许越过边界继续拖动内容。
	 *
	 * @return 是否允许越过边界继续拖动内容。
	 */
	@SimpleProperty
	boolean 允许越界拖动();

	/**
	 * 设置是否允许越过边界继续拖动内容。
	 *
	 * @param value 是否允许越过边界继续拖动内容。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_BOOLEAN,
		initializer = "False"
	)
	void 允许越界拖动(boolean value);

	/**
	 * 取得是否与支持嵌套滚动的内容协调手势。
	 *
	 * @return 是否与支持嵌套滚动的内容协调手势。
	 */
	@SimpleProperty
	boolean 启用嵌套滚动();

	/**
	 * 设置是否与支持嵌套滚动的内容协调手势。
	 *
	 * @param value 是否与支持嵌套滚动的内容协调手势。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_BOOLEAN,
		initializer = "True"
	)
	void 启用嵌套滚动(boolean value);

	/**
	 * 取得等待刷新完成时是否禁止操作内容区域。
	 *
	 * @return 等待刷新完成时是否禁止操作内容区域。
	 */
	@SimpleProperty
	boolean 刷新时禁止操作内容();

	/**
	 * 设置等待刷新完成时是否禁止操作内容区域。
	 *
	 * @param value 等待刷新完成时是否禁止操作内容区域。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_BOOLEAN,
		initializer = "False"
	)
	void 刷新时禁止操作内容(boolean value);

	/**
	 * 取得等待加载完成时是否禁止操作内容区域。
	 *
	 * @return 等待加载完成时是否禁止操作内容区域。
	 */
	@SimpleProperty
	boolean 加载时禁止操作内容();

	/**
	 * 设置等待加载完成时是否禁止操作内容区域。
	 *
	 * @param value 等待加载完成时是否禁止操作内容区域。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_BOOLEAN,
		initializer = "False"
	)
	void 加载时禁止操作内容(boolean value);

	/**
	 * 取得手指位移转换为内容位移的比例，范围大于 0 且不大于 1。
	 *
	 * @return 手指位移转换为内容位移的比例，范围大于 0 且不大于 1。
	 */
	@SimpleProperty
	float 拖动阻尼();

	/**
	 * 设置手指位移转换为内容位移的比例，范围大于 0 且不大于 1。
	 *
	 * @param value 手指位移转换为内容位移的比例，范围大于 0 且不大于 1。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_SINGLE,
		initializer = "0.5"
	)
	void 拖动阻尼(float value);

	/**
	 * 取得触发刷新所需距离相对刷新头高度的比例，范围大于 0 且不大于 1。
	 *
	 * @return 触发刷新所需距离相对刷新头高度的比例，范围大于 0 且不大于 1。
	 */
	@SimpleProperty
	float 下拉触发比例();

	/**
	 * 设置触发刷新所需距离相对刷新头高度的比例，范围大于 0 且不大于 1。
	 *
	 * @param value 触发刷新所需距离相对刷新头高度的比例，范围大于 0 且不大于 1。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_SINGLE,
		initializer = "1.0"
	)
	void 下拉触发比例(float value);

	/**
	 * 取得触发加载所需距离相对加载尾高度的比例，范围大于 0 且不大于 1。
	 *
	 * @return 触发加载所需距离相对加载尾高度的比例，范围大于 0 且不大于 1。
	 */
	@SimpleProperty
	float 上拉触发比例();

	/**
	 * 设置触发加载所需距离相对加载尾高度的比例，范围大于 0 且不大于 1。
	 *
	 * @param value 触发加载所需距离相对加载尾高度的比例，范围大于 0 且不大于 1。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_SINGLE,
		initializer = "1.0"
	)
	void 上拉触发比例(float value);

	/**
	 * 取得刷新头高度，单位 DIP，范围大于等于 1、小于 10000。
	 *
	 * @return 刷新头高度，单位 DIP，范围大于等于 1、小于 10000。
	 */
	@SimpleProperty
	float 刷新头高度();

	/**
	 * 设置刷新头高度，单位 DIP，范围大于等于 1、小于 10000。
	 *
	 * @param value 刷新头高度，单位 DIP，范围大于等于 1、小于 10000。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_SINGLE,
		initializer = "60"
	)
	void 刷新头高度(float value);

	/**
	 * 取得加载尾高度，单位 DIP，范围大于等于 1、小于 10000。
	 *
	 * @return 加载尾高度，单位 DIP，范围大于等于 1、小于 10000。
	 */
	@SimpleProperty
	float 加载尾高度();

	/**
	 * 设置加载尾高度，单位 DIP，范围大于等于 1、小于 10000。
	 *
	 * @param value 加载尾高度，单位 DIP，范围大于等于 1、小于 10000。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_SINGLE,
		initializer = "60"
	)
	void 加载尾高度(float value);

	/**
	 * 取得回弹动画时长，单位毫秒，范围 0 至 10000。
	 *
	 * @return 回弹动画时长，单位毫秒，范围 0 至 10000。
	 */
	@SimpleProperty
	int 回弹时长();

	/**
	 * 设置回弹动画时长，单位毫秒，范围 0 至 10000。
	 *
	 * @param value 回弹动画时长，单位毫秒，范围 0 至 10000。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_INTEGER,
		initializer = "300"
	)
	void 回弹时长(int value);

	/**
	 * 取得经典头尾的 ARGB 背景颜色。
	 *
	 * @return 经典头尾的 ARGB 背景颜色。
	 */
	@SimpleProperty
	int 主色();

	/**
	 * 设置经典头尾的 ARGB 背景颜色。
	 *
	 * @param value 经典头尾的 ARGB 背景颜色。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_COLOR,
		initializer = "-1118482"
	)
	void 主色(int value);

	/**
	 * 取得经典头尾文字与指示图形的 ARGB 颜色。
	 *
	 * @return 经典头尾文字与指示图形的 ARGB 颜色。
	 */
	@SimpleProperty
	int 文字颜色();

	/**
	 * 设置经典头尾文字与指示图形的 ARGB 颜色。
	 *
	 * @param value 经典头尾文字与指示图形的 ARGB 颜色。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_COLOR,
		initializer = "-12303292"
	)
	void 文字颜色(int value);

	/**
	 * 是否正在显示刷新状态，包含应用已提交结果但动画尚未结束的阶段。
	 *
	 * @return 是否正在显示刷新状态，包含应用已提交结果但动画尚未结束的阶段。
	 */
	@SimpleProperty
	boolean 正在刷新();

	/**
	 * 是否正在显示加载状态，包含应用已提交结果但动画尚未结束的阶段。
	 *
	 * @return 是否正在显示加载状态，包含应用已提交结果但动画尚未结束的阶段。
	 */
	@SimpleProperty
	boolean 正在加载();

	/**
	 * 上一次成功加载是否已标记没有更多数据。
	 *
	 * @return 上一次成功加载是否已标记没有更多数据。
	 */
	@SimpleProperty
	boolean 没有更多数据();

	/**
	 * 当前等待业务结果的请求序号；没有待处理业务时为 0。
	 *
	 * @return 当前等待业务结果的请求序号；没有待处理业务时为 0。
	 */
	@SimpleProperty
	int 当前请求序号();

	/**
	 * 当前状态：空闲、下拉中、上拉中、等待释放刷新、等待释放加载、刷新释放中、加载释放中、正在刷新、正在加载、刷新完成、加载完成、取消下拉或取消上拉。
	 *
	 * @return 当前状态：空闲、下拉中、上拉中、等待释放刷新、等待释放加载、刷新释放中、加载释放中、正在刷新、正在加载、刷新完成、加载完成、取消下拉或取消上拉。
	 */
	@SimpleProperty
	String 当前状态();

	/**
	 * 尝试自动下拉刷新；内容已附着、允许下拉且空闲时才受理。
	 *
	 * @return 动画请求被受理时为真；开始事件稍后触发。
	 */
	@SimpleFunction
	boolean 触发刷新();

	/**
	 * 尝试自动上拉加载；没有更多数据、方向关闭或忙碌时拒绝。
	 *
	 * @return 动画请求被受理时为真；开始事件稍后触发。
	 */
	@SimpleFunction
	boolean 触发加载();

	/**
	 * 提交指定刷新请求的成功或失败结果；成功会重置无更多数据状态。
	 *
	 * @param requestId 开始刷新事件提供的序号。
	 * @param success 业务是否成功。
	 * @return 同序号、同方向的待处理请求被受理时为真，重复或过期结果为假。
	 */
	@SimpleFunction
	boolean 完成刷新(int requestId, boolean success);

	/**
	 * 提交加载结果；仅成功时采用没有更多数据标志，不取消业务任务。
	 *
	 * @param requestId 开始加载事件提供的序号。
	 * @param success 业务是否成功。
	 * @param noMoreData 成功且已经没有后续数据时为真。
	 * @return 同序号、同方向的待处理请求被受理时为真，重复或过期结果为假。
	 */
	@SimpleFunction
	boolean 完成加载(int requestId, boolean success, boolean noMoreData);

	/**
	 * 使当前请求失效，以失败结束指示并发出操作取消事件；应用自行取消业务任务。
	 *
	 * @return 存在待处理请求并已取消时为真。
	 */
	@SimpleFunction
	boolean 取消当前操作();

	/**
	 * 清除没有更多数据标志，允许后续加载；不自动发起新请求。
	 *
	 */
	@SimpleFunction
	void 重置加载状态();

	/**
	 * 读取内置刷新头编号 0 至 15；选择飞行前须绑定场景与图标。自定义头由设置自定义刷新头建立。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	int 刷新头样式();

	/**
	 * 设置内置刷新头编号 0 至 15；选择飞行前须绑定场景与图标。自定义头由设置自定义刷新头建立。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_INTEGER,
		initializer = "0"
	)
	void 刷新头样式(int value);

	/**
	 * 读取内置加载尾编号 0 经典、1 球脉冲、2 虚拟。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	int 加载尾样式();

	/**
	 * 设置内置加载尾编号 0 经典、1 球脉冲、2 虚拟。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_INTEGER,
		initializer = "0"
	)
	void 加载尾样式(int value);

	/**
	 * 读取下拉时内容是否跟随刷新头移动。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	boolean 下拉内容跟随();

	/**
	 * 设置下拉时内容是否跟随刷新头移动。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_BOOLEAN,
		initializer = "True"
	)
	void 下拉内容跟随(boolean value);

	/**
	 * 读取上拉时内容是否跟随加载尾移动。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	boolean 上拉内容跟随();

	/**
	 * 设置上拉时内容是否跟随加载尾移动。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_BOOLEAN,
		initializer = "True"
	)
	void 上拉内容跟随(boolean value);

	/**
	 * 读取仅进行滚动与回弹，不执行刷新加载业务。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	boolean 纯滚动模式();

	/**
	 * 设置仅进行滚动与回弹，不执行刷新加载业务。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_BOOLEAN,
		initializer = "False"
	)
	void 纯滚动模式(boolean value);

	/**
	 * 读取加载完成后是否滚动内容显示新数据。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	boolean 加载完成滚动内容();

	/**
	 * 设置加载完成后是否滚动内容显示新数据。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_BOOLEAN,
		initializer = "True"
	)
	void 加载完成滚动内容(boolean value);

	/**
	 * 读取刷新完成后是否自动调整内容滚动位置。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	boolean 刷新完成滚动内容();

	/**
	 * 设置刷新完成后是否自动调整内容滚动位置。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_BOOLEAN,
		initializer = "True"
	)
	void 刷新完成滚动内容(boolean value);

	/**
	 * 读取没有更多数据时尾部是否跟随内容。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	boolean 末页尾部跟随();

	/**
	 * 设置没有更多数据时尾部是否跟随内容。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_BOOLEAN,
		initializer = "False"
	)
	void 末页尾部跟随(boolean value);

	/**
	 * 读取固定背后模式下是否剪裁刷新头。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	boolean 背后刷新头剪裁();

	/**
	 * 设置固定背后模式下是否剪裁刷新头。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_BOOLEAN,
		initializer = "True"
	)
	void 背后刷新头剪裁(boolean value);

	/**
	 * 读取固定背后模式下是否剪裁加载尾。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	boolean 背后加载尾剪裁();

	/**
	 * 设置固定背后模式下是否剪裁加载尾。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_BOOLEAN,
		initializer = "True"
	)
	void 背后加载尾剪裁(boolean value);

	/**
	 * 读取通过传址事件修改原生计算出的刷新和加载边界结果。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	boolean 启用自定义边界();

	/**
	 * 设置通过传址事件修改原生计算出的刷新和加载边界结果。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_BOOLEAN,
		initializer = "False"
	)
	void 启用自定义边界(boolean value);

	/**
	 * 读取刷新头起始偏移，单位 DIP，范围 0 至 10000。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	float 头部起始偏移();

	/**
	 * 设置刷新头起始偏移，单位 DIP，范围 0 至 10000。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_SINGLE,
		initializer = "0"
	)
	void 头部起始偏移(float value);

	/**
	 * 读取加载尾起始偏移，单位 DIP，范围 0 至 10000。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	float 尾部起始偏移();

	/**
	 * 设置加载尾起始偏移，单位 DIP，范围 0 至 10000。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_SINGLE,
		initializer = "0"
	)
	void 尾部起始偏移(float value);

	/**
	 * 读取最大下拉距离相对头高的比例，范围大于等于 1、小于 10。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	float 下拉最大比例();

	/**
	 * 设置最大下拉距离相对头高的比例，范围大于等于 1、小于 10。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_SINGLE,
		initializer = "2.5"
	)
	void 下拉最大比例(float value);

	/**
	 * 读取最大上拉距离相对尾高的比例，范围大于等于 1、小于 10。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	float 上拉最大比例();

	/**
	 * 设置最大上拉距离相对尾高的比例，范围大于等于 1、小于 10。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_SINGLE,
		initializer = "2.5"
	)
	void 上拉最大比例(float value);

	/**
	 * 读取0 原生、1 线性、2 加速、3 减速、4 加减速、5 弹跳、6 越界、7 回缩、8 回缩越界、9 自定义事件、10 贝塞尔。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	int 回弹插值模式();

	/**
	 * 设置0 原生、1 线性、2 加速、3 减速、4 加减速、5 弹跳、6 越界、7 回缩、8 回缩越界、9 自定义事件、10 贝塞尔。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_INTEGER,
		initializer = "0"
	)
	void 回弹插值模式(int value);

	/**
	 * 读取加速、减速、越界、回缩模式的因子，范围大于 0 且不大于 20。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	float 回弹插值因子();

	/**
	 * 设置加速、减速、越界、回缩模式的因子，范围大于 0 且不大于 20。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_SINGLE,
		initializer = "2"
	)
	void 回弹插值因子(float value);

	/**
	 * 刷新头样式。
	 *
	 */
	@SimpleDataElement
	int 头_经典 = 0;

	/**
	 * 刷新头样式。
	 *
	 */
	@SimpleDataElement
	int 头_雷达 = 1;

	/**
	 * 刷新头样式。
	 *
	 */
	@SimpleDataElement
	int 头_Material = 2;

	/**
	 * 刷新头样式。
	 *
	 */
	@SimpleDataElement
	int 头_虚拟 = 3;

	/**
	 * 刷新头样式。
	 *
	 */
	@SimpleDataElement
	int 头_二楼 = 4;

	/**
	 * 刷新头样式。
	 *
	 */
	@SimpleDataElement
	int 头_贝塞尔圆 = 5;

	/**
	 * 刷新头样式。
	 *
	 */
	@SimpleDataElement
	int 头_快递 = 6;

	/**
	 * 刷新头样式。
	 *
	 */
	@SimpleDataElement
	int 头_Dropbox = 7;

	/**
	 * 刷新头样式。
	 *
	 */
	@SimpleDataElement
	int 头_飞行 = 8;

	/**
	 * 刷新头样式。
	 *
	 */
	@SimpleDataElement
	int 头_坦克游戏 = 9;

	/**
	 * 刷新头样式。
	 *
	 */
	@SimpleDataElement
	int 头_打砖块 = 10;

	/**
	 * 刷新头样式。
	 *
	 */
	@SimpleDataElement
	int 头_凤凰 = 11;

	/**
	 * 刷新头样式。
	 *
	 */
	@SimpleDataElement
	int 头_仓库 = 12;

	/**
	 * 刷新头样式。
	 *
	 */
	@SimpleDataElement
	int 头_金牛 = 13;

	/**
	 * 刷新头样式。
	 *
	 */
	@SimpleDataElement
	int 头_水滴 = 14;

	/**
	 * 刷新头样式。
	 *
	 */
	@SimpleDataElement
	int 头_波浪 = 15;

	/**
	 * 加载尾样式。
	 *
	 */
	@SimpleDataElement
	int 尾_经典 = 0;

	/**
	 * 加载尾样式。
	 *
	 */
	@SimpleDataElement
	int 尾_球脉冲 = 1;

	/**
	 * 加载尾样式。
	 *
	 */
	@SimpleDataElement
	int 尾_虚拟 = 2;

	/**
	 * 头尾变换样式。
	 *
	 */
	@SimpleDataElement
	int 变换_原生 = -1;

	/**
	 * 头尾变换样式。
	 *
	 */
	@SimpleDataElement
	int 变换_平移 = 0;

	/**
	 * 头尾变换样式。
	 *
	 */
	@SimpleDataElement
	int 变换_拉伸 = 1;

	/**
	 * 头尾变换样式。
	 *
	 */
	@SimpleDataElement
	int 变换_固定背后 = 2;

	/**
	 * 头尾变换样式。
	 *
	 */
	@SimpleDataElement
	int 变换_固定前面 = 3;

	/**
	 * 头尾变换样式。
	 *
	 */
	@SimpleDataElement
	int 变换_填满 = 4;

	/**
	 * 报告拖动或回弹中的真实偏移，距离与高度均为像素。
	 *
	 * @param dragging 是否由手指拖动。
	 * @param percent 相对头尾高度的比例。
	 * @param offset 实际像素偏移。
	 * @param height 头尾像素高度。
	 * @param maxHeight 最大像素拖动高度。
	 */
	@SimpleEvent
	void 头部移动(boolean dragging, float percent, int offset, int height, int maxHeight);

	/**
	 * 手指释放并准备进入动画时触发。
	 *
	 * @param height 像素高度。
	 * @param maxHeight 最大像素高度。
	 */
	@SimpleEvent
	void 头部释放(int height, int maxHeight);

	/**
	 * 原生指示动画开始时触发。
	 *
	 * @param height 像素高度。
	 * @param maxHeight 最大像素高度。
	 */
	@SimpleEvent
	void 头部动画开始(int height, int maxHeight);

	/**
	 * 原生头尾收到完成结果时触发，不等同于收回动画已经结束。
	 *
	 * @param success 本次提交的结果。
	 */
	@SimpleEvent
	void 头部结束(boolean success);

	/**
	 * 自定义头尾启用水平拖动时报告水平偏移。
	 *
	 * @param percent 水平比例。
	 * @param offset 水平像素偏移。
	 * @param maxOffset 最大像素偏移。
	 */
	@SimpleEvent
	void 头部水平拖动(float percent, int offset, int maxOffset);

	/**
	 * 报告拖动或回弹中的真实偏移，距离与高度均为像素。
	 *
	 * @param dragging 是否由手指拖动。
	 * @param percent 相对头尾高度的比例。
	 * @param offset 实际像素偏移。
	 * @param height 头尾像素高度。
	 * @param maxHeight 最大像素拖动高度。
	 */
	@SimpleEvent
	void 尾部移动(boolean dragging, float percent, int offset, int height, int maxHeight);

	/**
	 * 手指释放并准备进入动画时触发。
	 *
	 * @param height 像素高度。
	 * @param maxHeight 最大像素高度。
	 */
	@SimpleEvent
	void 尾部释放(int height, int maxHeight);

	/**
	 * 原生指示动画开始时触发。
	 *
	 * @param height 像素高度。
	 * @param maxHeight 最大像素高度。
	 */
	@SimpleEvent
	void 尾部动画开始(int height, int maxHeight);

	/**
	 * 原生头尾收到完成结果时触发，不等同于收回动画已经结束。
	 *
	 * @param success 本次提交的结果。
	 */
	@SimpleEvent
	void 尾部结束(boolean success);

	/**
	 * 自定义头尾启用水平拖动时报告水平偏移。
	 *
	 * @param percent 水平比例。
	 * @param offset 水平像素偏移。
	 * @param maxOffset 最大像素偏移。
	 */
	@SimpleEvent
	void 尾部水平拖动(float percent, int offset, int maxOffset);

	/**
	 * 二楼打开前同步触发，可将允许设为假拒绝。
	 *
	 * @param allowed 是否允许打开。
	 */
	@SimpleEvent
	void 准备打开二楼(BooleanReferenceParameter allowed);

	/**
	 * 二楼实际进入打开状态后触发。
	 *
	 */
	@SimpleEvent
	void 二楼已打开();

	/**
	 * 二楼完成关闭或因视图移出界面关闭时触发。
	 *
	 */
	@SimpleEvent
	void 二楼已关闭();

	/**
	 * 飞行返回动画实际结束时触发，离开界面取消动画时不触发。
	 *
	 */
	@SimpleEvent
	void 飞行动画结束();

	/**
	 * 启用自定义边界时触发，初值是原生边界结果。
	 *
	 * @param allowed 是否允许在当前位置下拉刷新。
	 */
	@SimpleEvent
	void 判断下拉边界(BooleanReferenceParameter allowed);

	/**
	 * 启用自定义边界时触发，初值是原生边界结果。
	 *
	 * @param allowed 是否允许在当前位置上拉加载。
	 */
	@SimpleEvent
	void 判断上拉边界(BooleanReferenceParameter allowed);

	/**
	 * 插值模式为 9 时同步触发，输出须为有限数值。
	 *
	 * @param input 动画时间进度。
	 * @param output 可修改的动画位置进度。
	 */
	@SimpleEvent
	void 计算回弹位置(float input, SingleReferenceParameter output);

	/**
	 * 返回独立配置快照；修改后必须调用应用外观。
	 *
	 * @return 外观副本。
	 */
	@SimpleFunction
	SRL刷新外观 取刷新头外观();

	/**
	 * 空闲时复制并应用外观；修改原配置不会影响已应用的组件。
	 *
	 * @param appearance 外观配置。
	 */
	@SimpleFunction
	void 应用刷新头外观(SRL刷新外观 appearance);

	/**
	 * 空闲时借用本窗口中刷新框以外的可视组件；恢复内置样式或销毁时归还。
	 *
	 * @param content 原始内容组件。
	 */
	@SimpleFunction
	void 设置自定义刷新头(可视组件 content);

	/**
	 * 返回独立配置快照；修改后必须调用应用外观。
	 *
	 * @return 外观副本。
	 */
	@SimpleFunction
	SRL刷新外观 取加载尾外观();

	/**
	 * 空闲时复制并应用外观；修改原配置不会影响已应用的组件。
	 *
	 * @param appearance 外观配置。
	 */
	@SimpleFunction
	void 应用加载尾外观(SRL刷新外观 appearance);

	/**
	 * 空闲时借用本窗口中刷新框以外的可视组件；恢复内置样式或销毁时归还。
	 *
	 * @param content 原始内容组件。
	 */
	@SimpleFunction
	void 设置自定义加载尾(可视组件 content);

	/**
	 * 空闲时借用独立内容并启用二楼头；使用内置样式替换后恢复原归属。
	 *
	 * @param content 二楼内容组件。
	 */
	@SimpleFunction
	void 设置二楼内容(可视组件 content);

	/**
	 * 设置二楼全部行为参数；改变配置须在空闲时进行。
	 *
	 * @param refreshRate 一级刷新比例。
	 * @param floorRate 大于刷新比例。
	 * @param maxRate 不小于二楼比例，且不大于 10。
	 * @param duration 0 至 10000 毫秒。
	 * @param bottomRate 0 至 1 的高度比例。
	 * @param enabled 是否允许进入二楼。
	 * @param refresh 是否允许一级刷新。
	 * @param close 是否允许上滑关闭二楼。
	 * @param baseType 除二楼和飞行之外的内置头样式。
	 */
	@SimpleFunction
	void 配置二楼(float refreshRate, float floorRate, float maxRate, int duration, float bottomRate, boolean enabled, boolean refresh, boolean close, int baseType);

	/**
	 * 空闲且启用二楼时打开，可选择触发准备打开事件。
	 *
	 * @param notify 是否通过准备打开二楼事件判断。
	 * @return 存在二楼头且受理请求时为真；事件仍可拒绝打开。
	 */
	@SimpleFunction
	boolean 打开二楼(boolean notify);

	/**
	 * 关闭当前已打开或正在打开的二楼。
	 *
	 * @return 受理关闭请求时为真。
	 */
	@SimpleFunction
	boolean 关闭二楼();

	/**
	 * 游戏样式下设置 0 准备、1 进行、2 结束、3 完成、4 失败。
	 *
	 * @param state 游戏状态。
	 */
	@SimpleFunction
	void 设置游戏状态(int state);

	/**
	 * 飞行样式下结束飞行并播放返回动画，结束时发出飞行动画结束。
	 *
	 */
	@SimpleFunction
	void 结束飞行动画();

	/**
	 * 选择飞行样式前，绑定本窗口内的飞行场景和作为图标的可视组件。
	 *
	 * @param scene 飞行场景组件。
	 * @param image 作为飞机的可视组件。
	 */
	@SimpleFunction
	void 绑定飞行场景(SRL飞行场景 scene, 可视组件 image);

	/**
	 * 带延时、持续时间和比例触发动画；仅动画模式使用关闭头尾收回。
	 *
	 * @param delay 0 至 32767 毫秒。
	 * @param duration 1 至 10000 毫秒。
	 * @param rate 大于 0 且不大于最大拖动比例。
	 * @param animationOnly 仅显示动画而不触发开始业务事件。
	 * @return 原生请求被受理时为真，业务事件在动画后触发。
	 */
	@SimpleFunction
	boolean 动画刷新(int delay, int duration, float rate, boolean animationOnly);

	/**
	 * 带延时、持续时间和比例触发动画；仅动画模式使用关闭头尾收回。
	 *
	 * @param delay 0 至 32767 毫秒。
	 * @param duration 1 至 10000 毫秒。
	 * @param rate 大于 0 且不大于最大拖动比例。
	 * @param animationOnly 仅显示动画而不触发开始业务事件。
	 * @return 原生请求被受理时为真，业务事件在动画后触发。
	 */
	@SimpleFunction
	boolean 动画加载(int delay, int duration, float rate, boolean animationOnly);

	/**
	 * 提交刷新结果及分页状态，延时开始完成动画。
	 *
	 * @param requestId 开始事件给出的序号。
	 * @param delay 0 至 32767 毫秒。
	 * @param success 业务是否成功。
	 * @param endState 成功时 -1 保留、0 重置、1 结束；失败时保留。
	 * @return 有效同方向请求被受理时为真，过期或重复结果为假。
	 */
	@SimpleFunction
	boolean 延时完成刷新(int requestId, int delay, boolean success, int endState);

	/**
	 * 提交加载结果，并延时开始完成动画。
	 *
	 * @param requestId 开始事件给出的序号。
	 * @param delay 0 至 32767 毫秒。
	 * @param success 业务是否成功。
	 * @param noMoreData 仅成功时采用此值。
	 * @return 有效同方向请求被受理时为真。
	 */
	@SimpleFunction
	boolean 延时完成加载(int requestId, int delay, boolean success, boolean noMoreData);

	/**
	 * 收回任意状态的头尾；存在业务请求时按取消处理，不伪造业务成功。
	 *
	 */
	@SimpleFunction
	void 关闭头尾();

	/**
	 * 设置三次贝塞尔回弹曲线并选择插值模式 10。
	 *
	 * @param x1 0 至 1。
	 * @param y1 有限数值。
	 * @param x2 0 至 1。
	 * @param y2 有限数值。
	 */
	@SimpleFunction
	void 设置回弹贝塞尔(float x1, float y1, float x2, float y2);

	/**
	 * 必须在首次附着前设置内容子树中的固定视图；空引用清除。
	 *
	 * @param component 内容子树内的可视组件或空引用。
	 */
	@SimpleFunction
	void 设置固定头部(可视组件 component);

	/**
	 * 必须在首次附着前设置内容子树中的固定视图；空引用清除。
	 *
	 * @param component 内容子树内的可视组件或空引用。
	 */
	@SimpleFunction
	void 设置固定尾部(可视组件 component);

	/**
	 * 空闲时指定内容子树内需要跟随移动的视图；空引用表示整个内容。
	 *
	 * @param component 内容子树内的可视组件或空引用。
	 */
	@SimpleFunction
	void 设置头部跟随视图(可视组件 component);

	/**
	 * 空闲时指定内容子树内需要跟随移动的视图；空引用表示整个内容。
	 *
	 * @param component 内容子树内的可视组件或空引用。
	 */
	@SimpleFunction
	void 设置尾部跟随视图(可视组件 component);

	/**
	 * 仅在空闲时设置是否没有更多数据，不伪造刷新或加载结果。
	 *
	 * @param ended 是否已经到达末页。
	 */
	@SimpleFunction
	void 置分页结束(boolean ended);

	/**
	 * 设置最多 32 项的原始 ARGB 颜色，供不同样式按其约定使用。
	 *
	 * @param colors 至少一个颜色。
	 */
	@SimpleFunction
	void 设置主题颜色(int[] colors);

	/**
	 * 读取 Android 颜色资源标识后应用完整主题，资源不存在时明确报错。
	 *
	 * @param ids 颜色资源标识数组。
	 */
	@SimpleFunction
	void 设置资源主题(int[] ids);

	/**
	 * 空闲时设置头或尾的原生布局尺寸，宽高可用 -1 匹配父级、-2 适应内容或非负像素。
	 *
	 * @param header 真为头，假为尾。
	 * @param width 像素宽度或 -1、-2。
	 * @param height 像素高度或 -1、-2。
	 */
	@SimpleFunction
	void 设置头尾像素尺寸(boolean header, int width, int height);

	/**
	 * 空闲时以像素设置头尾标准高度，同时同步 DIP 配置。
	 *
	 * @param header 真为头，假为尾。
	 * @param pixels 正整数像素。
	 */
	@SimpleFunction
	void 设置头尾像素高度(boolean header, int pixels);

	/**
	 * 空闲时以像素设置头或尾的起始偏移，同步 DIP 配置。
	 *
	 * @param header 真为头，假为尾。
	 * @param pixels 0 至 10000 像素。
	 */
	@SimpleFunction
	void 设置头尾像素偏移(boolean header, int pixels);

	/**
	 * 空闲时借用同窗口中的外部组件替换显示内容，恢复原内容或销毁时归还。
	 *
	 * @param component 刷新框以外的可视组件。
	 */
	@SimpleFunction
	void 替换刷新内容(可视组件 component);

	/**
	 * 空闲时恢复最初直接放入刷新框的组件，并归还临时借用内容。
	 *
	 */
	@SimpleFunction
	void 恢复原内容();

	/**
	 * 返回当前实际显示的内容组件。
	 *
	 * @return 实际内容组件。
	 */
	@SimpleFunction
	可视组件 取当前内容();

	/**
	 * 读取原生当前用于布局的头尾标准像素高度。
	 *
	 * @param header 真为头，假为尾。
	 * @return 当前像素高度。
	 */
	@SimpleFunction
	int 取头尾像素高度(boolean header);

	/**
	 * 空闲时重新测量适应内容高度的头尾；固定像素高度不受理。
	 *
	 * @param header 刷新头。
	 * @return 请求已提交测量时为真；高度在下一次布局后更新。
	 */
	@SimpleFunction
	boolean 重新测量头尾(boolean header);

}
