package simple.library.wsd.xui;

import android.view.View;

import com.xuexiang.xui.widget.button.SmoothCheckBox;

import simple.runtime.components.组件容器;
import simple.runtime.events.EventDispatcher;

/**
 * XUI复选框的原生视图及 Simple 事件适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI复选框Impl extends XUI视图组件基类 implements XUI复选框 {

	// 视图在父类构造期间创建；不得用字段初始化器重置此处状态。
	private SmoothCheckBox view;

	/**
	 * 创建XUI复选框。
	 *
	 * @param container 容纳组件的 Simple 容器。
	 */
	public XUI复选框Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected View createView() {
		view = new SmoothCheckBox(ComponentSupport.createContext());

		view.setOnCheckedChangeListener(new SmoothCheckBox.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(SmoothCheckBox button, boolean checked) {
				选中改变(checked);
			}
		});

		return view;
	}

	@Override
	public void 切换() {
		view.toggle();
	}

	@Override
	public void 置选中静默(boolean checked) {
		view.setCheckedSilent(checked);
	}

	@Override
	public void 置选中(boolean checked, boolean animate) {
		view.setChecked(checked, animate);
	}

	@Override
	public boolean 取选中() {
		return view.isChecked();
	}

	@Override
	public void 置选中(boolean checked) {
		view.setChecked(checked);
	}

	@Override
	public void 选中改变(boolean checked) {
		EventDispatcher.dispatchEvent(this, "选中改变", checked);
	}
}
