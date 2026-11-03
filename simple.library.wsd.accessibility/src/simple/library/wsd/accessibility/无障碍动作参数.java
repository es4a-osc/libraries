package simple.library.wsd.accessibility;

import android.os.Bundle;
import android.view.accessibility.AccessibilityNodeInfo;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * 节点动作参数包。只对声明支持相应动作的节点有效；创建后按动作填写参数。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class 无障碍动作参数 {

	/**
	 * 待提交的动作参数包。
	 */
	final Bundle values = new Bundle();

	/**
	 * 创建空参数包。
	 */
	public 无障碍动作参数() {
	}

	/* 方法 */

	/**
	 * 设置替换文本动作的参数。
	 *
	 * @param text 替换节点内容的文本，不为空
	 */
	@SimpleFunction
	public void 设置文本(String text) {
		AccessibilitySupport.notNull(text, "文本");
		values.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text);
	}

	/**
	 * 设置文本选择区动作的参数。
	 *
	 * @param start 从零开始的起始字符索引
	 * @param end 结束字符索引，不包含该字符
	 */
	@SimpleFunction
	public void 设置选择区(int start, int end) {
		if (start < 0 || end < start) {
			throw new IllegalArgumentException("选择区必须满足 0 ≤ 起始位置 ≤ 结束位置");
		}
		values.putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_SELECTION_START_INT, start);
		values.putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_SELECTION_END_INT, end);
	}

	/**
	 * 设置滚动到集合行列位置的参数。
	 *
	 * @param row 从零开始的行索引
	 * @param column 从零开始的列索引
	 */
	@SimpleFunction
	public void 设置滚动位置(int row, int column) {
		if (row < 0 || column < 0) {
			throw new IllegalArgumentException("行列索引不能为负数");
		}
		values.putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_ROW_INT, row);
		values.putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_COLUMN_INT, column);
	}

	/**
	 * 设置进度动作的参数，不替调用者推定节点范围。
	 *
	 * @param progress 节点范围允许的有限数值
	 */
	@SimpleFunction
	public void 设置进度(float progress) {
		AccessibilitySupport.finite(progress, "进度");
		values.putFloat(AccessibilityNodeInfo.ACTION_ARGUMENT_PROGRESS_VALUE, progress);
	}

	/**
	 * 设置按粒度移动文本的参数。
	 *
	 * @param granularity 字符、单词、行等移动粒度常量
	 * @param extend 是否在移动时扩展选择区
	 */
	@SimpleFunction
	public void 设置移动粒度(int granularity, boolean extend) {
		values.putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_MOVEMENT_GRANULARITY_INT, granularity);
		values.putBoolean(AccessibilityNodeInfo.ACTION_ARGUMENT_EXTEND_SELECTION_BOOLEAN, extend);
	}

	/**
	 * 设置网页元素导航动作的参数。
	 *
	 * @param element HTML 元素名称，例如 BUTTON
	 */
	@SimpleFunction
	public void 设置HTML元素(String element) {
		AccessibilitySupport.notNull(element, "元素名称");
		values.putString(AccessibilityNodeInfo.ACTION_ARGUMENT_HTML_ELEMENT_STRING, element);
	}

	/**
	 * 设置移动窗口动作的目标位置。
	 *
	 * @param x 目标屏幕像素横坐标
	 * @param y 目标屏幕像素纵坐标
	 */
	@SimpleFunction
	public void 设置窗口位置(int x, int y) {
		values.putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_MOVE_WINDOW_X, x);
		values.putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_MOVE_WINDOW_Y, y);
	}

	/**
	 * 移除已填写的全部参数。
	 */
	@SimpleFunction
	public void 清空() {
		values.clear();
	}
}
