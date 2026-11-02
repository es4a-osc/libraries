package simple.library.wsd.iconics;

import android.content.res.ColorStateList;
import android.graphics.*;
import android.view.*;
import android.widget.*;
import com.mikepenz.iconics.*;
import com.mikepenz.iconics.typeface.*;
import java.util.*;
import simple.runtime.annotations.*;
import simple.runtime.components.*;

/**
 * 状态颜色配置；原生颜色快照只读，清空后可重新配置。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class 图标状态颜色 {
	private final List<int[]> states = new ArrayList<int[]>();
	private final List<Integer> colors = new ArrayList<Integer>();
	int fallback = Color.BLACK;
	private ColorStateList base;
	/**
	 * 创建独立实例。
	 */
	public 图标状态颜色() {}
	/**
	 * 添加一条状态；正值表示存在，负值表示不存在。原生快照需先清空。
	 *
	 * @param state 状态。
	 * @param color 颜色。
	 */
	@SimpleFunction
	public void 添加(int[] state, int color) {
		if (base != null) {
			throw new 图标错误("原生状态色快照只读，请清空后重新配置");
		}
		if (state == null || state.length == 0) {
			throw new 图标错误("状态不能为空，默认值请用置默认颜色");
		}
		states.add(state.clone());
		colors.add(color);
	}

	/**
	 * 设置未匹配时颜色。原生快照需先清空。
	 *
	 * @param color 颜色。
	 */
	@SimpleFunction
	public void 置默认颜色(int color) {
		if (base != null) {
			throw new 图标错误("原生状态色快照只读，请清空后重新配置");
		}
		fallback = color;
	}

	/**
	 * 移除所有状态条目。
	 */
	@SimpleFunction
	public void 清空() {
		base = null;
		states.clear();
		colors.clear();
	}

	/**
	 * 创建不共享数组的状态色快照。
	 *
	 * @return Android 状态色。
	 */
	ColorStateList nativeValue() {
		if (base != null) {
			return base;
		}

		int[][] s = new int[states.size() + 1][];
		int[] c = new int[s.length];
		for (int i = 0; i < states.size(); i++) {
			s[i] = states.get(i).clone();
			c[i] = colors.get(i);
		}
		s[s.length - 1] = new int[0];
		c[c.length - 1] = fallback;
		return new ColorStateList(s, c);
	}

	/**
	 * 包装只读原生状态色；清空后可重新配置。
	 *
	 * @param colors 原生状态颜色。
	 * @return 独立配置，原生颜色为空时返回空。
	 */
	static 图标状态颜色 from(ColorStateList colors) {
		if (colors == null) {
			return null;
		}
		图标状态颜色 out = new 图标状态颜色();
		out.base = colors;
		out.fallback = colors.getDefaultColor();
		return out;
	}

	/**
	 * 读取未匹配状态的默认颜色。
	 *
	 * @return 读取未匹配状态的默认颜色。
	 */
	@SimpleFunction
	public int 取默认颜色() {
		return nativeValue().getDefaultColor();
	}

	/**
	 * 读取指定状态实际采用的颜色。
	 *
	 * @param state 状态。
	 * @return 读取指定状态实际采用的颜色。
	 */
	@SimpleFunction
	public int 取状态颜色(int[] state) {
		if (state == null) {
			throw new 图标错误("状态为空");
		}
		ColorStateList c = nativeValue();
		return c.getColorForState(state, c.getDefaultColor());
	}

	/**
	 * 是否包含按状态变化的颜色。
	 *
	 * @return 是否包含按状态变化的颜色。
	 */
	@SimpleFunction
	public boolean 是否有状态() {
		return nativeValue().isStateful();
	}
}
