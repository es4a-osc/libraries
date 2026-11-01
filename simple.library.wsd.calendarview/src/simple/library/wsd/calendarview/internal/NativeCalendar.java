package simple.library.wsd.calendarview.internal;

import android.content.Context;
import android.graphics.Canvas;
import android.os.*;
import android.view.*;
import simple.library.wsd.calendarview.*;
import simple.library.wsd.calendarview.vendor.*;
import simple.library.wsd.calendarview.vendor.Calendar;

/**
 * 固定原生实例上的批量配置、绘制作用域及资源管理。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class NativeCalendar extends CalendarView {
	private 日历配置 options = new 日历配置();
	private final ImagePool images = new ImagePool();
	private final Handler handler = new Handler(Looper.getMainLooper());
	private Listener listener;
	private int drawingDepth;
	private boolean disposed;

	/**
	 * 创建普通 Activity 可直接使用的日历。
	 *
	 * @param context 活动上下文。
	 */
	public NativeCalendar(Context context) {
		super(context);
		setWeekBar(CalendarCells.Header.class);
		configure(options);
	}

	/**
	 * 安装所属 Simple 组件的绘制接收者。
	 *
	 * @param value 绘制事件接收者。
	 */
	public void listener(Listener value) {
		listener = value;
	}

	/**
	 * 应用行为、外观和模式对应的视图类。
	 *
	 * @param config 待复制的配置。
	 */
	public void configure(日历配置 config) {
		if (config == null) {
			throw new IllegalArgumentException("日历配置不能为空");
		}
		日历配置 snapshot = config.copy();
		if (snapshot.最大选择跨度() != -1 && snapshot.最大选择跨度() < snapshot.最小选择跨度()) {
			throw new IllegalArgumentException("最大选择跨度不能小于最小选择跨度");
		}
		if (snapshot.填满高度() && getParent() instanceof CalendarLayout) {
			throw new IllegalArgumentException("收缩布局中的日历不能填满高度");
		}
		int mode = snapshot.选择模式();
		if (mode == options.选择模式() && getMultiSelectCalendars().size() > snapshot.最大多选数()) {
			throw new IllegalArgumentException("新的多选上限小于当前已选数量");
		}
		if (mode != options.选择模式()) {
			clearSelectRange();
			clearMultiSelect();
			clearSingleSelect();
		}
		options = snapshot;
		if (mode == 0) {
			setSelectDefaultMode();
		} else if (mode == 1) {
			setSelectSingleMode();
		} else if (mode == 2) {
			setSelectRangeMode();
		} else {
			setSelectMultiMode();
		}
		setMonthView(mode == 2 ? CalendarCells.RangeMonth.class : mode == 3 ? CalendarCells.MultiMonth.class : CalendarCells.MonthCell.class);
		setWeekView(mode == 2 ? CalendarCells.RangeWeek.class : mode == 3 ? CalendarCells.MultiWeek.class : CalendarCells.WeekCell.class);
		setMaxMultiSelectSize(snapshot.最大多选数());
		setSelectRange(snapshot.最小选择跨度(), snapshot.最大选择跨度());
		if (snapshot.周起始() == 1) {
			setWeekStarWithSun();
		} else if (snapshot.周起始() == 2) {
			setWeekStarWithMon();
		} else {
			setWeekStarWithSat();
		}
		if (snapshot.月份填充() == 0) {
			setAllMode();
		} else if (snapshot.月份填充() == 1) {
			setOnlyCurrentMode();
		} else {
			setFixMode();
		}
		if (snapshot.自动选择日() == 0) {
			setDefaultMonthViewSelectDay();
		} else if (snapshot.自动选择日() == 1) {
			setLastMonthViewSelectDay();
		} else {
			setLastMonthViewSelectDayIgnoreCurrent();
		}
		setMonthViewScrollable(snapshot.月视图可滑动());
		setWeekViewScrollable(snapshot.周视图可滑动());
		setYearViewScrollable(snapshot.年视图可滑动());
		es4aConfigure(snapshot);
		repaint();
	}

	/**
	 * 读取供调用方修改的独立配置副本。
	 *
	 * @return 配置快照。
	 */
	public 日历配置 configuration() {
		return options.copy();
	}

	/**
	 * 原生绘制类只读访问当前样式。
	 *
	 * @return 本实例的样式配置。
	 */
	日历配置 style() {
		return options;
	}

	/**
	 * 查询是否处于同步绘制作用域。
	 *
	 * @return 绘制回调正在执行时为真。
	 */
	public boolean drawing() {
		return drawingDepth != 0;
	}

	/**
	 * 取得预加载图片集合，禁止在绘制时修改。
	 *
	 * @return 图片集合。
	 */
	public ImagePool images() {
		return images;
	}

	/**
	 * 同步执行日项绘制回调，并保证画布及上下文恢复。
	 *
	 * @param canvas 原生画布。
	 * @param date 日期。
	 * @param stage 绘制阶段。
	 * @param selected 是否选中。
	 * @param previous 前项是否选中。
	 * @param next 后项是否选中。
	 * @param x 左边像素。
	 * @param y 顶边像素。
	 * @param width 宽度像素。
	 * @param height 高度像素。
	 * @return 是否继续默认绘制。
	 */
	boolean draw(Canvas canvas, Calendar date, int stage, boolean selected, boolean previous, boolean next, float x, float y, float width, float height) {
		if (disposed || listener == null || !options.启用绘制事件()) {
			return true;
		}
		日历绘图 drawing = scope(canvas, x, y, width, height);
		drawingDepth++;
		try {
			return listener.draw(drawing, 日历日期.from(date), stage, selected, previous, next);
		} catch (RuntimeException error) {
			report(error);
			return true;
		} finally {
			drawing.end();
			drawingDepth--;
		}
	}

	/**
	 * 同步执行年标题或星期栏的绘制回调。
	 *
	 * @param canvas 原生画布。
	 * @param year 年份。
	 * @param month 月份。
	 * @param stage 标题阶段。
	 * @param week 星期编号。
	 * @param x 左边像素。
	 * @param y 顶边像素。
	 * @param width 宽度像素。
	 * @param height 高度像素。
	 * @return 是否继续默认绘制。
	 */
	boolean heading(Canvas canvas, int year, int month, int stage, int week, float x, float y, float width, float height) {
		if (disposed || listener == null || !options.启用绘制事件()) {
			return true;
		}
		日历绘图 drawing = scope(canvas, x, y, width, height);
		drawingDepth++;
		try {
			return listener.heading(drawing, year, month, stage, week);
		} catch (RuntimeException error) {
			report(error);
			return true;
		} finally {
			drawing.end();
			drawingDepth--;
		}
	}

	/**
	 * 创建局部绘图作用域。
	 *
	 * @param canvas 原生画布。
	 * @param x 左边像素。
	 * @param y 顶边像素。
	 * @param width 宽度像素。
	 * @param height 高度像素。
	 * @return 本回调专用上下文。
	 */
	private 日历绘图 scope(Canvas canvas, float x, float y, float width, float height) {
		android.util.DisplayMetrics metrics = getResources().getDisplayMetrics();
		return 日历绘图.begin(canvas, images, x, y, width, height, metrics.density, metrics.scaledDensity);
	}

	/**
	 * 脱离绘制栈后通知错误，避免错误处理再次进入当前画布。
	 *
	 * @param error 绘制异常。
	 */
	private void report(final RuntimeException error) {
		handler.post(new Runnable() {
			@Override
			public void run() {
				if (!disposed && listener != null) {
					listener.error(error.getMessage() == null ? error.getClass().getSimpleName() : error.getMessage());
				}
			}
		});
	}

	/**
	 * 读取绘制日期是否超出范围或被拦截。
	 *
	 * @param date 待绘制日期。
	 * @return 该日期不可选择时为真。
	 */
	public boolean blocked(Calendar date) {
		return !isInRange(date) || onCalendarIntercept(date);
	}

	/**
	 * 刷新所有缓存页面及星期栏。
	 */
	public void repaint() {
		update();
		updateWeekBar();
		invalidateTree(this);
	}

	/**
	 * 使页面树中的缓存绘制失效。
	 *
	 * @param view 页面树节点。
	 */
	private static void invalidateTree(View view) {
		view.invalidate();
		if (view instanceof ViewGroup) {
			ViewGroup group = (ViewGroup) view;
			for (int i = 0; i < group.getChildCount(); i++) {
				invalidateTree(group.getChildAt(i));
			}
		}
	}

	/**
	 * 释放图片、监听与队列回调。
	 */
	public void close() {
		disposed = true;
		listener = null;
		handler.removeCallbacksAndMessages(null);
		images.close();
		setOnCalendarInterceptListener(null);
		setOnCalendarSelectListener(null);
		setOnCalendarRangeSelectListener(null);
		setOnCalendarMultiSelectListener(null);
		setOnMonthChangeListener(null);
		setOnYearChangeListener(null);
		setOnWeekChangeListener(null);
		setOnViewChangeListener(null);
		setOnYearViewChangeListener(null);
		setOnClickCalendarPaddingListener(null);
		setOnCalendarLongClickListener(null);
		es4aDispose();
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		return !disposed && super.dispatchTouchEvent(event);
	}

	/**
	 * 绘制及错误回调。
	 *
	 * @author 树先生 xhwsd@qq.com
	 */
	public interface Listener {

		/**
		 * 执行同步日项绘制。
		 *
		 * @param drawing 局部上下文。
		 * @param date 日期快照。
		 * @param stage 绘制阶段。
		 * @param selected 是否选中。
		 * @param previous 前项是否选中。
		 * @param next 后项是否选中。
		 * @return 是否继续默认绘制。
		 */
		boolean draw(日历绘图 drawing, 日历日期 date, int stage, boolean selected, boolean previous, boolean next);

		/**
		 * 执行同步标题绘制。
		 *
		 * @param drawing 局部上下文。
		 * @param year 年份。
		 * @param month 月份。
		 * @param stage 绘制阶段。
		 * @param week 星期编号。
		 * @return 是否继续默认绘制。
		 */
		boolean heading(日历绘图 drawing, int year, int month, int stage, int week);

		/**
		 * 报告绘制失败。
		 *
		 * @param reason 失败原因。
		 */
		void error(String reason);
	}
}
