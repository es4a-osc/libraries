package simple.library.wsd.xui;

import simple.runtime.android.MainActivity;
import simple.runtime.components.impl.android.视图组件;
import simple.runtime.components.组件容器;
import simple.runtime.events.EventDispatcher;

/**
 * 非文本组件的共用属性和事件实现，具体视图由各组件创建。
 *
 * @author 树先生 xhwsd@qq.com
 */
abstract class XUI视图组件基类 extends 视图组件 implements XUI视图组件, MainActivity.OnDestroyListener {

	// 显式销毁与 Activity 销毁共用同一幂等释放边界。
	private boolean destroyed;

	/**
	 * 创建并绑定组件基础事件。
	 *
	 * @param container 容纳组件的 Simple 容器。
	 */
	protected XUI视图组件基类(组件容器 container) {
		super(container);
		ComponentSupport.bindViewEvents(this, getView());
		ThemeSupport.applyFont(getView());
		MainActivity.getContext().addOnDestroyListener(this);
	}

	@Override
	public boolean 启用() {
		return getView().isEnabled();
	}

	@Override
	public void 启用(boolean enabled) {
		getView().setEnabled(enabled);
	}

	@Override
	public String 内容描述() {
		CharSequence description = getView().getContentDescription();
		return description == null ? "" : description.toString();
	}

	@Override
	public void 内容描述(String description) {
		getView().setContentDescription(description);
	}

	@Override
	public void 置内容边距(int left, int top, int right, int bottom) {
		getView().setPadding(left, top, right, bottom);
	}

	@Override
	public void 被单击() {
		EventDispatcher.dispatchEvent(this, "被单击");
	}

	@Override
	public void 被长按() {
		EventDispatcher.dispatchEvent(this, "被长按");
	}

	@Override
	public void 焦点改变(boolean focused) {
		EventDispatcher.dispatchEvent(this, "焦点改变", focused);
	}

	@Override
	public void 销毁() {
		if (destroyed) {
			return;
		}
		destroyed = true;
		MainActivity.getContext().removeOnDestroyListener(this);

		// 先注销事件并移出父容器，再停止原生定时器、动画及网页资源。
		super.销毁();
		ComponentSupport.releaseView(getView());
	}

	@Override
	public void onDestroy() {
		销毁();
	}

}
