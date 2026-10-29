package simple.library.wsd.xui;

import android.view.View;

import androidx.viewpager.widget.ViewPager;

import com.xuexiang.xui.widget.tabbar.EasyIndicator;

import simple.runtime.components.impl.android.视图组件;
import simple.runtime.components.可视组件;
import simple.runtime.components.组件容器;
import simple.runtime.errors.非法参数错误;
import simple.runtime.events.EventDispatcher;

/**
 * XUI指示器的原生视图及 Simple 事件适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI指示器Impl extends XUI视图组件基类 implements XUI指示器 {

	// 视图在父类构造期间创建；不得用字段初始化器重置此处状态。
	private EasyIndicator view;

	/**
	 * 创建XUI指示器。
	 *
	 * @param container 容纳组件的 Simple 容器。
	 */
	public XUI指示器Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected View createView() {
		view = new EasyIndicator(ComponentSupport.createContext());

		view.setOnTabClickListener(new EasyIndicator.OnTabClickListener() {

			@Override
			public void onTabClick(String title, int index) {
				标签被选择(index);
			}
		});

		return view;
	}

	@Override
	public void 置标签标题数组(String[] tabTitles) {
		view.setTabTitles(tabTitles);
	}

	@Override
	public void 绑定页面框(可视组件 pager) {
		View target = ((视图组件) pager).getView();
		if (!(target instanceof ViewPager)) {
			throw new 非法参数错误();
		}
		ViewPager pages = (ViewPager) target;
		view.setViewPager(pages, pages.getAdapter());
	}

	@Override
	public void 标签被选择(int index) {
		EventDispatcher.dispatchEvent(this, "标签被选择", index);
	}
}
