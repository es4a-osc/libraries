package simple.library.wsd.xui;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

/**
 * 将 Simple 文本分组转换为可展开列表的数据与条目视图。
 *
 * @author 树先生 xhwsd@qq.com
 */
final class GroupedDataAdapter extends BaseExpandableListAdapter {

	private final Context context;
	private final List<String> titles;
	private final List<String[]> items;
	private int selectedGroup = -1;
	private int selectedChild = -1;

	/**
	 * 使用所属组件维护的数据创建适配器。
	 *
	 * @param context 列表主题上下文。
	 * @param titles XUI分组标题，调用方负责数据变更后刷新。
	 * @param items 与标题一一对应的子项目数组。
	 */
	GroupedDataAdapter(Context context, List<String> titles, List<String[]> items) {
		this.context = context;
		this.titles = titles;
		this.items = items;
	}

	/**
	 * 更新单项选择并刷新复用行的高亮。
	 *
	 * @param group 分组索引，-1 表示未选。
	 * @param child 子项索引，-1 表示未选。
	 */
	void setSelection(int group, int child) {
		selectedGroup = group;
		selectedChild = child;
		notifyDataSetChanged();
	}

	@Override
	public int getGroupCount() {
		return titles.size();
	}

	@Override
	public int getChildrenCount(int group) {
		return items.get(group).length;
	}

	@Override
	public Object getGroup(int group) {
		return titles.get(group);
	}

	@Override
	public Object getChild(int group, int child) {
		return items.get(group)[child];
	}

	@Override
	public long getGroupId(int group) {
		return group;
	}

	@Override
	public long getChildId(int group, int child) {
		return child;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public View getGroupView(int group, boolean expanded, View recycled, ViewGroup parent) {
		TextView text = recycled instanceof TextView ? (TextView) recycled : new TextView(context);
		text.setPadding(64, 24, 24, 24);
		text.setText(titles.get(group));
		return text;
	}

	@Override
	public View getChildView(int group, int child, boolean last, View recycled, ViewGroup parent) {
		TextView text = recycled instanceof TextView ? (TextView) recycled : new TextView(context);
		text.setPadding(80, 24, 24, 24);
		text.setText(items.get(group)[child]);
		boolean selected = group == selectedGroup && child == selectedChild;
		text.setActivated(selected);
		text.setBackgroundColor(selected ? 0x2233B5E5 : android.graphics.Color.TRANSPARENT);
		return text;
	}

	@Override
	public boolean isChildSelectable(int group, int child) {
		return true;
	}
}
