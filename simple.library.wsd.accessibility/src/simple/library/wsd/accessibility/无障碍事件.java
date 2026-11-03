package simple.library.wsd.accessibility;

import android.view.accessibility.AccessibilityEvent;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.SimpleProperty;

/**
 * 系统无障碍事件的只读视图。收到事件回调的参数仅在该次回调中有效；需保存时复制事件，并在用完副本后释放。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class 无障碍事件 extends 无障碍记录 {

	/**
	 * 接管已经复制的原生事件。
	 *
	 * @param event 事件副本
	 */
	无障碍事件(AccessibilityEvent event) {
		super(event);
	}

	/**
	 * 取得尚未释放的事件。
	 *
	 * @return 原生事件
	 */
	private AccessibilityEvent event() {
		return (AccessibilityEvent) require();
	}

	/* 属性 */

	/**
	 * 事件类型。
	 *
	 * @return 事件类型。
	 */
	@SimpleProperty
	public int 事件类型() {
		return event().getEventType();
	}

	/**
	 * 动作编号。
	 *
	 * @return 动作编号。
	 */
	@SimpleProperty
	public int 动作编号() {
		return event().getAction();
	}

	/**
	 * 内容变化类型。
	 *
	 * @return 内容变化类型。
	 */
	@SimpleProperty
	public int 内容变化类型() {
		return event().getContentChangeTypes();
	}

	/**
	 * 时间。时间以开机以来的毫秒表示。
	 *
	 * @return 时间。时间以开机以来的毫秒表示。
	 */
	@SimpleProperty
	public long 时间() {
		return event().getEventTime();
	}

	/**
	 * 移动粒度。
	 *
	 * @return 移动粒度。
	 */
	@SimpleProperty
	public int 移动粒度() {
		return event().getMovementGranularity();
	}

	/**
	 * 包名。
	 *
	 * @return 包名。
	 */
	@SimpleProperty
	public String 包名() {
		return AccessibilitySupport.text(event().getPackageName());
	}

	/**
	 * 记录数。
	 *
	 * @return 记录数。
	 */
	@SimpleProperty
	public int 记录数() {
		return event().getRecordCount();
	}

	/**
	 * 窗口变化掩码，需要 API 28 及以上。
	 *
	 * @return 窗口变化掩码，需要 API 28 及以上。
	 */
	@SimpleProperty
	public int 窗口变化类型() {
		AccessibilitySupport.api(28, "窗口变化类型");
		return event().getWindowChanges();
	}

	/**
	 * 水平滚动增量，需要 API 28 及以上，单位为像素。
	 *
	 * @return 水平滚动增量，需要 API 28 及以上，单位为像素。
	 */
	@SimpleProperty
	public int 水平滚动增量() {
		AccessibilitySupport.api(28, "滚动增量");
		return event().getScrollDeltaX();
	}

	/**
	 * 垂直滚动增量，需要 API 28 及以上，单位为像素。
	 *
	 * @return 垂直滚动增量，需要 API 28 及以上，单位为像素。
	 */
	@SimpleProperty
	public int 垂直滚动增量() {
		AccessibilitySupport.api(28, "滚动增量");
		return event().getScrollDeltaY();
	}

	/* 方法 */

	/**
	 * 取得独立记录副本，使用后释放。
	 *
	 * @param index 从零开始的记录索引
	 * @return 独立记录
	 */
	@SimpleFunction
	public 无障碍记录 取记录(int index) {
		AccessibilityEvent event = event();
		if (index < 0 || index >= event.getRecordCount()) {
			throw new IndexOutOfBoundsException("记录索引越界：" + index);
		}

		return new 无障碍记录(android.view.accessibility.AccessibilityRecord.obtain(event.getRecord(index)));
	}

	/**
	 * 复制事件，用于回调结束后保留数据，使用后释放。
	 *
	 * @return 独立事件副本
	 */
	@SimpleFunction
	public 无障碍事件 复制事件() {
		return new 无障碍事件(AccessibilityEvent.obtain(event()));
	}
}
