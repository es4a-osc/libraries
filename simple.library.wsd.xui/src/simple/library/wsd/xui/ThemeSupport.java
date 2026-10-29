package simple.library.wsd.xui;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.view.ContextThemeWrapper;

import simple.runtime.android.MainActivity;
import simple.runtime.errors.非法参数错误;

/**
 * 保存后续 XUI 实例的主题与字体，不修改 Simple Activity 或已有实例。
 *
 * @author 树先生 xhwsd@qq.com
 */
final class ThemeSupport {

	private static int overlay;
	private static String fontAsset = "";
	private static Typeface font;

	/**
	 * 工具类不创建实例。
	 */
	private ThemeSupport() {
	}

	/**
	 * 创建独立主题，覆盖层始终以 XUI 手机主题为基础。
	 *
	 * @return 独立组件上下文。
	 */
	static Context createContext() {
		ConfiguredContext context = new ConfiguredContext(MainActivity.getContext(), font);
		if (overlay != 0) {
			context.getTheme().applyStyle(overlay, true);
		}
		return context;
	}

	/**
	 * 验证样式资源后保存配置；0 取消自定义覆盖。
	 *
	 * @param style 应用中已打包的样式资源。
	 */
	static void setOverlay(int style) {
		if (style != 0) {
			try {
				if (!"style".equals(MainActivity.getContext().getResources().getResourceTypeName(style))) {
					throw new 非法参数错误();
				}
			} catch (Resources.NotFoundException failure) {
				throw new 非法参数错误();
			}
		}
		overlay = style;
	}

	/**
	 * 取得后续组件的样式覆盖资源。
	 *
	 * @return 样式资源，0 表示默认。
	 */
	static int getOverlay() {
		return overlay;
	}

	/**
	 * 加载资产字体成功后才替换默认配置；空文本恢复系统字体。
	 *
	 * @param asset 资产相对路径。
	 */
	static void setFont(String asset) {
		Typeface replacement = loadFont(asset);
		font = asset.isEmpty() ? null : replacement;
		fontAsset = asset;
	}

	/**
	 * 取得后续组件的字体资产路径。
	 *
	 * @return 字体资产路径，空文本表示默认。
	 */
	static String getFont() {
		return fontAsset;
	}

	/**
	 * 从资产加载字体，统一报告错误路径而不静默回退。
	 *
	 * @param asset 字体资产路径，空文本返回系统默认字体。
	 * @return 可用字体。
	 */
	static Typeface loadFont(String asset) {
		if (asset == null) {
			throw new 非法参数错误();
		}
		if (asset.isEmpty()) {
			return Typeface.DEFAULT;
		}
		try {
			return Typeface.createFromAsset(MainActivity.getContext().getAssets(), asset);
		} catch (RuntimeException failure) {
			throw new 非法参数错误();
		}
	}

	/**
	 * 为本次刚创建的文字视图树应用默认字体，保留各文字原有粗斜体样式。
	 *
	 * @param view 新创建的 XUI 视图或弹层根。
	 */
	static void applyFont(View view) {
		Typeface selected = getContextFont(view.getContext());
		if (selected != null && view instanceof TextView && view.getTag(R.id.es4a_xui_default_font) == null) {
			TextView text = (TextView) view;
			int style = text.getTypeface() == null ? Typeface.NORMAL : text.getTypeface().getStyle();
			text.setTypeface(selected, style);
			text.setTag(R.id.es4a_xui_default_font, Boolean.TRUE);
		}
		if (view instanceof ViewGroup) {
			ViewGroup group = (ViewGroup) view;
			for (int index = 0; index < group.getChildCount(); index++) {
				applyFont(group.getChildAt(index));
			}
		}
	}

	/**
	 * 从组件自身上下文读取创建时的字体快照，不覆盖借入的普通 Simple 内容。
	 *
	 * @param context 视图上下文。
	 * @return 字体快照；未配置时为空。
	 */
	private static Typeface getContextFont(Context context) {
		while (context instanceof ContextWrapper) {
			if (context instanceof ConfiguredContext) {
				return ((ConfiguredContext) context).typeface;
			}
			Context next = ((ContextWrapper) context).getBaseContext();
			if (next == context) {
				break;
			}
			context = next;
		}
		return null;
	}

	/**
	 * 字体配置随上下文固定，后续修改默认值不影响已有组件新创建的内部行。
	 */
	private static final class ConfiguredContext extends ContextThemeWrapper {
		private final Typeface typeface;

		/**
		 * 固定本次组件创建时的字体。
		 *
		 * @param base Simple 宿主上下文。
		 * @param typeface 本次字体快照。
		 */
		ConfiguredContext(Context base, Typeface typeface) {
			super(base, com.xuexiang.xui.R.style.XUITheme_Phone);
			this.typeface = typeface;
		}
	}

}
