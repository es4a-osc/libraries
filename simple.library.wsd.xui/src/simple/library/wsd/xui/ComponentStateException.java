package simple.library.wsd.xui;

import simple.runtime.errors.运行错误;

/**
 * 将组件状态错误交给 Simple 的错误分支处理，同时保留具体失败原因。
 *
 * @author 树先生 xhwsd@qq.com
 */
final class ComponentStateException extends 运行错误 {

	private static final long serialVersionUID = 1L;

	/**
	 * 创建无法在当前组件状态下执行的操作错误。
	 *
	 * @param message 失败原因。
	 */
	ComponentStateException(String message) {
		super(message);
	}
}
