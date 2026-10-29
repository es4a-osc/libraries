package simple.library.wsd.xui;

import android.view.LayoutInflater;
import android.view.View;

import com.xuexiang.xui.widget.tabbar.TabControlView;

import simple.runtime.components.组件容器;
import simple.runtime.errors.非法参数错误;
import simple.runtime.events.EventDispatcher;

/**
 * XUI单选标签的原生视图及 Simple 事件适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI单选标签Impl extends XUI视图组件基类 implements XUI单选标签 {

	// 视图在父类构造期间创建；不得用字段初始化器重置此处状态。
	private TabControlView view;

	/**
	 * 创建XUI单选标签。
	 *
	 * @param container 容纳组件的 Simple 容器。
	 */
	public XUI单选标签Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected View createView() {
		view = (TabControlView) LayoutInflater.from(ComponentSupport.createContext()).inflate(R.layout.es4a_xui_single_tab, null);

		view.setOnTabSelectionChangedListener(new TabControlView.OnTabSelectionChangedListener() {

			@Override
			public void newSelection(String title, String value) {
				选择改变(title, value);
			}
		});

		return view;
	}

	@Override
	public String 取选中() {
		return view.getChecked();
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
	public void 置项目数组(String[] items, String[] values, int defaultSelection) {
		ComponentSupport.validateTabArrays(items, values);
		try {
			view.setItems(items, values, defaultSelection);
		} catch (Exception exception) {
			throw new 非法参数错误();
		}
	}

	@Override
	public void 置选择(String value) {
		view.setSelection(value);
	}

	@Override
	public void 置选择(String value, boolean isSilent) {
		view.setSelection(value, isSilent);
	}

	@Override
	public void 置选择标题(String title) {
		view.setSelectionTitle(title);
	}

	@Override
	public void 置选择标题(String title, boolean isSilent) {
		view.setSelectionTitle(title, isSilent);
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
	public void 选择改变(String title, String value) {
		EventDispatcher.dispatchEvent(this, "选择改变", title, value);
	}
}
