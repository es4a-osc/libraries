package simple.library.wsd.calendarview.internal;

import android.content.Context;
import android.graphics.*;
import android.view.*;
import simple.library.wsd.calendarview.*;
import simple.library.wsd.calendarview.vendor.*;
import simple.library.wsd.calendarview.vendor.Calendar;

/**
 * 各选择模式对应的月周绘制类，共享原生默认样式与 Simple 绘制桥接。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class CalendarCells {

	/**
	 * 禁止创建绘制类集合。
	 */
	private CalendarCells() {
	}

	/**
	 * 默认及单选月视图。
	 *
	 * @author 树先生 xhwsd@qq.com
	 */
	public static final class MonthCell extends MonthView {
		private final Painter painter = new Painter(this);

		/**
		 * 创建对应模式的原生页面。
		 *
		 * @param context 活动上下文。
		 */
		public MonthCell(Context context) {
			super(context);
		}

		@Override
		protected boolean onDrawSelected(Canvas canvas, Calendar date, int x, int y, boolean hasScheme) {
			painter.day(canvas, date, 0, true, false, false, x, y, mItemWidth, mItemHeight, false);
			return true;
		}

		@Override
		protected void onDrawScheme(Canvas canvas, Calendar date, int x, int y) {
			painter.day(canvas, date, 1, isSelected(date), false, false, x, y, mItemWidth, mItemHeight, false);
		}

		@Override
		protected void onDrawText(Canvas canvas, Calendar date, int x, int y, boolean hasScheme, boolean selected) {
			painter.day(canvas, date, 2, selected, false, false, x, y, mItemWidth, mItemHeight, false);
		}
	}

	/**
	 * 默认及单选周视图。
	 *
	 * @author 树先生 xhwsd@qq.com
	 */
	public static final class WeekCell extends WeekView {
		private final Painter painter = new Painter(this);

		/**
		 * 创建对应模式的原生页面。
		 *
		 * @param context 活动上下文。
		 */
		public WeekCell(Context context) {
			super(context);
		}

		@Override
		protected boolean onDrawSelected(Canvas canvas, Calendar date, int x, boolean hasScheme) {
			painter.day(canvas, date, 0, true, false, false, x, 0, mItemWidth, mItemHeight, false);
			return true;
		}

		@Override
		protected void onDrawScheme(Canvas canvas, Calendar date, int x) {
			painter.day(canvas, date, 1, isSelected(date), false, false, x, 0, mItemWidth, mItemHeight, false);
		}

		@Override
		protected void onDrawText(Canvas canvas, Calendar date, int x, boolean hasScheme, boolean selected) {
			painter.day(canvas, date, 2, selected, false, false, x, 0, mItemWidth, mItemHeight, false);
		}
	}

	/**
	 * 范围月视图。
	 *
	 * @author 树先生 xhwsd@qq.com
	 */
	public static final class RangeMonth extends RangeMonthView {
		private final Painter painter = new Painter(this);

		/**
		 * 创建对应模式的原生页面。
		 *
		 * @param context 活动上下文。
		 */
		public RangeMonth(Context context) {
			super(context);
		}

		@Override
		protected boolean onDrawSelected(Canvas canvas, Calendar date, int x, int y, boolean hasScheme,
				boolean previous, boolean next) {
			painter.day(canvas, date, 0, true, previous, next, x, y, mItemWidth, mItemHeight, false);
			return true;
		}

		@Override
		protected void onDrawScheme(Canvas canvas, Calendar date, int x, int y, boolean selected) {
			painter.day(canvas, date, 1, selected, false, false, x, y, mItemWidth, mItemHeight, false);
		}

		@Override
		protected void onDrawText(Canvas canvas, Calendar date, int x, int y, boolean hasScheme, boolean selected) {
			painter.day(canvas, date, 2, selected, false, false, x, y, mItemWidth, mItemHeight, false);
		}
	}

	/**
	 * 范围周视图。
	 *
	 * @author 树先生 xhwsd@qq.com
	 */
	public static final class RangeWeek extends RangeWeekView {
		private final Painter painter = new Painter(this);

		/**
		 * 创建对应模式的原生页面。
		 *
		 * @param context 活动上下文。
		 */
		public RangeWeek(Context context) {
			super(context);
		}

		@Override
		protected boolean onDrawSelected(Canvas canvas, Calendar date, int x, boolean hasScheme, boolean previous,
				boolean next) {
			painter.day(canvas, date, 0, true, previous, next, x, 0, mItemWidth, mItemHeight, false);
			return true;
		}

		@Override
		protected void onDrawScheme(Canvas canvas, Calendar date, int x, boolean selected) {
			painter.day(canvas, date, 1, selected, false, false, x, 0, mItemWidth, mItemHeight, false);
		}

		@Override
		protected void onDrawText(Canvas canvas, Calendar date, int x, boolean hasScheme, boolean selected) {
			painter.day(canvas, date, 2, selected, false, false, x, 0, mItemWidth, mItemHeight, false);
		}
	}

	/**
	 * 多选月视图。
	 *
	 * @author 树先生 xhwsd@qq.com
	 */
	public static final class MultiMonth extends MultiMonthView {
		private final Painter painter = new Painter(this);

		/**
		 * 创建对应模式的原生页面。
		 *
		 * @param context 活动上下文。
		 */
		public MultiMonth(Context context) {
			super(context);
		}

		@Override
		protected boolean onDrawSelected(Canvas canvas, Calendar date, int x, int y, boolean hasScheme,
				boolean previous, boolean next) {
			painter.day(canvas, date, 0, true, previous, next, x, y, mItemWidth, mItemHeight, false);
			return true;
		}

		@Override
		protected void onDrawScheme(Canvas canvas, Calendar date, int x, int y, boolean selected) {
			painter.day(canvas, date, 1, selected, false, false, x, y, mItemWidth, mItemHeight, false);
		}

		@Override
		protected void onDrawText(Canvas canvas, Calendar date, int x, int y, boolean hasScheme, boolean selected) {
			painter.day(canvas, date, 2, selected, false, false, x, y, mItemWidth, mItemHeight, false);
		}
	}

	/**
	 * 多选周视图。
	 *
	 * @author 树先生 xhwsd@qq.com
	 */
	public static final class MultiWeek extends MultiWeekView {
		private final Painter painter = new Painter(this);

		/**
		 * 创建对应模式的原生页面。
		 *
		 * @param context 活动上下文。
		 */
		public MultiWeek(Context context) {
			super(context);
		}

		@Override
		protected boolean onDrawSelected(Canvas canvas, Calendar date, int x, boolean hasScheme, boolean previous,
				boolean next) {
			painter.day(canvas, date, 0, true, previous, next, x, 0, mItemWidth, mItemHeight, false);
			return true;
		}

		@Override
		protected void onDrawScheme(Canvas canvas, Calendar date, int x, boolean selected) {
			painter.day(canvas, date, 1, selected, false, false, x, 0, mItemWidth, mItemHeight, false);
		}

		@Override
		protected void onDrawText(Canvas canvas, Calendar date, int x, boolean hasScheme, boolean selected) {
			painter.day(canvas, date, 2, selected, false, false, x, 0, mItemWidth, mItemHeight, false);
		}
	}

	/**
	 * 提供月份、星期及日期绘制事件的年视图。
	 *
	 * @author 树先生 xhwsd@qq.com
	 */
	public static final class YearCell extends YearView {
		private final Painter painter = new Painter(this);

		/**
		 * 创建年视图中的一个月份。
		 *
		 * @param context 活动上下文。
		 */
		public YearCell(Context context) {
			super(context);
		}

		@Override
		protected void onDrawMonth(Canvas canvas, int year, int month, int x, int y, int width, int height) {
			painter.heading(canvas, year, month, 0, -1, x, y, width, height);
		}

		@Override
		protected void onDrawWeek(Canvas canvas, int week, int x, int y, int width, int height) {
			painter.heading(canvas, mYear, mMonth, 1, week, x, y, width, height);
		}

		@Override
		protected boolean onDrawSelected(Canvas canvas, Calendar date, int x, int y, boolean hasScheme) {
			painter.day(canvas, date, 0, true, false, false, x, y, mItemWidth, mItemHeight, true);
			return true;
		}

		@Override
		protected void onDrawScheme(Canvas canvas, Calendar date, int x, int y) {
			NativeCalendar owner = painter.owner();
			painter.day(canvas, date, 1, owner != null && owner.es4aSelected(date), false, false, x, y, mItemWidth,
					mItemHeight, true);
		}

		@Override
		protected void onDrawText(Canvas canvas, Calendar date, int x, int y, boolean hasScheme, boolean selected) {
			NativeCalendar owner = painter.owner();
			boolean actual = owner != null && owner.es4aSelected(date);
			if (actual && !selected) {
				painter.day(canvas, date, 0, true, false, false, x, y, mItemWidth, mItemHeight, true);
			}
			painter.day(canvas, date, 2, actual, false, false, x, y, mItemWidth, mItemHeight, true);
		}
	}

	/**
	 * 不依赖 TextView 子树的可自定义星期栏。
	 *
	 * @author 树先生 xhwsd@qq.com
	 */
	public static final class Header extends WeekBar {
		private final Painter painter = new Painter(this);
		private int weekStart = 2;

		/**
		 * 创建星期栏。
		 *
		 * @param context 活动上下文。
		 */
		public Header(Context context) {
			super(context);
			setWillNotDraw(false);
		}

		@Override
		protected void onWeekStartChange(int value) {
			weekStart = value;
			invalidate();
		}

		@Override
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);
			NativeCalendar owner = painter.owner();
			if (owner == null) {
				return;
			}
			int width = (getWidth() - getPaddingLeft() - getPaddingRight()) / 7;
			Calendar selected = owner.getSelectedCalendar();
			for (int i = 0; i < 7; i++) {
				int week = (weekStart - 1 + i) % 7;
				painter.heading(canvas, selected.getYear(), selected.getMonth(), 2, week, getPaddingLeft() + i * width,
						0, width, getHeight());
			}
		}
	}

	/**
	 * 每个页面复用一个画笔；Simple 回调关闭时直接在原生层绘制。
	 *
	 * @author 树先生 xhwsd@qq.com
	 */
	private static final class Painter {
		private final View view;
		private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		private final RectF rect = new RectF();
		private NativeCalendar calendar;

		/**
		 * 创建页面画笔。
		 *
		 * @param target 所属原生页面。
		 */
		Painter(View target) {
			view = target;
			paint.setTextAlign(Paint.Align.CENTER);
		}

		/**
		 * 从真实父链定位所属日历，不使用全局组件单例。
		 *
		 * @return 所属日历，尚未附着时为空。
		 */
		NativeCalendar owner() {
			if (calendar == null) {
				ViewParent parent = view.getParent();
				while (parent != null) {
					if (parent instanceof NativeCalendar) {
						calendar = (NativeCalendar) parent;
						break;
					}
					parent = parent.getParent();
				}
			}
			return calendar;
		}

		/**
		 * 按阶段绘制日项。
		 *
		 * @param canvas   原生画布。
		 * @param date     日期。
		 * @param stage    本地阶段。
		 * @param selected 是否选中。
		 * @param previous 前项是否选中。
		 * @param next     后项是否选中。
		 * @param x        左边像素。
		 * @param y        顶边像素。
		 * @param width    宽度像素。
		 * @param height   高度像素。
		 * @param yearView 是否为年视图。
		 */
		void day(Canvas canvas, Calendar date, int stage, boolean selected, boolean previous, boolean next, float x,
				float y, float width, float height, boolean yearView) {
			NativeCalendar owner = owner();
			if (owner == null || width <= 0 || height <= 0) {
				return;
			}
			if (!owner.draw(canvas, date, stage + (yearView ? 3 : 0), selected, previous, next, x, y, width, height)) {
				return;
			}
			CV日历配置 config = owner.style();
			float density = view.getResources().getDisplayMetrics().density;
			float scaled = view.getResources().getDisplayMetrics().scaledDensity;
			float cx = x + width / 2;
			float cy = y + height / 2;
			float radius = Math.max(1, Math.min(width, height) / 2 - (yearView ? 1 : 4) * density);
			paint.setStyle(Paint.Style.FILL);
			paint.setStrokeWidth(2 * density);
			paint.setFakeBoldText(false);
			if (stage == 0) {
				paint.setColor(yearView ? config.年选中颜色() : config.选中背景颜色());
				if (!yearView && config.选择模式() == 2) {
					float top = cy - radius;
					float bottom = cy + radius;
					rect.set(cx - radius, top, cx + radius, bottom);
					canvas.drawRoundRect(rect, radius, radius, paint);
					if (previous) {
						canvas.drawRect(x, top, cx, bottom, paint);
					}
					if (next) {
						canvas.drawRect(cx, top, x + width, bottom, paint);
					}
				} else if (config.内置样式() == 1) {
					canvas.drawRect(cx - radius, cy - radius, cx + radius, cy + radius, paint);
				} else if (config.内置样式() == 2) {
					rect.set(cx - radius, cy - radius, cx + radius, cy + radius);
					canvas.drawRoundRect(rect, 6 * density, 6 * density, paint);
				} else {
					canvas.drawCircle(cx, cy, radius, paint);
				}
			} else if (stage == 1) {
				java.util.List<Calendar.Scheme> marks = date.getSchemes();
				int count = marks == null || marks.isEmpty() ? 1 : Math.min(5, marks.size());
				if (!yearView && config.内置样式() == 3) {
					int progress = marks == null || marks.isEmpty() ? 100
							: Math.max(0, Math.min(100, marks.get(0).getType()));
					paint.setColor(date.getSchemeColor() == 0 ? config.标记颜色() : date.getSchemeColor());
					paint.setStyle(Paint.Style.STROKE);
					rect.set(cx - radius, cy - radius, cx + radius, cy + radius);
					canvas.drawArc(rect, -90, progress * 3.6f, false, paint);
				} else if (yearView || config.内置样式() == 4) {
					for (int i = 0; i < count; i++) {
						int color = marks == null || marks.isEmpty() ? date.getSchemeColor()
								: marks.get(i).getShcemeColor();
						paint.setColor(color == 0 ? config.标记颜色() : color);
						canvas.drawCircle(cx + (i - (count - 1) / 2f) * 6 * density, y + height - 4 * density,
								(yearView ? 1 : 2) * density, paint);
					}
				} else {
					paint.setColor(date.getSchemeColor() == 0 ? config.标记颜色() : date.getSchemeColor());
					float badgeX = x + width - 8 * density;
					float badgeY = y + 8 * density;
					canvas.drawCircle(badgeX, badgeY, 6 * density, paint);
					String label = date.getScheme();
					if (label == null || label.isEmpty()) {
						label = config.默认标记文字();
					}
					if (!label.isEmpty()) {
						label = label.substring(0, label.offsetByCodePoints(0, 1));
						paint.setColor(0xffffffff);
						paint.setTextSize(8 * scaled);
						Paint.FontMetrics metrics = paint.getFontMetrics();
						canvas.drawText(label, badgeX, badgeY - (metrics.ascent + metrics.descent) / 2, paint);
					}
				}
			} else {
				boolean lunar = !yearView && config.显示农历();
				boolean blocked = owner.blocked(date);
				int color = selected ? (yearView ? config.年选中颜色() : config.选中文字颜色())
						: date.isCurrentDay() ? (yearView ? config.年今日颜色() : config.今日文字颜色())
								: yearView ? config.年日期颜色()
										: date.isCurrentMonth() ? config.本月文字颜色() : config.其他月文字颜色();
				if (!selected && !date.isCurrentDay() && date.hasScheme()) {
					color = yearView ? config.年标记颜色() : config.标记文字颜色();
				}
				if (yearView && selected) {
					color = config.选中文字颜色();
				}
				paint.setColor(blocked ? (color & 0x00ffffff) | 0x55000000 : color);
				paint.setTextSize((yearView ? config.年日期字号() : config.日期字号()) * scaled);
				Paint.FontMetrics metrics = paint.getFontMetrics();
				float baseline = cy - (metrics.ascent + metrics.descent) / 2 - (lunar ? height * 0.12f : 0);
				canvas.drawText(String.valueOf(date.getDay()), cx, baseline, paint);
				if (lunar) {
					paint.setTextSize(config.农历字号() * scaled);
					paint.setColor(selected ? config.选中农历颜色()
							: date.isCurrentDay() ? config.今日农历颜色()
									: date.hasScheme() ? config.标记农历颜色()
											: date.isCurrentMonth() ? config.本月农历颜色() : config.其他月农历颜色());
					if (blocked) {
						paint.setAlpha(85);
					}
					metrics = paint.getFontMetrics();
					canvas.drawText(date.getLunar() == null ? "" : date.getLunar(), cx,
							cy + height * 0.21f - (metrics.ascent + metrics.descent) / 2, paint);
				}
			}
		}

		/**
		 * 绘制月份标题和星期标题。
		 *
		 * @param canvas 原生画布。
		 * @param year   公历年。
		 * @param month  公历月。
		 * @param stage  标题阶段。
		 * @param week   星期，月份标题为 -1。
		 * @param x      左边像素。
		 * @param y      顶边像素。
		 * @param width  宽度像素。
		 * @param height 高度像素。
		 */
		void heading(Canvas canvas, int year, int month, int stage, int week, float x, float y, float width,
				float height) {
			NativeCalendar owner = owner();
			if (owner == null || !owner.heading(canvas, year, month, stage, week, x, y, width, height)) {
				return;
			}
			CV日历配置 config = owner.style();
			paint.setStyle(Paint.Style.FILL);
			paint.setFakeBoldText(stage == 0);
			paint.setColor(stage == 0 ? config.年月份颜色() : stage == 1 ? config.年星期颜色() : config.周栏文字颜色());
			float size = stage == 0 ? config.年月份字号() : stage == 1 ? config.年星期字号() : config.周栏字号();
			paint.setTextSize(size * view.getResources().getDisplayMetrics().scaledDensity);
			Paint.FontMetrics metrics = paint.getFontMetrics();
			String text = stage == 0 ? month + "月" : config.取星期文字()[week];
			canvas.drawText(text, x + width / 2, y + height / 2 - (metrics.ascent + metrics.descent) / 2, paint);
		}
	}
}
