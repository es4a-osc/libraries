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
 * 图标图像快照及输出。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class 图标图像 {
	IconicsDrawable value;
	/**
	 * 创建独立实例。
	 */
	public 图标图像() {}
	/**
	 * 验证图像。
	 */
	void check() {
		Support.main();
		if (value == null) {
			throw new 图标错误("请用图标工具创建图标");
		}
	}
	/**
	 * 复制图标及样式。
	 *
	 * @return 复制图标及样式。
	 */
	@SimpleFunction
	public 图标图像 复制() {
		check();
		图标图像 x = new 图标图像();
		x.value = Support.copy(value);
		return x;
	}

	/**
	 * 更新本图像样式，不影响已应用视图。
	 *
	 * @param style 样式。
	 */
	@SimpleFunction
	public void 应用样式(图标样式 style) {
		check();
		if (style == null) {
			throw new 图标错误("样式为空");
		}
		IconicsDrawable x = Support.copy(value);
		style.apply(x);
		value = x;
	}

	/**
	 * 应用到图片框或图片按钮。
	 *
	 * @param target 目标。
	 */
	@SimpleFunction
	public void 应用图片(可视组件 target) {
		check();
		View v = Support.view(target);
		if (!(v instanceof ImageView)) {
			throw new 图标错误("目标必须是图片视图");
		}
		Support.prepareShadow(v, value);
		((ImageView) v).setImageDrawable(Support.copy(value));
	}

	/**
	 * 应用为组件背景。
	 *
	 * @param target 目标。
	 */
	@SimpleFunction
	public void 应用背景(可视组件 target) {
		check();
		View v = Support.view(target);
		Support.prepareShadow(v, value);
		v.setBackground(Support.copy(value));
	}

	/**
	 * 设置文字附图之一并保留其他位置；0左1上2右3下4起始5结束。
	 *
	 * @param target 目标。
	 * @param position 位置。
	 */
	@SimpleFunction
	public void 应用文字附图(可视组件 target, int position) {
		check();
		View v = Support.view(target);
		if (!(v instanceof TextView)) {
			throw new 图标错误("目标必须为文本视图");
		}
		ViewIcons.set((TextView) v, position, this, false);
	}

	/**
	 * 输出透明 PNG 字节。
	 *
	 * @return 输出透明 PNG 字节。
	 */
	@SimpleFunction
	public byte[] 取PNG() {
		check();
		if (value.getIntrinsicWidth() <= 0
				|| value.getIntrinsicHeight() <= 0
				|| value.getIntrinsicWidth() > 4096
				|| value.getIntrinsicHeight() > 4096) {
			throw new 图标错误("导出尺寸须为1至4096像素");
		}
		Bitmap b = Support.copy(value).toBitmap();
		try {
			java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
			b.compress(Bitmap.CompressFormat.PNG, 100, out);
			return out.toByteArray();
		} finally {
			b.recycle();
		}
	}

	/**
	 * 保存 PNG；相对路径基于应用内部目录，父目录须存在。
	 *
	 * @param path 路径。
	 */
	@SimpleFunction
	public void 保存PNG(String path) {
		byte[] data = 取PNG();
		if (path == null || path.isEmpty()) {
			throw new 图标错误("路径为空");
		}
		java.io.File f = new java.io.File(path);
		if (!f.isAbsolute()) {
			f = new java.io.File(MainActivity.getContext().getFilesDir(), path);
		}
		try (java.io.FileOutputStream out = new java.io.FileOutputStream(f)) {
			out.write(data);
		} catch (java.io.IOException e) {
			throw new 图标错误(e.getMessage());
		}
	}

	/**
	 * 设置导出图像采用的状态。
	 *
	 * @param state 状态。
	 */
	@SimpleFunction
	public void 设置状态(int[] state) {
		check();
		if (state == null) {
			throw new 图标错误("状态为空");
		}
		value.setState(state.clone());
	}

	/**
	 * 设置整体图标着色。
	 *
	 * @param color 颜色。
	 */
	@SimpleFunction
	public void 设置着色(int color) {
		check();
		value.setTint(color);
	}

	/**
	 * 将原生字体图标转为独立快照。
	 *
	 * @param drawable 原图像。
	 * @return 快照；不是字体图标时为空。
	 */
	static 图标图像 wrap(android.graphics.drawable.Drawable drawable) {
		if (drawable == null) {
			return null;
		}
		if (!(drawable instanceof IconicsDrawable)) {
			drawable = drawable.getCurrent();
		}
		if (!(drawable instanceof IconicsDrawable)) {
			return null;
		}
		图标图像 out = new 图标图像();
		out.value = Support.copy((IconicsDrawable) drawable);
		return out;
	}

	/**
	 * 读取名称；直接字符或短文字图标返回空文本。
	 *
	 * @return 读取名称；直接字符或短文字图标返回空文本。
	 */
	@SimpleFunction
	public String 取图标名称() {
		check();
		return value.getIcon() == null ? "" : value.getIcon().getName();
	}

	/**
	 * 读取实际绘制字符或短文字。
	 *
	 * @return 读取实际绘制字符或短文字。
	 */
	@SimpleFunction
	public String 取图标文字() {
		check();
		return value.getIcon() != null
				? String.valueOf(value.getIcon().getCharacter())
				: (value.getIconText() == null ? "" : value.getIconText());
	}

	/**
	 * 设置整体状态着色；空引用清除着色。
	 *
	 * @param colors 颜色。
	 */
	@SimpleFunction
	public void 设置状态着色(图标状态颜色 colors) {
		check();
		value.setTintList(colors == null ? null : colors.nativeValue());
	}

	/**
	 * 清除整体着色，恢复图标本身颜色。
	 */
	@SimpleFunction
	public void 清除着色() {
		check();
		value.setTintList(null);
	}

	/**
	 * 设置20项颜色矩阵；空数组清除滤镜。
	 *
	 * @param matrix 矩阵。
	 */
	@SimpleFunction
	public void 设置颜色矩阵(float[] matrix) {
		check();
		value.setColorFilter(Support.filter(matrix));
	}

	/**
	 * 清除颜色滤镜。
	 */
	@SimpleFunction
	public void 清除滤镜() {
		check();
		value.clearColorFilter();
	}

	/**
	 * 读取完整样式快照；尺寸按当前密度换算为DIP。
	 *
	 * @return 读取完整样式快照；尺寸按当前密度换算为DIP。
	 */
	@SimpleFunction
	public 图标样式 取得样式() {
		check();
		return 图标样式.from(value);
	}

	/**
	 * 读取图像状态数组副本。
	 *
	 * @return 读取图像状态数组副本。
	 */
	@SimpleFunction
	public int[] 取状态() {
		check();
		return value.getState().clone();
	}

	/**
	 * 读取实际渲染宽度，物理像素。
	 *
	 * @return 读取实际渲染宽度，物理像素。
	 */
	@SimpleFunction
	public int 取像素宽度() {
		check();
		return value.getIntrinsicWidth();
	}

	/**
	 * 读取实际渲染高度，物理像素。
	 *
	 * @return 读取实际渲染高度，物理像素。
	 */
	@SimpleFunction
	public int 取像素高度() {
		check();
		return value.getIntrinsicHeight();
	}

	/**
	 * 查询是否设置颜色滤镜。
	 *
	 * @return 查询是否设置颜色滤镜。
	 */
	@SimpleFunction
	public boolean 是否有滤镜() {
		check();
		return value.getIconColorFilter() != null;
	}
}
