package simple.library.wsd.iconics;

import android.graphics.*;
import android.view.*;
import android.widget.*;
import com.mikepenz.iconics.*;
import com.mikepenz.iconics.typeface.*;
import java.util.*;
import simple.runtime.annotations.*;
import simple.runtime.components.*;
import simple.runtime.events.EventDispatcher;

/**
 * 字体图标原生控件适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class 字体图标切换按钮Impl extends WidgetBase implements 字体图标切换按钮 {
	/**
	 * 创建控件。
	 *
	 * @param container 所属容器。
	 */
	public 字体图标切换按钮Impl(组件容器 container) {
		super(container);
		if (getView() instanceof CompoundButton) {
			((CompoundButton) getView())
					.setOnCheckedChangeListener(
							new CompoundButton.OnCheckedChangeListener() {
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
		return new com.mikepenz.iconics.view.IconicsCompoundButton(Support.context());
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
		com.mikepenz.iconics.view.IconicsCompoundButton v =
				(com.mikepenz.iconics.view.IconicsCompoundButton) getView();
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
		return 图标图像.wrap(
				((com.mikepenz.iconics.view.IconicsCompoundButton) getView()).getCheckedIcon());
	}

	@Override
	public 图标图像 取未选中图标() {
		check();
		return 图标图像.wrap(
				((com.mikepenz.iconics.view.IconicsCompoundButton) getView()).getUncheckedIcon());
	}
}
