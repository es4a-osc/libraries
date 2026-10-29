package simple.library.wsd.xui;

import android.view.View;
import android.view.ViewGroup;

import simple.runtime.android.MainActivity;
import simple.runtime.components.impl.android.视图组件;
import simple.runtime.components.impl.组件Impl;
import simple.runtime.components.可视组件;
import simple.runtime.components.组件容器;
import simple.runtime.errors.非法参数错误;
import simple.runtime.events.EventDispatcher;

/**
 * 绑定非可视 XUI 功能与所属窗口，切窗关闭界面，销毁时注销事件和宿主监听。
 * 不接管 Activity，也不将暂时离开窗口等同于永久销毁组件。
 *
 * @author 树先生 xhwsd@qq.com
 */
abstract class XUI弹层基类 extends 组件Impl implements MainActivity.OnDestroyListener, View.OnAttachStateChangeListener {

	private View owner;
	private boolean destroyed;

	/**
	 * 创建非可视功能；所属窗口的视图须等初始化完成后读取。
	 *
	 * @param container 所属 Simple 窗口。
	 */
	protected XUI弹层基类(组件容器 container) {
		super(container);
		MainActivity.getContext().addOnDestroyListener(this);
	}

	@Override
	public void 初始化() {
		if (owner == null) {
			owner = getNativeView((可视组件) getComponentContainer());
			owner.addOnAttachStateChangeListener(this);
		}
		super.初始化();
	}

	/**
	 * 取得仍属于当前组件的窗口视图，并拒绝向隐藏或销毁窗口弹出界面。
	 *
	 * @return 当前可见窗口的根视图。
	 */
	protected final View requireVisibleOwner() {
		if (destroyed || owner == null || !owner.isAttachedToWindow() || !owner.isShown()) {
			throw new ComponentStateException("请在所属 Simple 窗口显示后调用此功能。");
		}
		return owner;
	}

	/**
	 * 选择器必须覆盖在 Activity 内容帧上，不能追加到 Simple 的纵向窗口布局末尾。
	 * 所属窗口仍由本组件单独监听，离开时移除此覆盖层。
	 *
	 * @return Activity 内容帧。
	 */
	protected final ViewGroup getOverlayContainer() {
		requireVisibleOwner();
		return MainActivity.getContext().findViewById(android.R.id.content);
	}

	/**
	 * 校验锚点，防止向其它窗口或不可见组件显示弹层。
	 *
	 * @param component 锚点组件。
	 * @return 与所属窗口共用视图树的锚点。
	 */
	protected final View requireAnchor(可视组件 component) {
		View root = requireVisibleOwner();
		View target = getNativeView(component);
		View current = target;
		while (current != root && current.getParent() instanceof View) {
			current = (View) current.getParent();
		}
		if (current != root || !target.isShown()) {
			throw new 非法参数错误();
		}
		return target;
	}

	/**
	 * 把公开可视组件转换为原生视图，拒绝非本机组件。
	 *
	 * @param component Simple 可视组件。
	 * @return 原生视图。
	 */
	protected static View getNativeView(可视组件 component) {
		if (!(component instanceof 视图组件)) {
			throw new 非法参数错误();
		}
		return ((视图组件) component).getView();
	}

	/**
	 * 释放完成后不再发送延迟回调。
	 *
	 * @param name 事件名。
	 * @param values 事件参数。
	 */
	protected final void dispatchEvent(String name, Object... values) {
		if (!destroyed) {
			EventDispatcher.dispatchEvent(this, name, values);
		}
	}

	/**
	 * 关闭属于本组件的弹层及延迟任务；实现必须可重复调用。
	 * 此声明承接各弹层组件导出的关闭接口，名称与公开契约保持一致。
	 */
	public abstract void 关闭();

	@Override
	public void onViewAttachedToWindow(View view) {
		// 窗口再次进入时仍可使用组件，由调用者决定何时重新显示。
	}

	@Override
	public void onViewDetachedFromWindow(View view) {
		关闭();
	}

	@Override
	public void 销毁() {
		if (destroyed) {
			return;
		}
		destroyed = true;
		关闭();
		if (owner != null) {
			owner.removeOnAttachStateChangeListener(this);
		}
		MainActivity.getContext().removeOnDestroyListener(this);
		super.销毁();
	}

	@Override
	public void onDestroy() {
		销毁();
	}
}
