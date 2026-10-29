package simple.library.wsd.xutil;

import android.graphics.PointF;

import com.xuexiang.xutil.data.GeometryUtils;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * 二维点的距离、中点和插值。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class XUtil几何 {

	/**
	 * 函数集仅提供静态成员，不创建实例。
	 */
	private XUtil几何() {
	}

	/**
	 * 计算两个二维点之间的直线距离。
	 *
	 * @param x1 起点X。
	 * @param y1 起点Y。
	 * @param x2 终点X。
	 * @param y2 终点Y。
	 * @return 返回的单精度数值。
	 */
	@SimpleFunction
	public static float 距离(float x1, float y1, float x2, float y2) {
		return GeometryUtils.getDistanceBetween2Points(new PointF(x1, y1), new PointF(x2, y2));
	}

	/**
	 * 返回包含 X、Y 的两个元素数组。
	 *
	 * @param x1 起点X。
	 * @param y1 起点Y。
	 * @param x2 终点X。
	 * @param y2 终点Y。
	 * @return 按 X、Y 顺序保存的坐标。
	 */
	@SimpleFunction
	public static float[] 中点(float x1, float y1, float x2, float y2) {
		PointF point = GeometryUtils.getMiddlePoint(new PointF(x1, y1), new PointF(x2, y2));
		return new float[] { point.x, point.y };
	}

	/**
	 * 返回包含 X、Y 的两个元素数组。
	 *
	 * @param x1 起点X。
	 * @param y1 起点Y。
	 * @param x2 终点X。
	 * @param y2 终点Y。
	 * @param fraction 0 为起点，1 为终点，范围外表示延长线。
	 * @return 按 X、Y 顺序保存的坐标。
	 */
	@SimpleFunction
	public static float[] 插值(float x1, float y1, float x2, float y2, float fraction) {
		PointF point = GeometryUtils.getPointByPercent(new PointF(x1, y1), new PointF(x2, y2), fraction);
		return new float[] { point.x, point.y };
	}
}
