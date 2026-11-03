package simple.library.wsd.accessibility;

import android.graphics.Rect;

import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.SimpleProperty;

/**
 * 屏幕像素坐标中的矩形快照；不随节点后续移动自动变化。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class 无障碍边界 {

	/**
	 * 独立的矩形快照，不共享调用者的可变矩形。
	 */
	private final Rect bounds;

	/**
	 * 复制 Android 矩形，避免与调用者共享可变对象。
	 *
	 * @param bounds 要复制的像素边界
	 */
	无障碍边界(Rect bounds) {
		this.bounds = new Rect(bounds);
	}

	/* 属性 */

	/**
	 * 左边缘像素坐标。
	 *
	 * @return 左边缘像素坐标。
	 */
	@SimpleProperty
	public int 左边() {
		return bounds.left;
	}

	/**
	 * 上边缘像素坐标。
	 *
	 * @return 上边缘像素坐标。
	 */
	@SimpleProperty
	public int 上边() {
		return bounds.top;
	}

	/**
	 * 右边缘像素坐标，不包含该边缘。
	 *
	 * @return 右边缘像素坐标，不包含该边缘。
	 */
	@SimpleProperty
	public int 右边() {
		return bounds.right;
	}

	/**
	 * 下边缘像素坐标，不包含该边缘。
	 *
	 * @return 下边缘像素坐标，不包含该边缘。
	 */
	@SimpleProperty
	public int 下边() {
		return bounds.bottom;
	}

	/**
	 * 矩形像素宽度。
	 *
	 * @return 矩形像素宽度。
	 */
	@SimpleProperty
	public int 宽度() {
		return bounds.width();
	}

	/**
	 * 矩形像素高度。
	 *
	 * @return 矩形像素高度。
	 */
	@SimpleProperty
	public int 高度() {
		return bounds.height();
	}

	/**
	 * 水平中心像素坐标。
	 *
	 * @return 水平中心像素坐标。
	 */
	@SimpleProperty
	public int 中心X() {
		return bounds.centerX();
	}

	/**
	 * 垂直中心像素坐标。
	 *
	 * @return 垂直中心像素坐标。
	 */
	@SimpleProperty
	public int 中心Y() {
		return bounds.centerY();
	}
}
