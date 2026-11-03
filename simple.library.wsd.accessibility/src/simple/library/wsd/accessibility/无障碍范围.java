package simple.library.wsd.accessibility;

import android.view.accessibility.AccessibilityNodeInfo;

import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.SimpleProperty;

/**
 * 节点的范围只读描述；仅表达元数据，不修改真实界面。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class 无障碍范围 {

	/**
	 * 系统报告的节点数值范围。
	 */
	private final AccessibilityNodeInfo.RangeInfo value;

	/**
	 * 保存系统返回的不可变描述。
	 *
	 * @param value 非空的系统描述
	 */
	无障碍范围(AccessibilityNodeInfo.RangeInfo value) {
		this.value = value;
	}

	/* 属性 */

	/**
	 * 类型。
	 *
	 * @return 类型。
	 */
	@SimpleProperty
	public int 类型() {
		return value.getType();
	}

	/**
	 * 最小值。
	 *
	 * @return 最小值。
	 */
	@SimpleProperty
	public float 最小值() {
		return value.getMin();
	}

	/**
	 * 最大值。
	 *
	 * @return 最大值。
	 */
	@SimpleProperty
	public float 最大值() {
		return value.getMax();
	}

	/**
	 * 当前值。
	 *
	 * @return 当前值。
	 */
	@SimpleProperty
	public float 当前值() {
		return value.getCurrent();
	}
}
