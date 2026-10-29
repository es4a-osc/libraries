package simple.library.wsd.xutil;

import com.xuexiang.xutil.data.DateUtils;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * 毫秒时间戳、格式转换、日期判断与星期查询。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class XUtil日期 {

	/**
	 * 函数集仅提供静态成员，不创建实例。
	 */
	private XUtil日期() {
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
	 * @param millis 时间戳。
	 * @param pattern 格式。
	 * @param zone 时区标识，例如 Asia/Shanghai 或 UTC；无效标识拒绝。
	 * @return 返回的文本。
	 */
	@SimpleFunction
	public static String 格式化(long millis, String pattern, String zone) {
		return DateUtils.millis2String(millis, XUtilSupport.dateFormat(pattern, zone));
	}

	/**
	 * 严格解析日期时间，格式或日期不匹配时抛出异常。
	 *
	 * @param text 文本。
	 * @param pattern 格式。
	 * @param zone 时区。
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
}
