package simple.library.wsd.xui;

import android.content.Context;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.xuexiang.xui.widget.slideback.widget.SlideBackIconView;

import simple.runtime.android.MainActivity;
import simple.runtime.components.impl.android.视图组件容器;
import simple.runtime.components.布局;
import simple.runtime.components.组件;
import simple.runtime.components.组件容器;
import simple.runtime.errors.非法参数错误;

/**
 * 侧滑返回容器的 XUI 实现与 Simple 生命周期适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI侧滑返回容器Impl extends XUI视图组件基类 implements XUI侧滑返回容器, 视图组件容器 {

	/**
	 * 创建XUI侧滑返回容器。
	 *
	 * @param container 所属 Simple 窗口。
	 */
	public XUI侧滑返回容器Impl(组件容器 container) {
		super(container);
	}

	// 子组件与返回图标分层，避免图标进入 Simple 子组件布局。
	private SlideBackLayout view;

	@Override
	protected View createView() {
		view = new SlideBackLayout(ComponentSupport.createContext());
		return view;
	}

	@Override
	public ViewGroup getLayoutManager() {
		return view.content;
	}

	@Override
	public void addComponent(组件 component) {
		ComponentSupport.addChildComponent(view.content, component);
	}

	@Override
	public 布局 getLayout() {
		return null;
	}

	@Override
	public void 置响应边缘(int edge) {
		ComponentSupport.validateIndex(edge, 3);
		view.edge = edge;
	}

	@Override
	public void 置触发距离(int distance) {
		if (distance <= 0) {
			throw new 非法参数错误();
		}
		view.threshold = distance * view.getResources().getDisplayMetrics().density;
	}

	/**
	 * 仅拦截本容器边缘开始的横向手势，不包装 Activity 根布局或替换其触摸监听。
	 */
	private static final class SlideBackLayout extends FrameLayout {
		private final FrameLayout content;
		private final SlideBackIconView icon;
		private final float density;
		private final int slop;
		private float downX;
		private float downY;
		private float threshold;
		private int edge;
		private int direction;
		private boolean tracking;

		/**
		 * 创建内容和装饰层，按实际屏幕密度确定边缘与绘制尺寸。
		 *
		 * @param context XUI 主题上下文。
		 */
		SlideBackLayout(Context context) {
			super(context);
			density = getResources().getDisplayMetrics().density;
			slop = ViewConfiguration.get(context).getScaledTouchSlop();
			threshold = 90 * density;
			content = new FrameLayout(context);
			addView(content, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
			icon = new SlideBackIconView(context);
			icon.setBackViewHeight(72 * density);
			icon.setArrowSize(6 * density);
			icon.setMaxSlideLength(40 * density);
			icon.setVisibility(INVISIBLE);
			addView(icon, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, Math.round(72 * density)));
		}

		@Override
		public boolean onInterceptTouchEvent(MotionEvent event) {
			if (!isEnabled()) {
				return false;
			}
			if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
				downX = event.getX();
				downY = event.getY();
				direction = downX <= 24 * density && edge != 1 ? 1
					: downX >= getWidth() - 24 * density && edge != 0 ? -1 : 0;
				tracking = false;
			} else if (event.getActionMasked() == MotionEvent.ACTION_MOVE && direction != 0) {
				float dx = (event.getX() - downX) * direction;
				float dy = Math.abs(event.getY() - downY);
				if (dy > slop && dy > dx) {
					direction = 0;
				} else if (dx > slop && dx > dy) {
					tracking = true;
					getParent().requestDisallowInterceptTouchEvent(true);
					return true;
				}
			}
			return tracking;
		}

		@Override
		public boolean onTouchEvent(MotionEvent event) {
			// 空白区域由容器自己接收触摸，不会再次经过子视图拦截分支，同样需要识别方向。
			if (!tracking && direction != 0 && event.getActionMasked() == MotionEvent.ACTION_MOVE) {
				float dx = (event.getX() - downX) * direction;
				float dy = Math.abs(event.getY() - downY);
				if (dy > slop && dy > dx) {
					direction = 0;
				} else if (dx > slop && dx > dy) {
					tracking = true;
					getParent().requestDisallowInterceptTouchEvent(true);
				}
			}
			if (!tracking) {
				return super.onTouchEvent(event);
			}
			float distance = Math.max(0, (event.getX() - downX) * direction);
			if (event.getActionMasked() == MotionEvent.ACTION_MOVE) {
				icon.setVisibility(VISIBLE);
				icon.setRotationY(direction < 0 ? 180 : 0);
				icon.setTranslationY(Math.max(0, Math.min(getHeight() - icon.getHeight(), event.getY() - icon.getHeight() / 2)));
				icon.updateSlideLength(Math.min(40 * density, distance * 40 * density / threshold));
			} else if (event.getActionMasked() == MotionEvent.ACTION_UP || event.getActionMasked() == MotionEvent.ACTION_CANCEL) {
				boolean back = event.getActionMasked() == MotionEvent.ACTION_UP && distance >= threshold;
				tracking = false;
				direction = 0;
				icon.updateSlideLength(0);
				icon.setVisibility(INVISIBLE);
				if (back) {
					// 与按实体返回键走同一入口，取消、切窗和退出仍由 Simple 窗口决定。
					MainActivity.getContext().onKeyDown(KeyEvent.KEYCODE_BACK,
						new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
				}
			}
			return true;
		}
	}

}
