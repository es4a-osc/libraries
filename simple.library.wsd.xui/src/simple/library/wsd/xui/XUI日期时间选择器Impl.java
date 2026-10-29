package simple.library.wsd.xui;

import java.util.Calendar;
import java.util.Date;

import android.view.View;

import com.xuexiang.xui.widget.picker.widget.TimePickerView;
import com.xuexiang.xui.widget.picker.widget.builder.TimePickerBuilder;
import com.xuexiang.xui.widget.picker.widget.configure.TimePickerType;
import com.xuexiang.xui.widget.picker.widget.listener.OnTimeSelectChangeListener;
import com.xuexiang.xui.widget.picker.widget.listener.OnTimeSelectListener;

import simple.runtime.components.组件容器;
import simple.runtime.errors.非法参数错误;

/**
 * 日期时间选择器的 XUI 实现与 Simple 生命周期适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI日期时间选择器Impl extends XUI选择器基类 implements XUI日期时间选择器 {

	private String[] labels;
	private boolean cyclic;

	/**
	 * 创建XUI日期时间选择器。
	 *
	 * @param container 所属 Simple 窗口。
	 */
	public XUI日期时间选择器Impl(组件容器 container) {
		super(container);
	}

	private long time = System.currentTimeMillis();
	private Long minimum;
	private Long maximum;
	private int mode;
	private boolean lunar;

	@Override
	public void 置日期(long value) {
		time = value;
	}

	@Override
	public long 取日期() {
		return time;
	}

	@Override
	public void 置范围(long start, long end) {
		if (start > end) {
			throw new 非法参数错误();
		}
		minimum = start;
		maximum = end;
	}

	@Override
	public void 置显示模式(int value) {
		ComponentSupport.validateIndex(value, 4);
		mode = value;
	}

	@Override
	public void 置农历(boolean enabled) {
		lunar = enabled;
	}

	/**
	 * 使用本地时区创建独立 Calendar，避免构建器修改调用状态。
	 *
	 * @param value 毫秒时间戳。
	 * @return 本地日期。
	 */
	private Calendar createCalendar(long value) {
		Calendar date = Calendar.getInstance();
		date.setTimeInMillis(value);
		return date;
	}

	@Override
	public void 显示() {
		requireVisibleOwner();
		if ((minimum != null && time < minimum) || (maximum != null && time > maximum)) {
			throw new 非法参数错误();
		}
		关闭();
		TimePickerType[] types = {TimePickerType.DEFAULT, TimePickerType.ALL, TimePickerType.TIME, TimePickerType.DATE};
		final TimePickerView[] current = new TimePickerView[1];
		TimePickerBuilder builder = new TimePickerBuilder(ComponentSupport.createContext(), new OnTimeSelectListener() {
			@Override
			public void onTimeSelected(Date date, View source) {
				if (!confirmPicker(current[0])) {
					return;
				}
				time = date.getTime();
				日期已确认(time);
			}
		}).setType(types[mode]).setDate(createCalendar(time)).setDecorView(getOverlayContainer());
		if (minimum != null) {
			builder.setRangDate(createCalendar(minimum), createCalendar(maximum));
		}
		configure(builder);
		builder.isCyclic(cyclic);
		if (labels != null) {
			builder.setLabel(labels[0], labels[1], labels[2], labels[3], labels[4], labels[5]);
		}
		builder.setTimeSelectChangeListener(new OnTimeSelectChangeListener() {
			@Override
			public void onTimeSelectChanged(Date date) {
				if (isCurrentPicker(current[0])) {
					日期已改变(date.getTime());
				}
			}
		});
		current[0] = builder.build();
		current[0].setLunarCalendar(lunar);
		showPicker(current[0]);
	}

	@Override
	public void 日期已改变(long time) {
		dispatchEvent("日期已改变", time);
	}

	@Override
	public void 日期已确认(long time) {
		dispatchEvent("日期已确认", time);
	}

	@Override
	public void 置列标签(String[] values) {
		String[] copy = ComponentSupport.copyTextArray(values);
		if (copy.length != 6) {
			throw new 非法参数错误();
		}
		labels = copy;
	}

	@Override
	public void 置循环(boolean enabled) {
		cyclic = enabled;
	}

}
