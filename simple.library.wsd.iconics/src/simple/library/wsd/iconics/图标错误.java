package simple.library.wsd.iconics;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * 字体图标可捕获错误。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class 图标错误 extends simple.runtime.errors.运行错误 {
	private static final long serialVersionUID = 1L;

	/**
	 * 创建独立实例。
	 */
	public 图标错误() {
	}

	/**
	 * 创建包含原因的错误。
	 *
	 * @param reason 原因。
	 */
	public 图标错误(String reason) {
		super(reason);
	}

	/**
	 * 取得失败原因。
	 *
	 * @return 取得失败原因。
	 */
	@SimpleFunction
	public String 取说明() {
		return getMessage() == null ? "图标错误" : getMessage();
	}
}
