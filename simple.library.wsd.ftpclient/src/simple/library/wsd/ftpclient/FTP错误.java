package simple.library.wsd.ftpclient;

import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.SimpleProperty;
import simple.runtime.errors.运行错误;

/**
 * FTP 或 SFTP 操作被拒绝或失败时可捕获的错误。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class FTP错误 extends 运行错误 {
	private static final long serialVersionUID = 1L;

	/**
	 * 创建带明确原因的传输错误。
	 *
	 * @param message 错误原因。
	 */
	FTP错误(String message) {
		super(message);
	}

	/**
	 * 读取错误原因。
	 *
	 * @return 错误说明。
	 */
	@SimpleProperty
	public String 说明() {
		return getMessage();
	}
}
