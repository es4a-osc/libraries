package simple.library.wsd.accessibility;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.os.Parcel;

import simple.runtime.android.MainActivity;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.SimpleProperty;

/**
 * 无障碍服务信息快照。仅事件类型、反馈类型、标志、通知间隔及监听包名可动态修改；修改后由服务的应用配置方法提交。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class 无障碍服务信息 {

	/**
	 * 独立服务信息快照，修改不会直接影响运行中的服务。
	 */
	final AccessibilityServiceInfo value;

	/**
	 * 从系统服务信息复制全部字段。
	 *
	 * @param source 非空系统服务信息
	 */
	无障碍服务信息(AccessibilityServiceInfo source) {
		Parcel parcel = Parcel.obtain();
		try {
			source.writeToParcel(parcel, 0);
			parcel.setDataPosition(0);
			value = AccessibilityServiceInfo.CREATOR.createFromParcel(parcel);
		} finally {
			parcel.recycle();
		}
	}

	/* 属性 */

	/**
	 * 事件类型掩码，见无障碍常量。
	 *
	 * @return 事件类型掩码，见无障碍常量。
	 */
	@SimpleProperty
	public int 事件类型() {
		return value.eventTypes;
	}

	/**
	 * 反馈类型掩码，见无障碍常量。
	 *
	 * @return 反馈类型掩码，见无障碍常量。
	 */
	@SimpleProperty
	public int 反馈类型() {
		return value.feedbackType;
	}

	/**
	 * 标志掩码，见无障碍常量。
	 *
	 * @return 标志掩码，见无障碍常量。
	 */
	@SimpleProperty
	public int 标志() {
		return value.flags;
	}

	/**
	 * 通知间隔；通知间隔单位为毫秒。
	 *
	 * @return 通知间隔；通知间隔单位为毫秒。
	 */
	@SimpleProperty
	public long 通知间隔() {
		return value.notificationTimeout;
	}

	/**
	 * 编号，属于服务的静态描述。
	 *
	 * @return 编号，属于服务的静态描述。
	 */
	@SimpleProperty
	public String 编号() {
		return AccessibilitySupport.text(value.getId());
	}

	/**
	 * 设置界面，属于服务的静态描述。
	 *
	 * @return 设置界面，属于服务的静态描述。
	 */
	@SimpleProperty
	public String 设置界面() {
		return AccessibilitySupport.text(value.getSettingsActivityName());
	}

	/**
	 * 能力，属于服务的静态描述。
	 *
	 * @return 能力，属于服务的静态描述。
	 */
	@SimpleProperty
	public int 能力() {
		return value.getCapabilities();
	}

	/* 方法 */

	/**
	 * 修改快照的事件类型；尚未提交到运行中的服务。
	 *
	 * @param amount 事件类型值
	 */
	@SimpleFunction
	public void 设置事件类型(int amount) {
		value.eventTypes = amount;
	}

	/**
	 * 修改快照的反馈类型；尚未提交到运行中的服务。
	 *
	 * @param amount 反馈类型值
	 */
	@SimpleFunction
	public void 设置反馈类型(int amount) {
		value.feedbackType = amount;
	}

	/**
	 * 修改快照的标志；尚未提交到运行中的服务。
	 *
	 * @param amount 标志值
	 */
	@SimpleFunction
	public void 设置标志(int amount) {
		value.flags = amount;
	}

	/**
	 * 修改快照的通知间隔；尚未提交到运行中的服务。
	 *
	 * @param amount 通知间隔值
	 */
	@SimpleFunction
	public void 设置通知间隔(long amount) {
		if (amount < 0) {
			throw new IllegalArgumentException("通知间隔不能为负数");
		}
		value.notificationTimeout = amount;
	}

	/**
	 * 加载服务的本地化说明文本。
	 *
	 * @return 说明文本；未提供时为空文本
	 */
	@SimpleFunction
	public String 取说明() {
		return AccessibilitySupport.text(value.loadDescription(MainActivity.getContext().getPackageManager()));
	}

	/**
	 * 加载服务摘要，需要 API 26 及以上。
	 *
	 * @return 摘要文本；未提供时为空文本
	 */
	@SimpleFunction
	public String 取摘要() {
		AccessibilitySupport.api(26, "服务摘要");
		return AccessibilitySupport.text(value.loadSummary(MainActivity.getContext().getPackageManager()));
	}

	/**
	 * 取得当前限制的应用包名。
	 *
	 * @return 包名数组；空数组表示监听全部应用
	 */
	@SimpleFunction
	public String[] 取监听包名() {
		return value.packageNames == null ? new String[0] : value.packageNames.clone();
	}

	/**
	 * 修改监听包名快照。空数组表示监听全部应用；不会授予额外权限。
	 *
	 * @param packages 包名数组；不得含空项
	 */
	@SimpleFunction
	public void 设置监听包名(String[] packages) {
		AccessibilitySupport.notNull(packages, "包名数组");
		for (String name : packages) {
			AccessibilitySupport.notEmpty(name, "包名");
		}
		value.packageNames = packages.length == 0 ? null : packages.clone();
	}
}
