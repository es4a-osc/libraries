package simple.library.wsd.xui;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.xuexiang.xui.widget.textview.ExpandableTextView;

import simple.runtime.components.组件容器;
import simple.runtime.events.EventDispatcher;

/**
 * XUI折叠文本的原生视图及 Simple 事件适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI折叠文本Impl extends XUI视图组件基类 implements XUI折叠文本 {

	// 视图在父类构造期间创建；不得用字段初始化器重置此处状态。
	private ExpandableTextView view;

	/**
	 * 创建XUI折叠文本。
	 *
	 * @param container 容纳组件的 Simple 容器。
	 */
	public XUI折叠文本Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected View createView() {
		view = (ExpandableTextView) LayoutInflater.from(ComponentSupport.createContext()).inflate(R.layout.es4a_xui_expandable_text, null);

		view.setOnExpandStateChangeListener(new ExpandableTextView.OnExpandStateChangeListener() {

			@Override
			public void onExpandStateChanged(TextView text, boolean expanded) {
				展开改变(expanded);
			}
		});

		return view;
	}

	@Override
	public void 置文本(String text) {
		view.setText(text);
	}

	@Override
	public String 取文本() {
		return String.valueOf(view.getText());
	}

	@Override
	public void 展开改变(boolean expanded) {
		EventDispatcher.dispatchEvent(this, "展开改变", expanded);
	}
}
