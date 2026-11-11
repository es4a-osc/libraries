package simple.library.wsd.iconics;

import android.view.View;
import android.widget.Checkable;
import android.widget.CompoundButton;

import simple.runtime.components.组件容器;
import simple.runtime.events.EventDispatcher;

/**
 * 字体图标原生控件适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class 字体图标复选框Impl extends WidgetBase implements 字体图标复选框 {
	/**
	 * 创建控件。
	 *
	 * @param container 所属容器。
	 */
	public 字体图标复选框Impl(组件容器 container) {
		super(container);
		if (getView() instanceof CompoundButton) {
			((CompoundButton) getView()).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton v, boolean checked) {
					选中改变(checked);
				}
			});
		}
	}

	@Override
	protected View createView() {
		Support.init();
		return new com.mikepenz.iconics.view.IconicsCheckBox(Support.context());
	}

	@Override
	public boolean 是否选中() {
		check();
		return ((Checkable) getView()).isChecked();
	}

	@Override
	public void 设置选中(boolean checked) {
		check();
		((Checkable) getView()).setChecked(checked);
	}

	@Override
	public void 设置选中图标(图标图像 checked, 图标图像 unchecked) {
		check();
		if (checked != null) {
			checked.check();
		}
		if (unchecked != null) {
			unchecked.check();
		}
		com.mikepenz.iconics.view.IconicsCheckBox v = (com.mikepenz.iconics.view.IconicsCheckBox) getView();
		v.setCheckedIcon(checked == null ? null : Support.copy(checked.value));
		v.setUncheckedIcon(unchecked == null ? null : Support.copy(unchecked.value));
	}

	@Override
	public void 选中改变(boolean checked) {
		if (!destroyed) {
			EventDispatcher.dispatchEvent(this, "选中改变", checked);
		}
	}

	@Override
	public void 切换选中() {
		check();
		设置选中(!是否选中());
	}

	@Override
	public 图标图像 取选中图标() {
		check();
		return 图标图像.wrap(((com.mikepenz.iconics.view.IconicsCheckBox) getView()).getCheckedIcon());
	}

	@Override
	public 图标图像 取未选中图标() {
		check();
		return 图标图像.wrap(((com.mikepenz.iconics.view.IconicsCheckBox) getView()).getUncheckedIcon());
	}
}
