package simple.library.wsd.xui;

import android.view.View;

import androidx.viewpager.widget.ViewPager;

import com.xuexiang.xui.widget.imageview.preview.view.BezierBannerView;

import simple.runtime.components.impl.android.视图组件;
import simple.runtime.components.可视组件;
import simple.runtime.components.组件容器;
import simple.runtime.errors.非法参数错误;

/**
 * XUI贝塞尔指示器的原生视图及 Simple 事件适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI贝塞尔指示器Impl extends XUI视图组件基类 implements XUI贝塞尔指示器 {

	private ViewPager boundPager;
	private int direction;


	// 视图在父类构造期间创建；不得用字段初始化器重置此处状态。
	private BezierBannerView view;

	/**
	 * 创建XUI贝塞尔指示器。
	 *
	 * @param container 容纳组件的 Simple 容器。
	 */
	public XUI贝塞尔指示器Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected View createView() {
		view = new BezierBannerView(ComponentSupport.createContext());
		direction = 2;

		return view;
	}

	@Override
	public void 置进度(float progress) {
		if (!Float.isFinite(progress) || progress < 0 || progress > 1 || boundPager == null) {
			throw new 非法参数错误();
		}
		if (progress == 0) {
			重置进度();
		} else {
			view.setProgress(progress);
		}
	}

	@Override
	public void 绑定页面框(可视组件 pager) {
		View target = ((视图组件) pager).getView();
		if (!(target instanceof ViewPager)) {
			throw new 非法参数错误();
		}
		if (boundPager != null) {
			boundPager.removeOnPageChangeListener(view);
		}
		boundPager = (ViewPager) target;
		view.attachToViewpager(boundPager);
		view.setDirection(direction);
		重置进度();
		view.requestLayout();
	}

	@Override
	public void 置方向(int direction) {
		if (direction != 1 && direction != 2) {
			throw new 非法参数错误();
		}
		this.direction = direction;
		view.setDirection(direction);
	}

	@Override
	public void 重置进度() {
		if (boundPager == null) {
			throw new ComponentStateException("请先绑定页面框。");
		}
		view.resetProgress();
		// 上游零进度会直接返回，不重算已有路径；最小正数用于触发原生几何计算，
		// 随后清零进度。浮点精度下路径回到原点，同时避免反射私有坐标或重建视图。
		view.setProgress(Float.MIN_VALUE);
		view.resetProgress();
		view.invalidate();
	}


	@Override
	public void 销毁() {
		if (boundPager != null) {
			boundPager.removeOnPageChangeListener(view);
			boundPager = null;
		}
		super.销毁();
	}

}
