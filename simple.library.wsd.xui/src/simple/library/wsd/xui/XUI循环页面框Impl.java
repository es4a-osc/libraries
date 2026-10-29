package simple.library.wsd.xui;

import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.xuexiang.xui.widget.banner.widget.loopviewpager.LoopViewPager;

import simple.runtime.components.组件容器;
import simple.runtime.events.EventDispatcher;

/**
 * XUI循环页面框的原生视图及 Simple 事件适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI循环页面框Impl extends XUI视图组件基类 implements XUI循环页面框 {

	// 视图在父类构造期间创建；不得用字段初始化器重置此处状态。
	private LoopViewPager view;

	// 独立页面数据，循环副本分别创建视图。
	private String[] pages;

	/**
	 * 创建XUI循环页面框。
	 *
	 * @param container 容纳组件的 Simple 容器。
	 */
	public XUI循环页面框Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected View createView() {
		view = new LoopViewPager(ComponentSupport.createContext());

		pages = new String[0];

		view.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int index) {
				页面改变(index);
			}
		});

		return view;
	}

	@Override
	public void 置边界缓存(boolean flag) {
		view.setBoundaryCaching(flag);
	}

	@Override
	public void 置页面数组(String[] items) {
		pages = ComponentSupport.copyTextArray(items);
		view.setAdapter(new PagerAdapter() {
			@Override
			public int getCount() {
				return pages.length;
			}

			@Override
			public boolean isViewFromObject(View item, Object object) {
				return item == object;
			}

			@Override
			public Object instantiateItem(ViewGroup parent, int position) {
				TextView item = new TextView(parent.getContext());
				item.setText(pages[position]);
				item.setGravity(Gravity.CENTER);
				parent.addView(item);
				return item;
			}

			@Override
			public void destroyItem(ViewGroup parent, int position, Object object) {
				parent.removeView((View) object);
			}
		});
	}

	@Override
	public void 选择页面(int index) {
		ComponentSupport.validateIndex(index, pages.length);
		view.setCurrentItem(index);
	}

	@Override
	public int 取当前页面() {
		return pages.length == 0 ? -1 : view.getCurrentItem();
	}

	@Override
	public void 页面改变(int index) {
		EventDispatcher.dispatchEvent(this, "页面改变", index);
	}
}
