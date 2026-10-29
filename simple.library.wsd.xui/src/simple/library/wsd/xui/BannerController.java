package simple.library.wsd.xui;

import android.view.View;
import android.view.ViewGroup;

import androidx.viewpager.widget.PagerAdapter;

import com.xuexiang.xui.widget.banner.widget.banner.base.BaseBanner;

import simple.runtime.errors.非法参数错误;

/**
 * 统一轮播的数据准备、自动播放意图和视图附着状态，保留正常数据的上游页面适配器。
 * 暂停会同时关闭上游的触摸后自动恢复入口；切窗只停任务，返回后保留原播放意图。
 *
 * @author 树先生 xhwsd@qq.com
 */
final class BannerController implements View.OnAttachStateChangeListener {

	private final BaseBanner<?, ?> banner;
	private boolean started;
	private boolean paused;
	private boolean automatic = true;
	private boolean destroyed;
	private boolean titleVisible = true;
	private boolean indicatorVisible = true;

	/**
	 * 将控制状态绑定到一个轮播视图。
	 *
	 * @param banner 对应的原生轮播。
	 */
	BannerController(BaseBanner<?, ?> banner) {
		this.banner = banner;
		banner.setAutoScrollEnable(false);
		banner.addOnAttachStateChangeListener(this);
	}

	/**
	 * 数据替换后重建已启动的页面；空数据替换旧适配器，不保留过期条目。
	 */
	void onDataReplaced() {
		banner.recycle();
		banner.setTitleShow(titleVisible && banner.size() > 0);
		banner.setIndicatorShow(indicatorVisible && banner.size() > 0);
		if (banner.size() == 0) {
			started = false;
			banner.setAutoScrollEnable(false);
			// LoopViewPager 不接受空适配器，且仍会创建两个边界页；显式提供无内容的占位视图。
			banner.getViewPager().setAdapter(new PagerAdapter() {
				@Override
				public int getCount() {
					return 0;
				}

				@Override
				public Object instantiateItem(ViewGroup container, int position) {
					View empty = new View(container.getContext());
					container.addView(empty);
					return empty;
				}

				@Override
				public void destroyItem(ViewGroup container, int position, Object object) {
					container.removeView((View) object);
				}

				@Override
				public boolean isViewFromObject(View view, Object object) {
					return view == object;
				}
			});
		} else if (started) {
			buildPages();
		}
	}

	/**
	 * 构建页面并请求播放；至少需要一项数据。
	 */
	void start() {
		if (destroyed || banner.size() == 0) {
			throw new 非法参数错误();
		}
		started = true;
		paused = false;
		buildPages();
	}

	/**
	 * 停止自动播放，保留手动滑动及当前页。
	 */
	void pause() {
		paused = true;
		banner.setAutoScrollEnable(false);
		banner.recycle();
	}

	/**
	 * 改变自动滚动开关，不覆盖调用方的显式暂停状态。
	 *
	 * @param enabled 是否允许自动滚动。
	 */
	void setAutoScrollEnabled(boolean enabled) {
		automatic = enabled;
		updatePlayback();
	}

	/**
	 * 保存标题显示意图，空数据时暂不显示旧标题。
	 *
	 * @param visible 是否显示标题。
	 */
	void setTitleVisible(boolean visible) {
		titleVisible = visible;
		banner.setTitleShow(visible && banner.size() > 0);
	}

	/**
	 * 保存指示器显示意图，空数据时不保留旧页码标记。
	 *
	 * @param visible 是否显示指示器。
	 */
	void setIndicatorVisible(boolean visible) {
		indicatorVisible = visible;
		banner.setIndicatorShow(visible && banner.size() > 0);
	}

	/**
	 * 设置首次播放延迟；正在播放的任务从新的延迟重新计时。
	 *
	 * @param seconds 非负秒数。
	 */
	void setDelay(long seconds) {
		if (seconds < 0) {
			throw new 非法参数错误();
		}
		banner.setDelay(seconds);
		updatePlayback();
	}

	/**
	 * 设置自动切页周期；不允许创建零周期定时任务。
	 *
	 * @param seconds 大于零的秒数。
	 */
	void setPeriod(long seconds) {
		if (seconds <= 0) {
			throw new 非法参数错误();
		}
		banner.setPeriod(seconds);
		updatePlayback();
	}

	/**
	 * 永久释放监听及任务；允许重复调用。
	 */
	void dispose() {
		destroyed = true;
		banner.removeOnAttachStateChangeListener(this);
		banner.setAutoScrollEnable(false);
		banner.recycle();
	}

	/**
	 * 重建页面前保留暂停设置，尚未附着时只准备页面，不启动后台任务。
	 */
	private void buildPages() {
		banner.setAutoScrollEnable(automatic && !paused && banner.isAttachedToWindow());
		banner.startScroll();
		updatePlayback();
	}

	/**
	 * 定时器是否存在只由播放意图及当前视图状态共同决定。
	 */
	private void updatePlayback() {
		banner.recycle();
		boolean enabled = !destroyed && started && !paused && automatic && banner.isAttachedToWindow();
		banner.setAutoScrollEnable(enabled);
		if (enabled) {
			banner.goOnScroll();
		}
	}

	@Override
	public void onViewAttachedToWindow(View view) {
		updatePlayback();
	}

	@Override
	public void onViewDetachedFromWindow(View view) {
		banner.recycle();
		banner.setAutoScrollEnable(false);
	}

	/**
	 * 返回真实页码；尚无数据时返回 -1。
	 *
	 * @return 从零开始的真实页码。
	 */
	int getCurrentPage() {
		return banner.size() == 0 ? -1 : banner.getViewPager().getCurrentItem();
	}

	/**
	 * 准备必要的页面适配器后跳到真实页，不改变自动播放意图。
	 *
	 * @param index 真实页码。
	 * @param smooth 是否使用切页动画。
	 */
	void setCurrentPage(int index, boolean smooth) {
		ComponentSupport.validateIndex(index, banner.size());
		if (!started) {
			started = true;
			buildPages();
		}
		banner.getViewPager().setCurrentItem(index, smooth);
	}

}
