package simple.library.wsd.xui;

import java.util.ArrayList;
import java.util.List;

import simple.runtime.errors.非法参数错误;

/**
 * 一维文本及图文列表共用的数据和选择状态，先校验全部输入再替换内容。
 *
 * @author 树先生 xhwsd@qq.com
 */
final class ListData {

	/**
	 * 同一条目的文本、说明和图片保持同一个更新边界。
	 */
	static final class Entry {
		String title;
		String detail;
		String image;

		/**
		 * 创建独立条目。
		 *
		 * @param title 主文本。
		 * @param detail 说明。
		 * @param image 图片标识。
		 */
		Entry(String title, String detail, String image) {
			if (title == null || detail == null || image == null) {
				throw new 非法参数错误();
			}
			this.title = title;
			this.detail = detail;
			this.image = image;
		}
	}

	final List<Entry> entries = new ArrayList<Entry>();
	int selected = -1;

	/**
	 * 原子替换数据并清除旧选择，不保留调用者的可变数组。
	 *
	 * @param titles 主文本数组。
	 * @param details 说明数组；空引用表示全部没有说明。
	 * @param images 图片数组；空引用表示全部没有图片。
	 */
	void replace(String[] titles, String[] details, String[] images) {
		String[] names = ComponentSupport.copyTextArray(titles);
		String[] descriptions = details == null ? new String[names.length] : ComponentSupport.copyTextArray(details);
		String[] icons = images == null ? new String[names.length] : ComponentSupport.copyTextArray(images);
		if (names.length != descriptions.length || names.length != icons.length) {
			throw new 非法参数错误();
		}
		List<Entry> next = new ArrayList<Entry>();
		for (int i = 0; i < names.length; i++) {
			next.add(new Entry(names[i], details == null ? "" : descriptions[i], images == null ? "" : icons[i]));
		}
		entries.clear();
		entries.addAll(next);
		selected = -1;
	}

	/**
	 * 取得已验证索引的条目。
	 *
	 * @param index 从零开始的索引。
	 * @return 当前条目。
	 */
	Entry get(int index) {
		ComponentSupport.validateIndex(index, entries.size());
		return entries.get(index);
	}

	/**
	 * 移除条目并保持选择指向原来的条目，被移除的选择恢复为未选。
	 *
	 * @param index 从零开始的索引。
	 */
	void remove(int index) {
		get(index);
		entries.remove(index);
		if (selected == index) {
			selected = -1;
		} else if (selected > index) {
			selected--;
		}
	}
}
