package simple.library.wsd.xutil;

import com.xuexiang.xutil.system.ClipboardUtils;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * 前台窗口的系统文本剪贴板；界面线程调用。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class XUtil剪贴板 {

	/**
	 * 函数集仅提供静态成员，不创建实例。
	 */
	private XUtil剪贴板() {
	}

	/**
	 * 把文本放到系统剪贴板；界面线程调用。
	 *
	 * @param text 文本。
	 */
	@SimpleFunction
	public static void 写文本(String text) {
		XUtilSupport.requireMainThread();
		XUtilSupport.context();
		ClipboardUtils.copyText(text);
	}

	/**
	 * 读取系统剪贴板文本，无文本时返回空文本；受系统前台访问限制。
	 *
	 * @return 返回的文本。
	 */
	@SimpleFunction
	public static String 读文本() {
		XUtilSupport.requireMainThread();
		XUtilSupport.context();
		return XUtilSupport.text(ClipboardUtils.getText());
	}
}
