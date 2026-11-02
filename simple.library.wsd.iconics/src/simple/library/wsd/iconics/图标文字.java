package simple.library.wsd.iconics;

import android.graphics.*;
import android.view.*;
import android.widget.*;
import com.mikepenz.iconics.*;
import com.mikepenz.iconics.typeface.*;
import java.util.*;
import simple.runtime.annotations.*;
import simple.runtime.components.*;

/**
 * 文字图标混排构造器。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class 图标文字 {
	private Iconics.Builder builder = new Iconics.Builder();
	/**
	 * 创建独立实例。
	 */
	public 图标文字() {}
	/**
	 * 添加作用于全部图标的样式快照。
	 *
	 * @param style 样式。
	 */
	@SimpleFunction
	public void 统一样式(图标文字样式 style) {
		if (style == null) {
			throw new 图标错误("样式为空");
		}
		builder.style(style.spans());
	}

	/**
	 * 添加作用于指定图标的样式快照。
	 *
	 * @param name 图标名称。
	 * @param style 样式。
	 */
	@SimpleFunction
	public void 指定样式(String name, 图标文字样式 style) {
		Support.resolve(name);
		if (style == null) {
			throw new 图标错误("样式为空");
		}
		builder.styleFor(name, style.spans());
	}

	/**
	 * 处理图标标记并应用到文本控件。
	 *
	 * @param target 目标。
	 * @param text 文本。
	 */
	@SimpleFunction
	public void 应用(可视组件 target, String text) {
		View v = Support.view(target);
		if (!(v instanceof TextView) || text == null) {
			throw new 图标错误("需要文本视图及文本");
		}
		Support.init();
		java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\{([^{}]+)\\}").matcher(text);
		while (m.find()) {
			Support.resolve(m.group(1));
		}
		Support.setText((TextView) v, builder.on(text).build());
	}

	/**
	 * 清空构造器样式，不改变已应用文本。
	 */
	@SimpleFunction
	public void 清空() {
		builder = new Iconics.Builder();
	}
}
