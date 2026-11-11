package simple.library.wsd.iconics;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.view.View;
import android.widget.TextView;

import com.mikepenz.iconics.Iconics;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.typeface.GenericFont;
import com.mikepenz.iconics.typeface.IIcon;
import com.mikepenz.iconics.typeface.ITypeface;

import java.util.Map;
import java.util.TreeMap;

import simple.runtime.android.MainActivity;
import simple.runtime.components.impl.android.视图组件;
import simple.runtime.components.可视组件;

/**
 * 字体注册与 Android 适配边界。
 *
 * @author 树先生 xhwsd@qq.com
 */
final class Support {

	static final Map<String, ITypeface> fonts = new TreeMap<String, ITypeface>();
	private static boolean ready;
	private static final java.util.regex.Pattern ICON_MARKER = java.util.regex.Pattern.compile("\\{([^{}]+)\\}");

	/**
	 * 工具不创建实例。
	 */
	private Support() {
	}

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
		GenericFont f = new GenericFont("XUIIconFont", "xuexiangjys", "xui", R.font.es4a_iconics_xui) {
			/**
			 * 返回配套 XUI 字体的说明。
			 *
			 * @return 字体说明。
			 */
			@Override
			public String getDescription() {
				return "XUI字体图标库";
			}

			/**
			 * 返回配套 XUI 字体的项目地址。
			 *
			 * @return 上游项目地址。
			 */
			@Override
			public String getUrl() {
				return "https://github.com/xuexiangjys/XUI";
			}
		};
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
		return new androidx.appcompat.view.ContextThemeWrapper(MainActivity.getContext(),
				androidx.appcompat.R.style.Theme_AppCompat_Light);
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
	 * 在更新文本前检查其中的全部图标标记。
	 *
	 * @param text 待显示的非空引用文本。
	 */
	static void validateText(String text) {
		java.util.regex.Matcher matcher = ICON_MARKER.matcher(text);
		while (matcher.find()) {
			resolve(matcher.group(1));
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
	 * 创建保留图标边界内透明区域的绘制对象。
	 *
	 * @return 独立字体图标。
	 */
	static IconicsDrawable drawable() {
		return new IconicsDrawable(MainActivity.getContext()) {
			/**
			 * 设置图标整体透明度，保留各颜色本身的透明通道。
			 *
			 * @param alpha 0 至 255 的整体透明度。
			 */
			@Override
			public void setAlpha(int alpha) {
				// 上游 setAlpha 会把透明背景画笔也变成实色，整体透明度在绘制层合成。
				setCompatAlpha(alpha);
			}

			/**
			 * 绘制图标并隔离透明度、镜像操作对画布的影响。
			 *
			 * @param canvas 目标画布。
			 */
			@Override
			public void draw(Canvas canvas) {
				int alpha = getAlpha();
				if (alpha == 0) {
					return;
				}
				int saved = alpha == 255 ? canvas.save() : canvas.saveLayerAlpha(null, alpha);
				try {
					super.draw(canvas);
				} finally {
					canvas.restoreToCount(saved);
				}
			}

			/**
			 * 图形笔画之外可能透明，不能按整个矩形完全不透明处理。
			 *
			 * @return 包含透明像素的类型。
			 */
			@Override
			@SuppressWarnings("deprecation")
			public int getOpacity() {
				// 保留 Drawable 的抽象契约；上游只看画笔 alpha 会误报 OPAQUE。
				return android.graphics.PixelFormat.TRANSLUCENT;
			}
		};
	}

	/**
	 * 设置精确的总内边距，抵消上游描边 setter 对内边距的联动。
	 *
	 * @param image  独立字体图标。
	 * @param pixels 最终像素内边距。
	 * @throws 图标错误 描边补偿超出整数范围。
	 */
	static void padding(IconicsDrawable image, int pixels) {
		long added = (image.getDrawContour() ? (long) image.getContourWidthPx() : 0L)
				+ (image.getDrawBackgroundContour() ? (long) image.getBackgroundContourWidthPx() : 0L);
		long base = (long) pixels - added;
		if (base < Integer.MIN_VALUE || base > Integer.MAX_VALUE) {
			throw new 图标错误("内边距与描边宽度超出整数范围");
		}
		if (image.getPaddingPx() != pixels) {
			// 先改变当前值，再补偿，避免上游相等值短路跳过赋值。
			image.setPaddingPx(pixels);
			image.setPaddingPx((int) base);
		}
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
		IconicsDrawable out = drawable();
		out.setIcon(source.getIcon());
		out.setIconText(source.getIconText());
		out.setTypeface(source.getTypeface());
		out.setColorList(source.getColorList());
		out.setStyle(source.getStyle());
		out.setBackgroundContourColorList(source.getBackgroundContourColorList());
		out.setBackgroundColorList(source.getBackgroundColorList());
		out.setContourColorList(source.getContourColorList());
		// 使用封装中的整体透明度，保留颜色自身的透明通道。
		out.setAlpha(source.getAlpha());
		out.setAutoMirroredCompat(source.getAutoMirroredCompat());
		out.setSizeXPx(source.getSizeXPx());
		out.setSizeYPx(source.getSizeYPx());
		out.setRespectFontBounds(source.getRespectFontBounds());
		out.setRoundedCornerRxPx(source.getRoundedCornerRxPx());
		out.setRoundedCornerRyPx(source.getRoundedCornerRyPx());
		out.setContourWidthPx(source.getContourWidthPx());
		out.setBackgroundContourWidthPx(source.getBackgroundContourWidthPx());
		// 上游的描边宽度 setter 会开启描边，因此最后还原两个显式开关。
		out.setDrawContour(source.getDrawContour());
		out.setDrawBackgroundContour(source.getDrawBackgroundContour());
		padding(out, source.getPaddingPx());
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
			target.setSpan(span, text.getSpanStart(span), text.getSpanEnd(span), text.getSpanFlags(span));
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
