package simple.library.wsd.accessibility;

import java.util.ArrayList;
import java.util.List;

import android.view.accessibility.AccessibilityNodeInfo;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.SimpleProperty;

/**
 * 固定成员的无障碍节点列表，索引从零开始。取项目返回独立副本；使用后释放列表和取得的项目。仅可串行使用。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class 无障碍节点列表 {

	/**
	 * 本列表拥有的原生项目；释放列表后为空。
	 */
	private List<AccessibilityNodeInfo> values;

	/**
	 * 接管查询结果中的原生对象；空结果转换为空列表。
	 *
	 * @param values 查询结果，可以为空
	 */
	无障碍节点列表(List<AccessibilityNodeInfo> values) {
		this.values = values == null
				? new ArrayList<AccessibilityNodeInfo>() : new ArrayList<AccessibilityNodeInfo>(values);
	}

	/**
	 * 拒绝访问已释放的列表。
	 *
	 * @return 仍然有效的列表
	 */
	private List<AccessibilityNodeInfo> require() {
		if (values == null) {
			throw new IllegalStateException("列表已释放");
		}

		return values;
	}

	/* 属性 */

	/**
	 * 列表中的项目数；已释放时拒绝读取。
	 *
	 * @return 列表中的项目数；已释放时拒绝读取。
	 */
	@SimpleProperty
	public int 数量() {
		return require().size();
	}

	/* 方法 */

	/**
	 * 取得独立项目副本，使用后单独释放。
	 *
	 * @param index 从零开始的有效索引
	 * @return 项目副本；原生项目为空时为空值
	 * @throws IndexOutOfBoundsException 索引无效
	 */
	@SimpleFunction
	public 无障碍节点 取项目(int index) {
		List<AccessibilityNodeInfo> list = require();
		if (index < 0 || index >= list.size()) {
			throw new IndexOutOfBoundsException("项目索引越界：" + index);
		}

		AccessibilityNodeInfo value = list.get(index);
		return 无障碍节点.wrap(value == null ? null : AccessibilityNodeInfo.obtain(value));
	}

	/**
	 * 释放列表持有的原生项目，可重复调用。已经取出的项目副本不受影响。
	 */
	@SimpleFunction
	public void 释放() {
		if (values != null) {
			for (AccessibilityNodeInfo value : values) {
				if (value != null) {
					value.recycle();
				}
			}
			values = null;
		}
	}
}
