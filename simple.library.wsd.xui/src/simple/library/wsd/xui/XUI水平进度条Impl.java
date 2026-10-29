package simple.library.wsd.xui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

import com.xuexiang.xui.R;
import com.xuexiang.xui.utils.ThemeUtils;
import com.xuexiang.xui.widget.progress.HorizontalProgressView;

import simple.runtime.components.组件容器;
import simple.runtime.events.EventDispatcher;

/**
 * XUI水平进度条的原生视图及 Simple 事件适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI水平进度条Impl extends XUI视图组件基类 implements XUI水平进度条 {

	// 视图在父类构造期间创建；不得用字段初始化器重置此处状态。
	private HorizontalProgressView view;

	/**
	 * 创建XUI水平进度条。
	 *
	 * @param container 容纳组件的 Simple 容器。
	 */
	public XUI水平进度条Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected View createView() {
		view = new ProgressView(ComponentSupport.createContext());

		view.setProgressViewUpdateListener(new HorizontalProgressView.HorizontalProgressUpdateListener() {

			@Override
			public void onHorizontalProgressStart(View progressView) {
				进度开始();
			}

			@Override
			public void onHorizontalProgressUpdate(View progressView, float progress) {
				进度改变(progress);
			}

			@Override
			public void onHorizontalProgressFinished(View progressView) {
				进度完成();
			}
		});

		return view;
	}

	@Override
	public void 置动画类型(int type) {
		view.setAnimateType(type);
	}

	@Override
	public void 置进度(float progress) {
		view.setProgress(progress);
	}

	@Override
	public float 取进度() {
		return view.getProgress();
	}

	@Override
	public void 置开始进度(float startProgress) {
		view.setStartProgress(startProgress);
	}

	@Override
	public void 置结束进度(float endProgress) {
		view.setEndProgress(endProgress);
	}

	@Override
	public void 置开始颜色(int startColor) {
		view.setStartColor(startColor);
	}

	@Override
	public void 置结束颜色(int endColor) {
		view.setEndColor(endColor);
	}

	@Override
	public void 置轨道宽度(int width) {
		view.setTrackWidth(width);
	}

	@Override
	public void 置轨道颜色(int color) {
		view.setTrackColor(color);
	}

	@Override
	public void 置进度文本颜色(int textColor) {
		view.setProgressTextColor(textColor);
	}

	@Override
	public void 置进度文本尺寸(int size) {
		view.setProgressTextSize(size);
	}

	@Override
	public void 置进度时长(int duration) {
		view.setProgressDuration(duration);
	}

	@Override
	public void 置轨道启用(boolean trackAble) {
		view.setTrackEnabled(trackAble);
	}

	@Override
	public void 置进度文本可见性(boolean visibility) {
		view.setProgressTextVisibility(visibility);
	}

	@Override
	public void 置进度文本跟随(boolean moved) {
		view.setProgressTextMoved(moved);
	}

	@Override
	public void 开始进度动画() {
		view.startProgressAnimation();
	}

	@Override
	public void 停止进度动画() {
		view.stopProgressAnimation();
	}

	@Override
	public void 置进度圆角(int radius) {
		view.setProgressCornerRadius(radius);
	}

	@Override
	public void 置进度文本内边距底部(int offset) {
		view.setProgressTextPaddingBottom(offset);
	}

	@Override
	public void 进度开始() {
		EventDispatcher.dispatchEvent(this, "进度开始");
	}

	@Override
	public void 进度改变(float progress) {
		EventDispatcher.dispatchEvent(this, "进度改变", progress);
	}

	@Override
	public void 进度完成() {
		EventDispatcher.dispatchEvent(this, "进度完成");
	}

	/**
	 * 保留原生轨道、动画和事件，只修正百分比文本的绘制边界。
	 * 上游采用固定的左右偏移，无法容纳 0% 与 100% 等不同宽度的文字。
	 */
	private static final class ProgressView extends HorizontalProgressView {

		// 文本画笔独立于原生渐变画笔，复用实例以免每帧创建对象。
		private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		private boolean textVisible;
		private boolean textMoved;
		private int textBottomOffset;

		/**
		 * 读取同一 XUI 样式，避免修正边界时丢失主题中配置的字体和颜色。
		 *
		 * @param context 已应用 XUI 主题的上下文。
		 */
		ProgressView(Context context) {
			super(context);

			TypedArray attributes = context.obtainStyledAttributes(null,
				R.styleable.HorizontalProgressView, R.attr.HorizontalProgressViewStyle, 0);
			try {
				textPaint.setTextSize(attributes.getDimensionPixelSize(
					R.styleable.HorizontalProgressView_hpv_progress_textSize,
					getResources().getDimensionPixelSize(R.dimen.default_pv_horizontal_text_size)));
				textPaint.setColor(attributes.getColor(
					R.styleable.HorizontalProgressView_hpv_progress_textColor,
					ThemeUtils.getMainThemeColor(context)));
				textVisible = attributes.getBoolean(
					R.styleable.HorizontalProgressView_hpv_progress_textVisibility, true);
				textMoved = attributes.getBoolean(
					R.styleable.HorizontalProgressView_hpv_text_movedEnable, true);
				textBottomOffset = attributes.getDimensionPixelSize(
					R.styleable.HorizontalProgressView_hpv_text_padding_bottom,
					getResources().getDimensionPixelSize(R.dimen.default_pv_corner_radius));
			} finally {
				attributes.recycle();
			}

			textPaint.setTextAlign(Paint.Align.CENTER);
			super.setProgressTextVisibility(false);
		}

		@Override
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);
			if (!textVisible) {
				return;
			}

			String text = ((int) getProgress()) + "%";
			float left = getPaddingLeft();
			float right = getWidth() - getPaddingRight();
			float halfTextWidth = textPaint.measureText(text) / 2;
			float fraction = Math.max(0, Math.min(1, getProgress() / 100));
			float center = textMoved ? left + (right - left) * fraction : (left + right) / 2;

			// 以实际文本宽度夹住中心点，既保留跟随动画，也避免两端的百分号被截断。
			if (right - left >= halfTextWidth * 2) {
				center = Math.max(left + halfTextWidth, Math.min(right - halfTextWidth, center));
			} else {
				center = (left + right) / 2;
			}

			float baseline = getHeight() / 2F - getPaddingTop() - textBottomOffset;
			Paint.FontMetrics metrics = textPaint.getFontMetrics();
			baseline = Math.max(getPaddingTop() - metrics.ascent,
				Math.min(getHeight() - getPaddingBottom() - metrics.descent, baseline));
			canvas.drawText(text, center, baseline, textPaint);
		}

		@Override
		public void setProgressTextColor(int color) {
			textPaint.setColor(color);
			invalidate();
		}

		@Override
		public void setProgressTextSize(int size) {
			textPaint.setTextSize(size);
			invalidate();
		}

		@Override
		public void setProgressTextVisibility(boolean visible) {
			textVisible = visible;
			invalidate();
		}

		@Override
		public void setProgressTextMoved(boolean moved) {
			textMoved = moved;
			invalidate();
		}

		@Override
		public void setProgressTextPaddingBottom(int offset) {
			textBottomOffset = offset;
			invalidate();
		}
	}
}
