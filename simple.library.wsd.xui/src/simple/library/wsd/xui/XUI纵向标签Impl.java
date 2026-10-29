package simple.library.wsd.xui;

import android.view.LayoutInflater;
import android.view.View;

import androidx.viewpager.widget.ViewPager;

import com.xuexiang.xui.widget.tabbar.VerticalTabLayout;
import com.xuexiang.xui.widget.tabbar.vertical.ITabView;
import com.xuexiang.xui.widget.tabbar.vertical.TabView;

import simple.runtime.components.impl.android.视图组件;
import simple.runtime.components.可视组件;
import simple.runtime.components.组件容器;
import simple.runtime.errors.非法参数错误;
import simple.runtime.events.EventDispatcher;

/**
 * XUI纵向标签的原生视图及 Simple 事件适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI纵向标签Impl extends XUI视图组件基类 implements XUI纵向标签 {

	// 视图在父类构造期间创建；不得用字段初始化器重置此处状态。
	private VerticalTabLayout view;

	/**
	 * 创建XUI纵向标签。
	 *
	 * @param container 容纳组件的 Simple 容器。
	 */
	public XUI纵向标签Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected View createView() {
		view = (VerticalTabLayout) LayoutInflater.from(ComponentSupport.createContext()).inflate(R.layout.es4a_xui_vertical_tab, null);

		view.addOnTabSelectedListener(new VerticalTabLayout.OnTabSelectedListener() {

			@Override
			public void onTabSelected(TabView tab, int index) {
				标签被选择(index);
			}

			@Override
			public void onTabUnselected(TabView tab, int index) {

			}

			@Override
			public void onTabReselected(TabView tab, int index) {
				标签被选择(index);
			}
		});

		return view;
	}

	@Override
	public void 清空标签() {
		view.removeAllTabs();
	}

	@Override
	public int 取标签数量() {
		return view.getTabCount();
	}

	@Override
	public int 取选中标签位置() {
		return view.getSelectedTabPosition();
	}

	@Override
	public void 置标签徽标数(int tabPosition, int badgeNum) {
		view.setTabBadge(tabPosition, badgeNum);
	}

	@Override
	public void 置标签徽标文本(int tabPosition, String badgeText) {
		view.setTabBadge(tabPosition, badgeText);
	}

	@Override
	public void 置标签模式(int mode) {
		view.setTabMode(mode);
	}

	@Override
	public void 置标签外边距(int margin) {
		view.setTabMargin(margin);
	}

	@Override
	public void 置标签高度(int height) {
		view.setTabHeight(height);
	}

	@Override
	public void 置指示颜色(int color) {
		view.setIndicatorColor(color);
	}

	@Override
	public void 置指示宽度(int width) {
		view.setIndicatorWidth(width);
	}

	@Override
	public void 置指示圆角(int corners) {
		view.setIndicatorCorners(corners);
	}

	@Override
	public void 置指示对齐(int gravity) {
		view.setIndicatorGravity(gravity);
	}

	@Override
	public void 置标签数组(String[] items) {
		ComponentSupport.populateVerticalTabs(view, ComponentSupport.copyTextArray(items));
		ThemeSupport.applyFont(view);
	}

	@Override
	public void 选择标签(int index) {
		ComponentSupport.validateIndex(index, view.getTabCount());
		view.setTabSelected(index);
	}

	@Override
	public void 绑定页面框(可视组件 pager) {
		View target = ((视图组件) pager).getView();
		if (!(target instanceof ViewPager)) {
			throw new 非法参数错误();
		}
		view.setupWithViewPager((ViewPager) target);
	}

	@Override
	public void 标签被选择(int index) {
		EventDispatcher.dispatchEvent(this, "标签被选择", index);
	}

	@Override
	public void 置标签文本(int index, String text) {
		ComponentSupport.validateIndex(index, view.getTabCount());
		view.getTabAt(index).getTitleView().setText(text);
	}

	@Override
	public void 置标签图标资源(int index, int normal, int selected, int width, int height, int gravity) {
		ComponentSupport.validateIndex(index, view.getTabCount());
		if (width <= 0 || height <= 0) {
			throw new 非法参数错误();
		}
		view.getTabAt(index).setIcon(new ITabView.TabIcon.Builder().setIcon(selected, normal)
			.setIconSize(width, height).setIconGravity(gravity).build());
	}

}
