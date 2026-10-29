package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * XUI倒计时按钮组件。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI倒计时按钮 extends XUI文本组件 {

	/**
	 * 设置启用数量倒计时。
	 *
	 * @param enableCountDown 启用数量倒计时。
	 */
	@SimpleFunction
	void 置启用数量倒计时(boolean enableCountDown);

	/**
	 * 设置倒计时格式。
	 *
	 * @param countDownFormat 倒计时格式。
	 */
	@SimpleFunction
	void 置倒计时格式(String countDownFormat);

	/**
	 * 设置倒计时毫秒。
	 *
	 * @param countDownTime 倒计时毫秒。
	 */
	@SimpleFunction
	void 置倒计时毫秒(long countDownTime);

	/**
	 * 设置间隔毫秒。
	 *
	 * @param interval 间隔毫秒。
	 */
	@SimpleFunction
	void 置刻度间隔(long interval);

	/**
	 * 是否正在执行倒计时。
	 *
	 * @return 是否正在执行倒计时。
	 */
	@SimpleFunction
	boolean 是否数量倒计时中();

	/**
	 * 设置倒计时数据。
	 *
	 * @param count 时长。
	 * @param interval 间隔。
	 * @param countDownFormat 文字格式。
	 */
	@SimpleFunction
	void 置数量倒计时(long count, long interval, String countDownFormat);

	/**
	 * 取消倒计时。
	 */
	@SimpleFunction
	void 取消倒计时();
}
