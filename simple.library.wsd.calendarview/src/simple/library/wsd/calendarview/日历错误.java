package simple.library.wsd.calendarview;

import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.SimpleProperty;
import simple.runtime.errors.运行错误;

/**
 * 日历状态、绘制作用域或资源操作无效时抛出的可捕获错误。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class 日历错误 extends 运行错误 {
	private static final long serialVersionUID = 1L;
	private final String reason;

	/**
	 * 创建带通用说明的日历错误。
	 */
	public 日历错误() {
		this("日历操作无效");
	}

	/**
	 * 创建带具体说明的日历错误。
	 *
	 * @param message 错误说明。
	 */
	public 日历错误(String message) {
		super(message);
		reason = message;
	}

	/**
	 * 读取拒绝操作的原因。
	 *
	 * @return 错误说明。
	 */
	@SimpleProperty
	public String 说明() {
		return reason;
	}
}
