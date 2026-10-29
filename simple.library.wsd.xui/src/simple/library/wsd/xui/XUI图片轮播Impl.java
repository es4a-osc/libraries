package simple.library.wsd.xui;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.xuexiang.xui.widget.banner.widget.banner.BannerItem;
import com.xuexiang.xui.widget.banner.widget.banner.SimpleImageBanner;
import com.xuexiang.xui.widget.banner.widget.banner.base.BaseBanner;

import simple.runtime.components.组件容器;
import simple.runtime.errors.非法参数错误;
import simple.runtime.events.EventDispatcher;

/**
 * XUI图片轮播的原生视图及 Simple 事件适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI图片轮播Impl extends XUI视图组件基类 implements XUI图片轮播 {

	private long imageGeneration;

	private Drawable placeholder;
	private Drawable errorImage;

	// 视图在父类构造期间创建；不得用字段初始化器重置此处状态。
	private SimpleImageBanner view;

	// 播放意图在暂停和窗口重新附着时保持一致。
	private BannerController playback;

	// 轮播原始文本或图片标识。
	private String[] items;

	/**
	 * 创建XUI图片轮播。
	 *
	 * @param container 容纳组件的 Simple 容器。
	 */
	public XUI图片轮播Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected View createView() {
		// 标题区域需要在构造时启用；上游默认的居中指示器模式不会把标题加入视图树。
		Context context = ComponentSupport.createContext();
		context.getTheme().applyStyle(R.style.Es4aXuiImageBanner, true);
		view = new SimpleImageBanner(context) {
			@Override
			public SimpleImageBanner setBarShowWhenLast(boolean show) {
				super.setBarShowWhenLast(show);
				// 上游只在翻页回调中更新栏；配置当前末页时也立即生效。
				if (mRlBottomBarParent.getChildCount() > 0) {
					mRlBottomBarParent.getChildAt(0).setVisibility(
						!show && !mDatas.isEmpty() && mCurrentPosition == mDatas.size() - 1 ? GONE : VISIBLE);
				}
				return this;
			}

			@Override
			public boolean dispatchTouchEvent(MotionEvent event) {
				// setSource 只准备数据；开始轮播前触摸不得触发上游尚未建好适配器的定时任务。
				if (getViewPager().getAdapter() == null) {
					return false;
				}
				return super.dispatchTouchEvent(event);
			}

			@Override
			protected void loadingImageView(ImageView image, BannerItem item) {
				final long generation = imageGeneration;
				// Simple 在构造后才赋予外层尺寸；未指定缩放时，图片须填充实际页面。
				// 保留显式缩放的原生行为，避免 wrap_content 占位图将加载目标收缩到 1px。
				if (getScale() <= 0) {
					image.setLayoutParams(new LinearLayout.LayoutParams(
						ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
					image.setScaleType(ImageView.ScaleType.CENTER_CROP);
				}

				if (getScale() > 0) {
					image.setLayoutParams(new LinearLayout.LayoutParams(getItemWidth(), (int) (getItemWidth() * getScale())));
					image.setScaleType(ImageView.ScaleType.CENTER_CROP);
				}
				Glide.with(image).load(item.imgUrl).placeholder(placeholder).error(errorImage)
					.listener(new RequestListener<Drawable>() {
						@Override
						public boolean onLoadFailed(GlideException error, Object model, Target<Drawable> target, boolean first) {
							if (generation == imageGeneration && view.isAttachedToWindow()) {
								图片加载失败(item.imgUrl);
							}
							return false;
						}

						@Override
						public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource source, boolean first) {
							return false;
						}
					}).into(image);
			}
		};

		playback = new BannerController(view);
		items = new String[0];

		view.setOnItemClickListener(new BaseBanner.OnItemClickListener<BannerItem>() {

			@Override
			public void onItemClick(View clicked, BannerItem item, int index) {
				if (index >= 0 && index < items.length) {
					项目被单击(index, items[index]);
				}
			}
		});

		view.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int index) {
				if (index >= 0 && index < items.length) {
					页面被选择(index);
				}
			}
		});

		return view;
	}

	@Override
	public void 置容器缩放(float containerScale) {
		view.setContainerScale(containerScale);
	}

	@Override
	public double 取缩放() {
		return view.getScale();
	}

	@Override
	public void 置缩放(double scale) {
		view.setScale(scale);
	}

	@Override
	public void 置指示样式(int indicatorStyle) {
		view.setIndicatorStyle(indicatorStyle);
	}

	@Override
	public void 置指示宽度(float indicatorWidth) {
		view.setIndicatorWidth(indicatorWidth);
	}

	@Override
	public void 置指示高度(float indicatorHeight) {
		view.setIndicatorHeight(indicatorHeight);
	}

	@Override
	public void 置指示间隔(float indicatorGap) {
		view.setIndicatorGap(indicatorGap);
	}

	@Override
	public void 置指示选择颜色(int selectColor) {
		view.setIndicatorSelectColor(selectColor);
	}

	@Override
	public void 置指示圆角(float indicatorCornerRadius) {
		view.setIndicatorCornerRadius(indicatorCornerRadius);
	}

	@Override
	public int 取项目高度() {
		return view.getItemHeight();
	}

	@Override
	public int 取项目宽度() {
		return view.getItemWidth();
	}

	@Override
	public float 取容器缩放() {
		return view.getContainerScale();
	}

	@Override
	public void 置延迟(long delay) {
		playback.setDelay(delay);
	}

	@Override
	public void 置周期(long period) {
		playback.setPeriod(period);
	}

	@Override
	public void 置自动滚动启用(boolean isAutoScrollEnable) {
		playback.setAutoScrollEnabled(isAutoScrollEnable);
	}

	@Override
	public void 置栏颜色(int barColor) {
		view.setBarColor(barColor);
	}

	@Override
	public void 置文字颜色(int textColor) {
		view.setTextColor(textColor);
	}

	@Override
	public void 置文字大小(float textSize) {
		view.setTextSize(textSize);
	}

	@Override
	public void 置标题显示(boolean isTitleShow) {
		playback.setTitleVisible(isTitleShow);
	}

	@Override
	public void 置指示显示(boolean isIndicatorShow) {
		playback.setIndicatorVisible(isIndicatorShow);
	}

	@Override
	public void 置是否单个页面循环(boolean isOnePageLoop) {
		view.setIsOnePageLoop(isOnePageLoop);
	}

	@Override
	public void 置项目数组(String[] values) {
		String[] copy = ComponentSupport.copyTextArray(values);
		List<BannerItem> data = new ArrayList<BannerItem>();
		for (String image : copy) {
			data.add(new BannerItem().setImgUrl(ComponentSupport.resolveImageUri(image)));
		}
		items = copy;
		imageGeneration++;
		view.setSource(data);
		playback.onDataReplaced();
	}

	@Override
	public int 取项目数() {
		return items.length;
	}

	@Override
	public void 开始轮播() {
		playback.start();
	}

	@Override
	public void 暂停轮播() {
		playback.pause();
	}

	@Override
	public void 项目被单击(int index, String text) {
		EventDispatcher.dispatchEvent(this, "项目被单击", index, text);
	}

	@Override
	public void 页面被选择(int index) {
		EventDispatcher.dispatchEvent(this, "页面被选择", index);
	}

	@Override
	public void 置切换效果(String effect) {
		BannerEffects.apply(view, effect);
	}

	@Override
	public void 置图文项目(String[] images, String[] titles) {
		String[] data = ComponentSupport.copyTextArray(images);
		String[] labels = ComponentSupport.copyTextArray(titles);
		if (data.length != labels.length) {
			throw new 非法参数错误();
		}
		List<BannerItem> values = new ArrayList<BannerItem>();
		for (int i = 0; i < data.length; i++) {
			values.add(new BannerItem().setImgUrl(ComponentSupport.resolveImageUri(data[i])).setTitle(labels[i]));
		}
		imageGeneration++;
		items = data;
		view.setSource(values);
		playback.onDataReplaced();
	}

	@Override
	public void 销毁() {
		imageGeneration++;
		playback.dispose();
		super.销毁();
	}

	@Override
	public int 取当前页() {
		return playback.getCurrentPage();
	}

	@Override
	public void 置当前页(int index, boolean smooth) {
		playback.setCurrentPage(index, smooth);
	}

	@Override
	public void 置占位图片(String loading, String error) {
		Drawable pending = ComponentSupport.getDrawable(loading);
		Drawable failed = ComponentSupport.getDrawable(error);
		placeholder = pending;
		errorImage = failed;
	}

	@Override
	public void 图片加载失败(String image) {
		EventDispatcher.dispatchEvent(this, "图片加载失败", image);
	}

	@Override
	public void 置栏边距(float left, float top, float right, float bottom) {
		float[] padding = {left, top, right, bottom};
		for (float value : padding) {
			if (value < 0 || Float.isNaN(value) || Float.isInfinite(value)) {
				throw new 非法参数错误();
			}
		}
		view.barPadding(left, top, right, bottom);
	}

	@Override
	public void 置末页栏显示(boolean show) {
		view.setBarShowWhenLast(show);
	}

	@Override
	public void 置指示动画(int effect) {
		view.setSelectAnimClass(BannerEffects.indicatorClass(effect));
		view.setUnselectAnimClass(null);
		if (view.size() > 0 && view.getViewPager().getAdapter() != null) {
			// 重建原指示器内容，避免关闭或更换动画后保留上一种缩放、旋转状态。
			view.onCreateIndicator();
		}
	}

}
