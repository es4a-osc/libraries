package simple.library.wsd.xui;

import android.view.View;

import com.xuexiang.xui.widget.progress.loading.LoadingViewLayout;

import simple.runtime.components.组件容器;
import simple.runtime.events.EventDispatcher;

/**
 * XUI加载框的原生视图及 Simple 事件适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI加载框Impl extends XUI视图组件基类 implements XUI加载框 {

	// 视图在父类构造期间创建；不得用字段初始化器重置此处状态。
	private LoadingViewLayout view;

	// 上游布局不实现取消协议；封装使用点击当前加载框取消。
	private boolean cancelable;

	/**
	 * 创建XUI加载框。
	 *
	 * @param container 容纳组件的 Simple 容器。
	 */
	public XUI加载框Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected View createView() {
		view = new LoadingViewLayout(ComponentSupport.createContext()) {
			@Override
			public boolean performClick() {
				boolean handled = super.performClick();
				if (cancelable && isLoading()) {
					dismiss();
					已取消();
					return true;
				}
				return handled;
			}
		};

		return view;
	}

	@Override
	public void 置图标缩放(float iconScale) {
		view.setIconScale(iconScale);
	}

	@Override
	public void 置加载图标资源(int iconResId) {
		view.setLoadingIcon(iconResId);
	}

	@Override
	public void 显示() {
		view.show();
	}

	@Override
	public void 隐藏() {
		view.dismiss();
	}

	@Override
	public boolean 是否加载() {
		return view.isLoading();
	}

	@Override
	public void 置可取消(boolean cancelable) {
		this.cancelable = cancelable;
	}

	@Override
	public void 置加载图标(String image) {
		view.setLoadingIcon(ComponentSupport.getDrawable(image));
	}

	@Override
	public void 置提示文本(String text) {
		view.updateMessage(text);
	}

	@Override
	public void 已取消() {
		EventDispatcher.dispatchEvent(this, "已取消");
	}

}
