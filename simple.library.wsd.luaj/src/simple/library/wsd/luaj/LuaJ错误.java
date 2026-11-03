package simple.library.wsd.luaj;

import simple.runtime.annotations.SimpleObject;
import simple.runtime.errors.运行错误;

/**
 * LuaJ 加载、执行、转换及调用失败。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class LuaJ错误 extends 运行错误 {
	private static final long serialVersionUID = 1L;

	/**
	 * 创建明确失败说明。
	 *
	 * @param message 失败说明。
	 */
	public LuaJ错误(String message) {
		super(message);
	}

	/**
	 * 创建失败说明并保留原因。
	 *
	 * @param message 失败说明。
	 * @param cause 原始失败。
	 */
	LuaJ错误(String message, Throwable cause) {
		super(message);
		initCause(cause);
	}
}
