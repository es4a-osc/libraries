package simple.library.wsd.xui;

import java.util.ArrayList;
import java.util.List;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.xuexiang.xui.widget.grouplist.XUICommonListItemView;
import com.xuexiang.xui.widget.grouplist.XUIGroupListView;

import simple.runtime.components.impl.android.视图组件容器;
import simple.runtime.components.布局;
import simple.runtime.components.组件;
import simple.runtime.components.组件容器;
import simple.runtime.errors.非法参数错误;
import simple.runtime.events.EventDispatcher;

/**
 * 管理原生分组与暂借的 Simple 列表项，按当前模型分派连续索引并恢复组件归属。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI分组列表Impl extends XUI视图组件基类 implements XUI分组列表, 视图组件容器 {

	// 创建视图发生在父类构造中，不能在字段初始化器中覆盖模型。
	private XUIGroupListView view;
	private List<GroupData> groups;

	/**
	 * 分组数据持有行的真实身份，重建标题与分隔线时不重建调用方组件。
	 */
	private static final class GroupData {
		String title;
		String description;
		final List<Row> rows = new ArrayList<Row>();
	}

	/**
	 * 暂借前保存上游 Section 会改动的外观与子布局参数。
	 */
	private static final class Row {
		XUICommonListItemView view;
		XUI分组列表项Impl component;
		BorrowedContent borrowed;
		ViewGroup originalParent;
		List<View> siblingOrder;
		Drawable background;
		int[] padding;
		final List<ViewGroup.LayoutParams> childParameters = new ArrayList<ViewGroup.LayoutParams>();
	}

	/**
	 * 创建分组列表。
	 *
	 * @param container 所属 Simple 容器。
	 */
	public XUI分组列表Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected View createView() {
		view = new XUIGroupListView(ComponentSupport.createContext());
		groups = new ArrayList<GroupData>();
		return view;
	}

	@Override
	public ViewGroup getLayoutManager() {
		return ComponentSupport.getChildLayout(view);
	}

	@Override
	public void addComponent(组件 component) {
		ComponentSupport.addChildComponent(view, component);
	}

	@Override
	public 布局 getLayout() {
		return null;
	}

	@Override
	public int 取分隔样式() {
		return view.getSeparatorStyle();
	}

	@Override
	public void 置分隔样式(int style) {
		view.setSeparatorStyle(style);
		refreshGroups();
	}

	@Override
	public int 取分组数量() {
		return groups.size();
	}

	@Override
	public void 置排列方向(int orientation) {
		view.setOrientation(orientation);
	}

	@Override
	public void 添加分组(String title, String[] items) {
		String heading = requireText(title);
		String[] data = ComponentSupport.copyTextArray(items);
		GroupData group = new GroupData();
		group.title = heading;
		group.description = "";
		for (String text : data) {
			Row row = new Row();
			row.view = view.createItemView(text);
			group.rows.add(row);
		}
		groups.add(group);
		refreshGroups();
	}

	@Override
	public void 删除分组(int index) {
		GroupData group = requireGroup(index);
		clearNativeGroups();
		groups.remove(index);
		for (int row = group.rows.size() - 1; row >= 0; row--) {
			restoreRow(group.rows.get(row));
		}
		refreshGroups();
	}

	@Override
	public void 清空分组() {
		clearNativeGroups();
		for (int group = groups.size() - 1; group >= 0; group--) {
			List<Row> rows = groups.get(group).rows;
			for (int index = rows.size() - 1; index >= 0; index--) {
				restoreRow(rows.get(index));
			}
		}
		groups.clear();
	}

	@Override
	public void 项目被单击(int section, int index, String text) {
		EventDispatcher.dispatchEvent(this, "项目被单击", section, index, text);
	}

	/**
	 * 清除原生 Section 的稀疏索引，不影响直接加入容器的其它子组件。
	 */
	private void clearNativeGroups() {
		for (int index = view.getSectionCount() - 1; index >= 0; index--) {
			view.getSection(index).removeFrom(view);
		}
	}

	/**
	 * 在重建后绑定实时索引，避免删除中间分组或行后继续通知旧位置。
	 */
	private void refreshGroups() {
		clearNativeGroups();
		for (int position = 0; position < groups.size(); position++) {
			final int groupIndex = position;
			GroupData data = groups.get(position);
			XUIGroupListView.Section section = XUIGroupListView.newSection(view.getContext()).setTitle(data.title);
			if (!data.description.isEmpty()) {
				section.setDescription(data.description);
			}
			for (int index = 0; index < data.rows.size(); index++) {
				final int rowIndex = index;
				final Row row = data.rows.get(index);
				section.addItemView(row.view, null);
				// 上游开关行会覆盖点击监听；保留开关切换，同时通知组件与分组事件。
				row.view.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View clicked) {
						if (row.view.getAccessoryType() == XUICommonListItemView.ACCESSORY_TYPE_SWITCH) {
							row.view.getSwitch().toggle();
						}
						if (row.component != null) {
							row.component.被单击();
						}
						项目被单击(groupIndex, rowIndex, String.valueOf(row.view.getText()));
					}
				});
				row.view.setOnLongClickListener(new View.OnLongClickListener() {
					@Override
					public boolean onLongClick(View clicked) {
						if (row.component != null) {
							row.component.被长按();
						}
						项目被长按(groupIndex, rowIndex, String.valueOf(row.view.getText()));
						return true;
					}
				});
			}
			section.addTo(view);
		}
	}

	/**
	 * 验证并取得分组，所有改动先检查边界。
	 *
	 * @param index 分组索引。
	 * @return 对应模型。
	 */
	private GroupData requireGroup(int index) {
		ComponentSupport.validateIndex(index, groups.size());
		return groups.get(index);
	}

	/**
	 * 空文本有效，但空引用不能进入原生 Section。
	 *
	 * @param text 输入文字。
	 * @return 原始文字。
	 */
	private static String requireText(String text) {
		if (text == null) {
			throw new 非法参数错误();
		}
		return text;
	}

	/**
	 * 创建空组时先校验全部参数。
	 *
	 * @param title 标题。
	 * @param description 说明。
	 * @return 新组索引。
	 */
	private int createGroup(String title, String description) {
		GroupData group = new GroupData();
		group.title = requireText(title);
		group.description = requireText(description);
		groups.add(group);
		refreshGroups();
		return groups.size() - 1;
	}

	/**
	 * 借入已配置行，拒绝重复归属；原附属内容及开关监听留在组件自身。
	 *
	 * @param index 目标分组。
	 * @param component 已配置的列表项。
	 */
	private void addConfiguredItem(int index, XUI分组列表项 component) {
		GroupData group = requireGroup(index);
		if (!(component instanceof XUI分组列表项Impl)) {
			throw new 非法参数错误();
		}
		XUI分组列表项Impl item = (XUI分组列表项Impl) component;
		if (item.groupOwner != null) {
			throw new ComponentStateException("列表项已加入分组，请先移除。");
		}
		View target = ComponentOwnership.requireSibling(view, item);
		Row row = new Row();
		row.component = item;
		row.view = (XUICommonListItemView) target;
		row.background = target.getBackground();
		row.padding = new int[] {target.getPaddingLeft(), target.getPaddingTop(), target.getPaddingRight(), target.getPaddingBottom()};
		for (int child = 0; child < row.view.getChildCount(); child++) {
			row.childParameters.add(new RelativeLayout.LayoutParams((RelativeLayout.LayoutParams) row.view.getChildAt(child).getLayoutParams()));
		}
		row.originalParent = (ViewGroup) target.getParent();
		row.siblingOrder = originalSiblingOrder(row.originalParent, target);
		row.borrowed = new BorrowedContent(target, view.getRootView());
		item.groupOwner = this;
		group.rows.add(row);
		refreshGroups();
	}

	/**
	 * 归还借入行；按借入相反顺序调用以恢复相邻组件顺序。
	 *
	 * @param row 待归还的行。
	 */
	private void restoreRow(Row row) {
		if (row.borrowed == null) {
			return;
		}
		row.component.groupOwner = null;
		row.view.setBackground(row.background);
		row.view.setPadding(row.padding[0], row.padding[1], row.padding[2], row.padding[3]);
		for (int child = 0; child < Math.min(row.childParameters.size(), row.view.getChildCount()); child++) {
			row.view.getChildAt(child).setLayoutParams(row.childParameters.get(child));
		}
		row.borrowed.restore();
		restoreSiblingOrder(row);
		row.borrowed = null;
		row.view.setOnClickListener(null);
		ComponentSupport.bindViewEvents(row.component, row.view);
	}

	/**
	 * 移除行并在重建之前归还原组件。
	 *
	 * @param section 分组索引。
	 * @param index 行索引。
	 */
	private void removeConfiguredItem(int section, int index) {
		GroupData group = requireGroup(section);
		ComponentSupport.validateIndex(index, group.rows.size());
		clearNativeGroups();
		restoreRow(group.rows.remove(index));
		refreshGroups();
	}

	/**
	 * 子组件主动销毁时先解除分组归属，防止后续重建复活已销毁视图。
	 *
	 * @param component 正在销毁的列表项。
	 */
	void releaseGroupedItem(XUI分组列表项Impl component) {
		for (int section = 0; section < groups.size(); section++) {
			List<Row> rows = groups.get(section).rows;
			for (int index = 0; index < rows.size(); index++) {
				if (rows.get(index).component == component) {
					removeConfiguredItem(section, index);
					return;
				}
			}
		}
	}

	@Override
	public void 销毁() {
		清空分组();
		super.销毁();
	}

	@Override
	public int 创建分组(String title, String description) {
		return createGroup(title, description);
	}

	@Override
	public void 添加分组项目(int section, XUI分组列表项 component) {
		addConfiguredItem(section, component);
	}

	@Override
	public void 移除分组项目(int section, int index) {
		removeConfiguredItem(section, index);
	}

	@Override
	public void 置分组说明(int section, String description) {
		GroupData group = requireGroup(section);
		group.description = requireText(description);
		refreshGroups();
	}

	@Override
	public void 项目被长按(int section, int index, String text) {
		EventDispatcher.dispatchEvent(this, "项目被长按", section, index, text);
	}


	/**
	 * 同一父容器的暂借行共享首次移动前的顺序，不依赖移除后的相对索引。
	 * 快照仅由仍在分组模型中的行持有，最后一行解除后即可释放。
	 *
	 * @param parent 原父容器。
	 * @param target 本次加入的行，可能在暂借周期内才创建。
	 * @return 本次暂借周期的原相邻视图顺序。
	 */
	private List<View> originalSiblingOrder(ViewGroup parent, View target) {
		for (GroupData group : groups) {
			for (Row row : group.rows) {
				if (row.borrowed != null && row.originalParent == parent) {
					if (!row.siblingOrder.contains(target)) {
						// 新建行按当前后继插入；没有可见后继时放在原暂借行之后。
						int insertion = row.siblingOrder.size();
						for (int index = parent.indexOfChild(target) + 1; index < parent.getChildCount(); index++) {
							int next = row.siblingOrder.indexOf(parent.getChildAt(index));
							if (next >= 0) {
								insertion = next;
								break;
							}
						}
						row.siblingOrder.add(insertion, target);
					}
					return row.siblingOrder;
				}
			}
		}
		List<View> order = new ArrayList<View>();
		for (int index = 0; index < parent.getChildCount(); index++) {
			order.add(parent.getChildAt(index));
		}
		return order;
	}

	/**
	 * 用仍存在的原后继或前驱定位，不移动其它视图或覆盖原布局参数。
	 *
	 * @param row 刚归还的行。
	 */
	private void restoreSiblingOrder(Row row) {
		ViewGroup parent = row.originalParent;
		int original = row.siblingOrder.indexOf(row.view);
		int desired = -1;
		for (int index = original + 1; index < row.siblingOrder.size(); index++) {
			View next = row.siblingOrder.get(index);
			if (next.getParent() == parent) {
				desired = parent.indexOfChild(next);
				break;
			}
		}
		if (desired < 0) {
			for (int index = original - 1; index >= 0; index--) {
				View previous = row.siblingOrder.get(index);
				if (previous.getParent() == parent) {
					desired = parent.indexOfChild(previous) + 1;
					break;
				}
			}
		}
		int current = parent.indexOfChild(row.view);
		if (desired >= 0) {
			if (current < desired) {
				desired--;
			}
			if (current != desired) {
				ViewGroup.LayoutParams parameters = row.view.getLayoutParams();
				parent.removeView(row.view);
				parent.addView(row.view, desired, parameters);
			}
		}
	}

}
