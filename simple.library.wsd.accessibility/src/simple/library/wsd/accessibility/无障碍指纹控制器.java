package simple.library.wsd.accessibility;

import android.accessibilityservice.FingerprintGestureController;

import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.SimpleProperty;

/**
 * 查询指纹手势可用性，需要 API 26、USE_FINGERPRINT 权限及支持手势的硬件。改变事件由无障碍服务组件报告。
 * 控制器仅在取得它的服务连接存续期间有效，必须在主线程使用。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class 无障碍指纹控制器 {

	/**
	 * 取得控制器时的服务连接，用于拒绝旧连接上的操作。
	 */
	private final AccessibilityListener owner;

	/**
	 * 绑定当前连接的系统服务。
	 *
	 * @param owner 已连接的服务
	 */
	无障碍指纹控制器(AccessibilityListener owner) {
		this.owner = owner;
	}

	/**
	 * 拒绝断线、错误线程或版本不支持的控制器。
	 *
	 * @return 有效的原生控制器
	 */
	private FingerprintGestureController require() {
		AccessibilitySupport.mainThread();
		AccessibilitySupport.api(26, "无障碍指纹控制器");
		if (AccessibilityListener.current() != owner) {
			throw new IllegalStateException("控制器所属服务已断开，请重新取得控制器");
		}

		return owner.getFingerprintGestureController();
	}

	/* 属性 */

	/**
	 * 系统当前是否允许使用该控制器的能力。
	 *
	 * @return 系统当前是否允许使用该控制器的能力。
	 */
	@SimpleProperty
	public boolean 可用() {
		return require().isGestureDetectionAvailable();
	}
}
