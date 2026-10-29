package simple.library.wsd.xui;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

import android.view.View;
import android.widget.ExpandableListView;

import com.xuexiang.xui.widget.XUIWrapContentExpandableListView;

import simple.runtime.components.组件容器;
import simple.runtime.errors.非法参数错误;
import simple.runtime.events.EventDispatcher;

/**
 * XUI自适应分组列表的原生视图及 Simple 事件适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI自适应分组列表Impl extends XUI视图组件基类 implements XUI自适应分组列表 {

	// 视图在父类构造期间创建；不得用字段初始化器重置此处状态。
	private XUIWrapContentExpandableListView view;

	// 分组标题与子项目保持相同顺序。
	private List<String> groupTitles;

	// 每个分组的独立文本数组。
	private List<String[]> groupItems;

	// 原生分组适配器随数据变更刷新。
	private GroupedDataAdapter adapter;
	private int selectedGroup;
	private int selectedChild;

	/**
	 * 创建XUI自适应分组列表。
	 *
	 * @param container 容纳组件的 Simple 容器。
	 */
	public XUI自适应分组列表Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected View createView() {
		view = new XUIWrapContentExpandableListView(ComponentSupport.createContext());

		groupTitles = new ArrayList<String>();
		groupItems = new ArrayList<String[]>();
		selectedGroup = -1;
		selectedChild = -1;
		adapter = new GroupedDataAdapter(view.getContext(), groupTitles, groupItems);
		view.setAdapter(adapter);

		view.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

			@Override
			public boolean onChildClick(ExpandableListView parent, View child, int group, int index, long id) {
				项目被单击(group, index, groupItems.get(group)[index]);
				return true;
			}
		});

		return view;
	}

	@Override
	public void 添加分组(String title, String[] items) {
		String[] data = ComponentSupport.copyTextArray(items);
		if (title == null) {
			throw new 非法参数错误();
		}
		groupTitles.add(title);
		groupItems.add(data);
		adapter.setSelection(selectedGroup, selectedChild);
	}

	@Override
	public void 清空分组() {
		selectedGroup = -1;
		selectedChild = -1;
		groupTitles.clear();
		groupItems.clear();
		adapter.setSelection(selectedGroup, selectedChild);
	}

	@Override
	public void 展开分组(int index) {
		ComponentSupport.validateIndex(index, groupTitles.size());
		view.expandGroup(index);
	}

	@Override
	public void 收起分组(int index) {
		ComponentSupport.validateIndex(index, groupTitles.size());
		view.collapseGroup(index);
	}

	@Override
	public void 置最大高度(int height) {
		if (height < 0) {
			throw new 非法参数错误();
		}
		view.setMaxHeight(height);
		view.requestLayout();
	}

	@Override
	public void 项目被单击(int group, int index, String text) {
		置选中项目(group, index);
		EventDispatcher.dispatchEvent(this, "项目被单击", group, index, text);
	}

	@Override
	public int 取分组数() {
		return groupTitles.size();
	}

	@Override
	public int 取项目数(int group) {
		ComponentSupport.validateIndex(group, groupItems.size());
		return groupItems.get(group).length;
	}

	@Override
	public String 取项目文本(int group, int index) {
		validateItem(group, index);
		return groupItems.get(group)[index];
	}

	@Override
	public void 置项目文本(int group, int index, String text) {
		validateItem(group, index);
		if (text == null) {
			throw new 非法参数错误();
		}
		groupItems.get(group)[index] = text;
		adapter.setSelection(selectedGroup, selectedChild);
	}

	@Override
	public void 更新分组(int group, String title, String[] items) {
		ComponentSupport.validateIndex(group, groupTitles.size());
		String[] data = ComponentSupport.copyTextArray(items);
		if (title == null) {
			throw new 非法参数错误();
		}
		if (selectedGroup == group) {
			selectedGroup = -1;
			selectedChild = -1;
		}
		groupTitles.set(group, title);
		groupItems.set(group, data);
		adapter.setSelection(selectedGroup, selectedChild);
	}

	@Override
	public void 移除分组(int group) {
		ComponentSupport.validateIndex(group, groupTitles.size());
		view.collapseGroup(group);
		groupTitles.remove(group);
		groupItems.remove(group);
		if (selectedGroup == group) {
			selectedGroup = -1;
			selectedChild = -1;
		} else if (selectedGroup > group) {
			selectedGroup--;
		}
		adapter.setSelection(selectedGroup, selectedChild);
	}

	@Override
	public void 添加项目(int group, String text) {
		ComponentSupport.validateIndex(group, groupTitles.size());
		if (text == null) {
			throw new 非法参数错误();
		}
		String[] old = groupItems.get(group);
		String[] next = Arrays.copyOf(old, old.length + 1);
		next[old.length] = text;
		groupItems.set(group, next);
		adapter.setSelection(selectedGroup, selectedChild);
	}

	@Override
	public void 移除项目(int group, int index) {
		validateItem(group, index);
		String[] old = groupItems.get(group);
		String[] next = new String[old.length - 1];
		System.arraycopy(old, 0, next, 0, index);
		System.arraycopy(old, index + 1, next, index, old.length - index - 1);
		groupItems.set(group, next);
		if (selectedGroup == group) {
			if (selectedChild == index) {
				selectedGroup = -1;
				selectedChild = -1;
			} else if (selectedChild > index) {
				selectedChild--;
			}
		}
		adapter.setSelection(selectedGroup, selectedChild);
	}

	@Override
	public void 置选中项目(int group, int index) {
		if (group != -1 || index != -1) {
			validateItem(group, index);
		}
		selectedGroup = group;
		selectedChild = index;
		adapter.setSelection(group, index);
	}

	@Override
	public int 取选中分组() {
		return selectedGroup;
	}

	@Override
	public int 取选中项目() {
		return selectedChild;
	}

	@Override
	public void 滚动到项目(int group, int index, boolean smooth) {
		validateItem(group, index);
		view.expandGroup(group);
		int position = view.getFlatListPosition(ExpandableListView.getPackedPositionForChild(group, index));
		if (smooth) {
			view.smoothScrollToPosition(position);
		} else {
			view.setSelection(position);
		}
	}

	/**
	 * 统一验证二维索引，拒绝负数和超出当前数据的项目。
	 *
	 * @param group 分组索引。
	 * @param index 组内项目索引。
	 */
	private void validateItem(int group, int index) {
		ComponentSupport.validateIndex(group, groupItems.size());
		ComponentSupport.validateIndex(index, groupItems.get(group).length);
	}
}
