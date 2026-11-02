package simple.library.wsd.iconics;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.view.View;
import android.widget.TextView;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.internal.CheckedCompoundIconicsDrawables;

/**
 * 管理绝对或相对四侧附图，保留各侧常态和选中态。
 *
 * @author 树先生 xhwsd@qq.com
 */
final class ViewIcons {
	/**
	 * 工具不创建实例。
	 */
	private ViewIcons() {}
	/**
	 * 获取目标状态，首次读取实际已有附图。
	 *
	 * @param view 文字视图。
	 * @return 视图私有状态。
	 */
	private static State state(TextView view) {
		Object old = view.getTag(R.id.es4a_iconics_slots);
		if (old instanceof State) {
			return (State) old;
		}
		State s = new State();
		Drawable[] current = view.getCompoundDrawables();
		System.arraycopy(current, 0, s.normal, 0, 4);
		if (view instanceof CheckedCompoundIconicsDrawables) {
			CheckedCompoundIconicsDrawables c = (CheckedCompoundIconicsDrawables) view;
			Drawable[] a = {
				c.getCheckedIconicsDrawableStart(),
				c.getCheckedIconicsDrawableTop(),
				c.getCheckedIconicsDrawableEnd(),
				c.getCheckedIconicsDrawableBottom()
			};
			if (view.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
				Drawable x = a[0];
				a[0] = a[2];
				a[2] = x;
			}
			System.arraycopy(a, 0, s.checked, 0, 4);
		}
		view.setTag(R.id.es4a_iconics_slots, s);
		return s;
	}
	/**
	 * 校验位置并转换坐标系，转换时保持当前视觉位置。
	 *
	 * @param view 文字视图。
	 * @param s 图标状态。
	 * @param position 0左1上2右3下4起始5结束。
	 * @return 四侧数组位置。
	 */
	private static int slot(TextView view, State s, int position) {
		if (position < 0 || position > 5) {
			throw new 图标错误("附图位置须为0至5");
		}
		boolean relative = (position == 1 || position == 3) ? s.relative : position >= 4;
		if (relative != s.relative) {
			if (view.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
				Drawable x = s.normal[0];
				s.normal[0] = s.normal[2];
				s.normal[2] = x;
				x = s.checked[0];
				s.checked[0] = s.checked[2];
				s.checked[2] = x;
			}
			s.relative = relative;
		}
		return position == 4 ? 0 : position == 5 ? 2 : position;
	}
	/**
	 * 读取普通或选中附图，不改变坐标系。
	 *
	 * @param view 目标。
	 * @param position 图标位置。
	 * @param checked 是否读取选中态。
	 * @return 独立图标；无字体图标时为空。
	 */
	static 图标图像 get(TextView view, int position, boolean checked) {
		if (position < 0 || position > 5) {
			throw new 图标错误("附图位置须为0至5");
		}
		State s = state(view);
		int i = position == 4 ? 0 : position == 5 ? 2 : position;
		if ((position >= 4) != s.relative
				&& view.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL
				&& (i == 0 || i == 2)) {
			i = 2 - i;
		}
		return 图标图像.wrap((checked ? s.checked : s.normal)[i]);
	}
	/**
	 * 写入一个位置的图标；空引用表示清除该侧。
	 *
	 * @param view 目标。
	 * @param position 位置。
	 * @param image 图像。
	 * @param checked 是否写入选中态。
	 */
	static void set(TextView view, int position, 图标图像 image, boolean checked) {
		if (image != null) {
			image.check();
		}
		State s = state(view);
		int i = slot(view, s, position);
		(checked ? s.checked : s.normal)[i] = image == null ? null : Support.copy(image.value);
		render(view, s);
	}
	/**
	 * 设置四侧统一图标，空引用清除所有对应状态。
	 *
	 * @param view 目标。
	 * @param image 图像。
	 * @param checked 选中态。
	 */
	static void all(TextView view, 图标图像 image, boolean checked) {
		if (image != null) {
			image.check();
		}
		State s = state(view);
		for (int i = 0; i < 4; i++) {
			(checked ? s.checked : s.normal)[i] = image == null ? null : Support.copy(image.value);
		}
		render(view, s);
	}
	/**
	 * 同时保持普通和选中图标；状态改变由 Android 刷新。
	 *
	 * @param view 目标。
	 * @param s 状态。
	 */
	private static void render(TextView view, State s) {
		Drawable[] out = new Drawable[4];
		for (int i = 0; i < 4; i++) {
			Drawable normal = s.normal[i], checked = s.checked[i];
			if (normal instanceof IconicsDrawable) {
				Support.prepareShadow(view, (IconicsDrawable) normal);
			}
			if (checked instanceof IconicsDrawable) {
				Support.prepareShadow(view, (IconicsDrawable) checked);
			}
			if (checked != null) {
				StateListDrawable list = new StateListDrawable();
				list.addState(new int[] {android.R.attr.state_checked}, checked);
				list.addState(
						new int[0],
						normal == null ? new android.graphics.drawable.ColorDrawable(0) : normal);
				out[i] = list;
			} else {
				out[i] = normal;
			}
			if (out[i] != null) {
				out[i].setBounds(
						0,
						0,
						Math.max(0, out[i].getIntrinsicWidth()),
						Math.max(0, out[i].getIntrinsicHeight()));
			}
		}
		if (s.relative) {
			view.setCompoundDrawablesRelative(out[0], out[1], out[2], out[3]);
		} else {
			view.setCompoundDrawables(out[0], out[1], out[2], out[3]);
		}
	}
	/**
	 * 私有图标状态，与视图共同释放。
	 */
	private static final class State {
		final Drawable[] normal = new Drawable[4];
		final Drawable[] checked = new Drawable[4];
		boolean relative;
		/**
		 * 创建空状态。
		 */
		State() {}
	}
}
