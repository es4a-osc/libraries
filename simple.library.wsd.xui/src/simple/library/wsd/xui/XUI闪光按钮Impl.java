package simple.library.wsd.xui;

import android.view.View;

import com.xuexiang.xui.widget.button.shinebutton.ShineButton;

import simple.runtime.android.MainActivity;
import simple.runtime.components.组件容器;
import simple.runtime.events.EventDispatcher;

/**
 * XUI闪光按钮的原生视图及 Simple 事件适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI闪光按钮Impl extends XUI视图组件基类 implements XUI闪光按钮 {

	// 视图在父类构造期间创建；不得用字段初始化器重置此处状态。
	private ShineButton view;

	/**
	 * 创建XUI闪光按钮。
	 *
	 * @param container 容纳组件的 Simple 容器。
	 */
	public XUI闪光按钮Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected View createView() {
		view = new ShineButton(ComponentSupport.createContext());

		// 独立主题上下文不是 Activity，显式绑定现有 Simple 宿主的窗口用于点赞动画。
		view.initWindow(MainActivity.getContext());

		view.setOnCheckStateChangeListener(new ShineButton.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(ShineButton button, boolean checked) {
				选中改变(checked);
			}
		});

		return view;
	}

	@Override
	public int 取颜色() {
		return view.getColor();
	}

	@Override
	public void 切换() {
		view.toggle();
	}

	@Override
	public void 置正常颜色(int normalColor) {
		view.setNormalColor(normalColor);
	}

	@Override
	public void 置选中颜色(int checkedColor) {
		view.setCheckedColor(checkedColor);
	}

	@Override
	public void 置选中(boolean checked, boolean anim) {
		view.setChecked(checked, anim);
	}

	@Override
	public void 置取消() {
		view.setCancel();
	}

	@Override
	public void 置允许随机颜色(boolean allowRandomColor) {
		view.setAllowRandomColor(allowRandomColor);
	}

	@Override
	public void 置动画时长(int durationMs) {
		view.setAnimDuration(durationMs);
	}

	@Override
	public void 置大闪光颜色(int color) {
		view.setBigShineColor(color);
	}

	@Override
	public void 置单击动画时长(int durationMs) {
		view.setClickAnimDuration(durationMs);
	}

	@Override
	public void 启用闪烁(boolean enable) {
		view.enableFlashing(enable);
	}

	@Override
	public void 置闪光数量(int count) {
		view.setShineCount(count);
	}

	@Override
	public void 置闪光距离倍数(float multiple) {
		view.setShineDistanceMultiple(multiple);
	}

	@Override
	public void 置闪光旋转角度(float angle) {
		view.setShineTurnAngle(angle);
	}

	@Override
	public void 置小闪光颜色(int color) {
		view.setSmallShineColor(color);
	}

	@Override
	public void 置小闪光关角度(float angle) {
		view.setSmallShineOffAngle(angle);
	}

	@Override
	public void 置闪光尺寸(int size) {
		view.setShineSize(size);
	}

	@Override
	public void 置着色颜色(int color) {
		view.setTintColor(color);
	}

	@Override
	public boolean 取选中() {
		return view.isChecked();
	}

	@Override
	public void 置选中(boolean checked) {
		view.setChecked(checked);
	}

	@Override
	public void 置图片(String image) {
		view.setIconDrawable(ComponentSupport.getDrawable(image));
	}

	@Override
	public void 选中改变(boolean checked) {
		EventDispatcher.dispatchEvent(this, "选中改变", checked);
	}
}
