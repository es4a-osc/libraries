package simple.library.wsd.accessibility;

import java.util.ArrayList;
import java.util.List;

import android.accessibilityservice.GestureDescription;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.SimpleProperty;

/**
 * 一个或多个笔画组成的手势，支持多点同时操作。派发可能因新手势、用户触摸或其它服务而被取消。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class 无障碍手势 {

	/**
	 * 按添加顺序保存不可变笔画，派发时生成独立手势。
	 */
	private final List<无障碍笔画> strokes = new ArrayList<无障碍笔画>();

	/**
	 * 创建空手势，派发前至少添加一笔。
	 */
	public 无障碍手势() {
	}

	/**
	 * 构建派发快照。
	 *
	 * @return 系统手势描述
	 */
	GestureDescription build() {
		if (strokes.isEmpty()) {
			throw new IllegalStateException("手势至少需要一个笔画");
		}
		GestureDescription.Builder builder = new GestureDescription.Builder();
		for (无障碍笔画 stroke : strokes) {
			builder.addStroke(stroke.value);
		}

		return builder.build();
	}

	/* 属性 */

	/**
	 * 当前笔画数量。
	 *
	 * @return 当前笔画数量。
	 */
	@SimpleProperty
	public int 笔画数() {
		return strokes.size();
	}

	/**
	 * 系统允许的最大笔画数量。
	 *
	 * @return 系统允许的最大笔画数量。
	 */
	@SimpleProperty
	public static int 笔画上限() {
		return GestureDescription.getMaxStrokeCount();
	}

	/**
	 * 单次手势允许的最大结束时间，单位为毫秒。
	 *
	 * @return 单次手势允许的最大结束时间，单位为毫秒。
	 */
	@SimpleProperty
	public static long 时长上限() {
		return GestureDescription.getMaxGestureDuration();
	}

	/* 方法 */

	/**
	 * 添加一笔，时间可与其它笔画重叠。
	 *
	 * @param stroke 不可变笔画
	 */
	@SimpleFunction
	public void 添加笔画(无障碍笔画 stroke) {
		AccessibilitySupport.notNull(stroke, "笔画");
		if (strokes.size() >= GestureDescription.getMaxStrokeCount()) {
			throw new IllegalArgumentException("笔画数量超过系统上限");
		}
		strokes.add(stroke);
	}

	/**
	 * 按索引取得不可变笔画。
	 *
	 * @param index 从零开始的有效索引
	 * @return 对应笔画
	 */
	@SimpleFunction
	public 无障碍笔画 取笔画(int index) {
		return strokes.get(index);
	}

	/**
	 * 移除全部笔画，不取消已经派发的手势。
	 */
	@SimpleFunction
	public void 清空() {
		strokes.clear();
	}
}
