package simple.library.wsd.citypicker.internal;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import simple.library.wsd.citypicker.R;

/**
 * CityPicker 的字母分组及悬浮组头绘制，搜索切换和列表预布局时校验位置。
 *
 * @author xuexiang
 * @author 树先生 xhwsd@qq.com
 */
final class SectionDecoration extends RecyclerView.ItemDecoration {

	private final CityAdapter adapter;
	private final Paint background = new Paint();
	private final Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
	private final int height;
	private final int padding;

	/**
	 * 按 Dialog 主题准备分组样式。
	 *
	 * @param context 主题上下文。
	 * @param adapter 当前城市列表适配器。
	 */
	SectionDecoration(Context context, CityAdapter adapter) {
		this.adapter = adapter;
		height = ThemeValues.dimension(context, R.attr.cpSectionHeight, 48);
		padding = context.getResources().getDimensionPixelSize(R.dimen.cp_default_padding);
		background.setColor(ThemeValues.color(context, R.attr.cpSectionBackground, 0xffeeeeee));
		text.setColor(ThemeValues.color(context, R.attr.cpSectionTextColor, 0xff777777));
		text.setTextSize(ThemeValues.dimension(context, R.attr.cpSectionTextSize, 24));
	}

	@Override
	public void getItemOffsets(@NonNull Rect output, @NonNull View view,
			@NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
		int position = parent.getChildAdapterPosition(view);
		if (valid(position) && startsSection(position)) {
			output.top = height;
		}
	}

	@Override
	public void onDraw(@NonNull Canvas canvas, @NonNull RecyclerView parent,
			@NonNull RecyclerView.State state) {
		for (int i = 0; i < parent.getChildCount(); i++) {
			View view = parent.getChildAt(i);
			int position = parent.getChildAdapterPosition(view);
			if (valid(position) && startsSection(position)) {
				draw(canvas, parent, adapter.section(position), view.getTop() - height);
			}
		}
	}

	@Override
	public void onDrawOver(@NonNull Canvas canvas, @NonNull RecyclerView parent,
			@NonNull RecyclerView.State state) {
		LinearLayoutManager manager = (LinearLayoutManager) parent.getLayoutManager();
		if (manager == null) {
			return;
		}
		int position = manager.findFirstVisibleItemPosition();
		if (!valid(position)) {
			return;
		}

		// 下一分组到达顶部时，将旧的悬浮组头推离，避免文字重叠。
		int top = parent.getPaddingTop();
		for (int i = 0; i < parent.getChildCount(); i++) {
			View view = parent.getChildAt(i);
			int next = parent.getChildAdapterPosition(view);
			if (valid(next) && next > position && startsSection(next)) {
				top = Math.min(top, view.getTop() - 2 * height);
				break;
			}
		}
		draw(canvas, parent, adapter.section(position), top);
	}

	/**
	 * 判断位置仍属于当前适配器快照。
	 *
	 * @param position 可能来自预布局的行位置。
	 * @return 是否可读取该行。
	 */
	private boolean valid(int position) {
		return position >= 0 && position < adapter.getItemCount();
	}

	/**
	 * 判断当前行是否开启新分组。
	 *
	 * @param position 有效行位置。
	 * @return 是否需要预留分组标题高度。
	 */
	private boolean startsSection(int position) {
		return position == 0 || !adapter.section(position).equals(adapter.section(position - 1));
	}

	/**
	 * 统一绘制普通组头和悬浮组头。
	 *
	 * @param canvas 目标画布。
	 * @param parent 城市列表。
	 * @param section 分组文本。
	 * @param top 组头顶部像素位置。
	 */
	private void draw(Canvas canvas, RecyclerView parent, String section, int top) {
		canvas.drawRect(parent.getPaddingLeft(), top,
				parent.getWidth() - parent.getPaddingRight(), top + height, background);
		Paint.FontMetrics metrics = text.getFontMetrics();
		canvas.drawText(section, parent.getPaddingLeft() + padding,
				top + height / 2f - (metrics.ascent + metrics.descent) / 2, text);
	}
}
