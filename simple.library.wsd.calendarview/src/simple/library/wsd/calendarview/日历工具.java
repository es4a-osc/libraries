package simple.library.wsd.calendarview;

import simple.runtime.annotations.*;
import simple.runtime.android.MainActivity;
import simple.library.wsd.calendarview.internal.DateMath;
import simple.library.wsd.calendarview.vendor.*;
import simple.library.wsd.calendarview.vendor.Calendar;

/**
 * 校验公历输入并提供公农历、节气和日期运算。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class 日历工具 {

	/**
	 * 阻止实例化静态日期工具。
	 */
	private 日历工具() {
	}

	/**
	 * 按公历年月日创建包含农历信息的快照；不合法日期抛出错误。
	 *
	 * @param year 年。
	 * @param month 月。
	 * @param day 日。
	 * @return 本次计算结果。
	 */
	@SimpleFunction
	public static 日历日期 创建日期(int year, int month, int day) {
		Calendar value = DateMath.calendar(year, month, day);
		CalendarAccess.initialize(MainActivity.getContext());
		LunarCalendar.setupLunarCalendar(value);
		java.util.Calendar today = java.util.Calendar.getInstance();
		value.setCurrentDay(year == today.get(java.util.Calendar.YEAR) && month == today.get(java.util.Calendar.MONTH) + 1 && day == today.get(java.util.Calendar.DAY_OF_MONTH));
		value.setCurrentMonth(year == today.get(java.util.Calendar.YEAR) && month == today.get(java.util.Calendar.MONTH) + 1);
		return 日历日期.from(value);
	}

	/**
	 * 读取系统本地日期并生成包含农历信息的快照。
	 *
	 * @return 本次计算结果。
	 */
	@SimpleFunction
	public static 日历日期 取今天() {
		java.util.Calendar value = java.util.Calendar.getInstance();
		return 创建日期(value.get(java.util.Calendar.YEAR), value.get(java.util.Calendar.MONTH) + 1, value.get(java.util.Calendar.DAY_OF_MONTH));
	}

	/**
	 * 按公历日增加天数，负数表示减少；结果越界时抛出错误。
	 *
	 * @param date 日期。
	 * @param days 天数。
	 * @return 本次计算结果。
	 */
	@SimpleFunction
	public static 日历日期 增加天数(日历日期 date, int days) {
		Calendar value = DateMath.add(checked(date), days);
		return 创建日期(value.getYear(), value.getMonth(), value.getDay());
	}

	/**
	 * 返回第一日期减第二日期的公历天数，不受夏令时影响。
	 *
	 * @param first 第一日期。
	 * @param second 第二日期。
	 * @return 本次计算结果。
	 */
	@SimpleFunction
	public static int 相差天数(日历日期 first, 日历日期 second) {
		return DateMath.differ(checked(first), checked(second));
	}

	/**
	 * 比较公历日期，早于返回 -1，相同返回 0，晚于返回 1。
	 *
	 * @param first 第一日期。
	 * @param second 第二日期。
	 * @return 本次计算结果。
	 */
	@SimpleFunction
	public static int 比较日期(日历日期 first, 日历日期 second) {
		return Integer.signum(相差天数(first, second));
	}

	/**
	 * 读取两个公历日期是否属于同年同月。
	 *
	 * @param first 第一日期。
	 * @param second 第二日期。
	 * @return 本次计算结果。
	 */
	@SimpleFunction
	public static boolean 是否同月(日历日期 first, 日历日期 second) {
		checked(first);
		checked(second);
		return first.年() == second.年() && first.月() == second.月();
	}

	/**
	 * 取得指定公历月份的天数。
	 *
	 * @param year 年。
	 * @param month 月。
	 * @return 本次计算结果。
	 */
	@SimpleFunction
	public static int 取本月天数(int year, int month) {
		DateMath.millis(year, month, 1);
		return CalendarUtil.getMonthDaysCount(year, month);
	}

	/**
	 * 读取公历年份是否为闰年。
	 *
	 * @param year 年。
	 * @return 本次计算结果。
	 */
	@SimpleFunction
	public static boolean 是否闰年(int year) {
		DateMath.millis(year, 1, 1);
		return CalendarUtil.isLeapYear(year);
	}

	/**
	 * 返回农历年、月、日、闰月标志四项；闰月标志为 0 或 1。
	 *
	 * @param date 公历日期。
	 * @return 本次计算结果。
	 */
	@SimpleFunction
	public static int[] 公历转农历(日历日期 date) {
		checked(date);
		return LunarUtil.solarToLunar(date.年(), date.月(), date.日());
	}

	/**
	 * 转换真实农历日期，并通过反向转换校验闰月及日号；结果超出公历支持范围时拒绝。
	 *
	 * @param year 农历年。
	 * @param month 农历月。
	 * @param day 农历日。
	 * @param leap 是否闰月。
	 * @return 本次计算结果。
	 */
	@SimpleFunction
	public static 日历日期 农历转公历(int year, int month, int day, boolean leap) {
		if (year < 1899 || year > 2099 || month < 1 || month > 12 || day < 1 || day > 30) {
			throw new IllegalArgumentException("农历日期超出范围");
		}
		if (day > LunarUtil.es4aMonthDays(year, month, leap)) {
			throw new IllegalArgumentException("农历日号不存在");
		}
		int[] solar = LunarUtil.lunarToSolar(year, month, day, leap);
		int[] back = LunarUtil.solarToLunar(solar[0], solar[1], solar[2]);
		if (back[0] != year || back[1] != month || back[2] != day || back[3] != (leap ? 1 : 0)) {
			throw new IllegalArgumentException("农历日号或闰月不存在");
		}
		return 创建日期(solar[0], solar[1], solar[2]);
	}

	/**
	 * 读取农历年编号对应的天干地支文字，年份为 1899 至 2099。
	 *
	 * @param year 年。
	 * @return 本次计算结果。
	 */
	@SimpleFunction
	public static String 取干支年(int year) {
		if (year < 1899 || year > 2099) {
			throw new IllegalArgumentException("干支年编号须在 1899 至 2099 之间");
		}
		TrunkBranchAnnals.init(MainActivity.getContext());
		return TrunkBranchAnnals.getTrunkBranchYear(year);
	}

	/**
	 * 读取指定年份的节气条目，格式为 yyyyMMdd 加节气名。
	 *
	 * @param year 年。
	 * @return 本次计算结果。
	 */
	@SimpleFunction
	public static String[] 取全年节气(int year) {
		DateMath.millis(year, 1, 1);
		CalendarAccess.initialize(MainActivity.getContext());
		return SolarTermUtil.getSolarTerms(year).clone();
	}

	/**
	 * 读取月视图行数；周起始为 1、2、7，填充方式为 0 固定六行、1 仅本月或 2 自适应。
	 *
	 * @param year 年。
	 * @param month 月。
	 * @param weekStart 周起始。
	 * @param mode 填充方式。
	 * @return 本次计算结果。
	 */
	@SimpleFunction
	public static int 取月份行数(int year, int month, int weekStart, int mode) {
		DateMath.millis(year, month, 1);
		if ((weekStart != 1 && weekStart != 2 && weekStart != 7) || mode < 0 || mode > 2) {
			throw new IllegalArgumentException("周起始或填充方式无效");
		}
		return CalendarUtil.getMonthViewLineCount(year, month, weekStart, mode);
	}

	/**
	 * 复制日期并附加独立标记数组，原日期及调用方数组保持不变；附加数据中的对象仍按引用保留。
	 *
	 * @param date 日期。
	 * @param schemes 标记数组。
	 * @return 带标记的新日期快照。
	 */
	@SimpleFunction
	public static 日历日期 附加标记(日历日期 date, 日历标记[] schemes) {
		if (date == null || schemes == null || schemes.length > 10000) {
			throw new IllegalArgumentException("日期或标记数组无效");
		}
		Calendar value = date.nativeValue();
		java.util.List<Calendar.Scheme> values = new java.util.ArrayList<>();
		for (日历标记 scheme : schemes) {
			if (scheme == null) {
				throw new IllegalArgumentException("标记不能是空引用");
			}
			values.add(scheme.nativeValue());
		}
		value.setSchemes(values);
		value.setScheme(values.isEmpty() ? "" : values.get(0).getScheme());
		value.setSchemeColor(values.isEmpty() ? 0 : values.get(0).getShcemeColor());
		return 日历日期.from(value);
	}

	/**
	 * 按月份行数计算高度，输入及返回使用相同单位；不含星期栏与分隔线。
	 *
	 * @param year 年。
	 * @param month 月。
	 * @param itemHeight 日项高度。
	 * @param weekStart 周起始。
	 * @param mode 月份填充。
	 * @return 日项高度乘以有效行数。
	 */
	@SimpleFunction
	public static float 取月份高度(int year, int month, float itemHeight, int weekStart, int mode) {
		if (!Float.isFinite(itemHeight) || itemHeight <= 0 || itemHeight > 10000) {
			throw new IllegalArgumentException("日项高度须为 0 至 10000 之间的有限正数");
		}
		return 取月份行数(year, month, weekStart, mode) * itemHeight;
	}

	/**
	 * 计算日期在本月排列中的周序号，1 为第一行。
	 *
	 * @param date 日期。
	 * @param weekStart 周起始。
	 * @return 从 1 开始的周序号。
	 */
	@SimpleFunction
	public static int 取月内周序号(日历日期 date, int weekStart) {
		Calendar value = checked(date);
		Calendar first = DateMath.calendar(value.getYear(), value.getMonth(), 1);
		return (DateMath.weekOffset(first, weekStart) + value.getDay() - 1) / 7 + 1;
	}

	/**
	 * 计算日期前面距本周起始日的天数；周起始为 1 周日、2 周一、7 周六。
	 *
	 * @param date 日期。
	 * @param weekStart 周起始。
	 * @return 0 至 6 天。
	 */
	@SimpleFunction
	public static int 取周前偏移(日历日期 date, int weekStart) {
		return DateMath.weekOffset(checked(date), weekStart);
	}

	/**
	 * 计算日期后面距本周末日的天数；周起始为 1、2 或 7。
	 *
	 * @param date 日期。
	 * @param weekStart 周起始。
	 * @return 0 至 6 天。
	 */
	@SimpleFunction
	public static int 取周后偏移(日历日期 date, int weekStart) {
		return 6 - 取周前偏移(date, weekStart);
	}

	/**
	 * 计算包含两个端点的范围跨越几周；终点不能早于起点。
	 *
	 * @param minimum 起点。
	 * @param maximum 终点。
	 * @param weekStart 周起始。
	 * @return 至少一周。
	 */
	@SimpleFunction
	public static int 取范围周数(日历日期 minimum, 日历日期 maximum, int weekStart) {
		Calendar first = checked(minimum);
		int days = DateMath.differ(checked(maximum), first);
		if (days < 0) {
			throw new IllegalArgumentException("范围终点不能早于起点");
		}
		return (DateMath.weekOffset(first, weekStart) + days) / 7 + 1;
	}

	/**
	 * 计算日期所在周相对范围起点的序号；日期不能早于范围起点。
	 *
	 * @param date 日期。
	 * @param minimum 范围起点。
	 * @param weekStart 周起始。
	 * @return 从 1 开始的周序号。
	 */
	@SimpleFunction
	public static int 取范围周序号(日历日期 date, 日历日期 minimum, int weekStart) {
		return 取范围周数(minimum, date, weekStart);
	}

	/**
	 * 取得范围中指定周的起始日期；周序号从 1 开始，结果超出公历支持范围时抛错。
	 *
	 * @param minimum 范围起点。
	 * @param week 周序号。
	 * @param weekStart 周起始。
	 * @return 包含农历信息的周首日。
	 */
	@SimpleFunction
	public static 日历日期 取范围周首日(日历日期 minimum, int week, int weekStart) {
		Calendar first = checked(minimum);
		if (week < 1 || week > 10437) {
			throw new IllegalArgumentException("周序号须为 1 至 10437");
		}
		Calendar result = DateMath.add(first, (week - 1) * 7 - DateMath.weekOffset(first, weekStart));
		return 创建日期(result.getYear(), result.getMonth(), result.getDay());
	}

	/**
	 * 取得农历 1899 至 2099 年普通月或实际闰月的天数；不存在的闰月抛错。
	 *
	 * @param year 农历年。
	 * @param month 农历月。
	 * @param leap 是否闰月。
	 * @return 29 或 30 天。
	 */
	@SimpleFunction
	public static int 取农历月天数(int year, int month, boolean leap) {
		if (year < 1899 || year > 2099) {
			throw new IllegalArgumentException("农历年份须为 1899 至 2099");
		}
		return LunarUtil.es4aMonthDays(year, month, leap);
	}

	/**
	 * 验证日期并取得独立引擎值。
	 *
	 * @param date 输入日期。
	 * @return 已校验的日期副本。
	 */
	private static Calendar checked(日历日期 date) {
		if (date == null) {
			throw new IllegalArgumentException("日期不能是空引用");
		}
		DateMath.millis(date.年(), date.月(), date.日());
		return date.nativeValue();
	}

}
