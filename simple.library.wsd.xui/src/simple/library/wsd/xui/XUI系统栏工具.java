package simple.library.wsd.xui;

import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowInsetsController;

import com.xuexiang.xui.utils.StatusBarUtils;

import simple.runtime.android.MainActivity;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * 查询系统栏尺寸并设置当前 Activity 的状态栏图标明暗。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class XUI系统栏工具 {

	/**
	 * 静态工具不创建实例。
	 */
	private XUI系统栏工具() {
	}

	/**
	 * 取得状态栏高度。
	 *
	 * @return 物理像素高度。
	 */
	@SimpleFunction
	public static int 取状态栏高度() {
		return StatusBarUtils.getStatusBarHeight(MainActivity.getContext());
	}

	/**
	 * 取得导航栏高度。
	 *
	 * @return 物理像素高度。
	 */
	@SimpleFunction
	public static int 取导航栏高度() {
		return StatusBarUtils.getNavigationBarHeight(MainActivity.getContext());
	}

	/**
	 * 查询当前 Activity 是否处于全屏模式。
	 *
	 * @return 是否为全屏。
	 */
	@SimpleFunction
	public static boolean 是否全屏() {
		return StatusBarUtils.isFullScreen(MainActivity.getContext());
	}

	/**
	 * 设置当前 Activity 状态栏使用深色或浅色图标；效果持续到下一次系统栏设置。
	 *
	 * @param dark 真为深色图标，假为浅色图标。
	 */
	@SimpleFunction
	public static void 置状态栏暗色图标(boolean dark) {
		Window window = MainActivity.getContext().getWindow();
		if (Build.VERSION.SDK_INT >= 30) {
			WindowInsetsController controller = window.getInsetsController();
			if (controller == null) {
				throw new ComponentStateException("当前窗口尚未显示。");
			}
			controller.setSystemBarsAppearance(dark ? WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS : 0,
				WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS);
		} else {
			setLegacyStatusBarIcons(window, dark);
		}
	}

	/**
	 * API 24～29 使用系统 UI 标记，仅替换状态栏图标位，保留调用者设置的其它位。
	 *
	 * @param window 当前 Activity 的窗口。
	 * @param dark 是否使用深色图标。
	 */
	@SuppressWarnings("deprecation")
	private static void setLegacyStatusBarIcons(Window window, boolean dark) {
		View decor = window.getDecorView();
		int flags = decor.getSystemUiVisibility();
		decor.setSystemUiVisibility(dark ? flags | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
			: flags & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
	}
}
