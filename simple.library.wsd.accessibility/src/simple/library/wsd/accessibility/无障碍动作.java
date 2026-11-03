package simple.library.wsd.accessibility;

import android.view.accessibility.AccessibilityNodeInfo;

import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.SimpleProperty;

/**
 * 节点的动作只读描述；仅表达元数据，不修改真实界面。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class 无障碍动作 {

	/**
	 * 系统报告的不可变节点动作描述。
	 */
	private final AccessibilityNodeInfo.AccessibilityAction value;

	/**
	 * 保存系统返回的不可变描述。
	 *
	 * @param value 非空的系统描述
	 */
	无障碍动作(AccessibilityNodeInfo.AccessibilityAction value) {
		this.value = value;
	}

	/* 属性 */

	/**
	 * 编号。
	 *
	 * @return 编号。
	 */
	@SimpleProperty
	public int 编号() {
		return value.getId();
	}

	/**
	 * 标签。
	 *
	 * @return 标签。
	 */
	@SimpleProperty
	public String 标签() {
		return AccessibilitySupport.text(value.getLabel());
	}
}
