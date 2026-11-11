package simple.library.wsd.iconics;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;

import com.mikepenz.iconics.IconicsDrawable;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.SimpleProperty;

/**
 * 图标渲染配置。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class 图标样式 {
	private int p宽度 = 24;

	/**
	 * 图标宽度，DIP，必须大于零。
	 *
	 * @return 当前值。
	 */
	@SimpleProperty
	public int 宽度() {
		return p宽度;
	}

	/**
	 * 图标宽度，DIP，必须大于零。
	 *
	 * @param value 新值。
	 */
	@SimpleProperty
	public void 宽度(int value) {
		p宽度 = value;
	}

	private int p高度 = 24;

	/**
	 * 图标高度，DIP，必须大于零。
	 *
	 * @return 当前值。
	 */
	@SimpleProperty
	public int 高度() {
		return p高度;
	}

	/**
	 * 图标高度，DIP，必须大于零。
	 *
	 * @param value 新值。
	 */
	@SimpleProperty
	public void 高度(int value) {
		p高度 = value;
	}

	private int p颜色 = Color.BLACK;

	/**
	 * 图标 ARGB 颜色。
	 *
	 * @return 当前值。
	 */
	@SimpleProperty
	public int 颜色() {
		return p颜色;
	}

	/**
	 * 图标 ARGB 颜色。
	 *
	 * @param value 新值。
	 */
	@SimpleProperty
	public void 颜色(int value) {
		p颜色 = value;
	}

	private int p透明度 = 255;

	/**
	 * 透明度，0 至 255。
	 *
	 * @return 当前值。
	 */
	@SimpleProperty
	public int 透明度() {
		return p透明度;
	}

	/**
	 * 透明度，0 至 255。
	 *
	 * @param value 新值。
	 */
	@SimpleProperty
	public void 透明度(int value) {
		p透明度 = value;
	}

	private int p内边距 = 0;

	/**
	 * 图标内边距，DIP。
	 *
	 * @return 当前值。
	 */
	@SimpleProperty
	public int 内边距() {
		return p内边距;
	}

	/**
	 * 图标内边距，DIP。
	 *
	 * @param value 新值。
	 */
	@SimpleProperty
	public void 内边距(int value) {
		p内边距 = value;
	}

	private int p描边宽度 = 0;

	/**
	 * 图标描边宽度，DIP。
	 *
	 * @return 当前值。
	 */
	@SimpleProperty
	public int 描边宽度() {
		return p描边宽度;
	}

	/**
	 * 图标描边宽度，DIP。
	 *
	 * @param value 新值。
	 */
	@SimpleProperty
	public void 描边宽度(int value) {
		p描边宽度 = value;
	}

	private int p描边颜色 = Color.BLACK;

	/**
	 * 图标描边颜色。
	 *
	 * @return 当前值。
	 */
	@SimpleProperty
	public int 描边颜色() {
		return p描边颜色;
	}

	/**
	 * 图标描边颜色。
	 *
	 * @param value 新值。
	 */
	@SimpleProperty
	public void 描边颜色(int value) {
		p描边颜色 = value;
	}

	private int p背景颜色 = Color.TRANSPARENT;

	/**
	 * 背景颜色。
	 *
	 * @return 当前值。
	 */
	@SimpleProperty
	public int 背景颜色() {
		return p背景颜色;
	}

	/**
	 * 背景颜色。
	 *
	 * @param value 新值。
	 */
	@SimpleProperty
	public void 背景颜色(int value) {
		p背景颜色 = value;
	}

	private int p背景描边宽度 = 0;

	/**
	 * 背景描边宽度，DIP。
	 *
	 * @return 当前值。
	 */
	@SimpleProperty
	public int 背景描边宽度() {
		return p背景描边宽度;
	}

	/**
	 * 背景描边宽度，DIP。
	 *
	 * @param value 新值。
	 */
	@SimpleProperty
	public void 背景描边宽度(int value) {
		p背景描边宽度 = value;
	}

	private int p背景描边颜色 = Color.BLACK;

	/**
	 * 背景描边颜色。
	 *
	 * @return 当前值。
	 */
	@SimpleProperty
	public int 背景描边颜色() {
		return p背景描边颜色;
	}

	/**
	 * 背景描边颜色。
	 *
	 * @param value 新值。
	 */
	@SimpleProperty
	public void 背景描边颜色(int value) {
		p背景描边颜色 = value;
	}

	private float p圆角横半径 = 0f;

	/**
	 * 背景横向圆角半径，DIP。
	 *
	 * @return 当前值。
	 */
	@SimpleProperty
	public float 圆角横半径() {
		return p圆角横半径;
	}

	/**
	 * 背景横向圆角半径，DIP。
	 *
	 * @param value 新值。
	 */
	@SimpleProperty
	public void 圆角横半径(float value) {
		p圆角横半径 = value;
	}

	private float p圆角纵半径 = 0f;

	/**
	 * 背景纵向圆角半径，DIP。
	 *
	 * @return 当前值。
	 */
	@SimpleProperty
	public float 圆角纵半径() {
		return p圆角纵半径;
	}

	/**
	 * 背景纵向圆角半径，DIP。
	 *
	 * @param value 新值。
	 */
	@SimpleProperty
	public void 圆角纵半径(float value) {
		p圆角纵半径 = value;
	}

	private int p横偏移 = 0;

	/**
	 * 图标横向偏移，DIP。
	 *
	 * @return 当前值。
	 */
	@SimpleProperty
	public int 横偏移() {
		return p横偏移;
	}

	/**
	 * 图标横向偏移，DIP。
	 *
	 * @param value 新值。
	 */
	@SimpleProperty
	public void 横偏移(int value) {
		p横偏移 = value;
	}

	private int p纵偏移 = 0;

	/**
	 * 图标纵向偏移，DIP。
	 *
	 * @return 当前值。
	 */
	@SimpleProperty
	public int 纵偏移() {
		return p纵偏移;
	}

	/**
	 * 图标纵向偏移，DIP。
	 *
	 * @param value 新值。
	 */
	@SimpleProperty
	public void 纵偏移(int value) {
		p纵偏移 = value;
	}

	private float p阴影半径 = 0f;

	/**
	 * 阴影半径，DIP。
	 *
	 * @return 当前值。
	 */
	@SimpleProperty
	public float 阴影半径() {
		return p阴影半径;
	}

	/**
	 * 阴影半径，DIP。
	 *
	 * @param value 新值。
	 */
	@SimpleProperty
	public void 阴影半径(float value) {
		p阴影半径 = value;
	}

	private float p阴影横偏移 = 0f;

	/**
	 * 阴影横向偏移，DIP。
	 *
	 * @return 当前值。
	 */
	@SimpleProperty
	public float 阴影横偏移() {
		return p阴影横偏移;
	}

	/**
	 * 阴影横向偏移，DIP。
	 *
	 * @param value 新值。
	 */
	@SimpleProperty
	public void 阴影横偏移(float value) {
		p阴影横偏移 = value;
	}

	private float p阴影纵偏移 = 0f;

	/**
	 * 阴影纵向偏移，DIP。
	 *
	 * @return 当前值。
	 */
	@SimpleProperty
	public float 阴影纵偏移() {
		return p阴影纵偏移;
	}

	/**
	 * 阴影纵向偏移，DIP。
	 *
	 * @param value 新值。
	 */
	@SimpleProperty
	public void 阴影纵偏移(float value) {
		p阴影纵偏移 = value;
	}

	private int p阴影颜色 = Color.BLACK;

	/**
	 * 阴影颜色。
	 *
	 * @return 当前值。
	 */
	@SimpleProperty
	public int 阴影颜色() {
		return p阴影颜色;
	}

	/**
	 * 阴影颜色。
	 *
	 * @param value 新值。
	 */
	@SimpleProperty
	public void 阴影颜色(int value) {
		p阴影颜色 = value;
	}

	private boolean p遵循字体边界 = false;

	/**
	 * 是否按照字体边界缩放。
	 *
	 * @return 当前值。
	 */
	@SimpleProperty
	public boolean 遵循字体边界() {
		return p遵循字体边界;
	}

	/**
	 * 是否按照字体边界缩放。
	 *
	 * @param value 新值。
	 */
	@SimpleProperty
	public void 遵循字体边界(boolean value) {
		p遵循字体边界 = value;
	}

	private boolean p自动镜像 = false;

	/**
	 * 是否在 RTL 布局中镜像。
	 *
	 * @return 当前值。
	 */
	@SimpleProperty
	public boolean 自动镜像() {
		return p自动镜像;
	}

	/**
	 * 是否在 RTL 布局中镜像。
	 *
	 * @param value 新值。
	 */
	@SimpleProperty
	public void 自动镜像(boolean value) {
		p自动镜像 = value;
	}

	private int p绘画方式 = 0;

	/**
	 * 0 填充，1 描边，2 填充及描边。
	 *
	 * @return 当前值。
	 */
	@SimpleProperty
	public int 绘画方式() {
		return p绘画方式;
	}

	/**
	 * 0 填充，1 描边，2 填充及描边。
	 *
	 * @param value 新值。
	 */
	@SimpleProperty
	public void 绘画方式(int value) {
		p绘画方式 = value;
	}

	private int p着色模式 = 5;

	/**
	 * Android PorterDuff 模式序号。
	 *
	 * @return 当前值。
	 */
	@SimpleProperty
	public int 着色模式() {
		return p着色模式;
	}

	/**
	 * Android PorterDuff 模式序号。
	 *
	 * @param value 新值。
	 */
	@SimpleProperty
	public void 着色模式(int value) {
		p着色模式 = value;
	}

	private 图标状态颜色 p图标状态色 = null;

	/**
	 * 图标状态色，空引用使用固定颜色。
	 *
	 * @return 当前值。
	 */
	@SimpleProperty
	public 图标状态颜色 图标状态色() {
		return p图标状态色;
	}

	/**
	 * 图标状态色，空引用使用固定颜色。
	 *
	 * @param value 新值。
	 */
	@SimpleProperty
	public void 图标状态色(图标状态颜色 value) {
		p图标状态色 = value;
	}

	private 图标状态颜色 p背景状态色 = null;

	/**
	 * 背景状态色。
	 *
	 * @return 当前值。
	 */
	@SimpleProperty
	public 图标状态颜色 背景状态色() {
		return p背景状态色;
	}

	/**
	 * 背景状态色。
	 *
	 * @param value 新值。
	 */
	@SimpleProperty
	public void 背景状态色(图标状态颜色 value) {
		p背景状态色 = value;
	}

	private 图标状态颜色 p描边状态色 = null;

	/**
	 * 图标描边状态色。
	 *
	 * @return 当前值。
	 */
	@SimpleProperty
	public 图标状态颜色 描边状态色() {
		return p描边状态色;
	}

	/**
	 * 图标描边状态色。
	 *
	 * @param value 新值。
	 */
	@SimpleProperty
	public void 描边状态色(图标状态颜色 value) {
		p描边状态色 = value;
	}

	private 图标状态颜色 p背景描边状态色 = null;

	/**
	 * 背景描边状态色。
	 *
	 * @return 当前值。
	 */
	@SimpleProperty
	public 图标状态颜色 背景描边状态色() {
		return p背景描边状态色;
	}

	/**
	 * 背景描边状态色。
	 *
	 * @param value 新值。
	 */
	@SimpleProperty
	public void 背景描边状态色(图标状态颜色 value) {
		p背景描边状态色 = value;
	}

	/**
	 * 创建独立实例。
	 */
	public 图标样式() {
	}

	/**
	 * 验证并应用样式。
	 *
	 * @param x 独立图标。
	 */
	void apply(IconicsDrawable x) {
		if (p宽度 <= 0 || p高度 <= 0 || p宽度 > 4096 || p高度 > 4096 || p透明度 < 0 || p透明度 > 255 || p内边距 < 0 || p描边宽度 < 0
				|| p背景描边宽度 < 0 || p圆角横半径 < 0 || p圆角纵半径 < 0 || p阴影半径 < 0 || p绘画方式 < 0 || p绘画方式 > 2 || p着色模式 < 0
				|| p着色模式 >= PorterDuff.Mode.values().length) {
			throw new 图标错误("图标尺寸或样式越界");
		}
		if (Support.dp(p宽度) > 4096 || Support.dp(p高度) > 4096) {
			throw new 图标错误("图标像素尺寸不能超过4096");
		}
		x.setSizeXPx(Support.dp(p宽度));
		x.setSizeYPx(Support.dp(p高度));
		x.setAlpha(p透明度);
		x.setColorList(p图标状态色 == null ? ColorStateList.valueOf(p颜色) : p图标状态色.nativeValue());
		x.setBackgroundColorList(p背景状态色 == null ? ColorStateList.valueOf(p背景颜色) : p背景状态色.nativeValue());
		x.setContourColorList(p描边状态色 == null ? ColorStateList.valueOf(p描边颜色) : p描边状态色.nativeValue());
		x.setBackgroundContourColorList(p背景描边状态色 == null ? ColorStateList.valueOf(p背景描边颜色) : p背景描边状态色.nativeValue());
		x.setContourWidthPx(Support.dp(p描边宽度));
		x.setDrawContour(p绘制描边 && p描边宽度 > 0);
		x.setBackgroundContourWidthPx(Support.dp(p背景描边宽度));
		x.setDrawBackgroundContour(p绘制背景描边 && p背景描边宽度 > 0);
		Support.padding(x, Support.dp(p内边距));
		x.setRoundedCornerRxPx(Support.dpf(p圆角横半径));
		x.setRoundedCornerRyPx(Support.dpf(p圆角纵半径));
		x.setIconOffsetXPx(Support.dp(p横偏移));
		x.setIconOffsetYPx(Support.dp(p纵偏移));
		x.setShadowRadiusPx(Support.dpf(p阴影半径));
		x.setShadowDxPx(Support.dpf(p阴影横偏移));
		x.setShadowDyPx(Support.dpf(p阴影纵偏移));
		x.setShadowColorInt(p阴影颜色);
		x.setRespectFontBounds(p遵循字体边界);
		x.setAutoMirroredCompat(p自动镜像);
		x.setStyle(Paint.Style.values()[p绘画方式]);
		x.setTintMode(PorterDuff.Mode.values()[p着色模式]);
		x.setTintList(p着色状态色 == null ? null : p着色状态色.nativeValue());
		x.setColorFilter(filter);
	}

	private 图标状态颜色 p着色状态色;
	private ColorFilter filter;

	/**
	 * 取得整体状态着色。
	 *
	 * @return 状态着色，空表示无着色。
	 */
	@SimpleProperty
	public 图标状态颜色 着色状态色() {
		return p着色状态色;
	}

	/**
	 * 设置整体状态着色。
	 *
	 * @param value 颜色，空引用清除。
	 */
	@SimpleProperty
	public void 着色状态色(图标状态颜色 value) {
		p着色状态色 = value;
	}

	/**
	 * 复制20项颜色矩阵；空数组清除滤镜。
	 *
	 * @param matrix 矩阵。
	 */
	@SimpleFunction
	public void 设置颜色矩阵(float[] matrix) {
		filter = Support.filter(matrix);
	}

	/**
	 * 清除配置中的颜色滤镜。
	 */
	@SimpleFunction
	public void 清除滤镜() {
		filter = null;
	}

	/**
	 * 从完整渲染状态读取快照，像素尺寸按当前密度换算为 DIP。
	 *
	 * @param x 原生图标。
	 * @return 样式快照。
	 */
	static 图标样式 from(IconicsDrawable x) {
		图标样式 out = new 图标样式();
		float density = Support.dpf(1);
		out.p宽度 = Math.round(x.getSizeXPx() / density);
		out.p高度 = Math.round(x.getSizeYPx() / density);
		out.p颜色 = (x.getColorList() == null ? 0 : x.getColorList().getDefaultColor());
		out.p透明度 = x.getAlpha();
		out.p内边距 = Math.round(x.getPaddingPx() / density);
		out.p描边宽度 = Math.round(x.getContourWidthPx() / density);
		out.p描边颜色 = (x.getContourColorList() == null ? 0 : x.getContourColorList().getDefaultColor());
		out.p背景颜色 = (x.getBackgroundColorList() == null ? 0 : x.getBackgroundColorList().getDefaultColor());
		out.p背景描边宽度 = Math.round(x.getBackgroundContourWidthPx() / density);
		out.p背景描边颜色 = (x.getBackgroundContourColorList() == null ? 0
				: x.getBackgroundContourColorList().getDefaultColor());
		out.p圆角横半径 = x.getRoundedCornerRxPx() / density;
		out.p圆角纵半径 = x.getRoundedCornerRyPx() / density;
		out.p横偏移 = Math.round(x.getIconOffsetXPx() / density);
		out.p纵偏移 = Math.round(x.getIconOffsetYPx() / density);
		out.p阴影半径 = x.getShadowRadiusPx() / density;
		out.p阴影横偏移 = x.getShadowDxPx() / density;
		out.p阴影纵偏移 = x.getShadowDyPx() / density;
		out.p阴影颜色 = x.getShadowColorInt();
		out.p遵循字体边界 = x.getRespectFontBounds();
		out.p自动镜像 = x.getAutoMirroredCompat();
		out.p绘画方式 = x.getStyle().ordinal();
		out.p着色模式 = x.getTintPorterMode().ordinal();
		out.p图标状态色 = 图标状态颜色.from(x.getColorList());
		out.p背景状态色 = 图标状态颜色.from(x.getBackgroundColorList());
		out.p描边状态色 = 图标状态颜色.from(x.getContourColorList());
		out.p背景描边状态色 = 图标状态颜色.from(x.getBackgroundContourColorList());
		out.p着色状态色 = 图标状态颜色.from(x.getTint());
		out.p绘制描边 = x.getDrawContour();
		out.p绘制背景描边 = x.getDrawBackgroundContour();
		out.filter = x.getIconColorFilter();
		return out;
	}

	/**
	 * 复制样式及状态颜色，不验证尚未应用的配置。
	 *
	 * @return 复制样式及状态颜色，不验证尚未应用的配置。
	 */
	@SimpleFunction
	public 图标样式 复制() {
		图标样式 out = new 图标样式();
		out.p宽度 = p宽度;
		out.p高度 = p高度;
		out.p颜色 = p颜色;
		out.p透明度 = p透明度;
		out.p内边距 = p内边距;
		out.p描边宽度 = p描边宽度;
		out.p描边颜色 = p描边颜色;
		out.p背景颜色 = p背景颜色;
		out.p背景描边宽度 = p背景描边宽度;
		out.p背景描边颜色 = p背景描边颜色;
		out.p圆角横半径 = p圆角横半径;
		out.p圆角纵半径 = p圆角纵半径;
		out.p横偏移 = p横偏移;
		out.p纵偏移 = p纵偏移;
		out.p阴影半径 = p阴影半径;
		out.p阴影横偏移 = p阴影横偏移;
		out.p阴影纵偏移 = p阴影纵偏移;
		out.p阴影颜色 = p阴影颜色;
		out.p遵循字体边界 = p遵循字体边界;
		out.p自动镜像 = p自动镜像;
		out.p绘画方式 = p绘画方式;
		out.p着色模式 = p着色模式;
		out.p图标状态色 = p图标状态色 == null ? null : 图标状态颜色.from(p图标状态色.nativeValue());
		out.p背景状态色 = p背景状态色 == null ? null : 图标状态颜色.from(p背景状态色.nativeValue());
		out.p描边状态色 = p描边状态色 == null ? null : 图标状态颜色.from(p描边状态色.nativeValue());
		out.p背景描边状态色 = p背景描边状态色 == null ? null : 图标状态颜色.from(p背景描边状态色.nativeValue());
		out.p着色状态色 = p着色状态色 == null ? null : 图标状态颜色.from(p着色状态色.nativeValue());
		out.p绘制描边 = p绘制描边;
		out.p绘制背景描边 = p绘制背景描边;
		out.filter = filter;
		return out;
	}

	private boolean p绘制描边 = true;

	/**
	 * 读取是否允许绘制描边，宽度为零时仍不绘制。
	 *
	 * @return 是否允许。
	 */
	@SimpleProperty
	public boolean 绘制描边() {
		return p绘制描边;
	}

	/**
	 * 设置是否允许绘制描边。
	 *
	 * @param enabled 是否允许。
	 */
	@SimpleProperty
	public void 绘制描边(boolean enabled) {
		p绘制描边 = enabled;
	}

	private boolean p绘制背景描边 = true;

	/**
	 * 读取是否允许绘制背景描边，宽度为零时仍不绘制。
	 *
	 * @return 是否允许。
	 */
	@SimpleProperty
	public boolean 绘制背景描边() {
		return p绘制背景描边;
	}

	/**
	 * 设置是否允许绘制背景描边。
	 *
	 * @param enabled 是否允许。
	 */
	@SimpleProperty
	public void 绘制背景描边(boolean enabled) {
		p绘制背景描边 = enabled;
	}
}
