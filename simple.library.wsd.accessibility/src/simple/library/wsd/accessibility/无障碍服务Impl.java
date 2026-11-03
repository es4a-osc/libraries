package simple.library.wsd.accessibility;

import java.util.List;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.accessibilityservice.GestureDescription;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.CaptioningManager;

import simple.runtime.android.MainActivity;
import simple.runtime.components.impl.组件Impl;
import simple.runtime.components.组件容器;
import simple.runtime.events.EventDispatcher;
import simple.runtime.parameters.BooleanReferenceParameter;

/**
 * 无障碍服务组件的主线程、事件订阅和宿主生命周期适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class 无障碍服务Impl extends 组件Impl implements 无障碍服务,
		AccessibilityListener.Listener, MainActivity.OnDestroyListener {

	/* 内部状态 */

	/**
	 * 组件所属窗口，用于打开设置和注销销毁监听。
	 */
	private final MainActivity host;

	/**
	 * 把手势完成和取消回调交付到主线程。
	 */
	private final Handler handler = new Handler(Looper.getMainLooper());

	/**
	 * 系统无障碍状态管理器，不表示本应用服务已经连接。
	 */
	private final AccessibilityManager manager;

	/**
	 * 系统字幕首选项管理器。
	 */
	private final CaptioningManager captioning;

	/**
	 * 组件是否已经完成一次性清理。
	 */
	private boolean destroyed;

	/**
	 * 本组件是否接收事件，暂停不改变系统服务授权。
	 */
	private boolean listening = true;

	/**
	 * 最近接收通知的内容操作，连接断开后清除。
	 */
	private PendingIntent notification;

	/**
	 * 监听系统中任意无障碍服务的启用状态。
	 */
	private final AccessibilityManager.AccessibilityStateChangeListener systemListener =
		new AccessibilityManager.AccessibilityStateChangeListener() {
			/**
			 * 系统无障碍状态改变时通知当前组件。
			 *
			 * @param enabled 系统无障碍是否已启用
			 */
			@Override
			public void onAccessibilityStateChanged(boolean enabled) {
				signal("系统启用状态改变", enabled);
			}
		};

	/**
	 * 监听触摸探索模式的启用状态。
	 */
	private final AccessibilityManager.TouchExplorationStateChangeListener touchListener =
		new AccessibilityManager.TouchExplorationStateChangeListener() {
			/**
			 * 触摸探索状态改变时通知当前组件。
			 *
			 * @param enabled 当前是否已启用触摸探索
			 */
			@Override
			public void onTouchExplorationStateChanged(boolean enabled) {
				signal("触摸探索状态改变", enabled);
			}
		};

	/**
	 * 监听系统字幕的启用、语言、字号和样式改变。
	 */
	private final CaptioningManager.CaptioningChangeListener captionListener =
		new CaptioningManager.CaptioningChangeListener() {
			/**
			 * 字幕启用状态改变后请求重新读取首选项。
			 *
			 * @param enabled 当前字幕启用状态
			 */
			@Override
			public void onEnabledChanged(boolean enabled) {
				signal("字幕设置改变");
			}

			/**
			 * 字幕字号改变后请求重新读取首选项。
			 *
			 * @param scale 当前字幕字号比例
			 */
			@Override
			public void onFontScaleChanged(float scale) {
				signal("字幕设置改变");
			}

			/**
			 * 字幕语言改变后请求重新读取首选项。
			 *
			 * @param locale 新的语言首选项，可以为空
			 */
			@Override
			public void onLocaleChanged(java.util.Locale locale) {
				signal("字幕设置改变");
			}

			/**
			 * 字幕样式改变后请求重新读取首选项。
			 *
			 * @param style 新的系统字幕样式
			 */
			@Override
			public void onUserStyleChanged(CaptioningManager.CaptionStyle style) {
				signal("字幕设置改变");
			}
		};

	/* 构造与内部方法 */

	/**
	 * 连接宿主窗口并注册各类系统状态监听。
	 *
	 * @param container 组件所属容器
	 */
	public 无障碍服务Impl(组件容器 container) {
		super(container);
		AccessibilitySupport.mainThread();
		host = MainActivity.getContext();
		manager = (AccessibilityManager) host.getSystemService(Context.ACCESSIBILITY_SERVICE);
		captioning = (CaptioningManager) host.getSystemService(Context.CAPTIONING_SERVICE);
		host.addOnDestroyListener(this);
		AccessibilityListener.add(this);
		manager.addAccessibilityStateChangeListener(systemListener);
		manager.addTouchExplorationStateChangeListener(touchListener);
		captioning.addCaptioningChangeListener(captionListener);
	}

	/**
	 * 拒绝非主线程或已销毁组件的业务操作。
	 */
	private void check() {
		AccessibilitySupport.mainThread();
		if (destroyed) {
			throw new IllegalStateException("无障碍服务组件已销毁");
		}
	}

	/* 属性 */

	@Override
	public boolean 已启用() {
		check();
		String enabled = Settings.Secure.getString(host.getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
		ComponentName expected = new ComponentName(host, AccessibilityListener.class);
		if (enabled != null) {
			for (String name : enabled.split(":")) {
				if (expected.equals(ComponentName.unflattenFromString(name))) {
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public boolean 已连接() {
		check();
		return AccessibilityListener.current() != null;
	}

	@Override
	public boolean 系统无障碍已启用() {
		check();
		return manager.isEnabled();
	}

	@Override
	public boolean 触摸探索已启用() {
		check();
		return manager.isTouchExplorationEnabled();
	}

	@Override
	public boolean 事件监听() {
		check();

		return listening;
	}

	@Override
	public void 事件监听(boolean enabled) {
		check();
		listening = enabled;
	}

	@Override
	public boolean 字幕已启用() {
		check();
		return captioning.isEnabled();
	}

	@Override
	public float 字幕字体比例() {
		check();
		return captioning.getFontScale();
	}

	@Override
	public String 字幕语言() {
		check();
		java.util.Locale locale = captioning.getLocale();
		return locale == null ? "" : locale.toLanguageTag();
	}

	/* 方法 */

	@Override
	public boolean 打开设置() {
		check();
		try {
			host.startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
			return true;
		} catch (android.content.ActivityNotFoundException error) {
			return false;
		}
	}

	@Override
	public 无障碍节点 取根节点() {
		check();
		AccessibilityListener service = AccessibilityListener.current();
		return service == null ? null : 无障碍节点.wrap(service.getRootInActiveWindow());
	}

	@Override
	public 无障碍窗口列表 取窗口列表() {
		check();
		AccessibilityListener service = AccessibilityListener.current();
		return new 无障碍窗口列表(service == null ? null : service.getWindows());
	}

	@Override
	public 无障碍节点 查找焦点(int type) {
		check();
		AccessibilityListener service = AccessibilityListener.current();
		return service == null ? null : 无障碍节点.wrap(service.findFocus(type));
	}

	@Override
	public boolean 执行全局动作(int action) {
		check();
		AccessibilityListener service = AccessibilityListener.current();
		return service != null && service.performGlobalAction(action);
	}

	@Override
	public boolean 禁用服务() {
		check();
		AccessibilityListener service = AccessibilityListener.current();
		if (service == null) {
			return false;
		}
		service.disableSelf();
		return true;
	}

	@Override
	public 无障碍服务信息 取服务信息() {
		check();
		AccessibilityListener service = AccessibilityListener.current();
		return service == null ? null : new 无障碍服务信息(service.getServiceInfo());
	}

	@Override
	public boolean 应用配置(无障碍服务信息 info) {
		check();
		AccessibilitySupport.notNull(info, "服务信息");
		AccessibilityListener service = AccessibilityListener.current();
		if (service == null) {
			return false;
		}
		service.setServiceInfo(new 无障碍服务信息(info.value).value);
		return true;
	}

	@Override
	public 无障碍服务信息[] 取已安装服务() {
		check();

		List<AccessibilityServiceInfo> values = manager.getInstalledAccessibilityServiceList();
		无障碍服务信息[] result = new 无障碍服务信息[values.size()];
		for (int i = 0; i < values.size(); i++) {
			result[i] = new 无障碍服务信息(values.get(i));
		}

		return result;
	}

	@Override
	public 无障碍服务信息[] 取已启用服务() {
		check();

		List<AccessibilityServiceInfo> values = manager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK);
		无障碍服务信息[] result = new 无障碍服务信息[values.size()];
		for (int i = 0; i < values.size(); i++) {
			result[i] = new 无障碍服务信息(values.get(i));
		}

		return result;
	}

	@Override
	public void 中断所有反馈() {
		check();
		manager.interrupt();
	}

	@Override
	public void 发送事件(无障碍事件 event) {
		check();
		AccessibilitySupport.notNull(event, "事件");
		if (!manager.isEnabled()) {
			throw new IllegalStateException("系统无障碍未启用");
		}
		manager.sendAccessibilityEvent(AccessibilityEvent.obtain((AccessibilityEvent) event.require()));
	}

	@Override
	public boolean 打开最近通知() {
		check();
		if (notification == null) {
			return false;
		}
		try {
			notification.send();
			return true;
		} catch (PendingIntent.CanceledException error) {
			return false;
		}
	}

	@Override
	public 无障碍软键盘控制器 取软键盘控制器() {
		check();
		AccessibilitySupport.api(24, "取软键盘控制器");
		AccessibilityListener service = AccessibilityListener.current();
		return service == null ? null : new 无障碍软键盘控制器(service);
	}

	@Override
	public 无障碍放大控制器 取放大控制器() {
		check();
		AccessibilitySupport.api(24, "取放大控制器");
		AccessibilityListener service = AccessibilityListener.current();
		return service == null ? null : new 无障碍放大控制器(service);
	}

	@Override
	public 无障碍按钮控制器 取辅助按钮控制器() {
		check();
		AccessibilitySupport.api(26, "取辅助按钮控制器");
		AccessibilityListener service = AccessibilityListener.current();
		return service == null ? null : new 无障碍按钮控制器(service);
	}

	@Override
	public 无障碍指纹控制器 取指纹控制器() {
		check();
		AccessibilitySupport.api(26, "取指纹控制器");
		AccessibilityListener service = AccessibilityListener.current();
		return service == null ? null : new 无障碍指纹控制器(service);
	}

	@Override
	public boolean 派发手势(无障碍手势 gesture, int requestId) {
		check();
		AccessibilitySupport.notNull(gesture, "手势");
		GestureDescription description = gesture.build();
		AccessibilityListener service = AccessibilityListener.current();
		if (service == null) {
			return false;
		}

		return service.dispatchGesture(description, new AccessibilityService.GestureResultCallback() {
			/**
			 * 手势实际完成后通知尚未销毁且仍连接原服务的组件。
			 *
			 * @param completed 已经完成的手势快照
			 */
			@Override
			public void onCompleted(GestureDescription completed) {
				if (!destroyed && AccessibilityListener.current() == service) {
					手势完成(requestId);
				}
			}

			/**
			 * 手势被取消后通知尚未销毁的组件。
			 *
			 * @param cancelled 被系统取消的手势快照
			 */
			@Override
			public void onCancelled(GestureDescription cancelled) {
				if (!destroyed) {
					手势取消(requestId);
				}
			}
		}, handler);
	}

	@Override
	public 无障碍字幕样式 取字幕样式() {
		check();
		return new 无障碍字幕样式(captioning.getUserStyle());
	}

	/* 事件 */

	@Override
	public void 连接改变(boolean connected) {
		if (!destroyed && listening) {
			EventDispatcher.dispatchEvent(this, "连接改变", connected);
		}
	}

	@Override
	public void 服务被中断() {
		if (!destroyed && listening) {
			EventDispatcher.dispatchEvent(this, "服务被中断");
		}
	}

	@Override
	public void 收到事件(无障碍事件 event) {
		if (!destroyed && listening) {
			EventDispatcher.dispatchEvent(this, "收到事件", event);
		}
	}

	@Override
	public void 收到按键(int code, int action, BooleanReferenceParameter handled) {
		if (!destroyed && listening) {
			EventDispatcher.dispatchEvent(this, "收到按键", code, action, handled);
		}
	}

	@Override
	public void 收到触摸手势(int gesture, BooleanReferenceParameter handled) {
		if (!destroyed && listening) {
			EventDispatcher.dispatchEvent(this, "收到触摸手势", gesture, handled);
		}
	}

	@Override
	public void 手势完成(int requestId) {
		if (!destroyed && listening) {
			EventDispatcher.dispatchEvent(this, "手势完成", requestId);
		}
	}

	@Override
	public void 手势取消(int requestId) {
		if (!destroyed && listening) {
			EventDispatcher.dispatchEvent(this, "手势取消", requestId);
		}
	}

	@Override
	public void 软键盘模式改变(int mode) {
		if (!destroyed && listening) {
			EventDispatcher.dispatchEvent(this, "软键盘模式改变", mode);
		}
	}

	@Override
	public void 放大状态改变(无障碍边界 bounds, float scale, float x, float y) {
		if (!destroyed && listening) {
			EventDispatcher.dispatchEvent(this, "放大状态改变", bounds, scale, x, y);
		}
	}

	@Override
	public void 辅助按钮被单击() {
		if (!destroyed && listening) {
			EventDispatcher.dispatchEvent(this, "辅助按钮被单击");
		}
	}

	@Override
	public void 辅助按钮可用性改变(boolean available) {
		if (!destroyed && listening) {
			EventDispatcher.dispatchEvent(this, "辅助按钮可用性改变", available);
		}
	}

	@Override
	public void 收到指纹手势(int gesture) {
		if (!destroyed && listening) {
			EventDispatcher.dispatchEvent(this, "收到指纹手势", gesture);
		}
	}

	@Override
	public void 指纹手势可用性改变(boolean available) {
		if (!destroyed && listening) {
			EventDispatcher.dispatchEvent(this, "指纹手势可用性改变", available);
		}
	}

	@Override
	public void 系统启用状态改变(boolean enabled) {
		if (!destroyed && listening) {
			EventDispatcher.dispatchEvent(this, "系统启用状态改变", enabled);
		}
	}

	@Override
	public void 触摸探索状态改变(boolean enabled) {
		if (!destroyed && listening) {
			EventDispatcher.dispatchEvent(this, "触摸探索状态改变", enabled);
		}
	}

	@Override
	public void 字幕设置改变() {
		if (!destroyed && listening) {
			EventDispatcher.dispatchEvent(this, "字幕设置改变");
		}
	}

	/* 系统回调 */

	@Override
	public void event(AccessibilityEvent event) {
		if (destroyed || !listening) {
			return;
		}
		if (event.getEventType() == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
			android.os.Parcelable data = event.getParcelableData();
			notification = data instanceof Notification ? ((Notification) data).contentIntent : null;
		}
		// 不把系统池对象交给调用者；回调副本结束时总是释放。
		无障碍事件 snapshot = new 无障碍事件(AccessibilityEvent.obtain(event));
		try {
			收到事件(snapshot);
		} finally {
			snapshot.释放();
		}
	}

	@Override
	public boolean key(KeyEvent event) {
		BooleanReferenceParameter handled = new BooleanReferenceParameter(false);
		收到按键(event.getKeyCode(), event.getAction(), handled);
		return handled.get();
	}

	@Override
	public boolean gesture(int id) {
		BooleanReferenceParameter handled = new BooleanReferenceParameter(false);
		收到触摸手势(id, handled);
		return handled.get();
	}

	@Override
	public void signal(String name, Object... args) {
		if ("连接改变".equals(name) && Boolean.FALSE.equals(args[0])) {
			notification = null;
		}
		if (!destroyed && listening) {
			EventDispatcher.dispatchEvent(this, name, args);
		}
	}

	@Override
	public void onDestroy() {
		销毁();
	}

	@Override
	public void 销毁() {
		AccessibilitySupport.mainThread();
		if (destroyed) {
			return;
		}
		destroyed = true;
		listening = false;
		notification = null;
		AccessibilityListener.remove(this);
		manager.removeAccessibilityStateChangeListener(systemListener);
		manager.removeTouchExplorationStateChangeListener(touchListener);
		captioning.removeCaptioningChangeListener(captionListener);
		host.removeOnDestroyListener(this);
		super.销毁();
	}
}
