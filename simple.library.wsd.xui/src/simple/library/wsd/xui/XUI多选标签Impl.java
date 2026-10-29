package simple.library.wsd.xui;

import android.view.LayoutInflater;
import android.view.View;

import com.xuexiang.xui.widget.tabbar.MultiTabControlView;

import simple.runtime.components.组件容器;
import simple.runtime.errors.非法参数错误;
import simple.runtime.events.EventDispatcher;

/**
 * XUI多选标签的原生视图及 Simple 事件适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI多选标签Impl extends XUI视图组件基类 implements XUI多选标签 {

	// 视图在父类构造期间创建；不得用字段初始化器重置此处状态。
	private MultiTabControlView view;

	/**
	 * 创建XUI多选标签。
	 *
	 * @param container 容纳组件的 Simple 容器。
	 */
	public XUI多选标签Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected View createView() {
		view = (MultiTabControlView) LayoutInflater.from(ComponentSupport.createContext()).inflate(R.layout.es4a_xui_multi_tab, null);

		view.setOnMultiTabSelectionChangedListener(new MultiTabControlView.OnMultiTabSelectionChangedListener() {

			@Override
			public void newSelection(String title, String value, boolean checked) {
				选择改变(title, value, checked);
			}
		});

		return view;
	}

	@Override
	public boolean 是否选中(int position) {
		return view.isChecked(position);
	}

	@Override
	public void 置是否选中(int position, boolean isChecked) {
		view.setIsChecked(position, isChecked);
	}

	@Override
	public void 置项目数组(String[] itemArray, String[] valueArray) {
		ComponentSupport.validateTabArrays(itemArray, valueArray);
		try {
			view.setItems(itemArray, valueArray);
		} catch (Exception exception) {
			throw new 非法参数错误();
		}
	}

	@Override
	public void 置项目数组(String[] items, String[] values, int[] defaultSelection) {
		ComponentSupport.validateTabArrays(items, values);
		try {
			view.setItems(items, values, defaultSelection);
		} catch (Exception exception) {
			throw new 非法参数错误();
		}
	}

	@Override
	public void 置选择数组(String[] values) {
		view.setSelection(values);
	}

	@Override
	public void 置选择(String value) {
		view.setSelection(value);
	}

	@Override
	public void 置选择状态(String value, boolean isChecked) {
		view.setSelectionStatus(value, isChecked);
	}

	@Override
	public void 置颜色(int primaryColor, int secondaryColor) {
		view.setColors(primaryColor, secondaryColor);
	}

	@Override
	public void 置颜色(int selectedColor, int selectedTextColor, int unselectedColor, int unselectedTextColor) {
		view.setColors(selectedColor, selectedTextColor, unselectedColor, unselectedTextColor);
	}

	@Override
	public void 置等宽度(boolean equalWidth) {
		view.setEqualWidth(equalWidth);
	}

	@Override
	public void 置拉伸(boolean stretch) {
		view.setStretch(stretch);
	}

	@Override
	public void 选择改变(String title, String value, boolean checked) {
		EventDispatcher.dispatchEvent(this, "选择改变", title, value, checked);
	}
}
