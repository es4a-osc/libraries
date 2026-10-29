package simple.library.wsd.xui;

import simple.runtime.events.EventDispatcher;

import java.util.regex.Pattern;

import android.view.View;

import com.xuexiang.xui.widget.edittext.ValidatorEditText;
import com.xuexiang.xui.widget.edittext.materialedittext.validation.METValidator;
import com.xuexiang.xui.widget.edittext.materialedittext.validation.NotAllowEmptyValidator;

import simple.runtime.components.组件容器;
import simple.runtime.errors.非法参数错误;

/**
 * XUI校验编辑框的原生视图及 Simple 事件适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI校验编辑框Impl extends XUI文本组件基类 implements XUI校验编辑框 {

	// 视图在父类构造期间创建；不得用字段初始化器重置此处状态。
	private ValidatorEditText view;

	/**
	 * 创建XUI校验编辑框。
	 *
	 * @param container 容纳组件的 Simple 容器。
	 */
	public XUI校验编辑框Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected View createView() {
		view = new ValidatorEditText(ComponentSupport.createContext());
		view.setOnValidateListener(new ValidatorEditText.OnValidateListener() {
			@Override
			public void onValidateError(String input, String message) {
				校验失败(input, message);
			}
		});

		return view;
	}

	@Override
	public void 添加必填规则(String error) {
		validateMessage(error);
		view.addValidator(new NotAllowEmptyValidator(error));
		view.updateValid();
	}

	@Override
	public void 添加长度规则(final int minimum, final int maximum, String error) {
		validateMessage(error);
		if (minimum < 0 || maximum < minimum) {
			throw new 非法参数错误();
		}
		view.addValidator(new METValidator(error) {
			@Override
			public boolean isValid(CharSequence text, boolean isEmpty) {
				return text.length() >= minimum && text.length() <= maximum;
			}
		});
		view.updateValid();
	}

	@Override
	public void 添加正则规则(String expression, String error) {
		validateMessage(error);
		if (expression == null) {
			throw new 非法参数错误();
		}

		// 先编译再登记；非法表达式不能留下部分规则或改变当前输入状态。
		final Pattern pattern = Pattern.compile(expression);
		view.addValidator(new METValidator(error) {
			@Override
			public boolean isValid(CharSequence text, boolean isEmpty) {
				return pattern.matcher(text).matches();
			}
		});
		view.updateValid();
	}

	@Override
	public void 清空校验规则() {
		view.clearValidators();
		// 上游在规则为空时直接返回通过，必须另行清除前一次的错误外观。
		view.setError(null);
		view.updateValid();
	}

	/**
	 * 拒绝无法向用户解释规则失败原因的空提示。
	 *
	 * @param error 校验失败提示。
	 */
	private static void validateMessage(String error) {
		if (error == null || error.trim().isEmpty()) {
			throw new 非法参数错误();
		}
	}

	@Override
	public void 置扩展单击区域尺寸(int extraClickArea) {
		view.setExtraClickAreaSize(extraClickArea);
	}

	@Override
	public boolean 校验() {
		view.updateValid();
		return view.isInputValid();
	}

	@Override
	public boolean 是否输入有效() {
		return view.isInputValid();
	}

	@Override
	public void 置错误(String error) {
		view.setError(error);
	}

	@Override
	public String 取错误消息() {
		CharSequence error = view.getErrorMsg();
		return error == null ? "" : error.toString();
	}

	@Override
	public String 取输入值() {
		return view.getInputValue();
	}

	@Override
	public boolean 是否为空() {
		return view.isEmpty();
	}

	@Override
	public boolean 是否非空() {
		return view.isNotEmpty();
	}

	@Override
	public void 置提示文本(String text) {
		view.setHint(text);
	}

	@Override
	public String 取输入文本() {
		return view.getText().toString();
	}

	@Override
	public void 置输入类型(int type) {
		view.setInputType(type);
	}

	@Override
	public void 置光标位置(int index) {
		ComponentSupport.validateIndex(index, view.length() + 1);
		view.setSelection(index);
	}

	@Override
	public void 校验失败(String input, String message) {
		EventDispatcher.dispatchEvent(this, "校验失败", input, message);
	}

}
