package simple.library.wsd.smartrefresh;

import android.view.View;
import com.scwang.smart.refresh.header.flyrefresh.MountainSceneView;
import simple.runtime.android.MainActivity;
import simple.runtime.components.组件容器;
import simple.runtime.components.impl.android.视图组件;

/**
 * 将真实山丘绘制视图接入 Simple 的可视组件及飞行场景绑定。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class 飞行场景Impl extends 视图组件 implements 飞行场景, MainActivity.OnDestroyListener {
	private int color;
	private float percent;
	private float viewportHeight;
	private boolean destroyed;

	/**
	 * 创建飞行场景。
	 *
	 * @param container 所属组件容器。
	 */
	public 飞行场景Impl(组件容器 container) {
		super(container);
		MainActivity.getContext().addOnDestroyListener(this);
	}

	@Override
	protected View createView() {
		return new SceneView(MainActivity.getContext());
	}

	@Override
	public int 场景主色() {
		check();
		return color;
	}

	@Override
	public void 场景主色(int value) {
		check();
		color = value;
		scene().setPrimaryColor(value);
		getView().invalidate();
	}

	@Override
	public float 场景比例() {
		check();
		return percent;
	}

	@Override
	public void 场景比例(float value) {
		check();
		if (!Float.isFinite(value) || value < 0 || value > 10) {
			throw new IllegalArgumentException("场景比例必须为 0 至 10 的有限数值。");
		}
		percent = value;
		scene().updatePercent(value);
		getView().invalidate();
	}

	/**
	 * 取得绑定所需的原生场景，不作为 Simple 接口公开。
	 *
	 * @return 原生场景。
	 */
	MountainSceneView scene() {
		check();
		return (MountainSceneView) getView();
	}

	@Override
	public float 场景视口高度() {
		check();
		return viewportHeight;
	}

	@Override
	public void 场景视口高度(float value) {
		check();
		if (!Float.isFinite(value) || value < 0 || value > 10000) {
			throw new IllegalArgumentException("场景视口高度须为 0 至 10000 DIP 的有限数值");
		}
		viewportHeight = value;
		((SceneView) scene()).viewport(Math.round(value * getView().getResources().getDisplayMetrics().density));
	}

	/**
	 * 保留上游山丘绘制并开放其 XML 视口配置。
	 *
	 * @author 树先生 xhwsd@qq.com
	 */
	private static final class SceneView extends MountainSceneView {
		/**
		 * 创建当前组件的山丘视图。
		 *
		 * @param context 所属活动。
		 */
		SceneView(android.content.Context context) {
			super(context);
		}

		/**
		 * 修改实际绘制视口并重新测量。
		 *
		 * @param pixels 视口像素高度。
		 */
		void viewport(int pixels) {
			mViewportHeight = pixels;
			requestLayout();
			invalidate();
		}
	}

	/**
	 * 拒绝非主线程及销毁后访问场景。
	 */
	private void check() {
		if (destroyed || android.os.Looper.myLooper() != android.os.Looper.getMainLooper()) {
			throw new 刷新错误("飞行场景只能在主线程且销毁前访问");
		}
	}

	@Override
	public void onDestroy() {
		销毁();
	}

	@Override
	public void 销毁() {
		if (!destroyed) {
			check();
			destroyed = true;
			MainActivity.getContext().removeOnDestroyListener(this);
			super.销毁();
		}
	}

}
