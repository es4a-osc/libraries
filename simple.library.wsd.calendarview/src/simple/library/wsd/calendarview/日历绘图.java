package simple.library.wsd.calendarview;

import android.graphics.*;
import android.os.Looper;
import simple.runtime.annotations.*;
import simple.library.wsd.calendarview.internal.ImagePool;

/**
 * 同步绘制回调专用的局部画布，原点为本单元左上角，尺寸以 DIP 表示。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class 日历绘图 {
	private Canvas canvas;
	private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
	private ImagePool images;
	private float left;
	private float top;
	private float width;
	private float height;
	private float fontScale;
	private int baseSave;
	private int saves;

	/**
	 * 创建无效上下文，实际绘图对象由日历绘制事件提供。
	 */
	public 日历绘图() {
	}

	/**
	 * 为一个回调创建裁剪到本单元的局部画布。
	 *
	 * @param target 原生画布。
	 * @param pool 图片集合。
	 * @param x 单元左边像素。
	 * @param y 单元顶边像素。
	 * @param w 单元宽像素。
	 * @param h 单元高像素。
	 * @param density DIP 比例。
	 * @param scaledDensity SP 比例。
	 * @return 仅本回调有效的上下文。
	 */
	public static 日历绘图 begin(Canvas target, ImagePool pool, float x, float y, float w, float h, float density, float scaledDensity) {
		日历绘图 drawing = new 日历绘图();
		drawing.canvas = target;
		drawing.images = pool;
		drawing.left = x / density;
		drawing.top = y / density;
		drawing.width = w / density;
		drawing.height = h / density;
		drawing.fontScale = scaledDensity / density;
		drawing.baseSave = target.save();
		target.translate(x, y);
		target.scale(density, density);
		target.clipRect(0, 0, drawing.width, drawing.height);
		return drawing;
	}

	/**
	 * 恢复进入回调前的画布并立即失效。
	 */
	public void end() {
		if (canvas != null) {
			canvas.restoreToCount(baseSave);
			canvas = null;
			images = null;
		}
	}

	/**
	 * 检查本次同步回调是否仍有效。
	 */
	private void check() {
		if (canvas == null || Looper.myLooper() != Looper.getMainLooper()) {
			throw new simple.library.wsd.calendarview.日历错误("绘图对象仅在所属的同步绘制回调中有效");
		}
	}

	/**
	 * 统一准备当前绘制画笔。
	 *
	 * @param color ARGB 颜色。
	 * @param fill 是否填充。
	 * @param lineWidth 描边 DIP 宽度。
	 */
	private void prepare(int color, boolean fill, float lineWidth) {
		check();
		finite(lineWidth);
		if (lineWidth < 0) {
			throw new IllegalArgumentException("线宽不能为负");
		}
		paint.setColor(color);
		paint.setStyle(fill ? Paint.Style.FILL : Paint.Style.STROKE);
		paint.setStrokeWidth(lineWidth);
	}

	/**
	 * 拒绝非有限的绘制坐标。
	 *
	 * @param values 坐标值。
	 */
	private static void finite(float... values) {
		for (float value : values) {
			if (!Float.isFinite(value)) {
				throw new IllegalArgumentException("绘图参数不能为非有限数");
			}
		}
	}

	/**
	 * 回调是否仍在进行；此属性可在回调外读取。
	 *
	 * @return 回调是否仍在进行；此属性可在回调外读取。
	 */
	@SimpleProperty
	public boolean 有效() {
		return canvas != null;
	}

	/**
	 * 单元在所属视图中的左边位置，DIP。
	 *
	 * @return 单元在所属视图中的左边位置，DIP。
	 */
	@SimpleProperty
	public float 左边() {
		check();
		return left;
	}

	/**
	 * 单元在所属视图中的顶边位置，DIP。
	 *
	 * @return 单元在所属视图中的顶边位置，DIP。
	 */
	@SimpleProperty
	public float 顶边() {
		check();
		return top;
	}

	/**
	 * 单元宽度，DIP。
	 *
	 * @return 单元宽度，DIP。
	 */
	@SimpleProperty
	public float 宽度() {
		check();
		return width;
	}

	/**
	 * 单元高度，DIP。
	 *
	 * @return 单元高度，DIP。
	 */
	@SimpleProperty
	public float 高度() {
		check();
		return height;
	}

	/**
	 * 绘制线段，坐标及线宽用 DIP。
	 *
	 * @param x1 起点横坐标。
	 * @param y1 起点纵坐标。
	 * @param x2 终点横坐标。
	 * @param y2 终点纵坐标。
	 * @param color 颜色。
	 * @param lineWidth 线宽。
	 */
	@SimpleFunction
	public void 画线(float x1, float y1, float x2, float y2, int color, float lineWidth) {
		finite(x1, y1, x2, y2);
		prepare(color, false, lineWidth);
		canvas.drawLine(x1, y1, x2, y2, paint);
	}

	/**
	 * 绘制矩形，坐标及尺寸用 DIP。
	 *
	 * @param x 左边。
	 * @param y 顶边。
	 * @param w 宽。
	 * @param h 高。
	 * @param color 颜色。
	 * @param fill 填充。
	 * @param lineWidth 线宽。
	 */
	@SimpleFunction
	public void 画矩形(float x, float y, float w, float h, int color, boolean fill, float lineWidth) {
		finite(x, y, w, h);
		prepare(color, fill, lineWidth);
		canvas.drawRect(x, y, x + w, y + h, paint);
	}

	/**
	 * 绘制圆角矩形，坐标、尺寸及圆角用 DIP。
	 *
	 * @param x 左边。
	 * @param y 顶边。
	 * @param w 宽。
	 * @param h 高。
	 * @param radius 圆角半径。
	 * @param color 颜色。
	 * @param fill 填充。
	 * @param lineWidth 线宽。
	 */
	@SimpleFunction
	public void 画圆角矩形(float x, float y, float w, float h, float radius, int color, boolean fill, float lineWidth) {
		finite(x, y, w, h, radius);
		prepare(color, fill, lineWidth);
		canvas.drawRoundRect(new RectF(x, y, x + w, y + h), radius, radius, paint);
	}

	/**
	 * 绘制圆形，坐标及半径用 DIP。
	 *
	 * @param x 圆心横坐标。
	 * @param y 圆心纵坐标。
	 * @param radius 半径。
	 * @param color 颜色。
	 * @param fill 填充。
	 * @param lineWidth 线宽。
	 */
	@SimpleFunction
	public void 画圆(float x, float y, float radius, int color, boolean fill, float lineWidth) {
		finite(x, y, radius);
		prepare(color, fill, lineWidth);
		canvas.drawCircle(x, y, radius, paint);
	}

	/**
	 * 绘制椭圆弧，角度按顺时针度数表示。
	 *
	 * @param x 左边。
	 * @param y 顶边。
	 * @param w 宽。
	 * @param h 高。
	 * @param start 起始角度。
	 * @param sweep 经过角度。
	 * @param center 连接圆心。
	 * @param color 颜色。
	 * @param lineWidth 线宽。
	 */
	@SimpleFunction
	public void 画弧(float x, float y, float w, float h, float start, float sweep, boolean center, int color, float lineWidth) {
		finite(x, y, w, h, start, sweep);
		prepare(color, false, lineWidth);
		canvas.drawArc(new RectF(x, y, x + w, y + h), start, sweep, center, paint);
	}

	/**
	 * 绘制文字，位置用 DIP，字号用 SP；对齐 0 左、1 中、2 右，纵坐标是基线。
	 *
	 * @param text 文本。
	 * @param x 横坐标。
	 * @param baseline 基线纵坐标。
	 * @param size 字号。
	 * @param color 颜色。
	 * @param align 对齐。
	 * @param bold 粗体。
	 */
	@SimpleFunction
	public void 画文本(String text, float x, float baseline, float size, int color, int align, boolean bold) {
		finite(x, baseline, size);
		if (text == null || size <= 0 || align < 0 || align > 2) {
			throw new IllegalArgumentException("绘制文字参数无效");
		}
		prepare(color, true, 0);
		paint.setTextSize(size * fontScale);
		paint.setFakeBoldText(bold);
		paint.setTextAlign(align == 1 ? Paint.Align.CENTER : align == 2 ? Paint.Align.RIGHT : Paint.Align.LEFT);
		canvas.drawText(text, x, baseline, paint);
	}

	/**
	 * 按 SP 字号测量文字，返回 DIP 宽度。
	 *
	 * @param text 文本。
	 * @param size 字号。
	 * @param bold 粗体。
	 * @return 文本的 DIP 宽度。
	 */
	@SimpleFunction
	public float 取文本宽度(String text, float size, boolean bold) {
		check();
		finite(size);
		if (text == null || size <= 0) {
			throw new IllegalArgumentException("文字或字号无效");
		}
		paint.setTextSize(size * fontScale);
		paint.setFakeBoldText(bold);
		return paint.measureText(text);
	}

	/**
	 * 绘制本日历预载入的图片，目标位置及尺寸用 DIP。
	 *
	 * @param imageId 图片编号。
	 * @param x 左边。
	 * @param y 顶边。
	 * @param w 宽。
	 * @param h 高。
	 */
	@SimpleFunction
	public void 画图片(int imageId, float x, float y, float w, float h) {
		check();
		finite(x, y, w, h);
		canvas.drawBitmap(images.get(imageId), null, new RectF(x, y, x + w, y + h), null);
	}

	/**
	 * 保存本次回调内的平移、裁剪及缩放状态。
	 */
	@SimpleFunction
	public void 保存() {
		check();
		canvas.save();
		saves++;
	}

	/**
	 * 恢复本次回调内最近一次保存的状态；不能恢复到回调外。
	 */
	@SimpleFunction
	public void 恢复() {
		check();
		if (saves == 0) {
			throw new simple.library.wsd.calendarview.日历错误("本回调没有可恢复的保存状态");
		}
		canvas.restore();
		saves--;
	}

	/**
	 * 按 DIP 平移局部画布。
	 *
	 * @param x 横向位移。
	 * @param y 纵向位移。
	 */
	@SimpleFunction
	public void 平移(float x, float y) {
		check();
		finite(x, y);
		canvas.translate(x, y);
	}

	/**
	 * 缩放局部画布，作用仅限本回调。
	 *
	 * @param x 横向比例。
	 * @param y 纵向比例。
	 */
	@SimpleFunction
	public void 缩放(float x, float y) {
		check();
		finite(x, y);
		canvas.scale(x, y);
	}

	/**
	 * 绕局部原点按顺时针度数旋转。
	 *
	 * @param angle 角度。
	 */
	@SimpleFunction
	public void 旋转(float angle) {
		check();
		finite(angle);
		canvas.rotate(angle);
	}

	/**
	 * 继续收紧当前单元的可绘制矩形。
	 *
	 * @param x 左边。
	 * @param y 顶边。
	 * @param w 宽。
	 * @param h 高。
	 */
	@SimpleFunction
	public void 裁剪(float x, float y, float w, float h) {
		check();
		finite(x, y, w, h);
		canvas.clipRect(x, y, x + w, y + h);
	}
}
