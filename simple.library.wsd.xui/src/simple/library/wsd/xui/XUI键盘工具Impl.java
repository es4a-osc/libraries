package simple.library.wsd.xui;

import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import simple.runtime.android.MainActivity;
import simple.runtime.components.可视组件;
import simple.runtime.components.组件容器;
import simple.runtime.errors.非法参数错误;

/**
 * 使用窗口 Insets 查询软键盘，将显示请求、监听和所属窗口的生命周期绑定。
 * 只注册独立布局观察器，不替换宿主或其它组件的 Insets 监听器。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI键盘工具Impl extends XUI弹层基类 implements XUI键盘工具 {

	private EditText editor;
	private Runnable showTask;
	private View decor;
	private ViewTreeObserver observer;
	private ViewTreeObserver.OnGlobalLayoutListener listener;
	private Boolean previous;

	/**
	 * 创建XUI键盘工具。
	 *
	 * @param container 所属 Simple 窗口。
	 */
	public XUI键盘工具Impl(组件容器 container) {
		super(container);
	}

	@Override
	public void 显示(可视组件 component) {
		View target = requireAnchor(component);
		if (!(target instanceof EditText) || !target.isEnabled() || !target.requestFocus()) {
			throw new 非法参数错误();
		}
		cancelShowRequest();
		editor = (EditText) target;
		final EditText requested = editor;
		showTask = new Runnable() {
			@Override
			public void run() {
				showTask = null;
				if (requested.isAttachedToWindow() && requested.isShown() && requested.hasFocus()) {
					// 等焦点连接后再提交请求，避免 SHOW_FORCED 在离开应用后仍显示输入法。
					new WindowInsetsControllerCompat(MainActivity.getContext().getWindow(), requested)
						.show(WindowInsetsCompat.Type.ime());
				}
			}
		};
		editor.post(showTask);
	}

	@Override
	public void 隐藏() {
		requireVisibleOwner();
		cancelShowRequest();
		new WindowInsetsControllerCompat(MainActivity.getContext().getWindow(), getWindowRootView())
			.hide(WindowInsetsCompat.Type.ime());
	}

	@Override
	public boolean 是否显示() {
		requireVisibleOwner();
		return isKeyboardVisible();
	}

	@Override
	public void 开始监听() {
		requireVisibleOwner();
		关闭();
		decor = getWindowRootView();
		observer = decor.getViewTreeObserver();
		listener = new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				boolean visible = isKeyboardVisible();
				if (previous == null || previous.booleanValue() != visible) {
					previous = visible;
					显示状态改变(visible);
				}
			}
		};
		observer.addOnGlobalLayoutListener(listener);
		listener.onGlobalLayout();
	}

	@Override
	public void 关闭() {
		cancelShowRequest();
		if (listener != null) {
			ViewTreeObserver current = observer != null && observer.isAlive() ? observer
				: decor.getViewTreeObserver();
			if (current.isAlive()) {
				current.removeOnGlobalLayoutListener(listener);
			}
		}
		listener = null;
		observer = null;
		decor = null;
		previous = null;
		editor = null;
	}

	@Override
	public void onViewDetachedFromWindow(View view) {
		if (editor != null) {
			new WindowInsetsControllerCompat(MainActivity.getContext().getWindow(), getWindowRootView())
				.hide(WindowInsetsCompat.Type.ime());
		}
		super.onViewDetachedFromWindow(view);
	}

	@Override
	public void 显示状态改变(boolean visible) {
		dispatchEvent("显示状态改变", visible);
	}

	/**
	 * 移除属于本组件的待执行显示请求。
	 */
	private void cancelShowRequest() {
		if (editor != null && showTask != null) {
			editor.removeCallbacks(showTask);
		}
		showTask = null;
	}

	/**
	 * 取得当前窗口根节点，不依赖 Simple 内容容器的布局类型。
	 *
	 * @return 当前 Activity 的 DecorView。
	 */
	private View getWindowRootView() {
		return MainActivity.getContext().getWindow().getDecorView();
	}

	/**
	 * 使用 AndroidX 对各支持系统的 IME Insets 适配结果，统一屏幕坐标基准。
	 *
	 * @return 输入法是否可见。
	 */
	private boolean isKeyboardVisible() {
		WindowInsetsCompat insets = ViewCompat.getRootWindowInsets(getWindowRootView());
		return insets != null && insets.isVisible(WindowInsetsCompat.Type.ime());
	}
}
