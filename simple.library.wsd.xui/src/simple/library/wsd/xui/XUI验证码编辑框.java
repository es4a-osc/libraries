package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleEvent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * XUI验证码编辑框组件。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI验证码编辑框 extends XUI视图组件 {

	/**
	 * 获取输入文本。
	 *
	 * @return 输入文本。
	 */
	@SimpleFunction
	String 取输入值();

	/**
	 * 删除输入内容。
	 */
	@SimpleFunction
	void 清空输入();

	/**
	 * 设置输入框个数。
	 *
	 * @param etNumber 输入框个数。
	 */
	@SimpleFunction
	void 置输入格数量(int etNumber);

	/**
	 * 获取输入的位数。
	 *
	 * @return 输入的位数。
	 */
	@SimpleFunction
	int 取输入格数量();

	/**
	 * 设置是否是密码模式 默认false。
	 *
	 * @param isPwdMode 是否是密码模式。
	 */
	@SimpleFunction
	void 置密码模式(boolean isPwdMode);

	/**
	 * 设置验证码内容。
	 *
	 * @param text 文本内容。
	 */
	@SimpleFunction
	void 置输入文本(String text);

	/**
	 * 全部验证码输入格填满时触发。
	 *
	 * @param text 文本内容。
	 */
	@SimpleEvent
	void 输入完成(String text);

	/**
	 * 验证码内容变化时触发。
	 *
	 * @param text 文本内容。
	 */
	@SimpleEvent
	void 输入改变(String text);

	/**
	 * 验证码被清空时触发。
	 */
	@SimpleEvent
	void 输入清空();
}
