package simple.library.wsd.xui;

import android.view.View;

import com.xuexiang.xui.widget.textview.ReadMoreTextView;

import simple.runtime.components.组件容器;

/**
 * XUI展开文本的原生视图及 Simple 事件适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI展开文本Impl extends XUI文本组件基类 implements XUI展开文本 {

	// 视图在父类构造期间创建；不得用字段初始化器重置此处状态。
	private ReadMoreTextView view;

	/**
	 * 创建XUI展开文本。
	 *
	 * @param container 容纳组件的 Simple 容器。
	 */
	public XUI展开文本Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected View createView() {
		view = new ReadMoreTextView(ComponentSupport.createContext());

		return view;
	}

	@Override
	public void 置裁剪长度(int trimLength) {
		view.setTrimLength(trimLength);
	}

	@Override
	public void 置颜色可单击文本(int colorClickableText) {
		view.setColorClickableText(colorClickableText);
	}

	@Override
	public void 置裁剪收起文本(String trimCollapsedText) {
		view.setTrimCollapsedText(trimCollapsedText);
	}

	@Override
	public void 置裁剪展开文本(String trimExpandedText) {
		view.setTrimExpandedText(trimExpandedText);
	}

	@Override
	public void 置裁剪模式(int trimMode) {
		view.setTrimMode(trimMode);
	}

	@Override
	public void 置裁剪行数(int trimLines) {
		view.setTrimLines(trimLines);
	}
}
