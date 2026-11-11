package simple.library.wsd.xutil;

import java.util.Calendar;
import java.util.GregorianCalendar;

import com.xuexiang.xutil.data.DateUtils;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * 毫秒时间戳、格式转换、日期判断与星期查询。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class XU日期 {

	/**
	 * 函数集仅提供静态成员，不创建实例。
	 */
	private XU日期() {
	}

	/**
	 * 取得当前 Unix 毫秒时间戳。
	 *
	 * @return 返回的长整数值。
	 */
	@SimpleFunction
	public static long 取当前时间() {
		return DateUtils.getNowMills();
	}

	/**
	 * 按指定时区和格式输出时间。
	 *
	 * @param millis  时间戳。
	 * @param pattern 格式。
	 * @param zone    时区标识，例如 Asia/Shanghai 或 UTC；无效标识拒绝。
	 * @return 返回的文本。
	 */
	@SimpleFunction
	public static String 格式化(long millis, String pattern, String zone) {
		return DateUtils.millis2String(millis, XUtilSupport.dateFormat(pattern, zone));
	}

	/**
	 * 严格解析日期时间，格式或日期不匹配时抛出异常。
	 *
	 * @param text    文本。
	 * @param pattern 格式。
	 * @param zone    时区。
	 * @return 返回的长整数值。
	 */
	@SimpleFunction
	public static long 解析(String text, String pattern, String zone) {
		return XUtilSupport.parseDate(text, pattern, zone);
	}

	/**
	 * 按设备当前时区判断时间戳是否属于今天。
	 *
	 * @param millis 时间戳。
	 * @return 判断或操作的结果。
	 */
	@SimpleFunction
	public static boolean 是否今天(long millis) {
		return DateUtils.isToday(millis);
	}

	/**
	 * 按设备时区取星期，星期日为 1，星期六为 7。
	 *
	 * @param millis 时间戳。
	 * @return 返回的整数值。
	 */
	@SimpleFunction
	public static int 取星期(long millis) {
		return DateUtils.getWeekIndex(millis);
	}

	/**
	 * 取得相对当前时间的中文时间描述。
	 *
	 * @param millis 时间戳。
	 * @return 返回的文本。
	 */
	@SimpleFunction
	public static String 取友好间隔(long millis) {
		return DateUtils.getFriendlyTimeSpanByNow(millis);
	}

	/**
	 * 按公历判断闰年。
	 *
	 * @param year 年份。
	 * @return 按公历判断闰年。
	 */
	@SimpleFunction
	public static boolean 是否闰年(int year) {
		return new GregorianCalendar().isLeapYear(year);
	}

	/**
	 * 按指定时区的日历字段偏移，月份末日按日历规则调整。
	 *
	 * @param millis 时间戳。
	 * @param field  1=年，2=月，5=日，11=小时，12=分钟，13=秒，14=毫秒。
	 * @param amount 可为负数。
	 * @param zone   时区。
	 * @return 按指定时区的日历字段偏移，月份末日按日历规则调整。
	 */
	@SimpleFunction
	public static long 偏移(long millis, int field, int amount, String zone) {
		if (field != 1 && field != 2 && field != 5 && field != 11 && field != 12 && field != 13 && field != 14) {
			throw new IllegalArgumentException("不支持的日期字段");
		}
		Calendar c = Calendar.getInstance(XUtilSupport.dateFormat("yyyy", zone).getTimeZone());
		c.setTimeInMillis(millis);
		c.add(field, amount);
		return c.getTimeInMillis();
	}

	/**
	 * 计算第一时间减第二时间后按单位截断的有符号间隔；溢出拒绝。
	 *
	 * @param first  第一时间。
	 * @param second 第二时间。
	 * @param unit   必须大于零，例如一天为86400000。
	 * @return 计算第一时间减第二时间后按单位截断的有符号间隔；溢出拒绝。
	 */
	@SimpleFunction
	public static long 取间隔(long first, long second, long unit) {
		if (unit <= 0) {
			throw new IllegalArgumentException("时间单位必须大于零");
		}
		try {
			return Math.subtractExact(first, second) / unit;
		} catch (ArithmeticException failure) {
			throw XUtilSupport.failure("时间间隔溢出", failure);
		}
	}

	/**
	 * 按指定时区比较两个时间戳是否属于同一公历日。
	 *
	 * @param first  第一时间。
	 * @param second 第二时间。
	 * @param zone   时区。
	 * @return 按指定时区比较两个时间戳是否属于同一公历日。
	 */
	@SimpleFunction
	public static boolean 是否同一天(long first, long second, String zone) {
		java.text.SimpleDateFormat f = XUtilSupport.dateFormat("G yyyy-MM-dd", zone);
		return f.format(new java.util.Date(first)).equals(f.format(new java.util.Date(second)));
	}

	/**
	 * 按设备时区取得生肖。
	 *
	 * @param millis 时间戳。
	 * @return 按设备时区取得生肖。
	 */
	@SimpleFunction
	public static String 取生肖(long millis) {
		return DateUtils.getChineseZodiac(millis);
	}

	/**
	 * 按设备时区取得星座。
	 *
	 * @param millis 时间戳。
	 * @return 按设备时区取得星座。
	 */
	@SimpleFunction
	public static String 取星座(long millis) {
		return DateUtils.getZodiac(millis);
	}
}
