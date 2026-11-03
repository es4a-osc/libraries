package simple.library.wsd.accessibility;

import android.view.accessibility.AccessibilityNodeInfo;

import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.SimpleProperty;

/**
 * 节点的集合项只读描述；仅表达元数据，不修改真实界面。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class 无障碍集合项 {

	/**
	 * 系统报告的集合项位置及选择状态。
	 */
	private final AccessibilityNodeInfo.CollectionItemInfo value;

	/**
	 * 由所属节点按设备版本读取的标题语义快照。
	 */
	private final boolean heading;

	/**
	 * 保存系统返回的不可变描述。
	 *
	 * @param value 非空的系统描述
	 * @param heading 从所属节点按系统版本读取的标题语义
	 */
	无障碍集合项(AccessibilityNodeInfo.CollectionItemInfo value, boolean heading) {
		this.value = value;
		this.heading = heading;
	}

	/* 属性 */

	/**
	 * 行索引；索引从零开始，范围类型及选择模式见无障碍常量。
	 *
	 * @return 行索引；索引从零开始，范围类型及选择模式见无障碍常量。
	 */
	@SimpleProperty
	public int 行索引() {
		return value.getRowIndex();
	}

	/**
	 * 列索引；索引从零开始，范围类型及选择模式见无障碍常量。
	 *
	 * @return 列索引；索引从零开始，范围类型及选择模式见无障碍常量。
	 */
	@SimpleProperty
	public int 列索引() {
		return value.getColumnIndex();
	}

	/**
	 * 行跨度。
	 *
	 * @return 行跨度。
	 */
	@SimpleProperty
	public int 行跨度() {
		return value.getRowSpan();
	}

	/**
	 * 列跨度。
	 *
	 * @return 列跨度。
	 */
	@SimpleProperty
	public int 列跨度() {
		return value.getColumnSpan();
	}

	/**
	 * 标题项。
	 *
	 * @return 标题项。
	 */
	@SimpleProperty
	public boolean 标题项() {

		return heading;
	}

	/**
	 * 已选中。
	 *
	 * @return 已选中。
	 */
	@SimpleProperty
	public boolean 已选中() {
		return value.isSelected();
	}
}
