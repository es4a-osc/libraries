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
public final class 飞行场景Impl extends 视图组件 implements 飞行场景 {
	private int color;
	private float percent;

	/**
	 * 创建飞行场景。
	 *
	 * @param container 所属组件容器。
	 */
	public 飞行场景Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected View createView() {
		return new MountainSceneView(MainActivity.getContext());
	}

	@Override
	public int 场景主色() {
		return color;
	}

	@Override
	public void 场景主色(int value) {
		color = value;
		scene().setPrimaryColor(value);
		getView().invalidate();
	}

	@Override
	public float 场景比例() {
		return percent;
	}

	@Override
	public void 场景比例(float value) {
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
		return (MountainSceneView) getView();
	}
}
