package simple.library.wsd.xui;

import android.content.res.ColorStateList;
import android.graphics.drawable.LayerDrawable;
import android.view.LayoutInflater;
import android.view.View;

import com.xuexiang.xui.widget.progress.materialprogressbar.CircularProgressDrawable;
import com.xuexiang.xui.widget.progress.materialprogressbar.HorizontalProgressDrawable;
import com.xuexiang.xui.widget.progress.materialprogressbar.IndeterminateCircularProgressDrawable;
import com.xuexiang.xui.widget.progress.materialprogressbar.IndeterminateHorizontalProgressDrawable;
import com.xuexiang.xui.widget.progress.materialprogressbar.MaterialProgressBar;

import simple.runtime.components.组件容器;
import simple.runtime.errors.非法参数错误;

/**
 * XUI材质进度条的原生视图及 Simple 事件适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI材质进度条Impl extends XUI视图组件基类 implements XUI材质进度条 {

	// 稳定视图身份，切换样式时保留任务进度、缓冲进度及所有已配置颜色。
	private int progressStyle;
	private int secondaryProgress;

	// 视图在父类构造期间创建；不得用字段初始化器重置此处状态。
	private MaterialProgressBar view;

	// Android 在不确定模式下不保存 setProgress 的值，因此由封装持有任务进度。
	private int progress;

	/**
	 * 创建XUI材质进度条。
	 *
	 * @param container 容纳组件的 Simple 容器。
	 */
	public XUI材质进度条Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected View createView() {
		// Android 的默认圆形样式会锁定不确定模式，须在构造时关闭 indeterminateOnly。
		// 同时让上游初始化两种 Drawable，保留其主题着色、内边距和模式切换行为。
		view = (MaterialProgressBar) LayoutInflater.from(ComponentSupport.createContext())
			.inflate(R.layout.es4a_xui_material_progress, null);

		return view;
	}

	@Override
	public void 置不确定进度(boolean indeterminate) {
		view.setIndeterminate(indeterminate);
		if (!indeterminate) {
			restoreProgress();
		}
	}

	@Override
	public boolean 是否不确定进度() {
		return view.isIndeterminate();
	}

	@Override
	public int 进度() {
		return progress;
	}

	@Override
	public void 进度(int value) {
		if (value < 0 || value > 最大进度()) {
			throw new 非法参数错误();
		}
		progress = value;
		view.setProgress(value);
	}

	@Override
	public int 最大进度() {
		return view.getMax();
	}

	@Override
	public void 最大进度(int value) {
		if (value <= 0) {
			throw new 非法参数错误();
		}
		view.setMax(value);
		progress = Math.min(progress, value);
		secondaryProgress = Math.min(secondaryProgress, value);
		restoreProgress();
	}

	@Override
	public int 取进度样式() {
		return progressStyle;
	}

	@Override
	public boolean 是否使用固有内边距() {
		return view.getUseIntrinsicPadding();
	}

	@Override
	public void 置使用固有内边距(boolean useIntrinsicPadding) {
		view.setUseIntrinsicPadding(useIntrinsicPadding);
	}

	@Override
	public boolean 是否显示进度背景() {
		return view.getShowProgressBackground();
	}

	@Override
	public void 置显示进度背景(boolean show) {
		view.setShowProgressBackground(show);
	}

	@Override
	public void 置进度颜色(int color) {
		view.setSupportProgressTintList(ColorStateList.valueOf(color));
	}

	@Override
	public void 置等待颜色(int color) {
		view.setSupportIndeterminateTintList(ColorStateList.valueOf(color));
	}

	@Override
	public void 置进度背景颜色(int color) {
		view.setSupportProgressBackgroundTintList(ColorStateList.valueOf(color));
	}

	@Override
	public void 置次级进度颜色(int color) {
		view.setSupportSecondaryProgressTintList(ColorStateList.valueOf(color));
	}

	@Override
	public void 置进度样式(int style) {
		if (style < 0 || style > 1) {
			throw new 非法参数错误();
		}
		if (progressStyle == style) {
			return;
		}
		boolean padding = view.getUseIntrinsicPadding();
		boolean background = view.getShowProgressBackground();
		boolean waiting = view.isIndeterminate();
		ColorStateList foreground = view.getSupportProgressTintList();
		ColorStateList secondary = view.getSupportSecondaryProgressTintList();
		ColorStateList track = view.getSupportProgressBackgroundTintList();
		ColorStateList indefinite = view.getSupportIndeterminateTintList();
		if (style == 0) {
			view.setProgressDrawable(new CircularProgressDrawable(0, view.getContext()));
			view.setIndeterminateDrawable(new IndeterminateCircularProgressDrawable(view.getContext()));
		} else {
			view.setProgressDrawable(new HorizontalProgressDrawable(view.getContext()));
			view.setIndeterminateDrawable(new IndeterminateHorizontalProgressDrawable(view.getContext()));
		}
		progressStyle = style;
		view.setSupportProgressTintList(foreground);
		view.setSupportSecondaryProgressTintList(secondary);
		view.setSupportProgressBackgroundTintList(track);
		view.setSupportIndeterminateTintList(indefinite);
		view.setUseIntrinsicPadding(padding);
		view.setShowProgressBackground(background);
		view.setIndeterminate(waiting);
		restoreProgress();
		view.requestLayout();
	}

	/**
	 * 同步任务数值与新绘制对象的层级；等待模式中的平台 setter 不会更新实际进度层。
	 * 样式切换后数值可能未变，不能依赖平台的“数值变化”分支刷新替换后的 Drawable。
	 */
	private void restoreProgress() {
		view.setProgress(progress);
		view.setSecondaryProgress(secondaryProgress);
		if (!view.isIndeterminate()) {
			LayerDrawable drawable = (LayerDrawable) view.getProgressDrawable();
			drawable.findDrawableByLayerId(android.R.id.progress).setLevel((int) (10000L * progress / view.getMax()));
			drawable.findDrawableByLayerId(android.R.id.secondaryProgress).setLevel((int) (10000L * secondaryProgress / view.getMax()));
			view.invalidate();
		}
	}

	@Override
	public void 置次级进度(int value) {
		if (value < 0 || value > 最大进度()) {
			throw new 非法参数错误();
		}
		secondaryProgress = value;
		view.setSecondaryProgress(value);
	}

	@Override
	public int 取次级进度() {
		return secondaryProgress;
	}

}
