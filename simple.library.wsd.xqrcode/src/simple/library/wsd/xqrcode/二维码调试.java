package simple.library.wsd.xqrcode;

import simple.library.wsd.xqrcode.vendor.logs.QCLog;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * 上游日志入口及本库相机会话诊断，不自动记录识别内容。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class 二维码调试 {
	private static volatile String tag = "";

	/**
	 * 静态工具不创建实例。
	 */
	private 二维码调试() {
	}

	/**
	 * 供内部会话输出不含识别内容的状态。
	 *
	 * @param text 状态信息。
	 */
	public static void log(String text) {
		if (!tag.isEmpty()) {
			android.util.Log.d(tag, text);
		}
	}

	/**
	 * 开启全库调试日志；建议只在开发时启用。
	 *
	 * @param value 1 至 23 个字符的日志标签。
	 */
	@SimpleFunction
	public static void 开启(String value) {
		if (value == null || value.isEmpty() || value.length() > 23) {
			throw new 二维码错误("日志标签须为1至23个字符");
		}
		tag = value;
		QCLog.debug(value);
	}

	/**
	 * 关闭本库及上游生成核心日志。
	 */
	@SimpleFunction
	public static void 关闭() {
		tag = "";
		QCLog.debug(false);
	}

	/**
	 * 查询日志开关。
	 *
	 * @return 已开启时为真。
	 */
	@SimpleFunction
	public static boolean 是否开启() {
		return !tag.isEmpty();
	}

	/**
	 * 取得日志标签，关闭时为空文本。
	 *
	 * @return 当前标签；关闭时为空文本。
	 */
	@SimpleFunction
	public static String 取标签() {
		return tag;
	}

	/**
	 * 使用默认标签开启调试日志。
	 */
	@SimpleFunction
	public static void 开启() {
		开启("ES4A-XQRCode");
	}
}
