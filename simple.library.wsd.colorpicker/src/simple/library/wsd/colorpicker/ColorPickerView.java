package simple.library.wsd.colorpicker;

import java.util.Locale;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.os.Build;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;

/**
 * 色环、黑白渐变条和透明棋盘预览，按实际可用宽度计算绘制与触摸区域。
 *
 * <p>色环分段插色及黑白渐变交互参考 msdx 的 ColorPickerDialog；
 * 布局、触摸边界和键盘操作按当前对话框重新组织，不依赖屏幕固定像素或运行时全局函数。
 *
 * @author msdx 原色环与渐变交互
 * @author 树先生 xhwsd@qq.com
 */
final class ColorPickerView extends View {

	/**
	 * 接收草稿颜色变化，不代表用户已经确认。
	 */
	interface Listener {

		/**
		 * 通知视图内的当前草稿颜色。
		 *
		 * @param color 保留初始透明度的 ARGB 颜色。
		 */
		void onColorChanged(int color);
	}

	/* 色环首尾颜色相同，使角度起点处平滑衔接。 */

	private static final int[] WHEEL_COLORS = {
		0xFFFF0000, 0xFFFF00FF, 0xFF0000FF, 0xFF00FFFF,
		0xFF00FF00, 0xFFFFFF00, 0xFFFF0000
	};

	/* 绘制资源与回调；透明度在单次选色期间固定。 */

	private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private final RectF bar = new RectF();
	private final Path clip = new Path();
	private final Listener listener;
	private final int alpha;

	/* 草稿与选色位置；shade 中点对应色环选出的基础色。 */

	private int color;
	private int baseColor;
	private float unit;
	private float shade = 0.5f;

	/* 实际尺寸确定后计算的绘制区域与渐变。 */

	private float centerX;
	private float centerY;
	private float radius;
	private float stroke;
	private Shader wheelShader;
	private Shader barShader;

	// 0 表示未捕获手势，1 表示色环，2 表示明暗条。
	private int tracking;

	/**
	 * 创建选色视图，保留初始颜色及透明度。
	 *
	 * @param context 对话框使用的宿主上下文。
	 * @param initial 初始 ARGB 颜色。
	 * @param changeListener 草稿变化监听，不得为空引用。
	 */
	ColorPickerView(Context context, int initial, Listener changeListener) {
		super(context);

		listener = changeListener;
		color = initial;
		alpha = Color.alpha(initial);
		baseColor = initial | 0xFF000000;

		// 用色相定位标记，草稿保留传入的原始颜色。
		float[] hsv = new float[3];
		Color.colorToHSV(initial, hsv);
		unit = (1 - hsv[0] / 360) % 1;

		setFocusable(true);
		setClickable(true);
		if (Build.VERSION.SDK_INT < 18) {
			// 旧系统的硬件画布不支持 clipPath，圆形棋盘预览使用软件画布。
			setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		}

		updateDescription();
	}

	/**
	 * 读取当前草稿，不执行确认或提交。
	 *
	 * @return 当前 ARGB 颜色。
	 */
	int getColor() {
		return color;
	}

	/* 自适应布局；矮窗口由外层滚动容器保留完整操作空间。 */

	/**
	 * 按可用宽度测量选色区域，矮窗口由外层滚动容器承载。
	 *
	 * @param widthSpec 父容器给出的宽度测量约束。
	 * @param heightSpec 父容器给出的高度测量约束。
	 */
	@Override
	protected void onMeasure(int widthSpec, int heightSpec) {
		int width = resolveSize(dp(280), widthSpec);
		int height = resolveSize(Math.min(dp(300), width + dp(16)), heightSpec);
		setMeasuredDimension(width, height);
	}

	/**
	 * 尺寸变化后重算色环、明暗条及对应渐变的位置。
	 *
	 * @param width 当前宽度，单位为像素。
	 * @param height 当前高度，单位为像素。
	 * @param oldWidth 变化前的宽度，单位为像素。
	 * @param oldHeight 变化前的高度，单位为像素。
	 */
	@Override
	protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
		stroke = dp(24);
		float diameter = Math.min(width - dp(32), height - dp(80));
		radius = Math.max(dp(24), (diameter - stroke) / 2);
		centerX = width / 2f;
		centerY = dp(8) + radius + stroke / 2;
		bar.set(dp(16), centerY + radius + stroke / 2 + dp(28),
			width - dp(16), centerY + radius + stroke / 2 + dp(60));

		wheelShader = new SweepGradient(centerX, centerY, WHEEL_COLORS, null);
		updateBarShader();
	}

	/* 色环、棋盘预览和当前位置标记。 */

	/**
	 * 绘制色环、透明预览、明暗条和选色位置标记。
	 *
	 * @param canvas 当前视图的绘制画布。
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		// 色环保持不透明，透明效果单独在中心预览。
		paint.setShader(wheelShader);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(stroke);
		canvas.drawCircle(centerX, centerY, radius, paint);

		// 棋盘格限制在圆形预览内，用于辨认半透明颜色。
		paint.setShader(null);
		float previewRadius = radius * 0.53f;
		clip.reset();
		clip.addCircle(centerX, centerY, previewRadius, Path.Direction.CW);
		int save = canvas.save();
		canvas.clipPath(clip);
		paint.setStyle(Paint.Style.FILL);
		int tile = dp(8);
		for (int y = 0; y * tile < previewRadius * 2 + tile; y++) {
			for (int x = 0; x * tile < previewRadius * 2 + tile; x++) {
				paint.setColor((x + y) % 2 == 0 ? Color.WHITE : 0xFFD9DDE2);
				float left = centerX - previewRadius + x * tile;
				float top = centerY - previewRadius + y * tile;
				canvas.drawRect(left, top, left + tile, top + tile, paint);
			}
		}

		paint.setColor(color);
		canvas.drawCircle(centerX, centerY, previewRadius, paint);
		canvas.restoreToCount(save);
		outline(canvas, centerX, centerY, previewRadius, 0xFF9298A1, dp(1));

		// 明暗条从黑色过渡到基础色和白色。
		paint.setStyle(Paint.Style.FILL);
		paint.setShader(barShader);
		canvas.drawRoundRect(bar, dp(6), dp(6), paint);
		paint.setShader(null);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(dp(1));
		paint.setColor(0xFF9298A1);
		canvas.drawRoundRect(bar, dp(6), dp(6), paint);

		// 黑白双描边使标记在深色和浅色背景上都可辨认。
		double angle = unit * Math.PI * 2;
		float wheelX = centerX + (float) Math.cos(angle) * radius;
		float wheelY = centerY + (float) Math.sin(angle) * radius;
		marker(canvas, wheelX, wheelY, dp(7));
		marker(canvas, bar.left + bar.width() * shade, bar.centerY(), dp(9));

		if (isFocused()) {
			paint.setStyle(Paint.Style.STROKE);
			paint.setStrokeWidth(dp(2));
			paint.setColor(0xFF3689D9);
			canvas.drawRect(dp(2), dp(2), getWidth() - dp(2), getHeight() - dp(2), paint);
		}
	}

	/**
	 * 绘制圆形轮廓，供预览边框和位置标记复用。
	 *
	 * @param canvas 当前绘制画布。
	 * @param x 圆心横坐标，单位为像素。
	 * @param y 圆心纵坐标，单位为像素。
	 * @param size 圆半径，单位为像素。
	 * @param lineColor 轮廓颜色。
	 * @param lineWidth 描边宽度，单位为像素。
	 */
	private void outline(Canvas canvas, float x, float y, float size, int lineColor, float lineWidth) {
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(lineWidth);
		paint.setColor(lineColor);
		canvas.drawCircle(x, y, size, paint);
	}

	/**
	 * 通过黑白双描边绘制当前位置标记。
	 *
	 * @param canvas 当前绘制画布。
	 * @param x 标记中心横坐标，单位为像素。
	 * @param y 标记中心纵坐标，单位为像素。
	 * @param size 标记半径，单位为像素。
	 */
	private void marker(Canvas canvas, float x, float y, float size) {
		outline(canvas, x, y, size, 0xAA000000, dp(4));
		outline(canvas, x, y, size, Color.WHITE, dp(2));
	}

	/* 触摸只捕获色环或渐变条，外侧手势仍可滚动对话框。 */

	/**
	 * 捕获色环或明暗条上的手势，更新草稿并协调外层滚动。
	 *
	 * @param event 当前触摸事件。
	 * @return 正在处理选色手势时为真，其余位置不捕获。
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float x = event.getX();
		float y = event.getY();
		int action = event.getActionMasked();

		// 按下时确定操作区域，后续拖动允许越过区域边缘。
		if (action == MotionEvent.ACTION_DOWN) {
			float distance = (float) Math.hypot(x - centerX, y - centerY);
			if (Math.abs(distance - radius) <= stroke / 2 + dp(10)) {
				tracking = 1;
			} else if (x >= bar.left - dp(10) && x <= bar.right + dp(10)
				&& y >= bar.top - dp(10) && y <= bar.bottom + dp(10)) {
				tracking = 2;
			} else {
				return false;
			}
			getParent().requestDisallowInterceptTouchEvent(true);
		}

		if (tracking == 0) {
			return false;
		}

		// 坐标更新草稿，明暗位置会限制在渐变条两端。
		if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE
			|| action == MotionEvent.ACTION_UP) {
			if (tracking == 1) {
				float angle = (float) (Math.atan2(y - centerY, x - centerX) / (Math.PI * 2));
				setHue(angle < 0 ? angle + 1 : angle);
			} else {
				setShade((x - bar.left) / bar.width());
			}
		}

		// 结束后归还滚动拦截权，取消手势不发送点击反馈。
		if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
			tracking = 0;
			getParent().requestDisallowInterceptTouchEvent(false);
			if (action == MotionEvent.ACTION_UP) {
				performClick();
			}
		}

		return true;
	}

	/**
	 * 发送选中反馈，保留标准点击和辅助功能行为。
	 *
	 * @return 始终为真，表示已处理点击。
	 */
	@Override
	public boolean performClick() {
		super.performClick();
		sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_SELECTED);
		return true;
	}

	/**
	 * 左右键调整色相，上下键调整明暗，其余按键交给父类。
	 *
	 * @param keyCode 当前按键代码。
	 * @param event 当前按键事件。
	 * @return 选色方向键返回真，其余按键使用父类处理结果。
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
			setHue((unit + (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT ? 1 : -1) / 36f + 1) % 1);
			return true;
		}

		if (keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
			setShade(shade + (keyCode == KeyEvent.KEYCODE_DPAD_UP ? 0.05f : -0.05f));
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	/**
	 * 焦点变化后重绘，用边框提示键盘当前操作区域。
	 *
	 * @param focused 当前是否获得焦点。
	 * @param direction 焦点进入或离开的方向。
	 * @param previous 前一个焦点区域，可以为空引用。
	 */
	@Override
	protected void onFocusChanged(boolean focused, int direction, Rect previous) {
		super.onFocusChanged(focused, direction, previous);
		invalidate();
	}

	/* 分段 RGB 插值保持原有色环和黑白明暗条的交互。 */

	/**
	 * 在色环相邻颜色间插值，并将明暗位置重置到基础色。
	 *
	 * @param value 沿色环顺时针方向归一化的角度，范围为 0 到 1。
	 */
	private void setHue(float value) {
		unit = value;
		float position = unit * (WHEEL_COLORS.length - 1);
		int segment = Math.min((int) position, WHEEL_COLORS.length - 2);
		baseColor = blend(WHEEL_COLORS[segment], WHEEL_COLORS[segment + 1], position - segment);

		shade = 0.5f;
		updateBarShader();
		publish(baseColor);
	}

	/**
	 * 在黑色、基础色和白色之间选择明暗。
	 *
	 * @param value 明暗位置；超出 0 到 1 的值取最近端点。
	 */
	private void setShade(float value) {
		shade = Math.max(0, Math.min(1, value));
		publish(shade <= 0.5f ? blend(Color.BLACK, baseColor, shade * 2)
			: blend(baseColor, Color.WHITE, shade * 2 - 1));
	}

	/**
	 * 合并固定透明度后更新草稿、预览和辅助说明，再通知外层数值显示。
	 *
	 * @param rgb 插值得到的颜色，只使用 RGB 分量。
	 */
	private void publish(int rgb) {
		color = (alpha << 24) | (rgb & 0x00FFFFFF);
		updateDescription();
		invalidate();

		listener.onColorChanged(color);
	}

	/**
	 * 更新辅助功能描述，包含当前色值及键盘操作方式。
	 */
	private void updateDescription() {
		setContentDescription(String.format(Locale.ROOT,
			"颜色选取区域，当前 #%08X；左右键调整色相，上下键调整明暗", color));
	}

	/**
	 * 根据当前基础色和渐变条边界重建明暗渐变。
	 */
	private void updateBarShader() {
		barShader = new LinearGradient(bar.left, bar.top, bar.right, bar.top,
			new int[] {Color.BLACK, baseColor, Color.WHITE}, null, Shader.TileMode.CLAMP);
	}

	/**
	 * 按 RGB 分量线性插值；透明度由发布草稿时统一恢复。
	 *
	 * @param from 起始颜色。
	 * @param to 结束颜色。
	 * @param progress 插值比例，调用方保证范围为 0 到 1。
	 * @return 插值后的不透明 RGB 颜色。
	 */
	private static int blend(int from, int to, float progress) {
		int red = Color.red(from) + Math.round(progress * (Color.red(to) - Color.red(from)));
		int green = Color.green(from) + Math.round(progress * (Color.green(to) - Color.green(from)));
		int blue = Color.blue(from) + Math.round(progress * (Color.blue(to) - Color.blue(from)));
		return Color.rgb(red, green, blue);
	}

	/**
	 * 按显示密度换算绘制尺寸，并保证至少占一个像素。
	 *
	 * @param value DIP 尺寸。
	 * @return 四舍五入后且不小于 1 的像素尺寸。
	 */
	private int dp(int value) {
		return Math.max(1, (int) (value * getResources().getDisplayMetrics().density + 0.5f));
	}
}
