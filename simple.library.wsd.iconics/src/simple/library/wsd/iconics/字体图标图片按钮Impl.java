package simple.library.wsd.iconics;

import android.view.View;

import simple.runtime.components.组件容器;

/**
 * 字体图标原生控件适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class 字体图标图片按钮Impl extends WidgetBase implements 字体图标图片按钮 {
	/**
	 * 创建控件。
	 *
	 * @param container 所属容器。
	 */
	public 字体图标图片按钮Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected View createView() {
		Support.init();
		return new com.mikepenz.iconics.view.IconicsImageButton(Support.context());
	}
}
