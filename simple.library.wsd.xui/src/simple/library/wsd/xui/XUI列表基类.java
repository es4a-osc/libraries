package simple.library.wsd.xui;

import simple.runtime.components.组件容器;
import simple.runtime.errors.非法参数错误;
import simple.runtime.events.EventDispatcher;

/**
 * 一维列表的数据修改及选择不变量，原生刷新和滚动由不同列表实现。
 *
 * @author 树先生 xhwsd@qq.com
 */
abstract class XUI列表基类 extends XUI视图组件基类 implements XUI列表 {

	// 原生视图在父类构造期间创建，数据在具体 createView 中初始化。
	protected ListData data;

	/**
	 * 绑定列表所属容器。
	 *
	 * @param container 所属容器。
	 */
	protected XUI列表基类(组件容器 container) {
		super(container);
	}

	@Override
	public void 置项目数组(String[] items) {
		data.replace(items, null, null);
		refreshData();
	}

	@Override
	public int 取项目数() {
		return data.entries.size();
	}

	@Override
	public String 取项目文本(int index) {
		return data.get(index).title;
	}

	@Override
	public void 置项目文本(int index, String text) {
		if (text == null) {
			throw new 非法参数错误();
		}
		data.get(index).title = text;
		refreshData();
	}

	@Override
	public void 添加项目(String text) {
		data.entries.add(new ListData.Entry(text, "", ""));
		refreshData();
	}

	@Override
	public void 移除项目(int index) {
		data.remove(index);
		refreshData();
	}

	@Override
	public void 清空项目() {
		data.entries.clear();
		data.selected = -1;
		refreshData();
	}

	@Override
	public void 置选中项目(int index) {
		if (index != -1) {
			data.get(index);
		}
		data.selected = index;
		refreshData();
	}

	@Override
	public int 取选中项目() {
		return data.selected;
	}

	@Override
	public void 滚动到项目(int index, boolean smooth) {
		data.get(index);
		scrollTo(index, smooth);
	}

	@Override
	public void 项目被单击(int index, String text) {
		置选中项目(index);
		EventDispatcher.dispatchEvent(this, "项目被单击", index, text);
	}

	/**
	 * 刷新原生行和选择外观，不重建数据模型。
	 */
	protected abstract void refreshData();

	/**
	 * 按原生列表协议定位项目。
	 *
	 * @param index 已验证的索引。
	 * @param smooth 是否平滑滚动。
	 */
	protected abstract void scrollTo(int index, boolean smooth);
}
