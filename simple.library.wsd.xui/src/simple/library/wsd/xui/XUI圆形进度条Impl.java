package simple.library.wsd.xui;

import android.view.View;

import com.xuexiang.xui.widget.progress.CircleProgressView;

import simple.runtime.components.组件容器;
import simple.runtime.events.EventDispatcher;

/**
 * XUI圆形进度条的原生视图及 Simple 事件适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI圆形进度条Impl extends XUI视图组件基类 implements XUI圆形进度条 {

	// 视图在父类构造期间创建；不得用字段初始化器重置此处状态。
	private CircleProgressView view;

	/**
	 * 创建XUI圆形进度条。
	 *
	 * @param container 容纳组件的 Simple 容器。
	 */
	public XUI圆形进度条Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected View createView() {
		view = new CircleProgressView(ComponentSupport.createContext());

		view.setProgressViewUpdateListener(new CircleProgressView.CircleProgressUpdateListener() {

			@Override
			public void onCircleProgressStart(View progressView) {
				进度开始();
			}

			@Override
			public void onCircleProgressUpdate(View progressView, float progress) {
				进度改变(progress);
			}

			@Override
			public void onCircleProgressFinished(View progressView) {
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
	public void 置进度宽度(int width) {
		view.setProgressWidth(width);
	}

	@Override
	public void 置进度文本尺寸(int size) {
		view.setProgressTextSize(size);
	}

	@Override
	public void 置进度文本颜色(int textColor) {
		view.setProgressTextColor(textColor);
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
	public void 置轨道颜色(int color) {
		view.setTrackColor(color);
	}

	@Override
	public void 置填充启用(boolean fillEnabled) {
		view.setFillEnabled(fillEnabled);
	}

	@Override
	public void 置圆形缺口(boolean isBroken) {
		view.setCircleBroken(isBroken);
	}

	@Override
	public void 置刻度启用(boolean isGraduated) {
		view.setGraduatedEnabled(isGraduated);
	}

	@Override
	public void 置缩放区域宽度(float zoneWidth) {
		view.setScaleZoneWidth(zoneWidth);
	}

	@Override
	public void 置缩放区域长度(float zoneLength) {
		view.setScaleZoneLength(zoneLength);
	}

	@Override
	public void 置缩放区域内边距(int zonePadding) {
		view.setScaleZonePadding(zonePadding);
	}

	@Override
	public void 置缩放区域圆角(int cornerRadius) {
		view.setScaleZoneCornerRadius(cornerRadius);
	}

	@Override
	public void 置进度文本可见性(boolean visibility) {
		view.setProgressTextVisibility(visibility);
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
}
