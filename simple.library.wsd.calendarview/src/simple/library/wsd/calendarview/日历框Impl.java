package simple.library.wsd.calendarview;

import android.os.*;
import android.view.View;
import java.util.*;
import simple.runtime.android.MainActivity;
import simple.runtime.components.组件容器;
import simple.runtime.components.impl.android.视图组件;
import simple.runtime.events.EventDispatcher;
import simple.runtime.parameters.BooleanReferenceParameter;
import simple.library.wsd.calendarview.internal.*;
import simple.library.wsd.calendarview.vendor.Calendar;
import simple.library.wsd.calendarview.vendor.CalendarView;

/**
 * 日历行为、快照集合及同步查询和普通通知的统一边界。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class 日历框Impl extends 视图组件 implements 日历框, MainActivity.OnDestroyListener {
	private final Handler handler = new Handler(Looper.getMainLooper());
	private final TreeMap<String, Calendar> schemes = new TreeMap<>();
	private final TreeMap<String, Calendar> disabled = new TreeMap<>();
	private boolean destroyed;
	private boolean queryEnabled;
	private boolean changingRange;
	private int queries;
	private int visibleYear;
	private int visibleMonth;
	private boolean monthView = true;
	private 日历日期 rangeStart;

	/**
	 * 创建独立原生日历并接入事件与活动销毁。
	 *
	 * @param container 所属 Simple 容器。
	 */
	public 日历框Impl(组件容器 container) {
		super(container);
		visibleYear = view().getCurYear();
		visibleMonth = view().getCurMonth();
		bindEvents();
		MainActivity.getContext().addOnDestroyListener(this);
	}

	@Override
	protected View createView() {
		return new NativeCalendar(MainActivity.getContext());
	}

	/**
	 * 安装所有原生日历通知；普通通知进入主线程队列，查询和绘制保持同步。
	 */
	private void bindEvents() {
		view().setOnCalendarSelectListener(new CalendarView.OnCalendarSelectListener() {
			@Override
			public void onCalendarOutOfRange(Calendar date) {
				日期越界(日历日期.from(date));
			}

			@Override
			public void onCalendarSelect(Calendar date, boolean click) {
				int mode = view().configuration().选择模式();
				if (!changingRange && (mode == 0 || mode == 1)) {
					日期选中(日历日期.from(date), click);
				}
			}
		});
		view().setOnCalendarRangeSelectListener(new CalendarView.OnCalendarRangeSelectListener() {
			@Override
			public void onCalendarSelectOutOfRange(Calendar date) {
				日期越界(日历日期.from(date));
			}

			@Override
			public void onSelectOutOfRange(Calendar date, boolean tooShort) {
				跨度越界(日历日期.from(date), tooShort);
			}

			@Override
			public void onCalendarRangeSelect(Calendar date, boolean end) {
				日历日期 snapshot = 日历日期.from(date);
				if (!end) {
					rangeStart = snapshot;
				}
				范围选择(snapshot, end);
			}
		});
		view().setOnCalendarMultiSelectListener(new CalendarView.OnCalendarMultiSelectListener() {
			@Override
			public void onCalendarMultiSelectOutOfRange(Calendar date) {
				日期越界(日历日期.from(date));
			}

			@Override
			public void onMultiSelectOutOfSize(Calendar date, int maximum) {
				多选数量超限(日历日期.from(date), maximum);
			}

			@Override
			public void onCalendarMultiSelect(Calendar date, int count, int maximum) {
				多选改变(日历日期.from(date), count, maximum, view().es4aSelected(date));
			}
		});
		view().setOnYearChangeListener(new CalendarView.OnYearChangeListener() {
			@Override
			public void onYearChange(int year) {
				年份改变(year);
			}
		});
		view().setOnMonthChangeListener(new CalendarView.OnMonthChangeListener() {
			@Override
			public void onMonthChange(int year, int month) {
				visibleYear = year;
				visibleMonth = month;
				if (!changingRange) {
					月份改变(year, month);
				}
			}
		});
		view().setOnWeekChangeListener(new CalendarView.OnWeekChangeListener() {
			@Override
			public void onWeekChange(List<Calendar> dates) {
				周改变(snapshots(dates));
			}
		});
		view().setOnViewChangeListener(new CalendarView.OnViewChangeListener() {
			@Override
			public void onViewChange(boolean month) {
				monthView = month;
				视图改变(month);
			}
		});
		view().setOnYearViewChangeListener(new CalendarView.OnYearViewChangeListener() {
			@Override
			public void onYearViewChange(boolean close) {
				年视图改变(!close);
			}
		});
		view().setOnClickCalendarPaddingListener(new CalendarView.OnClickCalendarPaddingListener() {
			@Override
			public void onClickCalendarPadding(float x, float y, boolean month, Calendar adjacent, Object payload) {
				float density = view().getResources().getDisplayMetrics().density;
				边距被点击(x / density, y / density, month, 日历日期.from(adjacent));
			}
		});
		view().listener(new NativeCalendar.Listener() {
			@Override
			public void scrolled(boolean monthView, int position, float offset, int pixels) {
				翻页进度(monthView, position, offset, pixels);
			}

			@Override
			public void scrollState(boolean monthView, int state) {
				翻页状态改变(monthView, state);
			}

			@Override
			public boolean draw(日历绘图 drawing, 日历日期 date, int stage, boolean selected, boolean previous, boolean next) {
				BooleanReferenceParameter useDefault = new BooleanReferenceParameter(true);
				绘制日项(drawing, date, stage, selected, previous, next, useDefault);
				return useDefault.get();
			}

			@Override
			public boolean heading(日历绘图 drawing, int year, int month, int stage, int week) {
				BooleanReferenceParameter useDefault = new BooleanReferenceParameter(true);
				绘制标题(drawing, year, month, stage, week, useDefault);
				return useDefault.get();
			}

			@Override
			public void error(String reason) {
				绘制失败(reason);
			}
		});
		bindQueries();
	}

	/**
	 * 在选择模式及长按配置改变后重新连接同步查询。
	 */
	private void bindQueries() {
		view().setOnCalendarInterceptListener(new CalendarView.OnCalendarInterceptListener() {
			@Override
			public boolean onCalendarIntercept(Calendar date) {
				return blocked(date);
			}

			@Override
			public void onCalendarInterceptClick(Calendar date, boolean click) {
				日期被拦截(日历日期.from(date), click);
			}
		});
		view().setOnCalendarLongClickListener(new CalendarView.OnCalendarLongClickListener() {
			@Override
			public void onCalendarLongClickOutOfRange(Calendar date) {
				长按越界(日历日期.from(date));
			}

			@Override
			public void onCalendarLongClick(Calendar date) {
				日期长按(日历日期.from(date));
			}
		}, view().configuration().长按阻止选择());
	}

	/**
	 * 同步查询禁用条件，期间禁止修改当前日历。
	 *
	 * @param date 引擎日期。
	 * @return 该日期禁止选择时为真。
	 */
	private boolean blocked(Calendar date) {
		if (destroyed || view().configuration().选择模式() == 0) {
			return false;
		}
		if (disabled.containsKey(date.toString())) {
			return true;
		}
		if (!queryEnabled) {
			return false;
		}
		queries++;
		try {
			BooleanReferenceParameter result = new BooleanReferenceParameter(false);
			日期拦截(日历日期.from(date), result);
			return result.get();
		} finally {
			queries--;
		}
	}

	/**
	 * 拒绝非主线程及销毁后访问。
	 */
	private void check() {
		if (destroyed || Looper.myLooper() != Looper.getMainLooper()) {
			throw new simple.library.wsd.calendarview.日历错误("日历只能在主线程且销毁前访问");
		}
	}

	/**
	 * 拒绝在绘制或拦截调用栈中修改日历。
	 */
	private void write() {
		check();
		if (queries != 0 || view().drawing() || view().layoutQuerying()) {
			throw new simple.library.wsd.calendarview.日历错误("不能在日期拦截、绘制或顶部判断回调中修改日历，请在后续用户操作中执行");
		}
	}

	/**
	 * 取得本组件固定的原生实例。
	 *
	 * @return 本实例的原生日历。
	 */
	private NativeCalendar view() {
		return (NativeCalendar) getView();
	}

	/**
	 * 校验输入公历日期并取回独立原生值。
	 *
	 * @param date 输入日期。
	 * @return 已校验的原生副本。
	 */
	private static Calendar nativeDate(日历日期 date) {
		if (date == null) {
			throw new IllegalArgumentException("日期不能是空引用");
		}
		DateMath.millis(date.年(), date.月(), date.日());
		return date.nativeValue();
	}

	/**
	 * 验证整个日期数组后再允许修改引擎。
	 *
	 * @param dates 输入数组。
	 * @return 原生日期副本数组。
	 */
	private static Calendar[] validated(日历日期[] dates) {
		if (dates == null || dates.length > 73049) {
			throw new IllegalArgumentException("日期数组为空引用或超过公历支持范围内的总天数");
		}
		Calendar[] values = new Calendar[dates.length];
		for (int i = 0; i < values.length; i++) {
			values[i] = nativeDate(dates[i]);
		}
		return values;
	}

	/**
	 * 把原生集合转换成不共享选择或标记的快照。
	 *
	 * @param dates 引擎日期集合。
	 * @return 日期快照数组。
	 */
	private static 日历日期[] snapshots(Collection<Calendar> dates) {
		if (dates == null) {
			return new 日历日期[0];
		}
		日历日期[] values = new 日历日期[dates.size()];
		int index = 0;
		for (Calendar date : dates) {
			values[index++] = 日历日期.from(date);
		}
		return values;
	}

	/**
	 * 校验允许日期范围及拦截条件并报告未接受的选择。
	 *
	 * @param date 输入日期。
	 * @return 可接受此日期时为真。
	 */
	private boolean accept(Calendar date) {
		if (date.compareTo(view().getMinRangeCalendar()) < 0 || date.compareTo(view().getMaxRangeCalendar()) > 0) {
			日期越界(日历日期.from(date));
			return false;
		}
		if (blocked(date)) {
			日期被拦截(日历日期.from(date), false);
			return false;
		}
		return true;
	}

	/**
	 * 确认当前选择模式。
	 *
	 * @param mode 所需模式。
	 */
	private void mode(int mode) {
		if (view().configuration().选择模式() != mode) {
			throw new simple.library.wsd.calendarview.日历错误(mode == 2 ? "此操作要求范围选择模式" : "此操作要求多选模式");
		}
	}

	@Override
	public 日历日期 当前选择() {
		check();
		return 日历日期.from(view().getSelectedCalendar());
	}

	@Override
	public int 显示年() {
		check();
		return visibleYear;
	}

	@Override
	public int 显示月() {
		check();
		return visibleMonth;
	}

	@Override
	public boolean 是月视图() {
		check();
		return monthView;
	}

	@Override
	public boolean 年视图已打开() {
		check();
		return view().isYearSelectLayoutVisible();
	}

	@Override
	public void 应用配置(日历配置 options) {
		write();
		int oldMode = view().configuration().选择模式();
		view().configure(options);
		if (oldMode != options.选择模式()) {
			rangeStart = null;
		}
		bindQueries();
	}

	@Override
	public 日历配置 取得配置() {
		check();
		return view().configuration();
	}

	@Override
	public void 设置日期范围(日历日期 minimum, 日历日期 maximum) {
		write();
		Calendar min = nativeDate(minimum);
		Calendar max = nativeDate(maximum);
		if (min.compareTo(max) > 0) {
			throw new IllegalArgumentException("最小日期不能晚于最大日期");
		}
		Calendar retained = view().getSelectedCalendar();
		if (retained == null || !retained.isAvailable()) {
			retained = DateMath.calendar(visibleYear, visibleMonth, 1);
		}
		if (retained.compareTo(min) < 0) {
			retained = min;
		} else if (retained.compareTo(max) > 0) {
			retained = max;
		}
		changingRange = true;
		try {
			view().setRange(min.getYear(), min.getMonth(), min.getDay(), max.getYear(), max.getMonth(), max.getDay());
			view().scrollToCalendar(retained.getYear(), retained.getMonth(), retained.getDay(), false, false);
		} finally {
			changingRange = false;
		}
		月份改变(visibleYear, visibleMonth);
		rangeStart = null;
		view().clearSelectRange();
		Calendar[] selected = view().getMultiSelectCalendars().toArray(new Calendar[0]);
		for (Calendar date : selected) {
			if (date.compareTo(min) < 0 || date.compareTo(max) > 0) {
				view().removeMultiSelect(date);
			}
		}
	}

	@Override
	public 日历日期 取最小日期() {
		check();
		return 日历日期.from(view().getMinRangeCalendar());
	}

	@Override
	public 日历日期 取最大日期() {
		check();
		return 日历日期.from(view().getMaxRangeCalendar());
	}

	@Override
	public boolean 跳转日期(日历日期 date, boolean smooth, boolean notify) {
		write();
		Calendar value = nativeDate(date);
		if (!accept(value)) {
			return false;
		}
		view().scrollToCalendar(value.getYear(), value.getMonth(), value.getDay(), smooth, notify);
		return true;
	}

	@Override
	public boolean 返回今天(boolean smooth, boolean notify) {
		write();
		return 跳转日期(日历工具.取今天(), smooth, notify);
	}

	@Override
	public void 前一页(boolean smooth) {
		write();
		view().scrollToPre(smooth);
	}

	@Override
	public void 后一页(boolean smooth) {
		write();
		view().scrollToNext(smooth);
	}

	@Override
	public void 滚动到选择() {
		write();
		view().scrollToSelectCalendar();
	}

	@Override
	public void 打开年视图(int year) {
		write();
		if (year < view().getMinRangeCalendar().getYear() || year > view().getMaxRangeCalendar().getYear()) {
			throw new IllegalArgumentException("年份超出当前允许范围");
		}
		view().showYearSelectLayout(year);
	}

	@Override
	public void 关闭年视图() {
		write();
		view().closeYearSelectLayout();
	}

	@Override
	public 日历日期[] 取当前月日期() {
		check();
		return snapshots(view().getCurrentMonthCalendars());
	}

	@Override
	public 日历日期[] 取当前周日期() {
		check();
		return snapshots(view().getCurrentWeekCalendars());
	}

	@Override
	public 日历日期[] 取多选日期() {
		check();
		return snapshots(view().getMultiSelectCalendars());
	}

	@Override
	public 日历日期[] 取范围日期() {
		check();
		return snapshots(view().getSelectCalendarRange());
	}

	@Override
	public 日历日期[] 取全部标记日期() {
		check();
		return snapshots(schemes.values());
	}

	@Override
	public void 加入多选(日历日期[] dates) {
		write();
		mode(3);
		Calendar[] values = validated(dates);
		TreeMap<String, Calendar> future = new TreeMap<>();
		for (Calendar existing : view().getMultiSelectCalendars()) {
			future.put(existing.toString(), existing);
		}
		for (Calendar value : values) {
			if (!accept(value)) {
				throw new IllegalArgumentException("多选输入包含越界或被拦截的日期");
			}
			future.put(value.toString(), value);
		}
		int maximum = view().getMaxMultiSelectSize();
		if (future.size() > maximum) {
			if (values.length > 0) {
				多选数量超限(日历日期.from(values[values.length - 1]), maximum);
			}
			throw new IllegalArgumentException("批量多选超过数量上限");
		}
		TreeMap<String, Calendar> unique = new TreeMap<>();
		for (Calendar value : values) {
			if (!view().es4aSelected(value)) {
				unique.put(value.toString(), value);
			}
		}
		view().putMultiSelect(values);
		for (Calendar value : unique.values()) {
			多选改变(日历日期.from(value), future.size(), maximum, true);
		}
	}

	@Override
	public void 移除多选(日历日期[] dates) {
		write();
		mode(3);
		Calendar[] values = validated(dates);
		TreeMap<String, Calendar> removed = new TreeMap<>();
		for (Calendar value : values) {
			if (view().es4aSelected(value)) {
				removed.put(value.toString(), value);
			}
		}
		view().removeMultiSelect(values);
		for (Calendar value : removed.values()) {
			多选改变(日历日期.from(value), view().getMultiSelectCalendars().size(), view().getMaxMultiSelectSize(), false);
		}
	}

	@Override
	public void 清除单选() {
		write();
		view().clearSingleSelect();
	}

	@Override
	public void 清除多选() {
		write();
		List<Calendar> previous = view().getMultiSelectCalendars();
		view().clearMultiSelect();
		for (Calendar value : previous) {
			多选改变(日历日期.from(value), 0, view().getMaxMultiSelectSize(), false);
		}
	}

	@Override
	public void 清除范围() {
		write();
		rangeStart = null;
		view().clearSelectRange();
	}

	@Override
	public boolean 设置范围选择(日历日期 start, 日历日期 end) {
		write();
		mode(2);
		Calendar first = nativeDate(start);
		Calendar last = nativeDate(end);
		if (!accept(first) || !accept(last)) {
			return false;
		}
		int count = DateMath.differ(last, first) + 1;
		if (count <= 0) {
			throw new IllegalArgumentException("范围终点不能早于起点");
		}
		日历配置 config = view().configuration();
		if (count < config.最小选择跨度() || (config.最大选择跨度() != -1 && count > config.最大选择跨度())) {
			跨度越界(end, count < config.最小选择跨度());
			return false;
		}
		view().setSelectCalendarRange(first, last);
		return true;
	}

	@Override
	public boolean 设置范围起点(日历日期 date) {
		write();
		mode(2);
		Calendar value = nativeDate(date);
		if (!accept(value)) {
			return false;
		}
		rangeStart = 日历日期.from(value);
		view().setSelectStartCalendar(value);
		范围选择(rangeStart, false);
		return true;
	}

	@Override
	public boolean 设置范围终点(日历日期 date) {
		write();
		mode(2);
		if (rangeStart == null) {
			return false;
		}
		return 设置范围选择(rangeStart, date);
	}

	@Override
	public void 设置全部标记(日历日期[] dates) {
		write();
		Calendar[] values = validated(dates);
		TreeMap<String, Calendar> replacement = new TreeMap<>();
		for (Calendar value : values) {
			if (value.hasScheme()) {
				replacement.put(value.toString(), value);
			}
		}
		schemes.clear();
		schemes.putAll(replacement);
		view().setSchemeDate(new TreeMap<>(schemes));
	}

	@Override
	public void 追加标记(日历日期[] dates) {
		write();
		Calendar[] values = validated(dates);
		for (Calendar value : values) {
			if (value.hasScheme()) {
				schemes.put(value.toString(), value);
			} else {
				schemes.remove(value.toString());
			}
		}
		view().setSchemeDate(new TreeMap<>(schemes));
	}

	@Override
	public void 移除标记(日历日期 date) {
		write();
		Calendar value = nativeDate(date);
		schemes.remove(value.toString());
		view().removeSchemeDate(value);
	}

	@Override
	public void 清除标记() {
		write();
		schemes.clear();
		view().clearSchemeDate();
	}

	@Override
	public void 设置禁用日期(日历日期[] dates) {
		write();
		Calendar[] values = validated(dates);
		disabled.clear();
		for (Calendar value : values) {
			disabled.put(value.toString(), value);
		}
		view().repaint();
	}

	@Override
	public 日历日期[] 取禁用日期() {
		check();
		return snapshots(disabled.values());
	}

	@Override
	public void 启用日期拦截(boolean enabled) {
		write();
		queryEnabled = enabled;
		view().repaint();
	}

	@Override
	public void 刷新今天() {
		write();
		view().updateCurrentDate();
		view().repaint();
	}

	@Override
	public void 请求重绘() {
		write();
		view().repaint();
	}

	@Override
	public int 载入绘制图片(String path) {
		write();
		return view().images().load(MainActivity.getContext().getApplicationContext(), path);
	}

	@Override
	public int 载入绘制图片字节(byte[] bytes) {
		write();
		return view().images().load(bytes);
	}

	@Override
	public void 释放绘制图片(int imageId) {
		write();
		view().images().release(imageId);
		view().repaint();
	}

	@Override
	public void 日期选中(final 日历日期 date, final boolean isClick) {
		handler.post(new Runnable() {
			@Override
			public void run() {
				if (!destroyed) {
					EventDispatcher.dispatchEvent(日历框Impl.this, "日期选中" , date, isClick);
				}
			}
		});
	}

	@Override
	public void 日期越界(final 日历日期 date) {
		handler.post(new Runnable() {
			@Override
			public void run() {
				if (!destroyed) {
					EventDispatcher.dispatchEvent(日历框Impl.this, "日期越界" , date);
				}
			}
		});
	}

	@Override
	public void 范围选择(final 日历日期 date, final boolean isEnd) {
		handler.post(new Runnable() {
			@Override
			public void run() {
				if (!destroyed) {
					EventDispatcher.dispatchEvent(日历框Impl.this, "范围选择" , date, isEnd);
				}
			}
		});
	}

	@Override
	public void 跨度越界(final 日历日期 date, final boolean tooShort) {
		handler.post(new Runnable() {
			@Override
			public void run() {
				if (!destroyed) {
					EventDispatcher.dispatchEvent(日历框Impl.this, "跨度越界" , date, tooShort);
				}
			}
		});
	}

	@Override
	public void 多选改变(final 日历日期 date, final int count, final int maximum, final boolean selected) {
		handler.post(new Runnable() {
			@Override
			public void run() {
				if (!destroyed) {
					EventDispatcher.dispatchEvent(日历框Impl.this, "多选改变" , date, count, maximum, selected);
				}
			}
		});
	}

	@Override
	public void 多选数量超限(final 日历日期 date, final int maximum) {
		handler.post(new Runnable() {
			@Override
			public void run() {
				if (!destroyed) {
					EventDispatcher.dispatchEvent(日历框Impl.this, "多选数量超限" , date, maximum);
				}
			}
		});
	}

	@Override
	public void 日期长按(final 日历日期 date) {
		handler.post(new Runnable() {
			@Override
			public void run() {
				if (!destroyed) {
					EventDispatcher.dispatchEvent(日历框Impl.this, "日期长按" , date);
				}
			}
		});
	}

	@Override
	public void 长按越界(final 日历日期 date) {
		handler.post(new Runnable() {
			@Override
			public void run() {
				if (!destroyed) {
					EventDispatcher.dispatchEvent(日历框Impl.this, "长按越界" , date);
				}
			}
		});
	}

	@Override
	public void 日期拦截(日历日期 date, BooleanReferenceParameter blocked) {
		if (!destroyed) {
			EventDispatcher.dispatchEvent(日历框Impl.this, "日期拦截" , date, blocked);
		}
	}

	@Override
	public void 日期被拦截(final 日历日期 date, final boolean isClick) {
		handler.post(new Runnable() {
			@Override
			public void run() {
				if (!destroyed) {
					EventDispatcher.dispatchEvent(日历框Impl.this, "日期被拦截" , date, isClick);
				}
			}
		});
	}

	@Override
	public void 年份改变(final int year) {
		handler.post(new Runnable() {
			@Override
			public void run() {
				if (!destroyed) {
					EventDispatcher.dispatchEvent(日历框Impl.this, "年份改变" , year);
				}
			}
		});
	}

	@Override
	public void 月份改变(final int year, final int month) {
		handler.post(new Runnable() {
			@Override
			public void run() {
				if (!destroyed) {
					EventDispatcher.dispatchEvent(日历框Impl.this, "月份改变" , year, month);
				}
			}
		});
	}

	@Override
	public void 周改变(final 日历日期[] dates) {
		handler.post(new Runnable() {
			@Override
			public void run() {
				if (!destroyed) {
					EventDispatcher.dispatchEvent(日历框Impl.this, "周改变" , (Object) dates);
				}
			}
		});
	}

	@Override
	public void 视图改变(final boolean monthView) {
		handler.post(new Runnable() {
			@Override
			public void run() {
				if (!destroyed) {
					EventDispatcher.dispatchEvent(日历框Impl.this, "视图改变" , monthView);
				}
			}
		});
	}

	@Override
	public void 年视图改变(final boolean opened) {
		handler.post(new Runnable() {
			@Override
			public void run() {
				if (!destroyed) {
					EventDispatcher.dispatchEvent(日历框Impl.this, "年视图改变" , opened);
				}
			}
		});
	}

	@Override
	public void 边距被点击(final float x, final float y, final boolean monthView, final 日历日期 adjacent) {
		handler.post(new Runnable() {
			@Override
			public void run() {
				if (!destroyed) {
					EventDispatcher.dispatchEvent(日历框Impl.this, "边距被点击" , x, y, monthView, adjacent);
				}
			}
		});
	}

	@Override
	public void 绘制日项(日历绘图 drawing, 日历日期 date, int stage, boolean selected, boolean previous, boolean next, BooleanReferenceParameter useDefault) {
		if (!destroyed) {
			EventDispatcher.dispatchEvent(日历框Impl.this, "绘制日项" , drawing, date, stage, selected, previous, next, useDefault);
		}
	}

	@Override
	public void 绘制标题(日历绘图 drawing, int year, int month, int stage, int week, BooleanReferenceParameter useDefault) {
		if (!destroyed) {
			EventDispatcher.dispatchEvent(日历框Impl.this, "绘制标题" , drawing, year, month, stage, week, useDefault);
		}
	}

	@Override
	public void 绘制失败(final String reason) {
		handler.post(new Runnable() {
			@Override
			public void run() {
				if (!destroyed) {
					EventDispatcher.dispatchEvent(日历框Impl.this, "绘制失败" , reason);
				}
			}
		});
	}

	@Override
	public void onDestroy() {
		销毁();
	}

	@Override
	public void 销毁() {
		if (!destroyed) {
			write();
			destroyed = true;
			MainActivity.getContext().removeOnDestroyListener(this);
			handler.removeCallbacksAndMessages(null);
			view().close();
			schemes.clear();
			disabled.clear();
			rangeStart = null;
			super.销毁();
		}
	}

	@Override
	public boolean 定位年份(int year, boolean smooth) {
		write();
		if (year < view().getMinRangeCalendar().getYear() || year > view().getMaxRangeCalendar().getYear()) {
			throw new IllegalArgumentException("年份超出当前允许范围");
		}
		if (!view().isYearSelectLayoutVisible()) {
			return false;
		}
		view().scrollToYear(year, smooth);
		return true;
	}

	@Override
	public int 年视图年份() {
		check();
		return view().yearPosition();
	}

	@Override
	public void 翻页进度(final boolean monthView, final int position, final float offset, final int pixels) {
		handler.post(new Runnable() {
			@Override
			public void run() {
				if (!destroyed && view().isAttachedToWindow()) {
					EventDispatcher.dispatchEvent(日历框Impl.this, "翻页进度", monthView, position, offset, pixels);
				}
			}
		});
	}

	@Override
	public void 翻页状态改变(final boolean monthView, final int state) {
		handler.post(new Runnable() {
			@Override
			public void run() {
				if (!destroyed && view().isAttachedToWindow()) {
					EventDispatcher.dispatchEvent(日历框Impl.this, "翻页状态改变", monthView, state);
				}
			}
		});
	}

	/**
	 * 供所属布局检查本日历的同步回调边界。
	 *
	 * @return 正在拦截或绘制回调中时为真。
	 */
	boolean 同步查询中() {
		return queries != 0 || view().drawing();
	}

}
