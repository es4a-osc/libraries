package simple.library.wsd.achartengine;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * 基于 AChartEngine 的时间图；在界面线程初始化、添加数据并构建。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface ACE时间图 extends ACE坐标图表 {

	/**
	 * 添加数据组并复制输入数组，构建或刷新后显示；时间横坐标使用整数毫秒时间戳。
	 *
	 * @param name   非空引用的名称，允许空文本。
	 * @param xs     严格递增的有限数值数组，不允许重复。
	 * @param values 与横坐标或分类名称等长的有限数值数组。
	 * @param color  ARGB 颜色。
	 * @param axis   0 左轴，1 右轴。
	 */
	@SimpleFunction
	void 添加数值组(String name, double[] xs, double[] values, int color, int axis);

	/**
	 * 设置时间轴格式，格式错误时拒绝修改，构建或刷新后生效。
	 *
	 * @param pattern SimpleDateFormat 格式，空文本自动选择；按设备时区显示。
	 * @param smart   是否按时间跨度生成时间刻度。
	 */
	@SimpleFunction
	void 设置时间格式(String pattern, boolean smart);
}
