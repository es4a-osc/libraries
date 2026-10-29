package simple.library.wsd.xui;

import java.util.IdentityHashMap;
import java.util.Map;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.xuexiang.xui.widget.StickyNestedScrollView;

import simple.runtime.components.impl.android.视图组件;
import simple.runtime.components.impl.android.视图组件容器;
import simple.runtime.components.可视组件;
import simple.runtime.components.布局;
import simple.runtime.components.组件;
import simple.runtime.components.组件容器;
import simple.runtime.errors.非法参数错误;
import simple.runtime.events.EventDispatcher;

/**
 * XUI吸顶滚动框的原生视图及 Simple 事件适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI吸顶滚动框Impl extends XUI滚动容器基类 implements XUI吸顶滚动框, 视图组件容器 {

	// 视图在父类构造期间创建；不得用字段初始化器重置此处状态。
	private StickyNestedScrollView view;

	// 只为显式注册的 Simple 组件转换原生吸顶回调。
	private Map<View, 可视组件> stickyComponents;

	/**
	 * 创建XUI吸顶滚动框。
	 *
	 * @param container 容纳组件的 Simple 容器。
	 */
	public XUI吸顶滚动框Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected View createView() {
		view = new StickyNestedScrollView(ComponentSupport.createContext());

		stickyComponents = new IdentityHashMap<View, 可视组件>();

		view.addOnViewStickyListener(new StickyNestedScrollView.OnViewStickyListener() {

			@Override
			public void onSticky(View target) {
				可视组件 component = stickyComponents.get(target);
				if (component != null) {
					吸顶改变(component, true);
				}
			}

			@Override
			public void onUnSticky(View target) {
				可视组件 component = stickyComponents.get(target);
				if (component != null) {
					吸顶改变(component, false);
				}
			}
		});

		return view;
	}

	@Override
	public ViewGroup getLayoutManager() {
		return ComponentSupport.getChildLayout((ViewGroup) view);
	}

	@Override
	public void addComponent(组件 component) {
		ComponentSupport.addChildComponent((ViewGroup) view, component);
	}

	@Override
	public 布局 getLayout() {
		return null;
	}

	@Override
	public void 置阴影高度(int height) {
		view.setShadowHeight(height);
	}

	@Override
	public void 刷新吸顶属性() {
		view.notifyStickyAttributeChanged();
	}

	@Override
	public void 置阴影图像(String image) {
		view.setShadowDrawable(ComponentSupport.getDrawable(image));
	}

	@Override
	public void 标记吸顶(可视组件 component, boolean enabled) {
		View target = ((视图组件) component).getView();
		boolean inside = false;
		for (ViewParent parent = target.getParent(); parent instanceof View; parent = parent.getParent()) {
			if (parent == view) {
				inside = true;
				break;
			}
		}
		if (!inside || (target.getTag() != null && !(target.getTag() instanceof String))) {
			throw new 非法参数错误();
		}
		String tag = target.getTag() == null ? "" : (String) target.getTag();
		if (enabled) {
			if (!tag.contains(StickyNestedScrollView.STICKY_TAG)) {
				target.setTag(tag + " " + StickyNestedScrollView.STICKY_TAG);
			}
			stickyComponents.put(target, component);
		} else {
			target.setTag(tag.replace(StickyNestedScrollView.STICKY_TAG, ""));
			stickyComponents.remove(target);
		}
		view.setup();
	}

	@Override
	public void 吸顶改变(可视组件 component, boolean sticky) {
		EventDispatcher.dispatchEvent(this, "吸顶改变", component, sticky);
	}
}
