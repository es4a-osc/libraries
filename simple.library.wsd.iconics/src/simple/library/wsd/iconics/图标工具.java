package simple.library.wsd.iconics;

import android.graphics.Typeface;
import android.view.View;
import android.widget.TextView;

import com.mikepenz.iconics.Iconics;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.typeface.GenericFont;

import java.util.ArrayList;
import java.util.List;

import simple.runtime.android.MainActivity;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.components.可视组件;

/**
 * 字体图标静态工具。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class 图标工具 {
	/**
	 * 静态工具不创建实例。
	 */
	private 图标工具() {
	}

	/**
	 * 取得所有已注册字体前缀。
	 *
	 * @return 取得所有已注册字体前缀。
	 */
	@SimpleFunction
	public static String[] 取字体前缀() {
		Support.init();
		return Support.fonts.keySet().toArray(new String[0]);
	}

	/**
	 * 查询已注册字体。
	 *
	 * @param prefix 前缀。
	 * @return 查询已注册字体。
	 * @throws 图标错误 前缀无效或字体尚未注册。
	 */
	@SimpleFunction
	public static 图标字体 取字体(String prefix) {
		Support.init();
		Support.prefix(prefix);
		图标字体 f = new 图标字体();
		f.value = Support.fonts.get(prefix);
		f.check();
		f.registered = true;
		return f;
	}

	/**
	 * 从应用资产创建字体，注册前添加映射。
	 *
	 * @param prefix 前缀。
	 * @param asset  资产路径。
	 * @return 从应用资产创建字体，注册前添加映射。
	 */
	@SimpleFunction
	public static 图标字体 创建资产字体(String prefix, String asset) {
		Support.init();
		Support.prefix(prefix);
		GenericFont g = new GenericFont(prefix, asset);
		try {
			Typeface.createFromAsset(MainActivity.getContext().getAssets(), asset);
		} catch (RuntimeException e) {
			throw new 图标错误("无法加载字体资产：" + asset);
		}
		图标字体 f = new 图标字体();
		f.value = g;
		return f;
	}

	/**
	 * 从字体资源创建字体。
	 *
	 * @param prefix   前缀。
	 * @param resource 字体资源。
	 * @return 从字体资源创建字体。
	 */
	@SimpleFunction
	public static 图标字体 创建资源字体(String prefix, int resource) {
		Support.init();
		Support.prefix(prefix);
		try {
			if (androidx.core.content.res.ResourcesCompat.getFont(MainActivity.getContext(), resource) == null) {
				throw new Exception();
			}
		} catch (Exception e) {
			throw new 图标错误("字体资源无效");
		}
		图标字体 f = new 图标字体();
		f.value = new GenericFont(prefix, resource);
		return f;
	}

	/**
	 * 注册字体，重复前缀拒绝覆盖。
	 *
	 * @param font 字体。
	 */
	@SimpleFunction
	public static void 注册字体(图标字体 font) {
		Support.init();
		if (font == null) {
			throw new 图标错误("字体为空");
		}
		font.check();
		String p = font.value.getMappingPrefix();
		Support.prefix(p);
		if (Support.fonts.containsKey(p)) {
			throw new 图标错误("字体前缀重复：" + p);
		}
		if (font.value.getIconCount() == 0) {
			throw new 图标错误("字体没有映射");
		}
		Iconics.registerFont(font.value);
		Support.fonts.put(p, font.value);
		font.registered = true;
	}

	/**
	 * 检查图标是否存在。
	 *
	 * @param name 图标名称。
	 * @return 检查图标是否存在。
	 * @throws 图标错误 未在主线程调用。
	 */
	@SimpleFunction
	public static boolean 图标存在(String name) {
		Support.init();
		try {
			Support.resolve(name);
			return true;
		} catch (图标错误 e) {
			return false;
		}
	}

	/**
	 * 按名称包含关键词搜索，空文本列出全部。
	 *
	 * @param prefix 前缀。
	 * @param query  关键词。
	 * @return 按名称包含关键词搜索，空文本列出全部。
	 */
	@SimpleFunction
	public static String[] 搜索图标(String prefix, String query) {
		图标字体 f = 取字体(prefix);
		if (query == null) {
			throw new 图标错误("关键词为空");
		}
		List<String> out = new ArrayList<String>();
		String keyword = query.toLowerCase(java.util.Locale.ROOT);
		for (String s : f.value.getIcons()) {
			if (s.toLowerCase(java.util.Locale.ROOT).contains(keyword)) {
				out.add(s);
			}
		}
		return out.toArray(new String[0]);
	}

	/**
	 * 创建独立图标快照。
	 *
	 * @param name  图标名称。
	 * @param style 样式。
	 * @return 创建独立图标快照。
	 */
	@SimpleFunction
	public static 图标图像 创建图标(String name, 图标样式 style) {
		Support.main();
		if (style == null) {
			throw new 图标错误("样式为空");
		}
		IconicsDrawable x = Support.drawable();
		x.setIcon(Support.resolve(name));
		style.apply(x);
		图标图像 out = new 图标图像();
		out.value = x;
		return out;
	}

	/**
	 * 解析文字中的 {前缀_图标名}，无效名称抛出错误。
	 *
	 * @param target 目标。
	 * @param text   文本。
	 */
	@SimpleFunction
	public static void 应用文字图标(可视组件 target, String text) {
		Support.init();
		View v = Support.view(target);
		if (!(v instanceof TextView) || text == null) {
			throw new 图标错误("目标必须是文本视图");
		}
		Support.validateText(text);
		Support.setText((TextView) v, new Iconics.Builder().on(text).build());
	}

	/**
	 * 从可读字体文件创建自定义字体。
	 *
	 * @param prefix 前缀。
	 * @param path   字体路径。
	 * @return 从可读字体文件创建自定义字体。
	 */
	@SimpleFunction
	public static 图标字体 创建文件字体(String prefix, String path) {
		Support.init();
		Support.prefix(prefix);
		final Typeface face;
		try {
			face = Typeface.createFromFile(path);
		} catch (RuntimeException e) {
			throw new 图标错误("字体文件无效：" + path);
		}
		图标字体 f = new 图标字体();
		f.value = new GenericFont(prefix, "") {
			@Override
			public Typeface getRawTypeface() {
				return face;
			}
		};
		return f;
	}

	/**
	 * 使用字体和 BMP 字符码直接生成图标。
	 *
	 * @param font  字体。
	 * @param code  字符码。
	 * @param style 样式。
	 * @return 使用字体和 BMP 字符码直接生成图标。
	 */
	@SimpleFunction
	public static 图标图像 创建字符图标(图标字体 font, int code, 图标样式 style) {
		Support.init();
		if (font == null || style == null || code < 0 || code > 65535 || code >= 55296 && code <= 57343) {
			throw new 图标错误("字体或字符码无效");
		}
		font.check();
		IconicsDrawable x = Support.drawable();
		x.setIconText(String.valueOf((char) code));
		x.setTypeface(font.value.getRawTypeface());
		style.apply(x);
		图标图像 out = new 图标图像();
		out.value = x;
		return out;
	}

	/**
	 * 按顺序创建独立图标数组，任一名称无效时拒绝整次生成。
	 *
	 * @param names 名称。
	 * @param style 样式。
	 * @return 按顺序创建独立图标数组，任一名称无效时拒绝整次生成。
	 */
	@SimpleFunction
	public static 图标图像[] 批量创建图标(String[] names, 图标样式 style) {
		Support.main();
		if (names == null || style == null) {
			throw new 图标错误("名称数组或样式为空");
		}
		for (String name : names) {
			Support.resolve(name);
		}
		图标图像[] out = new 图标图像[names.length];
		for (int i = 0; i < names.length; i++) {
			out[i] = 创建图标(names[i], style);
		}
		return out;
	}

	/**
	 * 使用同一字体绘制短文字，不解析图标标记。
	 *
	 * @param font  字体。
	 * @param text  文字。
	 * @param style 样式。
	 * @return 使用同一字体绘制短文字，不解析图标标记。
	 */
	@SimpleFunction
	public static 图标图像 创建文字图标(图标字体 font, String text, 图标样式 style) {
		Support.main();
		if (font == null || text == null || text.isEmpty() || style == null) {
			throw new 图标错误("字体、文字或样式无效");
		}
		font.check();
		IconicsDrawable x = Support.drawable();
		x.setIconText(text);
		// 设置短文字会清除旧图标并重置字体，随后再应用指定字体。
		x.setTypeface(font.value.getRawTypeface());
		style.apply(x);
		图标图像 out = new 图标图像();
		out.value = x;
		return out;
	}
}
