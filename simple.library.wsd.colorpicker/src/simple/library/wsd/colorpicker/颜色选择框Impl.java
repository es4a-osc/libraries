package simple.library.wsd.colorpicker;

import java.util.Locale;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Looper;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import simple.runtime.android.MainActivity;
import simple.runtime.components.组件容器;
import simple.runtime.components.impl.组件Impl;
import simple.runtime.events.EventDispatcher;

/**
 * 将选色视图包装为非阻塞对话框，按每次显示的会话隔离确认和取消回调。
 *
 * <p>结束会话时先关闭对话框并更新属性，再派发事件，允许事件处理中再次显示或销毁。
 * 宿主销毁时只释放视图，不派发交互结果。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class 颜色选择框Impl extends 组件Impl implements 颜色选择框 {

	/* 宿主与属性；初始颜色用于下次显示，选中颜色只在确认时更新。 */

	private final MainActivity activity;
	private String title = "选择颜色";
	private int initialColor = 0xFFFF0000;
	private int selectedColor = 0xFFFF0000;

	/* 当前对话框及销毁状态，由界面线程串行处理。 */

	private Session session;
	private boolean destroyed;

	private final MainActivity.OnDestroyListener destroyListener = new MainActivity.OnDestroyListener() {
		@Override
		public void onDestroy() {
			// Activity 正在遍历监听列表，不能在回调内移除当前监听器。
			dispose(false);
		}
	};

	/**
	 * 建立颜色选择框并监听宿主销毁。
	 *
	 * @param container 容纳非可视组件的窗口。
	 */
	public 颜色选择框Impl(组件容器 container) {
		super(container);

		activity = MainActivity.getContext();
		activity.addOnDestroyListener(destroyListener);
	}

	/* 组件事件 */

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void 颜色选择完毕(int color) {
		EventDispatcher.dispatchEvent(this, "颜色选择完毕", color);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void 取消选择() {
		EventDispatcher.dispatchEvent(this, "取消选择");
	}

	/* 组件属性 */

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String 标题() {
		return title;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void 标题(String value) {
		check();
		if (value == null) {
			throw new IllegalArgumentException("标题不能是空引用。");
		}

		title = value;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int 初始颜色() {
		return initialColor;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void 初始颜色(int color) {
		check();
		initialColor = color;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int 选中颜色() {
		return selectedColor;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean 正在显示() {
		return session != null && session.dialog.isShowing();
	}

	/* 组件方法 */

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void 显示() {
		check();
		end(session, false, false);

		// 新对话框独立保存草稿，替换旧框不提交结果。
		try {
			Session next = new Session(title, initialColor);
			session = next;
			next.dialog.show();
		} catch (RuntimeException error) {
			end(session, false, false);
			throw new IllegalStateException("无法显示颜色选择框。", error);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void 关闭() {
		check();
		end(session, false, true);
	}

	/**
	 * 释放对话框并注销事件，不派发确认或取消事件；可以重复调用。
	 *
	 * @throws IllegalStateException 未销毁的组件在非界面线程调用。
	 */
	@Override
	public void 销毁() {
		if (destroyed) {
			return;
		}

		check();
		dispose(true);
	}

	/* 生命周期和会话边界 */

	/**
	 * 检查修改组件状态的调用是否有效。
	 *
	 * @throws IllegalStateException 不在界面线程调用或组件已销毁。
	 */
	private void check() {
		if (Looper.myLooper() != Looper.getMainLooper()) {
			throw new IllegalStateException("颜色选择框只能在界面线程调用。");
		}

		if (destroyed) {
			throw new IllegalStateException("颜色选择框已经销毁。");
		}
	}

	/**
	 * 结束指定会话；先解除当前引用，再关闭界面并派发结果。
	 * 重复结束和旧会话回调不会影响新打开的对话框。
	 *
	 * @param current 待结束的会话；空引用时不处理。
	 * @param confirm 是否提交草稿颜色。
	 * @param notify 是否向当前有效组件派发确认或取消事件。
	 */
	private void end(Session current, boolean confirm, boolean notify) {
		if (current == null || current.ended) {
			return;
		}

		// dismiss 会同步触发关闭监听，先标记结束以避免重复派发。
		current.ended = true;
		boolean active = session == current;
		if (active) {
			session = null;
		}

		current.dialog.dismiss();
		if (!active || destroyed || !notify) {
			return;
		}

		// 对外状态先完成提交，用户事件中的再次显示不会被旧会话清理。
		if (confirm) {
			selectedColor = current.picker.getColor();
			颜色选择完毕(selectedColor);
		} else {
			取消选择();
		}
	}

	/**
	 * 释放对话框和事件绑定，不产生用户交互结果。
	 *
	 * @param removeListener 主动销毁时注销宿主监听；宿主销毁回调中保留监听列表。
	 */
	private void dispose(boolean removeListener) {
		destroyed = true;
		end(session, false, false);

		if (removeListener) {
			activity.removeOnDestroyListener(destroyListener);
		}

		super.销毁();
	}

	/**
	 * 单次显示的对话框及草稿；拖动不修改组件的颜色属性。
	 */
	private final class Session {

		private final AlertDialog dialog;
		private final ColorPickerView picker;
		private boolean ended;

		/**
		 * 创建内容并绑定本次会话的确认、取消和关闭回调。
		 *
		 * @param caption 本次标题，空文本不显示标题。
		 * @param color 本次草稿的初始 ARGB 颜色。
		 */
		Session(String caption, int color) {
			LinearLayout content = new LinearLayout(activity);
			content.setOrientation(LinearLayout.VERTICAL);
			content.setPadding(dp(16), dp(8), dp(16), dp(8));

			// 数值随草稿实时变化，确认前不写回选中颜色。
			final TextView value = new TextView(activity);
			value.setGravity(Gravity.CENTER);
			value.setTextSize(16);
			value.setTypeface(Typeface.MONOSPACE);
			value.setPadding(0, dp(8), 0, dp(4));
			value.setText(hex(color));

			picker = new ColorPickerView(activity, color, new ColorPickerView.Listener() {
				@Override
				public void onColorChanged(int draft) {
					value.setText(hex(draft));
				}
			});
			content.addView(picker, new LinearLayout.LayoutParams(-1, -2));
			content.addView(value, new LinearLayout.LayoutParams(-1, -2));

			TextView hint = new TextView(activity);
			hint.setGravity(Gravity.CENTER);
			hint.setTextSize(12);
			hint.setText("色环选色 · 渐变条调节明暗");
			content.addView(hint, new LinearLayout.LayoutParams(-1, -2));

			// 矮窗口可滚动查看完整的选色区域和底部提示。
			ScrollView scroll = new ScrollView(activity);
			scroll.setFillViewport(false);
			scroll.addView(content, new ScrollView.LayoutParams(-1, -2));

			// 统一结束入口，确保每次显示只通知一次结果。
			AlertDialog.Builder builder = new AlertDialog.Builder(activity);
			if (caption.length() > 0) {
				builder.setTitle(caption);
			}

			builder.setView(scroll);
			builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface clicked, int which) {
					end(Session.this, true, true);
				}
			});

			builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface clicked, int which) {
					end(Session.this, false, true);
				}
			});

			dialog = builder.create();
			dialog.setCanceledOnTouchOutside(true);
			dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(DialogInterface cancelled) {
					end(Session.this, false, true);
				}
			});
			dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dismissed) {
					end(Session.this, false, true);
				}
			});
		}
	}

	/**
	 * 按宿主显示密度换算对话框尺寸。
	 *
	 * @param value DIP 尺寸。
	 * @return 四舍五入后的像素尺寸。
	 */
	private int dp(int value) {
		return (int) (value * activity.getResources().getDisplayMetrics().density + 0.5f);
	}

	/**
	 * 生成保留透明度的十六进制颜色文本。
	 *
	 * @param color ARGB 颜色。
	 * @return 固定为 #AARRGGBB 的大写文本。
	 */
	private static String hex(int color) {
		return String.format(Locale.ROOT, "#%08X", color);
	}
}
