package com.xuexiang.xui.widget.popupwindow.bar;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.xuexiang.xui.R;

/**
 * 为 XUI 1.2.1 的 Cookie 提供独立主题上下文和可取消的显示周期。
 * 同包访问只用于构造上游包内参数，不修改依赖包或 Simple Activity 的主题。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class Es4aCookie extends FrameLayout {

	private final Handler handler = new Handler(Looper.getMainLooper());
	private final long duration;
	private final Cookie cookie;
	private final Runnable dismissTask = new Runnable() {
		@Override
		public void run() {
			dismiss();
		}
	};

	/**
	 * 创建消息条；自动关闭任务由此适配层拥有，离开视图树后立即取消。
	 *
	 * @param context XUI 主题上下文。
	 * @param title 标题。
	 * @param message 正文。
	 * @param action 操作文本，可为空。
	 * @param duration 显示毫秒数。
	 * @param bottom 是否位于底部。
	 * @param listener 操作回调。
	 */
	public Es4aCookie(Context context, String title, String message, String action,
		long duration, boolean bottom, View.OnClickListener listener) {
		super(context);
		this.duration = duration;
		CookieBar.Params parameters = new CookieBar.Params();
		parameters.title = title;
		parameters.message = message;
		parameters.action = action;
		parameters.onActionClickListener = new View.OnClickListener() {
			@Override
			public void onClick(View source) {
				listener.onClick(source);
				dismiss();
			}
		};
		parameters.layoutGravity = bottom ? Gravity.BOTTOM : Gravity.TOP;
		// 禁用上游无法单独注销的匿名延迟任务。
		parameters.duration = -1;
		cookie = new Cookie(context);
		cookie.setParams(parameters);
		addView(cookie, new FrameLayout.LayoutParams(
			ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
	}

	/**
	 * 将 ARGB 颜色直接应用到已构建内容；上游 Params 的颜色字段只接受资源编号。
	 *
	 * @param colors 背景、标题、正文、操作颜色；空值保留主题。
	 * @param icon 图标；空值隐藏。
	 */
	public void applyStyle(int[] colors, Drawable icon) {
		if (colors != null) {
			cookie.findViewById(R.id.cookie).setBackgroundColor(colors[0]);
			((TextView) cookie.findViewById(R.id.tv_title)).setTextColor(colors[1]);
			((TextView) cookie.findViewById(R.id.tv_message)).setTextColor(colors[2]);
			((TextView) cookie.findViewById(R.id.btn_action)).setTextColor(colors[3]);
		}
		ImageView image = cookie.findViewById(R.id.iv_icon);
		image.setBackground(icon);
		image.setVisibility(icon == null ? GONE : VISIBLE);
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		handler.postDelayed(dismissTask, duration);
	}

	@Override
	protected void onDetachedFromWindow() {
		handler.removeCallbacks(dismissTask);
		cookie.clearAnimation();
		clearAnimation();
		super.onDetachedFromWindow();
	}

	/**
	 * 立即移除消息条，并取消其显示周期。
	 */
	public void dismiss() {
		handler.removeCallbacks(dismissTask);
		cookie.clearAnimation();
		clearAnimation();
		if (getParent() instanceof ViewGroup) {
			((ViewGroup) getParent()).removeView(this);
		}
	}
}
