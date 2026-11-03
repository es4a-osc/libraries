package simple.library.wsd.accessibility;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import android.accessibilityservice.AccessibilityButtonController;
import android.accessibilityservice.AccessibilityGestureEvent;
import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.FingerprintGestureController;
import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Region;
import android.os.Build;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;

/**
 * 由系统绑定的实际无障碍服务。仅存储连接，不缓存窗口或节点。
 * 控制器监听随连接注册和撤销，窗口组件通过订阅接收主线程事件。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class AccessibilityListener extends AccessibilityService {

	/* 内部状态 */

	/**
	 * 已订阅的窗口组件；回调期间允许对称注销。
	 */
	private static final Set<Listener> LISTENERS = new CopyOnWriteArraySet<Listener>();

	/**
	 * 当前已连接的系统服务，未连接时为空。
	 */
	private static volatile AccessibilityListener active;

	/**
	 * 本次连接注册的 API 26 控制器回调。
	 */
	private Api26Callbacks callbacks26;

	/**
	 * 本连接的放大状态监听。
	 */
	private final MagnificationController.OnMagnificationChangedListener magnification =
		new MagnificationController.OnMagnificationChangedListener() {
			/**
			 * 把放大区域复制为边界快照后通知组件。
			 *
			 * @param controller 触发回调的放大控制器
			 * @param region 本次系统区域，不接管或保存
			 * @param scale 当前倍率
			 * @param centerX 未缩放屏幕像素横坐标
			 * @param centerY 未缩放屏幕像素纵坐标
			 */
			@Override
			public void onMagnificationChanged(MagnificationController controller, Region region,
					float scale, float centerX, float centerY) {
				signal("放大状态改变", new 无障碍边界(region.getBounds()), scale, centerX, centerY);
			}
		};

	/**
	 * 本连接的软键盘策略监听。
	 */
	private final SoftKeyboardController.OnShowModeChangedListener keyboard =
		new SoftKeyboardController.OnShowModeChangedListener() {
			/**
			 * 转发软键盘显示策略改变。
			 *
			 * @param controller 触发回调的控制器
			 * @param mode 当前显示策略
			 */
			@Override
			public void onShowModeChanged(SoftKeyboardController controller, int mode) {
				signal("软键盘模式改变", mode);
			}
		};

	/* 构造与内部方法 */

	/**
	 * 加入组件订阅，不主动开启系统服务。
	 *
	 * @param listener 组件订阅者
	 */
	static void add(Listener listener) {
		LISTENERS.add(listener);
	}

	/**
	 * 窗口或组件销毁时撤销订阅。
	 *
	 * @param listener 组件订阅者
	 */
	static void remove(Listener listener) {
		LISTENERS.remove(listener);
	}

	/**
	 * 取得实际连接的系统服务。
	 *
	 * @return 当前连接；未连接时为空
	 */
	static AccessibilityListener current() {

		return active;
	}

	/**
	 * 分发状态变化，单个订阅者失败不影响其它组件。
	 *
	 * @param name 事件名称
	 * @param args 参数
	 */
	private static void signal(String name, Object... args) {
		for (Listener listener : LISTENERS) {
			try {
				listener.signal(name, args);
			} catch (RuntimeException error) {
				Log.e("ES4AAccessibility", "无障碍状态回调失败：" + name, error);
			}
		}
	}

	/**
	 * 撤销本连接的回调，防止旧服务解绑时清除新连接。
	 */
	private void disconnected() {
		getMagnificationController().removeListener(magnification);
		getSoftKeyboardController().removeOnShowModeChangedListener(keyboard);
		if (callbacks26 != null) {
			callbacks26.close();
			callbacks26 = null;
		}
		if (active == this) {
			active = null;
			signal("连接改变", false);
		}
	}

	/**
	 * 分发新旧系统都提供的手势编号。
	 *
	 * @param id 触摸探索手势编号
	 * @return 任一订阅者已处理时为真
	 */
	private boolean forwardGesture(int id) {
		boolean handled = false;
		for (Listener listener : LISTENERS) {
			try {
				handled |= listener.gesture(id);
			} catch (RuntimeException error) {
				Log.e("ES4AAccessibility", "无障碍手势回调失败", error);
			}
		}

		return handled;
	}

	/* 系统回调 */

	/**
	 * 系统完成绑定后建立当前连接，并注册该连接独有的控制器回调。
	 */
	@Override
	protected void onServiceConnected() {
		super.onServiceConnected();
		active = this;
		getMagnificationController().addListener(magnification);
		getSoftKeyboardController().addOnShowModeChangedListener(keyboard);
		// 26 以上控制器的类型及注册隔离在版本辅助类中，低版本不创建它。
		if (Build.VERSION.SDK_INT >= 26) {
			callbacks26 = new Api26Callbacks(this);
		}
		signal("连接改变", true);
	}

	@Override
	public boolean onUnbind(Intent intent) {
		disconnected();
		return super.onUnbind(intent);
	}

	@Override
	public void onDestroy() {
		disconnected();
		super.onDestroy();
	}

	@Override
	public void onInterrupt() {
		signal("服务被中断");
	}

	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		if (event == null) {
			return;
		}
		for (Listener listener : LISTENERS) {
			try {
				listener.event(event);
			} catch (RuntimeException error) {
				Log.e("ES4AAccessibility", "无障碍事件回调失败", error);
			}
		}
	}

	@Override
	protected boolean onKeyEvent(KeyEvent event) {
		boolean handled = false;
		for (Listener listener : LISTENERS) {
			try {
				handled |= listener.key(event);
			} catch (RuntimeException error) {
				Log.e("ES4AAccessibility", "无障碍按键回调失败", error);
			}
		}

		return handled;
	}

	/**
	 * API 24–29 的系统仍通过此旧回调交付手势，必须保留。
	 *
	 * @param gestureId 手势编号
	 * @return 任一订阅者已处理时为真
	 */
	@Override
	@SuppressWarnings("deprecation")
	protected boolean onGesture(int gestureId) {
		return forwardGesture(gestureId);
	}

	/**
	 * API 30 起使用新的手势事件回调，系统不会在低版本调用此入口。
	 *
	 * @param event 系统手势事件
	 * @return 任一订阅者已处理时为真
	 */
	@Override
	@TargetApi(30)
	public boolean onGesture(AccessibilityGestureEvent event) {
		return forwardGesture(event.getGestureId());
	}

	/* 内部类型 */

	/**
	 * Simple 组件订阅协议，所有调用来自系统主线程。
	 */
	interface Listener {
		/**
		 * 同步转发系统事件；订阅者不可回收原生参数。
		 *
		 * @param event 本次系统事件
		 */
		void event(AccessibilityEvent event);

		/**
		 * 询问组件是否消费本次按键。
		 *
		 * @param event 按键事件
		 * @return 组件要求消费时为真
		 */
		boolean key(KeyEvent event);

		/**
		 * 询问组件是否处理本次触摸探索手势。
		 *
		 * @param id 系统手势编号
		 * @return 组件要求消费时为真
		 */
		boolean gesture(int id);

		/**
		 * 转发不持有原生池对象的状态参数。
		 *
		 * @param name Simple 事件名称
		 * @param args 状态参数
		 */
		void signal(String name, Object... args);
	}

	/**
	 * 仅在 API 26 以上创建的按钮及指纹回调注册。
	 */
	@TargetApi(26)
	private static final class Api26Callbacks {

		/**
		 * 当前连接的系统辅助按钮控制器。
		 */
		private final AccessibilityButtonController button;

		/**
		 * 当前连接的指纹手势控制器。
		 */
		private final FingerprintGestureController fingerprint;

		/**
		 * 辅助按钮的点击和可用性回调。
		 */
		private final AccessibilityButtonController.AccessibilityButtonCallback buttonCallback =
			new AccessibilityButtonController.AccessibilityButtonCallback() {
				/**
				 * 转发系统辅助按钮的点击。
				 *
				 * @param controller 触发回调的控制器
				 */
				@Override
				public void onClicked(AccessibilityButtonController controller) {
					signal("辅助按钮被单击");
				}

				/**
				 * 转发系统辅助按钮的可用性改变。
				 *
				 * @param controller 触发回调的控制器
				 * @param available 当前是否可用
				 */
				@Override
				public void onAvailabilityChanged(AccessibilityButtonController controller, boolean available) {
					signal("辅助按钮可用性改变", available);
				}
			};

		/**
		 * 指纹手势及检测可用性的回调。
		 */
		private final FingerprintGestureController.FingerprintGestureCallback fingerprintCallback =
			new FingerprintGestureController.FingerprintGestureCallback() {
				/**
				 * 转发指纹传感器报告的手势编号。
				 *
				 * @param gesture 系统指纹手势编号
				 */
				@Override
				public void onGestureDetected(int gesture) {
					signal("收到指纹手势", gesture);
				}

				/**
				 * 转发指纹手势检测的可用性改变。
				 *
				 * @param available 当前是否可用
				 */
				@Override
				public void onGestureDetectionAvailabilityChanged(boolean available) {
					signal("指纹手势可用性改变", available);
				}
			};

		/**
		 * 注册系统控制器回调，服务清单须声明对应能力及指纹权限。
		 *
		 * @param service 已连接的服务
		 */
		Api26Callbacks(AccessibilityListener service) {
			button = service.getAccessibilityButtonController();
			fingerprint = service.getFingerprintGestureController();
			button.registerAccessibilityButtonCallback(buttonCallback);
			fingerprint.registerFingerprintGestureCallback(fingerprintCallback, null);
		}

		void close() {
			button.unregisterAccessibilityButtonCallback(buttonCallback);
			fingerprint.unregisterFingerprintGestureCallback(fingerprintCallback);
		}
	}
}
