package simple.library.wsd.agentweb;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.webkit.WebChromeClient;
import android.widget.FrameLayout;

import androidx.annotation.RequiresApi;

/**
 * 将网页视频覆盖到现有 Activity 内容帧，退出时恢复本次改变的系统栏状态。
 * 不强制旋转宿主，不创建第二个 Simple 窗口。
 *
 * @author 树先生 xhwsd@qq.com
 */
final class BrowserFullscreen {

	private final Activity activity;
	private FrameLayout overlay;
	private WebChromeClient.CustomViewCallback callback;
	private int legacyFlags;
	private int visibleBars;
	private int behavior;

	/**
	 * 绑定视频全屏操作的宿主。
	 *
	 * @param activity 当前 Simple Activity。
	 */
	BrowserFullscreen(Activity activity) {
		this.activity = activity;
	}

	/**
	 * 显示视频全屏视图；已有全屏时拒绝替换并完成新请求的回调。
	 *
	 * @param view WebView 提供的视频视图。
	 * @param result 退出视频全屏的回调。
	 * @return 是否创建了新的全屏层。
	 */
	boolean enter(View view, WebChromeClient.CustomViewCallback result) {
		if (overlay != null || view.getParent() != null) {
			result.onCustomViewHidden();
			return false;
		}
		FrameLayout layer = new FrameLayout(activity);
		layer.setBackgroundColor(Color.BLACK);
		layer.setKeepScreenOn(true);
		layer.addView(view, new FrameLayout.LayoutParams(-1, -1));
		ViewGroup content = activity.findViewById(android.R.id.content);
		content.addView(layer, new ViewGroup.LayoutParams(-1, -1));
		overlay = layer;
		callback = result;

		// 新旧系统使用各自的系统栏控制方式，记录原值供本次退出恢复。
		if (Build.VERSION.SDK_INT >= 30) {
			hideBars();
		} else {
			hideLegacy();
		}
		return true;
	}

	/**
	 * 退出视频全屏，先清理状态再通知 WebView，允许回调重入。
	 *
	 * @return 是否移除了全屏层。
	 */
	boolean exit() {
		if (overlay == null) {
			return false;
		}
		FrameLayout layer = overlay;
		WebChromeClient.CustomViewCallback result = callback;
		overlay = null;
		callback = null;
		if (layer.getParent() instanceof ViewGroup) {
			((ViewGroup) layer.getParent()).removeView(layer);
		}
		if (Build.VERSION.SDK_INT >= 30) {
			restoreBars();
		} else {
			restoreLegacy();
		}
		result.onCustomViewHidden();
		return true;
	}

	/**
	 * 查询视频全屏覆盖层是否存在。
	 *
	 * @return 是否正在全屏。
	 */
	boolean isShowing() {
		return overlay != null;
	}

	/**
	 * API 30 起按系统栏可见性保存并隐藏状态栏与导航栏。
	 */
	@RequiresApi(30)
	private void hideBars() {
		View decor = activity.getWindow().getDecorView();
		WindowInsets insets = decor.getRootWindowInsets();
		WindowInsetsController controller = activity.getWindow().getInsetsController();
		visibleBars = 0;
		if (insets != null) {
			if (insets.isVisible(WindowInsets.Type.statusBars())) {
				visibleBars |= WindowInsets.Type.statusBars();
			}
			if (insets.isVisible(WindowInsets.Type.navigationBars())) {
				visibleBars |= WindowInsets.Type.navigationBars();
			}
		}
		if (controller != null) {
			behavior = controller.getSystemBarsBehavior();
			controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
			controller.hide(WindowInsets.Type.systemBars());
		}
	}

	/**
	 * API 30 起只恢复进入全屏之前可见的系统栏。
	 */
	@RequiresApi(30)
	private void restoreBars() {
		WindowInsetsController controller = activity.getWindow().getInsetsController();
		if (controller != null) {
			controller.setSystemBarsBehavior(behavior);
			controller.show(visibleBars);
		}
	}

	/**
	 * API 24 至 29 使用传统沉浸标记隐藏系统栏。
	 */
	@SuppressWarnings("deprecation")
	private void hideLegacy() {
		View decor = activity.getWindow().getDecorView();
		legacyFlags = decor.getSystemUiVisibility();
		decor.setSystemUiVisibility(legacyFlags | View.SYSTEM_UI_FLAG_FULLSCREEN
				| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
	}

	/**
	 * API 24 至 29 恢复进入前保存的系统栏标记。
	 */
	@SuppressWarnings("deprecation")
	private void restoreLegacy() {
		activity.getWindow().getDecorView().setSystemUiVisibility(legacyFlags);
	}
}
