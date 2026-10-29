package simple.library.wsd.xui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.PopupWindow;

import com.xuexiang.xui.adapter.simple.AdapterItem;
import com.xuexiang.xui.adapter.simple.ExpandableItem;
import com.xuexiang.xui.adapter.simple.XUISimpleAdapter;
import com.xuexiang.xui.adapter.simple.XUISimpleExpandableListAdapter;
import com.xuexiang.xui.widget.popupwindow.popup.XUIBasePopup;
import com.xuexiang.xui.widget.popupwindow.popup.XUIPopup;
import com.xuexiang.xui.widget.popupwindow.popup.XUISimpleExpandablePopup;
import com.xuexiang.xui.widget.popupwindow.popup.XUISimplePopup;

import simple.runtime.components.可视组件;
import simple.runtime.components.组件容器;
import simple.runtime.errors.非法参数错误;

/**
 * 弹出菜单的 XUI 实现与 Simple 生命周期适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI弹出菜单Impl extends XUI弹层基类 implements XUI弹出菜单 {

	private Boolean separators;
	private Boolean singleExpandedGroup;

	private int direction = 1;
	private int offsetX;
	private int offsetTop;
	private int offsetBottom;
	private int[] disabledItems = new int[0];

	/**
	 * 创建XUI弹出菜单。
	 *
	 * @param container 所属 Simple 窗口。
	 */
	public XUI弹出菜单Impl(组件容器 container) {
		super(container);
	}

	private XUIBasePopup popup;

	@Override
	public void 显示列表(可视组件 anchor, String[] items, String[] images, int width) {
		View target = requireAnchor(anchor);
		final String[] data = ComponentSupport.copyTextArray(items);
		if (width <= 0 || data.length == 0) {
			throw new 非法参数错误();
		}
		List<AdapterItem> entries = createMenuItems(data, images);
		final boolean[] enabled = createEnabled(data.length);
		关闭();
		if (popup != null) {
			throw new ComponentStateException("关闭事件已打开新的菜单。");
		}
		final XUISimplePopup<?> menu = new XUISimplePopup<>(ComponentSupport.createContext(), new XUISimpleAdapter(ComponentSupport.createContext(), entries) {
			@Override
			public boolean areAllItemsEnabled() {
				return false;
			}

			@Override
			public boolean isEnabled(int position) {
				return enabled[position];
			}

			@Override
			public View getView(int position, View recycled, ViewGroup parent) {
				View row = super.getView(position, recycled, parent);
				row.setEnabled(enabled[position]);
				row.setAlpha(enabled[position] ? 1.0f : 0.4f);
				ThemeSupport.applyFont(row);
				return row;
			}
		});
		if (separators != null) {
			menu.setHasDivider(separators);
		}
		menu.create(width, Math.min(requireVisibleOwner().getHeight(), 800), (AdapterView.OnItemClickListener) null);
		menu.getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View item, int position, long id) {
				if (!enabled[position]) {
					return;
				}
				menu.dismiss();
				项目被单击(-1, position, data[position]);
			}
		});
		showPopup(menu, target);
	}

	@Override
	public void 显示分组(可视组件 anchor, String[] groups, String[] groupImages, String[] items,
		String[] images, int[] parents, int width) {
		View target = requireAnchor(anchor);
		String[] titles = ComponentSupport.copyTextArray(groups);
		final String[] data = ComponentSupport.copyTextArray(items);
		if (parents == null || parents.length != data.length || titles.length == 0 || width <= 0) {
			throw new 非法参数错误();
		}
		List<AdapterItem> groupItems = createMenuItems(titles, groupImages);
		List<AdapterItem> childItems = createMenuItems(data, images);
		final boolean[] enabled = createEnabled(data.length);
		final List<List<Integer>> indices = new ArrayList<List<Integer>>();
		List<ExpandableItem> entries = new ArrayList<ExpandableItem>();
		for (AdapterItem title : groupItems) {
			entries.add(ExpandableItem.of(title));
			indices.add(new ArrayList<Integer>());
		}
		for (int i = 0; i < data.length; i++) {
			ComponentSupport.validateIndex(parents[i], titles.length);
			entries.get(parents[i]).addChild(childItems.get(i));
			indices.get(parents[i]).add(i);
		}
		关闭();
		if (popup != null) {
			throw new ComponentStateException("关闭事件已打开新的菜单。");
		}
		final XUISimpleExpandablePopup<?> menu = new XUISimpleExpandablePopup<>(ComponentSupport.createContext(), new XUISimpleExpandableListAdapter(entries) {
			@Override
			public boolean isChildSelectable(int group, int child) {
				return enabled[indices.get(group).get(child)];
			}

			@Override
			public View getChildView(int group, int child, boolean last, View recycled, ViewGroup parent) {
				View row = super.getChildView(group, child, last, recycled, parent);
				boolean active = enabled[indices.get(group).get(child)];
				row.setEnabled(active);
				row.setAlpha(active ? 1.0f : 0.4f);
				ThemeSupport.applyFont(row);
				return row;
			}
		});
		if (separators != null) {
			menu.setHasDivider(separators);
		}
		if (singleExpandedGroup != null) {
			menu.setEnableOnlyExpandOne(singleExpandedGroup);
		}
		menu.create(width, Math.min(requireVisibleOwner().getHeight(), 800));
		menu.getExpandableListView().setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView parent, View item, int group, int child, long id) {
				int index = indices.get(group).get(child);
				if (!enabled[index]) {
					return true;
				}
				menu.dismiss();
				项目被单击(group, index, data[index]);
				return true;
			}
		});
		showPopup(menu, target);
	}

	/**
	 * 将已经校验的文本与可选图标组装为原生菜单项目。
	 *
	 * @param titles 已复制且不含空引用的项目文本。
	 * @param images 图标路径；空数组表示整组无图标，空文本表示单项无图标。
	 * @return 保持原始顺序的菜单项目。
	 * @throws 非法参数错误 图标数组为空引用、含空引用或非空长度与文本不一致。
	 */
	private static List<AdapterItem> createMenuItems(String[] titles, String[] images) {
		String[] icons = ComponentSupport.copyTextArray(images);
		if (icons.length != 0 && icons.length != titles.length) {
			throw new 非法参数错误();
		}

		// 在替换当前弹层前完成图像解析，错误输入不留下半次菜单更新。
		List<AdapterItem> entries = new ArrayList<AdapterItem>();
		for (int i = 0; i < titles.length; i++) {
			AdapterItem item = AdapterItem.of(titles[i]);
			if (icons.length != 0 && !icons[i].isEmpty()) {
				item.setIcon(ComponentSupport.getDrawable(icons[i]));
			}
			entries.add(item);
		}
		return entries;
	}

	@Override
	public boolean 是否显示() {
		return popup != null && popup.getPopupWindow().isShowing();
	}

	@Override
	public void 关闭() {
		if (popup != null) {
			XUIBasePopup current = popup;
			popup = null;
			current.dismiss();
		}
	}

	@Override
	public void 项目被单击(int group, int index, String text) {
		dispatchEvent("项目被单击", group, index, text);
	}

	@Override
	public void 置弹出位置(int preferred, int x, int top, int bottom) {
		if (preferred < 0 || preferred > 2) {
			throw new 非法参数错误();
		}
		direction = preferred;
		offsetX = x;
		offsetTop = top;
		offsetBottom = bottom;
	}

	@Override
	public void 置禁用项目(int[] indices) {
		if (indices == null) {
			throw new 非法参数错误();
		}
		for (int index : indices) {
			if (index < 0) {
				throw new 非法参数错误();
			}
		}
		disabledItems = indices.clone();
	}

	@Override
	public void 已关闭() {
		dispatchEvent("已关闭");
	}

	/**
	 * 在替换旧菜单之前生成独立的条目状态快照。
	 *
	 * @param size 项目数量。
	 * @return 按项目排列的启用状态。
	 */
	private boolean[] createEnabled(int size) {
		boolean[] enabled = new boolean[size];
		Arrays.fill(enabled, true);
		for (int index : disabledItems) {
			ComponentSupport.validateIndex(index, size);
			enabled[index] = false;
		}
		return enabled;
	}

	/**
	 * 配置弹出位置，并使关闭通知只清除本次界面。
	 *
	 * @param current 本次菜单。
	 * @param anchor 锚点视图。
	 */
	private void showPopup(final XUIPopup current, View anchor) {
		current.setPreferredDirection(direction);
		current.setPositionOffsetX(offsetX);
		current.setPositionOffsetYWhenTop(offsetTop);
		current.setPositionOffsetYWhenBottom(offsetBottom);
		current.setOnDismissListener(new PopupWindow.OnDismissListener() {
			@Override
			public void onDismiss() {
				if (popup == current) {
					popup = null;
				}
				已关闭();
			}
		});
		popup = current;
		try {
			current.show(anchor);
		} catch (RuntimeException failure) {
			if (popup == current) {
				popup = null;
			}
			throw failure;
		}
	}

	@Override
	public void 置分隔线(boolean show) {
		separators = show;
	}

	@Override
	public void 置单组展开(boolean single) {
		singleExpandedGroup = single;
	}

}
