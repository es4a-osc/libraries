package simple.library.wsd.xui;

import android.view.View;

import com.xuexiang.xui.widget.edittext.ClearEditText;

import simple.runtime.components.组件容器;

/**
 * XUI清除编辑框的原生视图及 Simple 事件适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI清除编辑框Impl extends XUI文本组件基类 implements XUI清除编辑框 {

	// 视图在父类构造期间创建；不得用字段初始化器重置此处状态。
	private ClearEditText view;

	/**
	 * 创建XUI清除编辑框。
	 *
	 * @param container 容纳组件的 Simple 容器。
	 */
	public XUI清除编辑框Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected View createView() {
		view = new ClearEditText(ComponentSupport.createContext());

		return view;
	}

	@Override
	public void 置扩展单击区域尺寸(int extraClickArea) {
		view.setExtraClickAreaSize(extraClickArea);
	}

	@Override
	public void 抖动() {
		view.setShakeAnimation();
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
}
