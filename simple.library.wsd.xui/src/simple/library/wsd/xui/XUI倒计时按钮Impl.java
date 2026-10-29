package simple.library.wsd.xui;

import android.view.View;

import com.xuexiang.xui.widget.button.CountDownButton;

import simple.runtime.components.组件容器;

/**
 * XUI倒计时按钮的原生视图及 Simple 事件适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI倒计时按钮Impl extends XUI文本组件基类 implements XUI倒计时按钮 {

	// 视图在父类构造期间创建；不得用字段初始化器重置此处状态。
	private CountDownButton view;

	/**
	 * 创建XUI倒计时按钮。
	 *
	 * @param container 容纳组件的 Simple 容器。
	 */
	public XUI倒计时按钮Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected View createView() {
		view = new CountDownButton(ComponentSupport.createContext());

		return view;
	}

	@Override
	public void 置启用数量倒计时(boolean enableCountDown) {
		view.setEnableCountDown(enableCountDown);
	}

	@Override
	public void 置倒计时格式(String countDownFormat) {
		view.setCountDownFormat(countDownFormat);
	}

	@Override
	public void 置倒计时毫秒(long countDownTime) {
		view.setCountDownTime(countDownTime);
	}

	@Override
	public void 置刻度间隔(long interval) {
		view.setInterval(interval);
	}

	@Override
	public boolean 是否数量倒计时中() {
		return view.isCountDownNow();
	}

	@Override
	public void 置数量倒计时(long count, long interval, String countDownFormat) {
		view.setCountDown(count, interval, countDownFormat);
	}

	@Override
	public void 取消倒计时() {
		view.cancelCountDown();
	}
}
