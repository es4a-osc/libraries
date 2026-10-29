package simple.library.wsd.xui;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;

import com.xuexiang.xui.utils.SpanUtils;

import simple.runtime.android.MainActivity;
import simple.runtime.components.可视组件;
import simple.runtime.components.组件容器;
import simple.runtime.errors.非法参数错误;

/**
 * 富文本工具的 XUI 实现与 Simple 生命周期适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI富文本工具Impl extends XUI弹层基类 implements XUI富文本工具 {

	/**
	 * 创建XUI富文本工具。
	 *
	 * @param container 所属 Simple 窗口。
	 */
	public XUI富文本工具Impl(组件容器 container) {
		super(container);
	}

	// 上游 create() 会追加待处理片段；独立保存片段状态，使重复读取保持无副作用。
	private final List<TextSegment> segments = new ArrayList<TextSegment>();
	private TextSegment current;
	private long generation;

	@Override
	public void 添加文本(String text) {
		if (text == null) {
			throw new 非法参数错误();
		}
		current = new TextSegment();
		current.text = text;
		segments.add(current);
	}

	@Override
	public void 添加换行() {
		添加文本("\n");
	}

	@Override
	public void 置文字颜色(int color) {
		getCurrentSegment().foreground = color;
	}

	@Override
	public void 置背景颜色(int color) {
		getCurrentSegment().background = color;
	}

	@Override
	public void 置文字大小(int size) {
		if (size <= 0) {
			throw new 非法参数错误();
		}
		getCurrentSegment().size = size;
	}

	@Override
	public void 置粗体() {
		getCurrentSegment().bold = true;
	}

	@Override
	public void 置斜体() {
		getCurrentSegment().italic = true;
	}

	@Override
	public void 置下划线() {
		getCurrentSegment().underline = true;
	}

	@Override
	public void 置删除线() {
		getCurrentSegment().strike = true;
	}

	@Override
	public void 置上标() {
		getCurrentSegment().superscript = true;
	}

	@Override
	public void 置下标() {
		getCurrentSegment().subscript = true;
	}

	@Override
	public void 添加图片(String image) {
		current = new TextSegment();
		current.image = ComponentSupport.getDrawable(image);
		segments.add(current);
	}

	@Override
	public void 置点击标识(String tag) {
		if (tag == null) {
			throw new 非法参数错误();
		}
		getCurrentSegment().tag = tag;
	}

	@Override
	public void 置链接(String url) {
		if (url == null || url.trim().isEmpty()) {
			throw new 非法参数错误();
		}
		getCurrentSegment().tag = url;
	}

	@Override
	public void 片段被单击(String tag, String text) {
		dispatchEvent("片段被单击", tag, text);
	}

	@Override
	public void 应用到(可视组件 component) {
		View target = getNativeView(component);
		if (!(target instanceof TextView)) {
			throw new 非法参数错误();
		}
		TextView textView = (TextView) target;
		SpannableStringBuilder text = buildText();
		textView.setText(text);
		if (text.getSpans(0, text.length(), ClickableSpan.class).length > 0) {
			textView.setMovementMethod(LinkMovementMethod.getInstance());
		} else if (textView.getMovementMethod() instanceof LinkMovementMethod) {
			textView.setMovementMethod(null);
		}
	}

	@Override
	public String 取纯文本() {
		return buildText().toString();
	}

	@Override
	public void 清空() {
		segments.clear();
		current = null;
	}

	@Override
	public void 关闭() {
		generation++;
		清空();
	}

	/**
	 * 样式只能作用于已经添加的文本片段。
	 *
	 * @return 当前文本片段。
	 */
	private TextSegment getCurrentSegment() {
		if (current == null || current.image != null) {
			throw new 非法参数错误();
		}
		return current;
	}

	/**
	 * 为每次读取创建独立的上游构建器，既保留样式也避免 finalize 式读取重复追加。
	 *
	 * @return 与片段状态独立的富文本副本。
	 */
	private SpannableStringBuilder buildText() {
		SpanUtils spans = new SpanUtils();
		final long token = generation;
		for (TextSegment item : segments) {
			if (item.image != null) {
				Drawable.ConstantState constant = item.image.getConstantState();
				Drawable picture = constant == null ? item.image
					: constant.newDrawable(MainActivity.getContext().getResources()).mutate();
				if (item.imageLeft > 0 || item.imageRight > 0) {
					picture = new InsetDrawable(picture, item.imageLeft, 0, item.imageRight, 0);
					picture.setBounds(0, 0, picture.getIntrinsicWidth(), picture.getIntrinsicHeight());
				}
				spans.appendImage(picture, item.imageAlign);
				continue;
			}
			spans.append(item.text);
			if (item.font != null) {
				spans.setTypeface(item.font);
			}
			if (item.paragraphAlign != null) {
				Layout.Alignment[] alignments = {Layout.Alignment.ALIGN_NORMAL, Layout.Alignment.ALIGN_CENTER, Layout.Alignment.ALIGN_OPPOSITE};
				spans.setAlign(alignments[item.paragraphAlign]);
			}
			if (item.lineHeight > 0) {
				spans.setLineHeight(item.lineHeight);
			}
			if (item.leading != null) {
				spans.setLeadingMargin(item.leading[0], item.leading[1]);
			}
			if (item.quote != null) {
				spans.setQuoteColor(item.quote[0], item.quote[1], item.quote[2]);
			}
			if (item.bullet != null) {
				spans.setBullet(item.bullet[0], item.bullet[1], item.bullet[2]);
			}
			if (item.tag != null && !item.text.isEmpty()) {
				// 捕获应用时的值；后续构建器修改不影响已经显示的片段。
				final String tag = item.tag;
				final String text = item.text;
				final Integer color = item.foreground;
				spans.setClickSpan(new ClickableSpan() {
					@Override
					public void onClick(View widget) {
						if (token == generation) {
							requireVisibleOwner();
							片段被单击(tag, text);
						}
					}

					@Override
					public void updateDrawState(TextPaint paint) {
						super.updateDrawState(paint);
						if (color != null) {
							paint.setColor(color);
						}
					}
				});
			}
			if (item.foreground != null) {
				spans.setForegroundColor(item.foreground);
			}
			if (item.background != null) {
				spans.setBackgroundColor(item.background);
			}
			if (item.size > 0) {
				spans.setFontSize(item.size, true);
			}
			if (item.bold && item.italic) {
				spans.setBoldItalic();
			} else if (item.bold) {
				spans.setBold();
			} else if (item.italic) {
				spans.setItalic();
			}
			if (item.underline) {
				spans.setUnderline();
			}
			if (item.strike) {
				spans.setStrikethrough();
			}
			if (item.superscript) {
				spans.setSuperscript();
			} else if (item.subscript) {
				spans.setSubscript();
			}
		}
		return spans.create();
	}

	/**
	 * Simple 可表达的单个文本或图片片段，不保留已应用组件的引用。
	 */
	private static final class TextSegment {
		private Integer paragraphAlign;
		private int lineHeight;
		private int[] leading;
		private int[] quote;
		private int[] bullet;
		private int imageAlign;
		private String text = "";
		private String tag;
		private Drawable image;
		private Typeface font;
		private int imageLeft;
		private int imageRight;
		private Integer foreground;
		private Integer background;
		private int size;
		private boolean bold;
		private boolean italic;
		private boolean underline;
		private boolean strike;
		private boolean superscript;
		private boolean subscript;
	}

	@Override
	public void 置段落对齐(int alignment) {
		ComponentSupport.validateIndex(alignment, 3);
		getCurrentSegment().paragraphAlign = alignment;
	}

	@Override
	public void 置段落行高(int height) {
		if (height <= 0) {
			throw new 非法参数错误();
		}
		getCurrentSegment().lineHeight = height;
	}

	@Override
	public void 置段落缩进(int first, int rest) {
		if (first < 0 || rest < 0) {
			throw new 非法参数错误();
		}
		getCurrentSegment().leading = new int[] {first, rest};
	}

	@Override
	public void 置引用样式(int color, int width, int gap) {
		if (width <= 0 || gap < 0) {
			throw new 非法参数错误();
		}
		getCurrentSegment().quote = new int[] {color, width, gap};
	}

	@Override
	public void 置项目符号(int color, int radius, int gap) {
		if (radius <= 0 || gap < 0) {
			throw new 非法参数错误();
		}
		getCurrentSegment().bullet = new int[] {color, radius, gap};
	}

	@Override
	public void 添加图片(String image, int width, int height, int alignment) {
		if (width <= 0 || height <= 0) {
			throw new 非法参数错误();
		}
		ComponentSupport.validateIndex(alignment, 4);
		Bitmap original = ComponentSupport.toBitmap(ComponentSupport.getDrawable(image));
		if (original == null) {
			throw new 非法参数错误();
		}
		Bitmap scaled = Bitmap.createScaledBitmap(original, width, height, true);
		BitmapDrawable drawable = new BitmapDrawable(MainActivity.getContext().getResources(), scaled);
		drawable.setTargetDensity(scaled.getDensity());
		current = new TextSegment();
		current.image = drawable;
		current.imageAlign = alignment;
		segments.add(current);
	}

	@Override
	public void 置字体(String asset) {
		getCurrentSegment().font = ThemeSupport.loadFont(asset);
	}

	@Override
	public void 置图片间距(int left, int right) {
		// 图片片段不能走文字样式的检查边界。
		TextSegment segment = current;
		if (segment == null || segment.image == null || left < 0 || right < 0) {
			throw new 非法参数错误();
		}
		segment.imageLeft = left;
		segment.imageRight = right;
	}

}
