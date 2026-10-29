package simple.library.wsd.xui;

import java.util.List;

import android.view.View;

import com.xuexiang.xui.widget.spinner.DropDownMenu;

import simple.runtime.components.可视组件;
import simple.runtime.components.组件容器;
import simple.runtime.errors.非法参数错误;

/**
 * XUI下拉菜单的原生视图及 Simple 事件适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI下拉菜单Impl extends XUI视图组件基类 implements XUI下拉菜单 {

	// 视图在父类构造期间创建；不得用字段初始化器重置此处状态。
	private DropDownMenu view;

	/**
	 * 创建XUI下拉菜单。
	 *
	 * @param container 容纳组件的 Simple 容器。
	 */
	public XUI下拉菜单Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected View createView() {
		view = new DropDownMenu(ComponentSupport.createContext(), null);

		return view;
	}

	@Override
	public void 置标签菜单文本(String text) {
		view.setTabMenuText(text);
	}

	@Override
	public void 置标签菜单可单击(boolean clickable) {
		view.setTabMenuClickable(clickable);
	}

	@Override
	public boolean 是否显示中() {
		return view.isShowing();
	}

	@Override
	public void 关闭菜单() {
		view.closeMenu();
	}

	@Override
	public void 置菜单(String[] titles, 可视组件[] menus, 可视组件 content) {
		String[] names = ComponentSupport.copyTextArray(titles);
		if (menus == null || names.length != menus.length) {
			throw new 非法参数错误();
		}
		List<View> targets = ComponentSupport.detachMenuComponents(view, menus, content);
		View body = targets.remove(targets.size() - 1);
		ComponentSupport.resetDropDownContent(view);
		view.setDropDownMenu(names, targets, body);
	}
}
