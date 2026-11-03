package simple.library.wsd.accessibility;

import android.accessibilityservice.AccessibilityService;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.SimpleProperty;

/**
 * 查询及修改软键盘显示策略。改变事件由无障碍服务组件报告。控制器仅在取得它的服务连接存续期间有效，必须在主线程使用。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class 无障碍软键盘控制器 {

	/**
	 * 取得控制器时的服务连接，用于拒绝旧连接上的操作。
	 */
	private final AccessibilityListener owner;

	/**
	 * 绑定当前连接的系统服务。
	 *
	 * @param owner 已连接的服务
	 */
	无障碍软键盘控制器(AccessibilityListener owner) {
		this.owner = owner;
	}

	/**
	 * 拒绝断线、错误线程或版本不支持的控制器。
	 *
	 * @return 有效的原生控制器
	 */
	private AccessibilityService.SoftKeyboardController require() {
		AccessibilitySupport.mainThread();
		AccessibilitySupport.api(24, "无障碍软键盘控制器");
		if (AccessibilityListener.current() != owner) {
			throw new IllegalStateException("控制器所属服务已断开，请重新取得控制器");
		}

		return owner.getSoftKeyboardController();
	}

	/* 属性 */

	/**
	 * 当前软键盘显示策略，见无障碍常量。
	 *
	 * @return 当前软键盘显示策略，见无障碍常量。
	 */
	@SimpleProperty
	public int 显示模式() {
		return require().getShowMode();
	}

	/* 方法 */

	/**
	 * 请求切换软键盘显示策略，不表示输入法窗口已显示或隐藏。忽略硬键盘模式需要 API 29。
	 *
	 * @param mode 自动、隐藏或忽略硬键盘模式
	 * @return 系统接受修改时为真
	 */
	@SimpleFunction
	public boolean 设置显示模式(int mode) {
		if (mode == 2) {
			AccessibilitySupport.api(29, "忽略硬键盘模式");
		}
		if (mode < 0 || mode > 2) {
			throw new IllegalArgumentException("无效的软键盘模式");
		}

		return require().setShowMode(mode);
	}
}
