package simple.library.wsd.accessibility;

import android.accessibilityservice.GestureDescription;
import android.graphics.Path;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.SimpleProperty;

/**
 * 不可变手势笔画。路径采用屏幕像素，开始时间与持续时间采用毫秒；创建后不受路径后续修改影响。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class 无障碍笔画 {

	/**
	 * 创建时已固定路径和时长的原生笔画。
	 */
	final GestureDescription.StrokeDescription value;

	/**
	 * 保存原生笔画。
	 *
	 * @param value 系统笔画描述
	 */
	无障碍笔画(GestureDescription.StrokeDescription value) {
		this.value = value;
	}

	/* 属性 */

	/**
	 * 开始时间，单位为毫秒。
	 *
	 * @return 开始时间，单位为毫秒。
	 */
	@SimpleProperty
	public long 开始时间() {
		return value.getStartTime();
	}

	/**
	 * 持续时间，单位为毫秒。
	 *
	 * @return 持续时间，单位为毫秒。
	 */
	@SimpleProperty
	public long 持续时间() {
		return value.getDuration();
	}

	/**
	 * 是否在下一次派发前保留触摸，需要 API 26 及以上。
	 *
	 * @return 是否在下一次派发前保留触摸，需要 API 26 及以上。
	 */
	@SimpleProperty
	public boolean 继续触摸() {
		AccessibilitySupport.api(26, "续接状态");
		return value.willContinue();
	}

	/* 方法 */

	/**
	 * 创建手势笔画。
	 *
	 * @param path 已设置起点的单轮廓路径
	 * @param start 相对该次手势的非负毫秒偏移
	 * @param duration 正数毫秒，结束时间不能超过时限
	 * @return 新的笔画
	 */
	@SimpleFunction
	public static 无障碍笔画 创建(无障碍手势路径 path, long start, long duration) {
		AccessibilitySupport.notNull(path, "路径");
		path.check();
		AccessibilitySupport.duration(start, duration);
		return new 无障碍笔画(new GestureDescription.StrokeDescription(new Path(path.value), start, duration));
	}

	/**
	 * 创建手势笔画。需要 API 26 及以上。
	 *
	 * @param path 已设置起点的单轮廓路径
	 * @param start 相对该次手势的非负毫秒偏移
	 * @param duration 正数毫秒，结束时间不能超过时限
	 * @param willContinue 下一次派发续接笔画前是否保持触摸
	 * @return 新的笔画
	 */
	@SimpleFunction
	public static 无障碍笔画 创建可续接(无障碍手势路径 path, long start, long duration, boolean willContinue) {
		AccessibilitySupport.api(26, "续接笔画");
		AccessibilitySupport.notNull(path, "路径");
		path.check();
		AccessibilitySupport.duration(start, duration);
		return new 无障碍笔画(new GestureDescription.StrokeDescription(new Path(path.value), start, duration, willContinue));
	}

	/**
	 * 从已声明保持触摸的笔画创建续接笔画；新起点须等于原终点，需要 API 26 及以上。
	 *
	 * @param path 已设置起点的单轮廓路径
	 * @param start 相对该次手势的非负毫秒偏移
	 * @param duration 正数毫秒，结束时间不能超过时限
	 * @param willContinue 是否继续保持触摸
	 * @return 下一次派发使用的笔画
	 */
	@SimpleFunction
	public 无障碍笔画 续接(无障碍手势路径 path, long start, long duration, boolean willContinue) {
		AccessibilitySupport.api(26, "续接笔画");
		AccessibilitySupport.notNull(path, "路径");
		path.check();
		AccessibilitySupport.duration(start, duration);
		return new 无障碍笔画(value.continueStroke(new Path(path.value), start, duration, willContinue));
	}
}
