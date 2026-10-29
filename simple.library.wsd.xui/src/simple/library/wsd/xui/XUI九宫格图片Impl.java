package simple.library.wsd.xui;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.xuexiang.xui.widget.imageview.nine.NineGridImageView;
import com.xuexiang.xui.widget.imageview.nine.NineGridImageViewAdapter;

import simple.runtime.components.组件容器;
import simple.runtime.events.EventDispatcher;

/**
 * XUI九宫格图片的原生视图及 Simple 事件适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI九宫格图片Impl extends XUI视图组件基类 implements XUI九宫格图片 {

	private Drawable placeholder;
	private Drawable errorImage;
	private long imageGeneration;
	private boolean disposed;
	private String[] sources;
	private Set<Integer> completed;
	private Set<Integer> failed;

	// 视图在父类构造期间创建；不得用字段初始化器重置此处状态。
	private NineGridImageView <String> view;

	/**
	 * 创建XUI九宫格图片。
	 *
	 * @param container 容纳组件的 Simple 容器。
	 */
	public XUI九宫格图片Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected View createView() {
		view = new NineGridImageView<String>(ComponentSupport.createContext());
		sources = new String[0];
		completed = new HashSet<Integer>();
		failed = new HashSet<Integer>();

		view.setAdapter(new NineGridImageViewAdapter<String>() {
			@Override
			protected void onDisplayImage(Context context, ImageView imageView, String image) {
				final int index = view.indexOfChild(imageView);
				final long generation = imageGeneration;
				final String source = image;
				Glide.with(imageView).load(ComponentSupport.resolveImageUri(image)).placeholder(placeholder).error(errorImage)
					.listener(new RequestListener<Drawable>() {
						@Override
						public boolean onLoadFailed(GlideException error, Object model, Target<Drawable> target, boolean first) {
							if (isCurrentImage(index, source, generation) && failed.add(index)) {
								图片加载失败(index, source);
							}
							return false;
						}

						@Override
						public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource origin, boolean first) {
							if (isCurrentImage(index, source, generation) && completed.add(index)) {
								图片加载完成(index, source);
							}
							return false;
						}
					}).into(imageView);
			}

			@Override
			protected void onItemImageClick(ImageView imageView, int index, List<String> images) {
				图片被单击(index, images.get(index));
			}

			@Override
			protected boolean onItemImageLongClick(ImageView imageView, int index, List<String> images) {
				图片被长按(index, images.get(index));
				return true;
			}
		});

		return view;
	}

	@Override
	public void 置间隔(int gap) {
		view.setGap(gap);
	}

	@Override
	public void 置显示样式(int showStyle) {
		view.setShowStyle(showStyle);
	}

	@Override
	public void 置单图尺寸(int singleImgSize) {
		view.setSingleImgSize(singleImgSize);
	}

	@Override
	public void 置最大尺寸(int maxSize) {
		view.setMaxSize(maxSize);
	}

	@Override
	public void 置图片数组(String[] images) {
		String[] copy = ComponentSupport.copyTextArray(images);
		// 先校验全部地址，非法输入不替换正在显示的数据或使旧回调提前失效。
		for (String source : copy) {
			ComponentSupport.resolveImageUri(source);
		}
		sources = copy;
		imageGeneration++;
		completed.clear();
		failed.clear();
		view.setImagesData(Arrays.asList(copy));
	}

	@Override
	public void 图片被单击(int index, String image) {
		EventDispatcher.dispatchEvent(this, "图片被单击", index, image);
	}

	@Override
	public void 图片被长按(int index, String image) {
		EventDispatcher.dispatchEvent(this, "图片被长按", index, image);
	}

	@Override
	public void 图片加载完成(int index, String image) {
		EventDispatcher.dispatchEvent(this, "图片加载完成", index, image);
	}

	@Override
	public void 图片加载失败(int index, String image) {
		EventDispatcher.dispatchEvent(this, "图片加载失败", index, image);
	}

	@Override
	public void 置占位图片(String loading, String error) {
		Drawable pending = ComponentSupport.getDrawable(loading);
		Drawable failure = ComponentSupport.getDrawable(error);
		placeholder = pending;
		errorImage = failure;
	}

	/**
	 * 替换数据、移出窗口或销毁后，迟到的网络回调不能通知新页面。
	 *
	 * @param index 原始位置。
	 * @param source 原始图片标识。
	 * @param generation 本次数据代数。
	 * @return 是否仍对应当前显示数据。
	 */
	private boolean isCurrentImage(int index, String source, long generation) {
		return !disposed && generation == imageGeneration && index >= 0 && index < sources.length
			&& sources[index].equals(source) && view.isAttachedToWindow();
	}

	@Override
	public void 销毁() {
		disposed = true;
		imageGeneration++;
		super.销毁();
	}

}
