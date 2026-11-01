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
		Calendar value = DateMath.add(date.nativeValue(), days);
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
		return DateMath.differ(first.nativeValue(), second.nativeValue());
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
		DateMath.millis(date.年(), date.月(), date.日());
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
}
