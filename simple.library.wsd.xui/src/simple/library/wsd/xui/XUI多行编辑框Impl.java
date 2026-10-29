package simple.library.wsd.xui;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import com.xuexiang.xui.widget.edittext.MultiLineEditText;

import simple.runtime.components.组件容器;
import simple.runtime.events.EventDispatcher;

/**
 * XUI多行编辑框的原生视图及 Simple 事件适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI多行编辑框Impl extends XUI视图组件基类 implements XUI多行编辑框 {

	// 视图在父类构造期间创建；不得用字段初始化器重置此处状态。
	private MultiLineEditText view;

	/**
	 * 创建XUI多行编辑框。
	 *
	 * @param container 容纳组件的 Simple 容器。
	 */
	public XUI多行编辑框Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected View createView() {
		view = new MultiLineEditText(ComponentSupport.createContext());

		view.getEditText().addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence text, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence text, int start, int before, int count) {
				文本改变(text.toString());
			}

			@Override
			public void afterTextChanged(Editable text) {
			}
		});

		return view;
	}

	@Override
	public void 置内容文本(String content) {
		view.setContentText(content);
	}

	@Override
	public String 取内容文本() {
		return view.getContentText();
	}

	@Override
	public void 置提示文本(String hintText) {
		view.setHintText(hintText);
	}

	@Override
	public void 置内容文本尺寸(int size) {
		view.setContentTextSize(size);
	}

	@Override
	public void 置内容文本颜色(int color) {
		view.setContentTextColor(color);
	}

	@Override
	public void 置提示文本颜色(int color) {
		view.setHintColor(color);
	}

	@Override
	public String 取提示文本() {
		return view.getHintText();
	}

	@Override
	public void 置最大字数(int max_count) {
		view.setMaxCount(max_count);
	}

	@Override
	public void 置忽略中文或英文(boolean ignoreCnOrEn) {
		view.setIgnoreCnOrEn(ignoreCnOrEn);
	}

	@Override
	public void 置显示剩余字数(boolean isShowSurplusNumber) {
		view.setIsShowSurplusNumber(isShowSurplusNumber);
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
	public void 文本改变(String text) {
		EventDispatcher.dispatchEvent(this, "文本改变", text);
	}
}
