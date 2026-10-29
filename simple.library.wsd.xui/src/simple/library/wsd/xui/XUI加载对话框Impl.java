package simple.library.wsd.xui;

import android.app.Dialog;

import com.xuexiang.xui.widget.dialog.LoadingDialog;
import com.xuexiang.xui.widget.dialog.MiniLoadingDialog;
import com.xuexiang.xui.widget.progress.loading.LoadingCancelListener;

import simple.runtime.components.组件容器;
import simple.runtime.errors.非法参数错误;

/**
 * 加载对话框的 XUI 实现与 Simple 生命周期适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI加载对话框Impl extends XUI弹层基类 implements XUI加载对话框 {

	/**
	 * 创建XUI加载对话框。
	 *
	 * @param container 所属 Simple 窗口。
	 */
	public XUI加载对话框Impl(组件容器 container) {
		super(container);
	}

	private Dialog dialog;

	@Override
	public void 显示(String message, boolean mini, boolean cancelable) {
		requireVisibleOwner();
		关闭();
		LoadingCancelListener listener = new LoadingCancelListener() {
			@Override
			public void onCancelLoading() {
				被取消();
			}
		};
		if (mini) {
			MiniLoadingDialog loading = new MiniLoadingDialog(ComponentSupport.createContext(), message);
			loading.setCancelable(cancelable);
			loading.setLoadingCancelListener(listener);
			dialog = loading;
		} else {
			LoadingDialog loading = new LoadingDialog(ComponentSupport.createContext(), message);
			loading.setCancelable(cancelable);
			loading.setLoadingCancelListener(listener);
			dialog = loading;
		}
		dialog.show();
	}

	@Override
	public void 置提示文本(String message) {
		if (!是否显示()) {
			throw new 非法参数错误();
		}
		if (dialog instanceof LoadingDialog) {
			((LoadingDialog) dialog).updateMessage(message);
		} else {
			((MiniLoadingDialog) dialog).updateMessage(message);
		}
	}

	@Override
	public boolean 是否显示() {
		return dialog != null && dialog.isShowing();
	}

	@Override
	public void 关闭() {
		if (dialog != null) {
			dialog.dismiss();
			dialog = null;
		}
	}

	@Override
	public void 被取消() {
		dispatchEvent("被取消");
	}

}
