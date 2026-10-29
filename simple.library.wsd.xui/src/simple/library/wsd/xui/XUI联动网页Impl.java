package simple.library.wsd.xui;

import android.view.View;

import com.xuexiang.xui.widget.layout.linkage.view.LinkageWebView;

import simple.runtime.components.组件容器;

/**
 * XUI联动网页的原生视图及 Simple 事件适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI联动网页Impl extends XUI视图组件基类 implements XUI联动网页 {

	// 视图在父类构造期间创建；不得用字段初始化器重置此处状态。
	private LinkageWebView view;

	/**
	 * 创建XUI联动网页。
	 *
	 * @param container 容纳组件的 Simple 容器。
	 */
	public XUI联动网页Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected View createView() {
		view = new LinkageWebView(ComponentSupport.createContext());

		return view;
	}

	@Override
	public int 取滚动范围() {
		return view.getScrollRange();
	}

	@Override
	public void 滚动到底部() {
		view.scrollToBottom();
	}

	@Override
	public void 平滑滚动到底部() {
		view.smoothScrollToBottom();
	}

	@Override
	public void 加载网址(String url) {
		view.loadUrl(url);
	}
}
