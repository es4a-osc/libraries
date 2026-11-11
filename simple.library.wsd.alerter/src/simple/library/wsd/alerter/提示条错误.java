package simple.library.wsd.alerter;

import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.errors.运行错误;

/**
 * 提示条参数、资源或显示状态不符合约定时抛出的可捕获错误。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class 提示条错误 extends 运行错误 {

	private static final long serialVersionUID = 1L;

	/**
	 * 创建未指定原因的错误。
	 */
	public 提示条错误() {
		super("提示条操作失败。");
	}

	/**
	 * 创建包含具体原因的错误。
	 *
	 * @param reason 失败原因。
	 */
	public 提示条错误(String reason) {
		super(reason);
	}

	/**
	 * 保留外部资源或第三方实现抛出的原始异常。
	 *
	 * @param reason 可读的失败原因。
	 * @param cause  原始异常。
	 */
	提示条错误(String reason, Throwable cause) {
		super(reason);
		initCause(cause);
	}

	/**
	 * 取得操作失败的具体原因。
	 *
	 * @return 错误说明。
	 */
	@SimpleFunction
	public String 取说明() {
		return getMessage() == null ? "提示条操作失败。" : getMessage();
	}
}
