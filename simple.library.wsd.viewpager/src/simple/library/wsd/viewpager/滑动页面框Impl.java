package simple.library.wsd.viewpager;

/* Simple基础类 */
import simple.runtime.components.组件容器;
import simple.runtime.components.可视组件;
import simple.runtime.components.布局;
import simple.runtime.components.组件;
import simple.runtime.components.impl.android.视图组件;
import simple.runtime.components.impl.android.视图组件容器;
import simple.runtime.android.MainActivity;
import simple.runtime.events.EventDispatcher;
import simple.runtime.errors.索引超出界限错误;

/* Android类 */
import android.os.Parcelable;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

/* AndroidX 页面容器 */
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

/* Java类 */
import java.util.ArrayList;

/**
 * 滑动页面框组件的实现
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class 滑动页面框Impl extends 视图组件 implements 滑动页面框, 视图组件容器 {

	// 是否开启切换特效
	private boolean switchEffects;
	// 页面视图列表
	private ArrayList<View> pageViews;
	// 适配器
	private PagerAdapter adapter;
	// 是否禁止切换
	private boolean noScroll;

	/**
	 * 创建新组件
	 *
	 * @param container 容纳组件的容器（对于不可见的组件必须是窗口，不可为{@code null}）
	 */
	public 滑动页面框Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected View createView() {
		pageViews = new ArrayList<View>();

		// 按视图身份维护页面位置，删除前面的页面时保留仍然存在的当前页面。
		adapter = new PagerAdapter() {

			@Override
			public int getCount() {
				return pageViews.size();
			}

			@Override
			public boolean isViewFromObject(View view, Object object) {
				return view == object;
			}

			@Override
			public int getItemPosition(Object object) {
				int position = pageViews.indexOf(object);
				return position >= 0 ? position : POSITION_NONE;
			}

			@Override
			public void destroyItem(ViewGroup container, int position, Object object) {
				container.removeView((View) object);
			}

			@Override
			public Object instantiateItem(ViewGroup container, int position) {
				container.addView((View) pageViews.get(position));
				return pageViews.get(position);
			}

			@Override
			public Parcelable saveState() {
				return null;
			}
		};

		// 禁止手势仅影响触摸分发，程序切换仍由现行页面属性控制。
		ViewPager view = new ViewPager(MainActivity.getContext()) {

			@Override
			public boolean onTouchEvent(MotionEvent ev) {
				if (noScroll) {
					return false;
				}

				return super.onTouchEvent(ev);
			}

			@Override
			public boolean onInterceptTouchEvent(MotionEvent ev) {
				if (noScroll) {
					return false;
				}

				return super.onInterceptTouchEvent(ev);
			}
		};

		// 页面改变监听器
		view.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

			@Override
			public void onPageScrollStateChanged(int state) {
				滑动状态改变(state);
			}

			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
				页面被滑动(position, positionOffset, positionOffsetPixels);
			}

			@Override
			public void onPageSelected(int position) {
				页面被选择(position);
			}
		});
		view.setFocusable(true);
		view.setAdapter(adapter);
		return view;
	}

	/* 事件 */

	@Override
	public void 页面被滑动(int position, float positionOffset, int positionOffsetPixels) {
		EventDispatcher.dispatchEvent(this, "页面被滑动", position, positionOffset, positionOffsetPixels);
	}

	@Override
	public void 滑动状态改变(int state) {
		EventDispatcher.dispatchEvent(this, "滑动状态改变", state);
	}

	@Override
	public void 页面被选择(int position) {
		EventDispatcher.dispatchEvent(this, "页面被选择", position);
	}

	/* 属性 */

	@Override
	public boolean 禁止滑动() {
		return noScroll;
	}

	@Override
	public void 禁止滑动(boolean noScroll) {
		this.noScroll = noScroll;
	}

	@Override
	public boolean 切换特效() {
		return switchEffects;
	}

	@Override
	public void 切换特效(boolean isOpen) {
		switchEffects = isOpen;
	}

	@Override
	public int 现行页面() {
		if (pageViews.size() == 0) {
			return -1;
		}

		ViewPager view = (ViewPager) getView();

		return view.getCurrentItem();
	}

	@Override
	public void 现行页面(int position) {
		checkPageIndex(position);
		ViewPager view = (ViewPager) getView();

		view.setCurrentItem(position, switchEffects);
	}

	/* 方法 */

	@Override
	public void 添加页面(可视组件 visibleComponent) {
		if (!(visibleComponent instanceof 视图组件)) {
			throw new IllegalArgumentException("页面必须是已经创建视图的可视组件。");
		}
		addPageView(((视图组件) visibleComponent).getView());
	}

	@Override
	public void 删除页面(int position) {
		checkPageIndex(position);
		pageViews.remove(position);
		adapter.notifyDataSetChanged();
	}

	@Override
	public void 清空页面() {
		pageViews.clear();
		adapter.notifyDataSetChanged();
	}

	@Override
	public int 取页面数() {
		return pageViews.size();
	}

	/* 内部接口与平台回调 */

	@Override
	public ViewGroup getLayoutManager() {
		return (ViewGroup) getView();
	}

	@Override
	public void addComponent(组件 component) {
		// 把可视组件接口转为实现接口视图组件，并取视图组件的视图
		View view = ((视图组件) component).getView();

		addPageView(view);
	}

	@Override
	public 布局 getLayout() {
		return null;
	}

	/* 内部辅助方法 */

	/**
	 * 统一校验显式切页和删除操作，不依赖底层控件截断越界索引。
	 *
	 * @param position 从零开始的页面索引。
	 * @throws 索引超出界限错误 页面索引无效。
	 */
	private void checkPageIndex(int position) {
		if (position < 0 || position >= pageViews.size()) {
			throw new 索引超出界限错误();
		}
	}

	/**
	 * 校验页面身份后移出原父容器，准备延迟挂载需要的布局参数并登记页面。
	 *
	 * @param view 目标原生视图。
	 * @throws IllegalArgumentException 视图为空、页面重复或造成容器循环嵌套。
	 */
	private void addPageView(View view) {
		// 先验证，失败时不能把组件从原容器移走，也不能登记重复页面。
		if (view == null || pageViews.contains(view)) {
			throw new IllegalArgumentException("页面视图尚未创建或已经添加到当前滑动页面框。");
		}
		View ancestor = getView();
		while (ancestor != null) {
			if (ancestor == view) {
				throw new IllegalArgumentException("不能将滑动页面框自身或祖先容器添加为页面。");
			}
			ViewParent parent = ancestor.getParent();
			ancestor = parent instanceof View ? (View) parent : null;
		}

		// 页面尚未挂载到 ViewPager，布局参数可能为空。
		// 必须提前建立参数，否则 Simple 初始化宽高时会发生空指针异常。
		ViewGroup.LayoutParams current = view.getLayoutParams();

		// 已有 ViewPager 参数直接沿用，避免丢失其状态。
		if (!(current instanceof ViewPager.LayoutParams)) {
			// 使用 ViewPager 专用参数，默认宽高均为匹配父级。
			ViewPager.LayoutParams params = new ViewPager.LayoutParams();

			if (current != null) {
				// 从其他容器移入的页面保留已有宽高。
				params.width = current.width;
				params.height = current.height;
			}

			view.setLayoutParams(params);
		}

		// 原容器解除挂载后由适配器接管；清空后原页面仍可再次添加。
		ViewParent parent = view.getParent();
		if (parent instanceof ViewGroup) {
			((ViewGroup) parent).removeView(view);
		}
		pageViews.add(view);
		adapter.notifyDataSetChanged();
	}
}
