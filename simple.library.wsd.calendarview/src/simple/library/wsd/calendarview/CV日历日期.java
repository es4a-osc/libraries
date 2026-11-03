package simple.library.wsd.calendarview;

import simple.runtime.annotations.*;
import simple.library.wsd.calendarview.vendor.Calendar;
import simple.library.wsd.calendarview.internal.DateMath;

/**
 * 已复制的公历日期及农历信息；标记对象再次读取时也返回副本。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class CV日历日期 {
	private Calendar value;

	/**
	 * 创建 1970 年 1 月 1 日的普通日期；使用日历工具创建日期可补齐农历信息。
	 */
	public CV日历日期() {
		value = DateMath.calendar(1970, 1, 1);
		value.setWeek(4);
	}

	/**
	 * 复制引擎日期及标记集合。
	 *
	 * @param source 引擎日期。
	 * @return 独立快照，空或尚未选择的日期返回空引用。
	 */
	public static CV日历日期 from(Calendar source) {
		if (source == null || source.getYear() == 0 || source.getMonth() == 0 || source.getDay() == 0) {
			return null;
		}
		CV日历日期 result = new CV日历日期();
		Calendar copy = new Calendar();
		copy.setYear(source.getYear());
		copy.setMonth(source.getMonth());
		copy.setDay(source.getDay());
		copy.setCurrentDay(source.isCurrentDay());
		copy.setCurrentMonth(source.isCurrentMonth());
		copy.setLunar(source.getLunar());
		copy.setSolarTerm(source.getSolarTerm());
		copy.setGregorianFestival(source.getGregorianFestival());
		copy.setTraditionFestival(source.getTraditionFestival());
		copy.setLeapYear(source.isLeapYear());
		copy.setLeapMonth(source.getLeapMonth());
		copy.setWeek(source.getWeek());
		copy.setWeekend(source.isWeekend());
		copy.setScheme(source.getScheme());
		copy.setSchemeColor(source.getSchemeColor());
		if (source.getLunarCalendar() != null) {
			Calendar lunar = new Calendar();
			lunar.setYear(source.getLunarCalendar().getYear());
			lunar.setMonth(source.getLunarCalendar().getMonth());
			lunar.setDay(source.getLunarCalendar().getDay());
			lunar.setLeapMonth(source.getLunarCalendar().getLeapMonth());
			lunar.setLunar(source.getLunarCalendar().getLunar());
			copy.setLunarCalendar(lunar);
		}
		if (source.getSchemes() != null) {
			java.util.List<Calendar.Scheme> schemes = new java.util.ArrayList<>();
			for (Calendar.Scheme scheme : source.getSchemes()) {
				schemes.add(CV日历标记.from(scheme).nativeValue());
			}
			copy.setSchemes(schemes);
		}
		result.value = copy;
		return result;
	}

	/**
	 * 取回不共享状态的引擎日期。
	 *
	 * @return 引擎日期副本。
	 */
	public Calendar nativeValue() {
		return from(value).value;
	}

	/**
	 * 公历年。
	 *
	 * @return 公历年。
	 */
	@SimpleProperty
	public int 年() {
		return value.getYear();
	}

	/**
	 * 公历月，1 至 12。
	 *
	 * @return 公历月，1 至 12。
	 */
	@SimpleProperty
	public int 月() {
		return value.getMonth();
	}

	/**
	 * 公历日号。
	 *
	 * @return 公历日号。
	 */
	@SimpleProperty
	public int 日() {
		return value.getDay();
	}

	/**
	 * 八位公历日期标识 yyyyMMdd。
	 *
	 * @return 八位公历日期标识 yyyyMMdd。
	 */
	@SimpleProperty
	public String 日期文本() {
		return value.toString();
	}

	/**
	 * 星期编号，0 为周日，6 为周六。
	 *
	 * @return 星期编号，0 为周日，6 为周六。
	 */
	@SimpleProperty
	public int 星期() {
		return value.getWeek();
	}

	/**
	 * 是否为周六或周日。
	 *
	 * @return 是否为周六或周日。
	 */
	@SimpleProperty
	public boolean 是周末() {
		return value.isWeekend();
	}

	/**
	 * 生成快照时是否为今天。
	 *
	 * @return 生成快照时是否为今天。
	 */
	@SimpleProperty
	public boolean 是今天() {
		return value.isCurrentDay();
	}

	/**
	 * 该快照在对应视图中是否属于当前月。
	 *
	 * @return 该快照在对应视图中是否属于当前月。
	 */
	@SimpleProperty
	public boolean 属于当前月() {
		return value.isCurrentMonth();
	}

	/**
	 * 公历年是否闰年。
	 *
	 * @return 公历年是否闰年。
	 */
	@SimpleProperty
	public boolean 是闰年() {
		return value.isLeapYear();
	}

	/**
	 * 农历或优先显示的节日、节气文字。
	 *
	 * @return 农历或优先显示的节日、节气文字。
	 */
	@SimpleProperty
	public String 农历文字() {
		return empty(value.getLunar());
	}

	/**
	 * 农历年；未填充时为 0。
	 *
	 * @return 农历年；未填充时为 0。
	 */
	@SimpleProperty
	public int 农历年() {
		return value.getLunarCalendar() == null ? 0 : value.getLunarCalendar().getYear();
	}

	/**
	 * 农历月；未填充时为 0。
	 *
	 * @return 农历月；未填充时为 0。
	 */
	@SimpleProperty
	public int 农历月() {
		return value.getLunarCalendar() == null ? 0 : value.getLunarCalendar().getMonth();
	}

	/**
	 * 农历日；未填充时为 0。
	 *
	 * @return 农历日；未填充时为 0。
	 */
	@SimpleProperty
	public int 农历日() {
		return value.getLunarCalendar() == null ? 0 : value.getLunarCalendar().getDay();
	}

	/**
	 * 该日是否属于农历闰月。
	 *
	 * @return 该日是否属于农历闰月。
	 */
	@SimpleProperty
	public boolean 是农历闰月() {
		return value.getLunarCalendar() != null && value.getLunarCalendar().getLeapMonth() != 0;
	}

	/**
	 * 当日节气，没有则为空文本。
	 *
	 * @return 当日节气，没有则为空文本。
	 */
	@SimpleProperty
	public String 节气() {
		return empty(value.getSolarTerm());
	}

	/**
	 * 当日公历节日，没有则为空文本。
	 *
	 * @return 当日公历节日，没有则为空文本。
	 */
	@SimpleProperty
	public String 公历节日() {
		return empty(value.getGregorianFestival());
	}

	/**
	 * 当日传统节日，没有则为空文本。
	 *
	 * @return 当日传统节日，没有则为空文本。
	 */
	@SimpleProperty
	public String 传统节日() {
		return empty(value.getTraditionFestival());
	}

	/**
	 * 当日主标记文字，没有则为空文本。
	 *
	 * @return 当日主标记文字，没有则为空文本。
	 */
	@SimpleProperty
	public String 标记文字() {
		return empty(value.getScheme());
	}

	/**
	 * 当日主标记颜色，ARGB。
	 *
	 * @return 当日主标记颜色，ARGB。
	 */
	@SimpleProperty
	public int 标记颜色() {
		return value.getSchemeColor();
	}

	/**
	 * 是否有主标记或多项标记。
	 *
	 * @return 是否有主标记或多项标记。
	 */
	@SimpleProperty
	public boolean 有标记() {
		return value.hasScheme();
	}

	/**
	 * 该公历日 UTC 零点的毫秒时间戳。
	 *
	 * @return 该公历日 UTC 零点的毫秒时间戳。
	 */
	@SimpleProperty
	public long UTC时间戳() {
		return value.getTimeInMillis();
	}

	/**
	 * 读取当日全部标记的独立副本数组。
	 *
	 * @return 标记数组，没有时为空数组。
	 */
	@SimpleFunction
	public CV日历标记[] 取全部标记() {
		if (value.getSchemes() == null) {
			return new CV日历标记[0];
		}
		CV日历标记[] result = new CV日历标记[value.getSchemes().size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = CV日历标记.from(value.getSchemes().get(i));
		}
		return result;
	}

	/**
	 * 把可空引擎文字规范为空文本。
	 *
	 * @param text 原始文字。
	 * @return 非空文字。
	 */
	private static String empty(String text) {
		return text == null ? "" : text;
	}
}
