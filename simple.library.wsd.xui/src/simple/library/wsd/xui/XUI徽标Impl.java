package simple.library.wsd.xui;

import android.view.View;

import com.xuexiang.xui.widget.textview.badge.Badge;

import simple.runtime.android.MainActivity;
import simple.runtime.components.impl.android.视图组件;
import simple.runtime.components.impl.组件Impl;
import simple.runtime.components.可视组件;
import simple.runtime.components.组件容器;
import simple.runtime.errors.非法参数错误;
import simple.runtime.events.EventDispatcher;

/**
 * XUI徽标的原生视图及 Simple 事件适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI徽标Impl extends 组件Impl implements XUI徽标 {

	// 徽标在绑定目标后才挂载，不创建占位视图。
	private BadgeView view;

	// 显式销毁与宿主销毁共用一次性释放边界。
	private boolean destroyed;

	// 仅在组件存活期间保留宿主回调。
	private MainActivity.OnDestroyListener destroyListener;

	/**
	 * 创建XUI徽标。
	 *
	 * @param container 容纳组件的 Simple 容器。
	 */
	public XUI徽标Impl(组件容器 container) {
		super(container);
		view = new BadgeView(ComponentSupport.createContext());

		view.setOnDragStateChangedListener(new Badge.OnDragStateChangedListener() {

			@Override
			public void onDragStateChanged(int state, Badge badge, View target) {
				拖动状态改变(state);
			}
		});

		destroyListener = new MainActivity.OnDestroyListener() {
			@Override
			public void onDestroy() {
				销毁();
			}
		};
		MainActivity.getContext().addOnDestroyListener(destroyListener);
	}

	@Override
	public void 重置() {
		view.reset();
	}

	@Override
	public void 隐藏(boolean animate) {
		view.hide(animate);
	}

	@Override
	public void 置徽标数(int badgeNumber) {
		view.setBadgeNumber(badgeNumber);
	}

	@Override
	public int 取徽标数() {
		return view.getBadgeNumber();
	}

	@Override
	public void 置徽标文本(String badgeText) {
		view.setBadgeText(badgeText);
	}

	@Override
	public String 取徽标文本() {
		return view.getBadgeText();
	}

	@Override
	public void 置精确模式(boolean isExact) {
		view.setExactMode(isExact);
	}

	@Override
	public boolean 是否精确模式() {
		return view.isExactMode();
	}

	@Override
	public void 置显示阴影(boolean showShadow) {
		view.setShowShadow(showShadow);
	}

	@Override
	public boolean 是否显示阴影() {
		return view.isShowShadow();
	}

	@Override
	public void 置徽标背景颜色(int color) {
		view.setBadgeBackgroundColor(color);
	}

	@Override
	public int 取徽标背景颜色() {
		return view.getBadgeBackgroundColor();
	}

	@Override
	public void 置徽标文本颜色(int color) {
		view.setBadgeTextColor(color);
	}

	@Override
	public int 取徽标文本颜色() {
		return view.getBadgeTextColor();
	}

	@Override
	public void 置徽标文本尺寸(float size, boolean isSpValue) {
		view.setBadgeTextSize(size, isSpValue);
	}

	@Override
	public float 取徽标文本尺寸(boolean isSpValue) {
		return view.getBadgeTextSize(isSpValue);
	}

	@Override
	public void 置徽标内边距(float padding, boolean isDpValue) {
		view.setBadgePadding(padding, isDpValue);
	}

	@Override
	public float 取徽标内边距(boolean isDpValue) {
		return view.getBadgePadding(isDpValue);
	}

	@Override
	public boolean 是否可拖动() {
		return view.isDraggable();
	}

	@Override
	public void 置徽标对齐(int gravity) {
		view.setBadgeGravity(gravity);
	}

	@Override
	public int 取徽标对齐() {
		return view.getBadgeGravity();
	}

	@Override
	public void 置对齐偏移(float offset, boolean isDpValue) {
		view.setGravityOffset(offset, isDpValue);
	}

	@Override
	public void 置对齐偏移(float offsetX, float offsetY, boolean isDpValue) {
		view.setGravityOffset(offsetX, offsetY, isDpValue);
	}

	@Override
	public float 取对齐偏移横向(boolean isDpValue) {
		return view.getGravityOffsetX(isDpValue);
	}

	@Override
	public float 取对齐偏移纵向(boolean isDpValue) {
		return view.getGravityOffsetY(isDpValue);
	}

	@Override
	public void 绑定目标(可视组件 target) {
		View nativeTarget = ((视图组件) target).getView();
		if (nativeTarget == view || nativeTarget.getParent() == null) {
			throw new 非法参数错误();
		}
		view.bindTarget(nativeTarget);
	}

	@Override
	public void 置徽标背景(String image) {
		view.setBadgeBackground(ComponentSupport.getDrawable(image));
	}

	@Override
	public void 拖动状态改变(int state) {
		EventDispatcher.dispatchEvent(this, "拖动状态改变", state);
	}

	@Override
	public void 销毁() {
		if (destroyed) {
			return;
		}
		destroyed = true;
		MainActivity.getContext().removeOnDestroyListener(destroyListener);
		super.销毁();
		view.dispose();
	}

	@Override
	public void 置描边(int color, float width) {
		if (width < 0 || Float.isNaN(width) || Float.isInfinite(width)) {
			throw new 非法参数错误();
		}
		view.stroke(color, width, true);
	}

}
