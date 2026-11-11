package simple.library.wsd.citypicker;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.errors.运行错误;

/**
 * 城市数据、资源或选择器状态不符合约定时抛出的可捕获错误。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class CP城市错误 extends 运行错误 {

	private static final long serialVersionUID = 1L;

	/**
	 * 创建没有指定原因的错误。
	 */
	public CP城市错误() {
		super("城市选择器操作失败。");
	}

	/**
	 * 创建包含具体原因的错误。
	 *
	 * @param reason 失败原因。
	 */
	public CP城市错误(String reason) {
		super(reason);
	}

	/**
	 * 保留数据源或窗口调用失败的原始异常。
	 *
	 * @param reason 失败原因。
	 * @param cause  原始异常。
	 */
	CP城市错误(String reason, Throwable cause) {
		super(reason);
		initCause(cause);
	}

	/**
	 * 取得失败原因。
	 *
	 * @return 可读的错误说明。
	 */
	@SimpleFunction
	public String 取说明() {
		return getMessage() == null ? "城市选择器操作失败。" : getMessage();
	}
}
