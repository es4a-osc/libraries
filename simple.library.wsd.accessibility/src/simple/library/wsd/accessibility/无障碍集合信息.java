package simple.library.wsd.accessibility;

import android.view.accessibility.AccessibilityNodeInfo;

import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.SimpleProperty;

/**
 * 节点的集合信息只读描述；仅表达元数据，不修改真实界面。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class 无障碍集合信息 {

	/**
	 * 系统报告的集合结构描述。
	 */
	private final AccessibilityNodeInfo.CollectionInfo value;

	/**
	 * 保存系统返回的不可变描述。
	 *
	 * @param value 非空的系统描述
	 */
	无障碍集合信息(AccessibilityNodeInfo.CollectionInfo value) {
		this.value = value;
	}

	/* 属性 */

	/**
	 * 行数。
	 *
	 * @return 行数。
	 */
	@SimpleProperty
	public int 行数() {
		return value.getRowCount();
	}

	/**
	 * 列数。
	 *
	 * @return 列数。
	 */
	@SimpleProperty
	public int 列数() {
		return value.getColumnCount();
	}

	/**
	 * 层级结构。
	 *
	 * @return 层级结构。
	 */
	@SimpleProperty
	public boolean 层级结构() {
		return value.isHierarchical();
	}

	/**
	 * 选择模式。
	 *
	 * @return 选择模式。
	 */
	@SimpleProperty
	public int 选择模式() {
		return value.getSelectionMode();
	}
}
