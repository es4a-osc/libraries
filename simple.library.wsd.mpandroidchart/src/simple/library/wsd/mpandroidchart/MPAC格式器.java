package simple.library.wsd.mpandroidchart;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * 轴标签或数据标注的同步数值格式器；回调中不得重新操作图表。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public interface MPAC格式器 {
	/**
	 * 将当前显示数值转换为文字。
	 *
	 * @param value 原始刻度或数据值；百分比饼图传入百分比。
	 * @return 显示文字；空引用按空文本处理。
	 */
	@SimpleFunction
	String 格式化(float value);
}
