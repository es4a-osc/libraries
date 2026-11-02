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
import simple.runtime.components.impl.android.视图组件;

/**
 * 字体注册与 Android 适配边界。
 *
 * @author 树先生 xhwsd@qq.com
 */
final class Support {

	static final Map<String, ITypeface> fonts = new TreeMap<String, ITypeface>();
	private static boolean ready;
	/**
	 * 工具不创建实例。
	 */
	private Support() {}
	/**
	 * 只在 Android 主线程操作视图及字体注册。
	 */
	static void main() {
		if (android.os.Looper.myLooper() != android.os.Looper.getMainLooper()) {
			throw new 图标错误("请在主线程调用字体图标");
		}
	}
	/**
	 * 注册交付中的字体，保留字体包自身元数据。
	 */
	static void init() {
		main();
		if (ready) {
			return;
		}
		Iconics.init(MainActivity.getContext());
		for (ITypeface f : Iconics.getRegisteredFonts(MainActivity.getContext())) {
			fonts.put(f.getMappingPrefix(), f);
		}
		GenericFont f =
				new GenericFont("XUIIconFont", "xuexiangjys", "xui", R.font.es4a_iconics_xui);
		f.registerIcon("file", (char) 58880);
		f.registerIcon("chat", (char) 58881);
		f.registerIcon("voice", (char) 58882);
		f.registerIcon("delete", (char) 58883);
		f.registerIcon("delete1", (char) 58899);
		f.registerIcon("delete2", (char) 58928);
		f.registerIcon("delete3", (char) 58968);
		f.registerIcon("back", (char) 58889);
		f.registerIcon("back1", (char) 58900);
		f.registerIcon("add", (char) 58898);
		f.registerIcon("add1", (char) 58901);
		f.registerIcon("add2", (char) 58929);
		f.registerIcon("reset", (char) 58902);
		f.registerIcon("complete", (char) 58960);
		f.registerIcon("complete1", (char) 58995);
		f.registerIcon("collect", (char) 59263);
		f.registerIcon("emoj", (char) 58920);

		Iconics.registerFont(f);
		fonts.put("xui", f);
		ready = true;
	}
	/**
	 * 获取独立 AppCompat 主题。
	 *
	 * @return 主题上下文。
	 */
	static android.content.Context context() {
		return new androidx.appcompat.view.ContextThemeWrapper(
				MainActivity.getContext(), androidx.appcompat.R.style.Theme_AppCompat_Light);
	}
	/**
	 * 验证前缀。
	 *
	 * @param p 字体前缀。
	 */
	static void prefix(String p) {
		if (p == null || !p.matches("[a-z0-9]{3}")) {
			throw new 图标错误("字体前缀须为三个小写字母或数字");
		}
	}
	/**
	 * 验证映射短名。
	 *
	 * @param n 短名。
	 */
	static void requireName(String n) {
		if (n == null || !n.matches("[A-Za-z0-9_]+")) {
			throw new 图标错误("图标短名须为字母数字或下划线");
		}
	}
	/**
	 * 解析图标，拒绝缺字静默回退。
	 *
	 * @param n 图标名称。
	 * @return 已注册字符。
	 */
	static IIcon resolve(String n) {
		init();
		if (n == null) {
			throw new 图标错误("图标名为空");
		}
		n = n.replace('-', '_');
		if (n.length() < 5) {
			throw new 图标错误("图标名需要前缀");
		}
		ITypeface f = fonts.get(n.substring(0, 3));
		if (f == null) {
			throw new 图标错误("字体不存在：" + n);
		}
		return icon(f, n);
	}
	/**
	 * 在字体内查找字符。
	 *
	 * @param f 字体。
	 * @param n 图标名称。
	 * @return 图标。
	 */
	static IIcon icon(ITypeface f, String n) {
		if (n == null) {
			throw new 图标错误("图标名称为空");
		}
		n = n.replace('-', '_');
		try {
			return f.getIcon(n);
		} catch (RuntimeException e) {
			throw new 图标错误("图标不存在：" + n);
		}
	}
	/**
	 * 获取原生视图。
	 *
	 * @param target 目标。
	 * @return Android 视图。
	 */
	static View view(可视组件 target) {
		main();
		if (!(target instanceof 视图组件)) {
			throw new 图标错误("目标不是原生可视组件");
		}
		if (target instanceof WidgetBase) {
			((WidgetBase) target).check();
		}
		return ((视图组件) target).getView();
	}
	/**
	 * DIP 转像素。
	 *
	 * @param v DIP。
	 * @return 像素。
	 */
	static int dp(int v) {
		return Math.round(dpf(v));
	}
	/**
	 * DIP 转浮点像素。
	 *
	 * @param v DIP。
	 * @return 像素。
	 */
	static float dpf(float v) {
		if (!Float.isFinite(v)) {
			throw new 图标错误("尺寸必须为有限数值");
		}
		float px = v * MainActivity.getContext().getResources().getDisplayMetrics().density;
		if (!Float.isFinite(px)) {
			throw new 图标错误("像素尺寸溢出");
		}
		return px;
	}

	/**
	 * 复制全部公开渲染状态，不复制 View 回调。
	 *
	 * @param source 原图标。
	 * @return 独立图标。
	 */
	static IconicsDrawable copy(IconicsDrawable source) {
		IconicsDrawable out = new IconicsDrawable(MainActivity.getContext());
		out.setIcon(source.getIcon());
		out.setIconText(source.getIconText());
		out.setTypeface(source.getTypeface());
		out.setColorList(source.getColorList());
		out.setStyle(source.getStyle());
		out.setBackgroundContourColorList(source.getBackgroundContourColorList());
		out.setBackgroundColorList(source.getBackgroundColorList());
		out.setContourColorList(source.getContourColorList());
		out.setCompatAlpha(source.getCompatAlpha());
		out.setAutoMirroredCompat(source.getAutoMirroredCompat());
		out.setSizeXPx(source.getSizeXPx());
		out.setSizeYPx(source.getSizeYPx());
		out.setRespectFontBounds(source.getRespectFontBounds());
		out.setDrawContour(source.getDrawContour());
		out.setDrawBackgroundContour(source.getDrawBackgroundContour());
		out.setRoundedCornerRxPx(source.getRoundedCornerRxPx());
		out.setRoundedCornerRyPx(source.getRoundedCornerRyPx());
		out.setPaddingPx(source.getPaddingPx());
		out.setContourWidthPx(source.getContourWidthPx());
		out.setBackgroundContourWidthPx(source.getBackgroundContourWidthPx());
		out.setIconOffsetXPx(source.getIconOffsetXPx());
		out.setIconOffsetYPx(source.getIconOffsetYPx());
		out.setShadowRadiusPx(source.getShadowRadiusPx());
		out.setShadowDxPx(source.getShadowDxPx());
		out.setShadowDyPx(source.getShadowDyPx());
		out.setShadowColorInt(source.getShadowColorInt());
		out.setTint(source.getTint());
		out.setTintPorterMode(source.getTintPorterMode());
		out.setIconColorFilter(source.getIconColorFilter());
		out.setState(source.getState().clone());
		out.setLayoutDirection(source.getLayoutDirection());
		return out;
	}

	/**
	 * 保留字符 Span，避开上游控件将 CharSequence 转为 String 的重复处理。
	 *
	 * @param view 文本控件。
	 * @param text 已处理图标的文字。
	 */
	static void setText(TextView view, android.text.Spanned text) {
		view.setText(text.toString(), TextView.BufferType.SPANNABLE);
		android.text.Spannable target = (android.text.Spannable) view.getText();
		for (Object span : text.getSpans(0, text.length(), Object.class)) {
			target.setSpan(
					span, text.getSpanStart(span), text.getSpanEnd(span), text.getSpanFlags(span));
		}
	}

	/**
	 * 验证颜色矩阵并复制，空数组表示无滤镜。
	 *
	 * @param matrix 20项矩阵。
	 * @return 颜色滤镜或空。
	 */
	static ColorFilter filter(float[] matrix) {
		if (matrix == null || (matrix.length != 0 && matrix.length != 20)) {
			throw new 图标错误("颜色矩阵须含20项，或用空数组清除");
		}
		if (matrix.length == 0) {
			return null;
		}
		for (float x : matrix) {
			if (!Float.isFinite(x)) {
				throw new 图标错误("矩阵必须为有限数值");
			}
		}
		return new ColorMatrixColorFilter(new ColorMatrix(matrix.clone()));
	}

	/**
	 * 旧系统的路径阴影需要软件图层；不影响未使用阴影的视图。
	 *
	 * @param view 接收图标的视图。
	 * @param icon 图标。
	 */
	static void prepareShadow(View view, IconicsDrawable icon) {
		if (icon.getShadowRadiusPx() > 0) {
			view.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		}
	}
}
