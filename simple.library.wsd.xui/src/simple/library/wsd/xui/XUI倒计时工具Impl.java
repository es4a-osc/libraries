package simple.library.wsd.xui;

import android.view.View;
import android.widget.TextView;

import com.xuexiang.xui.utils.CountDownButtonHelper;

import simple.runtime.components.可视组件;
import simple.runtime.components.组件容器;
import simple.runtime.errors.非法参数错误;

/**
 * 倒计时工具的 XUI 实现与 Simple 生命周期适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI倒计时工具Impl extends XUI弹层基类 implements XUI倒计时工具 {

	/**
	 * 创建XUI倒计时工具。
	 *
	 * @param container 所属 Simple 窗口。
	 */
	public XUI倒计时工具Impl(组件容器 container) {
		super(container);
	}

	private CountDownButtonHelper helper;
	private TextView target;
	private CharSequence originalText;
	private boolean originalEnabled;

	@Override
	public void 开始(可视组件 component, int seconds, int interval) {
		View view = requireAnchor(component);
		if (!(view instanceof TextView) || seconds < 1 || seconds > 86400 || interval < 1 || interval > seconds) {
			throw new 非法参数错误();
		}
		关闭();
		target = (TextView) view;
		originalText = target.getText();
		originalEnabled = target.isEnabled();
		helper = new CountDownButtonHelper(target, seconds, interval);
		helper.setOnCountDownListener(new CountDownButtonHelper.OnCountDownListener() {
			@Override
			public void onCountDown(int time) {
				target.setText(time + "s");
				剩余时间改变(time);
			}

			@Override
			public void onFinished() {
				关闭();
				倒计时结束();
			}
		});
		helper.start();
	}

	@Override
	public void 关闭() {
		if (helper != null) {
			helper.recycle();
			helper = null;
		}
		if (target != null) {
			target.setText(originalText);
			target.setEnabled(originalEnabled);
			target = null;
			originalText = null;
		}
	}

	@Override
	public void 剩余时间改变(int seconds) {
		dispatchEvent("剩余时间改变", seconds);
	}

	@Override
	public void 倒计时结束() {
		dispatchEvent("倒计时结束");
	}

}
