package simple.library.wsd.xui;

import android.view.ViewGroup;

import android.util.TypedValue;
import android.view.View;

import com.xuexiang.xui.widget.popupwindow.ViewTooltip;

import simple.runtime.components.可视组件;
import simple.runtime.components.组件容器;
import simple.runtime.errors.非法参数错误;

/**
 * 管理气泡样式、借用内容与单次显示状态，关闭后恢复原 Simple 布局。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI气泡Impl extends XUI弹层基类 implements XUI气泡 {

	private boolean clickToHide = true;

	private Session session;
	private int background = 0xFF333333;
	private int textColor = 0xFFFFFFFF;
	private float textSize = 14;
	private int corner = 8;
	private int alignment = 1;
	private int[] padding = {12, 8, 12, 8};

	/**
	 * 将延迟的显示和关闭回调限制在所属会话中。
	 */
	private static final class Session {
		ViewTooltip tooltip;
		ViewTooltip.TooltipView view;
		BorrowedContent content;
		boolean closed;
	}

	/**
	 * 创建气泡组件。
	 *
	 * @param container 所属窗口。
	 */
	public XUI气泡Impl(组件容器 container) {
		super(container);
	}

	@Override
	public void 显示(可视组件 anchor, String text, int position, long duration) {
		showTip(requireAnchor(anchor), text, null, position, duration);
	}

	/**
	 * 构建独立气泡；外部回调重新打开的气泡不被旧会话覆盖。
	 *
	 * @param target 锚点。
	 * @param text 文本内容。
	 * @param content 自定义内容，空表示文本气泡。
	 * @param position 方向常量。
	 * @param duration 自动关闭毫秒数，0 不自动关闭。
	 */
	private void showTip(View target, String text, View content, int position, long duration) {
		ComponentSupport.validateIndex(position, 4);
		if (duration < 0 || text == null) {
			throw new 非法参数错误();
		}
		关闭();
		if (session != null) {
			throw new ComponentStateException("关闭事件已打开新的气泡。");
		}
		final Session current = new Session();
		ViewTooltip.Position[] positions = {ViewTooltip.Position.TOP, ViewTooltip.Position.BOTTOM,
			ViewTooltip.Position.LEFT, ViewTooltip.Position.RIGHT};
		current.tooltip = ViewTooltip.on(target).text(text).textTypeFace(ThemeSupport.loadFont(ThemeSupport.getFont()))
			.position(positions[position])
			.clickToHide(clickToHide).autoHide(duration > 0, duration).color(background).textColor(textColor)
			.textSize(TypedValue.COMPLEX_UNIT_SP, textSize).corner(corner)
			.padding(padding[0], padding[1], padding[2], padding[3])
			.align(alignment == 0 ? ViewTooltip.ALIGN.START : ViewTooltip.ALIGN.CENTER)
			.onDisplay(new ViewTooltip.ListenerDisplay() {
				@Override
				public void onDisplay(View shown) {
					if (!current.closed && session == current) {
						ThemeSupport.applyFont(shown);
						已显示();
					}
				}
			}).onHide(new ViewTooltip.ListenerHide() {
				@Override
				public void onHide(View hidden) {
					finish(current);
				}
			});
		try {
			if (content != null) {
				current.content = new BorrowedContent(content, requireVisibleOwner());
				current.tooltip.customView(content);
			}
			session = current;
			current.view = current.tooltip.show();
			// 上游 show 延迟 100ms 才附着；关闭不能阻止其私有任务，须拦截迟到的附着。
			current.view.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
				@Override
				public void onViewAttachedToWindow(final View attached) {
					if (current.closed) {
						attached.setVisibility(View.GONE);
						// 等上游一次性预绘制与动画完成后移出，既不闪现也不遗留全局预绘制监听。
						attached.postDelayed(new Runnable() {
							@Override
							public void run() {
								attached.animate().cancel();
								if (attached.getParent() instanceof ViewGroup) {
									((ViewGroup) attached.getParent()).removeView(attached);
								}
							}
						}, 500);
					}
				}

				@Override
				public void onViewDetachedFromWindow(View detached) {
					detached.removeOnAttachStateChangeListener(this);
				}
			});
		} catch (RuntimeException failure) {
			if (session == current) {
				session = null;
			}
			current.closed = true;
			if (current.content != null) {
				current.content.restore();
			}
			throw failure;
		}
	}

	/**
	 * 先恢复借用内容再发送关闭事件，每次会话仅通知一次。
	 *
	 * @param current 结束的会话。
	 */
	private void finish(Session current) {
		if (current.closed) {
			return;
		}
		current.closed = true;
		if (session == current) {
			session = null;
		}
		if (current.content != null) {
			current.content.restore();
			current.content = null;
		}
		已关闭();
	}

	@Override
	public boolean 是否显示() {
		return session != null && !session.closed;
	}

	@Override
	public void 关闭() {
		Session current = session;
		if (current != null) {
			if (!current.view.isAttachedToWindow()) {
				current.view.setVisibility(View.GONE);
			}
			current.tooltip.close();
			finish(current);
		}
	}

	@Override
	public void 置颜色(int color, int text) {
		background = color;
		textColor = text;
	}

	@Override
	public void 置文字大小(float size) {
		if (!Float.isFinite(size) || size <= 0) {
			throw new 非法参数错误();
		}
		textSize = size;
	}

	@Override
	public void 置内边距(int left, int top, int right, int bottom) {
		if (left < 0 || top < 0 || right < 0 || bottom < 0) {
			throw new 非法参数错误();
		}
		padding = new int[] {left, top, right, bottom};
	}

	@Override
	public void 置圆角(int radius) {
		if (radius < 0) {
			throw new 非法参数错误();
		}
		corner = radius;
	}

	@Override
	public void 置对齐(int align) {
		if (align != 0 && align != 1) {
			throw new 非法参数错误();
		}
		alignment = align;
	}

	@Override
	public void 显示自定义(可视组件 anchor, 可视组件 content, int position, long duration) {
		View target = requireAnchor(anchor);
		View child = ComponentOwnership.requireSibling(target, content);
		showTip(target, "", child, position, duration);
	}

	@Override
	public void 已显示() {
		dispatchEvent("已显示");
	}

	@Override
	public void 已关闭() {
		dispatchEvent("已关闭");
	}

	@Override
	public void 置点击隐藏(boolean hide) {
		clickToHide = hide;
	}

}
