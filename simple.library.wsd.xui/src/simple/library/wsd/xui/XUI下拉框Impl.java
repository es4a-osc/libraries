package simple.library.wsd.xui;

import java.util.Arrays;

import android.view.View;

import com.xuexiang.xui.widget.spinner.materialspinner.MaterialSpinner;

import simple.runtime.components.组件容器;
import simple.runtime.events.EventDispatcher;

/**
 * XUI下拉框的原生视图及 Simple 事件适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI下拉框Impl extends XUI文本组件基类 implements XUI下拉框 {

	// 视图在父类构造期间创建；不得用字段初始化器重置此处状态。
	private MaterialSpinner view;

	// 当前数据用于校验索引及事件文本。
	private String[] items;

	/**
	 * 创建XUI下拉框。
	 *
	 * @param container 容纳组件的 Simple 容器。
	 */
	public XUI下拉框Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected View createView() {
		view = new MaterialSpinner(ComponentSupport.createContext());

		items = new String[0];

		view.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {

			@Override
			public void onItemSelected(MaterialSpinner spinner, int index, long id, String text) {
				项目被选择(index, text);
			}
		});

		return view;
	}

	@Override
	public void 展开() {
		view.expand();
	}

	@Override
	public void 收起() {
		view.collapse();
	}

	@Override
	public void 置箭头颜色(int color) {
		view.setArrowColor(color);
	}

	@Override
	public void 置下拉最大高度(int height) {
		view.setDropdownMaxHeight(height);
	}

	@Override
	public void 置下拉高度(int height) {
		view.setDropdownHeight(height);
	}

	@Override
	public void 置项目数组(String[] itemsValue) {
		items = ComponentSupport.copyTextArray(itemsValue);
		view.setItems(Arrays.asList(items));
	}

	@Override
	public int 取项目数() {
		return items.length;
	}

	@Override
	public void 选择项目(int index) {
		ComponentSupport.validateIndex(index, items.length);
		view.setSelectedIndex(index);
	}

	@Override
	public int 取选中索引() {
		return items.length == 0 ? -1 : view.getSelectedIndex();
	}

	@Override
	public void 项目被选择(int index, String text) {
		EventDispatcher.dispatchEvent(this, "项目被选择", index, text);
	}
}
