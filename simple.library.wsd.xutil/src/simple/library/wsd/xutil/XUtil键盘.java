package simple.library.wsd.xutil;

import com.xuexiang.xutil.system.KeyboardUtils;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * 操作当前 Simple 窗口的软键盘；界面线程调用。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class XUtil键盘 {

	/**
	 * 函数集仅提供静态成员，不创建实例。
	 */
	private XUtil键盘() {
	}

	/**
	 * 对当前窗口显示软键盘；显示需要窗口内已有可输入焦点。
	 */
	@SimpleFunction
	public static void 显示() {
		XUtilSupport.requireMainThread();
		KeyboardUtils.showSoftInput(XUtilSupport.activity());
	}

	/**
	 * 对当前窗口隐藏软键盘；显示需要窗口内已有可输入焦点。
	 */
	@SimpleFunction
	public static void 隐藏() {
		XUtilSupport.requireMainThread();
		KeyboardUtils.hideSoftInput(XUtilSupport.activity());
	}

	/**
	 * 对当前窗口是否显示软键盘；显示需要窗口内已有可输入焦点。
	 *
	 * @return 判断或操作的结果。
	 */
	@SimpleFunction
	public static boolean 是否显示() {
		XUtilSupport.requireMainThread();
		return KeyboardUtils.isSoftInputVisible(XUtilSupport.activity());
	}
}
