package simple.library.wsd.xui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.view.View;

import com.xuexiang.xui.widget.picker.widget.OptionsPickerView;
import com.xuexiang.xui.widget.picker.widget.builder.OptionsPickerBuilder;
import com.xuexiang.xui.widget.picker.widget.listener.OnOptionsSelectChangeListener;
import com.xuexiang.xui.widget.picker.widget.listener.OnOptionsSelectListener;

import simple.runtime.components.组件容器;
import simple.runtime.errors.非法参数错误;

/**
 * 多级选择器的 XUI 实现与 Simple 生命周期适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI多级选择器Impl extends XUI选择器基类 implements XUI多级选择器 {

	private boolean restoreChildSelection;

	private String[] labels;
	private boolean[] cyclic = new boolean[3];

	/**
	 * 创建XUI多级选择器。
	 *
	 * @param container 所属 Simple 窗口。
	 */
	public XUI多级选择器Impl(组件容器 container) {
		super(container);
	}

	private List<String> first;
	private List<List<String>> second;
	private List<List<List<String>>> third;
	private List<String> independentSecond;
	private List<String> independentThird;
	private int columns;
	private final int[] selected = new int[3];
	private final String[] selectedTexts = {"", "", ""};

	@Override
	public void 置联动项目(String[] texts, int[] parents) {
		String[] data = ComponentSupport.copyTextArray(texts);
		if (parents == null || parents.length != data.length || data.length == 0) {
			throw new 非法参数错误();
		}
		List<List<Integer>> children = new ArrayList<List<Integer>>();
		List<Integer> roots = new ArrayList<Integer>();
		int[] depth = new int[data.length];
		int level = 0;
		for (int i = 0; i < data.length; i++) {
			children.add(new ArrayList<Integer>());
			if (parents[i] == -1) {
				roots.add(i);
				depth[i] = 1;
			} else {
				// 限制父项在前即可排除循环、越界和跨级的无根节点。
				if (parents[i] < 0 || parents[i] >= i) {
					throw new 非法参数错误();
				}
				depth[i] = depth[parents[i]] + 1;
				children.get(parents[i]).add(i);
			}
			level = Math.max(level, depth[i]);
		}
		if (level > 3 || roots.isEmpty()) {
			throw new 非法参数错误();
		}
		for (int i = 0; i < data.length; i++) {
			if (children.get(i).isEmpty() && depth[i] != level) {
				throw new 非法参数错误();
			}
		}

		List<String> one = new ArrayList<String>();
		List<List<String>> two = new ArrayList<List<String>>();
		List<List<List<String>>> three = new ArrayList<List<List<String>>>();
		for (int root : roots) {
			one.add(data[root]);
			List<String> sub = new ArrayList<String>();
			List<List<String>> descendants = new ArrayList<List<String>>();
			for (int child : children.get(root)) {
				sub.add(data[child]);
				List<String> leaf = new ArrayList<String>();
				for (int value : children.get(child)) {
					leaf.add(data[value]);
				}
				descendants.add(leaf);
			}
			two.add(sub);
			three.add(descendants);
		}
		// 验证全部成功后一次替换数据，错误输入不得留下半棵联动树。
		关闭();
		first = one;
		second = two;
		third = three;
		independentSecond = null;
		independentThird = null;
		columns = level;
		Arrays.fill(selected, 0);
		Arrays.fill(selectedTexts, "");
	}

	@Override
	public void 置独立项目(String[] one, String[] two, String[] three) {
		String[] a = ComponentSupport.copyTextArray(one);
		String[] b = ComponentSupport.copyTextArray(two);
		String[] c = ComponentSupport.copyTextArray(three);
		if (a.length == 0 || (b.length == 0 && c.length > 0)) {
			throw new 非法参数错误();
		}
		关闭();
		first = Arrays.asList(a);
		independentSecond = Arrays.asList(b);
		independentThird = Arrays.asList(c);
		second = null;
		third = null;
		columns = c.length > 0 ? 3 : b.length > 0 ? 2 : 1;
		Arrays.fill(selected, 0);
		Arrays.fill(selectedTexts, "");
	}

	/**
	 * 校验当前路径各列索引，未使用的列只接受零。
	 *
	 * @param a 第一列索引。
	 * @param b 第二列索引。
	 * @param c 第三列索引。
	 */
	private void validateSelection(int a, int b, int c) {
		if (first == null) {
			throw new 非法参数错误();
		}
		ComponentSupport.validateIndex(a, first.size());
		if (columns > 1) {
			ComponentSupport.validateIndex(b, second == null ? independentSecond.size() : second.get(a).size());
		} else if (b != 0) {
			throw new 非法参数错误();
		}
		if (columns > 2) {
			ComponentSupport.validateIndex(c, third == null ? independentThird.size() : third.get(a).get(b).size());
		} else if (c != 0) {
			throw new 非法参数错误();
		}
	}

	@Override
	public void 置默认选项(int a, int b, int c) {
		validateSelection(a, b, c);
		selected[0] = a;
		selected[1] = b;
		selected[2] = c;
	}

	@Override
	public String 取选中文本(int column) {
		ComponentSupport.validateIndex(column, 3);
		return selectedTexts[column];
	}

	@Override
	public void 显示() {
		requireVisibleOwner();
		validateSelection(selected[0], selected[1], selected[2]);
		关闭();
		final List<OptionsPickerView<String>> current = new ArrayList<OptionsPickerView<String>>(1);
		OptionsPickerBuilder builder = new OptionsPickerBuilder(ComponentSupport.createContext(), new OnOptionsSelectListener() {
			@Override
			public boolean onOptionsSelect(View source, int a, int b, int c) {
				if (current.isEmpty() || !confirmPicker(current.get(0))) {
					return true;
				}
				selected[0] = a;
				selected[1] = b;
				selected[2] = c;
				selectedTexts[0] = first.get(a);
				selectedTexts[1] = columns > 1 ? (second == null ? independentSecond.get(b) : second.get(a).get(b)) : "";
				selectedTexts[2] = columns > 2 ? (third == null ? independentThird.get(c) : third.get(a).get(b).get(c)) : "";
				选择已确认(a, columns > 1 ? b : -1, columns > 2 ? c : -1);
				return false;
			}
		}).setDecorView(getOverlayContainer()).setSelectOptions(selected[0], selected[1], selected[2]);
		configure(builder);
		builder.setCyclic(cyclic[0], cyclic[1], cyclic[2]).isRestoreItem(restoreChildSelection);
		if (labels != null) {
			builder.setLabels(labels[0], labels[1], labels[2]);
		}
		builder.setOptionsSelectChangeListener(new OnOptionsSelectChangeListener() {
			@Override
			public void onOptionsSelectChanged(int a, int b, int c) {
				if (!current.isEmpty() && isCurrentPicker(current.get(0))) {
					选择已改变(a, columns > 1 ? b : -1, columns > 2 ? c : -1);
				}
			}
		});
		OptionsPickerView<String> picker = builder.build();
		current.add(picker);
		if (second == null) {
			picker.setNPicker(first, columns > 1 ? independentSecond : null, columns > 2 ? independentThird : null);
		} else if (columns == 1) {
			picker.setPicker(first);
		} else if (columns == 2) {
			picker.setPicker(first, second);
		} else {
			picker.setPicker(first, second, third);
		}
		showPicker(picker);
	}

	@Override
	public void 选择已改变(int first, int second, int third) {
		dispatchEvent("选择已改变", first, second, third);
	}

	@Override
	public void 选择已确认(int first, int second, int third) {
		dispatchEvent("选择已确认", first, second, third);
	}

	@Override
	public void 置列标签(String first, String second, String third) {
		labels = ComponentSupport.copyTextArray(new String[] {first, second, third});
	}

	@Override
	public void 置循环(boolean first, boolean second, boolean third) {
		cyclic = new boolean[] {first, second, third};
	}

	@Override
	public void 置下级重置(boolean reset) {
		restoreChildSelection = reset;
	}

}
