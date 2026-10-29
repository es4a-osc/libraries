package simple.library.wsd.xui;

import android.view.View;

import com.xuexiang.xui.widget.edittext.verify.VerifyCodeEditText;

import simple.runtime.components.组件容器;
import simple.runtime.events.EventDispatcher;

/**
 * XUI验证码编辑框的原生视图及 Simple 事件适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI验证码编辑框Impl extends XUI视图组件基类 implements XUI验证码编辑框 {

	// 视图在父类构造期间创建；不得用字段初始化器重置此处状态。
	private VerifyCodeEditText view;

	/**
	 * 创建XUI验证码编辑框。
	 *
	 * @param container 容纳组件的 Simple 容器。
	 */
	public XUI验证码编辑框Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected View createView() {
		view = new VerifyCodeEditText(ComponentSupport.createContext());

		view.setOnInputListener(new VerifyCodeEditText.OnInputListener() {

			@Override
			public void onComplete(String text) {
				输入完成(text);
			}

			@Override
			public void onChange(String text) {
				输入改变(text);
			}

			@Override
			public void onClear() {
				输入清空();
			}
		});

		return view;
	}

	@Override
	public String 取输入值() {
		return view.getInputValue();
	}

	@Override
	public void 清空输入() {
		view.clearInputValue();
	}

	@Override
	public void 置输入格数量(int etNumber) {
		view.setEtNumber(etNumber);
	}

	@Override
	public int 取输入格数量() {
		return view.getEtNumber();
	}

	@Override
	public void 置密码模式(boolean isPwdMode) {
		view.setPwdMode(isPwdMode);
	}

	@Override
	public void 置输入文本(String text) {
		view.getEditText().setText(text);
	}

	@Override
	public void 输入完成(String text) {
		EventDispatcher.dispatchEvent(this, "输入完成", text);
	}

	@Override
	public void 输入改变(String text) {
		EventDispatcher.dispatchEvent(this, "输入改变", text);
	}

	@Override
	public void 输入清空() {
		EventDispatcher.dispatchEvent(this, "输入清空");
	}
}
