package simple.library.wsd.citypicker.internal;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import simple.library.wsd.citypicker.R;

/**
 * CityPicker 字母索引条；以当前可用高度计算触点，键盘改变窗口尺寸后重新布局。
 *
 * @author xuexiang
 * @author 树先生 xhwsd@qq.com
 */
public final class SideIndexBar extends View {

	/**
	 * 接收用户当前触及的城市分组。
	 */
	public interface Listener {
		/**
		 * 切换到指定分组；列表中没有该组时保持当前位置。
		 *
		 * @param section 定位、热门、字母或井号。
		 */
		void selected(String section);
	}

	private static final String[] SECTIONS = {
		"定位", "热门", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K",
		"L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "#"
	};
	private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private final int normalColor;
	private final int selectedColor;
	private int selected = -1;
	private TextView overlay;
	private Listener listener;

	/**
	 * 从布局和 Dialog 主题创建字母索引条。
	 *
	 * @param context 当前主题上下文。
	 * @param attributes XML 属性。
	 */
	public SideIndexBar(Context context, AttributeSet attributes) {
		super(context, attributes);
		paint.setTextSize(ThemeValues.dimension(context, R.attr.cpIndexBarTextSize, 24));
		normalColor = ThemeValues.color(context, R.attr.cpIndexBarNormalTextColor, 0xff777777);
		selectedColor = ThemeValues.color(context, R.attr.cpIndexBarSelectedTextColor, 0xff333333);
		setContentDescription("城市字母索引");
		setClickable(true);
	}

	/**
	 * 绑定字母悬浮提示及列表滚动回调。
	 *
	 * @param overlay 显示当前字母的提示视图。
	 * @param listener 分组切换监听器。
	 */
	public void bind(TextView overlay, Listener listener) {
		this.overlay = overlay;
		this.listener = listener;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		float itemHeight = (float) getHeight() / SECTIONS.length;
		Paint.FontMetrics metrics = paint.getFontMetrics();
		for (int i = 0; i < SECTIONS.length; i++) {
			paint.setColor(i == selected ? selectedColor : normalColor);
			canvas.drawText(SECTIONS[i], (getWidth() - paint.measureText(SECTIONS[i])) / 2,
					itemHeight * (i + 0.5f) - (metrics.ascent + metrics.descent) / 2, paint);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (getHeight() <= 0) {
			return false;
		}
		int action = event.getActionMasked();
		if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
			getParent().requestDisallowInterceptTouchEvent(true);
			int index = Math.max(0, Math.min(SECTIONS.length - 1,
					(int) (event.getY() * SECTIONS.length / getHeight())));
			if (selected != index) {
				selected = index;
				if (overlay != null) {
					overlay.setText(SECTIONS[index]);
					overlay.setVisibility(VISIBLE);
				}
				if (listener != null) {
					listener.selected(SECTIONS[index]);
				}
				invalidate();
			}
		} else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
			selected = -1;
			if (overlay != null) {
				overlay.setVisibility(GONE);
			}
			if (action == MotionEvent.ACTION_UP) {
				performClick();
			}
			invalidate();
		}
		return true;
	}

	@Override
	public boolean performClick() {
		return super.performClick();
	}
}
