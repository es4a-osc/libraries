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
public final class 字体图标可选文本框Impl extends WidgetBase implements 字体图标可选文本框 {
	/**
	 * 创建控件。
	 *
	 * @param container 所属容器。
	 */
	public 字体图标可选文本框Impl(组件容器 container) {
		super(container);
		((com.mikepenz.iconics.view.IconicsCheckableTextView) getView())
				.setOnCheckedChangeListener(
						new com.mikepenz.iconics.view.IconicsCheckableTextView
								.OnCheckedChangeListener() {
							@Override
							public void onCheckedChanged(
									com.mikepenz.iconics.view.IconicsCheckableTextView v,
									boolean checked) {
								选中改变(checked);
							}
						});
	}

	@Override
	protected View createView() {
		Support.init();
		return new com.mikepenz.iconics.view.IconicsCheckableTextView(Support.context()) {
			@Override
			public void toggle() {
				setChecked(!isChecked());
			}
		};
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
		ViewIcons.set(text(), 4, checked, true);
		ViewIcons.set(text(), 4, unchecked, false);
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
		return ViewIcons.get(text(), 4, true);
	}

	@Override
	public 图标图像 取未选中图标() {
		check();
		return ViewIcons.get(text(), 4, false);
	}

	@Override
	public void 设置位置选中图标(int position, 图标图像 checked, 图标图像 unchecked) {
		check();
		ViewIcons.set(text(), position, checked, true);
		ViewIcons.set(text(), position, unchecked, false);
	}

	@Override
	public void 设置四侧选中图标(图标图像 checked, 图标图像 unchecked) {
		check();
		ViewIcons.all(text(), checked, true);
		ViewIcons.all(text(), unchecked, false);
	}

	@Override
	public 图标图像 取位置选中图标(int position) {
		check();
		return ViewIcons.get(text(), position, true);
	}
}
