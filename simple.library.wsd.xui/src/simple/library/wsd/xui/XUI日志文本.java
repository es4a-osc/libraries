package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleDataElement;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * XUI日志文本组件。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI日志文本 extends XUI文本组件 {

	/**
	 * 清除日志。
	 */
	@SimpleFunction
	void 清空日志();

	/**
	 * 按级别追加一行日志并滚动到末尾，0 普通，1 成功，2 警告，3 错误，4 自定义。
	 *
	 * @param text 日志文字。
	 * @param level 日志级别常量。
	 */
	@SimpleFunction
	void 追加日志(String text, int level);

	/**
	 * 普通日志。
	 */
	@SimpleDataElement
	int 日志级别_普通 = 0;

	/**
	 * 成功日志。
	 */
	@SimpleDataElement
	int 日志级别_成功 = 1;

	/**
	 * 警告日志。
	 */
	@SimpleDataElement
	int 日志级别_警告 = 2;

	/**
	 * 错误日志。
	 */
	@SimpleDataElement
	int 日志级别_错误 = 3;

	/**
	 * 自定义日志。
	 */
	@SimpleDataElement
	int 日志级别_自定义 = 4;

}
