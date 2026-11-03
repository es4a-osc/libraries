package simple.library.wsd.accessibility;

import android.graphics.Path;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * 以屏幕像素构造单轮廓笔画。先设置起点，再添加线段或曲线；坐标须为非负有限数。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class 无障碍手势路径 {

	/**
	 * 按屏幕像素记录单轮廓手势路径。
	 */
	final Path value = new Path();

	/**
	 * 路径是否已经建立唯一轮廓的起点。
	 */
	private boolean started;

	/**
	 * 创建空手势路径。
	 */
	public 无障碍手势路径() {
	}

	/**
	 * 拒绝没有起点的路径。
	 */
	void check() {
		if (!started) {
			throw new IllegalStateException("请先设置路径起点");
		}
	}

	/* 方法 */

	/**
	 * 清除既有路径并设置起点；只有起点的路径可用于点击。
	 *
	 * @param x 目标屏幕像素横坐标
	 * @param y 目标屏幕像素纵坐标
	 */
	@SimpleFunction
	public void 设置起点(float x, float y) {
		AccessibilitySupport.coordinate(x, y);
		value.reset();
		value.moveTo(x, y);
		started = true;
	}

	/**
	 * 从当前点添加直线。
	 *
	 * @param x 目标屏幕像素横坐标
	 * @param y 目标屏幕像素纵坐标
	 */
	@SimpleFunction
	public void 添加直线(float x, float y) {
		check();
		AccessibilitySupport.coordinate(x, y);
		value.lineTo(x, y);
	}

	/**
	 * 添加三次贝塞尔曲线，单位为屏幕像素。
	 *
	 * @param x1 第一控制点横坐标
	 * @param y1 第一控制点纵坐标
	 * @param x2 第二控制点横坐标
	 * @param y2 第二控制点纵坐标
	 * @param x 目标屏幕像素横坐标
	 * @param y 目标屏幕像素纵坐标
	 */
	@SimpleFunction
	public void 添加曲线(float x1, float y1, float x2, float y2, float x, float y) {
		check();
		AccessibilitySupport.coordinate(x1, y1);
		AccessibilitySupport.coordinate(x2, y2);
		AccessibilitySupport.coordinate(x, y);
		value.cubicTo(x1, y1, x2, y2, x, y);
	}
}
