package simple.library.wsd.calendarview.vendor;

import android.content.Context;

/**
 * 为封装层提供上游包内的资源初始化入口。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class CalendarAccess {

	/**
	 * 禁止创建工具实例。
	 */
	private CalendarAccess() {
	}

	/**
	 * 初始化农历、节气及干支文字资源。
	 *
	 * @param context 活动或应用上下文。
	 */
	public static void initialize(Context context) {
		LunarCalendar.init(context);
	}
}
