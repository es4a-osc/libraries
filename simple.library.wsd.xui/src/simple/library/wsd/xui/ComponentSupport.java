package simple.library.wsd.xui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.xuexiang.xui.widget.banner.recycler.BannerLayout;
import com.xuexiang.xui.widget.banner.widget.banner.base.BaseBanner;
import com.xuexiang.xui.widget.button.CountDownButton;
import com.xuexiang.xui.widget.flowlayout.FlowLayout;
import com.xuexiang.xui.widget.layout.linkage.LinkageScrollLayout;
import com.xuexiang.xui.widget.progress.CircleProgressView;
import com.xuexiang.xui.widget.progress.HorizontalProgressView;
import com.xuexiang.xui.widget.progress.loading.ARCLoadingView;
import com.xuexiang.xui.widget.progress.loading.LoadingViewLayout;
import com.xuexiang.xui.widget.progress.loading.MiniLoadingView;
import com.xuexiang.xui.widget.progress.loading.RotateLoadingView;
import com.xuexiang.xui.widget.spinner.DropDownMenu;
import com.xuexiang.xui.widget.tabbar.VerticalTabLayout;
import com.xuexiang.xui.widget.tabbar.vertical.TabAdapter;
import com.xuexiang.xui.widget.tabbar.vertical.TabView;
import com.xuexiang.xui.widget.textview.marqueen.MarqueeView;

import simple.runtime.android.MainActivity;
import simple.runtime.components.impl.android.util.ImageUtil;
import simple.runtime.components.impl.android.util.ResourcesUtil;
import simple.runtime.components.impl.android.视图组件;
import simple.runtime.components.可视组件;
import simple.runtime.components.组件;
import simple.runtime.errors.文件未存在错误;
import simple.runtime.errors.索引超出界限错误;
import simple.runtime.errors.非法参数错误;

/**
 * 统一处理 XUI 主题上下文、Simple 视图边界及图片读写。
 *
 * @author 树先生 xhwsd@qq.com
 */
final class ComponentSupport {

	/**
	 * 停止组件拥有的原生任务；由销毁边界在注销事件和移出视图之后调用。
	 *
	 * @param view 已移出父容器的原生组件。
	 */
	static void releaseView(View view) {
		view.animate().cancel();
		view.clearAnimation();
		if (view instanceof BaseBanner) {
			((BaseBanner<?, ?>) view).recycle();
		} else if (view instanceof BannerLayout) {
			((BannerLayout) view).setAutoPlaying(false);
		} else if (view instanceof CountDownButton) {
			((CountDownButton) view).cancelCountDown();
		} else if (view instanceof CircleProgressView) {
			((CircleProgressView) view).stopProgressAnimation();
		} else if (view instanceof HorizontalProgressView) {
			((HorizontalProgressView) view).stopProgressAnimation();
		} else if (view instanceof ARCLoadingView) {
			((ARCLoadingView) view).recycle();
		} else if (view instanceof RotateLoadingView) {
			((RotateLoadingView) view).recycle();
		} else if (view instanceof MiniLoadingView) {
			((MiniLoadingView) view).stop();
		} else if (view instanceof LoadingViewLayout) {
			((LoadingViewLayout) view).recycle();
		} else if (view instanceof MarqueeView) {
			((MarqueeView) view).stopFlipping();
		} else if (view instanceof WebView) {
			((WebView) view).destroy();
		}
	}

	/**
	 * 内部数据适配器的条目选择回调。
	 */
	interface ItemSelectionListener {

		/**
		 * 报告用户选择的条目。
		 *
		 * @param index 从 0 开始的条目索引。
		 * @param text 条目文本。
		 */
		void onItemSelected(int index, String text);
	}

	/**
	 * 工具类不允许实例化。
	 */
	private ComponentSupport() {
	}

	/**
	 * 创建组件专用主题，不改变 Simple 窗口自身主题或宿主。
	 *
	 * @return 使用 XUI 手机主题的上下文。
	 */
	static Context createContext() {
		return ThemeSupport.createContext();
	}

	/**
	 * 把 Simple 图片标识转换为可绘制对象。
	 *
	 * @param image 资源标识、资产文件名或外部文件路径。
	 * @return 图片，空文本返回空。
	 * @throws 文件未存在错误 找不到图片时抛出。
	 */
	static Drawable getDrawable(String image) {
		try {
			return ImageUtil.getDrawable(image);
		} catch (IOException exception) {
			throw new 文件未存在错误(image);
		}
	}

	/**
	 * 校验从零开始的项目索引，不依赖第三方组件静默截断。
	 *
	 * @param index 项目索引。
	 * @param size 项目总数。
	 * @throws simple.runtime.errors.索引超出界限错误 索引越界时抛出。
	 */
	static void validateIndex(int index, int size) {
		if (index < 0 || index >= size) {
			throw new 索引超出界限错误();
		}
	}

	/**
	 * 复制外部项目数据，拒绝空引用，避免调用方后续修改数组影响组件。
	 *
	 * @param items 原始项目数组。
	 * @return 与原数组独立的数据副本。
	 * @throws 非法参数错误 数组或任意项目为空引用时抛出。
	 */
	static String[] copyTextArray(String[] items) {
		if (items == null) {
			throw new 非法参数错误();
		}
		String[] copy = items.clone();
		for (String item : copy) {
			if (item == null) {
				throw new 非法参数错误();
			}
		}
		return copy;
	}

	/**
	 * 在修改标签前检查标题、值数组及标题唯一性。
	 *
	 * @param titles 标签标题。
	 * @param values 与标题一一对应的项目值。
	 * @throws 非法参数错误 数组为空引用、长度不同或含重复标题时抛出。
	 */
	static void validateTabArrays(String[] titles, String[] values) {
		String[] checked = copyTextArray(titles);
		copyTextArray(values);
		if (titles.length != values.length || new HashSet<String>(Arrays.asList(checked)).size() != checked.length) {
			throw new 非法参数错误();
		}
	}

	/**
	 * 保留 XUI 下拉菜单的标题栏、分隔线和正文外壳，只清除旧内容。
	 *
	 * @param view 要重新设置内容的 XUI 1.2.1 XUI下拉菜单。
	 * @throws ComponentStateException 原生视图结构与当前适配版本不符。
	 */
	static void resetDropDownContent(DropDownMenu view) {
		if (view.getChildCount() != 3 || !(view.getChildAt(0) instanceof LinearLayout)
				|| !(view.getChildAt(2) instanceof ViewGroup)) {
			throw new ComponentStateException("XUI 下拉菜单的内部结构已改变。");
		}
		view.closeMenu();
		((ViewGroup) view.getChildAt(0)).removeAllViews();
		((ViewGroup) view.getChildAt(2)).removeAllViews();
	}

	/**
	 * 将选中位置列表转换为 Simple 整数数组。
	 *
	 * @param values 已选索引列表。
	 * @return 独立的索引数组。
	 */
	static int[] toIntArray(List<?> values) {
		int[] result = new int[values.size()];
		for (int index = 0; index < result.length; index++) {
			result[index] = ((Number) values.get(index)).intValue();
		}
		return result;
	}

	/**
	 * 校验全部菜单组件后再移出父容器，避免无效输入造成部分移动。
	 *
	 * @param owner 将接管组件的下拉菜单。
	 * @param menus 下拉内容组件。
	 * @param content 正文组件。
	 * @return 下拉内容及末尾的正文视图。
	 * @throws 非法参数错误 输入重复、包含祖先或不是有效可视组件时抛出。
	 */
	static List<View> detachMenuComponents(View owner, 可视组件[] menus, 可视组件 content) {
		List<View> targets = new ArrayList<View>();
		可视组件[] all = Arrays.copyOf(menus, menus.length + 1);
		all[menus.length] = content;
		for (可视组件 component : all) {
			if (!(component instanceof 视图组件)) {
				throw new 非法参数错误();
			}
			View target = ((视图组件) component).getView();
			if (target == null || targets.contains(target)) {
				throw new 非法参数错误();
			}
			for (View parent = owner; parent != null; parent = parent.getParent() instanceof View ? (View) parent.getParent() : null) {
				if (parent == target) {
					throw new 非法参数错误();
				}
			}
			targets.add(target);
		}

		// 菜单内容不能相互包含，否则移动父组件时会隐式改变另一个输入。
		for (View target : targets) {
			for (ViewParent parent = target.getParent(); parent instanceof View; parent = parent.getParent()) {
				if (targets.contains((View) parent)) {
					throw new 非法参数错误();
				}
			}
		}
		for (View target : targets) {
			if (target.getParent() instanceof ViewGroup) {
				((ViewGroup) target.getParent()).removeView(target);
			}
		}
		return targets;
	}

	/**
	 * 创建列表和轮播共用的文本适配器。
	 *
	 * @param items 独立的条目数据。
	 * @param listener 项目单击回调。
	 * @return AndroidX 回收列表适配器。
	 */
	static RecyclerView.Adapter<RecyclerView.ViewHolder> createTextListAdapter(final String[] items, final ItemSelectionListener listener) {
		return new RecyclerView.Adapter<RecyclerView.ViewHolder>() {
			@Override
			public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int type) {
				TextView text = new TextView(parent.getContext());
				text.setTextSize(16);
				text.setPadding(24, 24, 24, 24);
				text.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
				return new RecyclerView.ViewHolder(text) {
				};
			}

			@Override
			public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
				((TextView) holder.itemView).setText(items[position]);
				holder.itemView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						int index = holder.getBindingAdapterPosition();
						if (index != RecyclerView.NO_POSITION) {
							listener.onItemSelected(index, items[index]);
							}
					}
				});
			}

			@Override
			public int getItemCount() {
				return items.length;
			}
		};
	}

	/**
	 * 使用文本标题创建纵向标签，保留 XUI 自身的指示器及选中处理。
	 *
	 * @param view 纵向标签视图。
	 * @param titles 标签标题。
	 */
	static void populateVerticalTabs(VerticalTabLayout view, final String[] titles) {
		view.setTabAdapter(new TabAdapter() {
			@Override
			public int getCount() {
				return titles.length;
			}

			@Override
			public TabView.TabBadge getBadge(int position) {
				return null;
			}

			@Override
			public TabView.TabIcon getIcon(Context context, int position) {
				return null;
			}

			@Override
			public TabView.TabTitle getTitle(int position) {
				return new TabView.TabTitle.Builder().setContent(titles[position]).build();
			}

			@Override
			public int getBackground(int position) {
				return 0;
			}
		});
	}

	/**
	 * 把可绘制对象转换为独立位图，供裁剪和贴图引擎使用。
	 * 位图资源保留原始像素尺寸与密度；其它可绘制对象按固有尺寸栅格化。
	 *
	 * @param drawable 待转换图片。
	 * @return 图片位图。
	 * @throws 非法参数错误 图片为空时抛出。
	 * @throws OutOfMemoryError 无法分配独立位图时抛出。
	 */
	static Bitmap toBitmap(Drawable drawable) {
		if (drawable == null) {
			throw new 非法参数错误();
		}
		if (drawable instanceof BitmapDrawable) {
			// Drawable 的固有尺寸已按目标密度换算，不能据此重采样原始图片。
			Bitmap source = ((BitmapDrawable) drawable).getBitmap();
			if (source != null) {
				Bitmap bitmap = source.copy(Bitmap.Config.ARGB_8888, true);
				if (bitmap == null) {
					throw new OutOfMemoryError("无法复制图片位图");
				}
				bitmap.setDensity(source.getDensity());
				return bitmap;
			}
		}

		int width = Math.max(1, drawable.getIntrinsicWidth());
		int height = Math.max(1, drawable.getIntrinsicHeight());
		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		drawable.setBounds(0, 0, width, height);
		drawable.draw(new Canvas(bitmap));
		return bitmap;
	}

	/**
	 * 将 Simple 资产和外部路径转换为轮播引擎使用的 URI。
	 *
	 * @param image 图片标识或网络地址。
	 * @return 可由 Glide 加载的地址。
	 */
	static String resolveImageUri(String image) {
		if (image.startsWith("http://") || image.startsWith("https://") || image.startsWith("file://") || image.startsWith("content://")) {
			return image;
		}
		if (image.startsWith("/")) {
			return "file://" + image;
		}
		int resource = 0;
		if (image.startsWith("@")) {
			resource = ResourcesUtil.getResourcesId(image);
		} else if (image.matches("[0-9]+")) {
			try {
				resource = Integer.parseInt(image);
			} catch (NumberFormatException exception) {
				throw new 非法参数错误();
			}
		}
		if (resource != 0) {
			return "android.resource://" + MainActivity.getContext().getPackageName() + "/" + resource;
		}
		if (image.startsWith("@")) {
			throw new 文件未存在错误(image);
		}
		return "file:///android_asset/" + image;
	}

	/**
	 * 加载 Simple 本地图片或网络图片，不触发任何预览窗口。
	 *
	 * @param target 目标图片组件。
	 * @param image 图片标识或网络地址。
	 */
	static void loadImage(ImageView target, String image) {
		if (image.startsWith("http://") || image.startsWith("https://") || image.startsWith("content://")) {
			Glide.with(target).load(image).into(target);
		} else {
			target.setImageDrawable(getDrawable(image));
		}
	}

	/**
	 * 保存 PNG 图片；不隐式创建外部目录或申请存储权限。
	 *
	 * @param bitmap 要保存的位图。
	 * @param path 相对或绝对目标路径。
	 * @throws 非法参数错误 图像尚未准备好时抛出。
	 * @throws 文件未存在错误 无法写入路径时抛出。
	 */
	static void saveImage(Bitmap bitmap, String path) {
		saveImage(bitmap, path, Bitmap.CompressFormat.PNG, 100);
	}

	/**
	 * 裁去全透明的外围行列，保留内容像素及其透明度；全透明图和无需裁剪时返回原图。
	 *
	 * @param source 原始位图，不转移其所有权。
	 * @return 原图或独立裁剪结果；调用者只回收新建结果。
	 */
	static Bitmap trimTransparentEdges(Bitmap source) {
		int width = source.getWidth();
		int height = source.getHeight();
		int left = width;
		int top = height;
		int right = -1;
		int bottom = -1;
		int[] row = new int[width];
		for (int y = 0; y < height; y++) {
			source.getPixels(row, 0, width, 0, y, width, 1);
			for (int x = 0; x < width; x++) {
				if ((row[x] >>> 24) != 0) {
					left = Math.min(left, x);
					top = Math.min(top, y);
					right = Math.max(right, x);
					bottom = y;
				}
			}
		}
		if (right < 0 || (left == 0 && top == 0 && right == width - 1 && bottom == height - 1)) {
			return source;
		}
		return Bitmap.createBitmap(source, left, top, right - left + 1, bottom - top + 1);
	}

	/**
	 * 将相对图片输出路径按 Simple 应用内部文件目录解析。
	 *
	 * @param path 相对或绝对文件路径。
	 * @return 实际输出文件，不隐式创建上级目录。
	 * @throws 非法参数错误 路径为空时抛出。
	 */
	static File resolveOutputFile(String path) {
		if (path == null || path.isEmpty()) {
			throw new 非法参数错误();
		}
		File file = new File(path);
		return file.isAbsolute() ? file : new File(MainActivity.getContext().getFilesDir(), path);
	}

	/**
	 * 取得可容纳 Simple 子组件的布局；单子项滚动容器使用内部纵向布局。
	 *
	 * @param view XUI 容器。
	 * @return 子组件实际所属布局。
	 */
	static ViewGroup getChildLayout(ViewGroup view) {
		if (view instanceof ScrollView || view instanceof NestedScrollView) {
			if (view.getChildCount() == 0) {
				LinearLayout content = new LinearLayout(view.getContext());
				content.setOrientation(LinearLayout.VERTICAL);
				view.addView(content, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
			}
			return (ViewGroup) view.getChildAt(0);
		}
		return view;
	}

	/**
	 * 把新建的 Simple 可视组件放入 XUI 容器。
	 *
	 * @param parent 父容器。
	 * @param component 新建的子组件。
	 * @throws 非法参数错误 组件不是可视组件或已经有父容器时抛出。
	 */
	static void addChildComponent(ViewGroup parent, 组件 component) {
		if (!(component instanceof 视图组件)) {
			throw new 非法参数错误();
		}
		View child = ((视图组件) component).getView();
		if (child == null || child.getParent() != null) {
			throw new 非法参数错误();
		}
		ViewGroup layout = getChildLayout(parent);
		// 这两类原生 ViewGroup 的程序化默认参数不含边距，Simple 子组件必须使用 MarginLayoutParams。
		if (layout instanceof FlowLayout
				|| layout instanceof LinkageScrollLayout) {
			child.setLayoutParams(new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		}
		layout.addView(child);
	}

	/**
	 * 绑定基础交互；已有内部单击处理器的组件保留其原有行为。
	 *
	 * @param component 事件接收组件。
	 * @param view 原生视图。
	 */
	static void bindViewEvents(final XUI视图组件 component, View view) {
		if (!(view instanceof AdapterView) && !view.hasOnClickListeners()) {
			view.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View clicked) {
					component.被单击();
				}
			});
		}
		view.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View clicked) {
				component.被长按();
				return true;
			}
		});
		view.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View changed, boolean focused) {
				component.焦点改变(focused);
			}
		});
	}

	/**
	 * 将 Android 文本监听转换为 Simple 事件，不替换组件内部文本监听。
	 *
	 * @param component 接收事件的文本组件。
	 * @param view 文本视图。
	 */
	static void bindTextEvents(final XUI文本组件 component, TextView view) {
		view.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence text, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence text, int start, int before, int count) {
				component.文本改变(text.toString());
			}

			@Override
			public void afterTextChanged(Editable text) {
			}
		});
	}

	/**
	 * 按明确格式编码独立图片，调用方负责输出路径与异步配置快照。
	 *
	 * @param bitmap 已生成的图片。
	 * @param path 输出文件路径。
	 * @param format 编码格式。
	 * @param quality 编码质量。
	 */
	static void saveImage(Bitmap bitmap, String path, Bitmap.CompressFormat format, int quality) {
		if (bitmap == null || format == null || quality < 0 || quality > 100) {
			throw new 非法参数错误();
		}
		try (FileOutputStream output = new FileOutputStream(resolveOutputFile(path))) {
			if (!bitmap.compress(format, quality, output)) {
				throw new IOException("无法编码图片。");
			}
		} catch (IOException exception) {
			throw new 文件未存在错误(path + "：" + exception.getMessage());
		}
	}

}
