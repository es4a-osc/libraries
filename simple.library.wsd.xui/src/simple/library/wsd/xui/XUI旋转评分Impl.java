package simple.library.wsd.xui;

import android.view.View;

import com.xuexiang.xui.widget.progress.ratingbar.RatingBar;
import com.xuexiang.xui.widget.progress.ratingbar.RotationRatingBar;

import simple.runtime.components.组件容器;
import simple.runtime.events.EventDispatcher;

/**
 * XUI旋转评分的原生视图及 Simple 事件适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI旋转评分Impl extends XUI视图组件基类 implements XUI旋转评分 {

	// 视图在父类构造期间创建；不得用字段初始化器重置此处状态。
	private RotationRatingBar view;

	/**
	 * 创建XUI旋转评分。
	 *
	 * @param container 容纳组件的 Simple 容器。
	 */
	public XUI旋转评分Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected View createView() {
		view = new RotationRatingBar(ComponentSupport.createContext());

		view.setOnRatingChangeListener(new RatingBar.OnRatingChangeListener() {

			@Override
			public void onRatingChange(RatingBar bar, float rating) {
				评分改变(rating);
			}
		});

		return view;
	}

	@Override
	public void 置星数(int numStars) {
		view.setNumStars(numStars);
	}

	@Override
	public int 取星数() {
		return view.getNumStars();
	}

	@Override
	public void 置评分(float rating) {
		view.setRating(rating);
	}

	@Override
	public float 取评分() {
		return view.getRating();
	}

	@Override
	public void 置星宽度(int starWidth) {
		view.setStarWidth(starWidth);
	}

	@Override
	public int 取星宽度() {
		return view.getStarWidth();
	}

	@Override
	public void 置星高度(int starHeight) {
		view.setStarHeight(starHeight);
	}

	@Override
	public int 取星高度() {
		return view.getStarHeight();
	}

	@Override
	public void 置星内边距(int ratingPadding) {
		view.setStarPadding(ratingPadding);
	}

	@Override
	public int 取星内边距() {
		return view.getStarPadding();
	}

	@Override
	public void 置最小星数(float minimumStars) {
		view.setMinimumStars(minimumStars);
	}

	@Override
	public boolean 是否指示() {
		return view.isIndicator();
	}

	@Override
	public void 置是否指示(boolean indicator) {
		view.setIsIndicator(indicator);
	}

	@Override
	public boolean 是否可滑动() {
		return view.isScrollable();
	}

	@Override
	public void 置可滑动(boolean scrollable) {
		view.setScrollable(scrollable);
	}

	@Override
	public boolean 是否可单击() {
		return view.isClickable();
	}

	@Override
	public void 置可单击(boolean clickable) {
		view.setClickable(clickable);
	}

	@Override
	public void 置清除评分启用(boolean enabled) {
		view.setClearRatingEnabled(enabled);
	}

	@Override
	public boolean 是否清除评分启用() {
		return view.isClearRatingEnabled();
	}

	@Override
	public float 取步长尺寸() {
		return view.getStepSize();
	}

	@Override
	public void 置步长尺寸(float stepSize) {
		view.setStepSize(stepSize);
	}

	@Override
	public void 置为空图像(String image) {
		view.setEmptyDrawable(ComponentSupport.getDrawable(image));
	}

	@Override
	public void 置实心图像(String image) {
		view.setFilledDrawable(ComponentSupport.getDrawable(image));
	}

	@Override
	public void 评分改变(float rating) {
		EventDispatcher.dispatchEvent(this, "评分改变", rating);
	}
}
