package simple.library.wsd.calendarview.internal;

import java.util.GregorianCalendar;
import java.util.TimeZone;
import simple.library.wsd.calendarview.vendor.Calendar;

/**
 * 与夏令时无关的公历校验、日序和日期运算。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class DateMath {

	/**
	 * 阻止创建工具实例。
	 */
	private DateMath() {
	}

	/**
	 * 将严格公历日期转换为 UTC 零点，拒绝自动滚入下个月。
	 *
	 * @param year 公历年份。
	 * @param month 月份，1 至 12。
	 * @param day 日号。
	 * @return UTC 零点毫秒数。
	 */
	public static long millis(int year, int month, int day) {
		if (year < 1900 || year > 2099) {
			throw new IllegalArgumentException("支持的公历年份为 1900 至 2099");
		}
		GregorianCalendar value = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
		value.clear();
		value.setLenient(false);
		value.set(year, month - 1, day);
		return value.getTimeInMillis();
	}

	/**
	 * 创建已校验的原生公历值，农历信息由使用边界填充。
	 *
	 * @param year 公历年份。
	 * @param month 月份。
	 * @param day 日号。
	 * @return 独立原生日期。
	 */
	public static Calendar calendar(int year, int month, int day) {
		millis(year, month, day);
		Calendar value = new Calendar();
		value.setYear(year);
		value.setMonth(month);
		value.setDay(day);
		return value;
	}

	/**
	 * 按日序增加日期，不用本地时区的 24 小时长度推算。
	 *
	 * @param value 原日期。
	 * @param days 增加的天数，可为负。
	 * @return 范围内的新日期。
	 */
	public static Calendar add(Calendar value, int days) {
		GregorianCalendar result = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
		result.setTimeInMillis(Math.addExact(millis(value.getYear(), value.getMonth(), value.getDay()), Math.multiplyExact((long) days, 86400000L)));
		return calendar(result.get(java.util.Calendar.YEAR), result.get(java.util.Calendar.MONTH) + 1, result.get(java.util.Calendar.DAY_OF_MONTH));
	}

	/**
	 * 计算含符号的公历日差。
	 *
	 * @param first 被减日期。
	 * @param second 减去的日期。
	 * @return 第一日期减第二日期的天数。
	 */
	public static int differ(Calendar first, Calendar second) {
		return (int) ((millis(first.getYear(), first.getMonth(), first.getDay()) - millis(second.getYear(), second.getMonth(), second.getDay())) / 86400000L);
	}
}
