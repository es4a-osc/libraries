package simple.library.wsd.xui;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.xuexiang.xui.widget.banner.recycler.BannerLayout;
import com.xuexiang.xui.widget.banner.recycler.layout.BannerLayoutManager;

import simple.runtime.components.组件容器;
import simple.runtime.errors.非法参数错误;
import simple.runtime.events.EventDispatcher;

/**
 * XUI轮播的原生视图及 Simple 事件适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI轮播Impl extends XUI视图组件基类 implements XUI轮播 {

	// 视图在父类构造期间创建；不得用字段初始化器重置此处状态。
	private BannerLayout view;

	/**
	 * 创建XUI轮播。
	 *
	 * @param container 容纳组件的 Simple 容器。
	 */
	public XUI轮播Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected View createView() {
		view = new BannerLayout(ComponentSupport.createContext());

		view.setOnIndicatorIndexChangedListener(new BannerLayout.OnIndicatorIndexChangedListener() {

			@Override
			public void onIndexChanged(int index) {
				页面被选择(index);
			}
		});

		return view;
	}

	@Override
	public void 置自动播放(boolean isAutoPlaying) {
		view.setAutoPlaying(isAutoPlaying);
	}

	@Override
	public boolean 是否播放() {
		return view.isPlaying();
	}

	@Override
	public void 置显示指示(boolean showIndicator) {
		view.setShowIndicator(showIndicator);
	}

	@Override
	public void 置中间缩放(float centerScale) {
		view.setCenterScale(centerScale);
	}

	@Override
	public void 置移动速度(float moveSpeed) {
		view.setMoveSpeed(moveSpeed);
	}

	@Override
	public void 置项目间距(int itemSpace) {
		view.setItemSpace(itemSpace);
	}

	@Override
	public void 置自动播放时长(int autoPlayDuration) {
		view.setAutoPlayDuration(autoPlayDuration);
	}

	@Override
	public void 置项目数组(String[] items) {
		final String[] data = ComponentSupport.copyTextArray(items);
		view.setAdapter(ComponentSupport.createTextListAdapter(data, new ComponentSupport.ItemSelectionListener() {
			@Override
			public void onItemSelected(int index, String text) {
				项目被单击(index, text);
			}
		}));
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
	public void 置图片项目(String[] images, final int width, final int height) {
		final String[] data = ComponentSupport.copyTextArray(images);
		if (width <= 0 || height <= 0) {
			throw new 非法参数错误();
		}
		view.setAdapter(new RecyclerView.Adapter<RecyclerView.ViewHolder>() {
			@Override
			public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int type) {
				ImageView image = new ImageView(parent.getContext());
				image.setLayoutParams(new RecyclerView.LayoutParams(width, height));
				image.setScaleType(ImageView.ScaleType.CENTER_CROP);
				return new RecyclerView.ViewHolder(image) {
				};
			}

			@Override
			public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
				ComponentSupport.loadImage((ImageView) holder.itemView, data[position]);
				holder.itemView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View clicked) {
						int index = holder.getBindingAdapterPosition();
						if (index != RecyclerView.NO_POSITION) {
							项目被单击(index, data[index]);
						}
					}
				});
			}

			@Override
			public void onViewRecycled(RecyclerView.ViewHolder holder) {
				Glide.with(holder.itemView).clear((ImageView) holder.itemView);
			}

			@Override
			public int getItemCount() {
				return data.length;
			}
		});
	}

	/**
	 * 取得 XUI 1.2.1 轮播正文列表，避免误操作指示器列表。
	 *
	 * @return 轮播正文列表。
	 */
	private RecyclerView getPages() {
		if (view.getChildCount() == 0 || !(view.getChildAt(0) instanceof RecyclerView)) {
			throw new ComponentStateException("轮播正文列表不可用。");
		}
		return (RecyclerView) view.getChildAt(0);
	}

	@Override
	public int 取当前页() {
		RecyclerView pages = getPages();
		return pages.getAdapter() == null || pages.getAdapter().getItemCount() == 0 ? -1
			: ((BannerLayoutManager) pages.getLayoutManager()).getCurrentPosition() % pages.getAdapter().getItemCount();
	}

	@Override
	public void 置当前页(int index, boolean smooth) {
		RecyclerView pages = getPages();
		ComponentSupport.validateIndex(index, pages.getAdapter() == null ? 0 : pages.getAdapter().getItemCount());
		if (smooth) {
			pages.smoothScrollToPosition(index);
		} else {
			pages.scrollToPosition(index);
		}
	}

	@Override
	public void 置排列方向(int orientation) {
		if (orientation != 0 && orientation != 1) {
			throw new 非法参数错误();
		}
		view.setOrientation(orientation);
	}

}
