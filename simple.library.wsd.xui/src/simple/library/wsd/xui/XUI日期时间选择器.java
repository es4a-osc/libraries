package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleDataElement;
import simple.runtime.annotations.SimpleEvent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * 显示日期时间滚轮选择器；时间戳单位为毫秒，按设备本地时区显示。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI日期时间选择器 extends XUI选择器 {

	/**
	 * 设置下次显示时的日期时间。
	 *
	 * @param time Unix 毫秒时间戳。
	 */
	@SimpleFunction
	void 置日期(long time);

	/**
	 * 取得初始或最后确认的日期时间。
	 *
	 * @return Unix 毫秒时间戳。
	 */
	@SimpleFunction
	long 取日期();

	/**
	 * 设置可选时间范围，包含起止日期。
	 *
	 * @param minimum 起始时间戳。
	 * @param maximum 结束时间戳，不得早于起始。
	 */
	@SimpleFunction
	void 置范围(long minimum, long maximum);

	/**
	 * 设置显示字段。
	 *
	 * @param mode 0：年月日；1：年月日时分秒；2：时分秒；3：年月日时分。
	 */
	@SimpleFunction
	void 置显示模式(int mode);

	/**
	 * 设置是否显示农历；确认结果仍转换为公历时间戳。
	 *
	 * @param lunar 是否显示农历。
	 */
	@SimpleFunction
	void 置农历(boolean lunar);

	/**
	 * 在所属窗口上显示选择器。
	 */
	@SimpleFunction
	void 显示();

	/**
	 * 用户通过确定按钮提交日期。
	 *
	 * @param time 选中的 Unix 毫秒时间戳。
	 */
	@SimpleEvent
	void 日期已确认(long time);

	/**
	 * 关闭当前显示的界面。
	 */
	@SimpleFunction
	void 关闭();

	/**
	 * 取得当前界面是否正在显示。
	 *
	 * @return 是否显示。
	 */
	@SimpleFunction
	boolean 是否显示();

	/**
	 * 用户滚动选项时触发；不替代确认事件，也不提交取值结果。
	 *
	 * @param time 当前滚轮的 Unix 毫秒时间戳。
	 */
	@SimpleEvent
	void 日期已改变(long time);

	/**
	 * 设置年月日时分秒六列的单位文本，空文本隐藏单位。
	 *
	 * @param values 按年、月、日、时、分、秒排列的六项。
	 */
	@SimpleFunction
	void 置列标签(String[] values);

	/**
	 * 设置后续日期时间滚轮是否循环滚动。
	 *
	 * @param enabled 是否循环。
	 */
	@SimpleFunction
	void 置循环(boolean enabled);

	/**
	 * 年月日时分显示模式。
	 */
	@SimpleDataElement
	int 模式_年月日时分 = 0;

	/**
	 * 完整显示模式。
	 */
	@SimpleDataElement
	int 模式_完整 = 1;

	/**
	 * 时间显示模式。
	 */
	@SimpleDataElement
	int 模式_时间 = 2;

	/**
	 * 日期显示模式。
	 */
	@SimpleDataElement
	int 模式_日期 = 3;

}
