package simple.library.wsd.iconics;

import android.graphics.*;
import android.view.*;
import android.widget.*;
import com.mikepenz.iconics.*;
import com.mikepenz.iconics.typeface.*;
import java.util.*;
import simple.runtime.android.MainActivity;
import simple.runtime.annotations.*;
import simple.runtime.components.*;

/**
 * 文字内嵌图标样式。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class 图标文字样式 {
	private int p颜色 = Color.BLACK;
	/**
	 * 文字图标颜色。
	 *
	 * @return 当前值。
	 */
	@SimpleProperty
	public int 颜色() {
		return p颜色;
	}
	/**
	 * 文字图标颜色。
	 *
	 * @param value 新值。
	 */
	@SimpleProperty
	public void 颜色(int value) {
		p颜色 = value;
	}

	private int p背景颜色 = Color.TRANSPARENT;
	/**
	 * 文字图标背景颜色。
	 *
	 * @return 当前值。
	 */
	@SimpleProperty
	public int 背景颜色() {
		return p背景颜色;
	}
	/**
	 * 文字图标背景颜色。
	 *
	 * @param value 新值。
	 */
	@SimpleProperty
	public void 背景颜色(int value) {
		p背景颜色 = value;
	}

	private int p字号 = 0;
	/**
	 * 字号 SP；0保持原字号。
	 *
	 * @return 当前值。
	 */
	@SimpleProperty
	public int 字号() {
		return p字号;
	}
	/**
	 * 字号 SP；0保持原字号。
	 *
	 * @param value 新值。
	 */
	@SimpleProperty
	public void 字号(int value) {
		p字号 = value;
	}

	private boolean p粗体 = false;
	/**
	 * 是否加粗。
	 *
	 * @return 当前值。
	 */
	@SimpleProperty
	public boolean 粗体() {
		return p粗体;
	}
	/**
	 * 是否加粗。
	 *
	 * @param value 新值。
	 */
	@SimpleProperty
	public void 粗体(boolean value) {
		p粗体 = value;
	}

	private boolean p斜体 = false;
	/**
	 * 是否倾斜。
	 *
	 * @return 当前值。
	 */
	@SimpleProperty
	public boolean 斜体() {
		return p斜体;
	}
	/**
	 * 是否倾斜。
	 *
	 * @param value 新值。
	 */
	@SimpleProperty
	public void 斜体(boolean value) {
		p斜体 = value;
	}

	private boolean p下划线 = false;
	/**
	 * 是否带下划线。
	 *
	 * @return 当前值。
	 */
	@SimpleProperty
	public boolean 下划线() {
		return p下划线;
	}
	/**
	 * 是否带下划线。
	 *
	 * @param value 新值。
	 */
	@SimpleProperty
	public void 下划线(boolean value) {
		p下划线 = value;
	}

	private boolean p删除线 = false;
	/**
	 * 是否带删除线。
	 *
	 * @return 当前值。
	 */
	@SimpleProperty
	public boolean 删除线() {
		return p删除线;
	}
	/**
	 * 是否带删除线。
	 *
	 * @param value 新值。
	 */
	@SimpleProperty
	public void 删除线(boolean value) {
		p删除线 = value;
	}
	/**
	 * 创建独立实例。
	 */
	public 图标文字样式() {}
	/**
	 * 转换为独立字符样式。
	 *
	 * @return 字符样式数组。
	 */
	android.text.style.CharacterStyle[] spans() {
		if (p字号 < 0) {
			throw new 图标错误("字号不能为负");
		}
		List<android.text.style.CharacterStyle> s =
				new ArrayList<android.text.style.CharacterStyle>();
		s.add(new android.text.style.ForegroundColorSpan(p颜色));
		s.add(new android.text.style.BackgroundColorSpan(p背景颜色));
		if (p字号 > 0) {
			s.add(
					new android.text.style.AbsoluteSizeSpan(
							Math.round(
									p字号
											* MainActivity.getContext()
													.getResources()
													.getDisplayMetrics()
													.scaledDensity)));
		}
		if (p粗体 || p斜体) {
			s.add(
					new android.text.style.StyleSpan(
							(p粗体 ? Typeface.BOLD : 0) | (p斜体 ? Typeface.ITALIC : 0)));
		}
		if (p下划线) {
			s.add(new android.text.style.UnderlineSpan());
		}
		if (p删除线) {
			s.add(new android.text.style.StrikethroughSpan());
		}
		return s.toArray(new android.text.style.CharacterStyle[0]);
	}
}
