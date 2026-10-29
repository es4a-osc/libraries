package simple.library.wsd.xui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.view.View;

import com.xuexiang.xui.widget.flowlayout.FlowTagLayout;

import simple.runtime.components.组件容器;
import simple.runtime.errors.非法参数错误;
import simple.runtime.events.EventDispatcher;

/**
 * XUI流式标签的原生视图及 Simple 事件适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI流式标签Impl extends XUI视图组件基类 implements XUI流式标签 {

	private int selectionMode;

	// 视图在父类构造期间创建；不得用字段初始化器重置此处状态。
	private FlowTagLayout view;

	// 当前数据用于校验索引及事件文本。
	private String[] items;

	/**
	 * 创建XUI流式标签。
	 *
	 * @param container 容纳组件的 Simple 容器。
	 */
	public XUI流式标签Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected View createView() {
		view = new FlowTagLayout(ComponentSupport.createContext());

		items = new String[0];

		selectionMode = FlowTagLayout.FLOW_TAG_CHECKED_SINGLE;
		view.setTagCheckedMode(selectionMode);

		view.setOnTagClickListener(new FlowTagLayout.OnTagClickListener() {

			@Override
			public void onItemClick(FlowTagLayout parent, View child, int index) {
				项目被选择(index, items[index]);
			}
		});

		view.setOnTagSelectListener(new FlowTagLayout.OnTagSelectListener() {

			@Override
			public void onItemSelect(FlowTagLayout parent, int index, List<Integer> selected) {
				选择改变(ComponentSupport.toIntArray(selected));
			}
		});

		return view;
	}

	@Override
	public void 置单选允许取消(boolean singleCancelable) {
		view.setSingleCancelable(singleCancelable);
	}

	@Override
	public void 清空标签() {
		items = new String[0];
		view.clearTags();
	}

	@Override
	public void 清空选择() {
		view.clearSelection();
		ThemeSupport.applyFont(view);
	}

	@Override
	public void 置子项宽度(int width) {
		view.setChildWidth(width);
	}

	@Override
	public void 置选中位置数组(int[] ps) {
		if (ps == null || (selectionMode == 0 && ps.length != 0) || (selectionMode == 1 && ps.length > 1)) {
			throw new 非法参数错误();
		}
		int[] copy = ps.clone();
		for (int i = 0; i < copy.length; i++) {
			ComponentSupport.validateIndex(copy[i], items.length);
			for (int j = 0; j < i; j++) {
				if (copy[i] == copy[j]) {
					throw new 非法参数错误();
				}
			}
		}
		view.clearSelection();
		ThemeSupport.applyFont(view);
		view.setSelectedPositions(copy);
	}

	@Override
	public void 置项目数组(String[] itemsValue) {
		items = ComponentSupport.copyTextArray(itemsValue);
		view.setItems(items);
		view.clearSelection();
		ThemeSupport.applyFont(view);
	}

	@Override
	public int 取项目数() {
		return items.length;
	}

	@Override
	public int[] 取选中索引数组() {
		return view.getAdapter() == null ? new int[0] : ComponentSupport.toIntArray(view.getAdapter().getSelectedIndexs());
	}

	@Override
	public void 项目被选择(int index, String text) {
		EventDispatcher.dispatchEvent(this, "项目被选择", index, text);
	}

	@Override
	public void 选择改变(int[] indices) {
		EventDispatcher.dispatchEvent(this, "选择改变", indices);
	}

	@Override
	public void 置选择模式(int mode) {
		if (mode < 0 || mode > 2) {
			throw new 非法参数错误();
		}
		selectionMode = mode;
		view.setTagCheckedMode(mode);
		view.clearSelection();
		ThemeSupport.applyFont(view);
	}

	@Override
	public int 取选择模式() {
		return selectionMode;
	}

	@Override
	public void 添加标签(String text) {
		if (text == null) {
			throw new 非法参数错误();
		}
		int[] selected = 取选中索引数组();
		String[] data = Arrays.copyOf(items, items.length + 1);
		data[items.length] = text;
		置项目数组(data);
		置选中位置数组(selected);
	}

	@Override
	public void 移除标签(int index) {
		ComponentSupport.validateIndex(index, items.length);
		int[] previous = 取选中索引数组();
		String[] data = new String[items.length - 1];
		System.arraycopy(items, 0, data, 0, index);
		System.arraycopy(items, index + 1, data, index, items.length - index - 1);
		List<Integer> selected = new ArrayList<Integer>();
		for (int item : previous) {
			if (item != index) {
				selected.add(item > index ? item - 1 : item);
			}
		}
		置项目数组(data);
		置选中位置数组(ComponentSupport.toIntArray(selected));
	}

	@Override
	public String 取标签文本(int index) {
		ComponentSupport.validateIndex(index, items.length);
		return items[index];
	}

}
